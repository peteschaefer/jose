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

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.io.File;


/**
 * this behaviour waits for the user to press the F3 key
 * the call JoCanvas3D.capture(): the next frame will be stored on disk
 */
public class CaptureBehavior 
		extends Behavior 
{ 
	private WakeupCriterion wakeup;
	private JoCanvas3D canvas;
	
	public CaptureBehavior(JoCanvas3D canv) { 
		super(); 
		canvas = canv;
	} 

	// Sets timer to zero and registers the wakeup condition. 
	public void initialize() { 
		wakeup = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
		wakeupOn(wakeup); 
	} 

	// Print frame rate and re-register wakeup condition. 
	public void processStimulus (Enumeration criteria) { 
		if (criteria.hasMoreElements())
		{
			WakeupOnAWTEvent crit = (WakeupOnAWTEvent)criteria.nextElement();
			AWTEvent[] evts = crit.getAWTEvent();
			KeyEvent kevt = (KeyEvent)evts[0];
			
			if (kevt.getKeyCode()==KeyEvent.VK_F3)
			{
				canvas.capture((File)null);
				canvas.repaint();
			}
		}
		wakeupOn(wakeup); 
	} 
} 
