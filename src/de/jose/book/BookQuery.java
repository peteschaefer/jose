package de.jose.book;

import de.jose.Application;
import de.jose.book.crafty.CraftyHashKey;
import de.jose.book.polyglot.PolyglotBook;
import de.jose.book.polyglot.PolyglotHashKey;
import de.jose.book.shredder.ShredderHashKey;
import de.jose.chess.Board;
import de.jose.chess.Position;
import de.jose.comm.msg.MessageProducer;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;
import de.jose.plugin.AnalysisRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.jose.Application.ANALYSIS;
import static de.jose.Application.USER_INPUT;
import static de.jose.plugin.Plugin.BOOK_RESPONSE;

public class BookQuery
        extends MessageProducer
        implements Runnable
{
    public static long theSeqNo=0;

    //  Application state. If it changes, the query is discarded
    long seqNo; // sequence number for each query
    Game appGame;
    MoveNode appMove;
    int appMode;

    // Query argument
    public String fen;

    //  Query results
    public List<BookEntry> result;

    //  upon completion, send message to Application
    public boolean switchEngineAnalysis;

    public BookQuery(Position pos, boolean switchEngineAnalysis)
    {
        seqNo = ++theSeqNo;
        appGame = Application.theApplication.theGame;
        appMove = appGame.getCurrentMove();
        appMode = Application.theApplication.theMode;

        fen = pos.toString();

        this.switchEngineAnalysis = switchEngineAnalysis;
        addMessageListener(Application.theApplication);
    }

    public boolean isValid()
    {
        return seqNo==theSeqNo
                && appGame==Application.theApplication.theGame
                && appMove==appGame.getCurrentMove()
                && appMode==Application.theApplication.theMode;
    }

    void throwInvalid() throws InterruptedException {
        if (!isValid()) //  this query is discarded. abort anything
            throw new InterruptedException("query out of date");
    }

    @Override
    public void run()
    {
        try {

            OpeningLibrary lib = Application.theApplication.theOpeningLibrary;
            boolean go_deep = (appMode==USER_INPUT || appMode==ANALYSIS);
            result = lib.collectMoves(null,fen, go_deep,true,false);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (isValid())
            sendMessage(BOOK_RESPONSE,this);
    }
}
