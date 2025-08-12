package de.jose.pgn;

import de.jose.chess.*;
import de.jose.util.BitUtil;

import static de.jose.chess.Constants.BLACK_KING;
import static de.jose.chess.Constants.KING;

public class PosSearchRecord
{
    public static final int POS_EXACT     = 0x001;     //  search for exact position
    public static final int PAWNS_EXACT   = 0x002;     //  search for exact pawn structure
    public static final int PAWNS_SUBSET  = 0x004;     //  search for pawn subset
    public static final int POS_MASK      = 0x007;
        //  material balance can be combined with PAWNS_*, but not with POS_EXACT
    public static int VARS	              = 0x010;
    public static int REVERSED            = 0x020;
        //  material pattern can be combined with PAWN_*, but not with POS_EXACT
    public static int MAT_BALANCE         = 0x100;
    public static int MAT_PATTERN         = 0x200;

    public int what=0;
    //  if!=0: search for exact position
    public HashKey key;
    //  if!=0: search for exact position with reversed colors
    public HashKey keyReversed;
    //  search reversed color position (or pawn structure)
    public boolean reversedColor;
    //  search inside variations
    public boolean variations;
    //  position FEN (used for reference & sql queries only)
    public String fen;

    //  if key!=0: signature of search position
    //  if key==0: pawn structure to search for
    public MatSignatureV2 sigEarly, sigEarlyReversed;
    public MatSignatureV2 sigMatch, sigMatchReversed;
    //  for pawn structure search: signature with max. officers
    protected MatSignatureV2 sigLate, sigLateReversed;

    //  material balance
    public int min[] = null;
    public int max[] = null;
    //  material pattern (officers only)
    public Piece mat[] = null;

    //  Bishop features
    public enum BishopColors { EVEN_COLORED, OPPOSITE_COLORED }
    public enum BishopQuality { GOOD, BAD }
    public BishopColors bishopColors=null;
    public BishopQuality whiteBishop=null, blackBishop=null;
    /*  lots of other position features spring to mind: */
    //  Pawn features (color-agnostic?)
//    public boolean doublePawns,triplePawns;
//    public boolean freePawns,backwardPawns,isolatedPawns;
//    public int pawnsOnRank;
    //  File features
//    public int openFiles,semiOpenFiles;
    //  King features
//    public enum KingQuality { ANY, PROTECTED, VULNERABLE }
//  public KingQuality whiteKing, blackKing;
    //  Game features
//    public boolean mate,stalemate, check,exposedCheck;
//    public boolean shortCastling,longCastling;

    public PosSearchRecord() { }

    //
    //      Setup Query Conditions
    //

    public boolean isEmpty() {
        return key == null && sigMatch == null
                && min == null && max == null
                && mat == null
                && bishopColors == null
                && whiteBishop == null
                && blackBishop == null;
    }

    public void clear() {
        what = 0;
        fen = null;
        key = null;
        keyReversed = null;
        sigEarly = sigEarlyReversed = null;
        sigLate = sigLateReversed = null;
        sigMatch = sigMatchReversed = null;
        reversedColor = false;
        variations = false;
        min = max = null;
        mat = null;
        bishopColors = null;
        whiteBishop = blackBishop = null;
    }

    public void assign(PosSearchRecord that)
    {
        what = that.what;
        fen = that.fen; //  read-only can be shared
        key = that.key; //  read-only; can be shared
        keyReversed = that.keyReversed;
        reversedColor = that.reversedColor;
        variations = that.variations;
        bishopColors = that.bishopColors;
        whiteBishop = that.whiteBishop;
        blackBishop = that.blackBishop;
        //  MatSignatureV2 keeps internal state; not thread-safe
        sigEarly            = assign(sigEarly,that.sigEarly);
        sigEarlyReversed    = assign(sigEarlyReversed,that.sigEarlyReversed);
        sigLate             = assign(sigLate,that.sigLate);
        sigLateReversed     = assign(sigLateReversed,that.sigLateReversed);
        sigMatch            = assign(sigMatch,that.sigMatch);
        sigMatchReversed    = assign(sigMatchReversed,that.sigMatchReversed);
        //  read-only can be shared
        min = that.min;
        max = that.max;
        mat = that.mat;
    }

    private static MatSignatureV2 assign(MatSignatureV2 old, MatSignatureV2 that) {
        if (that==null) return null;
        if (old==null) return (MatSignatureV2) that.clone();
        return old.init(that);
    }
/*
    public Object clone() {
        return new PosSearchRecord(this);
    }
*/
public void setSearch(Position pos, int flags)
{
    variations = (flags & VARS) != 0;
    reversedColor = (flags & REVERSED) != 0;
    switch (flags & POS_MASK) {
        case POS_EXACT:
            setExactSearch(pos);
            return;
        case PAWNS_EXACT:
            setPawnSearch(pos, true);
            return;
        case PAWNS_SUBSET:
            setPawnSearch(pos, false);
            return;
    }
    assert false;
}

    public void setExactSearch(Position pos)
    {
        what = (what&~POS_MASK) | POS_EXACT;
        fen = pos.toString();
        setHashKey(pos);

        sigEarly = sigMatch = sigLate = (MatSignatureV2) pos.updateMatSig().clone();
        sigEarlyReversed = sigMatchReversed = sigLateReversed = (MatSignatureV2) sigMatch.cloneReversed();
        //  sig = sigMax used for both types of cut-offs
        // note: can not search for exact position and mat balance at the same time
        // min = max = null;
    }

