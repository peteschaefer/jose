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

import de.jose.Language;

import javax.swing.*;
import java.util.Vector;

/**
 * a popup that displays available Languages
 *
 * @author Peter Schäfer
 */

public class LanguageList
		extends JComboBox
        implements ValueHolder
{
	public LanguageList(Vector langCodes)
	{
		super(getLanguageEntries(langCodes));	
	}
	
	public String getSelectedLanguage()
	{
		LanguageEntry ety = (LanguageEntry)getSelectedItem();
		if (ety != null)
			return ety.code;
		else
			return null;
	}
	
	public void setSelectedLanguage(String code)
	{
		ListModel lm = getModel();
		for (int i=0; i<lm.getSize(); i++) {
			LanguageEntry ety = (LanguageEntry)lm.getElementAt(i);
			if (ety.code.equals(code)) {
				setSelectedIndex(i);
				return;
			}
		}
	}

	//  implements ValueHolder

	public Object getValue()                { return getSelectedLanguage(); }

	public void setValue(Object value)      { setSelectedLanguage((String)value); }

	//-------------------------------------------------------------------------------
	//	Private Part
	//-------------------------------------------------------------------------------
	
	private static Vector getLanguageEntries(Vector langCodes)
	{
		Vector result = new Vector();
		for (int i=0; i < langCodes.size(); i++)
			result.add(new LanguageEntry((String)langCodes.get(i)));
		return result;
	}
	
	private static class LanguageEntry
	{
		String code;
		
		LanguageEntry(String cd)	{ code = cd; }
		public String toString()	{ return Language.get("lang."+code); }
	}
}
