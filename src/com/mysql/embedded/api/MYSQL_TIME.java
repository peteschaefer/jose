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

package com.mysql.embedded.api;

import java.nio.ByteBuffer;

/**
 * Java wrapper for a MYSQL_TIME structure
 *
 * @author Peter Schäfer
 */

/*
	typedef struct st_mysql_time
	{
	  unsigned int  year, month, day, hour, minute, second;
	  unsigned long second_part;
	  my_bool       neg;
	  enum enum_mysql_timestamp_type time_type;
	} MYSQL_TIME;

*/

public class MYSQL_TIME
        extends StructWrapper
{
	//  ----------------------------
	//      accessors
	//  ----------------------------

	//  short MYSQL_TIME.year
	public int getYear()                          { return getInt(OFFSET_YEAR); }
	public void setYear(int year)                 { setInt(OFFSET_YEAR,year); }

	//  int MYSQL_TIME.month
	public int getMonth()                         { return getInt(OFFSET_MONTH); }
	public void setMonth(int month)               { setInt(OFFSET_MONTH,month); }

	//  int MYSQL_TIME.day
	public int getDay()                           { return getInt(OFFSET_DAY); }
	public void setDay(int day)                   { setInt(OFFSET_DAY,day); }

	//  int MYSQL_TIME.hour
	public int getHour()                          { return getInt(OFFSET_HOUR); }
	public void setHour(int hour)                 { setInt(OFFSET_HOUR,hour); }

	//  int MYSQL_TIME.minute
	public int getMinute()                        { return getInt(OFFSET_MINUTE); }
	public void setMinute(int minute)             { setInt(OFFSET_MINUTE,minute); }

	//  int MYSQL_TIME.second
	public int getSecond()                        { return getInt(OFFSET_SECOND); }
	public void setSecond(int second)             { setInt(OFFSET_SECOND,second); }

	//  int MYSQL_TIME.second_part
	public int getSecondPart()                    { return getInt(OFFSET_SECONDPART); }
	public void setSecondPart(int second_part)    { setInt(OFFSET_SECONDPART,second_part); }

	public boolean isNeg()                        { return getBoolean(offset+OFFSET_NEG); }
	public void setNeg(boolean neg)               { setBoolean(OFFSET_NEG, neg); }


	//  ----------------------------
	//      memory layout
	//  ----------------------------

	public static int SIZE;
	protected static int OFFSET_YEAR;
	protected static int OFFSET_MONTH;
	protected static int OFFSET_DAY;
	protected static int OFFSET_HOUR;
	protected static int OFFSET_MINUTE;
	protected static int OFFSET_SECOND;
	protected static int OFFSET_NEG;
	protected static int OFFSET_SECONDPART;

	protected static native int sizeof_MYSQL_TIME();
	protected static native int offset_year();
	protected static native int offset_month();
	protected static native int offset_day();
	protected static native int offset_hour();
	protected static native int offset_minute();
	protected static native int offset_second();
	protected static native int offset_neg();
	protected static native int offset_secondpart();

	static
	{
		SIZE = sizeof_MYSQL_TIME();
		OFFSET_YEAR = offset_year();
		OFFSET_MONTH = offset_month();
		OFFSET_DAY = offset_day();
		OFFSET_HOUR = offset_hour();
		OFFSET_MINUTE = offset_minute();
		OFFSET_SECOND = offset_second();
		OFFSET_NEG = offset_neg();
		OFFSET_SECONDPART = offset_secondpart();
	}

}
