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

/**
 * keeps track of the player's time
 *
 */

import de.jose.MessageProducer;

public class Clock
		extends MessageProducer
		implements Constants
{

    //-------------------------------------------------------------------------------
	//	inner class Tick
	//-------------------------------------------------------------------------------

    /**
     * ticks each second and sends an update event
     * to registered listeners
	 *
	 * TODO think about using java.util.Timer or javax.swing.Timer
	 * (but I don't think it's more precise)
	 *
     */
    private class Tick extends Thread
 	{
		Tick () {
			super("jose.clock-tick");
			setDaemon(true);
		}

        private boolean finished = false;

        public void destroy() {
            finished = true;
            interrupt();
        }

        public void run() {
            while(!finished) {
                long time = System.currentTimeMillis();
                updateClocks(time, this, true);

                long sleepTime;
                switch (getCurrent()) {
                default:		sleepTime = Long.MAX_VALUE; break;
                case WHITE:		sleepTime = (getWhiteTime()%1000); break;
                case BLACK:		sleepTime = (getBlackTime()%1000); break;
                }

                if (sleepTime<0) sleepTime = 1000-Math.abs(sleepTime);
                if (sleepTime<100) sleepTime += 1000;	//	accounts for inacurate system clock
                try {
                    sleep(sleepTime);
                } catch (InterruptedException ex) {
                    //	keep on ...
                }
            }
        }
    }

	/**	event constants	 */
    /** clock times have been updated   */
	public static final int EVENT_UPDATE_CURRENT	= 1;
	/** both clock times have been updated   */
	public static final int EVENT_UPDATE_BOTH		= 2;
    /** state has changed */
	public static final int EVENT_STATE				= 3;
	/**	time has elapsed	*/
	public static final int TIME_ELAPSED			= 10;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------
	
	/**	time (in milliseconds) for white and black	 */
	protected long whiteTime;
	protected long blackTime;
	/**	when was the clock update for the last time ?	 */
	protected long lastUpdate;
	
	/**	which clock is running ? */
	protected int current;
    /** background thread   */
    protected Tick tick;
	
	public Clock()
	{
		whiteTime = 0;
		blackTime = 0;
		current = 0;
        tick = new Tick();
        tick.start();
	}
	
	public final long getWhiteTime()			{ return whiteTime; }
	public final long getBlackTime()			{ return blackTime; }

	public final void setWhiteTime(long millis)	{ whiteTime = millis; }
	public final void setBlackTime(long millis)	{ blackTime = millis; }

	public final void addWhiteTime(long millis)	{ whiteTime += millis; }
	public final void addBlackTime(long millis)	{ blackTime += millis; }
	
	public final int getCurrent()				{ return current; }
	
	public final void setCurrent(int newColor) {
		boolean state_change = (current != newColor);
        updateClocks(System.currentTimeMillis(),!state_change);

        if (state_change) {
			current = newColor;
 			sendMessage(EVENT_STATE);
            tick.interrupt();   //  will eventually send an update event, too
		}
	}
	
	public final void halt()					{ setCurrent(0); }
	public final void startWhite()				{ setCurrent(WHITE); }
	public final void startBlack()				{ setCurrent(BLACK); }
	public final void toggle()					{ setCurrent(current ^ COLORS); }

    public final boolean isRunning()            { return current != 0; }

	public void updateClocks() {
		updateClocks(System.currentTimeMillis(),null,true);
	}
	
	public void updateClocks(long time, boolean notify) {
		updateClocks(time,null,notify);
	}
	
	public void updateClocks(long time, Object who, boolean notify) {
		long elapsed = time-lastUpdate;
		if (elapsed==0) return;
		
		lastUpdate = time;
		long oldTime;

		switch (current) {
		case WHITE:		oldTime = whiteTime;
						whiteTime -= elapsed;
						if (whiteTime <= 0 && oldTime > 0)
							sendMessage(TIME_ELAPSED);
						break;
		case BLACK:		oldTime = blackTime;
						blackTime -= elapsed;
						if (blackTime <= 0 && oldTime > 0)
							sendMessage(TIME_ELAPSED);
						break;
		}

        if (notify)
		    sendMessage(EVENT_UPDATE_CURRENT,who);
	}
/*
    public void sendMessage(int what, Object who)
    {
        super.sendMessage(what,who);
        switch (what) {
        case EVENT_UPDATE:  System.out.print("update "); break;
        case EVENT_STATE:   System.out.print("state  "); break;
        }
        switch (current) {
        case WHITE: System.out.println(" ["+whiteTime+"]  "+blackTime); break;
        case BLACK: System.out.println("  "+whiteTime+"  ["+blackTime+"]"); break;
        default:    System.out.println("  "+whiteTime+"   "+blackTime+" "); break;
        }
    }
*/
	public void reset(long white, long black)
	{
		reset(white,black,0);
	}

	public void reset(long white, long black, int color)
	{
		whiteTime = white;
		blackTime = black;
		lastUpdate = System.currentTimeMillis();
		if (color >= 0) setCurrent(color);
		sendMessage(EVENT_UPDATE_BOTH,this);
	}
}
