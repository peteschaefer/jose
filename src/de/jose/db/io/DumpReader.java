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

import de.jose.Util;
import de.jose.db.Row;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Types;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
/** @deprecated */
public class DumpReader
        implements IDumpFlags
{
	/**	the ZIP file	*/
	private ZipFile zipFile;
	/**	File info	*/
	private DBFileInfo fileInfo;
	/**	currently open result set	*/
	private ResultSetInfo resInfo;
	/**	currently open result stream	*/
	private InputStream	input;
	/**	current row number (1=first ...)	 */
	private int currentRow;
	/**	row header flags */
	private byte[] flags;
	/**	data buffer	*/
	private byte[] buffer;

	public DumpReader(File file)
		throws IOException
	{
		zipFile = new ZipFile(file);
		readInfo();
	}


	public void close() throws IOException
	{
		zipFile.close();
	}


	public int getFileVersion()				{ return fileInfo.getFileVersion();	}

	public String getContentType()			{ return fileInfo.getContentType(); }

	public Set getTableNames()				{ return fileInfo.getTableNames(); }

	public int getTableVersion(String schema, String tableName)
	{
		return fileInfo.getTableVersion(schema,tableName);
	}

	public ResultSetInfo getResultInfo(String zipName)
	{
		return fileInfo.getResultInfo(zipName);
	}


	/**
	 * open a result set for reading
	 */
	public void openResult(String zipName)
		throws IOException
	{
		openResult(getResultInfo(zipName));
	}

	/**
	 * open a result set for reading
	 */
	public void openResult(ResultSetInfo info)
		throws IOException
	{
		resInfo = info;
		ZipEntry ety = zipFile.getEntry(info.getZipName());
		input = zipFile.getInputStream(ety);
		currentRow = 1;
		flags = new byte[(resInfo.countColumns()+1)/2];
		ensureBuffer(8);
	}

	/**
	 * @return the current row in the result set
	 */
	public int getRowNum()
	{
		return currentRow;
	}

	/**
	 * @return the number of rows in the current result set
	 */
	public int countRows()
	{
		return resInfo.countRows();
	}

	public boolean hasNext()
	{
		return currentRow <= countRows();
	}


	public Row readRow(Row r)
		throws IOException
	{
		int cols = resInfo.countColumns();
		if (r==null)
			r = new Row(cols);
		else
			r.setColumnCount(cols);

		input.read(flags,0,flags.length);
		for (int col=1; col<=cols; col++)
		{
			byte flag = flags[(col-1)/2];
			if ((col%2)==0) flag >>= 4;
			flag &= 0x0f;

			Object obj = readObject(flag, resInfo.getDataType(col));
			r.set(col,obj);
			if (resInfo.getColumnName(col).equals("Id"))
				r.setPK(((Number)obj).intValue());
		}

		r.setRowNum(currentRow++);
		return r;
	}

	public void closeResult()
		throws IOException
	{
		input.close();
	}

	protected void readInfo()
		throws IOException
	{
		ZipEntry ety = zipFile.getEntry("i");
		InputStream in = zipFile.getInputStream(ety);
		fileInfo = readInfo(in);
		in.close();
	}

	public static DBFileInfo readInfo(InputStream in) throws IOException
	{
		ObjectInputStream oin = new ObjectInputStream(in);
		try {
			return (DBFileInfo)oin.readObject();
		} catch (ClassNotFoundException cnfex) {
			throw new IOException(cnfex.getMessage());
		}
	}


    protected static DBFileInfo readInfo(File file)
        throws IOException
    {
        DumpReader reader = new DumpReader(file);
        reader.readInfo();
        reader.close();
        return reader.fileInfo;
    }

	private Object readObject(byte flags, int type)
		throws IOException
	{
		if (flags==NULL)
			return null;

		switch (type)
		{
			 case Types.LONGVARBINARY:
			 case Types.BINARY:
					break;

			 case Types.LONGVARCHAR:
		     case Types.VARBINARY:
					break;

//		     case Types.BOOLEAN:
		     case Types.BIT:
					int i = readInt(flags);
					return Util.toBoolean(i!=0);

		     case Types.BLOB:

		     case Types.CLOB:
					break;

		     case Types.CHAR:
		     case Types.VARCHAR:
					int len = readInt(flags);
					return readCharData(len);

		     case Types.DATE:
					long millis = readLong(flags);
					return new java.sql.Date(millis);
		     case Types.TIME:
					millis = readLong(flags);
					return new java.sql.Time(millis);

		     case Types.TIMESTAMP:
					millis = readLong(flags);
					return new java.sql.Timestamp(millis);

		     case Types.DECIMAL:
		     case Types.NUMERIC:
		     case Types.REAL:
		     case Types.FLOAT:
					int ibits = readInt(flags);
					return new Float(Float.intBitsToFloat(ibits));

		     case Types.DOUBLE:
					long lbits = readLong(flags);
					return new Double(Double.longBitsToDouble(lbits));

		     case Types.SMALLINT:
		     case Types.TINYINT:
		     case Types.INTEGER:
					return new Integer(readInt(flags));

		     case Types.BIGINT:
   					return new Long(readLong(flags));
		}

		throw new IllegalArgumentException("unknown datatype "+type);
	}

	private int readInt(byte flags)
		throws IOException
	{
		switch (flags)
		{
		case NULL:		throw new IllegalArgumentException("must not pass NULL");
		case ZERO:		return 0;
		case ONE:		return 1;

		case UNSIGNED1:		return readUInt1();
		case UNSIGNED2:		return readUInt2();
		case UNSIGNED3:		return readUInt3();
		case UNSIGNED4:		return readUInt4();

		case SIGNED1:		return -readUInt1();
		case SIGNED2:		return -readUInt2();
		case SIGNED3:		return -readUInt3();
		case SIGNED4:		return -readUInt4();
		}

		throw new IllegalArgumentException();
	}

	private int readUInt1()
		throws IOException
	{
		input.read(buffer,0,1);
		return buffer[0] & 0x000000ff;
	}

	private int readUInt2()
		throws IOException
	{
		input.read(buffer,0,2);
		return (((int)buffer[1] << 8)
		        | buffer[0]) & 0x0000ffff;
	}

	private int readUInt3()
		throws IOException
	{
		input.read(buffer,0,3);
		return (((int)buffer[2] << 16)
		        | ((int)buffer[1] << 8)
		        | buffer[0]) & 0x00ffffff;
	}

	private int readUInt4()
		throws IOException
	{
		input.read(buffer,0,4);
		return (((int)buffer[3] << 24)
		        | ((int)buffer[2] << 16)
				| ((int)buffer[1] << 8)
				| buffer[0]);
	}

	private long readULong5()
		throws IOException
	{
		input.read(buffer,0,5);
		return (((long)buffer[4] << 32)
		        | ((long)buffer[3] << 24)
		        | ((long)buffer[2] << 16)
				| ((long)buffer[1] << 8)
				| buffer[0]) & 0x000000ffffffffffL;
	}

	private long readULong6()
		throws IOException
	{
		input.read(buffer,0,6);
		return (((long)buffer[5] << 40)
		        | ((long)buffer[4] << 32)
		        | ((long)buffer[3] << 24)
		        | ((long)buffer[2] << 16)
				| ((long)buffer[1] << 8)
				| buffer[0]) & 0x0000ffffffffffffL;
	}

	private long readLong8()
		throws IOException
	{
		input.read(buffer,0,8);
		return (((long)buffer[7] << 56)
		        | ((long)buffer[6] << 48)
		        | ((long)buffer[5] << 40)
		        | ((long)buffer[4] << 32)
		        | ((long)buffer[3] << 24)
		        | ((long)buffer[2] << 16)
				| ((long)buffer[1] << 8)
				| buffer[0]);
	}

	private long readLong(byte flags)
		throws IOException
	{
		switch (flags)
		{
		case NULL:		throw new IllegalArgumentException("must not pass NULL");
		case ZERO:		return 0L;
		case ONE:		return 1L;

		case UNSIGNED1:		return readUInt1();
		case UNSIGNED2:		return readUInt2();
		case UNSIGNED3:		return readUInt3();
		case UNSIGNED4:		return readUInt4();
		case UNSIGNED5:		return readULong5();
		case UNSIGNED6:		return readULong6();

		case SIGNED1:		return -readUInt1();
		case SIGNED2:		return -readUInt2();
		case SIGNED3:		return -readUInt3();
		case SIGNED4:		return -readUInt4();
		case SIGNED5:		return -readULong5();
		case SIGNED6:		return -readULong6();

		case LONG:			return readLong8();
		}

		throw new IllegalArgumentException();
	}

	private String readCharData(int len)
		throws IOException
	{
		ensureBuffer(len);
		input.read(buffer,0,len);
		return new String(buffer,0,len,"UTF-8");
	}



	private void ensureBuffer(int len)
	{
		if (buffer==null || buffer.length < len)
			buffer = new byte[nextPow2(len)];
	}

	private int nextPow2(int x)
	{
		int result = 64;
		while (result < x)
			result *= 2;
		return result;
	}
}
