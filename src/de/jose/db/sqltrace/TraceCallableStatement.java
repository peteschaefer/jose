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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class TraceCallableStatement
		extends TracePreparedStatement
		implements CallableStatement
{
	
	public TraceCallableStatement(CallableStatement stm, String sql)
	{
		super(stm,sql);
	}

	public Array getArray(int i) throws SQLException
	{ return ((CallableStatement)stmt).getArray(i); }
	
	public BigDecimal getBigDecimal(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getBigDecimal(parameterIndex); }

	/**	@deprecated	 */
 	public BigDecimal getBigDecimal(int parameterIndex, int scale)  throws SQLException
	{ return ((CallableStatement)stmt).getBigDecimal(parameterIndex,scale); }
	
	public Blob getBlob(int i)  throws SQLException
	{ return ((CallableStatement)stmt).getBlob(i); }
	
	public boolean getBoolean(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getBoolean(parameterIndex); }
	
	public byte getByte(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getByte(parameterIndex); }
	
	public byte[] getBytes(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getBytes(parameterIndex); }
	
	public Clob getClob(int i)  throws SQLException
	{ return ((CallableStatement)stmt).getClob(i); }
	
	public Date getDate(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getDate(parameterIndex); }
	
	public Date getDate(int parameterIndex, Calendar cal)  throws SQLException
	{ return ((CallableStatement)stmt).getDate(parameterIndex,cal); }
	
	public double getDouble(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getDouble(parameterIndex); }
	
	public float getFloat(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getFloat(parameterIndex); }
	
	public int getInt(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getInt(parameterIndex); }
	
	public long getLong(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getLong(parameterIndex); }
	
	public Object getObject(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getObject(parameterIndex); }
	
	public Object getObject(int i, Map map)  throws SQLException
	{ return ((CallableStatement)stmt).getObject(i,map); }

	public Object getObject(String parameterName, Map map) throws SQLException
	{
		return ((CallableStatement)stmt).getObject(parameterName,map);
	}

	public Ref getRef(int i)  throws SQLException
	{ return ((CallableStatement)stmt).getRef(i); }
	
	public short getShort(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getShort(parameterIndex); }
	
	public String getString(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getString(parameterIndex); }
	
	public Time getTime(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getTime(parameterIndex); }
	
	public Time getTime(int parameterIndex, Calendar cal)  throws SQLException
	{ return ((CallableStatement)stmt).getTime(parameterIndex,cal); }
	
	public Timestamp getTimestamp(int parameterIndex)  throws SQLException
	{ return ((CallableStatement)stmt).getTimestamp(parameterIndex); }
	
	public Timestamp getTimestamp(int parameterIndex, Calendar cal)  throws SQLException
	{ return ((CallableStatement)stmt).getTimestamp(parameterIndex); }
	
	public void registerOutParameter(int parameterIndex, int sqlType)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(parameterIndex,sqlType); }
	
	public void registerOutParameter(int parameterIndex, int sqlType, int scale)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(parameterIndex,sqlType,scale); }
	
	public void registerOutParameter(int paramIndex, int sqlType, String typeName)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(paramIndex,sqlType,typeName); }

    public void registerOutParameter(String parameterName, int sqlType)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(parameterName,sqlType); }

    public void registerOutParameter(String parameterName, int sqlType, int scale)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(parameterName,sqlType); }

    public void registerOutParameter(String parameterName, int sqlType, String typeName)  throws SQLException
	{ ((CallableStatement)stmt).registerOutParameter(parameterName,sqlType,typeName); }

    public URL getURL(int parameterIndex) throws SQLException {
        return ((CallableStatement)stmt).getURL(parameterIndex);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        setParameter(parameterName, val);
        ((CallableStatement)stmt).setURL(parameterName,val);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        setParameter(parameterName, null);
        ((CallableStatement)stmt).setNull(parameterName,sqlType);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        setParameter(parameterName, new Boolean(x));
        ((CallableStatement)stmt).setBoolean(parameterName,x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        setParameter(parameterName, new Byte(x));
        ((CallableStatement)stmt).setByte(parameterName,x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        setParameter(parameterName, new Short(x));
        ((CallableStatement)stmt).setShort(parameterName,x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        setParameter(parameterName, new Integer(x));
        ((CallableStatement)stmt).setInt(parameterName,x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        setParameter(parameterName, new Long(x));
        ((CallableStatement)stmt).setLong(parameterName,x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        setParameter(parameterName, new Float(x));
        ((CallableStatement)stmt).setFloat(parameterName,x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        setParameter(parameterName, new Double(x));
        ((CallableStatement)stmt).setDouble(parameterName,x);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setBigDecimal(parameterName,x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setString(parameterName,x);
    }

    public void setBytes(String parameterName, byte x[]) throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setBytes(parameterName,x);
    }

    public void setDate(String parameterName, Date x)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setDate(parameterName,x);
    }

    public void setTime(String parameterName, Time x)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setTime(parameterName,x);
    }

    public void setTimestamp(String parameterName, Timestamp x)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setTimestamp(parameterName,x);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setAsciiStream(parameterName,x,length);
    }

    public void setBinaryStream(String parameterName, InputStream x,
                                int length) throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setBinaryStream(parameterName,x,length);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setObject(parameterName,x,targetSqlType,scale);
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setObject(parameterName,x,targetSqlType);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setObject(parameterName,x);
    }

    public void setCharacterStream(String parameterName,
                                   Reader reader,
                                   int length) throws SQLException {
        setParameter(parameterName, reader);
        ((CallableStatement)stmt).setCharacterStream(parameterName,reader,length);
    }

    public void setDate(String parameterName, Date x, Calendar cal)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setDate(parameterName,x,cal);
    }

    public void setTime(String parameterName, Time x, Calendar cal)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setTime(parameterName,x,cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
            throws SQLException {
        setParameter(parameterName, x);
        ((CallableStatement)stmt).setTimestamp(parameterName,x,cal);
    }

    public void setNull(String parameterName, int sqlType, String typeName)
            throws SQLException {
        setParameter(parameterName, null);
        ((CallableStatement)stmt).setNull(parameterName,sqlType,typeName);
    }

    public String getString(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getString(parameterName);
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getBoolean(parameterName);
    }

    public byte getByte(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getByte(parameterName);
    }

    public short getShort(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getShort(parameterName);
    }

    public int getInt(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getInt(parameterName);
    }

    public long getLong(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getLong(parameterName);
    }

    public float getFloat(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getFloat(parameterName);
    }

    public double getDouble(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getDouble(parameterName);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getBytes(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getDate(parameterName);
     }

    public Time getTime(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getTime(parameterName);
     }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getTimestamp(parameterName);
    }

    public Object getObject(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getObject(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getBigDecimal(parameterName);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getRef(parameterName);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getBlob(parameterName);
    }

    public Clob getClob(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getClob(parameterName);
     }

    public Array getArray(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getArray(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal)
            throws SQLException {
        return ((CallableStatement)stmt).getDate(parameterName,cal);
    }

    public Time getTime(String parameterName, Calendar cal)
            throws SQLException {
        return ((CallableStatement)stmt).getTime(parameterName,cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
            throws SQLException {
        return ((CallableStatement)stmt).getTimestamp(parameterName,cal);
    }

    public URL getURL(String parameterName) throws SQLException {
        return ((CallableStatement)stmt).getURL(parameterName);
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return null;
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return null;
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {

    }

    public void setNString(String parameterName, String value) throws SQLException {

    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {

    }

    public void setNClob(String parameterName, NClob value) throws SQLException {

    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {

    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {

    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {

    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return null;
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return null;
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {

    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return null;
    }

    public String getNString(int parameterIndex) throws SQLException {
        return "";
    }

    public String getNString(String parameterName) throws SQLException {
        return "";
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return null;
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {

    }

    public void setClob(String parameterName, Clob x) throws SQLException {

    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {

    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {

    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {

    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {

    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {

    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {

    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {

    }

    public void setClob(String parameterName, Reader reader) throws SQLException {

    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {

    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {

    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return null;
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return null;
    }

    public boolean wasNull()  throws SQLException
	{ return ((CallableStatement)stmt).wasNull(); }

}
