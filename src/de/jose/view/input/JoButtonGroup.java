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

import javax.swing.*;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Enumeration;

/**
 * JoButtonGroup
 * a buttonGroup with a name (= common prefix of all Radio buttons)
 *
 * @author Peter Schäfer
 */
public class JoButtonGroup
	extends ButtonGroup
	implements ValueHolder
{
	protected String prefix;

	public JoButtonGroup(String prefix)
	{
		this.prefix = prefix+".";
	}

	public void add(AbstractButton b)
	{
		if (! b.getName().startsWith(prefix)) throw new IllegalArgumentException();
		super.add(b);
	}

	public void addAll(Collection elements)
	{
		Iterator i = elements.iterator();
		while (i.hasNext())
		{
			Object comp = i.next();
			if ((comp instanceof AbstractButton) &&
				((AbstractButton)comp).getName().startsWith(prefix))
			{
				i.remove();
				this.add((AbstractButton)comp);
			}
		}
	}

	public Object getValue()
	{
		ButtonModel selection = this.getSelection();
		if (selection==null) return null;

		String selected = selection.getActionCommand();
		if (selected==null) return null;

		return selected.substring(prefix.length());
	}

	public void setValue(Object value)
	{
		String svalue = prefix+value;
		Enumeration buttons = this.getElements();
		if (buttons.hasMoreElements()) {
			AbstractButton button = (AbstractButton)buttons.nextElement();
			boolean selected = (value==null) || button.getActionCommand().equals(svalue);
			this.setSelected(button.getModel(), selected);
		}
		while (buttons.hasMoreElements()) {
			AbstractButton button = (AbstractButton)buttons.nextElement();
			boolean selected = (value!=null) && button.getActionCommand().equals(svalue);
			this.setSelected(button.getModel(), selected);
		}
	}
}
