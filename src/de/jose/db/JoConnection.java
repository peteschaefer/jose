/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.db;

import de.jose.Application;
import de.jose.Command;
import de.jose.Config;
import de.jose.Util;
import de.jose.util.IntArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.*;
import java.util.HashMap;

public class JoConnection
{

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------


	/**	connection name (optional)	 */
	protected String name;

	/**	JDBC connection	 */
	protected Connection jdbcConnection;
    /** connection mode (usually READ_WRITE)    */
//    protected int connectionMode;
	/**	Map of Prepared Statements (maps String to JoPreparedStatement)	 */
	protected HashMap preparedStatements;

	//-------------------------------------------------------------------------------
	//	Static Fields
	//-------------------------------------------------------------------------------

	/**	DB adapter	 */
	protected static DBAdapter		theAdapter;

	/**	Map of all open connections (maps String to JoConnection)*/
	protected static ConnectionPool theConnections;

	/**	Map of sequences (maps String to Util.IntHandle)	 */
	protected static HashMap 		theSequences;

    /** Prepared Statements must be private to a thread */
    static class PStatementHashKey
    {
        String sql;
        Thread thread;

        PStatementHashKey(String sql, Thread thread)
        {
            this.sql = sql;
            this.thread = thread;
        }

        PStatementHashKey(String sql)
        {
            this(sql, Thread.currentThread());
        }

        public boolean equals(Object obj) {
            PStatementHashKey that = (PStatementHashKey)obj;
            return this.sql.equals(that.sql) && this.thread.equals(that.thread);
        }

        public int hashCode() {
            return sql.hashCode() ^ thread.hashCode();
        }
    }

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public static void init(Config cfg, File wd, File dd, String database, int poolSize)
		throws SQLException
	{
		if (theAdapter==null) theAdapter = DBAdapter.get(database, cfg, wd, dd);
		if (theConnections==null) theConnections = new ConnectionPool(poolSize);
		if (theSequences==null) theSequences = new HashMap();
	}

	public static void init() throws SQLException
	{
		init(Application.theApplication.theConfig,
				Application.theWorkingDirectory,
				Application.theDatabaseDirectory,
				Application.theApplication.theDatabaseId, 0);
	}

	/**	creates a new connection
	 */
	public JoConnection(String aName)
		throws Exception
	{
		this(theAdapter,aName);
	}

	/**	creates a new connection
	 */
	public JoConnection(DBAdapter adapter, String aName)
		throws SQLException
	{
		if (adapter==null) adapter = theAdapter;	//	 = default database
		name = aName;
		jdbcConnection = adapter.createConnection(DBAdapter.READ_WRITE);
		preparedStatements = new HashMap();
	}

	protected JoConnection(Connection dbconn, String aName)
	{
		name = aName;
		jdbcConnection = dbconn;
		preparedStatements = new HashMap();
	}

	/**
	 * @return a named connection (creating it if necessary)
	 */
	public static JoConnection get()
		throws SQLException
	{
		if (theConnections==null) init();
		return theConnections.get();
	}

    public static void release(JoConnection conn)
    {
        if (conn!=null) conn.release();
    }

	public static boolean postWithConnection(Command cmd) {
		return theAdapter.postAfterLaunch(cmd);
}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------
	
	public static final DBAdapter getAdapter()  	{ return getAdapter(true); }

	public static final DBAdapter getAdapter(boolean create)
	{
		if ((theAdapter==null) && create)
			try {
				init();
			} catch (Exception e) {
				Application.error(e);
			}
		return theAdapter;
	}
/*
	public final static DBAdapter getDefaultAdapter()
		throws Exception
	{
		return DBAdapter.get(Application.theApplication.theDatabaseId, 
							 (Config)Application.theApplication.theConfig,
							 Application.theWorkingDirectory);
	}
*/

	public Connection getJdbcConnection()
	{
		return jdbcConnection;
	}

	public static ConnectionPool getPool()                 { return theConnections; }

	/**
	 * release the connection
	 */
	public void release()
	{
		if (theConnections!=null) theConnections.release(this);
	}

