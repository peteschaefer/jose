package de.jose.chess;

import de.jose.util.BitUtil;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static de.jose.chess.EngUtil.*;
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
 *
 * 	---------------
 * 	is_reachable(a,b) establishes a relation that is:
 *
 *  + transitive
 *      is_reachable(a,b) AND is_reachable(b,c) ==> is_reachable(a,c);
 *
 *  + reflexive
 *      is_reachable(a,a) is always true
 *
 *  + anti-symmetric if pawn advance counts are exact (not estimated from a FEN)
 *
 *    is_reachable(a,b) AND (matsig(a) != matsig(b)) ==>  ! is_reachable(b,a)
 *
 *    intuitively, MatSignaure records noisy moves (captures, pawn moves) that can't be undone.
 *    Provided that the pawn move count is exact (which is not always the case if derived from a FEN).
 *    With pawn advances estimated (lower, upper bounds), it is:
 *
 *  + not symmetric
 *      is_reachable(a,b) DOES NOT IMPLY is_reachable(b,a)
 *
 *      however, there might be symmetric pairs (a,b) such that
 *      is_reachable(a,b) AND is_reachable(b,a)
 *      as a result, this relation can not be ordered
 *      (and can, sadly, not be used for indexing)
 *
 *      we try hard to reduce the number of symmetric pairs to a minimum
 *      (e.g. by not only counting moves, but by detailed pawn structure analysis)
 */
public class MatSignatureV2 implements MatSignature
{
    private Features wfeat = new Features(WHITE);
    private Features bfeat = new Features(BLACK);
    //  for debuggin
    public int backtrack=0;

    // --------------------------------------
    //      Constructors
    // --------------------------------------

    public MatSignatureV2()
    { }

    public MatSignatureV2(long wsig, long bsig) {
        init(wsig, bsig);
    }

    public MatSignatureV2(MatSignatureV2 that) {
        wfeat.copyFrom(that.wfeat);
        bfeat.copyFrom(that.bfeat);
    }

    public Object clone() {
        return new MatSignatureV2(this);
    }

    @Override
    public boolean equals(Object that) {
        return (that instanceof MatSignatureV2)
                && ((MatSignatureV2) that).wfeat.equals(wfeat,true)
                && ((MatSignatureV2) that).bfeat.equals(bfeat,true);
    }

    public MatSignature cloneReversed() {
        MatSignatureV2 result = new MatSignatureV2(this);
        result.reverse();
        return result;
    }

    public boolean isExact() {
        return wfeat.isExact() && bfeat.isExact();
    }

    public boolean similar(MatSignatureV2 that) {
        return  that.wfeat.equals(wfeat,false) && that.bfeat.equals(bfeat,false);
    }


    public MatSignatureV2(Board board) {
        setBoard(board);
    }

    public void init(long wsig, long bsig) {
        wfeat.setSignature(wsig);
        bfeat.setSignature(bsig);
    }

    public void setBoard(Board board)
    {
        clear();
        wfeat.setBoard(board);
        bfeat.setBoard(board);
    }

    public void setInitial()
    {
        init(0x5960000000000ffL,0x596ff0000000000L);
    }

    public boolean matches(Board board)
    {
        return wfeat.matches(board) && bfeat.matches(board);
    }

    // --------------------------------------
    //      Methods
    // --------------------------------------

    public long getWhiteSignature() { return wfeat.sig; }
    public long getBlackSignature() { return bfeat.sig; }
    public int getBacktrackCount() { return backtrack; }

    public void clear()  {
        wfeat.clear();
        bfeat.clear();
    }

    public void reverse()
    {
        wfeat.reverse();
        bfeat.reverse();

        Features swapf = wfeat;
        wfeat = bfeat;
        bfeat = swapf;
    }

    public boolean oppositeColouredBishops()
    {
        return      wfeat.bishopCount()==1
                &&  bfeat.bishopCount()==1
                &&  wfeat.lightBishopCount() != bfeat.lightBishopCount();
    }

    public boolean evenColouredBishops()
    {
        return      wfeat.bishopCount()==1
                &&  bfeat.bishopCount()==1
                &&  wfeat.lightBishopCount() == bfeat.lightBishopCount();
    }

    public boolean goodBishop(int color)
    {
        return EngUtil.isWhite(color) ? wfeat.good_bishop() : bfeat.good_bishop();
    }

