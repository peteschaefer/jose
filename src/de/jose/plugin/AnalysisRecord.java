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

package de.jose.plugin;

import de.jose.Util;
import de.jose.book.BookEntry;
import de.jose.chess.Move;
import de.jose.util.StringUtil;

import java.util.ArrayList;
import java.util.BitSet;

import static de.jose.plugin.Score.UNKNOWN;

public class AnalysisRecord
{
	/** Plugin returns current move */
	public static final int	CURRENT_MOVE  		    = 0x000100;
	/** Plugin returns current move no. */
	public static final int	CURRENT_MOVE_NO		    = 0x000200;
	/** Plugin returns elapsed time */
	public static final int	ELAPSED_TIME			= 0x000400;
	/** Plugin returns node count */
	public static final int	NODE_COUNT			    = 0x000800;
	/** Plugin returns node count per second */
	public static final int	NODES_PER_SECOND  	    = 0x001000;
	/** Plugin returns search depth */
	public static final int	DEPTH					= 0x002000;
	/** Plugin returns selective search depth */
	public static final int	SELECTIVE_DEPTH		    = 0x004000;
	/** Plugin returns position evaluation */
	public static final int	EVAL					= 0x008000;
	/** Plugin returns general info */
	public static final int	INFO					= 0x010000;
	/** reset content for next move */
	public static final int NEW_MOVE                = 0x800000;

	/**	value for depth = move was evaluated from book	*/
	public static final int BOOK_MOVE		= -1;
	/**	value for depth = move was evaluated from hash table	*/
	public static final int HASH_TABLE		= -2;
	/**	value for depth = move was evaluated from endgame table	*/
	public static final int ENDGAME_TABLE	= -3;

	/** engine state: THINKING,ANALYZING,PONDERING
	 *  (ponder PVs are a bit different than others)
	 * */
	public EngineState  engineMode;
	/** current game ply */
	public int      ply;
	public boolean	white_next;
	/** current move    */
	public String   currentMove;
	/** ponder move (if available)  */
	public String   ponderMove;
	/** current move no.    */
	public int      currentMoveNo;
	/**	search depth (plies)	*/
	public int		depth;
	/**	selective search depth (plies)	*/
	public int 		selectiveDepth;
	/**	elapsed time (in milliseconds)	*/
	public long 	elapsedTime;
	/**	number of processed nodes	*/
	public long 	nodes;
	/**	number of processed nodes per second	*/
	public long 	nodesPerSecond;

	/**	position evaluation in centipawns etc.	*/
    public static class LineData {
		public Score eval = new Score();	// score
		public ArrayList<Move> moves; // parsed moves
		public StringBuffer line;	// line text
		public StringBuffer info;	// additional info
		public BookEntry book;
		// .. todo more to come (reference links)

		void clear() {
			eval.clear();
			if (line!=null) line.setLength(0);
			if (info!=null) info.setLength(0);
			if (moves!=null) moves.clear();
			book = null;
		}
	}
	public LineData[] data;
	//	todo move struct-of-arrays to an array of structs

	/** general info    */
	public String   info;
	/** time-to-live for info string. will be deleted after this time   */
	public long     info_ttl;

	/** fields that were just modified  */
	public int      modified;
	/** PVs that where just modified    */
	public BitSet pvmodified;
	/** max. number of used PVs */
	public int      maxpv;

	public AnalysisRecord()
	{
		data = new LineData[256];
		for(int i=0; i<256; i++)
			data[i] = new LineData();
		pvmodified = new BitSet(256);
		maxpv = 0;
	}

	public StringBuffer getLine(int pv)
	{
		if (pv >= maxpv) maxpv = pv+1;
		if (data[pv].line==null) data[pv].line = new StringBuffer();
		return data[pv].line;
	}

	public StringBuffer getLineInfo(int pv)
	{
		if (pv >= maxpv) maxpv = pv+1;
		if (data[pv].info==null) data[pv].info = new StringBuffer();
		return data[pv].info;
	}

	public ArrayList<Move> getMoves(int pv)
	{
		if (pv >= maxpv) maxpv = pv+1;
		if (data[pv].moves==null) data[pv].moves = new ArrayList<Move>();
		return data[pv].moves;
	}

	public boolean wasModified(int set)
	{
		return Util.anyOf(modified,set);
	}

	public boolean wasPvModified(int pv)
	{
		return pvmodified.get(pv);
	}

	public boolean wasPvModified() {
		for(int i=0; i < maxpv; ++i)
			if (wasPvModified(i)) return true;
		return false;
	}

	public void setPvModified(int pv)
	{
		if (pv >= maxpv) maxpv = pv+1;
		pvmodified.set(pv);
	}

	public void clearPvModified(int pv) {
		pvmodified.clear(pv);
	}

	public void clearPvModified()
	{
		pvmodified.clear();
	}

	public int findPvMoveIdx(Move mv)
	{
		for(int i=0; i < maxpv; ++i)
		{
			if (data[i].moves!=null && !data[i].moves.isEmpty() && mv.equals(data[i].moves.get(0)))
				return i;
		}
		return -1;
	}

	public void addMoveInfo(int i, String info)
	{
		Score sc = data[i].eval;
		StringBuffer liinfo = getLineInfo(i);
		liinfo.setLength(0);
		liinfo.append("  {");
		liinfo.append(info);
		if (sc!=null && sc.moves_left > 0) {
			liinfo.append(", ml: ");
			liinfo.append(sc.moves_left);
		}
		liinfo.append("}");
		setPvModified(i);
	}

	public void clear()
	{
		currentMove = null;
		currentMoveNo = -1;
		ply = UNKNOWN;
		white_next = false;
		depth = UNKNOWN;
		selectiveDepth = UNKNOWN;
		elapsedTime = UNKNOWN;
		nodes = UNKNOWN;
		nodesPerSecond = UNKNOWN;
		for (int i=0; i<maxpv; i++)
			data[i].clear();
		info = null;
		modified = -1;  //  all fields modified
		pvmodified.set(0,256);
	}

	public void reset()
	{
		clear();
		maxpv = 0;
		modified = 0;
		clearPvModified();
	}

	public Score findScore(Move mv)
	{
		for(int i=0; i < maxpv; ++i) {
			if (data[i].moves!=null && !data[i].moves.isEmpty()
					&& mv.equals(data[i].moves.get(0)))
				return data[i].eval;
		}
		return null;
	}

	public static long parseLong(char[] chars, int offset, int len)
	{
		if (offset >= chars.length) return 0L;
		if ((offset+len) > chars.length) len = chars.length-offset;
		return StringUtil.parseLong(chars,offset,len);
	}

	public static int parseInt(char[] chars, int offset, int len)
	{
		if (offset >= chars.length) return 0;
		if ((offset+len) > chars.length) len = chars.length-offset;
		return StringUtil.parseInt(chars,offset,len);
	}

	public static String substring(char[] chars, int offset, int len)
	{
		if (offset >= chars.length) return "";
		if ((offset+len) > chars.length) len = chars.length-offset;
		return new String(chars,offset,len);
	}
}

