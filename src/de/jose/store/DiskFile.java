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

import java.io.*;

/**
 * a wrapper around RandomAccessFile
 * implements StorageFile
 * 
 * @author Peter Schäfer
 */

public class DiskFile
		extends RandomAccessFile
		implements StorageFile
{
	protected File file;
	
	public DiskFile(File f) throws IOException
	{
		super(f,"rw");
		file=f;
	}
	
	public DiskFile(File f, String mode) throws IOException
	{
		super(f,mode);
		file=f;
	}
	
	public static DiskFile tempFile() throws IOException
	{
		File f = File.createTempFile("tmp",".dat");
		f.deleteOnExit();
		return new DiskFile(f);
	}								 
	
	public long length()	throws IOException
	{
		getFD().sync();
		return super.length();
	}

	public void setLength(long length) throws IOException
	{
		getFD().sync();
		super.setLength(length);
	}
	
	/**	flush the buffer contents to the underyling file	 */
	public void write(long filePosition, byte[] buf, int offset, int len) throws IOException
	{
		getFD().sync();
		seek(filePosition);
		write(buf,offset,len);
	}
	
	public int read(long filePosition, byte[] buffer, int offset, int len) throws IOException
	{
		getFD().sync();
		seek(filePosition);
		return read(buffer,offset,len);
	}

	public void copy(long filePosition, InputStream src, int offset, int len) throws IOException
	{
		if (offset > 0) src.skip(offset);

		RandomOutputStream out = new RandomOutputStream(this);
		out.seek(filePosition);
		out.copy(src,len);
		out.close();
	}

	public void copy(InputStream in) throws IOException
	{
		RandomOutputStream out = new RandomOutputStream(this);
		out.copy(in);
		out.close();
	}
	
	public void copy(StorageFile file) throws IOException
	{
		RandomInputStream in = new RandomInputStream(file);
		copy(in);
		in.close();
	}
	
	public void copy(File file) throws IOException
	{
		FileInputStream in = new FileInputStream(file);
		copy(in);
		in.close();
	}
	
	public String getName()
	{
		return file.getAbsolutePath();
	}
	
	public boolean delete() throws IOException
	{
		close();
		return file.delete();
	}
}
