/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;

import de.jose.view.style.JoStyleContext;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.swing.text.Style;

public class LineLabelNode
		extends StaticTextNode
{
	public static final int	PREFIX	 = 1;
	public static final int	SUFFIX	 = 2;

	protected int location;

	protected LineLabelNode(int loc)
	{
		super("","body.line","body");
		location = loc;
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return parent().getStyle(doc,styleName);
	}

	public void insert (StyledDocument doc, int at) throws BadLocationException
	{
		style = parent().getStyle(doc,"body.line");
		super.insert (doc, at);
	}

	public String toString(StyledDocument doc)
	{
		if (style==null) {
				style = getDefaultStyle(doc);
				if (style==null)
					style = parent().getStyle(doc,altStyleName);
		}
		return toString();
	}

	public String toString ()
	{
		Node nd;
		boolean newline =  (style!=null) && JoStyleContext.getBooleanAttribute(style,"variation.newline");
		switch (location) {
		case PREFIX:
			if (style!=null)
				text = (String)style.getAttribute("variation.prefix");
			else
				text = " (";
			if (newline) {
				nd = parent().previous();
				if (nd!=null && !nd.is(LINE_NODE))
					text = "\n"+text;
			}
			break;
		case SUFFIX:
			if (style!=null)
				text = (String)style.getAttribute("variation.suffix");
			else
				text = ") ";
			if (newline)
				text = text+"\n";
			break;
		default:
			throw new IllegalStateException();
		}

		if (text==null) text = "";
		return text;
	}
}
