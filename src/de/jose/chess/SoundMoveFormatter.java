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

package de.jose.chess;

import de.jose.Sound;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * a move formatter that can speak ;-)
 */
public class SoundMoveFormatter
        extends MoveFormatter
{
	/** base file (directory or ZIP file)
	 */
	private File dir;
	/** true if zip file    */
	private boolean zipfile;
	/** true for ChessBase files. there is a sound for every square on the board.
	 *  if false, there are sounds for files and rows
	 */
	private boolean allsquares;
	/**
	 * true to pronounce stalemate and checkmate
	 * (usually false since this is handled by Application.gameFinished)
	 */
	private boolean pronounceMate;

	/** available sound files
	 *  Map<lowercase String,String>
	 */
	private Map files;

	public SoundMoveFormatter()
	{
		super();

		files = new HashMap();
		dir = null;

		pieceChars = new String[6];

		separator          = "";    //  not pronounced (but could be !?)
		captureSeparator   = "takes.wav";

		promotionSeparator  = "";    //  not pronounced (but could be !?)
		enPassant           = "Enpassant.wav.";

		check               = "";   //  not pronounced (why ?)
		mate				= "Checkmate.wav";
		stalemate			= "Stalemate.wav";
		draw3				= "";   //  not pronounced (why ?)
		draw50				= "";   //  not pronounced (why ?)
		nullmove            = "";   //  not pronounced

		pronounceMate       = true;
	}

	public SoundMoveFormatter(File dir, int format) throws IOException
	{
		this();
		setFormat(format);
		setDirectory(dir);
	}

	public void setPronounceMate(boolean on)    { pronounceMate = on; }

	public void setDirectory(File dir) throws IOException
	{
		this.dir = dir;
		this.files.clear();

		if (dir.isDirectory()) {
			zipfile = false;

			String[] filelist = dir.list();
			for (int i=0; i<filelist.length; i++)
				files.put(filelist[i].toLowerCase(), filelist[i]);
		}
		else {
			zipfile = true;

			ZipFile zipf = new ZipFile(dir);
			Enumeration entries = zipf.entries();
			while (entries.hasMoreElements())
			{
				ZipEntry zety = (ZipEntry)entries.nextElement();
				files.put(zety.getName().toLowerCase(),zety.getName());
			}
			zipf.close();
		}

		kingsideCastling = oneOf("00.wav","0-0.wav");
		queensideCastling = oneOf("000.wav","0-0-0.wav");

		pieceChars[0]   = englishPieces[1] = oneOf("Pa.wav","Pawn.wav");
		pieceChars[1]   = englishPieces[2] = oneOf("Na.wav","Knight.wav");
		pieceChars[2]   = englishPieces[3] = oneOf("Ba.wav","Bishop.wav");
		pieceChars[3]   = englishPieces[4] = oneOf("Ra.wav","Rook.wav");
		pieceChars[4]   = englishPieces[5] = oneOf("Qa.wav","queen.wav");
		pieceChars[5]   = englishPieces[6] = oneOf("Ka.wav","King.wav");

		allsquares = isAvailable("A1.wav") || isAvailable("A1_lv.wav");
	}

	protected void doFormat(Move mv, Position pos, int format, boolean castling, boolean chck, boolean ep)
	{
		super.doFormat(mv, pos, format, castling, pronounceMate && chck, ep);
	}

	protected void doFormatTelegraphic(Move mv)
	{
		//  use algebraic instead
		super.doFormatAlgebraic(mv);
	}

	public void figurine(int pc, boolean promotion)
	{
		int idx = EngUtil.uncolored(pc)-PAWN;
		play(pieceChars[idx]);
	}

	public void text(char chr)
	{
		//  shouldn't be called at all
		System.err.println("SoundMoveFormatter.text(char) called with: "+chr);
	}

	public void square(int sq, boolean file, boolean row, boolean leaving)
	{
		/** Fritz has sounds for each square; file and row can't be pronounced separately   */
		if (allsquares) {
			String s = EngUtil.square2String(sq);
			play(s,leaving, "_lv","");
		}
		else {
			/** others have separate sounds for files and rows  */
			if (file) {
				String ch = String.valueOf(EngUtil.fileChar(EngUtil.fileOf(sq)));
				play(ch,leaving, "from","to");
			}
			if (row) {
				String ch = String.valueOf(EngUtil.rowChar(EngUtil.rowOf(sq)));
				play(ch,leaving, "from","to");
			}
		}
	}

	public void play(String text, boolean origin, String orig_suffix, String dest_suffix)
	{
		String suffix = origin ? orig_suffix:dest_suffix;
		String file = text+suffix+".wav";

		if (isAvailable(file))
			play(file);
		else {
			suffix = origin ? dest_suffix:orig_suffix;
			file = text+suffix+".wav";
			if (isAvailable(file))
				play(file);
		}
	}
	public void text(String str, int castling)
	{
		if ((str.length() > 0) && str.endsWith(".wav"))
			play(str);
	}

	protected boolean isAvailable(String audioFile)
	{
		return files.containsKey(audioFile.toLowerCase());
	}

	protected String oneOf(String filea, String fileb)
	{
		if (isAvailable(filea))
			return filea;
		else if (isAvailable(fileb))
			return fileb;
		else
			return "";
	}

	public boolean play(String audioFile)
	{
		if (isAvailable(audioFile))
		try {
			URL url;
			audioFile = (String)files.get(audioFile.toLowerCase()); //  map to REAL name (with proper casing)
//			System.out.println(audioFile);

			if (zipfile)
				url = new URL("file",null, dir.getAbsolutePath()+"!"+audioFile);
			else
				url = new URL("file",null, dir.getAbsolutePath()+File.separator+audioFile);
			Sound.play(url);
			return true;

		} catch (MalformedURLException muex) {
			//  what can we do ?
			System.err.println("couldn't play "+audioFile);
		}

		return false;
	}
}
