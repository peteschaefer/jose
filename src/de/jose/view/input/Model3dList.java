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

package de.jose.view.input;

import de.jose.Application;
import de.jose.Version;
import de.jose.jo3d.Jo3DFileReader;
import de.jose.jo3d.Jo3DFileUtil;
import de.jose.util.file.FileUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * a JComboBox that displays a list of available 3d models
 * (display names are extracted from the file !)
 *
 * @author Peter Schäfer
 */
public class Model3dList
		extends JComboBox
        implements ValueHolder
{
	public Model3dList(File directory, String language, boolean includeStream)
	{
		super(getFileEntries(directory,language,includeStream));
	}

	public String getSelectedFile()
	{
		FileEntry ety = (FileEntry)getSelectedItem();
		if (ety != null)
			return ety.fileName;
		else
			return null;
	}

	public void setSelectedFile(String fileName)
	{
		ListModel lm = getModel();
		for (int i=0; i<lm.getSize(); i++) {
			FileEntry ety = (FileEntry)lm.getElementAt(i);
			if (ety.fileName.equals(fileName)) {
				setSelectedIndex(i);
				return;
			}
		}
	}

	//  implements ValueHolder

	public Object getValue()            { return getSelectedFile(); }

	public void setValue(Object value)  { setSelectedFile((String)value); }

	//-------------------------------------------------------------------------------
	//	Private Part
	//-------------------------------------------------------------------------------

	private static Vector getFileEntries(File directory, String language, boolean acceptStream)
	{
        String[] fileNames = Jo3DFileUtil.list(directory,acceptStream);
		Vector result = new Vector();
		if (fileNames!=null)
			for (int i=0; i < fileNames.length; i++)
			   try {
					result.add(new FileEntry(directory,fileNames[i],language));
				} catch (IOException ioex) {
					Application.error(ioex);
				}
		return result;
	}

	private static class FileEntry
	{
		String fileName;
        String displayName;

        FileEntry(String name)
        {
            fileName = name;
            displayName = FileUtil.trimExtension(fileName);
        }

		FileEntry(File dir, String name, String language)
            throws IOException
        {
            fileName = name;
			if (Version.hasJava3d(true,Application.theUserProfile.getBoolean("board.3d.ogl"))) {
            	displayName =  Jo3DFileReader.getDisplayName(new File(dir,name), language);
				//	Java3D must be installed for this; otherwise we just display the file name:
			}
            if (displayName==null)
                displayName = FileUtil.trimExtension(fileName);
        }


		public String toString()	{ return displayName; }
	}
}
