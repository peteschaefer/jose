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

import de.jose.util.Units;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.Language;
import de.jose.Application;

import java.awt.print.Paper;
import java.awt.print.PageFormat;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.dom.Element;

/**
 * Paper with name and Measurement Unit
 *
 * @author Peter Schäfer
 */

public class NamedPaper
        extends Paper
{
	/** pape named (like "A4")  */
	public String name;
	/** width in units (super.getWidth() in Points) */
	public double width, height;
	public int unit;

	public NamedPaper(String name, double width, double height, String unit)
	{
		this.name = name;
		setSize(width,height,Units.toUnit(unit));
	}

	public void setSize(double width, double height, int unit)
	{
		this.width = width;
		this.height = height;
		this.unit = unit;
		updateSuper();
	}


	public void setSize(double width, double height)
	{
		setSize(width,height,this.unit);
	}

	private void updateSuper()
	{
		super.setSize(Units.convert(width,unit,Units.POINT), Units.convert(height,unit,Units.POINT));
	}

	public void setPointSize(double width, double height)
	{
		setSize(Units.convert(width,Units.POINT,this.unit),
		        Units.convert(height,Units.POINT,this.unit));
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer(Language.get(name));
		if (width > 0 && height > 0) {
			buf.append(" (");
			buf.append(Units.toString(width));
			buf.append(" x ");
			buf.append(Units.toString(height));
			buf.append(" ");
			buf.append(Units.toString(unit));
			buf.append(")");
		}
		return buf.toString();
	}

	public void setSize(Object value)
	{
		if (value instanceof NamedPaper) {
			NamedPaper that = (NamedPaper)value;
			setSize(that.width, that.height);
		}
		else if (value instanceof Paper) {
			Paper that = (Paper)value;
			setPointSize(that.getWidth(),that.getHeight());
		}
		else if (value instanceof PageFormat) {
			setSize(((PageFormat)value).getPaper());
		}
		else
			throw new IllegalArgumentException();
	}

	public boolean equals(Object object)
	{
		if (object instanceof NamedPaper) {
			NamedPaper that = (NamedPaper)object;
			return equals(that.width,that.height,that.unit);
		}
		if (object instanceof Paper) {
			Paper that = (Paper)object;
			return equals(that.getWidth(),this.getWidth(),0.01)
			        && equals(that.getHeight(),this.getHeight(),0.01);
		}
		if (object instanceof PageFormat)
			return equals(((PageFormat)object).getPaper());
		throw new IllegalArgumentException();
	}

	public boolean equals(double width, double height, int unit)
	{
		return     (this.unit==unit)
		        && equals(this.width,width,0.01)
		        && equals(this.height,height,0.01);
	}


	protected static boolean equals(double a, double b, double tolerance)
	{
        if (b==0.0)
            return Math.abs(a) < tolerance;
        else
		    return Math.abs(a/b-1.0) < tolerance;
	}

	public static Vector getDefaultPaperFormats()
	{
		Vector collect = new Vector();
		Enumeration elements = Application.theApplication.theConfig.enumerateElements("paper");

		while (elements.hasMoreElements()) {
			Element elm = (Element)elements.nextElement();
			String name = XMLUtil.getChildValue(elm,"name");
			double width = XMLUtil.getChildDoubleValue(elm,"width");
			double height = XMLUtil.getChildDoubleValue(elm,"height");
			String unit = XMLUtil.getChildValue(elm,"unit");

			collect.add(new NamedPaper(name,width,height,unit));
		}
		return collect;
	}

}
