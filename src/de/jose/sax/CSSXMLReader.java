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

package de.jose.sax;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.awt.print.PageFormat;
import java.awt.*;
import java.sql.SQLException;
import java.util.Enumeration;

import de.jose.Application;
import de.jose.Util;
import de.jose.export.ExportContext;
import de.jose.view.style.JoStyleContext;
import de.jose.view.style.JoFontConstants;

import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;
import javax.swing.text.MutableAttributeSet;

/**
 * @author Peter Schäfer
 */

public class CSSXMLReader extends AbstractObjectReader
{
	protected ExportContext context;

	protected static AttributesImpl INHERITED = new AttributesImpl();
	static {
		INHERITED.addAttribute(null,"inherited","inherited","CDATA",null);
	};

	public CSSXMLReader(ExportContext context)
	{
		this.context = context;
	}

	public void parse(InputSource input) throws IOException, SAXException
	{
//		if (games == null)
//			throw new NullPointerException("Parameter GameSource must not be null");
		if (handler == null)
			throw new IllegalStateException("ContentHandler not set");

//Start the document
		handler.setContext(this.context);
		handler.startDocument();
		handler.startElement("jose-export");

		//  style info
		toSAX(context.styles,handler);

//End the document
		handler.endElement("jose-export");
		handler.endDocument();
	}

	public static void toSAX(JoStyleContext styles, JoContentHandler handler) throws SAXException
	{
		handler.startElement("styles");
			toSAX((StyleContext.NamedStyle)styles.getStyle("base"),handler);
		handler.endElement("styles");
	}

	private static void toSAX(StyleContext.NamedStyle style, JoContentHandler handler) throws SAXException
	{
		handler.startElement("style");
			handler.element("name", style.getName());
			//  dump attributes
			Enumeration attrNames = style.getAttributeNames();
			while (attrNames.hasMoreElements())
			{
				Object key = attrNames.nextElement();
				//  don't dump these attributes:
				if (key.equals(StyleConstants.ResolveAttribute)) continue;
				if (key.equals(StyleConstants.NameAttribute)) continue;
				if (key.equals("children")) continue;

				Object value = style.getAttribute(key);
				if (value instanceof Color)
					handler.keyValue("a",key.toString(), toString((Color)value));
				else
					handler.keyValue("a",key.toString(), value.toString());
			}

			//  dump /some/ inherited attributes
			//  Font Family, Size, Style, Weight, Color
			if (!style.isDefined(StyleConstants.FontConstants.Family))
				handler.keyValue("a",INHERITED,
						StyleConstants.FontConstants.Family.toString(),
						JoFontConstants.getFontFamily(style));
			if (!style.isDefined(StyleConstants.FontConstants.Size))
				handler.keyValue("a",INHERITED,
						StyleConstants.FontConstants.Size.toString(),
						String.valueOf(JoFontConstants.getFontSize(style)));
			if (!style.isDefined(StyleConstants.FontConstants.Bold))
				handler.keyValue("a",INHERITED,
						StyleConstants.FontConstants.Bold.toString(),
						Boolean.toString(StyleConstants.FontConstants.isBold(style)));
			if (!style.isDefined(StyleConstants.FontConstants.Italic))
				handler.keyValue("a",INHERITED,
						StyleConstants.FontConstants.Italic.toString(),
						Boolean.toString(StyleConstants.FontConstants.isItalic(style)));
			if (!style.isDefined(StyleConstants.ColorConstants.Foreground))
				handler.keyValue("a",INHERITED,
						StyleConstants.ColorConstants.Foreground.toString(),
						toString(StyleConstants.ColorConstants.getForeground(style)));

			//  dump children
			java.util.List children = JoStyleContext.getChildren(style);
			if (children != null)
				for (int i=0; i < children.size(); i++) {
					StyleContext.NamedStyle child = (StyleContext.NamedStyle)children.get(i);
					toSAX(child,handler);
				}

		handler.endElement("style");
	}

	private static String toString(Color color)
	{
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		StringBuffer buf = new StringBuffer("#");
		buf.append(Integer.toHexString(red/16));
		buf.append(Integer.toHexString(red%16));
		buf.append(Integer.toHexString(green/16));
		buf.append(Integer.toHexString(green%16));
		buf.append(Integer.toHexString(blue/16));
		buf.append(Integer.toHexString(blue%16));
		return buf.toString();
	}
}
