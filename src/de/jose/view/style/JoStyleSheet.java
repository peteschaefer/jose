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
import de.jose.util.FontUtil;

import javax.swing.text.html.StyleSheet;
import javax.swing.text.AttributeSet;
import java.awt.*;

/**
 * JoStyleSheet
 * 
 * @author Peter Schäfer
 */

public class JoStyleSheet
        extends StyleSheet
{
	protected float fontScale;

	public float getFontScale()     	        { return fontScale; }

	public void setFontScale(float fontScale)   { this.fontScale = fontScale; }

	public void setNormFontScale()              { setFontScale((float)AWTUtil.getNormalizingTransform().getScaleX()); }

	public void setScreenResolution(float dpi)  { setFontScale(dpi/72.0f); }


	public JoStyleSheet()
	{
		super();
		setNormFontScale();
	}

	/**
	 * get sclaed font for a specific screen resolution
	 *
	 * @param attr
	 * @param dpi
	 * @return
	 */
	public Font getFont(AttributeSet attr, float dpi)
	{
		// PENDING(prinz) add cache behavior
		int style = Font.PLAIN;
		if (JoFontConstants.isBold(attr))
		    style |= Font.BOLD;
		if (JoFontConstants.isItalic(attr))
		    style |= Font.ITALIC;
		String family = JoFontConstants.getFontFamily(attr);
		int size = JoFontConstants.getFontSize(attr);

		/**
		 * if either superscript or subscript is
		 * is set, we need to reduce the font size
		 * by 2.
		 */
		if (JoFontConstants.isSuperscript(attr) ||
			JoFontConstants.isSubscript(attr)) {
			size -= 2;
		}

		return getFont(family, style, size, dpi);
	}


	/**
	 * get sclaed font for a specific screen resolution
	 *
	 * @param dpi
	 * @return
	 */
	public Font getFont(String family, int style, float size, float dpi)
	{
		float scale = dpi/72.0f;
		return FontUtil.newFont(family, style, scale*size);
	}

	public Font getFont(String family, int style, float size)
	{
		if (fontScale <= 0.0f) fontScale = 1.0f;
		return FontUtil.newFont(family, style, fontScale*size);
	}

	public Font getFont(String family, int style, int size)
	{
		return getFont(family,style,(float)size);
	}

	public int getPixelSize(float ptsize)
	{
		return Math.round(ptsize * fontScale);
	}

	public int getPixelSize(AttributeSet set)
	{
		return getFont(set).getSize();
	}

}