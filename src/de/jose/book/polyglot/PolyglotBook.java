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

package de.jose.book.polyglot;

import de.jose.book.OpeningBook;
import de.jose.book.BookEntry;
import de.jose.chess.Position;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Book
 *
 * @author Peter Schäfer
 */
public class PolyglotBook extends OpeningBook
{
	protected int BookSize;
	protected PolyglotHashKey hashkey;
	protected PolyglotHashKey reversed_hashkey;

	public PolyglotBook()
	{
		hashkey = new PolyglotHashKey(false);
		reversed_hashkey = new PolyglotHashKey(true);
	}

	public boolean canTranspose()                               { return true; }
	public boolean canTransposeColor()                      { return true; }
	public boolean canTransposeIntoBook()               { return true; }


	public boolean open(RandomAccessFile file)
			throws IOException
	{
		disk = file;
		BookSize = (int) (file.length() / 16);
		//  make at least one valid probe
		boolean result = (BookSize > 0) && find_pos(PolyglotHashKey.START_POSITION) < BookSize;
		if (!result) disk = null;
		return result;
	}

	public boolean getBookMoves(Position pos, boolean withTransposedColors, List result)
			throws IOException
	{
		if (!canTransposeColor()) withTransposedColors = false;  //  no use looking for transposed colors
		boolean res1 = getBookMovesColored(pos,false,result);
		boolean res2 = false;
		if (withTransposedColors) res2 = getBookMovesColored(pos,true,result);
		return res1||res2;
	}

	private boolean getBookMovesColored(Position pos, boolean reversed, List result) throws IOException
	{
		PolyglotHashKey key = reversed ? reversed_hashkey:hashkey;
		pos.computeHashKey(key);

		boolean in_book = false;
		if (disk != null && BookSize > 0)
		{
			int p = find_pos(key.value());
			in_book = p < BookSize;

			for ( ; p < BookSize; p++)
			{
				PolyglotBookEntry entry = read_entry(p, reversed);
				if (entry.key != key.value()) break;

				result.add(entry);   //  TODO translate Polyglot entry.move to de.jose.Move
			}
		}

		return in_book;
	}

	public BookEntry selectBookMove(Position pos, boolean withTransposedColors, Random random)
	{
		return null;    //  let OpeningLibrary select a move
	}


	private int find_pos(long key)
			throws IOException
	{
		int left, right, mid;
		long entry_key;

		// binary search (finds the leftmost entry)

		left = 0;
		right = BookSize - 1;

//	   assert(left<=right);

		while (left < right) {

			mid = (left + right) / 2;
//	      ASSERT(mid>=left&&mid<right);

			entry_key = read_entry_key(mid);

			if (compareUnsignedLong(key,entry_key) <= 0) {
				right = mid;
			} else {
				left = mid + 1;
			}
		}

//	   ASSERT(left==right);

		entry_key = read_entry_key(left);

		return (entry_key == key) ? left : BookSize;
	}


	private static long compareUnsignedLong(long a, long b)
	{
		long a1 = (a>>32) & 0x00000000FFFFFFFFL;
		long b1 = (b>>32) & 0x00000000FFFFFFFFL;
		long result = a1-b1;

		if (result==0L) {
			a1 = a & 0x00000000FFFFFFFFL;
			b1 = b & 0x00000000FFFFFFFFL;
			result = a1-b1;
		}

		return result;
	}


	private PolyglotBookEntry read_entry(int n, boolean isTransposedColor)
			throws IOException
	{
//	   ASSERT(entry!=NULL);
//	   ASSERT(n>=0&&n<BookSize);

//	   ASSERT(BookFile!=NULL);
		PolyglotBookEntry entry = new PolyglotBookEntry();

		disk.seek(n * 16);

		entry.key = read_integer(disk, 8);
		entry.move = PolyglotBookEntry.createMove((int) read_integer(disk, 2), isTransposedColor);
		entry.isTransposedColor = isTransposedColor;
		entry.count = (int) read_integer(disk, 2);
		entry.n = (int) read_integer(disk, 2);
		entry.sum = (int) read_integer(disk, 2);
		/* todo the meaning of count, n, sum is not clear
		 	some sources call count = weight, providing a 'scaled' value.
		 	But it is not clear how that is supposed to be interpreted; conventions seem to vary.
		 	n,sum is meant for book learning. Again, there's no consensus how to interpret it.

		 	This is a bit sad, since I _do_ want to extract WDL scores from the book.
		 */

		return entry;
	}

	private long read_entry_key(int n)
			throws IOException
	{
		disk.seek(n * 16);
		return read_integer(disk, 8);
	}

	private long read_integer(RandomAccessFile file, int size)
			throws IOException
	{

		long n;
		int i;
		int b;

//	   ASSERT(file!=NULL);
//	   ASSERT(size>0&&size<=8);

		n = 0;

		for (i = 0; i < size; i++) {

			b = file.read();

			if (b < 0)
				throw new IOException("EOF reached");

//	      ASSERT(b>=0&&b<256);
			n = (n << 8) | b;
		}

		return n;
	}

}

