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

package de.jose;

import de.jose.util.ClassPathUtil;
import de.jose.util.file.FileUtil;

import java.io.File;

public class Version
{
	/**	Application Version Info
	 *
	 *	the application name is found in lang.properties as "application.name"
	 *	(might be localized)
	 */
	public static final String jose = SplashScreen.version;

	/**	copyright year
	 */
	public static final String year	= SplashScreen.year;

	/**	the author (that's me)
	 */
	public static final String author	= SplashScreen.author;
	/**	author contact
	 */
	public static final String contact	= "peterschaefer@users.sourceforge.net";

    /** download site for Sun JRE
    	TODO move to xml config
    */
    public static final String  SunJRE_URL      = "http://java.sun.com/j2se/1.5.0/download.jsp";
    /** download site for jose  */

	/** project home page   */
	public static final String  projectURL      = SplashScreen.url;

	/**
	 * do we need MySQL UDFs ? currently not.
	 * if so, we must enable the grant tables.
	 */
	public static final boolean MYSQL_UDF = false;

	/**	Java Runtime Version
	 */
	public static final String java				= getSystemProperty("java.version");

	public static final String javaVM			= getSystemProperty("java.vm.version");

	public static final String javaRuntime		= getSystemProperty("java.runtime.version");


	public static final boolean java13			= java.substring(0,3).equals("1.3");
	public static final boolean java14			= java.substring(0,3).equals("1.4");
	public static final boolean java15			= java.substring(0,3).equals("1.5");

	public static final boolean java14orLater	= java.substring(0,3).compareTo("1.4") >= 0;
	public static final boolean java15orLater	= java.substring(0,3).compareTo("1.5") >= 0;

	/**	Operating system
	 */
	public static final String	osName			= getSystemProperty("os.name");

	public static final String	osVersion		= getSystemProperty("os.version");

	public static final String	arch			= getSystemProperty("os.arch");


	public static final boolean	windows			= osName.startsWith("Windows");
	public static final boolean windowsNT		= osName.startsWith("Windows NT");
	public static final boolean windows2000		= osName.startsWith("Windows 2000");
	public static final boolean windowsXP		= osName.startsWith("Windows XP") ||
												  (windows2000 && (osVersion.compareTo("5.1") >= 0));
	//	note that Windows XP may also identify as Windows 2000; version 5.1 is XP
	public static final boolean winNTfamily		= windowsNT || windows2000 || windowsXP;

	public static final boolean linux			= osName.startsWith("Linux");
	public static boolean linuxIntel;
	public static final boolean mac				= osName.startsWith("Mac OS");
	public static final boolean macIntel        = false;    //  not yet implemented

	public static final boolean unix			= !windows;

	public static String osDir;

	static {
		/**	currently, we have three subdirectories for native code in /bin and /engines:
			/bin/Windows
		 	/bin/Linux_i386
		 	/bin/Mac
		 */
		if (windows)
			osDir = "Windows";
		else if (linux) {
			if (arch.equals("i386") || arch.equals("x86")) {
				osDir = "Linux_i386";		//	i386 and x86 are synonymous to us
				linuxIntel = true;
			}
			else
				osDir = "Linux_"+arch;
		}
		else if (mac) {
			osDir = "Mac";
			//  TODO Mac with Intel architecture
		}
		else
			osDir = osName+"_"+arch;
	}

    public static boolean runtimeIsGreaterOrEqual(String version)
    {
        return javaRuntime.compareTo(version) >= 0;
    }

	private static boolean 	checked3d = false;
	private static boolean	hasJava3d;
	private static boolean 	loadedJava3d = false;
	private static String 	java3dVersion;
	private static String 	java3dImpl;

	private static boolean checkedFop = false;

	public static String mysqlVersion = "4.1.8";

	public static boolean mysql41 = mysqlVersion.startsWith("4.1");
	public static boolean mysql40 = mysqlVersion.startsWith("4.0");

