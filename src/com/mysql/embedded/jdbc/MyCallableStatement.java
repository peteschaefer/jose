//-----------------------------------------------------------------------------
// class MyCallableStatement
//
// Copyright (C) Healy Hudson Gmbh 1999-2005
// \$Header$
//-----------------------------------------------------------------------------

package com.mysql.embedded.jdbc;

import com.mysql.embedded.api.api;
import com.mysql.embedded.api.stmt;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * MyCallableStatement
 *
 * CallabeStatement
 *
 * MySQL has no special API for stored procedures.
 * OUT and INOUT parameters are nothing but server variables.
 *
 * MyCallableStatement adds support for managing parameters
 * through explicit "SET" and "SELECT" statements,
 * but functionally it is not different from PreparedStatement.
 *
 * set..(int parameterIndex,...) methods are not supported.
 *
 * Array and Ref are not supported
 *
 * @author Peter Sch�fer
 */
public class MyCallableStatement
				extends MyPreparedStatement
				implements CallableStatement
{
	private boolean wasNull = false;

	private PreparedStatement prepareSet(String parameterName)
			throws SQLException
	{
		if (!parameterName.startsWith("@")) parameterName = "@"+parameterName;
		return connection.prepareStatement("SET "+parameterName+" = ?",
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}

	private ResultSet prepareGet(String parameterName)
			throws SQLException
	{
		if (!parameterName.startsWith("@")) parameterName = "@"+parameterName;
		PreparedStatement get = connection.prepareStatement("SELECT "+parameterName,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet res = get.executeQuery();
		if (res.next())
			return res;
		else {
			res.close();
			return null;
		}
	}

	public MyCallableStatement(MyConnection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
        super(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        //  we only support stored result sets, see below why this is so
        if (resultSetType!=ResultSet.TYPE_SCROLL_INSENSITIVE)
            throw new UnsupportedOperationException();
	}

    /**
     * callable statements can return any number of result sets.
     * However, we can't rely on columnCount.
     * That's why we call mysql_store_result() to retrieve it
     *
     * @return
     * @throws SQLException
     */
    protected boolean retrieveResult() throws SQLException
    {
        stmt.mysql_stmt_store_result(statementHandle);
        long resHandle = stmt.mysql_stmt_result_metadata(statementHandle);

        if (resHandle!=0L) {
            //  select
            columnCount = api.mysql_num_fields(resHandle);
            //  allocate bind records
            resultColumns = new BindArray(this,columnCount);

            resultMetaData = new MyResultSetMetaData(resHandle,columnCount,true);
            resultColumns.initialize(resultMetaData);

            if (result==null)
                result = new MyPreparedResultSet(this,true);
            else
                ((MyPreparedResultSet)result).reset(true);
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

    public Array getArray(int i)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Array getArray(String parameterName)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public BigDecimal getBigDecimal(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	/**@deprecated*/
	public BigDecimal getBigDecimal(int parameterIndex, int scale)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public BigDecimal getBigDecimal(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		BigDecimal result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getBigDecimal(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Blob getBlob(int i)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Blob getBlob(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Blob result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getBlob(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public boolean getBoolean(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean getBoolean(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		boolean result = false;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getBoolean(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public byte getByte(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public byte getByte(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		byte result = 0;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getByte(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public byte[] getBytes(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public byte[] getBytes(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		byte[] result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getBytes(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Clob getClob(int i)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Clob getClob(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Clob result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getClob(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Date getDate(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Date getDate(int parameterIndex, Calendar cal)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Date getDate(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Date result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getDate(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Date getDate(String parameterName, Calendar cal)
			throws SQLException
	{
		ResultSet res = null;
		Date result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getDate(1,cal);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public double getDouble(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public double getDouble(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		double result = 0.0;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getDouble(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public float getFloat(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public float getFloat(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		float result = 0.0f;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getFloat(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public int getInt(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int getInt(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		int result = 0;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getInt(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public long getLong(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public long getLong(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		long result = 0L;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getLong(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Object getObject(int i, Map map)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Object getObject(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Object getObject(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Object result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getObject(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Object getObject(String parameterName, Map map)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Ref getRef(int i)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Ref getRef(String parameterName)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public short getShort(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public short getShort(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		short result = 0;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getShort(1);
				wasNull = res.wasNull();
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public String getString(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getString(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		String result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getString(1);
				wasNull =(result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Time getTime(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Time getTime(int parameterIndex, Calendar cal)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Time getTime(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Time result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getTime(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Time getTime(String parameterName, Calendar cal)
			throws SQLException
	{
		ResultSet res = null;
		Time result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getTime(1,cal);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Timestamp getTimestamp(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public Timestamp getTimestamp(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		Timestamp result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getTimestamp(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException
	{
		ResultSet res = null;
		Timestamp result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getTimestamp(1,cal);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	public URL getURL(int parameterIndex)
			throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public URL getURL(String parameterName)
			throws SQLException
	{
		ResultSet res = null;
		URL result = null;
		try {
			res = prepareGet(parameterName);
			if (res!=null) {
				result = res.getURL(1);
				wasNull = (result!=null);
			}
		} finally {
			if (res!=null) res.close();
		}
		return result;
	}

	@Override
	public RowId getRowId(int parameterIndex) throws SQLException {
		return null;
	}

	@Override
	public RowId getRowId(String parameterName) throws SQLException {
		return null;
	}

	@Override
	public void setRowId(String parameterName, RowId x) throws SQLException {

	}

	@Override
	public void setNString(String parameterName, String value) throws SQLException {

	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {

	}

	@Override
	public void setNClob(String parameterName, NClob value) throws SQLException {

	}

	@Override
	public void setClob(String parameterName, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {

	}

	@Override
	public void setNClob(String parameterName, Reader reader, long length) throws SQLException {

	}

	@Override
	public NClob getNClob(int parameterIndex) throws SQLException {
		return null;
	}

	@Override
	public NClob getNClob(String parameterName) throws SQLException {
		return null;
	}

	@Override
	public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {

	}

	@Override
	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		return null;
	}

	@Override
	public SQLXML getSQLXML(String parameterName) throws SQLException {
		return null;
	}

	@Override
	public String getNString(int parameterIndex) throws SQLException {
		return "";
	}

	@Override
	public String getNString(String parameterName) throws SQLException {
		return "";
	}

	@Override
	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getNCharacterStream(String parameterName) throws SQLException {
		return null;
	}

	@Override
	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		return null;
	}

	@Override
	public Reader getCharacterStream(String parameterName) throws SQLException {
		return null;
	}

	@Override
	public void setBlob(String parameterName, Blob x) throws SQLException {

	}

	@Override
	public void setClob(String parameterName, Clob x) throws SQLException {

	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {

	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {

	}

	@Override
	public void setAsciiStream(String parameterName, InputStream x) throws SQLException {

	}

	@Override
	public void setBinaryStream(String parameterName, InputStream x) throws SQLException {

	}

	@Override
	public void setCharacterStream(String parameterName, Reader reader) throws SQLException {

	}

	@Override
	public void setNCharacterStream(String parameterName, Reader value) throws SQLException {

	}

	@Override
	public void setClob(String parameterName, Reader reader) throws SQLException {

	}

	@Override
	public void setBlob(String parameterName, InputStream inputStream) throws SQLException {

	}

	@Override
	public void setNClob(String parameterName, Reader reader) throws SQLException {

	}

	@Override
	public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
		return null;
	}

	@Override
	public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
		return null;
	}

	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException
	{
		/*noop*/
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException
	{
		/*noop*/
	}

	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException
	{
		/*noop*/
	}

	public void registerOutParameter(String parameterName, int sqlType, int scale)
			throws SQLException
	{
		/*noop*/
	}

	public void registerOutParameter(String parameterName, int sqlType, String typeName)
			throws SQLException
	{
		/*noop*/
	}

	public void registerOutParameter(int paramIndex, int sqlType, String typeName)
			throws SQLException
	{
		/*noop*/
	}

	public void setAsciiStream(String parameterName, InputStream x, int length)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setAsciiStream(1,x,length);
		set.executeUpdate();
	}

	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setBigDecimal(1,x);
		set.executeUpdate();
	}

	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setBinaryStream(1,x,length);
		set.executeUpdate();
	}

	public void setBoolean(String parameterName, boolean x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setBoolean(1,x);
		set.executeUpdate();
	}

	public void setByte(String parameterName, byte x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setByte(1,x);
		set.executeUpdate();
	}

	public void setBytes(String parameterName, byte x[])
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setBytes(1,x);
		set.executeUpdate();
	}

	public void setCharacterStream(String parameterName, Reader reader, int length)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setCharacterStream(1,reader,length);
		set.executeUpdate();
	}

	public void setDate(String parameterName, Date x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setDate(1,x);
		set.executeUpdate();
	}

	public void setDate(String parameterName, Date x, Calendar cal)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setDate(1,x,cal);
		set.executeUpdate();
	}

	public void setDouble(String parameterName, double x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setDouble(1,x);
		set.executeUpdate();
	}

	public void setFloat(String parameterName, float x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setFloat(1,x);
		set.executeUpdate();
	}

	public void setInt(String parameterName, int x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setInt(1,x);
		set.executeUpdate();
	}

	public void setLong(String parameterName, long x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setLong(1,x);
		set.executeUpdate();
	}

	public void setNull(String parameterName, int sqlType)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setNull(1,sqlType);
		set.executeUpdate();
	}

	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setNull(1,sqlType,typeName);
		set.executeUpdate();
	}

	public void setObject(String parameterName, Object x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setObject(1,x);
		set.executeUpdate();
	}

	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setObject(1,x,targetSqlType);
		set.executeUpdate();
	}

	public void setObject(String parameterName, Object x, int targetSqlType, int scale)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setObject(1,x,targetSqlType, scale);
		set.executeUpdate();
	}

	public void setShort(String parameterName, short x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setShort(1,x);
		set.executeUpdate();
	}

	public void setString(String parameterName, String x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setString(1,x);
		set.executeUpdate();
	}

	public void setTime(String parameterName, Time x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setTime(1,x);
		set.executeUpdate();
	}

	public void setTime(String parameterName, Time x, Calendar cal)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setTime(1,x,cal);
		set.executeUpdate();
	}

	public void setTimestamp(String parameterName, Timestamp x)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setTimestamp(1,x);
		set.executeUpdate();
	}

	public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setTimestamp(1,x,cal);
		set.executeUpdate();
	}

	public void setURL(String parameterName, URL val)
			throws SQLException
	{
		PreparedStatement set = prepareSet(parameterName);
		set.setURL(1,val);
		set.executeUpdate();
	}

	public boolean wasNull()
			throws SQLException
	{
		return wasNull;
	}
}