    public boolean badBishop(int color)
    {
        return EngUtil.isWhite(color) ? wfeat.bad_bishop() : bfeat.bad_bishop();
    }

    public boolean isReachableFrom(MatSignature from) {
        return (from instanceof MatSignatureV2) && is_reachable((MatSignatureV2)from,this);
    }

    public boolean canReach(MatSignature to) {
        return (to instanceof MatSignatureV2) && is_reachable(this,(MatSignatureV2)to);
    }

    @Override
    public boolean canReachReversed(MatSignature ato) {
        if (!(ato instanceof MatSignatureV2)) return false;
        //  todo expensive. better use reversed()
        MatSignatureV2 to =(MatSignatureV2) ato;
        to.reverse();
        boolean result = is_reachable(this,to);
        to.reverse();
        return result;
    }

    public boolean isLegal()
    {
        //  position must be reachable from the initial position
        MatSignatureV2 i = new MatSignatureV2();
        i.setInitial();
        if (!i.canReach(this))
            return false;

        //  additional checks that are not part of canReach()
        //  (b/c canReach() assumes that both arguments *are* legal)

        int promo_lower = wfeat.computePromotionLowerBound();
        if (promo_lower > (8-bfeat.pawnCount()))
            return false; //  more promoted pieces that missing pawns

        promo_lower = bfeat.computePromotionLowerBound();
        if (promo_lower > (8-wfeat.pawnCount()))
            return false;  //  more promoted pieces that missing pawns

        return true;
    }

    /**
     * incremental update
     * @param mv
     */
    public void update(Board board, Move mv)
    {
        //  notify "noisy" moves: captures and pawn moves
        if (mv.isCapture())
        {
            Features fthat = EngUtil.isWhite(mv.moving.piece) ? bfeat:wfeat;
            int capturedSquare = mv.getCapturedSquare();
            if (mv.captured.isPawn())
                fthat.del_pawn(capturedSquare);
            else {
                int capturedPiece = mv.getCapturedPiece();
                fthat.del_piece(capturedPiece, capturedSquare, board);   //  decrease piece count
            }
        }
        if (mv.moving!=null && mv.moving.isPawn())
        {
            Features fthis = EngUtil.isWhite(mv.moving.piece) ? wfeat:bfeat;
            if (mv.isPromotion())
                fthis.promote(mv.from,mv.to,mv.getPromotionPiece());
            else
                fthis.advance_pawn(mv.from,mv.to);
        }
    }

    // --------------------------------------
    //      Static Methods
    // --------------------------------------

    public static boolean is_reachable(MatSignatureV2 from, MatSignatureV2 to)
    {
        from.backtrack=0;
        //  (1) check counting arguments
        boolean wreach = is_reachable(from.wfeat, to.wfeat);
        if (!wreach) return false;
        boolean breach = is_reachable(from.bfeat, to.bfeat);
        if (!breach) return false;
        //  post-conditiion: all .piece_cnt are up to date. and will be used below.

        //  (2) backtrack pawn position
        long frompawns = from.wfeat.sig&PAWN_MASK;
        long topawns = to.wfeat.sig&PAWN_MASK;
        wreach = from.wfeat.resolve_pawns(frompawns,topawns,
                15-from.bfeat.totalPieceCount(),
                from.bfeat.totalPieceCount() - to.bfeat.totalPieceCount());
        if (!wreach) return false;

        frompawns = from.bfeat.sig&PAWN_MASK;
        topawns = to.bfeat.sig&PAWN_MASK;
        if (frompawns==topawns) return true;

        frompawns = (BitUtil.reverseBits(frompawns)>>16) &PAWN_MASK;
        topawns = (BitUtil.reverseBits(topawns)>>16)&PAWN_MASK;
        breach = from.bfeat.resolve_pawns(frompawns,topawns,
                        15-from.wfeat.totalPieceCount(),
                        from.wfeat.totalPieceCount() - to.wfeat.totalPieceCount());
        //  todo should work for mirrored pawns just the same !?
        return breach;
        /** of course, the above could be placed in a single statement; split it just for better debuggability */
    }

