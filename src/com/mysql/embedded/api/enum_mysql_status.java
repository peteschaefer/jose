//-----------------------------------------------------------------------------
// interface enum_mysql_status
//
// Copyright (C) Healy Hudson AG 2002
// $Header: /ECOS20/Java/src/de/ecosx/xml/CustomEntityResolver.java 5     12.04.01 11:21 Alexander.helf $
//-----------------------------------------------------------------------------

package com.mysql.embedded.api;


/**
 * User: peter.schaefer
 * Date: 18.11.2002
 * Time: 12:12:24
 *
 *
 * @author Peter Schäfer 
 */
public interface enum_mysql_status 
{
	public static final int MYSQL_STATUS_READY	=  0;
	public static final int MYSQL_STATUS_GET_RESULT	=  1;
	public static final int MYSQL_STATUS_USE_RESULT	=  2;
}
