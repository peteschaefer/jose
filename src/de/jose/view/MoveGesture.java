package de.jose.view;

import de.jose.chess.Constants;
import de.jose.chess.Move;

public class MoveGesture implements Constants
{
    public static int origPiece(Move mv) {
        if (mv.isStationaryFRCCastling())
            return ROOK+mv.moving.color();
        else
            return mv.moving.piece();
    }
    public static int origSquare(Move mv) {
        if (mv.isStationaryFRCCastling())
            return mv.to;
        else
            return mv.from;
    }

    public static int destPiece(Move mv) {
        if (mv.isPromotion())
            return mv.getColoredPromotionPiece();
        else
            return origPiece(mv);
    }

    public static int destSquare(Move mv) {
        //  kingside castling FRC
        //  king ALWAYS moves to G1, rook ALWAYS moves to F1
        if (mv.isStationaryFRCCastling()) {
            switch (mv.castlingMask())
            {
                case WHITE_KINGS_CASTLING:	return F1;
                case BLACK_KINGS_CASTLING:  return F8;
                case WHITE_QUEENS_CASTLING:	return D1;
                case BLACK_QUEENS_CASTLING: return D8;
            }
        }
        else {
            switch (mv.castlingMask())
            {
                case WHITE_KINGS_CASTLING:	return G1;
                case BLACK_KINGS_CASTLING:  return G8;
                case WHITE_QUEENS_CASTLING:	return C1;
                case BLACK_QUEENS_CASTLING: return C8;
            }
        }
        //else
        return mv.to;
    }

    public static int diffCount(Move mv) {
        if (mv.isEnPassant()) return 3;
        int cnt=2;
        switch (mv.castlingMask())
        {
            case WHITE_KINGS_CASTLING:
                if (mv.from!=F1 && mv.from!=G1) cnt++;
                if (mv.to!=F1 && mv.to!=G1) cnt++;
                break;
            case BLACK_KINGS_CASTLING:
                if (mv.from!=F8 && mv.from!=G8) cnt++;
                if (mv.to!=F8 && mv.to!=G8) cnt++;
                break;
            case WHITE_QUEENS_CASTLING:
                if (mv.from!=C1 && mv.from!=D1) cnt++;
                if (mv.to!=C1 && mv.to!=D1) cnt++;
                break;
            case BLACK_QUEENS_CASTLING:
                if (mv.from!=C8 && mv.from!=D8) cnt++;
                if (mv.to!=C8 && mv.to!=D8) cnt++;
                break;

        }
        return cnt;
    }

}
