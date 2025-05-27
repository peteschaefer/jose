package de.jose.chess;

import de.jose.Application;
import de.jose.Config;
import de.jose.Version;
import de.jose.db.*;
import de.jose.pgn.BinReader;
import de.jose.pgn.PositionFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static de.jose.chess.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

class MatSignatureV2Test {

    class TestBinReader extends BinReader
    {
        public ArrayList<MatSignatureV2> sigs = new ArrayList<>();

        public TestBinReader(Position position) {
            super(position);
        }

        @Override
        public void afterMove(Move mv, int ply) {

        }

        @Override public void beforeMove(Move mv, int ply, boolean displayHint) { }
        @Override public void annotation(int nagCode) { }
        @Override public void comment(StringBuffer text) { }
        @Override public void startOfLine(int nestLevel) { }
        @Override public void endOfLine(int nestLevel) { }
        @Override public void result(int resultCode) { }
    }

    MatSignatureV2 sig;
    Position pos;
    TestBinReader reader;

    public MatSignatureV2Test() {
        sig = new MatSignatureV2();
        pos = new Position(JoseHashKey.class,MatSignatureV2.class);
        pos.setOption(Position.INCREMENT_SIGNATURE,true);
        reader = new TestBinReader(pos);
    }

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
        if (Version.linux)
            System.setProperty("java.library.path","java.library.path=lib/Linux_amd64");
        if (Version.windows)
            System.setProperty("java.library.path","java.library.path=.;lib/Windows");
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

    void test1(String fen, String hexExpected, String stringExpected) throws Exception
    {
        pos.setup(fen);
        sig.setBoard(pos);
        System.out.println(sig.toString());

        assertEquals(hexExpected,sig.toHexString());
        assertEquals(stringExpected,sig.toString());
        assertTrue(sig.matches(pos));
    }

    @Test
    void testEmpty() throws Exception {
        sig = new MatSignatureV2();
        pos = new Position();
        pos.setOption(Position.INCREMENT_SIGNATURE,true);

        test1(START_POSITION, "[5960000000000ff-596ff0000000000]", "[8/8/8/8/8/PPPPPPPP 2N 1+1B 2R 1Q 0 - pppppppp/8/8/8/8/8 2n 1+1b 2r 1q 0]");
        test1(EMPTY_POSITION, "[0-0]", "[8/8/8/8/8/8 ? - 8/8/8/8/8/8 ?]");
        test1("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "[d960000001000ef-d96ef0010000000]", "[8/8/8/4P3/8/PPPP1PPP 2N 1+1B 2R 1Q 2 - pppp1ppp/8/4p3/8/8/8 2n 1+1b 2r 1q 2]");
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
        reader.sigs.clear();
        reader.read(bin,0, null,0, initFen, true,true);
        //  reachability:
        for(int i=1; i < reader.sigs.size(); i++) {
            MatSignatureV2 sigi = (MatSignatureV2) reader.sigs.get(i);
            for (int j = 0; j <= i; ++i) {
                MatSignatureV2 sigj = (MatSignatureV2) reader.sigs.get(j);
                assertTrue(sigj.canReach(sigi));
            }
        }
    }
}