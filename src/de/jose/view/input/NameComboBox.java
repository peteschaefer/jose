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

import de.jose.Application;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.util.StringUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * an input combo box for player names
 * (not yet in use)
 *
 * @author Peter Schäfer 
 */
public class NameComboBox
		extends JComboBox
		implements PopupMenuListener, ActionListener

{
	protected ComboBoxEditor editor;

	public NameComboBox()
	{
		super();
		setEditable(true);
		addPopupMenuListener(this);
		editor = getEditor();
	}

	public String getText()
	{
		Object obj = editor.getItem();
		if (obj==null)
			return null;
		else
			return obj.toString();
	}

	public void setText(String text)
	{
		editor.setItem(text);
	}

	public List getItems(String text, int max) throws Exception
	{
		JoConnection conn = null;;
		JoPreparedStatement stm = null;
		List result = new ArrayList();

		try {
			conn = JoConnection.get();
			stm = conn.getPreparedStatement(
					"SELECT Name FROM Player" +
					" WHERE Name LIKE ?" +
					" ORDER BY Name "+
					" LIMIT "+max);
			stm.setMaxRows(max);

			stm.setString(1, text+"%");
			stm.execute();

			while (stm.next())
				result.add(stm.getString(1));

			return result;
			
		} finally {
			if (stm!=null) stm.closeResult();
			if (conn!=null) conn.release();
		}
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e)
	{
		/**	update the displayed names	*/
		String text = getText();
		removeAllItems();

		if (text!=null) {
			/**	make an SQL query	*/
			try {
				List items = getItems(text, 20);
				for (int i=0; i<items.size(); i++)
					addItem(items.get(i));
			} catch (Exception ex) {
				Application.error(ex);
			}
		}

	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	public void popupMenuCanceled(PopupMenuEvent e) {
	}
}
