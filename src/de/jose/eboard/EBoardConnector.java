package de.jose.eboard;

import de.jose.*;
import de.jose.chess.*;
import de.jose.profile.UserProfile;
import de.jose.util.StringUtil;
import de.jose.view.IBoardAdapter;
import de.jose.view.MoveGesture;

import javax.swing.*;
import java.util.Stack;

public abstract class EBoardConnector
{

    public enum Mode {
        PLAY,
        SETUP_LEAD, SETUP_FOLLOW
    }

    enum Orientation {
        WHITE_UP, BLACK_UP, AUTO_DETECT
    }

    public boolean connected;
    //public Mode mode;

    Orientation inputOri;
    private Orientation currentOri;

    static class AppState implements Cloneable {
        IBoardAdapter board;
        Mode mode;
        CommandListener listener;

        public void clear() {
            board = null;
            mode = Mode.PLAY;
            listener = null;
        }
        @Override
        public AppState clone() {
            AppState appState = new AppState();
            appState.board = board;
            appState.mode = mode;
            appState.listener = listener;
            return appState;
        }
    }

    private Stack<AppState> oldAppStates = new Stack<>();
    private AppState app = new AppState();
    private boolean wasAcked = false;

    protected abstract boolean doAvailable();
    protected abstract boolean doConnect();
    protected abstract void doDisconnect();
    protected abstract void doShowLeds(String leds);
    protected abstract void doBeep(int freq, int ms);

    public EBoardConnector()
    {
        connected = false;
        app.mode = Mode.PLAY;
        inputOri = Orientation.AUTO_DETECT;  // todo read from UserProfile
        currentOri = Orientation.WHITE_UP;  // todo read from UserProfile

        board = new BoardState[2];
        board[0] = new BoardState();
        board[1] = new BoardState();
    }

    public Mode getMode() {
        return app.mode;
    }
    public void setMode(Mode mode) {
        app.mode=mode;
    }

    public void useAppBoard(IBoardAdapter anAppBoard, Mode anMode, CommandListener anListener) {
        oldAppStates.push(app.clone());
        app.board = anAppBoard;
        app.mode = anMode;
        app.listener = anListener;

        switch (app.mode) {
            case SETUP_LEAD: if (connected) synchFromBoard(); break;
            case SETUP_FOLLOW: synchFromApp (); break;
        }
    }

    public void reuseAppBoard() {
        if (oldAppStates.isEmpty())
            app.clear();
        else
            app = oldAppStates.pop();
    }

    public void readProfile(UserProfile prf) {
        boolean enabled = prf.getBoolean("eboard.chessnut",false);
        this.inputOri = orientationFromProfile(prf, "eboard.orientation", this.inputOri);
        this.currentOri = orientationFromProfile(prf, "eboard.orientation.current", this.currentOri);
        if (enabled) connect();
    }

    public void storeProfile(UserProfile prf) {
        prf.set("eboard.orientation", this.inputOri.toString());
        prf.set("eboard.orientation.current", this.currentOri.toString());
    }

    private static Orientation orientationFromProfile(UserProfile prf, String key, Orientation deflt)
    {
        String value = prf.getString(key,deflt.toString());
        return Orientation.valueOf(value);
    }

    public boolean isAvailable()
    {
        return doAvailable();
    }

    public boolean connect()
    {
        if(!doAvailable())
            return false;

        if (doConnect()) {
            connected = true;
            synchFromApp();
            doBeep(800,300);
        }
        else
            connected = false;
        return connected;
    }

    public void disconnect()
    {
        connected = false;
        doDisconnect();
    }


