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

package de.jose.pgn;

import de.jose.util.IntArray;
import de.jose.chess.EngUtil;
import de.jose.plugin.AnalysisRecord;
import de.jose.Util;

/**
 * Maintains an array of position evaluations
 * @author Peter Schäfer
 */

public class EvalArray
{
	/** the array (one value for each ply)  */
	protected IntArray values;
	/** offset of first ply */
	protected int firstPly;
	/** positive maximum    */
	protected int posMax;
	/** negative maximum    */
	protected int negMax;
	/** max was just modified */
	protected boolean maxDirty;
	/** adjust max values ? */
	protected int adjustMax;

	public static final int ADJUST_NONE     = 0;
	public static final int ADJUST_HIGH     = 1;
	public static final int ADJUST_LOW      = 2;
	public static final int ADJUST_LOW_HIGH = 3;


	public EvalArray(int first)
	{
		values = new IntArray(120);
		firstPly = first;
		adjustMax = ADJUST_LOW_HIGH;
		clear();
	}

	public int firstPly()
	{
		return firstPly;
	}

	public int plyCount()
	{
		return firstPly+values.size();
	}


	public int firstMove()
	{
		return firstPly/2;
	}

	public int moveCount()
	{
		return (plyCount()+1)/2;
	}


	public void clear()
	{
		values.clear();
		firstPly = 0;
		posMax = 0;
		negMax = 0;
		maxDirty = false;
	}

	public void setGame(Game gm)
	{
		clear();

		int ply = firstPly = gm.getPosition().firstPly();
		MoveNode node = gm.getMainLine().firstMove();
		for ( ; node != null; node = node.nextMove())
			setPlyValue(ply++, node.getEngineValue());
	}

	public void setAdjustMax(int flags)
	{
		adjustMax = flags;
	}

	public int plyValue(int ply)
	{
		if (ply < firstPly)
			return 0;
		ply -= firstPly;
		if (ply >= values.size())
			return 0;
		else
			return values.get(ply);
	}

	public int moveValue(int move, int color)
	{
		if (EngUtil.isWhite(color))
			return plyValue(2*move);
		else
			return plyValue(2*move+1);
	}

	public int moveValue(int move)
	{
		int whiteValue = plyValue(2*move);
		int blackValue = plyValue(2*move+1);

		if (whiteValue==0)
			return blackValue;
		else
			return whiteValue;
	}


	public int setPlyValue(int ply, int value)
	{
		if (ply < firstPly) throw new ArrayIndexOutOfBoundsException(ply+" < "+firstPly);
		ply -= firstPly;

		int oldValue = (ply < values.size()) ? values.get(ply) : 0;
		boolean wasPosMax = (oldValue==posMax);
		boolean wasNegMax = (oldValue==negMax);

		if (!isValid(value))
			;
		else if (value > posMax && Util.allOf(adjustMax,ADJUST_HIGH)) {
			posMax = value;
			maxDirty = true;
		}
		else if (value < negMax && Util.allOf(adjustMax,ADJUST_HIGH)) {
			negMax = value;
			maxDirty = true;
		}

		values.set(ply,value);

		if ((wasPosMax && Util.allOf(adjustMax,ADJUST_LOW) && (value < oldValue || !isValid(value))) ||
		    (wasNegMax && Util.allOf(adjustMax,ADJUST_LOW) && (value > oldValue || !isValid(value))))
			calcMax();

		return oldValue;
	}

	public int setMoveValue(int move, int color, int value)
	{
		if (EngUtil.isWhite(color))
			return setPlyValue(2*move,value);
		else
			return setPlyValue(2*move+1,value);
	}

	public int getMaximum()         { return posMax; }

	public int getMinimum()         { return negMax; }

	public boolean isMaxDirty()     { return isMaxDirty(true); }

	public boolean isMaxDirty(boolean reset)
	{
		boolean result = maxDirty;
		if (reset) maxDirty = false;
		return result;
	}

	private static boolean isValid(int value)
	{
		if (value <= AnalysisRecord.UNKNOWN) return false;
		if (value >= AnalysisRecord.WHITE_MATES) return false;
		if (value <= AnalysisRecord.BLACK_MATES) return false;

		return true;
	}

	private void calcMax()
	{
		int newPosMax = 0;
		int newNegMax = 0;

		for (int i=values.size()-1; i >= 0; i--)
		{
			int value = values.get(i);
			if (!isValid(value)) continue;

			if (value > newPosMax) newPosMax = value;
			if (value < newNegMax) newNegMax = value;
		}

		maxDirty = (newPosMax!=posMax) || (newNegMax!=negMax);
		posMax = newPosMax;
		negMax = newNegMax;
	}
}
