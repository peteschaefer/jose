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
 * 	6 bits		0 = number of pawn moves not known
 * 	            [1..46] number of pawn moves +1
 * 	            47 = [46..48] pawn moves (very rare)
 *
 *  (2 bits)	castling rights
 *
 * 	======
 * 	61 (63) bits
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

    public boolean oppositeColouredBishops()
    {
        return      bishopCount(wfeat.sig)==1
                &&  bishopCount(bfeat.sig)==1
                &&  lightBishopCount(wfeat.sig)!=lightBishopCount(bfeat.sig);
    }

    public boolean evenColouredBishops()
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
        int padv_base = 0; //  pawn moves (including already captured and promoted pawns)
        int padv_upper = 0; //  pawn moves upper bound (including unknown promotions)

        void clear() {
            sig = 0;
            count_officers = 0;
            padv_base = 0;
            padv_upper = 0;
        }
        void copyFrom(Features that) {
            sig = that.sig;
            count_officers = that.count_officers;
            padv_base = that.padv_base;
            padv_upper = that.padv_upper;
        }

        int pawnAdvanceRemaining() {
            return 48-computePawnAdvance(sig);
        }

        private void setBoard(Board board, int color)
        {
            /** copy pawn structure */
            sig = 0;
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

        private void compute()
        {
            int ncnt = knightCount(sig);
            int lbcnt = lightBishopCount(sig);
            int dbcnt = darkBishopCount(sig);
            int rcnt = rookCount(sig);
            int qcnt = queenCount(sig);
            count_officers = ncnt+lbcnt+dbcnt+rcnt+qcnt;

            int counted_promos = 0;
            boolean more_promos=false;
            if (ncnt>=3)    { counted_promos++; more_promos = true; }
            if (lbcnt==2)   { counted_promos++; }
            if (lbcnt>=3)   { counted_promos++; more_promos = true; }
            if (dbcnt==2)   { counted_promos++; }
            if (dbcnt>=3)   { counted_promos++; more_promos = true; }
            if (rcnt>=3)    { counted_promos++; more_promos = true; }
            if (qcnt==2)    { counted_promos++; }
            if (qcnt>=3)    { counted_promos++; more_promos = true; }

            int stored_padv = get6(sig,ADV_OFFSET)-1;
            int max_promos = 8-pawnCount(sig);
            switch (stored_padv) {
                case -1: //  not known; estimate lower and upper bounds
                        padv_base = computePawnAdvance(sig) + counted_promos*6;
                        padv_upper = padv_base+max_promos*6;
                        break;
                case 46: // [46..48] rare case
                        padv_base = 46;
                        padv_upper = 48;
                        break;
                default:// exact value was stored
                        padv_base = stored_padv;
                        padv_upper = padv_base+max_promos*6;
                        assert(padv_base >= computePawnAdvance(sig));
                        break;
            }

            if (padv_base==padv_upper)
                sig |= BitUtil.set6(Math.max(47,padv_base+1),ADV_OFFSET);
        }

        public void del_pawn(int square) {
            sig = BitUtil.clear1(sig,pawnOffset(square));
        }

        public void updatePiece(Board board, int piece) {
            //  todo
        }

        public void advance_pawn(int from, int to) {
            sig = clear1(sig,pawnOffset(from));
            sig |= set1(1,pawnOffset(to));
            padv_base += (rowOf(to)-rowOf(from));
        }
    }

    //  lower 48 bits = 6 bytes hold the pawn structure
    private static long PAWN_MASK = 0x0ffffffffffffL;
    //  just the light-colored square (from a2-h2-a7-h7)
    private static long LIGHT_PAWN_MASK = 0x055aa55aa55aaL;
    private static long DARK_PAWN_MASK  = 0x0aa55aa55aa55L;

    //  next 10 bits encode piece count, 2 bits each
    private static final int KNIGHT_OFFSET        = 48;
    private static final int LIGHT_BISHOP_OFFSET  = 50;
    private static final int DARK_BISHOP_OFFSET   = 52;
    private static final int ROOK_OFFSET          = 54;
    private static final int QUEEN_OFFSET         = 56;
    //  pawn advance count (bits, may be unknown)
    private static final int ADV_OFFSET       = 58;

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
        if (to.count_officers > (from.count_officers+pcfrom)) return false; //  not enough officers
        //  todo for each officer in detail

        /** check pawn advance (lower/upper bounds) */
        if (from.padv_base > to.padv_upper) return false;  //  pawns are too advanced
        if ((from.padv_upper+from.pawnAdvanceRemaining()) < to.padv_base) return false; //  target is too advanced

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
