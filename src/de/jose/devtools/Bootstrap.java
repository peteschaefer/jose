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

package de.jose.devtools;

import de.jose.Application;
import de.jose.Version;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Installer looks for a JRE 1.3 or JRE 1.4 installation on the current
 * system; it is meant to run with all JREs even with Microsoft's)
 */
public class Bootstrap
		implements WindowListener, ActionListener, FilenameFilter
{
	private static final String text[] = {
		"Jose requires Java Runtime Environment ",
		"version 1.3 or 1.4.",
		"",
		"Please locate the Java executable:",
		"",
		"If the required JRE is not installed on your system,",
		"you can download it free of charge from: ",
		"java.sun.com ",
	};
	
	private Dialog dialog;
	private Button cancel,next,browse;
	private TextField path;
	
	private String javaVersion;
	private File javaHome;
	private String os;
	
	public Bootstrap()
	{
		super();
	}
	
	public void run()
		throws IOException
	{
		//	look for the installed java version
		javaVersion = Version.getSystemProperty("java.version");
		javaHome = new File(Version.getSystemProperty("java.home"));
		os = Version.getSystemProperty("os.name");
		
		if (javaVersion==null || javaVersion.compareTo("1.3") < 0  || 
			javaHome==null || !javaHome.exists())
			openDialog();
		else if (os.startsWith("Windows"))
			runSetup(javaHome.getAbsolutePath()+File.separator+"bin"+File.separator+"java.exe");
		else
			runSetup(javaHome.getAbsolutePath()+File.separator+"bin"+File.separator+"java");
	}
	
	public void runSetup(String javaExec)
		throws IOException
	{
		File workingDir = Application.getWorkingDirectory();
		workingDir = new File(workingDir.getCanonicalPath());
		
		/*	start the 1.3 JVM 
		   .../java.exe -cp ... de.jose.Setup
		 */
		StringBuffer command = new StringBuffer();
		command.append(javaExec);
		command.append(" -classpath ");
		command.append(workingDir);
		command.append(File.separator);
		command.append("classes");
		
		//	append alll jar files found in the lib directory
		File lib = new File(workingDir,"lib");
		String[] jars = lib.list();
		for (int i=0; i<jars.length; i++)
			if (jars[i].endsWith(".jar"))
			{
				command.append(File.pathSeparator);
				command.append(workingDir);
				command.append(File.separator);
				command.append("lib");
				command.append(File.separator);
				command.append(jars[i]);
			}
		
		command.append(" de.jose.Setup ");
		command.append(workingDir.getAbsolutePath());
		System.out.println(command.toString());
		
		// de.jose.Setup will do the real setup
		Runtime.getRuntime().exec(command.toString());
		System.exit(1);
	}
	
	public void openDialog()
	{
		//	center on screen
		dialog = new Dialog((Frame)null,"Jose Installation",true);
		Dimension screen = dialog.getToolkit().getScreenSize();
		dialog.setSize(480,360);
		dialog.setLocation((screen.width-dialog.getSize().width)/2, 
						   (screen.height-dialog.getSize().height)/2);
		dialog.setLayout(new BorderLayout());
		
		Panel textPanel = new Panel();
		Font font = new Font("SansSerif", 16, 0);
		textPanel.setLayout(new GridLayout(text.length+1,1));
		for (int i=0; i<4; i++)
		{
			Label lab = new Label(text[i]);
			lab.setFont(font);
			textPanel.add(lab);
		}
		
		Panel inputPanel = new Panel();
		inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		inputPanel.add(path = new TextField());
		if (os.startsWith("Windows"))
			path.setText("C:\\Program Files\\JavaSoft\\JRE\\1.4\\bin\\java.exe");
		else
			path.setText("/usr/local/JRE/1.4/bin/java.exe");
		path.setSize(400,20);
		inputPanel.add(browse = new Button("Browse..."));
		textPanel.add(inputPanel);
		
		font = new Font("SansSerif", 12, 0);
		for (int i=4; i<text.length; i++)
		{
			Label lab = new Label(text[i]);
			lab.setFont(font);
			textPanel.add(lab);
		}
		
		dialog.add(textPanel, BorderLayout.NORTH);
		
		Panel buttonPanel = new Panel();
		buttonPanel.add(cancel = new Button("Cancel"));
		buttonPanel.add(next = new Button("Next >"));
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		
		cancel.addActionListener(this);
		next.addActionListener(this);
		browse.addActionListener(this);
		
		dialog.addWindowListener(this);
		dialog.show();
	}
	
	public void windowOpened(WindowEvent evt)		{ }
	public void windowClosed(WindowEvent evt)		{ }
	public void windowActivated(WindowEvent evt)	{ }
	public void windowDeactivated(WindowEvent evt)	{ }
	public void windowIconified(WindowEvent evt)	{ }
	public void windowDeiconified(WindowEvent evt)	{ }
	public void windowClosing(WindowEvent evt)		{ System.exit(1); }
	
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource()==cancel) System.exit(1);
		
		if (evt.getSource()==next) 
		{
			String filePath = path.getText().trim();
			if (filePath.length() > 0)
			{
				javaHome = new File(filePath);
				try {
					runSetup(javaHome.getAbsolutePath());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		if (evt.getSource()==browse)
		{
			//	open a File dialog
			Point p = path.getLocationOnScreen();
			FileDialog fd = new FileDialog((Frame)null,"Search File",FileDialog.LOAD);
			fd.setLocation(p.x-20,p.y-20);
			fd.setFilenameFilter(this);
			fd.setDirectory(path.getText());
			fd.show();
			path.setText(fd.getDirectory()+fd.getFile());
			fd.hide();
		}
	}
	
	public boolean accept(File file, String name)
	{
		if (os.startsWith("Windows"))
			return file.isDirectory() || name.equalsIgnoreCase("java.exe");
		else
			return file.isDirectory() || name.equalsIgnoreCase("java");
	}
	
	public static void main(String[] args)
	{
		
//		System.out.println(System.getProperties().toString());
		try {
			new Bootstrap().run();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}
