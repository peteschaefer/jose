/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view;

import de.jose.*;
import de.jose.comm.Command;
import de.jose.comm.CommandListener;
import de.jose.window.JoFrame;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class ContextMenu
		extends JPopupMenu
		implements AWTEventListener
{

	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	public static final String SEPARATOR =	null;
	
	/**	source component	 */
	protected Component source;
	
	/**	is docking enabled ?	 */
	protected boolean dockDragging;
	
	/**	Vector of DockingZone	*/
	protected Vector zones;
	/**	currently activated zone	 */
	protected DockingZone currentZone;
	protected DockingZone undockZone;
	protected Point mouseStart;
	/**	current panel controls	*/
//	protected PanelControls controls;

	/**
	 *	overwrites JPopupMeu.isPopupTrigger
	 *	@return true is the mouse is event should popup the context menu
	 */
	public static boolean isTrigger(MouseEvent e)
	{
//		return e.isPopupTrigger();	//	doesn't work as expected :-( why ?
        if (Version.mac)
        {
            return e.isPopupTrigger();
        }
        else
        {
            //  TODO why don't we use e.isPopupTrigger ? Test on Windows, Linux
            int mods = e.getModifiers();
            return (e.getID()==MouseEvent.MOUSE_RELEASED)
                && (Util.allOf(mods,MouseEvent.BUTTON3_MASK));
			     /*Util.allOf(mods,MouseEvent.ALT_MASK)*/
            //  note that ALT_MASK is identical to BUTTON2_MASK (why?)
            //  but we want BUTTON2 (=mouse wheel) for other purposes
        }
	}
	
	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public ContextMenu()
	{
		super();

		dockDragging = false;
		listenTo(AWTEvent.MOUSE_EVENT_MASK);
		zones = new Vector();
	}

	public boolean showMenu(MouseEvent event)
	{
		source = findSource(event);
//		source = getInvoker();
		if (source==null) return false;
		
		/*	result value if garuanteed to implement ContextMenu.Enabled	
			so the next cast is OK
		*/
		JoComponent jocomp = (JoComponent)source;
		
//		setVisible(false);
		
        final Point where = ViewUtil.localPoint(event,source);
		Vector items = new Vector();
		jocomp.adjustContextMenu(items,event);
		
		removeAll();


		JoMenuBar.addItems(this, items.iterator(), null);

		if (source instanceof ActionListener)
			JoMenuBar.addMenuItemListener(this, (ActionListener)source);
		else
			JoMenuBar.addMenuItemListener(this, Application.theApplication);		

		if (source instanceof CommandListener)
			JoMenuBar.adjustMenu(this,(CommandListener)source, false);
		else
			JoMenuBar.adjustMenu(this,Application.theApplication, false);

        /**
         * make sure that the menu is shown after all events are handled
         * otherwise the same mouse event could be fed to the menu, and
         * cause it to close immediately
         */
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                show(source,where.x,where.y);
            }
        });

