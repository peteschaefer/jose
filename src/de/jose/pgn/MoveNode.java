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

import de.jose.Application;
import de.jose.plugin.Score;
import de.jose.sax.JoContentHandler;
import de.jose.chess.*;
import de.jose.profile.FontEncoding;
import de.jose.view.style.JoStyleContext;
import de.jose.view.style.JoFontConstants;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xml.sax.SAXException;

import static de.jose.pgn.INodeConstants.LINE_NODE;
import static de.jose.pgn.INodeConstants.MOVE_NODE;

public class MoveNode
		extends Node
		implements Constants
{
	//-------------------------------------------------------------------------------
	//	Field
	//-------------------------------------------------------------------------------

    protected int ply;
	protected Move move;
    protected long hashKey;
	protected int moveCountLen;
	public Score engineValue;

    protected static StyledMoveFormatter formatter = new StyledMoveFormatter();
	protected static SAXMoveFormatter saxFormatter = new SAXMoveFormatter();

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public MoveNode(int pl, Move mv)
	{
        super(MOVE_NODE);
        ply = pl;
		move = mv;
		engineValue = null;
	}

	public final int getPly() 		{ return ply; }
	public final int getMoveNo()	{ return ply/2; }
	public final Move getMove()		{ return move; }

	@Override
	public Node clone() {
		MoveNode clone = new MoveNode(ply, new Move(move));
		clone.hashKey = this.hashKey;
		clone.moveCountLen = this.moveCountLen;
		clone.engineValue = (this.engineValue==null) ? null:new Score(this.engineValue);
		return clone;
	}

	public void play(de.jose.chess.Position pos)
	{
		if (!pos.tryMove(move))
			throw new ReplayException(pos,move);
        hashKey = pos.getHashKey().value();
	}

	public void undo(de.jose.chess.Position pos)
	{
		pos.undoMove();
	}

	/**	@return the next move in the current line
	 */
	public MoveNode nextMove()
	{
		Node nd = this;
		for (;;) {
			if (nd.isLast())
				return null;
			else
				nd = nd.next();
			if (nd instanceof MoveNode)
				return (MoveNode)nd;
		}
	}

	/**
	 * show move number on black moves ?
	 * @return
	 */
	protected boolean showNumber()
	{
		for (Node nd = previous(); nd != null; nd = nd.previous())
			switch (nd.type()) {
			case MOVE_NODE:
				MoveNode mvnd = (MoveNode)nd;
				return mvnd.move.isNullMove();
			case ANNOTATION_NODE:		continue;
			case DIAGRAM_NODE:
			default:
			case COMMENT_NODE:			return true;
			}
		return true;
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return parent().getStyle(doc,"body.line");
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		formatter.setTextStyle(getDefaultStyle(doc));
		formatter.setTextLanguage();

		int moveFormat = Application.theUserProfile.getInt("doc.move.format",MoveFormatter.SHORT);

		de.jose.chess.Position pos = getGame().getPosition();
		moveCountLen = 0;
		if (!move.isNullMove()) {
			if ((ply % 2) == 0) {
				String moveCount = (ply / 2 + 1) + ".";
				moveCountLen = moveCount.length();
				formatter.text(moveCount, 0);
			} else if (showNumber()) {
				String moveCount = (ply / 2 + 1) + "...";
				moveCountLen = moveCount.length();
				formatter.text(moveCount, 0);
			}
		}

        formatter.setDocument(doc,at);
        formatter.setFormat(moveFormat);

		boolean useFigurines = JoStyleContext.useFigurineFont(formatter.getTextStyle());
		if (useFigurines) {
			Style figStyle = parent().getStyle(doc,"body.figurine");
			formatter.setFigStyle(figStyle);
		}
		else {
            formatter.setFigStyle(null);
		}

        formatter.format(move,pos);
        formatter.text(' ');
        formatter.flush();
        int len = formatter.position() - at;

		pos.tryMove(move);
		if (move.moving==null && !move.isNullMove())
			throw new ReplayException(pos,move);
		setLength(len);
	}

	public void updateMoveCount(StyledDocument doc)
		throws BadLocationException
	{
		if ((ply%2)==0)
			return;
		if ((moveCountLen==0) && showNumber()) {
			//	insert move count
			String moveCount = (ply/2+1)+"...";
			moveCountLen = moveCount.length();
			addLength(moveCountLen);
			doc.insertString(getStartOffset(),moveCount, getDefaultStyle(doc));
		}
		else if ((moveCountLen>0) && !showNumber()) {
			//	remove move count
			subLength(moveCountLen);
			doc.remove(getStartOffset(), moveCountLen);
			moveCountLen = 0;
		}
	}

    void writeBinary(BinWriter writer) {
        writer.move(move);
    }

	public String toString() {
		if (move!=null)
			return move.toString();
		else
			return "missing move?";
	}

	public static String toString(MoveNode mvnd) {
		if (mvnd==null)
			return "null";
		else
			return mvnd.toString();
	}

	static class SAXMoveFormatter extends StringMoveFormatter
    {
		JoContentHandler handler;
		boolean useFigurines = true;
		char[] chrs = new char[1];
		SAXException exception;

		public void figurine(int piece, boolean promotion)
		{
			try {
				if (useFigurines) {
					char pc = LOWER_PIECE_CHARACTERS[EngUtil.uncolored(piece)];
					handler.element(String.valueOf(pc),null);
				}
				else
					text(pieceChars[EngUtil.uncolored(piece)]);
			} catch (SAXException saxex) {
				exception = saxex;
			}
		}

		public void text(char chr)
		{
			try {
				chrs[0] = chr;
				handler.characters(chrs,0,1);
			} catch (SAXException saxex) {
				exception = saxex;
			}
		}

		public void text(String str, int castling)
		{
			try {
				handler.characters(str);
			} catch (SAXException saxex) {
				exception = saxex;
			}
		}
    }

	public boolean isCloseDuplicate()
	{
		//	first in variation ?
		if (previous(MOVE_NODE)!=null) return false;
		//	nest level >= 2
		LineNode line = parent();
		if (line.level() <= 1) return false;
		//
		Node anchor = line.previous(LINE_NODE,MOVE_NODE);
		if (anchor.is(MOVE_NODE)) {
			MoveNode ma = (MoveNode)anchor;
			return ma.move.equals(this.move);
		}
		return false;
	}

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		de.jose.chess.Position pos = getGame().getPosition();

		boolean hide=false;
		if (handler.context.hide_nullmoves && move.isNullMove())
			hide = true;
		if (handler.context.hide_closedupls && isCloseDuplicate())
			hide = true;

		if (!hide)
			saxBeforeMove(pos, ply,move,showNumber(), handler);

		pos.tryMove(move);
		if (!move.isNullMove() && (move.moving==null))
			throw new ReplayException(pos,move);

		if (!hide)
			saxAfterMove(pos, move, handler);
	}

	public static void saxBeforeMove(de.jose.chess.Position pos,
	                         int ply, Move move, boolean showNumber,
	                         JoContentHandler handler) throws SAXException
	{
/*
		System.err.print(" ");
		System.err.print(pos.gameMove());
		if (pos.whiteMovesNext())
			System.err.print(".");
		else
			System.err.print("...");
		System.err.print(move.toString());
*/
		//  print formatted text, what else ?
		handler.startElement("m");

		int format = handler.context.profile.getInt("doc.move.format",MoveFormatter.SHORT);
        saxFormatter.useFigurines = handler.context.styles.useFigurineFont();

		saxFormatter.handler = handler;
		saxFormatter.exception = null;
		saxFormatter.setFormat(format);

		if (!saxFormatter.useFigurines) {
			String language = handler.context.styles.getFigurineLanguage();
			saxFormatter.setLanguage(language);
		}

		if ((ply%2) == 0) {
			String moveCount = (ply/2+1)+".";
			saxFormatter.text(moveCount, 0);
		}
	   else if (showNumber) {
		   String moveCount = (ply/2+1)+"...";
		   saxFormatter.text(moveCount, 0);
	   }

		//  formatting must be done before the move is executed; to detect ambiguities
		saxFormatter.format(move,pos,false);
	}

	public static void saxAfterMove(de.jose.chess.Position pos, Move move,
	                         JoContentHandler handler) throws SAXException
	{
		//  check can only be detected after move has beem executed
		saxFormatter.formatCheck(move);

		saxFormatter.flush();

		if (saxFormatter.exception!=null)
			throw saxFormatter.exception;

		//  dump position AFTER move
		if (handler.moveFen)	
			handler.element("fen",pos.toString());

		handler.endElement("m");
	}

}
