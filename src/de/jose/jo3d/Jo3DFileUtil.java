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

package de.jose.jo3d;

import de.jose.util.file.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Utils for listing j3d files
 */

public class Jo3DFileUtil
{
	public static String[] list(File dir, boolean acceptStream)
	{
		if (!dir.exists())
			return null;
		else
			return dir.list(new Jo3DFilter(acceptStream));
	}

	public static File[] listFiles(File dir, boolean acceptStream)
	{
		if (!dir.exists())
			return null;
		else
			return dir.listFiles((FileFilter)new Jo3DFilter(acceptStream));
	}

	public static class Jo3DFilter
		implements FilenameFilter, FileFilter
	{
        private boolean acceptStream;

        public Jo3DFilter (boolean stream) {
            acceptStream = stream;
        }

		public boolean accept(File file)
		{
			return accept(file.getName());
		}

		public boolean accept(String fileName)
		{
            return  FileUtil.hasExtension(fileName,"j3df")
                    || acceptStream && (FileUtil.hasExtension(fileName,"zip")
                                    || FileUtil.hasExtension(fileName,"j3ds"));
		}

		public boolean accept(File file, String fileName)
		{
			return accept(fileName);
		}
	}
}
