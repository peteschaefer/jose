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

import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class JoLineBorder
        extends EmptyBorder
{
	private Color lightColor,darkColor;
	private int paintBorder;
	private int thickness;

	public static final int TOP     = 0x01;
	public static final int LEFT    = 0x02;
	public static final int BOTTOM  = 0x04;
	public static final int RIGHT   = 0x08;

	public static final int NONE    = 0;
	public static final int ALL     = TOP+LEFT+BOTTOM+RIGHT;


	public JoLineBorder(int paintBorder, int thickness,
	                    int paddingTop, int paddingLeft, int paddingBottom, int paddingRight)
	{
		this(paintBorder,
		        null,null,
		        thickness,
		        paddingTop,paddingLeft,paddingBottom,paddingRight);
	}

	public JoLineBorder(int paintBorder, Color color, int thickness,
	                    int paddingTop, int paddingLeft, int paddingBottom, int paddingRight)
	{
		this (paintBorder,
		        (color==null) ? null:color.brighter(),
		        (color==null) ? null:color.darker(),
		        thickness,
		        paddingTop,paddingLeft,paddingBottom,paddingRight);
	}

	public JoLineBorder(int paintBorder, Color lightColor, Color darkColor, int thickness,
						int paddingTop, int paddingLeft, int paddingBottom, int paddingRight)
	{
		super(paddingTop,paddingLeft,paddingBottom,paddingRight);
		this.lightColor = lightColor;
		this.darkColor = darkColor;
		this.paintBorder = paintBorder;
		this.thickness =  thickness;
	}

    /**
     * Paints the border for the specified component with the
     * specified position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Color oldColor = g.getColor();
        int i;

	    Color lightColor;
	    if (this.lightColor!=null)
	        lightColor = this.lightColor;
	    else
	        lightColor = c.getBackground().darker();

	    Color darkColor;
	    if (this.darkColor!=null)
		    darkColor = this.darkColor;
	    else
		    darkColor = c.getBackground().darker();

        for(i = 0; i < thickness; i++)
        {
	        if (Util.anyOf(paintBorder,TOP))    {
		        g.setColor(darkColor);
		        g.drawLine(x, y+i, x+width, y+i);
	        }


	        if (Util.anyOf(paintBorder,LEFT)) {
		        g.setColor(darkColor);
		        g.drawLine(x+i, y, x+i, y+height);
	        }


	        if (Util.anyOf(paintBorder,BOTTOM)) {
		        g.setColor(lightColor);
		        g.drawLine(x, y+height-1-i, x+width, y+height-1-i);
	        }

	        if (Util.anyOf(paintBorder,RIGHT))  {
		        g.setColor(lightColor);
		        g.drawLine(x+width-1-i, y, x+width-1-i,  y+height);
	        }
        }
        g.setColor(oldColor);
    }
}
