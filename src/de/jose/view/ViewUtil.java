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

package de.jose.view;

import de.jose.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ViewUtil
		extends Util
{
	/**	@return the global coordinates of a MouseEvent
	 */
	public static final Point globalPoint(MouseEvent e)
	{
		/*	e.getPoint() is relative to e.getSource()	*/
		return globalPoint(e.getPoint(), (Component)e.getSource());
	}
	
	public static final Point localPoint(MouseEvent e, Component target)
	{
		return localPoint(globalPoint(e), target);
	}
	
	public static final Point globalPoint(Point p, Component c)
	{
		Point result = c.getLocationOnScreen();
		result.x += p.x;
		result.y += p.y;
		return result;
	}
	
	public static final Point localPoint(Point p, Component c)
	{
		Point result = c.getLocationOnScreen();
		result.x = p.x-result.x;
		result.y = p.y-result.y;
		return result;
	}
	
	public static final Point localPoint(Point p, Component source, Component target)
	{
		return localPoint(globalPoint(p,source),target);
	}
	
	public static final Rectangle globalRect(Rectangle r, Component c)
	{
		Point p = c.getLocationOnScreen();
		return new Rectangle(r.x+p.x, r.y+p.y, r.width, r.height);
	}
	
	public static final Rectangle localRect(Rectangle r, Component c)
	{
		Point p = c.getLocationOnScreen();
		return new Rectangle(r.x-p.x, r.y-p.y, r.width, r.height);
	}
	
	public static final Rectangle localRect(Rectangle r, Component source, Component target)
	{
		Point p1 = source.getLocationOnScreen();
		Point p2 = target.getLocationOnScreen();
		return new Rectangle(r.x+p1.x-p2.x, r.y+p1.y-p2.y, r.width, r.height);
	}
	
	public static final Point center(Rectangle a)
	{
		return new Point(a.x+a.width/2, a.y+a.height/2);
	}
	
	public static final void centerOn(Rectangle a, Point p)
	{
		a.x = p.x-a.width/2;
		a.y = p.y-a.height/2;
	}
	
	public static final void centerOn(Rectangle a, int x, int y)
	{
		a.x = x-a.width/2;
		a.y = y-a.height/2;
	}

	public static final Point topLeft(Rectangle a)
	{
		return new Point(a.x,a.y);
	}

	public static final Point topRight(Rectangle a)
	{
		return new Point(a.x+a.width,a.y);
	}

	public static final Point bottomLeft(Rectangle a)
	{
		return new Point(a.x,a.y+a.height);
	}

	public static final Point bottomRight(Rectangle a)
	{
		return new Point(a.x+a.width,a.y+a.height);
	}

	public static final Point rotate(Point p, double theta)
	{
		double sin = Math.sin(theta);
		double cos = Math.cos(theta);
		return new Point((int)Math.round(p.x*cos-p.y*sin), 
						 (int)Math.round(p.x*sin+p.y*cos));
	}
	
	public static final void drawCentered(Graphics g, String text, Point p)
	{
		drawCentered(g,text,p.x,p.y);
	}
	
	public static void drawCentered(Graphics g, String text, int x, int y)
	{
		FontMetrics fmx = g.getFontMetrics();
		Rectangle bounds = new Rectangle(0,0, 
				fmx.stringWidth(text), 
				fmx.getAscent()-fmx.getLeading()-fmx.getDescent());
		/*		this calculation is not quite correct, of course
				but it eliminates the space above the characters
		*/
		//	center bounds on box;
		centerOn(bounds,x,y);
		g.drawString(text, bounds.x, bounds.y+bounds.height);
	}
	
	public static final void inset(Rectangle r, int dx, int dy)
	{
		r.x += dx;
		r.y += dy;
		r.width -= 2*dx;
		r.height -= 2*dy;
	}
	
	public static final void inset(Rectangle r, float dx, float dy)
	{
		inset(r, (int)(r.width*dx), (int)(r.height*dy));
	}
	
	public static final boolean hitClip(Graphics g, Rectangle r)
	{
		return g.hitClip(r.x,r.y,r.width,r.height);
	}

	public static final void revalidate(JComponent comp) 
	{
		RepaintManager rpm = RepaintManager.currentManager(comp);
		if (rpm!=null)
			rpm.removeInvalidComponent(comp);
	}
}


