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

package de.jose.pgn;

import de.jose.sax.JoContentHandler;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xml.sax.SAXException;

public class ResultNode
		extends Node
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

	protected int result;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public ResultNode(int res)
	{
        super(RESULT_NODE);
		result = res;
	}

	
	public final int getResult()				{ return result; }

	public final void setResult(int res)		{ result = res; }

	public Style getDefaultStyle(StyledDocument doc)
	{
		return doc.getStyle("body.result");
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at) 
		throws BadLocationException
	{
		if (result==PgnConstants.RESULT_UNKNOWN)    {
			setLength(0);
		}
		else {
			Style style = getDefaultStyle(doc);
			String text = toString()+" ";
			doc.insertString(at, text, style);
			setLength(text.length());
		}
	}

	public void setResult(int res, StyledDocument doc) throws BadLocationException
	{
		if (res!=result) {
			remove(doc);
			result = res;
			insert(doc,getStartOffset());
		}
	}

	public String toString() {
		return PgnUtil.resultString(result);
	}

    void writeBinary(BinWriter writer) {
        writer.result(result);
    }

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		toSAX(result,toString(),handler);
	}

	public static void toSAX(int result, String str, JoContentHandler handler) throws SAXException
	{
		if (str==null) str = PgnUtil.resultString(result);

		handler.element("result",str);
	}
}
