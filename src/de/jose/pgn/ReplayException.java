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

/**
 *
 * @author Peter Schäfer
 */

public class ReplayException
		extends RuntimeException
{
	public ReplayException(Position pos, Move move)
	{
		super(createMessage(pos,move));
	}

	private static String createMessage(Position pos, Move move)
	{
		StringBuffer msg = new StringBuffer();
		msg.append("illegal move in replay: ");
		msg.append(pos.gameMove());
		if (pos.whiteMovesNext())
			msg.append(".");
		else
			msg.append("...");
		msg.append(move);
		msg.append("\n");

		if (pos!=null) {
			Move last = pos.getLastMove();
			if (last!=null) {
				msg.append("last move: ");
				msg.append(last);
				msg.append("\n");
			}

			Move expected = pos.getExpectedMove();
			if (expected != null) {
				msg.append("expected move(?): ");
				msg.append(expected);
			}
		}
		return msg.toString();
	}
}
