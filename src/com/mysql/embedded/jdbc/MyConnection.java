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

import com.mysql.embedded.api.MYSQL_TIME;
import com.mysql.embedded.api.api;
import com.mysql.embedded.api.common;
import com.mysql.embedded.api.enum_mysql_option;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author Peter Sch�fer
 */

public class MyConnection
        implements java.sql.Connection
{
	/** connection handle
	 *  MYSQL*
	 */
	public long connectionHandle;
	/** are we in autocommit mode ? */
	protected boolean autoCommit;
	/** current warnings */
	protected SQLWarning warning;

	/** current statement   */
	protected MyStatement currentStatement;

	/** allocated prepared statements   */
	protected HashSet preparedStatements;
	/** max. number of rows in a result set (default=0=unlimited) */
	protected int maxRows = 0;

	/** calendar for date calculations */
	protected Calendar utcCalendar;
	protected SimpleDateFormat timeFormat,dateFormat,timestampFormat,yearFormat;

	/** character set
	 * currently fixed to UTF-8
	 * TODO use database characters set
	 */
	protected Charset charset;
	protected CharsetEncoder charEncoder;
	protected CharsetDecoder charDecoder;
	protected CharBuffer charBuffer;

	protected Charset isoCharset;
	protected CharsetDecoder isoDecoder;

	/** temp buffers    */
	protected ByteBuffer bbuffer;
	protected ShortBuffer sbuffer;

	/** MYSQL_TIME record for temp. conversions */
	protected MYSQL_TIME mysqlTime;

    protected static Set threads = new HashSet();

	/** database version; initialized on demand */
	protected int databaseMajorVersion = -1;

	public MyConnection(Properties info)
		throws SQLException
	{
        initThread();

		String database = info.getProperty("database");
		String user = info.getProperty("user");
		String password = info.getProperty("password");

		connectionHandle = api.mysql_init(0);
		api.mysql_options(connectionHandle,
				enum_mysql_option.MYSQL_OPT_USE_EMBEDDED_CONNECTION, null);

		api.mysql_real_connect(connectionHandle, null,user,password,database, 0, null,
                                common.CLIENT_MULTI_STATEMENTS
                                | common.CLIENT_MULTI_RESULTS
                                | common.CLIENT_LOCAL_FILES
								| common.CLIENT_FOUND_ROWS);
	/** note: UPDATE should return the number of matching rows, not the number of actually modified rows !! */


		utcCalendar = (Calendar)MySqlTypes.UTC_CALENDAR.clone();
		timeFormat = (SimpleDateFormat)MySqlTypes.TIME_FORMAT.clone();
		dateFormat = (SimpleDateFormat)MySqlTypes.DATE_FORMAT.clone();
		timestampFormat = (SimpleDateFormat)MySqlTypes.TIMESTAMP_FORMAT.clone();
		yearFormat = (SimpleDateFormat)MySqlTypes.YEAR_FORMAT.clone();
		mysqlTime = new MYSQL_TIME();

		charset = Charset.forName("UTF-8");
		charEncoder = charset.newEncoder();
		charDecoder = charset.newDecoder();

		isoCharset = Charset.forName("ISO-8859-1");
		isoDecoder = isoCharset.newDecoder();

		bbuffer = ByteBuffer.allocateDirect(64);
		bbuffer.order(ByteOrder.nativeOrder());

		sbuffer = bbuffer.asShortBuffer();

		preparedStatements = new HashSet();
	}

    /**
     * call this before a connection is used in a different thread !
     * @throws SQLException
     */
    public static void initThread() throws SQLException
    {
        /** each new thread must be inited for mysql   */
        Thread thr = Thread.currentThread();
        if (!threads.contains(thr))
        {
            api.my_thread_init();
            threads.add(thr);
        }
    }

    public static void endThread()
    {
        Thread thr = Thread.currentThread();
        if (threads.contains(thr))
        {
            api.my_thread_end();
            threads.remove(thr);
        }
    }

	public int getDatabaseMajorVersion()
	{
		if (databaseMajorVersion < 0)
			databaseMajorVersion =  api.mysql_get_server_version(connectionHandle);
		return databaseMajorVersion;
	}

	public Statement createStatement() throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		MyStatement stm = new MyStatement(this,
		        ResultSet.TYPE_FORWARD_ONLY,
		        ResultSet.CONCUR_READ_ONLY,
		        ResultSet.CLOSE_CURSORS_AT_COMMIT);
		return stm;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		MyPreparedStatement pstm = new MyPreparedStatement(this, sql,
		        ResultSet.TYPE_FORWARD_ONLY,
		        ResultSet.CONCUR_READ_ONLY,
		        ResultSet.CLOSE_CURSORS_AT_COMMIT);
		return pstm;
	}

	public CallableStatement prepareCall(String sql) throws SQLException
	{
        return prepareCall(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyStatement(this,resultSetType,resultSetConcurrency,
		            ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyPreparedStatement(this,sql,resultSetType,resultSetConcurrency,
		            ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
	{
		return prepareCall(sql, resultSetType, resultSetConcurrency, ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyStatement(this,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyPreparedStatement(this,sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
	                                     int resultSetHoldability) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyCallableStatement(this,sql,resultSetType,resultSetConcurrency,resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
	{
		//  ignore parameter; MySQL can always find autoGenerated columns
		return prepareStatement(sql);
	}

	public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException
	{
		//  MySQL supports exactly ONE autogenerated column; you must specify int within the SQL statement
		throw new UnsupportedOperationException();
	}

	public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException
	{
		//  MySQL supports exactly ONE autogenerated column; you must specify int within the SQL statement
		throw new UnsupportedOperationException();
	}

	@Override
	public Clob createClob() throws SQLException {
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return null;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return false;
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {

	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {

	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return "";
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return null;
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return null;
	}

	@Override
	public void setSchema(String schema) throws SQLException {

	}

	@Override
	public String getSchema() throws SQLException {
		return "";
	}

	@Override
	public void abort(Executor executor) throws SQLException {

	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	public String nativeSQL(String sql) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		sql = EscapeProcessor.escapeSQLString(sql);

//		String result = api.mysql_odbc_escape_string(connectionHandle,sql,0);       //  obsolete ?
		String result = api.mysql_real_escape_string(connectionHandle,sql);
		return result;
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		if (autoCommit!=this.autoCommit) {
			this.autoCommit = autoCommit;
			api.mysql_autocommit(connectionHandle,autoCommit);
		}
	}

	public boolean getAutoCommit() throws SQLException
	{
		return autoCommit;
	}

	public void commit() throws SQLException
	{
		if (!autoCommit) {
			useStatement(null);
			api.mysql_commit(connectionHandle);
		}
	}

	public void rollback() throws SQLException
	{
		if (!autoCommit) {
			useStatement(null);
			api.mysql_rollback(connectionHandle);
		}
	}

	public void close() throws SQLException
	{
		//  cancel queries
		killQuery();
		//  close current result set (if any)
		try {
			useStatement(null);
		} catch (SQLException ignore) {
		}

		//  close all prepared statement
		Iterator i =  preparedStatements.iterator();
		while (i.hasNext()) {
			MyPreparedStatement pstm = (MyPreparedStatement)i.next();
			i.remove();
			pstm.close();
		}
		//  close handle
		if (! isClosed())
			try {
				api.mysql_close(connectionHandle);
//              unfortunately, there is no way to release the native memory in bbuffer
	//            api.my_thread_end();
			} finally {
				connectionHandle = 0;
				bbuffer = null;
				MySqlEmbeddedDriver.notifyClosed(this);
			}
	}

	public void killQuery()
	{
		if (connectionHandle!=0) {
			boolean version5 = getDatabaseMajorVersion() >= 50000;
			if (version5)
				api.kill_query(connectionHandle);
		}
	}

	public void killConnection()
	{
		if (connectionHandle!=0) {
			boolean version5 = getDatabaseMajorVersion() >= 50000;
			if (version5)
				api.kill_connection(connectionHandle);
		}
	}


	public boolean isClosed() throws SQLException
	{
		return connectionHandle==0;
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		return new MyDatabaseMetaData(this);
	}

	public void setReadOnly(boolean readOnly) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly() throws SQLException
	{
		return false;
	}

	public void setCatalog(String catalog) throws SQLException
	{
        if (MySqlEmbeddedDriver.unix) initThread();
		api.mysql_select_db(connectionHandle,catalog);
	}

	public String getCatalog() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setTransactionIsolation(int level) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getTransactionIsolation() throws SQLException
	{
		return 0;
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return warning;
	}

	public void clearWarnings() throws SQLException
	{
		warning = null;
	}

	public Map getTypeMap() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setTypeMap(Map map) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setHoldability(int holdability) throws SQLException
	{
		switch (holdability)
		{
		case ResultSet.HOLD_CURSORS_OVER_COMMIT:
			throw new UnsupportedOperationException();
		case ResultSet.CLOSE_CURSORS_AT_COMMIT:
			break;
		}
	}

	public String getMySQLVariable(String variableName) throws SQLException
	{
		PreparedStatement pstm = prepareStatement("SHOW VARIABLES WHERE Variable_Name = ? ");
		pstm.setString(1,variableName);

		ResultSet res = null;
		try {
			res = pstm.executeQuery();
			if (res.next())
				return res.getString(2);
		} finally {
			if (res!=null) res.close();
		}
		//  otherwise
		return null;
	}

	public int getHoldability() throws SQLException
	{
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	public Savepoint setSavepoint() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Savepoint setSavepoint(String name) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void rollback(Savepoint savepoint) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException
	{
		throw new UnsupportedOperationException();
	}


	protected boolean useStatement(MyStatement nextStatement)
		throws SQLException
	{
		if (currentStatement==nextStatement)
			return true;    //  that was easy
		//  else
		if (currentStatement!=null && currentStatement.result!=null
                && !currentStatement.result.stored)
			currentStatement.cancel();  //  close ResultSet, if streaming!
		//  maxRows need to be set on connection level
		currentStatement = nextStatement;
		return false;
	}

	protected void setMaxRows(int rows) throws SQLException
	{
		rows = Math.max(0,rows);
		if (rows!=maxRows) {
			maxRows = rows;
			if (maxRows > 0)
				api.mysql_real_query(connectionHandle,"SET SESSION SQL_SELECT_LIMIT="+maxRows);
			else
				api.mysql_real_query(connectionHandle,"SET SESSION SQL_SELECT_LIMIT=DEFAULT");
		}
	}

	public CharBuffer getCharBuffer(int capacity)
	{
		if (charBuffer==null || charBuffer.capacity() < capacity)
			charBuffer = CharBuffer.allocate(Math.max(capacity,256));

		charBuffer.position(0);
		charBuffer.limit(charBuffer.capacity());
		return charBuffer;
	}

	public String decodeString(ByteBuffer input, int len)
	{
		if (len==0) return "";

		CharsetDecoder dec = charDecoder;
		CharBuffer output = getCharBuffer(len);

		input.position(0);
		input.limit(len);

		dec.reset();
		CoderResult result = dec.decode(input,output,true);
		if (result.isError() || result.isMalformed() || result.isUnmappable())
		{
			//  for backward compatibility: try ISO-8859-1 encoding
			dec = isoDecoder;
			output = getCharBuffer(len);
			input.position(0);
			input.limit(len);
			dec.reset();
			dec.decode(input,output,true);
		}

		dec.flush(output);
		//  cbuffer is now positioned /after/ the end of the string ;-(
		output.limit(output.position());
		output.position(0);
		return output.toString();
	}


	protected void register(MyPreparedStatement pstm)
	{
		preparedStatements.add(pstm);
	}

	protected void unregister(MyPreparedStatement pstm)
	{
		preparedStatements.remove(pstm);
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
