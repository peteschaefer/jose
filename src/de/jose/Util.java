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

package de.jose;

import de.jose.util.StringUtil;
import de.jose.util.ListUtil;
import de.jose.util.file.FileUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.text.SimpleDateFormat;

public class Util
{
	//-------------------------------------------------------------------------------
	//	bit flag operations
	//-------------------------------------------------------------------------------

	public static final int plus(int flags, int set)		{ return flags | set; }
	public static final int minus(int flags, int set)		{ return flags & ~set; }
	
	public static final int set(int flags, int set, boolean on) {
		return on ? (flags | set) : (flags & ~set);
	}

	public static final int setMask(int flags, int mask, int set) {
		return (flags & ~mask) | (set & mask);
	}

	public static final boolean anyOf(int flags, int set)	{ return (flags & set) != 0; }
	public static final boolean allOf(int flags, int set)	{ return (flags & set) == set; }
	public static final boolean noneOf(int flags, int set)	{ return (flags & set) == 0; }

	public static final boolean equals(int flags, int mask, int value) { return (flags & mask) == value; }
	
	//-------------------------------------------------------------------------------
	//	Collection utilities
	//-------------------------------------------------------------------------------
	
	public static void add(Collection coll, Object[] array)
	{
		for (int i=0; i<array.length; i++)
			coll.add(array[i]);
	}
	
	public static void add(Map map, Object[] array)
	{
		for (int i=0; i<array.length; i +=2 )
			map.put(array[i], array[i+1]);
	}

	public static void swap(Object a, Object b)
	{
		Object aux=a;
		a=b;
		b=aux;
	}

	public static void swap(Object[] array, int a, int b)
	{
		Object aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swap(int[] array, int a, int b)
	{
		int aux = array[a];
		array[a] = array[b];
		array[b] = aux;
	}

	public static void swap(List list, int a, int b)
	{
		Object aux = list.get(a);
		list.set(a, list.get(b));
		list.set(b, aux);
	}

	/**
	 * count the number of occurences in a list
	 */
	public static int count (List l, int from, int to, Object what)
	{
		int result = 0;
		while (from < to) {
			Object which = l.get(from++);
			if (equals(which,what)) result++;
		}
		return result;
	}

	//-------------------------------------------------------------------------------
	//	Handle Classes
	//-------------------------------------------------------------------------------
	
	public static class IntHandle	
	{ 
		public int i; 
		
		public String toString()	{ return Integer.toString(i); }
	}
	
	public static class StringHandle	{ 
		public String s; 
	
		public String toString()	{ return s; }
	}

	public static class ObjectHandle	{ public Object o; }

	//-------------------------------------------------------------------------------
	//	Math
	//-------------------------------------------------------------------------------

	public static final int square(int x)		{ return x*x; }
	public static final double square(double x)	{ return x*x; }
	public static final long square(long x)		{ return x*x; }

	public static final int abs(int x)			{ return (x>0) ? x:-x; }
	public static final int max(int x, int y)	{ return (x>y) ? x:y; }
	public static final int min(int x, int y)	{ return (x<y) ? x:y; }

	public static final int max(int x, int y, int z)	{ return (x>y) ? max(x,z):max(y,z); }
	public static final int min(int x, int y, int z)	{ return (x<y) ? min(x,z):min(y,z); }

	public static final int round(int x, int mod) {
		//	to next multiple of ...
		int rest = x % mod;
		return x-rest;
	}

	public static final int roundUp(int x, int mod) {
		//	to next multiple of ...
		int rest = x % mod;
		if (rest==0)
			return x;
		else
			return x-rest+mod;
	}

 	//-------------------------------------------------------------------------------
	//	This & That
	//-------------------------------------------------------------------------------
	
	/**	a Calendar for te UTC (GMT) time zone	 */
	public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
	public static final Calendar UTC_CALENDAR = Calendar.getInstance(UTC_TIMEZONE);

    /** compare two object for equality with equals(). null values accepted */
    public static final boolean equals(Object a, Object b)
    {
	    if (a==b)               return true; 
        if (a==null || b==null) return false;
        return a.equals(b);
    }

	/**	@return the object that is not null	*/
	public static final Object nvl(Object a, Object b)
	{
		if (a!=null)	return a;
		return b;
	}

	/**	@return the object that is not null	*/
	public static final Object nvl(Object a, Object b, Object c)
	{
		if (a!=null)	return a;
		if (b!=null)	return b;
		return c;
	}

	/**	@return the number that is not zero	*/
	public static final int nvl(int a, int b)
	{
		return (a!=0) ? a : b;
	}

	public static final int inBounds(int lower, int x, int upper)
	{
		if (x < lower) return lower;
		if (x > upper) return upper;
		return x;
	}

	public static final double inBounds(double lower, double x, double upper)
	{
		if (x < lower) return lower;
		if (x > upper) return upper;
		return x;
	}

	//-------------------------------------------------------------------------------
	//	Classpath Tweaking
	//-------------------------------------------------------------------------------


    public static HashSet toHashSet(Object[] array)
    {
        HashSet result = new HashSet();
        for (int i=array.length-1; i>=0; i--)
            result.add(array[i]);
        return result;
    }

	public static Boolean toBoolean(Object object)
	{
		if (object==null)
			return Boolean.FALSE;
		if (object instanceof Boolean)
			return ((Boolean)object);

		boolean bool;
		if (object instanceof Number)
			bool = ((Number)object).doubleValue() != 0.0;
		else {
			String str = object.toString();
			bool = str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1") ||
			        str.equalsIgnoreCase("on") || str.equalsIgnoreCase("enabled");
		}
		return toBoolean(bool);
	}

	public static Boolean toBoolean(boolean bool)
	{
		return bool ? Boolean.TRUE:Boolean.FALSE;
	}

	public static boolean toboolean(Object object)
	{
		return toBoolean(object).booleanValue();
	}

	public static Number toNumber(Object obj) throws NumberFormatException
	{
		if (obj==null)
			return 0;
		if (obj instanceof Number)
			return (Number)obj;

		String text = obj.toString();
		if (text.length()==0)
			return 0;
		else if (StringUtil.isInteger(text))
			return Integer.valueOf(text);
		else
			return Double.valueOf(text);
	}

	public static int toint(Object obj) throws NumberFormatException
	{
		if (obj==null)
			return 0;
		if (obj instanceof Number)
			return ((Number)obj).intValue();

        String text = null;
        if (obj instanceof Reader)
            try {
                StringWriter out = new StringWriter();
                FileUtil.copyReader((Reader)obj,out);
                text = out.toString();
            } catch (IOException ioex) {
                throw new NumberFormatException(ioex.getMessage());
            }
        else if (obj instanceof InputStream)
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                FileUtil.copyStream((InputStream)obj,out);
                text = new String(out.toByteArray());
            } catch (IOException ioex) {
                throw new NumberFormatException(ioex.getMessage());
            }
        else
		    text = obj.toString();

		return Integer.parseInt(text);
	}

