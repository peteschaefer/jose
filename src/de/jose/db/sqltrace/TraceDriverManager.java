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

package de.jose.db.sqltrace;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * similar to DriverManager, but with a tracking option
 */

public class TraceDriverManager
{
	protected static boolean traceSQL = true;
	
	public static void deregisterDriver(Driver driver)	throws SQLException		{ DriverManager.deregisterDriver(driver); }
          
	public static Connection getConnection(String url) throws SQLException		{
		Connection conn = DriverManager.getConnection(url);
		if (conn==null)
			return null;
		else
			return new TraceConnection(conn);
	}
     
	public static Connection getConnection(String url, Properties info) throws SQLException {
		Connection conn = DriverManager.getConnection(url,info);
		if (conn==null)
			return null;
		else
			return new TraceConnection(conn);
	}
     
	public static Connection getConnection(String url, String user, String password) throws SQLException {
		Connection conn = DriverManager.getConnection(url,user,password);
		if (conn==null)
			return null;
		else
			return new TraceConnection(conn);
	}
     
	public static Driver getDriver(String url)	throws SQLException			{ return DriverManager.getDriver(url); }
          
	public static Enumeration getDrivers()	throws SQLException				{ return DriverManager.getDrivers(); }
          
	public static int getLoginTimeout()	throws SQLException					{ return DriverManager.getLoginTimeout(); }

	/**	@deprecated	 */
	public static PrintStream getLogStream()	throws SQLException			{ return DriverManager.getLogStream(); }
    
	public static PrintWriter getLogWriter()								{ return DriverManager.getLogWriter(); }
          
	public static void println(String message)								{ DriverManager.println(message); }
    
	public static void registerDriver(Driver driver)	throws SQLException	{ DriverManager.registerDriver(driver); }
          
	public static void setLoginTimeout(int seconds)							{ DriverManager.setLoginTimeout(seconds); }
          
	/**	@deprecated	 */
	public static void setLogStream(PrintStream out)						{ DriverManager.setLogStream(out); }
    
	public static void setLogWriter(PrintWriter out)						{ DriverManager.setLogWriter(out); }
	
	
	/**	these are new	 */
	public static boolean traceEnabled()									{ return traceSQL; }
	
	public static void enableTrace(boolean on)								{ traceSQL = on; }
}
