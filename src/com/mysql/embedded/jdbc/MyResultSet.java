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
import com.mysql.embedded.api.res;
import com.mysql.embedded.util.ByteBufferInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * @author Peter Sch�fer
 */

public class MyResultSet
        implements java.sql.ResultSet
{
    public static final boolean STORE_RESULT = true;
    public static final boolean STREAM_RESULT =  false;

    /** associated connection   */
	protected MyConnection connection;
	/** associated statement    */
	protected MyStatement statement;
	/** use stored result set ?
     * one of STORED, STREAMING
     * */
	protected boolean stored;
	/** max. number of rows */
	protected int maxRows;
	/** current row number (starts at 1)    */
	protected int currentRowNum;
	/** handle to MYSQL_RES */
	protected long resHandle;
	/** handle to MYSQL_ROW   */
	protected long rowHandle;
	/** field lengths array   */
	protected int[] fieldLengths;
	/** meta data (otpional)    */
	protected MyResultSetMetaData metaData;
	/** last queried field index (for wasNull) */
	protected int fieldIndex;

	protected MyResultSet() { }

	protected MyResultSet(MyStatement statement, int columnCount,
	                      boolean stored, int maxRows) throws SQLException
	{
		setup(statement,statement.connection, columnCount,
                stored, maxRows,resHandle);

		if (stored)
			resHandle = api.mysql_store_result(connection.connectionHandle);
			//  store_result fetches the complete result set
		else
			resHandle = api.mysql_use_result(connection.connectionHandle);
			//  use_result fetches one row after another. the api calls are identical.
	}

	protected void setup(MyStatement statement, MyConnection connection,
	                        int columnCount, boolean stored, int maxRows, long resHandle) throws SQLException
	{
		this.statement = statement;
		this.connection = connection;
		this.stored = stored;
		this.maxRows = maxRows;
		this.fieldLengths = new int[columnCount];
		this.resHandle = resHandle;

		this.currentRowNum = 0;  //  = before first row
		this.metaData = null;    //  inited on demand
	}

	/**
	 * ctor with result set from db meta data
	 * @param connection
	 * @param resHandle
	 * @throws SQLException
	 */
	protected MyResultSet(MyConnection connection, long resHandle) throws SQLException
	{
		int columnCount = api.mysql_field_count(connection.connectionHandle);

		setup(null,connection, columnCount, STORE_RESULT,-1,resHandle);
	}

	public boolean next() throws SQLException
	{
		if (resHandle==0)
			return false;
		//  throw new SQLException("Result Set already closed");

		if (maxRows>=0 && currentRowNum >= maxRows) {       //  cut off ?
			close();
			return false;
		}

		rowHandle = api.mysql_fetch_row(resHandle);
		//  does not set errno !?
		if (rowHandle!=0) {
			//  note that row is actually empty. We have to call fetch_lengths to actually fill it ?!
			//  why is this necessary ????
			api.mysql_fetch_lengths(resHandle,fieldLengths,fieldLengths.length);
			currentRowNum++;
			return true;
		}
		else {
			close();
			return false;
		}
	}

	public void close() throws SQLException
	{
		try {
			if (resHandle!=0)
				api.mysql_free_result(resHandle);
		} finally {
			resHandle = 0;
			rowHandle = 0;
			currentRowNum = -1;
		}
	}

	public boolean isClosed()
	{
		return resHandle==0;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {

	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {

	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		return "";
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		return "";
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {

	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {

	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {

	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {

	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return null;
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return null;
	}

	public boolean wasNull() throws SQLException
	{
		assertColumn(rowHandle,fieldIndex);
		return res.is_null(rowHandle,fieldIndex-1);
	}

	public String getString(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		ByteBuffer buffer = getByteBuffer(columnIndex);
		if (buffer==null)
			return null;
		if (buffer.capacity()==0)
			return "";
		//  else:
		return connection.decodeString(buffer,buffer.limit());
	}

	public boolean getBoolean(int columnIndex) throws SQLException
	{
		return getInt(columnIndex) != 0;
	}

	public byte getByte(int columnIndex) throws SQLException
	{
		return (byte)getInt(columnIndex);
	}

	public short getShort(int columnIndex) throws SQLException
	{
		return (short)getInt(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		return res.get_int(rowHandle,(fieldIndex=columnIndex)-1);
	}

	public long getLong(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		return res.get_long(rowHandle,(fieldIndex=columnIndex)-1);
	}

	public float getFloat(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		return res.get_float(rowHandle,(fieldIndex=columnIndex)-1);
	}

	public double getDouble(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		return res.get_double(rowHandle,(fieldIndex=columnIndex)-1);
	}

	/** @deprecated */
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		return getBigDecimal(columnIndex);  //  scale ignored !?
	}

	protected ByteBuffer getByteBuffer(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		fieldIndex=columnIndex;

		ByteBuffer buffer = res.get_bytes(rowHandle, columnIndex-1, fieldLengths[columnIndex-1]);
		if (buffer!=null) buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	public byte[] getBytes(int columnIndex) throws SQLException
	{
		ByteBuffer buffer = getByteBuffer(columnIndex);
		if (buffer!=null) {
			byte[] result = new byte[buffer.limit()];
			buffer.get(result);
			return result;
		}
		else
			return null;
	}

	public java.sql.Date getDate(int columnIndex) throws SQLException
	{
		return getDate(columnIndex,connection.utcCalendar);
	}

	public Time getTime(int columnIndex) throws SQLException
	{
		return getTime(columnIndex,connection.utcCalendar);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException
	{
		return getTimestamp(columnIndex,connection.utcCalendar);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException
	{
		ByteBuffer buffer = getByteBuffer(columnIndex);
		if (buffer!=null)
			return new ByteBufferInputStream(buffer);
		else
			return null;
	}

	/** @deprecated */
	public InputStream getUnicodeStream(int columnIndex) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException
	{
		ByteBuffer buffer = getByteBuffer(columnIndex);
		if (buffer!=null)
			return new ByteBufferInputStream(buffer);
		else
			return null;
	}

	public String getString(String columnName) throws SQLException
	{
		return getString(findColumn(columnName));
	}

	public boolean getBoolean(String columnName) throws SQLException
	{
		return getBoolean(findColumn(columnName));
	}

	public byte getByte(String columnName) throws SQLException
	{
		return getByte(findColumn(columnName));
	}

	public short getShort(String columnName) throws SQLException
	{
		return getShort(findColumn(columnName));
	}

	public int getInt(String columnName) throws SQLException
	{
		return getInt(findColumn(columnName));
	}

	public long getLong(String columnName) throws SQLException
	{
		return getLong(findColumn(columnName));
	}

	public float getFloat(String columnName) throws SQLException
	{
		return getFloat(findColumn(columnName));
	}

	public double getDouble(String columnName) throws SQLException
	{
		return getDouble(findColumn(columnName));
	}

	/** @deprecated */
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
	{
		return getBigDecimal(findColumn(columnName),scale);
	}

	public byte[] getBytes(String columnName) throws SQLException
	{
		return getBytes(findColumn(columnName));
	}

	public Date getDate(String columnName) throws SQLException
	{
		return getDate(findColumn(columnName),connection.utcCalendar);
	}

	public Time getTime(String columnName) throws SQLException
	{
		return getTime(findColumn(columnName),connection.utcCalendar);
	}

	public Timestamp getTimestamp(String columnName) throws SQLException
	{
		return getTimestamp(findColumn(columnName),connection.utcCalendar);
	}

	public InputStream getAsciiStream(String columnName) throws SQLException
	{
		return getAsciiStream(findColumn(columnName));
	}

	/** @deprecated */
	public InputStream getUnicodeStream(String columnName) throws SQLException
	{
		return getUnicodeStream(findColumn(columnName));
	}

	public InputStream getBinaryStream(String columnName) throws SQLException
	{
		return getBinaryStream(findColumn(columnName));
	}

	public SQLWarning getWarnings() throws SQLException
	{
		return connection.getWarnings();
	}

	public void clearWarnings() throws SQLException
	{
		connection.clearWarnings();
	}

	public String getCursorName() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public ResultSetMetaData getMetaData() throws SQLException
	{
		if (metaData==null) metaData = new MyResultSetMetaData(resHandle, fieldLengths.length, false);
		return metaData;
	}

	public Object getObject(int columnIndex) throws SQLException
	{
		switch (getMetaData().getColumnType(columnIndex))
		{
		case Types.BIGINT:				return getBigDecimal(columnIndex);
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:		return getBytes(columnIndex);
		case Types.BIT:					boolean b = getBoolean(columnIndex);
										if (wasNull())
											return null;
										else
											return (b?Boolean.TRUE:Boolean.FALSE);
		case Types.BLOB:				return getBlob(columnIndex);
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:			return getString(columnIndex);
		case Types.CLOB:				return getClob(columnIndex);
		case Types.DATE:				return getDate(columnIndex);
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.REAL:				double d = getDouble(columnIndex);
										if (wasNull())
											return null;
										else
											return new Double(d);
		case Types.FLOAT:				float f = getFloat(columnIndex);
										if (wasNull())
											return null;
										else
											return new Float(f);
		case Types.INTEGER:				int i = getInt(columnIndex);
										if (wasNull())
											return null;
										else
											return new Integer(i);
		case Types.SMALLINT:
		case Types.TINYINT:				short s = getShort(columnIndex);
										if (wasNull())
											return null;
										else
											return new Short(s);
		case Types.TIME:				return getTime(columnIndex);
		case Types.TIMESTAMP:			return getTimestamp(columnIndex);

		default:
			throw new SQLException("type "+getMetaData().getColumnTypeName(columnIndex)+" not supported");
		}
	}

	public Object getObject(String columnName) throws SQLException
	{
		return getObject(findColumn(columnName));
	}

	public int findColumn(String columnName) throws SQLException
	{
		return ((MyResultSetMetaData)getMetaData()).findColumn(columnName);
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException
	{
		try {
			return new InputStreamReader(getAsciiStream(columnIndex),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SQLException("unsupported encoding: "+e.getLocalizedMessage());
		}
	}

	public Reader getCharacterStream(String columnName) throws SQLException
	{
		return getCharacterStream(findColumn(columnName));
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException
	{
		String text = getString(columnIndex);
		if (text!=null)
			return new BigDecimal(text);
		else
			return null;
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException
	{
		return getBigDecimal(findColumn(columnName));
	}

	public boolean isBeforeFirst() throws SQLException
	{
		return currentRowNum==0;
	}

	public boolean isAfterLast() throws SQLException
	{
		return currentRowNum==-1;
	}

	public boolean isFirst() throws SQLException
	{
		return currentRowNum==1;
	}

	public boolean isLast() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void beforeFirst() throws SQLException
	{
		absolute(0);
	}

	public void afterLast() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean first() throws SQLException
	{
		beforeFirst();
		return next();
	}

	public boolean last() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getRow() throws SQLException
	{
		return currentRowNum;
	}

	public boolean absolute(int row) throws SQLException
	{
		if ((row-1)==currentRowNum)
			return next();    //  that was easy ;-)

		if (stored) {
			//  seek
			api.mysql_data_seek(resHandle, (currentRowNum=(row-1)));
			return next();
		}
		else if (currentRowNum < (row-1)) {
			//  step forward
			while (currentRowNum < (row-1))
				if (!next()) return false;
			return next();
		}
		else    //  can't scroll backwards
			throw new SQLException("can't scroll");
	}

	public boolean relative(int rows) throws SQLException
	{
		return absolute(currentRowNum+rows);
	}

	public boolean previous() throws SQLException
	{
		return relative(-1);
	}

	public void setFetchDirection(int direction) throws SQLException
	{
		switch (direction)
		{
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
		throw new UnsupportedOperationException();
	}

	public int getFetchSize() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getType() throws SQLException
	{
        if (stored)
            return ResultSet.TYPE_SCROLL_INSENSITIVE;
        else
            return ResultSet.TYPE_FORWARD_ONLY;
    }

	public int getConcurrency() throws SQLException
	{
		return ResultSet.CONCUR_READ_ONLY;
	}

	public boolean rowUpdated() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean rowInserted() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean rowDeleted() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateNull(int columnIndex) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateByte(int columnIndex, byte x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateShort(int columnIndex, short x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateInt(int columnIndex, int x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateLong(int columnIndex, long x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateFloat(int columnIndex, float x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateDouble(int columnIndex, double x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateString(int columnIndex, String x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBytes(int columnIndex, byte x[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateDate(int columnIndex, Date x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateTime(int columnIndex, Time x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateObject(int columnIndex, Object x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateNull(String columnName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateByte(String columnName, byte x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateShort(String columnName, short x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateInt(String columnName, int x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateLong(String columnName, long x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateFloat(String columnName, float x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateDouble(String columnName, double x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateString(String columnName, String x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBytes(String columnName, byte x[]) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateDate(String columnName, Date x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateTime(String columnName, Time x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateObject(String columnName, Object x, int scale) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateObject(String columnName, Object x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void insertRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void deleteRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void refreshRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void cancelRowUpdates() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void moveToInsertRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void moveToCurrentRow() throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Statement getStatement() throws SQLException
	{
		return statement;
	}

	public Object getObject(int i, Map map) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Ref getRef(int i) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Blob getBlob(int i) throws SQLException
	{
		ByteBuffer buffer = getByteBuffer(i);
		if (buffer!=null)
			return new MyBlob(buffer);
		else
			return null;
	}

	public Clob getClob(int i) throws SQLException
	{
		ByteBuffer buffer = getByteBuffer(i);
		if (buffer!=null)
			return new MyClob(buffer);
		else
			return null;
	}

	public Array getArray(int i) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Object getObject(String colName, Map map) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Ref getRef(String colName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Blob getBlob(String colName) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Clob getClob(String colName) throws SQLException
	{
		return getClob(findColumn(colName));
	}

	public Array getArray(String colName) throws SQLException
	{
		return getArray(findColumn(colName));
	}

	protected ShortBuffer getDatePartsShortBuffer(int columnIndex) throws SQLException
	{
		assertColumn(rowHandle,columnIndex);
		return res.get_date(rowHandle,(fieldIndex=columnIndex)-1,connection.sbuffer);
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException
	{
		ShortBuffer parts = getDatePartsShortBuffer(columnIndex);
		return getDate(parts, cal);
	}

	protected static Date getDate(ShortBuffer parts, Calendar cal)
	{
		if (parts==null) return null;

		cal.set(Calendar.YEAR,  parts.get(0));
		cal.set(Calendar.MONTH, Calendar.JANUARY+Math.max(0,+parts.get(1)-1));
		cal.set(Calendar.DAY_OF_MONTH,  Math.max(1,parts.get(2)));
		cal.set(Calendar.HOUR_OF_DAY,  0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		long millis = cal.getTimeInMillis();
		return new Date(millis);
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException
	{
		return getDate(findColumn(columnName),cal);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException
	{
		ShortBuffer parts = getDatePartsShortBuffer(columnIndex);
		return getTime(parts, cal);
	}

	protected static Time getTime(ShortBuffer parts, Calendar cal)
	{
		if (parts==null)
			return null;

		cal.set(Calendar.YEAR,  0);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH,  1);
		cal.set(Calendar.HOUR_OF_DAY,  parts.get(3));
		cal.set(Calendar.MINUTE,parts.get(4));
		cal.set(Calendar.SECOND,parts.get(5));
		cal.set(Calendar.MILLISECOND,parts.get(6));

		long millis = cal.getTimeInMillis();
		return new Time(millis);
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException
	{
		return getTime(findColumn(columnName),cal);
	}

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException
	{
		return getTimestamp(findColumn(columnName),cal);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
	{
		ShortBuffer parts = getDatePartsShortBuffer(columnIndex);
		return getTimestamp(parts, cal);
	}

	protected static Timestamp getTimestamp(ShortBuffer parts, Calendar cal)
	{
		if (parts==null)
			return null;

		cal.set(Calendar.YEAR,  parts.get(0));
		cal.set(Calendar.MONTH, Calendar.JANUARY+Math.max(0,parts.get(1)-1));
		cal.set(Calendar.DAY_OF_MONTH,  Math.max(1,parts.get(2)));
		cal.set(Calendar.HOUR_OF_DAY,  parts.get(3));
		cal.set(Calendar.MINUTE,parts.get(4));
		cal.set(Calendar.SECOND,parts.get(5));
		cal.set(Calendar.MILLISECOND,parts.get(6));

		long millis = cal.getTimeInMillis();
		return new Timestamp(millis);
	}

	public URL getURL(int columnIndex) throws SQLException
	{
		try {
			return new URL(getString(columnIndex));
		} catch (MalformedURLException e) {
			throw new SQLException("malformed url: "+e.getLocalizedMessage());
		}
	}

	public URL getURL(String columnName) throws SQLException
	{
		return getURL(findColumn(columnName));
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateRef(String columnName, Ref x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateBlob(String columnName, Blob x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateClob(String columnName, Clob x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateArray(int columnIndex, Array x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public void updateArray(String columnName, Array x) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {

	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {

	}

	@Override
	public int getHoldability() throws SQLException {
		return 0;
	}

	protected void assertColumn(long rowHandle, int columnIndex) throws SQLException
	{
		if (rowHandle==0)
			throw new SQLException("illegal result set status");
		if (columnIndex < 1 || columnIndex > fieldLengths.length)
			throw new SQLException("column index out of range");
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
