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
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.Row;
import de.jose.store.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * a DB Table Model that caches the complete result set
 * 
 * @author Peter Schäfer
 */

public class CachedResultModel
		extends DBTableModel
{
    /** current Reader thread  */
    protected Reader                reader;
	/**	release connection when reader finishes ?	*/
	protected boolean				releaseConnection;

	/**	data storage file	 */
	protected StorageFile			dataFile;
	protected RandomOutputStream	dataWrite;
	protected RandomInputStream		dataRead;
	
	/**	index storage file	 */
	protected StorageFile			indexFile;
	protected RandomOutputStream	indexWrite;
	protected RandomInputStream		indexRead;

	protected static DateFormat MYSQL_TIMESTAMP = new SimpleDateFormat("yyyyMMddHHmmss");
    protected static DateFormat MYSQL_DATE = new SimpleDateFormat("yyyy-MM-dd");
    protected static DateFormat MYSQL_TIME = new SimpleDateFormat("HH:mm:ss");

    static {
        MYSQL_DATE.setLenient(true);
        MYSQL_TIME.setLenient(true);
        MYSQL_TIMESTAMP.setLenient(true);
    };

	/**	the actual database query	 */
	protected JoPreparedStatement	statement;
	protected String sql;

	class Reader extends Thread
	{
		JoPreparedStatement pstm;
		int state;

		Reader(JoPreparedStatement statement)
		{
			state = 0;
			pstm = statement;
			setName(identifier+"-reader");
			setDaemon(true);
		}
		
		public void run()
		{
			try {
				state = THREAD_READ;
                fireRowEvent(true);

                if (! pstm.execute())
                    throw new SQLException("SELECT query expected");

				ResultSet res = pstm.getResultSet();
                setMetaData(res.getMetaData());

				while (state==THREAD_READ)
				{
					if (!res.next()) {
                        state = THREAD_FINISHED;
                        break;
                    }

					writeRow(res);
					maxRow++;

                    if ((maxRow < 10) ||
                        (maxRow < 100) && ((maxRow%10)==0) ||
                        (maxRow < 1000) && ((maxRow%100)==0) ||
                        ((maxRow%1000)==0))
                        fireRowEvent(false);
				}
			} catch (Throwable thr) {
				if (state!=THREAD_ABORT)
			    	Application.error(thr);
			} finally {
				fireRowEvent(true);
                try {
	                pstm.closeResult();
                } catch (SQLException sqlex) {
	                Application.error(sqlex);
                }
				if (releaseConnection)
					pstm.getConnection().release();
			}
		}
	}
	
	public CachedResultModel(String identifier, 
							int storage, int cacheSize)
		throws SQLException, IOException
	{
		super(identifier,cacheSize);

        switch (storage)
        {
        case STORE_RAM:
            dataFile = new RAMFile();
            indexFile = new RAMFile();
            break;
        case STORE_DISK:
            dataFile = DiskFile.tempFile();
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
		
		//	fetch row offset
		int offset, flags;
		synchronized (indexRead) {
			indexRead.seek(8*(rowIndex-1));
			offset = indexRead.readInt();
			flags = indexRead.readInt();
		}
		synchronized (dataRead) {
			dataRead.seek(offset);
			readRow(r, flags);
		}
		r.setPK(-1);	//	not known !
		return r;
	}


    public int fetchId(int rowIndex)
        throws IOException
    {
        if (rowIndex > maxRow) return -1;
        if (rowIndex > flushedRow) flush();

        //	fetch row offset
        int offset, flags;
        synchronized (indexRead) {
            indexRead.seek(8*(rowIndex-1));
            offset = indexRead.readInt();
        }
        synchronized (dataRead) {
            dataRead.seek(offset);
            return dataRead.readInt();
        }
    }

    public void open()
        throws Exception
    {
        dataWrite = new RandomOutputStream(dataFile);
        dataRead = new RandomInputStream(dataFile);

        indexWrite = new RandomOutputStream(indexFile);
        indexRead = new RandomInputStream(indexFile);

	    JoConnection conn = JoConnection.get();
	    statement = conn.getPreparedStatement(sql);

        reader = new Reader(statement);
	    reader.setPriority(Thread.MAX_PRIORITY-1);
        reader.start();
    }

	public void open(String sql)
		throws Exception
	{
		this.sql = sql;
		open();
	}

	public void commit()	throws SQLException
	{
		statement.getConnection().commit();
	}

	public void rollback()	throws SQLException
	{
		statement.getConnection().rollback();
	}

	public void close(boolean delete)
        throws Exception
	{
        //  terminate Reader
        if (reader.isAlive()) {
            reader.state = THREAD_ABORT;
	        reader.setPriority(Thread.MIN_PRIORITY);
/*	        try {
	        	reader.join();
	        } catch (InterruptedException iex) {
		        //	try again ...
	        }
*/      }

		try {
			dataWrite.close();
			dataRead.close();
			if (delete) {
                if (!dataFile.delete())
                    System.err.println("temp file "+dataFile.getName()+" not deleted");
                dataFile = null;
            }
            else
                indexFile.setLength(0L);
		} catch (IOException ioex) {
			//	can't help it
			Application.error(ioex);
		}
		try {
			indexWrite.close();
			indexRead.close();
            if (delete) {
                if (!indexFile.delete())
                    System.err.println("temp file "+indexFile.getName()+" not deleted");
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

	public void invalidateRowByPK (int pk)
	{
		throw new AbstractMethodError();
	}

	protected void readRow(Row r, int flags)
		throws IOException
	{
		r.setSize(columnTypes.length);
		for (int i=1; i<columnTypes.length; i++, flags>>=1)
		{
			if ((flags&1) == 0)
				r.set(i, null);
			else
				r.set(i, readObject(columnTypes[i]));
		}
	}
	
	protected Object readObject(int type)
		throws IOException
	{
		switch (type)
		{
		case Types.BIGINT:				String s = dataRead.readUTF();
										return new BigDecimal(s);

		case Types.BIT:					boolean b = dataRead.readBoolean();
										return Util.toBoolean(b);
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:		
		case Types.BLOB:				int len = dataRead.readInt();
										byte[] bytes = new byte[len];
										dataRead.readFully(bytes);
										return bytes;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
		case Types.CLOB:				return dataRead.readUTF();
		case Types.DATE:				return new java.sql.Date(dataRead.readLong());
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:	
		case Types.REAL:				return new Double(dataRead.readDouble());
		case Types.FLOAT:				return new Float(dataRead.readFloat());
		case Types.INTEGER:				return new Integer(dataRead.readInt());
		case Types.SMALLINT:	
		case Types.TINYINT:				return new Short(dataRead.readShort());
		case Types.TIME:				return new Time(dataRead.readLong());
		case Types.TIMESTAMP:			return new Timestamp(dataRead.readLong());
										
		default:						throw new IllegalArgumentException("type "+type+" not supported");
		}
	}
	
	protected void writeRow(ResultSet res)
		throws SQLException, IOException
	{
		int offset, flags;
		
		synchronized (dataWrite) {
			offset = (int)dataWrite.getPosition();
			flags = 0;
			
			for (int i=1, bit=1; i<columnTypes.length; i++, bit<<=1)
				if (writeObject(res,i, columnTypes[i]))
					flags |= bit;
		}
		
		synchronized (indexWrite) {
			indexWrite.writeInt(offset);
			indexWrite.writeInt(flags);
		}
	}
	
	protected boolean writeObject(ResultSet res, int column, int type)
		throws SQLException, IOException
	{
		boolean b=false;
		int i=0;
		short s=0;
        long l=0L;
		double d=0.0;
		float f=0.0f;
		Object dt=null;
		
		switch (type)
		{
		case Types.BIGINT:				BigDecimal bi = res.getBigDecimal (column); 
										if (bi==null)
											return false;
										dataWrite.writeUTF(bi.toString()); 
										return true;
										
		case Types.BIT:					b = res.getBoolean(column); break;
		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:		
		case Types.BLOB:				byte[] by = res.getBytes(column); 
										if (by==null)
											return false;
										dataWrite.write(by); 
										return true;
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:			
		case Types.CLOB:				String str = res.getString(column); 
										if (str==null)
											return false;
										dataWrite.writeUTF(str); 
										return true;
										
		case Types.DATE:				dt = res.getObject(column);
                                        if (dt instanceof String)   //  workaround for MySQL
                                            try {
                                                dt = MYSQL_DATE.parse((String)dt);
                                            } catch (ParseException pex) {
                                                throw new SQLException("illegal date format "+dt);
                                            }
                                        break;
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:	
		case Types.REAL:				d = res.getDouble(column); break;
		case Types.FLOAT:				f = res.getFloat(column); break;
		case Types.INTEGER:				i = res.getInt(column); break;
		case Types.SMALLINT:	
		case Types.TINYINT:				s = (short)res.getInt(column); break;
		case Types.TIME:				dt = res.getObject(column);
                                        if (dt instanceof String)   //  workaround for MySQL
                                            try {
                                                dt = MYSQL_TIME.parse((String)dt);
                                            } catch (ParseException pex) {
                                                throw new SQLException("illegal time format "+dt);
                                            }
                                        break;
		case Types.TIMESTAMP:			dt = res.getObject(column);
                                        if (dt instanceof String)   //  workaround for MySQL
                                            try {
                                                dt = MYSQL_TIMESTAMP.parse((String)dt);
                                            } catch (ParseException pex) {
                                                throw new SQLException("illegal timestamp format "+dt);
                                            }
										break;

		default:						throw new IllegalArgumentException("type "+type+" not supported");
		}

		if (res.wasNull())
			return false;
		
		switch (type)
		{
		case Types.BIT:					dataWrite.writeBoolean(b); break;
		
		case Types.TIME:
		case Types.TIMESTAMP:
		case Types.DATE:				if (dt==null)
											return false;
                                        if (dt instanceof java.util.Date)
										    dataWrite.writeLong(((java.util.Date)dt).getTime());
                                        else
                                            throw new SQLException("unexpected type "+dt.getClass().getName());
										return true;
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.NUMERIC:	
		case Types.REAL:				dataWrite.writeDouble(d); break;
		case Types.FLOAT:				dataWrite.writeFloat(f); break;
		case Types.INTEGER:				dataWrite.writeInt(i); break;
		case Types.SMALLINT:	
		case Types.TINYINT:				dataWrite.writeShort(s); break;
		}
		
		return true;
	}	
	
    public boolean isWorking()
    {
        return reader.isAlive() && (reader.state == THREAD_READ);
    }
    
	protected void flush()
		throws IOException
	{
		synchronized (dataWrite) {
			dataWrite.flush();
		}
		synchronized (indexWrite) {
			indexWrite.flush();
		}
		synchronized (dataRead) {
			dataRead.flush();
		}
		synchronized (indexRead) {
			indexRead.flush();
		}
		flushedRow = maxRow;
	}
}