    public MatSignatureV1 toMatSignatureV1()
    {
        long wsig1 = wfeat.toMatSignatureV1();
        long wsig2 = bfeat.toMatSignatureV1();
        return new MatSignatureV1(wsig1,wsig2);
    }

    // --------------------------------------
    //      Private Parts
    // --------------------------------------


    private class Features
    {
        public Features(int color) {
            this.color = color;
        }

        int color;
        long sig;
        //int count[] = new int[6];          //  number of officers on the board; lower bound, including known promotions
        int padv_base = 0;
        int padv_lower = 0; //  pawn moves lower bound (including already captured and promoted pawns)
        int padv_upper = 0; //  pawn moves upper bound (including unknown promotions)
        //  total piece_cnt. computed by is_reachable on demand!
        int piece_cnt = 0;

        public boolean isExact() {
            return padv_lower==padv_upper;
        }

        public boolean equals(Features that,boolean strict) {
            if (strict)
                return that.sig==this.sig;
            else
                return ((that.sig&PAWN_MASK) == (this.sig&PAWN_MASK))
                    && ((that.sig&OFFICER_MASK) == (this.sig&OFFICER_MASK))
                    && (this.padv_upper >= that.padv_lower) //  intervals overlap
                    && (this.padv_lower <= that.padv_upper);
        }

        void clear() {
            sig = 0;
            padv_base = padv_lower = padv_upper = 0;
        }
        void copyFrom(Features that) {
            assert this.color==that.color;
            sig = that.sig;
            padv_base = that.padv_base;
            padv_lower = that.padv_lower;
            padv_upper = that.padv_upper;
        }

        int knightCount()       { return MatSignatureV2.knightCount(sig); }
        int lightBishopCount()  { return MatSignatureV2.lightBishopCount(sig); }
        int darkBishopCount()   { return MatSignatureV2.darkBishopCount(sig); }
        int bishopCount()       { return lightBishopCount()+darkBishopCount(); }
        int rookCount()         { return MatSignatureV2.rookCount(sig); }
        int queenCount()        { return MatSignatureV2.queenCount(sig); }
        int officersCount()     { return knightCount()+bishopCount()+rookCount()+queenCount(); }

        int pawnCount()         { return MatSignatureV2.pawnCount(sig); }
        int lightPawnCount()    { return MatSignatureV2.lightPawnCount(sig); }
        int darkPawnCount()     { return MatSignatureV2.darkPawnCount(sig); }

        int totalPieceCount() {
            return piece_cnt+pawnCount();
        }

        void reverse()
        {
            color = EngUtil.oppositeColor(color);
            long mirroredPawns =
                        ((long)pawnRow(sig,ROW_7))
                    |   ((long)pawnRow(sig,ROW_6)) << 8
                    |   ((long)pawnRow(sig,ROW_5)) << 16
                    |   ((long)pawnRow(sig,ROW_4)) << 24
                    |   ((long)pawnRow(sig,ROW_3)) << 32
                    |   ((long)pawnRow(sig,ROW_2)) << 40;

            sig = (sig & ~PAWN_MASK) | mirroredPawns;
            //  mirror bishops
            sig = (sig & ~BISHOPS_MASK)
                    | BitUtil.clip2(lightBishopCount(),DARK_BISHOP_OFFSET)
                    | BitUtil.clip2(darkBishopCount(),LIGHT_BISHOP_OFFSET);
            //  note that all computed values (padv_ etc.) remain valid
        }

        boolean good_bishop()
        {
            return     (lightBishopCount()==1
                    &&  darkBishopCount()==0
                    &&  lightPawnCount() < darkPawnCount())
                    ||
                    (lightBishopCount()==0
                    &&  darkBishopCount()==1
                    &&  lightPawnCount() > darkPawnCount());
        }

        boolean bad_bishop()
        {
            return     (lightBishopCount()==1
                    &&  darkBishopCount()==0
                    &&  lightPawnCount() > darkPawnCount())
                    ||
                    (lightBishopCount()==0
                    &&  darkBishopCount()==1
                    &&  lightPawnCount() < darkPawnCount());
        }

        int pawnAdvanceRemaining() {
            assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
            return 6*pawnCount() - padv_base;
        }

