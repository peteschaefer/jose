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

package de.jose.jo3d;

import de.jose.Application;

import javax.media.j3d.Sound;
import javax.media.j3d.View;
import java.util.ArrayList;

/**
 * Note that View.stopView() must not be called from a behavior method
 * as a work-around, we run a parallel thread whose only purpose is to make this call
 *
 * TODO think about using javax.swing.Timer
 *
 * @author Peter Schäfer
 */

public class ViewController
           extends Thread
{
    /** the View to be controlled */
    private View view;
    /** counts the number of starts */
    private int startCount;
	private int stopRequest;
    /** acitve sounds */
    private ArrayList sounds;

    public ViewController()
    {
        view = null;
        startCount = 0;
        sounds = new ArrayList();

        setName("jose.3D-view-controller");
        setPriority(Thread.MAX_PRIORITY);
		setDaemon(true);
        start();
    }

    public synchronized void startView(View aview, boolean immediate)
    {
        startCount++;
        if (startCount > 0)
        {
            view = aview;
            if (immediate)
                checkState();
			else
                interrupt();
		}
    }

    public synchronized void stopView(View aview, boolean immediate)
    {
        if (startCount > 0) startCount--;
        if (startCount == 0)
        {
            view = aview;
            if (immediate)
                checkState();
            else {
				//	don't stop immediately but delay until another 2 frames are rendered !
				//	this makes sure that all changes in the scene are actually painted
				if (stopRequest < 1) stopRequest = 1;
			}
        }
    }

    public synchronized void doStopView(View aview, boolean immediate)
    {
        startCount = 0;
        view = aview;
        if (immediate)
            checkState();
        else
            interrupt();
    }

    private void checkState()
    {
        if (view!=null)    {
			if (startCount <= 0) {
				view.stopView();	//	note that this call is not immediately effective
                for (int i=0; i<sounds.size(); i++)
                    ((Sound)sounds.get(i)).setEnable(false);
				stopRequest = 0;
//				System.out.println("stop "+System.currentTimeMillis()%1000);
			}

            if (startCount > 0) {
				view.startView();
//				System.out.println("start "+System.currentTimeMillis()%1000);
			}
        }
    }

	//	call back from Canvas3D
	public void preRender()		{
//		System.out.println("pre "+System.currentTimeMillis()%1000);
		if (stopRequest > 0) stopRequest++;
	}

	public void postRender()	{
//		System.out.println("post "+System.currentTimeMillis()%1000);
		if (stopRequest > 0) stopRequest++;
	}

	public void postSwap()		{
//		System.out.println("swap "+System.currentTimeMillis()%1000);
		if (stopRequest >= 5)
			interrupt();
		else if (stopRequest > 0) stopRequest++;
	}

    public void addSound(Sound snd) {
        sounds.add(snd);
    }

    public void removeSound(Sound snd) {
        sounds.remove(snd);
    }

    public void run()
    {
        for (;;)
            try {
                checkState();
                sleep(Long.MAX_VALUE);
            } catch (InterruptedException iex) {
                //  that's OK
            } catch (Throwable ex) {
                Application.error(ex);
            }
    }

}
