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

import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.CSS;
import java.awt.*;


/**
 *	More font constants for jose
 *
 * @author Peter Schäfer
 */
public class JoFontConstants
{
	/**	this attribute indicates a style that is used
	 * 	for displaying chess figurines
	 * 	only special font should be used for it
	 *
	 * 	the associated values are Boolean
	 */
	public static final Object Figurine = "figurine";

	public static boolean isFigurine(Style style)
	{
		Boolean value = (Boolean)style.getAttribute(Figurine);
		return (value!=null) && value.booleanValue();
	}

	public static void setFigurine(Style style, boolean isFigurine)
	{
		style.addAttribute(Figurine,Boolean.valueOf(isFigurine));
	}

	/**	this attribute indicates a style that is used
	 * 	for displaying chess diagrams
	 * 	only special font should be used for it
	 *
	 * 	the associated values are Boolean
	 */
	public static final Object Diagram = "diagram";

	public static boolean isDiagram(Style style)
	{
		Boolean value = (Boolean)style.getAttribute(Diagram);
		return (value!=null) && value.booleanValue();
	}

	public static void setDiagram(Style style, boolean isDiagram)
	{
		style.addAttribute(Diagram,Boolean.valueOf(isDiagram));
	}


	/**	this attribute indicates a style that is used
	 * 	for displaying inline diagrams
	 * 	only special font should be used for it
	 *
	 * 	the associated values are Boolean
	 */
	public static final Object Inline = "inline";

	public static boolean isInline(Style style)
	{
		Boolean value = (Boolean)style.getAttribute(Inline);
		return (value!=null) && value.booleanValue();
	}

	public static void setInline(Style style, boolean isInline)
	{
		style.addAttribute(Inline,Boolean.valueOf(isInline));
	}


	/**	this attribute indicates a style that is used
	 * 	for displaying notation symbols
	 * 	only special font should be used for it
	 *
	 * 	the associated values are Boolean
	 */
	public static final Object Symbol = "symbol";

	public static boolean isSymbol(Style style)
	{
		Boolean value = (Boolean)style.getAttribute(Symbol);
		return (value!=null) && value.booleanValue();
	}

	public static void setSymbol(Style style, boolean isSymbol)
	{
		style.addAttribute(Symbol,Boolean.valueOf(isSymbol));
	}


	public static final Object FontScaleFactor = "font-scale";

    public static float getFontScaleFactor(AttributeSet style)
    {
	    Number value = (Number)JoStyleContext.get1Attribute(style,FontScaleFactor);
	    return (value!=null) ? value.floatValue() : 0.0f;
    }

	public static void setFontScaleFactor(MutableAttributeSet style, float factor)
	{
		if (factor != 0.0)
			style.addAttribute(FontScaleFactor, new Float(factor));
		else
			removeFontScaleFactor(style);
	}

	public static void removeFontScaleFactor(MutableAttributeSet style)
	{
		style.removeAttribute(FontScaleFactor);
	}



	public static String getFontFamily(AttributeSet attr)
	{
		return StyleConstants.getFontFamily(attr);
	}

	public static int getFontSize(AttributeSet attr)
	{
		return StyleConstants.getFontSize(attr);
	}

	public static boolean isItalic(AttributeSet attr)
	{
		return StyleConstants.isItalic(attr);
	}

	public static boolean isBold(AttributeSet attr)
	{
		return StyleConstants.isBold(attr);
	}

	public static boolean isUnderline(AttributeSet attr)
	{
		return StyleConstants.isUnderline(attr);
	}

	public static boolean isRightAligned(AttributeSet attr)
	{
		return StyleConstants.getAlignment(attr)==StyleConstants.ALIGN_RIGHT;
	}

	public static boolean isCenterAligned(AttributeSet attr)
	{
		return StyleConstants.getAlignment(attr)==StyleConstants.ALIGN_CENTER;
	}

	public static boolean isSuperscript(AttributeSet attr)
	{
		return StyleConstants.isSuperscript(attr);
	}

	public static boolean isSubscript(AttributeSet attr)
	{
		return StyleConstants.isSubscript(attr);
	}

	public static Color getForeground(AttributeSet attr)
	{
		return StyleConstants.getForeground(attr);
	}
}
