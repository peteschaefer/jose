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
import de.jose.task.GameHandler;
import de.jose.pgn.PgnDate;
import de.jose.pgn.PgnUtil;
import de.jose.util.map.IntHashSet;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Date;

import org.xml.sax.SAXException;

/**
 * @author Peter Schäfer
 */

public class PairingList
        extends TournamentInfo
{
	private static String SQL_GAME_INFO =
			"select game.id, "+
			" round, board, "+
			" gamedate, dateflags, "+
			" whiteid, white.name, whiteelo, whitetitle, "+
			" blackid, black.name, blackelo, blacktitle, "+
			" result "+
			" from game, moregame, player white, player black "+
			" where cid =? "+
			" and game.id = moregame.gid"+
			" and game.whiteid = white.id"+
			" and game.blackid = black.id"+
			" order by round,board,gamedate ";
	private static String SQL_MISSING_PLAYERS =
	        "select player.id, player.name "+
			" from player "+
			" where id in ";//...


	public PairingList(int collectionId)
	{
		super(collectionId);
	}

	public void toSAX(JoContentHandler handler, ExportContext context, JoConnection dbConnection) throws SQLException, SAXException
	{
		handler.startElement("pairing-list");

			IntHashSet allPlayers = showGeneralInfo(dbConnection, handler);

			//  game list
			handler.startElement("game-list");

				ResultSet games = null;
				String prevRound = null;
				IntHashSet missingPlayers = null;

				try {
					games = getGameInfo(dbConnection,CId);
					while (games.next())
					{
						int id = games.getInt(1);
						String round = games.getString(2);
						String board = games.getString(3);
						Date date = games.getDate(4);
						short dateFlags = games.getShort(5);
						PgnDate gameDate = PgnDate.toPgnDate(date,dateFlags);

						int whiteId = games.getInt(6);
						String whiteName = games.getString(7);
						int whiteElo = games.getInt(8);
						String whiteTitle = games.getString(9);

						int blackId = games.getInt(10);
						String blackName = games.getString(11);
						int blackElo = games.getInt(12);
						String blackTitle = games.getString(13);

						int result = games.getInt(14);

						if (prevRound==null || !prevRound.equals(round))
						{
							//  start next round
							if (prevRound!=null) {
								showMissingPlayers(handler,dbConnection, missingPlayers);
								handler.endElement("round");
							}
							prevRound = round;
							handler.startElement("round");
							missingPlayers = new IntHashSet(allPlayers);
							handler.element("description",round);
							handler.element("date",(gameDate==null) ? "":gameDate.toLocalDateString(true));
						}

						//  one game
						handler.startElement("game");
							handler.element("id",id);
							handler.element("board",board);
							handler.element("date",(gameDate==null) ? "":gameDate.toLocalDateString(true));
							handler.element("result", PgnUtil.resultString(result));

							handler.element("white-id",whiteId);
							handler.element("white-name",whiteName);
							handler.element("white-title",whiteTitle);
							handler.element("white-elo",whiteElo);

							handler.element("black-id",blackId);
							handler.element("black-name",blackName);
							handler.element("black-title",blackTitle);
							handler.element("blakc-elo",blackElo);

							missingPlayers.remove(whiteId);
							missingPlayers.remove(blackId);
						handler.endElement("game");
					}
				} finally {
					if (games!=null) games.close();
				}

				if (prevRound!=null) {
					showMissingPlayers(handler,dbConnection, missingPlayers);
					handler.endElement("round");
				}


			handler.endElement("game-list");

		handler.endElement("pairing-list");
	}

	private void showMissingPlayers(JoContentHandler handler, JoConnection dbConnection, IntHashSet playerSet) throws SQLException, SAXException
	{
		if (playerSet==null || playerSet.isEmpty()) return;

		ResultSet players = null;
		try {
			players = getMissingPlayerInfo(dbConnection,playerSet);

			while (players.next())
			{
				int id = players.getInt(1);
				String name = players.getString(2);

				handler.startElement("bye");
					handler.element("id",String.valueOf(id));
					handler.element("name",name);
				handler.endElement("bye");
			}

		} finally {
			if (players!=null) players.close();
		}
	}


	private ResultSet getMissingPlayerInfo(JoConnection dbConnection, IntHashSet players) throws SQLException
	{
		StringBuffer sql = new StringBuffer(SQL_MISSING_PLAYERS);
		players.appendString(sql,"(,)");

		JoPreparedStatement stm = dbConnection.getPreparedStatement(sql.toString());
		stm.execute();
		return stm.getResultSet();
	}
	private ResultSet getGameInfo(JoConnection dbConnection, int CId) throws SQLException
	{
		JoPreparedStatement stm = dbConnection.getPreparedStatement(SQL_GAME_INFO);
		stm.setInt(1,CId);
		stm.execute();
		return stm.getResultSet();
	}

}
