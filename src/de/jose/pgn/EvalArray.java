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

package de.jose.pgn;

import de.jose.plugin.Score;
import de.jose.util.FloatArray;
import de.jose.util.IntArray;
import de.jose.chess.EngUtil;
import de.jose.plugin.AnalysisRecord;
import de.jose.Util;

/**
 * Maintains an array of position evaluations
 * @author Peter Sch�fer
 */

public class EvalArray {
	/**
	 * the array (2 W-D values for each ply)
	 */
	protected FloatArray values;
	/**
	 * offset of first ply
	 */
	protected int firstPly;

	public EvalArray(int first) {
		values = new FloatArray(2 * 120);
		firstPly = first;
		//adjustMax = ADJUST_LOW_HIGH;
		clear();
	}

	public int firstPly() {
		return firstPly;
	}

	public int plyCount() {
		return firstPly + values.size() / 2;
	}


	public int firstMove() {
		return firstPly / 2;
	}

	public int moveCount() {
		return (plyCount() + 1) / 2;
	}


	public void clear() {
		values.clear();
		firstPly = 0;
	}

	public void setGame(Game gm) {
		clear();

		int ply = firstPly = gm.getPosition().firstPly();
		MoveNode node = gm.getMainLine().firstMove();
		for (; node != null; node = node.nextMove())
			if (EvalArray.isValid(node.engineValue))
				setPlyValue(ply++, node.engineValue);
			else
				setPlyValue(ply++, null);
	}

	public float[] plyValue(int ply, float[] result) {
		if (ply < firstPly)
			return null;
		ply -= firstPly;
		if ((2*ply+1) >= values.size())
			return null;

		if (result == null)
			result = new float[2];
		result[0] = values.get(2 * ply);
		result[1] = values.get(2 * ply + 1);
		return result;
	}

	public float[] moveValue(int move, int color, float[] result) {
		if (EngUtil.isWhite(color))
			return plyValue(2 * move, result);
		else
			return plyValue(2 * move + 1, result);
	}

	public static boolean isValid(float[] value)
	{
		return value!=null && (value[0]!=Float.MAX_VALUE) && (value[1]!=Float.MAX_VALUE);
	}

	public static boolean equals(float[] a, float[] b) {
		if (!isValid(a))
			return !isValid(b);
		else if (!isValid(b))
			return false;
		else
			return a[0]==b[0] && a[1]==b[1];
	}

	public static boolean isValid(float v1, float v2)
	{
		return (v1!=Float.MAX_VALUE) && (v2!=Float.MAX_VALUE);
	}

	public void setPlyValue(int ply, float[] value) {
		if (ply < firstPly) throw new ArrayIndexOutOfBoundsException(ply + " < " + firstPly);
		ply -= firstPly;

		if (value==null) {
			values.set(2*ply,Float.MAX_VALUE);
			values.set(2*ply+1,Float.MAX_VALUE);
		}
		else {
			values.set(2 * ply, value[0]);
			values.set(2 * ply + 1, value[1]);
		}
	}

	public void setMoveValue(int move, int color, float[] value) {
		if (EngUtil.isWhite(color))
			setPlyValue(2 * move, value);
		else
			setPlyValue(2 * move + 1, value);
	}
}

