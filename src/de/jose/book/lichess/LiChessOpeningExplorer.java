package de.jose.book.lichess;

import de.jose.AbstractApplication;
import de.jose.Application;
import de.jose.comm.Command;
import de.jose.Language;
import de.jose.book.BookEntry;
import de.jose.book.OpeningBook;
import de.jose.chess.Position;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.pgn.*;
import de.jose.task.GameSource;
import de.jose.task.io.PGNImport;
import de.jose.util.ListUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.window.JoDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    public boolean getBookMoves(Position pos, String fen,
                                boolean ignoreColors, boolean deep, List<BookEntry> result) throws IOException
    {
        //  run asynchroneously
        if (fen==null) fen = pos.toString();
        return getBookMoves1(fen, ignoreColors, deep, TOP_GAMES, result);
    }

    public boolean getBookMoves1(String fen, boolean ignoreColors, boolean deep,
                                 int topGames,
                                 List<BookEntry> result) throws IOException
    {
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
                int[] GIds=null;
                try {
                    int CId = Collection.DOWNLOADS_ID;
                    JoConnection conn = null;
                    try {
                        conn = JoConnection.get();
                        Collection.makeDownloads(conn);

                        GIds = findDownload(conn, CId, gameRef);

                    } finally {
                        if (conn!=null) conn.release();
                    }

                    if (GIds==null)
                        GIds = download1Game(CId, gameRef);

                    if (GIds!=null)
                        editLater(gameRef,GIds[0],GIds[1]);

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

    private static final String SQL_FIND =
            "select Game.Id "+
            " from Game"+
            " left outer join MoreGame on GId=Game.Id "+
            " where CId=? "+
            " and Info like '%GameId={lichessid}%' ";
    /*  todo like '%' is expensive
        have an indexed column MoreGame.ExternalKey
        (but we need to fill it during import, etc.)
     */

    private static int[] findDownload(JoConnection conn, int CId, LiChessGameRef gameRef) throws SQLException
    {
        String sql = SQL_FIND.replace("{lichessid}", gameRef.id);
        JoPreparedStatement pstm = conn.getPreparedStatement(sql);
        pstm.setInt(1, CId);
        int GId = pstm.selectInt();
        if (GId<=0)
            return null;
        else
            return new int[]{GId,GId};
    }

    private static int[] download1Game(int CId, LiChessGameRef gameRef) throws Exception
    {
        PGNImport reader;
        String urlStr = downloadUrl+"/"+ gameRef.id+"?evals=false&literate=true";
        URL url = new URL(urlStr);

        reader = PGNImport.newPgnImporter(CId,url);
        reader.setSilentTime(2000);

        reader.start();
        reader.join();
        //  wait for reader to finish
        //  fetch inserted Game.Id
        int GId1 = reader.getFirstGameId();
        int GId2 = reader.getLastGameId();

        if (GId2 <= 0)
            return null;
        else
            return new int[]{GId1,GId2};
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
                        insertNewLine(gm, mvnd, gameRef.toString(false));
                        return;
                    }
                }
            }
        });
    }

    private static String printSnippet(MoveNode mv1, int count, String label)
    {
        StringBuffer buf = new StringBuffer();
        while(mv1!=null && count>0) {
            buf.append(mv1.getMove().toString());
            buf.append(" ");
            mv1 = mv1.nextMove();
            count--;
        }
        buf.append(" {");
        buf.append(label);
        buf.append("} ");
        return buf.toString();
    }

    private static void insertNewLine(Game gm, MoveNode mvnd, String label)
    {
        //  clip sub-line from Game
        LineNode mainLine = gm.getMainLine();
        MoveNode cut1 = mainLine.moveByPly(mvnd.getPly()+1);
        if (cut1==null) return;
//        Node cut2 = mainLine.last();
//        NodeSection cut = new NodeSection(cut1,cut2);
//        cut.trim(INodeConstants.STATIC_TEXT_NODE);  //  skip Result nodes, e.g.

//        Game orig = Application.theApplication.theGame;
//        LineNode subline = new LineNode(orig);
        //new LineLabelNode(0).insertFirst(subline);
//        subline.cloneFrom(cut.first(),cut.last());
        //new LineLabelNode(0).insertLast(subline);

//        CommentNode comment = new CommentNode(label);
//        comment.insertLast(subline);

        //  insert into original Game
//        orig.insertNewLine(subline,mvnd);

    //  manufacturing LineNodes is complicated
    //  alternative: print snippet (5 moves) to plain text, then paste
    //  (-> no need for cloning and node subleties)
    //  GameUtil.UtilParser is limited, but ok for short snippets

        String snippet = printSnippet(cut1,8,label);
        Command cmd = new Command("menu.game.paste.line",null,snippet,Boolean.TRUE);
        Application.theCommandDispatcher.forward(cmd, Application.theApplication.enginePanel(), true);
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
