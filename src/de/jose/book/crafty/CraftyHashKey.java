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

package de.jose.book.crafty;

import de.jose.chess.HashKey;
import de.jose.chess.EngUtil;
import de.jose.chess.Position;

/**
 * CraftyHashKey
 *
 * @author Peter Schäfer
 */

public class CraftyHashKey
        extends HashKey
{
	//-------------------------------------------------------------------------------
	//	static fields
	//-------------------------------------------------------------------------------

	/**	random value for various pieces and squares	 */
	protected static long [] w_pawn_random = new long[64];
	protected static long [] b_pawn_random = new long[64];
	protected static long [] w_knight_random = new long[64];
	protected static long [] b_knight_random = new long[64];
	protected static long [] w_bishop_random = new long[64];
	protected static long [] b_bishop_random = new long[64];
	protected static long [] w_rook_random = new long[64];
	protected static long [] b_rook_random = new long[64];
	protected static long [] w_queen_random = new long[64];
	protected static long [] b_queen_random = new long[64];
	protected static long [] w_king_random = new long[64];
	protected static long [] b_king_random = new long[64];

	protected static long[] castle_random_w = new long[2];
	protected static long[] castle_random_b = new long[2];

	protected static long[] enpassant_random = new long[65];

	protected static long[] wtm_random = new long[2];

	public static long START_POSITION                         = 0x0L;
	public static long START_POSITION_REVERSED                = 0x0L;
	public static long START_POSITION_IGNORE_FLAGS            = 0x0L;
	public static long START_POSITION_NO_COLOR          = 0x0L;

	static {
		//  setup Crafty compatible random map
		InitializeRandomHash();

		Position pos = new Position(CraftyHashKey.class);
		pos.setupInitial();
		pos.computeHashKeys();

		START_POSITION = pos.getHashKey().value();
		START_POSITION_REVERSED = pos.getReversedHashKey().value();

		pos.setMovesNext(0);
		pos.computeHashKeys();
		START_POSITION_NO_COLOR = pos.getHashKey().value();

		pos.getHashKey().setIgnoreFlags(true);
		pos.computeHashKeys();

		START_POSITION_IGNORE_FLAGS = pos.getHashKey().value();
	}





	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public CraftyHashKey(boolean reversed)
	{
		super(reversed);
	}


	public CraftyHashKey()
	{
		this(false);
	}

	public CraftyHashKey(long value)
	{
		this();
		setValue(value);
	}

	//-------------------------------------------------------------------------------
	//	implements HashKey
	//-------------------------------------------------------------------------------

	public void set(int square, int piece)
	{
		/*  translate jose square encoding (0..80)
		    to crafty square encoding (A1=0, B1=1, .. H8=63)
		*/
		if (isReversed) {
			square = EngUtil.mirrorSquare(square);
			piece = EngUtil.oppositeColor(piece);
		}

		square = square64(square);

		switch (piece) {
		case WHITE_PAWN:        theValue ^= w_pawn_random[square]; break;
		case BLACK_PAWN:        theValue ^= b_pawn_random[square]; break;
		case WHITE_KNIGHT:      theValue ^= w_knight_random[square]; break;
		case BLACK_KNIGHT:      theValue ^= b_knight_random[square]; break;
		case WHITE_BISHOP:      theValue ^= w_bishop_random[square]; break;
		case BLACK_BISHOP:      theValue ^= b_bishop_random[square]; break;
		case WHITE_ROOK:        theValue ^= w_rook_random[square]; break;
		case BLACK_ROOK:        theValue ^= b_rook_random[square]; break;
		case WHITE_QUEEN:       theValue ^= w_queen_random[square]; break;
		case BLACK_QUEEN:       theValue ^= b_queen_random[square]; break;
		case WHITE_KING:        theValue ^= w_king_random[square]; break;
		case BLACK_KING:        theValue ^= b_king_random[square]; break;

		default:                throw new IllegalArgumentException();
		}
	}

	public void set(int flags)
	{
		if (!ignoreFlags) {
		    if (isReversed)
		        flags = EngUtil.reverseFlags(flags);

		    //  castling privileges
			if (!EngUtil.canCastle(flags,WHITE_KINGS_CASTLING))
				theValue ^= castle_random_w[0];
			if (!EngUtil.canCastle(flags,WHITE_QUEENS_CASTLING))
				theValue ^= castle_random_w[1];
			if (!EngUtil.canCastle(flags,BLACK_KINGS_CASTLING))
				theValue ^= castle_random_b[0];
			if (!EngUtil.canCastle(flags,BLACK_QUEENS_CASTLING))
				theValue ^= castle_random_b[1];

			boolean whiteToMove = (flags & WHITE) != 0;

			//  en-passant target square
			int epFile = EngUtil.enPassantFile(flags);
			if (epFile != 0) {
				int epSquare;
				if (whiteToMove)
					epSquare = EngUtil.square(epFile,ROW_6);
				else
					epSquare = EngUtil.square(epFile,ROW_3);
				theValue ^= enpassant_random[square64(epSquare)];
			}

			//  color to move
			if (EngUtil.allOf(flags,WHITE))
				theValue ^= wtm_random[0];
			else if (EngUtil.allOf(flags,BLACK))
				theValue ^= wtm_random[1];
			else
				/* this branch is used when computing hash keys for look up in the crafty book.
				    there is no turn color. */;
		}
	}

	public void clear(int square, int piece)
	{
		//  XOR is revesible
		set(square,piece);
	}

	public void clear(int flags)
	{
		//  XOR is revesible
		set(flags);
	}

	public void clear()
	{
		theValue = 0L;
	}

	public long getInitialValue(boolean ignoreFlags, boolean reversed)
	{
	    if (ignoreFlags)
	        return START_POSITION_IGNORE_FLAGS;
	    else if (reversed)
	        return START_POSITION_REVERSED;
	    else
	        return START_POSITION;
	}

	//-------------------------------------------------------------------------------
	//	private parts
	//-------------------------------------------------------------------------------

	/*  translate jose square encoding (0..80)
	    to crafty square encoding (A1=0, B1=1, .. H8=63)
	*/
	private static int square64(int jose_square)
	{
		int file0 = EngUtil.fileOf(jose_square)-FILE_A;
		int row0 = EngUtil.rowOf(jose_square)-ROW_1;
		return 8*row0+file0;
	}


	private static void InitializeRandomHash()
	{
		CraftyRandom random = new CraftyRandom();
		int i;
		for (i = 0; i < 64; i++) {
			w_pawn_random[i] = random.Random64();
			b_pawn_random[i] = random.Random64();
			w_knight_random[i] = random.Random64();
			b_knight_random[i] = random.Random64();
			w_bishop_random[i] = random.Random64();
			b_bishop_random[i] = random.Random64();
			w_rook_random[i] = random.Random64();
			b_rook_random[i] = random.Random64();
			w_queen_random[i] = random.Random64();
			b_queen_random[i] = random.Random64();
			w_king_random[i] = random.Random64();
			b_king_random[i] = random.Random64();
		}
		for (i = 0; i < 2; i++) {
			castle_random_w[i] = random.Random64();
			castle_random_b[i] = random.Random64();
		}
		enpassant_random[0] = 0;
		for (i = 1; i < 65; i++) {
			enpassant_random[i] = random.Random64();
		}
		for (i = 0; i < 2; i++) {
			wtm_random[i] = random.Random64();
		}
	}


}