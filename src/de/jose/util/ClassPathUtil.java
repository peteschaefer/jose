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

import de.jose.util.file.ExtensionFileFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

/**
 * 
 * @author Peter Schäfer
 */

public class ClassPathUtil
{
	public static boolean existsClass(String className)
	{
		String pathName = className.replace('.','/')+".class";
		return existsResource(pathName);
	}

	public static boolean existsResource(String pathName)
	{
		URL url = ClassLoader.getSystemClassLoader().getResource(pathName);
		return (url!=null);
	}

	/**
	 * 	dirty hack to append class path
	 * 	(e.g. system depend paths like lib/Windows; don't like to set those path via the command line)
	 *
	 * 	@deprecated this should not be necessary. We can set the classpath in a startup script. No need for such hacks.
	 * 	Plugin mechanisms for plaf, or DB drivers are not a priority anymore.
	 * */
	public static void addToClassPath(File jarFile)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, NoSuchFieldException
	{
		URL url = fileToURL(jarFile);
		ClassLoader scl = ClassLoader.getSystemClassLoader();

		if (scl instanceof URLClassLoader)
		{
			URLClassLoader cl = (URLClassLoader)ClassLoader.getSystemClassLoader();
			ReflectionUtil.invoke(cl,"addURL", URL.class,url);

			Object ucp = ReflectionUtil.getValue(cl,"ucp");
			ReflectionUtil.invoke(ucp,"push", URL[].class, new URL[] { url });
		}
	}

	public static URL fileToURL(File file)
    	throws IOException
	{
		file = file.getCanonicalFile();
	    String s = file.getAbsolutePath();
		s = s.replace('\\','/');
		if(!s.startsWith("/"))
			s = "/" + s;
		if(!s.endsWith("/") && file.isDirectory())
			s = s + "/";
		return new URL("file", "", s);
     }

	/**
	 * dirty hack to append library path
	 * (otherwise library path must be set via command line)
	 *
	 * @param dir
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	public static void addToLibraryPath(File dir)
			throws NoSuchFieldException, IllegalAccessException
	{
		String path = dir.getAbsolutePath();

		String[] oldValue = (String[])ReflectionUtil.getValue(ClassLoader.class,"usr_paths");
		String[] newValue = (String[])ListUtil.appendArray(oldValue, path);
		ReflectionUtil.setValue(ClassLoader.class,"usr_paths",newValue);
	}

	public static void addAllToClassPath(File baseDir, String path)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, NoSuchFieldException
	{
		StringTokenizer tok = new StringTokenizer(path,";");
		while (tok.hasMoreTokens()) {
			File jarFile = new File(baseDir,tok.nextToken());
			addToClassPath(jarFile);
		}
	}

}
