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

import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.chess.Position;
import de.jose.profile.FontEncoding;
import de.jose.profile.UserProfile;
import de.jose.sax.JoContentHandler;
import de.jose.Application;
import de.jose.view.style.JoFontConstants;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xml.sax.SAXException;

public class DiagramNode
		extends Node
		implements Constants
{

	protected String fen;

    public DiagramNode(String pos)
    {
        super(DIAGRAM_NODE);
		fen = pos;
	    setKeepTogether(true);  //  ALWAYS
    }

	protected static void appendBorder(StringBuffer buf, FontEncoding enc, int offset)
	{
		appendBorder(buf,enc,offset,-1);
	}

	protected static void appendBorder(StringBuffer buf, FontEncoding enc,
									   int offset1, int offset2)
	{
		if (!enc.hasBorder()) return;
		String c = enc.getBorder(false,offset1);
		if (c==null && offset2>=0) c = enc.getBorder(false,offset2);
		if (c!=null) buf.append(c);
	}

	public String toString() {
		return " {diagram} ";
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return parent().getStyle(doc,"body.inline");
	}

	public static String toString(String fen, FontEncoding enc)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("\n");
		//	top border
		if (enc.hasBorder()) {
			appendBorder(buf,enc,FontEncoding.BORDER_TOP_LEFT);
			for (int i=0; i<8; i++)
				appendBorder(buf,enc,FontEncoding.BORDER_TOP);
			appendBorder(buf,enc,FontEncoding.BORDER_TOP_RIGHT);
			buf.append("\n");
		}

		int file = FILE_A;
		int row = ROW_8;

		beginRow(buf,row,enc);

		for(int i = 0; row >= ROW_1; i++) {
		    char c = fen.charAt(i);
			if(c == '/' || c==':') {
				//	end row
//				while (file <= FILE_H) set(buf,file++,row, EMPTY, enc);
		    }
		    else if(c >= '1' && c <= '8') {
		        int count = c - '0';
				while (count-- > 0) set(buf,file++,row,EMPTY, enc);
			}
		    else {
		        int p = EngUtil.char2Piece(c);
		        set(buf,file++,row,p, enc);
		    }

			if (file > FILE_H) {
				endRow(buf,row, enc);
				if (--row >= ROW_1) beginRow(buf,row, enc);
				file = FILE_A;
			}
		}

		//	bottom border
		if (enc.hasBorder()) {
			appendBorder(buf,enc, FontEncoding.BORDER_BOTTOM_LEFT);
			for (int i=0; i<8; i++)
				appendBorder(buf,enc, FontEncoding.BORDER_BOTTOM_A+i,FontEncoding.BORDER_BOTTOM);
			appendBorder(buf,enc, FontEncoding.BORDER_BOTTOM_RIGHT);
			buf.append("\n");
		}

		buf.append("\n");
		return buf.toString();
	}

	protected static void beginRow(StringBuffer buf, int row, FontEncoding enc)
	{
		appendBorder(buf,enc,FontEncoding.BORDER_LEFT_1+row-ROW_1,FontEncoding.BORDER_LEFT);
	}

	protected static void endRow(StringBuffer buf, int row, FontEncoding enc)
	{
		appendBorder(buf,enc,FontEncoding.BORDER_RIGHT);
		buf.append("\n");
	}

	protected static void set(StringBuffer buf, int file, int row, int piece, FontEncoding enc)
	{
		int background = EngUtil.isLightSquare(file,row) ? LIGHT_SQUARE:DARK_SQUARE;
		buf.append(enc.get(piece, background));
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		Style style = getDefaultStyle(doc);
		String diaFont = JoFontConstants.getFontFamily(style);
		FontEncoding enc = FontEncoding.getEncoding(diaFont);

		String text = toString(fen, enc);
		doc.insertString(at,text,style);
		setLength(text.length());
	}

    /** write binary data   */
    void writeBinary(BinWriter writer) {
        writer.annotation(PgnConstants.NAG_DIAGRAM);
    }

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		Position pos = getGame().getPosition();
		toSAX(pos,handler);
	}

	public static void toSAX(Position pos, JoContentHandler handler) throws SAXException
	{
		handler.startElement("diagram");
			//  print fen, or whatever
			String fen = pos.toString();
			handler.element("fen",fen);

			Style style = handler.context.styles.getStyle("body.inline");
			//  TODO get style for appropriate nest level !!
			String diaFont = JoFontConstants.getFontFamily(style);
			FontEncoding enc = FontEncoding.getEncoding(diaFont);

			String text = toString(fen, enc);
			//  output FEN
			handler.element("text",text);

			saxTable(handler, enc, pos);

			//  output TeX board
			handler.startElement("texboard");

				for (int row = ROW_8; row >= ROW_1; row--)
				{
					handler.startElement("row");

					for (int file = FILE_A; file <= FILE_H; file++)
					{
						int pc = pos.pieceAt(file,row);
						boolean light = EngUtil.isLightSquare(file,row);

						if (pc==EMPTY)
							handler.characters(light ? " ":"*");
						else
							handler.characters(String.valueOf(EngUtil.coloredPieceCharacter(pc)));
					}

					handler.endElement("row");
				}

			handler.endElement("texboard");

		handler.endElement("diagram");
	}

	private static void saxTable(JoContentHandler handler, FontEncoding enc, Position pos)
	        throws SAXException
	{
		//  output HTML <table>
		handler.startElement("table");

		//  top border row
		handler.startElement("tr");
		//  top left edge
		tableCell(handler,enc,"brdtl",FontEncoding.BORDER_TOP_LEFT);
		for (int i=0; i<8; i++)
			tableCell(handler,enc,"brdt",FontEncoding.BORDER_TOP);
		tableCell(handler,enc,"brdtr",FontEncoding.BORDER_TOP_RIGHT);
		handler.endElement("tr");

		for (int row = ROW_8; row >= ROW_1; row--) {
			handler.startElement("tr");

			tableCell(handler,enc, "brdl"+('1'+row-ROW_1), "brdl",
			                FontEncoding.BORDER_LEFT_1+row-ROW_1,FontEncoding.BORDER_LEFT);

			for (int file = FILE_A; file <= FILE_H; file++) {
				handler.startElement("td");

				int pc = pos.pieceAt(file,row);
				boolean light = EngUtil.isLightSquare(file,row);

				String suffix = light ? "l":"d";
				if (pc==EMPTY)
					handler.element("img","e"+suffix);
				else if (EngUtil.isWhite(pc))
					handler.element("img", EngUtil.lowerPieceCharacter(pc)+"w"+suffix);
				else
					handler.element("img", EngUtil.lowerPieceCharacter(pc)+"b"+suffix);

				String chr = enc.get(pc, light ? FontEncoding.LIGHT_SQUARE:FontEncoding.DARK_SQUARE);
				handler.element("char",chr);

				handler.endElement("td");
			}

			tableCell(handler,enc, "brdr", FontEncoding.BORDER_RIGHT);

			handler.endElement("tr");
		}

		//  bottom border row
		handler.startElement("tr");
		//  bottom left edge
		tableCell(handler,enc, "brdbl", FontEncoding.BORDER_BOTTOM_LEFT);
		for (int i=0; i<8; i++)
			tableCell(handler,enc, "brdb"+('a'+i), "brdb",
			        FontEncoding.BORDER_BOTTOM_A+i,FontEncoding.BORDER_BOTTOM);
		tableCell(handler,enc, "brdbr", FontEncoding.BORDER_BOTTOM_RIGHT);
		handler.endElement("tr");

		handler.endElement("table");
	}


	private static void tableCell(JoContentHandler handler, FontEncoding enc,
	                              String img1, int chr1) throws SAXException
	{
		tableCell(handler,enc, img1,null, chr1,-1);
	}

	private static void tableCell(JoContentHandler handler, FontEncoding enc,
	                              String img1, String img2,
	                              int chr1, int chr2) throws SAXException
	{
		if (!enc.hasBorder()) return;

		handler.startElement("td");
			String c = enc.getBorder(false,chr1);
			if (c!=null) {
				handler.element("img",img1);
				handler.element("char",c);
			}
			else {
				c = enc.getBorder(false,chr2);
				if (c!=null) {
					handler.element("img",img2);
					handler.element("char",c);
				}
			}
		handler.endElement("td");
	}
 }
