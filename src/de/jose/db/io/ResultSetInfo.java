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

package de.jose.db.io;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Result Set info at the beginning of a database dump file
 *
 * @author Peter Schäfer
 */

public class ResultSetInfo
        implements Serializable
{
    /** serial version  */
	static final long serialVersionUID = 2486422509274226752L;

    /** ZIP entry name  */
    private String  zipName;

    /** schema names */
    private String[] schemaNames;

    /** table names */
    private String[] tableNames;

    /** column names    */
    private String[] columnNames;

    /** column data types   */
    private int[]    dataTypes;

    /** number of rows  */
    protected int      rowCount;


    public ResultSetInfo(ResultSetMetaData rmd, String zip)
        throws SQLException
    {
        zipName = zip;
        int count = rmd.getColumnCount();
        schemaNames = new String[count+1];
        tableNames = new String[count+1];
        columnNames = new String[count+1];
        dataTypes = new int[count+1];

        for (int i=1; i <= count; i++) {
            schemaNames[i] = rmd.getSchemaName(i);
            tableNames[i] = rmd.getTableName(i);
            columnNames[i] = rmd.getColumnName(i);
            dataTypes[i] = rmd.getColumnType(i);
        }
    }

    public final String getZipName()                        { return zipName; }

    public final void setRowCount(int count)                { rowCount = count; }

    public final int countRows()                            { return rowCount; }

    public final int countColumns()                         { return dataTypes.length-1; }

    public final String getSchemaName(int columnIndex)      { return schemaNames[columnIndex]; }

    public final String getTableName(int columnIndex)       { return columnNames[columnIndex]; }

    public final String getColumnName(int columnIndex)      { return columnNames[columnIndex]; }

    public final int getDataType(int columnIndex)           { return dataTypes[columnIndex]; }

	/**
	 * create a PreparedStatement for inserting data from the result set
	 * all columns must belong to the same table !!
	 */
	public String createInsertStatement()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("INSERT INTO ");
		buf.append(schemaNames[0]);
		buf.append(".");
		buf.append(tableNames[0]);
		buf.append(" (");
		for (int i=0; i < countColumns(); i++)
		{
			if (i > 0) {
				if (!schemaNames[i].equals(schemaNames[i-1]) ||
					!tableNames[i].equals(tableNames[i-1]))
					throw new IllegalArgumentException("can not insert into multiple tables !");
				buf.append(",");
			}
			buf.append(columnNames[i]);
		}
		buf.append(") VALUES (");
		for (int i=0; i < countColumns(); i++)
		{
			if (i > 0) buf.append(",");
			buf.append("?");
		}
		buf.append(")");
		return buf.toString();
	}

	/**
	 * create a PreparedStatement for inserting data from the result set
	 * all columns must belong to the same table !!
	 */
	public String createUpdateStatement(String[] keys)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("UPDATE ");
		buf.append(schemaNames[0]);
		buf.append(".");
		buf.append(tableNames[0]);
		buf.append(" SET ");
		for (int i=0; i < countColumns(); i++)
		{
			if (i > 0) {
				if (!schemaNames[i].equals(schemaNames[i-1]) ||
					!tableNames[i].equals(tableNames[i-1]))
					throw new IllegalArgumentException("can not insert into multiple tables !");
				buf.append(",");
			}
			buf.append(columnNames[i]);
			buf.append("=?");
		}
		buf.append(" WHERE ");
		for (int i=0; i<keys.length; i++)
		{
			if (i > 0) buf.append(" AND ");
			buf.append(keys[i]);
			buf.append("=?");
		}
		return buf.toString();
	}
}
