package de.jose.book.lichess;

import de.jose.Application;
import de.jose.pgn.Collection;
import de.jose.task.io.PGNImport;

import java.net.URL;

public class LiChessDownload
        implements Runnable
{
    private int CId = Collection.DOWNLOADS_ID;
    private String lichessId;

    public LiChessDownload(int CId, String lichessId) {
        this.CId = CId;
        this.lichessId = lichessId;
    }

    public static void download(String id) {
        LiChessDownload dnld = new LiChessDownload(Collection.DOWNLOADS_ID, id);
        Application.theExecutorService.submit(dnld);
    }

    @Override
    public void run() {
        Collection.makeDownloads();
//        URL url = new URL("?");

//        PGNImport imprt = PGNImport.openURL(url);
        //  get
        //  parse (PgnImport from URL?)
        //  save
        //  open tab
    }
}
