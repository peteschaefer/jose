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
import com.mysql.embedded.api.enum_field_types;
import com.mysql.embedded.api.stmt;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.Calendar;

/**
 * @author Peter Sch�fer
 */

public class MyPreparedStatement
        extends MyStatement
        implements java.sql.PreparedStatement
{
	/** handle to MYSQL_STMT    */
	protected long statementHandle;
	/** handle to array of MYSQL_BIND */
	protected BindArray parameters;

	/** result set meta data (created on demand?) */
	protected MyResultSetMetaData resultMetaData;
	/** parameter meta data (created on demand) */
	protected MyParameterMetaData paramMetaData;

	/** number of result column */
	protected int columnCount;
	/** handle to MYSQL_BIND array */
	protected BindArray resultColumns;
	/** note that columnBindHandle is maintained by this class,
	 *  not by MyPreparedResultSet. It can be shared by several sequential result sets
	 *  and is deallocated on close().
	 */
    /** number of rows to pre-fetch */
    protected int fetchSize = 0;

    protected MyPreparedStatement(MyConnection connection, String sql,
	                              int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
	{
		super(connection,resultSetType,resultSetConcurrency,resultSetHoldability);
		prepare(sql);
	}
	
	public int executeUpdate() throws SQLException
	{
		if (statementHandle==0)
			throw new SQLException("illegal state");
		if (columnCount > 0)
			throw new SQLException("Update statement expected");

		execute();
		return updateCount;
	}

	public void addBatch() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void clearParameters() throws SQLException
	{
		if (parameters!=null)
			parameters.clear();
		stmt.mysql_stmt_reset(statementHandle); //  OK ?
	}

	public synchronized boolean execute() throws SQLException
	{
//		assertPrepared(statementHandle);

		connection.useStatement(this);
		cancel();   //  close previous result

		//  bind parameters
		if (parameters!=null)
			stmt.mysql_stmt_bind_param(statementHandle,parameters.nativeAddress);

		connection.setMaxRows(maxRows); //  must be set on connection level
		//  execute
		stmt.mysql_stmt_execute(statementHandle);

        return retrieveResult();
    }

    public void setFetchSize(int rows) throws SQLException
    {
        super.setFetchSize(rows);

        if (rows < 0) rows = 0;

        if (statementHandle!=0L && fetchSize!=rows)
            stmt.mysql_stmt_attr_set(statementHandle,stmt.STMT_ATTR_PREFETCH_ROWS,fetchSize=rows);

        fetchSize=rows;
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
            columnCount = stmt.mysql_stmt_field_count(statementHandle);
            return retrieveResult();
        }
        else
            return false;
    }

    protected boolean retrieveResult()
            throws SQLException
    {
        if (columnCount > 0) {
            //  select

            //  allocate bind records
            if (resultColumns==null)
            {
                resultColumns = new BindArray(this,columnCount);

                getMetaData();

                resultColumns.initialize(resultMetaData);
            }

            if (result==null)
                result = new MyPreparedResultSet(this,false);
            else
                ((MyPreparedResultSet)result).reset(false);

            updateCount = -1;
            return true;
        }
        else {
            //  update
            result = null;
            updateCount = (int) stmt.mysql_stmt_affected_rows(statementHandle);
            return false;
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException
    {
        parameters.setByte(parameterIndex-1, x);
    }

	public void setDouble(int parameterIndex, double x) throws SQLException
	{
		parameters.setDouble(parameterIndex-1, x);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException
	{
		parameters.setFloat(parameterIndex-1, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException
	{
		parameters.setInt(parameterIndex-1, x);
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException
	{
		boolean version5 = connection.getDatabaseMajorVersion() >= 50000;
		parameters.setNull(parameterIndex-1,MySqlTypes.asMysqlType(sqlType,version5));
	}

	public void setLong(int parameterIndex, long x) throws SQLException
	{
		parameters.setLong(parameterIndex-1, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException
	{
		parameters.setShort(parameterIndex-1, x);
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException
	{
		parameters.setByte(parameterIndex-1, (byte)(x?1:0));
	}

	public void setBytes(int parameterIndex, byte x[]) throws SQLException
	{
		setBytes(parameterIndex,x,x.length);
	}

	public void setBytes(int parameterIndex, byte x[], int length) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else if (MySqlEmbeddedDriver.SEND_LONG_DATA)
		{
			ByteBuffer buffer = parameters.allocateBuffer(parameterIndex-1,
			                            enum_field_types.MYSQL_TYPE_BLOB, length);
			buffer.position(0);
			buffer.put(x,0,length);
			stmt.mysql_stmt_send_long_data(statementHandle,parameterIndex-1,buffer,length);
		}
		else
			parameters.setBytes(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, x, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else
			parameters.setInputStream(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, x, length);
	}

	public void setDirectByteBuffer(int parameterIndex, ByteBuffer x) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else if (MySqlEmbeddedDriver.SEND_LONG_DATA)
			stmt.mysql_stmt_send_long_data(statementHandle,parameterIndex-1,x,x.limit());
		else
			parameters.setDirectByteBuffer(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else
			parameters.setInputStream(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, x, length);
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else
			parameters.setInputStream(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, x, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
	{
		if (reader==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB);
		else
			parameters.setReader(parameterIndex-1,enum_field_types.MYSQL_TYPE_BLOB, reader, length);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException
	{
		setObject(parameterIndex, x, MySqlTypes.guessJavaType(x), 0);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
	{
		setObject(parameterIndex, x, targetSqlType, 0);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException
	{
        if (x==null) {
            setNull(parameterIndex, targetSqlType);
            return;
        }

        switch (targetSqlType)
        {
        case Types.ARRAY:           throw new SQLException("unsuported object type");
        case Types.BIGINT:          setLong(parameterIndex, MySqlTypes.longValue(x));
                                    break;

        case Types.BOOLEAN:
        case Types.BIT:             setBoolean(parameterIndex, MySqlTypes.booleanValue(x));
                                    break;

        case Types.DATALINK:        throw new SQLException("unsuported object type");
        case Types.DATE:            setDate(parameterIndex, MySqlTypes.dateValue(x));
                                    break;
        case Types.NUMERIC:
        case Types.DECIMAL:         setBigDecimal(parameterIndex, MySqlTypes.bigDecimalValue(x));
                                    break;
        case Types.DISTINCT:        throw new SQLException("unsuported object type");
        case Types.REAL:
        case Types.DOUBLE:          setDouble(parameterIndex, MySqlTypes.doubleValue(x));
                                    break;
        case Types.FLOAT:           setFloat(parameterIndex, MySqlTypes.floatValue(x));
                                    break;
        case Types.INTEGER:         setInt(parameterIndex, MySqlTypes.intValue(x));
                                    break;
        case Types.JAVA_OBJECT:     throw new SQLException("unsuported object type");

        case Types.CLOB:
        case Types.LONGVARCHAR:     if (x instanceof ByteBuffer && ((ByteBuffer)x).isDirect())
	                                    setDirectByteBuffer(parameterIndex, (ByteBuffer)x);
	                                else if (x instanceof Clob)
                                        setClob(parameterIndex, (Clob)x);
                                    else if (x instanceof String)
                                        setString(parameterIndex, x.toString());
                                    else if (x instanceof Reader)
                                        setCharacterStream(parameterIndex, (Reader)x, -1);
                                    else
                                        throw new SQLException("unsuported object type");
                                    break;

        case Types.NULL:            setNull(parameterIndex, targetSqlType);
                                    break;

        case Types.OTHER:           throw new SQLException("unsuported object type");

        case Types.SMALLINT:        setShort(parameterIndex, MySqlTypes.shortValue(x));
                                    break;

        case Types.STRUCT:          throw new SQLException("unsuported object type");

        case Types.TIME:            setTime(parameterIndex, MySqlTypes.timeValue(x));
                                    break;
        case Types.TIMESTAMP:       setTimestamp(parameterIndex, MySqlTypes.timestampValue(x));
                                    break;
        case Types.TINYINT:         setByte(parameterIndex, MySqlTypes.byteValue(x));
                                    break;
        case Types.BLOB:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:   if (x instanceof ByteBuffer && ((ByteBuffer)x).isDirect())
	                                    setDirectByteBuffer(parameterIndex, (ByteBuffer)x);
	                                else if (x instanceof byte[])
                                        setBytes(parameterIndex,(byte[])x);
                                    else if (x instanceof Blob)
                                         setBlob(parameterIndex, (Blob)x);
                                    else if (x instanceof InputStream)
                                        setBinaryStream(parameterIndex, (InputStream)x, -1);
                                    else
                                        throw new SQLException("unsuported object type");
                                    break;
        case Types.CHAR:
        case Types.VARCHAR:         setString(parameterIndex, MySqlTypes.stringValue(x));
                                    break;
        default:                    throw new SQLException("unsuported object type");
        }
	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {

	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {

	}

	@Override
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {

	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {

	}

	@Override
	public void setClob(int parameterIndex, Reader reader) throws SQLException {

	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {

	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
	{
		setNull(paramIndex,sqlType);
	}

	public void setString(int parameterIndex, String x) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex,enum_field_types.MYSQL_TYPE_STRING);
		else
			parameters.setString(parameterIndex-1, enum_field_types.MYSQL_TYPE_STRING, x);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1, enum_field_types.MYSQL_TYPE_DECIMAL);
		else
			parameters.setString(parameterIndex-1, enum_field_types.MYSQL_TYPE_DECIMAL, x.toString());
	}

	public void setURL(int parameterIndex, URL x) throws SQLException
	{
		setString(parameterIndex, x.toExternalForm());
	}

	public void setArray(int i, Array x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void setBlob(int i, Blob x) throws SQLException
	{
		if (x==null)
			parameters.setNull(i,enum_field_types.MYSQL_TYPE_BLOB);
		else
		{
			ByteBuffer buffer = ((MyBlob)x).buffer;
			if (MySqlEmbeddedDriver.SEND_LONG_DATA)
				stmt.mysql_stmt_send_long_data(statementHandle,i-1,buffer, (int)x.length());
			else
				parameters.setDirectByteBuffer(i-1, enum_field_types.MYSQL_TYPE_BLOB, buffer);
		}
	}

	public void setClob(int i, Clob x) throws SQLException
	{
		if (x==null)
			parameters.setNull(i,enum_field_types.MYSQL_TYPE_BLOB);
		else {
			ByteBuffer buffer = ((MyClob)x).buffer;
			if (MySqlEmbeddedDriver.SEND_LONG_DATA)
				stmt.mysql_stmt_send_long_data(statementHandle,i-1,buffer, (int)x.length());
			else
				parameters.setDirectByteBuffer(i-1, enum_field_types.MYSQL_TYPE_BLOB, buffer);
		}
	}

	public void setDate(int parameterIndex, Date x) throws SQLException
	{
		setDate(parameterIndex,x,connection.utcCalendar);
	}

	public ParameterMetaData getParameterMetaData() throws SQLException
	{
		if (paramMetaData==null) {
//			assertPrepared(statementHandle);

			long resHandle = stmt.mysql_stmt_param_metadata(statementHandle);
			if (resHandle==0) throw new NullPointerException("param_metadata");

			int paramCount = (parameters!=null) ? parameters.size() : 0;
			paramMetaData = new MyParameterMetaData(resHandle,paramCount,true);
		}
		return paramMetaData;
	}

	@Override
	public void setRowId(int parameterIndex, RowId x) throws SQLException {

	}

	@Override
	public void setNString(int parameterIndex, String value) throws SQLException {

	}

	@Override
	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, NClob value) throws SQLException {

	}

	@Override
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {

	}

	@Override
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {

	}

	public void setRef(int i, Ref x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public ResultSet executeQuery() throws SQLException
	{
		if (statementHandle==0)
			throw new SQLException("illegal state");
		if (columnCount <= 0)
			throw new SQLException("Select statement expected");

		execute();
		return result;
	}

	public ResultSetMetaData getMetaData() throws SQLException
	{
		if (resultMetaData==null) {
//			assertPrepared(statementHandle);

			long resHandle = stmt.mysql_stmt_result_metadata(statementHandle);

			resultMetaData = new MyResultSetMetaData(resHandle,columnCount,true);

			//  resHandle is released on close
		}
		return resultMetaData;
	}

	public void setTime(int parameterIndex, Time x) throws SQLException
	{
		setTime(parameterIndex,x,connection.utcCalendar);
	}

	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
	{
		setTimestamp(parameterIndex,x,connection.utcCalendar);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex-1,enum_field_types.MYSQL_TYPE_DATE);
		else {
			cal.setTime(x);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			parameters.setDate(parameterIndex-1, enum_field_types.MYSQL_TYPE_DATE, cal);
		}
	}

	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex,enum_field_types.MYSQL_TYPE_TIME);
		else {
			cal.setTime(x);
			cal.set(Calendar.YEAR,1970);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			parameters.setDate(parameterIndex-1, enum_field_types.MYSQL_TYPE_TIME, cal);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
	{
		if (x==null)
			parameters.setNull(parameterIndex,enum_field_types.MYSQL_TYPE_TIMESTAMP);
		else {
			cal.setTime(x);
			parameters.setDate(parameterIndex-1, enum_field_types.MYSQL_TYPE_TIMESTAMP, cal);
		}
	}

	public synchronized void close() throws SQLException
	{
		try {
			connection.unregister(this);
			cancel();  //  closes ResultSet

			if (resultColumns!=null)
				resultColumns.close();
			if (resultMetaData!=null)
				resultMetaData.close();
			if (paramMetaData!=null)
				paramMetaData.close();
			if (statementHandle!=0)
				stmt.mysql_stmt_close(statementHandle);
			if (parameters!=null)
				parameters.close();
		} finally {
			result = null;
			resultColumns = null;
			resultMetaData = null;
			paramMetaData = null;
			statementHandle = 0;
			parameters = null;
			resultMetaData = null;
			paramMetaData = null;
		}
	}

	public void cancel() throws SQLException
	{
		if (result!=null && !result.isClosed())
			result.close();
		//  don't set null - reuse
	}

	public boolean isClosed()
	{
		return statementHandle==0;
	}


	public int executeUpdate(String sql) throws SQLException
	{
		close();
		prepare(sql);
		return executeUpdate();
	}

	public boolean execute(String sql) throws SQLException
	{
		if (embeddedAdministration(sql)) {
			updateCount = 0;
			return false;
		}

		close();
		prepare(sql);
		return execute();
	}

	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int executeUpdate(String sql, int columnIndexes[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, int columnIndexes[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int executeUpdate(String sql, String columnNames[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean execute(String sql, String columnNames[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public ResultSet executeQuery(String sql) throws SQLException
	{
		close();
		prepare(sql);
		return executeQuery();
	}


	private MyPreparedResultSet preparedResult()
	{
		return (MyPreparedResultSet)result;
	}

/*
	@deprecated errors are handled in native code
	public static void handleError(long statementHandle) throws SQLException
	{
		int errno = stmt.mysql_stmt_errno(statementHandle);
		if (errno!=0) {
			String errorText = stmt.mysql_stmt_error(statementHandle);
			String sqlState = stmt.mysql_stmt_sqlstate(statementHandle);
			throw new SQLException(errorText,sqlState,errno);
		}
	}
*/

	protected static void assertPrepared(long statementHandle)
		throws SQLException
	{
		if (statementHandle==0)
			throw new SQLException("illegal state");
	}


	protected void prepare(String sql) throws SQLException
	{
        sql = EscapeProcessor.escapeSQLString(sql);

		connection.register(this);
		connection.useStatement(this);

		statementHandle = stmt.mysql_stmt_init(connection.connectionHandle);

		stmt.mysql_stmt_prepare(statementHandle,sql);

		stmt.mysql_stmt_attr_set(statementHandle,stmt.STMT_ATTR_UPDATE_MAX_LENGTH,false);
        if (fetchSize > 0)
            stmt.mysql_stmt_attr_set(statementHandle,stmt.STMT_ATTR_PREFETCH_ROWS,fetchSize);

        int paramCount = stmt.mysql_stmt_param_count(statementHandle);

		columnCount = stmt.mysql_stmt_field_count(statementHandle);

		/** allocate bind records   */
		if (paramCount > 0) {
			parameters = new BindArray(this,paramCount);
//				getParameterMetaData(); 
//			res.init_bind(paramBindHandle, paramMetaData.resHandle, paramCount);
		}
		else
			parameters = null;
	}


}