        protected void setBoard(Board board)
        {
            /** copy pawn structure */
            sig = 0;
            List<Piece> pawnList = board.pieceList(EngUtil.PAWN|color);
            for(Piece p : pawnList) {
                if (p.isVacant()) continue;
                int sq = p.square();
                sig |= pawnAt(sq);
            }

            /** count officers; clip at 3 */
            int ncnt = board.countPieces(EngUtil.KNIGHT|color);
            int lbcnt = countLightSquaredBishops(board,color);
            int dbcnt = countDarktSquaredBishops(board,color);
            int rcnt = board.countPieces(EngUtil.ROOK|color);
            int qcnt = board.countPieces(EngUtil.QUEEN|color);

            sig |= clip2(ncnt,KNIGHT_OFFSET);
            sig |= clip2(lbcnt,LIGHT_BISHOP_OFFSET);
            sig |= clip2(dbcnt,DARK_BISHOP_OFFSET);
            sig |= clip2(rcnt,ROOK_OFFSET);
            sig |= clip2(qcnt,QUEEN_OFFSET);

            /**  is the pawn advance exact, by coincidence ? */
            this.computePawnAdvanceBounds();
        }

        protected void setSignature(long sig)
        {
            /** copy pawn structure */
            this.sig = sig;

            /**  is the pawn advance exact, by coincidence ? */
            this.computePawnAdvanceBounds();
        }

        private int computePromotionLowerBound()
        {
            int promo_lower = 0;
            if (knightCount()>=3)       promo_lower++;
            if (lightBishopCount()>=2)  promo_lower++;
            if (lightBishopCount()>=3)  promo_lower++;
            if (darkBishopCount()>=2)   promo_lower++;
            if (darkBishopCount()>=3)   promo_lower++;
            if (rookCount()>=3)         promo_lower++;
            if (queenCount()>=2)        promo_lower++;
            if (queenCount()>=3)        promo_lower++;
            return promo_lower;
        }

        private void computePawnAdvanceBounds()
        {
            int promo_lower = computePromotionLowerBound();
            int promo_upper = 8-pawnCount();
            boolean more_promos=false;
            //  todo get a second promo_lower bound from Board

            int stored_padv = pawnAdvance(sig);
            padv_base = MatSignatureV2.computePawnAdvance(sig,color);
            switch (stored_padv) {
                case -1: //  not known; estimate lower and upper bounds
                        padv_lower = padv_base + promo_lower*6;
                        padv_upper = Math.min(ADV_TOP, padv_lower +promo_upper*6);
                        break;
                case ADV_MAX: // [46..48] rare case
                        padv_lower = ADV_MAX;
                        padv_upper = ADV_TOP;
                        break;
                default:// exact value was stored
                        padv_upper = padv_lower = stored_padv;
                        break;
            }
            updatePawnAdvance();
        }

        public boolean matches(Board board)
        {
            if (pawnCount() != board.countPieces(PAWN | color)) return false;
            if (!comparePieceCount(knightCount(), board.countPieces(KNIGHT | color))) return false;
            if (!comparePieceCount(lightBishopCount(), countLightSquaredBishops(board,color))) return false;
            if (!comparePieceCount(darkBishopCount(), countDarktSquaredBishops(board,color))) return false;
            if (!comparePieceCount(rookCount(), board.countPieces(ROOK | color))) return false;
            if (!comparePieceCount(queenCount(), board.countPieces(QUEEN | color))) return false;

            List<Piece> pawnList = board.pieceList(EngUtil.PAWN|color);
            for(Piece p : pawnList) {
                if (p.isVacant()) continue;
                int sq = p.square();
                if (! BitUtil.get1(sig,pawnOffset(sq))) return false;
            }
            return true;
        }

        void print(PrintWriter buf, boolean longFormat)
        {
            printboard(buf,sig&PAWN_MASK, EngUtil.coloredPieceCharacter(PAWN|color), longFormat);

            if (knightCount() > 0) {
                buf.print(' ');
                buf.print(knightCount());
                buf.print(EngUtil.coloredPieceCharacter(KNIGHT|color));
            }
            if (bishopCount() > 0) {
                buf.print(' ');
                buf.print(lightBishopCount());
                buf.print('+');
                buf.print(darkBishopCount());
                buf.print(EngUtil.coloredPieceCharacter(BISHOP|color));
            }
            if (rookCount() > 0) {
                buf.print(' ');
                buf.print(rookCount());
                buf.print(EngUtil.coloredPieceCharacter(ROOK|color));
            }
            if (queenCount() > 0) {
                buf.print(' ');
                buf.print(queenCount());
                buf.print(EngUtil.coloredPieceCharacter(QUEEN|color));
            }

            buf.print(' ');
            if (padv_lower==0 && padv_upper==ADV_TOP) {
                buf.print('?');
            }
            else {
                buf.print(padv_lower);
                if (padv_lower != padv_upper) {
                    buf.print("..");
                    buf.print(padv_upper);
                }
            }
        }

