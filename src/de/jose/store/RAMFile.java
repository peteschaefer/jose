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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *	stores data in memory
 *
 */

public class RAMFile
		implements StorageFile
{
	//-------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------

	/**		 */
	public static final int		DEFAULT_BLOCK_SIZE		= 4096;
	
	//-------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------
	
	/**	block size	 */
	protected int		fBlockSize;
	/**	block list	 */
	protected Vector	fBlocks;
	/**	total size	 */
	protected long		fLength;
	
	//-------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------
	
	public RAMFile()	
	{
		this(DEFAULT_BLOCK_SIZE);
	}
	
	public RAMFile(int blockSize)
	{
		fBlockSize = blockSize;
		fBlocks = new Vector();
		fLength = 0L;
	}
	
	//-------------------------------------------------------------------------
	//	Public Methods
	//-------------------------------------------------------------------------
	
	public final long length()		{ return fLength; }
	
	public synchronized void write(long position, byte[] src, int offset, int len)
	{
		int blockIdx = (int)(position / fBlockSize);
		int blockOff = (int)(position % fBlockSize);
		int total = len;
		
		while (len > 0)
		{
			int chunk = Math.min(len, fBlockSize-blockOff);
			writeChunk(blockIdx,blockOff, src,offset,chunk);
			
			offset += chunk;
			len -= chunk;
			
			blockIdx++;
			blockOff = 0;
		}
		
		position += total;
		if (position > fLength)
			fLength = position;
	}

	public synchronized void copy(long position, InputStream src, int offset, int len)
			throws IOException
	{
		if (offset > 0) src.skip(offset);

		int blockIdx = (int)(position / fBlockSize);
		int blockOff = (int)(position % fBlockSize);

		while (len > 0)
		{
			int chunk = Math.min(len, fBlockSize-blockOff);
			int received = copyChunk(blockIdx,blockOff, src,chunk);

			len -= received;
			position += received;

			if (received < chunk) break;

			blockIdx++;
			blockOff = 0;
		}

		if (position > fLength)
			fLength = position;
	}


	public int copyChunk(int blockIdx, int blockOff, InputStream src, int len) throws IOException
	{
		allocate(blockIdx+1);
		byte[] block = (byte[])fBlocks.elementAt(blockIdx);
		return src.read(block,blockOff,len);
	}


	public synchronized int read(long position, byte[] trg, int offset, int len)
	{
		if ((position + len) > fLength)
			len = (int)(fLength - position);
		if (len <= 0)
			return 0;
		
		int blockIdx = (int)(position / fBlockSize);
		int blockOff = (int)(position % fBlockSize);
		int result = len;
		
		while (len > 0)
		{
			int chunk = Math.min(len, fBlockSize-blockOff);
			readChunk(blockIdx,blockOff, trg,offset,chunk);
			
			offset += chunk;
			len -= chunk;
			
			blockIdx++;
			blockOff = 0;
		}
		
		return result;
	}

	public void setLength(long len)
	{
		if (len < 0) throw new IllegalArgumentException("length must not be negative");
		
		int newBlocks = (int)((len+fBlockSize-1) / fBlockSize);
		if (newBlocks > fBlocks.size())
			allocate(newBlocks);
		else
			fBlocks.setSize(newBlocks);
		fLength = len;
	}
	

	public void close()
	{	}
	
	public String getName()
	{
		return this.toString();
	}
	
	public boolean delete()
	{
		fBlocks.clear();
		fLength = 0;
		return true;
	}
	
	public void copy(InputStream in) throws IOException
	{
		RandomOutputStream out = new RandomOutputStream(this,fBlockSize,false);
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

	public byte[] getRawData(int position)
	{
		if (position >= fLength) return null;
		int blockIdx = (int)(position / fBlockSize);
		if (blockIdx >= fBlocks.size())
			return null;
		else
			return (byte[])fBlocks.elementAt(blockIdx);
	}

	public int getRawBlockSize(int position)
	{
		if (position >= fLength) return 0;
		int blockIdx = (int)(position / fBlockSize);
		if (blockIdx >= fBlocks.size())
			return 0;
		else if (blockIdx == fBlocks.size()-1)
			return (int)(fLength % fBlockSize);
		else
			return fBlockSize;
	}

	//-------------------------------------------------------------------------
	//	Protected Methods
	//-------------------------------------------------------------------------
	
	protected void writeChunk(int blockIdx, int blockOff, byte[] src, int offset, int len)
	{
		allocate(blockIdx+1);
		byte[] block = (byte[])fBlocks.elementAt(blockIdx);
		System.arraycopy(src,offset, block,blockOff, len);
	}
	
	protected void readChunk(int blockIdx, int blockOff, byte[] trg, int offset, int len)
	{
		byte[] block = (byte[])fBlocks.elementAt(blockIdx);
		System.arraycopy(block, blockOff, trg,offset, len);
	}
	
	protected void allocate(int numBlocks)
	{
		while (numBlocks > fBlocks.size())
			fBlocks.addElement(new byte[fBlockSize]);
	}
	
	public static void main(String[] args)
		throws Exception
	{
		RAMFile f = new RAMFile(357);
		RandomOutputStream out = new RandomOutputStream(f,123,true);
		RandomInputStream in = new RandomInputStream(f,235,true);
		
		for (int i=0; i<4321; i += 100) 
		{
			for (int j=i; j < (i+100); j++)
				out.writeInt(j);
			out.flush();
			
			for (int j=i; j < (i+100); j++)
				if (in.readInt() != j) {
					System.out.println("gotcha "+j);
					break;
				}
			System.out.println(i);
		}
	}
}
