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

package de.jose.view;

import de.jose.AbstractApplication;
import de.jose.Command;
import de.jose.MessageProducer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * animates a game
 */
public class Animation
        extends MessageProducer
		implements ActionListener
{
    /** inner thread */
    private Timer timer;

    /** message sent when state changes */
    public static final int EVENT_STARTED   = 1;
    public static final int EVENT_PAUSED    = 2;
    public static final int EVENT_STOPPED   = 3;
    public static final int EVENT_MOVED     = 4;

    public Animation(long speed)
    {
        timer = new Timer((int)speed,this);
//		timer.start();
		timer.setRepeats(true);
		timer.setCoalesce(false);
    }

    public long getSpeed() {
        return timer.getDelay();
    }

    public void setSpeed(long millisecondsPerMove) {
		if (millisecondsPerMove!=getSpeed()) {
			timer.setDelay((int)millisecondsPerMove);
			if (timer.isRunning())
				timer.restart();
		}
    }

    public boolean isRunning()
    {
        return timer.isRunning();
    }

	public void start(long initialSleep)
	{
		timer.setInitialDelay((int)initialSleep);
		start();
	}

    public void start()
    {
       	timer.start();
        sendMessage(EVENT_STARTED);
    }

    public void pause()
    {
       	timer.stop();
    }

    public void stop()
    {
        if (timer.isRunning()) {
			timer.stop();
            sendMessage(EVENT_STOPPED);
        }
    }

	/**	Timer callback	*/
	public void actionPerformed(ActionEvent e)
	{
		Command cmd = new Command("move.forward", null, null);
		AbstractApplication.theCommandDispatcher.handle(cmd,AbstractApplication.theAbstractApplication);
		sendMessage(EVENT_MOVED);
	}
}