        public void del_pawn(int square) {
            sig = BitUtil.clear1(sig,pawnOffset(square));
            padv_base -= Math.abs(rowOf(square)-EngUtil.homeRow(color));
            assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
        }

        private int countPieces(Board board, int piece, boolean is_light_square)
        {
            if (EngUtil.uncolored(piece)!=BISHOP)
                return board.countPieces(piece);
            else if (is_light_square)
                return countLightSquaredBishops(board,EngUtil.colorOf(piece));
            else
                return countDarktSquaredBishops(board,EngUtil.colorOf(piece));
        }

        public void del_piece(int piece, int square, Board board) {
            int offset = pieceOffset(piece,square);
            int cnt = get2(sig,offset);
            if (cnt==3) {
                //  note: cnt==3 means >=3
                //  we need to query the board for the new exact value
                cnt = countPieces(board,piece,EngUtil.isLightSquare(square));
            }
            else {
                cnt = Math.max(0, cnt - 1);
            }
            sig = clear2(sig,offset) | clip2(cnt,offset);
        }

        public void add_piece(int piece, int square) {
            int offset = pieceOffset(piece,square);
            int cnt = get2(sig,offset);
            cnt = Math.min(3,cnt+1);
            sig = clear2(sig,offset) | clip2(cnt,offset);
        }

        public void advance_pawn(int from, int to) {
            assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
            sig = clear1(sig,pawnOffset(from)) | set1(1,pawnOffset(to));
            int adv = Math.abs(rowOf(to)-rowOf(from));
            padv_base += adv;
            assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
            padv_lower += adv;
            padv_upper = Math.max(padv_upper, padv_lower);
            updatePawnAdvance();
       }

       public void promote(int from, int to, int promoted) {
           del_pawn(from);
           add_piece(promoted, to);
           assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
           padv_lower++;
           padv_upper = Math.max(padv_upper, padv_lower);
           updatePawnAdvance();
       }

       protected void updatePawnAdvance()
       {
           sig = BitUtil.clear6(sig,ADV_OFFSET);
           if (isExact())
               sig |= BitUtil.set6(Math.min(ADV_MAX, padv_lower +1),ADV_OFFSET);
       }

       int min_captures(int file, int pawns)
       {   //   symmetrical around d/e filea
           assert(pawns>=0 && pawns<=6);
           if (file>=FILE_E)
               file = FILE_H-file;
           else
               file = file-FILE_A;
           return PAWN_CAPTURES[file][pawns];
       }

       boolean resolve_pawns(long from, long to, int prev_captures, int avail_captures)
       {
           if (from==to) return (avail_captures>=0);    // that was easy
           if (Long.bitCount(to) > Long.bitCount(from)) return false;

           //   do a counting on files for captures that must occur between 'from' and 'to'
           int add_caps = 0;
           for(int file=FILE_A; file <= FILE_H; ++file)
               add_caps += min_captures(file, Long.bitCount(to&fileMask(file)));
           add_caps -= prev_captures;
           if (add_caps > avail_captures)
               return false;

           //   resolve critical pawns by backtracking
           return resolve_next(from&~to,to&~from, avail_captures);
       }

