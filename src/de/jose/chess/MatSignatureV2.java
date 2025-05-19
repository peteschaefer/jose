package de.jose.chess;

import de.jose.Util;
import de.jose.util.BitUtil;
import de.jose.view.MoveGesture;

import java.util.List;

import static de.jose.chess.Constants.*;
import static de.jose.chess.EngUtil.fileOf;
import static de.jose.chess.EngUtil.rowOf;
import static de.jose.util.BitUtil.*;

/**
 * MatSignature is a compressed representation of a Position (2x64 bits).
 * It helps to determine position features quickly (using only bit arithmetics).
 * If can estimate the number of pawn moves (exact, or lower/upper bound)
 *
 * - cut-off position searches, if the query position is not reachable anymore
 * - the final MatSignature is stored in the DB to make early cut-offs
 *
 * - detect pawn structure, or subsets thereof
 * - count pieces
 *
 *
 *  6x8 bits	encode all pawns in the square a2-h2-h7-a7
 *  			- compare exact pawn structure, or subset(!)
 * 				- compute lower/upper bound for pawn advance (missing pawn = [0..6])
 * 				- reachability through analyaing origin squares (bit shifting, or-ing)
 * 				- pawn count = popcnt()
 * 				- count light squared / dark square pawns
 *
 * 	5x2 bits	[0..3] count for each piece:
 * 				knight, light-squared bishop, dark-squared bishop, rook, queen
 * 				count = 0,1,2,>2 (extermely rare case)
 * 				- sum = total piece count.
 * 					mostly exact, very rarely lower bound
 *
 * 	6 bits		[0..48] pawn advance
 * 				exact if pawncount()==8 || incremental
 * 				unknown otherwise (only in query). compute lower/upper bound from: bitset, missing pawns, known promotions
 *
 *  2 bits		castling rights
 *
 * 	======
 * 	66 bits	 oh :(	 something has to give. probably castling right b/cause it has less discriminating value.
 * 						it can be used by PosFilter nevertheless (known in query and incremental)
 *
 * 	more things to detect
 * 	- opposite bishops / even bishops
 * 	- good bishop = pawns on different color
 * 	- bad bihsop = pawns on same color
 */
public class MatSignatureV2
{
    private Features wfeat = new Features();
    private Features bfeat = new Features();

    // --------------------------------------
    //      Constructors
    // --------------------------------------

    public MatSignatureV2()
    { }

    public MatSignatureV2(long wsig, long bsig)
    {
        wfeat.sig = wsig;
        bfeat.sig = bsig;
        wfeat.compute();
        bfeat.compute();
    }

    public MatSignatureV2(MatSignatureV2 that)
    {
        wfeat.copyFrom(that.wfeat);
        bfeat.copyFrom(that.bfeat);
    }

    public MatSignatureV2(Board board) {
        setBoard(board);
    }

    public void setBoard(Board board)
    {
        clear();
        wfeat.setBoard(board,WHITE);
        bfeat.setBoard(board,BLACK);
    }

    // --------------------------------------
    //      Methods
    // --------------------------------------


    public void clear()  {
        wfeat.clear();
        bfeat.clear();
    }

    public void reverse()
    {
        Features swapf = wfeat;
        wfeat = bfeat;
        bfeat = swapf;
    }

    public boolean opposingBishops()
    {
        return      bishopCount(wfeat.sig)==1
                &&  bishopCount(bfeat.sig)==1
                &&  lightBishopCount(wfeat.sig)!=lightBishopCount(bfeat.sig);
    }

    public boolean evenBishops()
    {
        return      bishopCount(wfeat.sig)==1
                &&  bishopCount(bfeat.sig)==1
                &&  lightBishopCount(wfeat.sig)==lightBishopCount(bfeat.sig);
    }

    public boolean goodBishop(int color)
    {
        return good_bishop(EngUtil.isWhite(color) ? wfeat.sig : bfeat.sig);
    }

    public boolean badBishop(int color)
    {
        return bad_bishop(EngUtil.isWhite(color) ? wfeat.sig : bfeat.sig);
    }


    public void update(Board board, Move mv)
    {
        if (mv.isCapture())
        {
            Features fthat = EngUtil.isWhite(mv.moving.piece) ? bfeat:wfeat;
            if (mv.captured.isPawn() && EngUtil.isWhite(mv.captured.piece))
                fthat.del_pawn(mv.captured.square);
            else if (mv.captured.isPawn())
                fthat.del_pawn(EngUtil.rotateSquare(mv.captured.square)); // black pawns are rotated
            else
                fthat.updatePiece(board, mv.captured.piece);   //  decrease piece count
        }
        if (mv.moving!=null && mv.moving.isPawn())
        {
            Features fthis = EngUtil.isWhite(mv.moving.piece) ? wfeat:bfeat;
            int color = mv.moving.color();
            fthis.advance_pawn(mv.from,mv.to);
            if (mv.isPromotion())
                fthis.updatePiece(board, mv.getPromotionPiece());
        }
    }

    // --------------------------------------
    //      Static Methods
    // --------------------------------------

