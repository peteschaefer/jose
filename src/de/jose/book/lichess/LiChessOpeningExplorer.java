package de.jose.book.lichess;

import de.jose.AbstractApplication;
import de.jose.Application;
import de.jose.Command;
import de.jose.Language;
import de.jose.book.BookEntry;
import de.jose.book.OpeningBook;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.db.JoConnection;
import de.jose.pgn.*;
import de.jose.task.GameSource;
import de.jose.task.io.PGNImport;
import de.jose.util.HttpsUtil;
import de.jose.util.ListUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.window.JoDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sound.midi.SysexMessage;
import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LiChessOpeningExplorer extends OpeningBook
{
    public static String apiUrl;
    public static String downloadUrl;

    //  list only moves if there are at least 20 games
    public static final int MIN_GAMES_PLAYED = 20;

    private boolean NETWORK_ERROR_REPORTED = false;

    //  number of top games in each query
    public static final int TOP_GAMES = 8;

    public LiChessOpeningExplorer(org.w3c.dom.Element config)
    {
        apiUrl = XMLUtil.getChildValue(config,"URL");
        downloadUrl = XMLUtil.getChildValue(config,"DOWNLOAD");
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
                return getBookMoves1(pos, ignoreColors, deep, TOP_GAMES, result);
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

    public boolean getBookMoves1(Position pos, boolean ignoreColors, boolean deep,
                                 int topGames,
                                 List<BookEntry> result) throws IOException
    {
        String fen = pos.toString();
        fen = URLEncoder.encode(fen);
        String urlString = apiUrl+"?fen="+fen+"&topGames="+topGames;    //  don't enumerate games

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
                    //System.out.println(ref);
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

    public static void startDownload(LiChessGameRef gameRef)     {
        Runnable job = new Runnable() {
            @Override
            public void run() {
                PGNImport reader = null;
                try {
                    int CId = Collection.DOWNLOADS_ID;
                    JoConnection conn = null;
                    try {
                        conn = JoConnection.get();
                        Collection.makeDownloads(conn);
                    } finally {
                        if (conn!=null) conn.release();
                    }

                    String urlStr = downloadUrl+"/"+gameRef.id+"?evals=false&literate=true";
                    URL url = new URL(urlStr);

                    reader = PGNImport.newPgnImporter(CId,url);
                    reader.setSilentTime(2000);

                    reader.start();
                    reader.join();
                    //  wait for reader to finish
                    //  fetch inserted Game.Id
                    int GId1 = reader.getFirstGameId();
                    int GId2 = reader.getLastGameId();

                    //  open downloaded game for editing:
                    if (GId2 > 0) editLater(gameRef,GId1,GId2);

                    //  open in Tab
                } catch(FileNotFoundException e) {
                  //    this can happen if the advertised ID is not available
                  //    not our fault :(
                    excuseLater(gameRef.toString());
                } catch (Exception e) {
                    Application.error(e);
                }
            }
        };

        Application.theExecutorService.submit(job);
    }

    protected static void excuseLater(String game)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HashMap args = new HashMap();
                args.put("game", game);
                String msg = Language.args("download.error.lichess", args);
                JoDialog.showErrorDialog(msg);
            }
        });
    }

    protected static void editLater(LiChessGameRef gameRef, int GId1,int GId2)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                openInTabs(GId1, GId2);

                //  get the Game dom and insert it as sub-line into the Original game:
                MoveNode mvnd = Application.theApplication.theGame.getCurrentMove();
                for(int GId = GId1; GId <= GId2; GId++) {
                    Game gm = Application.theHistory.getById(GId);
                    if (gm!=null) {
                        insertNewLine(gm, mvnd, gameRef.toString());
                        return;
                    }
                }
            }
        });
    }

    private static void insertNewLine(Game gm, MoveNode mvnd, String label)
    {
        //  clip sub-line from Game
        LineNode mainLine = gm.getMainLine();
        MoveNode cut = mainLine.moveByPly(mvnd.getPly()+1);
        if (cut==null) return;

        LineNode subline = mainLine.cloneFrom(cut);

        CommentNode comment = new CommentNode(label);
        comment.insertFirst(subline);

        //  insert into original Game
        subline.insertAfter(mvnd);  //  todo find the exact place to insert a new line
        //  trigger update, etc.
        Command cmd = new Command("edit.game.paste",null, mvnd, subline);
        AbstractApplication.theCommandDispatcher.handle(cmd,Application.theApplication);
    }

    private static void openInTabs(int GId1, int GId2) {
        //  open downloaded game in a Tab:
        int[] gids = ListUtil.intRange(GId1, GId2);
        Command cmd = new Command("edit.game", null,
                GameSource.gameArray(gids),
                Boolean.FALSE);
        AbstractApplication.theCommandDispatcher.handle(cmd,Application.theApplication);
    }
}
