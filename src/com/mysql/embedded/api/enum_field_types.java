/**
 * Created by IntelliJ IDEA.
 * User: Peter Schäfer
 * Date: 17.11.2002
 * Time: 18:56:35
 * To change this template use Options | File Templates.
 */
package com.mysql.embedded.api;

/*
enum enum_field_types { MYSQL_TYPE_DECIMAL, MYSQL_TYPE_TINY,
			MYSQL_TYPE_SHORT,  MYSQL_TYPE_LONG,
			MYSQL_TYPE_FLOAT,  MYSQL_TYPE_DOUBLE,
			MYSQL_TYPE_NULL,   MYSQL_TYPE_TIMESTAMP,
			MYSQL_TYPE_LONGLONG,MYSQL_TYPE_INT24,
			MYSQL_TYPE_DATE,   MYSQL_TYPE_TIME,
			MYSQL_TYPE_DATETIME, MYSQL_TYPE_YEAR,
			MYSQL_TYPE_NEWDATE, MYSQL_TYPE_VARCHAR,
			MYSQL_TYPE_BIT,
                        MYSQL_TYPE_NEWDECIMAL=246,
			MYSQL_TYPE_ENUM=247,
			MYSQL_TYPE_SET=248,
			MYSQL_TYPE_TINY_BLOB=249,
			MYSQL_TYPE_MEDIUM_BLOB=250,
			MYSQL_TYPE_LONG_BLOB=251,
			MYSQL_TYPE_BLOB=252,
			MYSQL_TYPE_VAR_STRING=253,
			MYSQL_TYPE_STRING=254,
			MYSQL_TYPE_GEOMETRY=255

};

};
*/
public interface enum_field_types
{
	public static final int MYSQL_TYPE_DECIMAL      = 0;
	public static final int MYSQL_TYPE_TINY         = 1;
	public static final int MYSQL_TYPE_SHORT        = 2;
	public static final int MYSQL_TYPE_LONG         = 3;
	public static final int MYSQL_TYPE_FLOAT        = 4;
	public static final int MYSQL_TYPE_DOUBLE       = 5;
	public static final int MYSQL_TYPE_NULL         = 6;
	public static final int MYSQL_TYPE_TIMESTAMP    = 7;
	public static final int MYSQL_TYPE_LONGLONG     = 8;
	public static final int MYSQL_TYPE_INT24        = 9;
	public static final int MYSQL_TYPE_DATE         = 10;
	public static final int MYSQL_TYPE_TIME         = 11;
	public static final int MYSQL_TYPE_DATETIME     = 12;
	public static final int MYSQL_TYPE_YEAR         = 13;
	public static final int MYSQL_TYPE_NEWDATE      = 14;
    /** @since MySQL 5.0 */
    public static final int MYSQL_TYPE_VARCHAR      = 15;
    /** @since MySQL 5.0 */
    public static final int MYSQL_TYPE_BIT          = 16;

    /** @since MySQL 5.0 */
    public static final int MYSQL_TYPE_NEWDECIMAL   = 246;
    public static final int MYSQL_TYPE_ENUM         = 247;
	public static final int MYSQL_TYPE_SET          = 248;
	public static final int MYSQL_TYPE_TINY_BLOB    = 249;
	public static final int MYSQL_TYPE_MEDIUM_BLOB  = 250;
	public static final int MYSQL_TYPE_LONG_BLOB    = 251;
	public static final int MYSQL_TYPE_BLOB         = 252;
	public static final int MYSQL_TYPE_VAR_STRING   = 253;
	public static final int MYSQL_TYPE_STRING       = 254;
	public static final int MYSQL_TYPE_GEOMETRY     = 255;

/* For backward compatibility */
	public static final int CLIENT_MULTI_QUERIES    = common.CLIENT_MULTI_STATEMENTS;
	public static final int FIELD_TYPE_DECIMAL     = MYSQL_TYPE_DECIMAL;
	public static final int FIELD_TYPE_TINY        = MYSQL_TYPE_TINY;
	public static final int FIELD_TYPE_SHORT       = MYSQL_TYPE_SHORT;
	public static final int FIELD_TYPE_LONG        = MYSQL_TYPE_LONG;
	public static final int FIELD_TYPE_FLOAT       = MYSQL_TYPE_FLOAT;
	public static final int FIELD_TYPE_DOUBLE      = MYSQL_TYPE_DOUBLE;
	public static final int FIELD_TYPE_NULL        = MYSQL_TYPE_NULL;
	public static final int FIELD_TYPE_TIMESTAMP   = MYSQL_TYPE_TIMESTAMP;
	public static final int FIELD_TYPE_LONGLONG    = MYSQL_TYPE_LONGLONG;
	public static final int FIELD_TYPE_INT24       = MYSQL_TYPE_INT24;
	public static final int FIELD_TYPE_DATE        = MYSQL_TYPE_DATE;
	public static final int FIELD_TYPE_TIME        = MYSQL_TYPE_TIME;
	public static final int FIELD_TYPE_DATETIME    = MYSQL_TYPE_DATETIME;
	public static final int FIELD_TYPE_YEAR        = MYSQL_TYPE_YEAR;
	public static final int FIELD_TYPE_NEWDATE     = MYSQL_TYPE_NEWDATE;
	public static final int FIELD_TYPE_ENUM        = MYSQL_TYPE_ENUM;
	public static final int FIELD_TYPE_SET         = MYSQL_TYPE_SET;
	public static final int FIELD_TYPE_TINY_BLOB   = MYSQL_TYPE_TINY_BLOB;
	public static final int FIELD_TYPE_MEDIUM_BLOB = MYSQL_TYPE_MEDIUM_BLOB;
	public static final int FIELD_TYPE_LONG_BLOB   = MYSQL_TYPE_LONG_BLOB;
	public static final int FIELD_TYPE_BLOB        = MYSQL_TYPE_BLOB;
	public static final int FIELD_TYPE_VAR_STRING  = MYSQL_TYPE_VAR_STRING;
	public static final int FIELD_TYPE_STRING      = MYSQL_TYPE_STRING;
	public static final int FIELD_TYPE_CHAR        = MYSQL_TYPE_TINY;
	public static final int FIELD_TYPE_INTERVAL    = MYSQL_TYPE_ENUM;
	public static final int FIELD_TYPE_GEOMETRY    = MYSQL_TYPE_GEOMETRY;

}
