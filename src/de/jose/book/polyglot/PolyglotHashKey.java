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

import de.jose.chess.*;

import java.util.List;
import java.util.Iterator;

/**
 * PolyglotHashKey
 *
 * hash key implementation for Polyglot books
 *
 * @author Peter Schäfer
 */
public class PolyglotHashKey extends HashKey
{
	//  TODO implement reversed keys

	public static long START_POSITION                         = 0x0L;
	public static long START_POSITION_REVERSED                = 0x0L;
	public static long START_POSITION_IGNORE_FLAGS            = 0x0L;

	static {

		Position pos = new Position(PolyglotHashKey.class);
		pos.setupInitial();
		pos.computeHashKeys();

		START_POSITION = pos.getHashKey().value();
		START_POSITION_REVERSED = pos.getReversedHashKey().value();

		pos.getHashKey().setIgnoreFlags(true);
		pos.computeHashKeys();

		START_POSITION_IGNORE_FLAGS = pos.getHashKey().value();
	}



	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public PolyglotHashKey(boolean reversed)
	{
		super(reversed);
	}


	public PolyglotHashKey()
	{
		this(false);
	}

	public PolyglotHashKey(long value)
	{
		this();
		setValue(value);
	}


	//-------------------------------------------------------------------------------
	//	implements HashKey
	//-------------------------------------------------------------------------------

	public void set(int square, int piece)
	{
		if (isReversed) {
			square = EngUtil.mirrorSquare(square);
			piece = EngUtil.oppositeColor(piece);
		}
		theValue ^= hash_piece_key(piece,square);
	}

	public void set(int flags)
	{
		if (!ignoreFlags) {
				if (isReversed)
					flags = EngUtil.reverseFlags(flags);
				// castle flags
				theValue ^= hash_castle_key(flags);

				// en-passant square
				if ((flags & Constants.EN_PASSANT_FILE) != 0)
					theValue ^= hash_ep_key(flags & Constants.EN_PASSANT_FILE);

				// turn
				theValue ^= hash_turn_key(flags & NEXT_MOVE);
		}
	}

	public void clear(int square, int piece)
	{
		//	XOR is reversible, of course
		set (square,piece);
	}

	public void clear(int flags)
	{
		//	XOR is reversible, of course
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



// constants

	private static final int RandomPiece     =   0; // 12 * 64
	private static final int RandomCastle    = 768; // 4
	private static final int RandomEnPassant = 772; // 8
	private static final int RandomTurn      = 780; // 1


	private static long hash_key(Board board)
	{
		long key;
		int colour;
//		const sq_t * ptr;
		int sq, piece;

//		ASSERT(board!=NULL);

		// init

		key = 0;

		// pieces

		for (piece = Constants.PAWN; piece <= Constants.KING; piece++)
		{
			List white_pieces = board.pieceList(piece+Constants.WHITE);
			for (Iterator i = white_pieces.iterator(); i.hasNext(); ) {
				Piece p = (Piece) i.next();
				if (p!=null && !p.isVacant())
					key ^= hash_piece_key(p.piece(),p.square());
			}
			List black_pieces = board.pieceList(piece+Constants.BLACK);
			for (Iterator i = white_pieces.iterator(); i.hasNext(); ) {
				Piece p = (Piece) i.next();
				if (p!=null && !p.isVacant())
					key ^= hash_piece_key(p.piece(),p.square());
			}
		}

		// castle flags

		key ^= hash_castle_key(board.flags());

		// en-passant square

		if (board.canEnPassant())
			key ^= hash_ep_key(board.enPassantFile());

		// turn

		key ^= hash_turn_key(board.movesNext());

		return key;

	}

	private static int SQUARE_TO_64 (int square)
	{
		int file0 = EngUtil.fileOf(square)-Constants.FILE_A;
		int row0 = EngUtil.rowOf(square)-Constants.ROW_1;
		return row0*8 + file0;
	}

	private static int PIECE_TO_12 (int piece)
	{
		if (EngUtil.isWhite(piece))
			return 2 * (EngUtil.uncolored(piece)-Constants.PAWN);
		else
			return 1 + 2 * (EngUtil.uncolored(piece)-Constants.PAWN);
	}

	private static long hash_piece_key(int piece, int square) {

//	   ASSERT(piece_is_ok(piece));
//	   ASSERT(SQUARE_IS_OK(square));

	   return PolyglotRandom.Random64[RandomPiece+(PIECE_TO_12(piece)^1)*64+SQUARE_TO_64(square)]; // HACK: ^1 for PolyGlot book
	}


	private static long hash_castle_key(int flags) {

	   long key;
	   int i;

//	   ASSERT((flags&~0xF)==0);

	   key = 0;

		if (EngUtil.canCastle(flags,Constants.WHITE_KINGS_CASTLING))
			key ^= PolyglotRandom.Random64[RandomCastle+0];
		if (EngUtil.canCastle(flags,Constants.WHITE_QUEENS_CASTLING))
			key ^= PolyglotRandom.Random64[RandomCastle+1];
		if (EngUtil.canCastle(flags,Constants.BLACK_KINGS_CASTLING))
			key ^= PolyglotRandom.Random64[RandomCastle+2];
		if (EngUtil.canCastle(flags,Constants.BLACK_QUEENS_CASTLING))
			key ^= PolyglotRandom.Random64[RandomCastle+3];

	   return key;
	}

// hash_ep_key()

	private static long hash_ep_key(int file) {

//	   ASSERT(SQUARE_IS_OK(square));
		if (file < Constants.FILE_A || file > Constants.FILE_H)
			return 0L;
		else
	        return PolyglotRandom.Random64[RandomEnPassant+file-Constants.FILE_A];
	}

// hash_turn_key()

	private static long hash_turn_key(int colour) {

//	   ASSERT(COLOUR_IS_OK(colour));

	   return EngUtil.isWhite(colour) ? PolyglotRandom.Random64[RandomTurn] : 0;
	}

}
