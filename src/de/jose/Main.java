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

import de.jose.window.JoDialog;

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
		Runtime rt = Runtime.getRuntime();
		long totalMem = rt.totalMemory();
		long maxMem = rt.maxMemory();
		long freeMem = rt.freeMemory();
		double megs = 1048576.0;

//		System.out.println ("Total Memory: " + totalMem + " (" + (totalMem/megs) + " MiB)");
//		System.out.println ("Max Memory:   " + maxMem + " (" + (maxMem/megs) + " MiB)");
//		System.out.println ("Free Memory:  " + freeMem + " (" + (freeMem/megs) + " MiB)");
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

			if (!checkSystemRequirements())
				System.exit(-1);
			if (show_splash)
				SplashScreen.open();
			Application.main(args);

		} catch (Throwable ex) {
			ex.printStackTrace();
			JoDialog.showErrorDialog(null,ex.getMessage());
		} finally {
	        try {
				SplashScreen.close();
	        } catch (Throwable e) {
		        //
	        }
        }
	}

}
