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

package de.jose.util.print;

import de.jose.Application;
import de.jose.db.JoConnection;
import de.jose.task.GameSource;
import de.jose.task.GameIterator;
import de.jose.task.GameHandler;
import de.jose.pgn.Game;
import de.jose.pgn.PgnConstants;
import de.jose.view.style.JoStyleContext;
import de.jose.util.IntArray;
import de.jose.util.ListUtil;
import de.jose.util.map.IntHashMap;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.print.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * prints a StyledDocument, uses a JTextPane for painting
 *
 * @author Peter Schäfer
 */

public class AWTPrintableDocument
        extends PrintableDocument
		implements Printable, Pageable
{
	protected DefaultStyledDocument doc;

	/**	JTextPane that does the actual drawing	*/
	protected JTextPane textPane;
	/**	belongs to textPane	*/
	protected View view;

	protected int columnCount = 2;
	protected int columnGap = 10;

	protected int[] columnBreaks;

	protected Vector contentDocs = new Vector();
	protected IntArray contentDocOffsets = new IntArray();

	/** aux. variables  */
	private Rectangle2D.Double paintRect = new Rectangle2D.Double();
	private Rectangle2D.Double clipRect = new Rectangle2D.Double();

	static class PageBreak {
		/**	document position	*/
		int pos;
		/**	view location (y-axis)	*/
		int y;

		PageBreak(int p, int y) { this.pos = p; this.y = y; }
	}

	public AWTPrintableDocument(de.jose.Document document, PageFormat format)
	{
		this((DefaultStyledDocument)document.copy(),format);
		if (document!=null) addContentDoc(document,0);
		((de.jose.Document)doc).getStyleContext().setFontScale(1.0f);  //  don't need it for printing
	}

	static class GameIteratorCallback implements GameHandler
	{
		Game game;
		de.jose.chess.Position pos;
		JoStyleContext styles;

		GameIteratorCallback(JoStyleContext styles) {
			this.styles = styles;
		}

		public void handleObject(Game game)
		{
			this.game = game;
		}

		public void handleRow(ResultSet res) throws Exception
		{
			if (pos==null) pos = new de.jose.chess.Position();
			game = new Game(styles, pos);
			game.read(res,true);
		}
	}

	public static AWTPrintableDocument newDocument(GameSource src, PageFormat format, JoStyleContext styles)
	        throws Exception
	{
		GameIterator gi = null;
		JoConnection connection = null;
		AWTPrintableDocument awt_doc = null;

		try {
			GameIteratorCallback callback = new GameIteratorCallback(styles);
			connection = JoConnection.get();

			gi = GameIterator.newGameIterator(src,connection);    //  get a DB connection

			if (gi.hasNext()) {
				gi.next(callback);
				awt_doc = new AWTPrintableDocument(callback.game, format);
			}
			while (gi.hasNext()) {
				gi.next(callback);
				awt_doc.appendDocument(callback.game);
			}
/*
			de.jose.Document doc = new de.jose.Document(styles);
			awt_doc = new AWTPrintableDocument(doc,format);

			int i=0;
			int offset = 0;
			ArrayList elements = new ArrayList();
			while (gi.hasNext()) {
				gi.next(callback);

				awt_doc.addContentDoc(callback.game,offset);
				offset += callback.game.getLength();

				callback.game.getElementSpecs(elements);
				/** seems to become slower when the document grows; * /
				System.out.println(i++);
			}

			doc.insert(0,elements);
*/
		} catch (BadLocationException blex) {
			Application.error(blex);
		} finally {
			if (connection!=null) connection.release();
			if (gi!=null) gi.close();
		}

		return awt_doc;
	}

	protected AWTPrintableDocument(DefaultStyledDocument document, PageFormat format)
	{
		super(format);
//		doc = new de.jose.Document(document.getDocumentContent(), styles);
//		super(document.getDocumentContent(), styleContext = styleContext.copy());
		if (document!=null)
			this.doc = document;
		else
			this.doc = new DefaultStyledDocument();

		textPane = new JTextPane(doc);
		textPane.setEditable(false);

		view = textPane.getUI().getRootView(textPane);
//		textPane.setDocument(this);
		columnBreaks = null;
		//  page breaks can only be calculated after format is set
		//  and textPane becomes visible
	}

	public void appendDocument(de.jose.Document document) throws BadLocationException
	{
		doc.insertString(doc.getLength(),"\n\n\n",null);
		addContentDoc(document, doc.getLength());
		((de.jose.Document)doc).append(document.getDefaultRootElement());
		/** TODO paragraph alignment (center,right align) is lost. Why ?    */
	}

	public double calcColumnWidth(double pageWidth)
	{
		return (pageWidth - columnGap*(columnCount-1)) / columnCount;
	}

	public void setPageFormat(PageFormat format)
	{
		super.setPageFormat(format);
		view.setSize((float)format.getImageableWidth(), Integer.MAX_VALUE);
		textPane.setBounds(0,0, (int)calcColumnWidth(format.getImageableWidth()), Integer.MAX_VALUE);
		findPageBreaks();
	}


	protected void addContentDoc(de.jose.Document doc, int offset)
	{
		contentDocs.add(doc);
		contentDocOffsets.add(offset);
	}

	protected int getContentDocument(int pos)
	{
		for (int i=contentDocOffsets.size()-1; i>=0; i--)
			if (pos >= contentDocOffsets.get(i))
				return i;
		return -1;
	}

	protected static Rectangle2D inset(Rectangle2D r, double x, double y)
	{
		r.setRect(r.getX()+x, r.getY()+y, r.getWidth()-2*x, r.getHeight()-2*y);
		return r;
	}

	protected static Rectangle2D offset(Rectangle2D r, double x, double y)
	{
		r.setRect(r.getX()+x, r.getY()+y, r.getWidth(), r.getHeight());
		return r;
	}

	/**
	 * implements Printable
	 */
	public int print(Graphics g, int pageNumber,
	                 int screenX, int screenY,
	                 double scaleX, double scaleY)
	{
		if (pageNumber >= getNumberOfPages())
			return Printable.NO_SUCH_PAGE;

		int columnX = 0;
		int columnWidth = (int)calcColumnWidth(format.getImageableWidth());

		for (int column = 0; column < columnCount; column++)
		{
			printColumn(g, pageNumber*columnCount + column,
			        screenX, screenY,
			        columnX, columnWidth,
			        scaleX, scaleY);
			columnX += columnWidth+columnGap;
		}

		return Printable.PAGE_EXISTS;
	}

	private int printColumn(Graphics g, int columnNumber,
	                 int screenX, int screenY,
	                 int columnX, int columnWidth,
	                 double scaleX, double scaleY)
	{
		if (columnNumber >= columnBreaks.length) return NOT_AVAILABLE;

		//  calc print coordinates
		int pageOffset = (columnNumber<=0) ? 0 : columnBreaks[columnNumber-1];
		int pageHeight = columnBreaks[columnNumber]-pageOffset;

		paintRect.x = format.getImageableX()+columnX+1;
		clipRect.x = paintRect.x-4;

		paintRect.y = format.getImageableY();
		clipRect.y = pageOffset+format.getImageableY();

		paintRect.width = columnWidth;
		paintRect.height = pageHeight;

		clipRect.width = paintRect.width+8;
		clipRect.height = paintRect.height;

		Graphics2D g2 = (Graphics2D)g;
		AffineTransform oldtf = g2.getTransform();
		Shape oldclip = g2.getClip();

		try {
			//  transform to screen coordinates
			g2.translate(screenX, screenY-pageOffset*scaleY);
			g2.scale(scaleX, scaleY);

			//  clipRect and paintRect are still in Print coordinates, right ?
			g2.clip(clipRect);
			Rectangle r = new Rectangle((int)paintRect.x, (int)paintRect.y, (int)paintRect.width, (int)paintRect.height);
			view.paint(g,r);

		} finally {
//			g2.setColor(Color.blue);
//			g2.drawRect((int)clipRect.x, (int)clipRect.y,(int)clipRect.width,(int)clipRect.height);
//			g2.setColor(Color.red);
//			g2.drawRect((int)paintRect.x, (int)paintRect.y,(int)paintRect.width,(int)paintRect.height);
			g2.setTransform(oldtf);
			g2.setClip(oldclip);
		}
		return Printable.PAGE_EXISTS;
	}

	protected int getDocumentPosition(int x, int y, boolean forceBreak)
	{
		Point p = new Point(x,y);
		int pos = textPane.viewToModel(p);
		if (!forceBreak) {
			//  get the related content model
			int cdoc_idx = getContentDocument(pos);
			if (cdoc_idx >= 0) {
				de.jose.Document contentDoc = (de.jose.Document)contentDocs.get(cdoc_idx);
				int offset = contentDocOffsets.get(cdoc_idx);
				pos = offset + contentDoc.getPreferredPageBreak(pos-offset);
			}
		}
		return pos;
	}

	protected Rectangle getLineBounds(int start, int end)
	{
		Rectangle result;
		try {
			result = textPane.modelToView(start);
			while (++start <= end)
				result.add(textPane.modelToView(start));
			return result;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected void findPageBreaks()
	{
		if (format==null || doc==null || !textPane.isVisible()) {
			columnBreaks = null;
			return;
		}

		double maxPageHeight = format.getImageableHeight();
		IntArray collect = new IntArray();
		int y0 = 0;
		boolean forceBreak = false;

		if (doc.getLength()==0) {
			collect.add(0);
		}
		else for (;;) {
			int pos1 = getDocumentPosition(0, (int)(y0+maxPageHeight),forceBreak);
			int pos2 = getDocumentPosition((int)format.getImageableWidth(), (int)(y0+maxPageHeight),forceBreak);
			if (pos2<=0) throw new IllegalStateException("must not happen");
/*
			if (pos2==0) {
				//  positions are not available ?!
				pageBreaks = null;
				return;
			}
*/
			forceBreak = false;
			Rectangle r = getLineBounds(pos1,pos2);
			if (pos2 >= (doc.getLength()-1)) {
				//	we are done
				collect.add(r.y+r.height);
				break;
			}
			else {
				y0 = r.y-1; //  TODO why is r.y not precise ?
				if (!collect.isEmpty() && (y0<=collect.get(collect.size()-1))) {
					forceBreak = true;
					/** due to keep-together conditions we can not advance (rare condition)
					 *  force the next line break to overwrite the keep-together
					 */
				}
				else
					collect.add(y0);
			}
		}

		columnBreaks = collect.toArray();
	}

	/**
	 * implements Pageable
	 */
	public int getNumberOfPages()
	{
		if (columnBreaks==null)
			return -1;
		else
			return (columnBreaks.length+columnCount-1) / columnCount;
	}

	public void render()
	{
		textPane.setDocument(doc);
		findPageBreaks();
	}

	public void dispose()
	{
		doc = null;
		contentDocs.clear();
		contentDocOffsets.clear();
		textPane.setDocument(textPane.getEditorKit().createDefaultDocument());
	}
}
