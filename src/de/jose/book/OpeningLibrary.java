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

import de.jose.profile.UserProfile;
import de.jose.Config;
import de.jose.Application;
import de.jose.util.xml.XMLUtil;
import de.jose.chess.Position;

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;

/**
 * OpeningLibrary
 *
 * @author Peter Schäfer
 */
public class OpeningLibrary
		extends Vector/*<BookFileEntry>*/
{
	/** when playing against a chess engine...
	 *  TODO these settings belong to EnginePlugin !?
	 * */

	/** Only use the GUI book. Disable the engine book  */
	public static final int GUI_BOOK_ONLY   = 0x0002;
	/** Prefere the GUI book. Let the engine book enabled, as backup
	 *      (this is the default)
	 * */
	public static final int PREFER_GUI_BOOK = 0x0003;
	/** If there is an engine book, use it.
	 *  Otherwise use the GUI book.
	 */
	public static final int PREFER_ENGINE_BOOK  = 0x0001;
	/** use neither GUI nor engine book.
	 */
	public static final int NO_BOOK = 0x0000;

	/** when retrieving moves ...   */
	/** Use the first book that contains the position   */
	public static final int COLLECT_FIRST       = 0;
	/** Collect moves from all books (is this useful ? I doubt) */
	public static final int COLLECT_ALL            = 1;


	/** when a random move is chosen ...    */
	/** let the OpeningBook implementation choose a move    */
	public static final int SELECT_IMPLEMENTATION   = 0x0000;
	/** chose move based on number of games
	 *  disregard results statistics
	 */
	public static final int SELECT_GAME_COUNT   = 0x0001;
	/** choose move based on the result percentage
	 *  (if available)
	 */
	public static final int SELECT_RESULT_RATIO     = 0x0002;
	/** choose move based on draw ratio */
	public static final int SELECT_DRAW_RATIO           = 0x0004;
	/** choose a move randomly, with equal probability  */
	public static final int SELECT_EQUAL           = 0x0008;


	/** a list of Opening Books (of various content and file formats)
	 *  List<BookFileEntry>
	 * */
	/** how to use the book when playing against an engine  */
	public int engineMode = PREFER_GUI_BOOK;
	public int collectMode = COLLECT_ALL;
	public int selectMode = SELECT_IMPLEMENTATION;

	protected Random random = new Random();

	protected BookEntry.BookEntryComparator sort = new BookEntry.BookEntryComparator(selectMode,true);

	public OpeningLibrary()
	{ }

	public void open(UserProfile profile, Config config)
	{
		close();

		engineMode = profile.getInt("book.engine",PREFER_GUI_BOOK);
		collectMode = profile.getInt("book.collect",COLLECT_ALL);
		selectMode = profile.getInt("book.select",SELECT_IMPLEMENTATION);

		//  get files and selection bitflags
		File[] files = (File[]) profile.get("book.files");
		boolean[] isopen = (boolean[]) profile.get("book.isopen");
		boolean openfirst = false;

		if (files==null) {
			//  factory settings
			Enumeration elems = config.enumerateElements("BOOK");
			while (elems.hasMoreElements())
			{
				Element elem = (Element)elems.nextElement();
				BookFile fentry = new BookFile(elem);

				add(fentry);
				if (!openfirst) openfirst = fentry.open();
			}
		}
		else {
			//  user settings
			for (int i=0; i < files.length; i++)
			{
				BookFile fentry = new BookFile(files[i],Application.theApplication.theConfig);

					add(fentry);
				if (isopen[i]) fentry.open();
			}
		}
	}

	public Object remove(int index)
	{
		BookFile entry =  (BookFile) super.remove(index);
		try {
			if (entry!=null && entry.book!=null)
				entry.book.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entry;
	}


	public void close()
	{
		try {
			for (int i=0; i < size(); i++)
				closeBook(i);
		} finally {
			clear();
		}
	}


	public int indexOf(File file)
	{
		for (int i=0; i < size(); i++)
		{
			BookFile fentry = (BookFile) get(i);
			if (file.equals(fentry.file)) return i;
		}
		return -1;
	}


	public List collectMoves(Position pos, boolean ignoreColors, boolean allowOutOfBook)
			throws IOException
	{
		ArrayList result = new ArrayList();
		for (int i=0; i < size(); i++)
		{
			BookFile fentry = (BookFile) get(i);
			if (fentry.book==null) continue;

			ArrayList one_result = new ArrayList();
			boolean containsPosition = fentry.book.getBookMoves(pos, ignoreColors, one_result);
			if (!containsPosition && !allowOutOfBook)
				continue;   //  transpose from out-of-book into the book. Ignore !

			if (collectMode == COLLECT_FIRST)
				return one_result;
			else
				mergeResult(result,one_result);
		}

		sort.selectMode = this.selectMode;
		sort.turnWhite = pos.whiteMovesNext();
		Collections.sort(result, this.sort);

		return result;
	}


	public BookEntry selectMove(Position pos, boolean ignoreColors, boolean turnWhite)
			throws IOException
	{
		if (selectMode==SELECT_IMPLEMENTATION)
			for (int i=0; i < size(); i++)
			{
				BookFile fentry = (BookFile) get(i);
				if (fentry.book==null) continue;

				ArrayList one_result = new ArrayList();
				BookEntry entry = fentry.book.selectBookMove(pos,ignoreColors,random);
				if (entry!=null) return entry;
			}

		List moves = collectMoves(pos,ignoreColors, false);
		return selectMove(moves, selectMode,turnWhite,random);
	}

	public BookEntry selectMove(List moves, boolean turnWhite)
	{
		return selectMove(moves, this.selectMode, turnWhite, this.random);
	}

	public BookEntry selectMove(List moves, int selectMode, boolean turnWhite, Random random)
	{
		if (moves.isEmpty()) return null;

		double[] scores = new double[moves.size()];

		for (int i=0; i < moves.size(); i++)
		{
			BookEntry entry = (BookEntry)moves.get(i);
			scores[i] = entry.score(selectMode,turnWhite);
		}

		return selectMove(moves, scores, random);
	}

	public BookEntry selectMove (List moves, double[] scores, Random random)
	{
		if (moves.isEmpty()) return null;

		double totalScore = 0.0;
		for (int i=0; i < scores.length; i++)
			totalScore += scores[i];

		totalScore = random.nextDouble() * totalScore;
		// note: random.nextDouble() in [0..1]

		for (int i=0; i < scores.length; i++)
		{
			totalScore -= scores[i];
			if (totalScore <= 0.0)
				return (BookEntry) moves.get(i);
		}

		return (BookEntry) moves.get(moves.size()-1);
	}
/*
	public boolean addBook(File file)
	{
		int i = indexOf(file);
		if (i >= 0)
		{
			BookFileEntry fentry = (BookFileEntry) books.get(i);
			if (open(fentry))
				return true;
		}

		BookFileEntry fentry = new BookFileEntry(file);
		books.add(fentry);
		return open(fentry);
	}

	public BookFileEntry removeBook(int index)
	{
		return (BookFileEntry) books.remove(index);
	}

	public void moveBook(int fromIndex, int toIndex)
	{
		BookFileEntry fentry = removeBook(fromIndex);
		books.add(toIndex,fentry);
	}
*/

	public boolean openBook(int index)
	{
		BookFile fentry = (BookFile)get(index);
		return fentry.open();
	}

	public void closeBook(int index)
	{
		BookFile fentry = (BookFile)get(index);
		fentry.close();
	}


	public void store(UserProfile profile)
	{
		profile.set("book.engine", engineMode);
		profile.set("book.collect", collectMode);
		profile.set("book.select", selectMode);

		File[] files = new File[size()];
		boolean[] isopen = new boolean[size()];

		for (int i=0; i < size(); i++)
		{
			BookFile fentry = (BookFile) get(i);
			files[i] = fentry.file;
			isopen[i] = fentry.isOpen();
		}

		profile.set("book.files",files);
		profile.set("book.isopen",isopen);
	}


	public void setEntries(Vector entries)
	{
		this.clear();
		this.addAll(entries);
	}


	private void mergeResult(List result, List add)
	{
outer:
		for (int i=0; i < add.size(); i++)
		{
			BookEntry new_bentry = (BookEntry) add.get(i);
			for (int j=0; j < result.size(); j++)
			{
				BookEntry old_bentry = (BookEntry)result.get(j);
				if (old_bentry.move.equals(new_bentry.move))
				{
					result.set(j, BookEntry.merge(old_bentry,new_bentry));
					continue outer;
				}
			}
			//  otherwise
			result.add(new_bentry);
		}
	}

	public static Vector shallowClone(Vector orig)
	{
		Vector result = new Vector(orig.size());
		for (int i=0; i < orig.size(); i++)
		{
			BookFile bf = (BookFile) orig.elementAt(i);
			result.add(bf);
}
		return result;
	}
}