	/**	Java3D
	 */
	public static boolean hasJava3d(boolean load, boolean preferOpenGL)	{
		if (!checked3d) {
			loadedJava3d = hasJava3d = ClassPathUtil.existsClass("javax.media.j3d.Canvas3D");
			if (!hasJava3d)
				hasJava3d = FileUtil.exists(Application.theWorkingDirectory, "lib/"+osDir+"/j3dcore.jar");
			if (!FileUtil.exists(Application.theWorkingDirectory,"3d"))
				hasJava3d = false;
			checked3d = true;
		}

		if (load && hasJava3d && !loadedJava3d)
			try {
				loadJava3d(preferOpenGL);
				loadedJava3d = true;
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		return hasJava3d;
	}

	private static void loadJava3d(boolean preferOpenGL) throws Exception
	{
		if (ClassPathUtil.existsClass("javax.media.j3d.Canvas3D")) {
			//	already in class path - fine
			Class.forName("javax.media.j3d.Canvas3D");
			return;
		}

		/**	append class path	*/
		if (windows && !preferOpenGL)
			setSystemProperty("j3d.rend","d3d");
		//  else: OpenGL is default
		File lib = new File(Application.theWorkingDirectory,"lib/"+Version.osDir);
		ClassPathUtil.addToLibraryPath(lib);
		ClassPathUtil.addAllToClassPath(lib, "j3dcore.jar;j3dutils.jar;vecmath.jar");   //  was: j3daudio.jar;
		Class.forName("javax.media.j3d.Canvas3D");
	}


	public static void loadFop() throws Exception
	{
		if (!checkedFop)
			synchronized (Application.theApplication)
			{
				if (checkedFop) return;

				if (ClassPathUtil.existsClass("org.apache.fop.apps.Driver")) {
					//	already in class path - fine
					Class.forName("org.apache.fop.apps.Driver");
					return;
				}

				//  append class path
				File lib = new File(Application.theWorkingDirectory,"lib");
				ClassPathUtil.addAllToClassPath(lib, "fop-plus.jar");
				Class.forName("org.apache.fop.apps.Driver");
				checkedFop = true;
			}
	}


	private static void readJava3dVersion()
	{
		try {
			/** DON'T put the following code into a static initializer
			 *  it might inadvertently initialize the AWT font system,
			 *  and subsequent changes to the font path will become ineffective
			 */
			Class.forName("javax.media.j3d.Canvas3D");
			Package pckg = 	Package.getPackage("javax.media.j3d");

			if (pckg!=null) {
				java3dVersion = pckg.getImplementationVersion();
				java3dImpl = pckg.getImplementationTitle();
			}
		} catch (ClassNotFoundException cnfex) {
			//	Java3D not installed
		} catch (UnsatisfiedLinkError ulex) {
			//	library missing
		} catch (Throwable ex) {
			//	Java3D not installed - jose will run anyway
		}
	}

	public static String getJava3dVersion(boolean preferOpenGL)
	{
		if (!hasJava3d(true,preferOpenGL)) return null;
		if (java3dVersion==null) readJava3dVersion();
		return java3dVersion;
	}

	public static String getJava3dImplementation(boolean preferOpenGL)
	{
		if (!hasJava3d(true,preferOpenGL)) return null;
		if (java3dImpl==null) readJava3dVersion();
		return java3dImpl;
	}

    public static final void disable3d()
    {
        hasJava3d = false;
		checked3d = true;
    }

    /**
     * use double buffering for 2d panels ?
     *  it is recommended but may be turned off for reasons (animation)
     */
    public static boolean useDoubleBuffer()
    {
        boolean defaultValue=false;
        if (mac) defaultValue=true; //  do enable it on Macs!
        return getSystemProperty("jose.2d.double.buffer",defaultValue);
    }

	public static String getSystemProperty(String key)
	{
        return getSystemProperty(key,null);
	}

    public static String getSystemProperty(String key, String def)
    {
        try {
            String value = System.getProperty(key);
            if (value==null)
                return def;
            else
                return value;
        } catch (Exception acex) {
            //	may happen when running as Applet !
            return def;
        }
    }


    public static boolean getSystemProperty(String key, boolean def)
    {
        try {
            String value = System.getProperty(key);
            if (value!=null) return parseBoolean(value);

            value = System.getProperty("no."+key);
            if (value!=null) return !parseBoolean(value);

            return def;

        } catch (Exception acex) {
            //	may happen when running as Applet
            return def;
        }
    }

    public static boolean parseBoolean(String str)
    {
        if (str==null) return false;
        if (str.length()==0) return true;

        return (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("on") ||
                str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("1"));
    }

    public static void setSystemProperty(String key, String value)
    {
        try {
            System.setProperty(key,value);
        } catch (Exception acex) {
            //  can't help it
	        acex.printStackTrace();
        }
    }

    public static void setDefaultSystemProperty(String key, String defaultValue)
    {
        if (System.getProperty(key)==null)
            setSystemProperty(key,defaultValue);
    }

	public static void main(String[] args)
	{
		System.out.println("version = "+jose);
		System.out.println();
		System.out.println("-- Java -----------------------------------");
		System.out.println("java = "+java);
		System.out.println("java runtime = "+javaRuntime);
		System.out.println("jaba VM = "+javaVM);
		System.out.println("java 3D = "+hasJava3d(false,false));
		System.out.println();
		System.out.println("-- Platform -------------------------------");
		System.out.println("arch = "+arch);
		System.out.println("osName = "+osName);
		System.out.println("osVersion = "+osVersion);
		System.out.println("osDir = "+osDir);
		System.out.println("unix = "+unix);
		System.out.println("linux = "+linux);
		System.out.println("windows = "+windows);
		System.out.println("mac = "+mac);
		System.out.println();
		System.getProperties().list(System.out);
		System.out.println("-------------------------------------------");
	}

}
