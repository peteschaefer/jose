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

package com.mysql.embedded.api;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * JNI wrappers for reading result rows
 *
 * result rows are char[][]
 * with a char[] for each column.
 *
 * rowHandles are actually char** returned by mysql_fetch_row
 *
 * @author Peter Schäfer
 */

public class res
{
	// ----------------------------------------------------------
	//  Memory Allocation
	// ----------------------------------------------------------

	/** allocate a byte buffer in native memory */
	public static native ByteBuffer allocate_ByteBuffer(int size);
	/** release a buffer that was allocated in native memory */
	public static native void free_ByteBuffer(ByteBuffer buffer);

	/** get the native address of a direct byte buffer  */
	public static native long get_address(ByteBuffer buffer);

	/** copy a native char* to a String */
	public static native String to_string(long charptr, int len);
	/** copy a (0-ended) native char* to a String */
	public static native String to_string(long charptr);

	// ----------------------------------------------------------
	//  Statement Result Set (MYSQL_RES*)
	// ----------------------------------------------------------

	public static native int get_int(long rowHandle, int index);

	public static native long get_long(long rowHandle, int index);

	public static native float get_float(long rowHandle, int index);

	public static native double get_double(long rowHandle, int index);

	public static native String get_string(long rowHandle, int index);

	public static native ShortBuffer get_date(long rowHandle, int index, ShortBuffer parts);

	public static native ByteBuffer get_bytes(long rowHandle, int index, int len);

	public static native boolean is_null(long rowHandle, int index);

}
