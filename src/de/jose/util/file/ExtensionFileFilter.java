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

import java.io.File;
import java.util.Collection;
import java.util.Vector;

public class ExtensionFileFilter
		extends javax.swing.filechooser.FileFilter
		implements java.io.FileFilter, java.io.FilenameFilter
{
    /** List of accepted extensions */
    protected Vector extensions;

    public ExtensionFileFilter()
    {
        extensions = new Vector();
    }

    public ExtensionFileFilter(String ext)
    {
        this();
        add(ext);
    }

    public ExtensionFileFilter(String ext1, String ext2)
    {
        this();
        add(ext1);
        add(ext2);
    }

	public ExtensionFileFilter(Collection extensions)
	{
		this();
		addAll(extensions);
	}

    public void add(String ext)
    {
        extensions.add(ext);
    }

	public void addAll(Collection exts)
	{
		extensions.addAll(exts);
	}

	public boolean accept(File f)
	{
		return (f!=null) && (f.isDirectory() || accept(f.getName()));
	}

	public boolean accept(File dir, String fileName)
	{
        if (dir!=null)
	        return accept(new File(dir,fileName));
		else
	        return accept(fileName);
	}

	public boolean accept(String fileName)
	{
		for (int i=0; i<extensions.size(); i++) {
		    if (FileUtil.hasExtension(fileName, (String)extensions.get(i)))
		        return true;
		}
		//  else
		return false;
	}

	public String getDescription()
	{
		for (int i=0; i<extensions.size(); i++)
        {
            String desc = Language.get("filechooser."+extensions.get(i),null);
            if (desc != null) return desc;
        }
        //  build default description
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<extensions.size(); i++)
        {
            if (i > 0) buf.append(", ");
            buf.append("*.");
            buf.append((String)extensions.get(i));
        }
        return buf.toString();
	}

}
