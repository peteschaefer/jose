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

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.ParamStatement;
import de.jose.pgn.*;
import de.jose.util.Metaphone;
import de.jose.util.StringUtil;
import de.jose.util.ClipboardUtil;
import de.jose.util.map.IntHashSet;
import de.jose.util.map.IntIntMap;
import de.jose.task.io.PGNExport;
import de.jose.task.io.PGNImport;
import de.jose.task.GameSource;
import de.jose.Util;
import de.jose.Application;
import de.jose.chess.Position;
import de.jose.chess.Move;

import javax.swing.text.BadLocationException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.awt.datatransfer.Clipboard;
import java.awt.*;
import java.io.StringWriter;
import java.io.StringReader;
/*
<
	Collection
>
0,     Field     ,      Type      , Null ,  Key , Default, Extra
1, 'Id'          , 'int(11)'      , ''   , 'PRI', '0'    , ''
2, 'PId'         , 'int(11)'      , ''   , 'MUL', '0'    , ''
3, 'OPId'        , 'int(11)'      , 'YES', ''   , null   , ''
4, 'Name'        , 'varchar(255)' , ''   , 'MUL', ''     , ''
5, 'Path'        , 'varchar(255)' , 'YES', 'MUL', null   , ''
6, 'Attributes'  , 'tinyint(4)'   , ''   , ''   , '0'    , ''
7, 'SourceURL'   , 'varchar(255)' , 'YES', 'MUL', null   , ''
8, 'LastModified', 'timestamp(14)', 'YES', 'MUL', null   , ''
9, 'GameCount'   , 'int(11)'      , ''   , ''   , '0'    , ''

<
	Game
>
0 ,     Field    ,     Type     , Null ,  Key , Default, Extra
1 , 'Id'         , 'int(11)'    , ''   , 'PRI', '0'    , ''
2 , 'CId'        , 'int(11)'    , ''   , 'MUL', '0'    , ''
3 , 'Idx'        , 'int(11)'    , ''   , 'MUL', '0'    , ''
4 , 'Attributes' , 'tinyint(4)' , ''   , 'MUL', '0'    , ''
5 , 'OCId'       , 'int(11)'    , 'YES', ''   , null   , ''
6 , 'OIdx'       , 'int(11)'    , 'YES', ''   , null   , ''
7 , 'PlyCount'   , 'int(11)'    , ''   , ''   , '0'    , ''
8 , 'Result'     , 'tinyint(4)' , ''   , 'MUL', '0'    , ''
9 , 'WhiteId'    , 'int(11)'    , ''   , 'MUL', '0'    , ''
10, 'BlackId'    , 'int(11)'    , ''   , 'MUL', '0'    , ''
11, 'WhiteELO'   , 'int(11)'    , 'YES', 'MUL', null   , ''
12, 'BlackELO'   , 'int(11)'    , 'YES', 'MUL', null   , ''
13, 'EventId'    , 'int(11)'    , ''   , 'MUL', '0'    , ''
14, 'SiteId'     , 'int(11)'    , ''   , 'MUL', '0'    , ''
15, 'GameDate'   , 'date'       , 'YES', 'MUL', null   , ''
16, 'EventDate'  , 'date'       , 'YES', 'MUL', null   , ''
17, 'DateFlags'  , 'smallint(6)', ''   , ''   , '0'    , ''
18, 'OpeningId'  , 'int(11)'    , ''   , 'MUL', '0'    , ''
19, 'ECO'        , 'char(3)'    , 'YES', 'MUL', null   , ''
20, 'AnnotatorId', 'int(11)'    , ''   , 'MUL', '0'    , ''

<
	MoreGame
>
0,    Field    ,      Type     , Null ,  Key , Default, Extra
1, 'GId'       , 'int(11)'     , ''   , 'PRI', '0'    , ''
2, 'WhiteTitle', 'varchar(32)' , 'YES', 'MUL', null   , ''
3, 'BlackTitle', 'varchar(32)' , 'YES', 'MUL', null   , ''
4, 'Round'     , 'varchar(32)' , 'YES', 'MUL', null   , ''
5, 'Board'     , 'varchar(32)' , 'YES', 'MUL', null   , ''
6, 'FEN'       , 'varchar(128)', 'YES', ''   , null   , ''
7, 'Info'      , 'varchar(255)', 'YES', 'MUL', null   , ''
8, 'Bin'       , 'mediumblob'  , 'YES', ''   , null   , ''
9, 'Comments'  , 'mediumtext'  , 'YES', 'MUL', null   , ''

*/
/**
 * static utilitiy methods for CopyTask, MoveTask, etc.
 *
 * Note that INSERT ... SELECT
 * into the same tables requires MySQL 4.0.14 or later.
 *
 */
public class GameUtilOldImpl extends GameUtil
{

	protected GameUtilOldImpl (JoConnection connection)
	{
		super(connection);
	}

