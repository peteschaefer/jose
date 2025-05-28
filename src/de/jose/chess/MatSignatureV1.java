/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.chess;

import de.jose.Util;

import java.lang.reflect.Constructor;
import java.util.List;

import static de.jose.util.BitUtil.*;

/**
 *
 * can be used to filter position searches
 * filter all games where:
 *  mat_reachable(target-postion, final-position)
 * 
 * during replay:
 *  if (!mat_reachable(current-position, target-position) break;
 * 
 * @author Peter Sch�fer
 */

public class MatSignatureV1
        implements MatSignature, Constants
{
    // --------------------------------------
    //      Fields
    // --------------------------------------


    /** material signature
     *
     *  4 bits  pawn count (0..8)
     *  4 bits  knight count (0..10)
     *  4 bits  bishop count (0..10)
     *  4 bits  rook count (0..10)
     *  4 bits  queen count (0..9)
     *
     *  4 bits  total piece count (0..15, not including king) (@deprecated? compute from sum)
     *
     *  8 bits  pawns on home row (1 bit for each file)
     *
     *  4 bits  min. number of promotions (exact / or lower bound) (0..8)
     *  4 bits  max. number of promotions (upper bound) (0..8)
     *
     *  6 bits min. pawn advance (exact / or lower bound) (0..48)
     *  6 bits max. pawn advance (upper bound) (0..48)
	 *
     *  1 bit   promotion count is exact
     *  1 bit   pawn advance count is exact (including promotions and missing pawns)
     *          otherwise: lower bound; upper bound is calculated from number of missing pawns
	 *
	 * =======
	 * 	54 bits
	 */
    protected long wsig, bsig;

	public final long getWhiteSignature() { return wsig; }
	public final long getBlackSignature() { return bsig; }

    // --------------------------------------
    //      Constants
    // --------------------------------------

    public static final int  OFF_PAWN                   = 0;    //  4 bits
    public static final int  OFF_KNIGHT                 = 4;    //  4 bits
    public static final int  OFF_BISHOP                 = 8;    //  4 bits
    public static final int  OFF_ROOK                   = 12;   //  4 bits
    public static final int  OFF_QUEEN                  = 16;   //  4 bits
    public static final int  OFF_TOTAL                  = 20;   //  4 bits
	public static final int  OFF_PAWN_HOME              = 24;   //  8 bits

	public static final int OFF_MIN_PROMO               = 32;   //  4 bits
	public static final int OFF_MAX_PROMO               = 36;   //  4 bits

	public static final int  OFF_MIN_ADVANCE           = 40;   //  6 bits
	public static final int  OFF_MAX_ADVANCE           = 46;   //  6 bits

	public static final long FLAG_PROMO_EXACT           = (1L<<52);
	public static final long FLAG_PAWN_ADV_EXACT        = (1L<<53);

	public static final long INITIAL_SIG                = 0x00000000fff12228L |
	                                                      FLAG_PROMO_EXACT |
	                                                      FLAG_PAWN_ADV_EXACT;
	/** during incremental updates, promo and advances counts are accurate
	 *  only when a position is setup manually, we need to check lower and upper bounds
	 */

    // --------------------------------------
    //      Constructors
    // --------------------------------------

    public MatSignatureV1()
    { }

	public MatSignatureV1(long wsig, long bsig) {
		init(wsig, bsig);
	}

	public MatSignatureV1(MatSignatureV1 that) {
		init(that.wsig, that.bsig);
	}

    public MatSignatureV1(Board board) {
		setBoard(board);
    }

	public void init(long wsig, long bsig)
	{
		this.wsig = wsig;
		this.bsig = bsig;
	}

    // --------------------------------------
    //      Basic Methods
    // --------------------------------------

	static int pawncount(long mat)                  { return get4(mat,OFF_PAWN); }
	static int knightcount(long mat)                { return get4(mat,OFF_KNIGHT); }
	static int bishopcount(long mat)                { return get4(mat,OFF_BISHOP); }
	static int rookcount(long mat)                  { return get4(mat,OFF_ROOK); }
	static int queencount(long mat)                 { return get4(mat,OFF_QUEEN); }

	static int totalcount(long mat)                 { return get4(mat,OFF_TOTAL); }

	static boolean isPromoExact(long mat)           { return is(mat,FLAG_PROMO_EXACT); }
	static boolean isPawnAdvanceExact(long mat)     { return is(mat,FLAG_PAWN_ADV_EXACT); }

	static int minpromo(long mat)                   { return get4(mat,OFF_MIN_PROMO); }
	static int maxpromo(long mat)                   { return get4(mat,OFF_MAX_PROMO); }

	static int phome(long mat)                      { return get8(mat,OFF_PAWN_HOME); }
	static int minadvance(long mat)                 { return get6(mat,OFF_MIN_ADVANCE); }
	static int maxadvance(long mat)                 { return get6(mat,OFF_MAX_ADVANCE); }

	public int minMoves() {
		return Math.max(minMoves(wsig),minMoves(bsig));
	}

	static int minMoves(long mat) {
		//	minimal number of moves that happened
		int captures = 15-totalcount(mat);
		int pawnadv = minadvance(mat);
		int promo = minpromo(mat)*5;
		return Math.max(captures, pawnadv+promo);
	}

	// --------------------------------------
	//      Default Methods
	// --------------------------------------

	public boolean equals(Object that) {
		return (that instanceof MatSignatureV1)
				&& ((MatSignatureV1)that).wsig==this.wsig
				&& ((MatSignatureV1)that).bsig==this.bsig;
	}

	public Object clone() {
		return new MatSignatureV1(this);
	}

	public MatSignature cloneReversed() {
		return new MatSignatureV1(bsig,wsig);
	}


	public int hashCode() {
		return (int)wsig ^ (int)bsig ^ (int)(wsig>>32) ^ (int)(bsig>>32);
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		toString(wsig,buf);
		buf.append(" - ");
		toString(bsig,buf);
		buf.append("]");
		return buf.toString();
	}

	protected static void toString(long mat, StringBuffer buf)
	{
		buf.append(Integer.toString(pawncount(mat)));
		buf.append("+");
		buf.append(Integer.toString(knightcount(mat)));
		buf.append("+");
		buf.append(Integer.toString(bishopcount(mat)));
		buf.append("+");
		buf.append(Integer.toString(rookcount(mat)));
		buf.append("+");
		buf.append(Integer.toString(queencount(mat)));
		buf.append("=");
		buf.append(Integer.toString(totalcount(mat)));
		buf.append(" ");
		buf.append(toBinString8(phome(mat)));
		buf.append(" ");
		buf.append(Integer.toString(minpromo(mat)));
		buf.append("/");
		buf.append(Integer.toString(maxpromo(mat)));
		buf.append(" ");
		buf.append(Integer.toString(minadvance(mat)));
		buf.append("/");
		buf.append(Integer.toString(maxadvance(mat)));
	}

	protected static String toBinString8(int x)
	{
		char[] result = new char[8];
		for (int i=7; i>=0; i--, x>>=1)
			result[i] = (char)('0'+(x&1));
		return new String(result);
	}

    // --------------------------------------
    //      Accessors
    // --------------------------------------

    public void clear()  {
        wsig = bsig = 0L;
    }
    
    public void setInitial() {
        wsig = bsig = INITIAL_SIG;
    }
    
    public void setBoard(Board board)
    {
        clear();

        wsig = setBoard(board,WHITE);
        bsig = setBoard(board,BLACK);
    }

	public void mergeWith(MatSignatureV1 that)
	{
		this.wsig = merge(this.wsig,that.wsig);
		this.bsig = merge(this.bsig,that.bsig);
	}

    // --------------------------------------
    //      Methods
    // --------------------------------------


    public void reverse()
    {
        long swap = wsig;
	    wsig = bsig;
	    bsig = swap;
    }

    public boolean isReachableFrom(MatSignature from) {
		return (from instanceof MatSignatureV1) && is_reachable((MatSignatureV1) from,this);
    }

	public boolean canReach(MatSignature to) {
		return (to instanceof MatSignatureV1) && is_reachable(this, (MatSignatureV1)to);
	}

	@Override
	public boolean canReachReversed(MatSignature to) {
		return (to instanceof MatSignatureV1) && is_reverse_reachable(this, (MatSignatureV1)to);
	}

	public static boolean isReachable(MatSignatureV1 from, MatSignatureV1 to)
    {
        return to.isReachableFrom(from);
    }

	// --------------------------------------
	//      Incremental Methods
	// --------------------------------------

	public void update(Move mv)
	{
		if (mv.isCapture()) {
			//  decrease piece count
			dec_piece(mv.captured.piece());
			if (mv.captured.isPawn())
			{
				int color = mv.captured.color();
				int adv = Pawn.getRowAdvance(color,mv.to);
				//  adjust pawn home row
				if (adv==0)
					clear_home(color, EngUtil.fileOf(mv.to));
				//  adjust pawn advance (unless exact)
				if (!isPawnAdvanceExact(EngUtil.isWhite(color)? wsig:bsig)) {
					dec_min_adv(color,adv);
					inc_max_adv(color,6-adv);
				}
			}
		}
		if (mv.moving!=null && mv.moving.isPawn()) {
			int color = mv.moving.color();
			//  adjust pawn home row
			if (Pawn.isOnHomeRow(color,mv.from))
				clear_home(color, EngUtil.fileOf(mv.from));
			//  adjust pawn advance
			if (mv.isPawnDouble())
				inc_min_adv(color,+2);
			else if (mv.isPromotion()) {
				if (isPawnAdvanceExact(EngUtil.isWhite(color)? wsig:bsig))
					inc_min_adv(color,+1);
				else {
					dec_min_adv(color,5);
					inc_max_adv(color,6);
				}
			}
			else
				inc_min_adv(color,+1);

			if (mv.isPromotion()) {
				//  adjust piece count
				dec_piece(mv.moving.piece());
				inc_piece(mv.getPromotionPiece());
				//  adjust min./max.promotions
				inc_min_promo(color);
			}
		}
	}

	protected void dec_piece(int pc)
	{
		int off = OFF_PAWN + 4*(EngUtil.uncolored(pc)-PAWN);
		if (EngUtil.isWhite(pc)) {
			wsig -= (1L<<off);
			wsig -= (1L<<OFF_TOTAL);
		}
		else {
			bsig -= (1L<<off);
			bsig -= (1L<<OFF_TOTAL);
		}
	}

	protected void inc_piece(int pc)
	{
		int off = OFF_PAWN + 4*(EngUtil.uncolored(pc)-PAWN);
		if (EngUtil.isWhite(pc)) {
			wsig += (1L<<off);
			wsig += (1L<<OFF_TOTAL);
		}
		else {
			bsig += (1L<<off);
			bsig += (1L<<OFF_TOTAL);
		}
	}

	protected void clear_home(int color, int file)
	{
		int offset;
		file -= FILE_A;
		if (EngUtil.isWhite(color)) {
			offset = OFF_PAWN_HOME+file;
			long flag = 1L << offset;
			wsig &= ~flag;
		}
		else {
			file = 7-file;
			offset = OFF_PAWN_HOME+file;
			long flag = 1L << offset;
			bsig &= ~flag;
		}
	}

	protected void inc_min_adv(int color, int inc)
	{
		if (EngUtil.isWhite(color))
			wsig += (long)inc << OFF_MIN_ADVANCE;
		else
			bsig += (long)inc << OFF_MIN_ADVANCE;
	}

	protected void dec_min_adv(int color, int dec)
	{
		if (EngUtil.isWhite(color))
			wsig -= (long)dec << OFF_MIN_ADVANCE;
		else
			bsig -= (long)dec << OFF_MIN_ADVANCE;
	}

	protected void inc_max_adv(int color, int inc)
	{
		if (EngUtil.isWhite(color))
			wsig += (long)inc << OFF_MAX_ADVANCE;
		else
			bsig += (long)inc << OFF_MAX_ADVANCE;
	}

	protected void dec_max_adv(int color, int dec)
	{
		if (EngUtil.isWhite(color))
			wsig -= (long)dec << OFF_MAX_ADVANCE;
		else
			bsig -= (long)dec << OFF_MAX_ADVANCE;
	}

	protected void inc_min_promo(int color)
	{
		if (EngUtil.isWhite(color))
			wsig += 1L << OFF_MIN_PROMO;
		else
			bsig += 1L << OFF_MIN_PROMO;
	}

    // --------------------------------------
    //      Static Methods
    // --------------------------------------

    public static boolean is_reachable(MatSignatureV1 from, MatSignatureV1 to)
    {
        return  is_reachable(from.wsig, to.wsig) &&
                is_reachable(from.bsig, to.bsig);
    }

	public static boolean is_reverse_reachable(MatSignatureV1 from, MatSignatureV1 to)
	{
		return  is_reachable(from.wsig, to.bsig) &&
				is_reachable(from.bsig, to.wsig);
	}

	/**
	 * @return true if position "to" can be reached from position "from"
	 *      by a legal sequence of moves
	 *
	 * this relation is supposed to be
	 *
	 *  + transitive
	 *      is_reachable(a,b) AND is_reachable(b,c) IMPLIES is_reachable(a,c);
	 *
	 *  + reflexive
	 *      is_reachable(a,a) is always true
	 *
	 *  + not symmetric
	 *      is_reachable(a,b) DOES NOT IMPLY is_reachable(b,a)
	 *
	 *      however, there might be symmetric pairs (a,b) such that
	 *      is_reachable(a,b) AND is_reachable(b,a)
	 *      as a result, this relation can not be ordered
	 *      (and can, sadly, not be used for indexing)
	 *
	 *  todo check only pawns
	 */
    public static boolean is_reachable(long from, long to)
    {
	    /** check total piece count */
	    int tf = totalcount(from);
	    int tt = totalcount(to);
	    if (tf < tt)  {
//		    System.err.println("total piece count "+tf+"<"+tt);
		    return false;   //  not enough pieces
	    }

	    /** check pawn count    */
		int pf = pawncount(from);
	    int pt = pawncount(to);
        int pdiff = pt-pf;
        if (pdiff > 0) {
//	        System.err.println("pawn count "+pf+"<"+pt);
	        return false;    // not enough pawns
        }

	    /** check pawn home row */
	    int home_diff = Util.minus(phome(to), phome(from));
	    if (home_diff != 0) {
//		    System.err.println("pawn home row mismatch "+toBinString8(home_diff));
		    return false;   //  pawns still on home row
	    }

	    /** check pawn advances    */
		if (pt==pf && isPawnAdvanceExact(to))
	    {
		    //  no promotions happened between the positions

		    int minf = minadvance(from);
			int mint = minadvance(to);
		    if (minf > mint) {
//			    System.err.println("pawn advance "+minf+" > "+mint);
			    return false;  //  pawns are too far advanced
		    }
	    }
	    else {
		    //  advance count is skewed since some pawns are missing;
		    //  compare against upper bound
			int minf = minadvance(from);
			int mint = Math.max(maxadvance(to),minadvance(to));
			if (minf > mint) {
//				System.err.println("pawn advance "+minf+" > "+mint);
			    return false;
		    }
	    }

	    /** check promotion constraints */
        int promodiff = 0;
        int ndiff = knightcount(to)-knightcount(from);
        int bdiff = bishopcount(to)-bishopcount(from);
        int rdiff = rookcount(to)-rookcount(from);
        int qdiff = queencount(to)-queencount(from);

        if (ndiff > 0) promodiff += ndiff;
        if (bdiff > 0) promodiff += bdiff;
        if (rdiff > 0) promodiff += rdiff;
        if (qdiff > 0) promodiff += qdiff;

        if (promodiff > -pdiff) {
//	        System.err.println("min promo diff "+promodiff+" > "+(-pdiff));
	        return false;    //  not enough pawns to create new pieces
        }

	    int maxf = Math.max(maxpromo(from),minpromo(from))+promodiff;
		int mint = minpromo(to);
		if (maxf < mint) {
//			System.err.println("max. promo diff "+maxf+" < "+mint);
			return false;   //  too little promotions
		}
//	    if ((fminpromo+promodiff) > tmaxpromo) return false;   //  too many promotions

        return true;
    }

    // --------------------------------------
    //      Private Methods
    // --------------------------------------


    private long setBoard(Board board, int color)
    {
        /**  count pieces    */
        int pawnCount   = Board.countValid(board.pieceList(color+PAWN));
        int knightCount = Board.countValid(board.pieceList(color+KNIGHT));
        int bishopCount = Board.countValid(board.pieceList(color+BISHOP));
        int rookCount   = Board.countValid(board.pieceList(color+ROOK));
        int queenCount  = Board.countValid(board.pieceList(color+QUEEN));
		int totalCount  = pawnCount+knightCount+bishopCount+rookCount+queenCount;

        long mat =  (long)pawnCount   << OFF_PAWN |
                    (long)knightCount << OFF_KNIGHT |
                    (long)bishopCount << OFF_BISHOP |
                    (long)rookCount   << OFF_ROOK |
                    (long)queenCount  << OFF_QUEEN |
                    (long)totalCount << OFF_TOTAL;
	                //  KING is never counted

	    /**  calculate promotions; are there obvious promotion pieces ? */
	    int minPromo =
	            Math.max(knightCount-2,   Board.countPromoted(board.pieceList(color+KNIGHT)))
	    +       Math.max(bishopCount-2,   Board.countPromoted(board.pieceList(color+BISHOP)))
	    +       Math.max(rookCount-2,     Board.countPromoted(board.pieceList(color+ROOK)))
	    +       Math.max(queenCount-1,    Board.countPromoted(board.pieceList(color+QUEEN)));

	    if (! board.checkBishopColors(color)) minPromo++;    //  same colored bishops


	    int maxPromo = Math.min(8-pawnCount, totalCount-pawnCount);
	    if (maxPromo <= minPromo)
	        mat |= FLAG_PROMO_EXACT;

	    mat |= (long)minPromo << OFF_MIN_PROMO;
		mat |= (long)maxPromo << OFF_MAX_PROMO;

	    /**  calculate pawn home row signature & number of pawn advances */
		int totaladv = 0;
	    List pawns = board.pieceList(color+PAWN);
	    for (int i=0; i<pawns.size(); i++) {
	        Piece pawn = (Piece)pawns.get(i);
	        if (pawn.isVacant()) continue;

	        int file = EngUtil.fileOf(pawn.square())-FILE_A;
	        int adv = EngUtil.rowOf(pawn.square())-ROW_2;

	        if (EngUtil.isBlack(color)) {
	            file = 7-file;
	            adv = 5-adv;
	        }

	        if (adv==0) mat |= (1L << (file+OFF_PAWN_HOME));  //  inidicates that pawn is still on original square
		    totaladv += adv;
	    }

		mat |= ((long)totaladv << OFF_MIN_ADVANCE);
		//  note that total does not include captured and promoted pawns
		//  that's why the PMOVES_EXACT flag is only set when all pawns are present
		if (pawnCount==8)
			mat |= FLAG_PAWN_ADV_EXACT;
	    else {
			//  if pawn advance count is not exact, estimate upper bound
			totaladv += (8-pawnCount)*6;
		}
	    mat |= (long)totaladv << OFF_MAX_ADVANCE;

	    return mat;
    }

	private long merge(long a, long b)
	{
		long mat = 0L;
		/** set min. piece counts   */
		int pawnCount       = Math.min(pawncount(a),pawncount(b));
		int knightCount     = Math.min(knightcount(a),knightcount(b));
		int bishopCount     = Math.min(bishopcount(a),bishopcount(b));
		int rookCount       = Math.min(rookcount(a),rookcount(b));
		int queenCount      = Math.min(queencount(a),queencount(b));
		int totalCount      = Math.min(totalcount(a),totalcount(b));

		mat |=  (long)pawnCount << OFF_PAWN |
		        (long)knightCount << OFF_KNIGHT |
		        (long)bishopCount << OFF_BISHOP |
		        (long)rookCount << OFF_ROOK |
		        (long)queenCount << OFF_QUEEN |
		        (long)totalCount << OFF_TOTAL;

		/** adjust min/max promotions   */
		int minPromo = Math.max(minpromo(a),minpromo(b));
		int maxPromo = Math.max(maxpromo(a),maxpromo(b));

		mat |=  (long)minPromo << OFF_MIN_PROMO |
		        (long)maxPromo << OFF_MAX_PROMO;

		if (minPromo==maxPromo)
			mat |= FLAG_PROMO_EXACT;

		/** set home pawn flags */
		int homePawn = phome(a) & phome(b);
		mat |= (long)homePawn << OFF_PAWN_HOME;

		/** set min/max pawn advancement    */
		int minAdv = Math.max(minadvance(a),minadvance(b));
		int maxAdv = Math.max(maxadvance(a),maxadvance(b));

		mat |= (long)minAdv << OFF_MIN_ADVANCE |
		       (long)maxAdv << OFF_MAX_ADVANCE;

		if (minAdv==maxAdv)
			mat |= FLAG_PAWN_ADV_EXACT;

		return mat;
	}

}
