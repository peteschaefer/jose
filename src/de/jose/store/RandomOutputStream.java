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
 * RandomOutputStream: a positionable Output Stream
 * 
 *	int addition to the normal Output Stream methods, data can be written to any part of the Stream
 *	by using seek() or write(position,...)
 * 
 *	it als has a buffering scheme, just like BufferedOutputStream
 *	(note that you could put a BufferedOutputStream on top, but yout would lose the positioned methods)
 * 
 *	contrary to most other stream implementations, a call to close() DOES NOT close the underlying file
 *	unless you explicitly request it !
 */

public class RandomOutputStream
		extends OutputStream
		implements DataOutput
{
	//-------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------

	/**	default buffer size	 */
	public static final int	DEFAULT_BUFFER_SIZE	= 4096;
	
	
	//-------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------

	/**	the underlying storage	 */
	protected StorageFile	fFile;
	/**	close underlying file ?	 */
	protected boolean		fCloseFile;
	/**	the write buffer	 */
	protected byte[]		fBuffer;
	/**	position of buffer in file	 */
	protected long			fBufferPosition;
	/**	current buffer size	 */
	protected int			fBufferFill;
	
	//-------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------
	
	public RandomOutputStream(StorageFile file)
	{
		this(file, DEFAULT_BUFFER_SIZE, false);
	}
	
	public RandomOutputStream(StorageFile file, int bufSize, boolean close)
	{
		super();
		fFile = file;
		fBuffer = new byte[bufSize];
		fBufferFill = 0;
		fBufferPosition = 0;
		fCloseFile = close;
	}
	
	public RandomOutputStream(File file) throws IOException
	{
		this(new DiskFile(file,"rw"), DEFAULT_BUFFER_SIZE, true);
	}
	
	//-------------------------------------------------------------------------
	//	Public Methods
	//-------------------------------------------------------------------------
	
	/**	@return the total size of the stream	 */
	public long length()
		throws IOException
	{
		return Math.max(lengthImpl(), fBufferPosition+fBufferFill); 
	}
	
	public long getPosition()
	{
		return fBufferPosition + fBufferFill;
	}
	
	/**	move to a certain position in the stream	 */
	public void seek(long position) 
		throws IOException
	{
		flush();
		if (position > length())
			setLength(position);
		fBufferPosition = position;
	}
	
	public void setLength(long len) throws IOException
	{
		flush();
		setLengthImpl(len);
		if (fBufferPosition > length())
			fBufferPosition = length();
	}
	
	public void flush()	throws IOException
	{
		if (fBufferFill > 0) {
			writeImpl(fBufferPosition, fBuffer, fBufferFill);
			fBufferPosition += fBufferFill;
		}
		fBufferFill = 0;
	}
	
	public void close() throws IOException
	{
		flush();
		if (fCloseFile)
			closeImpl();
	}
	
	public final void write(byte[] b) throws IOException
	{
		write(b,0,b.length);
	}
	
	public final void write(byte[] b, int off, int len) throws IOException
	{
		while (len > 0) {
			int chunk = Math.min(len, fBuffer.length-fBufferFill);
			System.arraycopy(b,off, fBuffer,fBufferFill, chunk);
			
			off += chunk;
			len -= chunk;
			
			fBufferFill += chunk;
			if (fBufferFill >= fBuffer.length)
				flush();
		}
	}
	
	public final void write(int b) throws IOException
	{
		writeByte((byte)b);
	}
	
	public final void writeByte(byte b) throws IOException			
	{
		fBuffer[fBufferFill++] = b;
		if (fBufferFill >= fBuffer.length)
			flush();
	}
	
	public final void writeByte(int b) throws IOException			
	{
		writeByte((byte)b);
	}
	
	public final void writeShort(short i) throws IOException		{
		if ((fBufferFill+2) >= fBuffer.length) {
			writeByte((byte)(i >>  8));
			writeByte((byte) i);
		} else {
			fBuffer[fBufferFill++] = (byte)(i >> 8);
			fBuffer[fBufferFill++] = (byte) i;
		}
	}
	
	public final void writeShort(int i) throws IOException		
	{
		writeShort((short)i);
	}
	
	public final void writeInt(int i) throws IOException		{
		if ((fBufferFill+4) >= fBuffer.length) {
			writeByte((byte)(i >> 24));
			writeByte((byte)(i >> 16));
			writeByte((byte)(i >>  8));
			writeByte((byte) i);
		}
		else {
			fBuffer[fBufferFill++] = (byte)(i >> 24);
			fBuffer[fBufferFill++] = (byte)(i >> 16);
			fBuffer[fBufferFill++] = (byte)(i >>  8);
			fBuffer[fBufferFill++] = (byte) i;
		}
	}
	
	public final void writeChar(char c) throws IOException
	{
		writeShort((short)c);
	}
	
	public final void writeChar(int c) throws IOException
	{
		writeShort((short)c);
	}
	
	public final void writeLong(long i) throws IOException		
	{
		writeInt((int)(i >> 32));
		writeInt((int) i);
	}
	
	public final void writeDouble(double d) throws IOException
	{
		writeLong(Double.doubleToRawLongBits(d));
	}
	
	public final void writeFloat(float f) throws IOException
	{
		writeInt(Float.floatToRawIntBits(f));
	}
	
	public final void writeBoolean(boolean b) throws IOException
	{
		writeByte(b ? 1:0);
	}
	
	public void writeChars(String s) throws IOException
	{
		for (int i=0; i<s.length(); i++)
			writeChar(s.charAt(i));
	}
	
	public void writeUTF(String s) throws IOException
	{
		byte[] bytes = s.getBytes("UTF-8");
		writeShort(bytes.length);
		write(bytes);
	}
	
	public void writeBytes(String s) throws IOException
	{
		byte[] bytes = s.getBytes();
		write(bytes);
	}
	
	public void copy(InputStream in) throws IOException
	{
		copy(in, Long.MAX_VALUE);
	}
	
	public void copy(InputStream in, long len) throws IOException
	{
		flush();
		while (len > 0) 
		{
			fBufferFill = in.read(fBuffer,0, (int)Math.min(fBuffer.length,len));
			if (fBufferFill==0) break;
			len -= fBufferFill;
			flush();
		}
	}
		
	//-------------------------------------------------------------------------
	//	Implementation Hooks
	//-------------------------------------------------------------------------

	/**	@return the length of the stream	 */
	protected long lengthImpl() throws IOException
	{
		return fFile.length();
	}
	
	protected void setLengthImpl(long length) throws IOException
	{
		fFile.setLength(length);
	}

	/**	flush the buffer contents to the underyling file	 */
	protected void writeImpl(long filePosition, byte[] buf, int len) throws IOException
	{
		fFile.write(filePosition,buf,0,len);
	}
	
	protected void closeImpl() throws IOException
	{
		fFile.close();
	}
}
