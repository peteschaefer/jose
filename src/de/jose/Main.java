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

import java.lang.reflect.Method;

/**
 * launches the application
 * (de-couples some critical imports; i.e. this class will run with any JVM and present
 *  a decent error message, at least)
 *
 * to make sure that this class runs with M$ rotten JVM, compile it with JVC (the J++ compiler)
 * (that's why Application.open() is not called directly, but via reflection)
 */
public class Main
{
    public static boolean checkSystemRequirements()
	{
		//	check minimum system requirements	*
		if (System.getProperty("no.jre.check")!=null)
		{
			String javaVersion = System.getProperty("java.runtime.version");
			if (javaVersion==null) javaVersion = System.getProperty("java.version");

			if ((javaVersion==null) || (javaVersion.compareTo("1.4") < 0))
			{
				String url1 = "http://java.sun.com/j2se/downloads.html";
				String url2 = "http://sourceforge.net/project/showfiles.php?group_id=60120";

				if (javaVersion==null) javaVersion = "unknown !?";

				BootError.showError(
						"Java Runtime Environment 1.4 or later required !\n" +
						"Your current version is "+javaVersion+"\n"+
						"Please download Java Runtime Environment from \n"+
						"        "+url1+"\n"+
						"or download the latest jose release from \n"+
						"        "+url2);

				return false;
			}
		}
		return true;
	}

	public static void main(String[] args)
	{
        try {
//			System.getProperties().list(System.out);
			boolean show_splash = true;
			for (int i=0; i<args.length; i++)
				if (args[i].equalsIgnoreCase("splash=off"))
					show_splash = false;
/*
				if (args[i].equalsIgnoreCase("--help") ||
				    args[i].equalsIgnoreCase("-help") ||
				    args[i].equalsIgnoreCase("/help") ||
			        args[i].equalsIgnoreCase("--h") ||
				    args[i].equalsIgnoreCase("-h") ||
				    args[i].equalsIgnoreCase("/h"))
				{
					BootError.printHelp(null);
					System.exit(1);
				}
*/
/*
            if (searchApplication()) {
                //  other application detected; exiting
                System.err.println("running instance detected");
                System.exit(+2);
            }
*/
			if (!checkSystemRequirements())
				System.exit(-1);
			//  open the Splash Screen as early as possible
			//  note that it requires JDK 1.4 - we have to check for JDK version before
			if (show_splash) {
				Class splashScreenClass = Class.forName("de.jose.SplashScreen");
				Method openMethod = splashScreenClass.getMethod("open",null);
				openMethod.invoke(null,null);
			}
			/**
			 * delegate to de.jose.Application.main()
			 * we use reflection, so that the class does not appear in the imports
			 * (i.e. this class will not load any other classes, except SplashScreen and BootError)
			 */
			Class applicationClass = Class.forName("de.jose.Application");
			Class[] paramTypes = new Class[] { String[].class };
			Object[] paramValues = new Object[] { args };

			Method mainMethod = applicationClass.getMethod("main",paramTypes);
			mainMethod.invoke(null,paramValues);

		} catch (Throwable ex) {
			BootError.showError(ex);
		} finally {
	        try {
		        Class splashScreenClass = Class.forName("de.jose.SplashScreen");
		        Method closeMethod = splashScreenClass.getMethod("close",null);
		        closeMethod.invoke(null,null);
	        } catch (Throwable e) {
		        //
	        }
        }
	}

}
