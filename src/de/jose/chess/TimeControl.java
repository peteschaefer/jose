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

package de.jose.chess;

import de.jose.Language;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class TimeControl
		implements Serializable, Cloneable
{
	static final long serialVersionUID = 1805100694773209005L;

	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------
	
	public static final int ALL_MOVES		= 0;
	public static final int REMAINING_MOVES	= 0;
	
	public static final long SECOND		= 1000L;
	public static final long MINUTE		= SECOND*60;
	public static final long HOUR		= MINUTE*60;
	
	/**	Factory Settings
	 */
	public static Vector FACTORY_SETTINGS = new Vector();
	
	static {
		FACTORY_SETTINGS.add(new TimeControl("time.control.blitz", ALL_MOVES, 5*MINUTE));
		FACTORY_SETTINGS.add(new TimeControl("time.control.rapid", ALL_MOVES, 15*MINUTE));
		FACTORY_SETTINGS.add(new TimeControl("time.control.fischer", ALL_MOVES, 3*MINUTE, 10*SECOND));
		FACTORY_SETTINGS.add(new TimeControl("time.control.tournament", 40, 2*HOUR).add(REMAINING_MOVES, 30*MINUTE));
	}
	
	//-------------------------------------------------------------------------------
	//	Inner Class
	//-------------------------------------------------------------------------------
	
	public class Record implements Serializable
	{
		/**	number of moves per phase
		 *	0 = all remaining moves
		 */
		public int moves;	
		/**	time in milliseconds
		 */
		public long millis;
		/**	increment per move in milliseconds
		 */
		public long increment;
		
		public Record(int mvs, long mills, long inc)
		{
			moves = mvs;
			millis = mills;
			increment = inc;
		}
	};
	
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------
	
	/**	display name
	 */
	protected String name;
	/**	user-assigned name
	 */
	protected String userName;
	/**	Vector of Record
	 */
	protected Vector recs;
	
	
	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public TimeControl(String aName)
	{
		name = aName;
		recs = new Vector();
	}
	
	public TimeControl(String name, 
					   int moves, long millis)
	{
		this(name,moves,millis,0L);
	}
	
	public TimeControl(String name, 
					   int moves, long millis, long increment)
	{
		this(name);
		add(moves,millis,increment);
	}

	private TimeControl(TimeControl copy)
	{
		this(copy.name);
		this.userName = copy.userName;
		for (int i=0; i<copy.countPhases(); i++)
		{
			Record rec = copy.getPhase(i);
			recs.add(new Record(rec.moves,rec.millis,rec.increment));
		}
	}

	public Object clone() {
		return new TimeControl(this);
	}

	//-------------------------------------------------------------------------------
	//	Basic Access
	//-------------------------------------------------------------------------------
	
	public final void setUserName(String aName)	{ userName = aName; }
	
	public final String getName()			{ return name; }

	public final void setName(String name)	{ this.name = name; }

	public final String getDisplayName()
	{
		if (userName!=null)
			return userName;
		else
			return Language.get(name);
	}

	public final String getDefaultDisplayName()
	{
		return Language.get(name);
	}

	public final String getToolTip()		{ return Language.getTip(name); }

	public final String toString()			{ return getDisplayName(); }
	
	public final TimeControl add(int moves, long millis)
	{
		return add(moves,millis,0L);
	}
	
	public final TimeControl add(int moves, long millis, long increment)
	{
		recs.add(new Record(moves,millis,increment));
		return this;
	}
	
	public final int countPhases()			{ return recs.size(); }
	
	public final Record getPhase(int n)		{ return (Record)recs.get(n); }
	
	public final int getMoves(int n)		{ return getPhase(n).moves; }		
	public final long getMillis(int n)		{ return getPhase(n).millis; }		
	public final long getIncrementMillis(int n)	{ return getPhase(n).increment; }

	public final Date getTime(int n)
	{
		long millis  = getMillis(n);
		if (millis==0L)
			return null;
		else
			return new Date(millis);
	}

	public final Date getIncrement(int n)
	{
		long millis = getIncrementMillis(n);
		if (millis==0L)
			return null;
		else
			return new Date(millis);
	}

	public void setPhaseCount(int count)
	{
		if (count < recs.size())
			recs.setSize(count);
		else while (count > recs.size())
			recs.add(new Record(ALL_MOVES,5*MINUTE,0L));
	}

	public void setMoves(int phase, int moves) {
		getPhase(phase).moves = moves;
	}

	public void setMillis(int phase, long millis) {
		getPhase(phase).millis = millis;
	}

	public void setIncrementMillis(int phase, long millis) {
		getPhase(phase).increment = millis;
	}

	public void setTime(int phase, Date time) {
		getPhase(phase).millis = time.getTime();
	}

	public void setIncrement(int phase, Date time) {
		getPhase(phase).increment = time.getTime();
	}

	public void reset(Clock clk)
	{
		clk.reset(getMillis(0), getMillis(0));
	}

	public void resetTime(Clock clk)
	{
		clk.reset(getMillis(0), getMillis(0), -1);
	}

    public int getPhaseFor(int moveNo)
    {
        for (int i=0; i < countPhases(); i++)
        {
            Record r = getPhase(i);
            if (r.moves==0 || moveNo <= r.moves)
                return i;
            //else
            moveNo -= r.moves;
        }
        return countPhases()-1;
    }

	/**
	 * @param moveNo 1-based
	 * @return
	 */
    public int movesToGo(int moveNo)
    {
        for (int i=0; i < countPhases(); i++)
        {
            Record r = getPhase(i);
            if (r.moves==0)
                return -1;   //  unlimited
            if (moveNo <= r.moves)
                return r.moves-moveNo+1;
            moveNo -= r.moves;
        }
        return -1;
    }

	/**	update clocks after move n	(startin at 1)
	 */
	public void update(Clock clk, int n, int color)
	{
		for (int i=0; n>=0; )
		{
			Record r = getPhase(i);
			if (n==0) {
				switch (color) {
				case Constants.WHITE:		clk.addWhiteTime(r.millis); clk.updateClocks(); break;
				case Constants.BLACK:		clk.addBlackTime(r.millis); clk.updateClocks(); break;
				}
			}
			if (r.moves==0 || n <= r.moves) {
				switch (color) {
				case Constants.WHITE:		clk.addWhiteTime(r.increment); clk.updateClocks(); break;
				case Constants.BLACK:		clk.addBlackTime(r.increment); clk.updateClocks(); break;
				}
			}
			if (r.moves==0) break;
			//	else: n > r.moves
			n -= r.moves;
			if (i < (countPhases()-1)) i++;
			//	last phase may be repeated
		}
	}
}
