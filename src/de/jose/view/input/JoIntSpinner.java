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

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * an attempt to write a Spinner input field for Time values
 * however, Sun's JSpinner code is so contracted that it is almost impossible
 * to overwite it ;-(
 *
 * @author Peter Schäfer
 * @deprecated
 */
public class JoIntSpinner
		extends JSpinner        
{
	protected static final String DEFAULT_FORMAT = "######0";

	public JoIntSpinner()
	{
		this(Integer.MIN_VALUE,+1,Integer.MAX_VALUE, DEFAULT_FORMAT);
	}

	public JoIntSpinner(int min, int increment, int max, String formatPattern)
	{
		super(new JoSpinnerIntModel(min,increment,max));
		DecimalFormat format = new DecimalFormat(formatPattern);
		NumberEditor editor = new NumberEditor(this,formatPattern);
		JoIntFormatter formatter = new JoIntFormatter(format);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
		editor.getTextField().setFormatterFactory(factory);
		setEditor(editor);
	}

	public void setIntValue(int value) {
		setValue(new Integer(value));
	}

	public int getIntValue() {
		return getIntValue(Integer.MIN_VALUE);
	}

	public int getIntValue(int emptyValue) {
		Number num = (Number)getValue();
		return (num!=null) ? num.intValue():emptyValue;
	}

	public void setEmpty() {
		setValue(null);
	}

	public boolean isEmpty() {
		return getValue()==null;
	}

	public void setLoop(boolean loopValues) {
		((JoSpinnerIntModel)getModel()).setLoop(loopValues);
	}

	static class JoSpinnerIntModel extends SpinnerNumberModel
	{
		protected boolean empty;
		protected int value;

		protected int minValue, maxValue;
		protected int increment;
		protected boolean loop;

		JoSpinnerIntModel(int min, int inc, int max)
		{
			empty = false;
			value = 0;
			minValue = min;
			increment = inc;
			maxValue = max;
			loop = false;
		}

		public Object getValue()
		{
			if (empty)
				return null;
			else
				return new Integer(value);
		}

		public void setValue(Object val)
		{
			if (val==null)
				empty = true;
			else {
				empty = false;
				value = ((Number)val).intValue();
			}
		}

		public void setLoop(boolean loopValues) {
			loop = loopValues;
		}

		public Object getNextValue() {
			if (empty)
				return new Integer(minValue);
			else {
				int next = value+increment;
				if (next > maxValue)
					next = loop ? minValue:maxValue;
				return new Integer(next);
			}
		}

		public Object getPreviousValue() {
			if (empty)
				return new Integer(maxValue);
			else {
				int previous = value-increment;
				if (previous < minValue)
					previous = loop ? maxValue:minValue;
				return new Integer(previous);
			}
		}
	}

	static class JoIntFormatter extends NumberFormatter
	{
		public JoIntFormatter(DecimalFormat format)
		{
			super(format);
			super.setValueClass(Integer.class);
			setCommitsOnValidEdit(true);
			setAllowsInvalid(false);
		}

		public Object stringToValue(String string) throws ParseException
		{
			if (string.length()==0)
				return null;
			else
				return super.stringToValue(string);
		}

		public String valueToString(Object value) throws ParseException
		{
			if (value==null)
				return "";
			else
				return super.valueToString(value);
		}
	}
}
