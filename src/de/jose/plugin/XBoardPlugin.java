/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.plugin;

import de.jose.chess.*;
import de.jose.pgn.PgnUtil;
import de.jose.util.StringUtil;
import de.jose.Util;

import java.io.IOException;
import java.util.HashMap;

import static de.jose.plugin.EngineState.*;

public class XBoardPlugin
		extends EnginePlugin
{
	/**	is randomizing on ?	 */
	protected boolean random;
	/**	is permanent brain (pondering) on ?	 */
	protected boolean permanentBrain;
	/**	print diagnostics during thinking ?	 */
	protected boolean diagnostics;
	/**	map of features (as returned by "protover")	 */
	protected HashMap features;

    public XBoardPlugin()
	{
		random = true;
		permanentBrain = true;
		diagnostics = true;
        features = new HashMap();
	}

	/**
	 * unfortunately, the XBoard does not specify this point of
	 * view.
	 */
	public int getEvaluationPointOfView()
	{
		return POINT_OF_VIEW_WHITE; //  is this right for all XBoard engines ?
	}

	public String getEngineDisplayName() {
        return getFeature("myname");
    }


    /**	set up a new XBoard plugin	 */
	public boolean open(String osName)
		throws IOException
	{
		//	put into xboard mode
		printOut.println("xboard");
		//	startup options
        String[] startup = StringUtil.separateLines(getStartup(osName));
		if (startup!=null)
			for (int i=0; i < startup.length; i++)
				printOut.println(startup[i]);

		//	check for features
		//	(some engines report "feature done=0" to indicate that they understand the feature command)
		//	features done=1 indicates the end of the feature list
		String presetFeatures = getValue(osName,"FEATURES");
		if (presetFeatures!=null)
			readFeatures(presetFeatures);
//		else if (! "1".equals(getFeature("done"))) {
		printOut.println("protover 2");
		//	request XBoard 2 protocol features
		//	the engine will respond with a series of "feature" lines

		setOptions(false);

		//	start a new game
		newGame();
		return true;
	}

	public void setOptions(boolean dirtyOnly)
	{
		//  dirtyOnly is currently ignored
		//  (changing these options in midgame should be not a problem, anyway)
		boolean ponder = Util.toboolean(getOptionValue("Ponder"));
		boolean random = Util.toboolean(getOptionValue("random"));

		setPermanentBrain(ponder);
		setRandom(random);                //  TODO make configurable
	}

	public boolean restartRequired()
	{
		//  modifications to Ponder and Random can be made anytime.
		//  no need to restart the engine.
		return false;
	}

	/**	close it	 */
	public void close()
	{
		try {
			printOut.println("quit");
		} catch (Exception e) {
			//	don't report error on close
		}

		super.close();
	}

    public boolean canPonder() {
        return permanentBrain;
    }

    public boolean isActivelyPondering() {
        return isPondering() && canPonder();
    }

    public String getFeature(String key)
	{
		return (String)features.get(key);
	}

    public boolean canFeature(String key)
    {
        String value = getFeature(key);
        return (value!=null) && value.equals("1");
    }

	public boolean canOfferDraw()
	{
		return canFeature("draw");
	}

    public boolean canAcceptDraw()
    {
        return true;
    }

	public boolean canResign()
	{
		return true;
	}

	/** FRC */
	public boolean supportsFRC()
	{
		String variants = getFeature("variants");
		return variants!=null && variants.indexOf("fischerandom")>=0;
	}
	/** FRC */
	public void enableFRC(boolean on)
	{
		if (on) printOut.println("variant fischerandom");
	}

	/**	set random mode	 */
	public void setRandom(boolean on)
	{
		if (isOpen()) {
			if (on != random)
				printOut.println("random");		//	toggle random mode
		}
		random = on;
	}

	public void setPermanentBrain(boolean on)
	{
		if (isOpen()) {
			if (on)
				printOut.println("hard");
			else
				printOut.println("easy");
		}
		permanentBrain = on;
	}

	public void setDiagnostics(boolean on)
	{
		if (isOpen()) {
			if (on)
				printOut.println("post");
			else
				printOut.println("nopost");
		}
		diagnostics = on;
	}


	/**	set the time controls	 */
	public void setTimeControls(int moves, long millis, long increment)
	{
		printOut.print("level ");
		printOut.print(moves);
		printOut.print(" ");

		printOut.print(millis / TimeControl.MINUTE);		//	minutes
		millis %= TimeControl.MINUTE;

		long seconds = millis / TimeControl.SECOND;			//	seconds
		if (seconds != 0) {
			printOut.print(":");
			printOut.print(seconds);
		}

		printOut.print(" ");
		printOut.print(increment / TimeControl.SECOND);
		printOut.println();
	}

	/**	set the plugin's time	 */
	public void setTime(long millis)
	{
		if (canFeature("time"))
			printOut.println("time "+(millis/10));	//	engine calculates in 1/100 seconds
	}

	/**	setup a new game	 */
	public void newGame()
	{
		if (!isOpen()) { brokenPipe(); return; }

		printOut.println("new");
		printOut.println("force");
		if (random) printOut.println("random");
		setPermanentBrain(permanentBrain);
		/*	we don't make assumptions about the pb default value	*/
		setDiagnostics(diagnostics);

		setMode(PAUSED);	//	= waiting for human move
	}

	/**	pause thinking	 */
	public void pause()
	{
		if (!isOpen()) {
			setMode(PAUSED);
			brokenPipe();
			return;
		}
		//  what shall we do ? throwing an exception is too harsh

		if (canFeature("pause"))
			printOut.println("pause");
		else
			printOut.println("new");	//	stops pondering

		setMode(PAUSED);
	}

	protected void setMode(EngineState newMode)
	{
		super.setMode(newMode);
		userOfferedDraw = false;
		engineOfferedDraw = false;
	}

	public int getParseCapabilities()
	{
		return 	AnalysisRecord.DEPTH +
				AnalysisRecord.EVAL +
				AnalysisRecord.ELAPSED_TIME +
				AnalysisRecord.NODE_COUNT +
		        AnalysisRecord.NODES_PER_SECOND +
		        AnalysisRecord.INFO +
				1;  //  we can show exactly one PV
	}


	public void parseAnalysis(String input, AnalysisRecord rec)
	{
/* GnuChess sample output:
 2.     14    0       38   d1d2  e8e7
 3+     78    0       65   d1d2  e8e7  d2d3
 3&     14    0       89   d1d2  e8e7  d2d3
 3&     76    0      191   d1e2  e8e7  e2e3
 3.     76    0      215   d1e2  e8e7  e2e3
 4&     15    0      366   d1e2  e8e7  e2e3  e7e6
 4.     15    0      515   d1e2  e8e7  e2e3  e7e6
 5+     74    0      702   d1e2  f7f5  e2e3  e8e7  e3f4
 5&     71    0     1085   d1e2  e8e7  e2e3  e7e6  e3f4
 5.     71    0     1669   d1e2  e8e7  e2e3  e7e6  e3f4
 6&     48    0     3035   d1e2  e8e7  e2e3  e7e6  e3e4  f7f5  e4d4
 6.     48    0     3720   d1e2  e8e7  e2e3  e7e6  e3e4  f7f5  e4d4
 7&     48    0     6381   d1e2  e8e7  e2e3  e7e6  e3e4  f7f5  e4d4
 7.     48    0    10056   d1e2  e8e7  e2e3  e7e6  e3e4  f7f5  e4d4
 8&     66    1    20536   d1e2  e8e7  e2e3  e7e6  e3d4  g7g5  a2a4  f7f5
 8.     66    1    24387   d1e2  e8e7  e2e3  e7e6  e3d4  g7g5  a2a4  f7f5
 9&     62    2    38886   d1e2  e8e7  e2e3  e7e6  e3d4  h7h5  a2a4  h5h4
                           d4e4
 9.     62    4    72578   d1e2  e8e7  e2e3  e7e6  e3d4  h7h5  a2a4  h5h4
                           d4e4
10&     34    7   135944   d1e2  e8e7  e2e3  e7e6  e3d4  h7h5  c2c4  h5h4
                           d4e4  f7f5  e4f4
10.     34    9   173474   d1e2  e8e7  e2e3  e7e6  e3d4  h7h5  c2c4  h5h4
                           d4e4  f7f5  e4f4
ply
        eval
		    time (seconds)
*/
		if (input==null || input.length()==0) {
			rec.clear();
			return;
		}

		rec.engineMode = mode;
		rec.ply = enginePosition.gamePly();
		rec.white_next = enginePosition.whiteMovesNext();

		if (Util.allOf(AnalysisRecord.NEW_MOVE,rec.modified)) {
			rec.clear();
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

		char[] chars = input.toCharArray();

		int k1 = StringUtil.nextWordBreak(input,0);
		int k2 = StringUtil.nextWordBreak(input,k1+1);
		int k3 = StringUtil.nextWordBreak(input,k2+1);
		int k4 = StringUtil.nextWordBreak(input,k3+1);

		if (k1 > 0) {
			rec.depth = AnalysisRecord.parseInt(chars,0,k1);
			rec.modified |= AnalysisRecord.DEPTH;
		}
		rec.selectiveDepth = Score.UNKNOWN;
		Score score = rec.data[0].eval;

		if (k2 > k1 && k1 >= 0) {
			int eval = AnalysisRecord.parseInt(chars,k1,k2-k1);
			if (eval > 32000) {
				//	white mates in ... plies
				int plies = 32768-eval;
				score.cp = Score.WHITE_MATES+plies;
			}
			else if (eval < -32000) {
				int plies = -32768-eval;
				score.cp = Score.BLACK_MATES-plies;
			}
			else
				score.cp = eval;

			adjustPointOfView(score,rec.white_next);

			rec.modified |= AnalysisRecord.EVAL;
		}
		if (k3 > k2 && k2 >= 0) {
			rec.elapsedTime = AnalysisRecord.parseLong(chars,k2,k3-k2);
			rec.modified |= AnalysisRecord.ELAPSED_TIME;
		}
		else {
			rec.elapsedTime = getElapsedTime();
			rec.modified |= AnalysisRecord.ELAPSED_TIME;
		}
		if (k4 > k3 && k3 >= 0) {
			rec.nodes = AnalysisRecord.parseLong(chars,k3,k4-k3);
			rec.nodesPerSecond = Math.round(((double)rec.nodes*1000)/((double)rec.elapsedTime));
			rec.modified |= AnalysisRecord.NODE_COUNT | AnalysisRecord.NODES_PER_SECOND;
		}

		if (k4 >= 0) {
			StringMoveFormatter mvFormatter = StringMoveFormatter.getDefaultFormatter();
			mvFormatter.replaceDefaultPieceChars(chars,k4,chars.length-k4);
            //  TODO parse and format ?
			StringBuffer line = rec.getLine(0);
			line.setLength(0);
			line.append(chars,k4,chars.length-k4);
			rec.setPvModified(0);      //  first PV modified
		}

	}


	public void go()
	{
		if (!isOpen()) { brokenPipe(); return; }

		if (mode == PAUSED) synch();

		if (isAnalyzing())
			printOut.println("exit");	//	exit analysis mode

        synchronized (this) {
            printOut.println("go");
            setMode(THINKING);
        }
    }

	/**	stop thinking an return a move immediately	 */
	public void moveNow()
	{
		if (!isOpen()) { brokenPipe(); return; }

		switch (mode) {
		case THINKING:
			printOut.println("?");
			break;
		default:
			throw new IllegalStateException();
		}
	}

	public void engineMoves(String moveText)
	{
		if (mode != THINKING) {
			//	happens when the engine is shut down
			//	but still responds
//			throw new IllegalStateException(mode+" != THINKING");
			return;
		}

		Move mv = moveParser.parseMove(moveText);
		if (mv == null) {
			sendMessage(PLUGIN_ERROR, "illegal move from engine: "+moveText);
			return;
		}
		else
			enginePosition.tryMove(mv);

		sendMessage(PLUGIN_MOVE, new EvaluatedMove(mv,analysis,this));
		/**	note that MoveParser holds a fixed pool of move objects
		 * 	better clone the result
		 */
		if (canPonder())
			setMode(PONDERING);
		else
			setMode(PAUSED);
	}

	public void engineHint(String moveText)
	{
		Object message;
		Move mv = moveParser.parseMove(moveText);   //  TODO is this the correct position ???
		if (mv!=null)
			message = new FormattedMove(mv,false,moveText);
		else
			message = StringMoveFormatter.getDefaultFormatter().replaceDefaultPieceChars(moveText);
//			message = moveText;

		if (message!=null)
			analysis.ponderMove = message.toString();
		else
			analysis.ponderMove = null;

		if (userRequestedHint)
			sendMessage(PLUGIN_REQUESTED_HINT, message);
		else
			sendMessage(PLUGIN_HINT, message);

		userRequestedHint = false;
	}

	public void getHint()
	{
		printOut.println("hint");
		userRequestedHint = true;
		//	engine may respond with "Hint: "
	}

	public void kibitz(String message)
	{
		sendMessage(PLUGIN_COMMENT, message);
	}

	/**	offers a draw to the engine (if possible)	 */
	public void offerDrawToEngine()
	{
		userOfferedDraw = true;
		if (canOfferDraw())
			printOut.println("draw");
	}

	public boolean isBookEnabled()
	{
		//  TODO needs to be implemented by derived classes, if possible
		return true;
	}

	public void disableBook()
	{
		//  TODO needs to be implemented by derived classes, if possible
	}

	/**	engine offers draw to user	*/
	public void engineOffersDraw()
	{
		/* ... */
		if (userOfferedDraw)
			sendMessage(PLUGIN_ACCEPT_DRAW);
		else {
			engineOfferedDraw = true;
			sendMessage(PLUGIN_DRAW);
		}
	}
	/**	engine resigns to user	*/
	public void engineResigns()
	{
		/* ... */
		setMode(PAUSED);
		sendMessage(PLUGIN_RESIGNS);
	}

	/**	the engine wants to tell you something ?!?
	 */
	public void tellUser(String message)
	{
	}

	/**	the engine asks the user;
	 *	we will reply ...
	 */
	public void askUser(String tag, String message)
	{
	}

	/**	request a hint	 */
	public void requestHint()
	{
		printOut.println("hint");
	}

	/**	request a list of book moves	 */
	public void requestBook()
	{
		printOut.println("bk");
	}

	/**	notify the plugin of the game result	 */
	public void notifyResult(int result)
	{
		printOut.print("result ");
		printOut.println(PgnUtil.resultString(result));
	}

	/**	setup a new position	 */
	protected void setBoardFEN(String fen)
	{
		if (canFeature("setboard")) {
			printOut.print("setboard ");
			printOut.println(fen);
		}
		else
			throw new RuntimeException("setboard not supported");
	}

	protected void setBoard(Board board)
	{
		if (canFeature("setboard")) {
			if (supportsFRC()) {
				enableFRC (! board.isClassic());
				setBoardFEN(board.toString(Board.XFEN));
			}
			else
				setBoardFEN(board.toString(Board.FEN_CLASSIC));
		}
		else {
            if (!board.whiteMovesNext()) {
                printOut.println("new");		//	clears the board
                printOut.println("force");
                printOut.println("a2a3");   //  the only way to switch moving color ?!
            }

			//	the use of "edit" is discouraged as it lacks castling and ep status
			printOut.println("edit");
			printOut.println("#");		//	clears the board

			for (int file=Constants.FILE_A; file <= Constants.FILE_H; file++)
				for (int row=Constants.ROW_1; row <= Constants.ROW_8; row++)
				{
					int p = board.pieceAt(file,row);
					if (EngUtil.isWhite(p)) {
						printOut.print(EngUtil.pieceCharacter(EngUtil.uncolored(p)));
						printOut.print(EngUtil.fileChar(file));
						printOut.print(EngUtil.rowChar(row));
						printOut.println();
					}
				}

			printOut.println("c");		//	toggles color

			for (int file=Constants.FILE_A; file <= Constants.FILE_H; file++)
				for (int row=Constants.ROW_1; row <= Constants.ROW_8; row++)
				{
					int p = board.pieceAt(file,row);
					if (EngUtil.isBlack(p)) {
						printOut.print(EngUtil.pieceCharacter(EngUtil.uncolored(p)));
						printOut.print(EngUtil.fileChar(file));
						printOut.print(EngUtil.rowChar(row));
						printOut.println();
					}
				}

			printOut.println(".");
			//  no use enabling FRC, can't castle anyway
/*
			if (board.whiteMovesNext())
				printOut.println("white");
			else
				printOut.println("black");
*/
		}

	}

	protected void replay(Position pos)
	{
		printOut.println("new");
		printOut.println("force");
		for (int i=0; i < pos.ply(); i++) {
			Move mv = pos.move(i);
			printOut.print(EngUtil.square2String(mv.from));
			printOut.print(EngUtil.square2String(mv.to));
			if (mv.isPromotion())
				printOut.print(EngUtil.pieceCharacter(mv.getPromotionPiece()));
			printOut.println();
		}
		pos.ply();
	}

	protected void synch()
	{
/*		if (applPosition.getStartFEN()==null) {
			//	instead: replay the game
			replay(applPosition);
		}
		else
*/
		setBoard(applPosition);
		enginePosition.setup(applPosition);
		legalMoveCount = enginePosition.countLegalMoves(true);
	}

	/**
	 * called when the input stream from the engine is broken.
	 * What shall we do ?
	 */
	public void brokenPipe()
	{
//		throw new IllegalStateException();  //  this is a bit too harsh
		stdInThread.notifyError(new IOException("connection to engine is broken"));
	}

	/**	make a user move	 */
	public void userMove(Move mv, boolean go)
	{
		if (!isOpen()) { brokenPipe(); return; }

		switch (mode) {
		case PAUSED:	return;	/* out of synch: no need to record user move	*/
		case THINKING:	throw new IllegalStateException();		//	must not happen; must pause() first
		case ANALYZING:
		case PONDERING:	break;	/* OK	*/
		}


		if (mv.isFRCCastling()) {
			//  FRC this is the Arena standard for sending castling moves!
			switch (mv.castlingMask())
			{
			case Constants.WHITE_KINGS_CASTLING:
			case Constants.BLACK_KINGS_CASTLING:
				printOut.println("O-O");
				break;
			case Constants.WHITE_QUEENS_CASTLING:
			case Constants.BLACK_QUEENS_CASTLING:
				printOut.println("O-O-O");
				break;
			}
		}
		else {
			printOut.print(EngUtil.fileChar(EngUtil.fileOf(mv.from)));
			printOut.print(EngUtil.rowChar(EngUtil.rowOf(mv.from)));

			int dest_square = mv.to;
			switch (mv.castlingMask())
			{
			case Constants.WHITE_KINGS_CASTLING:
				dest_square = Constants.G1; break;
			case Constants.WHITE_QUEENS_CASTLING:
				dest_square = Constants.C1; break;
			case Constants.BLACK_KINGS_CASTLING:
				dest_square = Constants.G8; break;
			case Constants.BLACK_QUEENS_CASTLING:
				dest_square = Constants.C8; break;
			}

			printOut.print(EngUtil.fileChar(EngUtil.fileOf(dest_square)));
			printOut.print(EngUtil.rowChar(EngUtil.rowOf(dest_square)));
		}


		if (mv.isPromotion())
			printOut.print(EngUtil.pieceCharacter(mv.getPromotionPiece()));

        synchronized (this)
        {
            //  synchronize with input (so that states don't get confused
            printOut.println();

            /** do not modify mv, use a clone instead ! */
            enginePosition.tryMove(new Move(mv,enginePosition));

            if (isAnalyzing())
                ;
            else if (mv.isGameFinished(true))
                setMode(PAUSED);
            else
                setMode(THINKING);

            if (go && !isThinking()) go();  //  user overrides Draw3, for example
        }
    }

	public void analyze(Position pos)
	{
		//String newPos = pos.toString();
		if (isAnalyzing())
			if (pos.equals(enginePosition)) return;	//	already analyzing this position - noop

		setBoard(pos);
		if (!isAnalyzing()) {
			printOut.println("post");
			printOut.println("analyze");
		}

		enginePosition.setup(pos);
		legalMoveCount = enginePosition.countLegalMoves(true);
		setMode(ANALYZING);
	}

	public void analyze(Position pos, Move userMove)
	{
		if  (!isAnalyzing())
			analyze(pos);
		else
			userMove(userMove,false);
	}


	protected void readFeatures(String s)
	{
		int k1 = 0, k2,k3,k4;
		String key, value;

		while (k1<s.length())
		{
			k2 = s.indexOf("=",k1);
			key = StringUtil.trimNewline(s.substring(k1,k2)).toLowerCase();

			k3 = k2+1;
			while (k3 < s.length() && StringUtil.isWhitespaceOrNewline(s.charAt(k3)))
				k3++;

			if (k3 >= s.length()) {
				/*	empty value	*/
				features.put(key,null);
				k1 = k3;
				continue;
			}

			if (s.charAt(k3)=='"') {
				/*	quoted value	*/
				k4 = s.indexOf("\"",k3+1);
				if (k4 >= 0)
					value = s.substring(k3+1,k4);
				else {
					value = s.substring(k3);
					k4 = s.length();
				}
			}
			else {
				/*	unquoted value	*/
				k4 = StringUtil.indexOfWhitespace(s,k3+1);
				if (k4 >= 0)
					value = s.substring(k3,k4);
				else {
					value = s.substring(k3);
					k4 = s.length();
				}
			}

			features.put(key,value);
			k1 = k4+1;
		}
	}

	protected boolean filterOutput(String s)
	{
		/**	may be overwritten	*/
		return true;
	}

	//-------------------------------------------------------------------------------
	//	implements InputListener
	//-------------------------------------------------------------------------------

	/**	called when a line of input is received from the plugin
     * @param chars
     * @param offset
     * @param len     */
	public void readLine(char[] chars, int offset, int len)
	{
        String s = String.valueOf(chars,offset,len);

		if (!filterOutput(s))
			return;

		if (s.startsWith("feature")) {
			readFeatures(StringUtil.rest(s));
			return;
		}
		if (StringUtil.startsWithIgnoreCase(s,"Illegal move"))
		{
			sendMessage(PLUGIN_ERROR,s);
			return;
		}
		if (StringUtil.startsWithIgnoreCase(s,"Error"))
		{
			sendMessage(PLUGIN_FATAL_ERROR,s);
			return;
		}

		if (s.startsWith("move")) {
			engineMoves(StringUtil.rest(s));
			return;
		}

		if (s.startsWith("My move is:")) {
			engineMoves(StringUtil.rest(s,3));
			return;
		}

		if (s.startsWith("Hint")) {
			engineHint(StringUtil.rest(s));
			return;
		}

		if (s.startsWith("resign")) {
			engineResigns();
			return;
		}

		if (s.startsWith("offer draw")) {
			engineOffersDraw();
			return;
		}

		if (s.startsWith("tell") ||
			s.startsWith("kibitz")) {
			tellUser(StringUtil.rest(s));
			return;
		}

		if (s.startsWith("askuser")) {
			s = StringUtil.rest(s);
			int k1 = s.indexOf(" ");
			askUser(s.substring(0,k1), s.substring(k1+1));
			return;
		}

		switch (mode)
		{
		case THINKING:
		case PONDERING:
		case ANALYZING:
				parseAnalysis(s,analysis);
				msgSent++;
				sendMessage(mode.numval,analysis);
				break;
		}
	}

}
