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

package de.jose.web;

import de.jose.util.StringUtil;
import de.jose.Util;

import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;
import java.util.*;

/**
 * SessionUtil
 *
 * @author Peter Schäfer
 */
public class SessionUtil
{
	protected HttpSession session;
	protected ServletRequest request;

	public SessionUtil(ServletRequest request, HttpSession session)
	{
		this.session = session;
		this.request = request;
	}

	public Object get(String key, boolean persistent)
	{
		Object value = request.getParameter(key);
		if (value==null && persistent)
			value = session.getAttribute(key);
		return value;
	}

	public void set(String key, Object new_value)
	{
		if (new_value==null)
			session.removeAttribute(key);
		else
			session.setAttribute(key,new_value);
	}

	public boolean wasModified(String key)
	{
		Object session_value = session.getAttribute(key);
		String request_value = request.getParameter(key);
		return request_value!=null && !request_value.equals(session_value);
	}

	public boolean wasSubmitted(String key)
	{
		return (request.getParameter(key)!=null) || (request.getParameter(key+".x")!=null);
	}


	public String getString(String key, boolean persistent)
	{
		return toString(get(key,persistent));
	}

	public String getString(String key, String defaultValue, boolean persistent)
	{
		String result = getString(key,persistent);
		if (result==null) result = defaultValue;
		return result;
	}

	public int getInt(String key, int defaultValue, boolean persistent)
	{
		return toInt(get(key,persistent),defaultValue);
	}

	public boolean getBoolean(String key, boolean defaultValue, boolean persistent)
	{
		return toBoolean(get(key,persistent),defaultValue);
	}

	public void set(String key, int value)
	{
		set(key, new Integer(value));
	}

	public void set(String key, boolean value)
	{
		set(key, value?Boolean.TRUE:Boolean.FALSE);
	}


	private static String toString(Object value)
	{
		if (value==null)
			return null;
		else
			return String.valueOf(value);
	}

	private static int toInt(Object value, int defaultValue)
	{
		if (value==null)
			return defaultValue;
		if (value instanceof Number)
			return ((Number)value).intValue();

		String strValue = toString(value);
		if (StringUtil.isInteger(strValue))
			return Integer.parseInt(strValue);
		else
			return defaultValue;
	}

	private static boolean toBoolean(Object value, boolean defaultValue)
	{
		if (value==null)
			return defaultValue;
		if (value instanceof Number)
			return ((Number)value).intValue() != 0;

		String strValue = toString(value);
		if (StringUtil.isInteger(strValue))
			return Integer.parseInt(strValue) != 0;

		if (strValue.equalsIgnoreCase("true")) return true;
		if (strValue.equalsIgnoreCase("false")) return false;

		return defaultValue;
	}
}
