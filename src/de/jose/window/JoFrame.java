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

package de.jose.window;

import de.jose.*;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.profile.FrameProfile;
import de.jose.profile.LayoutProfile;
import de.jose.util.ref.IntRef;
import de.jose.util.ref.ObjectRef;
import de.jose.view.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.Vector;

/**
 *	a window outside the main frame
 *	containing a single, undocked pane
 */

public class JoFrame
		extends JFrame
		implements ActionListener, CommandListener, ComponentListener, WindowListener
{
	//-------------------------------------------------------------------------------
	//	Static Fields
	//-------------------------------------------------------------------------------

	/**
	 * contains all open frames (Vector of JoFrame)
	 */
	public static Vector theFrames = new Vector();

	/**
	 * the currently active frame
	 */
	public static JoFrame theActiveFrame;

	/**
	 * the current fullscreen window (may be null)
	 */
	public static Window theFullScreenWindow = null;


	/**	state constants	 */
	public static final int VISIBLE		= 0x1000;
	public static final int ICONIFIED	= Frame.ICONIFIED;
	/*	jdk 1.4:			MAXIMIZED	= Frame.MAXIMIZED_BOTH	*/
	public static final int FULLSCREEN	= 0x08;


	protected static final int	PANE_CONTROL_LAYER = JLayeredPane.MODAL_LAYER.intValue();

	private FrameProfile	profile;

	/**
	 * original glass pane
	 */
	public Container defaultGlassPane;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	private JoFrame()
	{
		super();
        setIconImage(Application.theApplication.theIconImage);
        setTitle(Language.get("application.name"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   //  we will handle ourselves

		Application.theApplication.splashToFront();

		theFrames.add(this);

		addComponentListener(this);
		addWindowListener(this);

		JoMenuBar menubar = new JoMenuBar(this);
		setJMenuBar(menubar);
//		JoMenuBar.addMenuItemListener(menubar, Application.theApplication);


		defaultGlassPane = (Container)getGlassPane();
/*
		PanelControls controls = new PanelControls();
		getRootPane().getLayeredPane().add(controls);
		getRootPane().getLayeredPane().setLayer(controls, PANE_CONTROL_LAYER);
*/
		AbstractApplication.theCommandDispatcher.addCommandListener(this);

	}


	public JoFrame(FrameProfile fpf)
	{
		this();
		profile = fpf;

		if (isMaximized(fpf.state))
		{
			//	workaround for 1.4: maximizing works only when the frame is visible
			setBounds(adjustBounds(fpf.userBounds,false));		//	normal bounds
			super.setVisible(true);
			setExtendedState(fpf.state & ~FULLSCREEN);  //  don't start in "fullscreen" mode
			if (!isVisible(fpf.state))
				super.setVisible(false);	//	strange but that's the way it is ;-)
		}
		else {
			//	jdk 1.3: maximizing doesn't work at all
			setBounds(adjustBounds(fpf.bounds,true));
			setExtendedFrameState(fpf.state & ~FULLSCREEN);  //  don't start in "fullscreen" mode
		}

		if ((fpf.state & MAXIMIZED_BOTH) != 0) {
			setBounds(adjustBounds(fpf.userBounds,false));
			//	maximize !	(but how ?)
			/*	maximize !  (but how ?)
				simply setting the bounds is not enough 'cause the window remains in user state
			*/
			setExtendedState(MAXIMIZED_BOTH);
		}
		else {
			setBounds(adjustBounds(fpf.bounds,false));
		}

		Component comp = (Component)createComponent(fpf.componentLayout,null);
		getContentPane().add(comp);
		comp.setSize(getSize());
		
        Component focus = getPreferredFocus();

		updateLanguage();

		if (focus != null) {
		    focus.requestFocus();
			focus.requestFocusInWindow();
		}

		Application.theApplication.splashToFront();
	}

	public JoFrame(JoPanel panel, Insets insets)
	{
		this();
		Rectangle bounds = new Rectangle(new Point(40,40), panel.getSize());
		if (panel.getProfile().visibleLocation != null)
			bounds.setLocation(panel.getProfile().visibleLocation);

		if (insets!=null) {
			bounds.x -= insets.left;
			bounds.y -= insets.top;
			bounds.width += insets.left+insets.right;
			bounds.height += insets.top+insets.bottom;
		}

		setBounds(bounds);
		getContentPane().add(panel);
		updateLanguage();
		panel.setVisible(true);

		Component focus = getPreferredFocus();
		if (focus != null) {
			focus.requestFocus();
			focus.requestFocusInWindow();
		}

		setExtendedFrameState(VISIBLE);

		profile = new FrameProfile(getBounds(),VISIBLE, LayoutProfile.create(getMainComponent()));
		Application.theUserProfile.addFrameProfile(profile);
	}


	public void updateProfile()
	{
		profile.bounds = getBounds();
		profile.componentLayout = LayoutProfile.create(getMainComponent());
		profile.state = getExtendedFrameState();
	}

	public FrameProfile getProfile()
	{
		return profile;
	}

	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
/*		if (!visible || SwingUtilities.isEventDispatchThread())
			super.setVisible(visible);
		else
			SwingUtilities.invokeLater(new Runnable() {
				public void run() { JoFrame.super.setVisible(true); }
			});
*/
		/**	TODO this method deadlocks sometimes - that's why we use the invokeLater stuff	*/
	}


	/**
	 */
	public void setExtendedFrameState(int state)
	{
		if (Util.anyOf(state,FULLSCREEN))
			setFullScreen(true);
		else
			super.setExtendedState(Util.minus(state,FULLSCREEN+VISIBLE));	//	new in JDK 1.4
	}

	/**
	 */
	public int getExtendedFrameState()
	{
		int result;
		if (isFullScreen())
			result = FULLSCREEN+VISIBLE;
		else
			result = super.getExtendedState();	//	new in JDK 1.4

		if (isVisible())
			return result |= VISIBLE;

		return result;
	}

	public PanelControls getPanelControls()
	{
		JLayeredPane pane = getRootPane().getLayeredPane();
		Component[] comps = pane.getComponentsInLayer(PANE_CONTROL_LAYER);
		for (int i = comps.length-1; i >= 0; i--)
			if (comps[i] instanceof PanelControls)
				return (PanelControls)comps[i];
		throw new IllegalStateException("panel controls not found");
	}

    public static Rectangle adjustBounds(Rectangle r, boolean shrinkToFit)
    {
		Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		if (r.x < 0) r.x += screenBounds.x+screenBounds.width;
		if (r.y < 0) r.y += screenBounds.y+screenBounds.height;

		if (r.x < screenBounds.x)	r.x = screenBounds.x;
		if (r.y < screenBounds.y)	r.y = screenBounds.y;

        if (r.width < 0) r.width = screenBounds.width+r.width-r.x;
        if (r.height < 0) r.height = screenBounds.height+r.height-r.y;

		if (r.x > (screenBounds.x+screenBounds.width-80)) r.x = screenBounds.x+screenBounds.width-r.width;
		if (r.y > (screenBounds.y+screenBounds.height-80)) r.y = screenBounds.y+screenBounds.height-r.height;

		if ((r.x+r.width) > (screenBounds.x+screenBounds.width)) {
			if (shrinkToFit)
				r.width = screenBounds.width-r.x;
			else
				r.x = Math.max(screenBounds.x,	screenBounds.x+screenBounds.width-r.width);
		}
		if ((r.y+r.height) > (screenBounds.y+screenBounds.height)) {
			if (shrinkToFit)
				r.height = screenBounds.height-r.y;
			else
				r.y = Math.max(screenBounds.y, screenBounds.y+screenBounds.height-r.height);
		}

        return r;
    }

    public void setBounds(Rectangle r)
    {
        super.setBounds(adjustBounds(r,true));
    }

	public boolean isMaximized()
	{
		return isMaximized(getExtendedFrameState());
	}

	public static boolean isMaximized(int state)
	{
		return Util.anyOf(state, MAXIMIZED_BOTH);
	}

	public static boolean isVisible(int state)
	{
		return Util.anyOf(state, VISIBLE);
	}

	public boolean isFullScreen()
	{
		return this == theFullScreenWindow;
	}

	public void setFullScreen(boolean on)
	{
		if (on==isFullScreen())	return;		// no change

		if (on) {
			if (canFullScreen()) {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				gd.setFullScreenWindow(this);
			}
			else {
				//	use maximized window instead
				setExtendedFrameState(MAXIMIZED_BOTH+VISIBLE);
			}
			theFullScreenWindow = this;
		}
		else {
			if (canFullScreen()) {
				GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				gd.setFullScreenWindow(null);
			}
			else {
				//	return to normal size
				setExtendedFrameState(VISIBLE);
			}
			theFullScreenWindow = null;
		}
	}

	public boolean toggleFullScreen()
	{
		boolean newState = !isFullScreen();
		setFullScreen(newState);
		return newState;
	}

	public static boolean canFullScreen()
	{
		/**
		 * full screen mode poses a lot of problems:
		 * for example we can not display dialog windows or (heavy weight) menus;
		 * on the other hand we HAVE to use heavy weight menus for 3D windows ;-(
		 * that's why Full Screen mode is currently disabled
		 *
		 * the user can maximize the window instead
		 */
/*		if (Version.java14) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			return gd.isFullScreenSupported();
		}
*/		//	no full screen support prior to Java 1.4
		return false;
	}

	//-------------------------------------------------------------------------------
	//	Static Methods
	//-------------------------------------------------------------------------------

	public static int countFrames()
	{
		return theFrames.size();
	}

	public static JoFrame getFrame(int idx)
	{
		if (idx < 0 || idx >= theFrames.size())
			return null;
		else
			return (JoFrame)theFrames.get(idx);
	}

	public static int indexOf(JoFrame frame) {
		return theFrames.indexOf(frame);
	}

	public static JoFrame getFrame(String name)
	{
		return JoPanel.get(name).getParentFrame();
	}

	public static JoFrame getFrame(FrameProfile profile)
	{
		for (int i=0; i < theFrames.size(); i++)
		{
			JoFrame frame = getFrame(i);
			if (frame!=null && frame.getProfile()==profile)
				return frame;
		}
		return null;
	}


	public static int countVisibleFrames()
	{
		int count = 0;
		for (int i=0; i < theFrames.size(); i++) {
			JoFrame frame = getFrame(i);
			if (frame!=null && Util.allOf(getFrame(i).getExtendedFrameState(), VISIBLE))
				count++;
		}
		return count;
	}

	public static JoFrame getFrameFor(String panelName)
	{
		for (int i=0; i < theFrames.size(); i++) {
			JoFrame frame = getFrame(i);
			FrameProfile fpf = frame.getProfile();
			if (fpf!=null && fpf.containsComponent(panelName))
				return frame;
		}
		return null;
	}

	public static JoFrame getActiveFrame()
	{
		return theActiveFrame;
	}

    public static void activeToFront()
    {
        if (theActiveFrame!=null) {
            theActiveFrame.setState(JFrame.NORMAL);
            theActiveFrame.toFront();
        }
    }

	public static void closeAll()
	{
		for (int i=0; i<theFrames.size(); i++) {
			JoFrame frame = getFrame(i);
			if (frame!=null) frame.setVisible(false);
		}
		theFrames.clear();
	}


	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public void setComponentsVisible(boolean vis)
	{
		Component cmp = getMainComponent();
		if (cmp!=null) cmp.setVisible(vis);
	}

	/**	@return the main component (either a JoPanel or a JoSplitPane)
	 */
	public Component getMainComponent()
	{
		if (getContentPane().getComponentCount() > 0)
			return getContentPane().getComponent(0);
		else
			return null;
	}

	public void remove(JoPanel panel)
	{
		remove(panel.getParent(), locationInParent(panel));
	}

	public void remove(Container parent, int location)
	{
		switch (location) {
		case 0:		parent.removeAll();
					break;
		case 1:		replace(parent, ((JoSplitPane)parent).secondComponent());
					break;
		case 2:		replace(parent, ((JoSplitPane)parent).firstComponent());
					break;
		}

		updateTitle();
	}

	private void replace(Component a, Component b)
	{
		replace(a.getParent(),locationInParent(a), b);
	}

	private void replace(Container parent, int location, Component comp)
	{
		switch (location) {
		case 0:	parent.removeAll();
				parent.add(comp);
				break;
		case 1:	((JoSplitPane)parent).replaceFirst(comp);
				break;
		case 2:	((JoSplitPane)parent).replaceSecond(comp);
				break;
		}
		getRootPane().revalidate();
	}

	private int locationInParent(Component comp)
	{
		Container parent = comp.getParent();
		if (parent instanceof JoSplitPane) {
			if (comp == ((JoSplitPane)parent).firstComponent())
				return 1;
			else
				return 2;
		}
		else
			return 0;
	}

	/**
	 * @return the left-most, top-most panel (except toolbars)
	 */
	public JoPanel getAnchorPanel()
	{
		JoPanel result = getAnchorPanel(getMainComponent(),true);
		if (result==null)
			result = getAnchorPanel(getMainComponent(),false);
		return result;
	}

	protected JoPanel getAnchorPanel(Component comp, boolean ignoreToolbars)
	{
		if (comp==null)
			return null;
		if (ignoreToolbars && (comp instanceof JoToolBar))
			return null;
		if (comp instanceof JoPanel)
			return (JoPanel)comp;
		if (comp instanceof JoSplitPane) {
			JoPanel result = getAnchorPanel(((JoSplitPane)comp).firstComponent(), ignoreToolbars);
			if (result==null)
				result = getAnchorPanel(((JoSplitPane)comp).secondComponent(), ignoreToolbars);
			return result;
		}
		throw new IllegalStateException();
	}

	/**
	 * get the preferred window title (i.e. one of the panel's title)
	 * @return
	 */
	public String getPreferredTitle()
	{
		java.util.List panels = getPanels(false);
		if (panels.isEmpty()) return "?";
		
		JoPanel bestPanel = (JoPanel)panels.get(0);
		for (int i=1; i<panels.size(); i++)
		{
			JoPanel panel = (JoPanel)panels.get(i);
			if (panel.getTitlePriority() > bestPanel.getTitlePriority())
				bestPanel = panel;
		}
		return bestPanel.getName();
	}

    public java.util.List getPanels(boolean ignoreToolbars)
    {
        return getPanels(getMainComponent(), ignoreToolbars, null);
    }

    public java.util.List getPanels(Component comp, boolean ignoreToolbars, java.util.List collect)
    {
        if (collect==null) collect = new Vector();

        if (comp==null)
            ;
        else if (ignoreToolbars && (comp instanceof JToolBar))
            ;
        else if (comp instanceof JoPanel)
            collect.add(comp);
        else if (comp instanceof JoSplitPane) {
            getPanels(((JoSplitPane)comp).firstComponent(), ignoreToolbars, collect);
            getPanels(((JoSplitPane)comp).secondComponent(), ignoreToolbars, collect);
        }
        else
            throw new IllegalStateException();

        return collect;
    }

	public void setDefaultGlassPane()
	{
		setGlassPane(defaultGlassPane);
	}

	//-------------------------------------------------------------------------------
	//	interface WindowListener
	//-------------------------------------------------------------------------------

	public void windowOpened(WindowEvent e)
	{    }

	public void windowClosing(WindowEvent e)
	{
		/*	forward to CommandListener	*/
		AbstractApplication.theCommandDispatcher.handle(new Command("menu.file.close", e, e.getWindow()), this);
	}

	public void windowClosed(WindowEvent e)
	{
        if (theActiveFrame==e.getWindow()) theActiveFrame = null;
    }

	public void windowIconified(WindowEvent e)
	{ }

	public void windowDeiconified(WindowEvent e)
	{ }

	public void windowActivated(WindowEvent e)
	{
        if (e.getWindow() instanceof JoFrame)
            theActiveFrame = (JoFrame)e.getWindow();
	}

	public void windowDeactivated(WindowEvent e)
	{
        if (theActiveFrame==e.getWindow()) theActiveFrame = null;
    }

	//-------------------------------------------------------------------------------
	//	interface ComponentListener
	//-------------------------------------------------------------------------------

	public void componentHidden(ComponentEvent e)
	{ }

	public void componentShown(ComponentEvent e)
	{ }

	public void componentMoved(ComponentEvent e)
	{
		componentResized(e);
	}

	public void componentResized(ComponentEvent e)
	{
		if (e.getComponent()==this) {
			if (!isMaximized())
				getBounds(profile.userBounds);
		}
	}

	//-------------------------------------------------------------------------------
	//	interface ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		/*	forwar menu events to CommandListener	*/
		Object data = null;
		Object moreData = null;
		Object source = e.getSource();
		if (source instanceof JMenuItem) {
			data = ((JMenuItem)source).getClientProperty("action.data");
			moreData = ((JMenuItem)source).getClientProperty("action.more.data");
		}
		AbstractApplication.theCommandDispatcher.handle(
				new Command(e.getActionCommand(), e, data,moreData),
				this);
	}

	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public CommandListener getCommandParent()
	{
		return Application.theApplication;
	}

	public int numCommandChildren()
	{
		return 1;
	}

	public CommandListener getCommandChild(int i)
	{
		return (CommandListener)getMainComponent();
	}

	public void setupActionMap(Map map)
	{
		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				if (countVisibleFrames() <= 1)
                {
                    if (/*!Version.mac*/true)
                    {
					    cmd.code = "menu.file.quit";
					    AbstractApplication.theCommandDispatcher.forward(cmd, Application.theApplication);
                    }
                    else {
                        setVisible(false);
                        /** Mac OS X
                         *  Appplication without windows. We should show a Menu Bar now,
                         *  but it doesn't work. Using a "dummy" frame doesn't work either.
                         */
                    }
				}
				else
					setVisible(false);
			}
		};
		map.put("menu.file.close", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				toggleFullScreen();
			}
		};
		map.put("menu.window.fullscreen", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				updateLanguage();
			}
		};
		map.put("update.language", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				SwingUtilities.updateComponentTreeUI(JoFrame.this);
			}
		};
		map.put("update.ui", action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                //  dirty documents have changed
                if (Version.mac)
                    getRootPane().putClientProperty("windowModified",
                        Util.toBoolean(Application.theHistory.isDirty()));

            }
        };
        map.put("doc.dirty", action);

	}



	/**
	 * overrides Window.dispose()
	 */
	public void dispose()
	{
		Application.theUserProfile.removeFrameProfile(getProfile());
		theFrames.remove(this);
		super.dispose();
	}

	protected JoComponent createComponent(LayoutProfile lpf, String dockPath)
	{
		if (lpf.isPanelProfile()) {
			if (dockPath!=null) lpf.dockingPath = dockPath;
			if (lpf.hide)
				return null;
			else
				return JoPanel.create(lpf,true);
		}
		else {
			JoComponent leftComponent = createComponent(lpf.firstComponent, lpf.appendDockPath(dockPath,1));
			JoComponent rightComponent = createComponent(lpf.secondComponent, lpf.appendDockPath(dockPath,2));

			if (leftComponent==null)
				return rightComponent;
			else if (rightComponent==null)
				return leftComponent;
			else {
                return new JoSplitPane(lpf.orientation, lpf.dividerLocation,
                                        leftComponent, rightComponent, null);
            }
		}
	}

    /**
     * find the Component with the preferred keyboard focus
     */
    protected JComponent getPreferredFocus()
    {
        IntRef maxPriority = new IntRef(-1);
        ObjectRef maxPanel = new ObjectRef(null);

        getPreferredFocus(getMainComponent(), maxPanel, maxPriority);

        return (JComponent)maxPanel.value;
    }

    private void getPreferredFocus(Component comp, ObjectRef panel, IntRef priority)
    {
        if (comp instanceof JoPanel) {
            int prio = ((JoPanel)comp).getFocusPriority();
            if (prio > priority.value)
                panel.value = ((JoPanel)comp).getFocusComponent();
        }
        else if (comp instanceof JoSplitPane) {
            getPreferredFocus(((JoSplitPane)comp).firstComponent(), panel,priority);
            getPreferredFocus(((JoSplitPane)comp).secondComponent(), panel,priority);
        }
    }

	protected void updateTitle()
	{
		String title = Language.get("application.name");
		String name = getPreferredTitle();
		if (name != null)
			title += " - "+Language.get(name);
		setTitle(title);
	}

	protected void updateLanguage()
	{
		updateTitle();

		Language.update(getJMenuBar());

		/*	main component needs not be updated cause it listens to broadcasts !?!	*/
		//Language.update((Component)getMainComponent());
	}

	/**	dock a component at given path
	 */
	public static void dock(Component comp, JoFrame frame, String path)
	{
		if (path==null || path.length()==0)
			path = "W"; //  accounts for old bug in layout storage

		if (frame==null)	//	create unused frame from profile
			frame = new JoFrame();

		frame.dock1(comp, path);
		comp.setVisible(true);
	}

	/**	dock a component at a given position
	 */
	public static JoFrame dock(JoPanel panel, Component target,
							   char orientation, Dimension preferredSize)
	{
		JoFrame frm = panel.getParentFrame();
		if (panel.isDocked()) {
			//	undock
			//	take care if the target is also the parent (split pane) of the panel
			if (panel.getParent()==target)
				target = ((JoSplitPane)target).otherComponent(panel);
			frm.remove(panel);
		}
		else {
			//	standalone panel: close window
//			frm.remove(panel);
			frm.dispose();
		}

		//panel.setSize(preferredSize);
		panel.setVisible(true);
		panel.getProfile().frameProfile = frm.getProfile();
		panel.getProfile().dockFrame = null;
		frm = ((JoComponent)target).getParentFrame();
		frm.insert(panel, target, orientation);
		return frm;
	}

	private void dock1(Component comp, String path)
	{
		Component p = getMainComponent();
		char orientation = JoComponent.DOCK_WEST;

		for (int i=0; i<path.length(); i++) {
			orientation = path.charAt(i);
			if (! (p instanceof JoSplitPane))
				break;
			JoSplitPane sp = (JoSplitPane)p;

			if (sp.getOrientation()==JoSplitPane.HORIZONTAL_SPLIT) {
				if (orientation==JoComponent.DOCK_WEST)
					p = sp.getLeftComponent();
				else if (orientation==JoComponent.DOCK_EAST)
					p = sp.getRightComponent();
				else
					break;
			}
			else {
				if (orientation==JoComponent.DOCK_NORTH)
					p = sp.getTopComponent();
				else if (orientation==JoComponent.DOCK_SOUTH)
					p = sp.getBottomComponent();
				else
					break;
			}
		}
		if (comp==p)
			return;	//	already docked at the correct position - fine
		insert(comp,p,orientation);
	}

	private void insert(Component newComponent, Component neighbour, char orientation)
	{
		Container parent = neighbour.getParent();
		int at = locationInParent(neighbour);

		JoSplitPane newPane = null;
		Dimension size = neighbour.getSize();
		switch (orientation) {
		case JoComponent.DOCK_NORTH:
			newPane = new JoSplitPane(JoSplitPane.VERTICAL_SPLIT, JoSplitPane.DIVIDE_FIRST,
									  (JoComponent)newComponent, (JoComponent)neighbour, size); break;
		case JoComponent.DOCK_SOUTH:
			newPane = new JoSplitPane(JoSplitPane.VERTICAL_SPLIT, JoSplitPane.DIVIDE_SECOND,
									  (JoComponent)neighbour, (JoComponent)newComponent, size); break;
		case JoComponent.DOCK_WEST:
			newPane = new JoSplitPane(JoSplitPane.HORIZONTAL_SPLIT, JoSplitPane.DIVIDE_FIRST,
									  (JoComponent)newComponent, (JoComponent)neighbour, size); break;
		case JoComponent.DOCK_EAST:
			newPane = new JoSplitPane(JoSplitPane.HORIZONTAL_SPLIT, JoSplitPane.DIVIDE_SECOND,
									  (JoComponent)neighbour, (JoComponent)newComponent, size); break;
		}

		replace(parent,at, newPane);
	}


	/**
	 * calculate the docking path for a component
	 */
	public String getDockingPath(Component comp)
	{
		StringBuffer path = new StringBuffer();
		insertDockingPath(comp,path);
		return path.toString();
	}

	private void insertDockingPath(Component comp, StringBuffer path)
	{
		Container p = comp.getParent();
		if (p instanceof JoSplitPane) {
			JoSplitPane sp = (JoSplitPane)p;
			if (sp.getOrientation()==JoSplitPane.HORIZONTAL_SPLIT) {
				if (comp==sp.firstComponent())
					path.insert(0,JoComponent.DOCK_WEST);
				else
					path.insert(0,JoComponent.DOCK_EAST);
			} else {
				if (comp==sp.firstComponent())
					path.insert(0,JoComponent.DOCK_NORTH);
				else
					path.insert(0,JoComponent.DOCK_SOUTH);
			}

			insertDockingPath(sp,path);
		}
	}

/*
	protected void dumpHierarchy(java.io.PrintStream out)
	{
		dumpHierarchy(0,out,getMainComponent());
	}

	protected void dumpHierarchy(int level, java.io.PrintStream out, Component comp)
	{
		if (comp instanceof JoSplitPane) {
			JoSplitPane pane = (JoSplitPane)comp;
			indent(out,level);
			out.print("<split pane ");
			if (pane.getOrientation()==JoSplitPane.HORIZONTAL_SPLIT)
				out.println(" horitzontal");
			else
				out.println(" vertical");

			dumpHierarchy(level+1, out, pane.firstComponent());
			dumpHierarchy(level+1, out, pane.secondComponent());

			indent(out,level);
			out.println(">");
		}
		else {
			indent(out,level);
			out.print("<");
			out.print(comp.getClass().getName());
			out.print(" ");
			out.print(comp.getName());
			out.println(">");
		}

	}

	protected void indent(java.io.PrintStream out, int level)
	{
		while (level-- > 0) out.print(" ");
	}
*/
}
