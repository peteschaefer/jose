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

import de.jose.Command;
import de.jose.CommandAction;
import de.jose.Version;
import de.jose.pgn.Game;
import de.jose.window.JoDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

/**
 *
 * @author Peter Schäfer
 */

public class WriteModeDialog
		extends JoDialog
{
	protected JCheckBox	dontAsk;
	protected int result;

	public WriteModeDialog(String name)
	{
		super(name,true);
        JDialog frame = (JDialog)this.frame;
		frame.setResizable(false);

/*		if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
			setSize(160,160);
			getRootPane().setWindowDecorationStyle(JRootPane.QUESTION_DIALOG);
		}
		else {
*/
        if (Version.mac)
            frame.setSize(160,160);   //  optimized for Aqua L&F
        else
		    frame.setSize(160,140);   //  optimized for Metouia L&F
		frame.setUndecorated(true);

		JPanel pane = getButtonPane();
		pane.setLayout(new GridLayout(5,1));
//		pane.setBorder(new EmptyBorder(4,4,4,4));
		frame.getRootPane().setBorder(new BevelBorder(BevelBorder.RAISED));
		pane.setBorder(new EmptyBorder(8,8,8,8));

		JButton button;
		button = addButton("write.mode.new.line");
		button.putClientProperty("value",new Integer(Game.NEW_LINE));
		frame.getRootPane().setDefaultButton(button);

		button = addButton("write.mode.new.main.line");
		button.putClientProperty("value",new Integer(Game.NEW_MAIN_LINE));

		button = addButton("write.mode.overwrite");
		button.putClientProperty("value",new Integer(Game.OVERWRITE));

		button = addButton("write.mode.cancel");
		button.putClientProperty("value",new Integer(Game.CANCEL));

		dontAsk		= newCheckBox("write.mode.dont.ask");
		pane.add(dontAsk);

		frame.getContentPane().remove(elementPane);
	}


	public void setupActionMap (Map map)
	{
		super.setupActionMap (map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				JButton button = (JButton)cmd.data;
				Integer value = (Integer)button.getClientProperty("value");
				result = value.intValue();
				hide();
			}
		};
		map.put("write.mode.new.line",action);
		map.put("write.mode.new.main.line",action);
		map.put("write.mode.overwrite",action);
		map.put("write.mode.cancel",action);
	}

	public void show(int oldMode)
	{
		result = oldMode;
		dontAsk.setSelected(false);
		show();
	}

	public int getWriteMode()				{ return result; }

	public boolean askUser()				{ return !dontAsk.isSelected();	}
}
