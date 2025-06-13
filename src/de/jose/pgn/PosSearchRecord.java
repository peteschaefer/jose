package de.jose.pgn;

import de.jose.chess.*;

import static de.jose.chess.Constants.BLACK_KING;
import static de.jose.chess.Constants.KING;

public class PosSearchRecord
{
    public static int POS_EXACT     = 0x01;      //  search for exact position
    public static int PAWNS_EXACT   = 0x02;  //  search for exact pawn structure
    public static int PAWNS_SUBSET  = 0x04;   //  search for pawn subset
        //  material balance can be combined with PAWNS_*, but not with POS_EXACT
    public static int MAT_BALANCE   = 0x08;

    public int what;
    //  if!=0: search for exact position
    public HashKey key;
    //  if!=0: search for exact position with reversed colors
    public HashKey keyReversed;
    //  search reversed color position (or pawn structure)
    public boolean reversedColor;
    //  search inside variations
    public boolean variations;

    //  if key!=0: signature of search position
    //  if key==0: pawn structure to search for
    public MatSignatureV2 sig, sigReversed;
    //  for pawn structure search: signature with max. officers
    protected MatSignatureV2 sigMax, sigMaxReversed;

    //  material balance
    public int min[] = null;
    public int max[] = null;

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
        return key == null && sig == null
                && min == null && max == null
                && bishopColors == null
                && whiteBishop == null
                && blackBishop == null;
    }

    public void clear() {
        key = null;
        keyReversed = null;
        sig = sigReversed = null;
        sigMax = sigMaxReversed = null;
        reversedColor = false;
        variations = false;
        min = max = null;
        bishopColors = null;
        whiteBishop = blackBishop = null;
    }
/*
    public Object clone() {
        return new PosSearchRecord(this);
    }
*/
    public void setExact(Position pos)
    {
        what = POS_EXACT;
        setHashKey(pos);

        sigMax = sig = (MatSignatureV2) pos.updateMatSig().clone();
        sigMaxReversed = sigReversed = (MatSignatureV2) sig.cloneReversed();
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

    public void setPawnStructure(Position pos, boolean exact)
    {
        what = exact ? PAWNS_EXACT : PAWNS_SUBSET;
        key = null;
        keyReversed = null;

        sig = (MatSignatureV2) pos.updateMatSig().clone();
        sig.clearOfficers();    //  search w/o officers
        sigReversed = (MatSignatureV2) sig.cloneReversed();

        //  for early cutoffs: compare with all officers present
        sigMax = (MatSignatureV2) sig.clone();
        if (!exact) {
            //  pawn subset search. add Jokers for early cutoff
            sigMax.addJokerPawns();
        }
        //  add Jokers for early cutoff
        sigMax.addJokerPieces();
        sigMaxReversed = (MatSignatureV2) sigMax.cloneReversed();
    }

    public void clearMatBalance() {
        min = max = null;
    }

    public void setMatBalance(int piece, int min_cnt, int max_cnt) {
        if (min==null) min = new int[BLACK_KING];
        if (max==null) max = new int[BLACK_KING];
        min[piece] = min_cnt;
        max[piece] = max_cnt;
    }

    public boolean exactPosition() { return key!=null; }
    public boolean pawnStructure() { return key==null && sig!=null; }

    //
    //      Test Query Conditions
    //

    public void setPositionOptions(Position pos)
    {
        pos.setOption(Position.INCREMENT_HASH, exactPosition());
        pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH, exactPosition());
        pos.setOption(Position.INCREMENT_SIGNATURE,exactPosition()||pawnStructure());
    }

    //  @return true if we have found a position
    public boolean matches(Position pos, boolean wasNoisy) {
        if (exactPosition()) {
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

        if (pawnStructure()) {
            return sig.pawnsEqual((MatSignatureV2)pos.getMatSig()) ||
                   reversedColor && sigReversed.pawnsEqual((MatSignatureV2)pos.getMatSig());
            //  todo currently, we compare exact pawn structures.
            //   we could compare subsets. with upcoming mat balance search, subsets can be specified more accurately
            //  (search structure + 3 more pawns, e.g.)
        }
        //  todo compare mat balance
        //  todo compare bishop features
        return false;
    }

    //  early cut-off if query can not be reached from end
    public boolean earlyCutOff(MatSignature endSignature, boolean hasVariations) {
        if (exactPosition()) {
            if (!(variations && hasVariations)
                    && !sig.canReach(endSignature)
                    && (!reversedColor || !sigReversed.canReach(endSignature)))
                return true;
            //  except if we could find it in variations
            //  this.variations && Game.Attributes & HAS_VARIATIONS
        }
        if (pawnStructure()) {
            //  check with max. number of officers
            if (!(variations && hasVariations)
                    && !sigMax.canReach(endSignature)
                    && (!reversedColor || !sigMaxReversed.canReach(endSignature)))
                return true;
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
        if (exactPosition() || pawnStructure()) {
            if (!matSig.canReach(sig) && (!reversedColor || !matSig.canReach(sigReversed)))
                return true;
        }
        //  todo check mat balance
        return false;
    }
}
