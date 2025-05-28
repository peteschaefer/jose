package de.jose.chess;

import de.jose.util.BitUtil;

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
 */
public class MatSignatureV2 implements MatSignature
{
    private Features wfeat = new Features(WHITE);
    private Features bfeat = new Features(BLACK);

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
        return that instanceof MatSignatureV2
                && ((MatSignatureV2) that).wfeat.equals(wfeat)
                && ((MatSignatureV2) that).bfeat.equals(bfeat);
    }

    public MatSignature cloneReversed() {
        return new MatSignatureV2(getBlackSignature(), getWhiteSignature());
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
    public boolean canReachReversed(MatSignature to) {
        return (to instanceof MatSignatureV2) && is_reverse_reachable(this,(MatSignatureV2)to);
    }

    /**
     * incremental update
     * @param mv
     */
    public void update(Move mv)
    {
        //  notify "loud" moves: captures and pawn moves
        if (mv.isCapture())
        {
            Features fthat = EngUtil.isWhite(mv.moving.piece) ? bfeat:wfeat;
            if (mv.captured.isPawn())
                fthat.del_pawn(mv.getCapturedSquare());
            else
                fthat.del_piece(mv.getCapturedPiece(),mv.getCapturedSquare());   //  decrease piece count
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
        return  is_reachable(from.wfeat, to.wfeat) &&
                is_reachable(from.bfeat, to.bfeat);
    }

    public static boolean is_reverse_reachable(MatSignatureV2 from, MatSignatureV2 to)
    {
        return  is_reachable(from.wfeat, to.bfeat) &&
                is_reachable(from.bfeat, to.wfeat);
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

        public boolean equals(Object that) {
            return (that instanceof Features) && ((Features)that).sig==this.sig;
        }

        void clear() {
            sig = 0;
            padv_base = padv_lower = padv_upper = 0;
        }
        void copyFrom(Features that) {
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
            return ADV_TOP - padv_base;
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

        private void computePawnAdvanceBounds()
        {
            int promo_lower = 0;
            int promo_upper = 8-pawnCount();
            boolean more_promos=false;
            if (knightCount()>=3)       promo_lower++;
            if (lightBishopCount()>=2)  promo_lower++;
            if (lightBishopCount()>=3)  promo_lower++;
            if (darkBishopCount()>=2)   promo_lower++;
            if (darkBishopCount()>=3)   promo_lower++;
            if (rookCount()>=3)         promo_lower++;
            if (queenCount()>=2)        promo_lower++;
            if (queenCount()>=3)        promo_lower++;
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

        void print(StringBuffer buf, int color)
        {
            for(int row=ROW_7; row >= ROW_2; row--) {
                printPawnRow(buf, pawnRow(sig, row), color);
                if (row>ROW_2) buf.append("/");
            }

            if (knightCount() > 0) {
                buf.append(' ');
                buf.append(knightCount());
                buf.append(EngUtil.coloredPieceCharacter(KNIGHT|color));
            }
            if (bishopCount() > 0) {
                buf.append(' ');
                buf.append(lightBishopCount());
                buf.append('+');
                buf.append(darkBishopCount());
                buf.append(EngUtil.coloredPieceCharacter(BISHOP|color));
            }
            if (rookCount() > 0) {
                buf.append(' ');
                buf.append(rookCount());
                buf.append(EngUtil.coloredPieceCharacter(ROOK|color));
            }
            if (queenCount() > 0) {
                buf.append(' ');
                buf.append(queenCount());
                buf.append(EngUtil.coloredPieceCharacter(QUEEN|color));
            }

            buf.append(' ');
            if (padv_lower==0 && padv_upper==ADV_TOP) {
                buf.append('?');
            }
            else {
                buf.append(padv_lower);
                if (padv_lower != padv_upper) {
                    buf.append("..");
                    buf.append(padv_upper);
                }
            }
        }

        public void del_pawn(int square) {
            sig = BitUtil.clear1(sig,pawnOffset(square));
            padv_base -= Math.abs(rowOf(square)-EngUtil.homeRow(color));
            assert padv_base==MatSignatureV2.computePawnAdvance(sig,color);
        }

        public void del_piece(int piece, int square) {
            int offset = pieceOffset(piece,square);
            int cnt = get2(sig,offset);
            cnt = Math.max(0,cnt-1);
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
           if (padv_lower==padv_upper)
               sig |= BitUtil.set6(Math.min(ADV_MAX, padv_lower +1),ADV_OFFSET);
       }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        wfeat.print(buf,WHITE);
        buf.append(" - ");
        bfeat.print(buf,BLACK);
        buf.append(']');
        return buf.toString();
    }

    public String toHexString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        buf.append(Long.toHexString(getWhiteSignature()));
        buf.append('-');
        buf.append(Long.toHexString(getBlackSignature()));
        buf.append(']');
        return buf.toString();
    }

    private void printPawnRow(StringBuffer buf, long bits, int color) {
        int empty=0;
        for(int bit=1; bit <= 0x80; bit <<= 1)
            if ((bits&bit) != 0) {
                if (empty > 0) buf.append(empty);
                buf.append(EngUtil.coloredPieceCharacter(PAWN|color));
                empty=0;
            }
            else {
                empty++;
            }
        if (empty > 0) buf.append(empty);
    }

    int pieceOffset(int piece, int square)
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
    static long PAWN_MASK = 0x0ffffffffffffL;
    //  just the light-colored square (from a2-h2-a7-h7)
    static long LIGHT_PAWN_MASK = 0x055aa55aa55aaL;
    static long DARK_PAWN_MASK  = 0x0aa55aa55aa55L;

    //  next 10 bits encode piece count, 2 bits each
    static final int KNIGHT_OFFSET        = 48;
    static final int LIGHT_BISHOP_OFFSET  = 50;
    static final int DARK_BISHOP_OFFSET   = 52;
    static final int ROOK_OFFSET          = 54;
    static final int QUEEN_OFFSET         = 56;
    static final long OFFICER_MASK         = 0x03ff000000000000L;
    //  pawn advance count (bits, may be unknown)
    static final int ADV_OFFSET             = 58;

    static final int ADV_TOP              = 48;
    static final int ADV_MAX              = ADV_TOP-2;

    static int rowOffset(int row) {
        return (row-ROW_2)*8;
    }
    static int fileOffset(int file) {
        return file-FILE_A;
    }
    static long rowMask(int row) {
        return 0x0ffL << rowOffset(row);
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
        if (pcto > pcfrom) return false;    //  not enough pawns

        /** check officers count    */
        int offset = KNIGHT_OFFSET;
        int from_total=0;
        int to_total=0;

        for( ; offset <= QUEEN_OFFSET; offset += 2) {
            int from_cnt = BitUtil.get2(from.sig,offset);
            int to_cnt = BitUtil.get2(to.sig,offset);
            if (to_cnt > (from_cnt+pcfrom))
                return false; //  not enough officers
            from_total += from_cnt;
            to_total += to_cnt;
        }
        if ((to_total+pcto) > (from_total+pcfrom))
            return false; //  not enough pieces


        if (to_total > from_total) {
            // todo pawns need to advance to create new officers
        }

        /** check pawn advance (lower/upper bounds) */
        if (from.padv_lower > to.padv_upper) return false;  //  pawns are too advanced
        if ((from.padv_upper+from.pawnAdvanceRemaining()) < to.padv_lower) return false; //  target is too advanced

        /** check pawn home row */
        assert from.color == to.color;
        int homerow = EngUtil.homeRow(from.color);
        long homefrom = pawnRow(from.sig,homerow);
        long hometo = pawnRow(to.sig,homerow);

        if (minus8(hometo,homefrom) != 0) return false; //  pawns must not return to the home row

        //  todo more detailed pawn reachability by looking at the potential origin squares
        //  can be done by bit shifting and or-ing.

        return true;
    }


}
