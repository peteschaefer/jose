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
 *	a Knight is a (1,2)-Leaper
 */
public class Knight
		extends Leaper
		implements BinaryConstants
{
	private static final int[] v = 
	{ 
		+1,+2, 
		-1,+2, 
		-2,+1, 
		-2,-1, 
		-1,-2, 
		+1,-2,
		+2,-1, 
		+2,+1,
	};
	
	private static final byte[] vector = createVector(v);
	private static final byte[][] vtable = createVTable(vector);
	
	public Knight(int color, Board owner)
	{
		super(KNIGHT+color,owner);
	}

	public byte[] getVector()	{ return vector; }
	
	public byte[][] getVTable()	{ return vtable; }
	
		
	public short encodeMove(Move mv)
	{
		int direction = getVTable()[mv.from][mv.to]-1;
		int base = 0;
		
		if (isPromotionPiece())
		{
			base = SHORT_A_PROMOTED + getPawnFile()-FILE_A;
			return (short)((1<<13) | (direction << 8) | base);
		}
		else
		{
			switch (listIndex())
			{
			case 0:		base = SHORT_Q_KNIGHT; break;		//	queen's knight
			case 1:		base = SHORT_K_KNIGHT; break;		//	king's knight
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
			direction = (code >> 8) & 0x0007;
		}
		else	//	normal code
			direction = code & 0x0007;
		
		result.from = square();
		result.to = result.from + getVector()[direction+1];
		
		return checkMove(result);
	}

}
