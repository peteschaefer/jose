package de.jose.book.lichess;

import de.jose.book.BookEntry;
import de.jose.chess.EngUtil;
import de.jose.chess.Move;
import org.json.JSONObject;

import java.util.ArrayList;

public class LiChessBookEntry
    extends BookEntry
{
    public LiChessBookEntry(JSONObject json, boolean transposed)
    {
        String mv = json.getString("uci");
        move = Move.fromString(mv);
        countDraw = json.getInt("draws");
        if (transposed) {
            move.from = EngUtil.mirrorSquare(move.from);
            move.to = EngUtil.mirrorSquare(move.to);
            countWhite = json.getInt("black");
            countDraw = json.getInt("draws");
            countBlack = json.getInt("white");
        }
        else {
            countWhite = json.getInt("white");
            countDraw = json.getInt("draws");
            countBlack = json.getInt("black");
        }
        count = (countWhite+countDraw+countBlack);
        gameRefs = new ArrayList<>();
    }
}