       boolean resolve_next(long from, long to, int avail_captures)
       {
           if (avail_captures < 0) return false;
           if (to==0) return true;
           if ((to&rowMask(ROW_2))!=0) return false;
           //  pawns can not return to their home row; already checked above
           //   pick next pawn for resolving; choose lowest rank (=least fan-out)
           long sq = BitUtil.least(to);
           to &= ~sq;
           int file = FILE_A+BitUtil.indexOf(sq)%8;
           int row = ROW_2+BitUtil.indexOf(sq)/8;
           backtrack++;

           //  try to resolve from current file
           if (resolve_one(from,to, file, row-1, avail_captures))
               return true;
           //  try to resolve from nearby files
           for(int d=1; ((row-d) >= ROW_2) && (d <= avail_captures); ++d) {
               if (resolve_one(from,to, file+d, row-d, avail_captures-d))
                   return true;
               if (resolve_one(from,to,file-d,row-d,avail_captures-d))
                   return true;
           }
           /* todo if backtracking becomes too expensive, revert to counting
                i.e. estimate lower bound on captures for remaining pawns
                and find pawns that can not be resolved at all

                this would give us false positives, but reduces computation
            */
           return false;
       }

       boolean resolve_one(long from, long to, int file, int row, int avail_captures) {
            if (file<FILE_A || file>FILE_H) return false;
            if (row<ROW_2) return false;
            if (avail_captures < 0) return false;
            long candidate = Long.highestOneBit(from & fileMask(file) & rowsMask(row));
            if (candidate==0) return false;
            return resolve_next(from&~candidate,to,avail_captures);
       }

        public int mostAdvancedPawn() {
            if (EngUtil.isWhite(color)) {
                for(int row=ROW_7; row > ROW_2; --row)
                    if (MatSignatureV2.pawnCount(sig,row) > 0)
                        return row-ROW_2;
            }
            else {
                for(int row=ROW_2; row < ROW_7; ++row)
                    if (MatSignatureV2.pawnCount(sig,row) > 0)
                        return ROW_7-row;
            }
            return 0;
        }

        public long toMatSignatureV1() {
           long sig1 = 0;
           sig1 |= BitUtil.set4(pawnCount(),                MatSignatureV1.OFF_PAWN);
           sig1 |= BitUtil.set4(knightCount(),              MatSignatureV1.OFF_KNIGHT);
           sig1 |= BitUtil.set4(bishopCount(),              MatSignatureV1.OFF_BISHOP);
           sig1 |= BitUtil.set4(rookCount(),                MatSignatureV1.OFF_ROOK);
           sig1 |= BitUtil.set4(queenCount(),               MatSignatureV1.OFF_QUEEN);
           sig1 |= BitUtil.set4(pawnCount()+officersCount(),MatSignatureV1.OFF_TOTAL);

           int homeRow = EngUtil.homeRow(color);
           long homePawns = (this.sig >> rowOffset(homeRow)) & 0x00ffL;
           sig1 |= (homePawns << MatSignatureV1.OFF_PAWN_HOME);

           int promo_lower = computePromotionLowerBound();
           int promo_upper = 8-pawnCount();
           sig1 |= BitUtil.set4(promo_lower, MatSignatureV1.OFF_MIN_PROMO);
           sig1 |= BitUtil.set4(Math.max(promo_upper,promo_lower), MatSignatureV1.OFF_MAX_PROMO);

           sig1 |= BitUtil.set6(padv_lower, MatSignatureV1.OFF_MIN_ADVANCE);
           sig1 |= BitUtil.set6(padv_upper, MatSignatureV1.OFF_MAX_ADVANCE);

           if (promo_lower==promo_upper)
               sig1 |= MatSignatureV1.FLAG_PROMO_EXACT;
           if (padv_lower==padv_upper)
               sig1 |= MatSignatureV1.FLAG_PAWN_ADV_EXACT;
           return sig1;
        }
    }

    public void print(PrintStream out, int color, boolean longFormat) {
        PrintWriter outw = new PrintWriter(out);
        print(outw, color, longFormat);
        outw.flush();
    }

    public void print(PrintWriter out, int color, boolean longFormat) {
        if (EngUtil.isWhite(color))
            wfeat.print(out,longFormat);
        else
            bfeat.print(out,longFormat);
    }

    public String toString()
    {
        StringWriter buf = new StringWriter();
        PrintWriter pw = new PrintWriter(buf);
        pw.print('[');
        print(pw,WHITE,false);
        pw.print(" - ");
        print(pw,BLACK,false);
        pw.print(']');
        return buf.toString();
    }

    public String toHexString()
    {
        StringWriter sw = new StringWriter();
        sw.append('[');
        sw.append(Long.toHexString(getWhiteSignature()));
        sw.append('-');
        sw.append(Long.toHexString(getBlackSignature()));
        sw.append(']');
        return sw.toString();
    }

