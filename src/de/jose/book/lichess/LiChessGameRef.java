package de.jose.book.lichess;

import de.jose.book.GameRef;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import org.json.JSONObject;

public class LiChessGameRef extends GameRef
{
    public String id;

    public LiChessGameRef(JSONObject json)
    {
        String uci = json.getString("uci");
        this.id = json.getString("id");
        this.year = json.getInt("year");

        Object owinner = json.get("winner");
        this.result = PgnConstants.RESULT_UNKNOWN;
        if (owinner!=null) {
            String winner = owinner.toString();
            if (winner.equalsIgnoreCase("white"))
                this.result = PgnConstants.WHITE_WINS;
            if (winner.equalsIgnoreCase("black"))
                this.result = PgnConstants.BLACK_WINS;
        }

        JSONObject owhite = json.getJSONObject("white");
        this.white = owhite.getString("name");
        JSONObject oblack = json.getJSONObject("black");
        this.black = oblack.getString("name");
    }
}
