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

package de.jose.plugin;

import de.jose.Util;
import de.jose.util.StringUtil;

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

	/**	engine (current color) mates in ... moves (+plies to mate)	*/
	public static final int WHITE_MATES	    = +30000;
	/**	engine gets mated in ... moves (-plies to mate)	*/
	public static final int BLACK_MATES	    = -30000;
	/**	evaluation is not known	*/
	public static final int UNKNOWN		    = -36000;
	
	/**	value for depth = move was evaluated from book	*/
	public static final int BOOK_MOVE		= -1;
	/**	value for depth = move was evaluated from hash table	*/
	public static final int HASH_TABLE		= -2;
	/**	value for depth = move was evaluated from endgame table	*/
	public static final int ENDGAME_TABLE	= -3;

	/** evaluation flags:   this evaluation is exact (default)    */
	public static final int EVAL_EXACT              = 0x00;
	/** evaluation flags:   this evaluation is a lower bound    */
	public static final int EVAL_LOWER_BOUND        = 0x01;
	/** evaluation flags:   this evaluation is an upper bound    */
	public static final int EVAL_UPPER_BOUND        = 0x02;
	/** evaluation flags:   this evaluation is book game count   */
	public static final int EVAL_GAME_COUNT         = 0x03;

	/** engine state: THINKING,ANALYZING,PONDERING
	 *  (ponder PVs are a bit different than others)
	 * */
	public int      engineMode;
	/** current game ply */
	public int      ply;
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

	/**	position evaluation in centipawns	*/
	public int[]	eval;
	/** evaluation flags: (exact,lower,upper bound) */
	public int[]    evalFlags;
	/**	the primary variations	*/
	public StringBuffer[] line;

	/** general info    */
	public String   info;
	/** time-to-live for info string. will be deleted after this time   */
	public long     info_ttl;

	/** fields that were just modified  */
	public int      modified;
	/** PVs that where just modified    */
	public long[]   pvmodified;
	/** max. number of used PVs */
	public int      maxpv;

	public AnalysisRecord()
	{
		eval = new int[256];
		evalFlags = new int[256];
		line = new StringBuffer[256];
		pvmodified = new long[4];   // = 256 bits; one for each pv
		maxpv = 0;
	}

	public StringBuffer getLine(int pv)
	{
		if (pv > maxpv) maxpv = pv;
		if (line[pv]==null) line[pv] = new StringBuffer();
		return line[pv];
	}

	public boolean wasModified(int set)
	{
		return Util.anyOf(modified,set);
	}

	public boolean wasPvModified(int pv)
	{
		return (pvmodified[pv>>6] & (1<<(pv&0x3f))) != 0L;
	}

	public void setPvModified(int pv)
	{
		if (pv > maxpv) maxpv = pv;
		pvmodified[pv>>6] |= 1<<(pv&0x3f);
	}

	public void clearPvModified()
	{
		for (int i=0; i < pvmodified.length; i++)
			pvmodified[i] = 0L;
	}

	public void clear()
	{
		currentMove = null;
		currentMoveNo = -1;
		ply = UNKNOWN;
		depth = UNKNOWN;
		selectiveDepth = UNKNOWN;
		elapsedTime = UNKNOWN;
		nodes = UNKNOWN;
		nodesPerSecond = UNKNOWN;
		for (int i=0; i<=maxpv; i++) {
			eval[i] = UNKNOWN;
			evalFlags[i] = EVAL_EXACT;
		}
		for (int i=0; i<=maxpv; i++) if (line[i]!=null) line[i].setLength(0);
		info = null;
		modified = -1;  //  all fields modified
	}

	public void reset()
	{
		clear();
		maxpv = 0;
		modified = 0;
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

