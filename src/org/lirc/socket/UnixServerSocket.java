/**************************************************************************/
/*
/* UnixServerSocket.java -- Part of the org.lirc.socket package
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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A Unix domain server socket. The interface is similar to the ServerSocket class
 * from the standard library.
 *
 * @version $Revision: 1.1 $
 * @author Bjorn Bringert (bjorn@mumblebee.com)
 */
public class UnixServerSocket
		extends ServerSocket
{

	/**
	 * The default client backlog.
	 */
	private static final int DEFAULT_BACKLOG = 50;

	/**
	 * The socket implementation.
	 */
	private UnixSocketImpl impl;


	/**
	 * Creates a new UnixServerSocket.
	 * @param path The path name of the socket to connect to
	 * @throws IOException If there is a problem creating the socket
	 */
	public UnixServerSocket(String path) throws IOException {
		this(path, DEFAULT_BACKLOG);
	}

	/**
	 * Creates a new UnixServerSocket.
	 * @param path The path name of the socket to connect to
	 * @param backlog The maximum length of the queue
	 * @throws IOException If there is a problem creating the socket
	 */
	public UnixServerSocket(String path, int backlog) throws IOException {
		impl = new UnixSocketImpl();
		impl.bind(path);
		impl.listen(backlog);
	}

	/**
	* Closes this socket
	* @throws IOException If there is a problem closing the socket
	*/
	public void close() throws IOException {
		impl.close();
	}

	/**
	* Listens for a connection to be made to this socket and accepts
	* it. The method blocks until a connection is made.
	* @throws IOException if an I/O error occurs when waiting for a
	*               connection.
	* @return the new UnixSocket
	*/
	public Socket accept() throws IOException {
		return new UnixSocket(impl.accept());
	}

}