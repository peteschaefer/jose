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

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 *
 */

public class CornerLayout
		implements LayoutManager2
{
	
	public static final Point2D NORTH		=  new Point2D.Float (0.5f, 0.0f);
	public static final Point2D SOUTH		=  new Point2D.Float (0.5f, 1.0f);
	public static final Point2D EAST		=  new Point2D.Float (1.0f, 0.5f);
	public static final Point2D WEST		=  new Point2D.Float (0.0f, 0.5f);
	public static final Point2D NORTH_EAST	=  new Point2D.Float (1.0f, 1.0f);
	public static final Point2D NORTH_WEST	=  new Point2D.Float (0.0f, 0.0f);
	public static final Point2D SOUTH_EAST	=  new Point2D.Float (1.0f, 1.0f);
	public static final Point2D SOUTH_WEST	=  new Point2D.Float (0.0f, 1.0f);
	public static final Point2D CENTER		=  new Point2D.Float (0.5f, 0.5f);
	
	protected static final Dimension DEFAULT_MIN_SIZE	= new Dimension(0,0);
	protected static final Dimension DEFAULT_MAX_SIZE	= new Dimension(Short.MAX_VALUE,Short.MAX_VALUE);
	
	/**	maps Components to constraints	 */
	protected HashMap constraints;

	public static class Constraint
	{
		/**			 */
		float xalign, yalign;
		
		/**	offset		 */
		int xoffset, yoffset;
		
		public Constraint(float horizontal, float vertical, int hoffset, int voffset)
		{
			xalign = horizontal;
			yalign = vertical;
			xoffset = hoffset;
			yoffset = voffset;
		}
		
		public Constraint(float horizontal, float vertical)			{ this(horizontal,vertical,0,0); }
		public Constraint(Point2D align)							{ this((float)align.getX(), (float)align.getY()); }	
		public Constraint(Point2D align, Point offset)				{ this((float)align.getX(), (float)align.getY(), 
																		   offset.x, offset.y); }
		public Constraint(Point2D align, int xoff, int yoff)		{ this((float)align.getX(), (float)align.getY(), 
																		   xoff, yoff); }
	}
	
	public CornerLayout()
	{
		constraints = new HashMap();
	}
	
	public void addLayoutComponent(String name, Component comp) 
	{	}
	
	public void addLayoutComponent(Component comp, Object constraint)
	{
		constraints.put(comp,constraint);
	}

	public void removeLayoutComponent(Component comp)
	{	}

	/**
	 * @return the max. size (i.e. the min. of all components)
	 */
	public Dimension maximumLayoutSize(Container target)
	{
		Dimension s = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		int dx, dy;
		
		for (int i = target.getComponentCount()-1; i >= 0; i--)
		{
			Component comp = target.getComponent(i);
			Dimension d = comp.getMaximumSize();
			
			dx = d.width + getXOffset(comp);
			dy = d.height + getYOffset(comp);
			
			if (dx < s.width) s.width = dx;
			if (dy < s.height) s.height = dy;
		}
		
		return s;
	}

	/**
	 * @return the min. size (i.e. the max. of all components)
	 */
	public Dimension minimumLayoutSize(Container target)
	{
		Dimension s = new Dimension(0,0);
		int dx, dy;
		
		for (int i = target.getComponentCount()-1; i >= 0; i--)
		{
			Component comp = target.getComponent(i);
			Dimension d = comp.getMinimumSize();
			
			dx = d.width + getXOffset(comp);
			dy = d.height + getYOffset(comp);
			
			if (dx > s.width) s.width = dx;
			if (dy > s.height) s.height = dy;
		}
		
		return s;
	}

	public Dimension preferredLayoutSize(Container target)
	{
		Dimension s = new Dimension(0,0);
		int dx, dy;
		
		for (int i = target.getComponentCount()-1; i >= 0; i--)
		{
			Component comp = target.getComponent(i);
			Dimension d = comp.getPreferredSize();
			
			dx = d.width + getXOffset(comp);
			dy = d.height + getYOffset(comp);
			
			if (dx > s.width) s.width = dx;
			if (dy > s.height) s.height = dy;
		}
		
		return s;
	}
	
	public void layoutContainer(Container parent)
	{
		Dimension sz = parent.getSize();
		
		for (int i=parent.getComponentCount()-1; i >= 0; i--)
		{
			Component comp = parent.getComponent(i);
			
			layoutComponent (comp, sz);
		}
	}
	
	public float getLayoutAlignmentX(Container target)
	{
		return 0.5f;
	}
	
	public float getLayoutAlignmentY(Container target)
	{
		return 0.5f;
	}
	
	public void invalidateLayout(Container target)
	{
	}
	
	protected float getXAlign(Component comp)
	{
		Object constraint = constraints.get(comp);
		if (constraint == null) 
			return 0.5f;
		if (constraint instanceof Constraint) 
			return ((Constraint)constraint).xalign;
		if (constraint instanceof Point2D) 
			return (float)((Point2D)constraint).getX();
		throw new IllegalArgumentException("unknown constraint: "+constraint.getClass());
	}
	
	protected float getYAlign(Component comp)
	{
		Object constraint = constraints.get(comp);
		if (constraint == null) 
			return 0.5f;
		if (constraint instanceof Constraint) 
			return ((Constraint)constraint).yalign;
		if (constraint instanceof Point2D) 
			return (float)((Point2D)constraint).getY();
		throw new IllegalArgumentException("unknown constraint: "+constraint.getClass());
	}
	
	protected int getXOffset(Component comp)
	{
		Object constraint = constraints.get(comp);
		if (constraint!=null &&
			constraint instanceof Constraint) 
			return ((Constraint)constraint).xoffset;
		else
			return 0;
	}
	
	protected int getYOffset(Component comp)
	{
		Object constraint = constraints.get(comp);
		if (constraint!=null &&
			constraint instanceof Constraint) 
			return ((Constraint)constraint).yoffset;
		else
			return 0;
	}
	
	protected void layoutComponent(Component comp, Dimension parentSize)
	{
		Dimension minSize = comp.getMinimumSize();
		if (minSize == null) minSize = DEFAULT_MIN_SIZE;
		
		Dimension maxSize = comp.getMaximumSize();
		if (maxSize == null) maxSize = DEFAULT_MAX_SIZE;
		
		if (maxSize.width > parentSize.width) maxSize.width = parentSize.width;
		if (maxSize.height > parentSize.height) maxSize.height = parentSize.height;
		
		Dimension size = comp.getPreferredSize();
		if (size == null) size = parentSize;
		
		if (size.width > maxSize.width) size.width = maxSize.width;
		if (size.width < minSize.width) size.width = minSize.width;
		
		if (size.height > maxSize.height) size.height = maxSize.height;
		if (size.height < minSize.height) size.height = minSize.height;
		
		float xalign = getXAlign(comp);
		float yalign = getYAlign(comp);
		
		int xoffset = getXOffset(comp);
		int yoffset = getYOffset(comp);
		
		int x = (int)(xalign*parentSize.width
					- xalign*size.width
					+ xoffset + 0.5f);

		int y = (int)(yalign*parentSize.height
					- yalign*size.height
					+ yoffset + 0.5f);
		
		comp.setBounds(x,y, size.width,size.height);
	}
}
