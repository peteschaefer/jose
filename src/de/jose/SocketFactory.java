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

package de.jose;

import org.lirc.socket.UnixServerSocket;
import org.lirc.socket.UnixSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @deprecated
 * @author Peter Schäfer
 */

public class SocketFactory
{
	public static Socket getSocket(String host, int port, String pipe)
		throws IOException
	{
		if (useNamedPipe())		//	windows named pipes
			return new WinSocket("\\\\.\\pipe\\"+pipe);
		else if (useUnixPipe())	//	unix pipes
			return new UnixSocket("/tmp/"+pipe);
		else	//	TCP/IP
			return new Socket(InetAddress.getByName(host),port);
	}

	public static ServerSocket getServerSocket(int port, String pipe)
		throws IOException
	{
		if (useNamedPipe())		//	windows named pipes
			return new WinServerSocket("\\\\.\\pipe\\"+pipe);
		else if (useUnixPipe())	//	unix pipes
			return new UnixServerSocket("/tmp/"+pipe);
		else	//	TCP/IP
			return new ServerSocket(port);
	}

	public static boolean useNamedPipe()
	{
		if (Version.getSystemProperty("no.pipe",false)) return false;

		return Version.winNTfamily;
	}

	public static boolean useUnixPipe()
	{
		if (Version.getSystemProperty("no.pipe",false)) return false;

		return Version.unix;
	}

	public static boolean useTCP()
	{
		if (Version.getSystemProperty("no.pipe",false)) return true;

		if (Version.winNTfamily) return false;	//	use named pipes
		if (Version.unix) return false;			//	use Unix pipes

		return true;
	}



	static class WinSocket extends Socket
    {
		public boolean isClosed()           { return isClosed; }

        public InputStream getInputStream()
                    throws IOException
		{
		   return new RandomAccessFileInputStream(namedPipeFile);
		}

		public OutputStream getOutputStream()
			throws IOException
		{
			return new RandomAccessFileOutputStream(namedPipeFile);
		}

		public synchronized void close()
			throws IOException
		{
			namedPipeFile.close();
			isClosed = true;
        }

		private RandomAccessFile namedPipeFile;
		private boolean isClosed;

		WinSocket(String filePath)
			throws IOException
		{
			isClosed = false;
            namedPipeFile = new RandomAccessFile(filePath, "rws");
		}

		WinSocket(RandomAccessFile file)
			throws IOException
		{
			isClosed = false;
            namedPipeFile = file;
		}
    }

	static class WinServerSocket extends ServerSocket
	{
		public boolean isClosed()           { return isClosed; }

		public synchronized void close()
			throws IOException
		{
			namedPipeFile.close();
			isClosed = true;
        }

		/**
		* Listens for a connection to be made to this socket and accepts
		* it. The method blocks until a connection is made.
		* @throws IOException if an I/O error occurs when waiting for a
		*               connection.
		* @return the new UnixSocket
		*/
		public Socket accept() throws IOException {
			return new WinSocket(namedPipeFile);
		}

		private RandomAccessFile namedPipeFile;
		private boolean isClosed;

		WinServerSocket(String filePath)
			throws IOException
		{
			isClosed = false;
            namedPipeFile = new RandomAccessFile(filePath, "rws");
			/**
			 * this does not work ;-(
			 * creating a server pipe is more complicated
			 */
		}
	}

    static class RandomAccessFileOutputStream extends OutputStream
    {

		public void close() throws IOException
		{
			raFile.close();
		}

		public void write(byte b[], int off, int len) throws IOException
		{
			raFile.write(b, off, len);
		}

		public void write(byte b[]) throws IOException
		{
			raFile.write(b);
		}

		public void write(int i) throws IOException		{ raFile.write(i); }

		RandomAccessFile raFile;

		RandomAccessFileOutputStream(RandomAccessFile file)
		{
			raFile = file;
		}
    }

    static class RandomAccessFileInputStream extends InputStream
    {

		public int available() throws IOException
		{
			return -1;
		}

		public void close() throws IOException
		{
			raFile.close();
		}

		public int read() throws IOException
		{
			return raFile.read();
		}

		public int read(byte b[], int off, int len) throws IOException
		{
			return raFile.read(b, off, len);
		}

		public int read(byte b[]) throws IOException
		{
			return raFile.read(b);
		}

		RandomAccessFile raFile;

		RandomAccessFileInputStream(RandomAccessFile file)
		{
			raFile = file;
		}
    }
}
