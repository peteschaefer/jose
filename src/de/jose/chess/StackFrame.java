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
 * this class is only used internally by Position
 */

public class StackFrame
		extends Move
{
	/**	promoted piece	 */
	public Piece promoted;
	/**	position flags	*/
	public int positionFlags;
	/**	number of silent plies (before making the move)	 */
	public int silentPlies;
	/**	hash key	*/
	public long hashValue;
	/**	reversed hash key (optional, only in opening, maybe in endgame phase)	 */
	public long reversedHashValue;
	/** white material signature    */
	public long whiteSignature;
	/** black material siganture    */
	public long blackSignature;
}
