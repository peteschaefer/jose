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

import java.io.PrintWriter;

public class PrintMoveFormatter
           extends MoveFormatter
{
	protected PrintWriter out;

    public PrintMoveFormatter(PrintWriter pwriter)
    {
        super();
        setPieceChars(null);
        out = pwriter;
    }

	public void flush() {
        out.flush();
    }

    public void text(String str, int castling) {
        out.print(str);
    }

    public void text(char chr) {
        out.print(chr);
    }

    public void figurine(int piece, boolean promotion) {
        out.print(pieceChars[EngUtil.uncolored(piece)]);
    }
}
