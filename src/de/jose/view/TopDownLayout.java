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

/**
 *  @author Peter Schäfer
 */
public class TopDownLayout
        implements LayoutManager2
{
	public void layoutContainer(Container parent)
	{
		int width = parent.getWidth();
		int y = 0;
		Component lastComponent = null;

		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component comp = parent.getComponent(i);
			if (! comp.isVisible()) continue;
			lastComponent = comp;
			Dimension pref = comp.getPreferredSize();
			comp.setBounds(0,y, width, pref.height);
			y += comp.getHeight();
		}

		if (y >= parent.getHeight())
		{
			//  reduce to minimum heigth
			y = 0;
			for (int i=0; i < parent.getComponentCount(); i++)
			{
				Component comp = parent.getComponent(i);
				if (! comp.isVisible()) continue;
				Dimension min = comp.getMinimumSize();
				comp.setBounds(0,y, width, min.height);
				y += comp.getHeight();
			}
		}

		//  expend remaining space to last component
		if ((y < parent.getHeight()) && lastComponent!=null)
			lastComponent.setSize(width, lastComponent.getHeight()+parent.getHeight()-y);
	}


	public Dimension minimumLayoutSize(Container parent)
	{
		int width = 0;
		long height = 0;
		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component comp = parent.getComponent(i);
			Dimension min = comp.getMinimumSize();
			width = Math.max(width,min.width);
			height += min.height;
		}
		if (height > Integer.MAX_VALUE) height = Integer.MAX_VALUE;
		return new Dimension(width,(int)height);
	}

	public Dimension preferredLayoutSize(Container parent)
	{
		int width = 0;
		long height = 0;
		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component comp = parent.getComponent(i);
			Dimension min = comp.getPreferredSize();
			width = Math.max(width,min.width);
			height += min.height;
		}
		if (height > Integer.MAX_VALUE) height = Integer.MAX_VALUE;
		return new Dimension(width,(int)height);
	}

	public Dimension maximumLayoutSize(Container parent)
	{
		int width = 0;
		long height = 0L;
		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component comp = parent.getComponent(i);
			Dimension max = comp.getMaximumSize();
			width = Math.max(width,max.width);
			height += max.height;
		}
		if (height > Integer.MAX_VALUE) height = Integer.MAX_VALUE;
		return new Dimension(width,(int)height);
	}

	public void removeLayoutComponent(Component comp)   	{ /* no-op */	}

	public float getLayoutAlignmentX(Container target)      { return 0;	}

	public float getLayoutAlignmentY(Container target)      { return 0;	}

	public void invalidateLayout(Container target)      	{ /* no-op */ }

	public void addLayoutComponent(Component comp, Object constraints)  { /* no-op */ }

	public void addLayoutComponent(String name, Component comp)	{ /* no-op */  }
}
