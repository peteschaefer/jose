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

package de.jose.util.file;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**	
 * a filter writer that keeps tracks of the number of written chars
 */

public class CountWriter
		extends FilterWriter
{
	int count;
	
	public CountWriter(Writer out) {
		super(out);
		count = 0;
	}
	
	public int getPosition()	{ return count; }
	
	public void write(int c)
           throws IOException
	{
		out.write(c);
		count++;
	}
	
	public void write(char[] cbuf, int off, int len)
           throws IOException
	{
		out.write(cbuf,off,len);
		count += len;
	}
	
	public void write(String str,
                  int off,
                  int len)
           throws IOException
	{
		out.write(str,off,len);
		count += len;
	}
	
}
