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
import de.jose.Application;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.ParamStatement;
import de.jose.util.Metaphone;
import de.jose.util.StringUtil;
import de.jose.util.map.IntHashSet;
import de.jose.view.ListPanel;
import de.jose.view.input.JDateField;

import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.lang.reflect.Field;

public class SearchRecord implements Cloneable
{
    /** show system collection when no search condition is set ?
     *  default is true, because it's more efficient with MySQL
     */
//    public static final boolean SHOW_SYS_COLLECTIONS =
//            Version.getSystemProperty("jose.sys.collections",true);

	//-------------------------------------------------------------------------------
	//	Search Options
	//-------------------------------------------------------------------------------

	public static final int SEARCH_WIN			= 0x01;
	public static final int SEARCH_DRAW			= 0x02;
	public static final int SEARCH_LOSE			= 0x04;
	public static final int SEARCH_UNKNOWN		= 0x08;
	public static final int SEARCH_RESULT_MASK	= 0x0f;

	public static final int SEARCH_COLOR_SENS	= 0x10;
	public static final int SEARCH_CASE_SENS	= 0x20;
	public static final int SEARCH_SOUNDEX		= 0x40;


	private static final int JOIN_GAME			= 0x0001;
	private static final int JOIN_WHITE			= 0x0002;
	private static final int JOIN_BLACK			= 0x0004;
	private static final int JOIN_EVENT			= 0x0008;
	private static final int JOIN_SITE			= 0x0010;
	private static final int JOIN_OPENING		= 0x0020;
	private static final int JOIN_MORE			= 0x0040;
    private static final int JOIN_ANNOTATOR		= 0x0080;

	private static final int JOIN_STRAIGHT		= 0x8000;

	/**	parameter to parseDate: accept only complete dates
	 */
	public static final int EXACT				= 1;

	/**	parameter to parseDate: if date is incomplete, return the lower bound
	 * 	e.g. parseDate ("2002", LOWER_BOUND)  -->  "1.1.2002"
	 */
	public static final int LOWER_BOUND			= 2;
	/**	parameter to parseDate: if date is incomplete, return the uppper bound
	 * 	e.g. parseDate ("2002", UPPER_BOUND)  -->  "1.1.2003"
	 */
	public static final int UPPER_BOUND			= 3;

	/**
	 * index into positionSearch
	 */
	public static final int HASH_KEY_WHITE      = 0;
	public static final int HASH_KEY_BLACK      = 1;
	public static final int HASH_KEY_WHITE_REV  = 2;
	public static final int HASH_KEY_BLACK_REV  = 3;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	database adapter	*/
	public DBAdapter adapter;
	/**	currently selected collections	*/
	public IntHashSet	collections;
	/**	current sort order	*/
	public int 		sortOrder;

	/**	search field: first/white player	*/
	public String	firstPlayerName;
	/**	search field second/black player	*/
	public String	secondPlayerName;
	/**	search field: Event	*/
	public String	eventName;
	/**	search field: Site	*/
	public String	siteName;
	/**	search fields: ECO codes (from...to)	*/
	public String	eco1,eco2;
	/**	search field: Opening	*/
	public String	openingName;
    /** search field: Annotator */
    public String   annotatorName;
	/**	search field: comment text (fulltext search, if possible)	*/
	public String	commentText;
	/** search field: comment flag  */
	public boolean  flagComments;
	/** search field: variation flag  */
	public boolean  flagVariations;
	/**	search fields: Date (from...to)	*/
	public PgnDate	date1,date2;
    /** search fields: move count (from...to)   */
    public int      moveCount1,moveCount2;
	/**	bit flags	*/
	public int		options;
	/** hash keys for positional search */
	public PositionFilter posFilter = new PositionFilter();

	/**	*/
	private int		joins;
	private int		driving;

	public boolean getOption(int option)                    { return Util.anyOf(options,option); }

	public boolean setOption(int option, boolean newValue)
	{
		if (newValue==getOption(option)) return false;  //  nothing changed
		options = Util.set(options,option,newValue);
		return true;
	}

	public boolean setWin(boolean on)						{ return setOption(SEARCH_WIN,on); }
	public boolean setDraw(boolean on)						{ return setOption(SEARCH_DRAW,on); }
	public boolean setLose(boolean on)						{ return setOption(SEARCH_LOSE,on); }
	public boolean setUnknown(boolean on)					{ return setOption(SEARCH_UNKNOWN,on); }

	public boolean setColorSensitive(boolean on)			{ return setOption(SEARCH_COLOR_SENS,on); }
	public boolean setCaseSensitive(boolean on)				{ return setOption(SEARCH_CASE_SENS,on); }
	public boolean setSoundex(boolean on)					{ return setOption(SEARCH_SOUNDEX,on); }

	public boolean isResult()								{ return getOption(SEARCH_RESULT_MASK); }
	public boolean isWin()									{ return getOption(SEARCH_WIN); }
	public boolean isLose()									{ return getOption(SEARCH_LOSE); }
	public boolean isDraw()									{ return getOption(SEARCH_DRAW); }
	public boolean isUnknown()								{ return getOption(SEARCH_UNKNOWN); }

	public boolean isColorSensitive()						{ return getOption(SEARCH_COLOR_SENS); }
	public boolean isCaseSensitive()						{ return getOption(SEARCH_CASE_SENS); }
	public boolean isSoundex()								{ return getOption(SEARCH_SOUNDEX); }

	public int toggleSortOrder(int newOrder)
	{
		if (newOrder==+sortOrder)
			return -newOrder;
		else
			return +newOrder;
	}




	public Object getField(String fieldName)
	{
		try {
			Field field = this.getClass().getField(fieldName);
			return field.get(this);
		} catch (NoSuchFieldException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		}
		return null;
	}

	public boolean setField(String fieldName, Object newValue)
	{
		try {
			Field field = this.getClass().getField(fieldName);
			Object oldValue = field.get(this);

			if (Util.equals(oldValue,newValue))
				return false;   //  nothing changed
			else {
				field.set(this,newValue);
				return true;
			}
		} catch (NoSuchFieldException e) {
			Application.error(e);
		} catch (IllegalAccessException e) {
			Application.error(e);
		}
		return false;
	}


