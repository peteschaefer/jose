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
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.comm.msg.MessageProducer;
import de.jose.db.JoConnection;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.window.JoFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 *	a pane that is either
 *		docked: child of the main frame
 *	or
 *		undocked: contained in a separate frame
 */

public class JoPanel
		extends JPanel
		implements JoComponent, ActionListener, CommandListener
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

//	protected static final boolean DOUBLE_BUFFERING = true;
	
	/**	maps panel names to implementing classes
	 */
	protected static final HashMap<String,Class<? extends JoPanel>> CLASS_MAP = new HashMap<>();
	
	static {
		CLASS_MAP.put("window.board", 		    de.jose.view.MainBoardPanel.class);
		CLASS_MAP.put("window.list", 		    de.jose.view.ListPanel.class);
		CLASS_MAP.put("window.clock", 		    de.jose.view.ClockPanel.class);
		CLASS_MAP.put("window.game", 		    de.jose.view.DocumentPanel.class);
		CLASS_MAP.put("window.toolbar.1", 	    de.jose.view.JoToolBar.class);
		CLASS_MAP.put("window.toolbar.2", 	    de.jose.view.JoToolBar.class);
		CLASS_MAP.put("window.toolbar.3", 	    de.jose.view.JoToolBar.class);
        CLASS_MAP.put("window.toolbar.symbols", de.jose.view.SymbolBar.class);
		CLASS_MAP.put("window.console", 	    de.jose.view.ConsolePanel.class);
		CLASS_MAP.put("window.gamelist", 	    de.jose.view.ListPanel.class);
		CLASS_MAP.put("window.collectionlist", 	de.jose.view.CollectionPanel.class);
		CLASS_MAP.put("window.query", 		    de.jose.view.QueryPanel.class);
		CLASS_MAP.put("window.sqlquery", 	    de.jose.view.SQLQueryPanel.class);
		CLASS_MAP.put("window.sqllist", 	    de.jose.view.SQLResultPanel.class);
		CLASS_MAP.put("window.engine", 		    de.jose.view.EnginePanel.class);
		CLASS_MAP.put("window.eval",            de.jose.view.EvalPanel.class);
		CLASS_MAP.put("window.print.preview",   de.jose.window.PrintPreviewDialog.class);
	}

	public static String[] panelNames()
	{
		String[] result = new String[CLASS_MAP.size()];
		return CLASS_MAP.keySet().toArray(result);
	}

	public static Class<? extends JoPanel> classFor(String name) {
		return CLASS_MAP.get(name);
	}


	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**
	 * static hash map containing all open panels
	 * (and unused LayoutProfiles)
	 */
	public static final HashMap<String,JoPanel> thePanels = new HashMap<>();
	
	/**	info for hidden panels
	 */
	protected Dimension defaultSize;
	protected boolean inited = false;
	protected boolean showContextMenu;
    protected boolean showControls;

    /** priority to gain keyboard focus */
    protected int focusPriority = 0;
	protected int titlePriority = 0;

	protected LayoutProfile profile;

	/** can be used to transmit messages
	 *  (inited on demand)
	 *  */
	protected MessageProducer messageProducer;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public JoPanel(LayoutProfile prf, boolean witContextMenu, boolean withBorder)
	{
		super();
		profile = prf;

		setName(profile.name);
		showContextMenu = witContextMenu;
        showControls = withBorder;
		thePanels.put(profile.name,this);

		setMinimumSize(new Dimension(20,20));
		defaultSize = new Dimension(200,200);
        messageProducer = null; //  inited by getMessageProducer()

		setDoubleBuffered(Version.useDoubleBuffer());

        if (AbstractApplication.theCommandDispatcher != null)
		    AbstractApplication.theCommandDispatcher.addCommandListener(this);
	}

	public void setVisible(boolean visible)
	{
        Command cmd = new Command(visible ? "panel.isShown":"panel.isHidden",null,this);
        Application.theApplication.broadcast(cmd);

        super.setVisible(visible);

		if (!isVisible() || inited)
			return;	//	already done

		cmd = new Command("panel.init",null,this);
		if (this instanceof IDBPanel) {
			//	defer initialisation until we have a db connection
			JoConnection.postWithConnection(cmd);
		}
		else {
			Application.theCommandDispatcher.handle(cmd, Application.theApplication);
		}
	}

	public void doInit() throws Exception {
		if (!inited) {
			init();
			inited = true;
			postInit();
		}
	}

	@Override
    public boolean showControls()
    {
        return showControls;
    }


	/**	may be overwritten
	 *	to perform initialisation when panel is show for the first time
	 */
	public void init()
		throws Exception
	{	}

	/**
	 * to perform initialisation just after the panel has become visible
	 */
	public void postInit()
		throws Exception
	{	}

	public boolean isInited()       { return inited; }

	public static JoPanel create(LayoutProfile profile, boolean withContextMenu)
	{
        JoPanel result = get(profile.name);
        if (result != null)
            return result;

        try {
//            String className = (String)CLASS_MAP.get(name);
            Class<? extends JoPanel> clazz = classFor(profile.name);
            return create(clazz, profile,withContextMenu,false);
        } catch (Exception ex) {
            AbstractApplication.error(ex);
            throw new RuntimeException("could not create panel");
        }
    }

    public static JoPanel create(Class<? extends JoPanel> clazz, LayoutProfile profile,
                                 boolean withContextMenu, boolean withBorder)
    {
		try {
			if (clazz == null) clazz = JoPanel.class;
			
			JoPanel result = instantiate(clazz,profile,withContextMenu,withBorder);
			
			if (profile!=null && profile.size!=null)
				result.setSize(profile.size);
			else
				result.setSize(result.getDefaultSize());
			
			result.profile = profile;
			return result;
		} catch (Exception ex) {
			AbstractApplication.error(ex);
			throw new RuntimeException("could not create panel");
		}
	}

	
	protected static JoPanel instantiate(Class<? extends JoPanel> clazz, LayoutProfile profile, boolean withContextMenu, boolean withBorder)
		throws NoSuchMethodException, InstantiationException, 
			   IllegalAccessException, InvocationTargetException
	{
		Class<?>[] paramClasses = { LayoutProfile.class, boolean.class, boolean.class, };
		Object[] paramObjects = { profile, withContextMenu, withBorder };
		
		Constructor<?> ctor = clazz.getConstructor(paramClasses);
		return (JoPanel)ctor.newInstance(paramObjects);
	}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------


	public LayoutProfile getProfile()
	{
		return profile;
	}

	public JoFrame getParentFrame()
	{
        Container parent = getTopLevelAncestor();
        if (parent instanceof JoFrame)
		    return (JoFrame)parent;
        else
            return null;
	}
	
	public boolean isDocked()
	{
		return (getParent() instanceof JoSplitPane);
	}

	public boolean canDock()
	{
		return getProfile().dockFrame!=null;
	}

	public boolean isInFront()
	{
		JoFrame frame = getParentFrame();
		if (frame!=null)
			return frame==JoFrame.getActiveFrame();
		/// note that frame.isActive() won't suffice if a dialog is in front
		else
			return false;
	}

	public static JoPanel get(String name)
	{
		return thePanels.get(name);
	}
	
	public static Collection<JoPanel> getAllPanels()
	{
		return thePanels.values();
	}
	
	public static boolean exists(String name)
	{
		return thePanels.containsKey(name);
	}
	
	public static boolean isVisible(String name)
	{
		JoPanel p = get(name);
		return p!=null && p.isVisible();
	}
	
	public static boolean isShowing(String name)
	{
		JoPanel p = get(name);
		return p!=null && p.isShowing();
	}
	
	public Dimension getDefaultSize() {
		return defaultSize;
	}
	
	public static Insets getFrameInsets(Component comp, Frame frm) {
		Rectangle r = new Rectangle(0,0,comp.getWidth(),comp.getHeight());
		r = ViewUtil.localRect(r, comp, frm);
		
		return new Insets(r.y, r.x, 
						  frm.getHeight()-r.height-r.y,
						  frm.getWidth()-r.width-r.x);
	}

    /** priority to gain keyboard focus */
    public int getFocusPriority() {
        return focusPriority;
    }

	/** priority to appear in window title */
	public int getTitlePriority() {
		return titlePriority;
	}

    /** the component that gets the focus (could be this component, or a child)  */
    public Component getFocusComponent()
    {
        return getFocusableChild(this);
    }

    public boolean isFocusInside()
    {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

        return (focus==this) || this.isAncestorOf(focus);
    }

    protected Component getFocusableChild(Component parent)
    {
        if (parent.isFocusable())
            return parent;

        if (parent instanceof Container)
        {
            Container cparent = (Container)parent;
            int count = cparent.getComponentCount();
            for (int i=0; i < count; i++) {
                Component child = cparent.getComponent(i);
                Component result = getFocusableChild(child);
                if (result!=null) return result;
            }
        }
        return null;
    }
