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

package com.mysql.embedded.jdbc;

import java.util.Set;
import java.util.HashSet;

/**
 *  Replace { } sequences.
 *  What this class can & can't do:
 *
 *  {fn }       is interpreted by the MySQL server, fine.
 *  {oj }       is interpreted by the MySQL server, fine.
 *
 *  {d } {t } {ts }     is handled by this class. the tiime literals are identical,
 *                          so all we have to do is remove the braces
 *
 *  {escape }       braces are stripped
 *
 *  {fn convert()}      is interpreted by the server (since 4.0.2)
 *
 *  {call }
 *  {?=call }               not yet supported. use MySQL call syntax instead.
 *
 * @author Peter Schäfer
 */
public class EscapeProcessor
{
	/**
	 * strip the following esacpe braces
	 */
	private static final Set REMOVE_SET = new HashSet();
	static {
		REMOVE_SET.add("d");
		REMOVE_SET.add("t");
		REMOVE_SET.add("ts");
		REMOVE_SET.add("escape");
	}

	public static String escapeSQLString(String input)
	{
		StringBuffer buf = null;
		int i1;
		for (i1 = input.indexOf('{'); i1 >= 0; i1 = input.indexOf('{',i1+1))
		{
			int i2 = i1+1;
			while (Character.isLetter(input.charAt(i2))) i2++;

			String seq = input.substring(i1+1,i2);
			if (REMOVE_SET.contains(seq))
			{
				int i3 = input.indexOf('}', i2+1);
				if (buf==null) buf = new StringBuffer(input);

				//  don't move, just overwrite
				for (int j=i1; j<i2; j++)
					buf.setCharAt(j,' ');
				if (i3 >= 0)
					buf.setCharAt(i3,' ');
				i1 = i3;
			}
		}

		if (buf==null)
			return input;
		else
			return buf.toString();
	}
}
