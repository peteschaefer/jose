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

package de.jose.image;

import java.io.File;
import java.io.IOException;

/**
 * examines a list of TrueType font to look for JVM crashes.
 * note that the Java Font Manager sometimes crashes within native code
 * (especially in 1.4). This is definitely a bug on the side of the JVM
 * but we have to sort out the "dangerous" fonts.
 *
 * @author Peter Schäfer
 * @version "$Revision:  $","$Date:  $"
 */
public class FontCrash
{

	public static void move(File srcDir, File dstDir)
		throws IOException
	{
		move(srcDir,dstDir, Integer.MAX_VALUE);
	}

	public static void move(File srcDir, File dstDir, int count)
		throws IOException
	{
		File[] files = srcDir.listFiles();
		if (count > files.length) count = files.length;
		for (int i=0; i < count; i++)
		{
			File dstFile = new File(dstDir,files[i].getName());
			if (!files[i].renameTo(dstFile))
				System.out.println("could not move "+files[i]+" to "+dstDir);
 ;
		}
	}
/*
	public static void main(String[] args)
	{
		try {
			File base_dir;
			if (args.length >= 1)
				base_dir = new File(args[0]);
			else
				base_dir = new File(".");

//			File todo_dir = new File(base_dir, "fonts-todo");
			File work_dir = new File(base_dir, "fonts");
//			File ok_dir = new File(base_dir, "fonts-ok");
//			File broken_dir = new File(base_dir, "fonts-bad");

			//	1. get rid of fonts from previous run
//			move (work_dir, ok_dir);
			//	2. copy one file from the to do directory
//			move (todo_dir, work_dir, 1);
			//	3. set the font path
			String systemPath = sun.awt.font.NativeFontWrapper.getFontPath(false);
			String javaPath = Version.getSystemProperty("java.home")+"/lib/fonts";
			String myPath = work_dir.getAbsolutePath();

			System.setProperty("sun.java2d.fontpath",
					systemPath+";"+myPath);
			//	4. get the font list (may cause crash)
			String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			for (int i=0; i<fonts.length; i++)
			{
				System.out.println(fonts[i]+" is OK");
			}
		} catch (Throwable thr) {
			thr.printStackTrace();
		}
	}
*/
	//--------------------------------------------------------------------------

} // class FontCrash

/*
 * $Log: $
 *
 */