	public static long tolong(Object obj) throws NumberFormatException
	{
		if (obj==null)
			return 0L;
		if (obj instanceof Number)
			return ((Number)obj).longValue();

        String text = null;
        if (obj instanceof Reader)
            try {
                StringWriter out = new StringWriter();
                FileUtil.copyReader((Reader)obj,out);
                text = out.toString();
            } catch (IOException ioex) {
                throw new NumberFormatException(ioex.getMessage());
            }
        else if (obj instanceof InputStream)
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                FileUtil.copyStream((InputStream)obj,out);
                text = new String(out.toByteArray());
            } catch (IOException ioex) {
                throw new NumberFormatException(ioex.getMessage());
            }
        else
		    text = obj.toString();

		return Long.parseLong(text);
	}

	public static double todouble(Object obj) throws NumberFormatException
	{
		if (obj==null)
			return 0.0;
		if (obj instanceof Number)
			return ((Number)obj).doubleValue();
		//  else:
		String text = obj.toString();
		return Double.parseDouble(text);
	}

	public static void grow(Rectangle2D r, Insets insets)
	{
		r.setRect(
		r.getX()-insets.left,
		r.getY()-insets.top,
		r.getWidth()+insets.left+insets.right,
		r.getHeight()+insets.top+insets.bottom );
	}

	public static Color toColor(Object obj) throws NumberFormatException
	{
		if (obj==null)
			return null;
		if (obj instanceof Color)
			return (Color)obj;
		if (obj instanceof Number)
			return new Color(((Number)obj).intValue());
		if (obj instanceof float[]) {
			float[] ar = (float[])obj;
			if (ar.length >= 4)
				return new Color(ar[0],ar[1],ar[2],ar[3]);
			else if (ar.length >= 3)
				return new Color(ar[0],ar[1],ar[2]);
			else
				throw new IllegalArgumentException("at least three values expected");
		}
		if (obj instanceof int[]) {
			int[] ar = (int[])obj;
			if (ar.length >= 4)
				return new Color(ar[0],ar[1],ar[2],ar[3]);
			else if (ar.length >= 3)
				return new Color(ar[0],ar[1],ar[2]);
			else
				throw new IllegalArgumentException("at least three values expected");
		}
		//  else:
		String text = obj.toString();
		if (text.startsWith("#")) text = text.substring(1);
		int rgb = Integer.parseInt(text,16);
		return new Color(rgb);
	}


	public static boolean isValidURL(String path)
	{
		if (path==null) return false;

		try {
			URL url = new URL(path);
			return true;
		} catch (MalformedURLException e) {
			return false;    //  don't bail out, just return false
		}
	}

    public static String toString(Object obj)
    {
	    if (obj==null)
		    return null;
	    else if (ListUtil.isIteratable(obj))
	    {
		    StringBuffer buf = new StringBuffer();
		    buf.append("[");

		    boolean any = false;
		    Iterator i = ListUtil.iterator(obj);
		    while (i.hasNext()) {
			    if (any) buf.append(",");
		        buf.append(toString(i.next()));
		        any = true;
		    }

		    buf.append("]");
		    return buf.toString();
	    }
	    else
		    return obj.toString();
    }


	public static void printTime(String label, long startTime)
	{
		Date runTime = new Date(System.currentTimeMillis()-startTime);
		SimpleDateFormat timeformat = new SimpleDateFormat("mm:ss.SSS");
		timeformat.setTimeZone(Util.UTC_TIMEZONE);

		System.out.println(label+": "+timeformat.format(runTime));
	}
}


