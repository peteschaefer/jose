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

package de.jose.sax;

import de.jose.export.ExportContext;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.util.map.IntHashSet;
import de.jose.util.map.IntHashMap;
import de.jose.util.map.IntIntMap;
import org.xml.sax.SAXException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Peter Schäfer
 */

public class CrossTable extends TournamentInfo
{
	private static final String SQL_ALL_RESULTS = 
	        "select whiteid,blackid, result "+
			" from game "+
			" where game.cid = ? "+
            "  and game.result >= 0";
	private static final String SQL_CROSS_ROWS =
			"select gp.pid, "+
			" player.name, "+
			" sum(case when gp.result<0 then 0 else gp.result end) as total, "+
			" sum(case when gp.result<0 then 0 else 1 end) as gcount "+
			"from game, gameplayer gp, player "+
			"where game.cid = ? "+
			"  and game.id = gp.gid "+
			"  and player.id = gp.pid "+
			"group by gp.pid "+
			"order by total desc, gcount asc, player.name asc";

	private static class PlayerRecord
	{
		int rank;       //  tournament rank (0...)
		int id;         //  Player.Id
		String name;    //  Player.Name
		int total;      //  total point * 2
		int gameCount;  //  total number of games
	}


	public CrossTable(int collectionId)
	{
		super(collectionId);
	}

	public void toSAX(JoContentHandler handler, ExportContext context, JoConnection dbConnection) throws SAXException, SQLException
	{
		/* TODO */
		handler.startElement("cross-table");

			IntHashSet allPlayers = showGeneralInfo(dbConnection,handler);

			/** get ranking list    */
			ResultSet rows = null;
			ArrayList rankList = new ArrayList();   //  players, sorted by rank
			IntIntMap rank2id = new IntIntMap();    //  maps rank to Player.Id
			IntIntMap id2rank = new IntIntMap();    //  maps Player.Id to rank

			try {
				rows = getRankList(dbConnection);
				for (int rank=0; rows.next(); rank++)
				{
					PlayerRecord rec = new PlayerRecord();
					rec.rank = rank;
					rec.id = rows.getInt(1);
					rec.name = rows.getString(2);
					rec.total = rows.getInt(3);
					rec.gameCount = rows.getInt(4);

					rankList.add(rec);
					rank2id.put(rank,rec.id);
					id2rank.put(rec.id,rank);
				}
			} finally {
				if (rows!=null) rows.close();
			}

			/** create grid */
			ResultSet results = null;
			int[][] grid = new int[rankList.size()][rankList.size()];
			for (int i=0; i<grid.length; i++)
				Arrays.fill(grid[i],-1);
			/** fill grid with results  */
			try {
				results = getResults(dbConnection);

				while (results.next())
				{
					int whiteid = results.getInt(1);
					int blackId = results.getInt(2);
					int result = results.getInt(3);

					if (result < 0) continue;

					int whiteRank = id2rank.get(whiteid);
					int blackRank = id2rank.get(blackId);

					grid[whiteRank][blackRank] = result;
					grid[blackRank][whiteRank] = 2-result;
				}

			} finally {
				if (results!=null) results.close();
			}

			/** print grid  */
			handler.startElement("table");
				handler.startElement("head");
					for (int i=0; i < rankList.size(); i++)
						showPlayer(handler, (PlayerRecord)rankList.get(i));
				handler.endElement("head");

				handler.startElement("body");
					for (int row=0; row < grid.length; row++)
					{
						handler.startElement("row");

						showPlayer(handler, (PlayerRecord)rankList.get(row));

						for (int col=0; col < grid[row].length; col++)
						{
							if (col==row)
								handler.element("c","x");
							else switch (grid[row][col])
							{
							case 0:     handler.element("c","0"); break;
							case 1:     handler.element("c","0.5"); break;
							case 2:     handler.element("c","1"); break;
							default:    handler.element("c",""); break;
							}
						}
						handler.endElement("row");
					}
				handler.endElement("body");

			handler.endElement("table");

		handler.endElement("cross-table");
	}

	private void showPlayer(JoContentHandler handler, PlayerRecord rec) throws SAXException
	{
		handler.startElement("player");

			handler.element("rank", rec.rank+1);
			handler.element("id",rec.id);
			handler.element("name",rec.name);
			handler.element("total", String.valueOf(rec.total/2.0));
			handler.element("gameCount",rec.gameCount);

		handler.endElement("player");
	}

	private ResultSet getResults(JoConnection dbConnection) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_ALL_RESULTS);
		stm.setInt(1,CId);
		stm.execute();
		return stm.getResultSet();
	}


	private ResultSet getRankList(JoConnection dbConnection) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_CROSS_ROWS);
		stm.setInt(1,CId);
		stm.execute();
		return stm.getResultSet();
	}
}
