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

import de.jose.db.JoConnection;
import de.jose.db.JoStatement;
import de.jose.db.ParamStatement;
import de.jose.db.JoPreparedStatement;
import de.jose.util.IntArray;
import de.jose.util.ListUtil;
import de.jose.util.SoftCache;
import de.jose.util.StringUtil;
import de.jose.util.map.IntHashMap;
import de.jose.pgn.Game;
import de.jose.Util;
import de.jose.Application;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * CollectionIterator
 * 
 * @author Peter Schäfer
 */

public class CollectionIterator
        extends GameIterator
{
	/** current result set  */
	protected JoStatement stm;
	protected String sql;
	protected ResultSet resultSet;
	protected boolean hasNext;
	protected IntArray GIds;
	/** prepared */
	protected JoPreparedStatement prepStatement;

	protected String handlerName;
	protected static int gHandlerCount = 1;


	protected static int BLOCK_SIZE = 128;

	public CollectionIterator(int CId, JoConnection dbConnection) throws SQLException
	{
		super(dbConnection);
		GIds = new IntArray();
		open(CId);
	}


	public boolean hasNext() throws SQLException
	{
		return hasNext;
	}

	public void next(GameHandler callback) throws Exception
	{
		callback.handleRow(resultSet);
		//  goto next
		hasNext = resultSet.next();
		if (!hasNext)
			fetchNextBlock();
	}

	protected void open(int CId) throws SQLException
	{
		handlerName = "CollectionIteratorRead"+(gHandlerCount++);
		StringBuffer buf = new StringBuffer();

		/// create a prep statement for fetching actual game data
		buf.setLength(0);
		buf.append("SELECT ");
		buf.append(Game.DISPLAY_SELECT);
		buf.append(" FROM ");
		buf.append(Game.DISPLAY_FROM);
		buf.append(" WHERE ");
		buf.append(Game.DISPLAY_WHERE);
		buf.append(" AND Game.Id IN (");
		StringUtil.appendParams(buf,BLOCK_SIZE);
		buf.append(")");

		prepStatement = dbConnection.getPreparedStatement(buf.toString());

		dbConnection.executeUpdate("HANDLER Game OPEN AS "+handlerName+" ");

		stm = new JoStatement(dbConnection);
		//  fetch first block
		buf.setLength(0);
		buf.append("HANDLER "+handlerName);
		buf.append(" READ Game_16 = ("+CId+") ");
		buf.append(" WHERE CId = "+CId+" ");
		buf.append(" LIMIT "+BLOCK_SIZE);
		sql = buf.toString();

       	stm.executeQuery(sql);
		//  INDEX Game_16 ON Game(CId,Idx,Id)
		stm.selectIntArray(GIds,1);
		fetchBlock();

		//  modify statement for next fetches
		buf.setLength(0);
		buf.append("HANDLER "+handlerName+" READ Game_16 NEXT ");
		buf.append(" WHERE CId = "+CId+" ");
		buf.append(" LIMIT "+BLOCK_SIZE);
		sql = buf.toString();
	}

	protected void fetchNextBlock() throws SQLException
	{
		GIds.clear();
		stm.executeQuery(sql);
		stm.selectIntArray(GIds,1);
		fetchBlock();
	}

	protected void fetchBlock() throws SQLException
	{
		//  fetch all games from GIds
		if (GIds==null || GIds.isEmpty()) {
			hasNext = false;
			return;
		}

		//  fill in parameters
		int count = GIds.size();
		for (int i=0; i < count; i++)
			prepStatement.setInt(i+1, GIds.get(i));
		for (int i=count; i < BLOCK_SIZE; i++)
			prepStatement.setInt(i+1, 0);

		prepStatement.execute();
		resultSet = prepStatement.getResultSet();

		hasNext = (resultSet!=null) && resultSet.next();
	}

	public void close()
	{
		try {
			dbConnection.executeUpdate("HANDLER "+handlerName+" CLOSE");
		} catch (SQLException e) {
			Application.error(e);
		}
		stm.close();
	}

	protected static class SelectionIterator extends NestedIterator
	{
		DBSelectionModel sel;
		int current;

		SelectionIterator(DBSelectionModel sel, JoConnection dbConnection) throws SQLException
		{
			super(dbConnection);
			this.sel = sel;
			this.current = sel.getMinSelectionIndex();
			fetchNext();
		}

		GameIterator nextSubIterator() throws SQLException
		{
			while (current <= sel.getMaxSelectionIndex())
				if (sel.isSelectedIndex(current))
					return new CollectionIterator(sel.getDBId(current++), this.dbConnection);
				else
					current++;
			return null;
		}

		public void close()            {		}
	}

	protected static class ArrayIterator extends NestedIterator
	{
		Iterator i;

		ArrayIterator(Object obj, JoConnection dbConnection) throws SQLException
		{
			super(dbConnection);
			i = ListUtil.iterator(obj);
			fetchNext();
		}

		GameIterator nextSubIterator() throws SQLException
		{
			if (!i.hasNext()) {
				int CId = Util.toint(i.next());
				return new CollectionIterator(CId, this.dbConnection);
			}
			else
				return null;
		}

		public void close()            {		}
	}
}