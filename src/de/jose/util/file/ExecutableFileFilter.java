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

package de.jose.util.file;

import de.jose.Language;
import de.jose.Version;

import java.io.File;

/**
 * @author Peter Schäfer
 */

public class ExecutableFileFilter
        extends javax.swing.filechooser.FileFilter
        implements java.io.FileFilter, java.io.FilenameFilter
{
	public boolean accept(File file)
	{
		return file.isDirectory() || isExecutableFile(file);
	}

	public boolean accept(File dir, String name)
	{
		if (dir!=null)
			return accept(new File(dir,name));
		else
			return isExecutableFile(name);
	}

	public static boolean isExecutableFile(File file)
	{
		if (Version.windows)
			return FileUtil.hasExtension(file.getName(),"exe");
		else    //  UNIX
			return true;    //  TODO how can we get the executable attribute ??
	}

	public static boolean isExecutableFile(String fileName)
	{
		if (Version.windows)
			return FileUtil.hasExtension(fileName,"exe");
		else    //  UNIX
			return true;    //  TODO how can we get the executable attribute ??
	}


	public String getDescription()
	{
		return Language.get("filechooser.exe");
	}
}
