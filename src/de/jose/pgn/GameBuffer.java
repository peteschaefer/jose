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

package de.jose.pgn;

import de.jose.Application;
import de.jose.Version;
import de.jose.chess.BinaryConstants;
import de.jose.chess.Position;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.util.CharUtil;
import de.jose.util.Metaphone;
import de.jose.util.StringUtil;
import de.jose.util.map.ObjHashSet;
import de.jose.util.map.ObjIntMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;


public class GameBuffer
		implements PgnConstants, BinaryConstants
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	Trim Whitespace from quoted PGN fields,
	 * 	like [White " Erlbach, Gerhard "]
	  */
	protected static final boolean TRIM_QUOTED_VALUES = true;

	//-------------------------------------------------------------------------------
	//	Database Fields
	//-------------------------------------------------------------------------------

	/**	if true, strings are unique per collection
	 *	if false, strings are unique across the whole database
	 *	(may save space, but requires additional effort)
	 *
	 *	the question is: how likely are duplicate names in separate PGN files ?
	 *	is it worth filtering the identical names, or do we accept the waste of some disk space ?
	 */
//	protected static final boolean STRICT_CHARS	= Version.getSystemProperty("jose.unique.strings",true);

	public static class Row {
		/** Game Fields */
		public int Id;
		public int CId;
		public int Idx;
        public int Attributes;
		public int PlyCount;
		public byte Result;
		public int WhiteELO;
		public int BlackELO;
		public String WhiteTitle;
		public String BlackTitle;
		public PgnDate GameDate;
		public PgnDate EventDate;
		public short DateFlags;
		public String Round;
		public String Board;
		public String FEN;
		public String ECO;
		/** MoreGame Fields     */
		public StringBuffer More;
		public int binLen;
        public int commentsLen;
		//	two buffers for GameText: one used by preparedStatement, one by reader thread
		private byte[][] flipflopBin = new byte[2][512];
        private byte[][] flipflopComments = new byte[2][512];

		public byte[] Bin = flipflopBin[0];
        public byte[] Comments = flipflopComments[0];

		//	string ids
		public int[] sid = new int[6];
		//	temporary String values
		public String[] sval = new String[6];

		public final void flip()		{
			if (Bin==flipflopBin[0])
				Bin = flipflopBin[1];
			else
				Bin = flipflopBin[0];
            if (Comments==flipflopComments[0])
                Comments = flipflopComments[1];
            else
                Comments = flipflopComments[0];
		}

		public final void allocateBin(int size) {
			size = ((size+255) / 256) * 256;
            if (Bin==null || Bin.length < size) {
                if (Bin==flipflopBin[0])
                    Bin = flipflopBin[0] = new byte[size];
                else
                    Bin = flipflopBin[1] = new byte[size];
            }
		}
        public final void allocateComments(int size) {
            size = ((size+255) / 256) * 256;
            if (Comments==null || Comments.length < size) {
                if (Comments==flipflopBin[0])
                    Comments = flipflopComments[0] = new byte[size];
                else
                    Comments = flipflopComments[1] = new byte[size];
            }
        }
	}

	public static final int IWHITE = 0;
	public static final int IBLACK = 1;
	public static final int IEVENT = 2;
	public static final int ISITE  = 3;
	public static final int IOPEN  = 4;
    public static final int IANNOTATOR  = 5;

	public static final int IPLAYER = 1;

	/**	results from parseTag	 */
	public static final int EMPTY_LINE	 = 1;
	public static final int TAG_LINE	 = 2;
	public static final int TEXT_LINE	 = 3;

	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	public static final String SQL_INSERT_1 =
//          %DELAYED% ?
		"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		" EventId,SiteId,GameDate,EventDate,DateFlags,AnnotatorId,ECO,OpeningId) "+
		" VALUES ";
	public static final int COUNT_VALUES_1 = 18;

	public static final String SQL_INSERT_2 =
