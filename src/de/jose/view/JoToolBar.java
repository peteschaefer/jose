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
import de.jose.util.icon.ButtonIcon;
import de.jose.util.FontUtil;
import de.jose.util.StringUtil;
import de.jose.util.icon.TextIcon;
import de.jose.util.icon.TextShapeIcon;
import de.jose.util.style.StyleUtil;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JoToolBar
		extends JoPanel
{
	private static final Insets margin = new Insets(0,2,0,2);

	protected Dimension minDimension = new Dimension(28,28);

	public void actionPerformed(ActionEvent e)
	{
		/*	forward menu events to CommandDispatcher	*/
		if (e.getSource() instanceof JButton) {
			//	tppl buttons clicked
			CommandListener target = getCommandListener();
			/** forward all event to the target
			 *  JoToolBar handles only "on.broadcast" commands by itself
			 */
			AbstractApplication.theCommandDispatcher.handle(e, target);
		}
		if (e.getSource() instanceof JMenuItem) {
			//	from context menu
			AbstractApplication.theCommandDispatcher.handle(e, this);
		}
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

		action = new CommandAction() {
			public void Do(Command cmd) {
				boolean dark = (Boolean)cmd.moreData;
				updateButtonStyle(dark);
			}
		};
		map.put("update.ui",action);

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
		boolean dark = Application.theApplication.isDarkLookAndFeel();
		if (buttons!=null)
			for (int i=0; i < buttons.size(); i++) {
				Object button = buttons.get(i);
				if (button==null)
					addSpacer(16);
				else 
					addButton(button.toString(),dark);
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
		
	private void addButton(String name, boolean dark)
	{
		if (name==null)
			addSpacer(20);
		else {
			JButton button = new JButton();
			button.setName(name);
			button.setActionCommand(name);

			Dimension iconSize = createIcons(name, button, dark);

			//button.setBorderPainted(false);
			//button.setFocusPainted(false);

			minDimension.width = Math.max(minDimension.width, iconSize.width);
			minDimension.height = Math.max(minDimension.height, iconSize.height);
			if (!button.isBorderPainted()) {
				button.setBorder(new EmptyBorder(2, 2, 2, 2));
				button.setMargin(margin);
			}
			//else: border already set in createIcons. clean this up !

			button.setToolTipText(Language.getTip(name));
            if (Version.mac)
                button.putClientProperty("JButton.buttonType","toolbar");
			add(button);
		}
	}

	private void updateButtonStyle(boolean dark)
	{
		for(int i=0;i<getComponentCount();i++) {
			Component comp = getComponent(i);
			if (comp instanceof JButton) {
				JButton button = (JButton)comp;
				String name = button.getName();
				createIcons(name, button, dark);
			}
		}
	}


	private static Dimension createIcons(String name, JButton button, boolean dark)
	{
		//	get icon specification from menu entry
		String spec = JoMenuBar.ICON_SPECS.get(name);

		Icon[] icons = null;
		if (spec != null) {
			//	(1) create from Font Awesome
			icons = createAwesomeIcons(spec,28,dark);
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

		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);

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

			IconSpec ispec = new IconSpec(spec, 28);
			if ((ispec.style & BUTTON) != 0) {
				button.setFocusable(false); //  don't steal keyboard focus from game panel
//		button.setBorder(null);
				button.setBorderPainted(true);
				button.setFocusPainted(true);
				button.setContentAreaFilled(true);
				button.setRolloverEnabled(true);
				button.putClientProperty("JButton.buttonType","roundRect");
				button.putClientProperty("Button.arc",999);

				int hmargin = 24-iconSize.width;
				int vmargin = 24-iconSize.height;
				button.setMargin(new Insets(vmargin/2,hmargin/2, vmargin/2, hmargin/2));
			}
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

	static class IconSpec {
		String text;
		ArrayList<Color> colors = new ArrayList<>();
		int style = Font.PLAIN;
		float size;
		Insets insets = new Insets(2,2,2,2); // right ?

		IconSpec(String spec, float asize)
		{
			this.size = asize;
			String[] specs = spec.split(":");
			text = StringUtil.unescape(specs[0]);
			for(int i=1; i<specs.length; i++) {
				if (specs[i].equalsIgnoreCase("bold"))
					style |= Font.BOLD;
				if (specs[i].equalsIgnoreCase("italic"))
					style |= Font.ITALIC;
				if (specs[i].equalsIgnoreCase("flat"))
					style |= FLAT;
				if (specs[i].equalsIgnoreCase("button"))
					style |= BUTTON;
				if (specs[i].startsWith("#") || specs[i].startsWith("0x"))
					colors.add(Color.decode(specs[i]));
				if (specs[i].startsWith("%")) {
					float scale = Float.parseFloat(specs[i].substring(1));
					this.size *= scale/100.f;
				}
				if (specs[i].startsWith("(")) {
					int inset = Integer.parseInt(specs[i].substring(1));
					insets.top = insets.left = insets.bottom = insets.right = inset;
				}
			}
			if (colors.isEmpty()) colors.add(Color.darkGray);
		}
	}

	public static void makeDarkIcons(Icon[] icons) {
		for(Icon icon : icons)
			if (icon!=null)
				makeDarkIcon(icon);
	}


	public static void makeDarkIcons(Container container) {
		for(int i=0; i < container.getComponentCount(); ++i) {
			Component comp = container.getComponent(i);
			if (comp instanceof JButton) {
				makeDarkIcons((JButton)comp);
			}
			if (comp instanceof Container) {
				makeDarkIcons((Container)comp);
			}
		}
	}

	public static void makeDarkIcons(JButton button) {
		Icon[] icons = new Icon[] {
				button.getIcon(),
				button.getDisabledIcon(),
				button.getPressedIcon(),
				button.getRolloverIcon(),
				button.getDisabledSelectedIcon(),
				button.getSelectedIcon(),
				button.getRolloverSelectedIcon(),
		};
		makeDarkIcons(icons);
	}

	public static void makeDarkIcon(Icon icon) {
		if (icon instanceof TextIcon) {
			TextIcon textIcon = (TextIcon)icon;
			textIcon.setColor( StyleUtil.mapDarkIconColor(textIcon.getColor()) );
		}
		if (icon instanceof TextShapeIcon) {
			TextShapeIcon textIcon = (TextShapeIcon)icon;
			textIcon.setColor( StyleUtil.mapDarkIconColor(textIcon.getColor()) );
			textIcon.setBackgroundColor( StyleUtil.mapDarkIconColor(textIcon.getBackgroundColor()) );
		}
	}


	public static Icon[] createAwesomeIcons(String sp, float size, boolean dark)
	{
		IconSpec spec = new IconSpec(sp, size);
		boolean isButton = ((spec.style&BUTTON)!=0);
		if (isButton) {
			//	button style icons
			spec.style &= ~BUTTON;
			spec.style |= FLAT;
			spec.size *= 0.6f;
		}

		if (dark) {
			//	high contrast colors!
			spec.colors.set(0, StyleUtil.mapDarkIconColor(spec.colors.get(0)));
			if (spec.colors.size() < 2)
				spec.colors.add(Color.darkGray);
			else
				spec.colors.set(1, StyleUtil.mapDarkIconColor(spec.colors.get(1)));
		}
		else {
			if (spec.colors.size() < 2)
				spec.colors.add(Color.white);
		}

		Icon[] result = create7AwesomeIcons(spec);

		if (isButton) {
			spec.style &= BUTTON;
		}

		return result;
	}

	public static Icon create1AwesomeIcon(String spec, float size)
	{
		return create1AwesomeIcon(new IconSpec(spec,size));
	}

	public static Icon createAwesomeIconLike(String iconName, float size)
	{
		String iconSpec = JoMenuBar.ICON_SPECS.get(iconName);
		if (iconSpec!=null)
			return JoToolBar.create1AwesomeIcon(iconSpec,size);
		else
			return ImgUtil.getMenuIcon(iconName);
	}

	public static final int BUTTON = 4;
	public static final int FLAT = 8;

	private static Icon[] create7AwesomeIcons(IconSpec spec)
	{
		Font font = FontUtil.fontAwesome();
		font = font.deriveFont(spec.style & ~BUTTON);
		TextIcon[] result = new TextIcon[7];

		Color color1 = spec.colors.get(0);
		Color color2 = (spec.colors.size() > 1) ? spec.colors.get(1) : null;

		if ((spec.style&BUTTON) != 0) {
			result[0] = new ButtonIcon(spec.text,font,spec.size).fixedColor(Color.lightGray);
			result[1] = new ButtonIcon(spec.text,font,spec.size).huedColor(color1);
			result[2] = new ButtonIcon(spec.text,font,spec.size).huedColor(color1);
			result[3] = new ButtonIcon(spec.text,font,spec.size).huedColor(color1);
			if (color2!=null) {
				result[4] = new ButtonIcon(spec.text,font,spec.size).huedColor(color2);
				result[5] = new ButtonIcon(spec.text,font,spec.size).huedColor(color2);
				result[6] = new ButtonIcon(spec.text,font,spec.size).huedColor(color2);
			}
		}
		else if ((spec.style&FLAT) != 0) {
			result[0] = new TextIcon(spec.text,font,spec.size,Color.lightGray);
			result[1] = new TextIcon(spec.text,font,spec.size,color1);
			result[2] = new TextIcon(spec.text,font,spec.size,color1);
			result[3] = new TextIcon(spec.text,font,spec.size,color1);
			if (color2!=null) {
				result[4] = new TextIcon(spec.text,font,spec.size,color2);
				result[5] = new TextIcon(spec.text,font,spec.size,color2);
				result[6] = new TextIcon(spec.text,font,spec.size,color2);
			}
		}
		else {
			spec.size*=0.7f;
			if (color2==null) color2 = Color.white;

			result[0] = new TextShapeIcon(spec.text,font,spec.size,Color.lightGray,color2);
			result[1] = new TextShapeIcon(spec.text,font,spec.size,color1,color2);
			result[2] = new TextShapeIcon(spec.text,font,spec.size,color1,color2);
			result[3] = new TextShapeIcon(spec.text,font,spec.size,color1,color2);
			/*if (color2!=null) {
				result[4] = new TextShapeIcon(s,font,size,color2,Color.white);
				result[5] = new TextShapeIcon(s,font,size,color2,Color.white);
				result[6] = new TextShapeIcon(s,font,size,color2,Color.white);
			}*/
		}

		for(TextIcon icon : result)
			if (icon!=null)
				icon.setInsets(spec.insets);

		result[2].hilited();
		result[3].pushed();

		if (result[5]!=null) result[5].hilited();
		if (result[6]!=null) result[6].pushed();

		return result;
	}

	private static Icon create1AwesomeIcon(IconSpec spec)
	{
		Font font = FontUtil.fontAwesome();
		font = font.deriveFont(spec.style & ~BUTTON);
		TextIcon result;

		Color color1 = spec.colors.get(0);
		Color color2 = (spec.colors.size() > 1) ? spec.colors.get(1) : Color.white;

		if ((spec.style&BUTTON) != 0) {
			result = new ButtonIcon(spec.text,font,spec.size).huedColor(color1);
		}
		else if ((spec.style&FLAT) != 0) {
			result = new TextIcon(spec.text,font,spec.size,color1);
		}
		else {
			spec.size*=0.7f;
			result = new TextShapeIcon(spec.text,font,spec.size,color1,color2);
		}

		if (result!=null) result.setInsets(spec.insets);
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
