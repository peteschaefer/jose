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

package de.jose.store;

import java.io.IOException;
import java.io.InputStream;

/**
 * a common interface for DiskFile and RAMFile
 * 
 * used by RandomInputStream and RandomOutputStream
 * 
 * @author Peter Schäfer
 */

public interface StorageFile
{
	/**	@return the file length	 */
	public long length()	throws IOException;
	
	/**	set the file length, enlargint it, if necessary	 */
	public void setLength(long length) throws IOException;
	
	/**	write a number of bytes to the file	 */
	public void write(long filePosition, byte[] buf, int offset, int len) throws IOException;
	
	/**	read a number of bytes from the file	 */
	public int read(long filePosition, byte[] buffer, int offset, int len) throws IOException;

	/**	copy a stream into the file	*/
	public void copy(long position, InputStream src, int offset, int len) throws IOException;

	/**	close the file	 */
	public void close() throws IOException;
	
	/**	@return a meaningfull name of the file (e.g. the disk file name)	 */
	public String getName();
	
	/**	delete the file	 */
	public boolean delete() throws IOException;
	
}

