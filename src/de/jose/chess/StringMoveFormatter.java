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



public class StringMoveFormatter
           extends MoveFormatter
{
    protected StringBuffer buf = new StringBuffer();

	private static StringMoveFormatter defaultInstance = null;

	/** default formatter used for the application  */
	public static StringMoveFormatter getDefaultFormatter()
	{
		if (defaultInstance==null) {
			defaultInstance = new StringMoveFormatter();
			defaultInstance.setFormat(MoveFormatter.SHORT);
		}
		return defaultInstance;
	}

	public static void setDefaultLanguage(String language)
	{
		getDefaultFormatter().setLanguage(language);
	}

	public static String formatMove(Position pos, Move mv, boolean withNumber)
	{
		if (mv==null) return null;

		int oldOptions = pos.getOptions();
		try {

			pos.setOption(Position.CHECK+Position.STALEMATE,true);
			pos.setOption(Position.EXPOSED_CHECK,true);

			if (pos.tryMove(mv)) {
				pos.undoMove();

				StringMoveFormatter mvFormatter = StringMoveFormatter.getDefaultFormatter();
				if (withNumber)
				{
					mvFormatter.text(String.valueOf(pos.gamePly()/2+1));
					if (pos.whiteMovesNext())
						mvFormatter.text(".");
					else
						mvFormatter.text("...");
				}

				mvFormatter.format(mv,pos);
				return mvFormatter.flush();
			}
			//  else: unparseable
			return null;

		} finally {
			pos.setOptions(oldOptions);
		}
	}


    public StringMoveFormatter()
    {
        super();
        setPieceChars(null);
        buf = new StringBuffer();
    }

    public String flush() {
        if (buf.length()==0)
            return null;
        else {
            String result = buf.toString();
            buf.setLength(0);
            return result;
        }
    }

    public final String toString(Move mv)                   { return toString(format,mv,null); }

    public final String toString(Move mv, Position pos)     { return toString(format,mv,pos); }

    public final String toString(int format, Move mv)       { return toString(format,mv,null); }

    public String toString(int format, Move mv, Position pos)
    {
        format(format, mv, pos);
        return flush();
    }


    public void text(String str, int castling) {
        buf.append(str);
    }

    public void text(char chr) {
        buf.append(chr);
    }

    public void figurine(int piece, boolean promotion)    {
        buf.append(pieceChars[EngUtil.uncolored(piece)]);
    }

	public static void replaceDefaultPieceChars(char[] c, int offset, int len)
	{
		String[] pieceChars = getDefaultFormatter().pieceChars;
		for ( ; len-- > 0; offset++) {
			int i = DEFAULT_PIECE_CHARACTERS.indexOf(c[offset]);
			if (i >= 0) c[offset] = pieceChars[i+PAWN].charAt(0);
		}
	}

	public static String replaceDefaultPieceChars(String text)
	{
		char[] c = text.toCharArray();
		replaceDefaultPieceChars(c,0,c.length);
		return new String(c);
	}

}
