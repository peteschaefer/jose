package de.jose.chess;

import de.jose.util.BitUtil;

import java.util.Arrays;
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
        init(0,0);
    }

    public boolean matches(Board board)
    {
        return wfeat.matches(board) && bfeat.matches(board);
    }

    // --------------------------------------
    //      Methods
    // --------------------------------------

    public long getWhiteSignature() { return wfeat.assemble(); }
    public long getBlackSignature() { return bfeat.assemble(); }


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
            if (mv.captured.isPawn() && EngUtil.isWhite(mv.captured.piece))
                fthat.del_pawn(mv.captured.square);
            else if (mv.captured.isPawn())
                fthat.del_pawn(mv.captured.square);
            else
                fthat.del_piece(mv.captured.piece,mv.captured.square);   //  decrease piece count
        }
        if (mv.moving!=null && mv.moving.isPawn())
        {
            Features fthis = EngUtil.isWhite(mv.moving.piece) ? wfeat:bfeat;
            int color = mv.moving.color();
            fthis.advance_pawn(mv.from,mv.to);
            if (mv.isPromotion())
                fthis.add_piece(mv.getPromotionPiece(),mv.to);
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
        public Features(int color) {
            this.color = color;
        }

        int color;
        long pawns;
        int count[] = new int[6];          //  number of officers on the board; lower bound, including known promotions
        int padv_base = 0; //  pawn moves lower bound (including already captured and promoted pawns)
        int padv_upper = 0; //  pawn moves upper bound (including unknown promotions)

        void clear() {
            pawns = 0;
            Arrays.fill(count,0);
            padv_base = 0;
            padv_upper = 0;
        }
        void copyFrom(Features that) {
            pawns = that.pawns;
            System.arraycopy(that.count,0, this.count,0,this.count.length);
            padv_base = that.padv_base;
            padv_upper = that.padv_upper;
        }

        int knightCount() { return count[0]; }
        int lightBishopCount() { return count[1]; }
        int darkBishopCount() { return count[2]; }
        int bishopCount() { return lightBishopCount()+darkBishopCount(); }
        int rookCount() { return count[3]; }
        int queenCount() { return count[4]; }
        int officersCount() { return count[5]; }

        int pawnCount() { return MatSignatureV2.pawnCount(pawns); }
        int lightPawnCount() { return MatSignatureV2.lightPawnCount(pawns); }
        int darkPawnCount() { return MatSignatureV2.darkPawnCount(pawns); }

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
            return ADV_TOP- MatSignatureV2.computePawnAdvance(pawns,color);
        }

        protected long assemble()
        {
            long sig = this.pawns & PAWN_MASK;
            sig |= clip2(knightCount(),KNIGHT_OFFSET);
            sig |= clip2(lightBishopCount(),LIGHT_BISHOP_OFFSET);
            sig |= clip2(darkBishopCount(),DARK_BISHOP_OFFSET);
            sig |= clip2(rookCount(),ROOK_OFFSET);
            sig |= clip2(queenCount(),QUEEN_OFFSET);

            if (padv_base==padv_upper)
                sig |= BitUtil.set6(Math.min(ADV_MAX,padv_base+1),ADV_OFFSET);
            // else: unknown = 0
            return sig;
        }

        protected void setBoard(Board board)
        {
            /** copy pawn structure */
            pawns = 0;
            List<Piece> pawnList = board.pieceList(EngUtil.PAWN|color);
            for(Piece p : pawnList) {
                if (p.isVacant()) continue;
                int sq = p.square();
                pawns |= pawnAt(sq);
            }

            /** count officers; clip at 3 */
            count[5] = 0;
            count[5] += count[0] = board.countPieces(EngUtil.KNIGHT|color);
            count[5] += count[1] = board.countPieces(EngUtil.BISHOP|color, (Piece p) -> EngUtil.isLightSquare(p.square()));
            count[5] += count[2] = board.countPieces(EngUtil.BISHOP|color, (Piece p) -> EngUtil.isDarkSquare(p.square()));
            count[5] += count[3] = board.countPieces(EngUtil.ROOK|color);
            count[5] += count[4] = board.countPieces(EngUtil.QUEEN|color);

            /**  is the pawn advance exact, by coincidence ? */
            this.computePawnAdvanceBounds();
        }

        protected void setSignature(long sig)
        {
            /** copy pawn structure */
            pawns = sig & PAWN_MASK;

            /** count officers; clip at 3 */
            count[5] = 0;
            count[5] += count[0] = MatSignatureV2.knightCount(sig);
            count[5] += count[1] = MatSignatureV2.lightBishopCount(sig);
            count[5] += count[2] = MatSignatureV2.darkBishopCount(sig);
            count[5] += count[3] = MatSignatureV2.rookCount(sig);
            count[5] += count[4] = MatSignatureV2.queenCount(sig);

            /**  is the pawn advance exact, by coincidence ? */
            this.computePawnAdvanceBounds();
        }

        private void computePawnAdvanceBounds()
        {
            int promo_lower = 0;
            int promo_upper = 8-pawnCount();
            boolean more_promos=false;
            if (count[0]>=3)   promo_lower++;
            if (count[1]==2)   promo_lower++;
            if (count[1]>=3)   promo_lower++;
            if (count[2]==2)   promo_lower++;
            if (count[2]>=3)   promo_lower++;
            if (count[3]>=3)   promo_lower++;
            if (count[4]==2)   promo_lower++;
            if (count[4]>=3)   promo_lower++;
            //  todo get a second promo_lower bound from Board

            int stored_padv = pawnAdvance(pawns);
            switch (stored_padv) {
                case -1: //  not known; estimate lower and upper bounds
                        padv_base = MatSignatureV2.computePawnAdvance(pawns,color) + promo_lower*6;
                        padv_upper = padv_base+promo_upper*6;
                        break;
                case ADV_MAX: // [46..48] rare case
                        padv_base = ADV_MAX;
                        padv_upper = ADV_TOP;
                        break;
                default:// exact value was stored
                        padv_upper = padv_base = stored_padv;
                        break;
            }
        }

        public boolean matches(Board board)
        {
            if (pawnCount() != board.countPieces(PAWN | color)) return false;
            if (count[0] != board.countPieces(KNIGHT | color)) return false;
            if (count[1] != board.countPieces(EngUtil.BISHOP | color, (Piece p) -> EngUtil.isLightSquare(p.square()))) return false;
            if (count[2] != board.countPieces(EngUtil.BISHOP | color, (Piece p) -> EngUtil.isDarkSquare(p.square()))) return false;
            if (count[3] != board.countPieces(ROOK | color)) return false;
            if (count[4] != board.countPieces(QUEEN | color)) return false;

            List<Piece> pawnList = board.pieceList(EngUtil.PAWN|color);
            for(Piece p : pawnList) {
                if (p.isVacant()) continue;
                int sq = p.square();
                if (! BitUtil.get1(pawns,pawnOffset(sq))) return false;
            }
            return true;
        }

        void print(StringBuffer buf, int color)
        {
            for(int row=ROW_7; row >= ROW_2; row--) {
                printPawnRow(buf, pawnRow(pawns, row), color);
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
            if (padv_base==0 && padv_upper==ADV_TOP) {
                buf.append('?');
            }
            else {
                buf.append(padv_base);
                if (padv_base != padv_upper) {
                    buf.append("..");
                    buf.append(padv_upper);
                }
            }
        }

        public void del_pawn(int square) {
            pawns = BitUtil.clear1(pawns,pawnOffset(square));
        }

        public void del_piece(int piece, int square) {
            switch(EngUtil.uncolored(piece)) {
                case KNIGHT:    count[0]--; break;
                case BISHOP:
                    if (EngUtil.isLightSquare(square))
                        count[1]--;
                    else
                        count[2]--;
                    break;
                case ROOK:      count[3]--; break;
                case QUEEN:     count[4]--; break;
            }
            count[5]--;
        }

        public void add_piece(int piece, int square) {
            switch(EngUtil.uncolored(piece)) {
                case KNIGHT:    count[0]++; break;
                case BISHOP:
                    if (EngUtil.isLightSquare(square))
                        count[1]++;
                    else
                        count[2]++;
                    break;
                case ROOK:      count[3]++; break;
                case QUEEN:     count[4]++; break;
            }
            count[5]++;
        }

        public void advance_pawn(int from, int to) {
            pawns = clear1(pawns,pawnOffset(from));
            pawns |= set1(1,pawnOffset(to));
            padv_base += Math.abs(rowOf(to)-rowOf(from));
            padv_upper = Math.max(padv_upper,padv_base);
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
        if (EngUtil.isWhite(color)) {
            for (int row = ROW_3; row <= ROW_7; ++row)  //  jit compiler: unroll :)
                adv += (row - ROW_2) * pawnCount(sig, row);
        }
        else {
            for (int row = ROW_6; row >= ROW_2; --row)  //  jit compiler: unroll :)
                adv += (ROW_7-row) * pawnCount(sig, row);
        }
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

    static boolean is_reachable(Features from, Features to)
    {
        /** check pawn count */
        int pcfrom = from.pawnCount();
        int pcto = to.pawnCount();
        if (pcto > pcfrom) return false;    //  not enough pawns

        /** check officers count    */
        for(int i=0; i < 6; ++i)
            if (to.count[i] > (from.count[i]+pcfrom))
                return false; //  not enough officers

        /** check pawn advance (lower/upper bounds) */
        if (from.padv_base > to.padv_upper) return false;  //  pawns are too advanced
        if ((from.padv_upper+from.pawnAdvanceRemaining()) < to.padv_base) return false; //  target is too advanced

        /** check pawn home row */
        long homefrom = pawnRow(from.pawns,ROW_2);
        long hometo = pawnRow(to.pawns,ROW_2);

        if (minus8(hometo,homefrom) != 0) return false; //  pawns must not return to the home row

        //  todo more detailed pawn reachability by looking at the potential origin squares
        //  can be done by bit shifting and or-ing.

        return true;
    }


}
