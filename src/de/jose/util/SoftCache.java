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

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.WeakHashMap;
import java.util.HashMap;
import java.util.Stack;

/**
 * keeps a cache of soft references, that can be GC'ed without notice
 * to forcible keep a reference, use lock
 *
 * @author Peter Schäfer
 */

public class SoftCache
        extends HashMap
{
	public static SoftCache gInstance = new SoftCache();

	private HashSet locks = new HashSet();




	public Object put(Object key, Object value)
	{
		return put(key,value,false);
	}

	public Object put(Object key, Object value, boolean locked)
	{
		SoftReference result = (SoftReference)super.put(key, new SoftReference(value));
		if (locked) locks.add(value);
		if (result!=null)
			return result.get();
		else
			return null;
	}

	public Object get(Object key)
	{
		SoftReference result = (SoftReference)super.get(key);
		if (result!=null)
			return result.get();
		else
			return null;
	}

	public Object get(Object key, boolean locked)
	{
		SoftReference result = (SoftReference)super.get(key);
		if (result!=null) {
			Object value = result.get();
			if (locked)
				lock(key);
			else
				unlockValue(value);
			return value;
		}
		else
			return null;
	}

	public boolean lock(Object key)
	{
		Object value = get(key);
		if (value!=null) {
			locks.add(value);
			return true;
		}
		else
			return false;
	}

	public boolean unlockKey(Object key)
	{
		Object value = get(key);
		if (value!=null)
			return locks.remove(value);
		else
			return false;
	}

	public boolean unlockValue(Object value)
	{
		return locks.remove(value);
	}

	public void push(Object key, Object value, boolean locked)
	{
		Stack stack = (Stack)get(key);
		if (stack==null) {
			stack = new Stack();
			put(key,stack,true);
		}
		if (locked) locks.add(value);
		stack.push(new SoftReference(value));
	}

	public Object pop(Object key)
	{
		Stack stack = (Stack)get(key);
		if (stack==null) return null;
		while (!stack.isEmpty()) {
			SoftReference ref = (SoftReference)stack.pop();
			Object value = ref.get();
			if (value!=null) return value;
		}
		//  else
		remove(key);
		return null;
	}

	public void unlockAll()
	{
		locks.clear();
	}

}
