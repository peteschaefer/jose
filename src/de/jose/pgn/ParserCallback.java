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

package de.jose.pgn;

import de.jose.chess.Move;
import de.jose.chess.Position;

public interface ParserCallback
{
	/**	called after parsing a legal move	 */
	public void callbackLegalMove(Position pos, Move mv);
	
	/**	called after parsing an annotation	 */
	public void callbackAnnotation(char[] line, int offset, int len);
	
	/**	called after parsing a NAG annotation	 */
	public void callbackAnnotation(int nagNo);

	/**	called after parsing a result	 */
	public void callbackResult(int result);
	
	/**	called after parsin a comment	 */
	public void callbackComment(char[] line, int offset, int len);
	
	/**	called when a new variation starts	 */
	public void callbackStartVariation(Position pos, int level);

	/**	called when a variation ends	 */
	public void callbackEndVariation(Position pos, int level);

	/**	called when an illegible move is encountered	 */
	public void callbackIllegibleMove(char[] line, int offset, int len);

	/**	called when an illegal move is encountered	 */
	public void callbackIllegalMove(Position pos, Move mv, char[] line, int offset, int len);

	/**	called when an ambuguous move is encountered	 */
	public void callbackAmbiguousMove(Position pos, Move[] mvs, int count, char[] line, int offset, int len);

	/**	called when an unrecognized token is encountered	 */
	public void callbackUnrecognized(char[] line, int offset, int len);

	/**	called when no input was found	 */
	public void callbackEmptyInput();
}