//          %DELAYED% ?
		"INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
		" VALUES ";
	public static final int COUNT_VALUES_2 = 9;


	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	the database connection	 */
	protected JoConnection conn;
	/**	Collection Id	 */
	protected int CId;
	/**	game index	 */
	protected int gameIdx;
	/**	the row buffer	 */
	protected Row[] buffer;
	/**	current fill size	 */
	protected int fill;
	/**	current insert row	 */
	public Row row;
	/**	Soundex encoder	 */
	public Metaphone sndx;
	/** total number of inserted rows   */
	public int totalRows;
	public int nextFlush;
	/**	 range of consumed Game.Id (Sequence) **/
	public int seqFirst=0,seqLast=0;

	/**	String maps (String -> Integer)	 */
	protected ObjIntMap[] smap;
	/**	set of unresolved strings	 */
	protected ObjHashSet[] unresolved;

	/**	game parser & position	 */
	protected Position position;
	protected Parser parser;

	//-------------------------------------------------------------------------------
	//	Ctor
	//-------------------------------------------------------------------------------

	public GameBuffer(JoConnection co, int CollectionId, int gameIndex, int size)
		throws Exception
	{
		gameIdx = gameIndex;
		conn = co;
		CId = CollectionId;

		buffer = new Row[size];

		buffer[0] = new Row();
		flush(null);	//	will initialize the first row

		smap = new ObjIntMap[6];
		unresolved = new ObjHashSet[6];

		for (int i=1; i<smap.length; i++) {
			smap[i] = new ObjIntMap(1024,0.8f);
			unresolved[i] = new ObjHashSet(128,0.8f);
		}
		sndx = new Metaphone(6);

		position = new Position();
		parser = new Parser(position,0, false);
		totalRows = 0;
		nextFlush = 0;
	}

	/**	@return true if the buffer is full
	 */
	public boolean isFull()		{ return fill >= buffer.length; }
	public boolean isEmpty()	{ return fill == 0; }

	public Row getRow(int offset)   { return buffer[offset]; }

	public void flush(Thread reader)
		throws SQLException
	{
		synchronized (buffer) {
			totalRows += fill;
			fill = 0;
			row = buffer[0];
			initRow(row);

			if (reader!=null)
				reader.interrupt();		//	signal reader to fill the buffer
		}
	}

	public void setGameText(Row r, char[] chars, int start, int end)
	{
		int charlen = end-start;
		if (charlen > r.Bin.length)
			r.allocateBin(charlen);
        if (charlen > r.Comments.length)
            r.allocateComments(charlen);
        /*	binary must not be larger than original text */

		parser.setPosition(r.FEN);

		if (!parser.isClassic())
			r.Attributes |= Game.IS_FRC;

		parser.setStrictlyLegal(false);
        parser.parse(chars, start, charlen, r.Bin,0, r.Comments,0,true);
		if (parser.hasErrors) {
			parser.setStrictlyLegal(true);
	        parser.parse(chars, start, charlen, r.Bin,0, r.Comments,0,true);
		}

        r.binLen = parser.getBinLength();
        r.commentsLen = parser.getCommentsLength();

        r.Bin[r.binLen++] = (byte)SHORT_END_OF_DATA;
        r.PlyCount = parser.pos.ply();

        if (parser.hasVariations())
            r.Attributes |= Game.HAS_VARIATIONS;
        if (parser.hasComments())
            r.Attributes |= Game.HAS_COMMENTS;
        if (parser.hasErrors())
            r.Attributes |= Game.HAS_ERRORS;

		if (parser.hasResult()) {
			if (r.Result==RESULT_UNKNOWN)
				r.Result = (byte)parser.getResult();
			else if (r.Result != parser.getResult())
				;	//	what now ? "warning, Result Tag does not game text" ?
		}
    }


	/**	add the current row to the batch
	 *	@return true if the buffer is full
	 */
	public boolean addBatch()
		throws SQLException
	{
		synchronized (buffer) {
			if (isFull())
				throw new IllegalStateException("buffer is full");

			commitRow(row);
			fill++;

			if (isFull()) {
				row = null;
			}
			else {
				if (buffer[fill]==null)
					buffer[fill] = new Row();
				row = buffer[fill];
				initRow(row);
			}
			return isFull();
		}
	}

	protected void commitRow(Row r)
	{
		resolveStrings(r);
	}

	protected void resolveStrings(Row r)
	{
		resolveString(r, IWHITE, IPLAYER);
		resolveString(r, IBLACK, IPLAYER);
		resolveString(r, IEVENT, IEVENT);
		resolveString(r, ISITE,  ISITE);
		resolveString(r, IOPEN,  IOPEN);
        resolveString(r, IANNOTATOR,  IANNOTATOR);
	}

	protected void resolveString(Row r, int ridx, int midx)
	{
		String name = r.sval[ridx];
		if (name==null)
			return;		//	not set, fine

		int id = smap[midx].getInt(name);
		if (id!=ObjIntMap.NOT_FOUND) {
			//	String is resolved
			r.sid[ridx] = id;
			r.sval[ridx] = null;
		}
		else {
			//	String will be resolved later
			unresolved[midx].add(name);
		}
	}

	protected void initRow(Row r)
		throws SQLException
	{
		if (conn!=null)
			r.Id = getSequence(conn);
		else
			r.Id = 0;
		r.CId = CId;
		r.Idx = gameIdx++;
		r.Attributes = 0;
		//	reset all other fields
		r.PlyCount = 0;
		r.Result = RESULT_UNKNOWN;
		r.WhiteELO = 0;
		r.BlackELO = 0;
		r.WhiteTitle = null;
		r.BlackTitle = null;
		r.GameDate = null;
		r.EventDate = null;
		r.DateFlags = 0;
		r.Round = null;
		r.Board = null;
		r.FEN = null;
		r.ECO = null;
		r.More = null;
		r.binLen = 0;
		r.commentsLen = 0;

		for (int i=0; i<r.sid.length; i++) {
			r.sid[i] = 0;
			r.sval[i] = null;
		}

	}

	protected int getSequence(JoConnection conn) throws SQLException
	{
		int GId = Game.getSequence(conn);
		if (seqFirst==0) seqFirst=GId;
		seqLast=GId;
		return GId;
	}

	protected void setParameters(Row r,
	                             JoPreparedStatement pstm1, int p1,
	                             JoPreparedStatement pstm2, int p2,
	                             boolean copyBytes)
		throws SQLException
	{
/*
		"INSERT INTO Game (Id,CId,Idx,Attributes,PlyCount,Result,WhiteId,BlackId,WhiteELO,BlackELO,"+
		" EventId,SiteId,GameDate,EventDate,DateFlags,AnnotatorId,ECO,OpeningId) "+
		" VALUES ";
*/
		if (r.Id==0) r.Id = getSequence(conn);
		pstm1.setInt			(p1++, r.Id);
		pstm1.setInt			(p1++, r.CId);
		pstm1.setInt			(p1++, r.Idx);
        pstm1.setInt            (p1++, r.Attributes);
		pstm1.setInt			(p1++, r.PlyCount);
		pstm1.setInt			(p1++, r.Result);
		pstm1.setInt			(p1++, r.sid[IWHITE]);
		pstm1.setInt			(p1++, r.sid[IBLACK]);
		pstm1.setInt			(p1++, r.WhiteELO);
		pstm1.setInt			(p1++, r.BlackELO);
		pstm1.setInt			(p1++, r.sid[IEVENT]);
		pstm1.setInt			(p1++, r.sid[ISITE]);
		pstm1.setDate			(p1++, r.GameDate);
		pstm1.setDate			(p1++, r.EventDate);
		pstm1.setInt			(p1++, r.DateFlags);
        pstm1.setInt            (p1++, r.sid[IANNOTATOR]);
		pstm1.setFixedString	(p1++, r.ECO);
		pstm1.setInt			(p1++, r.sid[IOPEN]);
/*
		"INSERT INTO MoreGame (GId,WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments) "+
		" VALUES ";
*/		pstm2.setInt			(p2++, r.Id);
        pstm2.setString			(p2++, r.WhiteTitle);
        pstm2.setString			(p2++, r.WhiteTitle);
        pstm2.setString			(p2++, r.Round);
        pstm2.setString			(p2++, r.Board);
        pstm2.setString			(p2++, r.FEN);
		pstm2.setString			(p2++, r.More);

        if (r.binLen==0)
            pstm2.setNull           (p2++, Types.LONGVARBINARY);
        else if (copyBytes)
			pstm2.setBytes		    (p2++, r.Bin, 0,r.binLen);
		else
			pstm2.setBinaryStream   (p2++, r.Bin, 0,r.binLen);

        if (r.commentsLen==0)
            pstm2.setNull           (p2++, Types.LONGVARCHAR);
        else if (copyBytes)
            pstm2.setBytes		    (p2++, r.Comments, 0, r.commentsLen);
        else
            pstm2.setBinaryStream   (p2++, r.Comments, 0, r.commentsLen);

        r.flip();
	}

	public void update(Thread reader)
		throws SQLException
	{
		synchronized (this) {
			if (isEmpty())
				return;		//	that was easy ;-)

			if (totalRows >= nextFlush) {
				//  try to release some MySQL buffer memory (does it have any effect ??)
				JoConnection.getAdapter().flushResources(conn);
				nextFlush += 50000;
			}

			//	resolve strings
			resolve(IPLAYER, "Player");
			resolve(IEVENT, "Event");
			resolve(ISITE, "Site");
			resolve(IOPEN, "Opening");
            resolve(IANNOTATOR, "Player");

			//	fill in resolved strings
			for (int j=0; j<fill; j++)
				resolveStrings(buffer[j]);

			//	now update the Game table
			StringBuffer sql1 = new StringBuffer(SQL_INSERT_1);		//	INSERT INTO GAME (...) VALUES
			StringBuffer sql2 = new StringBuffer(SQL_INSERT_2);		//	INSERT INTO MOREGAME (...) VALUES

			JoPreparedStatement pstm1 = null;
			JoPreparedStatement pstm2 = null;

			if (JoConnection.getAdapter().canInsertMultiRow()) {
				for (int j=0; j<fill; j++) {
					if (j>0) sql1.append(",");
					sql1.append("(");
					StringUtil.appendParams(sql1, COUNT_VALUES_1);
					sql1.append(")");

					if (j>0) sql2.append(",");
					sql2.append("(");
					StringUtil.appendParams(sql2, COUNT_VALUES_2);
					sql2.append(")");
				}

				pstm1 = conn.getPreparedStatement(sql1.toString());
				pstm2 = conn.getPreparedStatement(sql2.toString());

				//	fill in parameters
				for (int j=0; j<fill; j++) {
					System.err.println("insert id="+buffer[j].Id);
					setParameters(buffer[j],
							pstm1, 1 + j * COUNT_VALUES_1,
							pstm2, 1 + j * COUNT_VALUES_2,
							true);
				}
				flush(reader);

				//	let's do it
//Object mark = de.jose.devtools.Profiler.set("data");
				pstm1.execute();
				pstm2.execute();
//de.jose.devtools.Profiler.printMax(mark);
			}
			else {
				sql1.append("(");
				StringUtil.appendParams(sql1, COUNT_VALUES_1);
				sql1.append(")");

				sql2.append("(");
				StringUtil.appendParams(sql2, COUNT_VALUES_2);
				sql2.append(")");

				pstm1 = conn.getPreparedStatement(sql1.toString());
				pstm2 = conn.getPreparedStatement(sql2.toString());

				for (int j=0; j<fill; j++) {
					setParameters(buffer[j],
								pstm1, 1,
								pstm2, 1,
								true);
					/*	note that binary stream parameters can't be used in batch modes
						bytes must be copied
					*/
					if (JoConnection.getAdapter().canBatchUpdate()) {
						pstm1.addBatch();
						pstm2.addBatch();
					}
					else {
						pstm1.execute();
						pstm2.execute();
					}
				}

				flush(reader);

				if (JoConnection.getAdapter().canBatchUpdate()) {
					/*int[] updateCount1 =*/ pstm1.executeBatch();
					/*int[] updateCount2 =*/ pstm2.executeBatch();
	/*				for (int i=0; i<updateCount.length; i++)
						switch (updateCount[i])
						{
						case 1:		//	fine
						case -2:		//	unknown, OK
							break;
						default:
							System.out.println("updateCount["+i+"]="+updateCount[i]); break;
						}

	*/			}
			}
		}
	}

	protected void resolve(int midx, String tableName)
		throws SQLException
	{
		ObjHashSet unres = unresolved[midx];
		ObjIntMap map = smap[midx];

		StringBuffer sql = new StringBuffer();
		JoPreparedStatement pstm = null;
		ResultSet res = null;

		//	resolve existing entries
		if (Version.getSystemProperty("jose.unique.strings",true) && !unres.isEmpty())
		{
			sql.append("SELECT Id,Name FROM ");
			sql.append(tableName);
			sql.append(" WHERE Name IN (");

            DBAdapter adapt = JoConnection.getAdapter();
            if (adapt.canParamInIn()) {
                StringUtil.appendParams(sql, unres.size());
                sql.append(")");

                pstm = conn.getPreparedStatement(sql.toString());
                Iterator j = unres.iterator();
                for (int i=1; j.hasNext(); i++)
                    pstm.setString(i, (String)j.next());
            }
            else {
                //  got to insert string literals
                Iterator j = unres.iterator();
                for (int i=1; j.hasNext(); i++) {
                    if (i > 1) sql.append(",");
                    sql.append(adapt.escapeForSQL((String)j.next()));
                }

                sql.append(")");
                pstm = conn.getPreparedStatement(sql.toString());
            }

//Object mark = de.jose.devtools.Profiler.set("select");
			pstm.execute();

			try {
                res = pstm.getResultSet();
				while (pstm.next()) {
					int id = res.getInt(1);
					String value = res.getString(2);

					unres.remove(value);
					map.put(value, id);
				}
			} catch (Throwable thrw) {
				Application.error(thrw);
			} finally {
				if (pstm!=null) pstm.closeResult();
				//	since the number of parameters is variable, it is unlikely that this
				//	statement is reused
			}
//de.jose.devtools.Profiler.printMax(mark);
		}

		insertNames(conn, tableName, unres, sndx, sql, map);
		unres.clear();

	}

	public static void insertNames(JoConnection conn, String tableName,
	                               java.util.Collection names, Metaphone sndx) throws SQLException
	{

		insertNames(conn,tableName,names,sndx, new StringBuffer(), null);
	}

	private static void insertNames(JoConnection conn, String tableName,
	                                java.util.Collection names, Metaphone sndx, StringBuffer sql,
	                                ObjIntMap map)
			throws SQLException
	{
		//	insert new entries
		if (names.isEmpty()) return;

		JoPreparedStatement pstm = null;
		try {
			sql.setLength(0);
	//                                  %DELAYED% ?
			sql.append("INSERT INTO ");
			sql.append(tableName);
			sql.append(" (Id,Name,Soundex) VALUES ");

			if (JoConnection.getAdapter().canInsertMultiRow()) {
				//	multi-row insert
				for (int i=0; i<names.size(); i++) {
					if (i>0) sql.append(",");
					sql.append("(?,?,?)");
				}

				pstm = conn.getPreparedStatement(sql.toString());
				Iterator j = names.iterator();
				for (int i=0; j.hasNext(); i++) {
					int id = conn.getSequence(tableName,"Id");
					String name = (String)j.next();
					pstm.setInt(3*i+1, id);
					pstm.setString(3*i+2, name);
					pstm.setFixedString(3*i+3, StringUtil.nvl(sndx.encode(name),"-"));

					if (map!=null) map.put(name, id);
				}
	//Object mark = de.jose.devtools.Profiler.set("insert");
				pstm.execute();
	//de.jose.devtools.Profiler.printMax(mark);
			}
			else {
				//	separate insert
				sql.append("(?,?,?)");

				pstm = conn.getPreparedStatement(sql.toString());
				Iterator j = names.iterator();
				for (int i=0; i<names.size(); i++) {
					int id = conn.getSequence(tableName,"Id");
					String name = (String)j.next();
					pstm.setInt(1, id);
					pstm.setString(2, name);
					pstm.setFixedString(3, StringUtil.nvl(sndx.encode(name),"-"));

					if (map!=null) map.put(name, id);

					if (JoConnection.getAdapter().canBatchUpdate())
						pstm.addBatch();
					else
						pstm.execute();
				}

				if (JoConnection.getAdapter().canBatchUpdate())
					pstm.executeBatch();
			}

		} finally {
			if (pstm!=null) pstm.closeResult();
			/*	since the number of parameters is variable, it is unlikely that this
				statement is reused
			*/
		}
	}


	public int parseTag(char[] line, int start, int end, int[] key, int[] value)
	{
		//	PGN escape line
		if (line[start]=='%')
			return EMPTY_LINE;
		//	trim right;
		while (end > start && Character.isWhitespace(line[end-1]))
			end--;
		if (start >= end) return EMPTY_LINE;	//	empty line

		if (line[start] != '[' || line[end-1] != ']')
			return TEXT_LINE;	//	missing brackets

		//	find key
		key[0] = key[1] = start+1;
		while (key[1] < end && Character.isLetterOrDigit(line[key[1]]))
			key[1]++;

		if (key[0] >= key[1]) return TEXT_LINE;		// no key

		//	find value
		value[0] = key[1];
		//	skip white space
		while (value[0] < end && Character.isWhitespace(line[value[0]]))
			value[0]++;

		value[1] = end-1;
		while (value[1] > value[0] && Character.isWhitespace(line[value[1]-1]))
			value[1]--;

		//	look for quoted value
		if ((value[1]-value[0]) >= 2 && line[value[0]]=='"' && line[value[1]-1]=='"') {
			value[0]++;
			value[1]--;

			if (TRIM_QUOTED_VALUES) {
				//	trim whitespace, even from quoted values
				while (value[0] < end && Character.isWhitespace(line[value[0]]))
					value[0]++;

				while (value[1] > value[0] && Character.isWhitespace(line[value[1]-1]))
					value[1]--;
			}
		}

		key[1] -= key[0];
		value[1] -= value[0];

		return TAG_LINE;
	}

	public void insertTag(char[] line, int[] key, int[] value)
	{

		try {
			/*	sorry we have to create new String objects; but there is no other way */
			if (StringUtil.equalsIgnoreCase(TAG_WHITE,line,key[0],key[1]))
				row.sval[IWHITE] = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_BLACK,line,key[0],key[1]))
				row.sval[IBLACK] = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_RESULT,line,key[0],key[1]))
				row.Result = PgnUtil.parseResult(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_WHITE_ELO,line,key[0],key[1]))
				row.WhiteELO = StringUtil.parseInt(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_BLACK_ELO,line,key[0],key[1]))
				row.BlackELO = StringUtil.parseInt(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_EVENT,line,key[0],key[1]))
				row.sval[IEVENT] = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_SITE,line,key[0],key[1]))
				row.sval[ISITE] = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_ROUND,line,key[0],key[1]))
				row.Round = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_BOARD,line,key[0],key[1]))
				row.Board = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_DATE,line,key[0],key[1]))
			{
				if (isEmpty(line,value[0],value[1]) || isEmptyDate(line,value[0],value[1])) {
					row.GameDate = null;
					row.DateFlags &= 0xFF00;
				}
				else {
					row.GameDate = PgnDate.parseDate(new String(line,value[0],value[1]));
					row.DateFlags |= row.GameDate.getDateFlags();
				}
			}
			else if (StringUtil.equalsIgnoreCase(TAG_EVENT_DATE,line,key[0],key[1]))
			{
				if (isEmpty(line,value[0],value[1]) || isEmptyDate(line,value[0],value[1])) {
					row.EventDate = null;
					row.DateFlags &= 0x00FF;
				}
				else {
					row.EventDate = PgnDate.parseDate(new String(line,value[0],value[1]));
					row.DateFlags |= (row.EventDate.getDateFlags() << 8);
				}
			}
			else if (StringUtil.equalsIgnoreCase(TAG_WHITE_TITLE,line,key[0],key[1]))
				row.WhiteTitle = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_BLACK_TITLE,line,key[0],key[1]))
				row.BlackTitle = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_FEN,line,key[0],key[1]))
				row.FEN = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_ECO,line,key[0],key[1])) {
				row.ECO = unescapeString(line,value[0], Math.min(value[1],3));
				if ((value[1] > 4) && (row.sval[IOPEN]==null))
					row.sval[IOPEN] = unescapeString(line,value[0]+4,value[1]-4);
			}
			else if (StringUtil.equalsIgnoreCase(TAG_OPENING,line,key[0],key[1]))
				row.sval[IOPEN] = unescapeString(line,value[0],value[1]);
			else if (StringUtil.equalsIgnoreCase(TAG_PLY_COUNT,line,key[0],key[1]))
				row.PlyCount = StringUtil.parseInt(line,value[0],value[1]);
            else if (StringUtil.equalsIgnoreCase(TAG_ANNOTATOR,line,key[0],key[1]))
                row.sval[IANNOTATOR] = unescapeString(line,value[0],value[1]);
			else {
				//	put into More
				if (row.More==null) row.More = new StringBuffer();
				if (row.More.length() > 0) row.More.append(";");
				row.More.append(line,key[0],key[1]);
				row.More.append("=");
				value[1] = unescapeChars(line,value[0],value[1]);
				row.More.append(line,value[0],value[1]);
			}

		} catch (ParseException pex) {
			/*	what can we do ? */
		}
	}

	public static final String unescapeString(String str)
	{
		return unescapeString(str.toCharArray(),0,str.length());
	}

	private static boolean isEmpty(char[] line, int start, int len)
	{
		return (len==0 || (len==1 && (line[start]=='?' || line[start]=='-')));
	}

	private static boolean isEmptyDate(char[] line, int start, int len)
	{
		return StringUtil.equals("????.??.??", line,start,len);
	}


	public static final String unescapeString(char[] line, int start, int len)
	{
		if (isEmpty(line,start,len)) return null;
		len = unescapeChars(line,start,len);
		return new String(line,start,len);
	}

	private static final int unescapeChars(char[] line, int start, int len)
	{
		int offset = start;
		int max = start+len-1;

		while (offset < max) {
			if (line[offset] == '\\') {
				switch (line[offset + 1]) {
					case '\\':
						if (max >= (offset + 1)) {
							System.arraycopy(line, offset + 2, line, offset + 1, max - offset - 1);
							max--;
						}
						break;
					default:
						if (max >= (offset + 2)) {
							line[offset] = CharUtil.unescape(line[offset + 1], line[offset + 2]);
							System.arraycopy(line, offset + 3, line, offset + 1, max - offset - 2);
							max -= 2;
						}
						break;
				}
			}
			else {
				//	heuristics: if two consecutive chars look like a utf-8 sequence,
				//	we assume they *are* a utf-8 sequence
				//	note: max is *inclusive*
				int ulen = CharUtil.utf8Len(line,offset,max+1-offset);
				if (ulen >= 2) {
					line[offset] = CharUtil.decodeUtf8(line,offset,ulen);
					System.arraycopy(line, offset + ulen, line, offset + 1, max+1-offset-ulen);
					max -= (ulen-1);
				}
			}
			offset++;
		}

		return max-start+1;
	}

	public static void main(String[] args)
	{
		for (int i=0; i<args.length; i++)
			System.out.println("\""+unescapeString(args[i].toCharArray(), 0,args[i].length())+"\"");
	}
}

/*
	Gr\"unfeld
	\vSi\vskovi\�c
*/