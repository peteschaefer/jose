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
import java.util.function.Supplier;

import static de.jose.chess.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

class MatSignatureV2Test {

    class TestBinReader extends BinReader
    {
        public ArrayList<MatSignatureV2> sigs = new ArrayList<>();
        public ArrayList<String> fens = new ArrayList<>();

        public TestBinReader(Position position) {
            super(position);
        }

        @Override
        public void afterMove(Move mv, int ply) {
            if (!pos.wasSilent()) {
                //  silent moves do not modify the signature
                MatSignatureV2 matSig = (MatSignatureV2) pos.getMatSig();
                assertTrue(matSig.matches(pos), () -> mv+": "+pos+" != "+matSig+" "+matSig.toHexString());
                if (getNestLevel()==0) {    //  don't record variations; our list is flat
                    sigs.add((MatSignatureV2) matSig.clone());
                    fens.add(pos.toString());
                }
            }
        }

        @Override public void beforeMove(Move mv, int ply, boolean displayHint) { }
        @Override public void annotation(int nagCode) { }
        @Override public void comment(StringBuffer text) { }
        @Override public void startOfLine(int nestLevel) { }
        @Override public void endOfLine(int nestLevel) { }
        @Override public void result(int resultCode) { }
    }

    class CountingBinReader extends BinReader
    {
        public int moves, noisy;
        public MatSignature cutoff;

        public CountingBinReader(Position position) {
            super(position);
        }

