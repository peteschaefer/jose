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

package de.jose.view;

import de.jose.chess.Move;
import de.jose.chess.Position;

/**
 *
 * @author Peter Schäfer
 */
public interface IBoardAdapter
{

	public Position getPosition();

	/**	get a piece from the internal board 	 */
	public int pieceAt(int square);

	/**	get a piece from the internal board 	 */
	public int pieceAt(int file, int row);

	/**	get the party that moves next	*/
	public int movesNext();

	/**	return true if this square is accessible	*/
	public boolean canMove(int square);

	/**	@return true if the given move is legal	 */
	public boolean isLegal(Move mv);

	/**	make a user move	 */
	public void userMove(Move mv);

}
