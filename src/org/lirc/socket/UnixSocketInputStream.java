/**************************************************************************/
/*
/* UnixSocketInputStream.java -- Part of the org.lirc.socket package
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

/**
 * Input stream for unix domain sockets
 *
 * @version $Revision: 1.1 $
 * @author Bjorn Bringert (bjorn@mumblebee.com)
 */
class UnixSocketInputStream extends InputStream {

	private UnixSocketImpl impl;

	public UnixSocketInputStream(UnixSocketImpl impl) {
		this.impl = impl;
	}

	public void close() throws IOException {
		impl.shutdownInput();
	}

	public int read() throws IOException {
		return nativeRead(impl.getFd());
	}

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null)
			throw new NullPointerException();
		else if (off < 0 || off > b.length || len < 0
			|| (off + len) > b.length || (off + len) < 0)
			throw new IndexOutOfBoundsException();
		else if (len == 0)
			return 0;
		return nativeRead(impl.getFd(), b, off, len);
	}

	private static native int nativeRead(int fd) throws IOException;

	private static native int nativeRead(int fd, byte[] b, int off, int len) throws IOException;

}

