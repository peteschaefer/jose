/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
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
import de.jose.chess.Position;

import java.awt.print.Book;
import java.util.*;
import java.io.File;
import java.io.IOException;

import de.jose.util.ListUtil;
import org.w3c.dom.Element;

import static de.jose.Application.AppMode.ANALYSIS;
import static de.jose.Application.AppMode.USER_INPUT;

/**
 * OpeningLibrary
 *
 * @author Peter Sch�fer
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

	protected Position pos = new Position();
	public Random random = new Random();

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
		File[] userFiles = (File[]) profile.get("book.files.2");
		boolean[] isopen = (boolean[]) profile.get("book.isopen");
		boolean openfirst = false;

		if (userFiles!=null) {
			//  user settings
			for (int i=0; i < userFiles.length; i++)
			{
				BookFile fentry = new BookFile(userFiles[i],Application.theApplication.theConfig);
				add(fentry);
				if (isopen[i]) openfirst = fentry.open();
			}
		}

		//	add all files from the factory settings that are missing
		//	in the user settings
		Enumeration elems = config.enumerateElements("BOOK");
		while (elems.hasMoreElements())
		{
			Element elem = (Element)elems.nextElement();
			BookFile fentry = new BookFile(elem);

			if (userFiles==null || !ListUtil.contains(userFiles,fentry.file)) {
				add(fentry);
				if (!openfirst) openfirst = fentry.open();
			}
		}
	}

	public String getTitle()
	{
		StringBuffer buf = new StringBuffer();
		for(int i=0; i < size(); i++) {
			BookFile bfile = (BookFile)get(i);
			if (bfile!=null && bfile.isOpen()) {
				if (buf.length() <= 60) {
					if (buf.length() > 0) buf.append(", ");
					buf.append(bfile.getTitle());
				}
				else {
					buf.append(", ...");
					break;
				}
			}
		}
		return buf.toString();
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

	public List<BookEntry> collectMoves(Position pos, String fen,
										boolean go_deep, boolean ignoreColors, boolean allowOutOfBook)
			throws IOException
	{
		if (pos==null) pos = this.pos;
		ArrayList<BookEntry> result = new ArrayList();
		for (int i=0; i < size(); i++)
		{
			BookFile fentry = (BookFile) get(i);
			if (fentry.book==null) continue;

			ArrayList one_result = new ArrayList();
			boolean containsPosition = fentry.book.getBookMoves(pos,fen, ignoreColors, go_deep, one_result);
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

	//	@deprecated use queryMove instead
/*
	public BookEntry selectMove(Position pos, int gameMode, boolean ignoreColors, boolean turnWhite)
			throws IOException
	{
		if (selectMode==SELECT_IMPLEMENTATION)
			for (int i=0; i < size(); i++)
			{
				BookFile fentry = (BookFile) get(i);
				if (fentry.book==null) continue;

				ArrayList one_result = new ArrayList();
				//	some engines can play from their own book
				BookEntry entry = fentry.book.selectBookMove(pos,ignoreColors,random);
				if (entry!=null) return entry;
			}

		//	else: play from application books
		boolean go_deep = (gameMode==USER_INPUT || gameMode==ANALYSIS);
		//	todo expensive; make asynch
		List moves = collectMoves(pos,null,go_deep,ignoreColors, false);
		return selectMove(moves, selectMode,turnWhite,random);
	}
*/
	public BookEntry selectMove(List<BookEntry> moves, int selectMode, boolean turnWhite, Random random)
	{
		if (moves.isEmpty()) return null;

		double[] scores = new double[moves.size()];

		for (int i=0; i < moves.size(); i++)
		{
			BookEntry entry = (BookEntry)moves.get(i);
			scores[i] = entry.score(selectMode,turnWhite);
		}

		if (random!=null)
			return selectMove(moves, scores, random);
		else
			return moves.get(ListUtil.maxIndex(scores));
	}


	private static double sum(double[] array) {
		double total = 0.0;
		for (double v : array) total += v;
		return total;
	}

	public BookEntry selectMove (List<BookEntry> moves, double[] scores, Random random)
	{
		if (moves.isEmpty()) return null;

		double selectedScore = random.nextDouble() * sum(scores);
		// note: random.nextDouble() in [0..1]

		int i=0;
		for (; i < Math.min(scores.length,moves.size()); i++)
		{
			selectedScore -= scores[i];
			if (selectedScore <= 0.0)
				return moves.get(i);
		}

		return moves.get(Math.min(i,moves.size()-1));
	}


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

		profile.set("book.files.2",files);
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
