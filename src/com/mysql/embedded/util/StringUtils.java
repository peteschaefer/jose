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

package com.mysql.embedded.util;

public class StringUtils
{

	/**
	 * @param text
	 * @param pattern
	 * @return if 'text' starts with 'pattern'. Case insensitive.
	 */
	public static boolean startsWithIgnoreCase(String text, String pattern)
	{
		return text.regionMatches(true, 0, pattern,0, pattern.length());
	}

	public static boolean startsWithIgnoreCaseAndWs(String text, String pattern)
	{
		int toffset = 0;
		int tlen = text.length();
		if (toffset < tlen && Character.isWhitespace(text.charAt(toffset)))
			toffset++;
		return text.regionMatches(true, toffset, pattern, 0, pattern.length());
	}

	public static int indexOfIgnoreCase(String text, String pattern)
	{
		int plen = pattern.length();
		int tlen = text.length();

outer:
		for (int i = plen-1; i < tlen; i++)
		{
			for (int j=0; j < plen-1; j++)
			{
				char ct = text.charAt(i-j);
				char cp = pattern.charAt(plen-1-j);
				if (ct==cp) continue;
				if (Character.toLowerCase(ct)==Character.toLowerCase(cp)) continue;
				continue outer; //  mismatch
			}
			//  complete match
			return i-plen+1;
		}

		return -1;
	}
}
