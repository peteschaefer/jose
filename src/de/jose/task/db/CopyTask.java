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
 * copies a set of games or collections
 */

public class CopyTask
			extends MaintenanceTask
{
	/**	target collection	*/
	protected int targetCId;
	/** adjust Game.Idx after copy ?    */
	protected boolean calcIdx;


	public CopyTask(GameSource src, int CId, boolean calcIdx) throws Exception
	{
		super("Copy",true);
		setSource(src);
		targetCId = CId;
		this.calcIdx = calcIdx;
	}


	public void processGame(int GId) throws SQLException
	{
		gutil.copyGame(GId,targetCId,calcIdx);
	}

	public void processGames(int[] GIds, int from, int to) throws SQLException
	{
		gutil.copyGames(GIds,from,to,targetCId,calcIdx);
	}

	public void processCollection(int CId) throws SQLException
	{
		int newCId = gutil.copyCollectionRow(CId,targetCId);
		gutil.copyCollectionContents(CId,newCId,calcIdx);
		//	no diff change
	}

	public void processCollectionContents(int CId) throws SQLException
	{
		gutil.copyCollectionContents(CId,targetCId,calcIdx);
	}
}
