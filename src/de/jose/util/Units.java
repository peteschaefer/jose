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

package de.jose.util;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.ParsePosition;

/**
 * @author Peter Schäfer
 */

public class Units
{
	public static class UnitPopup extends JComboBox
	{
		public UnitPopup() {
			super(STRINGS);
			setEditable(true);
		}

		public int getUnit() {
			String str = getSelectedItem().toString();
			return Units.toUnit(str);
		}

		public void setUnit(int unit) {
			setSelectedItem(Units.toString(unit));
		}
	}

	public static final int INCH          = 0;
	public static final int POINT         = 1;

	public static final int MILLIMETER    = 2;
	public static final int CENTIMETER    = 3;

	public static final String[] STRINGS  = { "in", "pt", "mm", "cm", };

	protected static final double[][] MATRIX = {
						/* in */    /* pt */    /* mm */    /* cm */
/* in to:   */		{   1.0,        72,         25.4,       2.54,        },
/* pt to:   */		{   0,          1.0,        0.35278,    0.035278,     },
/* mm to:   */		{   0,          0,          1.0,        0.1,          },
/* cm to:   */		{   0,          0,          0,          1.0,         },
	};

	/** print format    */
	protected static DecimalFormat outputFormat = new DecimalFormat("###0.##");

	public static int toUnit(String str) {
		for (int i=0; i<STRINGS.length; i++)
			if (STRINGS[i].equalsIgnoreCase(str)) return i;
		return -1;
	}

	public static String toString(int unit) {
		if (unit >= 0 && unit < STRINGS.length)
			return STRINGS[unit];
		else
			return null;
	}

	public static double convert(double value, String from, String to)
	{
		return convert(value,toUnit(from),toUnit(to));
	}

	public static double convert(double value, int from, int to)
	{
		if (from==to)
			return value;
		else if (from < to)
			return value * MATRIX[from][to];
		else
			return value / MATRIX[to][from];
	}

	public static String toString(double value)
	{
		return outputFormat.format(value);
	}

	public static double parse(String str)
	{
		ParsePosition pp = new ParsePosition(0);
		Number n = outputFormat.parse(str,pp);
		if (n==null)
			return 0.0;
		else
			return n.doubleValue();
	}

}
