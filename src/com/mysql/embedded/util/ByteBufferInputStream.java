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

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;

/**
 * an input stream reading bytes from a ByteBuffer
 * (which may be a direct ByteBuffer)
 *
 * @author Peter Schäfer
 */

public class ByteBufferInputStream extends InputStream

{
	/** backing buffer  */
	protected ByteBuffer buffer;

	public ByteBufferInputStream(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	public int read() throws IOException
	{
		try {
			return buffer.get();
		} catch (BufferUnderflowException ex) {
			return -1;
		}
	}

	public int available() throws IOException
	{
		return buffer.limit()-buffer.position();
	}

	public synchronized void reset() throws IOException
	{
		buffer.reset();
	}

	public boolean markSupported()
	{
		return true;
	}

	public void mark(int readlimit)
	{
		buffer.mark();
	}

	public long skip(long n) throws IOException
	{
		int old_pos = buffer.position();
		int new_pos = old_pos + (int)n;
		if (new_pos >= buffer.limit()) new_pos = buffer.limit()-1;
		buffer.position(new_pos);
		return new_pos-old_pos;
	}

	public int read(byte b[]) throws IOException
	{
		return read(b,0,b.length);
	}

	public int read(byte b[], int off, int len) throws IOException
	{
		len = Math.min(len,available());
		buffer.get(b,off,len);
		return len;
	}
}
