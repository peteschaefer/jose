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

package de.jose;

import de.jose.util.StringUtil;
import de.jose.task.io.PGNImport;
import de.jose.pgn.GameBuffer;
import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.sql.SQLException;

/**
 * command line PGN parser. Results are written into a tab separated text file.
 *
 * @author Peter Schäfer
 */

public class pgn2csv
{
	static class ParamError extends RuntimeException
	{
		ParamError(String message) { super(message); }
	};

	class FileOutputPGNImport extends PGNImport
	{
		FileOutputPGNImport(Reader input, long length) throws Exception
		{
			super(input,length, new FileOutputGameBuffer(1,40));
			setSilentTime(Long.MAX_VALUE/2);
			canDisableKeys = false;
		}
	}

	class FileOutputGameBuffer extends GameBuffer
	{
		FileOutputGameBuffer(int gameIndex, int size) throws Exception
		{
			super(null,0,gameIndex,size);
		}

		public void setGameText(Row r, char[] chars, int start, int end)
		{
			//  DON'T parse the game

		}

		public void update(Thread reader) throws SQLException
		{
			for (int i=0; i<fill; i++)
			{
				//  print one line
				printRow(buffer[i]);
			}

			flush(reader);
		}

	}

	/** set of input file
	 * ArrayList<File>*/
	protected ArrayList inputFiles;
	/** output file (optional)  */
	protected File      outputFile;
	/** append to output file ? (default=true)  */
	protected boolean   appendOutput;

	/** list of PGN tags to output
	 * ArrayList<String> */
	protected ArrayList outputColumns;
	/** column separator (default=tab)  */
	protected String    columnSeparator;
	/** line separator (default=\n) */
	protected String    lineSeparator;


	/** output print writer */
	protected PrintWriter   output;


	public pgn2csv()
	{
		inputFiles = new ArrayList();
		outputFile = null;  // = std.out
		appendOutput = true;
		outputColumns = new ArrayList();
		columnSeparator = "\t";
		lineSeparator = "\n";
	}

	public pgn2csv(String[] args) throws IOException
	{
		this();
		parseArgs(args);
	}

	public void addInputFile(File inputFile)
	{
		inputFiles.add(inputFile);
	}

	public void addColumn(String column)
	{
		outputColumns.add(column.toUpperCase());
	}

	public void addColumns(String columnList)
	{
		StringTokenizer tok = new StringTokenizer(columnList,",",false);
		while (tok.hasMoreTokens())
			addColumn(tok.nextToken());
	}

	public void run() throws Exception
	{
		if (outputColumns.isEmpty()) throw new ParamError("output column list expected");

		if (outputFile!=null)
			output = new PrintWriter(new FileWriter(outputFile,appendOutput));
		else
			output = new PrintWriter(System.out);

		if (inputFiles.isEmpty())
		{
			//  read from std. in
			BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
			parse(buf, -1);
			buf.close();
		}
		else for (int i=0; i < inputFiles.size(); i++)
		{
			File file = (File)inputFiles.get(i);
			BufferedReader buf = new BufferedReader(new FileReader(file));
			System.err.print("[");
			System.err.print(file.getName());

			parse(buf, file.length());

			buf.close();
			System.err.println("]");
		}

		if (outputFile!=null)
			output.close();

	}

	protected void parse(Reader input, long length) throws Exception
	{
		//  parse a pgn file
		FileOutputPGNImport importTask = new FileOutputPGNImport(input, length);
		importTask.run();
	}


