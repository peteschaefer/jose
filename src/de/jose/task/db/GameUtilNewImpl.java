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
import de.jose.pgn.Game;
import de.jose.pgn.Collection;

import java.sql.SQLException;

/**
 * @author Peter Schäfer
 */

public class GameUtilNewImpl extends GameUtil
{

	public GameUtilNewImpl(JoConnection connection)
	{
		super(connection);
	}

	/**
	 * copy one game to another collection
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

		/** get new index   */
		String sql2;
		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId)+1;

			/*	copy from game to game   */
		    sql2 =
		        "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT "+newGId+","+targetCId+","+newIdx+",Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM Game WHERE Id = "+oldGId;
		}
		else {
			/*	copy from game to game   */
		    sql2 =
		        "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT "+newGId+","+targetCId+",Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM Game WHERE Id = "+oldGId;
		}
		connection.executeUpdate(sql2);

		/*	copy from MoreGame to MoreGame		*/
	    String sql4 =
	        "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
			" SELECT "+newGId+",WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
			" FROM MoreGame WHERE GId = "+oldGId;
		connection.executeUpdate(sql4);

		addDiff(targetCId,+1);
	}



	/**
	 *  TEST use AUTO_INCREMENT
	 *  TEST adjust Game.Idx
	 * @param CId
	 * @param targetCId
	 * @throws SQLException
	 */
	public void copyCollectionGames(int CId, int targetCId, boolean calcIdx)
	    throws SQLException
	{
        int count = Collection.countGames(connection,CId);
		int newGId = Game.getSequence(connection,count);

	    /*	copy game to game   */
	    String sql3;
		if (calcIdx) {
			int newIdx = Collection.getMaxIndex(connection,targetCId);
			connection.executeUpdate("SET @newIdx="+newIdx);
			sql3 =
				"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT  @newId:=@newId+1, "+targetCId+", @newIdx:=@newIdx+1, Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM Game WHERE CId = "+CId+
				" ORDER BY Idx,Id";
                /** order by is important so that newIdx is copied correctly    */
		}
		else {
			sql3 =
				"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
				" SELECT  @newId:=@newId+1, "+targetCId+", Idx, Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO, "+
				"					EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId "+
				" FROM Game WHERE CId = "+CId+
				" ORDER BY Idx,Id";
		}

		connection.executeUpdate("SET @newId="+newGId);
		count = connection.executeUpdate(sql3);

		//  consume rest of sequence
		Game.getSequence(connection,count-1);

	    /*	copy more_game  */
	    String sql5 =
	        "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
	        " SELECT @newId:=@newId+1, WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
	        " FROM MoreGame, Game "+
	        " WHERE MoreGame.GId = Game.Id AND Game.CId = "+CId+
	        " ORDER BY Game.Idx,Id";
            /** order MUST be identical to previous statement */
		connection.executeUpdate("SET @newId="+newGId);
	    connection.executeUpdate(sql5);

		addDiff(targetCId,count);
	}


	/**
	 * TEST use AUTO_INCREMENT
	 * TEST adjust Game.Idx
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
        int count = to-from;
        int newGId = Game.getSequence(connection,count);

		/** Copy from Game INTO Game    */
		StringBuffer sql2;
        if (calcIdx) {
            int newIdx = Collection.getMaxIndex(connection, targetCId)+1;
            connection.executeUpdate("SET @newIdx="+newIdx);

            sql2 = new StringBuffer(
		        "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "   EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
		        " SELECT @newId:=@newId+1,"+targetCId+", @newIdx:=@newIdx+1 ,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "      EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId" +
		        " FROM Game "+
		        " WHERE Id IN (");
        }
        else
            sql2 = new StringBuffer(
		        "INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "   EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId) "+
		        " SELECT @newId:=@newId+1,"+targetCId+",Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		        "      EventId,SiteId,GameDate,EventDate,DateFlags,OpeningId,ECO,AnnotatorId" +
		        " FROM Game "+
		        " WHERE Id IN (");
                for (int i=from; i<to; i++) {
                    if (i > from) sql2.append(",");
                    sql2.append(String.valueOf(GIds[i]));
                }
                sql2.append(")");
                sql2.append("ORDER BY Idx,Id");

        connection.executeUpdate("SET @newId="+newGId);
		count = connection.executeUpdate(sql2.toString());

		/** Copy from MoreGame INTO MoreGame */
		StringBuffer sql4 = new StringBuffer(
		        "INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
		        " SELECT @newId:=@newId+1, WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments "+
		        " FROM MoreGame, Game" +
                " WHERE MoreGame.GId = Game.Id " +
                "   AND Game.Id IN (");
                for (int i=from; i<to; i++) {
                    if (i > from) sql4.append(",");
                    sql4.append(String.valueOf(GIds[i]));
                }
                sql4.append(")");
                sql4.append(" ORDER BY Game.Idx,Game.Id");
        /*  order MUST be identical to previous statement   */

        connection.executeUpdate("SET @newId="+newGId);
		connection.executeUpdate(sql4.toString());

		addDiff(targetCId,count);
	}

}
