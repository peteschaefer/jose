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
import de.jose.Application;
import de.jose.profile.LayoutProfile;
import de.jose.task.DBSelectionModel;
import de.jose.view.list.IDBTableModel;

import java.util.Map;

/**
 * a Table that displays results of a database query
 * 
 */

public abstract class DBTable
		extends JoTable
{
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	the table data model	 */
	protected IDBTableModel model;

    protected DBSelectionModel currentSelection;
    protected DBSelectionModel completeResult;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	public DBTable(LayoutProfile profile, boolean alwaysScroll, boolean withContextMenu, boolean withBorder)
	{
		super(profile,alwaysScroll,withContextMenu,withBorder);
        currentSelection = new CurrentSelection();
        completeResult = new CompleteResult();
	}


    public DBSelectionModel getCurrentSelection()       { return currentSelection; }

    public DBSelectionModel getCompleteResult()         { return completeResult; }


	public void close()
		throws Exception
	{
		if (model!=null) model.close(true);
	}

	public IDBTableModel getModel()
	{
		return model;
	}


	public abstract IDBTableModel createModel(String identifier)
		throws Exception;

    public void initModel()
        throws Exception
    {
        model = createModel(getName());
        setModel(model);
        model.fireTableStructureChanged();
    }

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				if (cmd.target!=Application.theApplication)
					Application.theCommandDispatcher.forward(cmd, Application.theApplication);
				else
					try { close(); } catch (Exception ex) { /* can't help it */ }				
			}
		};
		map.put("menu.file.quit", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	update column headers and (possibly) contents
				if (model!=null) {
					model.fireTableDataChanged();
					model.fireTableStructureChanged();	// doesn't seem to update header columns; why ?
				}
			}
		};
		map.put("update.language", action);
	}

	//-------------------------------------------------------------------------------
	//	interface DBSelectionModel
	//-------------------------------------------------------------------------------

    class CurrentSelection implements DBSelectionModel
    {
        public int getMinSelectionIndex() {
            return table.getSelectionModel().getMinSelectionIndex();
        }

        public int getMaxSelectionIndex() {
            return table.getSelectionModel().getMaxSelectionIndex();
        }

        public boolean hasSelection() {
            return !table.getSelectionModel().isSelectionEmpty();
        }

        public boolean isSelectedIndex(int index) {
            return table.getSelectionModel().isSelectedIndex(index);
        }

        public int getDBId(int index) {
            if (model!=null)
                return model.getDBId(index);
            else
                return -1;
        }
    }

    class CompleteResult implements DBSelectionModel
    {
        public int getDBId(int index) {
            if (model!=null)
                return model.getDBId(index);
            else
                return -1;
        }

        public int getMaxSelectionIndex() {
            if (hasSelection())
                return model.getRowCount()-1;
            else
                return -1;
        }

        public int getMinSelectionIndex() {
            if (hasSelection())
                return 0;
            else
                return -1;
        }

        public boolean hasSelection() {
            return (model!=null) && model.getRowCount() > 0;
        }

        public boolean isSelectedIndex(int index) {
            return true;
        }
    }

}
