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
import com.mysql.embedded.api.MYSQL_FIELD;

import java.sql.SQLException;

/**
 * @author Peter Sch�fer
 */

public class MyResultSetMetaData
        implements java.sql.ResultSetMetaData
{

	protected long resHandle;
	protected MYSQL_FIELD[] fields;
	protected boolean deallocHandle;

	protected MyResultSetMetaData(long resHandle, int columnCount, boolean deallocHandle)
	{
		this.resHandle = resHandle;
		this.deallocHandle = deallocHandle;

		fields = MYSQL_FIELD.newArray(api.mysql_fetch_fields(resHandle),columnCount);
	}

	protected void close()
	{
		try {
			if (resHandle!=0 && deallocHandle)
				api.mysql_free_result(resHandle);
		} finally {
			resHandle = 0;
		}
	}

	public int getColumnCount() throws SQLException
	{
		return fields.length;
	}

	public int getColumnDisplaySize(int column) throws SQLException
	{
		return (int)fields[column-1].getMaxLength();
	}

	public int getMySqlColumnType(int column)
	{
		return fields[column-1].getType();
	}

	public int getColumnType(int column) throws SQLException
	{
		int mysql_type = fields[column-1].getType();
		return MySqlTypes.asJavaType(mysql_type);
	}

	public int getLength(int column)
	{
		return fields[column-1].getLength();
	}

	public int getPrecision(int column)
	{
		return fields[column-1].getDecimals();
	}

	public int getScale(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public int isNullable(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isAutoIncrement(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isCaseSensitive(int column) throws SQLException
	{
		return false;
	}

	public boolean isCurrency(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isDefinitelyWritable(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isReadOnly(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isSearchable(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isSigned(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public boolean isWritable(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getCatalogName(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getColumnClassName(int column) throws SQLException
	{
        return MySqlTypes.getJavaClassName(getColumnType(column));
	}

	public String getColumnLabel(int column) throws SQLException
	{
		return getColumnName(column);
	}

	public String getColumnName(int column) throws SQLException
	{
		return fields[column-1].getName();
	}

	/**
	 * MySQL extension: get original column name,
	 * if the result column has an alias
	 *
	 * @param column
	 * @return the original column name
	 */
	public String getOriginalColumnName(int column)
	{
		return fields[column-1].getOrgName();
	}

	/**
	 * MySQL extension: get original column name,
	 * if the result column has an alias
	 *
	 * @param column
	 * @return the original column name
	 */
	public String getOriginalTableName(int column)
	{
		return fields[column-1].getOrgTable();
	}


	public String getColumnTypeName(int column) throws SQLException
	{
		int mysql_type = fields[column-1].getType();
		return MySqlTypes.getMysqlTypeName(mysql_type);
	}

	public String getSchemaName(int column) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	public String getTableName(int column) throws SQLException
	{
		return fields[column-1].getTable();
	}

	protected int findColumn(String columnName)
	{
		for (int i=0; i<fields.length; i++)
			if (fields[i].getName().equalsIgnoreCase(columnName))
				return i+1;
		return -1;
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
