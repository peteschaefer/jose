/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;

import de.jose.sax.JoContentHandler;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xml.sax.SAXException;

import static de.jose.pgn.INodeConstants.STATIC_TEXT_NODE;

public abstract class StaticTextNode
		extends Node
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

	protected String text;
	protected String styleName,altStyleName;
    protected Style style;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public StaticTextNode(String txt, String styleName, String altStyleName)
	{
        super(STATIC_TEXT_NODE);
		setText(txt,styleName,altStyleName);
	}

	public StaticTextNode(char[] c, int start, int len, String styleName, String altStyleName)
	{
		this(new String(c,start,len), styleName, altStyleName);
	}

	public String toString()		{ return text; }

	public void setText(String atext, Style astlye)
    {
        text = atext;
        styleName = altStyleName = null;
        style = astlye;
    }

    public void setText(String atext, String astyle, String altstyle)
    {
        text = atext;
        styleName = astyle;
	    altStyleName = altstyle;
        style = null;
    }

	public void setStyle(String astyle, String altstyle)
	{
		styleName = astyle;
		altStyleName = altstyle;
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		Style result = null;
		if (styleName!=null)
			result = parent().getStyle(doc,styleName);
		if (result==null && altStyleName!=null)
			result = parent().getStyle(doc,altStyleName);
		return result;
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		String text = toString();
		if (text==null || text.length()==0) {
			setLength(0);
		}
		else {
			if (style==null) style = getDefaultStyle(doc);
			doc.insertString(at, text, style);
			setLength(text.length());
		}
	}

    /** write binary data   */
    void writeBinary(BinWriter writer) {
        /* no-op */
    }

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		//  don't print static content to SAX document, or should we ?
		//  NOOP
	}
 }

