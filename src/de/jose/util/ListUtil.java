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

import de.jose.Application;
import de.jose.task.DBSelectionModel;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Array;
import java.util.*;

import org.w3c.dom.NodeList;

public class ListUtil
{

    /**
     * @return an Iterator for any type of iteratable object
     */
    public static Iterator iterator(Object obj)
    {
	    if (obj instanceof DBSelectionModel)
			return new DBSelectionIterator((DBSelectionModel)obj);

        if (obj instanceof Iterator)
            return ((Iterator)obj);

        if (obj.getClass().isArray())
            return new ArrayIterator(obj);

        if (obj instanceof Collection)
            return ((Collection)obj).iterator();

        if (obj instanceof Enumeration)
            return new EnumerationIterator((Enumeration)obj);

        if (obj instanceof Map)
            return ((Map)obj).entrySet().iterator();

        if (obj instanceof IntArray)
            return ((IntArray)obj).objIterator();

	    if (obj instanceof NodeList)
	        return new NodeListIterator((NodeList)obj);


        throw new IllegalArgumentException(obj.getClass()+" can not be iterated");
    }

    /**
     * @return an Iterator for any type of iteratable object
     */
    public static boolean isIteratable(Object obj)
    {
	    if (obj instanceof DBSelectionModel)
			return true;

        if (obj instanceof Iterator)
            return true;

        if (obj.getClass().isArray())
            return true;

        if (obj instanceof Collection)
            return true;

        if (obj instanceof Enumeration)
            return true;

        if (obj instanceof Map)
            return true;

        if (obj instanceof IntArray)
            return true;

	    if (obj instanceof NodeList)
	        return true;

        return false;
    }

    public static int size(Object obj)
    {
        if (obj.getClass().isArray())
            return Array.getLength(obj);

        if (obj instanceof Collection)
            return ((Collection)obj).size();

        if (obj instanceof Map)
            return ((Map)obj).size();

        if (obj instanceof IntArray)
            return ((IntArray)obj).size();

	    if (obj instanceof NodeList)
	        return ((NodeList)obj).getLength();

        throw new IllegalArgumentException("can't get size of "+obj.getClass());
    }

    public static boolean hasSize(Object obj)
    {
        if (obj.getClass().isArray())
            return true;

        if (obj instanceof Collection)
            return true;

        if (obj instanceof Map)
            return true;

        if (obj instanceof IntArray)
            return true;

	    if (obj instanceof NodeList)
	        return true;

        return false;
    }

	public static Vector toVector(Object obj)
	{
		return toVector(obj,null);
	}

	public static Vector toVector(Object obj, Vector result)
	{
		if (obj==null)
			return null;
		if (result==null)
			result = new Vector();
		if (obj instanceof Collection)
			result.addAll((Collection)obj);
		else if (isIteratable(obj)) {
			Iterator i = iterator(obj);
			while (i.hasNext())
				result.add(i.next());
		}
		else
			result.add(obj);
		return result;
	}

	public static Vector toFlatVector(Object obj)
	{
		return toFlatVector(obj,null);
	}

	public static Vector toFlatVector(Object obj, Vector result)
	{
		if (obj==null)
			return null;
		if (result==null)
			result = new Vector();
		if (isIteratable(obj)) {
			Iterator i = iterator(obj);
			while (i.hasNext())
				result.add(toFlatVector(i.next(),result));
		}
		else
			result.add(obj);
		return result;
	}

	protected static Class getElementType(Vector v)
	{
		for (int i=0; i<v.size(); i++)
			if (v.get(i)!=null)
				return v.get(i).getClass();
		return null;
	}

	public static Object[] toArray(Object obj, Class elementType)
	{
		Vector collect = toVector(obj);
		if (collect==null)
			return null;

		if (elementType==null)
			elementType = getElementType(collect);	//	use first object's element type
		if (elementType==null)
			elementType = Object.class;

		Object[] result = (Object[])Array.newInstance(elementType,collect.size());
		collect.toArray(result);
		return result;
	}

	public static Object[] toFlatArray(Object obj, Class elementType)
	{
		Vector collect = toFlatVector(obj);

		if (elementType==null)
			elementType = getElementType(collect);	//	use first object's element type
		if (elementType==null)
			elementType = Object.class;

		Object[] result = (Object[])Array.newInstance(elementType,collect.size());
		collect.toArray(result);
		return result;
	}

	public static List deepClone(List list)
	{
		List copy = null;
		try {
			copy = (List)list.getClass().newInstance();
			for (int i=0; i < list.size(); i++)
			{
				Object item = list.get(i);
				if (item!=null) item = ReflectionUtil.invoke(item,"clone");
				copy.add(item);
			}
		} catch (Exception e) {
			Application.error(e);
		}
		return copy;
	}

	public static Object[] appendArray(Object obj1, Object obj2)
	{
		return appendArray(obj1,obj2,null);
	}

	public static Object[] appendArray(Object obj1, Object obj2, Class elementType)
	{
		Vector collect = toVector(obj1);
		toVector(obj2,collect);

		if (elementType==null)
			elementType = getElementType(collect);	//	use first object's element type
		if (elementType==null)
			elementType = Object.class;

		Object[] result = (Object[])Array.newInstance(elementType,collect.size());
		collect.toArray(result);
		return result;
	}

	public static void sort(List coll, Comparator comp)
	{
		Object[] values = coll.toArray();
		if (comp!=null)
			Arrays.sort(values, comp);
		else
			Arrays.sort(values);
		coll.clear();
		for (int i=0; i<values.length; i++)
			coll.add(values[i]);
	}


	static class DBSelectionIterator implements Iterator
	{
		private DBSelectionModel model;
		private int currentIndex;

		DBSelectionIterator(DBSelectionModel model)
		{
			this.model = model;
			this.currentIndex = -1; //  empty
			if (model.hasSelection())
				currentIndex = fetchNext(model.getMinSelectionIndex());
		}

		public boolean hasNext()
		{
			return currentIndex >= 0;
		}

		public Object next()
		{
			if (currentIndex < 0)
				throw new IllegalStateException("must not call next after iterator is done");

			Integer result = new Integer(model.getDBId(currentIndex));
			currentIndex = fetchNext(currentIndex+1);
			return result;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}


		private int fetchNext(int idx)
		{
			while (idx <= model.getMaxSelectionIndex())
				if (model.isSelectedIndex(idx))
					return idx;
				else
					idx++;
			//  finished
			return -1;
		}
	}
}
