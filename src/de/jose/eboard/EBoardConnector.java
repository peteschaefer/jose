package de.jose.eboard;

import com.chessnut.EasyLink;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import de.jose.AbstractApplication;
import de.jose.Application;
import de.jose.Command;
import de.jose.chess.*;
import de.jose.util.StringUtil;
import de.jose.view.IBoardAdapter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.text.ParseException;

public class EBoardConnector implements EasyLink.IRealTimeCallback
{
    enum Mode {
        DISCONNECTED,
        PLAY,
        SETUP_LEAD, SETUP_FOLLOW
    }

    enum Orientation {
        WHITE_UP, BLACK_UP, AUTO_DETECT
    }

    public Mode mode;

    private Orientation inputOri;
    private Orientation currentOri;

    private IBoardAdapter appBoard;

    public EBoardConnector(IBoardAdapter anAppBoard)
    {
        mode = Mode.DISCONNECTED;
        inputOri = Orientation.AUTO_DETECT;  // todo read from UserProfile
        currentOri = Orientation.WHITE_UP;  // todo read from UserProfile

        board = new BoardState[2];
        board[0] = new BoardState();
        board[1] = new BoardState();

        appBoard = anAppBoard;
    }

    public boolean connect()
    {
        if (EasyLink.connect()!=0) {
            EasyLink.setRealtimeCallback(this);
            EasyLink.switchRealTimeMode();
            EasyLink.led(EasyLink.NO_LEDS);
            mode = Mode.PLAY;
            synchFromApp();
        }
        else
            mode = Mode.DISCONNECTED;
        return (mode!=Mode.DISCONNECTED);
    }

    public void disconnect()
    {
        mode = Mode.DISCONNECTED;
        EasyLink.disconnect();
    }

    @Override
    public void realTimeCallback(String fen) {
        //  called from E-Board
        synchFromBoard(fen);
    }


    public synchronized void synchFromBoard(String fen)
    {
        if (mode == Mode.DISCONNECTED) {
            //  now connected
            mode = Mode.PLAY;
            EasyLink.beep(800,500);
        }

        if (setExplodedFen(fen,board[0].fen)==0)
            return; //  nothing has changed
        if (appXFen==null)
            return;   //  nothing to do, yet

        computeDiff();
        showLeds(board[currentOri.ordinal()].diff,currentOri);

        /*  todo
            IF diff==empty AND currentOri changed
            THEN turn application board view to reflect currentOri
         */

        switch(mode) {
            case PLAY:
                //  todo look for (a) legal move,
                Move mv = guessMove(board[currentOri.ordinal()],appXFen,appBoard.getPosition());
                if (mv!=null /*&& appBoard.isLegal(mv)*/)
                    userMove(mv);
                //  todo (b) retract last _human_ move
                break;
            case SETUP_LEAD:
                //  todo send board[currentOri].fen to application
                break;
            case SETUP_FOLLOW:
                //  ignore
                break;
        }
    }

