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

import de.jose.book.BookEntry;

/**
 * @author Peter Schäfer
 */

public class Move extends BookEntry
{
	public char piece,victim,promotionPiece;
	public boolean isPromotion;
	public int from,to;

	public Move(Move that)
	{
		//  ?
	}

	public Move(int from, int to, char piece, char victim, boolean isPromotion, char promotionPiece)
	{
		this.from = from;
		this.to = to;
		this.piece = piece;
		this.victim = victim;
		this.isPromotion = isPromotion;
		this.promotionPiece = promotionPiece;
	}
}
