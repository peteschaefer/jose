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

import de.jose.chess.HashKey;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.db.JoPreparedStatement;
import de.jose.pgn.BinReader;
import de.jose.pgn.Collection;
import de.jose.pgn.Game;
import de.jose.task.MaintenanceTask;
import de.jose.util.ByteBuffer;

import java.sql.ResultSet;
import java.sql.SQLException;


/**

  (2)     CREATE TABLE Position (
              GId int not null references Game(Id),
              MainLine mediumtext,
              VarLine mediumtext)

    experimental
    + fast indexing
    + easy search (simply add a clause: MATCH ... AGAINST)
    - storage requirement about 2.2KB per game

 * @author Peter Schäfer
 */
public class CreatePositionIndex2
        extends MaintenanceTask
{
    protected CreatePosBinReader reader;
    protected Position pos;

    protected boolean inVariation;

    protected ByteBuffer mainOut;
    protected ByteBuffer varOut;
    protected ByteBuffer posOut;

    protected static final String BASE_SQL1  =
            "SELECT GId, FEN, Bin "+
            " FROM MoreGame" +
            " WHERE ";

    protected static final String BASE_SQL2  =
            "SELECT Game.Id, MoreGame.FEN, MoreGame.Bin "+
            " FROM Game, MoreGame" +
            " WHERE Game.Id = MoreGame.GId" +
//            "   AND (Game.Attributes & "+Game.POS_INDEX+") = 0 "+
            "   AND ";

    class CreatePosBinReader extends BinReader
    {
        protected boolean inVariation;

        CreatePosBinReader(Position pos)    { super(pos); }

        public void startOfLine(int nestLevel) {
            inVariation = (nestLevel > 0);
            posOut = inVariation ? varOut:mainOut;

            posOut.append('(');
            posOut.append(' ');

            if (nestLevel==0) {
                record(pos.getHashKey(), inVariation);
            }
        }

        public void endOfLine(int nestLevel) {
            posOut.append(')');
            posOut.append(' ');

            inVariation = (nestLevel > 1);
            posOut = inVariation ? varOut:mainOut;
        }

        public void afterMove(Move mv, int ply) {
            record(pos.getHashKey(), inVariation);
        }

        public void beforeMove(Move mv, int ply, boolean displayHint) { /* no-op */ }
        public void comment(StringBuffer text)    { /* no-op */ }
        public void result(int resultCode)  { /* no-op */ }
        public void annotation(int nagCode) { /* no-op */ }

    }


    public CreatePositionIndex2() throws Exception
    {
        super("create.position.index",true);
    }

    public void prepare() throws Exception
    {
        pos = new Position();
        pos.setOption(Position.INCREMENT_HASH,          true);
        pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,    true);

        pos.setOption(Position.CHECK,                   false);
        pos.setOption(Position.EXPOSED_CHECK,           false);
        pos.setOption(Position.DRAW_3,                  false);
        pos.setOption(Position.DRAW_50,                 false);
		pos.setOption(Position.DRAW_MAT,                false);

        reader = new CreatePosBinReader(pos);

        mainOut = new ByteBuffer(1024);
        varOut = new ByteBuffer(1024);
    }

    public void processGame(int GId) throws Exception
    {
        String sql = BASE_SQL1+" GId = ? ";

        processGames(sql,GId);
    }

    public void processGames(int[] GId, int from, int to) throws Exception
    {
        StringBuffer sql = new StringBuffer();
        sql.append(BASE_SQL1);
        sql.append(" GId IN (");

        sql.append(GId[from++]);
        while (from < to) {
            sql.append(",");
            sql.append(GId[from++]);
        }

        sql.append(")");

        processGames(sql.toString(),-1);
    }

    public void processCollection(int CId) throws Exception
    {
        processCollectionContents(CId);
    }

    public void processCollectionContents(int CId) throws Exception
    {
        //  get the game data
        String sql = BASE_SQL2+" Game.CId = ?";

        processGames(sql,CId);

        Collection.setAttribute(getConnection(), CId, Collection.POS_INDEX, true);
    }

    protected void processGames(String sql, int param1) throws SQLException
    {
        JoPreparedStatement pstm1 = null;
        try {
            pstm1 = getConnection().getPreparedStatement(sql);
            if (param1 > 0) pstm1.setInt(1,param1);
            pstm1.execute();
            ResultSet res = pstm1.getResultSet();

            while (res.next()) {
                int GId = res.getInt(1);
                String fen = res.getString(2);
                byte[] bin = res.getBytes(3);

                mainOut.setLength(0);
                varOut.setLength(0);

                reader.read(bin,0, null,0, fen,true);

                Game.updatePositionIndex(getConnection(), GId,
                        mainOut.getValue(),mainOut.length(),
                        varOut.getValue(),varOut.length());
            }

        } finally {
            if (pstm1!=null) pstm1.closeResult();
        }
    }

    public int done(int state) {
        System.out.println(getClass().getName()+": "+((double)getElapsedTime()/1000.0));
        return super.done(state);
    }



    protected void record(HashKey key, boolean variation)
    {
        HashKey.encode(key.value(),posOut,8);
        posOut.append(' ');
    }
}
