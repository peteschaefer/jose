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

package de.jose.util;

import de.jose.Version;

/**
 *
 * @author Peter Schäfer
 */

public class ProcessUtil
{
	/**
     * The minimum priority that a process can have.
     */
	public final static int MIN_PRIORITY 	= -2;
   /**
     * The default priority that is assigned to a process.
     */
    public final static int NORM_PRIORITY 	=  0;
    /**
     * The maximum priority that a process can have.
     */
    public final static int MAX_PRIORITY 	= +2;


	public static void setPriority(Process proc, int priority)
	{
		if (priority < MIN_PRIORITY) priority = MIN_PRIORITY;
		if (priority > MAX_PRIORITY) priority = MAX_PRIORITY;

		if (Version.windows)
		switch (priority) {
		case -2:		WinUtils.setPriorityClass(proc,WinUtils.IDLE_PRIORITY_CLASS); break;
				
		case -1:		if (Version.winNTfamily)
							WinUtils.setPriorityClass(proc,WinUtils.BELOW_NORMAL_PRIORITY_CLASS);
						else
							WinUtils.setPriorityClass(proc,WinUtils.IDLE_PRIORITY_CLASS);
						break;

		case  0:		WinUtils.setPriorityClass(proc,WinUtils.NORMAL_PRIORITY_CLASS); break;

		case +1:		if (Version.winNTfamily)
							WinUtils.setPriorityClass(proc,WinUtils.ABOVE_NORMAL_PRIORITY_CLASS);
						else
							WinUtils.setPriorityClass(proc,WinUtils.HIGH_PRIORITY_CLASS);
						break;

		case +2:		WinUtils.setPriorityClass(proc,WinUtils.HIGH_PRIORITY_CLASS); break;
		}

		//	else: not yet implemented for UNIXes
	}


}
