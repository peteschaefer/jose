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

import de.jose.pgn.Collection;
import de.jose.task.GameSource;
import de.jose.task.MaintenanceTask;

import java.sql.SQLException;

/**
 * moves a set of games or collections
 */

public class MoveTask
			extends MaintenanceTask
{
	/**	target collection	*/
	protected int targetCId;
	/**	adjust OId settings ?	*/
	protected boolean setOId;
	protected boolean calcIdx;
	//	skip system folders
	protected boolean skipSystem;

	public MoveTask(GameSource src, int CId,
					boolean adjustOId, boolean calcIdx, boolean noSystem) throws Exception
	{
		super("Move",true);
		setSource(src);
		targetCId = CId;
		setOId = adjustOId;
		this.calcIdx = calcIdx;
		skipSystem = noSystem;
	}

	public void processGame(int GId) throws SQLException
	{
		gutil.moveGame(GId,targetCId, setOId,calcIdx);
	}

	public void processGames(int[] GIds, int from, int to) throws SQLException
	{
		gutil.moveGames(GIds,from,to, targetCId,setOId,calcIdx);
	}

	public void processCollection(int CId) throws SQLException
	{
		if (skipSystem && Collection.isSystem(CId)) return;
		gutil.moveCollection(CId,targetCId,setOId);
	}

	public void processCollectionContents(int CId) throws SQLException
	{
		gutil.moveCollectionContents(CId,targetCId,setOId,calcIdx);
	}
}