/*
	public static JoPanel getPanel(Component comp)
	{
		for ( ; comp != null; comp = comp.getParent())
			if (comp instanceof JoPanel)
				return (JoPanel)comp;
		return null;
	}
*/
	/**	for testing	 */
/*	protected void paintComponent(Graphics g)
	{
		Rectangle bounds = getBounds();
		g.setColor(Color.white);
		g.fillRoundRect(0,0, bounds.width, bounds.height, 10, 10);
		
		g.setColor(Color.black);
		g.drawRoundRect(0,0, bounds.width, bounds.height, 10, 10);
		
		String text = "\""+getName()+"\""+
					  bounds.x+","+bounds.y+","+bounds.width+","+bounds.height;
		g.drawString(text, 10, 20);
	}
*/

    //-------------------------------------------------------------------------------
    //	MessageProducer
    //-------------------------------------------------------------------------------

	public MessageProducer getMessageProducer()
	{
		if (messageProducer==null)
			messageProducer = new MessageProducer(this);
		return messageProducer;
	}
	
	//-------------------------------------------------------------------------------
	//	interface JoComponent
	//-------------------------------------------------------------------------------
	
	public boolean isContinuousLayout()
	{
		return true;
	}

	public void startContinuousResize()
	{
		/* JSplitPane starts resizing
		 *  derived classes me intercept
		 */
	}

	public void finishContinuousResize()
	{
		/* JSplitPane finished resizing
		 *  derived classes me intercept
		 */
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
	}

	public Dimension getMaximumSize(int orientation) {
		Dimension dim = getMaximumSize();
        if (dim.width<=0) dim.width = Integer.MAX_VALUE;
        if (dim.height<=0) dim.height = Integer.MAX_VALUE;
        return dim;
	}

	public boolean showContextMenu()		{ return showContextMenu; }

	@Override
	public void adjustContextMenu(Collection<Object> list, MouseEvent event)
	{
		list.add("panel.hide");

		list.add("panel.move");
		list.add(event);

		if (isDocked())
			list.add("panel.undock");
		else if (canDock())
            list.add("panel.dock");
	}

	public float getWeightX() {
		return profile.weightX;
	}

	public float getWeightY() {
		return profile.weightY;
	}

	
	//-------------------------------------------------------------------------------
	//	interfac ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		/*	forward menu events to CommandDispatcher	*/
		AbstractApplication.theCommandDispatcher.handle(e, this);
	}
	

	public void calcLocation() {
		if (isShowing()) {
			profile.visibleLocation = getLocationOnScreen();
			profile.size = getSize();
		}
		JoFrame frame = getParentFrame();
//		if (canDock() && frame!=null)
//			profile.dockingPath = frame.getDockingPath(this);
		//  this doesn't seem to be correct; dockingPath refers to the dockingFrame NOT the current frame
	}
	
	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public CommandListener getCommandParent()
	{
        Container parent = getParent();
        if (parent instanceof CommandListener)
		    return (CommandListener)parent;	//	probably a JoSplitPane
        else
            return getParentFrame();
	}

	public int numCommandChildren()
	{
		/*	maybe we could broadcast commands to child elements
		 * 	but this is not needed, yet
		 */
		return 0;
	}

	public CommandListener getCommandChild(int i)
	{
		return null;
	}

	public void hidePanel()
	{
		if (isDocked()) {
			calcLocation();
			setVisible(false);
			getParentFrame().remove(JoPanel.this);
		}
		else {
			setVisible(false);
			//	close frame
			Command cmd = new Command("menu.file.close",null);
			AbstractApplication.theCommandDispatcher.forward(cmd, getParentFrame());
		}
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		/*	DEFAULT command handlers	*/

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				hidePanel();
			}
		};
		map.put("panel.hide", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				calcLocation();
				Application.theApplication.getContextMenu().startDragging(JoPanel.this,
				(MouseEvent)cmd.data);
//                getParentFrame().setSize(1280,1024);
			}
		};
		map.put("panel.move", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				if (cmd.data!=null)
					profile.visibleLocation = (Point)cmd.data;
				else
					profile.visibleLocation = getLocationOnScreen();
				profile.size = getSize();

				JoFrame frm = getParentFrame();
				profile.dockFrame = frm.getProfile();
				profile.dockingPath = frm.getDockingPath(JoPanel.this);

				Insets ins = null;
				if (frm!=null) {
					ins = getFrameInsets(frm.getMainComponent(), frm);
					frm.remove(JoPanel.this);
				}

				setVisible(true);

				JoFrame frame = new JoFrame(JoPanel.this, ins);
				frame.setComponentsVisible(true);
				frame.setVisible(true);
				frame.getProfile();
				profile.frameProfile = frame.getProfile();
			}
		};
		map.put("panel.undock", action);

		action = new CommandAction() {
			public void Do(Command cmd)
			{
				if (profile.dockFrame!=null) {
					JoFrame oldFrame = getParentFrame();
					JoFrame newFrame = JoFrame.getFrame(profile.dockFrame);
					if (newFrame==null)
						newFrame = new JoFrame(profile.dockFrame);

					JoFrame.dock(JoPanel.this, newFrame, profile.dockingPath);

					oldFrame.dispose();
					/*	panel in frame: show	*/
					newFrame.setVisible(true);
					newFrame.toFront();
				}
			}
		};
		map.put("panel.dock", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				updateLanguage();
			}
		};
		map.put("update.language",action);
	}


	public void updateLanguage()
	{
		/* overwrite   */
	}


	public String getDockingSpots()	{ return DOCK_ALL; }
	
	public Point getDockingSpot(char location) {
	//	Point p = new Point(getWidth(),getHeight());
		Rectangle r = getBounds();
		Point p = new Point(r.x, r.y);
		
		switch (location) {
		case DOCK_NORTH:
		case DOCK_SOUTH:
		case DOCK_CENTER:	p.x+=r.width/2; break;
		case DOCK_EAST:		p.x+=r.width; break;
		}

		switch (location) {
		case DOCK_SOUTH:	p.y+=r.height; break;
		case DOCK_EAST:
		case DOCK_WEST:
		case DOCK_CENTER:	p.y+=r.height/2; break;
		}

		return p;
	}

	public static void updateAllPanels(UserProfile prof)
	{
		Iterator i = getAllPanels().iterator();
		while (i.hasNext()) {
			JoPanel panel = (JoPanel)i.next();
			panel.profile = prof.getPanelProfile(panel.getName());
		}
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		//  implements ClipboardOwner
	}
}
