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

import de.jose.util.file.FileUtil;
import de.jose.util.file.FileIterator;
import de.jose.util.xml.XMLUtil;
import de.jose.util.ListUtil;
import de.jose.util.StringUtil;
import de.jose.Config;
import de.jose.Version;
import de.jose.Language;
import de.jose.Application;
import de.jose.task.io.PGNImport;
import de.jose.pgn.Collection;
import de.jose.pgn.PGNFileFilter;
import de.jose.window.JoFileChooser;
import de.jose.window.JoDialog;

import java.util.*;
import java.io.File;
import javax.swing.*;
import java.io.FileFilter;

import org.w3c.dom.Element;

/**
 * @author Peter Schäfer
 */

public class AutoImport
{
	/** list of files/directories to scan for auto imports    */
	protected FileIterator files;

	public AutoImport(Config cfg)
	{
		PGNFileFilter filter = new PGNFileFilter();
		filter.add("pgn");
		filter.add("epd");
		filter.add("fen");

		files = new FileIterator(filter);

        String[] paths = cfg.getPaths("auto-import");
        for (int i=0; i<paths.length; i++)
            files.add(paths[i],Application.theWorkingDirectory);

		String userdef = Version.getSystemProperty("jose.auto-import");
		if (userdef!=null)
			files.add(userdef,Application.theWorkingDirectory);
	}


	public boolean next() throws Exception
	{
		if (files.hasNext()) {
			importFile((File)files.next());
			return true;
		}
		else
			return false;
	}



	protected boolean importFile(File file) throws Exception
	{
		if (file==null || !file.exists()) return false; //  file does not exist anymore ? strange but OK

		//  look for previous version in DB
		String pattern1 = "file://"+file.getCanonicalPath();
		String pattern2 = "file://"+file.getCanonicalPath()+"!%";

		pattern1 = StringUtil.replace(pattern1,"\\","\\\\");
		pattern2 = StringUtil.replace(pattern2,"\\","\\\\");
		/** looks a bit sick, but it's right
		 *  there are several levels where backslashes get escaped:
		 *  Java, SQL processor, LIKE statement
		 */

		Date dbModified = Collection.getLatestModified(pattern1,pattern2);
		Date fileModified = new Date(file.lastModified());

		if (dbModified!=null)
		{
			if (fileModified.compareTo(dbModified)<=0)
				return false;   //  db is up-to-date

			//  file has changed on disk, ask user !
			String text = Language.get("dialog.autoimport.ask");
			text = StringUtil.replace(text,"^0",file.getName());

			int buttonPressed = JoDialog.showYesNoDialog(text, "dialog.autoimport.title",
			                                null,null, JOptionPane.YES_OPTION);
			switch (buttonPressed)
			{
			case JOptionPane.YES_OPTION:
				break;
			default:
			case JOptionPane.NO_OPTION:
				//  touch db entry (so that it won't be asked again)
				Collection.touch(dbModified,fileModified);
				return false;
			}
		}

		//  kick off import task
		PGNImport importer = PGNImport.openFile(file);

		return true;
	}

}
