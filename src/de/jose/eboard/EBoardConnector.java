package de.jose.eboard;

import com.chessnut.EasyLink;
import de.jose.chess.EngUtil;
import de.jose.util.StringUtil;
import de.jose.view.IBoardAdapter;
import de.jose.chess.Constants;

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


    public void synchFromBoard(String fen)
    {
        if (mode == Mode.DISCONNECTED) {
            //  now connected
            mode = Mode.PLAY;
            EasyLink.beep(800,500);
        }

        setExplodedFen(fen,board[0].fen);
        if (appFen==null) return;   //  nothing to do, yet

        computeDiff();
        showLeds(board[currentOri.ordinal()].diff,currentOri);

        switch(mode) {
            case PLAY:
                //  todo look for (a) legal move,
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

    public void synchFromApp()
    {
        setExplodedFen(appBoard,appFen);

        if (mode == Mode.DISCONNECTED)
            return;

        computeDiff();
        showLeds(board[currentOri.ordinal()].diff,currentOri);

        if(mode==Mode.SETUP_FOLLOW)
            throw new IllegalStateException("don't modify position in follow mode");
    }


    private class BoardState {
        StringBuilder fen = new StringBuilder();
        StringBuilder diff = new StringBuilder();
        int diff_cnt = 0;
    }

    // ---------------------------------------------

    private BoardState[] board;
    private StringBuilder appFen = new StringBuilder();
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
        return state.diff_cnt = computeDiff(state.fen,appFen,state.diff);
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
            EasyLink.led(newLeds = lastLeds);
    }

    private static void setExplodedFen(String fen, StringBuilder exploded)
    {
        exploded.setLength(0);
        for(int i=0; i<fen.length(); i++)
        {
            char c = fen.charAt(i);
            if (c=='/')
                exploded.append('/');
            else if (c>='1' && c<='8') {
                int empty = (c-'0');
                for(int j=0; j < empty; ++j)
                    exploded.append('1');
            }
            else
                exploded.append(c);
        }
    }

    private static void setExplodedFen(IBoardAdapter board, StringBuilder exploded)
    {
        exploded.setLength(0);
        for(int row=Constants.ROW_8; row >= Constants.ROW_1; row--)
        {
            for(int file=Constants.FILE_A; file <= Constants.FILE_H; file++) {
                int pc = board.pieceAt(file, row);
                char ch = EngUtil.coloredPieceCharacter(pc);
                if (ch==' ') ch = '1';
                exploded.append(ch);
            }
            if (row > Constants.ROW_1)
                exploded.append('/');
        }
    }

    private static void setReversed(StringBuilder a, StringBuilder b)
    {
        b.setLength(0);
        b.append(a);
        b.reverse();
    }
}
