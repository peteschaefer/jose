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

import de.jose.*;
import de.jose.db.JoConnection;
import de.jose.pgn.*;
import de.jose.profile.LayoutProfile;
import de.jose.task.GameSource;
import de.jose.task.DBTask;
import de.jose.task.DBSelectionModel;
import de.jose.util.map.IntHashSet;
import de.jose.util.AWTUtil;
import de.jose.view.dnd.GameTransferHandler;
import de.jose.view.list.IDBTableModel;
import de.jose.view.list.IntervalCacheModel;
import de.jose.window.JoDialog;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;
import java.sql.SQLException;

/**
 * this panel shows a list of available Games
 *
 */

public class ListPanel
		extends DBTable
		implements MouseListener, IDBPanel
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	the main SQL data query:
	 * 	fetch the row data, given a primary key
	 * */
	public static final String SQL_SELECT =
			/* SELECT */"Game.CId, Game.Idx, "
			+"  White.Name, MoreGame.WhiteTitle, Game.WhiteELO, "
			+"  Black.Name, MoreGame.BlackTitle, Game.BlackELO, "
			+"  Game.Result, "
			+"  Event.Name, Site.Name, MoreGame.Round, MoreGame.Board, Game.GameDate, "
			+"  Game.ECO, Opening.Name, Game.DateFlags, Annotator.Name,"
			+"  Game.PlyCount, Game.Attributes";
	public static final String SQL_FROM =
			/* FROM */"Game, Player White, Player Black, Event, Site, Opening, Player Annotator, MoreGame ";
	public static final String SQL_WHERE =
			/* WHERE */"White.Id = Game.WhiteId "
			+"   AND Black.Id = Game.BlackId "
			+"   AND Event.Id = Game.EventId "
			+"   AND Site.Id = Game.SiteId "
			+"   AND Opening.Id = Game.OpeningId "
            +"   AND Annotator.Id = Game.AnnotatorId "
            +"   AND MoreGame.GId = Game.Id";

	protected static final String DATA_SQL =
			"SELECT Game.Id, "+SQL_SELECT+
			" FROM "+SQL_FROM+
			" WHERE "+SQL_WHERE+
			"	 AND Game.Id %IN% ";

	/**	STRAIGH_JOIN is a performance hint for MySQL to join the tables in the exact order
	 * 	(cause MySQL's "optimizer" will never get it right)
	 * */

	/**	column indexes (starting at 0)	 */
	public static final int	COL_IDX				= 0;

	public static final int	COL_WNAME			= 1;
	public static final int	COL_WTITLE			= 2;
	public static final int	COL_WELO			= 3;

	public static final int	COL_BNAME			= 4;
	public static final int	COL_BTITLE			= 5;
	public static final int	COL_BELO			= 6;

	public static final int	COL_RESULT			= 7;

	public static final int	COL_EVENT			= 8;
	public static final int	COL_SITE			= 9;
	public static final int	COL_ROUND			= 10;
	public static final int	COL_BOARD			= 11;
	public static final int	COL_DATE			= 12;

	public static final int	COL_ECO				= 13;
	public static final int	COL_OPENING			= 14;
	public static final int COL_MOVECOUNT		= 15;
    public static final int	COL_ANNOTATOR   	= 16;

    public static final int	COL_MAX            	= 16;

	/**	column headers	 */
	public static final String[] COL_NAMES = {
		"column.game.index",
		"column.game.white.name",
		"column.game.white.title",
		"column.game.white.elo",
		"column.game.black.name",
		"column.game.black.title",
		"column.game.black.elo",
		"column.game.result",
		"column.game.event",
		"column.game.site",
		"column.game.round",
		"column.game.board",
		"column.game.date",
		"column.game.eco",
		"column.game.opening",
		"column.game.movecount",
        "column.game.annotator"
	};

	/**	column types	 */
	public static final Class[] COL_TYPES = {
		Integer.class,
		String.class,
		String.class,
		Integer.class,
		String.class,
		String.class,
		Integer.class,
		String.class,
		String.class,
		String.class,
		String.class,
		String.class,
		String.class,
		String.class,
		String.class,
		String.class,
        String.class,
	};

	/**	min. column widths	 */
	protected static final int[] MIN_COL_WIDTHS = {	16, 24, 16, 16, 24, 16, 16, 24, 24, 16, 16, 16, 16, 24, 24, 12 };
	/**	max. column widths	 */
	protected static final int[] MAX_COL_WIDTHS = {	};
	/**	preferred. column widths	 */
	protected static final int[] PREF_COL_WIDTHS = { 32, 120,16,48, 120,16,48, 40, 120,120, 24,24, 80, 40,120, 32, 120 };

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	Search Record	*/
	protected SearchRecord search;

	//-------------------------------------------------------------------------------
	//	Aux. Class
	//-------------------------------------------------------------------------------


	class ListTableModel extends IntervalCacheModel
	{

        public ListTableModel(String identifier) throws Exception
        {
            super(DATA_SQL,1024,128);
            setDisplayComponent(ListPanel.this);

 //           open();
            refresh(true);
        }

		public int getId(int row) {
			Number i = (Number)super.getValueAt(row,0);
			return (i!=null) ? i.intValue() : -1;
		}

		public int getResult(int row) {
			Number i = (Number)super.getValueAt(row,9);
			return (i!=null) ? i.intValue() : PgnConstants.RESULT_UNKNOWN;
		}

		public java.util.Date getDate(int row) {
			return (java.util.Date)super.getValueAt(row,14);
		}

		public int getDateFlags(int row) {
			Number num = (Number)super.getValueAt(row,17);
			return (num!=null) ? num.intValue():0;
		}

        public void setSortOrder(int sortOrder)
        {
            search.sortOrder = sortOrder;
            refresh(true);
        }

        public int getSortOrder()
        {
            return search.sortOrder;
        }

        public void setCollections(IntHashSet set)
        {
            search.collections = set;
            refresh(true);
        }

        public void refresh(boolean scrollTop)
        {
            try {
                boolean accurate = (DBTask.currentUpdates() == 0);
                int rows = search.estimateResults();
                /**  estimates might be incorrect during update
                 *  another update event will be triggered when the updates are finished
                 * in the meantime: be carful
                 */
//                System.out.println("estimated rows: "+rows);

	            if (scrollTop)
                    AWTUtil.scrollRectLater(ListPanel.this.getTable(), new Rectangle(0,0,0,0));

                reset(search.makeIdStatement(), search.makePositionFilter(), rows, accurate);

                fireTableDataChanged();

            } catch (Exception e) {
                Application.error(e);
            }
        }

/*
        public ParamStatement makeStatement(DBAdapter adapter) throws SQLException
        {
	        search.adapter = adapter;
	        search.sortOrder = sortOrder;

	        return search.makeStatement();
        }
*/
        public int calculateResultSize() throws Exception
        {
            return search.estimateResults();
        }

		public Object getValueAt(int row, int column) {
			switch (column) {
			case COL_IDX:		if (isSingleCell())
									return super.getValueAt(row,0);
								else
									return super.getValueAt(row,2);

			case COL_WNAME:		return super.getValueAt(row,3);
			case COL_WTITLE:	return super.getValueAt(row,4);
			case COL_WELO:		Number elo = (Number)super.getValueAt(row,5);
								if (elo==null || elo.intValue()<=0)
									return null;
								else
									return elo;

			case COL_BNAME:		return super.getValueAt(row,6);
			case COL_BTITLE:	return super.getValueAt(row,7);
			case COL_BELO:		elo = (Number)super.getValueAt(row,8);
								if (elo==null || elo.intValue()<=0)
									return null;
								else
									return elo;

			case COL_RESULT:	return PgnUtil.resultString(getResult(row));

			case COL_EVENT:		return super.getValueAt(row,10);
			case COL_SITE:		return super.getValueAt(row,11);
			case COL_ROUND:		return super.getValueAt(row,12);
			case COL_BOARD:		return super.getValueAt(row,13);

			case COL_DATE:		java.util.Date dt = getDate(row);
								if (dt==null)
									return null;
								else {
									int dateFlags = getDateFlags(row);
									PgnDate pgnDate = new PgnDate(dt,(short)(dateFlags & 0x00ff));
									return pgnDate.toLocalDateString(true);
								}

			case COL_ECO:		return super.getValueAt(row,15);
			case COL_OPENING:	return super.getValueAt(row,16);

            case COL_ANNOTATOR: return super.getValueAt(row,18);

			case COL_MOVECOUNT:	Number plyCount = (Number)super.getValueAt(row,19);
								Number attrs = (Number)super.getValueAt(row,20);
								return 	createMoveCount((plyCount!=null) ? plyCount.intValue():0,
														(attrs!=null) ? attrs.intValue():0);

			default:			throw new IndexOutOfBoundsException();
			}
		}

		protected String createMoveCount(int plies, int attributes)
		{
			if (attributes==0) {
				//	short cut
				if (plies > 0)
					return String.valueOf((plies+1)/2);
				else
					return null;
			}
			else {
				StringBuffer buf = new StringBuffer();
				if (plies > 0)
					buf.append(String.valueOf((plies+1)/2));
				buf.append(" ");
				if (Util.allOf(attributes,Game.IS_FRC))
					buf.append("F");
				if (Util.allOf(attributes,Game.HAS_VARIATIONS))
					buf.append("v");
				if (Util.allOf(attributes,Game.HAS_COMMENTS))
					buf.append("c");
				if (Util.allOf(attributes,Game.HAS_ERRORS))
					buf.append("e");
				return buf.toString();
			}
		}

		public int getColumnCount()
		{
			if (isSingleCell())
				return 1;
			else
				return COL_MAX+1;
		}

		public Object getIdentifier(int columnIndex) {
			return COL_NAMES[columnIndex];
		}

		public String getColumnName(int columnIndex)			{
			return Language.get(COL_NAMES[columnIndex],"");
		}

		public Class getColumnClass(int columnIndex)			{ return COL_TYPES[columnIndex]; }

		public int getMinColumnWidth(int columnIndex)			{
			if (columnIndex < MIN_COL_WIDTHS.length)
				return MIN_COL_WIDTHS[columnIndex];
			else
				return 4;
		}

		public int getMaxColumnWidth(int columnIndex)			{
			if (columnIndex < MAX_COL_WIDTHS.length)
				return MAX_COL_WIDTHS[columnIndex];
			else
				return Integer.MAX_VALUE;
		}

		public int getPreferredColumnWidth(int columnIndex)			{
			if (columnIndex < PREF_COL_WIDTHS.length)
				return PREF_COL_WIDTHS[columnIndex];
			else
				return 75;
		}
	}

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public ListPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
		throws Exception
	{
		super(profile,true,withContextMenu,withBorder);
        focusPriority = 3;
		search = new SearchRecord();
        search.adapter = JoConnection.getAdapter();
		titlePriority = 8;
        setOpaque(true);
	}

	public void adjustContextMenu(java.util.Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);
		list.add(ContextMenu.SEPARATOR);

		//	find selected collection
		int row = table.rowAtPoint(event.getPoint());
		int col = table.columnAtPoint(event.getPoint());

		int GId = 0;
		GameSource source = null;
		Collection currentCollection = null;
		if (row>=0 && col>=0) {
			GId = ((ListTableModel)model).getId(row);
			if (GId > 0)
				try {
					if (table.isCellSelected(row,col))
						source = GameSource.gameSelection(getCurrentSelection());	//	applies to selection
					else
						source = GameSource.singleGame(GId);	//	applies to one row
					currentCollection = Collection.readCollectionByGame(GId);
				} catch (Exception ex) {
					Application.error(ex);
				}
		}

        if (source != null) {
			if (source.size()==1) {
				list.add("edit.game");
				list.add(source);
				list.add("menu.game.details");
				list.add(source);
			}
			else {
				list.add("edit.all");
				list.add(source);
			}

	        list.add("menu.file.print");
	        list.add(source);
	        list.add("menu.file.save.as");
	        list.add(source);

			list.add(ContextMenu.SEPARATOR);
		}


		if (currentCollection!=null)
		{
			if (currentCollection.isInTrash() || currentCollection.isInClipboard())
			{
/*
				if (currentCollection.isInTrash()) {
					list.add("menu.edit.erase");
					list.add(source);
				}
*/

				list.add("menu.edit.restore");
				list.add(source);
			}
			else if ((source!=null && source.isGame()) || !currentCollection.isSystem()) {
				list.add("menu.edit.cut");
				list.add(source);

				list.add("menu.edit.copy");
				list.add(source);

				list.add("menu.edit.clear");
				list.add(source);

                list.add("menu.edit.collection.crunch");
                list.add(GameSource.singleCollection(currentCollection.Id));

                list.add(ContextMenu.SEPARATOR);

/*              TODO
				if (Version.POSITION_INDEX) {
                    list.add("menu.edit.position.index");
	                list.add(source);
				}
*/

                list.add("menu.edit.ecofy");
                list.add(source);

			}
			list.add(ContextMenu.SEPARATOR);
		}

		/** Hack: if Collection.GameCount gets out of synch (which it souldn't)
		 *  offer a menu entry for re-synching
		 *   */
		if (((IntervalCacheModel)table.getModel()).isOutOfSynch)
			list.add("debug.resynch");
	}

	public SearchRecord getSearchRecord()		{ return search; }

	/**	called when the panel is show for the first time	 */
	public void init()
		throws Exception
	{
        super.init();

		table.setDragEnabled(true);
		table.setTransferHandler(new GameTransferHandler());
		table.setIntercellSpacing(new Dimension(0,0));
        setInnerCellSpacing(0,4,0,4);

		initModel();

		table.addMouseListener(this);

        //  the query panel shows the number of result rows; connect it to the table model
        QueryPanel qpanel = (QueryPanel)JoPanel.get("window.query");
        if (qpanel!=null) {
            model.addTableModelListener(qpanel);
            model.fireTableDataChanged();
        }

		JoDialog.rescaleFonts(this);
	}

	public void stopResult() throws SQLException
	{
		((IntervalCacheModel)model).stopResult();
	}

	public IDBTableModel createModel(String identifier)
		throws Exception
	{
		return new ListTableModel(identifier);
	}

	public void invalidateGame(int GId)
	{
		if (model!=null) model.invalidateRowByPK(GId);
	}

	public void invalidateCollection(int CId) throws Exception
	{
		if (model!=null && search.isCollectionSelected(CId))
			model.refresh(false);
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) {
				try {
					/**	avoid deadlocks during update	*/
					if (model!=null) model.close(false);
//					((CachedIntervalModel)model).stopResult();
				} catch (Exception ex) {
					Application.error(ex);
				}
			}
		};
		map.put(DBTask.COMMAND_BEFORE_UPDATE, action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				try {

					if (model!=null) model.refresh(true);

				} catch (Exception ex) {
					try { close(); } catch (Exception ex2) { /* can't help it */ }
					Application.error(ex);
				}
			}
		};
		map.put(DBTask.COMMAND_AFTER_UPDATE, action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				JoPanel panel = (JoPanel)cmd.data;
				if (panel!=null && (panel instanceof QueryPanel) && model!=null)
                {
                    QueryPanel queryPanel = (QueryPanel)panel;
                    model.addTableModelListener(queryPanel);
                    model.fireTableDataChanged();
                }
			}
		};
		map.put("panel.init", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	broadcasted by Collection Panel
				IntHashSet cIds = (IntHashSet)cmd.data;
				if (table.getModel() instanceof ListTableModel)
					((ListTableModel)table.getModel()).setCollections(cIds);
			}
		};
		map.put("collection.selection.changed", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//	broadcasted by GameDetailsDialog
				int GId = ((Number)cmd.data).intValue();
				invalidateGame(GId);
			}
		};
		map.put("game.modified",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//	broadcasted by GameDetailsDialog
				int CId = ((Number)cmd.data).intValue();
				invalidateCollection(CId);
			}
		};
		map.put("collection.modified",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				Collection.repairGameCounts();
				((IntervalCacheModel)table.getModel()).isOutOfSynch = false;
			}
		};
		map.put("debug.resynch",action);
	}


    //-----------------------------------------------------------------------------------
	//	implements MouseListener
	//-----------------------------------------------------------------------------------

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			//	double click
			int rowNum = table.rowAtPoint(e.getPoint());
			if (rowNum >= 0 && rowNum < table.getRowCount()) {
				int GId = ((ListTableModel)model).getId(rowNum);

				Command cmd = new Command("edit.game", e,
						GameSource.singleGame(GId),
						Boolean.TRUE);
				AbstractApplication.theCommandDispatcher.handle(cmd,Application.theApplication);
			}
		}
	}

	public void mouseEntered(MouseEvent e) { }

	public void mouseExited(MouseEvent e)  { }

	public void mousePressed(MouseEvent e) { }

	public void mouseReleased(MouseEvent e) { }

}
