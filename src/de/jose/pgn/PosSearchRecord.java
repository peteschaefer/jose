package de.jose.pgn;

import de.jose.chess.*;

import static de.jose.chess.Constants.BLACK_KING;
import static de.jose.chess.Constants.KING;

public class PosSearchRecord
{
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
    public MatSignature sig;

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
/*
    public PosSearchRecord(PosSearchRecord that) {
        this.key = (that.key==null) ? null : (HashKey) that.key.clone();
        this.keyReversed = (that.keyReversed==null) ? null : (HashKey) that.keyReversed.clone();
        this.reversedColor = that.reversedColor;
        this.variations = that.variations;
        this.sig = (that.sig==null) ? null : (MatSignature) that.sig.clone();
        this.min = (that.min==null) ? null : that.min.clone();
        this.max = (that.max==null) ? null : that.max.clone();
        this.bishopColors = that.bishopColors;
        this.whiteBishop = that.whiteBishop;
        this.blackBishop = that.blackBishop;
    }
*/

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
        sig = null;
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
    public void setExact(Position pos) {
        boolean wasIgnoreFlags = pos.hasOption(Position.IGNORE_FLAGS_ON_HASH);
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,true);

        assert(pos.hasOption(Position.IGNORE_FLAGS_ON_HASH));

        key = (HashKey) pos.getHashKey().clone();
        keyReversed = (HashKey) pos.getReversedHashKey().clone();

        pos.setOption(Position.IGNORE_FLAGS_ON_HASH, wasIgnoreFlags);

        sig = (MatSignature) pos.updateMatSig().clone();
        // note: can not search for exact position and mat balance at the same time
        // min = max = null;
    }

    public void setPawnStructure(Position pos, boolean on) {
        key = null;
        keyReversed = null;
        if (on)
            sig = (MatSignature) pos.updateMatSig().clone();
        else
            sig.clear();
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
        pos.setOption(Position.INCREMENT_REVERSED_HASH, exactPosition() && reversedColor);
        pos.setOption(Position.INCREMENT_SIGNATURE,exactPosition()||pawnStructure());
        pos.setOption(Position.IGNORE_FLAGS_ON_HASH, exactPosition());
    }

    //  @return true if we have found a position
    public boolean matches(Position pos, boolean wasNoisy) {
        if (exactPosition()) {
            //  hash key is checked with every position
            assert(pos.hasOption(Position.INCREMENT_HASH));
            assert(pos.hasOption(Position.INCREMENT_REVERSED_HASH));
            assert(pos.hasOption(Position.IGNORE_FLAGS_ON_HASH));

            if (pos.getHashKey().equals(key) || reversedColor && pos.getReversedHashKey().equals(keyReversed))
                return true;
        }
        //  pawn structure and mat features are only checked after *noisy* moves
        //  (i.e. after changes in pawn structure, mat count, ...)
        if (!wasNoisy) return false; // -> keep on searching

        if (pawnStructure()) {
// todo            return sig.isPawnSubsetOf(pos.getMatSig())
//                    || reversedColor && sig.isReversedPawnSubsetOf(pos.getMatSig());
        }
        //  todo compare mat balance
        //  todo compare bishop features
        return false;
    }

    //  early cut-off if query can not be reached from end
    public boolean earlyCutOff(MatSignature endSignature) {
        if (exactPosition()) {
            if (!sig.canReach(endSignature) && (!reversedColor || !sig.canReachReversed(endSignature)))
                return true;
            // todo except if we can find it in variations
            //  this.variations && Game.Attributes & HAS_VARIATIONS
        }
        if (pawnStructure()) {
            //  ignore officers during canReach()
            //  todo add all(?) officers to sig. Or have an additional flag for canReach()
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
        if (exactPosition()) {
            if (!matSig.canReach(sig) && (!reversedColor || !matSig.canReachReversed(sig)))
                return true;
        }
        if (pawnStructure()) {
            //  ignore officers during canReach()
            //  todo remove all officers from sig?
            if (!matSig.canReach(sig) && !(reversedColor && matSig.canReachReversed(sig)))
                return true;
        }
        //  todo check mat balance
        return false;
    }
}