    private void setHashKey(Position pos) {
        boolean wasHash = pos.hasOption(Position.INCREMENT_HASH);
        boolean wasRevHash = pos.hasOption(Position.INCREMENT_REVERSED_HASH);
        boolean wasIgnoreFlags = pos.hasOption(Position.IGNORE_FLAGS_ON_HASH);

        pos.setOption(Position.INCREMENT_HASH,true);
        pos.setOption(Position.INCREMENT_REVERSED_HASH,true);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,true);

        assert(pos.hasOption(Position.INCREMENT_HASH));
        assert(pos.hasOption(Position.INCREMENT_REVERSED_HASH));
        assert(pos.hasOption(Position.IGNORE_FLAGS_ON_HASH));

        key = (HashKey) pos.getHashKey().clone();
        keyReversed = (HashKey) pos.getReversedHashKey().clone();

        pos.setOption(Position.INCREMENT_HASH,wasHash);
        pos.setOption(Position.INCREMENT_REVERSED_HASH,wasRevHash);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH, wasIgnoreFlags);
    }

    public void setPawnSearch(Position pos, boolean exact)
    {
        what = (what&~POS_MASK) | (exact ? PAWNS_EXACT : PAWNS_SUBSET);
        fen = pos.toString();
        key = null;
        keyReversed = null;

        sigMatch = sigLate = (MatSignatureV2) pos.updateMatSig().clone();
        sigMatch.clearOfficers();    //  search w/o officers
        sigMatchReversed = sigLateReversed = (MatSignatureV2) sigMatch.cloneReversed();

        //  for early cutoffs: compare with all officers present
        sigEarly = (MatSignatureV2) sigMatch.clone();
        sigEarly.addJokerPieces();
        if (isPawnSubsetSearch()) {
            //  pawn subset search. add Joker pawns for early cutoff
            sigEarly.addJokerPawns();
        }
        //  add Joker pieces for early cutoff
        sigEarlyReversed = (MatSignatureV2) sigEarly.cloneReversed();
    }

    public void clearMatBalance() {
        what = what & ~MAT_BALANCE;
        min = max = null;
    }

    public void setMatBalanceSearch(int piece, int min_cnt, int max_cnt) {
        what |= MAT_BALANCE;
        if (min==null) min = new int[BLACK_KING];
        if (max==null) max = new int[BLACK_KING];
        min[piece] = min_cnt;
        max[piece] = max_cnt;
    }

    public boolean isExactPositionSearch() {
        return (what&POS_MASK)==POS_EXACT;
    }
    public boolean isExactPawnSearch() {
        return (what&POS_MASK)==PAWNS_EXACT;
    }
    public boolean isPawnSubsetSearch() {
        return (what&POS_MASK)==PAWNS_SUBSET;
    }
    public boolean isPawnSearch() {
        return (what&(PAWNS_EXACT|PAWNS_SUBSET))!=0;
    }
    public boolean isPositionSearch() {
        return (what&POS_MASK)!=0;
    }
    public boolean isMatBalanceSearch() {
        return (what&MAT_BALANCE)==MAT_BALANCE;
    }

    //
    //      Test Query Conditions
    //

    public void setPositionOptions(Position pos)
    {
        pos.setOption(Position.INCREMENT_HASH, isExactPositionSearch());
        pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH, isExactPositionSearch());
        pos.setOption(Position.INCREMENT_SIGNATURE,isPositionSearch());
        //  mat-signature cutoffs are used for both, exact search and pawn searches
    }

    //  @return true if we have found a position
    public boolean matches(Position pos, boolean wasNoisy) {
        if (isExactPositionSearch()) {
            //  hash key is checked with every position
            assert(pos.hasOption(Position.INCREMENT_HASH));
//            assert(pos.hasOption(Position.INCREMENT_REVERSED_HASH));
            assert(pos.hasOption(Position.IGNORE_FLAGS_ON_HASH));

            if (pos.getHashKey().equals(key) || reversedColor && pos.getHashKey().equals(keyReversed))
                return true;
        }
        //  pawn structure and mat features are only checked after *noisy* moves
        //  (i.e. after changes in pawn structure, mat count, ...)
        if (!wasNoisy) return false; // -> keep on searching

        if (isPawnSearch()) {
            //  compare pawn structure (exact, or subset)
            if ( sigMatch.pawnsEqual((MatSignatureV2)pos.getMatSig(),isExactPawnSearch()) ||
               ( reversedColor && sigMatchReversed.pawnsEqual((MatSignatureV2)pos.getMatSig(),isExactPawnSearch() )) )
                return true;
        }

        //  todo compare mat balance
        //  todo compare bishop features
        //  todo compare mat pattern (for silent moves, too. Store previous pawn match results.)
        return false;
    }

    //  early cut-off if query can not be reached from end
    public boolean earlyCutOff(MatSignature endSignature, boolean hasVariations) {
        if (isPositionSearch()) {
            if (!(variations && hasVariations)
                    && !sigEarly.canReach(endSignature)
                    && (!reversedColor || !sigEarlyReversed.canReach(endSignature)))
                return true;
            //  note: sigMax is used for early cutoffs. For exact searches it is identical to 'sig'.
            //  For pawn searches it has "jokers" that allow to compare signatures regardless of officers
            //  except if we could find it in variations
            //  this.variations && Game.Attributes & HAS_VARIATIONS
        }

        //  todo does it make sense to test mat balance against endSignature?
        //  officers can vanish an re-appear through promotion?
        return false;
    }

    //  in-game cut-off if query can not be reached from current position
    public boolean cutOff(Position pos, boolean wasNoisy)
    {
        //  matsig is only checked in noisy positions
        if (!wasNoisy) return false;
        MatSignature matSig = pos.getMatSig();
        if (isPositionSearch()) {
            if (!matSig.canReach(sigLate) && (!reversedColor || !matSig.canReach(sigLateReversed)))
                return true;
        }
        //  todo check mat balance
        return false;
    }
}
