/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view;

import de.jose.pgn.MoveNode;
import de.jose.profile.LayoutProfile;
import de.jose.Application;
import de.jose.comm.CommandAction;
import de.jose.comm.Command;
import de.jose.Util;
import de.jose.pgn.Game;

import javax.swing.*;
import java.util.Map;
import java.awt.*;

/**
 * @author Peter Sch�fer
 */

public class EvalPanel
        extends JoPanel
{
	protected EvalView view;

	public EvalPanel(LayoutProfile prf, boolean witContextMenu, boolean withBorder)
	{
		super(prf, witContextMenu, withBorder);
		titlePriority = 7;
		view = new EvalView();
	}

	public void init()
	{
		JScrollPane scroller = new JScrollPane(view,
		        JScrollPane.VERTICAL_SCROLLBAR_NEVER,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		setLayout(new BorderLayout());
		add(scroller,BorderLayout.CENTER);

		view.setGame(Application.theHistory.getCurrent());
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				view.setGame((Game)cmd.data);
			}
		};
		map.put("switch.game", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//  adjust array, if necessary !
				boolean destructive = Util.toboolean(cmd.moreData);
				if (destructive) view.updateGame();

				view.updateValue(view.game.getCurrentMove());
			}
		};
		map.put("move.notify", action);

		action = new CommandAction() {
			public void Do(Command cmd)
			{
				MoveNode mvnd = null;
				if (cmd.moreData instanceof MoveNode)
					mvnd = (MoveNode) cmd.moreData;

				view.updateValue(mvnd);
			}
		};
		map.put("move.value", action);

		action = new CommandAction() {
			public void Do(Command cmd)
			{
				boolean dark = (Boolean)cmd.moreData;
				view.setBgColor(dark);
			}
		};
		map.put("update.ui", action);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			view.setGame(Application.theHistory.getCurrent());
	}

}
