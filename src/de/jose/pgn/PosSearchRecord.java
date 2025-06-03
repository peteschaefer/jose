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
    public enum BishopColors { ANY, EVEN_COLORED, OPPOSITE_COLORED }
    public enum BishopQuality { ANY, GOOD, BAD }
    public BishopColors bishopColors;
    public BishopQuality whiteBishop, blackBishop;
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

    //
    //      Setup Query Conditions
    //

    public void setExact(Position pos) {
        key = (HashKey) pos.getHashKey().clone();
        keyReversed = (HashKey) pos.getReversedHashKey().clone();
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
    }

    //  @return true if we have found a position
    public boolean matches(Position pos) {
        if (exactPosition()) {
            return pos.getHashKey().equals(key)
                    || reversedColor && pos.getReversedHashKey().equals(keyReversed);
        }
        if (pawnStructure()) {
// todo            return sig.isPawnSubsetOf(pos.getMatSig())
//                    || reversedColor && sig.isReversedPawnSubsetOf(pos.getMatSig());
        }
        //  todo compare mat balance
        //  todo compare bishop features
        return false;
    }

    //  early cut-off if query can not be reached from end
    public boolean earlyCutOff(MatSignatureV2 endSignature) {
        if (exactPosition()) {
            return !endSignature.canReach(sig)
                    && !(reversedColor && endSignature.canReachReversed(sig));
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
    public boolean cutOff(Position pos) {
        if (exactPosition()) {
            return !pos.getMatSig().canReach(sig)
                    && !(reversedColor && pos.getMatSig().canReachReversed(sig));
        }
        if (pawnStructure()) {
            //  ignore officers during canReach()
            //  todo remove all officers from sig?
            return !pos.getMatSig().canReach(sig)
                    && !(reversedColor && pos.getMatSig().canReachReversed(sig));
        }
        //  todo check mat balance
        return false;
    }
}
