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
 * abstract base class for leaper pieces (knight, king)
 */

abstract public class Leaper
		extends Piece
{
	protected Leaper(int piece, Board owner)
	{
		super(piece,owner);
	}

	/**
	 * @return true if a piece on 'from' can move to 'to'
	 */
    public boolean checkMove(Move mv)
    {
		byte[][] vt = getVTable();
		byte v = vt[mv.from][mv.to];
		return v > 0;
    }
	

	/**
	 * compute the next move in an iteration
	 */
	public boolean nextMove(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		byte[] vector = getVector();
		
		result.from = square();
		while (++i1.i < vector.length) {
			result.to = result.from + vector[i1.i];
			if (canCapture(pos.pieceAt(result.to)))
				return true;
		}
		return false;
	}
	
	protected static byte[][] createVTable(byte[] v) {
		byte[][] t = new byte[OUTER_BOARD_SIZE][];
		for (int file=FILE_A; file<=FILE_H; file++) {
			for (int row=ROW_1; row<=ROW_8; row++) {
				int from = EngUtil.square(file,row);
				t[from] = new byte[OUTER_BOARD_SIZE];
				for (byte j=1; j<v.length; j++) {
					int to = from+v[j];
					if (EngUtil.innerSquare(to))
						t[from][to] = j;
				}
			}
		}
		return t;
	}
	
}
