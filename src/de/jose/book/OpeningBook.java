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

package de.jose.book;

import de.jose.chess.Position;
import de.jose.chess.Move;
import de.jose.chess.HashKey;
import de.jose.book.crafty.CraftyBook;
import de.jose.book.polyglot.PolyglotBook;
import de.jose.util.file.FileUtil;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.text.DecimalFormat;

/**
 * IOpeningBook
 *
 * @author Peter Schäfer
 */
public abstract class OpeningBook
{
	/** the book.bin file   */
	protected RandomAccessFile disk;

	protected static DecimalFormat VALUE_FORMAT = new DecimalFormat("#,##0.##");

	abstract public boolean canTranspose();
	abstract public boolean canTransposeColor();
	abstract public boolean canTransposeIntoBook();

	public static OpeningBook open(File file) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file,"r");
		//  try all known opening book formats, one after another
		OpeningBook book = new CraftyBook();
		if (book.open(raf)) return book;

		book = new PolyglotBook();
		if (book.open(raf)) return book;

		//  else
		return null;
	}

	public OpeningBook()
	{ }

	abstract public boolean  open(RandomAccessFile file)
			throws IOException;

	/**
	 *
	 * @param pos current position
	 * @param ignoreColors look for reversed color transpositions, too
	 * @param result list of book moves + book entries
	 * @return true if the position is contained in the book.
	 *      Note that an out-of-book position still might return moves that transpose into the book.
	 *      However, those moves are not chosen when playing against an engine.
	 *
	 * @throws IOException
	 */
	abstract public boolean getBookMoves(Position pos, boolean ignoreColors, List result)
			throws IOException;

	/**
	 * randomly choose one move
	 * @return a randomly chosen move, or null if the current position is not present
	 */
	abstract public BookEntry selectBookMove(Position pos, boolean ignoreColors, Random random)
			throws IOException;

	/**
	 *
	 * @throws java.io.IOException
	 */
	public void close() throws IOException
	{
		if (disk!=null) disk.close();
		disk = null;
	}


	public static void main(String[] args)
	{
		try {
			for (int i=0; i < args.length; i++)
			{
				OpeningBook book = OpeningBook.open(new File(args[i]));
				System.out.println(" --- "+args[i]+" --- ");

				Position pos = new Position(Position.START_POSITION);
/*
				HashKey[] hk = {
//						new JoseHashKey(false), new JoseHashKey(true),
						new CraftyHashKey(false), new CraftyHashKey(true),
//						new PolyglotHashKey(false), new PolyglotHashKey(true),
				};
*/
				printMoves(book, pos);
				System.out.println("\n\n");

				pos.setup("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
				printMoves(book, pos);
				System.out.println("\n\n");

				pos.setup("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1");
				printMoves(book, pos);
				System.out.println("\n\n");

				pos.setup("rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq - 1 1");
				printMoves(book, pos);
				System.out.println("\n\n");

				pos.setup("rnbqkbnr/pp1ppppp/8/2p5/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
				printMoves(book, pos);
				System.out.println("\n\n");

				pos.setup("rnbqkbnr/pp1ppppp/8/2p5/2P5/8/PP1PPPPP/RNBQKBNR w KQkq - 0 1");
				printMoves(book, pos);
				System.out.println("\n\n");

				book.close();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void printHashKeys(Position pos, HashKey[] hk)
	{
		for (int i=0; i < hk.length; i++)
		{
			pos.computeHashKey(hk[i]);
			System.out.println(hk[i]);
			if (i%2 > 0) System.out.println();
		}
	}

	private static void printMoves(OpeningBook book, Position pos)
			throws IOException
	{
		List book_moves = new ArrayList();

		boolean in_book = book.getBookMoves(pos,true,book_moves);

		if (in_book)
			System.out.println("position is in book");
		else
			System.out.println("position is out of book");

		for (int i=0; i<book_moves.size(); i++) {
			BookEntry entry = (BookEntry) book_moves.get(i);
			Move move = entry.move;
			System.out.print(move.toString() + " ");

			double i1 = entry.userValue;
			if (i1 == BookEntry.VERY_GOOD_MOVE) {
				System.out.print(" !!");
			} else if (i1 == BookEntry.GOOD_MOVE) {
				System.out.print(" !");
			} else if (i1 == BookEntry.NEUTRAL_MOVE) {
				System.out.print(" =");
			} else if (i1 == BookEntry.BAD_MOVE) {
				System.out.print(" ?");
			} else if (i1 == BookEntry.VERY_BAD_MOVE) {
				System.out.print(" ??");
			} else if (i1 == BookEntry.IUNKNOWN) {
			} else {
				System.out.print(" ");
				System.out.print(VALUE_FORMAT.format(entry.userValue));
			}

			System.out.print("\t\t");
			System.out.print(entry.count);

			if (entry.countWhite!=BookEntry.IUNKNOWN) {
				System.out.print(" +");
				System.out.print(entry.countWhite);
			}

			if (entry.countDraw!=BookEntry.IUNKNOWN) {
				System.out.print(" =");
				System.out.print(entry.countDraw);
			}

			if (entry.countBlack!=BookEntry.IUNKNOWN) {
				System.out.print(" -");
				System.out.print(entry.countBlack);
			}

			if (entry.isTransposedColor)
				System.out.print(" (transposed colors)");
/*
			if (Util.anyOf(entry.flags,0x20))
				System.out.print("-");  //  black won at least one game
			if (Util.anyOf(entry.flags,0x40))
				System.out.print("=");  //  at least one game was draw
			if (Util.anyOf(entry.flags,0x80))
				System.out.print("+");  //  white won at least one game
			System.out.print("\t\t\t\t");

			System.out.print(entry.gameCount);
			System.out.print("\t\t\t\t");
			System.out.print(entry.learnValue);
			System.out.print("\t\t\t\t");
			System.out.print(entry.capScore);
*/
			System.out.println();
		}
	}
}
