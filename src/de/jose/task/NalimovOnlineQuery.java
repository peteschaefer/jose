/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch‰fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.task;

import de.jose.chess.Position;
import de.jose.chess.Move;
import de.jose.chess.EngUtil;
import de.jose.util.StringUtil;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;
import de.jose.Application;
import de.jose.Command;
import de.jose.task.db.GameUtil;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.AnalysisRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;

/**
 *
 */
public class NalimovOnlineQuery
        extends Task
{
	private static final String serverURL = "http://www.k4it.de/games/EGTBViewerprobe2.php?fen=%fen%";

    private Game game;
    private MoveNode node;
    private Position pos;

	private int value;
    private List moveList;

    public NalimovOnlineQuery(Game game, MoveNode node, Object pos)
    {
        super("NalimovOnline");

        this.game = game;
        this.node = node;
        this.pos = new Position(pos);
		this.pos.setOption(Position.DETECT_ALL,true);

        setSilentTime(2000);
    }


	public Game getGame()           { return game; }
	public MoveNode getMoveNode()   { return node; }

	public String getText()
	{
		StringBuffer buf = new StringBuffer();

		if (value==0)
			buf.append("{EGTB: Draw} ");
		else if (value >= AnalysisRecord.WHITE_MATES)
			buf.append("{EGTB: Win in "+(value-AnalysisRecord.WHITE_MATES)/2+"} ");
		else if (value <= AnalysisRecord.BLACK_MATES)
			buf.append("{EGTB: Win in "+(AnalysisRecord.BLACK_MATES-value)/2+"} ");

		for (Iterator i = moveList.iterator(); i.hasNext(); )
		{
			Move mv = (Move)i.next();
			buf.append(mv.toString());
			buf.append(" ");
		}

		return buf.toString();
	}

    public int init()
            throws Exception
    {
		if (pos.isGameFinished())
			return FAILURE;
		else
			return RUNNING;
    }

    public int work()
            throws Exception
    {
		try {
			value = AnalysisRecord.UNKNOWN;
			moveList = new ArrayList();

			EnginePlugin.EvaluatedMove move = getBestMove(pos.toString());
			value = move.getValue();

			int maxPlies;
			if (value==0)
				maxPlies = 12;
			else if (value >= AnalysisRecord.WHITE_MATES)
				maxPlies = 100;
			else if (value <= AnalysisRecord.BLACK_MATES)
				maxPlies = 100;
			else
				maxPlies = 12;

			for (int i=0; i < maxPlies; i++)
			{
				if (!pos.tryMove(move)) break;
				moveList.add(move);
System.err.println(move);
				if (pos.isGameFinished()) break;
				//  retrieve next
				move = getBestMove(pos.toString());
			}

		} catch (Exception e) {
			setProgressText(e.getMessage());
			if (moveList.isEmpty())
				return FAILURE;
			else
				return SUCCESS;
		}
		return SUCCESS;
    }

    public int done(int state)
    {
        if (state==SUCCESS)
        {
			Command cmd = new Command("menu.game.paste.line",null,this);
			Application.theCommandDispatcher.forward(cmd, Application.theApplication);
        }
        return state;
    }


    private EnginePlugin.EvaluatedMove getBestMove(String fen) throws Exception
    {
		fen = fen.replace('/',',');
		fen = fen.replace(' ',',');
		fen = URLEncoder.encode(fen);
		String urlString = StringUtil.replace(serverURL,"%fen%",fen);

        URL url = new URL(urlString);
//        ArrayList result = new ArrayList();
        InputStream in = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

		int value = parseEval(br.readLine());   //  evaluation
		//  only the first (=best) line is interesting for us
		Move mv = parseMove(br.readLine());
		return new EnginePlugin.EvaluatedMove(mv,0,value,0);
    }

	private int parseEval(String line)
	{
		//  Win in 12
		//  Lose in 5
		//  Draw
		if (line.startsWith("Win in "))
		{
			int plies = Integer.parseInt(line.substring(7));
			return AnalysisRecord.WHITE_MATES+plies;
		}
		if (line.startsWith("Lose in "))
		{
			int plies = Integer.parseInt(line.substring(8));
			return AnalysisRecord.BLACK_MATES-plies;
		}
		if (line.startsWith("Draw"))
			return 0;
		//  else
		return AnalysisRecord.UNKNOWN;
	}


	private Move parseMove(String line)
	{
		//  g5-g6:
		//  g7-g8(Q):

		Move mv = new Move(
		            EngUtil.char2Square(line.charAt(0),line.charAt(1)),
					EngUtil.char2Square(line.charAt(3),line.charAt(4)));

		if (line.charAt(5)=='(')
			mv.setPromotionPiece(EngUtil.char2Piece(line.charAt(6)));

		return mv;
	}
}
