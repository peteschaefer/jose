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

import de.jose.Language;
import de.jose.Util;
import de.jose.chess.*;
import de.jose.pgn.*;
import de.jose.task.GameTask;
import de.jose.task.GameIterator;
import de.jose.task.GameHandler;
import de.jose.util.CharUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.file.LinePrintWriter;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarOutputStream;

/**
 *
 * @author Peter Schäfer
 */

public class PGNExport
        extends GameTask
        implements PgnConstants, GameHandler
{
	// ---------------------------------------------------------
	//  inner class: BinReader
	// ---------------------------------------------------------

	class PGNExportBinReader extends BinReader
	{
	    PGNExportBinReader(Position pos)
	    {
	        super(pos);
	    }

		public void replayError(Move mv, int ply)
		{
			throw new RuntimeException("illegal move "+(ply/2+1)+ (((ply%2)==0) ? ". ":"...")+
		                                EngUtil.square2String(mv.from)+"-"+
		                                EngUtil.square2String(mv.to)+" in replay");
		}

	    public void annotation(int nagCode) {
	        if (nagCode >= 1 && nagCode <= 6) {
		         //	!,?, etc. these MUST be defined in the translation
	            out.print(Language.get("pgn.nag."+nagCode));
	        }
	        else {
	            out.print("$");
	            out.print(String.valueOf(nagCode));
	        }
	        out.print(" ");
	    }

	    public void result(int resultCode) {
	        out.print(PgnUtil.resultString(resultCode));
	    }

	    public void startOfLine(int nestLevel) {
	        if (nestLevel > 0) out.print(" (");
	    }

	    public void endOfLine(int nestLevel) {
	        if (nestLevel > 0) out.print(") ");
	    }

	    public void beforeMove(Move mv, int ply, boolean displayHint) {
	        if ((ply%2)==0)  {
	            out.print(String.valueOf(ply/2+1));
	            out.print(".");
	        } else if (displayHint) {
	            out.print(String.valueOf(ply/2+1));
	            out.print("...");
	        }
		    formatter.format(mv,pos);
		    /**	format must be called before the move to detect ambigutities in short formatting	*/
	    }

		public void afterMove(Move mv, int ply) {
			if (mv.isCheck())
				out.print(formatter.check);
				/**	checks can only be detected after the move */
	        out.print(" ");
			out.breakIf(80);
	    }

        public void comment(StringBuffer buf)
        {
            if (out.column() > 0) out.print(" ");
            out.print("{");

            for (int i=0; i<buf.length(); i++)
            {
                char c = buf.charAt(i);
                switch (c)
                {
                case '{':   out.print('('); break;  //  got to escape braces
                case '}':   out.print(')'); break;  //  got to escape braces
		        case ' ':
		        case '\t':  if (out.breakIf(80)) {
			                    while ((i+1) < buf.length() && buf.charAt(i+1)==' ') i++;
		                    }
		                    else
			                    out.print(' ');
			                break;
                default:    out.print(c); break;
                }
            }

            out.print("} ");
        }
/*
	    public void comment(StringBuffer buf, StyleRun styles)
	    {
            //  print style runs
            if (styles!=null)
            {
                out.newLine();
                out.print("%jose:styles:");
                styles.print(out);
                out.println();
            }

            String text = escapeCommentText(buf);
	        out.print(" {");

            /**
             * ATTENTION: when breaking lines
             * make sure that the comment will be imported
             * exactly like it was; otherwise style runs could get out of synch
             * /
		    int i = 0;
		    int j= findBreak(text,0);
		    while (j >= 0)
		    {
				out.write(text,i,j-i);

                if (text.charAt(j)=='\n')
                {
                    //  intentional line break; write TWO breaks which will be imported as one
                    j++;
                    out.println();
                    out.println();
                    //  write consecutive line breaks, too
                    while ((j < text.length()) && text.charAt(j)=='\n') {
                        j++;
                        out.println();
                    }
                }
                else if (out.column() >= 80) {
                    j++;    //  eat whitespace, will be inserted again on import
                    out.println();
                }

			    i = j;
			    j = findBreak(text,j+1);
		    }

		    if (i < text.length()) out.write(text,i,text.length()-i);
	        out.print("} ");
		    out.breakIf(80);

	    }

        private int findBreak(String text, int i)
        {
            while (i < text.length())
            {
                char c = text.charAt(i);
                if (c==' '||c=='\n')
                    return i;
                else
                    i++;
            }
            return -1;
        }

		protected String escapeCommentText(StringBuffer text)
		{
			int k0 = text.indexOf("{");
			int k1 = text.indexOf("}");

			if (k0<0 && k1<0) return text.toString();

			if (k0>=0) StringUtil.replace(text,"{","(");
			if (k1>=0) StringUtil.replace(text,"}",")");
			return text.toString();
		}
*/

	}

	// ---------------------------------------------------------
	//  Fields
	// ---------------------------------------------------------

	/** output file */
	protected File outputFile;
	/** Zip output stream (only used if writing ZIP file)   */
	protected ZipOutputStream zout;
	/** GZip output stream (only used if writing GZIP file)   */
	protected GZIPOutputStream gzout;
	/** Tar output stream (only used if writing GZIP file)   */
	protected TarOutputStream tarout;
	/** BZip output stream (only used if writing GZIP file)   */
	protected CBZip2OutputStream bzout;
	/** output print writer   */
	protected LinePrintWriter out;
	/** replay Position */
	protected Position pos;
	/** reads binary data   */
	protected PGNExportBinReader binReader;
	/** formats moves   */
	protected PrintMoveFormatter formatter;

	static final int HANDLER_LIMIT   = 256;
	static final int BUFFER_LIMIT    = 256;

	// ---------------------------------------------------------
	//  Ctor
	// ---------------------------------------------------------

	public PGNExport(Object output)
			throws Exception
	{
		super("PGN export",true);
		if (output instanceof File)
			outputFile = (File)output;
		else if (output instanceof Writer)
			out = new LinePrintWriter((Writer)output,false);
		else if (output instanceof OutputStream)
		{
			OutputStreamWriter wout = new OutputStreamWriter((OutputStream)output,"ISO-8859-1");
			/**	PGN specifies ISO-8859-1	!	*/
			out = new LinePrintWriter(wout,false);
		}
		else
			throw new IllegalArgumentException();
	}

	// ---------------------------------------------------------
	//  overrides MaintenanceTask
	// ---------------------------------------------------------
/*
	public void processCollection(int CId) throws Exception
	{
		processCollectionContents(CId);
	}
*/
/*
	public void processCollectionContents(int CId) throws Exception
	{
		/**
		 * use MySQL HANDLER to traverse the Game table
		 * it is vastly more efficient with large tables
		 *
		 * we could use a SELECT with LIMIT but, unfortunately, the LIMIT clause
		 * causes a full index scan that gets more and more expensive.
		 * (working with large result sets in MySQL is a pain in the ass !!)
		 *
		 * the drawback is now, that we have to fetch data from MoreGame manually
		 * /
		try {
		    getConnection().executeUpdate("HANDLER Game OPEN AS PGNExportRead");

			JoStatement stm = new JoStatement(getConnection());
			StringBuffer buf = new StringBuffer("HANDLER PGNExportRead ");
			buf.append("READ Game_15 = ("+CId+") ");
			buf.append(" WHERE CId = "+CId+" ");
			buf.append(" LIMIT "+HANDLER_LIMIT);

			stm.executeQuery(buf.toString());
			//  INDEX Game_15 ON Game(CId,Id)

			buf.setLength(0);
			buf.append("HANDLER PGNExportRead READ Game_15 NEXT ");
			buf.append(" WHERE CId = "+CId+" ");
			buf.append(" LIMIT "+HANDLER_LIMIT);
			String sql = buf.toString();

			while (processGames(stm.selectIntArray()))
			{
				stm.executeQuery(sql);
				if (isAbortRequested()) break;
			}

		} finally {
			getConnection().executeUpdate("HANDLER PGNExportRead CLOSE");
		}
	}
*/
/*
	public void processGame(int GId) throws Exception
	{
		StringBuffer sql = new StringBuffer("SELECT * FROM Game WHERE Id = ");
		sql.append(String.valueOf(GId));

		processGames(sql.toString());
	}
*/
/*
	public void processGames(int[] GId, int from, int to) throws Exception
	{
		StringBuffer sql = new StringBuffer("SELECT * FROM Game WHERE Id IN (");

		sql.append(GId[from++]);
		while (from < to) {
		    sql.append(",");
		    sql.append(GId[from++]);
		}

		sql.append(")");

		processGames(sql.toString());
	}
*/

	public void prepare() throws Exception
	{
        OutputStream fout = null;

		if (outputFile!=null) {
            fout = new BufferedOutputStream(new FileOutputStream(outputFile),4096);
            String fileName = outputFile.getName();
            String trimmedName = FileUtil.trimExtension(fileName);

            //  TODO streamline this !
            if (FileUtil.hasExtension(fileName,"zip"))
            {
                //  ZIP
                fout = zout = new ZipOutputStream(fout);
                if (!FileUtil.hasExtension(fileName,"pgn"))
                    trimmedName += ".pgn";
                ZipEntry entry = new ZipEntry(trimmedName);
                zout.putNextEntry(entry);
            }
            else if (FileUtil.hasExtension(fileName,"gzip") || FileUtil.hasExtension(fileName,"gz"))
            {
                //  GZIP
                fout = gzout = new GZIPOutputStream(fout);
            }
            else if (FileUtil.hasExtension(fileName,"bz")
		            || FileUtil.hasExtension(fileName,"bz2")
		            || FileUtil.hasExtension(fileName,"bzip2")
		            || FileUtil.hasExtension(fileName,"bzip"))
            {
                //  BZIP
                fout = bzout = FileUtil.createBZipOutputStream(fout);
            }
        }
/*
		writing TAR files: we cant write TAR headers because we don't know the actual file size ;-(

		else if (FileUtil.hasExtension(trimmedName,"tar"))
		{
			//  tar (unzipped)
			fout = tarout = new TarOutputStream(fout);
			String entryName = FileUtil.trimExtension(trimmedName)+".pgn";
			TarEntry entry = new TarEntry(entryName);
			tarout.putNextEntry(entry);
		}
		else if (FileUtil.hasExtension(trimmedName,"tgz") || FileUtil.hasExtension(trimmedName,"tgzip") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(fileName,"gz") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(fileName,"gzip"))
		{
			//  tar.gz
			gzout = new GZIPOutputStream(fout);
			fout = tarout = new TarOutputStream(gzout);

			String entryName = FileUtil.trimExtension(trimmedName)+".pgn";
			TarEntry entry = new TarEntry(entryName);
			tarout.putNextEntry(entry);
		}
		else if (FileUtil.hasExtension(trimmedName,"tbz") || FileUtil.hasExtension(trimmedName,"tbz2") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(fileName,"bz") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(fileName,"bz2"))
		{
			//  tar.bz2
			bzout = new CBZip2OutputStream(fout);
			fout = tarout = new TarOutputStream(bzout);

			String entryName = FileUtil.trimExtension(trimmedName)+".pgn";
			TarEntry entry = new TarEntry(entryName);
			tarout.putNextEntry(entry);
		}
*/

		pos = new Position();
		pos.setOption(Position.INCREMENT_HASH, false);
		pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
		pos.setOption(Position.EXPOSED_CHECK, false);
		pos.setOption(Position.STALEMATE, false);
		pos.setOption(Position.DRAW_3, false);
		pos.setOption(Position.DRAW_50, false);
		pos.setOption(Position.DRAW_MAT, false);

        if (fout!=null) {
            OutputStreamWriter wout = new OutputStreamWriter(fout,"ISO-8859-1");
            /**	PGN specifies ISO-8859-1	!	*/
            out = new LinePrintWriter(wout,false);
        }
        //  else: out set in ctor !
        binReader = new PGNExportBinReader(pos);
        formatter = new PrintMoveFormatter(out);
        formatter.setFormat(MoveFormatter.SHORT);
	    formatter.enPassant = "";	//	e.p. moves are not indicated in PGN
		formatter.mate		= "+";	//	mate moves are not indicated
		formatter.stalemate	= "";
		formatter.draw3		= "";
		formatter.draw50	= "";
	}

	public int work() throws Exception
	{
		prepare();

		GameIterator gi = GameIterator.newGameIterator(source,getConnection());

		while (gi.hasNext()) {
			gi.next(this);
			processedGames++;

			if (isAbortRequested()) break;
		}

		return finish();
	}

	//  implements GameHandler; callback routines from GameIterator
	public void handleObject(Game game)
	{
		printGame(game);
	}

	public void handleRow(ResultSet res) throws Exception
	{
		printGame(res);
	}

	public int finish() throws Exception
	{
		if (out!=null)
			out.flush();
		if (tarout!=null)
			try {
				//  TAR entry must be closed (before closing the stream)
				tarout.closeEntry();
				tarout.close();
			} catch (IOException ioex) {
				return ERROR;
			}
		if (zout!=null)
			try {
				zout.closeEntry();
				zout.close();
			} catch (IOException ioex) {
				return ERROR;
			}
		if (gzout!=null)
			try {
				gzout.close();
			} catch (IOException ioex) {
				return ERROR;
			}
		if (bzout!=null)
			try {
				bzout.flush();
				bzout.close();
			} catch (IOException ioex) {
				return ERROR;
			}
		if (out!=null)
			out.close();

		return SUCCESS;
	}

	// ---------------------------------------------------------
	//  Private Parts
	// ---------------------------------------------------------
/*
	protected boolean processGames(String sql) throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = getConnection().getPreparedStatement(sql);
			pstm.execute();
			return processGames(pstm.selectIntArray());
		} finally {
			if (pstm!=null) pstm.close();
		}
	}
*/
/*
	protected boolean processGames(IntArray GIds) throws SQLException
	{
		if (GIds==null || GIds.isEmpty()) return false;

		ParamStatement pstm = new ParamStatement();
		pstm.select.append(Game.DISPLAY_SELECT);
		pstm.from.append(Game.DISPLAY_FROM);
		pstm.where.append(Game.DISPLAY_WHERE);
		pstm.where.append(" AND Game.Id IN (");
		pstm.where.append(String.valueOf(GIds.get(0)));
		for (int i=1; i<GIds.size(); i++) {
			pstm.where.append(",");
			pstm.where.append(String.valueOf(GIds.get(i)));
		}
		pstm.where.append(")");

		JoPreparedStatement prepStm = null;
		try {
			prepStm = pstm.execute(getConnection());
			return processGames(prepStm.getResultSet());
		} finally {
			if (prepStm!=null) prepStm.close();
		}
	}
*/
/*
	protected boolean processGames(ResultSet res) throws SQLException
	{
		boolean any = false;
		while (res.next())
		{
			printGame(res);
			processedGames++;
			any = true;
		}
		return any;
	}
*/


	private void printGame(ResultSet res)   throws SQLException
	{
/*
            +"       Game.Id,Game.CId,Game.Idx,Attributes,PlyCount, "
            +"       WhiteId, White.Name, WhiteTitle, WhiteELO, "
            +"       BlackId, Black.Name, BlackTitle, BlackELO, "
            +"       Result, GameDate, EventDate, DateFlags, "
			+"		 EventId, Event.Name, SiteId, Site.Name, Round, Board, "
            +"       Game.ECO, OpeningId, Opening.Name, AnnotatorId, Annotator.Name, FEN, MoreGame.Info, "
            +"       MoreGame.Bin, MoreGame.Comments ";

*/
	    int i = 5;  //  1 = Id, 2 = CId, 3 = Idx, 4 = Attributes

		int plycount = res.getInt(i++);

		int whiteId = res.getInt(i++);
		String whitePlayer = res.getString(i++);
		String whiteTitle = res.getString(i++);
		int whiteElo = res.getInt(i++);

		int blackId = res.getInt(i++);
		String blackPlayer = res.getString(i++);
		String blackTitle = res.getString(i++);
		int blackElo = res.getInt(i++);

		int result = res.getInt(i++);

		Date gameDate = res.getDate(i++);
		Date eventDate = res.getDate(i++);
		short dateFlags = res.getShort(i++);
		if (gameDate!=null)
			gameDate = new PgnDate(gameDate, (short)(dateFlags & 0xff));
		if (eventDate != null)
			eventDate = new PgnDate(eventDate, (short)((dateFlags>>8) & 0xff));

		int eventId = res.getInt(i++);
		String event = res.getString(i++);
		int siteId = res.getInt(i++);
		String site = res.getString(i++);

		String round = res.getString(i++);
		String board = res.getString(i++);

		String eco = res.getString(i++);
		int openingId = res.getInt(i++);
		String openingName = res.getString(i++);
		int annotatorId = res.getInt(i++);
		String annotator = res.getString(i++);

		String fen = res.getString(i++);
		String moreTags = res.getString(i++);

		//  the following tags must appear on top, and in precisely this order:
/*
			[Event "F/S Return Match"]
			[Site "Belgrade, Serbia JUG"]
			[Date "1992.11.04"]
			[Round "29"]
			[White "Fischer, Robert J."]
			[Black "Spassky, Boris V."]
			[Result "1/2-1/2"]
*/

		//  <HEADER info="Event">
		//  some PGN parser always expect the Event tag to be first
		printHeader(TAG_EVENT,          event);

		//  <HEADER info="Site">
		if (site != null)
		    printHeader(TAG_SITE,        site);

		//  <HEADER info="Date">
	    if (gameDate != null)
	        printHeader(TAG_DATE, gameDate.toString());

		//  <HEADER info="Round">
		if (round!=null)
		    printHeader(TAG_ROUND,   round);

	    //  ["White">
	    printHeader(TAG_WHITE,           whitePlayer);

	    //  <HEADER info="Black">
	    printHeader(TAG_BLACK,           blackPlayer);

		//  [Result
		printHeader(TAG_RESULT,          PgnUtil.resultString(result));

		//  these are non-standard tags:

	    //  <HEADER info="WhiteElo">
	    if (whiteElo > 0)
	        printHeader(TAG_WHITE_ELO,   String.valueOf(whiteElo));

	    //  <HEADER info="BlackElo">
	    if (blackElo > 0)
	        printHeader(TAG_BLACK_ELO,   String.valueOf(blackElo));

	    //  <HEADER info="WhiteTitle">
	    if (whiteTitle != null)
	        printHeader(TAG_WHITE_TITLE,  whiteTitle);

	    //  <HEADER info="BlackTitle">
	    if (blackTitle != null)
	        printHeader(TAG_BLACK_TITLE,  blackTitle);

	    //  <HEADER info="EventDate">
	    if (eventDate != null)
	        printHeader(TAG_EVENT_DATE,  eventDate.toString());

		//  <HEADER info="Board">
		if (board!=null)
		    printHeader(TAG_BOARD,   board);

	    //  <HEADER info="Opening">
	    if (openingName!=null && !openingName.equals("-"))
	        printHeader(TAG_OPENING,     openingName);

	    //  <HEADER info="ECO">
	    if (eco != null && !eco.equals("-"))
	        printHeader(TAG_ECO, eco);
//	    else if (oeco != null && !oeco.equals("-"))
//	        printHeader(TAG_ECO, oeco);

		//	<HEADER info="Annotator">
		if (annotator != null && !annotator.equals("-"))
			printHeader(TAG_ANNOTATOR, annotator);

	    //  <HEADER info="FEN">
	    if (fen!=null)
	        printHeader(TAG_FEN,     fen);

	    //  <HEADER info="...">
	    if (moreTags != null) {
	        StringTokenizer tok = new StringTokenizer(moreTags, "=;");
	        while (tok.hasMoreTokens()) {
	            String key = tok.nextToken();
	            String value = tok.nextToken();
	            printHeader(key,value);
	        }
	    }
		//  [PlyCount
		if (plycount > 0)
			printHeader(TAG_PLY_COUNT,       String.valueOf(plycount));

	    out.println();
	    //  <LINE>
	    byte[] bin = res.getBytes(i++);
		int idx = res.getInt(3);
	    byte[] comments = res.getBytes(i++);

	    try {
			binReader.read(bin,0, comments,0, fen,true);
	    } catch (RuntimeException rex) {
			//	replay error
		    out.println();
		    out.print(" {");
		    out.print(rex.getMessage());
		    out.println("} ");
		    out.flush();
		    throw rex;
		}

		if (!binReader.hasResult()) {
		    out.print(" ");
		    out.print(PgnUtil.resultString(result));
		}
		else if (binReader.getResult() != result)
			;	//	"warning: game text does not match Result Tag ?"

		out.println();
	    out.println();
	}

	public void printGame(Game gm)
	{
	    int i = 3;  //  1 = Id, 2 = Idx

		int plycount = gm.getMainLine().countMoves();;
		int result = gm.getResult();
        //  the following tags must appear on top, and in precisely this order:
/*
			[Event "F/S Return Match"]
			[Site "Belgrade, Serbia JUG"]
			[Date "1992.11.04"]
			[Round "29"]
			[White "Fischer, Robert J."]
			[Black "Spassky, Boris V."]
			[Result "1/2-1/2"]
*/
        Date gameDate = (Date)gm.getTagValue(TAG_DATE);
        Date eventDate = (Date)gm.getTagValue(TAG_EVENT_DATE);

        //  <HEADER info="Event">
        String event = (String)gm.getTagValue(TAG_EVENT);
        if (event != null)
            printHeader(TAG_EVENT,       event);

        //  <HEADER info="Site">
        String site = (String)gm.getTagValue(TAG_SITE);
        if (site != null)
            printHeader(TAG_SITE,        site);

        //  <HEADER info="Date">
        if (gameDate != null)
            printHeader(TAG_DATE, gameDate.toString());

        //  <HEADER info="Round">
        String round = (String)gm.getTagValue(TAG_ROUND);
        if (round!=null)
            printHeader(TAG_ROUND,   round);

	    //  ["White">
	    printHeader(TAG_WHITE,           (String)gm.getTagValue(TAG_WHITE));
	    //  <HEADER info="Black">
	    printHeader(TAG_BLACK,           (String)gm.getTagValue(TAG_BLACK));
		//  [Result
		printHeader(TAG_RESULT,          PgnUtil.resultString(result));


	    //  <HEADER info="WhiteElo">
	    int whiteElo = Util.toint(gm.getTagValue(TAG_WHITE_ELO));
	    if (whiteElo > 0)
	        printHeader(TAG_WHITE_ELO,   String.valueOf(whiteElo));

	    //  <HEADER info="BlackElo">
	    int blackElo = Util.toint(gm.getTagValue(TAG_BLACK_ELO));
	    if (blackElo > 0)
	        printHeader(TAG_BLACK_ELO,   String.valueOf(blackElo));

	    //  <HEADER info="WhiteTitle">
	    String whiteTitle = (String)gm.getTagValue(TAG_WHITE_TITLE);
	    if (whiteTitle != null)
	        printHeader(TAG_WHITE_TITLE,  whiteTitle);

	    //  <HEADER info="BlackTitle">
	    String blackTitle = (String)gm.getTagValue(TAG_BLACK_TITLE);
	    if (blackTitle != null)
	        printHeader(TAG_BLACK_TITLE,  blackTitle);

/*
		short dateFlags = res.getShort(i++);

		if (gameDate!=null)
			gameDate = new PgnDate(gameDate, (short)(dateFlags & 0xff));
		if (eventDate != null)
			eventDate = new PgnDate(eventDate, (short)((dateFlags>>8) & 0xff));
		//  dateflags are already set, right ?
*/

	    //  <HEADER info="EventDate">
	    if (eventDate != null)
	        printHeader(TAG_EVENT_DATE,  eventDate.toString());

	    //  <HEADER info="Opening">
	    String eco = (String)gm.getTagValue(TAG_ECO);
	    String openingName = (String)gm.getTagValue(TAG_OPENING);
	    if (openingName!=null && !openingName.equals("-"))
	        printHeader(TAG_OPENING,     openingName);

	    //  <HEADER info="ECO">
	    if (eco != null && !eco.equals("-"))
	        printHeader(TAG_ECO, eco);

		//	<HEADER info="Annotator">
		String annotator = (String)gm.getTagValue(TAG_ANNOTATOR);
		if (annotator != null && !annotator.equals("-"))
			printHeader(TAG_ANNOTATOR, annotator);

	    //  <HEADER info="Board">
	    String board = (String)gm.getTagValue(TAG_BOARD);
	    if (board!=null)
	        printHeader(TAG_BOARD,   board);

	    //  <HEADER info="FEN">
	    String fen = (String)gm.getTagValue(TAG_FEN);
	    if (fen!=null)
	        printHeader(TAG_FEN,     fen);

	    //  <HEADER info="...">
	    List moreTags =  gm.getMoreTags();
	    if (moreTags != null) {
	        Iterator j = moreTags.iterator();
		    while (j.hasNext())
		    {
			    TagNode tnd = (TagNode)j.next();
	            String key = tnd.getKey();
	            String value = Util.toString(tnd.getValue());
	            printHeader(key,value);
	        }
	    }
		//  [PlyCount
		printHeader(TAG_PLY_COUNT,       String.valueOf(plycount));

	    out.println();
	    //  <LINE>
		BinWriter writer = gm.getBinaryData();
	    byte[] bin = writer.getText();
	    byte[] comments = writer.getComments();

	    try {
			binReader.read(bin,0, comments,0, fen,true);
	    } catch (RuntimeException rex) {
			//	replay error
		    out.println();
		    out.print(" {");
		    out.print(rex.getMessage());
		    out.println("} ");
		    out.flush();
		    throw rex;
		}

		if (!binReader.hasResult()) {
		    out.print(" ");
		    out.print(PgnUtil.resultString(result));
		}
		else if (binReader.getResult() != result)
			;	//	"warning: game text does not match Result Tag ?"

		out.println();
	    out.println();
	}

	private void printHeader(String key, String value)
	{
	    out.print("[");
	    out.print(key);
	    out.print(" \"");
	    out.print(escapeTagValue(value));
		/*	PGN specifies no escape mechanism for characters outside ISO-8859-1
			but some programs use \"u or something ...

			note that PGNImport is able to decode these escaped characters, maybe we should
			be able to export them too (though it is not required by the PGN spec)
		  */
	    out.println("\"]");
	}


	/**
	 * PGN specifies ISO-8859-1 encoding;
	 * other chars are escaped with \"
	 *
	 * @param text
	 * @return
	 * @see CharUtil.escape()
	 */
	protected static String escapeTagValue(String text)
	{
		if (text==null)
			return "";
		else
			return CharUtil.escape(text,false);
		//  false = retain ASCII <= 256, escape all others
		//  true = retain ASCII <= 128, escape all diacritic chars
	}

}