	/**
	 * close the connection
	 */
	public void close()
	{
		try {
			if (jdbcConnection!=null)
				jdbcConnection.close();
		} catch (SQLException ex) {
			//	can't help it
		} finally {
			jdbcConnection = null;
			if (theConnections!=null) theConnections.remove(this);
		}
	}

	public static boolean isConnected()
	{
		return (theConnections!=null) && !theConnections.isEmpty();
	}

	/**
	 * @return  a meta data description of the database
	 */
	public final DatabaseMetaData getMetaData()
		throws SQLException
	{
		return jdbcConnection.getMetaData();
	}

	public final boolean isConnectorJ()
	{
		return jdbcConnection.getClass().getName().startsWith("com.mysql.jdbc.");
	}

	public final boolean isEmbedded()
	{
		return jdbcConnection.getClass().getName().startsWith("com.mysql.embedded.jdbc.");
	}

	/**
	 * close all open database connections
	 */
	public static void closeAll()
	{
		if (theConnections!=null) theConnections.closeAll();
	}

	public final boolean cancelQuery() throws SQLException
	{
		return getAdapter().cancelQuery(this);
	}

	/**
	 * @return true if a connection to teh given database exists
	 *
	public static boolean isConnected(DBAdapter adapter)
	{
		Iterator i = theConnections.values().iterator();
		while (i.hasNext()) {
			JoConnection conn = (JoConnection)i.next();
			if (conn.getAdapter() == adapter)
				return true;
		}
		return false;
	}
	*/
	/**
	 * get all connections associated with a given adapter
	 *
	public static JoConnection[] getAllConnections(DBAdapter adapter)
	{
		Vector temp = new Vector();
		Iterator i = theConnections.values().iterator();
		while (i.hasNext()) {
			JoConnection conn = (JoConnection)i.next();
			if (conn.getAdapter() == adapter)
				temp.add(conn);
		}
		
		JoConnection[] result = new JoConnection[temp.size()];
		temp.toArray(result);
		return result;
	}
	*/
	/**
	 * @return a prepared statement (creating it, if necessary)
	 */
	public JoPreparedStatement getPreparedStatement(String sql)
		throws SQLException
	{
		return getPreparedStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}
	
