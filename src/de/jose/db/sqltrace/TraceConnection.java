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

package de.jose.db.sqltrace;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class TraceConnection
		implements Connection
{
	protected Connection conn;
	
	public TraceConnection(Connection co) 
	{
		conn = co;
	}
 
	public void clearWarnings()	throws SQLException		{ conn.clearWarnings(); }
	public void close()	throws SQLException				{ conn.close(); }
	public void commit()	throws SQLException				{ conn.commit(); }

	public Statement createStatement() throws SQLException	{
		Statement stm = conn.createStatement();
		if (stm==null)
			return null;
		else
			return new TraceStatement(stm);
	}
     
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		Statement stm = conn.createStatement(resultSetType, resultSetConcurrency);
		if (stm==null)
			return null;
		else
			return new TraceStatement(stm);
	}
     
	public boolean getAutoCommit()	throws SQLException			{ return conn.getAutoCommit(); }
    public String getCatalog()	throws SQLException				{ return conn.getCatalog(); }
    public DatabaseMetaData getMetaData()	throws SQLException	{ return conn.getMetaData(); }
	
	public int getTransactionIsolation()	throws SQLException	{ return conn.getTransactionIsolation(); }
	public Map getTypeMap()	throws SQLException		            { return conn.getTypeMap(); }

	public SQLWarning getWarnings()	throws SQLException			{ return conn.getWarnings(); }
	public boolean isClosed()	throws SQLException				{ return conn.isClosed(); }
	public boolean isReadOnly()	throws SQLException				{ return conn.isReadOnly(); }
          
	public String nativeSQL(String sql)	throws SQLException		{ return conn.nativeSQL(sql); }
          
	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement stm = conn.prepareCall(sql);
		if (stm==null)
			return null;
		else
			return new TraceCallableStatement(stm,sql);
	}
     
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		CallableStatement stm = conn.prepareCall(sql, resultSetType, resultSetConcurrency);
		if (stm==null)
			return null;
		else
			return new TraceCallableStatement(stm,sql);
	}
     
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement stm = conn.prepareStatement(sql);
		if (stm==null)
			return null;
		else
			return new TracePreparedStatement(stm,sql);
	}
     
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		PreparedStatement stm = conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
		if (stm==null)
			return null;
		else
			return new TracePreparedStatement(stm,sql);
	}
     
	public void rollback()	throws SQLException							{ conn.rollback(); }
	public void setAutoCommit(boolean autoCommit)	throws SQLException	{ conn.setAutoCommit(autoCommit); }
	public void setCatalog(String catalog)	throws SQLException			{ conn.setCatalog(catalog); }
	public void setReadOnly(boolean readOnly)	throws SQLException		{ conn.setReadOnly(readOnly); }
	public void setTransactionIsolation(int level)	throws SQLException { conn.setTransactionIsolation(level); }
	public void setTypeMap(Map map)	throws SQLException	                { conn.setTypeMap(map); }

    public void setHoldability(int holdability) throws SQLException { conn.setHoldability(holdability);   }
    public int getHoldability() throws SQLException { return conn.getHoldability(); }
    public Savepoint setSavepoint() throws SQLException { return conn.setSavepoint(); }
    public Savepoint setSavepoint(String name) throws SQLException { return conn.setSavepoint(name); }

    public void rollback(Savepoint savepoint) throws SQLException { conn.rollback(savepoint); }
    public void releaseSavepoint(Savepoint savepoint) throws SQLException { conn.releaseSavepoint(savepoint); }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException
    {
        Statement stm = conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        if (stm==null)
            return null;
        else
            return new TraceStatement(stm);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        PreparedStatement stm = conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        if (stm==null)
            return null;
        else
            return new TracePreparedStatement(stm,sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException
    {
        CallableStatement stm = conn.prepareCall(sql,resultSetType,resultSetConcurrency,resultSetHoldability);
        if (stm==null)
            return null;
        else
            return new TraceCallableStatement(stm,sql);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException
    {
        PreparedStatement stm = conn.prepareStatement(sql, autoGeneratedKeys);
        if (stm==null)
            return null;
        else
            return new TracePreparedStatement(stm,sql);
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[])
            throws SQLException
    {
        PreparedStatement stm = conn.prepareStatement(sql, columnIndexes);
        if (stm==null)
            return null;
        else
            return new TracePreparedStatement(stm,sql);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[])
            throws SQLException
    {
        PreparedStatement stm = conn.prepareStatement(sql, columnNames);
        if (stm==null)
            return null;
        else
            return new TracePreparedStatement(stm,sql);
    }

	public Clob createClob() throws SQLException {
		return null;
	}

	public Blob createBlob() throws SQLException {
		return null;
	}

	public NClob createNClob() throws SQLException {
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		return null;
	}

	public boolean isValid(int timeout) throws SQLException {
		return false;
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {

	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {

	}

	public String getClientInfo(String name) throws SQLException {
		return "";
	}

	public Properties getClientInfo() throws SQLException {
		return null;
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return null;
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return null;
	}

	public void setSchema(String schema) throws SQLException {

	}

	public String getSchema() throws SQLException {
		return "";
	}

	public void abort(Executor executor) throws SQLException {

	}

	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

	}

	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}
