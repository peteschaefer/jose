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

package de.jose.book;

import de.jose.Language;
import de.jose.book.crafty.CraftyBook;
import de.jose.book.polyglot.PolyglotBook;

import java.io.*;

/**
 * OpeningBookFilter
 *
 * @author Peter Schäfer
 */
public class OpeningBookFilter
			extends javax.swing.filechooser.FileFilter
			implements java.io.FileFilter, FilenameFilter
{
	private OpeningBook book;

	public OpeningBookFilter(OpeningBook clazz)
	{
		this.book = clazz;
	}

	public boolean accept(File file)
	{
		if (file.isDirectory()) return true;

		try
		{
			boolean result = false;

			synchronized (this) {
				if (book==null) {
					OpeningBook instance = OpeningBook.open(file);
					result = (instance!=null);
					if (instance!=null) instance.close();
				}
				else {
					RandomAccessFile raf = new RandomAccessFile(file,"r");
					result = book.open(raf);
					book.close();
					raf.close();
				}
			}

			return result;

		} catch (IOException e) {
			return false;
		}
	}

	public String getDescription()
	{
		if (book!=null && (book instanceof CraftyBook))
			return Language.get("filechooser.crafty");
		else if (book!=null && (book instanceof PolyglotBook))
			return Language.get("filechooser.polyglot");
		else
			return Language.get("filechooser.book");
	}

	public boolean accept(File dir, String name)
	{
		return accept(new File(dir,name));
	}

}
