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

import com.mysql.embedded.api.*;
import com.mysql.embedded.util.ByteBufferOutputStream;
import com.mysql.embedded.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * @author Peter Sch�fer
 */

public class BindArray
        implements enum_field_types
{
	/** parent statement    */
	protected MyPreparedStatement statement;

	/** number of parameters    */
	protected int n;

	/** byte buffer with MYSQL_BIND records, length and is_null.
	 *  it is laid out as follows
	 *
	 *  MYSQL_BIND[n]
	 *
	 *  long int[n]  lengths (n * 8bytes)
	 *
	 *  byte[n] is_null (n bytes)
	 */
	protected ByteBuffer bindArray;
	/** native address  */
	protected long nativeAddress;

	/** MYSQL_BIND wrappers */
	protected MYSQL_BIND[] bind;

	/** value buffers (initially empty) */
	protected ByteBuffer[] value;

	/** minimum buffer size */
	protected static final int MIN_BUFFER = MYSQL_TIME.SIZE;

	//  -------------------------------
	//      ctor
	//  -------------------------------

	public BindArray(MyPreparedStatement stm, int n)
	{
		this.statement = stm;
		this.n = n;
		this.bindArray = ByteBuffer.allocateDirect(MYSQL_BIND.SIZE*n + MYSQL_BIND.SIZE_LEN*n + MYSQL_BIND.SIZE_BOOL*n);
		this.bindArray.order(ByteOrder.nativeOrder());

		this.nativeAddress = res.get_address(bindArray);
		this.bind = new MYSQL_BIND[n];
		this.value = new ByteBuffer[n]; //  initially empty

		//  wire up MYSQL_BIND pointers to is_null and length
		for (int i=0; i < n; i++)
		{
			this.bind[i] = new MYSQL_BIND(bindArray, nativeAddress, i*MYSQL_BIND.SIZE);
			this.bind[i].setLengthOffset(n*MYSQL_BIND.SIZE + MYSQL_BIND.SIZE_LEN*i);
			this.bind[i].setIsNullOffset(n*MYSQL_BIND.SIZE + MYSQL_BIND.SIZE_LEN*n + MYSQL_BIND.SIZE_BOOL*i);
			//  buffers are not yet set
		}
	}

	public void initialize(MyResultSetMetaData meta)
	{
		for (int i=0; i < n; i++)
		{
			int type = meta.getMySqlColumnType(i+1);
			int size = meta.getLength(i+1);

			switch (type)
			{
			case MYSQL_TYPE_TINY:
			case MYSQL_TYPE_SHORT:
			case MYSQL_TYPE_LONG:
			case MYSQL_TYPE_LONGLONG:
			case MYSQL_TYPE_FLOAT:
			case MYSQL_TYPE_DOUBLE:
			case MYSQL_TYPE_DATE:
			case MYSQL_TYPE_TIME:
			case MYSQL_TYPE_TIMESTAMP:
			case MYSQL_TYPE_DATETIME:
			case MYSQL_TYPE_YEAR:
            case MYSQL_TYPE_BIT:        allocateBuffer(i, type, MIN_BUFFER);
										break;

            case MYSQL_TYPE_BLOB:		if (size < 0) size = 4096;
	                                    allocateBuffer(i, type, Math.min(size,4096));
										break;

			default:					if (size < 0) throw new IllegalStateException(size+" < 0");
										allocateBuffer(i, type, size);
										break;
			}
		}
	}

	public void clear()
	{
		for (int i=0; i < n; i++)
		{
			bind[i].setNull(true);
			bind[i].setBufferPtr(0);
			value[i] = null;
		}
	}

	public void close()
	{
		//  is native memory gc'ed ??
		bindArray = null;
		bind = null;
		value = null;
	}


	//  -------------------------------
	//      accessors
	//  -------------------------------

	public int size()                       { return n; }

	public int getBufferType(int i)         { return bind[i].getBufferType(); }
	public boolean isNull(int i)            { return bind[i].isNull(); }
	public boolean isUnsigned(int i)        { return bind[i].isUnsigned(); }
	public int getLength(int i)             { return bind[i].getLength(); }
	public int getBufferLength(int i)       { return bind[i].getBufferLength(); }
//	public ByteBuffer getBuffer(int i)      { return value[i]; }

	public void setNull(int i, int type)
	{
		bind[i].setNull(true);
		bind[i].setBufferType(type);
	}
    

    public ByteBuffer allocateBuffer(int i, int type, int capacity)
	{
/*
		bind[i].setBufferType(type);
		bind[i].setUnsigned(false); //  java data is always signed
		bind[i].setNull(false);
		bind[i].setLength(capacity);
		// combined into one call:
*/
		if (value[i]==null || value[i].capacity() < capacity)
		{
			value[i] = ByteBuffer.allocateDirect(capacity);
			value[i].order(ByteOrder.nativeOrder());
/*
			bind[i].setBufferPtr(res.get_address(value[i]));
			bind[i].setBufferLength(capacity);
*/
			stmt.set_bind(nativeAddress+i*MYSQL_BIND.SIZE, type,capacity, value[i]);
		}
		else
			stmt.set_bind(nativeAddress+i*MYSQL_BIND.SIZE,type,capacity,null);

		//  reset limit to max. capacity
		value[i].limit(value[i].capacity());
		return value[i];
	}

	//  -------------------------------
	//      setters
	//  -------------------------------

	public void setByte(int i, byte x)
	{
		if (value[i]==null || value[i].capacity() < 1)
		{
			value[i] = ByteBuffer.allocateDirect(1);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_byte(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_byte(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setShort(int i, short x)
	{
		if (value[i]==null || value[i].capacity() < 2)
		{
			value[i] = ByteBuffer.allocateDirect(2);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_short(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_short(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setInt(int i, int x)
	{
		if (value[i]==null || value[i].capacity() < 4)
		{
			value[i] = ByteBuffer.allocateDirect(4);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_int(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_int(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setLong(int i, long x)
	{
		if (value[i]==null || value[i].capacity() < 8)
		{
			value[i] = ByteBuffer.allocateDirect(8);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_long(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_long(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setFloat(int i, float x)
	{
		if (value[i]==null || value[i].capacity() < 4)
		{
			value[i] = ByteBuffer.allocateDirect(4);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_float(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_float(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setDouble(int i, double x)
	{
		if (value[i]==null || value[i].capacity() < 8)
		{
			value[i] = ByteBuffer.allocateDirect(8);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_double(nativeAddress+i*MYSQL_BIND.SIZE, value[i], x);
		}
		else
			stmt.set_double(nativeAddress+i*MYSQL_BIND.SIZE, null, x);
	}

	public void setBytes(int i, int type, byte[] x, int len)
	{
		if (value[i]==null || value[i].capacity() < len)
		{
			value[i] = ByteBuffer.allocateDirect(len);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_bytes(nativeAddress+i*MYSQL_BIND.SIZE, value[i], type, x,len);
		}
		else
			stmt.set_bytes(nativeAddress+i*MYSQL_BIND.SIZE, null, type, x,len);
	}

	public void setCharBuffer(int i, int type, CharBuffer cbuffer)
	{
		CharsetEncoder enc = statement.connection.charEncoder;

		ByteBuffer bbuffer = allocateBuffer(i, type, 3*cbuffer.limit());
		bbuffer.position(0);

		enc.reset();
		enc.encode(cbuffer,bbuffer,true);
		enc.flush(bbuffer);
		bind[i].setLength(bbuffer.position());
	}

	public void setString(int i, int type, String string)
	{
		setCharBuffer(i,type, CharBuffer.wrap(string));
	}


	public void setDirectByteBuffer(int i, int type, ByteBuffer x)
	{
		x.order(ByteOrder.nativeOrder());
		x.position(0);

		stmt.set_bind(nativeAddress+i*MYSQL_BIND.SIZE, type, x.limit(), x);
/*
		bind[i].setBufferType(type);
		bind[i].setUnsigned(false); //  java data is always signed
		bind[i].setNull(false);

		value[i] = x;

		long nativeAddress = res.get_address(value[i]);
		bind[i].setBufferPtr(nativeAddress);
		bind[i].setBufferLength(x.capacity());
*/
	}

	public void setInputStream(int i, int type, InputStream value, int length) throws SQLException
	{
		ByteBuffer buffer;
		try {
			buffer = allocateBuffer(i,type,length);
			buffer.position(0);
			ByteBufferOutputStream output = new ByteBufferOutputStream(buffer);
			Streams.copyStream(value,output,length);
		} catch (IOException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	public void setReader(int i, int type, Reader value, int length) throws SQLException
	{
		ByteBuffer buffer;
		try {
			buffer = allocateBuffer(i,type,length);
			buffer.position(0);
			ByteBufferOutputStream output = new ByteBufferOutputStream(buffer);
			Streams.copyReader(value,output,length);
		} catch (IOException e) {
			throw new SQLException(e.getLocalizedMessage());
		}
	}

	public void setDate(int i, int type, Calendar cal)
	{
		if (value[i]==null || value[i].capacity() < MYSQL_TIME.SIZE)
		{
			value[i] = ByteBuffer.allocateDirect(MYSQL_TIME.SIZE);
			value[i].order(ByteOrder.nativeOrder());

			stmt.set_date(nativeAddress+i*MYSQL_BIND.SIZE, value[i], type,
			        (short)cal.get(Calendar.YEAR),
			        (short)(cal.get(Calendar.MONTH)-Calendar.JANUARY+1),
			        (short)cal.get(Calendar.DAY_OF_MONTH),
			        (short)cal.get(Calendar.HOUR_OF_DAY),
			        (short)cal.get(Calendar.MINUTE),
			        (short)cal.get(Calendar.SECOND),
			        (short)cal.get(Calendar.MILLISECOND));
		}
		else
			stmt.set_date(nativeAddress+i*MYSQL_BIND.SIZE, null, type,
			        (short)cal.get(Calendar.YEAR),
			        (short)(cal.get(Calendar.MONTH)-Calendar.JANUARY+1),
			        (short)cal.get(Calendar.DAY_OF_MONTH),
			        (short)cal.get(Calendar.HOUR_OF_DAY),
			        (short)cal.get(Calendar.MINUTE),
			        (short)cal.get(Calendar.SECOND),
			        (short)cal.get(Calendar.MILLISECOND));

	}

	protected static int roundUp(int size)
	{
		//  round to next multiple of 4096
		return 4096 * ((size+4095)/4096);
	}

	//  -------------------------------
	//      getters
	//  -------------------------------

	public boolean exceedsCapacity(int i)
	{
		if (bind[i].isNull())
			return false;
		else
			return bind[i].getLength() > value[i].capacity();
	}

	public void fetchRest(int i, long statementHandle) throws SQLException
	{
		//  result size exceeds buffer
		ByteBuffer oldBuffer = value[i];
		int oldSize = value[i].capacity();
		int len = bind[i].getLength();

		if (len<=oldSize) return;   //  nothing to do

		//  reallocate buffer
		int capacity = roundUp(len);
		allocateBuffer(i, bind[i].getBufferType(), capacity);
		//  copy existing part
		oldBuffer.position(0);
		oldBuffer.limit(oldSize);
		value[i].put(oldBuffer);
		//  fetch remaining part (with an offset!)
		stmt.mysql_stmt_fetch_column(statementHandle, nativeAddress+i*MYSQL_BIND.SIZE, i, oldSize, oldSize);
	}

	public ByteBuffer getByteBuffer(int i) throws SQLException
	{
		if (bind[i].isNull())
			return null;
		else {
			//  trim to size
			value[i].position(0);
			int len = bind[i].getLength();
			try {
				value[i].limit(len);
			} catch (IllegalArgumentException iaex) {
				throw new SQLException("illegal array size "+len);
			}
			return value[i];
		}
	}

	public byte getByte(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		return value[i].get(0);         //	possible loss of sign !!

		case MYSQL_TYPE_SHORT:		short svalue = value[i].getShort(0);
									return bind[i].isUnsigned() ?
									    (byte)(0x00FF & svalue) : (byte)svalue;     //	possible loss of precision

		case MYSQL_TYPE_LONG:		int ivalue = value[i].getInt(0);
									return bind[i].isUnsigned() ?
									    (byte)(0x000000FF & ivalue) : (byte)ivalue;   //	possible loss of precision

		case MYSQL_TYPE_LONGLONG:	long lvalue = value[i].getLong(0);
									return bind[i].isUnsigned() ?
		                                (byte)(0x00000000000000FF & lvalue) :
										(byte)lvalue;            		    //	possible loss of precision

		case MYSQL_TYPE_FLOAT:		return (byte)value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return (byte)value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
										return Byte.parseByte(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public short getShort(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		byte bvalue = value[i].get(0);
									return bind[i].isUnsigned() ?
		                                (short)(0x00FF & bvalue) : (short)bvalue;

		case MYSQL_TYPE_SHORT:		return value[i].getShort(0);            //	possible loss of sign !!

		case MYSQL_TYPE_LONG:		int ivalue = value[i].getInt(0);
									return bind[i].isUnsigned() ?
									    (short)(0x0000FFFF & ivalue) : (short)ivalue;   //	possible loss of precision

		case MYSQL_TYPE_LONGLONG:	long lvalue = value[i].getLong(0);
									return bind[i].isUnsigned() ?
		                                (short)(0x000000000000FFFF & lvalue) :
										(short)lvalue;            		    //	possible loss of precision

		case MYSQL_TYPE_FLOAT:		return (short)value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return (short)value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return Short.parseShort(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public int getInt(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		byte bvalue = value[i].get(0);
									return bind[i].isUnsigned() ?
		                                (int)(0x000000FF & bvalue) : (int)bvalue;

		case MYSQL_TYPE_SHORT:		short svalue = value[i].getShort(0);
									return bind[i].isUnsigned() ?
										(int)(0x0000FFFF & svalue) : (int)svalue;

		case MYSQL_TYPE_LONG:		return value[i].getInt(0);			    //	possible loss of sign !!

		case MYSQL_TYPE_LONGLONG:	long lvalue = value[i].getLong(0);
									return bind[i].isUnsigned() ?
		                                (int)(0x00000000FFFFFFFF & lvalue) :
										(int)lvalue;            		    //	possible loss of precision

		case MYSQL_TYPE_FLOAT:		return (int)value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return (int)value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return Integer.parseInt(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public long getLong(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		byte bvalue = value[i].get(0);
									return bind[i].isUnsigned() ?
		                                (long)(0x000000FF & bvalue) : (long)bvalue;

		case MYSQL_TYPE_SHORT:		short svalue = value[i].getShort(0);
									return bind[i].isUnsigned() ?
										(long)(0x0000FFFF & svalue) : (long)svalue;

		case MYSQL_TYPE_LONG:		int ivalue = value[i].getInt(0);
									return bind[i].isUnsigned() ?
									     (long)(0x00000000FFFFFFFF & ivalue) : (long)ivalue;

		case MYSQL_TYPE_LONGLONG:	return value[i].getLong(0);              //	possible loss of sign !!

		case MYSQL_TYPE_FLOAT:		return (long)value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return (long)value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return Long.parseLong(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public float getFloat(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		byte bvalue = value[i].get(0);
									return bind[i].isUnsigned() ?
		                                (float)(0x000000FF & bvalue) : (float)bvalue;

		case MYSQL_TYPE_SHORT:		short svalue = value[i].getShort(0);
									return bind[i].isUnsigned() ?
										(float)(0x0000FFFF & svalue) : (float)svalue;

		case MYSQL_TYPE_LONG:		int ivalue = value[i].getInt(0);
									return bind[i].isUnsigned() ?
									     (float)(0x00000000FFFFFFFF & ivalue) : (float)ivalue;

		case MYSQL_TYPE_LONGLONG:	return (float)value[i].getLong(0);      //  is this correct ?

		case MYSQL_TYPE_FLOAT:		return value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return (float)value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return Float.parseFloat(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public double getDouble(int i) throws SQLException
	{
		if (bind[i].isNull()) return 0;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:		byte bvalue = value[i].get(0);
									return bind[i].isUnsigned() ?
		                                (double)(0x000000FF & bvalue) : (double)bvalue;

		case MYSQL_TYPE_SHORT:		short svalue = value[i].getShort(0);
									return bind[i].isUnsigned() ?
										(double)(0x0000FFFF & svalue) : (double)svalue;

		case MYSQL_TYPE_LONG:		int ivalue = value[i].getInt(0);
									return bind[i].isUnsigned() ?
									     (double)(0x00000000FFFFFFFF & ivalue) : (double)ivalue;

		case MYSQL_TYPE_LONGLONG:	return (double)value[i].getLong(0);      //  is this correct ?

		case MYSQL_TYPE_FLOAT:		return (double)value[i].getFloat(0);		//	possible loss of precision
		case MYSQL_TYPE_DOUBLE:		return value[i].getDouble(0);		//	possible loss of precision

		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return Double.parseDouble(getString(i));

		default:                    throw new SQLException("invalid conversion");
		}
	}

	public MYSQL_TIME getDateParts(int i)
	{
		if (bind[i].isNull()) return null;

		MYSQL_TIME time = statement.connection.mysqlTime;
		time.attach(value[i],0,0);
		return time;
	}

	public String getString(int i) throws SQLException
	{
		if (bind[i].isNull()) return null;

		switch (bind[i].getBufferType())
		{
		case MYSQL_TYPE_TINY:       return String.valueOf(getByte(i));
		case MYSQL_TYPE_SHORT:      return String.valueOf(getShort(i));
		case MYSQL_TYPE_LONG:       return String.valueOf(getInt(i));
		case MYSQL_TYPE_LONGLONG:   return String.valueOf(getLong(i));
		case MYSQL_TYPE_FLOAT:      return String.valueOf(getFloat(i));
		case MYSQL_TYPE_DOUBLE:     return String.valueOf(getDouble(i));

		case MYSQL_TYPE_STRING:
		case MYSQL_TYPE_VAR_STRING:
		case MYSQL_TYPE_VARCHAR:
		case MYSQL_TYPE_TINY_BLOB:
		case MYSQL_TYPE_BLOB:
		case MYSQL_TYPE_MEDIUM_BLOB:
		case MYSQL_TYPE_LONG_BLOB:
		case MYSQL_TYPE_DECIMAL:
		case MYSQL_TYPE_NEWDECIMAL:
			return statement.connection.decodeString(value[i], bind[i].getLength());

		case MYSQL_TYPE_TIME:
			java.util.Date dt = MyPreparedResultSet.getTime(getDateParts(i), statement.connection.utcCalendar);
			return statement.connection.timeFormat.format(dt);

		case MYSQL_TYPE_DATE:
			dt = MyPreparedResultSet.getDate(getDateParts(i), statement.connection.utcCalendar);
			return statement.connection.dateFormat.format(dt);

		case MYSQL_TYPE_DATETIME:
		case MYSQL_TYPE_TIMESTAMP:
			dt = MyPreparedResultSet.getTimestamp(getDateParts(i), statement.connection.utcCalendar);
			return statement.connection.timestampFormat.format(dt);

		case MYSQL_TYPE_YEAR:
			dt = MyPreparedResultSet.getDate(getDateParts(i), statement.connection.utcCalendar);
			return statement.connection.yearFormat.format(dt);

		default:                   throw new SQLException("invalid conversion");
		}
	}

}
