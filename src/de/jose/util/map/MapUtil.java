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

package de.jose.util.map;

import java.util.Map;

/**
 * utilities for storing and reading values from Maps
 * (such as HashMaps)
 */
public class MapUtil
{

	public static void put(Map map, Object key, int value)
	{
		map.put(key, new Integer(value));
	}

	public static int get(Map map, Object key, int nullValue)
	{
	    return toInt(map.get(key), nullValue);
	}

    public static int toInt(Object value, int nullValue)
    {
        if (value==null)
            return nullValue;
        if (value instanceof Number)
            return ((Number)value).intValue();
        else
            return Integer.parseInt(value.toString());
    }


	public static void put(Map map, Object key, double value)
	{
		map.put(key, new Double(value));
	}

	public static double get(Map map, Object key, double nullValue)
	{
        return toDouble(map.get(key),nullValue);
    }

    public static double toDouble(Object value, double nullValue)
    {
	    if (value==null)
            return nullValue;
        if (value instanceof Number)
            return ((Number)value).doubleValue();
        else
            return Double.parseDouble(value.toString());
	}



	public static void put(Map map, Object key, float value)
	{
		map.put(key, new Float(value));
	}

	public static float get(Map map, Object key, float defaultValue)
	{
		Number n = (Number)map.get(key);
		if (n==null)
			return defaultValue;
		else
			return n.floatValue();
	}
}
