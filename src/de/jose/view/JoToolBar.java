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
import de.jose.image.ImgUtil;
import de.jose.profile.LayoutProfile;
import de.jose.util.ButtonIcon;
import de.jose.util.FontUtil;
import de.jose.util.StringUtil;
import de.jose.util.TextIcon;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class JoToolBar
		extends JoPanel
{
	private static final Insets margin = new Insets(0,2,0,2);

	protected Dimension minDimension = new Dimension(20,20);

	public void actionPerformed(ActionEvent e)
	{
		/*	forward menu events to CommandDispatcher	*/
		CommandListener target = getCommandListener();

		/** forward all event to the target
		 *  JoToolBar handles only "on.broadcast" commands by itself
		 */
		AbstractApplication.theCommandDispatcher.handle(e, target);
	}

	public JoToolBar(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
        this(profile,
            (List)AbstractApplication.theUserProfile.get(profile.name),
            FlowLayout.LEFT,
            withContextMenu, withBorder);
    }

    public JoToolBar(LayoutProfile profile, List buttons, int align,
                     boolean withContextMenu, boolean withBorder)
    {
		super(profile,withContextMenu,withBorder);
		setLayout(new FlowLayout(align,0,0));

		addButtons(buttons);
		addActionListener(this);

	    setMinimumSize(minDimension);
	    setFocusable(false);    //  don't request keyboard focus
	}

	public Dimension getMaximumSize() {
		if (getParent() instanceof JoSplitPane)
			return  getMaximumSize(((JoSplitPane)getParent()).getOrientation());
		else
			return super.getMaximumSize();
	}

	public CommandListener getCommandListener()
	{
		CommandListener result = Application.theApplication.getFocusPanel();
		if (result!=null)
			return result;
		else
			return Application.theApplication;
	}

	public Dimension getMaximumSize(int orientation)
	{
		Dimension dim = super.getMaximumSize();
		if (orientation==JoSplitPane.HORIZONTAL_SPLIT) {
			dim.width = minDimension.width;
            if (dim.height<=0) dim.height = Integer.MAX_VALUE;
        }
		else {
			dim.height = minDimension.height;
            if (dim.width<=0) dim.width = Integer.MAX_VALUE;
        }
		return dim;
	}

	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				Language.update(JoToolBar.this);
			}
		};
		map.put("update.language", action);

		/**	default action that is performed upon each broadcast	*/
		action = new CommandAction() {
			public void Do(Command cmd) {
				adjustEnable();
			}
		};
		map.put("on.broadcast",action);

		/**
		 * note that we don't handle button events directly
		 * they are directed to the focus panel
		 */
	}

	/**	adjust enable state of buttons
	 * 	(applies oly to certain buttons)
	 *
	 * TODO adjust enable status everytime the focus panel changes
	 */
	private void adjustEnable()
	{
		if (getCommandListener()!=null)
			adjustEnable(getCommandListener());
	}

	/**	adjust enable state of buttons
	 * 	(applies oly to certain buttons)
	 *
	 * TODO adjust enable status everytime the focus panel changes
	 */
	public void adjustEnable(CommandListener cmdlistener)
	{
		for (int i=getComponentCount()-1; i >= 0; i--)
			if (getComponent(i) instanceof AbstractButton)
			{
				AbstractButton button = (AbstractButton)getComponent(i);
				boolean enabled = Application.theCommandDispatcher.isEnabled(button.getName(), cmdlistener);
				if (button.isFocusable()) button.setFocusable(false); //  don't steal focus from game panel

				if (enabled)
					button.setEnabled(true);
				else {
					if (button.getDisabledIcon()==null) {
						ImageIcon icon = (ImageIcon)button.getIcon();
						icon = ImgUtil.createDisabledIcon(icon);
						button.setDisabledIcon(icon);
					}
					button.setEnabled(false);
				}
			}
	}

	public void addButtons(List buttons)
	{
		if (buttons!=null)
			for (int i=0; i < buttons.size(); i++) {
				Object button = buttons.get(i);
				if (button==null)
					addSpacer(16);
				else 
					addButton(button.toString());
			}
	}

	public String[] getButtons()
	{
		String[] result = new String[getComponentCount()];
		for (int i=0; i<result.length; i++)
			if (getComponent(i) instanceof AbstractButton)
				result[i] = getComponent(i).getName();
			else
				result[i] = null;
		return result;
	}
		
	private void addButton(String name)
	{
		if (name==null)
			addSpacer(20);
		else {
			JButton button = new JButton();
			button.setName(name);
			button.setActionCommand(name);

			Dimension iconSize = createIcons(name, button);

			button.setBorderPainted(false);
			button.setFocusPainted(false);

			minDimension.width = Math.max(minDimension.width, iconSize.width);
			minDimension.height = Math.max(minDimension.height, iconSize.height);
			if (iconSize.width < 32)
				button.setBorder(new EmptyBorder(2,2,2,2));
			else
				button.setBorder(new EmptyBorder(0,0,0,0));

			button.setMargin(margin);
			button.setToolTipText(Language.getTip(name));
            if (Version.mac)
                button.putClientProperty("JButton.buttonType","toolbar");
			add(button);
		}
	}

	private static Dimension createIcons(String name, JButton button)
	{
		//	get icon specification from menu entry
		String spec = JoMenuBar.ICON_SPECS.get(name);

		Icon[] icons = null;
		if (spec != null) {
			//	(1) create from Font Awesome
			icons = createAwesomeIcons(spec,16);
		}
		if (icons==null) {
			//	(2) lookup .gif in images/nav
			icons = ImgUtil.getNavigationIcons(name);
		}
		if (icons==null) {
			//	(3) lookup .gif in images/menu
			Icon menuIcon = ImgUtil.getMenuIcon(name);
			Icon menuIcon2 = ImgUtil.getMenuIcon(name,true);
			if (menuIcon!=null)
				icons = new Icon[] { null, menuIcon, null, null, menuIcon2, null, null };
		}

		Dimension iconSize = new Dimension();

		if (icons!=null) {
			assert(icons.length>=6);
			//  rollover icons
			button.setDisabledIcon(icons[0]);    //  *.off
			button.setIcon(icons[1]);                //  *.cold
			button.setRolloverIcon(icons[2]);    //  *.hot
			button.setPressedIcon(icons[3]);     //  *.pressed
			//  TODO
			button.setDisabledSelectedIcon(icons[0]);    //  *.off
			button.setSelectedIcon(icons[4]);                //  *.selected.cold
			button.setRolloverSelectedIcon(icons[5]);    //  *.selected.hot
			// icons[6] = Rollover Pressed is not used
			//  what about selected.pressed ?
			button.setText(null);		//	no text
			button.setRolloverEnabled(icons[2]!=null);
			button.setContentAreaFilled(icons[3]==null);

			iconSize.width = icons[1].getIconWidth();
			iconSize.height = icons[1].getIconHeight();
		}
		else {
			button.setIcon(null);
			button.setText(Language.get(name));
			button.setContentAreaFilled(true);

			iconSize.width = 16;
			iconSize.height = 16;
		}
		return iconSize;
	}

	public static Icon[] createAwesomeIcons(String spec, int size)
	{
		//	spec looks like "<text>:<#color1>[:#color2][:bold][:italic]"
		String text;
		Color color1=null;
		Color color2=null;
		int style = Font.PLAIN;

		String[] specs = spec.split(":");
		text = StringUtil.unescape(specs[0]);
		for(int i=1; i<specs.length; i++) {
			if (specs[i].equalsIgnoreCase("bold"))
				style |= Font.BOLD;
			if (specs[i].equalsIgnoreCase("italic"))
				style |= Font.ITALIC;
			if (specs[i].equalsIgnoreCase("button"))
				style |= BUTTON;
			if (specs[i].startsWith("#") || specs[i].startsWith("0x")) {
				if (color1==null)
					color1 = Color.decode(specs[i]);
				else if (color2==null)
					color2 = Color.decode(specs[i]);
			}
		}

		if (color1==null) color1 = Color.darkGray;
		return createAwesomeIcons(text,size,style,color1,color2);
	}

	public static final int BUTTON = 4;

	public static Icon[] createAwesomeIcons(String s, int size, int style, Color color1, Color color2)
	{
		Font font = FontUtil.fontAwesome();
		font = font.deriveFont(style & ~BUTTON);
		TextIcon[] result = new TextIcon[7];

		if ((style&BUTTON) != 0) {
			result[0] = new ButtonIcon(s,font,size).fixedColor(Color.lightGray);
			result[1] = new ButtonIcon(s,font,size).huedColor(color1);
			result[2] = new ButtonIcon(s,font,size).huedColor(color1);
			result[3] = new ButtonIcon(s,font,size).huedColor(color1);
			if (color2!=null) {
				result[4] = new ButtonIcon(s,font,size).huedColor(color2);
				result[5] = new ButtonIcon(s,font,size).huedColor(color2);
				result[6] = new ButtonIcon(s,font,size).huedColor(color2);
			}
		}
		else {
			result[0] = new TextIcon(s,font,size,Color.lightGray);
			result[1] = new TextIcon(s,font,size,color1);
			result[2] = new TextIcon(s,font,size,color1);
			result[3] = new TextIcon(s,font,size,color1);
			if (color2!=null) {
				result[4] = new TextIcon(s,font,size,color2);
				result[5] = new TextIcon(s,font,size,color2);
				result[6] = new TextIcon(s,font,size,color2);
			}
		}

		Insets insets = new Insets(2,2,2,2); // right ?
		for(TextIcon icon : result)
			if (icon!=null)
				icon.setInsets(insets);

		result[2].hilited();
		result[3].pushed();

		if (result[5]!=null) result[5].hilited();
		if (result[6]!=null) result[6].pushed();

		return result;
	}

	public void addSpacer(int size)
	{
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(size,size));
		add(spacer);
	}

    public void setSelected(String buttonName, boolean on)
    {
        for (int i=0; i < getComponentCount(); i++)
        {
            Component comp = getComponent(i);
            if (comp instanceof AbstractButton)
            {
                AbstractButton but = (AbstractButton)comp;
                if (buttonName.equals(comp.getName()))
                    but.setSelected(on);
            }
        }
    }

	private void addActionListener(ActionListener listener)
	{
		for (int i=0; i < getComponentCount(); i++)
			if (getComponent(i) instanceof AbstractButton) 
				((AbstractButton)getComponent(i)).addActionListener(listener);
	}

	public float getWeightX()	{ return 0.0f; }
	public float getWeightY()	{ return 0.0f; }

	public String getDockingSpots()	{ return DOCK_NONE; }

}
