package de.jose.book.lichess;

import de.jose.Application;
import de.jose.book.BookEntry;
import de.jose.book.OpeningBook;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;
import de.jose.pgn.ResultNode;
import de.jose.util.xml.XMLUtil;
import de.jose.window.JoDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LiChessOpeningExplorer extends OpeningBook
{
    private String apiUrl;

    //  list only moves if there are at least 20 games
    public static final int MIN_GAMES_PLAYED = 20;

    private boolean NETWORK_ERROR_REPORTED = false;

    public LiChessOpeningExplorer(org.w3c.dom.Element config)
    {
        apiUrl = XMLUtil.getChildValue(config,"URL");
    }

    public static String getInfoText(org.w3c.dom.Element config, boolean enabled)
    {
        StringBuffer buf = new StringBuffer();

        if (!enabled) buf.append("<font color=#aaaaaa>");

        buf.append("<b>");
        buf.append(getTitle(config));
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


    public static String getTitle(org.w3c.dom.Element config)
    {
        return XMLUtil.getChildValue(config,"FILE");
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
    public boolean getBookMoves(Position pos, boolean ignoreColors, boolean deep, List<BookEntry> result) throws IOException
    {
        //  run asynchroneously
        Callable<Boolean> task = new Callable() {
            @Override
            public Boolean call() throws Exception {
                return getBookMoves1(pos, ignoreColors, deep, result);
            }
        };

        /*
            I would like to query Lichess asynchroneously. It's perfectly possible with ExecutorService
            but then the logic for coordinating EnginePanel with OpeningBook and Application becomes difficult.
            In/out opening book triggers engine analysis, etc.
            With asynch book moves the state transitions becomes too complicated.

            As a compromise, we do a *blocking* query with fixed time-out.
         */
        Future<Boolean> fut = Application.theExecutorService.submit(task);
        try {
            //  .. synchroneously, but with fixed time-out
            return fut.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        } catch (ExecutionException e) {
            return false;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean getBookMoves1(Position pos, boolean ignoreColors, boolean deep, List<BookEntry> result) throws IOException
    {
        String fen = pos.toString();
        fen = URLEncoder.encode(fen);
        String urlString = apiUrl+"?fen="+fen+"&topGames=6";    //  don't enumerate games

        try {
            URL url = new URL(urlString);
            InputStream in = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String jsonText = br.lines().collect(Collectors.joining("\n"));

            JSONObject obj = new org.json.JSONObject(jsonText);
            JSONArray moves = obj.getJSONArray("moves");
            HashMap<String,LiChessBookEntry> mvmap = new HashMap();

            for(int i=0; i<moves.length(); i++) {
                JSONObject json = moves.getJSONObject(i);
                String key = json.getString("uci");

                LiChessBookEntry bk = new LiChessBookEntry(json);
                mvmap.put(key,bk);

                if (deep || bk.count >= MIN_GAMES_PLAYED)
                    result.add(bk);
            }

            JSONArray games = obj.getJSONArray("topGames");
            for(int j=0; j<games.length(); j++)
            {
                JSONObject json = games.getJSONObject(j);
                String key = json.getString("uci");

                LiChessGameRef ref = new LiChessGameRef(json);
                LiChessBookEntry bk = mvmap.get(key);

                if (bk!=null) {
                    bk.gameRefs.add(ref);
                    System.out.println(ref);
                }
            }

            return true;
        } catch (IOException e) {
            /*  network failures are not fatal.
                inform the user to check their Internet connection.
                Then proceed without Lichess data.
             */
            if (!NETWORK_ERROR_REPORTED) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JoDialog.showErrorDialog("network.error.lichess");
                    }
                });
                NETWORK_ERROR_REPORTED = true;
            }
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BookEntry selectBookMove(Position pos, boolean ignoreColors, Random random) throws IOException {
        //  let opening Library do it
        return null;
    }
}
