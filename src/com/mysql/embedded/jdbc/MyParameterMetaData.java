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
import com.mysql.embedded.api.res;

import java.sql.SQLException;
import java.nio.ByteBuffer;

/**
 * @author Peter Sch�fer
 */

public class MyParameterMetaData
        implements java.sql.ParameterMetaData
{

	protected long resHandle;
	boolean deallocHandle;
	protected MYSQL_FIELD[] fields;

	public MyParameterMetaData(long resHandle, int paramCount, boolean deallocHandle)
	{
		this.resHandle = resHandle;
		this.deallocHandle = deallocHandle;

		fields = MYSQL_FIELD.newArray(api.mysql_fetch_fields(resHandle),paramCount);
	}

	public void close()
	{
		try {
			if (resHandle!=0 && deallocHandle)
				api.mysql_free_result(resHandle);
		} finally {
			resHandle = 0;
		}
	}

	public int getParameterCount() throws SQLException
	{
		return fields.length;
	}

	public int getParameterMode(int param) throws SQLException
	{
		return 0;
	}

	public int getParameterType(int param) throws SQLException
	{
		int mysql_type = fields[param].getType();
		return MySqlTypes.asJavaType(mysql_type);
	}

	public int getPrecision(int param) throws SQLException
	{
		return fields[param].getDecimals();
	}

	public int getScale(int param) throws SQLException
	{
		return fields[param].getLength();
	}

	public int isNullable(int param) throws SQLException
	{
		return 0;   //(fields[param].getFlags() && ) != 0;
	}

	public boolean isSigned(int param) throws SQLException
	{
		return false;   //(fields[param].getFlags() && ) != 0;
	}

	public String getParameterClassName(int param) throws SQLException
	{
		int mysql_type = fields[param].getType();
		int java_type = MySqlTypes.asJavaType(mysql_type);
		return MySqlTypes.getJavaClassName(java_type);
	}

	public String getParameterTypeName(int param) throws SQLException
	{
		int mysql_type = fields[param].getType();
		return MySqlTypes.getMysqlTypeName(mysql_type);
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
