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

/**
 * represents a chess piece
 */


import de.jose.Util;

abstract public class Piece
		implements Constants
{
	//-------------------------------------------------------------------------------
	//	fields
	//-------------------------------------------------------------------------------

	/**	piece constant (color)	 */
	protected int piece;
	/**	current position	 */
	protected int square;

	/** owner position  */
	protected Board pos;

	/**	index in piece list	 */
	protected int listIndex;
	/** if this piece was promoted: file of original pawn (FILE_A..FILE_H), 0 indicates normal piece  */
    protected int pawnFile;



	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public Piece(int aPiece, Board owner)
	{
		piece = aPiece;
		pos = owner;
	}
	
	public static Piece newPiece(int aPiece, Board owner)
	{
		int color = EngUtil.colorOf(aPiece);
		if (color==BLOCK)
			return new Block(owner);
		
		switch (EngUtil.uncolored(aPiece))
		{
		case PAWN:		return new Pawn(color,owner);
		case KNIGHT:	return new Knight(color,owner);
		case BISHOP:	return new Bishop(color,owner);
		case ROOK:		return new Rook(color,owner);
		case QUEEN:		return new Queen(color,owner);
		case KING:		return new King(color,owner);
		default:		throw new IllegalArgumentException("unexpected piece");
		}
	}
	
	
	//-------------------------------------------------------------------------------
	//	basic access
	//-------------------------------------------------------------------------------

	public Board getOwner()                         { return pos; }

	public final int piece()						{ return piece; }
	
	public final int color()						{ return EngUtil.colorOf(piece); }
	public final int uncolored()					{ return EngUtil.uncolored(piece); }
	
	public final boolean isWhite()					{ return EngUtil.isWhite(piece); }
	public final boolean isBlack()					{ return EngUtil.isBlack(piece); }
	
	public final boolean isPawn()					{ return EngUtil.uncolored(piece)==PAWN; }
	public final boolean isKing()					{ return EngUtil.uncolored(piece)==KING; }
	public final boolean isRook()                   { return EngUtil.uncolored(piece)==ROOK; }

	public final boolean canCapture(int piece)		{ return EngUtil.canCapture(this.piece,piece); }
	public final boolean canCapture(Piece piece)	{ return EngUtil.canCapture(this.piece, piece.piece); }

	public final int square()						{ return square; }
	public final void setSquare(int square)			{ this.square = square; }
	
	public final boolean isVacant()					{ return square <= 0; }
	public final void setVacant()					{ square = 0; }
	
	public final int listIndex()					{ return listIndex; }
	public final void setListIndex(int index)		{ listIndex = index; }
	
	public final int getPawnFile()      			{ return pawnFile; }
	public void setPawnFile(int file)               { pawnFile = file; }

	//-------------------------------------------------------------------------------
	//	move generator
	//-------------------------------------------------------------------------------
	

	abstract public byte[] getVector();
	
	abstract public byte[][] getVTable();
	
	/**
	 * @return true if a piece on 'from' would attack a piece on 'to'
	 */
	public boolean checkMove(Move mv)
	{
		throw new AbstractMethodError();
	}
	

	public final boolean isPromotionPiece() { return pawnFile != 0; }
	
	abstract public short encodeMove(Move mv);
	
	abstract public boolean decodeMove(short code, Move result);
	
	/**
	 * compute the next move in an iteration
	 */
	public boolean nextMove(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		throw new AbstractMethodError();
	}
	
	protected static byte[] createVector(int[] v) {
		byte[] result = new byte[v.length/2+1];
		for (byte j=1; j<result.length; j++)
			result[j] = (byte)(v[2*(j-1)]*OUTER_BOARD_WIDTH + v[2*j-1]);
		return result;
	}

}
