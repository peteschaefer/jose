package de.jose.book.lichess;

import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import org.json.JSONObject;

public class LiChessGameRef
{
    public String white, black;
    public int year;
    public int result;
    public String id;

    public LiChessGameRef(JSONObject json)
    {
        String uci = json.getString("uci");
        this.id = json.getString("id");

        Object owinner = json.get("winner");
        this.result = PgnConstants.RESULT_UNKNOWN;
        if (owinner!=null) {
            String winner = owinner.toString();
            if (winner.equalsIgnoreCase("white"))
                this.result = PgnConstants.WHITE_WINS;
            if (winner.equalsIgnoreCase("black"))
                this.result = PgnConstants.BLACK_WINS;
        }

        this.year = json.getInt("year");
        JSONObject owhite = json.getJSONObject("white");
        this.white = owhite.getString("name");
        JSONObject oblack = json.getJSONObject("black");
        this.black = oblack.getString("name");
    }

    @Override
    public String toString() {
        return "["+white+"-"+black+" ("+year+") "
                +" "+ PgnUtil.resultString(result)
                +" ["+id+"]]";
    }
}
