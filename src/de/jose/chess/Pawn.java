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

public class Pawn
		extends Piece
		implements BinaryConstants
{
	private static final int[] v = {
		+1,  0,
		+1, +1,
		+1, -1,
		+2,  0,
	};
	
	private static final byte[] white_vector = createVector(v,WHITE);
	private static final byte[] black_vector = createVector(v,BLACK);
	private static final byte[][] white_vtable = createVTable(white_vector,WHITE);													  
	private static final byte[][] black_vtable = createVTable(black_vector,BLACK);													  
		
	public Pawn(int color, Board owner)
	{
		super(PAWN+color,owner);
	}

	public final byte[] getVector()					{ return isWhite() ? white_vector:black_vector; }
	public final byte[][] getVTable()				{ return isWhite() ? white_vtable:black_vtable; }
	public static byte[][] getVTable(int color)		{ return (EngUtil.isWhite(color)) ? white_vtable:black_vtable; }

	/**
	 * @return true if a piece on 'from' can move to 'to'
	 */
    public boolean checkMove(Move mv)
    {
		byte[][] vt = getVTable();
		byte[] vtf = vt[mv.from];
		if (vtf==null) return false;
		byte v = vtf[mv.to];
		
		switch (v) {
		case 0:	return false;
				
		case 1:	//	single step
			if (!pos.isEmpty(mv.to)) 
				return false;
			
			if (EngUtil.isPromotionRow(EngUtil.rowOf(mv.to), color()))
				return mv.isPromotion();
			else
				return true;
			
		case 2:	//	capture (right)
		case 3:	//	capture (left)
			if (pos.enPassantFile()==EngUtil.fileOf(mv.to) &&
				EngUtil.isEnPassantRow(EngUtil.rowOf(mv.from), color())) 
			{
				mv.setEnPassant(true);
				mv.captured = pos.piece(mv.getEnPassantSquare());
				return true;
			}
				
			if (mv.captured==null) 
				return false;
			
			if (EngUtil.isPromotionRow(EngUtil.rowOf(mv.to),color()))
				return mv.isPromotion();
			return true;
			
		case 4:	//	double step
			int step = isWhite() ? OUTER_BOARD_WIDTH : -OUTER_BOARD_WIDTH;
			if (!pos.isEmpty(mv.from+step) ||
				!pos.isEmpty(mv.to)) return false;
			
			mv.setPawnDouble(true);
			return true;
		}
		throw new IllegalStateException("");
	}

	/**
	 * @return true if a piece on 'from' can move to 'to'
	 */
	public static boolean couldBePromotion(Board pos, Move mv, int color)
	{
		byte[][] vt = getVTable(color);
		byte[] vtf = vt[mv.from];
		if (vtf==null) return false;
		byte v = vtf[mv.to];

		switch (v) {
		case 1:	//	single step
			if (!pos.isEmpty(mv.to))
				return false;
			else
				return EngUtil.isPromotionRow(EngUtil.rowOf(mv.to),color);

		case 2:	//	capture (right)
		case 3:	//	capture (left)
			if (pos.isEmpty(mv.to))
				return false;
			else
				return EngUtil.isPromotionRow(EngUtil.rowOf(mv.to),color);

		default:
		case 4:	//	double step
			return false;
		}
	}

	/**
	 * compute the next move in an iteration
	 */
	public boolean nextMove(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		result.from = square();
		byte[] vector = getVector();

		switch (i1.i) {
		case 0:		//  one-step
					result.to = result.from+vector[1];
					if (pos.isEmpty(result.to)
					        && nextPromo(i1,i2,result))
						return true;
					//	fall-through íntended
					i1.i++;
		case 1:		//  capture right
					result.to = result.from+vector[2];
					if (pos.checkMove(result)
							&& nextPromo(i1,i2,result)
							&& checkMove(result))
						return true;
					//	fall-through íntended
					i1.i++;
		case 2:		//  capture left
					result.to = result.from+vector[3];
					if (pos.checkMove(result)
							&& nextPromo(i1,i2,result)
							&& checkMove(result))
						return true;
					//	fall-through íntended
					i1.i++;
		case 3:		//  two-step
					if (EngUtil.rowOf(result.from)==EngUtil.pawnRow(this.color())) {
						result.to = result.from + vector[4];
						if (pos.isEmpty(result.to) && pos.isEmpty(result.from + vector[1])) {
							i1.i++;
							return true;
						}
					}
		}
		return false;
	}

	protected boolean nextPromo(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		if (!couldBePromotion(pos,result, EngUtil.colorOf(piece))) {
			//  increment i1
			i1.i++;
			return true;
		}

		//  else: increment i2
		switch (i2.i) {
		default:
			result.setPromotionPiece(i2.i=KNIGHT);
			return true;

		case KNIGHT:
		case BISHOP:
		case ROOK:
			result.setPromotionPiece(++i2.i);
			return true;

		case QUEEN:
			return false;
		}
	}

	public short encodeMove(Move mv)
	{
		int direction = getVTable()[mv.from][mv.to] - 1;
		
		if (mv.isPromotion())
		{
			//	promotion
			int base = SHORT_A_PROMOTION + listIndex();
			int promotionPiece = mv.getPromotionPiece()-KNIGHT;
			return (short)((1<<13) | (direction << 10) | (promotionPiece << 8) | base);
		}
		else
		{
			//	normal move
			int base = SHORT_A_PAWN + (listIndex() << 2);
			return (short)(base | direction);
		}
	}
	
	public boolean decodeMove(short code, Move result)
	{
		int direction;
		
		if ((code & 0x00f0) == SHORT_A_PROMOTION)
		{
			//	promotion code
			result.setPromotionPiece(KNIGHT + ((code >> 8) & 0x0003));
			direction = (code >> 10) & 0x0003;
		}
		else	//	normal code
			direction = code & 0x0003;
		
		result.from = square();
		result.to = result.from + getVector()[direction+1];
		
		return checkMove(result);
	}
	
	protected static byte[] createVector(int[] v, int color) {
		byte[] result = new byte[v.length/2+1];
		int factor = EngUtil.isWhite(color) ? +1 : -1;
		
		for (byte j=1; j<result.length; j++)
			result[j] = (byte)(factor * (v[2*(j-1)]*OUTER_BOARD_WIDTH + v[2*j-1]));
		
		return result;
	}
	
	protected static byte[][] createVTable(byte[] vector, int color)
	{
		byte[][] t = new byte[OUTER_BOARD_SIZE][];
		int homerow = EngUtil.isWhite(color) ? WHITE_PAWN_ROW : BLACK_PAWN_ROW;
		
		for (int file=FILE_A; file<=FILE_H; file++) {
			for (int row = ROW_2; row <= ROW_7; row++) {
				int from = EngUtil.square(file,row);
				t[from] = new byte[OUTER_BOARD_SIZE];
				
				for (byte j=1; j<(vector.length-1); j++)  {
					int to = from+vector[j];
					t[from][to] = j;
				}
				if (row==homerow) {
					int to = from + vector[vector.length-1];
					t[from][to] = (byte)(vector.length-1);
				}
			}
		}
		return t;
	}

	public static boolean isOnHomeRow(int color, int square)
	{
		return getRowAdvance(color,square)==0;
	}

	public static int getRowAdvance(int color, int square)
	{
		if (EngUtil.isWhite(color))
			return EngUtil.rowOf(square)- WHITE_PAWN_ROW;
		else
			return BLACK_PAWN_ROW - EngUtil.rowOf(square);
	}

	public static boolean leftHomeRow(Move mv)
	{
		return isOnHomeRow(mv.moving.color(),mv.from);
	}

}
