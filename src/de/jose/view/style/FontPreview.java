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

package de.jose.view.style;

import de.jose.util.AWTUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.Style;
import java.awt.*;

public class FontPreview
        extends JComponent
{
    protected FontSample sample;
    protected Style style;
	protected float scale;
	protected boolean antiAlias;

    public FontPreview(String sampleText1, String sampleText2)
    {
        sample = new FontSample(null,sampleText1,sampleText2,0, FontSample.showFont, 2,512);
	    antiAlias = false;
    }

    public void setStyle(Style st, float scale)
    {
        style = st;
	    this.scale = scale;
        repaint();
    }

	public void setAntiAliasing(boolean on)
	{
		antiAlias = on;
	}

    protected void paintComponent(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0,0,getWidth(),getHeight());

		sample.setStyle(style,scale);
        sample.paint(g, AWTUtil.getInsetBounds(this),
                antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                 : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
}
