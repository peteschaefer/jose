/**************************************************************************/
/*
/* UnixSocket.java -- Part of the org.lirc.socket package
/* Copyright (C) 2001 Bjorn Bringert (bjorn@mumblebee.com)
/*
/* This program is free software; you can redistribute it and/or
/* modify it under the terms of the GNU General Public License
/* as published by the Free Software Foundation; either version 2
/* of the License, or (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful,
/* but WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU General Public License for more details.
/*
/* You should have received a copy of the GNU General Public License
/* along with this program; if not, write to the Free Software
/* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
/*
/**************************************************************************/

package org.lirc.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A Unix domain socket. The interface is similar to the Socket class
 * from the standard library.
 *
 * @version $Revision: 1.1 $
 * @author Bjorn Bringert (bjorn@mumblebee.com)
 */
public class UnixSocket 
		extends Socket
{

	/**
	 * The socket implementation.
	 */
	private UnixSocketImpl impl;

	/**
	 * The input stream for this socket.
	 */
	private UnixSocketInputStream inputStream = null;

	/**
	 * The output stream for this socket.
	 */
	private UnixSocketOutputStream outputStream = null;

	/**
	 * Creates a new UnixSocket connected to a path.
	 * @param path The path name of the socket to connect to
	 */
	public UnixSocket(String path) throws IOException {
		impl = new UnixSocketImpl();
		impl.connect(path);
	}

	/**
	 * Creates a new UnixSocket from a UnixSocketImpl.
	 */
	protected UnixSocket(UnixSocketImpl impl) throws IOException {
		this.impl = impl;
	}

	/**
	* Closes this socket
	* @throws IOException If there is a problem closing the socket
	*/
	public void close() throws IOException {
		impl.close();
	}

	/**
	* Returns an input stream for this socket. Multiple calls
	* to this method on a socket will return the same stream.
	* @return an input stream for reading bytes from this socket.
	* @throws IOException if an I/O error occurs when creating the
	* input stream.
	*/
	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			inputStream = impl.getInputStream();
		}
		return inputStream;
	}

	/**
	* Returns an output stream for this socket. Multiple calls
	* to this method on a socket will return the same stream.
	* @return an output stream for writing bytes to this socket.
	* @throws IOException if an I/O error occurs when creating the
	* output stream.
	*/
	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			outputStream = impl.getOutputStream();
		}
		return outputStream;
	}

	/**
	* Disables the input stream for this socket.
	*
	* @throws IOException if an I/O error occurs when shutting down this
	* socket.
	*/
	public void shutdownInput() throws IOException {
		impl.shutdownInput();
	}

	/**
	* Disables the output stream for this socket.
	*
	* @throws IOException if an I/O error occurs when shutting down this
	* socket.
	*/
	public void shutdownOutput() throws IOException {
		impl.shutdownOutput();
	}


}