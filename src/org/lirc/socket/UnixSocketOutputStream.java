/**************************************************************************/
/*
/* UnixSocketOutputStream.java -- Part of the org.lirc.socket package
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
import java.io.OutputStream;

/**
 * Output stream for unix domain sockets
 *
 * @version $Revision: 1.1 $
 * @author Bjorn Bringert (bjorn@mumblebee.com)
 */
class UnixSocketOutputStream extends OutputStream {
	private UnixSocketImpl impl;

	public UnixSocketOutputStream(UnixSocketImpl impl) {
		this.impl = impl;
	}

	public void close() throws IOException {
		impl.shutdownOutput();
	}

	public void write(int b) throws IOException {
		nativeWrite(impl.getFd(), b);
	}

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null)
			throw new NullPointerException();
		else if (off < 0 || off > b.length || len < 0
			|| (off + len) > b.length || (off + len) < 0)
			throw new IndexOutOfBoundsException();
		else if (len == 0)
			return;

		nativeWrite(impl.getFd(), b, off, len);
	}

	private static native void nativeWrite(int fd, int b) throws IOException;

	private static native void nativeWrite(int fd, byte[] b, int off, int len) throws IOException;

}