	/**
	 * @return a prepared statement (creating it, if necessary)
	 */
	public JoPreparedStatement getPreparedStatement(String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException
	{
		sql = theAdapter.escapeSql(sql);
		JoPreparedStatement stm = (JoPreparedStatement)preparedStatements.get(new PStatementHashKey(sql));
		if (stm!=null) 
			return stm;
		else
			return new JoPreparedStatement(this, sql, resultSetType, resultSetConcurrency);
	}


	/**
	 * @return a unique sequence (key candidate) for a given database column
	 */
	public final int getSequence(String table, String column)
		throws SQLException
	{
        return getSequence(table,column,1);
    }

    public final int getSequence(String table, String column, int count)
        throws SQLException
    {
		synchronized (theSequences) {
			String key = table+"."+column;
			Util.IntHandle h = (Util.IntHandle)theSequences.get(key);
			if (h==null) {
				h = new Util.IntHandle();
				theSequences.put(key,h);
				String sql;
				if (getAdapter().preferMaxAggregate())
					sql = "SELECT MAX("+column+") FROM "+table;
					/*	this statement is faster on QED	('cause is doesn't support setMaxRows) */
				else
					sql = "SELECT "+column+" FROM "+table+" ORDER BY "+column+" DESC";
					/*	this statement is usually faster than the first one	*/
				
				h.i = Math.max(selectInt(sql),1000);
			}

            int result = h.i+1;
            h.i += count;
			return result;
		}
	}

	public final void resetSequence(String table, String column)
	{
		synchronized(theSequences)
		{
			String key = table+"."+column;
			theSequences.remove(key);
		}
	}

	public void commit()
		throws SQLException
	{
		if (!jdbcConnection.getAutoCommit())
			jdbcConnection.commit();
	}
	
	public void rollback()
		throws SQLException
	{
		if (!jdbcConnection.getAutoCommit())
			jdbcConnection.rollback();
	}
	
	public int executeUpdate(String sql)
		throws SQLException
	{
		JoStatement stm = null;
		int result = -1;
		try {
			stm = new JoStatement(this);
			result = stm.executeUpdate(sql);
		} finally {
			if (stm!=null) stm.close();
		}
		return result;
	}
	
	/**turn auto commit mode off or on (default = on)
	 */
	public boolean setAutoCommit(boolean auto)
		throws SQLException
	{
		if (jdbcConnection.getAutoCommit() != auto && getAdapter().canSetAutoCommit())
			jdbcConnection.setAutoCommit(auto);
		return auto;
	}
	
	/**
	 * @return the product name of the database
	 */
	public String getDatabaseProductName()
		throws SQLException
	{
		return getAdapter().getDatabaseProductName(jdbcConnection);
	}
	
	/**
	 * @return the product version of the database
	 */
	public String getDatabaseProductVersion()
		throws SQLException
	{
		return getAdapter().getDatabaseProductVersion(jdbcConnection);
	}
	
	
	/**
	 * @return the result of querying exactly one value
	 */
	public final int selectInt(String sql)
		throws SQLException
	{
		return selectInt(sql,Integer.MIN_VALUE);
	}
	
	/**
	 * @param defaultValue return value if query returns no result
	 * @return the result of querying exactly one value
	 */
	public int selectInt(String sql, int defaultValue)
		throws SQLException
	{
		JoStatement stm = null;
		try {
			stm = new JoStatement(this);
			stm.setMaxRows(1);
			stm.execute(sql);
			if (stm.next())
				return stm.getInt(1);
		} finally {
			if (stm!=null) stm.close();
		}
		return defaultValue;
	}


	public int[] selectIntArray(String sql, int defaultValue)
		throws SQLException
	{
		return selectIntArray(sql,null,defaultValue).getArray();
	}

	public IntArray selectIntArray(String sql, IntArray collect, int defaultValue)
		throws SQLException
	{
		if (collect==null) collect = new IntArray();
		JoStatement stm = null;
		try {
			stm = new JoStatement(this);
			stm.execute(sql);
			while (stm.next())
				collect.add(stm.getInt(1,defaultValue));
		} finally {
			if (stm!=null) stm.close();
		}

		return collect;
	}

	/**
	 * @return the result of querying exactly one value
	 */
	public final String selectString(String sql)
		throws SQLException
	{
		return selectString(sql,null);
	}
	
	
	/**
	 * @param defaultValue return value if query returns no result
	 * @return the result of querying exactly one value
	 */
	public String selectString(String sql, String defaultValue)
		throws SQLException
	{
		JoStatement stm = null;
		try {
			stm = new JoStatement(this);
			stm.setMaxRows(1);
			stm.execute(sql);
			if (stm.next())
				return stm.getString(1);
		} finally {
			if (stm!=null) stm.close();
		}
		return defaultValue;
	}
	
	//-------------------------------------------------------------------------------
	//	Protected Methods
	//-------------------------------------------------------------------------------

    /**
     *  closing a connection may be an expensive operation (MySQL, for example)
     *  this thread can be used to put it into the background
     *
     *  of course, you can not use the connection anymore once the Thread is started
     *
    public static class ConnectionCloser extends Thread
    {
        Connection connection;
	    long delayMillis;

        public ConnectionCloser(Connection conn, long delay)
        {
            connection = conn;
	        delayMillis = delay;
            setPriority(Thread.MIN_PRIORITY);
        }

        public void run() {
            try {
	            if (delayMillis > 0) try {
	            	sleep(delayMillis);
	            } catch (InterruptedException iex) {
		            //	don't mind
	            }
                connection.close();
            } catch (SQLException sqlex) {
                //  ignore
            }
        }
    }
*/
	protected final Statement createStatement(int type, int concurrency)
		throws SQLException
	{
		return jdbcConnection.createStatement(type,concurrency);
	}
	
	protected final PreparedStatement prepareStatement(String sql, int type, int concurrency)
		throws SQLException
	{
		return jdbcConnection.prepareStatement(sql, type, concurrency);
	}
	
	protected void addPreparedStatement(String key, JoPreparedStatement pstm)
	{
		preparedStatements.put(new PStatementHashKey(key),pstm);
	}
	
	protected void removePreparedStatement(String key)
	{
		preparedStatements.remove(new PStatementHashKey(key));
	}
}
