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

package de.jose.util.print;

import de.jose.Util;

import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class Triplet
{
	public String family;
	public boolean bold,italic;

	public Triplet(String family, boolean bold, boolean italic) {
		this.family = family;
		this.bold = bold;
		this.italic = italic;
	}

	public Triplet(String family, int style) {
		this(family,Util.allOf(style,Font.BOLD),Util.allOf(style,Font.ITALIC));
	}

	public Triplet(Font font) {
		this(font.getFamily(), font.getStyle());
	}

	public boolean equals(Object obj) {
		if (obj instanceof Triplet) {
			Triplet that = (Triplet)obj;
			return this.family.equalsIgnoreCase(that.family)
			        && (this.bold==that.bold)
			        && (this.italic==that.italic);
		}
		if (obj instanceof Font) {
			Font that = (Font)obj;
			return this.family.equalsIgnoreCase(that.getFamily())
					&& (this.bold==Util.allOf(that.getStyle(),Font.BOLD))
			        && (this.italic==Util.allOf(that.getStyle(),Font.ITALIC));
		}
		if (obj instanceof String)
			return this.toString().equalsIgnoreCase((String)obj);
		throw new IllegalArgumentException();
	}

	public int hashCode() {
		int result = family.hashCode();
		if (bold) result ^= 0x00001000;
		if (italic) result ^= 0x00080000;
		return result;
	}


	public String toString()        { return toString(family,bold,italic); }

	public static String toString(String family, boolean bold, boolean italic)
	{
		if (bold&&italic) return family+" BI";
		if (bold) return family+" B";
		if (italic) return family+" I";
		return family;
	}

	public static String toString(String family, int style)
	{
		return toString(family, Util.allOf(style,Font.BOLD), Util.allOf(style,Font.ITALIC));
	}

	public static String toString(Font font)
	{
		return toString(font.getFamily(), font.getStyle());
	}

}
