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

import java.io.*;

/**
 * @author Peter Schäfer
 */

public class Streams
{
	private static ThreadLocal byteArray = new ThreadLocal()
	{
		protected Object initialValue()
		{
			return new byte[4096];
		}
	};
	private static ThreadLocal charArray = new ThreadLocal()
	{
		protected Object initialValue()
		{
			return new char[4096];
		}
	};


	public static long copyStream(InputStream in, OutputStream out, long length)
		throws IOException
	{
		long total = 0;
		byte[] buf = (byte[])byteArray.get();

		while (total < length)
		{
			int chunk = (int)Math.min(buf.length,length-total);
			chunk = in.read(buf,0,chunk);
			if (chunk < 0) break; //  eof
			out.write(buf,0,chunk);
			total += chunk;
		}
		return total;
	}

	public static long copyReader(Reader in, Writer out, long length)
        throws IOException
	{
		long total = 0;
		char[] buf = (char[])charArray.get();

		while (total < length)
		{
			int chunk = (int)Math.min(buf.length,length-total);
			chunk = in.read(buf,0,chunk);
			if (chunk < 0) break; //  eof
			out.write(buf,0,chunk);
			total += chunk;
		}
		return total;
	}

	public static long copyReader(Reader in, OutputStream out, long length)
		throws IOException
	{
		OutputStreamWriter wout = new OutputStreamWriter(out);
		long result = copyReader(in,wout,length);
		wout.flush();
		return result;
	}

	public static String toString(Reader in)
		throws IOException
	{
		StringWriter swriter = new StringWriter();
		copyReader(in,swriter,Long.MAX_VALUE);
		return swriter.toString();
	}
}
