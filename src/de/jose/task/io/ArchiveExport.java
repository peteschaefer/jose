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
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.Setup;
import de.jose.db.MySQLAdapter;
import de.jose.db.io.ArchiveFile;
import de.jose.pgn.Game;
import de.jose.pgn.Collection;
import de.jose.task.MaintenanceTask;
import de.jose.task.DBSelectionModel;
import de.jose.util.IntArray;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class ArchiveExport
        extends MaintenanceTask
{
    //-------------------------------------------------------------------------------
    //	Constants
    //-------------------------------------------------------------------------------


	private static final String collectionByCId = " FROM Collection WHERE Id IN ";
	private static final String collectionByGId = " FROM Game,Collection WHERE Collection.Id=Game.CId AND Game.Id IN ";

	private static final String gameByCId = " FROM Game,MoreGame WHERE MoreGame.GId = Game.Id AND Game.CId IN ";
	private static final String gameByGId = " FROM Game,MoreGame WHERE MoreGame.Gid = Game.Id AND Game.Id IN ";

	private static String makeGameCIdConstraint(String otherTable, String foreignKey)
	{
		return " FROM Game,"+otherTable+" WHERE "+otherTable+".Id=Game."+foreignKey+" AND Game.CId IN ";
	}

	private static String makeGameGIdConstraint(String otherTable, String foreignKey)
	{
		return " FROM Game,"+otherTable+" WHERE "+otherTable+".Id=Game."+foreignKey+" AND Game.Id IN ";
	}

    //-------------------------------------------------------------------------------
    //	Fields
    //-------------------------------------------------------------------------------

    protected Setup setup;
    protected ArchiveFile archive;

	protected String tempdb;
	protected File dataDir;

	int[] CId;
	int[] GId;

    //-------------------------------------------------------------------------------
    //	Ctor
    //-------------------------------------------------------------------------------

    public ArchiveExport(File outFile) throws Exception
    {
        super("ArchiveExport",true);
        archive = new ArchiveFile(outFile);

        setBulkSize(256);
        setSilentTime(0);
        pollProgress = 500;
        progressText = "writing "+outFile.getName();
    }

    public static boolean isAvailable(DBAdapter adapt)
    {
        return adapt.canQuickDump();
    }

    //-------------------------------------------------------------------------------
    //	Methods
    //-------------------------------------------------------------------------------

    public void prepare() throws Exception
    {
        /** clean up IO schema  */
        setup = new Setup(Application.theApplication.theConfig, getConnection());
        setup.setSchema("IO");

	    tempdb = "IO_"+System.currentTimeMillis();
	    connection.executeUpdate("CREATE DATABASE "+tempdb);
	    dataDir = new File(Application.theDatabaseDirectory,"mysql/"+tempdb);

        archive.create(ArchiveFile.TABLE_MYISAM,
		        ArchiveFile.PACK_TAR,
		        ArchiveFile.COMPRESS_GZIP,
		        dataDir);

	    getSource();
    }

	private void getSource()
			throws Exception
	{
		GId = null;
		CId = null;

		if (source.isSingleGame()) {
			GId = new int[1];
			GId[0] = source.getId();
		}
		else if (source.isGameArray()) {
			GId = source.getIds();
		}
		else if (source.isSingleCollection()
				|| source.isCollectionContents()) {
			CId = new int[1];
			CId[0] = source.getId();
		}
		else if (source.isCollectionArray()) {
			CId = source.getIds();
		}
		else if (source.isGameSelection()) {
			DBSelectionModel select = source.getSelection();
		    IntArray array = new IntArray(bulkSize+1);
		    for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
			    if (select.isSelectedIndex(i)) array.add(select.getDBId(i));

		    GId = array.toArray();
		}
		else if (source.isCollectionSelection()) {
			//	process selection of collections
			DBSelectionModel select = source.getSelection();
			IntArray array = new IntArray(bulkSize+1);
			for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
				if (select.isSelectedIndex(i)) array.add(select.getDBId(i));

			CId = array.toArray();
		}
		else if (source.isObject()) {
			Game gm = source.getObject();
			gm.save();

			GId = new int[1];
			GId[0] = gm.getId();
		}
		else if (source.isList()) {
			GId = new int[source.size()];
			Iterator i = source.getIterator();
			for (int j=0; i.hasNext(); j++) {
				Game gm = (Game)i.next();
				gm.save();
				GId[j] = gm.getId();
			}
		}
		else if (source.isResultSet()) {
			throw new UnsupportedOperationException();
		}
		else
			throw new IllegalStateException();
	}


	private void store(String[] tableNames, String[] extensions)
			throws IOException, SQLException
	{
		//  order files by extension, right ?
		for (int j=0; j < tableNames.length; j++)
			for (int i=0; i < extensions.length; i++)
			{
				File dataFile = new File(dataDir, tableNames[j]+"."+extensions[i]);
				archive.storeFile(dataFile);
				throwAborted();
			}
	}

	private void myisampack(File dataDir, String[] tables) throws SQLException, IOException
	{
		StringBuffer commandLine = new StringBuffer();
		for (int i=0; i < tables.length; i++)
		{
			if (i>0) commandLine.append(" ");
			commandLine.append(tables[i]);
		}

		System.err.println(MySQLAdapter.myisampack(dataDir,commandLine.toString()));
		throwAborted();
	}

	private String makeCopyStatement(String tableName,
	                                       String constraint,
	                                       int[] ids, int min, int max)
	{
		StringBuffer sql = new StringBuffer();

		sql.append("INSERT IGNORE INTO ");
		sql.append(tempdb);
		sql.append(".IO_");
		sql.append(tableName);

		sql.append(" SELECT ");
		String[] columnNames = setup.getColumnNames("IO_"+tableName);
		for (int j=0; j < columnNames.length; j++)
		{
			if (j>0) sql.append(",");
			if (columnNames[j].equals("Id")
					|| columnNames[j].equals("ECO")) {
				sql.append(tableName);
				sql.append(".");
			}
			sql.append(columnNames[j]);
		}

		sql.append(" ");
		sql.append(constraint);

		sql.append(" (");
		for (int j=min; j < max; j++)
		{
			if (j>min) sql.append(",");
			sql.append(ids[j]);
		}
		sql.append(")");

		return sql.toString();
	}

	private int copy(String tableName, String cidConstraint, String gidConstraint) throws SQLException
	{
		int rows = 0;
		if (CId!=null) {
			int min = 0;
			int max = Math.min(CId.length,bulkSize);
			while (min < CId.length)
			{
				String sql = makeCopyStatement(tableName,
						cidConstraint, CId,min,max);

				rows += connection.executeUpdate(sql);
				throwAborted();

				min = max;
				max = Math.min(CId.length, max+bulkSize);
			}
		}
		if (GId!=null) {
			int min = 0;
			int max = Math.min(GId.length,bulkSize);
			while (min < GId.length)
			{
				String sql = makeCopyStatement(tableName,
						gidConstraint, GId,min,max);

				rows += connection.executeUpdate(sql);
				throwAborted();

				min = max;
				max = Math.min(GId.length, max+bulkSize);
			}
		}
		return rows;
	}




	public int work()
		throws Exception
	{
		String tableFormat = (archive.getTableFormat()==ArchiveFile.TABLE_ARCHIVE) ? "ARCHIVE":null;

		//  Store Meta Info
		String meta_copy =
				"CREATE TABLE "+tempdb+".MetaInfo" +
				" SELECT * FROM MetaInfo" +
				" WHERE SchemaName IN ('META','IO')";
		connection.executeUpdate(meta_copy);
		setProgress(0.01);

		setup.createTables(tempdb, tableFormat, true);
		setProgress(0.02);

		//  Collection
		getConnection().executeUpdate("LOCK TABLES "+tempdb+".IO_Collection WRITE, " +
				tempdb+".IO_Collection AS Parent WRITE, " +
				" Collection READ");
		int collectionRows = copy ("Collection", collectionByCId, collectionByGId);

		//  clear PId for root entries
		String sql = "UPDATE "+tempdb+".IO_Collection" +
				" LEFT OUTER JOIN "+tempdb+".IO_Collection Parent ON IO_Collection.PId = Parent.Id" +
				" SET IO_Collection.PId=NULL WHERE Parent.Id IS NULL";     //  = non-existing parents
		connection.executeUpdate(sql);
		//  clear attributes
		sql = "UPDATE "+tempdb+".IO_Collection"+
				" SET Attributes = Attributes & ~"+(Collection.DELETED+Collection.SYSTEM);
		connection.executeUpdate(sql);

		setup.dropIndexes(tempdb,"IO_Collection");
		connection.executeUpdate("FLUSH TABLE "+tempdb+".IO_Collection");
		setProgress(0.05);

		//  Player
		System.out.print("[Player ");
		connection.executeUpdate("LOCK TABLES "
				+tempdb+".IO_Player WRITE, Player READ, Game READ");
		
		int playerRows = copy("Player",
				makeGameCIdConstraint("Player","WhiteId"),
				makeGameGIdConstraint("Player","WhiteId"));
		setProgress(0.1);

		playerRows += copy("Player",
				makeGameCIdConstraint("Player","BlackId"),
				makeGameGIdConstraint("Player","BlackId"));
		setProgress(0.15);

		playerRows += copy("Player",
				makeGameCIdConstraint("Player","AnnotatorId"),
				makeGameGIdConstraint("Player","AnnotatorId"));
		setup.dropIndexes(tempdb,"IO_Player");
		connection.executeUpdate("FLUSH TABLE "+tempdb+".IO_Player");

		setProgress(0.25);
		System.out.print(" "+(System.currentTimeMillis()-getStartTime()));
		System.out.println(" "+playerRows+" rows]");

		//  Event
		copyNames("Event");
		setProgress(0.30);

		//  Site
		copyNames("Site");
		setProgress(0.35);

		//  Opening
		copyNames("Opening");
		setProgress(0.40);

		//  Game
		System.out.print("[Game ");
		getConnection().executeUpdate("LOCK TABLES "
				+tempdb+".IO_Game WRITE, Game READ, MoreGame READ");
		int gameRows = copy("Game", gameByCId, gameByGId);
//		setup.dropIndexes(tempdb,"IO_Game");
		connection.executeUpdate("FLUSH TABLE "+tempdb+".IO_Game");

		setProgress(0.70);
		System.out.print(" "+(System.currentTimeMillis()-getStartTime()));
		System.out.println(" "+gameRows+" rows]");

		if (GId!=null && GId.length>0) {
			//  update Collection.GameCount (because only a subset is exported)
			sql = "UPDATE "+tempdb+".IO_Collection " +
					" LEFT OUTER JOIN "+tempdb+".IO_Game ON CId = IO_Collection.Id " +
					" SET GameCount = COUNT(IO_Game.Id) " +
					" GROUP BY IO_Collection.Id";
			connection.executeUpdate(sql);
		}

		//  unlock
		connection.executeUpdate("UNLOCK TABLES");
		connection.executeUpdate("FLUSH TABLES");

		//  myisampack
		String[] tableNames = {
				"MetaInfo","IO_Collection","IO_Player","IO_Event",
				"IO_Site","IO_Opening","IO_Game",
		};
		String[] extensions = {
				"frm","MYI","MYD",
		};

		if (archive.getTableFormat()==ArchiveFile.TABLE_MYISAM_PACKED)
		{
			getConnection().close();
			shared=true;    // = don't release this connection to the pool
			connection = null;
			myisampack(dataDir,tableNames);
		}

		//  ZIP
		store(tableNames,extensions);
		setProgress(0.98);

//		if (isAbortRequested()) return ABORTED;

		archive.flush();
		archive.close();
		setProgress(1.00);

		System.out.println("["+archive.getFile().getName()+"]");

		return SUCCESS;
	}

	private int copyNames(String table)
			throws Exception
	{
		System.out.print("["+table+" ");
		connection.executeUpdate("LOCK TABLES "
				+tempdb+".IO_"+table+" WRITE, "
				+table+" READ, Game READ");

		int rows = copy(table,
				makeGameCIdConstraint(table,table+"Id"),
				makeGameGIdConstraint(table,table+"Id"));

		setup.dropIndexes(tempdb,"IO_"+table);
//		connection.executeUpdate("FLUSH TABLE "+tempdb+".IO_"+table);

		System.out.print(" "+(System.currentTimeMillis()-getStartTime()));
		System.out.println(" "+rows+" rows]");
		return rows;
	}

	public int done(int state)
	{
		try {
			connection.executeUpdate("UNLOCK TABLES");
		} catch (SQLException e) {
			Application.error(e);
		}

		try {
		    getConnection().executeUpdate("DROP DATABASE "+tempdb);
	    } catch (SQLException e) {
	    }

		switch (state) {
	    case SUCCESS:       archive.close();
		                    break;
	    case FAILURE:
	    case ABORTED:
	    case ERROR:			/**	clean up the rubble	*/
	                        archive.delete();
	                        break;
	    }

	    System.err.println((double)this.getElapsedTime()/1000);

	    return super.done(state);
	}


	public void processCollection(int CId) throws Exception
	{
		throw new AbstractMethodError();
	}

	public void processCollectionContents(int CId) throws Exception
	{
		throw new AbstractMethodError();
	}

	public void processGame(int GId) throws Exception
	{
		throw new AbstractMethodError();
	}

	public void processGames(int[] GId, int from, int to) throws Exception
	{
		throw new AbstractMethodError();
	}
}
