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

import de.jose.book.BookEntry;
import de.jose.chess.Move;
import de.jose.Util;

import java.io.IOException;

/**
 *******************************************************************************
 *                                                                             *
 *   Book() is used to determine if the current position is in the book data-  *
 *   base.  it simply takes the set of moves produced by root_moves() and then *
 *   tries each position's hash key to see if it can be found in the data-     *
 *   base.  if so, such a move represents a "book move."  the set of flags is  *
 *   used to decide on a sub-set of moves to be used as the "book move pool"   *
 *   from which a move is chosen randomly.                                     *
 *                                                                             *
 *   the format of a book position is as follows:                              *
 *                                                                             *
 *   64 bits:  hash key for this position.                                     *
 *                                                                             *
 *    8 bits:  flag bits defined as  follows:                                  *
 *                                                                             *
 *      0000 0001  ?? flagged move                (0001) (0x01)                *
 *      0000 0010   ? flagged move                (0002) (0x02)                *
 *      0000 0100   = flagged move                (0004) (0x04)                *
 *      0000 1000   ! flagged move                (0010) (0x08)                *
 *      0001 0000  !! flagged move                (0020) (0x10)                *
 *      0010 0000     black won at least 1 game   (0040) (0x20)                *
 *      0100 0000     at least one game was drawn (0100) (0x40)                *
 *      1000 0000     white won at least 1 game   (0200) (0x80)                *
 *                                                                             *
 *   24 bits:  number of games this move was played.                           *
 *                                                                             *
 *   32 bits:  learned value (floating point).                                 *
 *                                                                             *
 *   32 bits:  CAP score for this move (integer).                              *
 *                                                                             *
 *     (note:  counts are normalized to a max of 255.                          *
 *                                                                             *
 *******************************************************************************
 */
public class CraftyBookEntry
		extends BookEntry
		implements Cloneable
{

	public long key;
	public byte flags;
	public int capScore;   //  what's this ?

	public CraftyBookEntry()                        { }
	public CraftyBookEntry(long key)          { this.key = key; }

	public void read(CraftyBook book) throws IOException
	{
		key = book.readLong();
		int i = book.readInt();
		flags = (byte)( (i >> 24) & 0x00FF);

		count = i & 0x03FFFF;
		countBlack = Util.anyOf(flags, 0x20) ? 1 : 0;
		countDraw = Util.anyOf(flags, 0x40) ? 1 : 0;
		countWhite = Util.anyOf(flags, 0x80) ? 1 : 0;

		userValue = FUNKNOWN;
		if (Util.anyOf(flags,0x01))
			userValue = VERY_BAD_MOVE;
		if (Util.anyOf(flags,0x02))
			userValue = BAD_MOVE;
		if (Util.anyOf(flags,0x04))
			userValue = NEUTRAL_MOVE;
		if (Util.anyOf(flags,0x08))
			userValue = GOOD_MOVE;
		if (Util.anyOf(flags,0x10))
			userValue = VERY_GOOD_MOVE;

		learnValue = book.readFloat();
		capScore = book.readInt();
		//  book.readInt(); //  HACK  for older crafty versions !
		//  used to be sizeof(BOOK_POSITION), which may vary on platforms
		//  more recent versions use BOOK_POSITION_SIZE = 20
	}

	protected Object clone()
	{
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	//  the book entry can also serve as HashMap key
	public int hashCode() {
		return (int)key;
	}

	public boolean equals(Object obj) {
		CraftyBookEntry that = (CraftyBookEntry)obj;
		return this.key==that.key;
	}
}