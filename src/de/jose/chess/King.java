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

/**
 *	a King is a (0,1)-(1,1)-Leaper with the extra ability of castling
 */
public class King
		extends Leaper
		implements BinaryConstants
{
	private static final int[] v = 
	{ 
		+1,+1, 
		-1,+1, 
		-1,-1, 
		+1,-1,
		 0,+1,
		 0,-1,
		+1, 0,
		-1, 0,
	};
	
	private static final byte[] vector = createVector(v);
	private static final byte[][] white_vtable = createVTable(vector, WHITE);
	private static final byte[][] black_vtable = createVTable(vector, BLACK);
	
	public King(int color, Board owner)
	{
		super(Constants.KING+color,owner);
	}
	
	public byte[] getVector()		{ return vector; }
	
	public byte[][] getVTable()		{ return isWhite() ? white_vtable:black_vtable; }
	
	/**
	 * @return true if a piece on 'from' would attack a piece on 'to'
	 */
	public boolean checkMove(Move mv)
	{
		//  castling flags are supposed to be set already, by isCastlngGesture, and Board.checkMove
		if (mv.isCastling()) return true;

		//  normal (leaper) moves
		byte[][] vt = getVTable();
		byte v = vt[mv.from][mv.to];

		return (v!=0);
	}


	public boolean isCastlingGesture(Board pos, Move mv)
	{
		//  condition: move must be on home row
		int home_row = isWhite() ? ROW_1:ROW_8;

		if (EngUtil.rowOf(mv.from)!=home_row || EngUtil.rowOf(mv.to)!=home_row)
			return false;

		//  castling gesture (1): king moves two or more squares to the left/right
		if (Math.abs(mv.to-mv.from) >= 2)
			return checkCastling(pos,mv);

		//  FRC castling gesture (2): king moves onto castling rook
		Piece dest = pos.piece(mv.to);
		if (dest!=null && dest.isRook() && dest.color()==this.color())
			return checkCastling(pos,mv);

		return false;
	}


	protected boolean checkCastling(Board pos, Move mv)
	{
		if (isWhite()) {
			if (mv.to > mv.from)
				return checkCastling(pos, mv, WHITE_KINGS_CASTLING);
			else
				return checkCastling(pos, mv, WHITE_QUEENS_CASTLING);
		}
		else {
			if (mv.to > mv.from)
				return checkCastling(pos, mv, BLACK_KINGS_CASTLING);
			else
				return checkCastling(pos, mv, BLACK_QUEENS_CASTLING);
		}
	}

	protected boolean checkCastling(Board pos, Move mv, int castling)
	{
		//  internal representation: king moves onto rook, but no capture !
		if (!pos.canCastle(castling))
			return false;
		if (!pos.isEmptyForCastling(castling))
			return false;

		mv.to = pos.castlingRookSquare(castling);
		mv.flags |= castling;
		return true;
	}

	/**
	 * compute the next move in an iteration
	 */
	public boolean nextMove(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		if (super.nextMove(i1,i2,result)) return true;
		
		switch (i1.i) {
		case 9:		/*	kings castling	*/
					i1.i++; //  FRC
					result.to = result.from+2;
					if (checkCastling(pos,result)) return true;
					
		case 10:		/*	queens castling	*/
					i1.i++; //  FRC
					result.to = result.from-2;
					if (checkCastling(pos,result)) return true;
		}
		
		return false;
	}
	
	private static byte[][] createVTable(byte[] v, int color) {
		byte[][] t = Leaper.createVTable(v);
/*
		switch (color) {    //  FRC castling gestures: can't use VTable ?
		case WHITE:		t[E1][G1] = 9; 
						t[E1][C1] = 10;
						break;
		case BLACK:		t[E8][G8] = 9;
						t[E8][C8] = 10;
						break;
		}
*/
		return t;
	}

	
	public short encodeMove(Move mv)
	{
		if (mv.isCastling())
		{
			if (mv.to > mv.from)
				return SHORT_K_CASTLING;	//	kingside castling
			else
				return SHORT_Q_CASTLING;	//	queenside castling
		}
		
		byte direction = (byte)(getVTable()[mv.from][mv.to]-1);
		
		return (short)(SHORT_KING | direction);
	}
	
	public boolean decodeMove(short code, Move result)
	{
		result.from = square();
		
		switch (code)
		{   //  FRC
		case SHORT_K_CASTLING:	//	kingside castling
			result.to = result.from+2;
			return checkCastling(pos,result);

		case SHORT_Q_CASTLING:	//	queenside castling
			result.to = result.from-2;
			return checkCastling(pos,result);

		default:
			int direction = code & 0x0007;
			result.to = result.from + getVector()[direction+1]; break;
		}
		
		return checkMove(result);
	}

}
