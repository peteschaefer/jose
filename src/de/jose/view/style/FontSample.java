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

import de.jose.Util;
import de.jose.profile.FontEncoding;
import de.jose.util.StringUtil;
import de.jose.util.FontUtil;

import javax.swing.text.Style;
import java.awt.*;


/**
 * User: peter.schaefer
 * Date: 13.03.2003
 * Time: 12:28:45
 *
 *
 * @author Peter Schäfer
 */
public class FontSample
{
	static final int    showFont            = 0x01;
	static final int    showDiagramSample   = 0x02;
	static final int    showFigurineSample  = 0x03;

	int flags;
	String fontName;
	int fontSize, fontStyle;
	Color fontColor;
	String fontText;
	String sampleText;
	Font font;
	int minFontSize, maxFontSize;
	float fontScale;

	FontSample(String aFontName, String aFontText, String aSampleText,
	           int size, int theFlags,
	           int minSize, int maxSize)
	{
		this(minSize,maxSize);

		fontName = aFontName;
		fontText = aFontText;
		sampleText = aSampleText;
		flags = theFlags;
		fontStyle = Font.PLAIN;
		fontSize = size;
		fontColor = Color.black;
		fontScale = 1.0f;
	}

	FontSample(String aFontName, String aFontText, String aSampleText,
	           int size, int theFlags)
	{
		this(aFontName,aFontText, aSampleText, size,theFlags, 7,30);
	}

	FontSample(int minSize, int maxSize)
	{
		minFontSize = minSize;
		maxFontSize = maxSize;
	}

	public void setStyle(Style style, float scale, String name, int nestLevel)
	{
		fontText = name;
//        sampleText = Language.get(fontText);
		if (nestLevel > 0)
			fontText = StringUtil.blanks(nestLevel,' ')+fontText;
		setStyle(style,scale);
	}

	public void setStyle(Style style, float scale)
    {
        fontName = JoFontConstants.getFontFamily(style);
        fontSize = JoFontConstants.getFontSize(style);
        fontStyle = Font.PLAIN;
        if (JoFontConstants.isBold(style))
            fontStyle |= Font.BOLD;
        if (JoFontConstants.isItalic(style))
            fontStyle |= Font.ITALIC;

        fontColor = JoFontConstants.getForeground(style);

        if (JoFontConstants.isFigurine(style))
            flags = showFigurineSample;
        else if (JoFontConstants.isDiagram(style) || JoFontConstants.isInline(style))
            flags = showDiagramSample;
        else
            flags = showFont;

		font = null;
	    fontScale = scale;
    }

	public void paint(Graphics g, Rectangle bounds, Object antialiasMode)
	{
		Object oldMode = null;
		try {
			if (antialiasMode != null) {
				Graphics2D g2 = (Graphics2D)g;
				oldMode = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
				if (Util.equals(antialiasMode,oldMode))
					oldMode = null;
				else
					g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,antialiasMode);
			}

			if (font==null)
			switch (flags) {
			case FontSample.showFont:
					fontSize = Util.nvl(fontSize,12);
					font = FontUtil.newFont(fontName,fontStyle,
					                fontScale*Util.inBounds(minFontSize,fontSize,maxFontSize));
					if (font.canDisplayUpTo(fontText) >= 0)
						flags = 0;  //  can't display font text ;-(
					break;

			case FontSample.showDiagramSample:
					fontSize = Util.nvl(fontSize,24);
					font = FontUtil.newFont(fontName,fontStyle,
					                fontScale*Util.inBounds(minFontSize,fontSize,maxFontSize));
					FontEncoding enc = FontEncoding.getEncoding(fontName);
					sampleText = enc.getDiagramSampleString(true);
					if (font.canDisplayUpTo(sampleText) >= 0)
						flags = 0;  //  can't display sampleText ;-(
					break;

			case FontSample.showFigurineSample:
					fontSize = Util.nvl(fontSize,24);
					font = FontUtil.newFont(fontName,fontStyle,
					                fontScale*Util.inBounds(minFontSize,fontSize,maxFontSize));
					enc = FontEncoding.getEncoding(fontName);
					sampleText = enc.getFigurineSampleString();
					if (sampleText==null || font.canDisplayUpTo(sampleText) >= 0) {
						sampleText = enc.getDiagramSampleString(false);
						if (sampleText==null || font.canDisplayUpTo(sampleText) >= 0)
							flags = 0;  //  can't display sampleText ;-(
					}
					break;
			}

			Font textFont = null;
			if (flags != 0)
				textFont = g.getFont();

			if (flags==FontSample.showFont)
				g.setFont(font);

			g.setColor(fontColor);

			int x = 4;
			FontMetrics fmx = g.getFontMetrics();
			g.drawString(fontText, x, (bounds.height+fmx.getAscent())/2);
			x += fmx.stringWidth(fontText);

			if (flags==FontSample.showDiagramSample || flags==FontSample.showFigurineSample) {
				g.setFont(font);
				fmx = g.getFontMetrics();
				g.drawString(sampleText, x+8, (bounds.height+fmx.getAscent()-fmx.getDescent())/2);
			}

			if (textFont != null) g.setFont(textFont);
		} finally {
			if (oldMode!=null)
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldMode);
		}
	}


}
