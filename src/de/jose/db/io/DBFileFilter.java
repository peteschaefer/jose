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

package de.jose.db.io;

import de.jose.Language;
import de.jose.util.file.FileUtil;

import java.io.File;

/** @deprecated */
public class DBFileFilter
		extends javax.swing.filechooser.FileFilter
		implements java.io.FileFilter, java.io.FilenameFilter
{
    /** file extension for DB dump files    */
    public static final String DB_EXTENSION = "jose";

    public DBFileFilter()
    {
        super();
    }


	public boolean accept(File f)
	{
		return accept(f,f.getName());
	}

	public boolean accept(File file, String fileName)
	{
		try {
			if (file.isDirectory())
				return true;
			if (FileUtil.hasExtension(fileName,DB_EXTENSION))
                 return true;
			else
				return false;
		} catch (Throwable ex) {
            //  can't help it
            return false;
        }
	}

	public String getDescription()
	{
	    return "jose archive";
	}

}