    public static boolean is_reachable(MatSignatureV2 from, MatSignatureV2 to)
    {
        return  is_reachable(from.wfeat, to.wfeat) &&
                is_reachable(from.bfeat, to.bfeat);
    }

    // --------------------------------------
    //      Private Parts
    // --------------------------------------


    private class Features
    {
        long sig;
        int count_officers = 0;          //  number of officers on the board; lower bound, including known promotions
        int known_promos = 0;   //  number of known promotions
        int unknown_promos = 0;   //  number of unknown promotions
        int pawn_advance_lower = 0; //  lower bound for pawn moves (including past, known promotions)
        int pawn_advance_upper = 0; //  upper bound for pawn moves (including future promotions)
        int pawn_advance_left = 48; // pawn moves left

        void compute() {
            computeOfficersAndPromos();
            computePawnAdvanceBounds();
        }
        void clear() {
            sig = 0;
            count_officers = 0;
            known_promos = 0;
            unknown_promos = 0;
            pawn_advance_lower = 0;
            pawn_advance_upper = 0;
            pawn_advance_left = 48;
        }
        void copyFrom(Features that) {
            sig = that.sig;
            count_officers = that.count_officers;
            known_promos = that.known_promos;
            unknown_promos = that.unknown_promos;
            pawn_advance_lower = that.pawn_advance_lower;
            pawn_advance_upper = that.pawn_advance_upper;
            pawn_advance_left = that.pawn_advance_left;
        }

        private void setBoard(Board board, int color)
        {
            /** copy pawn structure */
            sig = 0L;
            List<Piece> pawns = board.pieceList(EngUtil.PAWN|color);
            for(Piece p : pawns) {
                int sq = p.square();
                if (color==BLACK) sq = EngUtil.rotateSquare(sq);
                sig |= pawnAt(sq);
            }

            /** count officers; clip at 3 */
            List<Piece> bishops = board.pieceList(EngUtil.BISHOP|color);
            int lbcnt = 0;
            for(Piece p : bishops) if (EngUtil.isLightSquare(p.square())) lbcnt++;
            int dbcnt = bishops.size()-lbcnt;
            int ncnt = board.pieceList(EngUtil.KNIGHT|color).size();
            int rcnt = board.pieceList(EngUtil.ROOK|color).size();
            int qcnt = board.pieceList(EngUtil.QUEEN|color).size();

            sig |= clip2(ncnt,KNIGHT_OFFSET);
            sig |= clip2(lbcnt,LIGHT_BISHOP_OFFSET);
            sig |= clip2(dbcnt,DARK_BISHOP_OFFSET);
            sig |= clip2(rcnt,ROOK_OFFSET);
            sig |= clip2(qcnt,QUEEN_OFFSET);

            /**  is the pawn advance exact, by coincidence ? */
            this.compute();
        }

        private void computeOfficersAndPromos() {
            boolean unknown_promo = false;
            int max_promo = 8-pawnCount(sig);

            int ncnt = knightCount(sig);
            int lbcnt = lightBishopCount(sig);
            int dbcnt = darkBishopCount(sig);
            int rcnt = rookCount(sig);
            int qcnt = queenCount(sig);

            if (ncnt>=3)    { known_promos++; unknown_promo=true; }
            if (lbcnt==2)   { known_promos++; }
            if (lbcnt>=3)   { known_promos++; unknown_promo=true; }
            if (dbcnt==2)   { known_promos++; }
            if (dbcnt>=3)   { known_promos++; unknown_promo=true; }
            if (rcnt>=3)    { known_promos++; unknown_promo=true; }
            if (qcnt==2)    { known_promos++; }
            if (qcnt>=3)    { known_promos++; unknown_promo=true; }

            //  lower bound
            count_officers = ncnt+lbcnt+dbcnt+rcnt+qcnt;
            //  upper bound
            if (unknown_promo)
                unknown_promos = max_promo-known_promos;
            else
                unknown_promos = 0;
        }


        public void computePawnAdvanceBounds()
        {
            int adv = getPawnAdvance(sig); //  pawn advance, not regarding promotions
            pawn_advance_left = pawnCount(sig)*6 - adv;   //  still possible

            sig = BitUtil.clear(sig,ADVANCE_MASK);
            if (adv!=ADVANCE_UNKNOWN) {
                pawn_advance_lower = pawn_advance_upper = adv;
                sig |= BitUtil.set6(adv,ADVANCE_OFFSET);
            }
            else {
                pawn_advance_lower = computePawnAdvance(sig) + known_promos*6;   //  pawn advance with past promotions
                pawn_advance_upper = Math.min(pawn_advance_lower + unknown_promos*6, 48);
                sig |= BitUtil.set6(ADVANCE_UNKNOWN,ADVANCE_OFFSET);
            }
        }

        public void del_pawn(int square) {
            sig = BitUtil.clear1(sig,pawnOffset(square));
            //pawn_advance_left -= ;
        }

        public void updatePiece(Board board, int captured) {
            //  todo
        }

        public void advance_pawn(int from, int to) {
            //pawn_advance_lower += ;
            //pawn_advance_left -= ;
        }
    }