    public static String longBoard(long board, char chr)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        printboard(pw,board,chr,true);
        pw.flush();
        return sw.toString();
    }

    public static void printboard(PrintWriter buf, long board, char chr, boolean longFormat)
    {
        for(int row=ROW_7; row >= ROW_2; row--) {
            printPawnRow(buf, pawnRow(board, row), row, chr, longFormat);
            if (longFormat)
                buf.append("\n");
            else if (row > ROW_2)
                buf.append("/");
        }
        if (longFormat)
            buf.println(" abcdefgh");
    }

    private static void printPawnRow(PrintWriter buf, long bits, int row, char chr, boolean longFormat) {
        int empty=0;
        if (longFormat)
            buf.print(EngUtil.rowChar(row));
        for(int bit=1,file=FILE_A; bit <= 0x80; bit <<= 1,++file)
            if ((bits&bit) != 0) {
                if (!longFormat) {
                    if (empty > 0) buf.print(empty);
                    empty=0;
                }
                buf.print(chr);
            }
            else if (longFormat)
                buf.print(EngUtil.isLightSquare(square(file,row)) ? ' ':'.');
            else
                empty++;
        if (empty > 0) buf.print(empty);
    }

    static int pieceOffset(int piece, int square)
    {
        switch(EngUtil.uncolored(piece)) {
            case KNIGHT:    return KNIGHT_OFFSET;
            case BISHOP:    return EngUtil.isLightSquare(square) ? LIGHT_BISHOP_OFFSET:DARK_BISHOP_OFFSET;
            case ROOK:      return ROOK_OFFSET;
            case QUEEN:     return QUEEN_OFFSET;
            default:        assert false; return 0;
        }
    }

    //  lower 48 bits = 6 bytes hold the pawn structure
    static long PAWN_MASK   = 0x0ffffffffffffL;
    static long PAWN_FILE   = 0x0101010101010101L;
    //  just the light-colored square (from a2-h2-a7-h7)
    static long LIGHT_PAWN_MASK = 0x0aa55aa55aa55L;
    static long DARK_PAWN_MASK  = 0x055aa55aa55aaL;

    //  next 10 bits encode piece count, 2 bits each
    static final int KNIGHT_OFFSET        = 48;
    static final int LIGHT_BISHOP_OFFSET  = 50;
    static final int DARK_BISHOP_OFFSET   = 52;
    static final int ROOK_OFFSET          = 54;
    static final int QUEEN_OFFSET         = 56;
    static final long OFFICER_MASK        = 0x03ff000000000000L;
    static final long BISHOPS_MASK        = 0x00fL << LIGHT_BISHOP_OFFSET;
    //  pawn advance count (bits, may be unknown)
    static final int ADV_OFFSET             = 58;

    static final int ADV_TOP              = 48;
    static final int ADV_MAX              = ADV_TOP-2;

    static final int PAWN_CAPTURES[][] = new int[][] {
            /*a-file*/ {0,0,1,3,6,10,15},
            /*b-file*/ {0,0,1,2,4, 7,11},
            /*c-file*/ {0,0,1,2,4, 6, 9},
            /*d-file*/ {0,0,1,2,4, 6, 9}
    };

    static int rowOffset(int row) {
        return (row-ROW_2)*8;
    }
    static int fileOffset(int file) {
        return file-FILE_A;
    }
    static long rowMask(int row) {
        return 0x0ffL << rowOffset(row);
    }
    static long rowsMask(int row) {
        return (0x1L << rowOffset(row+1)) - 1;
    }

    static long fileMask(int file) {
        return PAWN_FILE << fileOffset(file);
    }

    static int pawnOffset(int file, int row) {
        return rowOffset(row) + fileOffset(file);
    }

    static int pawnOffset(int square) {
        return pawnOffset(fileOf(square), rowOf(square));
    }

    static long pawnAt(int file, int row) {
        return 1L << pawnOffset(file,row);
    }
    static long pawnAt(int sq)       { return pawnAt(fileOf(sq), rowOf(sq)); }

    static int pawnRow(long sig, int row) {
        return get8(sig, (row-ROW_2)*8);
    }

    static int pawnCount(long sig) {
        return Long.bitCount(sig & PAWN_MASK);
    }
    static int pawnCount(long sig, int row) {
        return Long.bitCount(sig & rowMask(row));
    }

    static int computePawnAdvance(long sig, int color)
    {
        int adv = 0;
        int homeRow = EngUtil.homeRow(color);
        for (int row = ROW_2; row <= ROW_7; ++row)  //  jit compiler: unroll :)
            adv += Math.abs(row-homeRow) * pawnCount(sig, row);
        return adv;
    }

    static int lightPawnCount(long sig) {
        return Long.bitCount(sig & LIGHT_PAWN_MASK);
    }
    static int darkPawnCount(long sig) {
        return Long.bitCount(sig & DARK_PAWN_MASK);
    }

    static int knightCount(long sig)        { return BitUtil.get2(sig,KNIGHT_OFFSET); }
    static int lightBishopCount(long sig)   { return BitUtil.get2(sig,LIGHT_BISHOP_OFFSET); }
    static int darkBishopCount(long sig)    { return BitUtil.get2(sig,DARK_BISHOP_OFFSET); }
    static int bishopCount(long sig)        { return lightBishopCount(sig)+darkBishopCount(sig); }
    static int rookCount(long sig)          { return BitUtil.get2(sig,ROOK_OFFSET); }
    static int queenCount(long sig)         { return BitUtil.get2(sig,QUEEN_OFFSET); }
    static int pawnAdvance(long sig)        { return get6(sig,ADV_OFFSET)-1; }

    static int countLightSquaredBishops(Board board, int color) {
        return board.countPieces(EngUtil.BISHOP | color, (Piece p) -> EngUtil.isLightSquare(p.square()));
    }
    static int countDarktSquaredBishops(Board board, int color) {
        return board.countPieces(EngUtil.BISHOP | color, (Piece p) -> EngUtil.isDarkSquare(p.square()));
    }

    static boolean comparePieceCount(int stored, int actual)
    {
        if (stored >= 3)
            return actual >= 3;
        else
            return stored == actual;
    }

    static boolean is_reachable(Features from, Features to)
    {
        /** check pawn count */
        int pcfrom = from.pawnCount();
        int pcto = to.pawnCount();
        if (pcto > pcfrom)
            return false;    //  not enough pawns

        /** check officers count    */
        int offset = KNIGHT_OFFSET;
        from.piece_cnt=0;
        to.piece_cnt=0;

        for( ; offset <= QUEEN_OFFSET; offset += 2) {
            int from_cnt = BitUtil.get2(from.sig,offset);
            int to_cnt = BitUtil.get2(to.sig,offset);
            if (to_cnt > (from_cnt+pcfrom))
                return false; //  not enough officers   todo never reached?
            from.piece_cnt += from_cnt;
            to.piece_cnt += to_cnt;
        }
//        if (to_total > (from_total+pcfrom))
//            return false; //  not enough officers
        if ((to.piece_cnt+pcto) > (from.piece_cnt+pcfrom))
            return false; //  not enough pieces

        int padv_promo=0;
        if (to.piece_cnt > from.piece_cnt) {
            //  pawns need to advance to create new officers
            //  we already checked that there is a sufficient number of pawns
            //  compute the number of needed advances to promote (to-piece_cnt-from.piece_cnt) times
            //  since positions with 2 promotions are rare, we only look for the most advanced pawn
            padv_promo = (to.piece_cnt-from.piece_cnt)*(6-from.mostAdvancedPawn());
        }

        /** check pawn advance (lower/upper bounds) */
        assert pcto <= pcfrom;
        if ((pcfrom==pcto) && (from.padv_base > to.padv_base))
            return false;    //  pawns can't move backwards

        if ((from.padv_lower+padv_promo) > to.padv_upper)
            return false;  //  pawns are too advanced
        if ((from.padv_upper+from.pawnAdvanceRemaining()) < to.padv_lower)
            return false; //  target is too advanced     todo never reached?

        /** check pawn home row */
        assert from.color == to.color;
        int homerow = EngUtil.homeRow(from.color);
        long homefrom = pawnRow(from.sig,homerow);
        long hometo = pawnRow(to.sig,homerow);

        if (minus8(hometo,homefrom) != 0)
            return false; //  pawns must not return to the home row; never reached ?
        //  more detailed pawn reachability is done in resolve_captures()

        return true;
    }


}
