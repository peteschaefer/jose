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

package de.jose.task;

import de.jose.Command;
import de.jose.CommandAction;
import de.jose.Language;
import de.jose.Util;
import de.jose.util.StringUtil;
import de.jose.view.input.JoBigLabel;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

public class ProgressDialog
		extends JoDialog
{
	/**	associated task	 */
	protected Task task;
	/**	polls the progress in regular intervals (optional)	*/
	protected Timer pollTimer;

	protected static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public ProgressDialog(String name, Task aTask)
	{
		super(name,false);
		task = aTask;
		pollTimer = null;

		frame.setSize(400,160);
		center(400,160);
		((JFrame)frame).setResizable(false);

		getElementPane().add(new JoBigLabel(""), ELEMENT_WIDTH100);			//	info text
		setText(aTask.progressText);

		getElementPane().add(new JProgressBar(-1,100), ELEMENT_WIDTH100);	//	progress bar
		getProgressBar().setSize(380,20);

		getElementPane().add(new JoBigLabel(""), ELEMENT_WIDTH100);			//	remaining time

		addButton("dialog.button.cancel");

		//	we don't want no timezone shift in the time display
		TIME_FORMAT.setCalendar(Util.UTC_CALENDAR);
	}

	protected JoBigLabel getLabel()
	{
		return (JoBigLabel)getElementPane().getComponent(0);
	}

	protected JoBigLabel getRemainingLabel()
	{
		return (JoBigLabel)getElementPane().getComponent(2);
	}

	protected JProgressBar getProgressBar()
	{
		return (JProgressBar)getElementPane().getComponent(1);
	}

	public void setText(String text)
	{
		getLabel().setText(Language.get(text));
	}

	public void setRemaining(long millis)
	{
		JoBigLabel lab = getRemainingLabel();
		if (millis<=0)
			lab.setText("");
		else {
			HashMap pmap = new HashMap();
			pmap.put("time", TIME_FORMAT.format(new Date(millis)));
			String text = StringUtil.replace(Language.get("dialog.progress.time"),pmap);
			lab.setText(text);
		}
	}

	public void showError(String text)
	{
		getLabel().setText(text);
		getProgressBar().setVisible(false);

		getButtonPane().removeAll();
		addButton("dialog.button.close");
	}

	public synchronized void updateBar()
	{
		double progress = task.getProgress();
		JProgressBar bar = getProgressBar();
		int intValue;

		if (progress < 0.0)
			intValue = -1;
		else
			intValue = (int)Math.round(progress*100);

		boolean indeterminate = (intValue < 0);
		if (indeterminate != bar.isIndeterminate()) {
			bar.setIndeterminate(indeterminate);
		}
		else if (intValue != bar.getValue())
		{
			bar.setValue(intValue);

			String str = task.getProgressText();
			if (str==null && progress < 0.0) str = " ... ";

			if (str!=null) {
				bar.setString(str);
				bar.setStringPainted(true);
			}
			else
				bar.setStringPainted(false);

			//	estimate remaining time
			if (progress <= 0.0 || progress >= 1.0)
				setRemaining(0);
			else {
				long consumed = System.currentTimeMillis() - task.getStartTime();
				if (consumed >= 2000)
					setRemaining((long)(consumed * (1.0-progress) / progress));
				else
					setRemaining(0);
			}
			bar.invalidate();
		}
	}

	public void startPoll(int delay)
	{
		if (pollTimer==null) {
			pollTimer = new Timer(delay,this);
			pollTimer.setCoalesce(true);
			pollTimer.setRepeats(true);

		}
		else
			pollTimer.setDelay(delay);

		if (!pollTimer.isRunning())
			pollTimer.start();
	}

	public void stopPoll()
	{
		try {
			if (pollTimer!=null) 	pollTimer.stop();
		} finally {
			pollTimer = null;
		}
	}

	public void setVisible(boolean b)
	{
		if (!b && pollTimer!=null && pollTimer.isRunning())
			pollTimer.stop();
		frame.setVisible(b);
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource()==pollTimer)
			e = new ActionEvent(e.getSource(),e.getID(),"poll.progress", e.getWhen(),e.getModifiers());
		super.actionPerformed(e);
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) throws SQLException
			{
				task.requestAbort();
			}
		};
		map.put("dialog.button.cancel", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				frame.dispose();
			}
		};
		map.put("dialog.button.close", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				updateBar();
			}
		};
		map.put("poll.progress",action);

	}

}
