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
import de.jose.Command;
import de.jose.CommandListener;
import de.jose.Language;
import de.jose.pgn.Game;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Peter Schäfer
 */

public class WriteModePopup
		extends JPopupMenu
		implements PopupMenuListener, ActionListener
{
	protected CommandListener receiver;
	protected Command cmd;

	public WriteModePopup()
	{
		super(Language.get("popup.write.mode"));

		JMenuItem item1 = new JMenuItem(Language.get("write.mode.new.line"));
		item1.setActionCommand(String.valueOf(Game.NEW_LINE));

		JMenuItem item2 = new JMenuItem(Language.get("write.mode.new.main.line"));
		item2.setActionCommand(String.valueOf(Game.NEW_MAIN_LINE));

		JMenuItem item3 = new JMenuItem(Language.get("write.mode.overwrite"));
		item3.setActionCommand(String.valueOf(Game.OVERWRITE));

		add(item1);
		add(item2);
		add(item3);

		JoMenuBar.addMenuItemListener(this, this);
		addPopupMenuListener(this);
	}

	public void show(Component parent, CommandListener listener, Point where, Command command)
	{
		receiver = listener;
		cmd = command;
		super.show(parent, where.x,where.y);
	}

	/**
	 * implements ActionListener
	 * @param e
	 */
	public void actionPerformed (ActionEvent e)
	{
		String command = e.getActionCommand();
		int writeMode = Integer.parseInt(command);

		switch (writeMode)
		{
		case Game.NEW_LINE:
		case Game.NEW_MAIN_LINE:
		case Game.OVERWRITE:
			cmd.moreData = new Integer(writeMode);
			Application.theCommandDispatcher.forward(cmd,receiver);
			break;
		}
	}

	public void popupMenuWillBecomeVisible (PopupMenuEvent e)
	{	}

	public void popupMenuWillBecomeInvisible (PopupMenuEvent e)
	{	}

	public void popupMenuCanceled (PopupMenuEvent e)
	{	}
}
