/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;

import de.jose.Util;
import de.jose.util.StringUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * extends Date
 * certain parts of the date can be undefined
 * see PgnConstants.SECOND_UNKNOWN etc.
 *
 * @author Peter Schäfer
 */
public class PgnDate
		extends java.util.Date
		implements PgnConstants
{
	//---------------------------------------------------------------------------------------
	//	Constants
	//---------------------------------------------------------------------------------------

	/**	local date format	*/
	public static final SimpleDateFormat LOCAL_DATE_FORMAT =
			(SimpleDateFormat)DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
	/**	local time format	*/
	public static final SimpleDateFormat LOCAL_TIME_FORMAT =
			(SimpleDateFormat)DateFormat.getTimeInstance(SimpleDateFormat.MEDIUM);

	/**	local formats for PGN dates	*/
	public static final SimpleDateFormat[] LOCAL_DATE_FORMATS = createDateFormats(LOCAL_DATE_FORMAT.toPattern());
	/**	local formats for PGN time	*/
	public static final SimpleDateFormat[] LOCAL_TIME_FORMATS = createTimeFormats(LOCAL_TIME_FORMAT.toPattern());

	/**	used for displaying year only ("??.??.1924" looks just a bit silly)	*/
	public static final SimpleDateFormat YEAR_ONLY_FORMAT = new SimpleDateFormat("yyyy");

	protected static Calendar CALENDAR 			= Calendar.getInstance();
	protected static StringBuffer DATE_BUFFER	= new StringBuffer("????.??.??");
	protected static StringBuffer TIME_BUFFER	= new StringBuffer("??:??:??");

	public static final PgnDate MAX_VALUE       = new PgnDate(Long.MAX_VALUE);

	static {
		YEAR_ONLY_FORMAT.setTimeZone(Util.UTC_TIMEZONE);
		LOCAL_DATE_FORMAT.setTimeZone(Util.UTC_TIMEZONE);
		LOCAL_TIME_FORMAT.setTimeZone(Util.UTC_TIMEZONE);
	}

	//---------------------------------------------------------------------------------------
	//	Fields
	//---------------------------------------------------------------------------------------

	/**	flags indicating unknown date parts	*/
	protected short dateFlags;

	//---------------------------------------------------------------------------------------
	//	Constructor
	//---------------------------------------------------------------------------------------


	public static PgnDate toPgnDate(Date value)
	{
		if (value==null)
			return null;
		else if (value instanceof PgnDate)
			return (PgnDate)value;
		else
			return new PgnDate(value);
	}

	public static PgnDate toPgnDate(Date value, short flags)
	{
		if (value==null)
			return null;
		else if (value instanceof PgnDate)
			return (PgnDate)value;
		else
			return new PgnDate(value,flags);
	}

	public PgnDate(long millis, short flags)
	{
		super(millis);
		dateFlags = flags;
	}

	public PgnDate(long millis)
	{
		this(millis,(short)0);
	}

	public PgnDate(Date dt)
	{
		this(dt,(short)0);
	}

	public PgnDate(Date dt, short flags)
	{
		setTime(dt.getTime());
		dateFlags = flags;
	}

	public final short getDateFlags()
	{
		return dateFlags;
	}

	public final boolean isDayUnknown()			{ return Util.allOf(dateFlags,DAY_UNKNOWN); }
	public final boolean isMonthUnknown()		{ return Util.allOf(dateFlags,MONTH_UNKNOWN); }
	public final boolean isYearUnknown()		{ return Util.allOf(dateFlags,YEAR_UNKNOWN); }
	public final boolean isDateUnknown()		{ return Util.allOf(dateFlags,DAY_UNKNOWN+MONTH_UNKNOWN+YEAR_UNKNOWN); }

	public final boolean isSecondUnknown()		{ return Util.allOf(dateFlags,SECOND_UNKNOWN); }
	public final boolean isMinuteUnknown()		{ return Util.allOf(dateFlags,MINUTE_UNKNOWN); }
	public final boolean isHourUnknown()		{ return Util.allOf(dateFlags,HOUR_UNKNOWN); }
	public final boolean isTimeUnknown()		{ return Util.allOf(dateFlags,SECOND_UNKNOWN+MINUTE_UNKNOWN+HOUR_UNKNOWN); }

	public final boolean isExact()              { return dateFlags==DATE_EXACT; }

	public String toString()
	{
		return toDateString();
	}


	public PgnDate calcUpperBound()
	{
		//  offset for upper bound
		int upper_offset;
		if (isYearUnknown())
			return MAX_VALUE;
		else if (isMonthUnknown())
			upper_offset = Calendar.YEAR;
		else if (isDayUnknown())
			upper_offset = Calendar.MONTH;
		else if (isHourUnknown())
			upper_offset = Calendar.DATE;
		else if (isMinuteUnknown())
			upper_offset = Calendar.HOUR;
		else if (isSecondUnknown())
			upper_offset = Calendar.MINUTE;
		else
			return new PgnDate(this, this.dateFlags);

		//	add offset for upper bound
		Calendar cal = Calendar.getInstance();
		cal.setTime(this);
		cal.add(upper_offset, +1);
		return new PgnDate(cal.getTime(), this.dateFlags);
	}


	public final String toLocalDateString(boolean shortYear)
	{
		if (Util.allOf(dateFlags, DAY_UNKNOWN+MONTH_UNKNOWN)) {
			if (Util.allOf(dateFlags,YEAR_UNKNOWN))
				return "?";
			else
				return YEAR_ONLY_FORMAT.format(this);
		}
		else
			return toDateString(LOCAL_DATE_FORMATS);
	}

	public final String toLocalTimeString()
	{
		return toTimeString(LOCAL_TIME_FORMATS);
	}


	public final String toDateString(SimpleDateFormat[] formats)
	{
		return formats[(dateFlags>>4) % formats.length].format(this);
	}

	public final String toDateString()
	{
		CALENDAR.setTime(this);
		//	yyyy.MM.dd
		if (Util.noneOf(dateFlags,YEAR_UNKNOWN)) {
			int year = CALENDAR.get(Calendar.YEAR);
			DATE_BUFFER.setCharAt(3, (char)('0'+(year%10)));
			year /= 10;
			DATE_BUFFER.setCharAt(2, (char)('0'+(year%10)));
			year /= 10;
			DATE_BUFFER.setCharAt(1, (char)('0'+(year%10)));
			year /= 10;
			DATE_BUFFER.setCharAt(0, (char)('0'+(year%10)));
		}
		else
			DATE_BUFFER.replace(0,4,"????");

		if (Util.noneOf(dateFlags,MONTH_UNKNOWN)) {
			int month = CALENDAR.get(Calendar.MONTH)-Calendar.JANUARY+1;
			DATE_BUFFER.setCharAt(6, (char)('0'+(month%10)));
			month /= 10;
			DATE_BUFFER.setCharAt(5, (char)('0'+(month%10)));
		}
		else
			DATE_BUFFER.replace(5,7,"??");

		if (Util.noneOf(dateFlags,DAY_UNKNOWN)) {
			int day = CALENDAR.get(Calendar.DAY_OF_MONTH);
			DATE_BUFFER.setCharAt(9, (char)('0'+(day%10)));
			day /= 10;
			DATE_BUFFER.setCharAt(8, (char)('0'+(day%10)));
		}
		else
			DATE_BUFFER.replace(8,10,"??");

		return DATE_BUFFER.toString();
	}


	public final String toTimeString(SimpleDateFormat[] formats)
	{
		return formats[dateFlags&0x0f].format(this);
	}

	public final String toTimeString()
	{
		CALENDAR.setTime(this);
		//	HH:mm:ss
		if (Util.noneOf(dateFlags,HOUR_UNKNOWN)) {
			int hour = CALENDAR.get(Calendar.HOUR_OF_DAY);
			TIME_BUFFER.setCharAt(1, (char)('0'+(hour%10)));
			hour /= 10;
			TIME_BUFFER.setCharAt(0, (char)('0'+(hour%10)));
		}
		else
			TIME_BUFFER.replace(0,2,"??");

		if (Util.noneOf(dateFlags,MINUTE_UNKNOWN)) {
			int minute = CALENDAR.get(Calendar.MINUTE);
			TIME_BUFFER.setCharAt(4, (char)('0'+(minute%10)));
			minute /= 10;
			TIME_BUFFER.setCharAt(3, (char)('0'+(minute%10)));
		}
		else
			TIME_BUFFER.replace(3,5,"??");

		if (Util.noneOf(dateFlags,SECOND_UNKNOWN)) {
			int seconds = CALENDAR.get(Calendar.SECOND);
			TIME_BUFFER.setCharAt(7, (char)('0'+(seconds%10)));
			seconds /= 10;
			TIME_BUFFER.setCharAt(6, (char)('0'+(seconds%10)));
		}
		else
			TIME_BUFFER.replace(6,8,"??");

		return TIME_BUFFER.toString();
	}

	//---------------------------------------------------------------------------------------
	//	Static Methods
	//---------------------------------------------------------------------------------------

	public static PgnDate parseDate(String text)
		throws ParseException
	{
		if (text.length() < 10)
			throw new ParseException("invalid date format "+text, 0);

		//	yyyy.MM.dd
		short flags=0;
		CALENDAR.clear();

		if (text.charAt(0)=='?')
			flags |= YEAR_UNKNOWN;
		else
			CALENDAR.set(Calendar.YEAR,
					(text.charAt(0)-'0') * 1000 +
					(text.charAt(1)-'0') * 100 +
					(text.charAt(2)-'0') * 10 +
					(text.charAt(3)-'0'));

		if (text.charAt(5)=='?')
			flags |= MONTH_UNKNOWN;
		else
			CALENDAR.set(Calendar.MONTH, Calendar.JANUARY-1 +
					(text.charAt(5)-'0') * 10 +
					(text.charAt(6)-'0'));

		if (text.charAt(8)=='?')
			flags |= DAY_UNKNOWN;
		else
			CALENDAR.set(Calendar.DAY_OF_MONTH,
					(text.charAt(8)-'0') * 10 +
					(text.charAt(9)-'0'));

		return new PgnDate(CALENDAR.getTimeInMillis(),flags);
	}

	public static PgnDate parseLocalDate(String text)
		throws ParseException
	{
		return parseDate(text,LOCAL_DATE_FORMATS);
	}

	public static PgnDate parseDate(String text, SimpleDateFormat[] formats)
		throws ParseException
	{
		ParseException result = null;
		for (short idx=0; idx < formats.length; idx++)
			try {
				Date dt = formats[idx].parse(text);
				return new PgnDate(dt, (short)(idx << 4));
			} catch (ParseException pex) {
				if (result==null) result = pex;
				//	continue;
			}
		throw result;
	}


	public static PgnDate parseTime(String text)
		throws ParseException
	{
		if (text.length() < 8)
			throw new ParseException("invalid time format "+text, 0);

		//	HH.mm.ss
		short flags=0;
		CALENDAR.clear();

		if (text.charAt(0)=='?')
			flags |= HOUR_UNKNOWN;
		else
			CALENDAR.set(Calendar.HOUR_OF_DAY,
					(text.charAt(0)-'0') * 10 +
					(text.charAt(1)-'0'));

		if (text.charAt(3)=='?')
			flags |= MINUTE_UNKNOWN;
		else
			CALENDAR.set(Calendar.MINUTE,
					(text.charAt(3)-'0') * 10 +
					(text.charAt(4)-'0'));

		if (text.charAt(6)=='?')
			flags |= SECOND_UNKNOWN;
		else
			CALENDAR.set(CALENDAR.SECOND,
					(text.charAt(6)-'0') * 10 +
					(text.charAt(7)-'0'));

		return new PgnDate(CALENDAR.getTime(),flags);
	}


	public static PgnDate parseLocalTime(String text)
		throws ParseException
	{
		return parseTime(text,LOCAL_TIME_FORMATS);
	}

	public static PgnDate parseTime(String text, SimpleDateFormat[] formats)
		throws ParseException
	{
		ParseException result = null;
		for (short idx=0; idx < formats.length; idx++)
			try {
				Date dt = formats[idx].parse(text);
				return new PgnDate(dt, idx);
			} catch (ParseException pex) {
				if (result!=null) result = pex;
				//	continue;
			}
		throw result;
	}


	public static SimpleDateFormat[] createDateFormats(String pattern)
	{
		SimpleDateFormat[] result = new SimpleDateFormat[8];

		result[0] = new SimpleDateFormat(pattern);
		result[1] = new SimpleDateFormat(StringUtil.replace(pattern,"dDE",'?'));		//	days unknown
		result[2] = new SimpleDateFormat(StringUtil.replace(pattern,"M",'?'));		//	month unknown
		result[3] = new SimpleDateFormat(StringUtil.replace(pattern,"MdDE",'?'));		//	days+month unknown

		result[4] = new SimpleDateFormat(StringUtil.replace(pattern,"y",'?'));
		result[5] = new SimpleDateFormat(StringUtil.replace(pattern,"ydDE",'?'));		//	days unknown
		result[6] = new SimpleDateFormat(StringUtil.replace(pattern,"yM",'?'));		//	month unknown
		result[7] = new SimpleDateFormat(StringUtil.replace(pattern,"yMdDE",'?'));		//	days+month unknown

		/**
		 * but keep UTC timezone. We don't want timezone shifts.
		 */
		for (int i=0; i<result.length; i++)
			result[i].setTimeZone(Util.UTC_TIMEZONE);

		return result;
	}

	public static SimpleDateFormat[] createTimeFormats(String pattern)
	{
		SimpleDateFormat[] result = new SimpleDateFormat[8];

		result[0] = new SimpleDateFormat(pattern);
		result[1] = new SimpleDateFormat(StringUtil.replace(pattern,"sS",'?'));		//	seconds unknown
		result[2] = new SimpleDateFormat(StringUtil.replace(pattern,"m",'?'));		//	minutes unknown
		result[3] = new SimpleDateFormat(StringUtil.replace(pattern,"msS",'?'));		//	minutes+seconds unknown

		result[4] = new SimpleDateFormat(StringUtil.replace(pattern,"hHkKa",'?'));
		result[5] = new SimpleDateFormat(StringUtil.replace(pattern,"hHkKasS",'?'));		//	seconds unknown
		result[6] = new SimpleDateFormat(StringUtil.replace(pattern,"hHkKam",'?'));		//	minutes unknown
		result[7] = new SimpleDateFormat(StringUtil.replace(pattern,"hHkKamsS",'?'));		//	minutes+seconds unknown

		/**
		 * but keep UTC timezone. We don't want timezone shifts.
		 */
		for (int i=0; i<result.length; i++)
			result[i].setTimeZone(Util.UTC_TIMEZONE);

		return result;
	}

}
