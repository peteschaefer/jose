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
 * abstract base class for Rider pieces (bishop,rook,queen)
 */

import de.jose.Util;

abstract public class Rider	
		extends Piece
		implements BinaryConstants
{
	protected Rider(int piece, Board owner)
	{
		super(piece,owner);
	}

    public boolean checkMove(Move mv)
    {
		byte[][] vt = getVTable();
		byte v = vt[mv.from][mv.to];
		if (v<=0) return false;
		
		byte d = getVector()[v];
		for (int s = mv.from+d; s != mv.to; s += d)
			if (!pos.isEmpty(s)) return false;
		
		return true;
	}
	
	/**
	 * compute the next move in an iteration
	 */
	public boolean nextMove(Util.IntHandle i1, Util.IntHandle i2, Move result)
	{
		result.from = square();
		byte[] vector = getVector();

		if (i1.i==0) i1.i=1;
		while (i1.i < vector.length) {
			result.to = result.from + (++i2.i) * vector[i1.i];
			if (canCapture(pos.pieceAt(result.to)))
				return true;

			i1.i++;
			i2.i=0;
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
					for (int to = from+v[j]; EngUtil.innerSquare(to); to += v[j]) 
						t[from][to] = j;
				}
			}
		}
		return t;
	}
	
	protected static byte[] createVtoBinaryTable(byte[] v2b) {
		byte[] result = new byte[v2b.length+1];
		for (byte j=1; j<result.length; j++)
			result[j] = (byte)(v2b[j-1]);
		return result;
	}

	abstract public byte[] getVtoBinaryTable();
		
	protected int encodeDirection(int from, int to, 
								  int diabase, int orthobase)
	{
		//	vector index 1...8
		byte vi = getVTable()[from][to];
		//	map to binary index 1..4
		byte bi = getVtoBinaryTable()[vi];
		
		switch (bi)
		{
		case BYTE_HORIZONTAL:			return orthobase + 8 + EngUtil.fileOf(to)-FILE_A;
		case BYTE_VERTICAL:				return orthobase + EngUtil.rowOf(to)-ROW_1;
		case BYTE_DIAGONAL_UP:			return diabase + EngUtil.rowOf(to)-ROW_1;
		case BYTE_DIAGONAL_DOWN:		return diabase + 8 + EngUtil.rowOf(to)-ROW_1;
		}
		throw new IllegalArgumentException();
	}
	
	protected int decodeDirection(int from, int direction,
								  int diabase, int orthobase)
	{
		if (diabase >= 0 && direction >= diabase && direction < (diabase+16))
		{
			direction -= diabase;
			if (direction >= 8)
			{
				//	diagonal down
				int row = ROW_1 + direction-8;
				int diff = EngUtil.rowOf(from)-row;
				return EngUtil.square(EngUtil.fileOf(from)+diff, row);
			}
			else 
			{
				//	diagonal up
				int row = ROW_1 + direction;
				int diff = EngUtil.rowOf(from)-row;
				return EngUtil.square(EngUtil.fileOf(from)-diff, row);
			}
		}
		
		if (orthobase >= 0 && direction >= orthobase && direction < (orthobase+16))
		{
			direction -= orthobase;
			if (direction >= 8)
			{
				//	horizontal
				int file = FILE_A + direction - 8;
				return EngUtil.square(file, EngUtil.rowOf(from));
			}
			else
			{
				//	vertical
				int row = ROW_1 + direction;
				return EngUtil.square(EngUtil.fileOf(from), row);
			}
		}
		throw new IllegalArgumentException();
	}
}
