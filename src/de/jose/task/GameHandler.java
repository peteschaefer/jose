/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.task;

import de.jose.pgn.Game;
import de.jose.db.JoConnection;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.xml.sax.SAXException;

/**
 * GameHandler
 *
 * call back from GameIterator
 *
 * @author Peter Schäfer
 */

public interface GameHandler
{
	/** one game Object */
	public void handleObject(Game game);
	/** one game Row */
	public void handleRow(ResultSet res) throws Exception;
}