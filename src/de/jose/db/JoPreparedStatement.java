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

import de.jose.db.sqltrace.TraceDriverManager;
import de.jose.util.IntArray;
import de.jose.util.map.IntHashSet;

import java.io.ByteArrayInputStream;
import java.sql.*;

import com.mysql.embedded.jdbc.MyPreparedStatement;

public class JoPreparedStatement
		extends JoStatement
{
	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	/**
	 * creates a prepared statement
	 * @param conn the database connection
	 * @param sql the text of the query
	 * @param type result set type (FORWARD_ONLY, SCROLL_(IN)SENSITIVE)
	 * @param concurrency result set concurrency ()
	 */
	public JoPreparedStatement(JoConnection conn, String sql, int type, int concurrency)
		throws SQLException
	{
		connection = conn;
		sqlText = JoConnection.getAdapter().escapeSql(sql);
		statement = connection.prepareStatement(sqlText,type,concurrency);
		getConnection().addPreparedStatement(getSQL(), this);
	}
	
	/**
	 * creates a prepared statement
	 * @param conn the database connection
	 * @param sql the text of the query
	 */
	public JoPreparedStatement(JoConnection conn, String sql)
		throws SQLException
	{
		this (conn, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}


	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------
	
	/**
	 * executes the statement
	 * @return true if a result set was returned,
	 *	false if an update count was returned
	 */
	public boolean execute()
		throws SQLException
	{
		result = null;
		updateCount = -1;

		if (preparedStatement().execute()) {
			result = preparedStatement().getResultSet();
			return true;
		}
		else {
			updateCount = preparedStatement().getUpdateCount();
			return false;
		}
	}

    /**
     * executes the statement
     * @return true if a result set was returned,
     *	false if an update count was returned
     */
    public boolean execute(boolean enableTrace)
        throws SQLException
    {
        boolean wasTrace = TraceDriverManager.traceEnabled();

        if (wasTrace != enableTrace)
            try {
                TraceDriverManager.enableTrace(enableTrace);
                return execute();
            } finally {
                TraceDriverManager.enableTrace(wasTrace);
            }
        else
            return execute();
    }

	/**
	 *
	 * @return true if the query returns at least one result, or if at least one row is updated
	 * @throws SQLException
	 */
	public boolean exists()
		throws SQLException
	{
        preparedStatement().setMaxRows(1);
		if (execute()) {
			boolean exists = getResultSet().next();
			closeResult();
			return exists;
		}
		else
			return getUpdateCount() > 0;
	}



	/**	don't use execute(String) on a Prepared Statement	 */
	public boolean execute(String sql)
	{
		throw new AbstractMethodError();
	}
	
	/**	overwrites JoStatement.close()	 */
	public void close()
	{
		getConnection().removePreparedStatement(getSQL());
		super.close();
	}

	public IntArray selectIntArray(IntArray collect, int colCount) throws SQLException
	{
		execute();
		return super.selectIntArray(collect,colCount);
	}

	public void setObject(int i, Object value, int type)
		throws SQLException
	{
		if (value==null)
			preparedStatement().setNull(i,type);
		else 
			preparedStatement().setObject(i,value);
	}
	
	public void setString(int i, String value) 
		throws SQLException
	{ 
		if (value==null)
			preparedStatement().setNull(i, Types.VARCHAR);
		else
			preparedStatement().setString(i, value);
			/**	note that setObject won't work with caucho's MySQL driver			 */
	}
	
	public void setString(int i, StringBuffer value)
		throws SQLException
	{
		if (value==null)
			preparedStatement().setNull(i, Types.VARCHAR);
		else
			preparedStatement().setString(i, value.toString());
	}
	
	public void setFixedString(int i, String value) 
		throws SQLException
	{ 
		if (value==null)
			preparedStatement().setNull(i, Types.CHAR);
		else
			preparedStatement().setString(i, value); 
	}
	
	public void setInt(int i, int value) 
		throws SQLException
	{ 
		preparedStatement().setInt(i,value);
	}

	public void setIntNull(int i, int value) throws SQLException
	{
		if (value > 0)
			setInt(i,value);
		else
			setNull(i,Types.INTEGER);
	}

	public void setNull(int i, int sqlType)
		throws SQLException
	{
		preparedStatement().setNull(i++,sqlType);
	}

	public void setNullInt(int i)
		throws SQLException
	{
		setNull(i, Types.INTEGER);
	}

	public void setByte(int i, byte value)
		throws SQLException
	{ 
		preparedStatement().setByte(i,value);
	}
	
	public void setLong(int i, long value) 
		throws SQLException
	{ 
		preparedStatement().setLong(i,value);
	}
		
	public void setBoolean(int i, boolean value) 
		throws SQLException
	{ 
		preparedStatement().setByte(i, (byte)(value ? 1:0));
	}

	public void setInt(int i, int value, int nullValue) 
		throws SQLException
	{
		if (value==nullValue)
			preparedStatement().setNull(i,Types.INTEGER);
		else
			preparedStatement().setInt(i,value);
	}
	
	public void setDate(int i, java.util.Date value)
		throws SQLException
	{
		if (value==null)
			preparedStatement().setNull(i,Types.DATE);
		else if (value instanceof java.sql.Date)
			preparedStatement().setDate(i, (java.sql.Date)value);
		else
			preparedStatement().setDate(i, new java.sql.Date(value.getTime()));
	}

	public void setTime(int i, java.util.Date value)
		throws SQLException
	{
		if (value==null)
			preparedStatement().setNull(i,Types.TIME);
		else if (value instanceof java.sql.Time)
			preparedStatement().setTime(i, (java.sql.Time)value);
		else
			preparedStatement().setTime(i, new java.sql.Time(value.getTime()));
	}

	public void setTimestamp(int i, java.util.Date value)
		throws SQLException
	{
		if (value==null)
			preparedStatement().setNull(i,Types.TIMESTAMP);
		else if (value instanceof java.sql.Timestamp)
			preparedStatement().setTimestamp(i, (java.sql.Timestamp)value);
		else
			preparedStatement().setTimestamp(i, new Timestamp(value.getTime()));
	}
	
	public void setBytes(int i, byte[] value)
		throws SQLException
	{
		if (value==null || value.length<=0)
			preparedStatement().setNull(i,Types.LONGVARBINARY);
		else
			preparedStatement().setBytes(i, value);
	}
	
	public void setBytes(int i, byte[] value, int start, int length)
		throws SQLException
	{
		if (value==null || length<=0)
			preparedStatement().setNull(i,Types.LONGVARBINARY);
		else if (start==0 && getConnection().isEmbedded())
        {
			//  optimized for mysql-je
			((com.mysql.embedded.jdbc.MyPreparedStatement)preparedStatement()).setBytes(i,value,length);
		}
		else {
			//	make a copy
			byte[] copy = new byte[length];
			System.arraycopy(value,start, copy,0,length);
			preparedStatement().setBytes(i, copy);
		}
	}
	
	public void setBinaryStream(int i, byte[] value, int start, int len)
		throws SQLException
	{
		if (value==null || len<=0)
			preparedStatement().setNull(i,Types.LONGVARBINARY);
		else
			preparedStatement().setBinaryStream(i, new ByteArrayInputStream(value,start,len), len);
	}



	public void clearParameters()
		throws SQLException
	{
		preparedStatement().clearParameters();
	}
	
	public void addBatch()
		throws SQLException
	{
		if (JoConnection.getAdapter().canBatchUpdate())
			preparedStatement().addBatch();
		else
			preparedStatement().execute();
	}
	
	public int selectInt()
		throws SQLException
	{
		ResultSet res = null;
		try {
			setMaxRows(1);
			execute();
			res = getResultSet();
			if (res.next())
				return res.getInt(1);
			else
				return Integer.MIN_VALUE;
		} finally {
			if (res!=null) res.close();
		}
	}

	public Timestamp selectTimestamp()
		throws SQLException
	{
		ResultSet res = null;
		try {
			setMaxRows(1);
			execute();
			res = getResultSet();
			if (res.next())
				return res.getTimestamp(1);
			else
				return null;
		} finally {
			if (res!=null) res.close();
		}
	}

	public String selectString()
		throws SQLException
	{
		ResultSet res = null;
		try {
			setMaxRows(1);
			execute();
			res = getResultSet();
			if (res.next())
				return res.getString(1);
			else
				return null;
		} finally {
			if (res!=null) res.close();
		}
	}

	public IntHashSet selectIntHashSet(IntHashSet collect)
        throws SQLException
    {
        if (collect==null) collect = new IntHashSet();
        try {
            execute();
            while (next())
                collect.add(getInt(1));
        } finally {
            closeResult();
        }
        return collect;
    }

    public IntHashSet selectIntHashSet() throws SQLException
    {
        return selectIntHashSet(null);
    }

	private PreparedStatement preparedStatement()	{ return (PreparedStatement)statement; }
}
