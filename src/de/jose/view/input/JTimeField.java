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

import de.jose.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * a text input field for Time values
 *
 * @author Peter Schäfer
 */

public class JTimeField
		extends JDateField
{
	protected static Calendar calendar = (Calendar)Util.UTC_CALENDAR.clone();

	public JTimeField()
	{
		super(createFormat());
	}


	private static SimpleDateFormat[] createFormat() {
		SimpleDateFormat[] format = new SimpleDateFormat[8];
		format[0] = new SimpleDateFormat("HH:mm:ss");
		format[1] = new SimpleDateFormat("HH 'h'");
		format[2] = new SimpleDateFormat("HH:mm 'h'");
		format[3] = new SimpleDateFormat("mm 'min'");
		format[4] = new SimpleDateFormat("mm:ss 'min'");
		format[5] = new SimpleDateFormat("mm:ss");
		format[6] = new SimpleDateFormat("ss 'sec'");
		format[7] = new SimpleDateFormat("ss");

		for (int i=0; i < format.length; i++)
		{
			format[i].setCalendar(calendar);	
			format[i].setTimeZone(Util.UTC_TIMEZONE);
		}
		return format;
	}

	protected SimpleDateFormat getOutputFormat(Date date)
	{
		calendar.setTime(date);
		if (calendar.get(Calendar.HOUR_OF_DAY) > 0)
			return format[0];
		else if (calendar.get(Calendar.MINUTE) > 0)
			return format[4];
		else
			return format[6];
	}

}
