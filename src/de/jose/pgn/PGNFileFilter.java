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

package de.jose.pgn;

import de.jose.util.file.ExtensionFileFilter;
import de.jose.util.file.FileUtil;

import java.io.File;

public class PGNFileFilter
		extends ExtensionFileFilter
		implements java.io.FileFilter, java.io.FilenameFilter		
{
	private static final long ZIP_MAX_SIZE = 16*1024;
	private static final long GZIP_MAX_SIZE = 16*1024;
	private static final long BZIP_MAX_SIZE =  4*1024;

	public static PGNFileFilter newPGNFilter()
	{
		PGNFileFilter result = new PGNFileFilter();
		result.add("pgn");
		return result;
	}

	public static PGNFileFilter newEPDFilter()
	{
		PGNFileFilter result = new PGNFileFilter();
		result.add("epd");
		result.add("fen");
		return result;
	}

	public boolean accept(File f)
	{
		return f.isDirectory() || accept(f.getParentFile(),f.getName());
	}
	
	public boolean accept(File dir, String fileName)
	{
		if ((dir!=null) && new File(dir,fileName).isDirectory())
			return true;

		if (dir==null)
			return super.accept(fileName);

//		long millis = System.currentTimeMillis();
//		System.err.print("["+fileName);
		try {
			if (FileUtil.hasExtension(fileName,"zip"))
			{
				File file = new File(dir,fileName);
				return (file.length() > ZIP_MAX_SIZE) || ZipEnumeration.contains(file, this);
				//  don't examine large files; it's just too expensive
			}

			if (FileUtil.hasExtension(fileName,"gz") || FileUtil.hasExtension(fileName,"gzip"))
			{
				String trimmedName = FileUtil.trimExtension(fileName);
				if (FileUtil.hasExtension(trimmedName,"tar")) {
					File file = new File(dir,fileName);
					return (file.length() > GZIP_MAX_SIZE) || TarEnumeration.contains(file, this);   //  tar.gz
					//  don't examine large files; it's just too expensive
				}
				else
					return super.accept(trimmedName);
			}

			if (FileUtil.hasExtension(fileName,"tgz") ||
			    FileUtil.hasExtension(fileName,"tbz") || FileUtil.hasExtension(fileName,"tbz2"))
			{
				File file = new File(dir,fileName);
				return (file.length() > BZIP_MAX_SIZE) || TarEnumeration.contains(file, this);   //  tgz = tar.gz
				//  don't examine large files; it's just too expensive
			}

			if (FileUtil.hasExtension(fileName,"bz")
					|| FileUtil.hasExtension(fileName,"bz2")
					|| FileUtil.hasExtension(fileName,"bzip")
					|| FileUtil.hasExtension(fileName,"bzip2"))
			{
				String trimmedName = FileUtil.trimExtension(fileName);
				//  this can become expensive !
				if (FileUtil.hasExtension(trimmedName,"tar")) {
					File file = new File(dir,fileName);
					return (file.length() > BZIP_MAX_SIZE) || TarEnumeration.contains(file, this);   //  tar.bz
					//  don't examine large files; it's just too expensive
				}
				else
					return super.accept(trimmedName);
			}
		} catch (Exception e) {
			//  Zip file error - shit happens
			return false;
		} finally {
//			System.err.print(" ");
//			System.err.print(System.currentTimeMillis()-millis);
//			System.err.println("]");
		}

		return super.accept(fileName);
	}

}
