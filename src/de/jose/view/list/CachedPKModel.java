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
import de.jose.db.*;
import de.jose.store.*;
import de.jose.util.AWTUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * a Table Model that caches primay keys
 *
 * @author Peter Schäfer
 */

abstract public class CachedPKModel
		extends DBTableModel
{
	/** the Reader thread   */
    protected Reader                reader;
	/**	release connection where Reader finishes ? */
	protected boolean				releaseConnection;

    /**	index storage file	 */
	protected StorageFile			indexFile;
	protected RandomOutputStream	indexWrite;
	protected RandomInputStream		indexRead;

	/**	the actual database query	 */
	protected ParamStatement		pkStatement;
	protected JoPreparedStatement	dataStatement;
	/**	number of primary keys	 */
	protected int					pkCount;

	protected static final int		INITIAL_RANGE	= 1000;
	protected static final int		MAX_RANGE		= 32000;

	class Reader extends Thread
	{
        int state;    //  abort request by enclosing model
		JoConnection conn;
		ParamStatement sql;
		int rangeStart;
		int rangeLength;

		Reader(ParamStatement statement)
			throws Exception
		{
			conn = JoConnection.get();
			sql = statement;
			state = 0;
			rangeStart = 0;
			if (JoConnection.getAdapter().useResultLimit())
				rangeLength = INITIAL_RANGE;
			else
				rangeLength = -1;
			setName(identifier+"-reader");
			setDaemon(true);
		}

		public void run()
		{
			JoPreparedStatement pstm = null;
			try {
				AWTUtil.setWaitCursor(getDisplayComponent());

                state = THREAD_READ;
                fireRowEvent(true);

			 	JoConnection.getAdapter().setProcessPriority(DBAdapter.IMPORTANT_QUERY);
				pstm = makeStatement(conn,sql,rangeStart,rangeLength);

                if (! pstm.execute())
                    throw new SQLException("SELECT query expected");

				ResultSet res = pstm.getResultSet();
				pstm.setFetchSize(res,1);

                if (pkCount <= 0)
					pkCount = res.getMetaData().getColumnCount();

				while (state==THREAD_READ)
				{
					if (!res.next()) {
						if ((rangeLength < 0) || (maxRow < (rangeStart+rangeLength)))  {
							state = THREAD_FINISHED;
							JoConnection.getAdapter().setProcessPriority(DBAdapter.NORMAL_QUERY);
							break;
						}
						else {
							/**	fetch chunk by chunk
							 * 	we do this with mySQL because large result sets are too expensive to handle
							 * 	(especially closing them can become expensive)
							 * 	better break them in several parts
							 */
							pstm.closeResult();
							rangeStart += rangeLength;
							rangeLength = Math.min(2*rangeLength, MAX_RANGE);
							/*	if this is gonna be a longrunning query
								reduce process priority (give the GUI more CPU time)
							*/
							if (rangeLength==MAX_RANGE)
								JoConnection.getAdapter().setProcessPriority(DBAdapter.LONG_RUNNING_QUERY);
							else
								JoConnection.getAdapter().setProcessPriority(DBAdapter.NORMAL_QUERY);

							pstm = makeStatement(conn,sql,rangeStart,rangeLength);

							pstm.execute();
							res = pstm.getResultSet();
							res.setFetchSize(1000);

							if (!res.next()) {
								state = THREAD_FINISHED;
								break;
							}
							else
								continue;
						}
					}

					writeRow(res);
					maxRow++;

					if (maxRow==1)
						AWTUtil.setDefaultCursor(getDisplayComponent());

                    if ((maxRow < 25)) {
                    	fireRowEvent(false);
                    }
                    else if ((maxRow < 100) && ((maxRow%10)==0)) {
	                 	fireRowEvent(false);
                    }
                    else if ((maxRow < 1000) && ((maxRow%100)==0)) {
	                    res.setFetchSize(100);
	                    fireRowEvent(false);
                    }
                    else if ((maxRow%1000)==0) {
	                    res.setFetchSize(1000);
                        fireRowEvent(false);
                    }
				}
			} catch (Throwable thr) {
				if (state!=THREAD_ABORT)
                	Application.error(thr);
			} finally {
				fireRowEvent(true);
				AWTUtil.setDefaultCursor(getDisplayComponent());
				JoConnection.getAdapter().setProcessPriority(DBAdapter.NORMAL_QUERY);
				try {
                	if (pstm!=null) {
		                if (false/*(state==THREAD_ABORT) && conn.getAdapter().useResultLimit()*/) {
			             	//	closeResult() is an excessively expensive operation; better drop the connection
			                conn.close();
		                }
		                else {
			            	pstm.closeResult();
			                conn.release();
		                }
	                }
				} catch (SQLException sqlex) {
					Application.error(sqlex);
				}
			}
		}
	}


	public CachedPKModel(String identifier,
					    int storage, int cacheSize)
		throws SQLException, IOException
	{
		super(identifier,cacheSize);

        switch (storage)
        {
        case STORE_RAM:
            indexFile = new RAMFile();
            break;
        case STORE_DISK:
            indexFile = DiskFile.tempFile();
            break;
        default:
            throw new IllegalArgumentException("STORE_RAM or STORE_DISK expected");
        }
	}

	public Row fetchRow(Row r, int rowIndex)
		throws SQLException, IOException
	{
		if (rowIndex > maxRow) return null;
		if (rowIndex > flushedRow) flush();

		if (r==null) r = new Row();
		r.setRowNum(rowIndex);

		synchronized (indexRead) {
			//	fetch primary keys
			indexRead.seek(4*pkCount*(rowIndex-1));
		}

		try {
			int pk = indexRead.readInt();
			dataStatement.setInt(1, pk);
			r.setPK(pk);
			
			for (int i=2; i<=pkCount; i++) {
				pk = indexRead.readInt();
				dataStatement.setInt(i, pk);
			}
			dataStatement.execute();

			if (columnTypes==null)
				setMetaData(dataStatement.getResultSet().getMetaData());

			if (dataStatement.next()) {
				r.read(dataStatement.getResultSet(), columnTypes);
			}
			else {
//				throw new SQLException("no data found");
				return null;
			}
		} finally {
			dataStatement.closeResult();
		}

		return r;
	}

    public int fetchId(int rowIndex)
        throws IOException
    {
        if (rowIndex > maxRow) return -1;
        if (rowIndex > flushedRow) flush();

        synchronized (indexRead) {
            //	fetch primary keys
            indexRead.seek(4*pkCount*(rowIndex-1));
        }

        return indexRead.readInt();
    }


	protected void writeRow(ResultSet res)
		throws SQLException, IOException
	{
		synchronized (indexWrite) {
			for (int i=1; i<=pkCount; i++)
			{
				int pk = res.getInt(i);
				indexWrite.writeInt(pk);
			}
		}
	}

	abstract public ParamStatement makeStatement(DBAdapter adapter) throws SQLException;

	public JoPreparedStatement makeStatement(JoConnection conn,
	                                         ParamStatement sql,
	                                         int start, int length)
		throws SQLException
	{
		if (conn.getAdapter().useResultLimit())
			sql.setLimit(start,length+1);   //  retrieve one extra row (shown by experience ?!?)

		JoPreparedStatement pstm = sql.toPreparedStatement(conn);
		if (length > 0)
			pstm.setMaxRows(length);
		return pstm;
	}

    public void open()
            throws Exception
    {
        indexWrite = new RandomOutputStream(indexFile);
        indexRead = new RandomInputStream(indexFile);

//	    JoConnection queryConn = JoConnection.get();
	    ParamStatement pkStatement = makeStatement(JoConnection.getAdapter());

        /** is it possible to calculate the size of the result set beforehand ?
         *  if so, do it NOW
         */
        totalRows = calculateResultSize();

        reader = new Reader(pkStatement);
        fireRowEvent(0,true);

        if (totalRows >= 0) {            
            fireRowEvent(totalRows,true);
            reader.setPriority(Thread.MIN_PRIORITY);
        }
        else
	        reader.setPriority(Thread.NORM_PRIORITY);
        reader.start();
	    Thread.currentThread().yield();
    }

    /**
     *
     * @return the expected size of the result set, -1 if not known
     */
    public int calculateResultSize() throws Exception
    {
        return -1;
    }

	public void close(boolean delete)
        throws Exception
	{
		stopResult();

		try {
			indexWrite.close();
			indexRead.close();
            if (delete) {
			    indexFile.delete();
                indexFile = null;
            }
            else
                indexFile.setLength(0L);
		} catch (IOException ioex) {
			//	can't help it
			Application.error(ioex);
		}

        super.close(delete);
	}

	public void stopResult()
	{
		//  terminate Reader
		if (reader.isAlive()) {
		    reader.state = THREAD_ABORT;
			reader.setPriority(Thread.MIN_PRIORITY);
		}
	}

    public boolean isWorking()
    {
        return reader.isAlive() && (reader.state == THREAD_READ);
    }

	protected void flush()
		throws IOException
	{
		synchronized (indexWrite) {
			indexWrite.flush();
		}
		synchronized (indexRead) {
			indexRead.flush();
		}
		flushedRow = maxRow;
	}
}
