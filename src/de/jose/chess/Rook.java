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
 * a Rook is a (0,1)-Rider
 */
public class Rook
		extends Rider
		implements BinaryConstants
{
	private static final int[] v = 
	{ 
		 0,+1,
		 0,-1,
		+1, 0,
		-1, 0,
	};
	
	private static final byte[] v2b = 
	{
		BYTE_HORIZONTAL,
		BYTE_HORIZONTAL,
		BYTE_VERTICAL,
		BYTE_VERTICAL,
	};
	
	private static final byte[] vector = createVector(v);
	private static final byte[][] vtable = createVTable(vector);
	private static final byte[] vtobinary = createVtoBinaryTable(v2b);

	public Rook(int color, Board owner)
	{
		super(ROOK+color, owner);
	}

	public byte[] getVector()			{ return vector; }
	public byte[][] getVTable()			{ return vtable; }
	public byte[] getVtoBinaryTable()	{ return vtobinary; }

	public short encodeMove(Move mv)
	{
		int direction = encodeDirection(mv.from,mv.to,-1,0);
		int base = 0;
		
		if (isPromotionPiece()) {
			base = SHORT_A_PROMOTED + getPawnFile()-FILE_A;
			return (short)((1<<13) | (direction << 8) | base);
		}
		else
		{
			switch (listIndex())
			{
			case 0:		base = SHORT_Q_ROOK; break;		//	queen's rook
			case 1:		base = SHORT_K_ROOK; break;		//	king's rook
			}
			return (short)(base | direction);
		}
	}
	
	public boolean decodeMove(short code, Move result)
	{
		int direction;
		
		if ((code & 0x00f8) == SHORT_A_PROMOTED)
		{
			//	promoted piece code
			direction = (code >> 8) & 0x000f;
		}
		else	//	normal code
			direction = code & 0x000f;
		
		result.from = square();
		result.to = decodeDirection(result.from,direction,-1,0);
		
		return checkMove(result);
	}
}