        @Override
        public void afterMove(Move mv, int ply) {
            moves++;
            if (!pos.wasSilent()) {
                noisy++;
                if (cutoff!=null) {
                    MatSignature matSig = pos.getMatSig();
                    if (!matSig.canReach(cutoff)) eof=true;
                }
            }
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
    CountingBinReader counter;

    public MatSignatureV2Test() {
        sig = new MatSignatureV2();
        pos = new Position(JoseHashKey.class,MatSignatureV2.class);
        pos.setOption(Position.CHECK,false);
        pos.setOption(Position.STALEMATE,false);
        pos.setOption(Position.DRAW_3,false);
        pos.setOption(Position.INCREMENT_HASH,false);
        pos.setOption(Position.INCREMENT_REVERSED_HASH,false);
        pos.setOption(Position.EXPOSED_CHECK,false);
        pos.setOption(Position.INCREMENT_SIGNATURE,true);
        reader = new TestBinReader(pos);
        counter = new CountingBinReader(pos);
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
            System.setProperty("java.library.path","lib/Linux_amd64");
        if (Version.windows)
            System.setProperty("java.library.path",".;lib/Windows");
        System.setProperty("jose.splash","off");
        System.setProperty("jose.console.output","true");
        System.setProperty("java.awt.headless","true");

        System.setProperty("jose.db","MySQL-standalone");
        System.setProperty("jose.db.port","3306");
        System.setProperty("jose.datadir","C:\\dev\\jose\\packages\\jose-152-windows\\jose\\database");
        System.setProperty("jose.splash","false");
        System.setProperty("jose.console.output","true");
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
        test1(START_POSITION, "[5960000000000ff-596ff0000000000]", "[8/8/8/8/8/PPPPPPPP 2N 1+1B 2R 1Q 0 - pppppppp/8/8/8/8/8 2n 1+1b 2r 1q 0]");
        test1(EMPTY_POSITION, "[0-0]", "[8/8/8/8/8/8 ? - 8/8/8/8/8/8 ?]");
        test1("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2", "[d960000001000ef-d96ef0010000000]", "[8/8/8/4P3/8/PPPP1PPP 2N 1+1B 2R 1Q 2 - pppp1ppp/8/4p3/8/8/8 2n 1+1b 2r 1q 2]");
    }

    @Test
    void testBlackHomerow() throws Exception {
        String fen1 = "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1";
        String fen2 = "2r2rk1/p5pp/1q3n2/3N1p2/3p4/5PP1/P1p1P1BP/4RR1K w - - 0 26";

        pos.setup(fen1);
        MatSignatureV2 sig1 = (MatSignatureV2) pos.getMatSig().clone();

        pos.setup(fen2);
        MatSignatureV2 sig2 = (MatSignatureV2) pos.getMatSig().clone();

        assertTrue(sig1.canReach(sig2), () -> fen1+"="+sig1+" -> "+fen2+"="+sig2);
    }

    @Test
    void testExcessivePromotions() throws Exception {
        String fen = "QQ3QQ1/8/8/5K2/3k4/8/8/8 b - - 0 81";
        pos.setup(fen);
        MatSignatureV2 sig = (MatSignatureV2) pos.getMatSig();

        assertTrue(sig.matches(pos), () -> fen+" != "+sig);
    }

    @Test
    void testPieceCount() {
        MatSignatureV2 sig1 = new MatSignatureV2(0x3196000008800063L,0x2196334080000000L);
        MatSignatureV2 sig2 = new MatSignatureV2(0x3196000008800063L,0x2195334080000000L);
        //  a black officer was captured. not enough pieces
        assertFalse(sig2.canReach(sig1));
    }

    @Test
    void test4Queens() {
        String fen = "Q4Q2/3B4/6Q1/3K4/8/8/7Q/7k b - - 0 96"; // [8/8/8/8/8/8 1+0B 2Q 35 - 8/8/8/8/8/8 17]
        pos.setup(fen);
        //  four queens (recorded as 3Q)
        //  one gets captured
        MatSignatureV2 sig = (MatSignatureV2) pos.getMatSig();
        assertTrue(pos.tryMove(new Move(H1,H2)));
        //  3-1=3 :)
        assertTrue(sig.matches(pos));
    }

    @Test
    void testPawnAdvance() {
        //  r1bqkb1r/pp1ppp2/2n3p1/4P2p/2Bp2nP/2P2N2/PP3PP1/RNBQK2R w KQkq - 0 8
        MatSignatureV2 goal = new MatSignatureV2(0x2d96000000880063L,0x2196334080000000L);
        assertTrue(goal.isExact());
        //  the very same position, but with estimated
        MatSignatureV2 from = new MatSignatureV2(0x196000008800063L,0x196334080000000L);
        assertFalse(from.isExact());
        //  a pawn has advanced. We can deduce that it can't move back.
        //  (if the pawn counts are equal)
        assertFalse(from.canReach(goal));
    }

    @Test
    void testDBGames() throws Exception
    {
        withDBServer();
        ResultSet rs = selectGames(15800000,80000);
        int i;
        for (i=0; rs.next(); ++i) {
            int GId = rs.getInt(1);
            String FEN = rs.getString(2);
            byte[] bin = rs.getBytes(3);
            long whiteSignature = rs.getLong(4);
            long blackSignature = rs.getLong(5);

            //System.out.println(GId);
            MatSignatureV2 endSig = new MatSignatureV2(whiteSignature,blackSignature);
            test1Game(FEN,bin,endSig);
            /*  note that test1Game() test only MatSignatures from the same game (with monotonous pawn advance, etc.)
                a better test would use an arbitrary query (see below)
             */
        }
        System.out.println("["+i+" games replayed]");
    }

    @Test
    void testCutoffCount() throws Exception
    {
        String initial = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String opening = "rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/5NP1/PP2PP1P/RNBQKB1R b KQkq - 0 4";
        String middle1 = "2r1r1n1/p2b1p1k/2nq3p/1pp1pPbB/3pP3/PP1P3P/1BPN1PQK/R5R1 w - - 3 23";
        String middle2 = "2rqr1k1/pb3pp1/7p/1p1pN2n/4P3/1B5P/P4PPQ/3RR1K1 w - - 0 21";
        String endgame1 = "2K5/4kp2/7p/8/B4P2/8/8/8 b - - 0 63";
        String endgame2 = "8/8/R1bkp3/3p1rKp/3B4/2P5/8/8 w - - 10 67";

        int offset = 14800000;
        int limit = 1000000;
        withDBServer();
//        System.out.println("[unfiltered - all games]");
//        testCutoff(null,null, 0,0);
//        System.out.println("[unfiltered - 1M games]");
//        testCutoff(null,null, 14800000,1000000);
//        System.out.println("[initial - V1]");
//        testCutoff(initial,MatSignatureV1.class, offset,limit);
//        System.out.println("[initial - V2]");
//        testCutoff(initial,MatSignatureV2.class, offset,limit);
//        System.out.println("[middle game - V1]");
//        testCutoff(middle1,MatSignatureV1.class, offset,limit);
        System.out.println("[middle game - V2]");
        testCutoff(middle1,MatSignatureV2.class, offset,limit);
//        System.out.println("[end game - V1]");
//        testCutoff(endgame1,MatSignatureV1.class, offset,limit);
        System.out.println("[end game - V2]");
        testCutoff(endgame1,MatSignatureV2.class, offset,limit);
    }

    void testCutoff(String queryFen, Class matsigClass, int offset, int limit) throws SQLException {
        counter.moves = counter.noisy = 0;
        if (queryFen != null && matsigClass!=null) {
            pos.setup(queryFen);
            pos.useMatSignature(matsigClass);
            counter.cutoff = (MatSignature) pos.getMatSig().clone();
        }
        long startTime = System.currentTimeMillis();
        int games=0;
        ResultSet res = selectGames(offset,limit);
        while(res.next()) {
            games++;
            //int GId = res.getInt(1);
            String FEN = res.getString(2);
            byte[] bin = res.getBytes(3);
            //long whiteSignature = rs.getLong(4);
            //long blackSignature = rs.getLong(5);
            counter.read(bin,0, null,0, FEN,true,true);
        }
        long time = System.currentTimeMillis()-startTime;
        System.out.println("["+games+" games replayed]");
        System.out.println("["+counter.moves+" moves]");
        System.out.println("["+counter.noisy+" noisy moves]");
        System.out.println("["+time/1000.0+" secs]");
    }

    private static ResultSet selectGames(int offset, int limit) throws SQLException
    {
        JoConnection conn = JoConnection.get();
        String sql = "select GId,FEN,Bin,WhiteSignature,BlackSignature from MoreGame";
        if (offset > 0 || limit > 0) {
            sql += " limit ";
            if (offset > 0) sql += (offset+", ");
            sql += limit;
        }
        JoPreparedStatement pstm = conn.getPreparedStatement(sql);
        pstm.execute();

        ResultSet rs = pstm.getResultSet();
        return rs;
    }

    @Test
    void testDBQuery()
    {
        //  use a query MatSignature with estimated pawn advance
        //  apply to many games. What is the number of early cut-offs?
        //  Can it be improved by more detailed pawn analysis?
    }

    private static String print(String fen, MatSignatureV2 sig) {
        return "[" + fen + "]\n" + sig + "\n" + sig.toHexString();
    }

    private void test1Game(String initFen, byte[] bin, MatSignatureV2 endSig)
    {
        reader.sigs.clear();
        reader.fens.clear();
        reader.read(bin,0, null,0, initFen, true,true);
        //  reachability:
        for(int i=1; i < reader.sigs.size(); i++) {
            MatSignatureV2 sigi = reader.sigs.get(i);
            String feni= reader.fens.get(i);
            pos.setup(feni);
            MatSignatureV2 sigq = new MatSignatureV2(pos);

            for (int j = 0; j <= i; ++j) {
                MatSignatureV2 sigj = (MatSignatureV2) reader.sigs.get(j);
                String fenj = reader.fens.get(j);
                Supplier<String> printInfo = () -> print(fenj,sigj)+"\n->\n"+print(feni,sigi);
                Supplier<String> printQInfo = () -> print(fenj,sigj)+"\n->\n"+print(feni,sigq);

                assertTrue(sigj.canReach(sigi),printInfo);
                //  previous positions can not be reached (if we have an exact advance count!)
                //  (sigi!=sigj) => !sigi.canReach(sigj)
                assertTrue(sigi.equals(sigj) || !sigi.canReach(sigj),printInfo);
                //  this is not necessarily true, if the advance count is estimated.
                //  try this to find border cases that might be solvable:
                assertTrue(sigq.similar(sigj) || !sigq.canReach(sigj),printQInfo);
            }
        }
    }
}