    /**
     * copy one game to another collection
     * requires DB schemas IO and IO_Map
     *
     * @param oldGId the game
     * @param targetCId target collection
     * @throws SQLException
     */
	public void copyGame(int oldGId, int targetCId, boolean calcIdx)
		throws SQLException
	{
		/*	get new sequence	*/
		int newGId = Game.getSequence(connection);

	    /** copy to io_game */
	    connection.executeUpdate("DELETE FROM IO_Game");
	    String sql1 = "INSERT INTO IO_Game "+
	                    " SELECT Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
			            " 	EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
	                    " FROM Game WHERE Id = "+oldGId;
	    connection.executeUpdate(sql1);

		/*	copy from io_game to game   */
        String sql2;
		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId)+1;
			sql2 =
				"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT "+newGId+","+targetCId+","+newIdx+",Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM IO_Game";
		}
		else {
			sql2 =
				"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT "+newGId+","+targetCId+",Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM IO_Game";
		}
		connection.executeUpdate(sql2);

	    /** copy to io_moregame */
	    String sql3 =
	           "INSERT INTO IO_MoreGame "+
	           " SELECT GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
	           " FROM MoreGame WHERE GId = "+oldGId;
	    connection.executeUpdate(sql3);

		/*	copy from IO_MoreGame to MoreGame		*/
        String sql4 =
            "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
			" SELECT "+newGId+",WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
			" FROM IO_MoreGame";
		connection.executeUpdate(sql4);

		addDiff(targetCId,+1);
	}


    /**
     * TODO adjust Game.Idx
     * requires DB schemas IO and IO_Map
     *
     * @param CId
     * @param targetCId
     * @throws SQLException
     */
    public void copyCollectionGames(int CId, int targetCId, boolean calcIdx)
        throws SQLException
    {
	    int minId = connection.selectInt("SELECT MIN(Id) FROM Game WHERE CId = "+CId);
	    int maxId = connection.selectInt("SELECT MAX(Id) FROM Game WHERE CId = "+CId);

	    int newGId = Game.getSequence(connection, maxId-minId+1);
		int offsetId = newGId-minId;

	    /** copy to io_game */
	    connection.executeUpdate("DELETE FROM IO_Game");
	    String sql2 =
	         "INSERT INTO IO_Game "+
	          " SELECT Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO," +
	          "   EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId" +
	          " FROM Game "+
	          " WHERE CId = "+CId;
	    connection.executeUpdate(sql2);

        /*	copy io_game to game   */
        String sql3 =
            "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
            "					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
            " SELECT Id+"+offsetId+","+targetCId+",Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
            "					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
            " FROM IO_Game ";
		int count = connection.executeUpdate(sql3);

	    /** copy to io_moregame */
	    connection.executeUpdate("DELETE FROM IO_MoreGame");
	    String sql4 =
	          "INSERT INTO IO_MoreGame" +
	            " SELECT GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments " +
	            " FROM MoreGame,Game " +
	            " WHERE MoreGame.GId = Game.Id " +
	            "   AND Game.CId = "+CId;
	    connection.executeUpdate(sql4);

        /*	copy more game  */
        String sql5 =
            "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
            " SELECT GId+"+offsetId+",WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
            " FROM IO_MoreGame ";
        connection.executeUpdate(sql5);

        connection.executeUpdate("DELETE FROM Map_Game");
	    connection.executeUpdate("DELETE FROM IO_Game");
	    connection.executeUpdate("DELETE FROM IO_MoreGame");

	    addDiff(targetCId,count);   //getGameCount(targetCId));
    }

	/**
	 * TODO adjust Game.Idx
	 * copy a set of games to another collection
	 *
	 * @param GIds array of Game Ids
	 * @param from
	 * @param to
	 * @param targetCId target collection
	 * @throws SQLException
	 */
	public void copyGames(int[] GIds, int from, int to, int targetCId, boolean calcIdx)
		throws SQLException
	{
		/*	store new Ids into Map_Game	*/
        connection.executeUpdate("DELETE FROM Map_Game");
        int newGId = Game.getSequence(connection,to-from);

        ParamStatement pstm1 = new ParamStatement();
        pstm1.insert.append("Map_Game VALUES ");
        for (int i=from; i<to; i++) {
            if (i > from) pstm1.insert.append(",");
            pstm1.insert.append("(?,?)");
            pstm1.addIntParameter(GIds[i]);
            pstm1.addIntParameter(newGId+i-from);
        }
        pstm1.execute(connection);

		/**
		 * Note that we can not INSERT INTO Game ... SELECT FROM Game
		 * because this is not allowed by MySQL !
		 *
		 * we have to copy into IO_Game first ;-(
		 */
		connection.executeUpdate("DELETE FROM IO_Game");
		StringBuffer sql1 = new StringBuffer(
		        "INSERT INTO IO_Game "+
		        " SELECT Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
		        "    EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId " +
		        " FROM Game " +
		        " WHERE Id IN (");
		for (int i=from; i<to; i++) {
			if (i > from) sql1.append(",");
			sql1.append(String.valueOf(GIds[i]));
		}
		sql1.append(")");
		connection.executeUpdate(sql1.toString());

		/** Copy from IO_Game INTO Game    */
		String sql2 =
		        "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "   EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
		        " SELECT Map_Game.NId,"+targetCId+",Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "      EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId" +
		        " FROM IO_Game, Map_Game "+
		        " WHERE IO_Game.Id = Map_Game.OId";
		connection.executeUpdate(sql2);

		/** copy from MoreGame to IO_MoreGame   */
		connection.executeUpdate("DELETE FROM IO_MoreGame");
		StringBuffer sql3 = new StringBuffer(
		    "INSERT INTO IO_MoreGame "+
		    " SELECT GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
		    " FROM MoreGame WHERE GId IN (");
		for (int i=from; i<to; i++) {
			if (i > from) sql3.append(",");
			sql3.append(String.valueOf(GIds[i]));
		}
		sql3.append(")");
		connection.executeUpdate(sql3.toString());

		/** Copy from IO_MoreGame INTO MoreGame */
		String sql4 =
		        "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
		        " SELECT Map_Game.NId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
		        " FROM IO_MoreGame, Map_Game "+
		        " WHERE IO_MoreGame.GId = Map_Game.OId";
		connection.executeUpdate(sql4);

        connection.executeUpdate("DELETE FROM Map_Game");
		connection.executeUpdate("DELETE FROM IO_Game");
		connection.executeUpdate("DELETE FROM IO_MoreGame");

		addDiff(targetCId,to-from);
	}


}
