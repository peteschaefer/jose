package de.jose.book.lichess;

import de.jose.book.BookEntry;
import de.jose.chess.Move;
import org.json.JSONObject;

import java.util.ArrayList;

public class LiChessBookEntry
    extends BookEntry
{
    public ArrayList<LiChessGameRef> gameRef  = new ArrayList<>();

    public LiChessBookEntry(JSONObject json)
    {
        String mv = json.getString("uci");
        move = Move.fromString(mv);
        countWhite = json.getInt("white");
        countDraw = json.getInt("draws");
        countBlack = json.getInt("black");
        count = (countWhite+countDraw+countBlack);
    }
}
