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

package de.jose.task;

import de.jose.task.db.GameUtil;
import de.jose.util.IntArray;
import de.jose.util.ListUtil;
import de.jose.util.map.IntIntMap;
import de.jose.pgn.Game;

import java.util.Iterator;

abstract public class MaintenanceTask
		extends GameTask
{
	/**
	 * max. number of objects to process at once
	 * this relates to the max. number of entries of a SQL " IN (..)" clause
	 * (1024 is the limit for Oracle)
	 *
	 * set to 0 to disable buld processing
	 */
	protected int bulkSize = 1024;

	/**
	 * diffs for Collection.GameCount
	 */
	protected GameUtil gutil;


	/**
	 *	create a Task that automatically allocates a new database connection,
	 *	and closes it upon completion
	 */
	public MaintenanceTask(String name, boolean autoCommit) throws Exception
	{
		super(name,autoCommit);
		setSilentTime(2*1000);
		gutil = GameUtil.newGameUtil(connection);
		pollProgress = 1000;
	}


	public void setBulkSize(int sz)			{ bulkSize = sz; }
	public void disableBulkProcessint()		{ setBulkSize(0); }


	public int init() throws Exception
	{
		super.init();
		setWaitCursor();

		gutil.clearDiffs();
		prepare();

		if (source.isGame())
			prepareGame();
		else if (source.isCollectionContents())
			prepareContents();
		else if (source.isCollection())
			prepareCollection();

		broadcastOnUpdate(getName());
		return RUNNING;
	}

	public int done(int state)
	{
		int result = super.done(state);
		broadcastAfterUpdate(0);
		setDefaultCursor();
		return result;
	}

	/**	@return text to show inside the progress bar
	 */
	public String getProgressText()
	{
		return null;
	}

	/**
	 *	do a chunk of work
	 *	@return RUNNING, SUCCESS, FAILURE, or ERROR
	 *  */
	public int work()
		throws Exception
	{
		super.work();

		if (source.isSingleGame()) {
			//	process one single game
			processGame(source.getId());
		}
		else if (source.isGameArray()) {
			int[] ids = source.getIds();
			if (bulkSize<=1) {
				//	process game array one-by-one
				for (int i=0; i<ids.length; i++) {
					processGame(ids[i]);
					if (isAbortRequested()) break;
				}
			}
			else {
				//	bulk process array
				for (int i=0; i<ids.length; i += bulkSize) {
					processGames(ids, i, Math.min(ids.length, i+bulkSize));
					if (isAbortRequested()) break;
				}
			}
		}
		else if (source.isSingleCollection()) {
			//	process one single collection
			processCollection(source.getId());
		}
		else if (source.isCollectionArray()) {
			//	process array of collections (no bulk processing)
			int[] ids = source.getIds();
			for (int i=0; i < ids.length; i++) {
				processCollection(ids[i]);
				if (isAbortRequested()) break;
			}
		}
		else if (source.isCollectionContents()) {
			//	process contents of one collection
			processCollectionContents(source.getId());
		}
		else if (source.isGameSelection()) {
			DBSelectionModel select = source.getSelection();
			if (bulkSize <= 1) {
				//	process selection of games one-by-one
				for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
					if (select.isSelectedIndex(i)) {
						processGame(select.getDBId(i));
						if (isAbortRequested()) break;
					}
			} else {
				//	process selection of games bulk-wise
				IntArray array = new IntArray(bulkSize+1);
				for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
					if (select.isSelectedIndex(i)) {
						array.add(select.getDBId(i));
						if (array.size() >= bulkSize) {
							processGames(array.getArray(), 0,bulkSize);
							array.clear();
							if (isAbortRequested()) break;
						}
					}
				if (!array.isEmpty() && !isAbortRequested())
					processGames(array.getArray(), 0,array.size());
			}
		}
		else if (source.isCollectionSelection()) {
			//	process selection of collections
			DBSelectionModel select = source.getSelection();
			for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
				if (select.isSelectedIndex(i)) {
					processCollection(select.getDBId(i));
					if (isAbortRequested()) break;
				}
		}
		else if (source.isObject()) {
			processGame(source.getObject());
		}
		else if (source.isList()) {
			Iterator i = source.getIterator();
			while (i.hasNext()) {
				Game gm = (Game)i.next();
				processGame(gm);
				if (isAbortRequested()) break;
			}
		}
		else if (source.isResultSet()) {
			throw new UnsupportedOperationException();
		}
		else
			throw new IllegalStateException();

		gutil.applyDiffs();

		if (source.isSingleGame() || source.isGameSelection() || source.isGameArray())
			finishGame();
		else if (source.isCollectionContents())
			finishContents();
		else if (source.isSingleCollection() || source.isCollectionSelection() || source.isCollectionArray())
			finishCollection();
		else if (source.isObject())
			finishObject();
		else
			throw new IllegalStateException();

		return finish();
	}

	public void prepare() throws Exception							{	/* override */ }

	public void prepareGame() throws Exception						{	/* override */ }

	public void prepareCollection() throws Exception				{	/* override */ }

	public void prepareContents() throws Exception					{	/* override */ }


	abstract public void processGame(int GId) throws Exception;

	abstract public void processGames(int[] GId, int from, int to) throws Exception;

	abstract public void processCollection(int CId) throws Exception;

	abstract public void processCollectionContents(int CId) throws Exception;

	public void processGame(Game game) throws Exception
	{
		/** optional operation; only used by exporters  */
		throw new UnsupportedOperationException();
	}


	public void finishGame() throws Exception						{	/* override */ }

	public void finishCollection() throws Exception					{	/* override */ }

	public void finishContents() throws Exception					{	/* override */ }

	public void finishObject() throws Exception                     {   /* override */ }

	public int finish() throws Exception							{ return SUCCESS; }

}
