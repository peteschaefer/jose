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

package de.jose.task.db;

import de.jose.Language;
import de.jose.chess.MoveFormatter;
import de.jose.db.JoConnection;
import de.jose.db.ParamStatement;
import de.jose.db.JoPreparedStatement;
import de.jose.db.MySQLAdapter;
import de.jose.window.OnlineUpdate;
import de.jose.util.map.IntIntMap;
import de.jose.util.map.IntHashSet;
import de.jose.util.StringUtil;
import de.jose.util.Metaphone;
import de.jose.util.ClipboardUtil;
import de.jose.util.IntArray;
import de.jose.pgn.*;
import de.jose.task.io.PGNExport;
import de.jose.task.io.PGNImport;
import de.jose.task.GameSource;
import de.jose.chess.Position;
import de.jose.chess.Move;
import de.jose.Application;

import javax.swing.text.BadLocationException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.List;
import java.util.Iterator;

/**
 * @author Peter Sch�fer
 */

abstract public class GameUtil
{
	protected static boolean useNew;

	// ------------------------------------------------
	// Fields
	// ------------------------------------------------

	protected JoConnection connection;

	protected IntIntMap diff;


	// ------------------------------------------------
	//  private  Ctor
	// ------------------------------------------------

	protected GameUtil(JoConnection connection)
	{
		this.connection = connection;
		this.diff = null;   //  created on demand
	}

	// ------------------------------------------------
	//  public Ctor
	// ------------------------------------------------

	public static GameUtil newGameUtil(JoConnection connection) throws SQLException
	{
		useNew = StringUtil.compareVersion(connection.getDatabaseProductVersion(),"4.0.14") >= 0;
        //  MySQL 4.0.14 or later: INSERT ... SELECT can operate on the same table

		if (useNew)
			return new GameUtilNewImpl(connection);
		else
			return new GameUtilOldImpl(connection);
	}

	// ------------------------------------------------
	// Methods
	// ------------------------------------------------


	/**
	 * copy one game to another collection
	 *
	 * @param oldGId the game
	 * @param targetCId target collection
	 * @throws SQLException
	 */
	abstract public void copyGame(int oldGId, int targetCId, boolean calcIdx)
		throws SQLException;

	/**
	 * copy one collection (without contents)
	 *
	 * @param CId the collection
	 * @param targetCId the target parent collection
	 * @return the new Id
	 * @throws SQLException
	 */
	public int copyCollectionRow(int CId, int targetCId)
		throws SQLException
	{
		int newCId = Collection.getSequence(connection);
		String newPath = getPath(targetCId)+newCId+"/";

		Collection coll = Collection.readCollection(connection,CId);

		coll.Id = newCId;
		coll.PId = targetCId;
		coll.Path = newPath;
		coll.LastModified = new Timestamp(System.currentTimeMillis());
		coll.GameCount = 0; //  initially empty
		coll.insert(connection);

		return newCId;
	}

	/**
	 * copy the contents of a collection, including all children
	 * requires DB schemas IO and IO_Map
	 *
	 * @param CId the collection
	 * @param targetCId target collection
	 * @throws SQLException
	 */
	public void copyCollectionContents(int CId, int targetCId, boolean calcIdx)
		throws SQLException
	{
		copyCollectionGames(CId,targetCId,calcIdx);

	    /** get direct children */
	    String sql1 = "SELECT Id FROM Collection WHERE PId = ?";
	    JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	    stm1.setInt(1,CId);
	    stm1.execute();
	    while (stm1.next()) {
	        int oldCId = stm1.getInt(1);
	        int newCId = copyCollectionRow(oldCId,targetCId);
	        //  recurse:
	        copyCollectionContents(oldCId,newCId,calcIdx);
	    }
	}

	/**
	 * @param CId
	 * @param targetCId
	 * @throws SQLException
	 */
	abstract public void copyCollectionGames(int CId, int targetCId, boolean calcIdx)
	    throws SQLException;

	/**
	 * copy a set of games to another collection
	 *
	 * @param GIds array of Game Ids
	 * @param from
	 * @param to
	 * @param targetCId target collection
	 * @throws SQLException
	 */
	abstract public void copyGames(int[] GIds, int from, int to, int targetCId, boolean calcIdx)
		throws SQLException;

