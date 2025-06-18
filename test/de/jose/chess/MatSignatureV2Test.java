package de.jose.chess;

import de.jose.db.*;
import de.jose.db.crossover.Crossover1011;
import de.jose.pgn.BinReader;
import de.jose.pgn.PosSearchRecord;
import de.jose.pgn.PositionFilter;
import de.jose.util.BitUtil;
import org.junit.jupiter.api.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static de.jose.chess.Constants.*;
import static de.jose.chess.MatSignatureV2.PAWN_MASK;
import static de.jose.chess.MatSignatureV2.longBoard;
import static de.jose.pgn.BinReader.REPLAY;
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
        public int moves, noisy, early;
        public PosSearchRecord cutoff;
        public long backtrackSum;
        public int backtrackWatermark;

        public CountingBinReader(Position position) {
            super(position);
        }

        public void reset() {
            moves = noisy = early = 0;
            backtrackSum = backtrackWatermark = 0;
        }

        @Override
        public void afterMove(Move mv, int ply) {
            moves++;
            if (!pos.wasSilent()) {
                noisy++;
                if (cutoff!=null) {
                    if (cutoff.cutOff(pos,!pos.wasSilent())) eof=true;
                    MatSignature matSig = pos.getMatSig();
                    if (matSig instanceof MatSignatureV2) {
                        int backtrackCount = ((MatSignatureV2) matSig).getBacktrackCount();
                        backtrackWatermark = Math.max(backtrackWatermark, backtrackCount);
                        backtrackSum += backtrackCount;
                    }
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

    @AfterAll
    static void tearDown() throws Exception {
        MySQLAdapter adapter = (MySQLAdapter) JoConnection.getAdapter(false);
        if (adapter != null) {
            JoConnection conn = JoConnection.get();
            //adapter.shutDown(conn.getJdbcConnection());
            adapter.shutDown(conn);

        }
    }


    void withDBServer() throws Exception {
        if (JoConnection.getAdapter(false)==null)
            Crossover1011.launchDBServer();
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
    void testPrint() {
        String fen = "2r1r1n1/p2b1p1k/2nq3p/1pp1pPbB/3pP3/PP1P3P/1BPN1PQK/R5R1 w - - 3 23";
        pos.setup(fen);
        MatSignatureV2 sig = (MatSignatureV2) pos.getMatSig().clone();

        long wsig = sig.getWhiteSignature()&PAWN_MASK;
        long bsig = sig.getBlackSignature()&PAWN_MASK;
        System.out.println(longBoard(wsig,'P'));
        System.out.println(longBoard(bsig,'p'));

        assertEquals(
                "7. . . . \n" +
                "6 . . . .\n" +
                "5. . .P. \n" +
                "4 . .P. .\n" +
                "3PP.P. .P\n" +
                "2 .P. P .\n" +
                " abcdefgh\n",longBoard(wsig,'P'));
        assertEquals(
                "7p . .p. \n" +
                "6 . . . p\n" +
                "5.pp p . \n" +
                "4 . p . .\n" +
                "3. . . . \n" +
                "2 . . . .\n" +
                " abcdefgh\n", longBoard(bsig,'p'));

        //  black rotated
        long brotsig = (BitUtil.reverseBits(bsig)>>16)&PAWN_MASK;
        assertTrue((brotsig&~PAWN_MASK) == 0);
        assertEquals(Long.bitCount(bsig),Long.bitCount(brotsig));
        System.out.println(longBoard(brotsig,'p'));
        assertEquals("7. . . . \n" +
                "6 . . . .\n" +
                "5. . p . \n" +
                "4 . p pp.\n" +
                "3p . . . \n" +
                "2 .p. . p\n" +
                " abcdefgh\n",longBoard(brotsig,'p'));
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
        //  the very same position, but with estimated advance
        MatSignatureV2 from = new MatSignatureV2(0x196000008800063L,0x196334080000000L);
        assertFalse(from.isExact());
        //  a pawn has advanced. We can deduce that it can't move back.
        //  (if the pawn counts are equal)
        assertFalse(from.canReach(goal));
    }

    @Test
    void testReversed()
    {
        String fen1             = "3r2k1/6p1/1B2pn1p/p3p3/Pp2P1P1/5P1P/1P3K2/8 w - - 0 33";
        String fen2             = "6k1/6p1/4p2p/P3p3/1B1nP1P1/5P1P/5K2/8 b - - 0 37";
        String fen2_reversed    = "8/5k2/5p1p/1b1Np1p1/p3P3/4P2P/6P1/6K1 b - - 0 37";

        pos.setup(fen1);
        MatSignatureV2 sig1 = (MatSignatureV2) pos.computeMatSig().clone();
        pos.setup(fen2);
        MatSignatureV2 sig2 = (MatSignatureV2) pos.computeMatSig().clone();
        pos.setup(fen2_reversed);
        MatSignatureV2 sig2rev = (MatSignatureV2) pos.computeMatSig().clone();

        assertTrue(sig1.canReach(sig2));

        assertEquals(sig2,sig2.reverse().reverse());

        assertEquals(sig2.getWhiteSignature(), sig2.reverse().reverse().getWhiteSignature());
        assertEquals(sig2.getBlackSignature(), sig2.reverse().reverse().getBlackSignature());

        assertTrue(canReach(sig1,sig2,1));

        assertEquals(sig2rev,sig2.cloneReversed());
        assertEquals(sig2,sig2rev.cloneReversed());

        assertTrue(sig1.canReach(sig2));
        assertTrue(sig1.canReachReversed(sig2rev));
    }

    @Test
    void testIllegal()
    {
        //  illegal positions that can not be reached from the initial setup
        //  dense block of pawns
        assertFalse(isLegal("7k/8/8/8/8/P7/PP6/7K b - - 0 1"));
        //  too many promoted pieces
        assertFalse(isLegal("3qq2k/8/8/8/8/8/PPPPPPPP/7K b - - 0 1"));
        assertFalse(isLegal("4b2k/3b4/8/8/8/8/PPPPPPPP/7K b - - 0 1"));
        //  more captures than possible victims
        assertFalse(isLegal("rbnk1bnr/3P4/3P4/3P4/3P4/3P3P/3P3P/7K b - - 0 1"));
    }

    boolean canReach(String from, String to, int backtracks) {
        pos.setup(from);
        MatSignatureV2 sig1 = (MatSignatureV2) pos.computeMatSig().clone();
        pos.setup(to);
        MatSignatureV2 sig2 = (MatSignatureV2) pos.computeMatSig().clone();
        return canReach(sig1, sig2, backtracks);
    }

    boolean isLegal(String to) {
        pos.setup(to);
        MatSignatureV2 sig = (MatSignatureV2) pos.computeMatSig();
        return sig.isLegal();
    }

    boolean canReach(MatSignatureV2 sig1, MatSignatureV2 sig2, int backtracks) {
        sig1.print(System.out,WHITE,true);
        System.out.println("\n-->");
        sig2.print(System.out,WHITE,true);
        System.out.println("\n");
        boolean result = sig1.canReach(sig2);
        System.out.println("[backtracks="+sig1.backtrack+"]");
        if (backtracks >= 0)
            assertEquals(backtracks,sig1.backtrack);
        System.out.println("\n\n");
        return result;
    }

    @Test
    void testPawnCapture() {
        //  note: h-pawn compensates for advance counting,
        //  s.t. we walk into the resolve_pawns() branch

        //  a backward pawn
        assertFalse(canReach("7k/8/8/3P4/8/8/7P/7K w - - 0 1","7k/8/8/8/3P4/7P/8/7K w - - 0 1", 2));
        //  an extra pawn
        assertFalse(canReach("6rk/8/8/3P4/8/7P/7P/7K w - - 0 1","7k/8/8/3P4/3P4/8/7P/7K w - - 0 1", 1));
        //  a less obvious backward pawn
        assertFalse(canReach("7k/3P4/3P4/8/3P4/8/7P/7K w - - 0 1","7k/3P4/8/3P4/3P4/7P/8/7K w - - 0 1", 2));
        //  ...with an explanation by capture
        assertTrue(canReach("r6k/3P4/3P4/8/3P4/8/2P4P/7K w - - 0 1","7k/3P4/8/3P4/3P4/7P/8/7K w - - 0 1", 2));
        //  two captures required
        assertTrue(canReach("7k/3pp3/8/8/8/8/2PP4/7K w - - 0 1","7k/8/8/8/8/3PP3/8/7K w - - 0 1", 3));

        //  15 captures on a-file. can be resolved unambiguously
        assertTrue(canReach("rnbqkbnr/pppppppp/8/8/8/8/PPPPPP2/7K w - - 0 1",   "4k3/P7/P7/P7/P7/P7/P7/7K w - - 0 1", 5));
        //  mirrored
        assertTrue(canReach("rnbqkbnr/pppppppp/8/8/8/8/2PPPPPP/7K w - - 0 1",   "4k3/7P/7P/7P/7P/7P/7P/7K w - - 0 1", 5));
        //  same, but fails by counting victims -> we need no backtracking at all
        assertFalse(canReach("rnbqkbnr/ppppppp1/8/8/8/8/PPPPPP2/7K w - - 0 1",   "4k3/P7/P7/P7/P7/P7/P7/7K w - - 0 1", 5));

        //  9 captures on d-file; fails by capture count, but only after exhaustive backtracking !
        assertFalse(canReach("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/7K w - - 0 1", "r1bq1bnr/3P4/P2P1kP1/3P4/3P4/3P4/3P4/7K w - - 0 1", 245));
    }

    @Disabled("requires a Gigabase")
    @Test
    void testDBGames() throws Exception
    {
        withDBServer();
        ResultSet rs = selectGames(0,80000);
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

    @Disabled("benchmark on MatSignature efficiency; requires a Gigabase")
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
        int searchFlags = PosSearchRecord.POS_EXACT;
        withDBServer();
//        System.out.println("[unfiltered - all games]");
//        testCutoff(null,null, 0,0);
//        System.out.println("[unfiltered - 1M games]");
//        testCutoff(null,null, 14800000,1000000);
//        System.out.println("[initial - V1]");
//        testCutoff(initial,MatSignatureV1.class, offset,limit);
//        System.out.println("[initial - V2]");
//        testCutoff(initial,MatSignatureV2.class, offset,limit);
//        System.out.println("[opening - V1]");
//        testCutoff(opening,MatSignatureV1.class, offset,limit);
        System.out.println("[opening - V2]");
        testCutoff(opening,MatSignatureV2.class,searchFlags, offset,limit);
//        System.out.println("[middle game - V1]");
//        testCutoff(middle1,MatSignatureV1.class, offset,limit);
       System.out.println("[middle game - V2]");
        testCutoff(middle1,MatSignatureV2.class,searchFlags, offset,limit);
//        System.out.println("[middle game - V1]");
 //       testCutoff(middle2,MatSignatureV1.class, offset,limit);
        System.out.println("[middle game - V2]");
        testCutoff(middle2,MatSignatureV2.class,searchFlags, offset,limit);
//        System.out.println("[end game - V1]");
//        testCutoff(endgame1,MatSignatureV1.class, offset,limit);
        System.out.println("[end game - V2]");
        testCutoff(endgame1,MatSignatureV2.class,searchFlags, offset,limit);
//        System.out.println("[end game - V1]");
//        testCutoff(endgame2,MatSignatureV1.class, offset,limit);
        System.out.println("[end game - V2]");
        testCutoff(endgame2,MatSignatureV2.class,searchFlags, offset,limit);
    }

    @Disabled
    @Test
    void testDBPawnSearchCutoff2() throws Exception {
        String endgame1 = "2K5/4kp2/7p/8/B4P2/8/8/8 b - - 0 63";
        //  test effectiveness of early cutoffs.
        //  compare:
        //  - exact position search (expected to have best selectivity)
        //  - exact pawn search (expected to have less selectivity)
        //  - subset pawn search (expected to have least selectivity)
        withDBServer();

        int offset = 0;
        int limit = 1000000;

        System.out.println("[exact position search]");
        testCutoff(endgame1,MatSignatureV2.class, PosSearchRecord.POS_EXACT, offset,limit);
        int early1 = counter.early;

        System.out.println("[pawn search]");
        testCutoff(endgame1,MatSignatureV2.class, PosSearchRecord.PAWNS_EXACT, offset,limit);
        int early2 = counter.early;

        System.out.println("[pawn subset search]");
        testCutoff(endgame1,MatSignatureV2.class, PosSearchRecord.PAWNS_SUBSET, offset,limit);
        int early3 = counter.early;

        assertTrue(early1 >= early2);
        assertTrue(early2 >= early3);
    }

    @Test
    void test1PawnCutoff()
    {
        String endgame1 = "2K5/4kp2/7p/8/B4P2/8/8/8 b - - 0 63";
        MatSignatureV2 queryExact = getMatSignatureV2(endgame1);
        MatSignatureV2 queryPawns = (MatSignatureV2) queryExact.clone();
        queryPawns.clearOfficers();
        queryPawns.addJokerPieces();

        MatSignatureV2 querySubset = (MatSignatureV2) queryExact.clone();
        querySubset.clearOfficers();
        querySubset.addJokerPieces();
        querySubset.addJokerPawns();

        MatSignatureV2 endSig = new MatSignatureV2(0x2444000000000000L,0x3440000000000000L);

        boolean exactCanReach = queryExact.canReach(endSig);
        boolean pawnsCanReach = queryPawns.canReach(endSig);
        boolean subsetCanReach = querySubset.canReach(endSig);

        //  if it is reacable by exact comparison
        assertTrue(exactCanReach);
        //  it must also be reachable by pawns (=more relaxed) comparison)
        assertTrue(pawnsCanReach);
        //  and by pawn subset (even more relaxed)
        assertTrue(subsetCanReach);
    }

    @Disabled
    @Test
    void testDBPawnCutoffs() throws Exception {
        String endgame1 = "2K5/4kp2/7p/8/B4P2/8/8/8 b - - 0 63";
        withDBServer();

        MatSignatureV2 queryExact = getMatSignatureV2(endgame1);
        MatSignatureV2 queryPawns = (MatSignatureV2) queryExact.clone();
        queryPawns.clearOfficers();
        queryPawns.addJokerPieces();

        MatSignatureV2 querySubset = (MatSignatureV2) queryExact.clone();
        querySubset.clearOfficers();
        querySubset.addJokerPieces();
        querySubset.addJokerPawns();

        int exactCutoffs = 0;
        int pawnsCutoffs = 0;
        int subsetCutoffs = 0;
        int games=0;

        ResultSet res = selectSignatures(0,-1);
        while(res.next()) {
            games++;
            int GId = res.getInt(1);
            long whiteSig = res.getLong(2);
            long blackSig = res.getLong(3);
            MatSignatureV2 endSig = new MatSignatureV2(whiteSig,blackSig);

            boolean exactCutoff = !queryExact.canReach(endSig);
            boolean pawnsCutoff = !queryPawns.canReach(endSig);
            boolean subsetCutoff = !querySubset.canReach(endSig);

            if (exactCutoff) exactCutoffs++;
            if (pawnsCutoff) pawnsCutoffs++;
            if (subsetCutoff) subsetCutoffs++;

            //  pawns cutoff => exact cutoff
            assertTrue(!pawnsCutoff || exactCutoff, () -> ""+GId+" "+endSig.toHexString());
            //  subset cutoff => pawns cutoff
            assertTrue(!subsetCutoff || pawnsCutoff, () -> ""+GId+" "+endSig.toHexString());
        }

        System.out.println("["+games+" games]");
        System.out.println("["+exactCutoffs+" exactCutoffs]");
        System.out.println("["+pawnsCutoffs+" pawnsCutoffs]");
        System.out.println("["+subsetCutoffs+" subsetCutoffs]");
    }

    void testCutoff(String queryFen, Class matsigClass, int searchFlags, int offset, int limit) throws SQLException {
        counter.reset();
        if (queryFen != null && matsigClass!=null) {
            pos.setup(queryFen);
            pos.useMatSignature(matsigClass);
            counter.cutoff = new PosSearchRecord ();
            counter.cutoff.setSearch(pos,searchFlags);
        }
        long startTime = System.currentTimeMillis();
        int games=0;
        ResultSet res = selectGames(offset,limit);
        while(res.next()) {
            games++;
            //int GId = res.getInt(1);
            String FEN = res.getString(2);
            byte[] bin = res.getBytes(3);
            long whiteSignature = res.getLong(4);
            long blackSignature = res.getLong(5);
            MatSignatureV2 endSig = new MatSignatureV2(whiteSignature,blackSignature);
            if (counter.cutoff.earlyCutOff(endSig,false)) {
                counter.early++;
                continue;
            }

            counter.read(bin,0, null,0, FEN,REPLAY);
        }
        long time = System.currentTimeMillis()-startTime;
        System.out.println("["+games+" games replayed]");
        System.out.println("["+counter.early+" early cutoffs]");
        System.out.println("["+counter.moves+" moves]");
        System.out.println("["+counter.noisy+" noisy moves]");
        System.out.println("["+time/1000.0+" secs]");
        System.out.println("["+ counter.backtrackWatermark+" max. backtrack]");
        System.out.println("["+ ((double)counter.backtrackSum/counter.noisy)+" avg. backtrack]");
        System.out.println("\n");
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

    private static ResultSet selectSignatures(int offset, int limit) throws SQLException
    {
        JoConnection conn = JoConnection.get();
        String sql = "select GId,WhiteSignature,BlackSignature from MoreGame";
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
    void testRegressions()
    {
        //  long reversal with uppermost bit
        assertTrue(canReach("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1","5k2/p4p2/1p2q3/5RQp/6n1/4P3/PP6/6K1 w - - 0 35", 3));
        //  don't cap additional capture count
        assertTrue(canReach("r1bqkb1r/ppp2ppp/2n2n2/3Pp1N1/2B5/8/PPPP1PPP/RNBQK2R b KQkq - 0 5","r1bqr1k1/pp3pp1/2P2n1p/8/2P1p3/1NP4P/P1P1QPP1/R1B2RK1 b - - 0 16",6));
        //  subtract actual capture count
        assertTrue(canReach("r1bqkb1r/ppp2pp1/5n1p/3P4/2P1p3/5N2/PPP1QPPP/RNB1K2R b KQkq - 0 9","r1bqr1k1/pp3pp1/2P2n1p/8/2P1p3/1NP4P/P1P1QPP1/R1B2RK1 b - - 0 16", 3));

        assertTrue(canReach("rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2","rnbqkbnr/ppp1pppp/8/3p4/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 2",0));

        assertTrue(canReach("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1","1Q3Q2/5b2/6k1/q3bN2/4p2P/5pP1/5P1K/8 b - - 0 40",4));
    }

    MatSignatureV2 getMatSignatureV2(String fen) {
        pos.setup(fen);
        return (MatSignatureV2) pos.computeMatSig().clone();
    }

    @Test
    void testBishops()
    {
        //  good, bad, ugly
        assertTrue(getMatSignatureV2("rnbqk1nr/pppppppp/8/8/8/8/PPPPPPPP/RN1QKBNR w KQkq - 0 1").evenColouredBishops());
        assertTrue(getMatSignatureV2("rn1qkbnr/pppppppp/8/8/8/8/PPPPPPPP/RN1QKBNR w KQkq - 0 1").oppositeColouredBishops());

        assertTrue(getMatSignatureV2("rn1qkbnr/pppppppp/8/4P3/P2P4/2P2PP1/1P5P/RN1QKBNR w KQkq - 0 1").goodBishop(WHITE));
        assertTrue(getMatSignatureV2("rn1qkbnr/p3pppp/1p1p4/2p1P3/P2P4/2P1BPP1/1P5P/RN1QK1NR w KQkq - 0 1").badBishop(BLACK));
        assertTrue(getMatSignatureV2("rn1qkbnr/pppppppp/8/4P3/P2P4/2P1BPP1/1P5P/RN1QK1NR w KQkq - 0 1").badBishop(WHITE));
        assertTrue(getMatSignatureV2("rn1qk1nr/p2bpppp/1p1p4/2p1P3/P2P4/2P1BPP1/1P5P/RN1QK1NR w KQkq - 0 1").goodBishop(BLACK));
    }

    @Test
    void testJokers()
    {
        MatSignatureV2 from = getMatSignatureV2("7k/3pp3/8/8/8/8/2PP4/7K w - - 0 1");
        MatSignatureV2 to = getMatSignatureV2(  "7k/8/8/8/8/3PP3/8/R6K w - - 0 1");

        // can not create a piece from thin air
        assertFalse(from.canReach(to));
        //  joker pieces validates the counting argument
        from.addJokerPieces();  //  used when searching pawn structures only
        assertTrue(from.canReach(to));

        // unresolvable pawn structure
        from = getMatSignatureV2("7k/3P4/3P4/8/3P4/8/7P/7K w - - 0 1");
        to = getMatSignatureV2("7k/3P4/8/3P4/3P4/7P/8/7K w - - 0 1");
        assertFalse(from.canReach(to));
        // joker pieces make no difference
        from.addJokerPieces();
        assertFalse(from.canReach(to));
        //  but joker pawns do
        from.addJokerPawns();
        assertTrue(from.canReach(to));
    }

    @Disabled
    @Test
    void testCrossover() throws Exception {
        withDBServer();
        JoConnection conn = JoConnection.get();
        int rows = Crossover1011.updateMatSignatureV2(conn,"jose.MoreGame",-1/*2_000_000*/);
        System.out.println("["+rows+" rows updated]");
    }

    @Test
    void testMoreGameCache() throws Exception
    {
        withDBServer();

        String fen = "2K5/4kp2/7p/8/B4P2/8/8/8 b - - 0 63";
        MoreGameCache cache = new MoreGameCache();

        //  (1) read-through query
        System.out.println("\n\n[read through]");
        moreGameScan(fen, cache);
        //  (2) repeat with cached results
        System.out.println("\n\n[cached read]");
        moreGameScan(fen, cache);
    }

    @Test
    void testMoreGameCacheWarmup() throws Exception
    {
        withDBServer();

        MoreGameCache cache = new MoreGameCache();

        //  full-table scan and store in cache
        System.out.println("\n\n[warmup]");
        long time = System.currentTimeMillis();
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        assertTrue(!cache.hasFullTable());

        StringBuffer paramstm = new StringBuffer();
        paramstm.append("SELECT MoreGame.GId");
        paramstm.append("," +
                    " MoreGame.FEN, MoreGame.Bin," +
                    " MoreGame.WhiteSignature, MoreGame.BlackSignature," +
                    " 0 AS HasVariations");
        paramstm.append(" FROM MoreGame LIMIT "+Integer.MAX_VALUE/2);

        JoConnection connection = JoConnection.get();
        JoPreparedStatement prepstm = connection.getPreparedStatement(paramstm.toString());
        //  vvvv important vvvv
        prepstm.setFetchSize(Integer.MIN_VALUE);	//	hint to Connector/J driver: fetch row by row
        prepstm.execute();
        System.out.println("[executed: "+ (System.currentTimeMillis()-time) /1e3+" s]");

        ResultSet res0 = prepstm.getResultSet();
        ResultSet resa = cache.beginFullTableScan(res0);
        while(resa.next())
            ;
        resa.close();

        System.out.println("[scanned: "+ (System.currentTimeMillis()-time) /1e3+" s]");
        System.out.println("[cache "+ cache.size()+" rows]");
        memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory;
        System.out.println("[memory "+ memory /1e6+" MB]");
    }

    private void moreGameScan(String fen, MoreGameCache cache) throws SQLException
    {
        long time = System.currentTimeMillis();
        long memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        PosSearchRecord query = new PosSearchRecord();
        pos.setup(fen);
        query.setExactSearch(pos);
        PositionFilter posFilter = new PositionFilter(query);
        boolean readThrough = !cache.hasFullTable();

        StringBuffer paramstm = new StringBuffer();
        paramstm.append("SELECT MoreGame.GId");
        if (readThrough)
            paramstm.append("," +
                    " MoreGame.FEN, MoreGame.Bin," +
                    " MoreGame.WhiteSignature, MoreGame.BlackSignature," +
                    " 0 AS HasVariations");
        paramstm.append(" FROM MoreGame LIMIT "+Integer.MAX_VALUE/2);

        JoConnection connection = JoConnection.get();
        JoPreparedStatement prepstm = connection.getPreparedStatement(paramstm.toString());
        prepstm.setFetchSize(Integer.MIN_VALUE);	//	hint to Connector/J driver: fetch row by row
        prepstm.execute();
        System.out.println("[executed: "+ (System.currentTimeMillis()-time) /1e3+" s]");

        ResultSet res0 = prepstm.getResultSet();
        IntConsumer asyncCallback = (int GId) -> System.out.println("[found a result "+GId+"]");
        ResultSet resa;
        if (readThrough)
            resa = cache.beginFullTableScan(res0);
        else
            resa = cache.beginCachedScan(res0);
        while(resa.next()) {
            posFilter.accept(resa,asyncCallback);   //  schedules parallel jobs; calls back
        }
        resa.close();
        PositionFilter.executorPool.finish();

        System.out.println("[scanned: "+ (System.currentTimeMillis()-time) /1e3+" s]");
        System.out.println("[cache "+ cache.size()+" rows]");
        memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - memory;
        System.out.println("[memory "+ memory /1e6+" MB]");
    }

    private static String print(String fen, MatSignatureV2 sig) {
        return "[" + fen + "]\n" + sig + "\n" + sig.toHexString();
    }

    private void test1Game(String initFen, byte[] bin, MatSignatureV2 endSig)
    {
        reader.sigs.clear();
        reader.fens.clear();
        reader.read(bin,0, null,0, initFen, REPLAY);
        //  reachability:
        for(int i=1; i < reader.sigs.size(); i++) {
            MatSignatureV2 sigi = reader.sigs.get(i);
            String feni= reader.fens.get(i);
            pos.setup(feni);
            MatSignatureV2 sigq = new MatSignatureV2(pos);

            assertTrue(sigi.canReach(endSig));

            for (int j = 0; j <= i; ++j) {
                MatSignatureV2 sigj = (MatSignatureV2) reader.sigs.get(j);
                String fenj = reader.fens.get(j);
                Supplier<String> printInfo = () -> print(fenj,sigj)+"\n->\n"+print(feni,sigi);
                Supplier<String> printQInfo = () -> print(fenj,sigj)+"\n->\n"+print(feni,sigq);

                assertTrue(sigj.equals(sigi) || sigj.canReach(sigi),printInfo);
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