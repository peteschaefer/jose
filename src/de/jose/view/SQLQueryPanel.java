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

package de.jose.view;

import de.jose.*;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class SQLQueryPanel
		extends JoPanel
		implements IDBPanel
{
	/**	contains the query text	 */
	protected JTextArea input;
	/**	popup with available data sources	 */
	protected JComboBox dataSources;
	
	public SQLQueryPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		
		setLayout(new BorderLayout());
		input = new JTextArea();
		input.setName("sqlquery.text");
		input.setLineWrap(true);
		input.setWrapStyleWord(true);
		input.setText(AbstractApplication.theUserProfile.getString("sqlquery.text"));
		
		JScrollPane scroller = new JScrollPane(input, 
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroller, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		dataSources = new JComboBox(Application.theApplication.theConfig.getAllDataSourceNames());
		dataSources.setSelectedItem(Application.theApplication.theDatabaseId);
		buttonPane.add(dataSources);
		
		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(80,1));
		buttonPane.add(spacer);
		
		JButton button = new JButton();
		button.setName("dialog.button.ok");
		button.setActionCommand("sqlquery.submit");
		button.addActionListener(this);
		button.setText(Language.get("dialog.button.ok"));
		button.setToolTipText(Language.getTip("dialog.button.ok"));
		buttonPane.add(button);
		
		spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(80,1));
		buttonPane.add(spacer);
		
		button = new JButton();
		button.setName("dialog.button.commit");
		button.setActionCommand("sqlquery.commit");
		button.addActionListener(this);
		button.setText(Language.get("dialog.button.commit"));
		button.setToolTipText(Language.getTip("dialog.button.commit"));
		buttonPane.add(button);
		
		button = new JButton();
		button.setName("dialog.button.rollback");
		button.setActionCommand("sqlquery.rollback");
		button.addActionListener(this);
		button.setText(Language.get("dialog.button.rollback"));
		button.setToolTipText(Language.getTip("dialog.button.rollback"));
		buttonPane.add(button);
		
		add(buttonPane, BorderLayout.SOUTH);
	}
	
	public String getQueryText()		{ return input.getText(); }
	
	public String getDataSource()		{ return (String)dataSources.getSelectedItem(); }
	
	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				//	forward command to result panel
				cmd.data = this;
				SQLResultPanel result = (SQLResultPanel)JoPanel.get("window.sqllist");
				AbstractApplication.theCommandDispatcher.forward(cmd,result);
			}
		};
		map.put("sqlquery.submit", action);
		map.put("sqlquery.commit", action);
		map.put("sqlquery.rollback", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				UserProfile upf = (UserProfile)cmd.data;
				upf.set("sqlquery.text", input.getText());
			}
		};
		map.put("update.user.profile", action);
	}
}

