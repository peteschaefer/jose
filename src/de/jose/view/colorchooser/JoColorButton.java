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

package de.jose.view.colorchooser;

import de.jose.Language;
import de.jose.Version;
import de.jose.image.ImgUtil;
import de.jose.view.input.ValueHolder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

/**
 *	a button that displays a color selection
 * 	on clicking the button, a ColorChooser pops up
 *
 *	@author Peter Sch�fer
 */

public class JoColorButton
		extends JButton
		implements ActionListener, ValueHolder
{
	/**	the current color	*/
	protected Color color;
	protected static JColorChooser chooserPane;
	protected JDialog chooser;

	public JoColorButton()
	{
		super();
		setContentAreaFilled(false);
		addActionListener(this);
		//	we will draw the content area ourselves
        if (Version.mac) {
            putClientProperty("JButton.buttonType","colorWell");
            setBorderPainted(true);
            setBorder(new BevelBorder(BevelBorder.RAISED));
        }
	}

    public JoColorButton(String command)
	{
        this();
        setActionCommand(command);
        setText(Language.get(command));
        setToolTipText(Language.getTip(command));
    }

	public Color getColor()					{ return color; }

	public void setColor(Color col)
    { 
        color = col;
        repaint();
    }

	//  implements ValueHolder

	public Object getValue()                { return getColor(); }

	public void setValue(Object value)      { setColor((Color)value); }
	

	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource()==this)
		{
			if (chooserPane==null)
			{
				chooserPane = new JColorChooser();
				chooserPane.setName(this.getName());
			}
			chooserPane.setColor(color);

			//	I have been pressed
			chooser = JColorChooser.createDialog(getTopLevelAncestor(),
										this.getText(), true, chooserPane, this, this);
			chooser.setVisible(true);
		}

		//	callback from Dialog ?
		if (evt.getActionCommand().equals("OK"))
		{
			setColor(chooserPane.getColor());
			fireItemStateChanged(new ItemEvent(this,ItemEvent.SELECTED,
									color,ItemEvent.SELECTED));
			repaint();
			chooser.dispose();
		}

		if (evt.getActionCommand().equals("cancel"))
		{
			chooser.dispose();
		}
	}

	public void paint(Graphics g)
	{
		g.setColor(color);
		g.fillRect(0,0, getWidth(),getHeight());

		if (ImgUtil.isDark(color))
			setForeground(Color.white);
		else
			setForeground(Color.black);

		super.paint(g);
	}

}
