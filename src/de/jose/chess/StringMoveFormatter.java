/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.chess;


import de.jose.Language;

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

	private static boolean equals(String query, CharSequence str, int i)
	{
		if (i+query.length() > str.length()) return false;
		for(int j=0; j < query.length(); j++)
			if (str.charAt(i+j) != query.charAt(j)) return false;
		return true;
	}

	protected static int pieceFromChars(String[] pcs, CharSequence str, int start)
	{
		//  attention: multi-char encodings, and prefixes.
		//  e.g. in Russian, there is "K" and "Kp". Find the **longest** match.
		int pc=-1;
		int match=0;
		for(int i=0; i < pcs.length; i++)
			if (pcs[i]!=null && equals(pcs[i],str,start) && (pcs[i].length() > match)) {
				pc = (i - 1 + PAWN);
				match = pcs[i].length();
			}
		return pc;
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

	public String reformat(String text, String lang)
	{
		String[] newPieceCharArray = parsePieceChars(Language.getPieceChars(lang));
		StringBuffer buf = new StringBuffer(text);
		reformat(buf,this.getPieceCharArray(),newPieceCharArray);
		return buf.toString();
	}

	private void reformat(StringBuffer buf, String[] from, String[] to)
	{
		boolean comment=false;
		int i=0;
		while(i < buf.length())
		{
			char c = buf.charAt(i);
			if (c=='{') comment=true;
			if (!comment && Character.isUpperCase(c)) {
				int pc = pieceFromChars(from,buf,i);
				if (pc>0) {
					buf.replace(i,i+from[pc].length(), to[pc]);
					i += to[pc].length();
					continue;
				}
			}
			if (c=='}') comment=false;
			i++;
		}
	}

	public static String replaceDefaultPieceChars(String text)
	{
		char[] c = text.toCharArray();
		replaceDefaultPieceChars(c,0,c.length);
		return new String(c);
	}

}
