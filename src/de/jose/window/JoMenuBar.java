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
import de.jose.chess.TimeControl;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.CommandListener;
import de.jose.image.ImgUtil;
import de.jose.util.ListUtil;
import de.jose.util.AWTUtil;
import de.jose.util.map.IntHashSet;
import de.jose.view.DocumentPanel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class JoMenuBar
		extends JMenuBar
		implements MenuListener, ActionListener
{

	public static final Comparator SORT_BY_TITLE = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			JMenuItem thisMenu = (JMenuItem)o1;
			JMenuItem thatMenu = (JMenuItem)o2;
			return thisMenu.getText().compareTo(thatMenu.getText());
		}
	};

	//-------------------------------------------------------------------------------
	//	interface ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		/*	forwar menu events to CommandListener
			which is either the focused panel, the owner frame,
			or the active frame. In the end all commands are percolated
			upwards to the Application.		
		*/
		Object data = null;
		Object moreData = null;
		Object source = e.getSource();
		if (source instanceof JMenuItem) {
			data = ((JMenuItem)source).getClientProperty("action.data");
			moreData = ((JMenuItem)source).getClientProperty("action.more.data");
		}

		CommandListener target = getCommandListener();
		AbstractApplication.theCommandDispatcher.handle(
				new Command(e.getActionCommand(), e, data,moreData),
				target);
	}


	protected CommandListener owner;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	/**	default ctor	 */
	public JoMenuBar(CommandListener owner)
	{
		super();

		this.owner = owner;

		Enumeration menus = Application.theApplication.theConfig.enumerateElements("menu");
		while (menus.hasMoreElements()) {
			JMenu menu = createXMLMenu((Element)menus.nextElement(), this);
			add(menu);
		}

		if (!Version.mac)
            assignMnemonics(this);

        addMenuItemListener(this,this);
	}
	
	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------
	

	public CommandListener getCommandListener()
	{
		CommandListener focus = Application.theApplication.getFocusPanel();
		if (focus!=null)
			return focus;

		if (owner!=null)
			return owner;

		JoFrame active = JoFrame.getActiveFrame();
		if (active!=null && active.isShowing())
			return active;

		//  else
		return Application.theApplication;
	}


	public static void addMenuItemListener(JMenuBar bar, ActionListener listener)
	{
		for (int j=0; j < bar.getMenuCount(); j++)
			addMenuItemListener(bar.getMenu(j), listener);
	}

	public static void addMenuItemListener(Object menu, ActionListener listener)
	{
		for (int i=0; i < getItemCount(menu); i++) {
			JMenuItem item = getItem(menu,i);
			if (item != null) {
				if (item instanceof JMenu)
					addMenuItemListener(item,listener);
				else
					item.addActionListener(listener);
			}
		}
	}

	public static boolean adjustMenu(Object menu, CommandListener cmdListener, boolean adjustText)
	{
        /** during modal dialogs:
         *  enable ONLY Edit items with the "modal" attribute
         */
        boolean isModal = JoDialog.isModalActive();
		boolean anyEnabled = false;

		for (int i=0; i < getItemCount(menu); i++) {
			JMenuItem item1 = getItem(menu,i);
			if (item1==null)    //  separator
				continue;

			boolean enable = false;
			if (item1 instanceof JMenu) {
				//  sub-menues are enabled if at least one of their children are!
				enable = adjustMenu((JMenu)item1,cmdListener, adjustText);
			}
			else
			{
				String name = item1.getActionCommand();

				enable = AbstractApplication.theCommandDispatcher.isEnabled(name,cmdListener);
				if (isModal)
	                enable = enable && isModalMenuEnabled(item1);

				CommandAction action = Application.theCommandDispatcher.findTargetAction(name,cmdListener);
				if (action!=null)
				{
                    if (adjustText) {
                        item1.setSelected(action.isSelected(name));
					    item1.setText(action.getDisplayText(name));
					    item1.setToolTipText(action.getToolTipText(name));
                    }
				}
			}

			item1.setEnabled(enable);
			if (enable) anyEnabled = true;
		}
		return anyEnabled;
	}

	protected void adjustMenu(JMenu menu)
	{
		adjustMenu(menu,getCommandListener(), true);

		boolean isModal = JoDialog.isModalActive();

		if (menu.getName().equals("menu.game.time.controls"))
        {
			//	setup sub-menu with time controls
			JMenuItem item0 = menu.getItem(0);
			ActionListener[] actionListeners = item0.getActionListeners();
			while (menu.getItemCount() > 2)
				menu.remove(0);
			addTimeControlItems(menu, item0.getName(), actionListeners, isModal);
		}

        if (menu.getName().equals("dialog.option.font.figurine"))
        {
            //  figurines
            JMenuItem item0 = menu.getItem(0);
            ActionListener[] actionListeners = item0.getActionListeners();

            boolean usefont = Application.theUserProfile.useFigurineFont();
            String lang = Application.theUserProfile.getFigurineLanguage();
            menu.removeAll();
            DocumentPanel.createFigurineMenu(menu,usefont,lang, actionListeners);
        }
	}

	public void addTimeControlItems(JMenu menu, String name, ActionListener[] actionListeners, boolean isModal)
	{
		Vector<TimeControl> controls = Application.theUserProfile.getTimeControls();
		TimeControl current = Application.theUserProfile.getTimeControl();

		for (int i=0; i<controls.size(); i++) {
			TimeControl control = controls.get(i);
			JMenuItem item = createMenuItem(name, control==current);
            if (isModal)
                item.setEnabled(false);
			item.setText(control.getDisplayName());
			item.setToolTipText(control.getToolTip());
			item.putClientProperty("action.data",new Integer(i));
			if (actionListeners!=null)
				for (int j=0; j < actionListeners.length; j++)
					item.addActionListener(actionListeners[j]);
			menu.insert(item,i);
		}
	}

	public static void addTimeControlItems(Collection menu)
	{
		Vector<TimeControl> controls = Application.theUserProfile.getTimeControls();
		TimeControl current = Application.theUserProfile.getTimeControl();

		for (int i=0; i<controls.size(); i++) {
			TimeControl control = controls.get(i);
			JMenuItem item = createMenuItem("menu.game.time.control", control==current);
			item.setText(control.getDisplayName());
			item.setToolTipText(control.getToolTip());

			menu.add(item);
			menu.add(new Integer(i));
		}
	}


    public static boolean isModalMenuEnabled(JMenuItem item)
    {
        return Util.toboolean(item.getClientProperty("modal.enabled"));
    }


	//-------------------------------------------------------------------------------
	//	Interface MenuListener
	//-------------------------------------------------------------------------------

	public void menuSelected(MenuEvent e)
	{
		adjustMenu((JMenu)e.getSource());
	}

	public void menuDeselected(MenuEvent e)	{ }
	public void menuCanceled(MenuEvent e)	{ }


	//-------------------------------------------------------------------------------
	//	Private Methods
	//-------------------------------------------------------------------------------
