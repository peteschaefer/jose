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

package de.jose.db;

import de.jose.util.IntArray;

import java.sql.*;

/**
 * model an SQL Statement and Result Set
 */
public class JoStatement
{
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------
	
	/**	the database connection	 */
	protected JoConnection connection;
	/**	the JDBC statement	 */
	protected Statement statement;
	/**	text of SQL query	 */
	protected String sqlText;
	
	/**	result of a select statement	 */
	protected ResultSet result;
	/**	result of an update statement	 */
	protected int updateCount;
	
	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	/**
	 * create a new statement
	 * @param conn the database connection
	 * @param type result set type (FORWARD_ONLY, SCROLL_(IN)SENSITIVE)
	 * @param concurrency result set concurrency ()
	 */
	public JoStatement(JoConnection conn, int type, int concurrency)
		throws SQLException
	{
		connection = conn;
		statement = connection.createStatement(type,concurrency);
		sqlText = null;
	}
	
	/**
	 * create a new statement
	 * @param conn the database connection
	 */
	public JoStatement(JoConnection conn)
		throws SQLException
	{
		this(conn, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}
	
	/**	protected: used by JoPreparedStatement	 */
	protected JoStatement()
	{ }
	
	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------
	
	/**
	 * executes an sql statement
	 * @return true if a result set was return, 
	 *	false if an updatecount was returned
	 */
	public boolean execute(String sql)
		throws SQLException
	{
		sqlText = JoConnection.getAdapter().escapeSql(sql);
		if (statement.execute(sqlText)) {
			result = statement.getResultSet();
			return true;
		}
		else {
			updateCount = statement.getUpdateCount();
			close();
			return false;
		}
	}

	/**
	 * executes a select statement
	 * @return the result set
	 */
	public ResultSet executeQuery(String sql)
		throws SQLException
	{
		if (execute(sql))
			return getResultSet();
		else
			throw new IllegalStateException();
	}
	
	/**
	 * executes a select statement
	 * @return the result set
	 */
	public int executeUpdate(String sql)
		throws SQLException
	{
		if (!execute(sql))
			return getUpdateCount();
		else
			throw new IllegalStateException();
	}

	/**	@return the count produced by an update statement	 */
	public final int getUpdateCount()			{ return updateCount; }
	
	/**	@return the result set of a select statement	 */
	public final ResultSet getResultSet()		{ return result; }
	
	/**	@return the text of the SQL query	 */
	public final String getSQL()				{ return sqlText; }
	/**	@return the database connection	 */
	public final JoConnection getConnection()	{ return connection; }

	/**	set the max. number of returned results	 */
	public final void setMaxRows(int rows) 
		throws SQLException
	{
		if (JoConnection.getAdapter().canMaxRows())
			statement.setMaxRows(rows);
	}

    public final void setFetchSize(ResultSet res, int size)
        throws SQLException
    {
        if (JoConnection.getAdapter().canFetchSize())
            res.setFetchSize(size);
    }

    public final void setFetchSize(int size)
         throws SQLException
     {
         if (JoConnection.getAdapter().canFetchSize())
             statement.setFetchSize(size);
     }

    public String toString()
    {
        return sqlText;
    }

	/**	 @return true if there are more rowsin the result set,
	 *		false otherwise
	 */
	public boolean next() 
		throws SQLException
	{
		if (result==null) return false;
		boolean hasNext = result.next();
		if (!hasNext && result.getType()==ResultSet.TYPE_FORWARD_ONLY) 
			closeResult();
		return hasNext;
	}
	
	/**	close the result set	 */
	public synchronized void closeResult()
		throws SQLException
	{
		try {
			if (result!=null) result.close();
		} finally {
			result = null;
		}
	}
	
	/**	close the statement	 */
	public void close()
	{
		try {
//			if (result!=null) try { result.close(); } catch (Throwable ex) { /* ignore */ }
			if (statement!=null) try { statement.close(); } catch (Throwable ex) { /* ignore */ }
		} finally {
			statement = null;
			result = null;
			sqlText = null;
		}
	}


	public void addBatch(String sql)
		throws SQLException
	{
		if (JoConnection.getAdapter().canBatchUpdate())
			statement.addBatch(sql);
		else
			statement.execute(sql);
	}
	
	public int[] executeBatch()
		throws SQLException
	{
		if (JoConnection.getAdapter().canBatchUpdate())
			return statement.executeBatch();
		else
			return null;
	}
	
	public final Object getObject(int i)		throws SQLException { return result.getObject(i); }
	
	public static final int getInt(ResultSet res, int i, int nullValue) throws SQLException 
	{
		int r = res.getInt(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final int getInt(ResultSet res, int i)	throws SQLException 	{ return getInt(res,i,Integer.MIN_VALUE); }

	public static final int getInt(ResultSet res, String i, int nullValue) throws SQLException
	{
		int r = res.getInt(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final int getInt(ResultSet res, String i)	throws SQLException 	{ return getInt(res,i,Integer.MIN_VALUE); }
	
	public final int getInt(int i)						throws SQLException { return getInt(result,i); }
	public final int getInt(int i, int nullValue)		throws SQLException { return getInt(result,i,nullValue); }


	public static final long getLong(ResultSet res, int i)	throws SQLException 	{ return getLong(res,i,Long.MIN_VALUE); }

	public static final long getLong(ResultSet res, int i, long nullValue) throws SQLException
	{
		long r = res.getLong(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final long getLong(ResultSet res, String i, long nullValue) throws SQLException
	{
		long r = res.getLong(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final long getLong(ResultSet res, String i)	throws SQLException 	{ return getLong(res,i,Long.MIN_VALUE); }

	public final long getLong(int i)						throws SQLException { return getLong(result,i); }
	public final long getLong(int i, long nullValue)		throws SQLException { return getLong(result,i,nullValue); }

	public static final double getDouble(ResultSet res, int i, double nullValue) throws SQLException 
	{
		double r = res.getDouble(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final double getDouble(ResultSet res, int i)	throws SQLException { return getDouble(res,i, Double.MIN_VALUE); }
	
	public final double getDouble(int i)						throws SQLException { return getDouble(result,i); }
	public final double getDouble(int i, double nullValue)		throws SQLException { return getDouble(result,i,nullValue); }

	
	public static final byte getByte(ResultSet res, int i, byte nullValue) throws SQLException 
	{
		byte r = res.getByte(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final byte getByte(ResultSet res, int i)	throws SQLException { return getByte(res,i, Byte.MIN_VALUE); }
	
	public final byte getByte(int i)						throws SQLException { return getByte(result,i); }
	public final byte getByte(int i, byte nullValue)		throws SQLException { return getByte(result,i,nullValue); }
	
	public static final short getShort(ResultSet res, int i, short nullValue) throws SQLException 
	{
		short r = res.getShort(i);
		if (res.wasNull())
			return nullValue;
		else
			return r;
	}

	public static final short getShort(ResultSet res, int i)	throws SQLException { return getShort(res,i, Short.MIN_VALUE); }
	
	public final short getShort(int i)						throws SQLException { return getShort(result,i); }
	public final short getShort(int i, short nullValue)		throws SQLException { return getShort(result,i,nullValue); }
	
	public final String getString(int i)		throws SQLException { return result.getString(i); }
	public final java.util.Date getDate(int i)	throws SQLException { return result.getDate(i); }
	
	public final java.sql.Time getTime(int i)	throws SQLException { return result.getTime(i); }
	public final java.sql.Timestamp getTimestamp(int i)	throws SQLException { return result.getTimestamp(i); }

	public final Blob getBlob(int i)			throws SQLException	{ return result.getBlob(i); }
	
	public final byte[] getBytes(int i)			throws SQLException { return result.getBytes(i); }

	public final Clob getClob(int i)            throws SQLException { return result.getClob(i); }
	
	public final boolean getBoolean(int i)		throws SQLException {
		return result.getByte(i) != 0; 
	}

	public IntArray selectIntArray(IntArray collect, int colCount)
	    throws SQLException
	{
	    if (collect==null) collect = new IntArray();
	    try {
            while (next())
                for (int col=1; col <= colCount; col++)
                    collect.add(getInt(col));
	    } finally {
	        closeResult();
	    }
	    return collect;
	}

	public final IntArray selectIntArray() throws SQLException
    {
        return selectIntArray(null,1);
    }

}
