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

package de.jose.task.io;

import de.jose.pgn.GameBuffer;
import de.jose.pgn.PgnConstants;
import de.jose.util.StringUtil;
import de.jose.util.file.XStringBuffer;

import java.io.IOException;
import java.io.Reader;

/**
 * Importer for EPD and FEN files
 *
 * @author Peter Schäfer
 */
public class EPDImport
        extends PGNImport
{
	protected EPDImport(String taskName, String fileName, String url,
	                    Reader input, long length)
		throws Exception
	{
		super(taskName,fileName,url,input,length);
	}

	protected int[] fen = new int[2];
	protected int[] token = new int[2];
	protected int[] token2 = new int[2];
	protected XStringBuffer gameText = new XStringBuffer();

	public boolean read1Game()
	        throws IOException
	{
		for (;;)
		{
			if (!in.readLine(lineBuffer) && lineBuffer.length()==0) {
				eof = true;
				break;	//	EOF;
			}
			if (lineBuffer.length()==0) {
				continue;	//	empty line
			}

			//	parse line
			lineBuffer.append(' ');
			char[] chars = lineBuffer.getValue();
			int len = lineBuffer.length();

			//  first four tokens belong to FEN
            nextWord(chars,0,fen);
			appendWord(chars,fen);
			appendWord(chars,fen);
			appendWord(chars,fen);

            //  next words are either two numbers (FEN), of opcode sequence (EPD)
			int i = nextWord(chars,fen[1],token);

			if (i < 0) {
				//  no opcodes
				//  append silentcount and move no
				gm.row.FEN = GameBuffer.unescapeString(lineBuffer.getValue(), 0,fen[1])+" 0 1";

			} else if (isNumber(chars,token)) {
				//  FEN
				gm.row.FEN = GameBuffer.unescapeString(lineBuffer.getValue(), 0,lineBuffer.length());
			}
			else {
				//  FEN
				gm.row.FEN = GameBuffer.unescapeString(lineBuffer.getValue(), 0,fen[1])+" 0 1";

				//  EPD opcodes
				gameText.setLength(0);

				for (;;) {
					int end = endOpcode(lineBuffer,token[1]);
					parseOpcode(chars, token[0],token[1], end);

					if (end >= lineBuffer.length())
						break;
					else {
						int next = nextWord(chars,end+1,token);
						if (next < 0 || next > lineBuffer.length())
							break;
					}
				}

				if (gameText.length() > 0)
					gm.setGameText(gm.row, gameText.getValue(),0,gameText.length());
			}

			return true;
		}

		return false;
	}

	private void parseOpcode(char[] chars, int t0, int t1, int t2)
	{
		//  bm  Best Move
		if (StringUtil.equalsIgnoreCase("bm",chars,t0,t1-t0)) {
			//  best move
			gameText.append(' ');
			gameText.append(chars,t1,t2-t1);
			gameText.append(' ');
		}
		//  c0 - c9 comment
		if (((t1-t0)==2) && (chars[t0]=='c') && (chars[t0+1]>='0') && (chars[t0+1]<='9')) {
			gameText.append(" { ");
			gameText.append(chars,t1,t2-t1);
			gameText.append(" } ");
		}
		//  ce centipawn evaluation
		if (StringUtil.equalsIgnoreCase("ce",chars,t0,t1-t0)) {
			gameText.append(" {evaluation: ");
			gameText.append(chars,t1,t2-t1);
			gameText.append(" } ");
		}
		//  eco opening code
		if (StringUtil.equalsIgnoreCase("eco",chars,t0,t1-t0)) {
			gm.row.ECO = GameBuffer.unescapeString(chars,t1,t2-t1);
		}
		//  id identification
		if (StringUtil.equalsIgnoreCase("id",chars,t0,t1-t0)) {
			gm.row.sval[GameBuffer.IEVENT] = GameBuffer.unescapeString(chars,t1,t2-t1);
		}
		//  resign
		if (StringUtil.equalsIgnoreCase("resgin",chars,t0,t1-t0)) {
			gm.row.Result = PgnConstants.WHITE_WINS;    //  TODO the moving player resigned
		}

	}

	private static int nextWord (char[] chars, int offset, int[] word)
	{
		if (offset < 0) return -1;

		//  skip leading space
		while ((offset < chars.length) && chars[offset]==' ') offset++;
		if (offset >= chars.length) return -1;
		word[0] = offset;

		//  skip characters
		while ((offset < chars.length) && chars[offset]!=' ') offset++;
		return (word[1] = offset);
	}

	private static int appendWord (char[] chars, int[] word)
	{
		int offset = word[1];
		if (offset < 0) return -1;

		//  skip leading space
		while ((offset < chars.length) && chars[offset]==' ') offset++;
		if (offset >= chars.length) return -1;

		//  skip characters
		while ((offset < chars.length) && chars[offset]!=' ') offset++;
		return (word[1] = offset);
	}

	private static boolean isNumber(char[] chars, int[] word)
	{
		if (word[0] < 0) return false;
		for (int i=word[0]; i < word[1]; i++)
		{
			if ((chars[i] < '0') || (chars[i] > '9'))
				return false;
		}
		return true;
	}

	private static int endOpcode(XStringBuffer buf, int offset)
	{
		int result = buf.indexOf(';',offset);
		if (result < 0) result = buf.length();
		return result;
	}

}
