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

package de.jose.task.db;

import de.jose.task.GameSource;
import de.jose.task.MaintenanceTask;
import de.jose.util.map.IntIntMap;

import java.sql.SQLException;

/**
 * restore a set of games (probably from the trash
 */
public class RestoreTask
        extends MaintenanceTask
{
	public RestoreTask(GameSource src) throws Exception
	{
		super("Restore",true);
		setSource(src);
	}

	public void processGame(int GId) throws SQLException
	{
		gutil.restoreGame(GId);
	}

	public void processGames(int[] GIds, int from, int to) throws SQLException
	{
		gutil.restoreGames(GIds,from,to);
	}

	public void processCollection(int CId) throws SQLException
	{
		gutil.restoreCollection(CId);
	}

	public void processCollectionContents(int CId) throws SQLException
	{
		/**
		 * this method is not needed.
		 * We restore either Collections, or a sets of Games.
		 */
	}
}
