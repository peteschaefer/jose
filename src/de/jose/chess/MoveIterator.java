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

package de.jose.chess;

import de.jose.Util;

import java.util.ArrayList;

/**
 * iterates over a move list
 */

public class MoveIterator
{
	/**	current Position	 */
	protected Position position;
	/**	next move	 */
	protected Move move;
	/**	are there more moves ?	 */
	protected boolean hasNext;
	
	/**	iteration variables fro use by move generators	 */
	protected Util.IntHandle i1;
	protected Util.IntHandle i2;
	
	/**	current piece	 */
	protected Piece p;
	/**	iterate over list of pieces	 */
	protected ArrayList pieceList;
	protected int ilist;
	/**	iterate over all pieces (of a given color)	 */
	protected ArrayList[] pieceListArray;
	protected int iarray;
	
	/**
	 * a MoveIterator that iterates all (pseudo-legal) moves of a given piece
	 */
	public MoveIterator(Position pos)
	{
		position = pos;
		move = new Move(0,0);
		i1 = new Util.IntHandle();
		i2 = new Util.IntHandle();
		if (pos.whiteMovesNext())
			reset(pos.theWhitePieces);
		else
			reset(pos.theBlackPieces);
	}
	
	/**
	 * a MoveIterator that iterates all (pseudo-legal) moves of a given piece
	 */
	public MoveIterator(Position pos, Piece aPiece)
	{
		this(pos);
		reset(aPiece);
	}
	
	public void reset(Piece aPiece)
	{
		i1.i = i2.i = 0;
		hasNext = true;
		p = aPiece;
		pieceList = null;
		pieceListArray = null;
		hasNext = fetchNext();
	}
	
	/**
	 * a MoveIterator that iterates all (pseudo-legal) moves of a given piece class
	 */
	public MoveIterator(Position pos, ArrayList pieces)
	{
		this(pos);
		reset(pieces);
	}
	
	public void reset(ArrayList pieces)
	{
		i1.i = i2.i = 0;
		hasNext = true;
		pieceList = pieces;
		ilist = -1;
		pieceListArray = null;
		p = nextPiece();
	}
	
	/**
	 * a MoveIterator that iterates all (pseudo-legal) moves of a given color
	 */
	public MoveIterator(Position pos, ArrayList[] pieces)
	{
		this(pos);
		reset(pieces);
	}
	
	public void reset(ArrayList[] pieces)
	{
		i1.i = i2.i = 0;
		hasNext = true;
		pieceListArray = pieces;
		iarray = -1;
		pieceList = nextPieceList();
		p = nextPiece();
	}
	
	
	public final boolean next()	{ 
		if (!hasNext) return false;
		hasNext = fetchNext();
		return hasNext;
	}
	
	public final Move getMove()	{ 
		return move; 
	}
	
	
	protected boolean fetchNext()
	{
		while (p!=null)
		{
			move.flags = 0;
			move.moving = p;
			move.captured = null;

			if (p.nextMove(i1,i2, move))
				return true;
			
			p = nextPiece();
		}		
		return false;
	}
	
	protected Piece nextPiece()
	{
		while (pieceList != null) {
			while (++ilist < pieceList.size()) {
				Piece p = (Piece)pieceList.get(ilist);
				if (!p.isVacant()) {
					i1.i = i2.i = 0;
					return p;
				}
			}
		
			pieceList = nextPieceList();
		}
		return null;
	}
	
	protected ArrayList nextPieceList()
	{
		if (pieceListArray==null) return null;

		while (++iarray < pieceListArray.length)
			if (pieceListArray[iarray]!=null)	{
				ilist = -1;
				return pieceListArray[iarray];
			}
		return null;
	}
}
