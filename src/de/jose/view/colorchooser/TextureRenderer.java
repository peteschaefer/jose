/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view.colorchooser;

import de.jose.image.TextureCache;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;

/**
 * a list cell renderer that displays textures
 */

public class TextureRenderer 
	extends Component 
	implements ListCellRenderer 
{
	private String current;
	
	public TextureRenderer()
	{
		setSize(48,48);
	}
	
     // This is the only method defined by ListCellRenderer.
     // We just reconfigure the JLabel each time we're called.

     public Component getListCellRendererComponent(JList list, Object value, int index, 
												   boolean isSelected, boolean cellHasFocus)
     {
		current = value.toString();
		
   		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
        else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		return this;
     }
	 
	 public void paint(Graphics g)
	 {
		g.setColor(getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		
		try {
			TextureCache.paintTexture(g, 4, 4, getWidth()-8, getHeight()-8, current, TextureCache.LEVEL_64);
		} catch (FileNotFoundException fnex) {
			g.drawString(current+" not found", 0,getHeight()-4);
		}
	 }
 }
