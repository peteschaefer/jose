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

import de.jose.image.Surface;
import de.jose.image.TextureCache;
import de.jose.view.input.ValueHolder;
import de.jose.Version;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

/**
 *	a button that displays a surface selection (color, or gradient, or texture)
 * 	on clicking the button, a JoSurfaceChooser pops up
 *
 *	@author Peter Sch�fer
 */

public class JoSurfaceButton
		extends JButton
		implements ActionListener, ValueHolder
{
	protected Surface surface;
	protected static JoSurfaceChooser chooserPane;
	protected JDialog chooser;

	public JoSurfaceButton()
	{
		super();
		setContentAreaFilled(false);
		addActionListener(this);
		//	we will draw the content area ourselves
        if (Version.mac) {
            putClientProperty("JButton.buttonType","toolbar");
            setBorderPainted(true);
            setBorder(new BevelBorder(BevelBorder.RAISED));
        }
	}
	
	public Surface getSurface()				{
		if (surface == null)
			return null;
		else
			return surface.copy();
	}

	public void setSurface(Surface surf)	{
		if (surf!=null)
			surface = surf.copy();
		else
			surface = null;
	}

	//  implements ValueHolder
	public Object getValue()                { return getSurface(); }

	public void setValue(Object value)      { setSurface((Surface)value); }



	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource()==this)
		{
			if (chooserPane==null)
			{
				chooserPane = createChooser();
				chooserPane.setName(this.getName());
			}
			chooserPane.setSurface(surface);

			//	I have been pressed
			chooser = JoSurfaceChooser.createDialog(getTopLevelAncestor(), 
										this.getText(), true, chooserPane, this, this);
			chooser.setVisible(true);
		}
		
		//	callback from Dialog ?
		if (evt.getActionCommand().equals("OK"))
		{
			setSurface(chooserPane.getSurface());
			repaint();
			chooser.dispose();
            chooserPane = null;
		}

		if (evt.getActionCommand().equals("cancel"))
		{
			chooser.dispose();
            chooserPane = null;
		}
	}

    protected JoSurfaceChooser createChooser()
    {
        return new JoSurfaceChooser();
    }

	public void paintComponent(Graphics g)
	{
		if (surface==null) {

		}
		else if (surface.useTexture())
		{	
			try {
				TextureCache.paintTexture(g, 0, 0, getWidth(), getHeight(),
											surface.texture, TextureCache.LEVEL_64);
			} catch (FileNotFoundException fnex) {
				//	can't help it
			}
		}
		else
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setPaint(surface.getPaint(getWidth(),getHeight()));
			g2.fillRect(0,0, getWidth(),getHeight());

			if (surface.isDark())
				setForeground(Color.white);
			else
				setForeground(Color.black);
		}
        
		super.paintComponent(g);
	}


	private static final boolean isDark(Color col)
	{
		return (col.getRed()+col.getGreen()+col.getBlue()) <= (3*256/2);
	}

}
