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

import de.jose.Language;
import de.jose.view.style.JoFontConstants;
import de.jose.sax.JoContentHandler;
import de.jose.profile.FontEncoding;

import javax.swing.text.*;

import org.xml.sax.SAXException;

import static de.jose.pgn.INodeConstants.ANNOTATION_NODE;
import static de.jose.pgn.PgnConstants.NAG_MAX;

public class AnnotationNode
		extends Node
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

	protected int code;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public AnnotationNode(int cd)
	{
        super(ANNOTATION_NODE);
		code = cd;
	}

	public AnnotationNode(String text)
	{
		this(PgnUtil.annotationCode(text));
	}

	@Override
	public Node clone() {
		return new AnnotationNode(code);
	}

	public final int getCode()				{ return code; }

	public final void setCode(int code)		{ this.code = code; }

	public String toString()
	{
		return toString(code);
	}

	public static String toString(int code)
	{
		if (code==0)
			return "$";
		String s = PgnUtil.annotationString(code,Language.theLanguage);
		if (s!=null)
			return s;
		else
			return "$"+code;
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return parent().getStyle(doc, "body.symbol");
	}


	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		Style plainStyle = parent().getStyle(doc, "body.line");
		Style symStyle = getDefaultStyle(doc);
		String symFont = JoFontConstants.getFontFamily(symStyle);
		String text = FontEncoding.getSymbol(symFont,code);

		doc.insertString(at," ", plainStyle);
		if (text != null) {
			doc.insertString(at,text, symStyle);
		}
		else {
			//	replace color,bold,italic from symStyle
			Style symTextStyle = doc.addStyle("body.symbol.text", plainStyle);
			StyleConstants.setForeground(symTextStyle, StyleConstants.getForeground(symStyle));
			StyleConstants.setBold(symTextStyle, StyleConstants.isBold(symStyle));
			StyleConstants.setItalic(symTextStyle, StyleConstants.isItalic(symStyle));

			text = toString();
			doc.insertString(at, text, symTextStyle);
		}
		setLength(text.length()+1);
	}

	public void replace(StyledDocument doc, int newCode)
		throws BadLocationException
	{
		int at = getStartOffset();
		doc.remove(at,getLength());

		setCode(newCode);
		insert(doc,at);
	}

	public int canReplace(int from, int to, String newText)
	{
		if (from >= (getLength()-1)) from = getLength()-1;
		if (to >= (getLength()-1)) to = getLength()-1;

		//	1. textual replacement
		StringBuffer text = new StringBuffer(toString());
		text.replace(from,to,newText);
		int nag = PgnUtil.annotationCode(text.toString());
		if (nag>=0) return nag;
		//	2. numerial append
		if (from >= getLength()-1) {
			text.setLength(0);
			text.append(Integer.toString(code));
			text.append(newText);
            try {
                nag = Integer.parseInt(text.toString());
				if (nag>=0 && nag <= NAG_MAX)
					return nag;
            } catch (NumberFormatException e) {
                /**/
            }
        }
		return -1;
	}

	public boolean isCoveredBy(int pos1, int pos2)
	{
		return (pos1 <= 0) && (pos2 >= (getLength()-1));
	}

    /** write binary data   */
    void writeBinary(BinWriter writer) {
        writer.annotation(code);
    }

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		toSAX(code,toString(),handler);
	}

	public static void toSAX(int code, String plainText, JoContentHandler handler) throws SAXException
	{
		Style symStyle = handler.context.styles.getStyle("body.symbol");
		String symFont = JoFontConstants.getFontFamily(symStyle);
		String symText = FontEncoding.getSymbol(symFont,code);
		if (plainText==null) plainText = toString(code);

		handler.startElement("a");
			handler.element("nag",code);
			if (symText!=null) {//  use symbol font (if available)
				handler.element("sym", symText);
				handler.element("chr", Integer.toHexString(symText.charAt(0)));
			}
			handler.element("text",plainText);
		handler.endElement("a");
	}
 }
