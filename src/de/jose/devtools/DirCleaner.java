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

package de.jose.devtools;

import java.io.File;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Peter Schäfer
 */

public class DirCleaner
{
	public static void main(String[] args)
	{
		File dir = null;
		HashSet patterns = new HashSet();
		boolean delete = true;

		if (args.length > 0)
			dir = new File(args[0]);
		for (int i=1; i<args.length; i++)
			if (args[i].equals("-l"))
				delete = false;
			else
				patterns.add(args[i].toLowerCase());

		if (dir==null || patterns.isEmpty())
		{
			System.out.println("DirCleaner <directory> [-l] (<pattern>)+ ");
			return;
		}
		else
			clean(dir,patterns,delete);
	}

	public static void clean(File file, Set patterns, boolean delete)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			for (int i=0; i<files.length; i++)
				clean(files[i],patterns,delete);
		}
		else if (patterns.contains(file.getName().toLowerCase()))
		{
			//  match
			System.out.print(file);
			if (delete) {
				if (file.delete())
					System.out.print(" deleted.");
				else
					System.out.print(" not deleted.");
			}
			System.out.println();
		}
	}
}
