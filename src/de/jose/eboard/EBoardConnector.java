package de.jose.eboard;

import de.jose.AbstractApplication;
import de.jose.Application;
import de.jose.Command;
import de.jose.CommandAction;
import de.jose.chess.*;
import de.jose.util.StringUtil;
import de.jose.view.IBoardAdapter;
import de.jose.view.MoveGesture;

import javax.swing.*;

public abstract class EBoardConnector
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
    private boolean wasAcked = false;

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
        if (doConnect()) {
            mode = Mode.PLAY;
            synchFromApp();
        }
        else
            mode = Mode.DISCONNECTED;
        return (mode!=Mode.DISCONNECTED);
    }

    protected abstract boolean doConnect();
    protected abstract void doDisconnect();
    protected abstract void doShowLeds(String leds);

    public void disconnect()
    {
        mode = Mode.DISCONNECTED;
        doDisconnect();
    }


    public synchronized void synchFromBoard()
    {
        if (mode == Mode.DISCONNECTED) {
            //  now connected
            mode = Mode.PLAY;
            //EasyLink.beep(800,500);
        }

        //if (setExplodedFen(fen,board[0].fen)==0)
        //    return; //  nothing has changed
        if (appXFen==null)
            return;   //  nothing to do, yet

        computeDiff();
        BoardState st = board[currentOri.ordinal()];
        showLeds(st.diff,currentOri);

        if (st.diff_cnt==0)
            wasAcked = true;    //  App changed was replicated on the E-Board

        /*  todo
            IF diff==empty AND currentOri changed
            THEN turn application board view to reflect currentOri
         */

        switch(mode) {
            case PLAY:
                //  look for (a) legal move,
                Move mv = guessMove(st,appXFen,appBoard.getPosition());
                if (mv!=null /*&& appBoard.isLegal(mv)*/) {
                    userMove(mv);
                    break;
                }
                //  (b) retract last _human_ move
                if (st.diff_cnt < 2) break;
                if (!canUndoMove()) break;

                Position pos = appBoard.getPosition();
                Move m1 = pos.getLastMove(1);
                if (wasAcked && guessUndone(st,m1)) {
                    //  last engine move was replicated on board
                    //  may be taken back
                    undoMove();
                    break;
                }

                Move m2 = pos.getLastMove(2);
                if (!wasAcked && guessUndone(st,m1) && guessUndone(st,m2)) {
                    //  last two moves may be taken back
                    undoMove();
                    undoMove();
                    break;
                }
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
        AbstractApplication.theCommandDispatcher.handle(cmd,Application.theApplication);
    }


//    private static final String MOVE_DELETE = "move.delete";
    private static final String MOVE_UNDO = "move.backward";

    private boolean canUndoMove()
    {
        CommandAction delAction = Application.theCommandDispatcher.findTargetAction(MOVE_UNDO,Application.theApplication);
        return delAction!=null && delAction.isEnabled(MOVE_UNDO);
    }

    private void undoMove()
    {
        if (canUndoMove()) {
            Command cmd = new Command(MOVE_UNDO);
            Application.theCommandDispatcher.handle(cmd, Application.theApplication);
        }
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

    private Move guessMove(BoardState st, StringBuilder appXfen, Position pos)
    {
        if (st.diff_cnt < 2 || st.diff_cnt > 4) return null;
/**
 * todo move gesture
 *  * origin square,piece
 *  * dest square,piece
 *
 * todo extra case: FRC castling, FRC castling with king on G1
 */
        MoveIterator moves = new MoveIterator(pos);
        while(moves.next())
        {
            Move mv = moves.getMove();
            //  check move against diff and ..
            if (st.diff_cnt > MoveGesture.diffCount(mv)) continue;

            if (!st.changed(MoveGesture.origSquare(mv),Constants.EMPTY)) continue;
            if (!pos.checkMove(mv)) continue;
            if (!st.changed(MoveGesture.destSquare(mv),MoveGesture.destPiece(mv))) continue;
            return mv;
        }
        return null;
    }

    private boolean guessUndone(BoardState st, Move m)
    {
        if (m==null) return false;

        if (!st.equals(m.from,m.moving.piece())) return false;
        int dest = MoveGesture.destSquare(m);
        int cap = m.getCapturedPiece();
        if (!st.equals(dest,Constants.EMPTY) && !st.equals(dest,cap)) return false;
        return true;
    }

    public synchronized void synchFromApp()
    {
        if (setExplodedFen(appBoard,appXFen)==0)
            return;

        if (mode == Mode.DISCONNECTED)
            return;

        computeDiff();
        BoardState st = board[currentOri.ordinal()];
        showLeds(st.diff,currentOri);

        if (st.diff_cnt > 0)
            wasAcked = false;    //  App changed was not yet replicated on the E-Board

        if(mode==Mode.SETUP_FOLLOW)
            throw new IllegalStateException("don't modify position in follow mode");
    }

    private static String EMPTY_X_FEN = "11111111/11111111/11111111/11111111/11111111/11111111/11111111/11111111";

    protected class BoardState {
        StringBuilder fen = new StringBuilder(EMPTY_X_FEN);
        StringBuilder diff = new StringBuilder(NO_LEDS);
        int diff_cnt = 0;

        boolean changed(int square, int piece)
        {
            int idx = xfenIndex(square);
            char pc;
            if (piece==Constants.EMPTY)
                pc = '1';
            else
                pc = EngUtil.coloredPieceCharacter(piece);
            return diff.charAt(idx)=='1' && fen.charAt(idx)==pc;
        }
        boolean equals(int square, int piece)
        {
            int idx = xfenIndex(square);
            char pc;
            if (piece==Constants.EMPTY)
                pc = '1';
            else
                pc = EngUtil.coloredPieceCharacter(piece);
            return fen.charAt(idx)==pc;
        }
    }

    // ---------------------------------------------

    protected BoardState[] board;
    private StringBuilder appXFen = new StringBuilder(EMPTY_X_FEN);

    protected static String NO_LEDS = "00000000/00000000/00000000/00000000/00000000/00000000/00000000/00000000";
    protected String lastLeds = NO_LEDS;

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
            doShowLeds(lastLeds = newLeds);
    }

    protected static int setExplodedFen(String fen, StringBuilder exploded)
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
