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


package de.jose.util;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Native utilities to read and write the Windows Registry
 *
 * uses the winUtils.dll (together with WinUtils)
 *
 * @author Peter Schäfer
 */

public class WinRegistry
{

	/**
	 * check if a key already exists
	 * @param key
	 * @return true if this key exists in the Windows registry
	 */
    public static boolean existsKey(String key)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        return exists_key(key);
    }

	/**
	 * create a key
	 * @param key
	 * @return true if the key was created, false if it already existed
	 */
    public static boolean createKey(String key)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        return create_key(key);
    }

	/**
	 * delete a key
	 * @param key
	 * @return true if the key was deleted, false if it didn't exist
	 */
    public static boolean deleteKey(String key)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        return delete_key(key);
    }


	/**
	 * get a value from a key
	 *
	 * @param key a registry key
	 * @param value a value name, or null to retrieve the key's default value
	 * @return a value (currently, we only return Strings)
	 */
    public static Object getValue(String key, String value)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        return get_value(key,value);
    }

    public static int getIntValue(String key, String value)
    {
        Object val = WinRegistry.getValue(key,value);
        if (val!=null && val instanceof Number) {
            int i = ((Number) val).intValue();
            return i;
        }
        return Integer.MIN_VALUE;
    }

	/**
	 * set a value
	 *
	 * @param key a registry key
	 * @param value a value name, or null to modify the key's default value
	 * @param data data to assign. pass null to delete the value altogether
	 */
    public static void setValue(String key, String value, String data)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        if (data==null)
            delete_value(key,value);
        else
            set_string_value(key,value,data);
    }

	/**
	 * delete a value
	 * @param key
	 * @param value
	 * @return
	 */
    public static boolean deleteValue(String key, String value)
    {
        WinUtils.loadLib();
        key = key.replace('/','\\');
        return delete_value(key,value);
    }

	public static int countSubKeys(String key)
	{
		WinUtils.loadLib();
		key = key.replace('/','\\');
		return count_subkeys(key);
	}

	public static int countValues(String key)
	{
		WinUtils.loadLib();
		key = key.replace('/','\\');
		return count_values(key);
	}

	public static String getSubKey(String key, int index)
	{
		WinUtils.loadLib();
		key = key.replace('/','\\');
		return get_subkey(key,index);
	}

	public static String getValueName(String key, int index)
	{
		WinUtils.loadLib();
		key = key.replace('/','\\');
		return get_value_name(key,index);
	}

	public static Object getValue(String key, int index)
	{
		WinUtils.loadLib();
		key = key.replace('/','\\');
		return get_value(key,index);
	}

	public static String[] listSubKeys(String key)
	{
		String[] result = new String[countSubKeys(key)];
		for (int i=0; i < result.length; i++)
			result[i] = getSubKey(key,i);
		return result;
	}

	public static Map getAllValues(String key)
	{
		Map result = new HashMap();
		//  the default value for this key
		result.put(null, getValue(key,null));
		for (int i=countValues(key)-1; i >= 0; i--)
		{
			String valueName = getValueName(key,i);
			result.put(valueName, getValue(key,valueName));
		}

		return result;
	}



    /** JNI wrapper functions   */
    private static native boolean exists_key(String key);

    private static native boolean create_key(String key);

    private static native boolean delete_key(String key);

    private static native Object get_value(String key, String value);

	private static native Object get_value(String key, int index);

    private static native void set_string_value(String key, String value, String data);

    private static native boolean delete_value(String key, String value);

	private static native int count_subkeys(String key);

	private static native int count_values(String key);

	private static native String get_value_name(String key, int index);

	private static native String get_subkey(String key, int index);


    public static void main(String[] args) throws IOException
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        for (;;)
        {
            String line = r.readLine();
            if (line==null) break;

            String cmd = "";
            String key = null;
            String value = null;
            String data = null;

            StringTokenizer tok = new StringTokenizer(line," ");
            if (tok.hasMoreTokens()) cmd = tok.nextToken();
            if (tok.hasMoreTokens()) key = tok.nextToken();
            if (tok.hasMoreTokens()) {
                value = tok.nextToken();
                if (value.equalsIgnoreCase("null")) value = null;
            }
            if (tok.hasMoreTokens()) {
                data = tok.nextToken();
                if (data.equalsIgnoreCase("null")) data = null;
            }

            if (cmd.equalsIgnoreCase("x"))
            {
                System.out.println(key+" exists: "+existsKey(key));
            }
            if (cmd.equalsIgnoreCase("c"))
            {
                System.out.println(key+" created: "+createKey(key));
            }
            if (cmd.equalsIgnoreCase("d"))
            {
                if (value!=null)
                    System.out.println(key+" "+value+" deleted: "+deleteValue(key,value));
                else
                    System.out.println(key+" deleted: "+deleteKey(key));
            }
            if (cmd.equalsIgnoreCase("l"))
            {
                System.out.println(key+" "+value+" = "+getValue(key,value));
            }
            if (cmd.equalsIgnoreCase("s"))
            {
                setValue(key,value,data);
                System.out.println(key+" "+value+" = "+data+" : "+getValue(key,value));
            }
	        if (cmd.equalsIgnoreCase("e"))
	        {
		        String[] subKeys = WinRegistry.listSubKeys(key);
		        Map values = WinRegistry.getAllValues(key);

		        for (int i=0; i < subKeys.length; i++)
			        System.out.println(key+" \\ "+subKeys[i]);
		        System.out.println();

		        Iterator i = values.entrySet().iterator();
		        while (i.hasNext())
		        {
			        Map.Entry ety = (Map.Entry)i.next();
			        System.out.println(ety.getKey()+" = "+ety.getValue());
		        }
	        }
            if (cmd.equalsIgnoreCase("+jose"))
            {
                WinUtils.registerApplication("jose","G:\\jose\\work\\jose.exe");
                WinUtils.associateFileExtension("pgn","jose");
            }
            if (cmd.equalsIgnoreCase("-jose"))
            {
                WinUtils.removeFileExtension("pgn","jose",null);
            }
        }
    }
}