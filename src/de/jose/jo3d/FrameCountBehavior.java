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
import javax.media.j3d.WakeupOnElapsedFrames;
import java.util.Enumeration;

/**
 * this behaviour counts the number of rendered frames
 * and writes the result to std out
 *
 * @deprecated
 */
public class FrameCountBehavior 
		extends Behavior 
{ 
	private int frameIncr; 
	private int frameCount = 0;
	private WakeupCriterion wakeup; 
//	private long lastMS = 0; 
	
	public FrameCountBehavior(int frames) { 
		super(); 
		frameIncr = frames;
	} 

	// Sets timer to zero and registers the wakeup condition. 
	public void initialize() { 
		wakeup = new WakeupOnElapsedFrames(frameIncr); 
//		lastMS = System.currentTimeMillis(); 
		wakeupOn(wakeup); 
	} 

	// Print frame rate and re-register wakeup condition. 
	public void processStimulus (Enumeration criteria) { 
		frameCount += frameIncr; 
//		long et = System.currentTimeMillis() - lastMS; 
//		System.err.println((double)frameIncr * 1000.d/(double)et); 
//		lastMS = System.currentTimeMillis(); 
		if (getView() != null) {
			long[] startTimes = new long[frameIncr];
			getView().getFrameStartTimes(startTimes);
		
			int k = startTimes.length;
			while (k>0 && startTimes[k-1]==0) k--;
				
			if (k > 0) {
				long now = System.currentTimeMillis();
				System.err.println((double)k * 1000.0d / (double)(now-startTimes[k-1]));
			}
		}
		
		wakeupOn(wakeup); 
	} 
} 
