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
	 * */
	public static void addToClassPath(File jarFile)
			throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, NoSuchFieldException
	{
		URL url = fileToURL(jarFile);

		URLClassLoader cl = (URLClassLoader)ClassLoader.getSystemClassLoader();
		ReflectionUtil.invoke(cl,"addURL", URL.class,url);

		Object ucp = ReflectionUtil.getValue(cl,"ucp");
		ReflectionUtil.invoke(ucp,"push", URL[].class, new URL[] { url });
	}

	public static URL[] makeAllURLs(File dir)
	{
		File[] jars = dir.listFiles((FilenameFilter)new ExtensionFileFilter("jar"));
		URL[] urls = new URL[jars.length];

		for (int i=0; i<jars.length; i++)
			try {
				urls[i] = fileToURL(jars[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		return urls;
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
		URLClassLoader cl = (URLClassLoader)ClassLoader.getSystemClassLoader();

		String path = dir.getAbsolutePath();

		String[] oldValue = (String[])ReflectionUtil.getValue(ClassLoader.class,"usr_paths");
		String[] newValue = (String[])ListUtil.appendArray(oldValue, path);
		ReflectionUtil.setValue(ClassLoader.class,"usr_paths",newValue);
	}

	/**	add all jar files in a directory	*/
	public static void addAllToClassPath(File dir)
	{
		File[] jars = dir.listFiles((FilenameFilter)new ExtensionFileFilter("jar"));

		for (int i=0; i<jars.length; i++)
			try {
				addToClassPath(jars[i]);
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	/**
	 * set the JVM font path
	 * @deprecated custom fonts are loaded on demand by FontEncoding
	 * /
	public static final void setUserFontPath(String userPath) throws Exception
	{
		long time = System.currentTimeMillis();
		createFonts(userPath);

       	//	modifying the font path doesn't work since JDK 1.4.2 (bug ?, feature ? - nobody knows)
		//	we got to hack a little bit..
/*		if (Version.runtimeIsGreaterOrEqual("1.4.2"))
			registerFonts(userPath);
		else
            setFontProperty(userPath);
* /
		time = System.currentTimeMillis()-time;
		System.err.println("fonts: "+((double)time/1000.0));
	}

	/**
	 * set the JVM font path
	 * @deprecated custom fonts are loaded on demand by FontEncoding
	 * /
	public static final void createFonts(String userPath) throws Exception
	{
		StringTokenizer tok = new StringTokenizer(userPath,File.pathSeparator);
		while (tok.hasMoreTokens())
		{
			File dir = new File(tok.nextToken());
			String[] files = dir.list();
			for (int i=0; i<files.length; i++)
				if (FileUtil.hasExtension(files[i],"ttf"))
				{
					InputStream input = new FileInputStream(new File(dir,files[i]));
					input = new BufferedInputStream(input);
					Font.createFont(Font.TRUETYPE_FONT, input);
					input.close();
				}
		}
    }

	/**
	 * set the JVM font path
	 * @deprecated custom fonts are loaded on demand by FontEncoding
	 * /
	protected static void registerFonts(String userPath) throws Exception
    {
        /**
         * this is a dirty hack to work around a limitation in JRE 1.4.2
         * setting the system property "sun.java2d.fontpath" doesn't work ;-(
         * that's why we do the work ourselves...
         *
         * remove this workaround as soon as the JDK supports it...
         *
         * we call the private methods
         *  Win32GraphicsEnvironment.registerFontsWithPlatform()
         * and
         *  SunGraphicsEnvironment.registerFonts()
         * /
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ReflectionUtil.isInstanceOf(env,"sun.java2d.SunGraphicsEnvironment")) {
            StringTokenizer tok = new StringTokenizer(userPath,File.pathSeparator);
            while (tok.hasMoreTokens()) {
                String dir = tok.nextToken();
                ReflectionUtil.invoke("sun.java2d.SunGraphicsEnvironment", env,
                                        "registerFontsWithPlatform",
                                        String.class, dir);
                ReflectionUtil.invoke("sun.java2d.SunGraphicsEnvironment", env,
                                        "registerFonts",
                                        String.class, dir,
                                        boolean.class, Boolean.TRUE);
            }
        }
    }

	/**
	 * set the JVM font path
	 * @deprecated custom fonts are loaded on demand by FontEncoding
	 * /
	protected static void setFontProperty(String userPath)
    {
		String systemPath = getSystemFontPath();
		String javaPath = getJavaFontPath();

		String path =
			systemPath + File.pathSeparator +
			javaPath + File.pathSeparator +
			userPath;

        if (Version.java13)
	        System.setProperty("java.awt.fonts",path);      //  JDK 1.3
        else
		    System.setProperty("sun.java2d.fontpath",path); //  >= JDK 1.4
	}
     */
}