	public String getStringValue(GameBuffer.Row r, String tag)
	{
		if (tag.equalsIgnoreCase(PgnConstants.TAG_ANNOTATOR))
			return r.sval[GameBuffer.IANNOTATOR];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_BLACK))
			return r.sval[GameBuffer.IBLACK];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_BLACK_ELO))
			return (r.BlackELO <= 0) ? null : String.valueOf(r.BlackELO);
		if (tag.equalsIgnoreCase(PgnConstants.TAG_BLACK_TITLE))
			return r.BlackTitle;
		if (tag.equalsIgnoreCase(PgnConstants.TAG_BOARD))
			return r.Board;
		if (tag.equalsIgnoreCase(PgnConstants.TAG_DATE))
			return (r.GameDate==null) ? null : r.GameDate.toString();
		if (tag.equalsIgnoreCase(PgnConstants.TAG_ECO))
			return r.ECO;
		if (tag.equalsIgnoreCase(PgnConstants.TAG_EVENT))
			return r.sval[GameBuffer.IEVENT];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_EVENT_DATE))
			return (r.EventDate==null) ? null : r.EventDate.toString();
		if (tag.equalsIgnoreCase(PgnConstants.TAG_FEN))
			return r.FEN;
		if (tag.equalsIgnoreCase(PgnConstants.TAG_OPENING))
			return r.sval[GameBuffer.IOPEN];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_PLY_COUNT))
			return (r.PlyCount <= 0) ? null : String.valueOf(r.PlyCount);
		if (tag.equalsIgnoreCase(PgnConstants.TAG_RESULT))
			return PgnUtil.resultString(r.Result);
		if (tag.equalsIgnoreCase(PgnConstants.TAG_ROUND))
			return r.Round;
		if (tag.equalsIgnoreCase(PgnConstants.TAG_SITE))
			return r.sval[GameBuffer.ISITE];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_WHITE))
			return r.sval[GameBuffer.IWHITE];
		if (tag.equalsIgnoreCase(PgnConstants.TAG_WHITE_ELO))
			return (r.WhiteELO <= 0) ? null : String.valueOf(r.WhiteELO);
		if (tag.equalsIgnoreCase(PgnConstants.TAG_WHITE_TITLE))
			return r.WhiteTitle;
		//  else: get from "more"
		//  not efficient, but well...
		if (r.More==null) return null;
		StringTokenizer tok = new StringTokenizer(r.More.toString(), ";");
		while (tok.hasMoreTokens()) {
		    String str = tok.nextToken();
		    int k = str.indexOf("=");

		    if (k > 0) {
		        String key = str.substring(0,k).trim();
		        String value = str.substring(k+1);

			    if (key.equalsIgnoreCase(tag)) return value;
		    }
		}
		return null;
	}

	protected void printRow(GameBuffer.Row row)
	{
		int colCount = outputColumns.size();
		for (int i=0; i < colCount; i++)
		{
			String key = (String)outputColumns.get(i);
			String value;

			if (key.equalsIgnoreCase("BODY"))
				value = "?";    //  TODO not yet implemented
			else
				value = getStringValue(row,key);

			if (value != null) {
				value = StringUtil.escape(value);
				output.print(value);
			}

			if (i < (colCount-1))
				output.print(columnSeparator);
			else
				output.print(lineSeparator);
		}
	}


	protected void parseArgs(String[] args) throws IOException
	{
		for (int i=0; i<args.length; i++)
			if ("-o".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("output file name expected");
				if (outputFile!=null) throw new ParamError("output file specified twice");

				outputFile = new File(args[++i]);
				appendOutput = false;
			}
			else if ("-a".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("output file name expected");
				if (outputFile!=null) throw new ParamError("output file specified twice");

				outputFile = new File(args[++i]);
				appendOutput = true;
			}
			else if ("-c".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("output columns expected");
				addColumns(args[++i]);
			}
			else if ("-t".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("column separator expected");
				columnSeparator = StringUtil.unescape(args[++i]);
			}
			else if ("-n".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("line separator expected");
				lineSeparator = StringUtil.unescape(args[++i]);
			}
			else if (args[i].startsWith("@"))
				parseArgs(new File(args[i].substring(1)));
			else if ("-h".equalsIgnoreCase(args[i])
			        || "-help".equalsIgnoreCase(args[i]))
			{
				throw new ParamError("");
			}
			else if ("-i".equals(args[i])) {
				if ((i+1) >= args.length) throw new ParamError("input file name expected");
				addInputFile(new File(args[++i]));
			}
			else {
				addInputFile(new File(args[i]));
			}

	}

	protected void parseArgs(File argFile) throws IOException
	{
		StreamTokenizer tok = new StreamTokenizer(new FileReader(argFile));
		//  treat all characters and punctuation as chars
		//  only whitespace is separator
		tok.resetSyntax();
		tok.wordChars(33,126);
		tok.whitespaceChars(' ',' ');
		tok.whitespaceChars('\t','\t');
		tok.whitespaceChars('\n','\n');
		tok.whitespaceChars('\r','\r');

		ArrayList collect = new ArrayList();
		boolean eof = false;
		while (!eof)
			switch (tok.nextToken())
			{
			case StreamTokenizer.TT_EOF:    eof = true; break;
			case StreamTokenizer.TT_WORD:   if (tok.sval!=null) collect.add(tok.sval); break;
			}

		String[] args = (String[])collect.toArray(new String[collect.size()]);
		parseArgs(args);
	}

	public static void printHelp(PrintWriter out)
	{
		out.println("java -jar pgnparser [options] input1-file.pgn");
		out.println();
		out.println("Command Line Options:");
		out.println(" -o <file.txt>         write output to file (caution: file is clobbered without warning)");
		out.println(" -a <file.txt>         append text output to file");
		out.println(" -c <column,column>    list of PGN tags for output; use 'body' to output game body");
		out.println(" -t <char>             column separator char (default=tab)");
		out.println(" -n <char>             line separator char (default=\\n");
		out.println(" -V                    validate body (default=off)");
		out.println(" -v                    don't validate body (default=off)");
		out.println();
		out.println(" @config-file          read options from config file");
	}

	public static void printHelp()
	{
		PrintWriter pout = new PrintWriter(System.err);
		printHelp(pout);
		pout.flush();
	}

	public static void main(String[] args)
	{
		try {
			pgn2csv parser = new pgn2csv(args);
			parser.run();

		} catch (ParamError perr) {
			System.err.println("");
			System.err.println(perr.getMessage());
			System.err.println("");
			printHelp();
		} catch (IOException err) {
			System.err.println("");
			System.err.println(err.getMessage());
		} catch (Exception err) {
			System.err.println("");
			err.printStackTrace(System.err);
		}
	}
}
