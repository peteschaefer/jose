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

package de.jose.task.io;

import de.jose.Application;
import de.jose.TaskAbortedException;
import de.jose.util.Metaphone;
import de.jose.db.*;
import de.jose.db.io.ArchiveFile;
import de.jose.pgn.Collection;
import de.jose.pgn.Game;
import de.jose.pgn.GameBuffer;
import de.jose.task.DBTask;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ArchiveImport
        extends DBTask
{
    protected Setup setup;
	protected Setup map;
	protected String tempdb;
	protected File dataDir;
    protected ArchiveFile archive;
	protected Metaphone sndx;
	protected boolean canDisableKeys;
	protected boolean disableKeys;

	protected int nextCId;
	protected int gameCount;


    public ArchiveImport(File inFile) throws Exception
    {
        super("Archive Import",true);
        archive = new ArchiveFile(inFile);
        open(null);
    }

    public ArchiveImport(URL url) throws Exception
    {
        super("Archive Import",true);
        archive = new ArchiveFile(null);
        open(url.openStream());
    }

	protected void open(InputStream input) throws Exception
	{
		PGNImport.gGameImporterInstance++;
	    archive.open(input);

	    pollProgress = 500;
	    setSilentTime(0);
	}

	public static boolean isAvailable(DBAdapter adapt)
    {
        return adapt.canQuickDump();
    }

	public static void disableKeys(JoConnection connection) throws SQLException
	{
		/** this is a BIG one; let's disable indexes
		 *  and restore them later
		 */
		JoConnection.getAdapter().disableConstraints("Game",connection);
		JoConnection.getAdapter().disableConstraints("MoreGame",connection);
	}

	public static void enableKeys(JoConnection connection)
	{
		try {
			JoConnection.getAdapter().enableConstraints("Game",connection);
		} catch (SQLException sqlex) {
			Application.error(sqlex);
		}
		try {
			JoConnection.getAdapter().enableConstraints("MoreGame",connection);
		} catch (SQLException sqlex) {
			Application.error(sqlex);
		}
	}


    public int init()
            throws Exception
    {
	    /** clean up IO schema  */
	    setup = new Setup(Application.theApplication.theConfig, connection);
	    setup.setSchema("IO");

	    map = new Setup(Application.theApplication.theConfig, connection);
	    map.setSchema("IO_MAP");

		tempdb = "IO_"+System.currentTimeMillis();
		connection.executeUpdate("CREATE DATABASE "+tempdb);
		dataDir = new File(Application.theDatabaseDirectory,"mysql/"+tempdb);

	    sndx = new Metaphone(6);
//        MySQLAdapter.defineUDFs(getConnection());


	    /**
	     * if delayed key writing is enabled, we don't care about disabling keys
	     *
	     * if delayed key writing is off, we have to avoid huge key updates.
	     * in that case, it might be useful to completely disable keys and
	     * enable them afterwards.
	     */
	    canDisableKeys = ! connection.getAdapter().can("delayed_key_write");

	    if (canDisableKeys && archive.getFileSize() > PGNImport.DISABLE_KEY_FILE_SIZE) {
		    disableKeys(connection);
		    disableKeys = true;
	    }
	    else
		    disableKeys = false;

	    return RUNNING;
    }

    public int work()
            throws Exception
    {
        broadcastOnUpdate(getName());

        archive.extractAllFiles(dataDir);
        archive.close();
	    /**
	     * et voilá: the IO schema is back again !
	     * (now try THAT with Oracle ;-)
	     */
	    setProgress(0.1);

	    //  create Map tables
        map.createTables(tempdb,"MEMORY",true);

	    //  compare MetaInfo (not yet necessary)

	    copyCollection();
		setProgress(0.15);
		//  don't abort from within copyCollection, to keep database consistent !
	    throwAborted();

	    copyNames("Player");
	    setProgress(0.2);

	    copyNames("Event");
	    setProgress(0.25);

	    copyNames("Site");
	    setProgress(0.30);

	    copyNames("Opening");
	    setProgress(0.35);

	    gameCount = copyGame();


	    //  enable keys; we can do it safely since here we are in a separate thread
	    if (disableKeys) {
		    setProgress(0.90);
	        enableKeys(connection);
	        disableKeys = false;
	    }

	    setProgress(0.98);

        return SUCCESS;
    }

    public int done(int state)
    {
	    try {
		    getConnection().executeUpdate("DROP DATABASE "+tempdb);
	    } catch (SQLException e) {
	    }

	    if (state==ABORTED) {
		    //  just in case: res-synch the sequence generator
		    try {
			    Collection.resetSequence(connection);
			    Collection.repairGameCounts(connection);
		    } catch (SQLException e) {
		    }
	    }

	    /*	update finished; refresh display, if necessary	*/
	    DBTask.broadcastAfterUpdate(nextCId);

	    //  flag tables for needing analysis
	    Setup setup = new Setup(Application.theApplication.theConfig,"MAIN",connection);
	    if (gameCount > PGNImport.ANALYZE_LIMIT)   //  TODO
	        try { setup.markAllDirty(); } catch (SQLException ex) { /* ignore */ }

	    if (--PGNImport.gGameImporterInstance == 0)
	        setup.analyzeTables(false);  //  actually do analyze the tables

//        System.out.println("import "+(double)getElapsedTime()/1000.0);
        return super.done(state);
    }

    protected int copyCollection() throws Exception
    {
//	    System.out.print("[Collection ");

	    int count = 0;
	    try {
		String sql=    "INSERT INTO "+tempdb+".Map_Collection " +
				    " SELECT Import.Id AS OId, @NextId:=(@NextId+1) AS NId " +
				" FROM "+tempdb+".IO_Collection AS Import";

		    nextCId = Collection.getSequence(connection);
		    connection.executeUpdate("SET @NextId="+(nextCId-1));

		    count = connection.executeUpdate(sql);
		    Collection.getSequence(connection, count);

        /**	copy rows
		     *  Paths are updated later
         * */
        sql =
            "INSERT INTO Collection (Id,PId, Name,Path,Attributes,SourceURL,GameCount) "+
            " SELECT Map.NId AS Id, PMap.NId AS PId, Name, NULL, Attributes,SourceURL,GameCount "+
            " FROM "+tempdb+".IO_Collection Import " +
		    "   JOIN "+tempdb+".Map_Collection Map ON Import.Id=Map.OId" +
		    "   LEFT OUTER JOIN "+tempdb+".Map_Collection PMap ON Import.PId=Map.OId";

		    connection.executeUpdate(sql);

	    /**	resolve name clashes (on top level only)	*/
        sql =
            "SELECT C1.Id, C1.Name " +
		    " FROM Collection C1 JOIN Collection C2 ON C1.Name = C2.Name"+
		        " WHERE C1.PId IS NULL AND C2.PId IS NULL AND C1.Id>="+nextCId+" AND C1.Id != C2.Id";
		    JoPreparedStatement stm = null;
        try {
		        stm = connection.getPreparedStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			    stm.execute();
		        ResultSet res = stm.getResultSet();
            while (res.next()) {
                int id = res.getInt(1);
                String oldName = res.getString(2);
		            String newName = Collection.makeUniqueName(0,oldName,connection);
                if (!newName.equals(oldName)) {
                    String sql2 = "UPDATE Collection SET Name = ? WHERE Id = ?";
		                JoPreparedStatement pstm = connection.getPreparedStatement(sql2);
                    pstm.setString(1,newName);
                    pstm.setInt(2,id);
                    pstm.execute();
                }
            }
        } finally {
            if (stm!=null) stm.closeResult();
        }

        /** adjust the Paths    */
        /** start at the top level */
        sql =
           "UPDATE Collection"+
           " SET Path = concat('/',Id,'/') "+
           " WHERE Path IS NULL "+
           "   AND PId IS NULL ";
        int updcount = connection.executeUpdate(sql);

        /** work down   */
        while (updcount > 0) {
            sql =
               "UPDATE Collection, Collection PCollection "+
               " SET Collection.Path = PCollection.Path||Collection.Id||'/'"+
               " WHERE Collection.Path IS NULL"+
               "  AND PCollection.Path IS NOT NULL "+
               "  AND Collection.PId = PCollection.Id";
            updcount = connection.executeUpdate(sql);
        }
	    } catch (SQLException e) {
		    //  restore database consistency
		    Collection.resetSequence(connection);
	    }

//        setup.dropTable("IO_Collection");
//	    System.out.println(" "+count+"]");
	    return count;
    }

	protected int copyNames(String table) throws Exception
	{
//		System.out.print("["+table);

		//  resolve non-matching names
		ArrayList unresolved = new ArrayList();
		String sql =
			  " SELECT Import.Name" +
			  " FROM "+tempdb+".IO_"+table+" Import " +
			  " LEFT OUTER JOIN "+table+" Main ON Import.Name=Main.Name"+
			  " WHERE Main.Id IS NULL";
		ResultSet res = null;
		try {
			JoPreparedStatement pstm = connection.getPreparedStatement(sql);
			pstm.execute();
			res = pstm.getResultSet();
			while (res.next()) {
				unresolved.add(res.getString(1));
				if (unresolved.size() >= 256) {
						JoConnection connection2 = null;
					try {
						connection2 = JoConnection.get();
						GameBuffer.insertNames(connection2, table, unresolved, sndx);
						unresolved.clear();
		} finally {
						if (connection2!=null) connection2.release();
					}
					throwAborted();
				}
			}

		} finally {
			if (res!=null) res.close();
		}

		throwAborted();

		if (unresolved.size() > 0)
			GameBuffer.insertNames(connection, table, unresolved, sndx);
		throwAborted();

		//  Map Ids to matching names
		sql = "INSERT INTO "+tempdb+".Map_"+table+
			" SELECT Import.Id AS OId, MIN(Main.Id) AS NId " +
			" FROM "+tempdb+".IO_"+table+" AS Import"+
			"  JOIN "+table+" Main ON Import.Name=Main.Name"+
			" GROUP BY Import.Id";
		int count = connection.executeUpdate(sql);
		throwAborted();

//		int icount = connection.selectInt("SELECT COUNT(*) FROM "+tempdb+".IO_"+table);
//		if (count!=icount) throw new IllegalStateException("unexpected cross-product !?");

//		System.out.println(" "+count+"]");
		return count;
	}

	protected int copyGame() throws Exception
    {
//	    System.out.print("[Game ");

	    int count = 0;
	    try {

        /**	copy values	*/
        String sql =
            "INSERT INTO Game (Id,CId, Idx,Attributes,PlyCount," +
            "      Result,WhiteId,BlackId, WhiteELO,BlackELO, EventId,SiteId," +
            "      GameDate,EventDate,DateFlags, OpeningId,ECO, AnnotatorId) "+
		        " SELECT @NextId:=(@NextId+1) AS Id,"+
            "        CMap.NId AS CId,"+
            "        Idx,Attributes,PlyCount, " +
            "        Result, "+
            "        WMap.NId AS WhiteId,"+
            "        BMap.NId AS BlackId,"+
            "        WhiteELO,BlackELO, "+
            "        EMap.NId AS EventId, "+
            "        SMap.NId AS SiteId, "+
            "        GameDate,EventDate,DateFlags,"+
            "        OMap.NId AS OpeningId,"+
            "        ECO, "+
            "        AMap.NId AS AnnotatorId "+
            " FROM "+tempdb+".IO_Game AS Game" +
		    "  JOIN "+tempdb+".Map_Collection AS CMap ON Game.CId=CMap.OId" +
		    "  JOIN "+tempdb+".Map_Player AS WMap ON Game.WhiteId=WMap.OId" +
		    "  JOIN "+tempdb+".Map_Player AS BMap ON Game.BlackId=BMap.OId" +
		    "  JOIN "+tempdb+".Map_Event AS EMap ON Game.eventId=EMap.OId" +
		    "  JOIN "+tempdb+".Map_Site AS SMap ON Game.SiteId=SMap.OId" +
		    "  JOIN "+tempdb+".Map_Opening AS OMap ON Game.OpeningId=OMap.OId" +
		    "  JOIN "+tempdb+".Map_Player AS AMap ON Game.AnnotatorId=AMap.OId";

		    int nextId = Game.getSequence(connection);
	    connection.executeUpdate("SET @NextId="+(nextId-1));
		    count = connection.executeUpdate(sql);

		    Game.getSequence(connection, count);    //  reserve sequence

		    throwAborted();
		    setProgress(0.50);

	    sql =
	        "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle, Round,Board,FEN, Info,Bin," +
			"     Comments,PosMain,PosVar) "+
		        " SELECT @NextId:=(@NextId+1) AS GId," +
			"        WhiteTitle,BlackTitle, Round,Board,FEN, Info,Bin,Comments, PosMain,PosVar"+
	        " FROM "+tempdb+".IO_Game ";

		    connection.executeUpdate("SET @NextId="+(nextId-1));
	    connection.executeUpdate(sql);
		    throwAborted();

	    } catch (SQLException e)
	    {
		    //  roll back dangling games
		    rollBackGames();
		    throw e;
		} catch (TaskAbortedException e)
	    {
			//  roll back dangling games
		    rollBackGames();
		    throw e;
		}

//        setup.dropTable("IO_Game");
        return count;
    }

	private void rollBackGames()
			throws SQLException
	{
		connection.executeUpdate(
				"DELETE FROM Game LEFT OUTER JOIN MoreGame ON Game.Id = MoreGame.GId" +
			    " WHERE MoreGame.GId IS NULL");
		//  just in case: res-synch the sequence generator
		Collection.resetSequence(connection);
		Collection.repairGameCounts(connection);
	}

}
