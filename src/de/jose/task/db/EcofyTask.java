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

import de.jose.Application;
import de.jose.Language;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.JoStatement;
import de.jose.pgn.BinReader;
import de.jose.pgn.ECOClassificator;
import de.jose.pgn.Game;
import de.jose.profile.UserProfile;
import de.jose.task.MaintenanceTask;
import de.jose.util.Metaphone;
import de.jose.util.StringUtil;
import de.jose.util.map.IntHashMap;
import de.jose.util.map.IntMap;
import de.jose.util.map.ObjIntMap;
import de.jose.view.input.LanguageList;
import de.jose.window.JoDialog;
import de.jose.window.JoFrame;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 *  Classifies ECO opening codes & names
 *
 *  TODO use a GameIterator to simplify processing
 *
 * @author Peter Schäfer
 */
public class EcofyTask
        extends MaintenanceTask
{
    protected EcofyBinReader reader;
    protected Position pos;

    protected ECOClassificator classificator;

    protected boolean preserveECO;
    protected boolean preserveName;

    protected String language;

    protected boolean inVariation;
    protected int currentCode;

    protected Metaphone sndx;
	/** maps opening names to Opening.Ids   */
	protected ObjIntMap openingMap = new ObjIntMap(1024,0.7f);
	/** set of rows that need to be updated */
	protected IntHashMap updateBuffer;

	protected String handlerName;
	protected int gHandlerCount = 1;

	static final int HANDLER_LIMIT   = 256;
	static final int BUFFER_LIMIT    = 256;

	/** list of pending updates */
	protected static class UpdateRow
    {
		/** Game Fields */
		int Id, CId, Idx, Attributes, OCId,OIdx, 
			PlyCount, Result, WhiteId, BlackId, 
			WhiteELO, BlackELO,
		    EventId, SiteId;
		Date GameDate, EventDate;
		int DateFlags, OpeningId;
		String eco;
		int AnnotatorId;
		
		String openingName = null;
		boolean updateEco = false;
		boolean updateOpening = false;
		boolean resolveOpening = false;

		protected int readResult(ResultSet res, int i) throws SQLException
		{
			Id = res.getInt(i++);
			CId = res.getInt(i++);
			Idx = res.getInt(i++);
			Attributes = res.getInt(i++);
			OCId = res.getInt(i++);
			OIdx = res.getInt(i++);
			PlyCount = res.getInt(i++);
			Result = res.getInt(i++);
			WhiteId = res.getInt(i++);
			BlackId = res.getInt(i++);
			WhiteELO = res.getInt(i++);
			BlackELO = res.getInt(i++);
			EventId = res.getInt(i++);
			SiteId = res.getInt(i++);
			GameDate = res.getDate(i++);
			EventDate = res.getDate(i++);
			DateFlags = res.getInt(i++);
			OpeningId = res.getInt(i++);
			eco = res.getString(i++);
			AnnotatorId = res.getInt(i++);
			return i;
		}

		protected int setParameters(JoPreparedStatement stm, int i) throws SQLException
		{
			stm.setInt(i++,Id);
			stm.setInt(i++,CId);
			stm.setInt(i++,Idx);
			stm.setInt(i++,Attributes);
			stm.setIntNull(i++,OCId);
			stm.setIntNull(i++,OIdx);
			stm.setInt(i++,PlyCount);
			stm.setInt(i++,Result);
			stm.setInt(i++,WhiteId);
			stm.setInt(i++,BlackId);
			stm.setIntNull(i++,WhiteELO);
			stm.setIntNull(i++,BlackELO);
			stm.setInt(i++,EventId);
			stm.setInt(i++,SiteId);
			stm.setDate(i++,GameDate);
			stm.setDate(i++,EventDate);
			stm.setInt(i++,DateFlags);
			stm.setInt(i++,OpeningId);
			stm.setString(i++,eco);
			stm.setInt(i++,AnnotatorId);
			return i;
		}
	};


	protected static final int GAME_PARAM_COUNT = 20;

    class EcofyBinReader extends BinReader
    {
        protected boolean inVariation;

        EcofyBinReader(Position pos)    { super(pos); }

        public void startOfLine(int nestLevel) {
            inVariation = (nestLevel > 0);
            if (nestLevel==0) {
                int result = classificator.lookup(pos);
                if (result!=ECOClassificator.NOT_FOUND) currentCode = result;
            }
        }

        public void endOfLine(int nestLevel) {
            inVariation = (nestLevel > 1);
        }

        public void afterMove(Move mv, int ply) {
	        if (ply >= 30 && !classificator.isReachable(pos.getMatSig())) {
		        eof = true;
		        //  terminal matsig is not reachable anymore
	        }
	        else {
                int result = classificator.lookup(pos);
                if (result!=ECOClassificator.NOT_FOUND) currentCode = result;
	        }
        }

        public void beforeMove(Move mv, int ply, boolean displayHint) { /* no-op */ }
        public void comment(StringBuffer text)    { /* no-op */ }
        public void result(int resultCode)  { /* no-op */ }
        public void annotation(int nagCode) { /* no-op */ }

    }


    public EcofyTask() throws Exception
    {
        super("ecofy",true);
    }

	public boolean askParameters(JoFrame frontFrame, UserProfile profile)
	{
		//  input elements
		HashMap values = Application.theUserProfile.settings;

		JoDialog dialog = new JoDialog("dialog.eco",true);

		Vector availableLanguages =
		        Language.getAvailableEcoLanguages(Application.theApplication.theLanguageDirectory);
		LanguageList languageList = new LanguageList(availableLanguages);
		dialog.reg("dialog.eco.language",languageList);

		Box box = Box.createHorizontalBox();
		box.add(JoDialog.newLabel("dialog.eco.language"));
		box.add(languageList);

		Container cont = dialog.getElementPane();
		dialog.add(cont,1, JoDialog.newCheckBox("dialog.eco.clobber.eco"));
		dialog.add(cont,1, JoDialog.newCheckBox("dialog.eco.clobber.name"));
		dialog.add(cont,1, box);

		dialog.addButton(JoDialog.OK);
		dialog.addButton(JoDialog.CANCEL);
		dialog.center(240,160);
		dialog.read(values);
		dialog.show();

		if (!dialog.wasCancelled())
		{
			dialog.save(values);

			preserveECO = ! dialog.getBooleanValue("dialog.eco.clobber.eco");
			preserveName = ! dialog.getBooleanValue("dialog.eco.clobber.name");
			language = (String)dialog.getValue("dialog.eco.language");

			return true;
		}
		else
			return false;
	}

    public void prepare() throws Exception
    {
        pos = new Position();
        pos.setOption(Position.INCREMENT_HASH,          true);
        pos.setOption(Position.INCREMENT_REVERSED_HASH, true);
	    pos.setOption(Position.INCREMENT_SIGNATURE,     true);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,    false);

        pos.setOption(Position.CHECK,                   false);
        pos.setOption(Position.EXPOSED_CHECK,           false);
        pos.setOption(Position.DRAW_3,                  false);
        pos.setOption(Position.DRAW_50,                 false);

        reader = new EcofyBinReader(pos);

        classificator = Application.theApplication.getClassificator(language);
        sndx = new Metaphone(6);

	    updateBuffer = new IntHashMap(400,0.7f);
    }

	private boolean appendCondition(StringBuffer sql, String table, boolean more)
	{
        if (preserveECO && preserveName)
        {
			/** retrieve only games where at least one is missing */
            sql.append (more ? " AND ":" WHERE ");
            sql.append(" (");
			sql.append(table);
			sql.append(".ECO IS NULL");
			sql.append(" OR ");
			sql.append(table);
			sql.append(".ECO = ''");
            sql.append(" OR ");
            sql.append(table);
            sql.append(".OpeningId = 0");
            sql.append(") ");
            return true;
        }
		else
		    return false;
        /** retrieve all games, because ECO or name need to be written */
	}

	public void processGame(Game game) throws Exception
	{
		//  ecofy one game
		game.ecofy(classificator,!preserveECO,!preserveName);
	}

    public void processGame(int GId) throws Exception
    {
        StringBuffer sql = new StringBuffer("SELECT * FROM Game WHERE Id = ");
	    sql.append(String.valueOf(GId));
        appendCondition(sql,"Game",true);

        processGames(sql.toString());
    }

    public void processGames(int[] GId, int from, int to) throws Exception
    {
        StringBuffer sql = new StringBuffer("SELECT * FROM Game WHERE Id IN (");

        sql.append(GId[from++]);
        while (from < to) {
            sql.append(",");
            sql.append(GId[from++]);
        }

        sql.append(")");
        appendCondition(sql,"Game",true);

        processGames(sql.toString());
    }

    public void processCollection(int CId) throws Exception
    {
        processCollectionContents(CId);
    }

    public void processCollectionContents(int CId) throws Exception
    {
	    /**
	     * use MySQL HANDLER to traverse the Game table
	     * it is vastly more efficient with large tables
	     *
	     * we could use a SELECT with LIMIT but, unfortunately, the LIMIT clause
	     * causes a full index scan that gets more and more expensive.
	     * (working with large result sets in MySQL is a pain in the ass !!)
	     *
	     * the drawback is now, that we have to fetch data from MoreGame manually
	     */
	    try {
		    handlerName = "EcofyRead"+(gHandlerCount++);
	        getConnection().executeUpdate("HANDLER Game OPEN AS "+handlerName);

		    JoStatement stm = new JoStatement(getConnection());
		    StringBuffer buf = new StringBuffer("HANDLER "+handlerName+" ");
		    buf.append("READ Game_15 = ("+CId+") ");
		    buf.append(" WHERE CId = "+CId+" ");
		    appendCondition(buf,handlerName,true);
			buf.append(" LIMIT "+HANDLER_LIMIT);

		    stm.executeQuery(buf.toString());
			//  INDEX Game_15 ON Game(CId,Id)

		    buf.setLength(0);
		    buf.append("HANDLER "+handlerName+" READ Game_15 NEXT ");
		    buf.append(" WHERE CId = "+CId+" ");
		    appendCondition(buf,handlerName,true);
		    buf.append(" LIMIT "+HANDLER_LIMIT);
		    String sql = buf.toString();

		    while (processGames(stm.getResultSet()))
		    {
			    stm.executeQuery(sql);
			    if (isAbortRequested()) break;
		    }

	    } finally {
		    getConnection().executeUpdate("HANDLER "+handlerName+" CLOSE");
	    }

    }

	protected boolean processGames(String sql) throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = getConnection().getPreparedStatement(sql);
			pstm.execute();
			return processGames(pstm.getResultSet());
		} finally {
			if (pstm!=null) pstm.close();
		}
	}

	protected boolean processGames(ResultSet res) throws SQLException
	{
		boolean any = false;
		while (res.next()) {
			any = true;

			UpdateRow row = new UpdateRow();
			row.readResult(res,1);

			row.updateEco       = ! (preserveECO && row.eco!=null && row.eco.length() > 0);
			row.updateOpening   = ! (preserveName && row.OpeningId > 0);

			if (row.updateEco || row.updateOpening)
				updateBuffer.put(row.Id, row);
			//  else: skip that row
			processedGames++;
			if (updateBuffer.size() >= BUFFER_LIMIT) flushUpdate();
		}

		return any;
	}

	public int finish() throws Exception
	{
		flushUpdate();
		return SUCCESS;
	}

	protected void flushUpdate() throws SQLException
	{
		if (!updateBuffer.isEmpty())
			fetchBinaryData();

		if (!updateBuffer.isEmpty())
			resolveOpeningNames();

		if (!updateBuffer.isEmpty())
			fillMissingOpeningNames();

		if (!updateBuffer.isEmpty())
			updateGames();

		//  reset buffer
		updateBuffer.clear();
	}

	private void fetchBinaryData() throws SQLException
	{
		/** (1) fetch MoreGame.Bin and MoreGame.FEN */
		StringBuffer sql = new StringBuffer("SELECT GId,FEN,Bin FROM MoreGame WHERE GId IN (");
		IntMap.IntIterator i = updateBuffer.keyIterator();
		if (i.hasNext())
			sql.append(String.valueOf(i.nextInt()));
		while (i.hasNext())
		{
			sql.append(",");
			sql.append(String.valueOf(i.nextInt()));
		}
		sql.append(")");

		/** (2) get MoreGame.FEN and Bin    */
		JoPreparedStatement pstm = null;
		try {
			pstm = getConnection().getPreparedStatement(sql.toString());
			pstm.execute();

            while (pstm.next()) {
				int GId = pstm.getInt(1);
				String fen = pstm.getString(2);
				byte[] bin = pstm.getBytes(3);

				UpdateRow row = (UpdateRow)updateBuffer.get(GId);

				currentCode = ECOClassificator.NOT_FOUND;
                reader.read(bin,0, null,0, fen,true);

	            if (currentCode==ECOClassificator.NOT_FOUND)
	                updateBuffer.remove(GId);   //  no use updating
				else {
					if (row.updateEco) {
						String newEco = classificator.getEcoCode(currentCode,3);
						row.updateEco = !newEco.equals(row.eco);
						row.eco = newEco;
					}

					if (row.updateOpening) {
						row.openingName = classificator.getOpeningName(currentCode);
						//  openingId is resolved later
					}
				}

	            if (!row.updateEco && !row.updateOpening)
		            updateBuffer.remove(GId);
			}

        } finally {
			if (pstm!=null) pstm.close();
		}
	}

	private void resolveOpeningNames() throws SQLException
	{
		/** (1) resolve known opening names   */
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT Id, Name FROM Opening WHERE Name IN (");

		Iterator i = updateBuffer.valueIterator();
		int j=1;
		while (i.hasNext()) {
			UpdateRow row = (UpdateRow)i.next();
			if (row.updateOpening) {
				int newOpeningId = openingMap.getInt(row.openingName);
				if (newOpeningId > 0) {
					/** resolved from memory !   */
					row.updateOpening = (newOpeningId!=row.OpeningId);
					row.OpeningId = newOpeningId;
				}
				else {
					/** resolve from database   */
					if (j++ > 1) sql.append(",");
					sql.append("?");
					row.resolveOpening = true;
				}
			}

			if (!row.updateEco && !row.updateOpening)
				i.remove();
		}
		sql.append(")");

		/** (2) read unknown from database  */
		if (j > 1) {
			JoPreparedStatement stm = null;
			try {
				stm = getConnection().getPreparedStatement(sql.toString());
				j=1;
				i = updateBuffer.valueIterator();
				while (i.hasNext()) {
					UpdateRow row = (UpdateRow)i.next();
					if (row.resolveOpening)
						stm.setString(j++,row.openingName);
				}

				stm.execute();

				while (stm.next()) {
					int openingId = stm.getInt(1);
					String openingName = stm.getString(2);
					openingMap.put(openingName,openingId);
					/** are filled into UpdateBuffer later  */
				}

			} finally {
				if (stm!=null) stm.close();
			}
		}
	}

	private void fillMissingOpeningNames() throws SQLException
	{
		StringBuffer sql = new StringBuffer();
//          %DELAYED% ?
		sql.append("INSERT INTO Opening (Id,Name,Soundex) VALUES ");

		/** (2) fill in missing names   */
		Iterator i = updateBuffer.valueIterator();
		int j=0;
		while (i.hasNext()) {
			UpdateRow row = (UpdateRow)i.next();
			if (row.resolveOpening) {
				int newOpeningId = openingMap.getInt(row.openingName);
				if (newOpeningId < 0) {
					//  mark for insert
					row.OpeningId = -1;
					if (j > 0) sql.append(",");
					sql.append("(?,?,?)");
					j++;
				}
				else {
					row.updateOpening = (newOpeningId!=row.OpeningId);
					row.OpeningId = newOpeningId;
				}
			}

			if (!row.updateEco && !row.updateOpening) i.remove();
		}

		/** (3) insert new names into database    */
		if (!JoConnection.getAdapter().canInsertMultiRow())
			throw new SQLException("multi-row insert is required");

		JoPreparedStatement stm = null;
		if (j > 0)
			try {
				stm = getConnection().getPreparedStatement(sql.toString());

				i = updateBuffer.valueIterator();
				j = 0;
				while (i.hasNext())
				{
					UpdateRow row = (UpdateRow)i.next();

					if (row.updateOpening && row.OpeningId < 0)
					{
						row.OpeningId = getConnection().getSequence("Opening","Id");
						String name = row.openingName;
						stm.setInt(3*j+1, row.OpeningId);
						stm.setString(3*j+2, name);
						stm.setString(3*j+3, StringUtil.nvl(sndx.encode(name),"-"));
						j++;
					}
				}

				stm.execute();
				int insCount = stm.getUpdateCount();
//				System.out.println(insCount+" new entries in Opening");
			} finally {
				if (stm!=null) stm.close();
			}
	}

	private void updateGames() throws SQLException
    {
		/** (4) update Game */
/*
		String sql = "UPDATE Game SET ECO=?,OpeningId=? WHERE Id=?";
		JoPreparedStatement pstm = null;
		Iterator i = updateBuffer.valueIterator();
		try {
			pstm = getConnection().getPreparedStatement(sql);

			while (i.hasNext())
			{
				UpdateRow row = (UpdateRow)i.next();
				pstm.setString(1,row.eco);
				pstm.setInt(2,row.OpeningId);
				pstm.setInt(3,row.Id);
				pstm.execute();
			}
		} finally {
			if (pstm!=null) pstm.close();
		}
*/

//                                                   %DELAYED% ?	        
		StringBuffer sql = new StringBuffer("REPLACE INTO Game VALUES ");
		for (int i=0; i<updateBuffer.size(); i++)
		{
			if (i > 0) sql.append(",");
			sql.append("(");
			for (int j=0; j<GAME_PARAM_COUNT; j++) {
				if (j>0) sql.append(",");
				sql.append("?");
			}
			sql.append(")");
		}

		JoPreparedStatement stm = null;
		try {
			stm = getConnection().getPreparedStatement(sql.toString());
			int j=1;
			Iterator i = updateBuffer.valueIterator();
			while (i.hasNext()) {
				UpdateRow row = (UpdateRow)i.next();
				j = row.setParameters(stm,j);
			}

			stm.execute();
			int updCount = stm.getUpdateCount();

		} finally {
			if (stm!=null) stm.close();
		}
	}

}

