//-----------------------------------------------------------------------------
// interface enum_mysql_option
//
// Copyright (C) Healy Hudson AG 2002
// $Header: /ECOS20/Java/src/de/ecosx/xml/CustomEntityResolver.java 5     12.04.01 11:21 Alexander.helf $
//-----------------------------------------------------------------------------

package com.mysql.embedded.api;


/**
 * @author Peter Schäfer
 */
/*
enum mysql_option
{
  MYSQL_OPT_CONNECT_TIMEOUT, MYSQL_OPT_COMPRESS, MYSQL_OPT_NAMED_PIPE,
  MYSQL_INIT_COMMAND, MYSQL_READ_DEFAULT_FILE, MYSQL_READ_DEFAULT_GROUP,
  MYSQL_SET_CHARSET_DIR, MYSQL_SET_CHARSET_NAME, MYSQL_OPT_LOCAL_INFILE,
  MYSQL_OPT_PROTOCOL, MYSQL_SHARED_MEMORY_BASE_NAME, MYSQL_OPT_READ_TIMEOUT,
  MYSQL_OPT_WRITE_TIMEOUT, MYSQL_OPT_USE_RESULT,
  MYSQL_OPT_USE_REMOTE_CONNECTION, MYSQL_OPT_USE_EMBEDDED_CONNECTION,
  MYSQL_OPT_GUESS_CONNECTION, MYSQL_SET_CLIENT_IP, MYSQL_SECURE_AUTH
};
*/
public interface enum_mysql_option
{
	public static final int MYSQL_OPT_CONNECT_TIMEOUT	    =  0;
	public static final int MYSQL_OPT_COMPRESS	            =  1;
	public static final int MYSQL_OPT_NAMED_PIPE	        =  2;
	public static final int MYSQL_INIT_COMMAND	            =  3;
	public static final int MYSQL_READ_DEFAULT_FILE	        =  4;
	public static final int MYSQL_READ_DEFAULT_GROUP	    =  5;
	public static final int MYSQL_SET_CHARSET_DIR	        =  6;
	public static final int MYSQL_SET_CHARSET_NAME	        =  7;
	public static final int MYSQL_OPT_LOCAL_INFILE	        =  8;
	public static final int MYSQL_OPT_PROTOCOL              =  9;
	public static final int MYSQL_SHARED_MEMORY_BASE_NAME   = 10;
	public static final int MYSQL_OPT_READ_TIMEOUT          = 11;
	public static final int MYSQL_OPT_WRITE_TIMEOUT         = 12;
	public static final int MYSQL_OPT_USE_RESULT            = 13;
	public static final int MYSQL_OPT_USE_REMOTE_CONNECTION = 14;
	public static final int MYSQL_OPT_USE_EMBEDDED_CONNECTION = 15;
	public static final int MYSQL_OPT_GUESS_CONNECTION      = 16;
	public static final int MYSQL_SET_CLIENT_IP             = 17;
	public static final int MYSQL_SECURE_AUTH               = 18;
}
