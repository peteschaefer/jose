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

/**
 * @author Peter Schäfer
 */

/*
enum enum_server_command
{
  COM_SLEEP, COM_QUIT, COM_INIT_DB, COM_QUERY, COM_FIELD_LIST,
  COM_CREATE_DB, COM_DROP_DB, COM_REFRESH, COM_SHUTDOWN, COM_STATISTICS,
  COM_PROCESS_INFO, COM_CONNECT, COM_PROCESS_KILL, COM_DEBUG, COM_PING,
  COM_TIME, COM_DELAYED_INSERT, COM_CHANGE_USER, COM_BINLOG_DUMP,
  COM_TABLE_DUMP, COM_CONNECT_OUT, COM_REGISTER_SLAVE,
  COM_PREPARE, COM_EXECUTE, COM_LONG_DATA, COM_CLOSE_STMT,
  COM_RESET_STMT, COM_SET_OPTION,
  /* don't forget to update const char *command_name[] in sql_parse.cc * /

  /* Must be last * /
  COM_END
};
*/

public interface enum_server_command
{
	public static final int COM_SLEEP         =  0;
	public static final int COM_QUIT          =  1;
	public static final int COM_INIT_DB       =  2;
	public static final int COM_QUERY         =  3;
	public static final int COM_FIELD_LIST    =  4;
	public static final int COM_CREATE_DB     =  5;
	public static final int COM_DROP_DB       =  6;
	public static final int COM_REFRESH       =  7;
	public static final int COM_SHUTDOWN      =  8;
	public static final int COM_STATISTICS    =  9;
	public static final int COM_PROCESS_INFO  =  10;
	public static final int COM_CONNECT       =  11;
	public static final int COM_PROCESS_KILL  =  12;
	public static final int COM_DEBUG         =  13;
	public static final int COM_PING          =  14;

	public static final int COM_TIME          =  15;
	public static final int COM_DELAYED_INSERT=  16;
	public static final int COM_CHANGE_USER   =  17;
	public static final int COM_BINLOG_DUMP   =  18;
	public static final int COM_TABLE_DUMP    =  19;
	public static final int COM_CONNECT_OUT   =  20;
	public static final int COM_REGISTER_SLAVE=  21;

	public static final int COM_PREPARE     = 22;
	public static final int COM_EXECUTE     = 23;
	public static final int COM_LONG_DATA   = 24;
	public static final int COM_CLOSE_STMT  = 25;
	public static final int COM_RESET_STMT  = 26;
	public static final int COM_SET_OPTION  = 27;
  /* Must be last */
    public static final int COM_END         = 28;
}
