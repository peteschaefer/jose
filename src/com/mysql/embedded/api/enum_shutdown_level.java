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

public interface enum_shutdown_level
{
	  /*
	    We want levels to be in growing order of hardness (because we use number
	    comparisons). Note that DEFAULT does not respect the growing property, but
	    it's ok.
	  */
	  public static final int SHUTDOWN_DEFAULT = 0;
	  /* wait for existing connections to finish */
	  public static final int SHUTDOWN_WAIT_CONNECTIONS = common.MYSQL_SHUTDOWN_KILLABLE_CONNECT;
	  /* wait for existing trans to finish */
	  public static final int SHUTDOWN_WAIT_TRANSACTIONS = common.MYSQL_SHUTDOWN_KILLABLE_TRANS;
	  /* wait for existing updates to finish (=> no partial MyISAM update) */
	  public static final int SHUTDOWN_WAIT_UPDATES = common.MYSQL_SHUTDOWN_KILLABLE_UPDATE;
	  /* flush InnoDB buffers and other storage engines' buffers*/
	  public static final int SHUTDOWN_WAIT_ALL_BUFFERS = (common.MYSQL_SHUTDOWN_KILLABLE_UPDATE << 1);
	  /* don't flush InnoDB buffers, flush other storage engines' buffers*/
	  public static final int SHUTDOWN_WAIT_CRITICAL_BUFFERS = (common.MYSQL_SHUTDOWN_KILLABLE_UPDATE << 1) + 1;
	  /* Now the 2 levels of the KILL command */
//	#if MYSQL_VERSION_ID >= 50000
//	  KILL_QUERY= 254,
//	#endif
	  public static final int KILL_CONNECTION= 255;
}
