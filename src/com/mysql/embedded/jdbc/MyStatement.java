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

package com.mysql.embedded.jdbc;

import com.mysql.embedded.api.api;

import java.sql.*;

/**
 * @author Peter Sch�fer
 */

public class MyStatement
        implements java.sql.Statement
{
	/** associated connection   */
	protected MyConnection connection;
	/** current result set  */
	protected MyResultSet result;
	/** update count    */
	protected int updateCount;

	/** streamed/stored result set ? */
	protected boolean stored;
	/** max. result set rows (<0:unlimited)   */
	protected int maxRows;

	protected MyStatement(MyConnection connection, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
		throws SQLException
	{
		this.connection = connection;
		this.maxRows = -1;

		switch (resultSetType) {
		case ResultSet.TYPE_FORWARD_ONLY:
			stored = false;
			break;
		case ResultSet.TYPE_SCROLL_INSENSITIVE:
			stored = true;
			break;
		case ResultSet.TYPE_SCROLL_SENSITIVE:
			throw new UnsupportedOperationException("scroll sensitive result sets are not supported");
		}

		switch (resultSetConcurrency)
		{
		case ResultSet.CONCUR_READ_ONLY:
			break;
		case ResultSet.CONCUR_UPDATABLE:
			throw new UnsupportedOperationException("not yet implemented");
		}

		switch (resultSetHoldability)
		{
		case ResultSet.HOLD_CURSORS_OVER_COMMIT:
			throw new UnsupportedOperationException();
		case ResultSet.CLOSE_CURSORS_AT_COMMIT:
			break;
		}
	}


	public ResultSet executeQuery(String sql) throws SQLException
	{
		if (execute(sql))
			return result;
		else
			throw new SQLException("Select query expected");
	}

	public int executeUpdate(String sql) throws SQLException
	{
		if (!execute(sql))
			return updateCount;
		else
			throw new SQLException("Insert or Update query expected");
	}

	public void close() throws SQLException
	{
		try {
			cancel();
		} finally {
			updateCount = -1;
			connection = null;
		}
	}


	public boolean isClosed()
	{
		return connection==null;
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {

	}

	@Override
	public boolean isPoolable() throws SQLException {
		return false;
	}

	@Override
	public void closeOnCompletion() throws SQLException {

	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}

	public int getMaxFieldSize() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setMaxFieldSize(int max) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getMaxRows() throws SQLException
	{
		return maxRows;
	}

	public void setMaxRows(int max) throws SQLException
	{
		maxRows = Math.max(0,max);
		/** max. rows must be set on connection level
		 *  useStatement() will deal with it, as soon as this statement gets executed
		 */
	}

	public void setEscapeProcessing(boolean enable) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getQueryTimeout() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setQueryTimeout(int seconds) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void cancel() throws SQLException
	{
		try {
			if (result!=null && !result.isClosed())
				result.close();
		} finally {
			result = null;
		}
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return connection.getWarnings();
	}

	public void clearWarnings() throws SQLException
	{
		connection.clearWarnings();
	}

	public void setCursorName(String name) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	protected boolean embeddedAdministration(String sql) throws SQLException
	{

		//  special shortcuts for embedded server
		//  this functioniality is not available by default
		if (sql.equalsIgnoreCase("kill query"))
		{
			connection.killQuery();
			return true;
		}
		if (sql.equalsIgnoreCase("kill connection"))
		{
			connection.killConnection();
			connection.close();
			return true;
		}
		if (sql.equalsIgnoreCase("shutdown"))
		{
			connection.close();
			MySqlEmbeddedDriver.shutdown();
			return true;
		}

		return false;
	}

	public boolean execute(String sql) throws SQLException
	{
		if (embeddedAdministration(sql)) {
			updateCount = 0;
			return false;
		}

		sql = EscapeProcessor.escapeSQLString(sql);

		connection.useStatement(this);
		cancel();

		connection.setMaxRows(maxRows); //  must be set on connection level
		api.mysql_real_query(connection.connectionHandle,sql);

        return retrieveResult();
    }

    protected boolean retrieveResult()
            throws SQLException
    {
        int columnCount = api.mysql_field_count(connection.connectionHandle);
        if (columnCount > 0) {
            //  select
            result = new MyResultSet(this, columnCount, stored, maxRows);
            updateCount = -1;
            return true;
        }
        else {
            //  update
            result = null;
            updateCount = (int)api.mysql_affected_rows(connection.connectionHandle);
	        /** note: we want the number of all matching rows, even when content is not changed */
            return false;
        }
    }

    public ResultSet getResultSet() throws SQLException
    {
        return result;
    }

	public int getUpdateCount() throws SQLException
	{
		return updateCount;
	}

	public boolean getMoreResults() throws SQLException
	{
        return getMoreResults(Statement.CLOSE_CURRENT_RESULT);
    }

	public void setFetchDirection(int direction) throws SQLException
	{
		switch (direction) {
        case ResultSet.FETCH_FORWARD:
            break;
        default:
            throw new UnsupportedOperationException();
        }
	}

	public int getFetchDirection() throws SQLException
	{
		return ResultSet.FETCH_FORWARD;
	}

	public void setFetchSize(int rows) throws SQLException
	{
		/**
		 * backward compatibility with Connector/J
		 */
		switch (rows) {
		case Integer.MIN_VALUE:
			stored = false;
			break;
		case -1:
			stored = true;
			break;
		}
	}

	public int getFetchSize() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getResultSetConcurrency() throws SQLException
	{
		return ResultSet.CONCUR_READ_ONLY;
	}

	public int getResultSetType() throws SQLException
	{
		if (stored)
			return ResultSet.TYPE_SCROLL_INSENSITIVE;
		else
			return ResultSet.TYPE_FORWARD_ONLY;
	}

	public void addBatch(String sql) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void clearBatch() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int[] executeBatch() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Connection getConnection() throws SQLException
	{
		return connection;
	}

	public boolean getMoreResults(int current) throws SQLException
	{
		switch (current)
        {
        case Statement.CLOSE_CURRENT_RESULT:
        case Statement.CLOSE_ALL_RESULTS:
            cancel();   //  close previous result
            break;
        case Statement.KEEP_CURRENT_RESULT:
            if (result!=null && !result.stored)
                throw new SQLException("can't keep previous ResultSet open");
            break;
        }

        connection.useStatement(this);

        boolean more = api.mysql_more_results(connection.connectionHandle);
        if (more) {
            api.mysql_next_result(connection.connectionHandle);
            return retrieveResult();
        }
        else
            return false;
    }

	public ResultSet getGeneratedKeys() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int executeUpdate(String sql, int columnIndexes[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int executeUpdate(String sql, String columnNames[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, int columnIndexes[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, String columnNames[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getResultSetHoldability() throws SQLException
	{
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}