    public Object clone()
    {
        SearchRecord that = new SearchRecord();
        that.adapter = this.adapter;
        that.collections = (this.collections==null) ? null : new IntHashSet(this.collections);

        that.annotatorName = this.annotatorName;
        that.commentText = this.commentText;
        that.date1 = (this.date1==null) ? null : new PgnDate(date1);
        that.date2 = (this.date2==null) ? null : new PgnDate(date2);
        that.eco1 = this.eco1;
        that.eco2 = this.eco2;
        that.eventName = this.eventName;
        that.firstPlayerName = this.firstPlayerName;
        that.flagComments = this.flagComments;
        that.flagVariations = this.flagVariations;
        that.moveCount1 = this.moveCount1;
        that.moveCount2 = this.moveCount2;
        that.openingName = this.openingName;
        that.posFilter = (this.posFilter==null) ? null : (PositionFilter)this.posFilter.clone();
        that.siteName = this.siteName;
        that.secondPlayerName = this.secondPlayerName;

        that.options = this.options;
        that.sortOrder = this.sortOrder;
        that.joins = this.joins;
        that.driving = this.driving;
        return that;
    }

	public void clear()
	{
		firstPlayerName = null;
		secondPlayerName = null;
		eventName = null;
		siteName = null;
		eco1 = null;
		eco2 = null;
		openingName = null;

        moveCount1 = 0;
        moveCount2 = 0;

        annotatorName = null;
		commentText = null;
		flagComments = false;
		flagVariations = false;

		date1 = null;
		date2 = null;

		setWin(true);
		setDraw(true);
		setLose(true);
		setUnknown(true);

		setColorSensitive(false);
		setCaseSensitive(false);
		setSoundex(false);

		posFilter.clear();
	}

	public void finish(List errors)
	{
		if (Util.allOf(options,SEARCH_SOUNDEX)) 		options = Util.minus(options,SEARCH_CASE_SENS);
		//	soundex -> not case sensitive

		firstPlayerName = makeSearchText(firstPlayerName);
		secondPlayerName = makeSearchText(secondPlayerName);
		eventName = makeSearchText(eventName);
		siteName = makeSearchText(siteName);
		eco1 = makeSearchText(eco1,3);
		eco2 = makeSearchText(eco2,3);
		openingName = makeSearchText(openingName);
        annotatorName = makeSearchText(annotatorName);
		if (commentText != null) {
			commentText = commentText.trim();
			if (commentText.length()==0) commentText = null;
		}

		/**	check plausability	*/
		if (eco2!=null && eco2.length() > 0) {
			//  range search
			eco1 = trimEcoRange(eco1,errors,true);
			eco2 = trimEcoRange(eco2,errors,false);
		}
		else {
			//  like search
			eco1 = trimEcoLike(eco1,errors);
			eco2 = null;
		}

		//	check date plausability
		if (date1!=null && date2!=null && date1.after(date2)) {
			if (errors!=null) errors.add("query.error.date.too.small");
			date2 = null;
		}

		if (date2==null && date1!=null && !date1.isExact()) {
			//  range search
			date2 = date1.calcUpperBound();
		}

        //  check movecount plausibility
        if (moveCount1==Integer.MIN_VALUE)
            moveCount1 = 0;
        else if (moveCount1 < 0) {
			if (errors!=null) errors.add("query.error.movecount.too.small");
			moveCount1 = 0;
		}

        if (moveCount2==Integer.MIN_VALUE)
            moveCount2 = moveCount1;
        else if (moveCount2 < moveCount1) {
			if (errors!=null) errors.add("query.error.movecount.too.small");
			moveCount2 = moveCount1;
		}

		if (Util.allOf(options,SEARCH_RESULT_MASK))		options = Util.minus(options,SEARCH_RESULT_MASK);
		//	find all results == don't search at all
		if (firstPlayerName==null && secondPlayerName==null && !isResult())	options = Util.minus(options,SEARCH_COLOR_SENS);
		//	no use for color sensitive search

		if (!posFilter.isEmpty() && errors != null) {
			//	check position for plausibility
			posFilter.pos.checkLegality(errors);
			posFilter.pos.checkPlausibility(errors);
		}
	}

    /**
     *
     * @return true if there are any search conditions, but only Collections
     */
    public boolean hasFilter()
    {
		return 	hasInfoFilter() ||
				hasCommentFilter() ||
				hasPositionFilter();
    }

	public boolean hasSortOrder() {
		return sortOrder!=0;
	}

	public boolean hasInfoFilter()
	{
		return 	(firstPlayerName != null) ||
                (secondPlayerName != null) ||
                (eventName != null) ||
                (siteName != null) ||
                (eco1 != null) ||
                (eco2 != null) ||
                (openingName != null) ||
				(date1 != null) ||
				(date2 != null) ||
				(moveCount1 > 0) ||
				(moveCount2 > 0) ||
				isResult();
	}

	public boolean hasCommentFilter()
	{
		return  (annotatorName != null) ||
				(commentText != null) ||
		        flagComments || flagVariations;
	}

	public boolean hasPositionFilter()
	{
		return 	!posFilter.isEmpty();
	}

	private boolean hasUnion()
	{
		return !isColorSensitive() && (firstPlayerName != null || secondPlayerName != null);
	}

