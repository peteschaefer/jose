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

import de.jose.Util;
import de.jose.Version;
import de.jose.chess.BinaryConstants;
import de.jose.chess.Move;
import de.jose.chess.Position;

import java.io.ByteArrayOutputStream;

public class BinWriter 
        implements BinaryConstants, PgnConstants
{
	/** Position for replay */
	protected Position pos;

    /** stores game text    */
    protected ByteArrayOutputStream text;
    /** stores comments */
    protected ByteArrayOutputStream comments;
    /** stores main line hash keys  */
    protected ByteArrayOutputStream posMain;
    /** stores variation hash keys  */
    protected ByteArrayOutputStream posVar;
    protected ByteArrayOutputStream posOut;

    protected int flags;

    public BinWriter(Position position) {
	    pos = position;
        text = new ByteArrayOutputStream(1024);
        comments = new ByteArrayOutputStream(1024);
        posMain = new ByteArrayOutputStream(4096);
	    pos.reset();
	    if (!position.isClassic()) flags |= Game.IS_FRC;
/*
	    if (Version.POSITION_INDEX) {
            posVar = new ByteArrayOutputStream(4096);
			posOut = posMain;
	    }
        flags = 0;
*/
    }

    public byte[] getText() {
        return text.toByteArray();
    }

    public byte[] getComments() {
        return comments.toByteArray();
    }

    public final boolean hasVariations()        { return Util.anyOf(flags, Game.HAS_VARIATIONS); }
    public final boolean hasComments()          { return Util.anyOf(flags, Game.HAS_COMMENTS); }
    public final boolean hasErrors()            { return Util.anyOf(flags, Game.HAS_ERRORS); }
    public final boolean hasPositions()         { return Util.anyOf(flags, Game.POS_INDEX); }
	public final boolean isFRC()                { return Util.anyOf(flags, Game.IS_FRC); }
	public final boolean isClassic()            { return Util.noneOf(flags, Game.IS_FRC); }

	public final byte[] getMainPositions() {
		return (posMain!=null) ? posMain.toByteArray():null;
	}

	public final byte[] getVarPositions() {
		return (posVar!=null) ? posVar.toByteArray():null;
	}

	public void move(Move mv)
    {
        if (!mv.isPrepared() && !pos.prepareMove(mv)) throw new ReplayException(pos,mv);
	    pos.doMove(mv);
    //  this method may fail if the move is not "prepared" (how can this happen ?)

        short code = mv.encode();

        byte lowerByte = (byte)(code & 0x00ff);
        byte upperByte = (byte)(code >> 8);

        text.write(lowerByte);
        if (upperByte != 0) text.write(upperByte);
/*      TODO
        if (key!=0L) {
            HashKey.encode(key,posOut,8);
            posOut.write(' ');
            flags |= Game.POS_INDEX;
        }
*/    }

    public void annotation(int nagCode) {
        if (nagCode < 32)
            text.write(SHORT_ANNOTATION+nagCode);
        else {
            text.write(SHORT_ANNOTATION);
            text.write(nagCode);
        }
        flags |= Game.HAS_COMMENTS;
    }

    public void comment(String commentText)
    {
        text.write(SHORT_COMMENT);
        appendComment(commentText);
        flags |= Game.HAS_COMMENTS;
    }


    public void error(int errorCode, String errorText) {
        text.write(errorCode);
        appendComment(errorText);
        flags |= Game.HAS_ERRORS;
    }



    public void startOfLine(int nestLevel) {
	    if (nestLevel > 0) {
	        pos.startVariation();
		    flags |= Game.HAS_VARIATIONS;
	    }

        text.write(SHORT_START_OF_LINE);
/*        if (nestLevel > 0) {
            flags |= Game.HAS_VARIATIONS;
            posOut = posVar;
        }
        else
            posOut = posMain;

        posOut.write('(');
        posOut.write(' ');
*/  }

    public void endOfLine(int nestLevel) {
	    if (nestLevel > 0)
	        pos.undoVariation();

        text.write(SHORT_END_OF_LINE);
/*
        if (nestLevel==0)
            posOut = posMain;
        else
            posOut = posVar;

        posOut.write(')');
*/  }

    public void endOfData() {
        text.write(SHORT_END_OF_DATA);
    }

    public void result(int resultCode)
    {
        switch (resultCode) {
        case WHITE_WINS:        text.write(SHORT_WHITE_WINS); break;
        case BLACK_WINS:        text.write(SHORT_BLACK_WINS); break;
        case DRAW:              text.write(SHORT_DRAW); break;
        case RESULT_UNKNOWN:    text.write(SHORT_UNKNOWN); break;
        }
    }



    protected void appendComment(String text)
    {
		for (int i=0; i < text.length(); i++)
			writeUTF(text.charAt(i));
		comments.write(0);		//	marks end of comment
    }

	protected void appendComment(StringBuffer text)
	{
		for (int i=0; i < text.length(); i++)
			writeUTF(text.charAt(i));
		comments.write(0);		//	marks end of comment
	}

    protected void appendComment(char[] ch, int from, int to)
    {
        for ( ; from < to; from++)
            writeUTF(ch[from]);
        comments.write(0);		//	marks end of comment
    }

	protected void writeUTF(char c)
	{
		if (c < 0x0080)
			comments.write((byte)(c & 0x007f));
		else if (c < 0x0800) {
			comments.write((byte)(0xc0 | (c>>6) & 0x1f));
			comments.write((byte)(0x80 | c & 0x3f));
		}
		else {
			comments.write((byte)(0xe0 | (c >> 12) & 0x0f));
			comments.write((byte)(0x80 | (c >> 6) & 0x3f));
			comments.write((byte)(0x80 | c & 0x3f));
		}
	}

}
