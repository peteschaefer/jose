/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util.print;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Drawable
 * 
 * @author Peter Schäfer
 */

public interface Drawable
{
	public Rectangle2D getBounds();

	public void draw(Graphics2D g, Rectangle2D bounds);
}