/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view.input;

import de.jose.image.ImgUtil;
import de.jose.util.file.FileUtil;
import de.jose.window.JoFileChooser;
import de.jose.window.JoFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A text input field for files.
 * An additional button pops up a file chooser
 *
 * all instance share a single JFileChooser (for efficiency)
 *
 * @author Peter Schäfer
 */
public class FileInput
        extends JPanel
        implements ValueHolder, ActionListener
{
	/** base directory; if possible, show relative paths only   */
	protected File baseDir;
	/** text input field    */
	protected JTextField textInput;
	/** button for popping up the file chooser  */
	protected JButton button;
	/** A list of event listeners for this component. */
	protected java.util.List listeners;
	protected String actionCommand;
	protected ArrayList filters;
	protected int dialogType;
	protected int selectMode;

	protected static JoFileChooser gChooser = null;

	public FileInput()
	{
		super(new BorderLayout());
		textInput = new JTextField();
		button = new JButton(ImgUtil.getMenuIcon("menu.file.open"));
		button.addActionListener(this);
        button.setBorder(new EmptyBorder(2,2,2,2));

		add(textInput,BorderLayout.CENTER);
		add(button,BorderLayout.EAST);

		listeners = new ArrayList();
		filters = new ArrayList();
		dialogType = JFileChooser.OPEN_DIALOG;
		selectMode = JFileChooser.FILES_ONLY;
	}

	public FileInput(File baseDir, int dialogType, int selMode)
	{
		this();
		setBaseDirectory(baseDir);
		setDialogType(dialogType);
		setFileSelectionMode(selMode);
	}


	public Object getValue()
	{
		return getFile();
	}

	public void setValue(Object value)
	{
		if (value == null)
			setFile((File)null);
		else if (value instanceof File)
			setFile((File)value);
		else
			setFile(value.toString());
	}

	public File getFile()
	{
		String path = textInput.getText();
		if (path.length()==0) return null;

		File file;
		if (baseDir!=null)
			file = new File(baseDir,path);
		else
			file = new File(path);
		if (!file.exists())
			file = new File(path);
		return file;
	}

	public void setFile(File file)
	{
		if (file==null)
			textInput.setText("");
		else if (baseDir!=null && FileUtil.isChildOf(file,baseDir))      //  show only relative path
			textInput.setText(FileUtil.getRelativePath(baseDir,file, File.separator));
		else    //  show absolute path
			textInput.setText(file.toString());
	}

	public void setFile(String path)
	{
		if (path==null)
			textInput.setText("");
		else {
			File file1 = new File(path);
			File file2 = new File(baseDir,path);

			if (file1.exists())
				setFile(file1);
			else if (baseDir!=null && file2.exists())
				setFile(file2);
			else
				textInput.setText(path);    //  can't tell
		}
	}

	public void setBaseDirectory(File dir)
	{
		this.baseDir = dir;
		//  adjust text field, if necessary
		setFile(getFile());
	}

	public void setColumns(int columns)
	{
		textInput.setColumns(columns);
	}

	public void addFilter(javax.swing.filechooser.FileFilter filter)
	{
		this.filters.add(filter);
	}

	public void showFileChooser()
	{
		File current = getFile();
		File currentDir;
		String currentName;

		if (current==null) {
			currentDir = baseDir;
			currentName = "";
		}
		else {
			currentDir = current.getParentFile();
			if (currentDir==null || !currentDir.exists()) currentDir = baseDir;
			currentName = current.getName();
		}

		if (gChooser == null)
			gChooser = new JoFileChooser(currentDir, true);

		gChooser.setDialogType(dialogType);
		gChooser.setFileSelectionMode(selectMode);
		gChooser.setChoosableFileFilters(filters);
		gChooser.setAcceptAllFileFilterUsed(true);

		gChooser.setCurrentDirectory(currentDir);
		gChooser.setSelectedFile(new File(currentDir,currentName));

		if (gChooser.showDialog(JoFrame.getActiveFrame(),null)==JoFileChooser.APPROVE_OPTION)
		{
			setFile(gChooser.getSelectedFile());
			fireEvent(new ActionEvent(this, TextEvent.TEXT_VALUE_CHANGED, actionCommand));
		}
	}

	public void setFileSelectionMode(int fileMode)
	{
		selectMode = fileMode;
	}

	public void setDialogType(int dlgType)
	{
		dialogType = dlgType;
	}


	public void addActionListener(ActionListener list)
	{
		listeners.add(list);
	}

	public void removeActionListener(ActionListener list)
	{
		listeners.remove(list);
	}

	public void setActionCommand(String cmd)
	{
		actionCommand = cmd;
	}

	public void fireEvent(ActionEvent event)
	{
		Iterator i = listeners.iterator();
		while (i.hasNext())
		{
			ActionListener listener = (ActionListener)i.next();
			listener.actionPerformed(event);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		showFileChooser();
	}
}
