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

import de.jose.chess.Position;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;

/**
 * @author Peter Schäfer
 */

public class BoardStub
{


	protected char[] pieces = new char[64];
	protected boolean whiteMoves = true;

	public char[/*64*/] pieces()
	{
		return pieces;
	}

	public boolean whiteMoves()
	{
		return whiteMoves;
	}

	public BoardStub()
	{
		pieces[0] = Util.WHITE_ROOK;
		pieces[1] = Util.WHITE_KNIGHT;
		pieces[2] = Util.WHITE_BISHOP;
		pieces[3] = Util.WHITE_QUEEN;
		pieces[4] = Util.WHITE_KING;
		pieces[5] = Util.WHITE_BISHOP;
		pieces[6] = Util.WHITE_KNIGHT;
		pieces[7] = Util.WHITE_ROOK;

		pieces[8] = pieces[9] = pieces[10] = pieces[11] =
		pieces[12] = pieces[13] = pieces[14] = pieces[15] = Util.WHITE_PAWN;

		pieces[48] = pieces[49] = pieces[50] = pieces[51] =
		pieces[52] = pieces[53] = pieces[54] = pieces[55] = Util.BLACK_PAWN;

		pieces[56] = Util.BLACK_ROOK;
		pieces[57] = Util.BLACK_KNIGHT;
		pieces[58] = Util.BLACK_BISHOP;
		pieces[59] = Util.BLACK_QUEEN;
		pieces[60] = Util.BLACK_KING;
		pieces[61] = Util.BLACK_BISHOP;
		pieces[62] = Util.BLACK_KNIGHT;
		pieces[63] = Util.BLACK_ROOK;

		whiteMoves = true;
	}

	public BoardStub(Position pos, boolean reverseColors)
	{
		for (int row0 = 0; row0 < 8; row0++)
			for (int file0 = 0; file0 < 8; file0++)
			{
				int jose_piece = pos.pieceAt(file0+Constants.FILE_A, row0+Constants.ROW_1);
				int square64 = row0*8 + file0;

				if (reverseColors) {
					jose_piece = EngUtil.oppositeColor(jose_piece);
					square64 = (7-row0)*8 + file0;
				}

				switch (jose_piece)
				{
				case Constants.WHITE_KING:  pieces[square64] = Util.WHITE_KING; break;
				case Constants.WHITE_QUEEN:  pieces[square64] = Util.WHITE_QUEEN; break;
				case Constants.WHITE_ROOK:  pieces[square64] = Util.WHITE_ROOK; break;
				case Constants.WHITE_BISHOP:  pieces[square64] = Util.WHITE_BISHOP; break;
				case Constants.WHITE_KNIGHT:  pieces[square64] = Util.WHITE_KNIGHT; break;
				case Constants.WHITE_PAWN:  pieces[square64] = Util.WHITE_PAWN; break;

				case Constants.BLACK_KING:  pieces[square64] = Util.BLACK_KING; break;
				case Constants.BLACK_QUEEN:  pieces[square64] = Util.BLACK_QUEEN; break;
				case Constants.BLACK_ROOK:  pieces[square64] = Util.BLACK_ROOK; break;
				case Constants.BLACK_BISHOP:  pieces[square64] = Util.BLACK_BISHOP; break;
				case Constants.BLACK_KNIGHT:  pieces[square64] = Util.BLACK_KNIGHT; break;
				case Constants.BLACK_PAWN:  pieces[square64] = Util.BLACK_PAWN; break;

				case Constants.EMPTY:
				default:                                    pieces[square64] = Util.EMPTY; break;
				}
			}

		whiteMoves = pos.whiteMovesNext();
		if (reverseColors) whiteMoves = !whiteMoves;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		int empty;
		for (int row=7; row >= 0; row--)
		{
			empty = 0;

			for (int file=0; file <= 7; file++)
			{
				int p = pieces[8*row+file];
				if (p != Util.EMPTY && empty > 0) {
					buf.append((char)('0'+empty));
					empty = 0;
				}

				switch (p)
				{
				case Util.WHITE_KING:    buf.append('K'); break;
				case Util.WHITE_QUEEN:    buf.append('Q'); break;
				case Util.WHITE_ROOK:    buf.append('R'); break;
				case Util.WHITE_BISHOP:    buf.append('B'); break;
				case Util.WHITE_KNIGHT:    buf.append('N'); break;
				case Util.WHITE_PAWN:    buf.append('P'); break;

				case Util.BLACK_KING:    buf.append('k'); break;
				case Util.BLACK_QUEEN:    buf.append('q'); break;
				case Util.BLACK_ROOK:    buf.append('r'); break;
				case Util.BLACK_BISHOP:    buf.append('b'); break;
				case Util.BLACK_KNIGHT:    buf.append('n'); break;
				case Util.BLACK_PAWN:    buf.append('p'); break;

				default:
				case Util.EMPTY:     empty++; break;
				}
			}

			if (empty > 0) {
				buf.append((char)('0'+empty));
				empty = 0;
			}

			if (row>0) buf.append('/');
		}
		buf.append(' ');
		buf.append(whiteMoves ? 'w':'b');
		return buf.toString();
	}
}