	/**
	 * TEST Game.Idx
	 * move one game to another collection
	 *
	 * @param GId the game
	 * @param targetCId target collection
	 * @param setOId set Game.OCId (original collection Id) ?
	 * @throws SQLException
	 */
	public void moveGame(int GId, int targetCId, boolean setOId, boolean calcIdx)
		throws SQLException
	{
		int CId = getCId(GId);
		if (CId==targetCId)
			return;

		addDiff(CId,-1);
		addDiff(targetCId,+1);

		/*
			move

			UPDATE Game
			SET CId = targetCId, OCId = CId, OIdx = Idx
			WHERE Id = ?
		*/
		ParamStatement sql = new ParamStatement();
		sql.update.append("Game");

		if (setOId)
			sql.set.append("OCId=CId,OIdx=Idx, ");
		sql.set.append("CId=?");
		sql.addIntParameter(targetCId);

		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId);
			sql.set.append(",Idx=?");
			sql.addIntParameter(newIdx);
		}

		sql.where.append("Id=?");
		sql.addIntParameter(GId);
		sql.execute(connection);
	}

	/**
	 * TEST adjust Game.Idx
	 * move a set of games to another collection
	 *
	 * @param GIds array of game Ids
	 * @param targetCId target collection
	 * @param setOId set Game.OCId (original collection Id) ?
	 * @throws SQLException
	 */
	public void moveGames(int[] GIds, int from, int to,
		                  int targetCId, boolean setOId, boolean calcIdx)
		throws SQLException
	{
		/*
			calculate diffs:

			SELECT CId, COUNT(*)
			FROM Game
			WHERE Id IN (...)
			GROUP BY CId
		*/
		ParamStatement sql = addDiffs(GIds,from,to, targetCId);

		/*	move:

			UPDATE Game
			SET CId = targetId, OCId = CId, OIdx = Idx
			WHERE Id IN (...)
		 */
		sql.select.setLength(0);
		sql.from.setLength(0);
		sql.group.setLength(0);
		//	keep the WHERE clause, keep parameters

		sql.update.append("Game");

		if (setOId)
			sql.set.append("OCId=CId, OIdx=Idx, ");
		sql.set.append("CId=?");
		sql.insertIntParameter(1,targetCId);

		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId);
			connection.executeUpdate("SET @newIdx="+newIdx);
			sql.set.append(", Idx=(@newIdx:=@newIdx+1)");
            sql.order.append("Idx,Id");
            /**  order must be precise  */
		}

		//	keep the WHERE clause

		sql.execute(connection);
	}

	/**
	 * move a collection
	 *
	 * @param CId the collection
	 * @param targetCId target collection
	 * @param setOId set Game.OCId (original collection Id) ?
	 * @throws SQLException
	 */
	public void moveCollection(int CId, int targetCId,
		                       boolean setOId)
		throws SQLException
	{
		if (CId==targetCId)
			return; //	that was easy

		if (targetCId!=0 && isAncestorOf(CId,targetCId)) {
			//	target is descendant, raise to sibling level
			int PId = getParent(CId);
			moveCollection(targetCId,PId,false);
		}

		/*
			UPDATE Collection
			SET PId = targetId, OPId = PId
			WHERE Id = ?
		*/
		ParamStatement sql = new ParamStatement();
		sql.update.append("Collection");
		if (setOId)
			sql.set.append("OPId=PId, ");
		if (targetCId==0) {
			sql.set.append("PId=null");
		}
		else {
			sql.set.append("PId=?");
			sql.addIntParameter(targetCId);
		}

		sql.where.append("Id=?");
		sql.addIntParameter(CId);

		sql.execute(connection);

        Collection.updatePath(connection,CId,true);
	}

	/**
     * TEST Game.Idx
	 * move the contents of a collection (games + child collections)
	 *
	 * @param CId the collection
	 * @param targetCId target collection
	 * @param setOId set Game.OCId (original collection Id) ?
	 * @throws SQLException
	 */
	public void moveCollectionContents(int CId, int targetCId, boolean setOId, boolean calcIdx)
		throws SQLException
	{
		if (CId==targetCId)
			return;

		if (isAncestorOf(CId,targetCId)) {
			//	target is descendant, raise to siblng level
			int PId = getParent(CId);
			moveCollection(targetCId,PId,false);
		}

		/*
			 move child collections:
		 */
        List children = Collection.readChildren(connection,CId);
        for (Iterator i = children.iterator(); i.hasNext(); )
        {
            Collection child = (Collection)i.next();
            moveCollection(child.Id, targetCId, setOId);
        }

		/*
			calculate diffs

			SELECT GameCount
			FROM Collection
			WHERE Id = ?
		 */
		int count = Collection.countGames(connection,CId);
		addDiff(CId,-count);
		addDiff(targetCId,+count);

		/*
			move child games

			UPDATE Game
			SET CId = targetCId, OCId = CId, OIdx = Idx
			WHERE CId = ?
		*/
		ParamStatement sql = new ParamStatement();
		sql.update.append("Game");
		if (setOId)
			sql.set.append("OCId=CId, OIdx=Idx, ");
		sql.set.append("CId=?");
		sql.addIntParameter(targetCId);

		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId);
			connection.executeUpdate("SET @newIdx="+newIdx);
			sql.set.append(",Idx = (@newIdx:=@newIdx+1)");
            sql.order.append("Idx,Id");
            /** order must be precise */
		}

		sql.where.append("CId=?");
		sql.addIntParameter(CId);
		sql.execute(connection);
	}

	/**
	 * erase (physically delete) a set of games
	 *
	 * @param GIds an array of game Ids
	 * @param from
	 * @param to
	 * @throws SQLException
	 */
	 public void eraseGames(int[] GIds, int from, int to)
		throws SQLException
	{
	    /*
	        calculate diffs:

	        SELECT CId, COUNT(*)
	        FROM Game
	        WHERE Id IN (...)
	        GROUP BY CId
	    */
	    ParamStatement sql = new ParamStatement();
	    sql.select.append("CId, COUNT(*)");
	    sql.from.append("Game");
	    sql.where.append("Id IN (?");
	    StringUtil.append(sql.where,",?",to-from-1);
	    sql.where.append(")");
	    for (int i=from; i<to; i++)
	        sql.addIntParameter(GIds[i]);
	    sql.group.append("CId");

	    addDiffs(-1, sql);


	    sql.select.setLength(0);
	    sql.from.setLength(0);
	    sql.group.setLength(0);
	    sql.delete.append(" ");

	    if (JoConnection.getAdapter().canCascadingDelete()) {
	        /*  DELETE FROM Game WHERE Id IN (...)  */
	        sql.from.append("Game");
	        sql.execute(connection);
	    }
	    else if (JoConnection.getAdapter().canMultiTableDelete()) {
	        /*  DELETE Game,MoreGame
	            FROM Game,MoreGame
	            WHERE Game.Id IN (...) AND Game.Id = MoreGame.GId
	         */
	        sql.delete.append("Game,MoreGame");
	        sql.from.append("Game,MoreGame");
	        sql.where.insert(0,"Game.");
	        sql.where.append(" AND Game.Id = MoreGame.GId");
	        sql.execute(connection);
	    }
	    else if (JoConnection.getAdapter().canSubselect()) {
	        /*	DELETE FROM MoreGame WHERE GId IN (...) */
	        sql.from.append("MoreGame");
	        sql.where.insert(0,"G");
	        sql.execute(connection);

	        /*	DELETE FROM Game WHERE Id IN (...) */
	        sql.from.setLength(0);
	        sql.from.append("Game");
	        sql.where.deleteCharAt(0);
	        sql.execute(connection);
	    }
	    else
	        throw new UnsupportedOperationException("insufficient database capabilities");
	}

	/**
	 * erase (phisycall delete) a collection
	 * the collection must be empty
	 *
	 * @param CId the collection
	 * @throws SQLException
	 */
	public void eraseCollectionRow(int CId)
		throws SQLException
	{
	    String sql1 = "DELETE FROM Collection WHERE Id = ?";
	    JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	    stm1.setInt(1,CId);
	    stm1.execute();
	}

	/**
	 * erase (phisycall delete) the contents of a collection: games + children
	 *
	 * @param CId the collection
	 * @throws SQLException
	 */
	public void eraseCollectionContents(int CId)
		throws SQLException
	{
		/*
			get Child Collections
		*/
	    ParamStatement pstm = new ParamStatement();
		IntHashSet set = new IntHashSet();
		getChildCollections(CId,set);
		/*
			DELETE FROM Collection WHERE Id IN (...)
		 */
	    if (!set.isEmpty()) {
	        pstm.delete.append(" ");
	        pstm.from.append("Collection");
	        pstm.where.append("Id IN ");
	        set.appendString(pstm.where,"(,)");
	        pstm.execute(connection);
	    }

	    /*
	        DELETE FROM Game, MoreGame
	     */
	    set.add(CId);
	    pstm.where.setLength(0);
	    pstm.where.append("Game.CId IN ");
	    set.appendString(pstm.where,"(,)");

	    pstm.delete.setLength(0);
	    pstm.delete.append(" ");

		if (JoConnection.getAdapter().canCascadingDelete()) {
			/*
				DELETE FROM Game WHERE CId IN (...)
			 */
	        pstm.from.setLength(0);
	        pstm.from.append("Game");

	        pstm.execute(connection);
		}
		else if (JoConnection.getAdapter().canMultiTableDelete()) {
			/*
				DELETE Game,MoreGame
				FROM Game,MoreGame
				WHERE Game.Id = MoreGame.GId
				  AND Game.CId IN (...)
			 */
	        pstm.delete.append("Game,MoreGame");
	        pstm.from.setLength(0);
	        pstm.from.append("Game ");
            pstm.from.append(" LEFT OUTER JOIN MoreGame ON Game.Id = MoreGame.GId ");
	        pstm.execute(connection);
		}
		else if (JoConnection.getAdapter().canSubselect()) {
			/*
				DELETE FROM MoreGame WHERE GId IN (SELECT Id FROM Game WHERE CId IN (...))
	        */
	        ParamStatement pstm2 = new ParamStatement(pstm);
	        pstm2.from.setLength(0);
	        pstm2.from.append("MoreGame");
	        pstm2.where.insert(0,"GId IN (SELECT Id FROM Game WHERE ");
	        pstm2.where.append(")");
	        pstm2.execute(connection);

	        /*
				DELETE FROM Game WHERE CId IN (...)
			 */
	        pstm.from.setLength(0);
	        pstm.from.append("Game");
	        pstm.execute(connection);
		}
		else
			throw new UnsupportedOperationException("insufficient database capabilities");
	}

	/**
	 * reset the GameCount of a collection
	 *
	 * @param CId
	 * @throws SQLException
	 */
	public void zeroCollectionCount(int CId)
	    throws SQLException
	{
	    String sql1 = "UPDATE Collection SET GameCount = 0 WHERE Id = ?";
	    JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	    stm1.setInt(1,CId);
	    stm1.execute();
	}

	/**
	 * erase (phisycall delete) the games contained in a collection
	 *
	 * @param CId the collection
	 * @throws SQLException
	 */
	public void eraseChildGames(int CId) throws SQLException
	{
		if (JoConnection.getAdapter().canCascadingDelete())
	    {
	        //  cascading delete
	        String sql1 = "DELETE FROM Game WHERE CId = ?";
			JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	        stm1.setInt(1,CId);
	        stm1.execute();
			//	MoreGame will be delete automatically !
		}
		else if (JoConnection.getAdapter().canMultiTableDelete())
	    {
			//	MySQL dialect: multi table delete
	        String sql1 =
	            "DELETE Game,MoreGame "+
				" FROM Game,MoreGame "+
				" WHERE Game.CId = ? "+
				"  AND MoreGame.GId = Game.Id";
	        JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	        stm1.setInt(1,CId);
	        stm1.execute();
		}
		else if (JoConnection.getAdapter().canSubselect())
	    {
			//  two delete statements:
	        String sql1 =
	            "DELETE FROM MoreGame "+
				" WHERE GId IN (SELECT Id FROM Game WHERE CId = ?) ";
			JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	        stm1.setInt(1,CId);
	        stm1.execute();

	        String sql3 =
	            "DELETE FROM Game "+
				" WHERE CId = ? ";
			JoPreparedStatement stm3 = connection.getPreparedStatement(sql3);
	        stm3.setInt(1,CId);
	        stm3.execute();
		}
		else {
			throw new UnsupportedOperationException("unsufficient database capabilities");
		}

	}

	/**
	 * TEST implement !
	 * restore a game to its original location
	 *
	 * @param GId the game
	 * @throws SQLException
	 */
	public void restoreGame(int GId) throws SQLException
	{
		/*
			SELECT CId,OCId FROM Game WHERE Id = GId
        */
        String sql1 = "SELECT CId,OCId FROM Game WHERE Id = ?";
        JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
        stm1.setInt(1,GId);
        IntArray i = stm1.selectIntArray();

		int currentCId = i.get(0);
        int origCId = i.get(1);

        /*
			UPDATE Game
			SET CId = OCId, Idx = OIdx, OCId = NULL, OIdx = NULL
			WHERE Id = ?
		*/
        String sql2 = "UPDATE Game " +
                " SET CId = OCId, Idx = OIdx, OCId = NULL, OIdx = NULL " +
                " WHERE Id = ?";
        JoPreparedStatement stm2 = connection.getPreparedStatement(sql2);
        stm2.setInt(1,GId);
        stm2.execute();

        addDiff(currentCId,-1);
        addDiff(origCId,+1);
	}

	/**
	 * restore an array of games to their original location
	 *
	 * @param GIds the games
	 * @throws SQLException
	 */
	public void restoreGames(int[] GIds, int from, int to)
		throws SQLException
	{
        for (int i=from; i < to; i++)
            restoreGame(GIds[i]);

		/*
			SELECT CId, COUNT(*)
			FROM Game
			WHERE Id IN (...)
			GROUP BY CId

			updateDiffs();
            that's tricky cause the games could belong to any number of different source AND target collections

			SELECT OCId, COUNT(*)
			FRAME Game
			WHERE Id IN (...)
			GROUP BY OCId

			updateDiffs();

			UPDATE Game
			SET CId = OCId, Idx = OIdx, OCId = NULL, OIdx = NULL
			WHERE Id IN (...)
		 */
	}

	/**
     * TEST
	 * restore a collection to its original location
	 *
	 * @param CId the collection
	 * @throws SQLException
	 */
	public void restoreCollection(int CId)
		throws SQLException
	{
        Collection coll = Collection.readCollection(connection,CId);
        coll.restore(connection);
	}

	/**
     * it is doubtful, whether this method is needed
     * either we restore collections, or a set of games.
     *
     * Restoring only the content of a collection is not needed.
     *
	 * @param CId the collection
	 * @throws SQLException
	 * @deprecated
	 */
	public void restoreCollectionContents(int CId)
		throws SQLException
	{
        /** restore children    */
        List children = Collection.readChildren(connection, CId);
        for (Iterator i = children.iterator(); i.hasNext(); )
        {
            Collection child = (Collection)i.next();
            child.restore(connection);
        }

        //  TOOD restore games
		/*
			SELECT GameCount
			FROM Game
			WHERE CId = ?

			updateDiffs();

			SELECT OCId, COUNT(*)
			FRAME Game
			WHERE CId = ?
			GROUP BY OCId

			updateDiffs();

			UPDATE Game
			SET CId = OCId, Idx = OIdx, OCId = NULL, OIdx = NULL
			WHERE CId = ?
		 */
	}

	/**
	 * krunch all the Idx columns in a collection
     * use sort order from current search record
	 * @param CId
	 */
	public void crunchCollection(int CId, SearchRecord srec) throws SQLException
	{
        /**
         *      INSERT INTO Map_Game (OId)
         *      SELECT ...
         */
        srec = (SearchRecord)srec.clone();
        srec.collections = new IntHashSet();
        srec.collections.add(CId);

        ParamStatement stm = srec.makeIdStatement();
        stm.insert.append("Map_Game (OId,NId)");
		stm.select.append(", (@newIdx:=@newIdx+1)");

        connection.executeUpdate("DELETE FROM Map_Game");
		connection.executeUpdate("SET @newIdx=0");
        stm.executeUpdate(connection);

        /**
         *      UPDATE Game, Map_Game
         *      SET Game.Idx = Map_Game.NId
         *      WHERE Game.Id = Map_Game.OId
         *
         * note that we can't use order by on a mult-table update
         * that's why Map_Game is used temporarily
         */

        String sql1 =
                "UPDATE Game, Map_Game " +
                " SET Game.Idx = Map_Game.NId " +
                " WHERE Game.Id = Map_Game.OId";

        connection.executeUpdate(sql1);
        connection.executeUpdate("DELETE FROM Map_Game");
	}

    /**
     * krunch all the Idx columns in a collection (how?)
     * use "natural" sort order
     * @param CId
     */
    public void crunchCollection(int CId) throws SQLException
    {
        /**
         * UPDATE Game
         * SET Idx = (@newIdx:=@newIdx+1)
         * WHERE CId = ?
         * ORDER BY Idx,Id
         */
        String sql1 =
                "UPDATE Game" +
                " SET Idx = (@newIdx:=@newIdx+1)" +
                " WHERE CId=?" +
                " ORDER BY Idx,Id";

        JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
        stm1.setInt(1,CId);

        connection.executeUpdate("SET @newIdx=0");
        stm1.execute();
    }


    /**
     * krunch Idx columns in a selected set
     */
    public void crunchGames(int[] GId, int from, int to) throws SQLException
    {
        /**  get min. idx   */
        StringBuffer sql1 = new StringBuffer("SELECT MIN(Idx) FROM Game WHERE Id IN (");
        for (int i=from; i<to; i++)
        {
            if (i>from) sql1.append(",");
            sql1.append(GId[i]);
        }
        sql1.append(")");

        int newIdx = connection.selectInt(sql1.toString());

        /** update  */
        StringBuffer sql2 = new StringBuffer("UPDATE Game SET Idx = (@newIdx:=@newIdx+1) WHERE Id IN (");
        for (int i=from; i<to; i++)
        {
            if (i>from) sql2.append(",");
            sql2.append(GId[i]);
        }
        sql2.append(")");

        connection.executeUpdate("SET @newIdx="+(newIdx-1));
        connection.executeUpdate(sql2.toString());
    }

	public String getPath(int CId)
		throws SQLException
	{
		JoPreparedStatement pstm = connection.getPreparedStatement("SELECT Path FROM Collection WHERE Id = ?");
		pstm.setInt(1,CId);
		return pstm.selectString();
	}

	public int getGameCount(int CId)
		throws SQLException
	{
		JoPreparedStatement pstm = connection.getPreparedStatement("SELECT GameCount FROM Collection WHERE Id = ?");
		pstm.setInt(1,CId);
		return pstm.selectInt();
	}

	public int getParent(int CId)
		throws SQLException
	{
		JoPreparedStatement pstm = connection.getPreparedStatement("SELECT PId FROM Collection WHERE Id = ?");
		pstm.setInt(1,CId);
		return pstm.selectInt();
	}

	public int getCId(int GId)
		throws SQLException
	{
		JoPreparedStatement pstm = connection.getPreparedStatement("SELECT CId FROM Game WHERE Id = ?");
		pstm.setInt(1,GId);
		return pstm.selectInt();
	}

	public boolean isAncestorOf(int PId, int CId)
		throws SQLException
	{
		String path = getPath(CId);
		String ppath = "/"+PId+"/";
		return path.indexOf(ppath) >= 0;
	}

	public void getChildCollections(int CId, IntHashSet set)
		throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = connection.getPreparedStatement("SELECT Id FROM Collection WHERE Path LIKE ?");
			pstm.setString(1, "%/"+CId+"/%/");
			pstm.execute();
			while (pstm.next())
				set.add(pstm.getInt(1));
		} finally {
			if (pstm != null) pstm.closeResult();
		}
	}

	// ------------------------------------------------
	// resolve Player, event, Site, Opening
	// ------------------------------------------------

	public static int resolvePlayer(JoConnection connection, String name, boolean create)
	        throws SQLException
	{
	    return resolveString(connection,"Player", name, create);
	}

	public static int resolveEvent(JoConnection connection, String name, boolean create)
	        throws SQLException
	{
	    return resolveString(connection,"Event", name, create);
	}

	public static int resolveSite(JoConnection connection, String name, boolean create)
	        throws SQLException
	{
	    return resolveString(connection,"Site", name, create);
	}

	public static int resolveOpening(JoConnection connection, String name, boolean create)
	        throws SQLException
	{
	    return resolveString(connection,"Opening", name, create);
	}


	public static int resolveString(JoConnection connection, String table, String name, boolean create)
	    throws SQLException
	{
		if (name==null) return 0;

	    String sql1 = "SELECT Id FROM "+table+" WHERE Name = ?";
	    JoPreparedStatement stm1 = connection.getPreparedStatement(sql1);
	    stm1.setString(1,name);
	    int result = stm1.selectInt();

	    if (result > 0) return result;
	    if (!create) return 0;

	    //  create new
	    result = connection.getSequence(table,"Id");
	    Metaphone sndx = new Metaphone(6);

	    String sql2 = "INSERT INTO "+table+" (Id,Name,Soundex) VALUES (?,?,?)";
	    JoPreparedStatement stm2 = connection.getPreparedStatement(sql2);
	    stm2.setInt(1,result);
	    stm2.setString(2, name);
	    stm2.setFixedString(3, sndx.encode(name));
	    stm2.execute();

	    return result;
	}


	// ------------------------------------------------
	// PGN Utils
	// ------------------------------------------------

	/**
	 * copy the contents of the database clipboard to the system clipboard (as PGN text)
	 */
	public void copyPGNtoSystemClipboard(int CId, int limit)
	    throws Exception
	{
	    /** get game IDs    */
	    IntHashSet CIds = new IntHashSet();
	    getChildCollections(CId,CIds);
	    CIds.add(CId);

	    String sql = "SELECT Id FROM Game WHERE CId IN "+CIds+" LIMIT "+limit;

	    int[] GIds = connection.selectIntArray(sql,0);

	    /** setup PGN exporter  */
	    StringWriter swriter = new StringWriter();
	    PGNExport pgnex = new PGNExport(swriter,"Unicode");
	    pgnex.setSource(GameSource.gameArray(GIds));
	    pgnex.run();

	    /** copy text to clipboard  */
		ClipboardUtil.setDatabaseGames(swriter.toString(),null);
	}

	/**
	 * copy one game to the system clipoard (as PGN text)
	 */
	public static void copyPGNtoSystemClipboard(Game gm) throws Exception
	{
		/** setup PGN exporter  */
		StringWriter swriter = new StringWriter();
		PGNExport pgnex = new PGNExport(swriter,"Unicode");
		pgnex.prepare();
		pgnex.printGame(gm);
		pgnex.finish();

		/** copy text to clipboard  */
		ClipboardUtil.setPlainText(swriter,null);
	}

	/**
	 * paste PGN text into a game (not the database)
	 */
	public static void pastePGN(Game gm, String pgn) throws Exception
	{
		/**  setup a PGNImport that does not operate on the database
		 * */
		StringReader sreader = new StringReader(pgn);
		PGNImport pgnin = new PGNImport(sreader,pgn.length(),null);
		pgnin.read1Game();

		GameBuffer gmbuf = pgnin.getGameBuffer();
		gm.read(gmbuf.getRow(0),true);
	}

	static class UtilParser extends Parser
	{
		private Game gm;
		private Node current;

		public UtilParser(Position pos, Game gm, MoveNode node)
		{
			super(pos, 0, true);
			this.gm = gm;
			this.current = node;
		}

		protected void callbackComment(char[] line, int start, int len)
		{
			//  insert comment node
			try {
				CommentNode cm = new CommentNode(line,start,len);
				if (current==null) current = gm.getMainLine().firstLeaf(); 
				cm.insertAfter(current);
				cm.insert(gm,current.getEndOffset());
				current = cm;
			} catch (BadLocationException e) {
				Application.error(e);
			}
		}


		protected void callbackLegalMove(Move mv)
		{
			try {
				//  mv was created by parser.pos; create a copy for gm.getPosition()
	            gm.getPosition().undoMove();
				gm.insertMove(-1, new Move(mv,gm.getPosition()), Game.NEW_LINE);
				current = gm.getCurrentMove();
			} catch (BadLocationException e) {
				Application.error(e);
			}
		}

		protected void callbackError(short errorCode, char[] line, int start, int len)
		{
			/** bail out    */
			switch (errorCode) {
			case SHORT_ERROR_AMBIGUOUS:
			case SHORT_ERROR_EMPTY:
			case SHORT_ERROR_ILLEGAL:
			case SHORT_ERROR_UNREADABLE:
			case SHORT_ERROR_UNRECOGNIZED:
			}
			offset = max;
		}

		protected void callbackAnnotation(int nag)          { /* currently ignored */ }
		protected void callbackResult(int result)   		{ /* currently ignored */	}

		protected void callbackStartVariation()         	{ /* currently ignored */	}
		protected void callbackEndVariation()       		{ /* currently ignored */   }
	}

	/**
	 * insert a variation line into a game
	 * @param gm the Game object
	 * @param after insert variation just after this move (or null)
	 * @param text variation as human-readable text
	 * @return the last inserted move
	 */
	public static MoveNode pasteLine(Game gm, MoveNode after, String text)
	{
//		System.out.println("paste: "+text);
		MoveNode result = null;
		try {
			gm.ignoreCaretUpdate = true;

			gm.gotoMove(after);

			GameUtilOldImpl.UtilParser parser = new GameUtil.UtilParser(gm.getPosition(),gm,after);
			//	todo allow for various languages (like English)
			String figurineLanguage = Application.theUserProfile.getFigurineLanguage();
			String piecechars = Language.getPieceChars(figurineLanguage);
			if (!MoveFormatter.isAnsiChars(piecechars)) {
				//	fallback to English
				figurineLanguage = "en";
			}
			parser.setLanguage(figurineLanguage,true);
			parser.parse(text.toCharArray(), null,null, false);
			//  calls back, eventually
			result = gm.getCurrentMove();

			gm.gotoMove(after);

		} finally {
			gm.ignoreCaretUpdate = false;
		}
		return result;
	}

	// ------------------------------------------------
	//  diff utils
	// ------------------------------------------------

	public void clearDiffs()
	{
		if (diff==null) diff = new IntIntMap();
		diff.clear();
	}

	public void applyDiffs()
		throws SQLException
	{
        if (diff==null || diff.isEmpty())
            return;
		/*
			update diffs

			UPDATE Collection
			SET GameCount = GameCount + ?
			WHERE Id = ?
		 */
		JoPreparedStatement pstm =
		       connection.getPreparedStatement("UPDATE Collection SET GameCount = GameCount + ? WHERE Id = ?");

		//	apply diffs
		IntIntMap.EntryIterator i = diff.entryIterator();
		while (i.hasNext()) {
			IntIntMap.Entry ety = (IntIntMap.Entry)i.next();
			int CId = ety.getIntKey();
			int d = ety.getIntValue();

			pstm.setInt(1,d);
			pstm.setInt(2,CId);
			pstm.execute();
		}

	}

	protected void addDiff(int CId, int d)
	{
		int value = diff.get(CId);
		if (value==IntIntMap.NOT_FOUND) value=0;
		diff.put(CId,value+d);
	}


	protected void addDiffs(int targetCId,
	                        ParamStatement sql) throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = sql.toPreparedStatement(connection);
			pstm.execute();
			int total = 0;
			while (pstm.next()) {
				int CId = pstm.getInt(1);
				int count = pstm.getInt(2);

				addDiff(CId,-count);
				total += count;
			}

            if (targetCId > 0)
			    addDiff(targetCId,+total);

		} finally {
			if (pstm!=null) pstm.closeResult();
		}
	}


    protected ParamStatement addDiffs(int[] GIds, int from, int to, int targetCId) throws SQLException
    {
        ParamStatement sql = new ParamStatement();
        sql.select.append("CId, COUNT(*)");
        sql.from.append("Game");
        sql.where.append("Id IN (?");
        StringUtil.append(sql.where,",?",to-from-1);
        sql.where.append(")");
        for (int i=from; i<to; i++)
            sql.addIntParameter(GIds[i]);
        sql.group.append("CId");

        addDiffs(targetCId, sql);
        return sql;
    }

}
