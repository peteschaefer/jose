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

import de.jose.chess.Move;
import de.jose.plugin.Score;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * IBookEntry
 *
 * @author Peter Sch�fer
 */
public class BookEntry
{
	// -------- Constants ---------------------------------------------------------

	public static final int IUNKNOWN = Integer.MIN_VALUE;
	public static final double FUNKNOWN = Double.MIN_VALUE;

	public static final int VERY_GOOD_MOVE  = 200;
	public static final int GOOD_MOVE = 100;
	public static final int NEUTRAL_MOVE    = 0;
	public static final int BAD_MOVE    = -100;
	public static final int VERY_BAD_MOVE = -200;

	// -------- Fields --------------------------------------------------------------

	/** the move    */
	public Move move;
	/** total number of games where the move was played */
	public int count = IUNKNOWN;
	/** number of games that where won by black */
	public int countBlack = IUNKNOWN;
	/** number of games that where drawb */
	public int countDraw = IUNKNOWN;
	/** number of games that where won by white */
	public int countWhite = IUNKNOWN;
	/** true if the book position has reversed colors */
	public boolean isTransposedColor = false;
	/** learned value */
	public double learnValue = FUNKNOWN;
	/** user assigned value */
	public double userValue = FUNKNOWN;
	/** top game from Lichess Opening Explorer */
	public ArrayList<GameRef> gameRefs = null;

	// -------- Comparators ---------------------------------------------------------

	public static class BookEntryComparator implements Comparator
	{
		public int selectMode;
		public boolean turnWhite;

		public BookEntryComparator(int selectMode, boolean turnWhite)
		{
			this.selectMode = selectMode;
			this.turnWhite = turnWhite;
		}

		public int compare(Object o1, Object o2)
		{
			BookEntry b1 = (BookEntry)o1;
			BookEntry b2 = (BookEntry)o2;

			//  sort DESCENDING
			double result = b2.score(selectMode,turnWhite) - b1.score(selectMode,turnWhite);
			if (result > 0.0)
				return +1;
			else if (result < 0.0)
				return -1;
			else
				return 0;
		}
	}

	public static Comparator SORT_BY_GAME_COUNT =
			new BookEntryComparator(OpeningLibrary.SELECT_GAME_COUNT, false);
	public static Comparator SORT_BY_WHITE_RESULT =
			new BookEntryComparator(OpeningLibrary.SELECT_RESULT_RATIO, true);
	public static Comparator SORT_BY_BLACK_RESULT =
			new BookEntryComparator(OpeningLibrary.SELECT_RESULT_RATIO, false);
	public static Comparator SORT_BY_DRAW_RATIO =
			new BookEntryComparator(OpeningLibrary.SELECT_DRAW_RATIO, false);

	// -------- Methods ---------------------------------------------------------

	public static BookEntry merge(BookEntry entry1, BookEntry entry2)
	{
		BookEntry merged_entry = new BookEntry();
		merged_entry.move = entry1.move;
		merged_entry.add(entry1);
		merged_entry.add(entry2);
		return merged_entry;
	}

	public BookEntry()                       { }

	public int centipawnValue(	)
	{
		if (userValue==FUNKNOWN)
			return 0;
		else
			return (int)Math.round(userValue);
	}

	public float[] mappedValue(float[] result)
	{
		if (count==0 || (countWhite+countDraw+countBlack)==0)
			return null;

		if (result==null)
			result = new float[2];

		if (countWhite==IUNKNOWN || countDraw==IUNKNOWN || countBlack==IUNKNOWN) {
			//	at least have some decent displayable value
			result[0] = result[1] = Float.MAX_VALUE;	// invalid
		}
		else {
			result[0] = (float) countWhite / count;
			result[1] = (float) countDraw / count;
		}
		return result;
	}

	protected void add(BookEntry that)
	{
		if (! this.move.equals(that.move))
			throw new IllegalArgumentException();

		this.count = add(this.count, that.count);
		this.countWhite = add(this.countWhite, that.countWhite);
		this.countDraw = add(this.countDraw, that.countDraw);
		this.countBlack = add(this.countBlack, that.countBlack);
		this.isTransposedColor = this.isTransposedColor && that.isTransposedColor;
		this.learnValue = first(this.learnValue, that.learnValue);
		this.userValue = first(this.userValue, that.userValue);
	}


	public double score(int selectMode, boolean turnWhite)
	{
		double score;
		switch (selectMode)
		{
		default:
		case OpeningLibrary.SELECT_GAME_COUNT:
			score = nvl(count);
			break;
		case OpeningLibrary.SELECT_RESULT_RATIO:
			if (turnWhite) {
				score = nvl(countWhite)+BookEntry.nvl(countDraw)/2;
				score /= nvl(count);
			}
			else {
				score = nvl(countBlack)+BookEntry.nvl(countDraw)/2;
				score /= nvl(count);
			}
			break;
		case OpeningLibrary.SELECT_DRAW_RATIO:
			score = BookEntry.nvl(countDraw);
			score /= BookEntry.nvl(count);
			break;
		case OpeningLibrary.SELECT_EQUAL:
			score = 1.0;
			break;
		}
		return score;
	}

	public void toScore(Score score, int cap)
	{
		score.flags = Score.EVAL_GAME_COUNT;
		score.cp = score.cp_current = BookEntry.nvl(count);
		score.win = (countWhite != IUNKNOWN) ? countWhite : 0;
		score.draw = (countDraw != IUNKNOWN) ? countDraw : 0;
		score.lose = (countBlack != IUNKNOWN) ? countBlack : 0;
		if (count > cap && score.hasWDL()) {
			//	scale wdl down to 1000; do not display huge WDL counts (entry.count is already huge)
			score.win = score.win * cap / count;
			score.draw = score.draw * cap / count;
			score.lose = score.lose * cap / count;
		}
	}

	public static int add(int a, int b)
	{
		if (a==IUNKNOWN) return b;
		if (b==IUNKNOWN) return a;
		return a+b;
	}

	public static double first(double a, double b)
	{
		if (a==FUNKNOWN)
			return b;
		else
			return a;
	}

	public static double first(int a, int b)
	{
		if (a==IUNKNOWN)
			return b;
		else
			return a;
	}

	public static int nvl(int a)
	{
		if (a==IUNKNOWN)
			return 0;
		else
			return a;
	}

	public static double nvl(double a)
	{
		if (a==FUNKNOWN)
			return 0;
		else
			return a;
	}

}
