/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.task;

import de.jose.pgn.Collection;

import java.sql.SQLException;

abstract public class GameTask
        extends DBTask
{

	protected GameSource source;

	public int processedGames;
	public int totalGames;

	/**
	 *	create a Task that automatically allocates a new database connection,
	 *	and closes it upon completion
	 */
	public GameTask(String name, boolean autoCommit) throws Exception
	{
		super(name,autoCommit);
	}

	public void setSource(GameSource src)
	{
		source = src;
		silentTime = 1000;
		pollProgress = 1000;
	}

	/*	@return the approximate progress state (0.0 = just started, 1.0 = finished),
			< 0 if unknown
			should be thread-safe
	*/
	public double getProgress()
	{
		return (double)processedGames/totalGames;
	}

	public int init() throws Exception
	{
		int result = super.init();
		totalGames = source.size();
		processedGames = 0;
		return result;
	}

	public int size() {
		return totalGames;
	}

	/**
	 * @deprecated use GameSource.count() instead
	 * @return
	 * @throws SQLException
	 * /
	public int countSelectedGames()
		throws SQLException
	{
		if (source.isNotSet())
			return 0;

		if (source.isSingleGame())
			return 1;

		if (source.isGameSelection()) {
			DBSelectionModel select = source.getSelection();
			int i = select.getMinSelectionIndex();
			int i1 = select.getMaxSelectionIndex();
			int total = 0;

			for ( ; i <= i1; i++)
				if (select.isSelectedIndex(i))
					total++;
			return total;
		}

		if (source.isCollectionSelection()) {
			DBSelectionModel select = source.getSelection();
			int i = select.getMinSelectionIndex();
			int i1 = select.getMaxSelectionIndex();
			int total = 0;

			for ( ; i <= i1; i++)
				if (select.isSelectedIndex(i)) {
					int id = select.getDBId(i);
					total += Collection.countGames(connection,id);
				}
			return total;
		}

		if (source.isSingleCollection() || source.isCollectionContents())
			return Collection.countGames(connection,source.getId());

		if (source.isGameArray())
			return source.getIds().length;

		if (source.isCollectionArray())
		{
			int total = 0;
			int[] CIds = source.getIds();
			for (int i=0; i<CIds.length; i++)
				total += Collection.countGames(connection,CIds[i]);
			return total;
		}

		throw new IllegalStateException();
	}
	 */
}
