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
import java.util.HashMap;

/**
 * an input field for integer values
 *
 * @author Peter Schäfer
 */

public class JIntegerField
		extends JTextField
        implements ValueHolder
{
	/**	some values can be displayed as text	*/
	protected HashMap numToText = new HashMap();
	protected HashMap textToNum = new HashMap();

	public void setTextValue(int num, String text)
	{
		Integer n = new Integer(num);
		numToText.put(n,text);
		textToNum.put(text.toLowerCase(),n);
	}

	public void setValue(Object value)
	{
		String textValue = (String)numToText.get(value);
		if (textValue!=null)
			setText(textValue);
		else if (value==null)
			setText("");
		else
			setText(String.valueOf(((Number)value).intValue()));
	}

	public Object getValue() throws NumberFormatException
	{
		String text = getText();
		Number num = (Number)textToNum.get(text.toLowerCase());
		if (num!=null)
			return num;
		else if (text.length()==0)
			return null;
		else
			return new Integer(text);
	}

}
