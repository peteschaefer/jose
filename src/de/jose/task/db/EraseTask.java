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

import java.sql.SQLException;

/**
 * erase (= actually delete) a set of games and collections
 */

public class EraseTask
			extends MaintenanceTask
{
	public EraseTask(GameSource src) throws Exception
	{
		super("Erase",true);
		setSource(src);
	}

	public void processGame(int GId) throws SQLException
	{
        int[] GIds = { GId };
        gutil.eraseGames(GIds,0,1);
	}

	public void processGames(int[] GIds, int from, int to) throws SQLException
	{
		gutil.eraseGames(GIds,from,to);
	}

	public void processCollection(int CId) throws SQLException
	{
		gutil.eraseCollectionContents(CId);
		gutil.eraseCollectionRow(CId);
	}

	public void processCollectionContents(int CId) throws SQLException
	{
		gutil.eraseCollectionContents(CId);
		gutil.zeroCollectionCount(CId);
	}
}

