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
import java.awt.geom.Rectangle2D;

public class DockingZone
		extends JComponent
{
	protected Component target;
	protected char orientation;

	protected Point hotSpot;
	protected boolean isActive;

	protected Point startPoint;
	protected Color color;
	protected String text;

	protected static final Color SHADOW_64  = new Color(0,0,0, 64);
	protected static final Color RED_64     = new Color(255,0,0, 64);

	public DockingZone(Component aTarget, char anOrientation,
					   Container glassPane, Component source,
					   Point start, Color color, String text)
	{
		target = aTarget;
		orientation = anOrientation;

		hotSpot = ((JoComponent)target).getDockingSpot(orientation);
		hotSpot = ViewUtil.globalPoint(hotSpot, target);
		startPoint = start;
		this.color = color;
		this.text = text;

		glassPane.add(this);
		setBounds(calcBounds(source, glassPane));

		isActive = false;
	}

	public void setActive(boolean active)
	{
		isActive = active;
		repaint();
	}

	public void moveTo(Point p2)
	{
		Rectangle bounds = getBounds();
		bounds.translate(p2.x-startPoint.x, p2.y-startPoint.y);
		setBounds(bounds);
		startPoint = p2;
	}

	public Point getOffsetFrom (Point orig)
	{
		return new Point(startPoint.x-orig.x, startPoint.y-orig.y);
	}

	public void paintComponent(Graphics g)
	{
		if (isActive) {
			g.setColor(color);
//			g.fillRect(0,0, getWidth(), getHeight());
			for (int i=0; i<12; i++)
				g.drawRect(i,i, getWidth()-2*i, getHeight()-2*i);

			//  show text
//			g.setColor(Color.black);
			Rectangle2D strbounds = g.getFontMetrics().getStringBounds(text,g);
			Point p = new Point();
			p.x = (int)Math.round((getWidth()-strbounds.getWidth())/2);
			p.y = (int)Math.round((getHeight()-strbounds.getHeight())/2);

			g.drawString(text,p.x,p.y);
		}

		//show hotspot
		g.setColor(color);
		Point p = ViewUtil.localPoint(hotSpot,this);
//		g.setColor(Color.red);
//		g.drawRect(p.x-4,p.y-4, 8,8);
		g.fillOval(p.x-6,p.y-6,12,12);
	}

	public boolean containsGlobal(Point p) {
		Point o = ViewUtil.globalPoint(getLocation(), getParent());
		/*	note that we can not call globalPoint(..this)
			while this component is hidden
		*/
		return	(p.x >= o.x) && (p.x < (o.x+getWidth())) &&
				(p.y >= o.y) && (p.y < (o.y+getHeight()));
	}

	public double hotSpotDistance(Point p)
	{
		switch (orientation) {
		case JoComponent.DOCK_NORTH:
	    case JoComponent.DOCK_SOUTH:
				//  allow more horizontal tolerance
				return Util.square(p.x-hotSpot.x)/12 + Util.square(p.y-hotSpot.y);
		case JoComponent.DOCK_EAST:
		case JoComponent.DOCK_WEST:
				//  allow more vertical tolerance
				return Util.square(p.x-hotSpot.x) + Util.square(p.y-hotSpot.y)/12;
		default:
		case JoComponent.DOCK_CENTER:
				return Util.square(p.x-hotSpot.x) + Util.square(p.y-hotSpot.y);
		}
	}

	protected Rectangle calcBounds(Component source, Component parent) {
		Rectangle r = new Rectangle(0,0, target.getWidth(), target.getHeight());
		r = ViewUtil.localRect(r, target, parent);

		int width = r.width;
		int height = r.height;

		Dimension min = source.getMinimumSize();
		Dimension max = source.getMaximumSize();

		if (orientation==JoComponent.DOCK_EAST || orientation==JoComponent.DOCK_WEST) {
			double sourcew = ((JoComponent)source).getWeightX();
			double targetw = ((JoComponent)target).getWeightX();
			double sum = sourcew+targetw;
			double weight = (sum==0.0) ? 0.5 : (sourcew/sum);
			width = (int)Math.round(width*weight);

			if (max!=null && width > max.width) width = max.width;
			if (min!=null && width < min.width) width = min.width;
		}
		else {
			double sourcew = ((JoComponent)source).getWeightY();
			double targetw = ((JoComponent)target).getWeightY();
			double sum = sourcew+targetw;
			double weight = (sum==0.0) ? 0.5 : (sourcew/sum);
			height = (int)Math.round(height*weight);

			if (max!=null && height > max.height) height = max.height;
			if (min!=null && height < min.height) height = min.height;
		}

		switch (orientation) {
		case JoComponent.DOCK_SOUTH:	r.y += (r.height-height);	//	fall-through intended
		case JoComponent.DOCK_NORTH:	r.height = height;
										break;
		case JoComponent.DOCK_EAST:		r.x += (r.width-width);		//	fall-through intended
		case JoComponent.DOCK_WEST:		r.width = width;
										break;
		}
		return r;
	}
}
