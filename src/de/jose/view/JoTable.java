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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * base class for Table panels
 * 
 * contains a scrollable table
 */

public class JoTable
		extends JoPanel
        implements ListSelectionListener
{
	/**	the JTable 	 */
	protected JTable table;
	/**	the wrapping scroll pane	 */
	protected JScrollPane scroll;
	/**	user preferences	 */
	protected HashMap columnPreferences;
	
	/**	persisten column information that is stored in the User Profile	 */
	public static class ColumnInfo implements Serializable
	{
		static final long serialVersionUID = 5026315131509951426L;
		/**	view index		 */
		int viewIndex;
		/**	current width		 */
		int width;
	}
	
	public JoTable(LayoutProfile profile, boolean alwaysScroll, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		setLayout(new BorderLayout());

		table = new JTable();
        table.putClientProperty("Quaqua.Table.style", Version.getSystemProperty("Quaqua.Table.style"));

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		/*	automatic resizing is turned off - we show a scrollbar	 */
		
		table.setShowGrid(false);
		table.setToolTipText(null);
        table.setTableHeader(new JoTableHeader(table.getColumnModel()));

		scroll = new JScrollPane(table,
					alwaysScroll ? JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
								: JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (Version.mac)
            scroll.setBorder(null); //  supposed to improve L&F on Macs ?!
//		if (withWheel)
//			scroll.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, "Spinning Wheel");
		scroll.getViewport().setBackground(Color.white);
		
		add(scroll, BorderLayout.CENTER);
		
		columnPreferences = new HashMap();

		table.getSelectionModel().addListSelectionListener(this);
	}
	
	/**	called when the panel is show for the first time	 */
	public void init()
		throws Exception
	{	
		//	get user preferences
		HashMap prefs = (HashMap)AbstractApplication.theUserProfile.get(getName()+".columns");
		if (prefs != null)
			columnPreferences.putAll(prefs);
	}
	
	protected void paintComponent(Graphics g)
	{
		//	erase background
		g.setColor(getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		super.paintComponent(g);
	}

  	public void setModel(TableModel dataModel)
	{
		int oldCount = table.getRowCount();
		int newCount = dataModel.getRowCount();
		
		TableModelEvent evt;
		
		if (oldCount > 0) {
			evt = new TableModelEvent(table.getModel(), 0, oldCount-1, 
											  TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
			table.tableChanged(evt);
		}
		
		table.setModel(dataModel);
		
		if (newCount > 0) {
			evt = new TableModelEvent(dataModel, 0, newCount-1, 
											  TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
			table.tableChanged(evt);
		}
		
		TableColumnModel cmodel = table.getColumnModel();
        table.setAutoCreateColumnsFromModel(false);
        //  DON'T modify column model automatically (strange concept, isn't it ?)

        TableCellRenderer hrend = new JoHeaderCellRenderer();

        ArrayList mcolumns = new ArrayList(dataModel.getColumnCount());    //  list of TableColumn objects, sorted by model index
        int[] modelToView = new int[dataModel.getColumnCount()];   //  maps model indexes to view indexes

        for (int i=0; i < dataModel.getColumnCount(); i++) {
            TableColumn column = cmodel.getColumn(i);
            mcolumns.add(column);
            modelToView[i] = i;     //  will be rearranged later

            if (dataModel instanceof JoTableModel) {
                JoTableModel tmodel = (JoTableModel)dataModel;
                column.setIdentifier(tmodel.getIdentifier(i));
                column.setPreferredWidth(tmodel.getPreferredColumnWidth(i));
                column.setMinWidth(tmodel.getMinColumnWidth(i));
                column.setMaxWidth(tmodel.getMaxColumnWidth(i));
                column.setHeaderRenderer(hrend);
            }
        }

		//	re-size and re-order columns by view index
		for (int modelIndex=0; modelIndex < mcolumns.size(); modelIndex++) {
			TableColumn column = (TableColumn)mcolumns.get(modelIndex);
			ColumnInfo ci = (ColumnInfo)columnPreferences.get(column.getIdentifier());

			if (ci != null) {
				column.setPreferredWidth(ci.width);

                int oldViewIndex = modelToView[modelIndex];
                if (oldViewIndex != ci.viewIndex) {
				    cmodel.moveColumn(oldViewIndex, ci.viewIndex);
                    //  update modelToView
                    for (int i=0; i<modelToView.length; i++)
                    {
                        if (modelToView[i]==oldViewIndex)
                            modelToView[i]=ci.viewIndex;
                        else if (modelToView[i]>oldViewIndex && modelToView[i] <= ci.viewIndex)
                            modelToView[i]--;
                        else if (modelToView[i]<oldViewIndex && modelToView[i] >= ci.viewIndex)
                            modelToView[i]++;
                    }
                }
			}
		}
	}
	
	public JTable getTable()	{ return table; }


	/**
	 * calculate the visible part of the table
	 * in (row,column) coordinates (starts at 0,0)
	 *
	 */
	public final Rectangle getVisibleRange()
	{
		return getVisibleRange(new Rectangle());
	}

	/**
	 * calculate the visible part of the table
	 * in (row,column) coordinates (starts at 0,0)
	 *
	 */
	public Rectangle getVisibleRange(Rectangle r)
	{
		Point p = new Point();
		scroll.getViewport().getLocation(p);
		Dimension d = scroll.getViewport().getExtentSize();

		r.x = table.columnAtPoint(p);
		r.y = table.rowAtPoint(p);

		p.x += d.width;
		p.y += d.height;
		r.width = table.columnAtPoint(p)-r.x+1;
		r.height = table.rowAtPoint(p)-r.y+1;
		return r;
	}

	/**
	 * @return true if the given row i visible in the current viewport
	 */
	public boolean isVisible(int row)
	{
		Rectangle r = table.getCellRect(row,0,false);
		Point p = scroll.getViewport().getLocation();
		if ((r.y+r.height) <= r.y) return false;
		Dimension d = scroll.getViewport().getExtentSize();
		return (r.y < (p.y+d.height));
	}

	public boolean hasSelection()
	{
		return ! table.getSelectionModel().isSelectionEmpty();
	}

    private static class DummyCellRenderer extends DefaultTableCellRenderer
    {
        /** the sole purpose of this class is to have access to the protected field
         *  DefaultTableCellRenderer.noFocusBorder (yeah, that's OO programming ;-)
         */
        public static void setInnerCellSpacing(int top, int left, int bottom, int right)
        {
            EmptyBorder border = new EmptyBorder(top,left,bottom,right);
            noFocusBorder = border;
            UIManager.put("Table.focusCellHighlightBorder", border);
        }
    }


    public static void setInnerCellSpacing(int top, int left, int bottom, int right)
    {
        DummyCellRenderer.setInnerCellSpacing(top,left,bottom,right);
    }


	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				//	store column order & width
				UserProfile prf = (UserProfile)cmd.data;

				setPreferences();
				prf.set(getName()+".columns", columnPreferences);
			}
		};
		map.put("update.user.profile", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				table.selectAll();
			}
		};
		map.put("menu.edit.select.all", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				table.getSelectionModel().clearSelection();
			}
		};
		map.put("menu.edit.select.none", action);

	}

	protected void setPreferences()
	{
		TableColumnModel cmodel = table.getColumnModel();
		for (int viewIndex=0; viewIndex<cmodel.getColumnCount(); viewIndex++)
		{
			TableColumn column = cmodel.getColumn(viewIndex);
			
			ColumnInfo ci = new ColumnInfo();
			ci.viewIndex = viewIndex;
			ci.width = column.getWidth();
			
			columnPreferences.put(column.getIdentifier(), ci);
		}
	}

    public void updateLanguage()
    {
        super.updateLanguage();
        //  update column headers
        ((AbstractTableModel)table.getModel()).fireTableRowsUpdated(
                TableModelEvent.HEADER_ROW,TableModelEvent.HEADER_ROW);
        //  TODO has no effect. why ??
    }


	//-------------------------------------------------------------------------------
    //	interface ListSelectionListener
    //-------------------------------------------------------------------------------

    public void valueChanged(ListSelectionEvent lsev)
    {
        //  broadcast
        if (!lsev.getValueIsAdjusting()) {
            Command cmd = new Command("list.selection.changed", lsev, this);
	        Application.theApplication.broadcast(cmd);
        }
    }
}
