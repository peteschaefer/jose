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

package de.jose.book.shredder;

import de.jose.chess.EngUtil;
import de.jose.chess.Constants;

/**
 * @author Peter Schäfer
 */

public class Util
{
	public static String[/*13*/] pieceToString;

	public static String[/*64*/] squareToString;

	public static final char EMPTY          = 0;
	public static final char WHITE_KING     = 1;
	public static final char WHITE_QUEEN    = 2;
	public static final char WHITE_ROOK     = 3;
	public static final char WHITE_BISHOP   = 4;
	public static final char WHITE_KNIGHT   = 5;
	public static final char WHITE_PAWN     = 6;
	public static final char BLACK_KING     = 7;
	public static final char BLACK_QUEEN    = 8;
	public static final char BLACK_ROOK     = 9;
	public static final char BLACK_BISHOP   = 10;
	public static final char BLACK_KNIGHT   = 11;
	public static final char BLACK_PAWN     = 12;

	public static int square64(int square)
	{
		int row0 = EngUtil.rowOf(square)- Constants.ROW_1;
		int file0 = EngUtil.fileOf(square)-Constants.FILE_A;
		return row0*8+file0;
	}

	public static int squareFrom64(int square64)
	{
		int file0 = square64%8;
		int row0 = square64/8;
		return EngUtil.square(file0+Constants.FILE_A, row0+Constants.ROW_1);
	}

	public static int piece13(int piece)
	{
		switch (piece)
		{
		case Constants.WHITE_KING:  return Util.WHITE_KING;
		case Constants.WHITE_QUEEN:  return Util.WHITE_QUEEN;
		case Constants.WHITE_ROOK:  return Util.WHITE_ROOK;
		case Constants.WHITE_BISHOP:  return Util.WHITE_BISHOP;
		case Constants.WHITE_KNIGHT:  return Util.WHITE_KNIGHT;
		case Constants.WHITE_PAWN:  return Util.WHITE_PAWN;

		case Constants.BLACK_KING:  return Util.BLACK_KING;
		case Constants.BLACK_QUEEN:  return Util.BLACK_QUEEN;
		case Constants.BLACK_ROOK:  return Util.BLACK_ROOK;
		case Constants.BLACK_BISHOP:  return Util.BLACK_BISHOP;
		case Constants.BLACK_KNIGHT:  return Util.BLACK_KNIGHT;
		case Constants.BLACK_PAWN:  return Util.BLACK_PAWN;

		default:
		case Constants.EMPTY:   return Util.EMPTY;
		}
	}

	public static int pieceFrom13(int piece13)
	{
		switch (piece13)
		{
		case Util.WHITE_KING:  return Constants.WHITE_KING;
		case Util.WHITE_QUEEN:  return Constants.WHITE_QUEEN;
		case Util.WHITE_ROOK:  return Constants.WHITE_ROOK;
		case Util.WHITE_BISHOP:  return Constants.WHITE_BISHOP;
		case Util.WHITE_KNIGHT:  return Constants.WHITE_KNIGHT;
		case Util.WHITE_PAWN:  return Constants.WHITE_PAWN;

		case Util.BLACK_KING:  return Constants.BLACK_KING;
		case Util.BLACK_QUEEN:  return Constants.BLACK_QUEEN;
		case Util.BLACK_ROOK:  return Constants.BLACK_ROOK;
		case Util.BLACK_BISHOP:  return Constants.BLACK_BISHOP;
		case Util.BLACK_KNIGHT:  return Constants.BLACK_KNIGHT;
		case Util.BLACK_PAWN:  return Constants.BLACK_PAWN;

		default:
		case Constants.EMPTY:   return Util.EMPTY;
		}
	}
}
