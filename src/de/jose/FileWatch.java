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

import de.jose.window.JoFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author Peter Schäfer
 */

public class FileWatch implements ActionListener
{
	protected static final int 	SLEEP_TIME	= 10000;

	protected Timer				timer;

	protected RandomAccessFile	file;
	protected long				lastAccess;

	protected String			errorMessage;
	protected boolean			done;

	public FileWatch(File file, String message) throws IOException
	{
		this.file = new RandomAccessFile(file,"rwd");
		//	rwd = immediately write to storage device
		this.errorMessage = message;

		timer = new Timer(SLEEP_TIME,this);
		timer.setRepeats(true);
		timer.setCoalesce(true);
		timer.start();

		write();
	}

	/**	Timer callback	*/
	public void actionPerformed(ActionEvent e)
	{
		try {
			if (! test())
				showError();
			else
				write();
		} catch (IOException e1) {
			Application.error(e1);
		}

	}

	public void finish()
	{
		try {
			file.close();
		} catch (IOException e) {
			Application.error(e);
		}
		timer.stop();
	}

	protected void write() throws IOException
	{
		file.seek(0L);
		file.writeLong(lastAccess = System.currentTimeMillis());
	}

	protected boolean test() throws IOException
	{
		file.seek(0L);
		long time = file.readLong();
		return time <= lastAccess;
	}

	protected void showError()
	{
		JOptionPane opane = new JOptionPane(Language.get(errorMessage), JOptionPane.ERROR_MESSAGE);
		JDialog dlg = opane.createDialog(JoFrame.theActiveFrame, Language.get("dialog.error.title"));
        SplashScreen.close();
		dlg.show();
	}

}
