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
import de.jose.db.JoPreparedStatement;
import de.jose.db.Setup;
import de.jose.db.io.ResultSetInfo;
import de.jose.db.io.DumpWriter;
import de.jose.task.MaintenanceTask;
import de.jose.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BinaryExport
          extends MaintenanceTask
{
	protected static final String COPY_PLAYER_SQL	=
		"INSERT IGNORE INTO IO_Player "+
		" SELECT Player.Id, Player.Name "+
		" FROM Game, Player "+
		" WHERE Game.CId = ? "+
		"   AND Game.%key% = Player.Id";
	
	protected static final String SELECT_PLAYER_SQL =
		"SELECT Id,Name FROM IO_Player ";

	protected static final String COLL_EVENT_SQL =
		"SELECT DISTINCT Event.Id, Event.Name "+
	    " FROM Game, Event "+
	    " WHERE Game.CId = ? AND Game.EventId = Event.Id ";

	protected static final String COLL_SITE_SQL =
		"SELECT DISTINCT Site.Id, Site.Name "+
	    " FROM Game, Site "+
	    " WHERE Game.CId = ? AND Game.SiteId = Site.Id ";

	protected static final String COLL_OPENING_SQL =
		"SELECT DISTINCT Opening.Id, Opening.Name "+
	    " FROM Game, Opening "+
	    " WHERE Game.CId = ? AND Game.OpeningId = Opening.Id ";

	
	protected static final String COLL_GAME_SQL =
		"SELECT Id,CId,Idx, Attributes, PlyCount,Result, "+
	    "  WhiteId,BlackId, WhiteELO,BlackELO, WhiteTitle,BlackTitle, "+
		"  EventId,SiteId, GameDate,EventDate,DateFlags, "+
	    "  OpeningId, ECO, Round,Board,FEN, "+
	    "  MoreGame.Info, MoreGame.Bin, MoreGame.Comments "+
	    " FROM Game, MoreGame "+
	    " WHERE Game.CId = ? AND Game.Id = MoreGame.GId ";

	protected static final String COLL_COLLECTION_SQL =
		"SELECT Id, Name, SourceURL "+
	    " FROM Collection "+
	    " WHERE Id = ? ";

	protected DumpWriter dump;
	protected ResultSetInfo resultInfo;
	protected int totalGames;
	protected File outputFile;
	protected Setup setup;

    public BinaryExport(File outputFile)
        throws Exception
    {
        super("Binary Export",false);
		this.outputFile = outputFile;
		this.pollProgress = 500;
        setSilentTime(0);
    }


	public int init() throws Exception
	{
		int result = super.init();

		FileOutputStream out = new FileOutputStream(outputFile);
		dump = new DumpWriter(out,connection, "", false);

		return result;
	}

	public void prepare() throws Exception
	{
		progress = 0.0;
		totalGames = source.size();

		setup = new Setup(Application.theApplication.theConfig, getConnection());
		setup.drop("IO",Setup.IGNORE_ERRORS);
		setup.setup("IO",Setup.WITH_CONSTRAINTS);
	}

	public void processGame(int GId) throws Exception
	{
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

	public void processGames(int[] GId, int from, int to) throws Exception
	{
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

	public double getProgress()
	{
		if (progress < 0.6 && resultInfo != null)
			progress = (double)resultInfo.countRows() / totalGames * 0.6;

		return progress;
	}

	public void processCollection(int CId) throws Exception
	{
		export(COLL_COLLECTION_SQL,CId);
		export(COLL_GAME_SQL,CId);

		setProgress(0.6);

		/**	copy Player Data into IO_schema
		 * 	INSERT IGNORE seems to be more efficient
		 * 	than SELECT DISTINCT
		 * */
		copy(COPY_PLAYER_SQL, "WhiteId",CId);
		copy(COPY_PLAYER_SQL, "BlackId",CId);
		copy(COPY_PLAYER_SQL, "AnnotatorId",CId);

		export(SELECT_PLAYER_SQL,-1);

		setProgress(0.7);

		export(COLL_EVENT_SQL,CId);

		setProgress(0.8);

		export(COLL_SITE_SQL,CId);

		setProgress(0.9);

		export(COLL_OPENING_SQL,CId);

		setProgress(1.0);

	}

	public int finish() throws SQLException, IOException
	{
//		setup.drop(Setup.IGNORE_ERRORS);        //  drop IO schema
		dump.close();
		return SUCCESS;
	}

	public void processCollectionContents(int CId) throws Exception
	{
		throw new RuntimeException("NOT YET IMPLEMENTED");
	}

	public String getProgressText() {
		return "writing "+outputFile.getName();
	}

	protected void export(String sql, int CId)
		throws SQLException, IOException
	{
		JoPreparedStatement stm = connection.getPreparedStatement(sql,
									ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if (CId > 0) stm.setInt(1,CId);
		stm.setFetchSize(Integer.MIN_VALUE);//	hint to MySQL driver: use streaming ResultSet
		stm.execute();

		ResultSet res = stm.getResultSet();
//		if (!MySQLAdapter.isDynamicResultSet(res))
//			throw new SQLException("must not use static ResultSet");

		resultInfo = dump.openResultSet(res);
		dump.writeResultSet(stm.getResultSet(), resultInfo);
		dump.closeResultSet();
	}


	protected void copy(String sql, String key, int CId)
		throws SQLException
	{
		sql = StringUtil.replace(sql,"%key%",key);
		JoPreparedStatement stm = connection.getPreparedStatement(sql);
		stm.setInt(1,CId);
		stm.execute();
	}

	public int done(int state)
	{
		switch (state) {
		case SUCCESS:		break;
		case FAILURE:
		case ABORTED:
		case ERROR:			/**	clean up the rubble	*/
							outputFile.delete();
							break;
		}

		return super.done(state);
	}
}
