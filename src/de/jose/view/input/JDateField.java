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

import de.jose.Util;

import javax.swing.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * an input field for Dates
 *
 * @author Peter Schäfer
 */

public class JDateField
		extends JTextField
        implements ValueHolder
{
	/**	array of accepted formats	*/
	protected SimpleDateFormat[] format;

	public static class DateFormatException extends RuntimeException {
		public DateFormatException(String message) {
			super(message);
		}
	}

	protected JDateField(SimpleDateFormat format)
	{
		this(new SimpleDateFormat[] { format });
	}

	protected JDateField(SimpleDateFormat[] format)
	{
		super();
		setColumns(12);
		this.format = format;
	}

	public JDateField()
	{
		this(createFormat());
	}

	public void setValue(Object date)
	{
		if (date==null)
			setText("");
		else {
			SimpleDateFormat f = getOutputFormat((Date)date);
			setText(f.format(date));
		}
	}

	public Object getValue() throws DateFormatException
	{
		String text = getText();
		if (text.length()==0)
			return null;

		DateFormatException result = null;
		for (int i=0; i < format.length; i++)
			try {
				return format[i].parse(text);
			} catch (ParseException e) {
				if (result==null) result = new DateFormatException(text);
			}
		throw result;
	}

	protected SimpleDateFormat getOutputFormat(Date date)
	{
		return format[0];
	}
	
	private static SimpleDateFormat createFormat() {
		SimpleDateFormat format = (SimpleDateFormat)DateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		format.setCalendar(Util.UTC_CALENDAR);
		format.setTimeZone(Util.UTC_TIMEZONE);
		return format;
	}
}
