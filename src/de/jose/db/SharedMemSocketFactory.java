/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002,2003 Peter Schäfer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.db;

import com.mysql.jdbc.SocketFactory;

import java.net.Socket;
import java.net.SocketException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.nio.ByteBuffer;

import de.jose.util.WinUtils;

/**
 * @author Peter Schäfer
 */
//  TODO not implemented

public class SharedMemSocketFactory implements SocketFactory
{
	private static final String SHARED_MEMORY_BASE_NAME = "sharedMemoryBaseName";
	private SharedMemorySocket socket;

	public Socket afterHandshake() throws SocketException, IOException
	{
		return socket;
	}

	public Socket beforeHandshake() throws SocketException, IOException
	{
		return socket;
	}

	public Socket connect(String string, Properties properties) throws SocketException, IOException
	{
		return connect(string,4113,properties);
	}

	public Socket connect(String host, int port, Properties props) throws SocketException, IOException
	{
		String sharedMemoryBaseName = props.getProperty(SHARED_MEMORY_BASE_NAME);

		if (sharedMemoryBaseName == null) {
		    sharedMemoryBaseName = "MYSQL";
		} else if (sharedMemoryBaseName.length() == 0) {
		    throw new SocketException(
		        "Can not specify NULL or empty value for property '"
		        + SHARED_MEMORY_BASE_NAME + "'.");
		}

		socket = new SharedMemorySocket(sharedMemoryBaseName);

		return socket;
	}


	class SharedMemorySocket extends Socket
	{
		String mapName;
		int mapHandle;
		int writeEvent,readEvent;
		ByteBuffer buffer;

		SharedMemorySocket(String mapName)
		{
			//  TODO
			this.mapName = mapName;
			//  allocate shared memory
			mapHandle = WinUtils.openFileMapping(mapName);
			//  writeEvent = ?
			//  readEvent = ?
			buffer = WinUtils.getFileMappingBuffer(mapHandle);
		}

		public InputStream getInputStream() throws IOException
		{
			return new SharedMemoryInputStream();
		}

		public OutputStream getOutputStream() throws IOException
		{
			return new SharedMemoryOutputStream();
		}

		public synchronized void close() throws IOException
		{
			//  TODO
		}

		public boolean isClosed()
		{
			//  TODO
			return false;
		}
	}

	class SharedMemoryInputStream extends InputStream
	{
		public int read() throws IOException
		{
			//  TODO
			return 0;
		}

		public int available() throws IOException
		{
			//  TODO
			return 0;
		}

		public int read(byte b[]) throws IOException
		{
			//  TODO
			return 0;
		}

		public int read(byte b[], int off, int len) throws IOException
		{
			//  TODO
			return 0;
		}

		public long skip(long n) throws IOException
		{
			//  TODO
			return 0L;
		}

		public void close() throws IOException
		{
			//  TODO
		}
	}

	static class SharedMemoryOutputStream extends OutputStream
	{
		public void write(int b) throws IOException
		{
			//  TODO
		}

		public void flush() throws IOException
		{
			//  TODO
		}

		public void write(byte b[]) throws IOException
		{
			//  TODO
		}

		public void write(byte b[], int off, int len) throws IOException
		{
			//  TODO
		}

		public void close() throws IOException
		{
			//  TODO
		}
	}
}
