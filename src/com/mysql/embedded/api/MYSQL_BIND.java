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

package com.mysql.embedded.api;

import java.nio.ByteBuffer;

/**
 * Java wrapper for a MYSQL_BIND structure
 *
 * @author Peter Sch�fer
 */

/*

	typedef struct st_mysql_bind
	{
	  unsigned long	*length;          /* output length pointer * /
	my_bool       *is_null;	  /* Pointer to null indicator * /
	void		*buffer;	  /* buffer to get/put data * /
	enum enum_field_types buffer_type;	/* buffer type * /
	unsigned long buffer_length;    /* buffer length, must be set for str/binary * /

	/* Following are for internal use. Set by mysql_stmt_bind_param * /
	unsigned char *inter_buffer;    /* for the current data position * /
	unsigned long offset;           /* offset position for char/binary fetch * /
	unsigned long	internal_length;  /* Used if length is 0 * /
	unsigned int	param_number;	  /* For null count and error messages * /
	unsigned int  pack_length;	  /* Internal length for packed data * /
	my_bool       is_unsigned;      /* set if integer type is unsigned * /
	my_bool	long_data_used;	  /* If used with mysql_send_long_data * /
	my_bool	internal_is_null; /* Used if is_null is 0 * /
	void (*store_param_func)(NET *net, struct st_mysql_bind *param);
	void (*fetch_result)(struct st_mysql_bind *, unsigned char **row);
	void (*skip_result)(struct st_mysql_bind *, MYSQL_FIELD *,
				unsigned char **row);
	} MYSQL_BIND;

*/

public class MYSQL_BIND
        extends StructWrapper
{
	/** offset to is_null   */
	protected int isnull_offset;
	/** offset to length    */
	protected int length_offset;

	//  ----------------------------
	//      ctor
	//  ----------------------------

	public MYSQL_BIND(ByteBuffer b, long nativeAddress, int offset)
	{
		attach(b,nativeAddress,offset);
	}

	//  ----------------------------
	//      accessors
	//  ----------------------------

	//  char MYSQL_BIND.buffer_type
	public int getBufferType()                  { return getInt(OFFSET_BUFFER_TYPE); }
	public void setBufferType(int buffer_type)  { setInt(OFFSET_BUFFER_TYPE, buffer_type); }

	//  char* MYSQL_BIND.buffer
	public long getBufferPtr()                  { return getPointer(OFFSET_BUFFER_PTR); }
	public void setBufferPtr(long address)      { setPointer(OFFSET_BUFFER_PTR, address); }

	//  long MYSQL_BIND.buffer_length
	public int getBufferLength()                { return getInt(OFFSET_BUFFER_LENGTH); }
	public void setBufferLength(int length)     { setInt(OFFSET_BUFFER_LENGTH,length); }

	//  long* MYSQL_BIND.length
	public long getLengthPtr()                  { return getPointer(OFFSET_LENGTH_PTR); }
	public void setLengthPtr(long address)      { setPointer(OFFSET_LENGTH_PTR, address); }

	public void setLengthOffset(int offset)
	{
		setLengthPtr(nativeAddress + offset);
		length_offset = offset-this.offset;
	}

	public int getLength()                      { return (int)getLong(length_offset); }
	public void setLength(int length)           { setLong(length_offset, length); }

	//  char* MYSQL_BIND.is_null
	public long getIsNullPtr()                  { return getPointer(OFFSET_ISNULL_PTR); }
	public void setIsNullPtr(long address)      { setPointer(OFFSET_ISNULL_PTR, address); }

	public void setIsNullOffset(int offset)
	{
		setIsNullPtr(nativeAddress + offset);
		isnull_offset = offset-this.offset;
	}

	public boolean isNull()                     { return getBoolean(isnull_offset); }
	public void setNull(boolean isnull)         { setBoolean(isnull_offset, isnull); }

	//  char MYSQL_BIND.is_unsigned
	public boolean isUnsigned()                 { return getBoolean(OFFSET_ISUNSIGNED); }
	public void setUnsigned(boolean unsigned)   { setBoolean(OFFSET_ISUNSIGNED, unsigned); }


	//  ----------------------------
	//      memory layout
	//  ----------------------------

	public static int SIZE;
	protected static int OFFSET_BUFFER_TYPE;
	protected static int OFFSET_BUFFER_PTR;
	protected static int OFFSET_BUFFER_LENGTH;
	protected static int OFFSET_LENGTH_PTR;
	protected static int OFFSET_ISNULL_PTR;
	protected static int OFFSET_ISUNSIGNED;
	public static int SIZE_LEN = 8;	//	sizeof(unsigned long) ??
	public static int SIZE_BOOL = 1;

	protected static native int sizeof_MYQSL_BIND();
	protected static native int offset_buffer_type();
	protected static native int offset_buffer_ptr();
	protected static native int offset_buffer_length();
	protected static native int offset_length_ptr();
	protected static native int offset_isnull_ptr();
	protected static native int offset_isunsigned();

	static
	{
		SIZE = sizeof_MYQSL_BIND();
		OFFSET_BUFFER_TYPE = offset_buffer_type();
		OFFSET_BUFFER_PTR = offset_buffer_ptr();
		OFFSET_BUFFER_LENGTH = offset_buffer_length();
		OFFSET_LENGTH_PTR = offset_length_ptr();
		OFFSET_ISNULL_PTR = offset_isnull_ptr();
		OFFSET_ISUNSIGNED = offset_isunsigned();
	}

}
