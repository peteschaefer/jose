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
import de.jose.util.StringUtil;
import de.jose.Language;
import de.jose.view.style.JoFontConstants;
import de.jose.profile.FontEncoding;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xml.sax.SAXException;

import static de.jose.pgn.INodeConstants.TAG_NODE;

public class TagNode
		extends Node
		implements PgnConstants
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

	/**	TAG key ("EVENT", etc.)	 */
	protected String key;
	/**	data type	*/
	protected Class clazz;
	/**	value	 */
	protected Object value;
	/**	editable value	*/
	protected StringBuffer stringValue;
    /** view in document ? */
    protected boolean visible;
	/**	dirty (stringValue != value)	*/
	protected boolean dirty;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public TagNode(String aKey, Object aValue)
	{
        super(TAG_NODE);
		key = aKey;
		clazz = PgnUtil.getDataType(aKey);
		stringValue = new StringBuffer();
        visible = true;
		setValue(aValue);
	}

    public final void setVisible(boolean vis)
    {
        visible = vis;
        if (!visible) setLength(0);
    }

	public final String getKey()				{ return key; }

	public final Object getValue()
	{
		if (dirty) {
			if (isString())
			{
				if (stringValue.length()==0)
					value = null;
				else
					value = stringValue.toString();
			}
			else
				throw new IllegalStateException("can't edit non-String tags");
			dirty = false;
		}
		return value;
	}

	protected static String toString(Object value)
	{
		if (value==null)
			return "";
		if (value instanceof PgnDate) {
			PgnDate pgnDate = (PgnDate)value;
			if (pgnDate.isDateUnknown())
				return "";
			else
				return pgnDate.toLocalDateString(true);
		}
		else
			return value.toString();
	}

	public final void setKey(String key)
	{
		if (DEFAULT_TAGS.contains(key))
			throw new IllegalArgumentException();
		else
			this.key = key;
	}

	public final void setValue(Object obj)
	{
		value = obj;
		stringValue.setLength(0);
		stringValue.append(toString(value));
		dirty = false;
	}

	public final boolean isEmpty()
	{
		return !visible || isEmpty(value);
	}

	public static final boolean isEmpty(Object value)
	{
		if ((value instanceof PgnDate) && ((PgnDate)value).isDateUnknown())
			return true;
		else
			return value==null;
	}

	public final Class getDataType()			{ return clazz; }
	public final boolean isString()			    { return clazz==String.class; }

	public final boolean isEditable()
	{
		if (key.equals(TAG_RESULT)) return false;
		return isString();
	}

	public final String getStyleName()				{ return "header."+key.toLowerCase(); }
	public final String getAltStyleName()			{ return "header"; }

	public final void clear() {
		setValue(null);
		setLength(0);
	}
	
	public final int getIntValue()
	{
		if (value!=null)
			return ((Number)value).intValue(); 
		else
			return 0;
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		Style result = doc.getStyle(getStyleName());
		if (result==null) result = doc.getStyle(getAltStyleName());
		return result;
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		if (visible && value!=null)
        {
            if (clazz==de.jose.chess.Board.class)
            {
                //  insert diagram from fen
                Style style = parent().getStyle(doc,"body.inline");
                String diaFont = JoFontConstants.getFontFamily(style);
                FontEncoding enc = FontEncoding.getEncoding(diaFont);

                String text = DiagramNode.toString((String)value, enc);
                doc.insertString(at,text,style);
                setLength(text.length());
            }
            else
            {
                //  insert text
                Style style = getDefaultStyle(doc);

                String text = stringValue.toString();
                doc.insertString(at, text, style);
                setLength(text.length());
            }
		}
		else
			setLength(0);
	}

	public void replace(StyledDocument doc, int from, int to, String newText) throws BadLocationException
	{
		Style style = getDefaultStyle(doc);
		
		int offset = getStartOffset();
		if (to > from)
			doc.remove(offset+from, to-from);
		if (newText.length() > 0)
			doc.insertString(offset+from,newText,style);

		stringValue.replace(from,to,newText);
		setLength(stringValue.length());
		dirty = true;
	}

	public void setEmpty(StyledDocument doc) throws BadLocationException
	{
		int offset = getStartOffset();
		doc.remove(offset, getLength());

		setValue(null);
		setLength(0);
	}

	public boolean isCoveredBy(int pos1, int pos2)
	{
		return (pos1 <= 0) && (pos2 >= getLength());
	}

/*
	public static char charAt(StyledDocument doc, int pos)
		throws BadLocationException
	{
		if (pos < 0)
			return '\0';
		String t = doc.getText(pos,1);
		return t.charAt(0);
	}
*/
    void writeBinary(BinWriter writer) {
        throw new AbstractMethodError("");
		//	tags are not written to binary
    }

	public String toString() {
		return "["+key+" \""+((value==null) ? "":value.toString())+"\"]";
	}


	public void toSAX(JoContentHandler handler) throws SAXException
	{
		toSAX(getKey(),value, handler);
	}

	public static void toSAX(String key, Object value, JoContentHandler handler) throws SAXException
	{
		if (isEmpty(value)) return;

		String translated = Language.get("column.game."+key.toLowerCase());

		handler.startElement("tag");
			handler.element("key",key);
			handler.element("value", toString(value));
			if (translated!=null) handler.element("text",translated);
		//  TODO print type information
		handler.endElement("tag");
	}

}
