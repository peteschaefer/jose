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
import de.jose.Util;
import de.jose.Version;
import de.jose.task.DBTask;
import de.jose.db.*;
import de.jose.pgn.PositionFilter;
import de.jose.store.IntBuffer;
import de.jose.util.StringUtil;
import de.jose.util.ProcessUtil;
import de.jose.util.map.IntIntMap;
import de.jose.view.ListPanel;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.BitSet;

import com.mysql.embedded.jdbc.MySqlError;


abstract public class IntervalCacheModel
        extends AbstractTableModel
        implements IDBTableModel
{
    /** Thread status   */
    public static final int WAITING                 = 1;
    public static final int EXECUTING               = 2;
	public static final int WAITING_FOR_EXECUTE     = 3;
	public static final int EXECUTED                = 4;
    public static final int READING                 = 5;
	public static final int REFRESH                 = 6;
    public static final int HALTED                  = 7;
	public static final int FINISHED                = 8;

	class StatementExecutor extends Thread
	{
		protected JoConnection conn;
        protected ParamStatement paramStm;
		protected JoPreparedStatement preparedStatement;
		protected int status = WAITING;

		public StatementExecutor() throws SQLException
		{
            conn = JoConnection.get();
			start();
		}

		public void execute(ParamStatement paramStm)
		{
			if (status!=WAITING) throw new IllegalStateException();

            this.paramStm = paramStm;
			this.status = EXECUTING;

			setPriority(NORM_PRIORITY);
			interrupt();
		}

		public void halt() throws SQLException
		{
			if (conn!=null) conn.cancelQuery();

			status = HALTED;
			setPriority(MIN_PRIORITY);
		}

		public void finish() throws SQLException
		{
			boolean interrupt = (status==WAITING);
			status = FINISHED;
			setPriority(MIN_PRIORITY);

			if (conn!=null) conn.cancelQuery(); //  if implemented

			if (interrupt) interrupt();
		}


		public void run()
		{
            try {
                for (;;)
                switch (status)
                {
                case WAITING:
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    break;

                case EXECUTING:
					/**
					 * important: preparation and execution of a prepared statement
					 * must happen within the same thread
					 * that's why we create the prepared statement HERE
					 */
					preparedStatement = paramStm.toPreparedStatement(conn);
					if (conn.isConnectorJ())
						this.preparedStatement.setFetchSize(Integer.MIN_VALUE);	//	hint to Connector/J driver: fetch row by row

//	                System.out.println(preparedStatement.toString());

	                try {
		                long startTime = System.currentTimeMillis();
		                preparedStatement.execute();
		                Util.printTime("asynch. execute",startTime);
	                }
	                catch (SQLException e)
	                {
		                if (e.getErrorCode()==MySqlError.ER_QUERY_INTERRUPTED) {
			                //  execution was deliberately interrupted
//			                System.out.println("query interrupted (1)");
		                }
		                else
		                    throw e;
	                }

	                if (status!=HALTED)
		                status = WAITING;

	                if (reader.status==WAITING_FOR_EXECUTE) {
		                reader.status = EXECUTED;  //  call back to ResultSetReader
//		                System.out.println("EXECUTED (12)");
		                reader.interrupt();
	                }
                    break;

                case HALTED:
                    status = WAITING;
                    continue;

                case FINISHED:
                    return;
                }

			} catch (SQLException e) {
				Application.error(e);
//				status = HALTED;
            } finally {
                if (conn!=null) JoConnection.release(conn);
                conn = null;
            }
        }
	}

    class ResultSetReader extends Thread
    {
        /** the interval that we are currently reading (1-based) */
        protected int min,max;
        /** current row number (1-based)    */
        protected int current;
        protected int fired;
        protected boolean fetchAll;
        /** current state */
        protected int status;
        /** number of returned rows in one chunk    */
        protected int chunk;

        /** database connection for synchroneous queries */
        protected JoConnection synch_conn;
        /** current result set  */
        protected ResultSet res;
	    protected StatementExecutor executor;

        ResultSetReader() throws Exception
        {
            min = max = current = -1;
            status = WAITING;
            setPriority(Thread.MIN_PRIORITY);
			setName("resultset-reader");
			setDaemon(true);
        }

        final boolean isReading(int rowNum)
        {
            return (rowNum >= min) && (rowNum < max);
        }

        final boolean isRead(int rowNum)
        {
            return (current >= rowNum);
        }

        public void halt() throws SQLException
        {
	        switch (status)
	        {
	        default:
		        status = HALTED;
//		        System.out.println("HALTED (2a)");

		        interrupt();
		        break;

	        case READING:
		    case EXECUTING:
		    case WAITING_FOR_EXECUTE:
			    if (synch_conn!=null) synch_conn.cancelQuery();
		        if (executor!=null) executor.halt();    //  let executor run into nirvana

		        status = HALTED;
//			    System.out.println("HALTED (2b)");
		        interrupt();
		        break;
	        }
        }

	    public void finish(long waitFinished)
			    throws SQLException
	    {
		    if (synch_conn!=null) synch_conn.cancelQuery();
	        if (executor!=null) executor.finish();    //  let executor run into nirvana

		    status = FINISHED;
		    interrupt();

		    long waitStart = System.currentTimeMillis();
		    while (waitFinished > 0L)
		    try {
			    waitStart = System.currentTimeMillis();
			    if (executor!=null) executor.join(waitFinished);
			    this.join(waitFinished);
			    break;
		    } catch (InterruptedException iex) {
			    waitFinished -= (System.currentTimeMillis()-waitStart);
		    }
	    }

        protected void reset(int minRow, int maxRow, boolean allRows) throws Exception
        {
	        if (synch_conn!=null) synch_conn.cancelQuery();
	        if (executor!=null) executor.halt();    //  let executor run into nirvana

	        //  before preceeding, make sure that the previous cycle is completed
	        while (status!=WAITING) interrupt();

	        //  now prepare the next query
	        fetchAll = allRows;
            min = minRow;
            max = maxRow;
            current = min-1;
            status = EXECUTING;
//	        System.out.println("EXECUTING (3)");

            interrupt();
        }

        public void run()
        {
            try {
				JoPreparedStatement pstm = null;

                for (;;) {
                    switch (status) {
                    case HALTED:
                        min = max = current = -1;
                        status = WAITING;
//	                    System.out.println("WAITING (7)");
                        if (rowCount > fired)
                            fireTableRowsInserted(fired, fired=rowCount);
                        fireStatusChange();
                        if (res != null) res.close();
                        //  fall-through intended

                    case WAITING:
	                case WAITING_FOR_EXECUTE:
                        try {
                            sleep(5000L);
                        } catch (InterruptedException iex) {
                            //  continue;
                        }
                        break;

	                case FINISHED:
		                return;

					case REFRESH:
						try {
							status = WAITING;
//							System.out.println("WAITING (11)");
							if (min > 1) sleep(500L);
							fireUpdates();
						} catch (InterruptedException iex) {
							//  continue;
						}
						break;

                    case EXECUTING:
                        if (res != null) res.close();
                        pkStatement.setLimit(min-1, (max < 0) ? -1 : (max-min));

//						startTime = System.currentTimeMillis();
	                    if (max > 0 && max < Integer.MAX_VALUE/2) {
		                    try {
			                    //  execute is cheap - do it synchroneously
			                    if (synch_conn==null) synch_conn = JoConnection.get();
			                    pstm = pkStatement.toPreparedStatement(synch_conn);
			                    if (synch_conn.isConnectorJ())
			                        pstm.setFetchSize(0);	//	hint to Connector/J driver: fetch complete set
//			                    System.out.println(pkStatement.toString());
			                    pstm.execute();
//		                    Util.printTime("synch. execute",startTime);
		                    } catch (SQLException e) {
			                    if (e.getErrorCode()==MySqlError.ER_QUERY_INTERRUPTED)
			                    {
				                    //  was deliberately interrupted
//				                    System.out.println("query interrupted (2)");
			                    }
			                    else
			                        throw e;
		                    }

		                    if (status!=HALTED) {
			                    //  read the result
			                    status = EXECUTED;
//			                    System.out.println("EXECUTED (5)");
	                    }
	                    }
	                    else {
		                    //  execute is expensive: do it in parallel thread
		                    if (executor==null)
			                    executor = new StatementExecutor();
		                    else if (executor.status != WAITING) {
			                    executor.finish();
			                    executor = new StatementExecutor();
		                    }

                            /**
                             * important: make sure that preparation and execution of the statement is
                             * done within the same thread. What we must not do is:
                             * - prepare the statement in this thread
                             * - execute it in the StatemenExecutor thread
                             */
                            pstm = null;    //  will be created by StatementExecutor
		                    executor.execute(pkStatement);

		                    if (status!=HALTED) {
			                    status = WAITING_FOR_EXECUTE;
//			                    System.out.println("WAITING FOR EXECUTE (4)");
		                    }
		                    //  upon completion, executor will advance status to EXECUTED
		                    //  upon abortion, executor will run to nirvana
	                    }
                        break;

	                case EXECUTED:
                        if (pstm==null) pstm = executor.preparedStatement;
	                    res = pstm.getResultSet();
		                if (res==null) {
			                status = HALTED;
//			                System.out.println("HALTED (9)");
		                }
		                else {
	//	                    Util.printTime("got result set",startTime);
							chunk = 0;
							if (status!=HALTED) {
								status = READING;
//								System.out.println("READING (8)");
		  	            }
		  	            }
	                    break;

                    case READING:
		                    while (status==READING)
		                        if (res.next())
		                        {
		                            chunk++;
		                            if (posFilter.accept(res))
		                            {
		                                pkStore.set(current++, res.getInt(1));

		                                if (current > rowCount) {
		                                    rowCount = current;

		                                    if (rowCount < 30)
		                                        fireTableRowsInserted(fired, fired=rowCount);
		                                    else if (rowCount < 1000) {
		                                        if ((rowCount%50)==0)
		                                            fireTableRowsInserted(fired, fired=rowCount);
		                                    }
		                                    else if ((rowCount%500)==0)
		                                         fireTableRowsInserted(fired, fired=rowCount);
		                                }
		                            }
		                       }
		                       else {
		                            /*  reached end of result set */
		                            intvalMap.set(min/intvalSize,true); //  mark interval as completely read
		                            res.close();
		                            res = null;

		                            if (fetchAll && (chunk >= (max-min)))
		                            {
		                                //  more results
		                                min = max;
//                                max += intvalSize;
			                            max = Integer.MAX_VALUE/2;   //  get rest in ONE large chunk
		                                status = EXECUTING;
//			                            System.out.println("EXECUTING (6)");
		                            }
		                            else {
		                                //  results exhausted
		                                min = max = current = -1;
		                                status = REFRESH;
//			                            System.out.println("REFRESH (10)");
		                                if (rowCount > fired)
		                                    fireTableRowsInserted(fired, fired=rowCount);
		                                fireStatusChange();
//	                            Util.printTime("search",startTime);
		                            }
		                       }
	                    break;
                    }
                }

            } catch (Exception ex) {
                Application.error(ex);
            } finally {
                JoConnection.release(synch_conn);
	            if (executor!=null) try {
		            executor.finish();
	            } catch (SQLException e) { }
            }
        }
    }

    /** the statement used to retrieve primary keys */
    protected ParamStatement pkStatement;
	/** position search filter  */
	protected PositionFilter posFilter;

    /** the statement used to retrieve actual data  */
    protected String dataSql1;
	protected String dataSqlBulk;

    /** the in-memory row cache */
    protected Row[] rowCache;

    /** the underlying store (stores primary keys)    */
    protected IntBuffer pkStore;

    /** total size of the result size
     *  must be set in advance
     * */
    protected int rowCount;
    /** is rowcount accurate ? or an estimated based from search record */
    protected boolean rowCountAccurate;
    /** size of intervals   */
    protected int intvalSize;
    /** which intervals have alread been read from the database ? */
    protected BitSet intvalMap;

    /** the background reader thread    */
    protected ResultSetReader reader;

    /** column types (1-based)    */
    protected int[] columnTypes;
    /** ... and names (1-based)  */
    protected String[] columnNames;

    protected ListPanel panel;

	public boolean isOutOfSynch = false;

    public IntervalCacheModel(String data, int memCacheSize, int intervalSz) throws Exception
    {
        dataSql1 = StringUtil.replace(data,"%IN%"," = ? ");
		dataSqlBulk = StringUtil.replace(data,"%IN%"," IN (%IN%)");
        rowCache = new Row[memCacheSize];
        intvalSize = intervalSz;
        intvalMap = new BitSet((rowCount+intvalSize-1)/intervalSz);

        pkStore = new IntBuffer(Math.min(intervalSz,4096), 0);  //  so that one interval fits neatly into a block
        reader = new ResultSetReader();
        reader.start(); //  will go to sleep immediately and wait for reset()
    }

    public void reset(ParamStatement pkStm, PositionFilter filter,
                      int size, boolean accurate) throws Exception
    {
		clear(true);

		pkStore.ensureCapacity(size);
		pkStatement = pkStm;
	    posFilter = filter;
		rowCount = size;
        rowCountAccurate = accurate;

		if (size >= 0) {
			//  size already known
			//  fetching small result set chunks is supposed to be cheap.
			//  the disadvantage is that we have to issue lots of queries.
//System.err.println("expected result set size = "+size);
			fireTableRowsInserted(0, reader.fired=size);

			reader.reset(1,intvalSize+1,false); //  fetch chunks on demand
		}
		else {
			//  size not known
			//  it's better to fetch the result in two large chunks;
			//  this is to achieve a compromise:
			//  (a) get fast response for first rows and
			//  (b) don't require too many expensive queries
			reader.fired = 0;

			if (posFilter==null || posFilter==PositionFilter.PASS_FILTER)
				reader.reset(1,intvalSize+1,true); //  fetch some rows, then all the rest
			else
				reader.reset(1,Integer.MAX_VALUE/2,true); //  fetch all rows
		}
	    /**
	     */
/*
	     * think about using ONE query: cancelQuery() must work reliably & fast
	     * how fast can the user scroll ?
        reader.fired = 0;
		reader.reset(1,Integer.MAX_VALUE/2,true); //  fetch all rows
*/
	}



    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnTypes.length-1;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        /** columnIndex is 0-based (right ?)
         *  Row is 0-based
         */
        try {

            Row row = getRowAt(rowIndex);
            if (row==null)
                return null;
            else
                return row.get(columnIndex+1);

        } catch (Exception e) {
            Application.error(e);
            return "error";
        }
    }

    public Row getRowAt(int rowIndex) throws Exception
    {
        /**
         * keep in mind:
         * rowIndex is 0-based; database result sets are 1-based
         * */
        int rowNum = rowIndex+1;

        /** 1. examine in-memory cache  */
        Row result = rowCache[rowIndex%rowCache.length];
        if (result!=null && result.getRowNum()==rowNum)
            return result;

        /** 2. examine pk cache */
        int intvalIdx = rowIndex / intvalSize;
        if (intvalMap.get(intvalIdx))
            return readRow(rowIndex);

        /** 3. is interval currently read ? */
        if (reader.isReading(rowNum)) {
            /*  wait until reader has fetched it    */
            if (reader.isRead(rowNum))
                return readRow(rowIndex);   /*  get it  */
            else
                return null;    //  needs to be refreshed later
        }

        /** 4. if reader is busy, return null */
        if (isWorking())
            return null;    //  needs to be refreshed later

        /** 5. fetch new interval   */
        reader.reset(intvalIdx*intvalSize+1, Math.min(rowCount,(intvalIdx+1)*intvalSize)+1,false);
        if (reader.isRead(rowNum))
            return readRow(rowIndex);   /*  get it  */
        else
            return null;    //  needs to be refreshed later
    }
