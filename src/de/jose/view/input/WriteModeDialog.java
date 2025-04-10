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

package de.jose.view.input;

import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.Version;
import de.jose.pgn.Game;
import de.jose.view.ViewUtil;
import de.jose.window.JoDialog;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 *
 * @author Peter Sch�fer
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
            frame.setSize(200,200);   //  optimized for Aqua L&F
        else
		    frame.setSize(200,200);   //  optimized for Metouia L&F
		frame.setUndecorated(true);

		Font font = frame.getFont();
		Font bfont = font.deriveFont(Font.BOLD,16);
		frame.setFont(font);

		Dimension butSize = new Dimension(160,36);

		JPanel pane = getButtonPane();
		pane.setLayout(new GridLayout(5,1));
//		pane.setBorder(new EmptyBorder(4,4,4,4));
		frame.getRootPane().setBorder(new BevelBorder(BevelBorder.RAISED));
		pane.setBorder(new EmptyBorder(8,8,8,8));

		JButton button;
		button = addButton("write.mode.new.line");
		button.putClientProperty("value",Game.NEW_LINE);
		button.setFont(bfont);
		button.setPreferredSize(butSize);
		button.setMnemonic('N');
		frame.getRootPane().setDefaultButton(button);

		button = addButton("write.mode.new.main.line");
		button.putClientProperty("value",Game.NEW_MAIN_LINE);
		button.setFont(bfont);
		button.setPreferredSize(butSize);
		button.setMnemonic('M');
		button.setSize(butSize);

		button = addButton("write.mode.overwrite");
		button.putClientProperty("value",Game.OVERWRITE);
		button.setFont(bfont);
		button.setPreferredSize(butSize);
		button.setMnemonic('O');
		button.setSize(butSize);

		button = addButton("write.mode.cancel");
		button.putClientProperty("value",Game.CANCEL);
		button.setFont(bfont);
		button.setMnemonic('C');
		//button.setMnemonic(KeyEvent.VK_ESCAPE);
		button.setPreferredSize(butSize);

		dontAsk		= newCheckBox("write.mode.dont.ask");
		pane.add(dontAsk);
		dontAsk.setFont(font);

		frame.getContentPane().remove(elementPane);

		frame.getRootPane().registerKeyboardAction(e -> {
			result = Game.CANCEL;
			hide();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
	}

	public void fitInto(Point locationOnScreen, Component board)
	{
		//	global bounds of board panel
		Rectangle bbounds = ViewUtil.boundsOnScreen(board);
		//	global bounds of popup dialog
		Rectangle dbounds = this.getBounds();
		dbounds.x = locationOnScreen.x;
		dbounds.y = locationOnScreen.y;
		ViewUtil.fitInto(dbounds,bbounds);
		this.setLocation(new Point(dbounds.x,dbounds.y));
	}


	public void setupActionMap (Map<String, CommandAction> map)
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
