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
import de.jose.util.style.StyleUtil;
import de.jose.util.style.MarkupParser;
import de.jose.util.style.MarkupWriter;
import de.jose.Application;
import de.jose.view.style.JoFontConstants;

import javax.swing.text.*;

import org.xml.sax.SAXException;

import java.awt.*;

public class CommentNode
		extends Node
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

    /** marked up (or plain) text   */
	protected StringBuffer text;
    protected boolean isMarkup; // is it markup, or plain text
    /** when isMarkup==true, the contents of "text" DOES NOT reflect the
     *  current state of the document. It contains html markup and is longer.
     *  updates to the document must be copied with updateMarkup(), which is somewhat
     *  expensive.
     *
     *  when isMarkup==false, the contents of "text" is identical to the current
     *  state of the document. updates to the document are directly copied to text.
     */

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------


	public CommentNode(StringBuffer txt)
	{
		super(COMMENT_NODE);
		text = new StringBuffer(txt.toString());
		/** StringBuffer(CharSequence) since 1.5 !! */
	}

	public CommentNode(String txt)
	{
        super(COMMENT_NODE);
        text = new StringBuffer(txt);
	}

	public CommentNode(char[] c, int start, int len)
	{
		this(new StringBuffer());
		text.append(c,start,len);
	}

	public String debugString()
	{
		return "{}";
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return parent().getStyle(doc,"body.comment");
	}

	/**	insert into text document
	 * */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
        Style style = getDefaultStyle(doc);
        int length = MarkupParser.insertMarkup(doc,at, style, text);
        isMarkup = MarkupParser.hadMarkup();

		doc.insertString(at+length," ",style);   //  always append a trailing space
        setLength(length+1);
	}


    /**
     *
     * @param doc
     * @param from relative to this node
     * @param to relative to this node
     * @param newText
     * @throws BadLocationException
     */
	public void replace(StyledDocument doc, int from, int to, String newText)
        throws BadLocationException
    {
		if (from >= (getLength()-1)) from = getLength()-1;
		if (to >= (getLength()-1)) to = getLength()-1;
	    int offset = getStartOffset();

        //  determine character style
        AttributeSet style = null;
        Element paragraph = doc.getParagraphElement(offset+from);
        if ((offset+from)==paragraph.getStartOffset())
            style = StyleUtil.getCharacterStyleAt(doc, offset+from);  //  at line start
        else if ((offset+from)>=1)
            style = StyleUtil.getCharacterStyleAt(doc, offset+from-1);  //  inside line
	    if (style==null)
            style = getDefaultStyle(doc);

        //  cut out old
        int newLength = getLength();
        if (to > from) {
            doc.remove(offset+from, to-from);
            newLength -= (to-from);
        }
        //  insert new
        if (newText.length()==1) {
            //  shortcut for most common case; this can't be complex markup
            doc.insertString(offset+from,newText,style);
            newLength++;
            if (MarkupParser.isMarkup(newText.charAt(0))) isMarkup=true;
        }
        else if (newText.length() > 1) {
            //  got to parse html; fortunately this case is rare
            newLength += MarkupParser.insertMarkup(doc,offset+from, style, new StringBuffer(newText));
            if (MarkupParser.hadMarkup()) isMarkup = true;
        }
        setLength(newLength);

        //  update markup !!
        if (isMarkup)
            updateMarkup();
        else {
            //  simple
            text.replace(from,to,newText);
        }
    }

	public boolean isCoveredBy(int pos1, int pos2)
	{
		return (pos1 <= 0) && (pos2 >= (getLength()-1));
	}

	public void toggleAttribute(StyledDocument doc, int from, int to, Object attribute)
	{
		Style applyStyle = null;

		if (attribute==null) {  //=plain
            Style baseStyle = getDefaultStyle(doc);
            Color color = StyleConstants.getForeground(baseStyle);
            int size = JoFontConstants.getFontSize(baseStyle);
            String face = JoFontConstants.getFontFamily(baseStyle);
			applyStyle = StyleUtil.plainStyle(color,face,size);
        }
		else if (attribute==StyleConstants.Bold) {
			if (StyleUtil.isContiguous(doc,from,to,StyleConstants.Bold))
				applyStyle = StyleUtil.unbold;
			else {
				applyStyle = StyleUtil.bold;
                isMarkup = true;
            }
		}
		else if (attribute==StyleConstants.Italic) {
			if (StyleUtil.isContiguous(doc,from,to,StyleConstants.Italic))
				applyStyle = StyleUtil.unitalic;
			else {
				applyStyle = StyleUtil.italic;
                isMarkup = true;
            }
		}
		else if (attribute==StyleConstants.Underline) {
			if (StyleUtil.isContiguous(doc,from,to,StyleConstants.Underline))
				applyStyle = StyleUtil.ununderline;
			else {
				applyStyle = StyleUtil.underline;
                isMarkup = true;
            }
		}

		if (applyStyle!=null) {
			//  don't apply name
			applyStyle.removeAttribute(StyleConstants.NameAttribute);
			doc.setCharacterAttributes(from,to-from,applyStyle,false);            
            if (isMarkup) updateMarkup();
			/**
			 * TODO setting attributes to "false" doesn't work if they are inherited
             * from the parent style. Why ?
			 */
		}
	}

    /**
     *
     * @param doc
     * @param from absolute ! (to doc)
     * @param to
     * @param align
     */
	public void alignParagraph(StyledDocument doc, int from, int to, int align)
	{
		Style applyStyle = null;

		switch (align)
		{
		default:
		case StyleConstants.ALIGN_LEFT:
				applyStyle = StyleUtil.left;
				break;
		case StyleConstants.ALIGN_RIGHT:
				applyStyle = StyleUtil.right;
                isMarkup = true;
				break;
		case StyleConstants.ALIGN_JUSTIFIED:
		case StyleConstants.ALIGN_CENTER:
				applyStyle = StyleUtil.center;
                isMarkup = true;
				break;
		}

		//  make sure that neighboring paragraphs are NOT affected
        if (applyStyle!=null)
        {
            if (align!=StyleConstants.ALIGN_LEFT)
            try {
                int start = getStartOffset();
                int end = getEndOffset();

                Element p1 = doc.getParagraphElement(from);
                Element p2 = doc.getParagraphElement(to);

                if (p2.getEndOffset() > end) {
                    //  need to insert a break at end of this !
                    doc.insertString(end-1,"\n",getDefaultStyle(doc));    //  mind trailing space!
                    addLength(+1);
                    isMarkup = true;
                }

                if (p1.getStartOffset() < start) {
                    doc.insertString(start,"\n",getDefaultStyle(doc));
                    addLength(+1);
                    isMarkup = true;
                    from++; to++;
                }

            } catch (BadLocationException e) {
                Application.error(e);
            }

			doc.setParagraphAttributes(from,to-from,applyStyle,false);
            if (isMarkup) updateMarkup();
		}
	}

    /**
     * modify font size by "factor", but at least by "min_increment" points
     * @param doc
     * @param from absolute
     * @param to absolute
     * @param factor
     * @param min_increment
     */
	public void modifyFontSize(StyledDocument doc, int from, int to, float factor, int min_increment)
	{
		//  traverse style runs
        Style baseStyle = getDefaultStyle(doc);
        for (int pos=from; pos < to; )
        {
            Element elm = doc.getCharacterElement(pos);
            modifyFontSize(doc,
                    Math.max(from,elm.getStartOffset()),
                    Math.min(to,elm.getEndOffset()),
                    elm.getAttributes(), baseStyle,
                    factor,min_increment);
            pos = elm.getEndOffset();
        }
        if (isMarkup) updateMarkup();
	}

    /**
     *
     * @param doc
     * @param from absolute
     * @param to absolute
     * @param style
     * @param baseStyle
     * @param factor
     * @param min_increment
     */
    private void modifyFontSize(StyledDocument doc, int from, int to,
                                AttributeSet style, AttributeSet baseStyle,
                                float factor, int min_increment)
    {
        /** is font-scale already defined in style ?    */
        int size = JoFontConstants.getFontSize(style);
        float scale = JoFontConstants.getFontScaleFactor(style);

        if (scale <= 0.0f) {
            //  compute it!
            scale = (float)size / (float)JoFontConstants.getFontSize(baseStyle);
        }

        float newScale = scale*factor;
        int newSize = Math.round(JoFontConstants.getFontSize(baseStyle)*newScale);
        if (min_increment > 0)
            newSize = Math.max(size+min_increment,newSize);
        else
            newSize = Math.min(size+min_increment,newSize);

        doc.setCharacterAttributes(from,to-from, StyleUtil.sizedStyle(newSize,newScale),false);
        isMarkup = true;
    }

    /**
     *
     * @param doc
     * @param from  absolute
     * @param to  absolute
     * @param color
     */
	public void setColor(StyledDocument doc, int from, int to, Color color)
	{
		Style applyStyle;
		if (color==null) {
			Style baseStyle = getDefaultStyle(doc);
			applyStyle = StyleUtil.coloredStyle(StyleConstants.getForeground(baseStyle));
		}
		else {
			applyStyle = StyleUtil.coloredStyle(color);
            isMarkup = true;
        }

		if (applyStyle != null) {
			doc.setCharacterAttributes(from,to-from,applyStyle,false);
            if (isMarkup) updateMarkup();
		}
	}

    /**
     * attention: this method could become expensive, if used excessively
     */
    private void updateMarkup()
    {
        StyledDocument doc = getGame();
        Style baseStyle = getDefaultStyle(doc);
        text.setLength(0);
        if (isMarkup) {
            MarkupWriter.writeMarkup(doc, getStartOffset(), getLength(), baseStyle, text, false);
            isMarkup = MarkupWriter.hadMarkup();
        }
        else try {
            //  plain text can be handled more easily
            text.append(doc.getText(getStartOffset(),getLength()));
        } catch (BadLocationException e) {
            Application.error(e);
        }
    }

    /** write binary data
     *  */
    void writeBinary(BinWriter writer)
    {
        writer.comment(text.toString());
    }

	public void toSAX(JoContentHandler handler) throws SAXException
	{
        toSAX(text,handler,isMarkup);
	}

	public static void toSAX(StringBuffer markup, JoContentHandler handler, boolean isMarkup) throws SAXException
	{
        handler.startElement("c");
//		handler.characters(text,0,text.length());
        if (isMarkup)
		    MarkupParser.insertSAX(markup,handler);
        else
            handler.characters(markup.toString());
        handler.endElement("c");
	}

}

