package de.jose.chess;

import de.jose.Application;
import de.jose.Config;
import de.jose.Version;
import de.jose.db.*;
import de.jose.pgn.PositionFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static de.jose.chess.Constants.START_POSITION;
import static org.junit.jupiter.api.Assertions.*;

class MatSignatureV2Test {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
        MySQLAdapter adapter = (MySQLAdapter) JoConnection.getAdapter(false);
        if (adapter != null) {
            JoConnection conn = JoConnection.get();
            //adapter.shutDown(conn.getJdbcConnection());
            adapter.shutDown(conn);

        }
    }

    void launchDBServer() throws Exception {
        System.setProperty("java.library.path","java.library.path=lib/Linux_amd64");
        System.setProperty("jose.splash","off");
        System.setProperty("jose.console.output","true");
        System.setProperty("java.awt.headless","true");
        Application app = new Application();

        MySQLAdapter adapter = (MySQLAdapter) JoConnection.getAdapter(true);
        Thread launcher = adapter.launchProcess();
        launcher.join();
        //adapter.waitForStandaloneServer();
    }

    void withDBServer() throws Exception {
        if (JoConnection.getAdapter(false)==null)
            launchDBServer();
        assertNotNull(JoConnection.getAdapter(true));
    }

    @Test
    void testEmpty()
    {
        MatSignatureV2 sig = new MatSignatureV2();
        Position pos = new Position();
        pos.setOption(Position.INCREMENT_SIGNATURE,true);

        pos.setup(START_POSITION);
        sig.setBoard(pos);
        System.out.println(sig.toHexString());
        System.out.println(sig.toString());

        pos.setup(Constants.EMPTY_POSITION);
        sig.setBoard(pos);
        System.out.println(sig.toHexString());
        System.out.println(sig.toString());
    }

    @Test
    void testDB() throws Exception
    {
        withDBServer();
        JoConnection conn = JoConnection.get();
        JoPreparedStatement pstm = conn.getPreparedStatement("select GId,FEN,Bin,WhiteSignature,BlackSignature from MoreGame limit 10");
        pstm.setMaxRows(10);
        pstm.execute();

        String queryFen = START_POSITION;
        PositionFilter pf = new PositionFilter();
        pf.setTargetPosition(queryFen,true);

        ResultSet rs = pstm.getResultSet();
        while (rs.next()) {
            int GId = rs.getInt(1);
            String FEN = rs.getString(2);
            byte[] bin = rs.getBytes(3);
            long whiteSignature = rs.getLong(4);
            long blackSignature = rs.getLong(5);
            System.out.println(GId);
            MatSignatureV2 endSig = new MatSignatureV2(whiteSignature,blackSignature);
            test1Game(pf, FEN,bin,endSig);
        }
    }

    private void test1Game(PositionFilter pf, String initFen, byte[] bin, MatSignatureV2 endSig)
    {
        PositionFilter.Result res = pf.accept(initFen,bin,endSig);
    }
}