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

import de.jose.util.file.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * experimental; not yet in use
 */
public class JoClassLoader
        extends URLClassLoader
{
    protected static final FileFilter JAR_FILE_FILTER = new FileFilter()
    {
        public boolean accept(File f)
        {
            String name = f.getName();
            return FileUtil.hasExtension(name,"jar") || FileUtil.hasExtension(name,"zip");
        }
    };


    public JoClassLoader(ClassLoader parent)
    {
        super(new URL[0],parent);
    }

    public JoClassLoader(File directory, ClassLoader parent)
    {
        super(getURLs(directory),parent);
    }


    public static URL[] getURLs(File directory)
    {
        ArrayList collect = new ArrayList();
        File[] files = FileUtil.listAllFiles(directory, JAR_FILE_FILTER);
        URL[] urls = new URL[files.length];
        for (int i=0; i<urls.length; i++)
            try {
                urls[i] = new URL("file",null,files[i].getAbsolutePath());
            } catch (MalformedURLException muex) {
                //  what ?
            }
        return urls;
    }


}
