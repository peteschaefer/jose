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

import de.jose.util.ListUtil;

import java.io.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Iterator;

/**
 * @author Peter Schäfer
 */

public class FileIterator
        implements java.util.Iterator
{
	/** the root files/directories
	 *  ArrayList<File>
	 * */
	protected ArrayList root;
	/** file filter for scanning    */
	protected FileFilter filter;
	/** iterator stack
	 *  Stack<Iterator>
	 */
	protected Stack iterators;

	private File nextFile;


	public FileIterator(FileFilter filter)
	{
		this.root = new ArrayList();
		this.filter = filter;
		this.iterators = null;
	}

	public void add(File file)
	{
		root.add(file);
	}

	public void add(File[] files)
	{
		for (int i=0; i<files.length; i++)
			root.add(files[i]);
	}

	public void add(String path, File baseDir)
	{
		File[] dirs = FileUtil.scanPath(path,baseDir,false);
		add(dirs);
	}


	public boolean hasNext()
	{
		if (iterators==null) {
			init();
			nextFile = fetchNext();
		}
		return nextFile!=null;
	}

	public Object next()
	{
		File result = nextFile;
		if (nextFile!=null) nextFile = fetchNext();
		return result;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}


	private Iterator top()
	{
		if (iterators.isEmpty())
			return null;
		else
			return (Iterator)iterators.elementAt(iterators.size()-1);
	}

	private void init()
	{
		iterators = new Stack();
		iterators.push(root.iterator());
	}

	private File fetchNext()
	{
		Iterator current = top();
		while (current!=null)
		{
			while (current.hasNext())
			{
				File next = (File)current.next();
				if (filter!=null && !filter.accept(next)) continue;

				if (next.isFile()) return next;
				if (next.isDirectory())
				{
					File[] files = next.listFiles(filter);
					current = ListUtil.iterator(files);
					iterators.push(current);
				}
			}
			iterators.pop();
			current = top();
		}
		return null;
	}
}
