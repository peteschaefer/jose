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
 *	RandomInputStream: a positionable Input Stream
 * 
 *	in addition to the normal Input Stream methods, data can be read from any part of the stream
 *	by using seek() or read(position,...)
 * 
 *	it also has a buffering scheme, just like BufferedInputStream
 *	(note that you could put a BufferedInputStream on top, but yout would lose the positioned methods)
 * 
 *	contrary to most other stream implementations, a call to close() DOES NOT close the underlying file
 *	unless you explictly request it !
 */

public class RandomInputStream
		extends InputStream
		implements DataInput
{
	//-------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------

	public static final int		DEFAULT_BUFFER_SIZE	 = 4096;
	
	//-------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------

	/**	the underlying file	 */
	protected StorageFile	fFile;
	/**	the read buffer	 */
	protected byte[]		fBuffer;
	/**	close underlying file ?	 */
	protected boolean		fCloseFile;
	/**	position of buffer in file	 */
	protected long			fBufferPosition;
	/**	current buffer size	 */
	protected int			fBufferFill;
	/**	current read marker	 */
	protected int			fBufferMark;

	/**	current user mark	 */
	protected long			fUserMark;
	
	//-------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------
	
	public RandomInputStream(StorageFile file)
	{
		this (file,DEFAULT_BUFFER_SIZE,false);
	}
	
	public RandomInputStream(StorageFile file, int bufSize, boolean close)
	{
		fFile = file;
		fBuffer = new byte[bufSize];
		fBufferPosition = 0;
		fBufferFill = 0;
		fBufferMark = 0;
		fCloseFile = close;
	}
	
	
	public RandomInputStream(File file) throws IOException
	{
		this(new DiskFile(file,"r"), DEFAULT_BUFFER_SIZE, true);
	}
	
	//-------------------------------------------------------------------------
	//	Public Methods
	//-------------------------------------------------------------------------

	public int available() throws IOException
	{
		return (int)(length() - getPosition());
	}
	
	public void close() throws IOException
	{
		if (fCloseFile)
			closeImpl();
	}
	
	public boolean markSupported()	
	{
		return true; 
	}
	
	public void mark(int readlimit)
	{
		fUserMark = getPosition();
	}
	
	public void reset()
		throws IOException
	{
		seek(fUserMark);
	}
	
	public void seek(long position)
		throws IOException
	{
		if (position > length())
			throw new EOFException();
		
		if (position >= fBufferPosition &&
			position < (fBufferPosition + fBufferFill))
			fBufferMark = (int)(position-fBufferPosition);
		else {
			fBufferPosition = position;
			fBufferFill = 0;
			fBufferMark = 0;
		}
	}
	
	public long length() throws IOException
	{
		return lengthImpl();
	}
	
	public void flush()
	{
		fBufferFill = 0;
		fBufferMark = 0;
	}
	
	public long getPosition()
	{
		return fBufferPosition + fBufferMark;
	}
	
	public int read() throws IOException			
	{
		if (fBufferMark >= fBufferFill) { 
			fillBuffer(fBufferPosition+fBufferFill);
			if (fBufferFill==0)
				return -1;
		}
		return fBuffer[fBufferMark++];
	}

	
	public int read(byte[] b) throws IOException	{ return read(b, 0, b.length); }
	
	public int read(byte[] b, int off, int len) 
		throws IOException
	{
		int result = 0;
		
		while (len > 0) {
			if (fBufferMark >= fBufferFill) 
				fillBuffer(getPosition());
			if (fBufferFill==0)
				break;
			
			int chunk = Math.min(len, fBufferFill-fBufferMark);
			System.arraycopy(fBuffer,fBufferMark, b,off, chunk);
			
			off += chunk;
			len -= chunk;
			result += chunk;
			
			fBufferMark += chunk;
		}
		
		return result;
	}

	public byte readByte() throws IOException 
	{
		if (fBufferMark >= fBufferFill)  {
			fillBuffer(getPosition());
			if (fBufferFill==0)
				throw new EOFException();
		}
		return fBuffer[fBufferMark++];	
	}
	
	public short readShort() throws IOException
	{
		if ((fBufferMark+4) >= fBufferFill)
			return (short)(((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF));
		else
			return (short)(((fBuffer[fBufferMark++] & 0xFF) <<  8) | (fBuffer[fBufferMark++] & 0xFF));
	}
	
	public int readInt() throws IOException
	{
		if ((fBufferMark+4) >= fBufferFill)
			return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
				 | ((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF);
		else
			return ((fBuffer[fBufferMark++] & 0xFF) << 24) | ((fBuffer[fBufferMark++] & 0xFF) << 16)
				 | ((fBuffer[fBufferMark++] & 0xFF) <<  8) |  (fBuffer[fBufferMark++] & 0xFF);
	}
	
	public long readLong() throws IOException
	{
	    return (((long)readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
	}
	
	public boolean readBoolean() throws IOException
	{
		return readByte() != 0;
	}
	
	public int readUnsignedByte() throws IOException
	{
		return ((int)readByte() & 0x000000ff);
	}
	
	public int skipBytes(int n) throws IOException
	{
		long pos = getPosition();
		seek(pos+n);
		return (int)(getPosition()-pos);
	}
	
	public void readFully(byte[] b, int off, int len) throws IOException
	{
		read(b,off,len);
	}
	
	public void readFully(byte[] b) throws IOException
	{
		read(b);
	}
	
	public char readChar() throws IOException
	{
		return (char)readUnsignedShort();
	}
	
	public int readUnsignedShort() throws IOException
	{
		return ((int)readShort() & 0x0000ffff);
	}
	
	public double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}
	
	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}
	
	private char readc()	throws IOException 
	{
		return (char)(read()<<8 | read()); 
	}
	
	public String readLine() throws IOException
	{
		if (available() <= 0) return null;
		
		StringBuffer buf = new StringBuffer();
		for (;;) {
			char c = readc();
			if (c<0) break;		//	EOF
			
			if (c=='\r') {
				long pos = getPosition();
				if (readc() != '\n') seek(pos);	//	push back
				break;
			}
			if (c=='\n')
				break;
			
			buf.append(c);
		}
		
		return buf.toString();
	}
	
	public String readUTF() throws IOException
	{
		byte[] bytes = new byte[readUnsignedShort()];
		read(bytes);
		return new String(bytes, "UTF-8");
	}
	
	
	//-------------------------------------------------------------------------
	//	Protected Methods
	//-------------------------------------------------------------------------

	protected void fillBuffer(long position) throws IOException
	{
		fBufferFill = readImpl(position, fBuffer, fBuffer.length);
		fBufferMark = 0;
		fBufferPosition = position;
	}
	
	//-------------------------------------------------------------------------
	//	Implementation Hooks
	//-------------------------------------------------------------------------
	
	public long lengthImpl()	throws IOException
	{
		return fFile.length();
	}
	
	public void closeImpl()	throws IOException
	{
		fFile.close();
	}
	
	public int readImpl(long filePosition, byte[] buffer, int len) throws IOException
	{
		return fFile.read(filePosition,buffer,0,len);
	}
}