/*
    protected void waitFor(int rowNum)
    {
        if (intvalMap.get((rowNum-1)/intvalSize)) return;

        if (reader.current > rowNum) return;

        Thread myself = Thread.currentThread();
        reader.notify(rowNum,myself);

        while (reader.current < rowNum) {
            try {
                myself.sleep(5000L);
            } catch (InterruptedException iex) {
                //  continue
            }
        }
    }
*/
    protected Row readRow(int rowIndex) throws Exception
    {
		Row result;
		/*  interval has been read, fine    */
		int pk = pkStore.get(rowIndex);
		/*  read data */
		result = readData(rowIndex+1,pk);
		/*  add to in-memory cache  */
        rowCache[rowIndex%rowCache.length] = result;
        return result;
    }

    /**
     *  notifies listeners when the status of the Result Set changes
     *  (without changes to the list contents)
     */
    public void fireStatusChange()
    {
        fireTableChanged(new TableModelEvent(this, 0, 0,
                    TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }

    public void fireUpdates() throws Exception {
        JTable table = panel.getTable();
        //  get the range of visible rows
        Rectangle rect = table.getVisibleRect();
        Point topLeft = new Point(rect.x, rect.y);
        int top = Math.max(table.rowAtPoint(topLeft),0);
		int rowHeight = table.getRowHeight();
		int visibleRows = (rect.height+rowHeight-1)/table.getRowHeight();
		int bottom = top + visibleRows-1;

		/**	fetch data	*/
		readBulkData(top,bottom);
		fireTableRowsUpdated(top,bottom);
    }

    public Row readData(int rowNum, int pk) throws Exception
    {
		if (pk <= 0) {
			/**	indicates an inconsistent state of the database and should never happen
			 * 	(actual result set smaller than estimated ? )
             *
             * however, during longish DB operations the estimated number of rows
             * might be different from the actual row count; let's be more tolerant in these cases
			 */
            if (rowCountAccurate) {
                isOutOfSynch = true;
//                System.out.println("database out of synch ?!");
            }
			Row row = new Row(99);
			row.setRowNum(rowNum);
			row.setPK(-1);
			return row;
		}

        JoConnection conn = null;
        ResultSet result = null;
        try {
            conn = JoConnection.get();
            JoPreparedStatement pstm = conn.getPreparedStatement(dataSql1);
			pstm.setInt(1,pk);
			pstm.execute(false);

			result = pstm.getResultSet();

			if (columnTypes==null) setMetaData(result.getMetaData());

			Row row = new Row();
			if (result.next())
				row.read(result, columnTypes);
			else if (Version.mac) {
                row.setSize(columnTypes.length);
                System.out.println("data expected at row "+rowNum+", Id="+pk);
            }
			else
				throw new SQLException("data expected at row "+rowNum+", Id="+pk);
	        /**
	         * note: this can happen when data from trash is displayed & erased at the same time
	         * TODO: when erasing the trash, don't show data from trash
	         */

			row.setRowNum(rowNum);
			row.setPK(pk);
            return row;

        } finally {
			if (result!=null) result.close();
            JoConnection.release(conn);
        }
    }

	protected void readBulkData(int rowIdxTop, int rowIdxBottom) throws Exception
	{
		StringBuffer in = new StringBuffer();
		IntIntMap pkMap = new IntIntMap();
		Row row;
		int pk, rowIdx, rowNum;

		for (rowIdx=rowIdxTop; rowIdx <= rowIdxBottom; rowIdx++)
		{
			rowNum = rowIdx+1;
			row = rowCache[rowIdx%rowCache.length];
			if (row!=null && row.getRowNum()==rowNum) continue;

			pk = pkStore.get(rowIdx);
			if (pk <= 0) continue;	//	not yet available ?

			pkMap.put(pk,rowNum);

			if (in.length() > 0) in.append(",");
			in.append(String.valueOf(pk));
		}

		if (pkMap.isEmpty()) return;

		JoConnection conn = null;
		ResultSet result = null;
		JoPreparedStatement stm = null;
		try {
			conn = JoConnection.get();
			String sql = StringUtil.replace(dataSqlBulk,"%IN%",in.toString());
			stm = conn.getPreparedStatement(sql);
			stm.execute();
			result = stm.getResultSet();
			if (columnTypes==null) setMetaData(result.getMetaData());

			while (result.next()) {
				row = new Row();
				row.read(result, columnTypes);

				pk = row.getInt(1);
				rowNum = pkMap.get(pk);
				if (rowNum==IntIntMap.NOT_FOUND) throw new SQLException("unexpected row: "+pk);

				row.setRowNum(rowNum);
				row.setPK(pk);

				rowIdx = rowNum-1;
				rowCache[rowIdx%rowCache.length] = row;
			}
		} finally {
			if (result!=null) result.close();
			JoConnection.release(conn);
		}
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

    public void clear(boolean clearStore) throws IOException, SQLException
    {
        reader.halt();
        Arrays.fill(rowCache,null);
        intvalMap.set(0,intvalMap.size(), false);
        if (clearStore) pkStore.clear();
    }

    public void stopResult() throws SQLException
    {
        reader.halt();
    }



    public void open() throws Exception {
    }

    public void close(boolean delete) throws Exception
    {
        clear(delete);
	    if (delete) reader.finish(5000);
    }

    public void refresh(boolean scrollTop) {
        throw new AbstractMethodError();
    }

	public void invalidateRowByPK(int dbId)
	{
		for (int i=0; i < rowCache.length; i++)
			if (rowCache[i]!=null && dbId==rowCache[i].getPK())
			{
				int rowIdx = rowCache[i].getRowNum()-1;
				rowCache[i] = null;	//	force re-fetch
				fireTableRowsUpdated(rowIdx,rowIdx);
				return;	//	assuming there is only one row with a given PK (otherwise: continue)
			}
	}

    public Component getDisplayComponent() {
        return panel;
    }

    public void setDisplayComponent(Component displayComponent) {
        panel = (ListPanel)displayComponent;
    }

    public int getDBId(int rowIndex)
    {
        /**	convert to 1-based indexes		 */
        return pkStore.get(rowIndex);
    }

    public void singleCell(String header, Object content, int type) {
        throw new AbstractMethodError();
    }

    public boolean isSingleCell()
    {
        return false;
    }

    public boolean isSortable(int columnIndex)
    {
        return true;
    }

    public boolean isWorking() {
        return (reader.status==EXECUTING) || (reader.status==EXECUTED) || (reader.status==READING);
    }
}
