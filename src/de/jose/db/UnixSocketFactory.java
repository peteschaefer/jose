/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package de.jose.db;

import com.mysql.jdbc.SocketFactory;
import org.lirc.socket.UnixSocket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;


/**
 * A socket factory for Unix domain sockets
 * (based on org.lirc.socket.UnixSocket)
 *
 * @author Peter Schäfer
 */
public class UnixSocketFactory implements SocketFactory
 {
    private static final String SOCKET_PATH_PROP_NAME = "socketPath";
    private UnixSocket unixSocket;

    /**
     * Constructor forUnixSocketFactory.
     */
    public UnixSocketFactory() {
        super();
    }

    /**
     * @see com.mysql.jdbc.SocketFactory#afterHandshake()
     */
    public Socket afterHandshake() throws SocketException, IOException {
        return this.unixSocket;
    }

    /**
     * @see com.mysql.jdbc.SocketFactory#beforeHandshake()
     */
    public Socket beforeHandshake() throws SocketException, IOException {
        return this.unixSocket;
    }

	 public Socket connect(String string, Properties properties) throws SocketException, IOException
	 {
		 return connect(string,4113,properties);
	 }

	 /**
	  * @see com.mysql.jdbc.SocketFactory#connect(String, Properties)
	  */
	 public Socket connect(String host, int port, Properties props)
	     throws SocketException, IOException
		{
	     String socketPath = props.getProperty(SOCKET_PATH_PROP_NAME);

	     if (socketPath == null) {
	         socketPath = "/tmp/mysql.sock";
	     } else if (socketPath.length() == 0) {
	         throw new SocketException(
	             "Can not specify NULL or empty value for property '"
	             + SOCKET_PATH_PROP_NAME + "'.");
	     }

	     this.unixSocket = new UnixSocket(socketPath);

	     return this.unixSocket;
	 }
}
