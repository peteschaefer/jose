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

package de.jose.devtools;

import de.jose.Language;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Peter Schäfer
 */

public class I18nCompare
{
	public static void main(String[] args)
	{
		try {
			PrintWriter out = new PrintWriter(System.out,true);
			compare(args,out);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static Iterator sortedKeys(Map map)
	{
		List sorted = new ArrayList(map.size());
		sorted.addAll(map.keySet());
		Object[] keys = sorted.toArray();
		Arrays.sort(keys);
		return Arrays.asList(keys).iterator();
	}

	public static void compare(String[] args, PrintWriter out) throws IOException
	{
		File oldDir = null;
		File newDir = null;
		String lang = null;

		Language oldDefault = null;
		Language newDefault = null;
		Language local = null;

		for (int i=0; i<args.length; i++)
		{
			if (args[i].equals("-old"))
				oldDir = new File(args[++i]);
			else if (args[i].equals("-new"))
				newDir = new File(args[++i]);
			else if (args[i].equals("-local"))
				lang = args[++i];
		}

		oldDefault = new Language(oldDir,"lang.properties",null,false);
		newDefault = new Language(newDir,"lang.properties",null,false);
		local = new Language(newDir,"lang.properties",lang,false);

		/** look for new keys   */
		Iterator newKeys = sortedKeys(newDefault);
		while (newKeys.hasNext())
		{
			String key = (String)newKeys.next();
			if (! local.containsKey(key)) {
				boolean isnew = !oldDefault.containsKey(key);
				if (isnew)
					out.print("NEW, MISSING: ");
				else
					out.print("OLD, MISSING: ");
				out.print(key);
				out.print("=");
				out.println(newDefault.get1(key,null));
			}
		}
		/** look for deprecated keys    */
		Iterator oldKeys = sortedKeys(oldDefault);
		while (oldKeys.hasNext())
		{
			String key = (String)oldKeys.next();
			if (! newDefault.containsKey(key) && local.containsKey(key))
			{
				out.println("DELETED: ");
				out.print(key);
				out.print("=");
				out.println(oldDefault.get1(key,null));
			}
		}
		/** look for wrong keys */
		Iterator localKeys = sortedKeys(local);
		while (localKeys.hasNext())
		{
			String key = (String)localKeys.next();
			if (!newDefault.containsKey(key))
			{
				out.print("WRONG: ");
				out.print(key);
				out.print("=");
				out.println(local.get1(key,null));
			}
		}
	}
}
