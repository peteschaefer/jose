package de.jose.book.lichess;

import de.jose.Language;
import de.jose.book.BookEntry;
import de.jose.book.OpeningBook;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.util.StringUtil;
import de.jose.util.xml.XMLUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LiChessOpeningExplorer extends OpeningBook
{
    private String apiUrl;

    //  list only moves if there are at least 20 games
    public static final int MIN_GAMES_PLAYED = 20;

    public LiChessOpeningExplorer(org.w3c.dom.Element config)
    {
        apiUrl = XMLUtil.getChildValue(config,"URL");
    }

    public static String getInfoText(org.w3c.dom.Element config, boolean enabled)
    {
        StringBuffer buf = new StringBuffer();

        if (!enabled) buf.append("<font color=#aaaaaa>");

        String title = XMLUtil.getChildValue(config,"FILE");
        buf.append("<b>");
        buf.append(title);
        buf.append("</b>");

        if (!enabled) buf.append("</font>");

        String comment = XMLUtil.getChildValue(config,"COMMENT");
        if (comment!=null) {
            buf.append("<br><font size=2>");
            buf.append(comment);
            buf.append("</font>");
        }

        return buf.toString();
    }

    @Override
    public boolean canTranspose() {
        return false;
    }

    @Override
    public boolean canTransposeColor() {
        return false;
    }

    @Override
    public boolean canTransposeIntoBook() {
        return false;
    }

    @Override
    public boolean open(RandomAccessFile file) throws IOException {
        return false;
    }

    @Override
    public boolean getBookMoves(Position pos, boolean ignoreColors, List<BookEntry> result) throws IOException
    {
        String fen = pos.toString();
        fen = URLEncoder.encode(fen);
        String urlString = apiUrl+"?fen="+fen+"&topGames=0";    //  don't enumerate games

        try {
            URL url = new URL(urlString);
            InputStream in = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String jsonText = br.lines().collect(Collectors.joining("\n"));

            JSONObject obj = new org.json.JSONObject(jsonText);
            JSONArray moves = obj.getJSONArray("moves");

            for(int i=0; i<moves.length(); i++) {
                JSONObject move = moves.getJSONObject(i);
                String mv = move.getString("uci");

                BookEntry bk = new BookEntry();
                bk.move = Move.fromString(mv);
                bk.countWhite = move.getInt("white");
                bk.countDraw = move.getInt("draws");
                bk.countBlack = move.getInt("black");
                bk.count = (bk.countWhite+bk.countDraw+bk.countBlack);

                if (bk.count >= MIN_GAMES_PLAYED)
                    result.add(bk);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public BookEntry selectBookMove(Position pos, boolean ignoreColors, Random random) throws IOException {
        //  let opening Library do it
        return null;
    }
}
