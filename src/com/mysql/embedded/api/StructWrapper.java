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
import java.nio.ByteOrder;

/**
 * @author Peter Sch�fer
 */

public class StructWrapper
{
	public static boolean _32bit = false;
	public static boolean _64bit = true;

	/** the underlying byte buffer  */
	protected ByteBuffer b;
	/** its native address  */
	protected long nativeAddress;
	/** offset within byte buffer   */
	protected int offset;


    public void attach(ByteBuffer b, long nativeAddress, int offset)
    {
		attach(b,nativeAddress,offset,ByteOrder.nativeOrder());
    }

	public void attach(ByteBuffer b, long nativeAddress, int offset, ByteOrder byteOrder)
	{
		this.b = b;
		this.nativeAddress = nativeAddress;
		this.offset = offset;
		this.b.order(byteOrder);
	}

	//  -----------------------------------------------
	//      accessors
	//  -----------------------------------------------

	public boolean getBoolean(int offset)               { return getByte(offset) != 0; }
	public byte getByte(int offset)                     { return b.get(this.offset+offset); }
	public short getUnsignedByte(int offset)            { return (short)(0x00FF & b.get(this.offset+offset)); }

	public short getShort(int offset)                   { return b.getShort(this.offset+offset); }
	public int getUnsignedShort(int offset)             { return (0x0000FFFF & b.getShort(this.offset+offset)); }

	public int getInt(int offset)                       { return b.getInt(this.offset+offset); }
	public long getUnsignedInt(int offset)              { return (long)(0x00000000FFFFFFFF & b.getInt(this.offset+offset)); }

	public long getLong(int offset)                     { return b.getLong(this.offset+offset); }
	public float getFloat(int offset)                   { return b.getFloat(this.offset+offset); }
	public double getDouble(int offset)                 { return b.getDouble(this.offset+offset); }

	public long getPointer(int offset)                  { return _64bit ? getLong(offset) : getInt(offset); }

	public void setBoolean(int offset, boolean value)   { setByte(offset, value ? (byte)1 : (byte)0); }
	public void setByte(int offset, byte value)         { b.put(this.offset+offset,value); }
	public void setShort(int offset, short value)       { b.putShort(this.offset+offset,value); }
	public void setInt(int offset, int value)           { b.putInt(this.offset+offset,value); }
	public void setLong(int offset, long value)         { b.putLong(this.offset+offset,value); }
	public void setFloat(int offset, float value)       { b.putFloat(this.offset+offset,value); }
	public void setDouble(int offset, double value)     { b.putDouble(this.offset+offset,value); }

	public void setPointer(int offset, long value)      { if (_64bit) setLong(offset,value); else setInt(offset,(int)value); }

	//  -----------------------------------------------
	//      static accessors
	//  -----------------------------------------------

	public static boolean getBoolean(ByteBuffer b, int offset)               { return b.get(offset) != 0; }
	public static short getUnsignedByte(ByteBuffer b, int offset)            { return (short)(0x00FF & b.get(offset)); }
	public static int getUnsignedShort(ByteBuffer b, int offset)             { return (0x0000FFFF & b.getShort(offset)); }
	public static long getUnsignedInt(ByteBuffer b, int offset)              { return (long)(0x00000000FFFFFFFF & b.getInt(offset)); }

	public static void setBoolean(ByteBuffer b, int offset, boolean value)   { b.put(offset, value ? (byte)1 : (byte)0); }
}
