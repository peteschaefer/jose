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
import de.jose.Util;
import de.jose.chess.MoveFormatter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Peter Schäfer
 */

public class MoveFormatList
		extends JComboBox
        implements ValueHolder
{
	protected static MoveFormatEntry[] ITEMS = {
		new MoveFormatEntry(MoveFormatter.SHORT, 			"move.format.short"),
		new MoveFormatEntry(MoveFormatter.LONG, 			"move.format.long"),
		new MoveFormatEntry(MoveFormatter.ALGEBRAIC, 		"move.format.algebraic"),
		new MoveFormatEntry(MoveFormatter.CORRESPONDENCE, 	"move.format.correspondence"),
		new MoveFormatEntry(MoveFormatter.ENGLISH, 			"move.format.english"),
		new MoveFormatEntry(MoveFormatter.TELEGRAPHIC, 		"move.format.telegraphic"),
	};

	public static Collection createMenu(String title, int currentValue)
	{
		ArrayList result = new ArrayList();
		result.add(title);
		for (int i=0; i<ITEMS.length; i++) {
			result.add(Util.toBoolean(ITEMS[i].format==currentValue));
			result.add(ITEMS[i].name);
		}
		return result;
	}

	public static int toConst(String title)
	{
		for (int i=0; i < ITEMS.length; i++)
			if (ITEMS[i].name.equals(title))
				return ITEMS[i].format;
		return ITEMS[0].format;
	}



	public MoveFormatList()
	{
		super(ITEMS);
	}

	public void setFormat(int format)
	{
		for (int i=0; i<ITEMS.length; i++)
			if (ITEMS[i].format==format)
				setSelectedItem(ITEMS[i]);
	}

	public void setFormat(Number format)
	{
		setFormat((format==null) ? MoveFormatter.SHORT : format.intValue());
	}

	public int getFormat()
	{
		return ((MoveFormatEntry)getSelectedItem()).format;
	}

	public Object getValue()                { return new Integer(getFormat()); }

	public void setValue(Object value)      { setFormat((Number)value); }

	static class MoveFormatEntry {
		int format;
		String name;

		MoveFormatEntry (int val, String nm) 	{ format = val; name = nm; }
		public String toString()				{ return Language.get(name); }
	}
}