    private void userMove(Move mv)
    {
        Command cmd = new Command("move.user", null, mv);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AbstractApplication.theCommandDispatcher.handle(cmd,Application.theApplication);
            }
        });
    }

    private static int squareAt(int xfenIndex)
    {
        int row = Constants.ROW_8 - (xfenIndex/9);
        int file = Constants.FILE_A + xfenIndex % 9;
        return EngUtil.square(file,row);
    }

    private static int xfenIndex(int square)
    {
        int row = Constants.ROW_8-EngUtil.rowOf(square);
        int file = EngUtil.fileOf(square)-Constants.FILE_A;
        return row*9+file;
    }

    private Move guessMove(BoardState st, StringBuilder fen, Position pos)
    {
        MoveIterator moves = new MoveIterator(pos);
        while(moves.next())
        {
            Move mv = moves.getMove();
            //  check move against diff and ..
            if (mv.diffCount() != st.diff_cnt) continue;
            int ifrom = xfenIndex(mv.from);
            int ito = xfenIndex(mv.to);

            if (st.diff.charAt(ifrom)!='1') continue;
            if (st.diff.charAt(ito)!='1') continue;
            if (st.fen.charAt(ifrom)!='1') continue;

            if (!pos.checkMove(mv)) continue;

            //char pfrom = EngUtil.coloredPieceCharacter(pos.pieceAt(mv.from));
            char pto = EngUtil.coloredPieceCharacter(mv.getDestinationPiece());
            if (st.fen.charAt(ito)!=pto) continue;
            return mv;
        }
        return null;
    }

    public synchronized void synchFromApp()
    {
        if (setExplodedFen(appBoard,appXFen)==0)
            return;

        if (mode == Mode.DISCONNECTED)
            return;

        computeDiff();
        showLeds(board[currentOri.ordinal()].diff,currentOri);

        if(mode==Mode.SETUP_FOLLOW)
            throw new IllegalStateException("don't modify position in follow mode");
    }

    private static String EMPTY_X_FEN = "11111111/11111111/11111111/11111111/11111111/11111111/11111111/11111111";

    private class BoardState {
        StringBuilder fen = new StringBuilder(EMPTY_X_FEN);
        StringBuilder diff = new StringBuilder(EasyLink.NO_LEDS);
        int diff_cnt = 0;
    }

    // ---------------------------------------------

    private BoardState[] board;
    private StringBuilder appXFen = new StringBuilder(EMPTY_X_FEN);
    private String lastLeds = EasyLink.NO_LEDS;

    private void computeDiff()
    {
        setReversed(board[0].fen,board[1].fen);

        switch (inputOri) {
            case WHITE_UP:
            case BLACK_UP:
                computeDiff(board[inputOri.ordinal()]);
                currentOri=inputOri;
                return;
            case AUTO_DETECT:
                int d0 = computeDiff(board[0]);
                int d1 = computeDiff(board[1]);
                if (d0 < d1)
                    currentOri=Orientation.WHITE_UP;
                if (d0 > d1)
                    currentOri=Orientation.BLACK_UP;
        }
    }

    private int computeDiff(BoardState state)
    {
        return state.diff_cnt = computeDiff(state.fen,appXFen,state.diff);
    }
    
    private static int computeDiff(StringBuilder afen, StringBuilder bfen, StringBuilder diff)
    {
        int count=0;
        diff.setLength(0);
        assert(afen.length()==bfen.length());
        assert(afen.length()==71);

        for(int i=0; i<71; i++) {
            char ac = afen.charAt(i);
            char bc = bfen.charAt(i);
            if (ac=='/') {
                assert(bc=='/');
                diff.append('/');
            }
            else if (ac==bc)
                diff.append('0');
            else {
                diff.append('1');
                count++;
            }
        }
        return count;
    }

    private void showLeds(StringBuilder diff, Orientation ori)
    {
        String newLeds = diff.toString();
        if (ori==Orientation.BLACK_UP)
            newLeds = StringUtil.reverse(newLeds);
        if (! newLeds.equals(lastLeds))
            EasyLink.led(lastLeds = newLeds);
    }

    private static int setExplodedFen(String fen, StringBuilder exploded)
    {
        assert(exploded.length()==71);
        int xat=0;
        int mod=0;

        for(int i=0; i<fen.length(); i++)
        {
            char c = fen.charAt(i);
            if (c=='/') {
                assert(exploded.charAt(xat)=='/');
                xat++;
            }
            else if (c>='1' && c<='8') {
                int empty = (c-'0');
                for(int j=0; j < empty; ++j) {
                    if (exploded.charAt(xat)!='1') {
                        exploded.setCharAt(xat, '1');
                        mod++;
                    }
                    xat++;
                }
            }
            else {
                if (exploded.charAt(xat)!=c) {
                    exploded.setCharAt(xat, c);
                    mod++;
                }
                xat++;
            }
        }
        assert(xat==71);
        return mod;
    }

    private static int setExplodedFen(IBoardAdapter board, StringBuilder exploded)
    {
        assert(exploded.length()==71);
        int xat=0;
        int mod=0;

        for(int row=Constants.ROW_8; row >= Constants.ROW_1; row--)
        {
            for(int file=Constants.FILE_A; file <= Constants.FILE_H; file++) {
                int pc = board.pieceAt(file, row);
                char ch = EngUtil.coloredPieceCharacter(pc);
                if (ch==' ') ch = '1';
                if (exploded.charAt(xat)!=ch) {
                    exploded.setCharAt(xat, ch);
                    mod++;
                }
                xat++;
            }
            if (row > Constants.ROW_1) {
                assert (exploded.charAt(xat) == '/');
                xat++;
            }
        }
        assert(xat==71);
        return mod;
    }

    private static void setReversed(StringBuilder a, StringBuilder b)
    {
        b.setLength(0);
        b.append(a);
        b.reverse();
    }
}