//		setCursor(Cursor.getDefaultCursor());
		return true;
	}


	/**	@return the Component the event occurred in, AND where the context menu is enabled
	 */
	private Component findSource(MouseEvent event)
	{
		Component comp = (Component)event.getSource();
		if (comp instanceof JoComponent &&
			((JoComponent)comp).showContextMenu())
			return comp;
		
		if (comp instanceof Container) {
			Component child = ((Container)comp).findComponentAt(event.getPoint());
			for ( ; child != null; child = child.getParent())
				if (child instanceof JoComponent &&
					((JoComponent)child).showContextMenu())
					return child;
		}
		
		for (Container parent = comp.getParent(); parent != null; parent = parent.getParent())
			if (parent.isShowing()) {
				Component child = parent.getComponentAt(ViewUtil.localPoint(event,parent));	//	not recursive !
				if (child instanceof JoComponent &&
					((JoComponent)child).showContextMenu())
					return child;
			}
		
		return null;
	}

	protected void createDockingZones()
	{
		zones.clear();
		currentZone = null;

		for (int i=0; i < JoFrame.countFrames(); i++)
		{
			JoFrame frame = JoFrame.getFrame(i);
			if ((frame!=null) && frame.isShowing())
				createDockingZones(frame);
		}

		/**	make a zone for the source panel	*/
		Container sourceGlassPane = (Container)((JoComponent)source).getParentFrame().getGlassPane();
		zones.add(0, new DockingZone(source,'0', sourceGlassPane, source, mouseStart,
		        DockingZone.SHADOW_64, Language.get("panel.orig.pos")));

		undockZone = new DockingZone(source,'0',sourceGlassPane, source, mouseStart,
		        DockingZone.SHADOW_64, Language.get("panel.undock.here"));
	}
	
	protected void hideDockingZones()
	{
		for (int i=0; i < JoFrame.countFrames(); i++)
		{
			JoFrame frame = JoFrame.getFrame(i);
			if (frame!=null) {
				frame.setDefaultGlassPane();
				frame.getGlassPane().setVisible(false);
			}
		}
//System.err.println("hide "+frames[i].getTitle());
		zones.clear();
	}
	
	protected void createDockingZones(JoFrame frame)
	{
//System.err.println("create "+frame.getTitle());
		JPanel glassPane = new JPanel(null);
		Rectangle bounds = frame.getContentPane().getBounds();
		glassPane.setBounds(bounds);
		glassPane.setOpaque(false);
		frame.setGlassPane(glassPane);
		glassPane.setVisible(true);
		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
		createDockingZones(frame.getMainComponent(), glassPane);
	}
	
	protected void createDockingZones(Component comp, Container glassPane)
	{
		if (!comp.isShowing() || comp==source) return;
			//	don't create docking zones for the source panel
		if (comp instanceof JoComponent) {
			JoComponent jocomp = (JoComponent)comp;
			String where = jocomp.getDockingSpots();
			for (int i=0; i<where.length(); i++) {
				DockingZone zone = new DockingZone(comp, where.charAt(i), glassPane,
				        source, mouseStart, DockingZone.RED_64, Language.get("panel.dock.here"));
				//zone.setVisible(false);
				zones.add(zone);
			}
		}
		
		if (comp instanceof JoSplitPane) {
			JoSplitPane sp = (JoSplitPane)comp;
			createDockingZones(sp.firstComponent(), glassPane);
			createDockingZones(sp.secondComponent(), glassPane);
		}
	}
	
	protected void setCurrentZone(DockingZone z, MouseEvent mouseEvent)
	{
		if (currentZone!=null && currentZone!=z) {
			currentZone.setActive(false);
			currentZone=null;
		}

		if (z!=currentZone && z!=null) {
			currentZone = z;
			currentZone.setActive(true);
		}

		if (z==null && mouseEvent!=null) {
			currentZone = undockZone;
			undockZone.moveTo(ViewUtil.globalPoint(mouseEvent));
			undockZone.setActive(true);
		}
	}
	
	protected DockingZone findCurrentZone(Point p) {
		DockingZone result = null;
		double minDistance = 900;   //  min distance to hot spot
		for (int i=0; i<zones.size(); i++) {
			DockingZone z = (DockingZone)zones.get(i);
			if (z.containsGlobal(p)) {
				double dist = z.hotSpotDistance(p);
				if (dist < minDistance) {
					minDistance = dist;
					result = z;
				}
			}
		}
		return result;
	}
	
	//-------------------------------------------------------------------------------
	//	interface AWTEventListener
	//-------------------------------------------------------------------------------

	private void listenTo(long eventMask) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		tk.removeAWTEventListener(this);
		tk.addAWTEventListener(this,eventMask);
	}
	
	public void startDragging(Component src, MouseEvent event)
	{
		dockDragging = true;
		listenTo(AWTEvent.MOUSE_EVENT_MASK + AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.KEY_EVENT_MASK);
		source = src;
		mouseStart = ViewUtil.globalPoint(event);

		createDockingZones();
	}
	
	public void stopDragging(boolean move)
	{
		dockDragging = false;
		listenTo(AWTEvent.MOUSE_EVENT_MASK);
				
		hideDockingZones();
		if (currentZone==undockZone) {
			if (((JoPanel)source).isDocked()) {
				//	undock panel
				Point offset = undockZone.getOffsetFrom(mouseStart);
				Point location = source.getLocationOnScreen();
				location.translate(offset.x,offset.y);

				Command cmd = new Command("panel.undock",null, location);
				Application.theCommandDispatcher.forward(cmd, (JoPanel)source);
			}
			else {
				//	move frame
				Point offset = undockZone.getOffsetFrom(mouseStart);
				JFrame frame = ((JoPanel)source).getParentFrame();
				Point location = frame.getLocation();
				frame.setLocation(location.x+offset.x, location.y+offset.y);
				frame.toFront();
			}
		}
		else if (currentZone != null &&
			currentZone.target!=source && move)
		{
			//	dock panel to new location
			JoFrame frm = JoFrame.dock((JoPanel)source,
									   currentZone.target, 
									   currentZone.orientation,
									   currentZone.getSize());
			frm.setVisible(true);
			frm.toFront();
		}
				
		setCurrentZone(null,null);
		zones.clear();
	}
	
	public void eventDispatched(AWTEvent event)
	{
		switch (event.getID()) {
		case MouseEvent.MOUSE_MOVED:
			MouseEvent mevent = (MouseEvent)event;
			if (dockDragging) {
				DockingZone z = findCurrentZone(ViewUtil.globalPoint(mevent));
				setCurrentZone(z, (MouseEvent)event);
			}
			return;

		case MouseEvent.MOUSE_DRAGGED:
			return;

		case MouseEvent.MOUSE_EXITED:
			if (dockDragging)
				;
/*			else
				attachControls(null);
*/			return;

		case MouseEvent.MOUSE_ENTERED:
			if (dockDragging)
				;
/*			else{
				Component comp = (Component)event.getSource();
				attachControls(comp);
			}
*/			return;

		case MouseEvent.MOUSE_CLICKED:
			return;

		case MouseEvent.MOUSE_PRESSED:
			if (dockDragging)
                /* wait for mouse released */ ;
            else if (isTrigger((MouseEvent)event) && showMenu((MouseEvent)event))
            {
				listenTo(AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK);
				return;
			}
			break;

		case MouseEvent.MOUSE_RELEASED:
			if (dockDragging)
				stopDragging(!isTrigger((MouseEvent)event));
			else if (isTrigger((MouseEvent)event) && showMenu((MouseEvent)event))
            {
				listenTo(AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK);
				return;
			}
			break;

		case KeyEvent.KEY_PRESSED:
			if (dockDragging)
				stopDragging(false);
            //  any key stops dragging
			break;
		}

		if (isVisible()) {
//			setVisible(false);
			listenTo(AWTEvent.MOUSE_EVENT_MASK);
		}
	}


}
