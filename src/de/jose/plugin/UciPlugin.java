/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.plugin;

import de.jose.Application;
import de.jose.Util;
import de.jose.Version;
import de.jose.chess.*;
import de.jose.util.ProcessUtil;
import de.jose.util.StringUtil;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.List;

/**
 *  implements the UCI engine protocol
 *
 * the following UCI features are not yet supported
 *
 *  register, registration checkin, copyprotection checking
 *      which engines do require this ?
 *
 *  refutation, currline,
 *  setoption UCI_ShowRefutations, UCI_ShowCurrLine
 *      which engines do support this ?
 *
 *  setoption UCI_Opponent,
 *  info tbhits, hashfull, cpuload
 *  debug on/off
 *
 *  FRC
 *      option UCI_Chess960
 *
 * @author Peter Schäfer
 */

public class UciPlugin
		extends EnginePlugin
		implements Constants
{
	/** current thread that is waiting for "uciok"  */
    protected Move ponderMove;
	protected FormattedMove hint;

    protected String engineName;
    protected String engineAuthor;

    protected String waitCommand;
    protected Thread waitThread;

	protected StringBuffer currentLine = new StringBuffer();
	protected int ignoreMoves = 0;
	protected boolean hasAnalyseOption = false;
	protected boolean readOptions = false;
	protected boolean frcMode = false;
	protected boolean leelaMoveStats = false;

	protected Vector<Option> options = new Vector<Option>();

	/** option types (= input elements) */

	public static final int CHECKBOX    = 1;
	public static final int SPIN        = 2;
	public static final int COMBO       = 3;
	public static final int BUTTON      = 4;
	public static final int STRING      = 5;

	/** these UCI options are usually used for file paths   */
	public static final String[] FILE_OPTIONS   = {
		"BookFile", "LearnBookFile", "WeightsFile",
		"EvalFile", "EvalFileSmall",
		"LogFile", "ConfigFile", "Debug Log File"
	};
	/** these UCI options are usually used for directory paths */
	public static final String[] DIR_OPTIONS = {
		"NalimovPath","BitbasePath", "SyzygyPath"
	};
    /** this UCI option is displayed as read-only text */
    public static final String[] READ_ONLY_OPTIONS = {
        "UCI_EngineAbout",
    };

	/**
	 * for the position command:
	 * prefer       "position startpos moves ..."
	 * instead of   "position fen ..."
	 *
	 * "position fen" is shorter but it might confuse some UCI engines
	 * (causing them to flush hash tables, etc.)
	 */
	public static boolean PREFER_MOVES = true;


	public static class Option
	{
		public String name;
		public int type;

		public String defaultValue;
		public int minValue;
		public int maxValue;
		public Vector<String> values;

		public int defaultIntValue()            { return StringUtil.parseInt(defaultValue); }
		public boolean defaultBooleanValue()    { return Util.toboolean(defaultValue); }
	}

    public boolean canOfferDraw() {
        return false;   //  can't with UCI
    }

    public boolean canAcceptDraw() {
        return false;  //  can't with UCI
    }

	public void offerDrawToEngine() {
		//    can't with UCI
	}

	public boolean isBookEnabled()
	{
		String bookEnabled = getOptionValue("OwnBook");
		if (bookEnabled==null || ! "true".equals(bookEnabled))
			return false;
		else
			return true;
//		String bookFile = getOptionValue("BookFile");
	}

	public void disableBook()
	{
		if (supportsOption("OwnBook"))
			setOption("OwnBook","false");
	}

	public boolean canResign() {
		return false;
	}

	/** FRC TODO */
	public boolean supportsFRC()
	{
		return getOption(options,"UCI_Chess960")!=null;
	}

	/** FRC TODO */
	public void enableFRC(boolean on)
	{
		if (on && !frcMode && supportsFRC()) {
			setOption("UCI_Chess960", "true");
			frcMode=on;
			/**  As R.Scharnagl pointed out correctly, we don't need a dedicated FRC mode
			/** so there is no reason  to *disable* it ?
			 * */
		}
	}

	public String getEngineAuthor()
	{
		return engineAuthor;
	}

	public String getEngineName()
	{
		return engineName;
	}

	public Vector getUciOptions()
	{
		return options;
	}

	public boolean supportsOption(String optionName)
	{
		Option option = getOption(options,optionName);
		return option!=null;
		/*if (option==null)
			return false;
		else
			return option.defaultBooleanValue();*/
	}


	public Vector getUciButtons()
	{
		Vector collect = new Vector();
		for (int i=0; i < options.size(); i++)
		{
			Option option = (Option)options.get(i);
			if (option.type==BUTTON)
				collect.add(option);
		}
		return collect;
	}

	public static boolean isFileOption(String option)
	{
		for (int i=0; i < FILE_OPTIONS.length; i++)
			if (FILE_OPTIONS[i].equalsIgnoreCase(option))
				return true;
		return false;
	}

	public static boolean isDirectoryOption(String option)
	{
		for (int i=0; i < DIR_OPTIONS.length; i++)
			if (DIR_OPTIONS[i].equalsIgnoreCase(option))
				return true;
		return false;
	}

    public static boolean isReadOnly(String option)
    {
        for (int i=0; i < READ_ONLY_OPTIONS.length; i++)
            if (READ_ONLY_OPTIONS[i].equalsIgnoreCase(option))
                return true;
        return false;
    }

	public boolean useWDL(boolean on) {
		if (!supportsOption("UCI_WDL")) return false;
		setOption("UCI_WDL",Boolean.toString(on));
		return true;
	}


	public boolean open(String osName) throws IOException
	{
/*        stdInThread.addInputListener(new InputListener() {
            public void readLine(String s) { System.out.println(">"+s); }
            public void readEOF() {  }
            public void readError(Throwable ex) { ex.printStackTrace(); }
        });
*/
        //	startup commands
        String[] startup = StringUtil.separateLines(getStartup(osName));
        if (startup!=null)
            for (int i=0; i < startup.length; i++)
                printOut.println(startup[i]);

        //  wait for "uciok"
        //  engine responds with "id"
        //  and option definitions
		readOptions = true;
		hasAnalyseOption = supportsOption("UCI_AnalyseMode");
		if (!waitFor("uci","uciok",10000)) throw new IOException("uciok expected");

		//	"hidden" Leela option
		leelaMoveStats = supportsOption("VerboseMoveStats");
		if (leelaMoveStats && !supportsOption("LogLiveStats"))
			parseOption("name LogLiveStats type check default true");

        //  set options
		setOptions(false);
		//useWDL(true);	//	use WDL, if available (or maybe not?)

        setMode(PAUSED);
		return false;
	}

	public void setOptions(boolean dirtyOnly) throws IOException
	{
		//  all other options can be modified without restart
		String[] options = getOptions(dirtyOnly);
		for (int i=0; i<options.length; i += 2)
			if(options[i]!=null && !isReadOnly(options[i]))
				setOption(options[i],options[i+1]);

		//  wait for options to be acknowledged
		if (!waitFor("isready","readyok",10000))
			throw new IOException("engine does not respond");
	}

	public boolean restartRequired()
	{
		/** modifications to MultiPV take only effect
		 *  after a restart. at least with the engines I know.
		 */
		return isOptionDirty(config,"MultiPV");
	}

	public void init(Position pos, String osName) throws IOException
	{
		options.clear();
		super.init(pos, osName);
		ignoreMoves = 0;
		frcMode = false;
	}

	protected boolean waitFor(String command, String ackCommand, long millis)
	{
        waitCommand = ackCommand;
        waitThread = Thread.currentThread();

		printOut.println(command);
		//  wait for acknowledge

		try {
			Thread.sleep(millis);
            //  no response !
            return false;
		}
		catch (InterruptedException intex) {
            //  woken up by engine
            return true;
        }
        finally {
            waitCommand = null;
            waitThread = null;
        }
	}

    public void setOption(String name, String value)
    {
        printOut.print("setoption name ");
        printOut.print(name);
        if (value != null) {
            printOut.print(" value ");
            printOut.print(value);
        }
        printOut.println();
    }

	public void setDefaultOption(Option option)
	{
		setOption(option.name,option.defaultValue);
	}

    public String getEngineDisplayName() {
        return engineName;
    }

    public void close()
	{
		if (printOut!=null) printOut.println("quit");
		super.close();
	}

    public boolean canPonder() {
        return getOptionValue("Ponder").equalsIgnoreCase("true");
    }

    public boolean isActivelyPondering() {
        return isPondering() && (ponderMove!=null);
    }

    public void newGame() {
		// nothing to do
		pause();
	}

	protected void setMode(int newMode)
	{
		if (hasAnalyseOption) {
			if (mode==ANALYZING && newMode!=ANALYZING)
				printOut.println("setoption UCI_AnalyseMode false");
			else if (mode!=ANALYZING && newMode==ANALYZING)
				printOut.println("setoption UCI_AnalyseMode true");
		}

		super.setMode(newMode);
	}

	public void userMove(Move mv, boolean go)
	{
        synchronized (this)
        {
            if (mv.equals(ponderMove))
            {
                //  ponder hit
                ponderMove = null;      //  we are no more actively pondering
                printOut.println("ponderhit");
                setMode(THINKING);
            }
            if (go && !isThinking()) go();
        }
    }

	private void setPosition(Position pos)
	{
		currentLine.setLength(0);
		currentLine.append("position");

		if (PREFER_MOVES) {
			String start = pos.getStartFEN(Board.FEN_CLASSIC);
			if (start==null || start.equals(Position.START_POSITION))
				currentLine.append(" startpos");
			else {
				currentLine.append(" fen ");
				//  FRC:
				/** when in FRC mode, Shredder accepts ONLY FRC Fens, even for pseudo-classical positions ;-( */
				enableFRC(!pos.isClassic());
				if (frcMode)					//  frc-ify
					start = pos.getStartFEN(Board.SHREDDER_FEN);

				currentLine.append(start);
			}

			Move[] moves = pos.getMoves(0,pos.ply());
			if (moves.length > 0)
				currentLine.append(" moves");
			for (int i=0; i<moves.length; i++)
			{
				currentLine.append(" ");
				if (moves[i].isCastling())  {
					if (moves[i].isFRCCastling() && !frcMode) {
						//  trouble. can't insert FRC castling move - use plain "fen" instead
						currentLine.setLength(0);
						currentLine.append("position fen ");
						currentLine.append(pos.toString(Board.FEN_CLASSIC));
						break;
					}
					//  FRC
					//  encoding for castling should be backward compatible,
					//  i.e. use G1, C1 unless ambiguous
					//  Arena standard: use O-O O-O-O
					//  all UCI engines I tested against, support e1g1

					currentLine.append(EngUtil.square2String(moves[i].from));
					switch (moves[i].castlingMask())
					{
					case WHITE_KINGS_CASTLING:
						if (moves[i].from==F1)
							currentLine.append(EngUtil.square2String(moves[i].to));
						else
							currentLine.append("g1");   //  use G1 if unambiguous
						break;
					case WHITE_QUEENS_CASTLING:
						if (moves[i].from==D1||moves[i].from==B1)
							currentLine.append(EngUtil.square2String(moves[i].to));
						else
							currentLine.append("c1");   //  use C1 if unambiguous
						break;
					case BLACK_KINGS_CASTLING:
						if (moves[i].from==F8)
							currentLine.append(EngUtil.square2String(moves[i].to));
						else
							currentLine.append("g8");   //  use G8 if unambiguous
						break;
					case BLACK_QUEENS_CASTLING:
						if (moves[i].from==D8||moves[i].from==B8)
							currentLine.append(EngUtil.square2String(moves[i].to));
						else
							currentLine.append("c8");   //  use G1 if unambiguous
						break;
					}
				}
				else {
					currentLine.append(EngUtil.square2String(moves[i].from));
					currentLine.append(EngUtil.square2String(moves[i].to));
				}
				if (moves[i].isPromotion())
					currentLine.append(EngUtil.pieceCharacter(moves[i].getPromotionPiece()));
			}
		}
		else {
			enableFRC(!pos.isClassic());
			currentLine.append(" fen ");
			currentLine.append(pos.toString(frcMode ? Board.SHREDDER_FEN:Board.FEN_CLASSIC));
		}

		enginePosition.setup(pos);
		enginePosition.setFirstPly(pos.gamePly());

		legalMoveCount = enginePosition.countLegalMoves(true);
		printOut.println(currentLine.toString());
	}

	private void setCurrentPosition(String ponderMove1, String ponderMove2)
	{
		if (currentLine.indexOf("moves") < 0)
			currentLine.append(" moves");
		if (!ponderMove1.startsWith(" "))
			currentLine.append(" ");
		currentLine.append(ponderMove1);
		currentLine.append(' ');
		currentLine.append(ponderMove2);
		//  note that ponder moves came from the engine.
		//  we don't have to think about illegal FRC castlings, here

		printOut.println(currentLine.toString());
	}

	public void analyze(Position pos)
	{
		if (!canAnalyze(pos)) throw new IllegalArgumentException("game already finished");
		/**
		 * engines can usually handle illegal positions but they will not respond with "bestmove"
		 * ignoreMoves would get out of synch
		 */

		if (isAnalyzing())
			printOut.println("stop");       //  in analayze mode, ignoreMoves++ is called early
		else if (isThinking() || isActivelyPondering()) {
			printOut.println("stop");
			ignoreMoves++;
		}

		setPosition(pos);

		printOut.println("go infinite");
		ignoreMoves++;
        setMode(ANALYZING);
		/**
		 * some engines (ruffian) have the habit of responding immediately with "bestmove"
		 * if there is only one move available.
		 *
		 * that's why we increment ignoreMoves early. analysis mode must be explicitly finished with "stop"
		 */
	}

	public void analyze(Position pos, Move userMove)
	{
		analyze(pos);
	}

	public void go()
	{
		if (isThinking()) {
			//  this is rather moveNow()
			printOut.println("stop");
			ignoreMoves++;
			return;
		}
		else if (isAnalyzing()) {
			printOut.println("stop");       //  in analayze mode, ignoreMoves++ is called early
		}
		else if (isActivelyPondering()) {
			printOut.println("stop");
			ignoreMoves++;
		}

		setPosition(applPosition);

        printOut.print("go");
        printTimeCtrl();
        synchronized (this) {
            printOut.println();
            setMode(THINKING);
        }
    }

    private void printTimeCtrl()
    {
        Clock cl = Application.theApplication.theClock;
        TimeControl tc = Application.theUserProfile.getTimeControl();
        int phase = tc.getPhaseFor(applPosition.gameMove());
        int movestogo = tc.movesToGo(applPosition.gameMove());

        printOut.print(" wtime ");
        printOut.print(Math.max(cl.getWhiteTime(),0));
        printOut.print(" btime ");
        printOut.print(Math.max(cl.getBlackTime(),0));

	    long inc = tc.getIncrementMillis(phase);
	    if (inc > 0) {
			printOut.print(" winc ");
			printOut.print(inc);
			printOut.print(" binc ");
			printOut.print(inc);
	    }

        if (movestogo > 0) {
            printOut.print(" movestogo ");
            printOut.print(movestogo);
        }
    }

	public void moveNow()
	{
		if (!isThinking() && !isAnalyzing())
			throw new IllegalStateException("unexpected mode "+mode);

		printOut.println("stop");
	}

	public void pause() {
		if (isAnalyzing()) {
			printOut.println("stop");       //  in analayze mode, ignoreMoves++ is called early
		}
		else if (isThinking() || isActivelyPondering()) {
			printOut.println("stop");
			ignoreMoves++;
		}
        setMode(PAUSED);
	}

	public void getHint()
    {
		if (hint!=null)
            sendMessage(PLUGIN_REQUESTED_HINT, hint);
	}

	public void setTimeControls(int moves, long millis, long increment) {
		/* noop
            time settings will be set explicitly in GO
        */
	}

	public void setTime(long millis)
    {
        /* noop
            time settings will be set explicitly in GO
        */
	}

    protected void engineMoves(String moveText)
    {
        Move mv = parseMove(moveText,0);
		ponderMove = null;  //  we are no more actively pondering

	    if (isPondering()) {
		    //  pondering was interrupted; engines responds but we ignore
		    /** what shall we do with the move ?    */
		    return;
	    }

        if (!isThinking() && !isAnalyzing())
	        throw new IllegalStateException("unexpected mode "+mode);

        if (mv == null) {
	        sendMessage(PLUGIN_ERROR,"illegal move from engine: "+moveText);
	        return;
        }

	    if (canPonder())
	    {
		    //  start pondering
		    int ponderOffset = moveText.indexOf("ponder");
		    if (ponderOffset > 0)
		    {
			    String bestMoveText = moveText.substring(1,ponderOffset-1);
			    String ponderMoveText = moveText.substring(ponderOffset+7);

			    Move bestMove = parseMove(bestMoveText,0);
			    ponderMove = parseMove(ponderMoveText,0);

			    //  keep enginePosition in synch
			    enginePosition.tryMove(bestMove);   //  FRC castling ?
			    hint = new FormattedMove(ponderMove,false,null);  //  format hint
			    enginePosition.tryMove(ponderMove);

			    /** check for mate and stalemate here
			     */
			    if (enginePosition.isGameFinished()) {
				    /**  do not start pondering in an illegal position
				     *  (because the engine will not respond an ignoreMoves gets out of synch !!)
				     */
				    ponderMove = null;
				    setMode(PAUSED);
			    }
				else {
				    setCurrentPosition(bestMoveText,ponderMoveText);

			        printOut.print("go ponder");
			        printTimeCtrl();
			        printOut.println();
				    setMode(PONDERING);
			    }
			    //  (unrequested) hint
			    if (hint!=null)
				    analysis.ponderMove = hint.toString();
			    else
				    analysis.ponderMove = null;
			    sendMessage(PLUGIN_HINT, hint);
		    }
		    else {
			    hint = null;
		        ponderMove = null;
			    setMode(PAUSED);
		    }

	    }
	    else
	        setMode(PAUSED);

        sendMessage(PLUGIN_MOVE,
                new EvaluatedMove(mv, analysis));
        /**	note that MoveParser holds a fixed pool of move objects
         * 	got to clone the result
         */
    }

    protected Move parseMove(String text, int i)
    {
        int len = text.length();
        while (i < len && Character.isWhitespace(text.charAt(i))) i++;

	    if (text.startsWith("0000",i))
	        return Move.NULLMOVE;       //  UCI-2

	    if (text.equals("O-O") || text.equals("0-0"))
	    {
		    int king =  enginePosition.kingSquare(enginePosition.movesNext());
		    return new Move(king, king+2);
	    }
	    if (text.equals("O-O-O") || text.equals("0-0-0"))
	    {
		    int king =  enginePosition.kingSquare(enginePosition.movesNext());
		    return new Move(king, king-2);
	    }

        int sqfrom = EngUtil.char2Square(text.charAt(i),text.charAt(i+1));
        int sqto = EngUtil.char2Square(text.charAt(i+2),text.charAt(i+3));

        Move mv = new Move(sqfrom,sqto);    //  what about FRC castling ? we accept the usual gestures + O-O
        if (i+4 < len) {
            int promo = EngUtil.uncolored(EngUtil.char2Piece(text.charAt(i+4)));
            if (promo > 0)
                mv.setPromotionPiece(promo);
        }
        return mv;
    }

	public void readLine(char[] chars, int offset, int len)
	{
        String s = String.valueOf(chars,offset,len);
//System.err.println(s);
		if (s.startsWith("id name"))
			engineName = StringUtil.rest(s,2);
		if (s.startsWith("id author"))
			engineAuthor = StringUtil.rest(s,2);

		if (s.startsWith("bestmove")) {
			if (ignoreMoves == 0)
                engineMoves(StringUtil.rest(s));
			else
				ignoreMoves--;
		}

		if (s.startsWith("option") && readOptions)
			parseOption(s);

        if (s.startsWith("info")) {
			if (ignoreMoves==1) {
				parseAnalysis(StringUtil.rest(s), analysis);
				sendMessage(mode, analysis);
			}
        }

		if (s.startsWith("register")) {
			//  TODO display registration dialog and respond
			//  with "register later"
			//  or  "register name ... code ..."
		}

		if (s.startsWith("copyprotection checking")) {
			//  TODO wait until engine has finished copyprotection checking
			//  engine responds "copyprotection ok" or "copyprotection error"
		}

		if (s.startsWith("registration checking")) {
			//  TODO wait until engine has finished copyprotection checking
			//  engine responds "registration ok" or "registration error"
		}


        if ((waitCommand!=null) && s.startsWith(waitCommand))
            waitThread.interrupt();
	}


	public int getParseCapabilities()
	{
		return 	AnalysisRecord.CURRENT_MOVE+
		        AnalysisRecord.CURRENT_MOVE_NO+
		        AnalysisRecord.DEPTH +
		        AnalysisRecord.SELECTIVE_DEPTH +
		        AnalysisRecord.ELAPSED_TIME +
		        AnalysisRecord.NODE_COUNT +
		        AnalysisRecord.NODES_PER_SECOND +
				AnalysisRecord.EVAL +
		        AnalysisRecord.INFO +
				1;  //  1 PV is available (at least)
	}

	private static int mateScore(boolean isCurrent, int plies)
	{
		if (isCurrent)
			return Score.WHITE_MATES+plies;
		else
			return Score.BLACK_MATES-plies;
	}

	public int getEvaluationPointOfView()
	{
		//  as defined by the UCI protocol
		return POINT_OF_VIEW_CURRENT;
	}

	public int getMaxPVLines()
	{
		String multipv = getOptionValue(config,"MultiPV","1");
		return Integer.parseInt(multipv);
	}

	public void parseAnalysis(String input, AnalysisRecord rec)
	{
		if (input==null || input.length()==0) {
			rec.clear();
			return;
		}

		rec.engineMode = mode;
		rec.ply = enginePosition.gamePly();
		rec.white_next = enginePosition.whiteMovesNext();

		if (Util.allOf(AnalysisRecord.NEW_MOVE,rec.modified)) {
			rec.reset();
			rec.modified = AnalysisRecord.CURRENT_MOVE +
					AnalysisRecord.CURRENT_MOVE_NO +
					AnalysisRecord.ELAPSED_TIME +
					AnalysisRecord.NODE_COUNT +
					AnalysisRecord.NODES_PER_SECOND +
					AnalysisRecord.DEPTH +
					AnalysisRecord.SELECTIVE_DEPTH +
					AnalysisRecord.EVAL;
		}
		else
			rec.modified = 0;

		int pvidx = 0;  // 0 = main line, might differ for multi-pv
		Score score = new Score();
		boolean score_modified=false;
		StringTokenizer tok = new StringTokenizer(input);

		if (rec.info!=null &&
		   (rec.info_ttl!=0L) && (System.currentTimeMillis() > rec.info_ttl))
		{
			//  clear info
			rec.info = null;
			rec.modified |= AnalysisRecord.INFO;
		}

		boolean ispv = false;
		int engply = enginePosition.ply();
		while (tok.hasMoreTokens())
		{
			boolean tokispv = false;
			String t = tok.nextToken();

			if (t.equals("depth")) {
				rec.depth = StringUtil.parseInt(tok.nextToken());
				rec.modified |= AnalysisRecord.DEPTH;
			}
			else if (t.equals("seldepth")) {
				rec.selectiveDepth = StringUtil.parseInt(tok.nextToken());
				rec.modified |= AnalysisRecord.SELECTIVE_DEPTH;
			}
			if (t.equals("time")) {
				rec.elapsedTime = StringUtil.parseLong(tok.nextToken());
				rec.modified |= AnalysisRecord.ELAPSED_TIME;
				/** note that we have our own mechanism to calculate elapsed time   */
			}
			else if (t.equals("nodes")) {
				rec.nodes = StringUtil.parseLong(tok.nextToken());
				rec.modified |= AnalysisRecord.NODE_COUNT;
			}
			else if (t.equals("pv")) {
				StringBuffer line = rec.getLine(pvidx);
				line.setLength(0);
				if (rec.moves[pvidx]!=null)
					rec.moves[pvidx].clear();
				ispv = tokispv = true;                
/*              //  don't print ponder move !
				if (ponderMove!=null) {
					String formatted = printMove(ponderMove, true);
					if (formatted!=null)
						appendMove(rec,formatted,pvidx);
					else
						appendMove(rec,ponderMove.toString(),pvidx);
				}
*/
			}
			else if (t.equals("refutation")) {
				//  TODO parse refutation line (low prio)
				//  currently, UCI_ShowRefutations is always disabled
			}
			else if (t.equals("currline")) {
				//  TODO parse current line (low prio)
				//  currently, UCI_ShowCurrLine is always disabled
			}
			else if (t.equals("multipv")) {
				pvidx = StringUtil.parseInt(tok.nextToken()) - 1;    //  multi pv line
			}
			else if (t.equals("score"))
			{
				score.flags = Score.EVAL_EXACT;

				t = tok.nextToken();
				if (t.equals("cp")) {
					int cp = StringUtil.parseInt(tok.nextToken());
					int abscp = Math.abs(cp);
					if (abscp > 29000) {
						/** unfortunately, UCI engines are not consistent in reporting mates
						 *  we accept some variants:
						 *
						 * the return value is always 30.000 + x
						 */
						if (abscp < 30000)
							score.cp = mateScore(cp>0, 29999-abscp);   // 30.000 - x
						else if (abscp < 31000)
							score.cp = mateScore(cp>0, abscp-30000);   // 30.000 + x
//						else if (abscp < 32700)
//							rec.eval[pvidx] = mateScore(cp>0, 32700-abscp);   //  32.700 - x
//						else if (abscp < 32734)
//							rec.eval[pvidx] = mateScore(cp>0, abscp-32700);   //  32.700 + x
						else
							score.cp = mateScore(cp>0, 32767-abscp);   //  32.767 - x
					}
					else
						score.cp = cp;
				}
				else if (t.equals("mate")) {
					int moves = StringUtil.parseInt(tok.nextToken());
					if (moves > 0)
						score.cp = Score.WHITE_MATES + 2*moves;
					else
						score.cp = Score.BLACK_MATES + 2*moves;
				}

				score_modified = true;
			}
			else if (t.equals("lowerbound"))
				score.flags = enginePosition.whiteMovesNext() ? Score.EVAL_LOWER_BOUND:Score.EVAL_UPPER_BOUND;
			else if (t.equals("upperbound"))
				score.flags = enginePosition.whiteMovesNext() ? Score.EVAL_UPPER_BOUND:Score.EVAL_LOWER_BOUND;
			else if (t.equals("wdl")) {
				score.win = StringUtil.parseInt(tok.nextToken());
				score.draw = StringUtil.parseInt(tok.nextToken());
				score.lose = StringUtil.parseInt(tok.nextToken());
				score_modified = true;
			}
			else if (t.equals("movesleft")) {
				int moves_left = StringUtil.parseInt(tok.nextToken());
				//	ignore for now
			}
			else if (t.equals("currmove"))
			{
				Move mv = parseMove(t = tok.nextToken(),0);       //  parse & format move
				String formatted = StringMoveFormatter.formatMove(enginePosition,mv,false);
				if (formatted != null)
					t = formatted;

				rec.currentMove = t;
				rec.modified |= AnalysisRecord.CURRENT_MOVE;
			}
			else if (t.equals("currmovenumber")) {
				rec.currentMoveNo = StringUtil.parseInt(tok.nextToken());
				rec.modified |= AnalysisRecord.CURRENT_MOVE_NO;
			}
			else if (t.equals("hashfull"))
				Double.parseDouble(tok.nextToken()); //  TODO hash full (per mille); low priority..
			else if (t.equals("nps")) {
				rec.nodesPerSecond = Long.parseLong(tok.nextToken());
				rec.modified |= AnalysisRecord.NODES_PER_SECOND;
			}
			else if (t.equals("tbhits"))
				StringUtil.parseInt(tok.nextToken());     //  TODO table base hits; low priority..
			else if (t.equals("cpuload"))
				StringUtil.parseInt(tok.nextToken());     //  TODO CPU load (per mille); low priority. there are not likely many engines that can report this
			else if (t.equals("string")) {
				if (leelaMoveStats) {
					String info = input.substring(14);
					info = LeelaMoveStats.reformat(info);
					t = tok.nextToken();
					if (t.equals("node")) {
						//	summary
						rec.info = info;
						rec.info_ttl = System.currentTimeMillis()+5000;
						rec.modified |= AnalysisRecord.INFO;
					}
					else {
						Move mv = parseMove(t,0);
						rec.addMoveInfo(mv,info);
					}
				}
				else {
					rec.info = input;
					rec.info_ttl = System.currentTimeMillis()+5000;
					rec.modified |= AnalysisRecord.INFO;
				}
			}
			else if (ispv) {
				Move mv = parseMove(t,0);       //  parse & format move

				String formatted = printMove(mv, rec.getLine(pvidx).length()==0);
				if (formatted==null) formatted = t;
				//	if formatting fails, fall back to engine input
				appendMove(rec,mv,formatted,pvidx);
				//  TODO handle refutation lines and current lines (low prio)
				tokispv = true;
			}
			//  else: what ? unrecgonized info
			if (!tokispv) ispv = false;
		}

		if (score_modified) {
			adjustPointOfView(score,rec.white_next);
			rec.eval[pvidx] = score;
			rec.setPvModified(pvidx);
		}

		if (Util.noneOf(rec.modified, AnalysisRecord.ELAPSED_TIME))
		{
			//  use our own timekeeping
			rec.elapsedTime = getElapsedTime();
			rec.modified |= AnalysisRecord.ELAPSED_TIME;
		}

		if (Util.anyOf(rec.modified,AnalysisRecord.NODE_COUNT) &&
		    Util.noneOf(rec.modified,AnalysisRecord.NODES_PER_SECOND))
		{
			//  use our own timekeeping for calculating Nodes per second
			rec.nodesPerSecond = Math.round(((double)rec.nodes*1000) / (double)getElapsedTime());
			rec.modified |= AnalysisRecord.NODES_PER_SECOND;
		}

		while (enginePosition.ply() > engply)
			enginePosition.undoMove();

	}

	protected String printMove(Move mv, boolean showCount)
	{
		if (enginePosition.whiteMovesNext()) showCount = true;
		String formatted = StringMoveFormatter.formatMove(enginePosition,mv, showCount);
		if (formatted!=null) {
			enginePosition.tryMove(mv);
		}
		else {
			/**
			 *  else: formatting failed. position out of synch ?
			 *  this can happen if we are in THINKING mode but read an
			 *  old pondering line. It's not really a problem...
			 */
/*
			System.out.println("failed to format: "+t);
			System.out.println(enginePosition.toString());
			System.out.println(rec.getLine(pvidx));
			enginePosition.printBoard();
*/
		}
		return formatted;
	}

	protected void appendMove(AnalysisRecord rec, Move mv, String text, int pvidx)
	{
		StringBuffer line = rec.getLine(pvidx);
		line.append(text);
		line.append(" ");
		rec.getMoves(pvidx).add(mv);
		// todo also keep track of index into string; make it into an object
		/*
		{
			Move mv;
			Score score;
			int text_offset
		}
		 */
		rec.setPvModified(pvidx);
	}

	protected void parseOption(String s)
	{
		StringTokenizer tok = new StringTokenizer(s);
		tok.nextToken();    //  "option"

		Option option = new Option();

		StringBuffer name=null, defaultValue=null, value=null, minValue=null, maxValue=null;
		StringBuffer current=null;

		while (tok.hasMoreTokens())
		{
			String t = tok.nextToken();
			if (t.equals("type"))
			{
				t = tok.nextToken();
				if (t.equals("check"))
					option.type = CHECKBOX;
				else if (t.equals("spin"))
					option.type = SPIN;
				else if (t.equals("combo"))
					option.type = COMBO;
				else if (t.equals("button"))
					option.type = BUTTON;
				else if (t.equals("string"))
					option.type = STRING;
				//  else: unknown token
				current = null;
			}
			else if (t.equals("name"))
				current = name = new StringBuffer();
			else if (t.equals("default"))
				current = defaultValue = new StringBuffer();
			else if (t.equals("min"))
				current = minValue = new StringBuffer();
			else if (t.equals("max"))
				current = maxValue = new StringBuffer();
			else if (t.equals("var"))
			{
				if (value != null) {
					if (option.values==null) option.values = new Vector<String>();
					option.values.add(value.toString());
				}
				current = value = new StringBuffer();
			}
			else if ((current != null) && ! "<empty>".equals(t))
			{
				//  append to current value
				if (current.length() > 0) current.append(" ");
				current.append(t);
			}
		}

		if (name!=null) option.name = name.toString();
		if (defaultValue!=null) option.defaultValue = defaultValue.toString();
		if (minValue!=null) option.minValue = StringUtil.parseInt(minValue.toString());
		if (maxValue!=null) option.maxValue = StringUtil.parseInt(maxValue.toString());
		if (value!=null) {
			if (option.values==null) option.values = new Vector<String>();
			option.values.add(value.toString());
		}
		
		options.add(option);
	}

	public void setDebug(boolean on)
	{
		//  UCI-2
		printOut.print("debug ");
		if (on)
			printOut.println("on");
		else
			printOut.println("off");
	}



	public static Option getOption(List<Option> optionList, String key)
	{
		for (int i=0; i < optionList.size(); i++)
		{
			Option option = optionList.get(i);
			if (option.name.equalsIgnoreCase(key)) return option;
		}
		return null;
	}


	@Override
	protected String prepareCentipawnScore(Score score, HashMap pmap)
	{
		String scoreType = getOptionValue(config,"ScoreType");
		double perc = (double)score.cp/100.0;

		if (scoreType!=null && scoreType.equals("win_percentage"))
		{
			if (perc < 0.0) perc = 100.0+perc;
			//	note that negative percentages do not come from the engine.
			//	they are an artifact of EnginePlugin.adjustPointOfView()
			if (perc < 0.0) perc = 0.0;
			if (perc > 100.0) perc = 100.0;

			pmap.put("eval",PERCENTAGE_FORMAT.format(perc));
			return "plugin.percentage";
		}
		if (scoreType!=null && (scoreType.equals("Q") || scoreType.equals("W-L")))
		{
			pmap.put("eval",PERCENTAGE_FORMAT.format(perc));
			return "plugin.percentage";
		}
		//	else
		return super.prepareCentipawnScore(score, pmap);
	}

	@Override
	public double mapUnit(Score score)
	{
		String scoreType = getOptionValue(config,"ScoreType");
		double perc = (double)score.cp/100.0;

		if (scoreType!=null && scoreType.equals("win_percentage"))
		{
			if (perc < 0.0) perc = 100.0+perc;
			//	note that negative percentages do not come from the engine.
			//	they are an artifact of EnginePlugin.adjustPointOfView()
			if (perc < 0.0) perc = 0.0;
			if (perc > 100.0) perc = 100.0;

			return perc/100.0;
		}
		if (scoreType!=null && (scoreType.equals("Q") || scoreType.equals("W-L")))
		{
			return mapUnit(score.cp,-100,+100);
		}
		//	else

		return super.mapUnit(score);
	}
}
