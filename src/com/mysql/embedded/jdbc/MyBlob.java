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

package com.mysql.embedded.jdbc;

import com.mysql.embedded.util.ByteBufferInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author Peter Sch�fer
 */

public class MyBlob
        implements java.sql.Blob
{
	protected ByteBuffer buffer;


	protected MyBlob(ByteBuffer buffer)
	{
		this.buffer = buffer;
	}

	public long length() throws SQLException
	{
		return buffer.limit();
	}

	public void truncate(long len) throws SQLException
	{
		buffer.limit((int)len);
	}

	@Override
	public void free() throws SQLException {

	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException {
		return null;
	}

	public byte[] getBytes(long pos, int length) throws SQLException
	{
		byte[] result = new byte[length];
		buffer.position((int)pos);
		buffer.get(result);
		return result;
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException
	{
		return setBytes(pos,bytes,0,bytes.length);
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
	{
		buffer.position((int)pos);
		buffer.put(bytes,offset,len);
		return len;
	}

	public long position(byte pattern[], long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public InputStream getBinaryStream() throws SQLException
	{
		return new ByteBufferInputStream(buffer);
	}

	public OutputStream setBinaryStream(long pos) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public long position(Blob pattern, long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}
}
