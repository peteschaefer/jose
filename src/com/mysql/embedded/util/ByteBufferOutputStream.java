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

import java.nio.ByteBuffer;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Peter Schäfer
 */

public class ByteBufferOutputStream
        extends OutputStream
{
	protected ByteBuffer buffer;

	public ByteBufferOutputStream(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	public void write(int b) throws IOException
	{
		buffer.put((byte)b);
	}

	public void write(byte b[]) throws IOException
	{
		write(b,0,b.length);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		buffer.put(b,off,len);
	}
}
