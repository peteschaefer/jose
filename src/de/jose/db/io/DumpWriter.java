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

package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;

import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/** @deprecated */
public class DumpWriter
        implements IDumpFlags
{
    /** the output stream   */
    private OutputStream    out;

    /** the ZIP output stream   */
    private ZipOutputStream zout;

    /** the file info   */
    private DBFileInfo      fileInfo;

    /** database connection */
    private JoConnection    conn;

    /** row data buffer  */
    private byte[][]        buffer;
	/**	string length buffer	*/
	private int[]			intBuffer;
	/**	blob length buffer	*/
	private long[]			longBuffer;
	/**	blob object buffer	*/
	private Object[]		objBuffer;
	/**	stream copy buffer	*/
	private byte[]			copyBuffer;
	
    public DumpWriter(OutputStream output, JoConnection connection,
                      String contentType, boolean normPK)
    {
        out = output;
        zout = new ZipOutputStream(new BufferedOutputStream(out,4096));
//		zout.setMethod(9);	//	= best compression

        fileInfo = new DBFileInfo(contentType,normPK);
        conn = connection;
    }

    public JoPreparedStatement getPreparedStatement(String sql)
        throws SQLException
    {
        return conn.getPreparedStatement(sql);
    }

    public ResultSetInfo openResultSet(JoPreparedStatement stm)
        throws IOException, SQLException
    {
        stm.execute();

        ResultSet res = null;
        ResultSetInfo ri;

        res = stm.getResultSet();
        return openResultSet(res);
    }


    public ResultSetInfo openResultSet(ResultSet result)
        throws SQLException, IOException
    {
        ZipEntry zety = new ZipEntry("r"+(fileInfo.countResultSets()+1));
        zout.putNextEntry(zety);

        ResultSetMetaData rmd = result.getMetaData();
        ResultSetInfo ri = new ResultSetInfo(rmd, zety.getName());
        fileInfo.addResultSet(ri);

        createBuffer(rmd);
        return ri;
    }

    public void writeResultSet(ResultSet result, ResultSetInfo ri)
        throws IOException, SQLException
    {
        while (result.next())
        {
            writeResultRow(result, ri);
            ri.rowCount++;
        }
    }

	public void closeResultSet()
	    throws IOException
	{
	    zout.closeEntry();
	}

    protected void writeResultRow(ResultSet result, ResultSetInfo ri)
        throws IOException, SQLException
    {
        //  reset flags
        Arrays.fill(buffer[0], (byte)0);

        for (int i = 1; i <= ri.countColumns(); i++)
        {
            byte flags = bufferColumn(result,i, ri.getDataType(i));
            if ((i%2)==1)
                buffer[0][(i-1)/2] = flags;
            else
                buffer[0][(i-1)/2] |= (flags<<4);
        }

        //  write flags
        zout.write(buffer[0]);
	    //	write data
        for (int i = 1; i <= ri.countColumns(); i++)
        {
           byte flags = buffer[0][(i-1)/2];
           if ((i%2)==0) flags >>= 4;
	       flags &= 0x0f;
           writeColumn(i, ri.getDataType(i), flags);
        }
    }

    private byte[][] createBuffer(ResultSetMetaData rmd)
        throws SQLException
    {
        int count = rmd.getColumnCount();
        buffer = new byte[count+1][];

        //  buffer[0] = flag set, 4 bits per column
        buffer[0] = new byte[(count+1)/2];

        for (int i=1; i <= count; i++)
        {
            switch (rmd.getColumnType(i))
            {
            case Types.BINARY:
            case Types.VARBINARY:   buffer[i] = new byte[8+rmd.getColumnDisplaySize(i)]; break;
//            case Types.BOOLEAN:
            case Types.BIT:         buffer[i] = new byte[1]; break;
            case Types.BLOB:
            case Types.CLOB:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR: buffer[i] = new byte[8]; break;   //  no buffer needed
            case Types.CHAR:
            case Types.VARCHAR:     buffer[i] = new byte[8+3*rmd.getColumnDisplaySize(i)]; break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:   buffer[i] = new byte[8]; break;
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.FLOAT:       buffer[i] = new byte[8]; break;
            case Types.DOUBLE:      buffer[i] = new byte[8]; break;
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.INTEGER:     buffer[i] = new byte[8]; break;
            case Types.BIGINT:      buffer[i] = new byte[8]; break;

            default:                /*  not supported   */
                                    throw new SQLException("type "+rmd.getColumnType(i)+" not supported");
            }
        }

	    intBuffer = new int[count+1];
	    longBuffer = new long[count+1];
	    objBuffer = new Object[count+1];
		copyBuffer = new byte[1024];

        return buffer;
    }

    private byte bufferColumn(ResultSet res, int i, int type)
        throws SQLException
    {
        switch (type)
        {
//            case Types.BOOLEAN:
            case Types.BIT:			boolean b = res.getBoolean(i);
		        					if (res.wasNull()) return NULL;
		        					return b ? ONE:ZERO;

	        case Types.LONGVARCHAR:
            case Types.CLOB:		Clob clob = res.getClob(i);
		        					if (clob==null) return NULL;
		        					objBuffer[i] = clob;
		        					return bufferLong(longBuffer[i] = clob.length(), buffer[i]);

		    case Types.BINARY:
	        case Types.LONGVARBINARY:
		    case Types.VARBINARY:
            case Types.BLOB:		byte[] bin = res.getBytes(i);
		        					if (bin==null) return NULL;
		        					objBuffer[i] = bin;
		        					return bufferInt(intBuffer[i] = bin.length, buffer[i]);


            case Types.CHAR:
            case Types.VARCHAR:     String s = res.getString(i);
                                    if (s==null) return NULL;
                                    //  encode as UTF-8
		        					return bufferInt(intBuffer[i] = bufferUTF(s,buffer[i]), buffer[i]);

            case Types.DATE:        java.sql.Date dt = res.getDate(i);
                                    if (dt==null) return NULL;
                                    return bufferLong(dt.getTime(),buffer[i]);

            case Types.TIME:        java.sql.Time t = res.getTime(i);
                                    if (t==null) return NULL;
                                    return bufferLong(t.getTime(),buffer[i]);

            case Types.TIMESTAMP:   java.sql.Timestamp ts = res.getTimestamp(i);
                                    if (ts==null) return NULL;
                                    return bufferLong(ts.getTime(),buffer[i]);
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.FLOAT:       float f = res.getFloat(i);
                                    if (res.wasNull()) return NULL;
                                    return bufferInt(Float.floatToIntBits(f),buffer[i]);

            case Types.DOUBLE:      double d = res.getDouble(i);
                                    if (res.wasNull()) return NULL;
                                    return bufferLong(Double.doubleToLongBits(d),buffer[i]);

            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.INTEGER:     int j = res.getInt(i);
                                    if (res.wasNull()) return NULL;
                                    return bufferInt(j,buffer[i]);

            case Types.BIGINT:      long l = res.getLong(i);
                                    if (res.wasNull()) return NULL;
                                    return bufferLong(l,buffer[i]);
        }

		throw new SQLException("type "+type+" not supported");
    }

    /** TODO
     *  @return flags bits
     * */
    private byte bufferInt(int x, byte[] buffer)
    {
        if (x==0)
	    	return ZERO;	//	that was easy ;-)
		if (x==1)
			return ONE;

	    if (x < 0)
		    return (byte)(SIGNED1+bufferUInt(-x,buffer));

		return (byte)(UNSIGNED1+bufferUInt(x,buffer));
    }

	private byte bufferUInt(int x, byte[] buffer)
	{
		buffer[0] = (byte)(x & 0x000000ff);
		x >>= 8;
		if (x==0) return 0;

		buffer[1] = (byte)(x & 0x000000ff);
		x >>= 8;
		if (x==0) return 1;

		buffer[2] = (byte)(x & 0x000000ff);
		x >>= 8;
		if (x==0) return 2;

		buffer[3] = (byte)(x & 0x000000ff);
		return 3;
	}

    /** TODO
     *  @return flags bits
     * */
    private byte bufferLong(long x, byte[] buffer)
    {
		if (x==0L)
			return ZERO;
		if (x==1L)
			return ONE;

	    if (x < 0) {
		    if ((-x & 0xffff000000000000L) != 0L)
			    return buffer8Long(x,buffer);
		    return (byte)(SIGNED1+bufferULong(-x,buffer));
	    }
	    else {
		    if ((x & 0xffff000000000000L) != 0L)
			    return buffer8Long(x,buffer);
		    return (byte)(UNSIGNED1+bufferULong(x,buffer));
	    }
    }

	private byte bufferULong(long x, byte[] buffer)
	{
		buffer[0] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		if (x==0L) return 0;

		buffer[1] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		if (x==0L) return 1;

		buffer[2] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		if (x==0L) return 2;

		buffer[3] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		if (x==0L) return 3;

		buffer[4] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		if (x==0L) return 4;

		buffer[5] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		return 5;
	}

	private byte buffer8Long(long x, byte[] buffer)
	{
		buffer[0] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[1] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[2] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[3] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[4] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[5] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[6] = (byte)(x & 0x00000000000000ff);
		x >>= 8;
		buffer[7] = (byte)(x & 0x00000000000000ff);
		return LONG;
	}

    /** TODO
     *  @return flags bits
     * */
    private int bufferUTF(String x, byte[] buffer)
    {
        int len = 0;
        for (int i=0; i < x.length(); i++)
        {
            char c = x.charAt(i);
            if (c < 0x0080)
                buffer[4 + len++] = (byte)(c & 0x007f);
            else if (c < 0x0800) {
                buffer[4 + len++] = (byte)(0xc0 | (c>>6) & 0x1f);
	            buffer[4 + len++] = (byte)(0x80 | c & 0x3f);
            }
	        else {
	            buffer[4 + len++] = (byte)(0xe0 | (c >> 12) & 0x0f);
	            buffer[4 + len++] = (byte)(0x80 | (c >> 6) & 0x3f);
	            buffer[4 + len++] = (byte)(0x80 | c & 0x3f);
            }
        }
        return len;
    }

    /**
     * TODO
     * @param type
     * @param flags
     * @throws SQLException
     */
    private void writeColumn(int i, int type, byte flags)
        throws SQLException, IOException
    {
        switch (type)
         {
//             case Types.BOOLEAN:
             case Types.BIT:		/*	no data	*/
                                    break;

			 case Types.BINARY:
			 case Types.VARBINARY:
		     case Types.LONGVARBINARY:
	         case Types.BLOB:		if (flags==NULL) break;
		        					writeInt(buffer[i],flags);

		        					byte[] bin = (byte[])objBuffer[i];
		        					zout.write(bin);
		        				/*	Blob blob = (Blob)objBuffer[i];
		        					copyStream(blob.getBinaryStream(), zout, longBuffer[i]);
		        				*/	break;

             case Types.CLOB:		if (flags==NULL) break;
		        					writeInt(buffer[i],flags);
		        					Clob clob = (Clob)objBuffer[i];
		        					//copyStream(clob.getCharacterStream(), zout, longBuffer[i]);
		        					throw new SQLException("Clob not yet supported");
		        					//break;

             case Types.LONGVARCHAR:
                                     break;
             case Types.CHAR:
             case Types.VARCHAR:	writeInt(buffer[i],flags);
		        					writeCharData(buffer[i], intBuffer[i]);
		        					break;
             case Types.DATE:
             case Types.TIME:
             case Types.TIMESTAMP:	writeInt(buffer[i],flags);
                                    break;
             case Types.DECIMAL:
             case Types.NUMERIC:
             case Types.REAL:
             case Types.FLOAT:		writeInt(buffer[i],flags);
                                    break;
             case Types.DOUBLE:		writeInt(buffer[i],flags);
                                    break;
             case Types.SMALLINT:
             case Types.TINYINT:
             case Types.INTEGER:	writeInt(buffer[i],flags);
                                    break;

             default:                /*  not supported   */
                                     throw new SQLException("type "+type+" not supported");

         }
    }

	private void copyStream(InputStream bin, OutputStream out, long length)
		throws IOException
	{
		while (length > 0)
		{
			int count = (int)Math.min(length,copyBuffer.length);
			count = bin.read(copyBuffer);
			if (count < 0) throw new IOException("unexpected end of input");

			out.write(copyBuffer,0,count);
			length -= count;
		}
	}

	private void writeInt(byte[] buf, byte flags)
		throws IOException
	{
		switch (flags) {
		case NULL:
		case ZERO:
		case ONE:			/*	no data !	*/	return;
		case SIGNED1:
		case UNSIGNED1:		zout.write(buf,0,1); return;
		case SIGNED2:
		case UNSIGNED2:		zout.write(buf,0,2); return;
		case SIGNED3:
		case UNSIGNED3:		zout.write(buf,0,3); return;
		case SIGNED4:
		case UNSIGNED4:		zout.write(buf,0,4); return;
		case SIGNED5:
		case UNSIGNED5:		zout.write(buf,0,5); return;
		case SIGNED6:
		case UNSIGNED6:		zout.write(buf,0,6); return;
		case LONG:			zout.write(buf,0,8); return;
		}
		return;
	}

	private void writeCharData(byte[] buf, int len)
		throws IOException
	{
		zout.write(buf,4,len);
	}

    public void close()
        throws IOException, SQLException
    {
        fileInfo.finish(conn);

        ZipEntry zety = new ZipEntry("i");
        zout.putNextEntry(zety);

        ObjectOutputStream oout = new ObjectOutputStream(zout);
        oout.writeObject(fileInfo);
        oout.flush();

        zout.closeEntry();
        zout.finish();
        zout.close();
    }
}