    /**
     * estimate the number of results, if available
     */
    public int estimateResults() throws Exception
    {
        if (hasFilter())
            return -1;
            /** estimating is too expensive */
        /** else:
         *  estimating is easy, just sum up the collection sizes
         */
        ParamStatement sql = new ParamStatement();
        sql.select.append("SUM(GameCount)");
        sql.from.append("Collection");

        makeCollectionFilter(sql,"Id");

        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            return sql.toPreparedStatement(conn).selectInt();
        } finally {
            JoConnection.release(conn);
        }
    }

	public ParamStatement makeIdStatement() throws SQLException
	{
		ParamStatement pstm = makeIdStatement(false);
		if (hasUnion()) {
			//  UNION with reversed Colors
			pstm.addUnion(makeIdStatement(true));
		}
		return pstm;
	}

	private ParamStatement makeIdStatement(boolean reversedColors) throws SQLException
	{
		ParamStatement sql = new ParamStatement();
//		  sql.select.append(" Game.Id");

        joins = 0;  //  JOIN_STRAIGHT; not needed if tables are analyzed regularly !
	    driving = 0;

		makeCollectionFilter(sql,"Game.CId");

		makeSearchFilter(sql,reversedColors);

        makeOrder(sql);

		if (!posFilter.isEmpty())
			joins |= JOIN_MORE;
		/**	STRAIGHT_JOIN is a hint to the MySQL "optimiser"	*/

		if (joins==0) joins = JOIN_GAME;

		appendJoins(sql,joins,driving);

		if (Util.allOf(joins,JOIN_STRAIGHT)) {
			sql.select.insert(0," STRAIGHT_JOIN ");
			joins = Util.minus(joins,JOIN_STRAIGHT);
		}
//		System.out.println(sql.toString());

		if (!posFilter.isEmpty())
			sql.select.append(", MoreGame.FEN, MoreGame.Bin");

		return sql;
	}

	public ParamStatement makeDataStatement(int offset, int len) throws SQLException
	{
		ParamStatement pstm = makeDataStatement(offset, len, false);
		if (hasUnion()) {
			//  UNION with reversed Colors
			pstm.addUnion(makeDataStatement(offset,len,true));
		}
		return pstm;
	}

	private ParamStatement makeDataStatement(int offset, int len, boolean reversedColors) throws SQLException
	{
		ParamStatement sql = new ParamStatement();
//		  sql.select.append(" Game.Id");

		sql.select.append(ListPanel.SQL_SELECT);

        joins = JOIN_GAME | JOIN_WHITE | JOIN_BLACK
		        | JOIN_EVENT | JOIN_SITE | JOIN_OPENING | JOIN_ANNOTATOR
		        | JOIN_MORE;
	    driving = 0;

		makeCollectionFilter(sql,"Game.CId");

		makeSearchFilter(sql,reversedColors);

        makeOrder(sql);

		/** hack for optimize queries ordered by Idx */
		if ((Math.abs(sortOrder)-1)==ListPanel.COL_IDX)
			joins |= JOIN_STRAIGHT;

		if (!posFilter.isEmpty())
			joins |= JOIN_MORE;
		/**	STRAIGHT_JOIN is a hint to the MySQL "optimiser"	*/

		if (joins==0) joins = JOIN_GAME;

		appendJoins(sql,joins,driving);

		if (Util.allOf(joins,JOIN_STRAIGHT)) {
			sql.select.insert(0," STRAIGHT_JOIN ");
			joins = Util.minus(joins,JOIN_STRAIGHT);
		}
//		System.out.println(sql.toString());

		if (!posFilter.isEmpty())
			sql.select.append(", MoreGame.FEN, MoreGame.Bin");

		sql.setLimit(offset,len);

		return sql;
	}


	public PositionFilter makePositionFilter() throws SQLException
	{
		if (posFilter.isEmpty())
			return PositionFilter.PASS_FILTER;
		else
			return posFilter;
	}

	public static String trimEcoLike(String eco, java.util.List errors)
	{
		if (eco==null || eco.length()==0) return null;

		StringBuffer buf = new StringBuffer(eco.toUpperCase());

		//	check length
		if (buf.length() > 3) {
			if (errors!=null) errors.add("query.error.eco.too.long");
			buf.setLength(3);
		}

		if (!checkEcoCharWildcard(buf,0, 'A','E') && errors!=null)
			errors.add("query.error.eco.character.expected");
		if (!checkEcoCharWildcard(buf,1, '0','9') && errors!=null)
			errors.add("query.error.eco.number.expected");
		if (!checkEcoCharWildcard(buf,2, '0','9') && errors!=null)
			errors.add("query.error.eco.number.expected");

		return buf.toString();
	}

	private static boolean checkEcoCharWildcard(StringBuffer buf, int i, char min, char max)
	{
		if (buf.length() <= i) {
			buf.append("_");
			return true;
		}

		char c = buf.charAt(i);
		if (c=='%')
			buf.setCharAt(i,'_');
		else if (c=='_')
			;
		else if (c < min || c > max) {
			buf.setCharAt(i,'_');
			return false;
		}
		return true;
	}

	public static String trimEcoRange(String eco, java.util.List errors, boolean isLowerBound)
	{
		if (eco==null || eco.length()==0) return null;

		StringBuffer buf = new StringBuffer(eco.toUpperCase());
		if (buf.length() > 3) {
			if (errors!=null) errors.add("query.error.eco.too.long");
			buf.setLength(3);
		}

		if (!checkEcoCharRange(buf,0, 'A','E',isLowerBound) && errors!=null)
			errors.add("query.error.eco.character.expected");
		if (!checkEcoCharRange(buf,1, '0','9',isLowerBound) && errors!=null)
			errors.add("query.error.eco.number.expected");
		if (!checkEcoCharRange(buf,2, '0','9',isLowerBound) && errors!=null)
			errors.add("query.error.eco.number.expected");

		return buf.toString();
	}

	public static boolean checkEcoCharRange(StringBuffer buf, int i, char min, char max, boolean isLowerBound)
	{
		char bound = isLowerBound ? min:max;
		if (buf.length() <= i) {
			buf.append(bound);
			return true;
		}

		char c = buf.charAt(i);
		if (c=='%' || c=='_')
			buf.setCharAt(i,bound);
		else if (c < min || c > max) {
			buf.setCharAt(i,bound);
			return false;
		}
		return true;
	}


	public static int parseInt(String text, List errors)
	{
		if (text==null || text.length()==0) return Integer.MIN_VALUE;

		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException nfex) {
			if (errors!=null) {
				errors.add("query.error.number.format");
				errors.add(" '"+text+"' ");
			}
			return 0;
		}
	}

	public static PgnDate parseDate(String text, List errors, int tolerance)
	{
		if (text==null) return null;
		text = text.trim();
		if (text.length()==0) return null;

		//	get the local date format
		SimpleDateFormat frm = (SimpleDateFormat)DateFormat.getDateInstance();
		frm.setTimeZone(Util.UTC_TIMEZONE);

		//	try incomplete date formats
		String pattern = frm.toPattern();
		//	incomplete patterns:
		String[] try_patterns;
		int[] flags;

		if (tolerance==EXACT) {
			try_patterns = new String[] {
				stripYear(pattern),						//	2-letter year (alway try this first !)
				pattern,
			};
			flags = new int[] {
				PgnDate.DATE_EXACT, PgnDate.DATE_EXACT,
			};
		}
		else {
			try_patterns = new String[] {
				stripYear(pattern),						//	2-letter year
				pattern,
				stripYear(stripFormat(pattern,"dD")),	//	without days, 2-letter year
				stripFormat(pattern,"dD"),				//	without days
				stripYear(stripFormat(pattern,"dDM")),	//	without days and months, 2-letter year
				stripFormat(pattern,"dDM"),				//	without days and months
			};
			//	offsets for upper bound
			flags = new int[] {
				PgnDate.DATE_EXACT, PgnDate.DATE_EXACT,
				PgnDate.DAY_UNKNOWN, PgnDate.DAY_UNKNOWN,
				PgnDate.DAY_MONTH_UNKNOWN, PgnDate.DAY_MONTH_UNKNOWN,
			};
		}

		for (int i=0; i < try_patterns.length; i++) {
			if (try_patterns[i]==null) continue;
			frm = new SimpleDateFormat(try_patterns[i]);
			frm.setTimeZone(Util.UTC_TIMEZONE);

			try {
				Date dt = frm.parse(text);
				PgnDate result = new PgnDate(dt,(short)flags[i]);
				if (tolerance==UPPER_BOUND && flags[i] != PgnDate.DATE_EXACT) {
					//	add offset for upper bound
					result = result.calcUpperBound();
				}
				return result;
			} catch (ParseException pex) {
				continue;
			}
		}

		//  try default PGN formats
		try {
			PgnDate result = PgnDate.parseLocalDate(text);
			if (result != null)
				return result;
		} catch (ParseException e) {

		}

		if (errors != null) {
			errors.add("query.error.date.format");
			errors.add(" '"+text+"' ");
		}
		else
			throw new JDateField.DateFormatException(text);

		return null;
	}

	protected static final String stripYear(String pattern)
	{
		int k = pattern.indexOf("yyyy");
		if (k >= 0) {
			StringBuffer buf = new StringBuffer(pattern);
			buf.delete(k,k+2);
			return buf.toString();
		}
		else
			return null;
	}

	protected static final String stripFormat(String pattern, String chars)
	{
		StringBuffer buf = new StringBuffer(pattern);
		//	delete characters
		for (int i=buf.length()-1; i>=0; i--) {
			char c = buf.charAt(i);
			if (chars.indexOf(c) >= 0) buf.deleteCharAt(i);
		}
		//	trim punctuation
		while (buf.length() > 0) {
			char c = buf.charAt(0);
			if (Character.isLetterOrDigit(c))
				break;
			else
				buf.deleteCharAt(0);
		}
		while (buf.length() > 0) {
			char c = buf.charAt(buf.length()-1);
			if (Character.isLetterOrDigit(c))
				break;
			else
				buf.deleteCharAt(buf.length()-1);
		}
		//	trim inner punctuation
		for (int i=buf.length()-2; i>=0; i--) {
			char c1 = buf.charAt(i);
			char c2 = buf.charAt(i+1);
			if (c1==c2 && !Character.isLetterOrDigit(c1))
				buf.deleteCharAt(i);
		}
		return buf.toString();
	}

    public final String makeSearchText(String text)
    {
        return makeSearchText(text,Integer.MAX_VALUE);
    }

	public String makeSearchText(String text, int maxLen)
	{
		if (text==null) return null;

		StringBuffer buf = new StringBuffer(text);

		//	trim spaces
		StringUtil.trim(buf, StringUtil.TRIM_BOTH+StringUtil.COLLAPSE_WHITESPACE);
		if (buf.length()==0) return null;

		//	soundex
		if (isSoundex()) {
			Metaphone sndx = new Metaphone(6);
			sndx.encode(buf);
		}	//	uppercase

		//	look for valid search chars
		for (int i = buf.length()-1; i >= 0; i--)
			if (Character.isLetterOrDigit(buf.charAt(i)))
				return buf.toString();

		return null;	//	search '*' == don't search at all
	}

	protected void makeSearchPatterns(String searchText,
	                                StringBuffer likePattern,
	                                StringBuffer regexPattern)
	{
		int truncate = -1;

		char c0 = (0<searchText.length()) ?  searchText.charAt(0):'\0';
		char c1;

		if (c0!='*')
			regexPattern.append('^');   //  search at word-start only !

		for (int i=0; i < searchText.length(); i++)
		{
			c1 = ((i+1) < searchText.length()) ?  searchText.charAt(i+1):'\0';

			switch (c0) {
			case '?':       likePattern.append('_');
							regexPattern.append(".?");
							break;
			case '*':       likePattern.append('%');
							regexPattern.append(".*");
							break;
			case ' ':
			case '\t':      //  truncate the like pattern here,
							//  work only on the regex pattern
							if (truncate<0) truncate = i;
							likePattern.append(c0);
							//  match any number of whitespace
							regexPattern.append(" *");
							break;
			default:
				likePattern.append(c0);
				regexPattern.append(c0);

				if (!Character.isLetterOrDigit(c0) && Character.isLetterOrDigit(c1))
				{       //  truncate the like pattern here,
						//  work only on the regex pattern
						if (truncate<0) truncate = i;
						//  allow whitespace after punctuation
						regexPattern.append(" *");
				}
				break;
			}
			c0 = c1;
		}

		c1 = (likePattern.length()>0) ?  likePattern.charAt(likePattern.length()-1):'\0';
		switch (c1) {
		case '_':
		case '%':   break;
		default:    likePattern.append('%'); break;
		}


		if (truncate==0) {
			likePattern.setLength(0);
		}
		else if (truncate > 0) {
			//  truncate like pattern
			likePattern.replace(truncate,likePattern.length(),"%");
		}
		else {
			//  regex is not needed at all
			regexPattern.setLength(0);
		}
	}


    protected void makeSearchFilter(ParamStatement sql, boolean reversedColors)
    {
	    makeEcoFilter(sql);

	    makeDateFilter(sql);

	    makeMoveCountFilter(sql);

	    makeAttributeFilter(sql);

	    makeCommentFilter(sql);

	    makeStringFilter(sql);

	    makePlayerFilter(sql,reversedColors);
    }

	private void makeCommentFilter(ParamStatement sql)
	{
		if (commentText != null) {
	        joins |= JOIN_MORE;
	        driving = JOIN_MORE;
	        appendFulltextCondition(sql,"AND","MoreGame.Comments",commentText);
	    }
	}

	private void makeAttributeFilter(ParamStatement sql) {
		int attrMask = 0;
		if (flagComments) attrMask |= Game.HAS_COMMENTS;
		if (flagVariations) attrMask |= Game.HAS_VARIATIONS;

		if (attrMask!=0) {
			joins |= JOIN_GAME;
			appendCondition(sql,"AND","(Attributes & "+attrMask+") = "+attrMask);
		}
	}

	private void makeMoveCountFilter(ParamStatement sql)
	{
		if ((moveCount1 > 0) && (moveCount2 > 0)) {
	        //  range search
	        appendOperator(sql,"AND");
	        sql.where.append(" Game.PlyCount >= ? AND Game.PlyCount <= ? ");
	        sql.addIntParameter(moveCount1*2-1);
	        sql.addIntParameter(moveCount2*2);
				joins |= JOIN_GAME;
	    }
	    else if (moveCount1 > 0) {
	        appendOperator(sql,"AND");
	        sql.where.append(" Game.PlyCount >= ? ");
	        sql.addIntParameter(moveCount1*2-1);
				joins |= JOIN_GAME;
	    }
	    else if (moveCount2 > 0) {
	        appendOperator(sql,"AND");
	        sql.where.append(" Game.PlyCount <= ? ");
	        sql.addIntParameter(moveCount2*2);
				joins |= JOIN_GAME;
	    }
	}

	private void makeStringFilter(ParamStatement sql)
	{
		if (eventName != null) {
		    joins |= JOIN_EVENT+JOIN_GAME;
		    driving = JOIN_EVENT;
		    appendStringCondition(sql,"AND","Event",eventName);
		}

		if (siteName != null) {
		    joins |= JOIN_SITE+JOIN_GAME;
		    driving = JOIN_SITE;
		    appendStringCondition(sql,"AND","Site",siteName);
		}

		if (openingName != null) {
		    joins |= JOIN_OPENING+JOIN_GAME;
		    driving = JOIN_OPENING;
		    appendStringCondition(sql,"AND","Opening",openingName);
		}

		if (annotatorName != null) {
		    joins |= JOIN_ANNOTATOR+JOIN_GAME;
		    driving = JOIN_ANNOTATOR;
		    appendStringCondition(sql,"AND","Annotator",annotatorName);
		}
	}

	private void makeDateFilter(ParamStatement sql)
	{
		if (date1!=null && date2!=null) {
	        //	range search
	        appendOperator(sql,"AND");
	        sql.where.append(" GameDate >= ? AND GameDate < ? ");
	        sql.addParameter(Types.DATE,date1);
	        sql.addParameter(Types.DATE,date2);
				joins |= JOIN_GAME;
	    } else if (date1!=null) {
	        appendOperator(sql,"AND");
	        sql.where.append(" GameDate >= ? ");
	        sql.addParameter(Types.DATE,date1);
	    } else if (date2!=null) {
	        appendOperator(sql,"AND");
	        sql.where.append(" GameDate < ? ");
	        sql.addParameter(Types.DATE,date2);
				joins |= JOIN_GAME;
	    }
	}

	private void makeEcoFilter(ParamStatement sql)
	{
		if (eco1!=null && eco2!=null) {
	        //	range search
	        //	remove wildcards (TODO)
	        appendOperator(sql,"AND");
	        sql.where.append(" ECO >= ? AND ECO <= ? ");
	        sql.addParameter(Types.VARCHAR,eco1);
	        sql.addParameter(Types.VARCHAR,eco2);
				joins |= JOIN_GAME;
	    }
	    else if (eco1!=null) {
	        //	search with wildcards
	        appendOperator(sql,"AND");
	        appendLikeClause(sql,"ECO",eco1,false);
				joins |= JOIN_GAME;
	    }
	    else if (eco2!=null) {
	        //	semi-bounded search
	        //	remove wildcards (TODO)
	        appendOperator(sql,"AND");
	        sql.where.append(" ECO <= ? ");
	        sql.addParameter(Types.VARCHAR,eco2);
				joins |= JOIN_GAME;
	    }
	}


	private void makePlayerFilter(ParamStatement sql, boolean reversedColors)
	{
		if (isColorSensitive()) {
		    if (firstPlayerName != null) {
		        joins |= JOIN_WHITE;
		        driving = JOIN_WHITE;
		        appendStringCondition(sql,"AND","White",firstPlayerName);
		    }
		    if (secondPlayerName != null) {
		        joins |= JOIN_BLACK;
		        driving = JOIN_BLACK;
		        appendStringCondition(sql,"AND","Black",secondPlayerName);
		    }
		    joins |= JOIN_GAME;
			appendResultCondition(sql,"AND","Game.Result", options, false);
		}
		else {
		    //	color insensitve search condition
		    if (firstPlayerName!=null && secondPlayerName!=null) {

			    joins |= JOIN_WHITE+JOIN_BLACK+JOIN_GAME;
			    if (reversedColors) {
				    driving = JOIN_BLACK;
				    appendStringCondition(sql,"AND","Black", firstPlayerName);
					appendStringCondition(sql,"AND","White", secondPlayerName);
			    }
			    else {
				    driving = JOIN_WHITE;
				    appendStringCondition(sql,"AND","White", firstPlayerName);
					appendStringCondition(sql,"AND","Black", secondPlayerName);
			    }

			    appendResultCondition(sql,"AND","Game.Result", options, reversedColors);
			}
		    else if (firstPlayerName!=null) {
			    if (reversedColors) {
				    joins |= JOIN_BLACK+JOIN_GAME;
				    driving = JOIN_BLACK;
				    appendStringCondition(sql,"AND","Black",firstPlayerName);
			    }
			    else {
				    joins |= JOIN_WHITE+JOIN_GAME;
				    driving = JOIN_WHITE;
				    appendStringCondition(sql,"AND","White",firstPlayerName);
			    }

			    appendResultCondition(sql,"AND","Game.Result", options, reversedColors);
			}
		    else if (secondPlayerName!=null) {
			    if (reversedColors) {
				    joins |= JOIN_WHITE+JOIN_GAME;
				    driving = JOIN_WHITE;
				    appendStringCondition(sql,"AND","White",secondPlayerName);
			    } else {
				    joins |= JOIN_BLACK+JOIN_GAME;
				    driving = JOIN_BLACK;
				    appendStringCondition(sql,"AND","Black",secondPlayerName);
			    }
			    appendResultCondition(sql,"AND","Game.Result", options, !reversedColors);
		    }
		    else {
		        if (Util.anyOf(options,SEARCH_WIN+SEARCH_LOSE))
		            appendResultCondition(sql,"AND","Game.Result", options | SEARCH_WIN|SEARCH_LOSE, false);
		        else
		            //	no need for color sensitivity
		            appendResultCondition(sql,"AND","Game.Result", options, false);
		    }
		}
	}

	protected void makeCollectionFilter(ParamStatement sql, String cidColumn) throws SQLException
	{
		/* set collection filter    */
        if (collections==null || collections.isEmpty())
        {
            //  show all but system collections (trash, clipboard, autosave)
            if (! Version.getSystemProperty("jose.sys.collections",true)) {
                IntHashSet trashedCollections = null;
                try {
                    trashedCollections = Collection.getTrashedCollections(false);
                } catch (Exception ex) {
                    throw new SQLException(ex.getMessage());
                }

                sql.where.append(cidColumn);
                sql.where.append(" > 100");
                if (! trashedCollections.isEmpty()) {
                    sql.where.append(" AND ");
                    sql.where.append(cidColumn);
                    sql.where.append(" NOT IN ");
                    trashedCollections.appendString(sql.where,"(,)");
                }
				joins |= JOIN_GAME;
            }
        }
		else
		{
            //  show selected collections
            sql.where.append(cidColumn);
			sql.where.append(" ");
			if (collections.size()==1) {
				sql.where.append(" = ");
				collections.appendString(sql.where,"");
			}
			else {
				sql.where.append(" IN ");
				collections.appendString(sql.where,"(,)");
			}
			joins |= JOIN_GAME;
		}

	}


	public boolean isCollectionSelected(int CId)
		throws Exception
	{
		if (collections==null || collections.isEmpty()) {
			//	default collections
			if (Version.getSystemProperty("jose.sys.collections",true))
				return true;		//	show all
			else if (CId < 100)
				return false;		//	don't show sys collections
			else
				return ! Collection.isInTrash(CId);	//	don't show trashed collections
		}
		else
			return collections.contains(CId);
	}

	protected void makeOrder(ParamStatement sql)
	{
		boolean has_collections = (collections!=null && !collections.isEmpty());
		boolean order_in_select = hasUnion();

		/*   set sort order */
		if (sortOrder != 0) {
			int columnIdx = Math.abs(sortOrder)-1;
			switch (columnIdx) {
			case ListPanel.COL_IDX:
							appendOrderClause("Game.CId", sortOrder, sql, order_in_select);
							appendOrderClause("Game.Idx", sortOrder, sql, order_in_select);
							joins |= JOIN_GAME;
							break;

			case ListPanel.COL_WNAME:
							joins |= JOIN_WHITE+JOIN_GAME;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_WHITE;
							appendOrderClause("White.Name", sortOrder, sql, order_in_select);
							break;
			case ListPanel.COL_WTITLE:
                            joins |= JOIN_MORE;
                            if (driving==0)
                                driving = JOIN_MORE;
							appendOrderClause("MoreGame.WhiteTitle", sortOrder, sql, order_in_select);
							break;
			case ListPanel.COL_WELO:
							joins |= JOIN_GAME;
							appendOrderClause("Game.WhiteELO", sortOrder, sql, order_in_select);
							appendOrderClause("Game.BlackELO", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_BNAME:
							joins |= JOIN_BLACK+JOIN_GAME;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_BLACK;
							appendOrderClause("Black.Name", sortOrder, sql, order_in_select);
							break;
			case ListPanel.COL_BTITLE:
                            joins |= JOIN_MORE;
                            if (driving==0)
                                driving = JOIN_MORE;
							appendOrderClause("MoreGame.BlackTitle", sortOrder, sql, order_in_select);
							break;
			case ListPanel.COL_BELO:
							joins |= JOIN_GAME;
							appendOrderClause("Game.BlackELO", sortOrder, sql, order_in_select);
							appendOrderClause("Game.WhiteELO", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_RESULT:
							joins |= JOIN_GAME;
							appendOrderClause("Game.Result", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_EVENT:
							joins |= JOIN_EVENT+JOIN_GAME;
							if (driving==0)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_EVENT;
							appendOrderClause("Event.Name", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_ROUND:
                            joins |= JOIN_MORE;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_MORE;
							appendOrderClause("MoreGame.Round", sortOrder, sql, order_in_select);
//							appendOrderClause("MoreGame.Board", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_BOARD:
                            joins |= JOIN_MORE;
                            if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_MORE;
							appendOrderClause("MoreGame.Board", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_SITE:
							joins |= JOIN_SITE+JOIN_GAME;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_SITE;
							appendOrderClause("Site.Name", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_DATE:
							joins |= JOIN_GAME;
							appendOrderClause("Game.GameDate", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_ECO:
							joins |= JOIN_GAME;
							appendOrderClause("Game.ECO", sortOrder, sql, order_in_select);
							break;

			case ListPanel.COL_OPENING:
							joins |= JOIN_OPENING+JOIN_GAME;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_OPENING;
							appendOrderClause("Opening.Name", sortOrder, sql, order_in_select);
							break;

            case ListPanel.COL_ANNOTATOR:
                            joins |= JOIN_ANNOTATOR;
							if (driving==0) // && !has_collections)	//	be careful about straight joins if the join order is unclear
								driving = JOIN_ANNOTATOR;
                            appendOrderClause("Annotator.Name", sortOrder, sql, order_in_select);
                            break;

			case ListPanel.COL_MOVECOUNT:
							joins |= JOIN_GAME;
							appendOrderClause("Game.PlyCount", sortOrder, sql, order_in_select);
							break;

			default:
					throw new RuntimeException("illegal sort order "+columnIdx);
			}
		}
	}

	protected void appendOrderClause(String column, int direction,
	                                 ParamStatement sql, boolean order_in_select)
	{
		String columnAlias = column;

		if (order_in_select) {
			columnAlias = StringUtil.replace(column,".","");
		    if (sql.select.length() > 0) sql.select.append(", ");
		    sql.select.append(column);
			sql.select.append(" AS ");
			sql.select.append(columnAlias);
		}

	    if (sql.order.length()>0)
	        sql.order.append(", ");

	    sql.order.append(columnAlias);
	    if (direction < 0)
	        sql.order.append(" DESC");
	    else
	        sql.order.append(" ASC");

	}

	protected void appendCondition(ParamStatement sql, String operator, String condition)
	{
		appendOperator(sql,operator);
		sql.where.append(condition);
	}

	protected void appendStringCondition(ParamStatement sql,
	                                     String operator, String table,
	                                     String pattern)
	{
		appendOperator(sql,operator);

		if (isSoundex()) {
			appendLikeClause(sql,table+".Soundex", pattern, false);
			//	TODO handle wildcards in soundex patterns; how ?
		}
//		else if (isCaseSensitive()) {
//			appendLikeClause(sql,table+".CaseSensName", pattern, true);
//		}
		else {
			//  if there is Whitespace, or punctuation, use Regex
			//  otherwise use LIKE (it's faster, anyway)
			StringBuffer likePattern = new StringBuffer();
			StringBuffer regexPattern = new StringBuffer();

			makeSearchPatterns(pattern, likePattern,regexPattern);

			if ((regexPattern.length() > 0) || isCaseSensitive())
			{
				//  use LIKE pattern to narrow down search. It's more efficient than RLIKE
				sql.where.append(" (");
				appendLikeClause(sql,table+".Name",likePattern.toString(),false);
				appendOperator(sql,"AND");
				if (regexPattern.length() > 0)
					appendRegexClause(sql,table+".Name", regexPattern.toString(), isCaseSensitive());
				else
					appendLikeClause(sql,table+".Name", likePattern.toString(), isCaseSensitive());
				sql.where.append(") ");
			}
			else
				appendLikeClause(sql,table+".Name",likePattern.toString(),false);
		}
	}

	protected void appendFulltextCondition(ParamStatement sql, String operator, String column, String pattern)
	{
		appendOperator(sql,operator);
		if (adapter.canFulltextIndex()) {
			/**	MySQL fulltext query
			 * 	MATCH (column) AGAINST (expression)
			 * */
			sql.where.append("MATCH (");
			sql.where.append(column);
			sql.where.append(") AGAINST (?)");
			sql.addParameter(Types.VARCHAR,pattern);
		}
		else {
			/**	fallback: LIKE */
			appendLikeClause(sql,column,"%"+pattern+"%",false);
		}
	}

	protected void appendLikeClause(ParamStatement sql, String column, String pattern, boolean caseSensitive)
	{
		if (caseSensitive) sql.where.append(" BINARY ");
		sql.where.append(column);
		sql.where.append(" LIKE ");
		if (caseSensitive) sql.where.append(" BINARY ");
		sql.where.append(" ? ");
		sql.addParameter(Types.VARCHAR,pattern);
	}

	protected void appendRegexClause(ParamStatement sql, String column, String pattern, boolean caseSensitive)
	{
		if (caseSensitive) sql.where.append(" BINARY ");
		sql.where.append(column);
		sql.where.append(" RLIKE ");
		if (caseSensitive) sql.where.append(" BINARY ");
		sql.where.append(" ? ");
		sql.addParameter(Types.VARCHAR,pattern);
	}

	protected boolean appendResultCondition(ParamStatement sql, String operator, String column,
	                                     int flags, boolean reverseColors)
	{
		flags = flags & SEARCH_RESULT_MASK;
		if (flags==0) return false;
		if (flags==SEARCH_RESULT_MASK) return false;	//	search all = search any

		if (reverseColors) {
			boolean win = Util.allOf(flags,SEARCH_WIN);
			boolean lose = Util.allOf(flags,SEARCH_LOSE);
			flags = Util.minus(flags, SEARCH_WIN | SEARCH_LOSE);
			flags = Util.set(flags,SEARCH_WIN,lose);
			flags = Util.set(flags,SEARCH_LOSE,win);
		}

		joins |= JOIN_GAME;
		appendOperator(sql,operator);
		sql.where.append(column);
/*
		public static final int RESULT_UNKNOWN		= -1;
		public static final int BLACK_WINS			=  0;
		public static final int DRAW				=  1;
		public static final int WHITE_WINS			=  2;
*/
		switch (flags) {
		case SEARCH_WIN:		//	2
			sql.where.append(" = ");
			sql.where.append(PgnConstants.WHITE_WINS);
			break;
		case SEARCH_LOSE:		//	0
			sql.where.append(" = ");
			sql.where.append(PgnConstants.BLACK_WINS);
			break;
		case SEARCH_DRAW:		//	1
			sql.where.append(" = ");
			sql.where.append(PgnConstants.DRAW);
			break;
		case SEARCH_UNKNOWN:	//	-1
			sql.where.append(" = ");
			sql.where.append(PgnConstants.RESULT_UNKNOWN);
			break;

		case SEARCH_WIN+SEARCH_LOSE:	//	2,0
			sql.where.append(" IN (");
			sql.where.append(PgnConstants.WHITE_WINS);
			sql.where.append(",");
			sql.where.append(PgnConstants.BLACK_WINS);
			sql.where.append(")");
			break;
		case SEARCH_WIN+SEARCH_DRAW:	//	2,1
			sql.where.append(" >= ");
			sql.where.append(PgnConstants.DRAW);
			break;
		case SEARCH_WIN+SEARCH_UNKNOWN:	//	2,-1
			sql.where.append(" IN (");
			sql.where.append(PgnConstants.WHITE_WINS);
			sql.where.append(",");
			sql.where.append(PgnConstants.RESULT_UNKNOWN);
			sql.where.append(")");
			break;

		case SEARCH_LOSE+SEARCH_DRAW:	//	0,1
			sql.where.append(" IN (");
			sql.where.append(PgnConstants.BLACK_WINS);
			sql.where.append(",");
			sql.where.append(PgnConstants.DRAW);
			sql.where.append(")");
			break;
		case SEARCH_LOSE+SEARCH_UNKNOWN:	//	0,-1
			sql.where.append(" <= ");
			sql.where.append(PgnConstants.BLACK_WINS);
			break;

		case SEARCH_DRAW+SEARCH_UNKNOWN:	//	1,-1
			sql.where.append(" IN (");
			sql.where.append(PgnConstants.RESULT_UNKNOWN);
			sql.where.append(",");
			sql.where.append(PgnConstants.DRAW);
			sql.where.append(")");
			break;

		case SEARCH_WIN+SEARCH_LOSE+SEARCH_DRAW:	//	2,1,0
			sql.where.append(" >= ");
			sql.where.append(PgnConstants.BLACK_WINS);
			break;
		case SEARCH_WIN+SEARCH_LOSE+SEARCH_UNKNOWN:	//	2,0,-1
			sql.where.append(" != ");
			sql.where.append(PgnConstants.DRAW);
			break;
		case SEARCH_WIN+SEARCH_DRAW+SEARCH_UNKNOWN:	//	2,1,-1
			sql.where.append(" != ");
			sql.where.append(PgnConstants.BLACK_WINS);
			break;
		case SEARCH_LOSE+SEARCH_DRAW+SEARCH_UNKNOWN:	//	0,1,-1
			sql.where.append(" <= ");
			sql.where.append(PgnConstants.DRAW);
			break;

		default:
			throw new IllegalArgumentException();
		}
		return true;
	}

	protected boolean appendOperator(ParamStatement sql, String operator)
	{
		if (operator != null && sql.where.length() > 0) {
			sql.where.append(" ");
			sql.where.append(operator);
			sql.where.append(" ");
			return true;
		}
		else
			return false;
	}

	protected void appendJoin(ParamStatement sql, String table, boolean more)
	{
	    if (more) sql.from.append(", ");
	    sql.from.append(table);

		appendOperator(sql,"AND");
	    sql.where.append(" (");

	    int k = table.lastIndexOf(' ');
	    if (k >= 0)
	        table = table.substring(k+1);       //  alias

	    sql.where.append("Game.");
	    sql.where.append(table);
	    sql.where.append("Id = ");
	    sql.where.append(table);
	    sql.where.append(".Id) ");
	}

	protected void appendJoin(ParamStatement sql, String table, String condition, boolean more)
	{
	    if (more) sql.from.append(", ");
	    sql.from.append(table);

		if (condition != null) {
			appendOperator(sql,"AND");
			sql.where.append(" (");
			sql.where.append(condition);
			sql.where.append(") ");
		}
	}

	protected void appendJoins(ParamStatement sql, int joins, int driving)
	{
		/**	unfortunately, the MySQL "optimiser" is not very smart about
		 * 	finding the best join order; better give it some hints...
		 */
		int oldlen = sql.select.length();
		if (Util.anyOf(joins,JOIN_GAME)) {
			if (driving==0) driving = JOIN_GAME;
			sql.select.insert(0,"Game.Id");
		}
		else if (Util.anyOf(joins,JOIN_MORE)) {
			if (driving==0) driving = JOIN_MORE;
			sql.select.insert(0,"MoreGame.GId");
		}
		else
			throw new IllegalArgumentException();

		if (oldlen > 0) sql.select.insert(sql.select.length()-oldlen,", ");

		appendJoins(sql, driving, joins, false);
		appendJoins(sql, Util.minus(joins,driving), joins, true);
	}

	protected void appendJoins(ParamStatement sql, int add_joins, int all_joins, boolean more)
	{
		if (Util.anyOf(add_joins,JOIN_GAME))			appendJoin(sql, "Game", null, more);

	    if (Util.anyOf(add_joins,JOIN_WHITE))   		appendJoin(sql, "Player White", more);
	    if (Util.anyOf(add_joins,JOIN_BLACK))   		appendJoin(sql, "Player Black", more);

	    if (Util.anyOf(add_joins,JOIN_EVENT))   		appendJoin(sql, "Event", more);
	    if (Util.anyOf(add_joins,JOIN_SITE))    		appendJoin(sql, "Site", more);
	    if (Util.anyOf(add_joins,JOIN_OPENING)) 		appendJoin(sql, "Opening", more);
        if (Util.anyOf(add_joins,JOIN_ANNOTATOR)) 		appendJoin(sql, "Player Annotator", more);

	    if (Util.anyOf(add_joins,JOIN_MORE))
	    {
		    if (Util.anyOf(all_joins,JOIN_GAME))
		        appendJoin(sql, "MoreGame", "Game.Id = MoreGame.GId", more);
		    else
		        appendJoin(sql, "MoreGame", null, more);    //  when would this happen ? is there a risk of having an unclosed join ?
	    }
	}

}
