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

package de.jose.book.polyglot;

import de.jose.book.BookEntry;
import de.jose.chess.Move;
import de.jose.chess.EngUtil;
import de.jose.chess.Constants;
import de.jose.Util;

/**
 * entry_t
 *
 * @author Peter Schäfer
 */
public class PolyglotBookEntry extends BookEntry
{
	public /*uint64*/long key;
//	public /*uint16*/int move;
//	public /*uint16*/int count;
	public /*uint16*/int n;
	public /*uint16*/int sum;

	//  the book entry can also serve as HashMap key
	public int hashCode() {
		return (int)key;
	}

	public boolean equals(Object obj) {
		PolyglotBookEntry that = (PolyglotBookEntry)obj;
		return this.key==that.key;
	}


	private static final int MoveNone = 0;  // HACK: a1a1 cannot be a legal move
	private static final int MoveNull = 11; // HACK: a1d2 cannot be a legal move

	private static final int MovePromote   = 2 << 14;

	private static final int MovePromoteKnight = MovePromote | (0 << 12);
	private static final int MovePromoteBishop = MovePromote | (1 << 12);
	private static final int MovePromoteRook   = MovePromote | (2 << 12);
	private static final int MovePromoteQueen  = MovePromote | (3 << 12);

	public static Move createMove(int imove, boolean isTransposedColor)
	{
		if (imove==MoveNone) return null;
		if (imove==MoveNull) return Move.NULLMOVE;

		int from64 = (imove>>6) & 0x3f;
		int to64 = (imove & 0x3f);

		int from = SQUARE_FROM_64(from64);
		int to = SQUARE_FROM_64(to64);

		if (isTransposedColor) {
			from = EngUtil.mirrorSquare(from);
			to = EngUtil.mirrorSquare(to);
		}

		Move result = new Move(from,to);

		if (Util.allOf(imove,MovePromoteKnight)) result.setPromotionPiece(Constants.KNIGHT);
		if (Util.allOf(imove,MovePromoteBishop)) result.setPromotionPiece(Constants.BISHOP);
		if (Util.allOf(imove,MovePromoteRook)) result.setPromotionPiece(Constants.ROOK);
		if (Util.allOf(imove,MovePromoteQueen)) result.setPromotionPiece(Constants.QUEEN);

		return result;
	}

	private static int SQUARE_FROM_64 (int square)
	{
		int file0 = square % 8;
		int row0 = square / 8;
		return EngUtil.square(Constants.FILE_A+file0, Constants.ROW_1+row0);
	}

}

