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

package com.mysql.embedded.jdbc;

import com.mysql.embedded.api.enum_field_types;

import java.sql.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * maps from Mysql data types to Java types
 * and vice versa
 * @author Peter Schäfer
 */

public class MySqlTypes
        implements enum_field_types
{
	public static final int UNKNOWN = Integer.MIN_VALUE;

	public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final Calendar UTC_CALENDAR = new GregorianCalendar(UTC_TIMEZONE);
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss.SSS");
    public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	public static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

	/**
	 * note that SimpleDateFormat are NOT thread-safe
	 * before using them, make a copy for each connection !
	 */

    static {
        DATE_FORMAT.setTimeZone(UTC_TIMEZONE);
        TIME_FORMAT.setTimeZone(UTC_TIMEZONE);
        TIMESTAMP_FORMAT.setTimeZone(UTC_TIMEZONE);
	    TIMESTAMP_FORMAT.setTimeZone(UTC_TIMEZONE);
	    YEAR_FORMAT.setTimeZone(UTC_TIMEZONE);
    }

	public static int asMysqlType(int java_Type, boolean version5)
	{
		switch (java_Type)
		{
		case Types.ARRAY:           return UNKNOWN;
		case Types.BIGINT:          return MYSQL_TYPE_LONGLONG;
		case Types.BINARY:          return MYSQL_TYPE_BLOB;    //  ?
		case Types.BIT:             return version5 ? MYSQL_TYPE_BIT:MYSQL_TYPE_TINY;
		case Types.BLOB:            return MYSQL_TYPE_BLOB;
		case Types.BOOLEAN:         return MYSQL_TYPE_TINY;
		case Types.CHAR:            return MYSQL_TYPE_STRING;
		case Types.CLOB:            return MYSQL_TYPE_BLOB;
		case Types.DATALINK:        return UNKNOWN;
		case Types.DATE:            return MYSQL_TYPE_DATE;
		case Types.DECIMAL:         return MYSQL_TYPE_DECIMAL;
		case Types.DISTINCT:        return UNKNOWN;
		case Types.DOUBLE:          return MYSQL_TYPE_DOUBLE;
		case Types.FLOAT:           return MYSQL_TYPE_FLOAT;
		case Types.INTEGER:         return MYSQL_TYPE_LONG;
		case Types.JAVA_OBJECT:     return UNKNOWN;
		case Types.LONGVARBINARY:   return MYSQL_TYPE_LONG_BLOB;
		case Types.LONGVARCHAR:     return MYSQL_TYPE_LONG_BLOB;
		case Types.NULL:            return MYSQL_TYPE_NULL;
		case Types.NUMERIC:         return MYSQL_TYPE_DECIMAL;
		case Types.OTHER:           return UNKNOWN;
		case Types.REAL:            return MYSQL_TYPE_FLOAT;
		case Types.SMALLINT:        return MYSQL_TYPE_SHORT;
		case Types.STRUCT:          return UNKNOWN;
		case Types.TIME:            return MYSQL_TYPE_TIME;
		case Types.TIMESTAMP:       return MYSQL_TYPE_TIMESTAMP;
		case Types.TINYINT:         return MYSQL_TYPE_TINY;
		case Types.VARBINARY:       return MYSQL_TYPE_MEDIUM_BLOB;
		case Types.VARCHAR:         return version5 ? MYSQL_TYPE_VARCHAR:MYSQL_TYPE_VAR_STRING;
		default:                    return UNKNOWN;
		}
	}

	public static int asJavaType(int mysql_type)
	{
		switch (mysql_type)
		{
		case MYSQL_TYPE_DECIMAL:        return Types.DECIMAL;
		case MYSQL_TYPE_TINY:           return Types.TINYINT;
		case MYSQL_TYPE_SHORT:          return Types.SMALLINT;
		case MYSQL_TYPE_LONG:           return Types.INTEGER;
		case MYSQL_TYPE_FLOAT:          return Types.FLOAT;
		case MYSQL_TYPE_DOUBLE:         return Types.DOUBLE;
		case MYSQL_TYPE_NULL:           return Types.NULL;
		case MYSQL_TYPE_TIMESTAMP:      return Types.TIMESTAMP;
		case MYSQL_TYPE_LONGLONG:       return Types.BIGINT;
		case MYSQL_TYPE_INT24:          return Types.INTEGER;
		case MYSQL_TYPE_DATE:           return Types.DATE;
		case MYSQL_TYPE_TIME:           return Types.TIME;
		case MYSQL_TYPE_DATETIME:       return Types.TIMESTAMP;
		case MYSQL_TYPE_YEAR:           return Types.DATE;
		case MYSQL_TYPE_NEWDATE:        return Types.TIMESTAMP;
        case MYSQL_TYPE_VARCHAR:        return Types.VARCHAR;
        case MYSQL_TYPE_BIT:            return Types.BIT;
        case MYSQL_TYPE_NEWDECIMAL:     return Types.DECIMAL;
        case MYSQL_TYPE_ENUM:           return UNKNOWN;
		case MYSQL_TYPE_SET:            return UNKNOWN;
		case MYSQL_TYPE_TINY_BLOB:      return Types.BLOB;
		case MYSQL_TYPE_MEDIUM_BLOB:    return Types.BLOB;
		case MYSQL_TYPE_LONG_BLOB:      return Types.BLOB;
		case MYSQL_TYPE_BLOB:           return Types.BLOB;
		case MYSQL_TYPE_VAR_STRING:     return Types.VARCHAR;
		case MYSQL_TYPE_STRING:         return Types.CHAR;
		case MYSQL_TYPE_GEOMETRY:       return UNKNOWN;
		default:                    return UNKNOWN;
		}
	}

    public static int guessJavaType(Object object)
    {
        if (object==null)       return Types.NULL;

        if (object instanceof Byte)         return Types.TINYINT;
        if (object instanceof Short)        return Types.SMALLINT;
        if (object instanceof Integer)      return Types.INTEGER;
        if (object instanceof Long)         return Types.BIGINT;
        if (object instanceof Float)        return Types.FLOAT;
        if (object instanceof Double)       return Types.DOUBLE;
        if (object instanceof BigDecimal)   return Types.NUMERIC;
        if (object instanceof Number)       return Types.DECIMAL;

        if (object instanceof String)       return Types.VARCHAR;
        if (object instanceof Blob)         return Types.BLOB;
        if (object instanceof Clob)         return Types.CLOB;
        if (object instanceof byte[])       return Types.VARBINARY;
        if (object instanceof Boolean)      return Types.BIT;

        if (object instanceof java.sql.Date)        return Types.DATE;
        if (object instanceof java.sql.Time)        return Types.TIME;
        if (object instanceof java.sql.Timestamp)   return Types.TIMESTAMP;
        if (object instanceof java.util.Date)       return Types.TIMESTAMP;

        //  if all fails..
        return Types.JAVA_OBJECT;
    }

	public static String getJavaClassName(int java_type)
	{
		switch (java_type)
		{
		case Types.ARRAY:           return null;
		case Types.BIGINT:          return Long.class.getName();
		case Types.BINARY:          return Blob.class.getName();
		case Types.BIT:             return Boolean.class.getName();    //  ?
		case Types.BLOB:            return Blob.class.getName();
		case Types.BOOLEAN:         return Boolean.class.getName();
		case Types.CHAR:            return String.class.getName();
		case Types.CLOB:            return Clob.class.getName();
		case Types.DATALINK:        return null;
		case Types.DATE:            return Date.class.getName();
		case Types.DECIMAL:         return BigDecimal.class.getName();
		case Types.DISTINCT:        return null;
		case Types.DOUBLE:          return Double.class.getName();
		case Types.FLOAT:           return Float.class.getName();
		case Types.INTEGER:         return Integer.class.getName();
		case Types.JAVA_OBJECT:     return Object.class.getName();
		case Types.LONGVARBINARY:   return Blob.class.getName();
		case Types.LONGVARCHAR:     return Clob.class.getName();
		case Types.NULL:            return null;
		case Types.NUMERIC:         return BigDecimal.class.getName();
		case Types.OTHER:           return null;
		case Types.REAL:            return Float.class.getName();
		case Types.SMALLINT:        return Short.class.getName();
		case Types.STRUCT:          return null;
		case Types.TIME:            return Time.class.getName();
		case Types.TIMESTAMP:       return Timestamp.class.getName();
		case Types.TINYINT:         return Byte.class.getName();
		case Types.VARBINARY:       return Blob.class.getName();
		case Types.VARCHAR:         return String.class.getName();
		default:                    return null;
		}
	}

	public static String getMysqlTypeName(int mysql_type)
	{
		switch (mysql_type)
		{
		case MYSQL_TYPE_DECIMAL:
        case MYSQL_TYPE_NEWDECIMAL:     return "DECIMAL";
		case MYSQL_TYPE_TINY:           return "TINYINT";
		case MYSQL_TYPE_SHORT:          return "SMALLINT";
		case MYSQL_TYPE_LONG:           return "INTEGER";
		case MYSQL_TYPE_FLOAT:          return "FLOAT";
		case MYSQL_TYPE_DOUBLE:         return "DOUBLE";
		case MYSQL_TYPE_NULL:           return "NULL";
		case MYSQL_TYPE_TIMESTAMP:      return "TIMESTAMP";
		case MYSQL_TYPE_LONGLONG:       return "BIGINT";
		case MYSQL_TYPE_INT24:          return "MEDIUMINT";
		case MYSQL_TYPE_DATE:           return "DATE";
		case MYSQL_TYPE_TIME:           return "TIME";
		case MYSQL_TYPE_DATETIME:       return "DATETIME";
		case MYSQL_TYPE_YEAR:           return "YEAR";
		case MYSQL_TYPE_NEWDATE:        return "DATETIME";
		case MYSQL_TYPE_ENUM:           return "ENUM";
		case MYSQL_TYPE_SET:            return "SET";
		case MYSQL_TYPE_TINY_BLOB:      return "BLOB";
		case MYSQL_TYPE_MEDIUM_BLOB:    return "BLOB";
		case MYSQL_TYPE_LONG_BLOB:      return "BLOB";
		case MYSQL_TYPE_BLOB:           return "BLOB";
        case MYSQL_TYPE_BIT:            return "BIT";
        case MYSQL_TYPE_VAR_STRING:
        case MYSQL_TYPE_VARCHAR:        return "VARCHAR";
		case MYSQL_TYPE_STRING:         return "CHAR";
		case MYSQL_TYPE_GEOMETRY:       return null;
		default:                        return null;
		}

	}

    public static String stringValue(Object x)
    {
        if (x instanceof java.sql.Date)
            return DATE_FORMAT.format((java.sql.Date)x);
        if (x instanceof java.sql.Time)
            return TIME_FORMAT.format((java.sql.Date)x);
        if (x instanceof java.util.Date)
            return TIMESTAMP_FORMAT.format((java.util.Date)x);
        //  else:
        return x.toString();
    }

    public static long longValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).longValue();
        //  else
        return Long.parseLong(stringValue(x));
    }

    public static byte byteValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).byteValue();
        //  else
        return Byte.parseByte(stringValue(x));
    }

    public static short shortValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).shortValue();
        //  else
        return Short.parseShort(stringValue(x));
    }

    public static int intValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).intValue();
        //  else
        return Integer.parseInt(stringValue(x));
    }

    public static boolean booleanValue(Object x)
    {
        if (x instanceof Boolean)
            return ((Boolean)x).booleanValue();
        if (x instanceof Number)
            return ((Number)x).byteValue() != 0;
        //  else:
        return stringValue(x).equalsIgnoreCase("true");
    }

    public static java.sql.Date dateValue(Object x)
    {
        if (x instanceof java.sql.Date)
            return (java.sql.Date)x;
        //  else
        return new java.sql.Date(dateMillisValue(x));
    }

    public static java.sql.Time timeValue(Object x)
    {
        if (x instanceof java.sql.Time)
            return (java.sql.Time)x;
        //  else
        return new java.sql.Time(dateMillisValue(x));
    }

    public static java.sql.Timestamp timestampValue(Object x)
    {
        if (x instanceof java.sql.Timestamp)
            return (java.sql.Timestamp)x;
        //  else
        return new java.sql.Timestamp(dateMillisValue(x));
    }

    public static long dateMillisValue(Object x)
    {
        if (x instanceof java.util.Date)
            return ((java.util.Date)x).getTime();
        if (x instanceof Number)
            return ((Number)x).longValue();
        //  else: parse as string
        String s = stringValue(x);
        try {
            java.util.Date dt = TIMESTAMP_FORMAT.parse(s);
            return dt.getTime();
        } catch (ParseException e) {
        }
        try {
            java.util.Date dt = DATE_FORMAT.parse(s);
            return dt.getTime();
        } catch (ParseException e) {
        }
        try {
            java.util.Date dt = TIME_FORMAT.parse(s);
            return dt.getTime();
        } catch (ParseException e) {
        }
        throw new IllegalArgumentException(x.toString());
    }

    public static BigDecimal bigDecimalValue(Object x)
    {
        if (x instanceof BigDecimal)
            return (BigDecimal)x;
        if (x instanceof Number)
            return new BigDecimal(((Number)x).doubleValue());
        //  else
        return new BigDecimal(stringValue(x));
    }

    public static float floatValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).floatValue();
        //  else
        return Float.parseFloat(stringValue(x));
    }

    public static double doubleValue(Object x)
    {
        if (x instanceof Number)
            return ((Number)x).doubleValue();
        //  else
        return Double.parseDouble(stringValue(x));
    }
}
