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

import de.jose.task.GameIterator;
import de.jose.view.list.IntervalCacheModel;
import de.jose.view.list.IDBTableModel;
import de.jose.db.JoConnection;
import de.jose.db.Row;

import java.sql.SQLException;

/**
 * ResultSetIterator
 * 
 * @author Peter Schäfer
 */

public class ResultSetIterator
        extends GameIterator
{
	protected IntervalCacheModel model;
	protected int currentRow;

	public ResultSetIterator(IntervalCacheModel model, JoConnection dbConnection)
	{
		super(dbConnection);
		this.model = model;
		this.currentRow = 0;
	}

	public boolean hasNext() throws SQLException
	{
		return currentRow < model.getRowCount();
	}

	public void next(GameHandler callback) throws Exception
	{
		int GId = model.getDBId(currentRow);
		if (GId==0) {
			//  may not have been fetched, yet; what shall we do ??
			model.getRowAt(currentRow);
		}

		if (GId > 0) super.handle(GId,callback);
		currentRow++;
	}

	public void close()	{	}
}