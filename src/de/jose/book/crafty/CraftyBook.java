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

package de.jose.book.crafty;

import de.jose.chess.*;
import de.jose.book.OpeningBook;
import de.jose.book.BookEntry;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * CraftyBook
 * 
 * @author Peter Schäfer
 */

public class CraftyBook extends OpeningBook
{
	/** */
	protected boolean bigendian;
	protected CraftyHashKey hashkey;
	protected CraftyHashKey reversed_hashkey;

	/**
	 * opens a Crafty opening book file ("book.bin")
	 */
	public CraftyBook()
	{
		hashkey = new CraftyHashKey(false);
		reversed_hashkey = new CraftyHashKey(true);
	}

	public boolean canTranspose()                               { return true; }
	public boolean canTransposeColor()                      { return true; }
	public boolean canTransposeIntoBook()               { return false; }

	public boolean  open(RandomAccessFile file)
			throws IOException
	{
		disk = file;

		bigendian = false;
		if (seekCluster(CraftyHashKey.START_POSITION_NO_COLOR) <= 0) {
			bigendian = true;
			if (seekCluster(CraftyHashKey.START_POSITION_NO_COLOR) <= 0) {
				disk = null;
				return false;
			}
		}
		return true;
	}

	/**
	 * from a given position, get all book moves and the associated Book Entries
	 *
	 * @param pos
	 * @param deep
	 * @return
	 * @throws IOException
	 */
	public boolean getBookMoves(Position pos, boolean withTransposedColors, boolean deep, List result) throws IOException
	{
		if (!canTransposeColor()) withTransposedColors = false;  //  no use looking for transposed colors
		boolean res1 = getBookMovesColored(pos,false,result);
		boolean res2 = false;
		if (withTransposedColors) res2 = getBookMovesColored(pos,true,result);
		return res1||res2;
	}

	private boolean getBookMovesColored(Position pos, boolean reverse, List result)
			throws IOException
	{
		CraftyHashKey key = reverse ? reversed_hashkey:hashkey;
		//  this hash key is WITHOUT turn color. Used for looking up a cluster.
		int turn = pos. movesNext();
		pos.setMovesNext(0);
		pos.computeHashKey(key);
		pos.setMovesNext(turn);

		int posCount = seekCluster(key.value());

		//  upper 16 bits appear in the stored hash keys
		long common = key.value() & 0xffff000000000000L;
		long temp_hash_key;

		//  get set of all sought hash keys & map them to Moves
		HashMap lookingFor = new HashMap();
		MoveIterator it = new MoveIterator(pos);
		while (it.next()) {
			Move move = it.getMove();
			pos.doMove(move);
			pos.computeHashKey(key);    //  this hash key (inside a cluster) is WITH color
			pos.undoMove();

			//  leave upper 16 bits from previous position
			temp_hash_key = common | (key.value() & 0x0000ffffffffffffL);
			lookingFor.put(new CraftyHashKey(temp_hash_key), move.clone());
		}

		//  traverse the cluster, find all matching positions
		CraftyBookEntry entry = new CraftyBookEntry();
		for (int i=0; i < posCount; i++) {
			entry.read(this);

			Move move = (Move)lookingFor.get(new CraftyHashKey(entry.key));
			if (move!=null)
			{
				CraftyBookEntry result_entry = (CraftyBookEntry)entry.clone();
				result_entry.move = move;
				result_entry.isTransposedColor = reverse;
				result.add(result_entry);
			}
		}

		return posCount >= 0;
		//  note that Crafty will only return moves from book positions.
		//  It can not transpose from outside the book into the book.
		//  thus:
		//  NOT canTransposeIntoBook() IMPLIES  ( getBookMoves()==true <--> NOT result.isEmpty() )
	}

	public BookEntry selectBookMove(Position pos, boolean ignoreColors, Random random)
			throws IOException
	{
			return null;  //let OpeningLibrary choose a move
	}


	private int seekCluster(long key) throws IOException
	{
		//  use upper 15 bits to probe offset table
		//  which is located in the first 128 KBytes of the file
		int offset = 4*(int)((key >> 49) & 0x7fff);
		disk.seek(offset);
		int clusterOffset = readInt();
		if (clusterOffset <= 0) return -1;

		//  at that location we find a cluster of book entries
		disk.seek(clusterOffset);
		return readInt();
	}


	public float readFloat() throws IOException
	{
		int fi = readInt();
		return Float.intBitsToFloat(fi);
	}

	public int readInt() throws IOException
	{
		if (bigendian)
			return (disk.read()<<24) | (disk.read()<<16) | (disk.read()<<8) | disk.read();
		else
			return disk.read() | (disk.read()<<8) | (disk.read()<<16) | (disk.read()<<24);
	}

	public long readLong() throws IOException
	{
		long r1 = ((long)readInt()) & 0x00000000ffffffffL;
		long r2 = ((long)readInt()) & 0x00000000ffffffffL;

		if (bigendian)
			return (r1 << 32) | r2;
		else
			return r1 | (r2 << 32);
	}

}