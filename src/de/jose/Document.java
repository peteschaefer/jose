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

package de.jose;

import de.jose.view.style.JoStyleContext;
import de.jose.util.ListUtil;

import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.util.List;

public class Document
			extends DefaultStyledDocument
//            extends HTMLDocument
{
	public class DirtyEvent implements DocumentEvent
	{
		public final boolean isDirty()								{ return Document.this.isDirty(); }

		public DocumentEvent.ElementChange getChange(Element elem) 	{ return null; }

		public javax.swing.text.Document getDocument() 				{ return Document.this; }

		public int getLength() 										{ return 0;	}

		public int getOffset() 										{ return 0;	}

		public DocumentEvent.EventType getType() 					{ return DocumentEvent.EventType.CHANGE; }
	}

	/**	Id in database 	(optional) */
	protected int dbId;
	/**	current row in result set (optional)	 */
	protected int rowNum;
	/** dirty flag  */
    protected boolean dirty;
	/**	firec Document events when it becomes dirty ? */
	protected boolean fireEvents = true;

	public Document()
	{
		super();
		clearId();
	}
	
	public Document(StyleContext styles)
	{
		super(styles);
		clearId();
	}

	public Document(AbstractDocument.Content content, StyleSheet styles)
	{
		super(content,styles);
		clearId();
	}


	/**	Id in database 	(optional) */
	public final int getId()		        { return dbId; }

    /** @return true if the document is modified    */
    public final boolean isDirty()  { return dirty; }

	/**	current row in result set (optional)	 */
	public final int getRowNum()	{ return rowNum; }

	public final void setDirty()	{ setDirty(true); }

	public final void clearDirty()	{ setDirty(false); }


	public AbstractDocument.Content getDocumentContent()
	{
		return super.getContent();
	}

	public JoStyleContext getStyleContext()
	{
		return (JoStyleContext)super.getAttributeContext();
	}

	public de.jose.Document copy()
	{
		JoStyleContext styleCopy = getStyleContext().copy();
		de.jose.Document that = new de.jose.Document(styleCopy);
		that.append(this.getDefaultRootElement());
		return that;
		/**
		 * looks awkard;
		 *   new Document(this.getContent(),styleCopy)
		 * seems to be more efficient, but document positions don't work anymore !?!
		 */
	}

	public void insert(int offset, DefaultStyledDocument.ElementSpec[] specs) throws BadLocationException
	{
		super.insert(offset,specs);
	}

	public void insert(int offset, List specs) throws BadLocationException
	{
		DefaultStyledDocument.ElementSpec[] spec_array = (DefaultStyledDocument.ElementSpec[])
		        ListUtil.toArray(specs,DefaultStyledDocument.ElementSpec.class);
		insert(0,spec_array);
	}

	public int getPreferredPageBreak(int pos) {
		return pos;
	}

	public void append(Element elem)
	{
		if (elem.isLeaf())
			try {
				AttributeSet style = elem.getAttributes();
				int start = elem.getStartOffset();
				int end = elem.getEndOffset();
				String text = elem.getDocument().getText(start,end-start);

				this.insertString(getLength(),text,style);
			} catch (BadLocationException blex) {
				blex.printStackTrace();
			}
		else for (int i=0; i < elem.getElementCount(); i++)
			append(elem.getElement(i));
	}

	public final void setDirty(boolean on) {
		if (dirty != on) {
			dirty = on;
			if (fireEvents) fireChangedUpdate(new DirtyEvent());
		}
	}

	public void getElementSpecs(List collect)
	{
		Segment seg = new Segment();
		Content content = getContent();
		try {
			content.getChars(0,getLength(), seg);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		char[] chars = seg.array;

		collectElementSpecs(getDefaultRootElement(),chars,collect);
	}

	protected void collectElementSpecs(Element elem, char[] chars, List collect)
	{
		if (elem.isLeaf()) {
			AttributeSet style = elem.getAttributes();
			int start = elem.getStartOffset();
			int end = elem.getEndOffset();

			DefaultStyledDocument.ElementSpec spec =
					new DefaultStyledDocument.ElementSpec(style,
							DefaultStyledDocument.ElementSpec.ContentType,
							chars, start, end-start);

			collect.add(spec);

		}
		else for (int i=0; i < elem.getElementCount(); i++)
			collectElementSpecs(elem.getElement(i),chars,collect);
	}



	public final void clearId()	{
		dbId = 0;
		rowNum = 0;
        clearDirty();
	}
}
