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

package de.jose.view.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * paints an outline during a Drag & Drop operation
 *
 */
public class GameTransferIcon
        implements Icon
{
	protected Dimension size;

	public GameTransferIcon(int width, int height) {
		size = new Dimension(width,height);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.draw3DRect(x,y,size.width,size.height,false);
	}

	public int getIconWidth() {
		return size.width;
	}

	public int getIconHeight() {
		return size.height;
	}
}