    public synchronized void synchFromBoard()
    {
        if (!connected) {
            //  now connected
            connected = true;
            doBeep(800,300);
        }

        //if (setExplodedFen(fen,board[0].fen)==0)
        //    return; //  nothing has changed
        if (appXFen==null)
            return;   //  nothing to do, yet

        boolean flipped = computeDiff();
        BoardState st = board[currentOri.ordinal()];
        showLeds(st.diff,currentOri);

        if (st.diff_cnt==0)
            wasAcked = true;    //  App changed was replicated on the E-Board

        /*if (app.mode==Mode.PLAY && StringUtil.compare(board[currentOri.ordinal()].fen,START_XFEN)==0) {
            //  note that StringBuilder does not overwrite equals()
            //  start a new game?
            if (Application.theApplication.askNewGame())
                return;
        }*/

        switch(app.mode) {
            case PLAY:
                findUserMove(st);
                break;
            case SETUP_LEAD:
                //  send board[currentOri].fen to setup dialog
                Command cmd = new Command("eboard.changed",null, board[currentOri.ordinal()].fen.toString());
                Application.theCommandDispatcher.handle(cmd,app.listener);
                break;
            case SETUP_FOLLOW:
                //  when the position has been synched succesfully, we switch back to LEAD mode
                boolean resynched = (board[currentOri.ordinal()].diff_cnt==0);
                if (resynched) {
                    app.mode = Mode.SETUP_LEAD;
                    cmd = new Command("eboard.mode.changed",null,this);
                    Application.theCommandDispatcher.handle(cmd,app.listener);
                }
                break;
        }
    }

    private void findUserMove(BoardState st)
    {
        //  look for (a) legal move,
        Move mv = guessMove(st,appXFen,app.board.getPosition());
        if (mv!=null) {
            if (!app.board.isLegal(mv))
                doBeep(200,300);
            else
                userMove(mv);
            return;
        }
        /*  todo is there a feasible way to look ahead TWO moves
            to detect exchange sequences ?
         */
        //  (b) retract last _human_ move
        if (st.diff_cnt < 2) return;
        if (!canUndoMove()) return;

        Position pos = app.board.getPosition();
        Move m1 = pos.getLastMove(1);
        if (wasAcked && guessUndone(st,m1)) {
            //  last engine move was replicated on board
            //  may be taken back
            undoMove();
            return;
        }

        Move m2 = pos.getLastMove(2);
        if (!wasAcked && guessUndone(st,m1) && guessUndone(st,m2)) {
            //  last two moves may be taken back
            undoMove();
            undoMove();
            return;
        }
    }

    private void userMove(Move mv)
    {
        Command cmd = new Command("move.user", null, mv);
        AbstractApplication.theCommandDispatcher.handle(cmd,app.listener);
    }


//    private static final String MOVE_DELETE = "move.delete";
    private static final String MOVE_UNDO = "move.backward";

    private boolean canUndoMove()
    {
        CommandAction delAction = Application.theCommandDispatcher.findTargetAction(MOVE_UNDO,app.listener);
        return delAction!=null && delAction.isEnabled(MOVE_UNDO);
    }

    private void undoMove()
    {
        if (canUndoMove()) {
            Command cmd = new Command(MOVE_UNDO);
            Application.theCommandDispatcher.handle(cmd,app.listener);
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
        if (setExplodedFen(app.board,appXFen)==0)
            return;

        if (!connected)
            return;

        computeDiff();
        BoardState st = board[currentOri.ordinal()];
        showLeds(st.diff,currentOri);

        if (st.diff_cnt > 0)
            wasAcked = false;    //  App changed was not yet replicated on the E-Board

        if(app.mode==Mode.SETUP_FOLLOW)
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
    protected static StringBuilder START_XFEN = new StringBuilder(EMPTY_X_FEN);
    static {
        setExplodedFen(Constants.START_POSITION,START_XFEN);
    }

    private boolean computeDiff()
    {
        setReversed(board[0].fen,board[1].fen);
        boolean flipped = false;
        switch (inputOri) {
            case WHITE_UP:
            case BLACK_UP:
                computeDiff(board[inputOri.ordinal()]);
                flipped = (currentOri!=inputOri);
                currentOri=inputOri;
                break;
            case AUTO_DETECT:
                int d0 = computeDiff(board[0]);
                int d1 = computeDiff(board[1]);
                if (d0 < d1) {
                    flipped = (currentOri!=Orientation.WHITE_UP);
                    currentOri = Orientation.WHITE_UP;
                }
                if (d0 > d1) {
                    flipped = (currentOri!=Orientation.BLACK_UP);
                    currentOri = Orientation.BLACK_UP;
                }
                break;
        }
        return flipped;
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
            if (c==' ')
                break;
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
