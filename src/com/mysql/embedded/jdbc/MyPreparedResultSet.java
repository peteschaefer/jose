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

package com.mysql.embedded.jdbc;

import com.mysql.embedded.api.MYSQL_TIME;
import com.mysql.embedded.api.stmt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.*;
import java.util.Calendar;

/**
 * @author Peter Schäfer
 */

public class MyPreparedResultSet
        extends MyResultSet
        implements java.sql.ResultSet
{
	protected BindArray resultColumns;

    /* @deprecated */
    protected static final ByteOrder DATE_BYTE_ORDER = ByteOrder.nativeOrder();

    public MyPreparedResultSet(MyPreparedStatement aStatement, boolean wasStored)
	        throws SQLException
	{
		this.statement = aStatement;
		super.setup(statement, statement.connection, preparedStatement().columnCount,
		        (statement.stored||wasStored) ? STORE_RESULT:STREAM_RESULT, statement.maxRows, 0);
		resultColumns = preparedStatement().resultColumns;
		reset(wasStored);
	}

	protected void reset(boolean wasStored)
		throws SQLException
	{
        if (stored && !wasStored) {
			stmt.mysql_stmt_store_result(preparedStatement().statementHandle);
			//  store_result fetches the complete result set
            //  note that store_result has already been called for CallableStatements
        }

		//  bind result records
		stmt.mysql_stmt_bind_result(preparedStatement().statementHandle, resultColumns.nativeAddress);
		currentRowNum = 0;
	}


	public void close() throws SQLException
	{
		//  do nothing, keep handles for next execution
		try {

			if (preparedStatement().statementHandle != 0)
				stmt.mysql_stmt_free_result(preparedStatement().statementHandle);

		} finally {
			super.close();
		}
	}

	public boolean isClosed()
	{
		return resultColumns==null;
	}

	public boolean next() throws SQLException
	{
		boolean result = stmt.mysql_stmt_fetch(preparedStatement().statementHandle);
		if (result) {
			currentRowNum++;
			//  look for oversized columns
			for (int i=0; i < resultColumns.size(); i++)
				if (resultColumns.exceedsCapacity(i))
					resultColumns.fetchRest(i, preparedStatement().statementHandle);
		}
		return result;
	}


	public boolean wasNull() throws SQLException
	{
		return resultColumns.isNull(fieldIndex-1);
	}

	protected ByteBuffer getByteBuffer(int columnIndex) throws SQLException
	{
		return resultColumns.getByteBuffer((fieldIndex=columnIndex)-1);
	}

	public byte getByte(int columnIndex) throws SQLException
	{
		return resultColumns.getByte((fieldIndex=columnIndex)-1);
	}

	public double getDouble(int columnIndex) throws SQLException
	{
		return resultColumns.getDouble((fieldIndex=columnIndex)-1);
	}

	public float getFloat(int columnIndex) throws SQLException
	{
		return resultColumns.getFloat((fieldIndex=columnIndex)-1);
	}

	public int getInt(int columnIndex) throws SQLException
	{
		return resultColumns.getInt((fieldIndex=columnIndex)-1);
	}

	public long getLong(int columnIndex) throws SQLException
	{
		return resultColumns.getLong((fieldIndex=columnIndex)-1);
	}

	public short getShort(int columnIndex) throws SQLException
	{
		return resultColumns.getShort((fieldIndex=columnIndex)-1);
	}

	public boolean absolute(int row) throws SQLException
	{
		if ((row-1)==currentRowNum)
			return next();    //  that was easy ;-)

		if (stored) {
			//  seek
			stmt.mysql_stmt_data_seek(preparedStatement().statementHandle, (currentRowNum=(row-1)));
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

	public boolean getBoolean(int columnIndex) throws SQLException
	{
		return resultColumns.getByte((fieldIndex=columnIndex)-1) != 0;
	}


	protected MYSQL_TIME getDateParts(int columnIndex) throws SQLException
	{
		return resultColumns.getDateParts((fieldIndex=columnIndex)-1);
	}

	public String getString(int columnIndex) throws SQLException
	{
		return resultColumns.getString((fieldIndex=columnIndex)-1);
	}


	public ResultSetMetaData getMetaData() throws SQLException
	{
		//  PreparedStatement already keeps (one set of) meta data
		return preparedStatement().getMetaData();
	}


	private final MyPreparedStatement preparedStatement()
	{
		return (MyPreparedStatement)statement;
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException
	{
		MYSQL_TIME parts = getDateParts(columnIndex);
		return getTime(parts,cal);
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException
	{
		MYSQL_TIME parts = getDateParts(columnIndex);
		return getDate(parts,cal);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
	{
		MYSQL_TIME parts = getDateParts(columnIndex);
		return getTimestamp(parts,cal);
	}

	public static Date getDate(MYSQL_TIME parts, Calendar cal)
	{
		if (parts==null) return null;

		cal.set(Calendar.YEAR,  parts.getYear());
		cal.set(Calendar.MONTH, Calendar.JANUARY+Math.max(0,parts.getMonth()-1));
		cal.set(Calendar.DAY_OF_MONTH,  Math.max(1,parts.getDay()));
		cal.set(Calendar.HOUR_OF_DAY,  0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);

		long millis = cal.getTimeInMillis();
		return new Date(millis);
	}

	public static Time getTime(MYSQL_TIME parts, Calendar cal)
	{
		if (parts==null) return null;

		cal.set(Calendar.YEAR,  0);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH,  1);
		cal.set(Calendar.HOUR_OF_DAY,  parts.getDay());
		cal.set(Calendar.MINUTE,parts.getMinute());
		cal.set(Calendar.SECOND,parts.getSecond());
		cal.set(Calendar.MILLISECOND,parts.getSecondPart());

		long millis = cal.getTimeInMillis();
		return new Time(millis);
	}

	public static Timestamp getTimestamp(MYSQL_TIME parts, Calendar cal)
	{
		if (parts==null) return null;

		cal.set(Calendar.YEAR,  parts.getYear());
		cal.set(Calendar.MONTH, Calendar.JANUARY+Math.max(0,parts.getMonth()-1));
		cal.set(Calendar.DAY_OF_MONTH,  Math.max(1,parts.getDay()));
		cal.set(Calendar.HOUR_OF_DAY,  parts.getHour());
		cal.set(Calendar.MINUTE,parts.getMinute());
		cal.set(Calendar.SECOND,parts.getSecond());
		cal.set(Calendar.MILLISECOND,parts.getSecondPart());

		long millis = cal.getTimeInMillis();
		return new Timestamp(millis);
	}
}
