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

import de.jose.pgn.PgnDate;
import de.jose.pgn.SearchRecord;

/**
 * an input field for PGN Dates
 * acccepts "??" as input
 *
 * @author Peter Schäfer
 */

public class PgnDateField
		extends JDateField
{

	public void setValue(Object date)
	{
		if (date instanceof PgnDate)
			setText(((PgnDate)date).toLocalDateString(false));
		else
			super.setValue(date);
	}

	public Object getValue() throws DateFormatException
	{
		String text = getText();
		return SearchRecord.parseDate(text,null,SearchRecord.LOWER_BOUND);
	}
}
