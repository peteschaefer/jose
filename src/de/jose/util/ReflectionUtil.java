/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionUtil
{

	/**
	 * call method by reflection
	 * this method allows to call protected and private methods; use it with care...
	 *
	 * @param targetClass
	 * @param target
	 * @param methodName
	 * @param paramType0
	 * @param paramValue0
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    public static Object invoke(Class targetClass, Object target, String methodName,
                                Class paramType0, Object paramValue0)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Class[] paramTypes = new Class[1];
        Object[] paramValues = new Object[1];
        paramTypes[0] = paramType0;
        paramValues[0] = paramValue0;
        return invoke(targetClass,target,methodName,paramTypes,paramValues);
    }

	public static Object invoke(String targetClassName, Object target, String methodName,
								Class paramType0, Object paramValue0)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
	{
		Class targetClass = Class.forName(targetClassName);
		return invoke(targetClass,target,methodName,paramType0,paramValue0);
	}

    public static Object invoke(Class targetClass, Object target, String methodName,
                                Class paramType0, Object paramValue0,
                                Class paramType1, Object paramValue1)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        Class[] paramTypes = new Class[2];
        Object[] paramValues = new Object[2];
        paramTypes[0] = paramType0;
        paramValues[0] = paramValue0;
        paramTypes[1] = paramType1;
        paramValues[1] = paramValue1;
        return invoke(targetClass,target,methodName,paramTypes,paramValues);
    }

	public static Object invoke(Object target, String methodName,
								Class paramType0, Object paramValue0)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		return invoke(target.getClass(),target,methodName,paramType0,paramValue0);
	}

	public static Object invoke(String targetClassName, Object target, String methodName,
								Class paramType0, Object paramValue0,
								Class paramType1, Object paramValue1)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
	{
		Class targetClass = Class.forName(targetClassName);
		return invoke(targetClass,target,methodName,paramType0,paramValue0,paramType1,paramValue1);
	}

    public static Object invoke(Object target, String methodName,
                              Class[] paramTypes, Object[] paramValues)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        return invoke(target.getClass(), target, methodName, paramTypes, paramValues);
    }

	public static Object invoke(Object target, String methodName)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		return invoke(target.getClass(), target, methodName, (Class[])null, (Object[])null);
	}

	public static Method getMethod(Class targetClass, String methodName, Class[] paramTypes)
		throws NoSuchMethodException
	{
		Method method = null;
		NoSuchMethodException nsmex = null;
		for (Class clazz = targetClass; clazz != null; clazz = clazz.getSuperclass())
			try {
				method = clazz.getDeclaredMethod(methodName,paramTypes);
				break;
			} catch(NoSuchMethodException ex) {
				if (nsmex==null) nsmex = ex;
				continue;
			}

		if (method==null && nsmex!=null)
			throw nsmex;
		else
			return method;
	}

	public static Field getField(Class targetClass, String fieldName)
		throws NoSuchFieldException
	{
		Field field = null;
		NoSuchFieldException nsfex = null;
		for (Class clazz = targetClass; clazz != null; clazz = clazz.getSuperclass())
			try {
				field = clazz.getDeclaredField(fieldName);
				break;
			} catch(NoSuchFieldException ex) {
				if (nsfex==null) nsfex = ex;
				continue;
			}
		if (field==null && nsfex!=null)
			throw nsfex;
		else {
			field.setAccessible(true);
			return field;
		}
	}

    public static Object invoke(Class targetClass, Object target, String methodName,
                              Class[] paramTypes, Object[] paramValues)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
		Method method = getMethod(targetClass, methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target,paramValues);
    }

	public static Object invoke(String targetClassName, Object target, String methodName,
							  Class[] paramTypes, Object[] paramValues)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
	{
		Class targetClass = Class.forName(targetClassName);
		return invoke(targetClass,target,methodName,paramTypes,paramValues);
	}

	public static Object getValue(Object target, String fieldName)
			throws NoSuchFieldException, IllegalAccessException
	{
		return getValue(target.getClass(),target,fieldName);
	}

	public static Object getValue(Class targetClass, String fieldName)
			throws NoSuchFieldException, IllegalAccessException
	{
		return getValue(targetClass,null,fieldName);
	}

	public static Object getValue(Class targetClass, Object target, String fieldName)
			throws NoSuchFieldException, IllegalAccessException
	{
		Field field = getField(targetClass, fieldName);
		return field.get(target);
	}

	public static void setValue(Class targetClass, String fieldName, Object value)
			throws NoSuchFieldException, IllegalAccessException
	{
		setValue(targetClass,null,fieldName,value);
	}

	public static void setValue(Object target, String fieldName, Object value)
			throws NoSuchFieldException, IllegalAccessException
	{
		setValue(target.getClass(),target, fieldName,value);
	}

	public static void setValue(Class targetClass, Object target, String fieldName, Object value)
			throws NoSuchFieldException, IllegalAccessException
	{
		Field field = getField(targetClass, fieldName);
		field.set(target,value);
	}

	public static boolean isInstanceOf(Object obj, String className)
		throws ClassNotFoundException
	{
		Class clazz = Class.forName(className);
		return clazz.isInstance(obj);
	}


	public static void getConstants(Class clazz, Map name2value, Map value2name)
	{
		Field[] fields = clazz.getDeclaredFields();
		for (int i=0; i<fields.length; i++) {
			int mod = fields[i].getModifiers();
			if (Modifier.isStatic(mod)) {
				try {
					fields[i].setAccessible(true);
					String name = fields[i].getName();
					Object value = fields[i].get(null);
					if (name2value!=null) name2value.put(name,value);
					if (value2name!=null) value2name.put(value,name);
				} catch (Exception e) {
					e.printStackTrace();  //To change body of catch statement use Options | File Templates.
				}
			}
		}
	}

	public static String nameOfConstant(Class clazz, int value)
	{
		return nameOfConstant(clazz,new Integer(value));
	}

	public static String nameOfConstant(Class clazz, Object value)
	{
		HashMap value2name = new HashMap();
		getConstants(clazz,null,value2name);
		return (String)value2name.get(value);
	}


}
