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
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

/**
 * an attempt to write a Spinner input field for Time values
 * however, Sun's JSpinner code is so contracted that it is almost impossible
 * to overwite it ;-(
 *
 * @author Peter Schäfer
 * @deprecated
 */

public class JTimeField2
		extends JPanel
{
	private static final Integer ZERO = new Integer(0);

	protected Calendar calendar;
	protected JSpinner hour;
	protected JSpinner minute;
	protected JSpinner second;

	public JTimeField2(String name)
	{
		super();
		setName(name);
		calendar = (Calendar)Util.UTC_CALENDAR.clone();

		SpinnerNumberModel model = new SpinnerNumberModel(0,0,99,+1);
		hour = new JSpinner(model);
		hour.setEditor(new JSpinner.NumberEditor(hour,"#0"));

		model = new SpinnerNumberModel(0,0,59,+1);
		minute = new JSpinner(model);
		minute.setEditor(new JSpinner.NumberEditor(hour,"00"));

		model = new SpinnerNumberModel(0,0,59,+1);
		second = new JSpinner(model);
		second.setEditor(new JSpinner.NumberEditor(hour,"00"));

		Dimension dim = new Dimension(40,20);
		hour.setPreferredSize(dim);
		minute.setPreferredSize(dim);
		second.setPreferredSize(dim);

		add(hour);
		add(new JLabel(":"));
		add(minute);
		add(new JLabel(":"));
		add(second);
	}

	public void setValue(Date date)
	{
		if (date==null) {
			hour.setValue(ZERO);
			minute.setValue(ZERO);
			second.setValue(ZERO);
		}
		else {
			calendar.setTime(date);
			hour.setValue(new Integer(calendar.get(Calendar.HOUR_OF_DAY)));
			minute.setValue(new Integer(calendar.get(Calendar.MINUTE)));
			second.setValue(new Integer(calendar.get(Calendar.SECOND)));
		}
	}

	public Date getValue()
	{
		int h = ((Number)hour.getValue()).intValue();
		int m = ((Number)minute.getValue()).intValue();
		int s = ((Number)second.getValue()).intValue();

		if (h==0 && m==0 && s==0)
			return null;
		else {
			calendar.clear();
			calendar.set(Calendar.HOUR_OF_DAY, h);
			calendar.set(Calendar.MINUTE, m);
			calendar.set(Calendar.SECOND, s);
			return calendar.getTime();
		}
	}
}