    //  lower 48 bits = 6 bytes hold the pawn structure
    private static long PAWN_MASK = 0x0ffffffffffffL;
    //  just the light-colored square (from a2-h2-a7-h7)
    private static long LIGHT_PAWN_MASK = 0x055aa55aa55aaL;
    private static long DARK_PAWN_MASK  = 0x0aa55aa55aa55L;

    //  next 10 bits encode piece count, 2 bits each
    private static int KNIGHT_OFFSET        = 48;
    private static int LIGHT_BISHOP_OFFSET  = 50;
    private static int DARK_BISHOP_OFFSET   = 52;
    private static int ROOK_OFFSET          = 54;
    private static int QUEEN_OFFSET         = 56;
    //  pawn advance count (uppermost 6 bits, may be unknown)
    private static int ADVANCE_OFFSET       = 58;
    private static long ADVANCE_MASK = 0xfc00000000000000L;
    private static int ADVANCE_UNKNOWN = 0x03f;

    private static int rowOffset(int row) {
        return (row-ROW_1)*8;
    }
    private static int fileOffset(int file) {
        return file-FILE_A;
    }
    private static long rowMask(int row) {
        return 0x0ffL << rowOffset(row);
    }

    private static int pawnOffset(int file, int row) {
        return rowOffset(row) + fileOffset(file);
    }

    private static int pawnOffset(int square) {
        return pawnOffset(fileOf(square), rowOf(square));
    }

    private static long pawnAt(int file, int row) {
        return 1L << pawnOffset(file,row);
    }
    private static long pawnAt(int sq)       { return pawnAt(fileOf(sq), rowOf(sq)); }

    private static int pawnRow(long sig, int row) {
        return get8(sig, (row-ROW_2)*8);
    }

    private static int pawnCount(long sig) {
        return Long.bitCount(sig & PAWN_MASK);
    }
    private static int pawnCount(long sig, int row) {
        return Long.bitCount(sig & rowMask(row));
    }

    private static int getPawnAdvance(long sig) {
        return BitUtil.get6(sig,ADVANCE_OFFSET);
    }

    private static int computePawnAdvance(long sig)
    {
        int adv = 0;
        for (int row=ROW_3; row<=ROW_7; ++row)  //  jit compiler: unroll :)
            adv += (row-ROW_2)*pawnCount(sig,row);
        return adv;
    }

    private static int ligthPawnCount(long sig) {
        return Long.bitCount(sig & LIGHT_PAWN_MASK);
    }

    private static int darkPawnCount(long sig) {
        return Long.bitCount(sig & DARK_PAWN_MASK);
    }

    private static int knightCount(long sig)        { return BitUtil.get2(sig,KNIGHT_OFFSET); }
    private static int lightBishopCount(long sig)   { return BitUtil.get2(sig,LIGHT_BISHOP_OFFSET); }
    private static int darkBishopCount(long sig)    { return BitUtil.get2(sig,DARK_BISHOP_OFFSET); }
    private static int bishopCount(long sig)        { return lightBishopCount(sig)+darkBishopCount(sig); }
    private static int rookCount(long sig)          { return BitUtil.get2(sig,ROOK_OFFSET); }
    private static int queenCount(long sig)         { return BitUtil.get2(sig,QUEEN_OFFSET); }




    private static boolean is_reachable(Features from, Features to)
    {
        /** check pawn count */
        int pcto = pawnCount(to.sig);
        int pcfrom = pawnCount(from.sig);
        if (pcto > pcfrom) return false;    //  not enough pawns

        /** check officers count    */
        if (to.count_officers > (from.count_officers+from.unknown_promos+pcfrom)) return false; //  not enough officers
        //  todo for each officer in detail

        /** check pawn advance (lower/upper bounds) */
        if (from.pawn_advance_lower > to.pawn_advance_upper) return false;  //  pawns are too advanced
        if ((from.pawn_advance_upper+from.pawn_advance_left) < to.pawn_advance_lower) return false; //  target is too advanced

        /** check pawn home row */
        long hometo = pawnRow(to.sig,ROW_2);
        long homefrom = pawnRow(from.sig,ROW_2);

        if (minus8(hometo,homefrom) != 0) return false; //  pawns must not return to the home row

        //  todo more detailed pawn reachability by looking at the potential origin squares
        //  can be done by bit shifting and or-ing.

        return true;
    }

    private static boolean good_bishop(long sig)
    {
        return     (lightBishopCount(sig)==1
                &&  darkBishopCount(sig)==0
                &&  ligthPawnCount(sig) < darkPawnCount(sig))
                ||
                   (lightBishopCount(sig)==0
                &&  darkBishopCount(sig)==1
                &&  ligthPawnCount(sig) > darkPawnCount(sig));
    }

    private static boolean bad_bishop(long sig)
    {
        return     (lightBishopCount(sig)==1
                &&  darkBishopCount(sig)==0
                &&  ligthPawnCount(sig) > darkPawnCount(sig))
                ||
                   (lightBishopCount(sig)==0
                &&  darkBishopCount(sig)==1
                &&  ligthPawnCount(sig) < darkPawnCount(sig));
    }

}
