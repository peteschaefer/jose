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

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.pgn.PgnDate;
import de.jose.util.map.IntHashSet;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;

import org.xml.sax.SAXException;

/**
 * @author Peter Schäfer
 */

public class TournamentInfo
{
	private static String SQL_EVENT_NAME =
			"SELECT Event.Name "+
			" FROM Game, Event "+
			" WHERE Game.EventId = Event.Id "+
	        "   AND Game.CId = ? "+
			" GROUP BY Event.Id ";
	private static String SQL_COUNT_ROUNDS =
	        "select count(distinct moregame.round) "+
			" from game, moregame "+
			" where moregame.gid = game.id "+
            "   and game.cid = ? ";
	private static String SQL_COMPLETE_ROUNDS =
	        "select min(round)-1 "+
			" from game, moregame "+
			" where game.id=moregame.gid "+
            "  and game.result < 0 "+
            "  and game.cid = ? ";
	private static String SQL_COUNT_PLAYERS =
	        "select count(distinct pid) "+
			" from game,gameplayer "+
			" where game.id = gameplayer.gid "+
            "   and game.cid = ? ";
	private static String SQL_FIRST_DATE =
	        "select gamedate, dateflags "+
			" from game "+
			" where cid = ? "+
			" group by gamedate "+
			" order by gamedate asc";
	private static String SQL_LAST_DATE =
	        "select gamedate, dateflags "+
			" from game "+
			" where cid = ? "+
			" group by gamedate "+
			" order by gamedate desc";
	private static String SQL_PLAYER_INFO =
	        "select player.id, player.name as PlayerName, {fn ifnull(game.whiteelo,0)}, moregame.whitetitle "+
			" from game,player,moregame "+
			" where game.whiteid = player.id "+
            "   and game.id = moregame.gid "+
            "   and game.cid = ? "+
	        " group by player.id "+
			" union "+
			"select player.id, player.name as PlayerName, {fn ifnull(game.blackelo,0)}, moregame.blacktitle "+
			" from game,player,moregame "+
			" where game.blackid = player.id "+
            " and game.id = moregame.gid "+
            " and game.cid = ? "+
			" group by player.id "+
			"order by PlayerName ";

	protected int CId;

	protected TournamentInfo(int collectionId)
	{
		this.CId = collectionId;
	}

	protected String getEventName(JoConnection dbConnection, int CId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_EVENT_NAME);
		stm.setInt(1,CId);
		return stm.selectString();
	}

	protected int countRounds(JoConnection dbConnection, int CId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_COUNT_ROUNDS);
		stm.setInt(1,CId);
		return stm.selectInt();
	}

	protected int countCompleteRounds(JoConnection dbConnection, int cId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_COMPLETE_ROUNDS);
		stm.setInt(1,CId);
		return stm.selectInt();
	}

	protected int countPlayers(JoConnection dbConnection, int CId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_COUNT_PLAYERS);
		stm.setInt(1,CId);
		return stm.selectInt();
	}

	protected PgnDate getFirstDate(JoConnection dbConnection, int CId) throws SQLException
	{
		return selectPgnDate(dbConnection,SQL_FIRST_DATE,CId);
	}

	protected PgnDate getLastDate(JoConnection dbConnection, int CId) throws SQLException
	{
		return selectPgnDate(dbConnection,SQL_LAST_DATE,CId);
	}

	protected ResultSet getPlayerInfo(JoConnection dbConnection, int CId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_PLAYER_INFO);
		stm.setInt(1,CId);
		stm.setInt(2,CId);
		stm.execute();
		return stm.getResultSet();
	}

	private PgnDate selectPgnDate(JoConnection dbConnection, String sql, int CId) throws SQLException
	{
		JoPreparedStatement stm=null;
		try {
			stm = dbConnection.getPreparedStatement(sql);
			stm.setInt(1,CId);
			stm.execute();
			if (stm.next()) {
				Date dt = stm.getDate(1);
				short flags = stm.getShort(2);
				return PgnDate.toPgnDate(dt,flags);
			}
			else
				return null;
		} finally {
			if (stm!=null) stm.closeResult();
		}
	}

	protected IntHashSet showGeneralInfo(JoConnection dbConnection, JoContentHandler handler)
	        throws SQLException, SAXException
	{
		String eventName = getEventName(dbConnection,CId);
		if (eventName==null) eventName = "?";

		int countRounds = countRounds(dbConnection,CId);
		int countPlayers = countPlayers(dbConnection,CId);
		int completedRounds = countCompleteRounds(dbConnection,CId);

		PgnDate firstDate = getFirstDate(dbConnection,CId);
		PgnDate lastDate = getLastDate(dbConnection,CId);

		IntHashSet allPlayers = new IntHashSet();

		//  general info
		handler.element("id",String.valueOf(CId));
		handler.element("event",eventName);
		handler.element("count-rounds",countRounds);
		handler.element("count-players",countPlayers);
		handler.element("complete-rounds",
		        (completedRounds < 0) ? "all" : String.valueOf(completedRounds));

		handler.element("first-date",
		        (firstDate==null||firstDate.isDateUnknown()) ? "":
		        firstDate.toLocalDateString(true));
		handler.element("last-date",
				(lastDate==null||lastDate.isDateUnknown()) ? "":
				lastDate.toLocalDateString(true));

		//  player list (sorted alphabetically)
		handler.startElement("player-list");

		ResultSet players = null;
		try {
			players = getPlayerInfo(dbConnection,CId);
			while (players.next())
			{
				int id = players.getInt(1);
				allPlayers.add(id);

				String name = players.getString(2);
				int elo = players.getInt(3);
				String title = players.getString(4);

				handler.startElement("player");
					handler.element("id",id);
					handler.element("name",name);
					handler.element("elo", (elo<=0) ? null:String.valueOf(elo));
					handler.element("title",title);
				handler.endElement("player");
			}
		} finally {
			if (players!=null) players.close();
		}

		handler.endElement("player-list");
		return allPlayers;
	}
}