/*
	public static final KeyStroke KEY(int c)	{
		return KeyStroke.getKeyStroke(c, 0);
	}

	public static final KeyStroke CTRL(int c)	{
		return KeyStroke.getKeyStroke(c, Event.CTRL_MASK);
	}

	public static final KeyStroke CTRL_SHIFT(int c)	{
		return KeyStroke.getKeyStroke(c, Event.CTRL_MASK+Event.SHIFT_MASK);
	}

	private static final Integer INT(int i)
	{
		return new Integer(i);
	}
 */
	public JMenu createMenu(Object def)
	{
		return createMenu(def,this);
	}

	public static JMenu createMenu(Object def, MenuListener listener)
	{
        Iterator i = ListUtil.iterator(def);

		String name = (String)i.next();
		JMenu menu = new JMenu();
		menu.setName(name);
		menu.setText(Language.get(name));

		addItems(menu, i, listener);
		if (listener!=null)
			menu.addMenuListener(listener);
		return menu;
	}

	public static JMenu createXMLMenu(Element menuElement, MenuListener listener)
	{
		String name = menuElement.getAttribute("id");
		JMenu menu = new JMenu();
		menu.setName(name);
		menu.setText(Language.get(name));

		NodeList items = menuElement.getChildNodes();
		boolean iconindent = false;
		for (int i=0; i<items.getLength(); i++) {
			Node nd = items.item(i);
			if (nd instanceof Element) {
				JMenuItem item = addXMLItem(menu, (Element)nd, listener);
				if (item!=null && item.getIcon()!=null) iconindent = true;
			}
		}

		if (iconindent)
			adjustIndent(menu); /*	workaround if L&F can not indent menu items	*/
		if (listener!=null)
			menu.addMenuListener(listener);
		return menu;
	}

	public static JMenuItem addXMLItem(Object menu, Element itemElement, MenuListener listener)
	{
        String hide = itemElement.getAttribute("hide");
        //if ("mac".equalsIgnoreCase(hide) && Version.mac)
        //    return null; //  hide this menu item on Macs

		if (itemElement.getTagName().equals("menu")) {
			//	submenu
			JMenu submenu = createXMLMenu(itemElement,listener);
			add(menu,submenu);
			return submenu;
		}
		else if (itemElement.getTagName().equals("separator")) {
			addSeparator(menu);
			return null;
		}
		else if (itemElement.getTagName().equals("item")) {
			String name 	= itemElement.getAttribute("id");
			String stroke 	= itemElement.getAttribute("key");
			String check 	= itemElement.getAttribute("check");
			String intParam = itemElement.getAttribute("int");
			String urlParam = itemElement.getAttribute("url");
            String modal    = itemElement.getAttribute("modal");

			JMenuItem item;
			if (check!=null && check.length()>0)
				item = createMenuItem(name, check.equals("true"));
			else
				item = createMenuItem(name);

			if (stroke!=null && stroke.length()>0)
				item.setAccelerator(AWTUtil.getMenuKeyStroke(stroke));

			if (intParam!=null && intParam.length()>0)
				item.putClientProperty("action.data", new Integer(intParam));
			if (urlParam!=null && urlParam.length()>0)
				try {
					item.putClientProperty("action.data", new URL(urlParam));
				} catch (MalformedURLException muex) {
					//	?
				}

            if (Util.toboolean(modal))
                item.putClientProperty("modal.enabled",Boolean.TRUE);

			add(menu,item);
			return item;
		}
		else
			throw new IllegalStateException("unexpected xml element: "+itemElement.getTagName());
	}

	public static void appendMenu(JMenu menu, Object def, MenuListener listener)
	{
		addItems(menu, def, listener);
	}

	public static void addItems(Object menu, Object list, MenuListener listener)
	{
		JMenuItem item = null;
		boolean iconindent = false;
		boolean useCheckbox = false;
		boolean checkboxValue = false;
		boolean withSeparator = false;
        Iterator i = ListUtil.iterator(list);

		while (i.hasNext())
        {
            Object def = i.next();
			if (def == null) {
                //  null = separator
				withSeparator = getItemCount(menu) > 0;
			}
			else if (def instanceof JMenuItem) {
				item = (JMenuItem)def;

				if (withSeparator) { addSeparator(menu); withSeparator=false; }
				add(menu,item);

				useCheckbox = false;
				if (item.getIcon()!=null) iconindent = true;
			}
			else if (def instanceof Boolean) {
                //  checkbox in front of item
				useCheckbox = true;
				checkboxValue = ((Boolean)def).booleanValue();
			}
			else if (def instanceof String) {
                //  item text
				String s = (String)def;
				if (useCheckbox)
					item = createMenuItem(s,checkboxValue);
				else
					item = createMenuItem(s);

				if (withSeparator) { addSeparator(menu); withSeparator=false; }
				add(menu, item);

				useCheckbox = false;
				if (item.getIcon()!=null) iconindent = true;
			}
			else if (ListUtil.isIteratable(def)) {
				//	sub-menu
				item = createMenu(ListUtil.iterator(def), listener);

				if (withSeparator) { addSeparator(menu); withSeparator=false; }
				add(menu, item);
			}
			else if (def instanceof KeyStroke) {
                //  accelerator key
				item.setAccelerator((KeyStroke)def);
            }
			else if (def instanceof Icon) {
                //  icon
				item.setIcon((Icon)def);
				iconindent = true;
			}
			else {
                //  command parameters
				if (item.getClientProperty("action.data")==null)
					item.putClientProperty("action.data",def);
				else
					item.putClientProperty("action.more.data",def);
            }
        }

		if (iconindent) adjustIndent(menu); /*	workaround if L&F can not indent menu items	*/
	}

	protected static void adjustIndent(Object menu)
	{
        if (Version.mac) return;    //  don't (Aqua)

		for (int j=0; j<getItemCount(menu); j++) {
			JMenuItem item = getItem(menu, j);
			if (item!=null && (item.getIcon()==null) && !hasCheckbox(item)) {
				item.setIcon(ImgUtil.getMenuIcon("empty16"));
/*					Insets margin = item.getMargin();
					margin.left += 20;
					item.setMargin(margin);
*/				}
		}
	}

	public static void add(Object menu, JMenuItem item)
	{
		if (menu instanceof JMenu)
			((JMenu)menu).add(item);
		else if (menu instanceof JPopupMenu)
			((JPopupMenu)menu).add(item);
		else
			throw new IllegalArgumentException();
	}

	public static JMenuItem getItem(Object menu, int i)
	{
		if (menu instanceof JMenu)
			return ((JMenu)menu).getItem(i);
		else if (menu instanceof JPopupMenu) {
			Component comp = ((JPopupMenu)menu).getComponent(i);
			if (comp instanceof JMenuItem)
				return (JMenuItem)comp;
			else
				return null;
		}
		else
			throw new IllegalArgumentException();
	}

	public static JMenuItem getItem(Object menu, String name)
	{
		if (menu instanceof JMenu)
			return getItem((JMenu)menu, name);
		else if (menu instanceof JPopupMenu)
			return getItem((JPopupMenu)menu, name);
		else
			throw new IllegalArgumentException();
	}

	public static JMenuItem getItem(JMenu menu, String name)
	{
		for (int i=0; i<menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			if (item!=null && name.equals(item.getName()))
				return item;
		}
		return null;
	}

	private static JMenuItem getItem(JPopupMenu menu, String name)
	{
		for (int i=0; i<menu.getComponentCount(); i++) {
			Component comp = ((JPopupMenu)menu).getComponent(i);
			if (comp!=null && comp instanceof JMenuItem) {
				JMenuItem item = (JMenuItem)comp;
				if (name.equals(item.getName()))
					return item;
			}
		}
		return null;
	}

	public static int getItemCount(Object menu)
	{
		if (menu instanceof JMenu)
			return ((JMenu)menu).getItemCount();
		else if (menu instanceof JPopupMenu)
			return ((JPopupMenu)menu).getComponentCount();
		else
			throw new IllegalArgumentException();
	}

	public static void addSeparator(Object menu)
	{
		if (menu instanceof JMenu)
			((JMenu)menu).addSeparator();
		else if (menu instanceof JPopupMenu)
			((JPopupMenu)menu).addSeparator();
		else
			throw new IllegalArgumentException();
	}

	public static JCheckBoxMenuItem createMenuItem(String name, boolean checkValue)
	{
		JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		initItem(item,name);
		item.setState(checkValue);
		item.putClientProperty("has.checkbox", Boolean.TRUE);
		return item;
	}

	public static boolean hasCheckbox(JMenuItem item) {
		return item.getClientProperty("has.checkbox") != null;
	}

	public static JMenuItem createMenuItem(String name)
	{
		JMenuItem item = new JMenuItem();
		initItem(item,name);
		return item;
	}

	public static void initItem(JMenuItem item, String name)
	{
		item.setName(name);
		item.setActionCommand(name);

		Icon icon = ImgUtil.getMenuIcon(name);
		item.setIcon(icon);

		item.setText(Language.get(name));
		item.setToolTipText(Language.getTip(name));
	}

	public static void assignMnemonics(JMenuBar menuBar)
	{
		assignMnemonics(menuBar,null);
	}

	public static void assignMnemonics(JMenuBar menuBar, IntHashSet keysInUse)
	{
		if (keysInUse==null) keysInUse = new IntHashSet();
		for (int i=0; i < menuBar.getMenuCount(); i++)
		{
			JMenu menu = menuBar.getMenu(i);
			assignMnemonic(menu,keysInUse);
			assignMnemonics(menu);
		}
	}

	public static void assignMnemonics(JMenu menu)
	{
		assignMnemonics(menu,null);
	}

	public static void assignMnemonics(JMenu menu, IntHashSet keysInUse)
	{
		if (keysInUse==null) keysInUse = new IntHashSet();
		for (int i=0; i < menu.getItemCount(); i++)
		{
			JMenuItem item = menu.getItem(i);
			if (item!=null)
				assignMnemonic((AbstractButton)item,keysInUse);
			if (item instanceof JMenu) {
				//  recurse, but don't share used keys
				assignMnemonics((JMenu)item);
			}
		}
	}

	public static void assignMnemonics(JOptionPane optionPane)
	{
		assignMnemonics(optionPane.getOptions());
	}


	public static void assignMnemonics(Object buttons)
	{
		assignMnemonics(buttons,null);
	}

	public static void assignMnemonics(Object buttons, IntHashSet keysInUse)
	{
        if(Version.mac) return; //  don't

		if (keysInUse==null) keysInUse = new IntHashSet();
		if (buttons instanceof Container)
			buttons = ((Container)buttons).getComponents();
		Iterator i = ListUtil.iterator(buttons);
		while (i.hasNext())
		{
			Object item = i.next();
			if ((item!=null) && (item instanceof AbstractButton))
				assignMnemonic((AbstractButton)item,keysInUse);
		}
	}

	public static void assignMnemonics(JTabbedPane tabpane)
	{
		assignMnemonics(tabpane,null);
	}

	public static void assignMnemonics(JTabbedPane tabpane, IntHashSet keysInUse)
	{
        if (Version.mac) return;    //  don't

		if (keysInUse==null) keysInUse = new IntHashSet();
		for (int i=0; i < tabpane.getTabCount(); i++)
			assignMnemonic(tabpane,i, keysInUse);
	}

	public static void assignMnemonic(AbstractButton button, IntHashSet keysInUse)
	{
		String text = button.getText();
		if (text==null) return;
		
		int idx = getMnemonicIndex(text, keysInUse);
		if (idx < 0)
			button.setDisplayedMnemonicIndex(-1);
		else {
			char ch = text.charAt(idx);
			int keyCode = (int)ch; //getKeyCode(ch);
			button.setDisplayedMnemonicIndex(idx);
			button.setMnemonic(keyCode);
			keysInUse.add(keyCode);
		}
	}

	private static void assignMnemonic(JTabbedPane tabpane, int i, IntHashSet keysInUse)
	{
		String text = tabpane.getTitleAt(i);
		if (text==null) return;

		int idx = getMnemonicIndex(text, keysInUse);
		if (idx < 0)
			tabpane.setMnemonicAt(i,-1);
		else {
			char ch = text.charAt(idx);
			int keyCode = (int)ch;
			tabpane.setMnemonicAt(i,keyCode);
			keysInUse.add(keyCode);
		}
	}

	public static int[] getMnemonics(String[] items)
	{
		int[] result = new int[items.length];
		IntHashSet keysInUse = new IntHashSet();
		for (int i=0; i<items.length; i++)
		{
			result[i] = getMnemonic(items[i],keysInUse);
			if (result[i] >= 0) keysInUse.add(result[i]);
		}
		return result;
	}

	public static int getMnemonic(String text, IntHashSet keysInUse)
	{
		//  first: iterate through uppercase characters
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c) && Character.isUpperCase(c))
			{
				int keyCode = (int)c;   //  getKeyCode(c);
				if (!keysInUse.contains(keyCode)) return keyCode;
			}
		}
		//  next: iterate through lowercase characters
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c) && Character.isLowerCase(c))
			{
				int keyCode = (int)Character.toUpperCase(c);
				if (!keysInUse.contains(keyCode)) return keyCode;
			}
		}
		//  give up
		return 0;
	}

	public static int getMnemonicIndex(String text, IntHashSet keysInUse)
	{
		//  first: iterate through uppercase characters
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c) && Character.isUpperCase(c))
			{
				int keyCode = (int)c;   //  getKeyCode(c);
				if (!keysInUse.contains(keyCode)) return i;
			}
		}
		//  next: iterate through lowercase characters
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c) && Character.isLowerCase(c))
			{
				int keyCode = (int)Character.toUpperCase(c);
				if (!keysInUse.contains(keyCode)) return i;
			}
		}
		//  give up
		return -1;
	}

	public static int getKeyCode(char ch)
	{
		KeyStroke keyStroke = KeyStroke.getKeyStroke(ch);
		return keyStroke.getKeyCode();
	}

}
