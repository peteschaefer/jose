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

import java.sql.SQLException;
import java.sql.Clob;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author Peter Sch�fer
 */

public class MyClob
        implements java.sql.Clob
{
	protected ByteBuffer buffer;



	protected MyClob(ByteBuffer buffer)
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
	public Reader getCharacterStream(long pos, long length) throws SQLException {
		return null;
	}

	public InputStream getAsciiStream() throws SQLException
	{
		return new ByteBufferInputStream(buffer);
	}

	public OutputStream setAsciiStream(long pos) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Reader getCharacterStream() throws SQLException
	{
		try {
			return new InputStreamReader(getAsciiStream(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SQLException("unsupported encoding: "+e.getLocalizedMessage());
		}
	}

	public Writer setCharacterStream(long pos) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getSubString(long pos, int length) throws SQLException
	{
		try {
			byte[] bytes = new byte[length];
			buffer.position((int)pos);
			buffer.get(bytes);

			return new String(bytes,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SQLException("unsupported encoding: "+e.getLocalizedMessage());
		}
	}

	public int setString(long pos, String str) throws SQLException
	{
		return setString(pos,str,0,str.length());
	}

	public int setString(long pos, String str, int offset, int len) throws SQLException
	{
		byte[] bytes = str.substring(offset,offset+len).getBytes();
		buffer.position((int)pos);
		buffer.put(bytes);
		return len;
	}

	public long position(String searchstr, long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public long position(Clob searchstr, long start) throws SQLException
	{
		throw new UnsupportedOperationException();
	}
}
