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

package de.jose.view.list;

import de.jose.Application;
import de.jose.Language;
import de.jose.db.Row;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;

/**
 * a ListModel that is based on a database querey
 * results are cached
 *
 * @author Peter Schäfer
 */

abstract public class DBTableModel
		extends AbstractTableModel
        implements IDBTableModel
 {
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	for cached result sets: store data on disk	 */
	public static final int	STORE_DISK		= 1;

	/**	for cached result sets: store data in RAM	 */
	public static final int	STORE_RAM		= 2;

	/**	Reader Thread states	*/
	/**	thread state: still reading	 */
	public static final int	THREAD_READ		= 1;
	/**	thread state: abort requested	 */
	public static final int	THREAD_ABORT	= 2;
	/**	thread state: finished	 */
	public static final int THREAD_FINISHED	= 9;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	table identifier	 */
	protected String				identifier;

	/**	level 1 cache	 */
	protected Row[]					rowCache;

	/**	column types	 */
	protected int[]					columnTypes;

	/**	column names	 */
	protected String[]				columnNames;

    /** current sort order (+1 = ascending, -1 = descending)    */
    protected int                   sortOrder;

	/**	number of rows for which insert events have been fired	 */
	protected int					firedRows;
    /** number of rows for which udpate events have been fired */
	protected int                   updatedRows;

	/**	number of rows read from database */
	protected int					maxRow;
	/**	number of rows actually written to file	 */
	protected int					flushedRow;
    /** total size of the result set (if known) */
    protected int                   totalRows;

	/**	disply component (optional)	*/
	protected Component				displayComponent;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public DBTableModel(String id, int cacheSize)
	{
		super();
		identifier = id;
		rowCache = new Row[cacheSize];
	}

    public void close(boolean delete) throws Exception
    {
        /*  overwrite ! */

        clearCache();
        maxRow = flushedRow = 0;
        totalRows = -1;
    }

    public void refresh(boolean scrollTop)
    {
        try {
            close(false);
            open();
        } catch (Exception sqlex) {
            singleCell("ERROR", sqlex.getLocalizedMessage(), Types.VARCHAR);
            try {
                close(false);
            } catch (Exception sqlex2) {
                Application.error(sqlex2);
            }
        }
    }

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public void setCacheSize(int newSize)
	{
		//	re-hash
		Row[] newCache = new Row[newSize];
		for (int i=0; i<rowCache.length; i++)
		{
			int j = rowCache[i].getRowNum() % newSize;
			newCache[j] = rowCache[i];
		}
		rowCache = newCache;
	}

	public void clearCache()
	{
		for (int i=0; i<rowCache.length; i++)
			rowCache[i] = null;
	}

	public Row getRow(int rowNum)
	{
		Row r = rowCache[rowNum % rowCache.length];
		if (r != null && r.getRowNum() == rowNum)
			return r;
		else
			return null;
	}

	public void addRow(Row r)
	{
		rowCache[r.getRowNum() % rowCache.length] = r;
	}

	public void setMetaData(ResultSetMetaData rmd)
		throws SQLException
	{
		int count = rmd.getColumnCount();
		columnTypes = new int[count+1];
		columnNames = new String[count+1];

		for (int i=1; i<=count; i++)
		{
			columnTypes[i] = rmd.getColumnType(i);
			columnNames[i] = rmd.getColumnName(i);
		}
	}

	/**	fetch a row of data from the database	 */
	public abstract Row fetchRow(Row r, int rowIndex)
		throws SQLException, IOException;

    /** fetch the databae Id of one row */
    public abstract int fetchId(int rowIndex)
        throws IOException;

	/** Returns the Id of the database object at table index */
	public int getDBId(int rowIndex)
	{
	    /**	convert to 1-based indexes		 */
	    rowIndex++;
	    try {
	        return fetchId(rowIndex);
	    } catch (IOException ioex) {
	        Application.error(ioex);
	        return -1;
	    }
	}

	/**	constructs a single cell list model
	 */
	public void singleCell(String header, Object content, int type)
	{
		columnTypes = new int[] { 0, type };
		columnNames = new String[] { null, header };
		rowCache[1] = new Row(1);
		rowCache[1].setRowNum(1);
		rowCache[1].setPK(-1);	//	n/a
		rowCache[1].set(1, content);

		fireTableStructureChanged();
		fireRowEvent(true);

		maxRow = flushedRow = 1;
	}

	public boolean isSingleCell()
	{
		return columnTypes!=null && columnTypes.length==2 && maxRow==1;
	}

	//-------------------------------------------------------------------------------
	//	implements TableModel
	//-------------------------------------------------------------------------------

	/**
	 * Returns the most specific superclass for all the cell values in the column.
	 */
	public Class getColumnClass(int columnIndex)
	{
		switch (columnTypes[columnIndex+1])
		{
		case Types.BIGINT:		return BigInteger.class;
		case Types.BLOB:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BINARY:		return byte[].class;
		case Types.BIT:			return Boolean.class;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:	return String.class;
		case Types.DATE:		return java.sql.Date.class;
		case Types.TIME:		return Time.class;
		case Types.TIMESTAMP:	return Timestamp.class;
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.DOUBLE:		return Double.class;
		case Types.REAL:
		case Types.FLOAT:		return Float.class;
		case Types.INTEGER:		return Integer.class;
		case Types.TINYINT:
		case Types.SMALLINT:	return Short.class;

		default:				throw new IllegalArgumentException("type "+columnTypes[columnIndex+1]+" not supported");
		}
	}

	/**	Returns the number of columns in the model.	 */
	public int getColumnCount()
	{
		return columnTypes.length-1;
	}

	/**	Returns the name of the column at columnIndex. 	 */
	public String getColumnName(int columnIndex)
	{
		String name = columnNames[columnIndex+1];
		return Language.get("column."+identifier+"."+name, name);
	}

	/** Returns the number of rows in the model. 	 */
	public int getRowCount()
	{
        if (totalRows >= 0)
            return totalRows;
        else
		    return maxRow;
	}

	/**	Returns the value for the cell at columnIndex and rowIndex. 	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		/**	convert to 1-based indexes		 */
		rowIndex++;
		columnIndex++;

		Row r = rowCache[rowIndex % rowCache.length];

		if (r==null || r.getRowNum() != rowIndex)
		{
			try {
				r = fetchRow(r,rowIndex);
			} catch (SQLException sqlex) {
				//	got to catch it
				Application.error(sqlex);
				return sqlex.getLocalizedMessage();
			} catch (IOException ioex) {
				Application.error(ioex);
				return ioex.getLocalizedMessage();
			}

			if (r != null)
				addRow(r);
		}
		if (r != null)
			return r.get(columnIndex);
		else
			return null;
	}

	//-------------------------------------------------------------------------------
	//	implements JoTableModel
	//-------------------------------------------------------------------------------

	/**	@return an identifier for a column	 */
	public Object getIdentifier(int columnIndex)
	{
		return columnNames[columnIndex+1];
	}

	/** @return the preferred width of a column	 */
	public int getPreferredColumnWidth(int columnIndex)
	{
		switch (columnTypes[columnIndex+1])
		{
		case Types.BIGINT:		return 48;
		case Types.BLOB:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
		case Types.BINARY:		return 96;
		case Types.BIT:			return 16;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:	return 96;
		case Types.DATE:		return 48;
		case Types.TIME:		return 48;
		case Types.TIMESTAMP:	return 96;
		case Types.DECIMAL:
		case Types.NUMERIC:
		case Types.DOUBLE:		return 32;
		case Types.REAL:
		case Types.FLOAT:		return 32;
		case Types.INTEGER:		return 24;
		case Types.TINYINT:
		case Types.SMALLINT:	return 24;

		default:				throw new IllegalArgumentException("type "+columnTypes[columnIndex+1]+" not supported");
		}
	}

	/** @return the minimum width of a column	 */
	public int getMinColumnWidth(int columnIndex)
	{
		return 8;
	}

	/** @return the maximum width of a column	 */
	public int getMaxColumnWidth(int columnIndex)
	{
		return Integer.MAX_VALUE;
	}

    public int getSortOrder()   { return sortOrder; }

    public void setSortOrder(int order)
    {
        sortOrder = order;
    }

    public boolean isSortable(int columnIndex)
    {
        return true;
    }

	public Component getDisplayComponent() {
		return displayComponent;
	}

	public void setDisplayComponent(Component displayComponent) {
		this.displayComponent = displayComponent;
	}

	protected final void fireRowEvent(boolean force)
	{
        int lastRow = maxRow-1;  //  0-based
		//	notify table of row change
		if (lastRow > firedRows) {
			fireTableRowsInserted(firedRows+1,lastRow);
            firedRows = lastRow;
		}
        if (lastRow > updatedRows) {
            fireTableRowsUpdated(updatedRows+1,lastRow);
            updatedRows = lastRow;
        }
        else if (force) {
            fireTableRowsUpdated(updatedRows+1,firedRows);
        }
    }

    protected void fireRowEvent(int lastRow, boolean force)
    {
        lastRow--; //  0-based
		//	notify table of row change
		if (lastRow > firedRows) {
			fireTableRowsInserted(firedRows+1,lastRow);
            updatedRows = firedRows = lastRow;
		}
		else if (lastRow < firedRows) {
			fireTableRowsDeleted(lastRow+1,firedRows);
            updatedRows = firedRows = lastRow;
        }
        else if (force) {
            fireTableRowsUpdated(firedRows,firedRows);
        }
    }


}
