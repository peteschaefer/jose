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

package com.mysql.embedded.util;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;


public class Files
{
	public static void deleteContent(File directory)
	{
		File[] files = directory.listFiles();
		for (int i=0; i < files.length; i++)
		{
			if (files[i].isDirectory())
				deleteContent(files[i]);
			files[i].delete();
		}
	}


	public static void unzip(File zipfile, File baseDir)
	        throws IOException
	{
		ZipInputStream in = null;
		try {
			in = new ZipInputStream(new FileInputStream(zipfile));

			for (ZipEntry ety = in.getNextEntry(); ety != null; ety = in.getNextEntry())
			{
				File target;
				if (baseDir!=null)
					target = new File(baseDir,ety.getName());
				else
					target = new File(ety.getName());

				unzip(in,ety,target);
			}

		} finally {
			if (in != null) in.close();
		}
	}


	protected static void unzip(ZipInputStream in, ZipEntry ety, File target)
		throws IOException
	{
		if (ety.isDirectory())
			target.mkdirs();
		else {
			target.getParentFile().mkdirs();
			FileOutputStream out = null;
			long expectedSize;
			long actualSize;
			try {
				out = new FileOutputStream(target);
				expectedSize = ety.getSize();
				actualSize = Streams.copyStream(in,out, Long.MAX_VALUE);
			}
			finally {
				if (out!=null) out.close();
			}

			if (actualSize < expectedSize)
				throw new IOException("incomplete restore "+target);
		}

		long lastModified = ety.getTime();
		if (lastModified > 0) target.setLastModified(lastModified);

		in.closeEntry();
//		println(target.getAbsoluteFile()+" restored; size "+total);
	}

}
