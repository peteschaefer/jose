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

package de.jose.db;

import java.io.*;

public class Script
{
	protected BufferedReader input;
	
	
	public Script (Reader in)
	{
		input = new BufferedReader(in);
	}

	public Script (File file)
		throws FileNotFoundException
	{
		this (new FileReader(file));
	}

	public Script (String text)
	{
		this (new StringReader(text));
	}
	
	
	public String next()
		throws IOException
	{
		StringBuffer buf = new StringBuffer();
		
		for (;;) {
			String line = null;
			for (;;) {
				line = input.readLine();
				if (line == null) break;
				
				line = line.trim();
				if (line.length()==0 || line.startsWith(";")) continue;
				
				if (buf.length() > 0) buf.append(" ");
				if (line.endsWith(";")) {
					buf.append(line.substring(0,line.length()-1));
					break;
				}
				else
					buf.append(line);
			}
			
			if (buf.length() > 0)
				return buf.toString();
			if (line==null)
				return null;
		}
	}
}
