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

import de.jose.Language;
import de.jose.pgn.Game;

import javax.swing.*;

/**
 *
 * @author Peter Schäfer
 */

public class WriteModeList
		extends JComboBox
        implements ValueHolder
{
	protected static WriteModeEntry[] ITEMS = {
		new WriteModeEntry(Game.ASK, 			"write.mode.ask"),
		new WriteModeEntry(Game.NEW_LINE, 		"write.mode.new.line"),
		new WriteModeEntry(Game.NEW_MAIN_LINE, 	"write.mode.new.main.line"),
		new WriteModeEntry(Game.OVERWRITE, 		"write.mode.overwrite"),
	};

	public WriteModeList()
	{
		super(ITEMS);
	}

	public void setWriteMode(int writeMode)
	{
		for (int i=0; i<ITEMS.length; i++)
			if (ITEMS[i].mode==writeMode)
				setSelectedItem(ITEMS[i]);
	}

	public void setWriteMode(Number writeMode)
	{
		setWriteMode((writeMode==null) ? Game.ASK : writeMode.intValue());
	}

	public int getWriteMode()
	{
		return ((WriteModeEntry)getSelectedItem()).mode;
	}

	//  implements ValueHolder
	public Object getValue()                { return new Integer(getWriteMode()); }

	public void setValue(Object value)      { setWriteMode((Number)value); }

	
	static class WriteModeEntry {
		int mode;
		String name;

		WriteModeEntry (int val, String nm) { mode = val; name = nm; }
		public String toString()			{ return Language.get(name); }
	}
}
