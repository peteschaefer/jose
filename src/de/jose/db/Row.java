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

package de.jose.db;

import de.jose.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

/**
 * contains a Result Set Row
 * objects are indexed at 1 (as with result sets)
 */

public class Row
		extends ArrayList
{
	/**	row number (starting at 1)	 */
	protected int rowNum;
	/**	database primary key	*/
	protected int pk;

    /** true if this row contains volatile (temporary) data
     *  that needs to be updated from the database
     */
    protected boolean volatileData = false;

	/**	@return the row number	 */
	public final int getRowNum()			{ return rowNum; }

	public final int getPK()				{ return pk; }

	/**	set the row number	 */
	public final void setRowNum(int num)	{ rowNum = num; }

	public final void setPK(int dbId)		{ pk = dbId; }

	public Row()							{ super(); }

	public Row(int colCount)
	{
		super(colCount+1);
		setColumnCount(colCount);
	}

    public void setColumnCount(int colCount)
    {
        setSize(colCount+1);
    }

    public void setSize(int size)
    {
        super.ensureCapacity(size);
        while (size() < size) add(null);
        if (size() > size) removeRange(size+1,size());
    }

	/**	read values from result set	 */
	public void read(ResultSet res, int[] types)
		throws SQLException
	{
		setSize(types.length);
		for (int i=1; i < types.length; i++)
			set(i, getObject(res,i,types[i]));
	}
	
	
	public final int getInt(int i)
	{
		Number n = (Number)get(i);
		return (n!=null) ? n.intValue() : 0;
	}

	public final String getString(int i)
	{
		Object n = get(i);
		return (n!=null) ? n.toString() : null;
	}
	
	
	public static Object getObject(ResultSet res, int column, int type)
		throws SQLException
	{
		switch (type)
		{
		case Types.BIGINT:				return res.getBigDecimal(column);
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:		return res.getBytes(column);
		case Types.BIT:					boolean b = res.getBoolean(column);
										if (res.wasNull())
											return null;
										else
											return Util.toBoolean(b);
		case Types.BLOB:				return res.getBlob(column);
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:			return res.getString(column);
		case Types.CLOB:				return res.getClob(column);
		case Types.DATE:				return res.getDate(column);
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:	
		case Types.REAL:				double d = res.getDouble(column);
										if (res.wasNull())
											return null;
										else
											return d;
		case Types.FLOAT:				float f = res.getFloat(column);
										if (res.wasNull())
											return null;
										else
											return f;
		case Types.INTEGER:				int i = res.getInt(column);
										if (res.wasNull())
											return null;
										else
											return i;

		case Types.TINYINT:             byte by = res.getByte(column);
										if (res.wasNull())
											return null;
										else
											return by;

		case Types.SMALLINT:	        short s = res.getShort(column);
										if (res.wasNull())
											return null;
										else
											return s;
			
		case Types.TIME:				return res.getTime(column);
		case Types.TIMESTAMP:			return res.getTimestamp(column);
										
		default:						throw new IllegalArgumentException("type "+type+" not supported");
		}
	}


    public boolean isVolatile() {
        return volatileData;
    }

    public void setVolatile(boolean volatileData) {
        this.volatileData = volatileData;
    }

}
