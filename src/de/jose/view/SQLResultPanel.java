/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.view;

import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.db.JoConnection;
import de.jose.profile.LayoutProfile;
import de.jose.view.list.CachedResultModel;
import de.jose.view.list.IDBTableModel;

import javax.swing.*;
import java.awt.*;
import java.sql.Types;
import java.util.Map;

/**
 * this panel shows results from an SQL query
 * 
 */

public class SQLResultPanel
		extends DBTable
		implements IDBPanel
{
	String currentSql;
	
	public SQLResultPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
		throws Exception
	{
		super(profile,true,withContextMenu,withBorder);
	}
	
	/**	called when the panel is show for the first time	 */
	public void init()
		throws Exception
	{
        super.init();

//		conn = JoConnection.get("db.sqlquery");
//		conn.setAutoCommit(false);
		
//		model = resultModel = new DBTableModel(conn.getAdapter());
		
/*		stm = new JoStatement(conn, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet res = stm.executeQuery(" ? ");
		
		DBTableModel model = new DBTableModel(conn.getAdapter());
		model.setResult(res);
		
		setModel(model);
*/	}
	
	protected void submit(String dataSource, String sql)
	{
		try {
			
			if (model != null)
				model.close(false);

			currentSql = sql;
		
			model.open();
			
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
		} catch (Exception ex) {
			try { close(); } catch (Exception ex2) { /* can't help it */ }
			//setText("Error", ex.getLocalizedMessage());
		}
	}
	
	public IDBTableModel createModel(String identifier)
		throws Exception
	{
		/*	set up a database connection and list model	*/
		JoConnection conn = JoConnection.get();
		conn.setAutoCommit(false);

//		JoPreparedStatement pstm = conn.getPreparedStatement(currentSql);
        CachedResultModel model = new CachedResultModel(identifier, CachedResultModel.STORE_DISK, 40);
        model.open(currentSql);

		return model;
	}
	
	protected void commit()
	{
		try {
			((CachedResultModel)model).commit();
			model.singleCell("TRANSACTION", "Transaction Committed", Types.VARCHAR);
		} catch (Exception ex) {
			model.singleCell("ERROR", ex.getLocalizedMessage(), Types.VARCHAR);
		}
	}
	
	
	protected void rollback()
	{
		try {
			((CachedResultModel)model).rollback();
			model.singleCell("TRANSACTION", "Transaction Committed", Types.VARCHAR);
		} catch (Exception ex) {
			model.singleCell("ERROR", ex.getLocalizedMessage(), Types.VARCHAR);
		}
	}
	
	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				SQLQueryPanel qp = (SQLQueryPanel)cmd.data;
				submit(qp.getDataSource(), qp.getQueryText());
			}
		};
		map.put("sqlquery.submit", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				commit();
			}
		};
		map.put("sqlquery.commit", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				rollback();
			}
		};
		map.put("sqlquery.rollback", action);
	}
}
