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
import de.jose.db.JoStatement;
import de.jose.db.JoPreparedStatement;
import de.jose.view.style.JoStyleContext;
import de.jose.view.list.IntervalCacheModel;
import de.jose.chess.Position;
import de.jose.Application;
import de.jose.util.ListUtil;
import de.jose.util.IntArray;

import java.util.Iterator;
import java.sql.SQLException;

import org.xml.sax.SAXException;

/**
 * GameIterator
 *
 * provides methods for iterating all Games in a GameSource
 * 
 * @author Peter Schäfer
 */

abstract public class GameIterator
{
	protected JoConnection dbConnection;


	abstract public boolean hasNext() throws SQLException;

	abstract public void next(GameHandler callback) throws Exception;

	abstract public void close();


	public static GameIterator newGameIterator(GameSource source, JoConnection dbConnection) throws SQLException
	{
		if (source.isNotSet())
			throw new IllegalArgumentException();
		if (source.isSingleGame())      //  one single Game Id
			return new SingleIterator(source.data,dbConnection);
		if (source.isGameSelection())   //  selection of Game Ids
			return new SelectionIterator(source.getSelection(),dbConnection);
		if (source.isSingleCollection() || source.isCollectionContents())    //  ons single Collection Id
			return new CollectionIterator(source.getId(),dbConnection);
		if (source.isCollectionSelection())    //  selection of Collection Ids
			return new CollectionIterator.SelectionIterator(source.getSelection(),dbConnection);
		if (source.isCollectionArray())    //  array of Collection Ids
			return new CollectionIterator.ArrayIterator(source.getIds(),dbConnection);
		if (source.isGameArray())  //  array of Game Ids
			return new ArrayIterator(source.data,dbConnection);
		if (source.isObject())  //  one single Game object
			return new SingleIterator(source.data,dbConnection);
		if (source.isList())    //  array of Game objects
			return new ArrayIterator(source.data,dbConnection);
		if (source.isResultSet())   //  List Data Model
			return new ResultSetIterator((IntervalCacheModel)source.data, dbConnection);   //  TODO

		throw new IllegalArgumentException();
	}

	protected GameIterator(JoConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	protected void handle(Object obj, GameHandler callback) throws Exception
	{
		if (obj instanceof Game)
			callback.handleObject((Game)obj);
		else if (obj instanceof Number)
			handle(((Number)obj).intValue(), callback);
		else
			throw new IllegalArgumentException();
	}

	protected void handle(int GId, GameHandler callback) throws Exception
	{
		JoPreparedStatement pstm = dbConnection.getPreparedStatement(Game.DISPLAY_SQL); //  TODO unify with EXPORT_SQL
		pstm.setInt(1, GId);
		pstm.execute();
		if (pstm.next())
			callback.handleRow(pstm.getResultSet());
	}


	protected static class SelectionIterator extends GameIterator
	{
		DBSelectionModel sel;
		int current;

		SelectionIterator(DBSelectionModel sel, JoConnection dbConnection) {
			super(dbConnection);
			this.sel = sel;
			this.current = fetch(sel.getMinSelectionIndex());
		}

		private int fetch(int i) {
			for ( ; i <= sel.getMaxSelectionIndex(); i++)
				if (sel.isSelectedIndex(i)) return i;
			return -1;
		}

		public boolean hasNext()        { return current >= 0; }

		public void next(GameHandler callback) throws Exception
		{
			handle(sel.getDBId(current),callback);
			current = fetch(current+1);
		}

		public void close()             { sel = null; }
	}

	protected static class ArrayIterator extends GameIterator
	{
		Iterator i;

		ArrayIterator(Object array, JoConnection dbConnection)     {
			super(dbConnection);
			if (array instanceof Iterator)
				i = (Iterator)array;
			else if (ListUtil.isIteratable(array))
				i = ListUtil.iterator(array);
			else
				throw new IllegalArgumentException();
		}

		public boolean hasNext()        { return i.hasNext(); }

		public void next(GameHandler callback) throws Exception
		{
			handle(i.next(),callback);
		}

		public void close()             { i = null; }
	}

	protected static class SingleIterator extends GameIterator
	{
		Object obj;

		SingleIterator(Object obj, JoConnection dbConnection)		{
			super(dbConnection);
			this.obj = obj;
		};

		public boolean hasNext()		{ return obj!=null; }

		public void next(GameHandler callback)   throws Exception
		{
			handle(obj,callback);
			close();
		}

		public void close()             { obj = null; }
	}

	abstract protected static class NestedIterator extends GameIterator
	{
		GameIterator subit;

		NestedIterator(JoConnection connection) throws SQLException
		{
			super(connection);
		}

		public boolean hasNext() throws SQLException
		{
			return subit!=null;
		}

		public void next(GameHandler callback) throws Exception
		{
			subit.next(callback);
			if (!subit.hasNext())
				fetchNext();
		}


		protected void fetchNext() throws SQLException
		{
			do {
				subit = nextSubIterator();
			} while (subit!=null && !subit.hasNext());
		}

		abstract GameIterator nextSubIterator() throws SQLException;
	}
}