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

package de.jose.pgn;

import de.jose.chess.BinaryConstants;
import de.jose.chess.EngUtil;
import de.jose.chess.Move;
import de.jose.chess.Position;

abstract public class BinReader
            implements BinaryConstants
{
    public byte[] bin;

    public int offset;

    protected Position pos;

	protected boolean hasVariations;
	protected boolean hasComments;
	protected boolean hasErrors;

	protected boolean hasResult;
	protected int result;
	protected boolean eof; 
	protected boolean wasMove;

    public BinReader(Position position)
    {
        pos = position;
    }


    abstract public void beforeMove(Move mv, int ply, boolean displayHint);

	abstract public void afterMove(Move mv, int ply);

    abstract public void annotation(int nagCode);

    abstract public void comment(StringBuffer text);

    abstract public void startOfLine(int nestLevel);

    abstract public void endOfLine(int nestLevel);

    abstract public void result(int resultCode);

    public void endOfFile()                              { }

	public void replayError(StringBuffer text, Move mv, int ply)
	{
		text.append("replay error: "+(ply/2+1)+ (((ply%2)==0) ? ". ":"...")+
	                                EngUtil.square2String(mv.from)+"-"+
	                                EngUtil.square2String(mv.to)+".\n" +
		                            " (this is most likely a bug in jose!)");
		replayError(text);
	}

	public void replayError(StringBuffer text, short code, int ply)
	{
		text.append("replay error: "+Integer.toHexString(code)+".\n" +
		                "(this is a bug in jose!)");
		replayError(text);
	}

	protected void replayError(StringBuffer text)
	{
		comment(text);
		text.setLength(0);
		hasErrors=true;
		eof = true;
	}

    public void read(byte[] binary, int startOffset,
                     byte[] comments, int coffset,
                     String fen, boolean replay)
    {
        bin = binary;
        offset = startOffset;

        wasMove = false;
        int nestLevel = 0;

        pos.setup(fen);
		startOfLine(0);

        eof = false;
        Move mv;
        StringBuffer text = new StringBuffer();

	    hasVariations = false;
	    hasComments = false;
	    hasErrors = false;
	    hasResult = false;
	    result = PgnConstants.RESULT_UNKNOWN;

        while (!eof) {
            short code = (short)((short)bin[offset++] & 0x00ff);

            //		working with signed bytes is a pain in the ass....
            switch (code)
            {
            case SHORT_ANNOTATION:
                short nagCode = (short)((short)bin[offset++] & 0x00ff);
	            if (nagCode==PgnConstants.NAG_DIAGRAM || nagCode==PgnConstants.NAG_DIAGRAM_DEPRECATED)
	                wasMove=false;
		        hasComments = true;
                annotation(nagCode);
                continue;

            case SHORT_BLACK_WINS:
				hasResult = true;
				result(result = PgnUtil.BLACK_WINS);
				continue;
			case SHORT_DRAW:
				hasResult = true;
				result(result = PgnUtil.DRAW);
				continue;
			case SHORT_WHITE_WINS:
				hasResult = true;
				result(result = PgnUtil.WHITE_WINS);
				continue;
			case SHORT_UNKNOWN:
				hasResult = true;
                result(result = PgnUtil.RESULT_UNKNOWN);
                continue;

            case SHORT_START_OF_LINE:
		        hasVariations = true;
                startOfLine(++nestLevel);

                if (replay)
                    pos.startVariation();
                wasMove = false;
                continue;

            case SHORT_END_OF_LINE:
                endOfLine(nestLevel--);

                if (replay)
                    pos.undoVariation();
                wasMove = false;
                continue;

            case SHORT_END_OF_DATA:
                eof = true;
                endOfFile();
                continue;
            }

            if (code <= SHORT_MOVE_MAX)
            {
                if (code >= SHORT_A_PROMOTION)
                    code |= ((short)bin[offset++] << 8);

                //	 a move
                int ply = pos.gamePly();
                mv = pos.decodeMove(code);

	            if (mv==null) {
	                replayError(text,code,ply);
	                continue;
	            }
                if (! pos.prepareMove(mv)) {
                	replayError(text,mv,ply);
                    continue;
                }

                beforeMove(mv,ply, !wasMove);

	            if (replay) {
		            pos.doMove(mv);
		            if (! pos.detect(mv)) {
		            	replayError(text,mv,ply);
		                continue;
		            }
	            }

	            afterMove(mv,ply);

	            /**	why are there two methods ?
	             * 	beforeMove() is needed to detect ambiguities in short formatting (must be done before)
	             * 	afterMove() can be used to detect checks (must be done after)
	             */

                wasMove = true;
                continue;
            }

            if (code <= SHORT_ANNOTATION_MAX)
            {
	            hasComments = true;
	            int nagCode = code-SHORT_ANNOTATION;
	            if (nagCode==PgnConstants.NAG_DIAGRAM || nagCode==PgnConstants.NAG_DIAGRAM_DEPRECATED)
	                wasMove=false;
	            //  else: simple annotations do not trigger 'wasMove' but diagrams do !
                annotation(nagCode);
                continue;
            }

            if (code <= SHORT_ERROR_MAX)
            {
                switch (code)
                {
                case SHORT_ERROR_UNREADABLE:	text.append("unreadable input:"); hasErrors=true; break;
                case SHORT_ERROR_UNRECOGNIZED:	text.append("unrecognized input:"); hasErrors=true; break;
                case SHORT_ERROR_ILLEGAL:		text.append("illegal move:"); hasErrors=true; break;
                case SHORT_ERROR_EMPTY:			text.append("input expected:"); hasErrors=true; break;
                case SHORT_ERROR_AMBIGUOUS:		text.append("ambiguous move:"); hasErrors=true; break;


                default:
		        case SHORT_COMMENT:             hasComments = true; break; 	//	user comment
                }

                if (comments!=null)
                {
                    coffset = PgnUtil.readText(text, comments,coffset);
                }
                else
                    text.append("...");     //  comments not available

	            comment(text);

	            text.setLength(0);
                wasMove = false;
                continue;
            }
        }

		endOfLine(0);
        pos.reset();
    }

	public boolean hasVariations() {
		return hasVariations;
	}

	public boolean hasComments() {
		return hasComments;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public boolean hasResult() {
		return hasResult;
	}

	public int getResult() {
		return result;
	}

}
