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
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * a read only text area
 * that inherits its UI defaults from JLabel
 * (= a JLabel with line breaks)
 *
 * @author Peter Schäfer
 */

public class JoBigLabel
		extends JTextArea
{

    public JoBigLabel(String text)
    {
        this(text,10,40);
    }


    public JoBigLabel(String text, int rows, int columns)
	{
		super(text,rows,columns);
		setEditable(false);
		setLineWrap(true);
		setWrapStyleWord(true);
		setTabSize(4);
		setName(text);
		updateUI();
	}
	
	public void updateUI() {
		super.updateUI();
		
		JLabel uimodel = new JLabel();
		setFont(uimodel.getFont());
		setForeground(uimodel.getForeground());
		setBackground(uimodel.getBackground());
	}

	public int setPreferredHeight(int preferredWidth)
	{
		Insets ins = getInsets();
		setSize(preferredWidth, Integer.MAX_VALUE);
		Rectangle r;
		try {
			r = modelToView(getDocument().getLength());
		} catch (BadLocationException blex) {
			return 0;
		}
		if (ins!=null)
			setSize(preferredWidth+ins.right, r.y+r.height+ins.bottom);
		else
			setSize(preferredWidth, r.y+r.height);
		return getHeight();
	}
}