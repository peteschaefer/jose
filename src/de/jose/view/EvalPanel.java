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
import de.jose.plugin.Score;
import de.jose.profile.LayoutProfile;
import de.jose.Application;
import de.jose.CommandAction;
import de.jose.Command;
import de.jose.Util;
import de.jose.pgn.Game;
import de.jose.plugin.EnginePlugin;

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


	public void setupActionMap(Map map)
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
			}
		};
		map.put("move.notify", action);

		action = new CommandAction() {
			public void Do(Command cmd)
			{
				Score score = (Score) cmd.data;
				MoveNode mvnd = null;
				int ply = 0;
				if (cmd.moreData instanceof Integer)
					ply = (Integer)cmd.moreData;
				if (cmd.moreData instanceof MoveNode) {
					mvnd = (MoveNode) cmd.moreData;
					ply = mvnd.getPly();
				}

				view.updateValue(ply, mvnd, score);
			}
		};
		map.put("move.value", action);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			view.setGame(Application.theHistory.getCurrent());
	}

}
