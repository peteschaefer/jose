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

import java.io.IOException;


public interface InputListener
{
	
	public void readLine(char[] chars, int offset, int len) throws IOException;

	public void readEOF() throws IOException;

	public void readError(Throwable ex) throws IOException;
}
