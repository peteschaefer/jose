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

package de.jose.chess;

import de.jose.util.StringUtil;
import de.jose.util.ByteBuffer;
import de.jose.Util;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

abstract public class HashKey
		implements Constants, Serializable, Comparable, Cloneable
{

	//-------------------------------------------------------------------------------
	//	fields
	//-------------------------------------------------------------------------------

	/**	the actual hash value	 */
	protected long theValue;
	
	/**	compute reversed hash key ?	 */
	protected boolean isReversed;
	/** ignore position flags ? */
    protected boolean ignoreFlags;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	public HashKey(boolean reversed)
	{ 
		clear();
		isReversed = reversed;
	}

	public Object clone()
	{
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * factory method
	 * @return
	 */
	public static HashKey newHashKey(Class clazz, boolean reversed)
	{
		Class[] paramTypes = { boolean.class };
		Object[] paramValues = { Util.toBoolean(reversed) };

		try {

			Constructor ctor = clazz.getConstructor(paramTypes);
			return (HashKey)ctor.newInstance(paramValues);

		} catch (Exception e) {
			return null;
		}
	}


	//-------------------------------------------------------------------------------
	//	Access
	//-------------------------------------------------------------------------------
	
	public final long value()					    { return theValue; }
	
	public final void setValue(long value)	        { theValue = value; }

    public void  setIgnoreFlags(boolean ignore)     { ignoreFlags = ignore; }

	public boolean isReversed()                 	{ return isReversed; }

	abstract public void set(int square, int piece);

	abstract public void set(int flags);

	abstract public void clear(int square, int piece);

	abstract public void clear(int flags);

	abstract public void clear();

	abstract public long getInitialValue(boolean ignoreFlags, boolean reversed);

	//-------------------------------------------------------------------------------
	//	interface Comparable
	//-------------------------------------------------------------------------------
	
	public int compareTo(Object object)
	{
		HashKey that = (HashKey)object;
		if (this.theValue < that.theValue)
			return -1;
		else if (this.theValue == that.theValue)
			return 0;
		else
			return +1;
	}

	//-------------------------------------------------------------------------------
	//	interface Serializable
	//-------------------------------------------------------------------------------

	public int hashCode() {
		return (int)theValue;
	}

	public boolean equals(Object object) {
		HashKey that = (HashKey)object;
		return this.theValue == that.theValue;
	}

    public boolean equals(long value) {
        return this.theValue == value;
    }

	public String toString() {
		return Long.toHexString(theValue);
	}

	//-------------------------------------------------------------------------------
	//	convert to iso encoded string
	//-------------------------------------------------------------------------------

	public static void encode(long val, byte[] bytes, int start, int len)
	{
	    if (val < 0) val = -val;
	    while (len-- > 0) {
	        bytes[start++] = StringUtil.BASE_ISO[(int)(val % StringUtil.BASE_ISO_MOD)];
	        val /= StringUtil.BASE_ISO_MOD;
	    }
	}

	public static void encode(long val, char[] chars, int start, int len)
	{
	    if (val < 0) val = -val;
	    while (len-- > 0) {
	       chars[start++] = (char)StringUtil.BASE_ISO[(int)(val % StringUtil.BASE_ISO_MOD)];
	        val /= StringUtil.BASE_ISO_MOD;
	    }
	}

	public static void encode(long val, ByteBuffer buf, int len)
	{
	    if (val < 0) val = -val;
	    while (len-- > 0) {
	        buf.append(StringUtil.BASE_ISO[(int)(val % StringUtil.BASE_ISO_MOD)]);
	        val /= StringUtil.BASE_ISO_MOD;
	    }
	}

	public static void encode(long val, ByteArrayOutputStream out, int len)
	{
	    if (val < 0) val = -val;
	    while (len-- > 0) {
	        out.write(StringUtil.BASE_ISO[(int)(val % StringUtil.BASE_ISO_MOD)]);
	        val /= StringUtil.BASE_ISO_MOD;
	    }
	}
}
