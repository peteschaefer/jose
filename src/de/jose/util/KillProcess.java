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

/**
 * shutdown hook for killing running processes
 * (only necessary when the JVM shuts down uncontrolledly)
 *
 * @author Peter Schäfer
 */

public class KillProcess extends Thread
{
	protected Process process;
	protected boolean done = false;

	public KillProcess(Process process) {
		this.process = process;
	}

	public void run() {
		try {
			if (!done) process.destroy();
			done = true;
		} catch (Throwable e) {
		}
	}
}
