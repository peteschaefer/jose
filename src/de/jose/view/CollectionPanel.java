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
import de.jose.db.JoConnection;
import de.jose.db.io.AutoImport;
import de.jose.image.ImgUtil;
import de.jose.pgn.Collection;
import de.jose.profile.LayoutProfile;
import de.jose.task.DBSelectionModel;
import de.jose.task.GameSource;
import de.jose.task.DBTask;
import de.jose.util.map.IntHashSet;
import de.jose.util.IntArray;
import de.jose.view.dnd.GameTransferHandler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Map;
import java.sql.SQLException;

public class CollectionPanel
        extends JoPanel
        implements TreeSelectionListener, TreeExpansionListener, TreeModelListener, DBSelectionModel
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	public static final ImageIcon	CLOSED_ICON		= ImgUtil.getMenuIcon("folder");
	public static final ImageIcon	OPEN_ICON		= ImgUtil.getMenuIcon("folder.open");
	public static final ImageIcon	CLIPBOARD_ICON	= ImgUtil.getMenuIcon("clipboard");
	public static final ImageIcon	TRASH_ICON		= ImgUtil.getMenuIcon("trash");
	public static final ImageIcon	AUTOSAVE_ICON	= ImgUtil.getMenuIcon("menu.file.save");
	public static final ImageIcon	EMPTY_ICON		= ImgUtil.getMenuIcon("folder.grey");

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	the tree view	*/
	protected JTree tree;
	/**	surrounding scroll pane	*/
	protected JScrollPane scroll;
	/**	set of selected Id's	*/
	protected IntHashSet selectedIds;
	/**	set of expanded Id's	*/
	protected IntHashSet expandedIds;
	/**	the Clipboard collection	*/
	protected Collection theClipboard;
	/**	the Trash collection */
	protected Collection theTrash;
	/** auto import manager */
	protected AutoImport autoImport;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public CollectionPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
		throws Exception
	{
		super(profile,withContextMenu,withBorder);
        setLayout(new BorderLayout());

		selectedIds = new IntHashSet(64,0.8f);
		expandedIds = new IntHashSet(64,0.8f);
		titlePriority = 7;
		setFocusable(false);    //  don't request keyboard focus (or should we ?)
	}

    public void init()
            throws Exception
    {
        tree = new JTree(newRootNode());
        tree.setRootVisible(false);
        CollectionCellRenderer renderer = new CollectionCellRenderer();
        tree.setCellRenderer(renderer);
        tree.setCellEditor(new CollectionCellEditor(tree,renderer));
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(this);
        tree.addTreeExpansionListener(this);
        tree.setEditable(true);
        tree.getModel().addTreeModelListener(this);
        if (Version.java14orLater) {
            tree.setDragEnabled(true);
            tree.setTransferHandler(new GameTransferHandler());
        }

        scroll = new JScrollPane(tree,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(Color.white);

        add(scroll, BorderLayout.CENTER);
    }

	public void postInit() throws Exception
	{
		super.postInit();

		//  kick off auto import
		try {
			autoImport = new AutoImport(Application.theApplication.theConfig);
			autoImport.next();
		} catch (Exception e) {
			Application.error(e);   //  won't stop us from proceeeding !
		}
	}

/*
    public void doLayout()
    {
        int w = getWidth();
        int h = getHeight();
        scroll.setBounds(0,0, w,h);

        int iw = searchIcon.getWidth();
        int ih = searchIcon.getHeight();
        searchIcon.setBounds(w-32,0, 16,16);
    }
*/
	//-------------------------------------------------------------------------------
	//	Inner Class: TreeModel
	//-------------------------------------------------------------------------------

	private CollectionTreeNode newRootNode() throws Exception
	{
		/*	find the children	*/
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			return new CollectionTreeNode(conn,null);
		} finally {
			if (conn!=null) conn.release();
		}
	}

	class CollectionTreeNode extends DefaultMutableTreeNode
 	{
		Collection getCollection() 		{ return (Collection)getUserObject(); }

		CollectionTreeNode(JoConnection conn, Collection collection)
		    throws Exception
		{
			super();
			setUserObject(collection);

			de.jose.pgn.Collection[] children;
			if (collection==null)
				children = Collection.childrenArray(conn,0);
			else
				children = Collection.childrenArray(conn,collection.Id);

			for (int i=0; i<children.length; i++)
			{
				CollectionTreeNode node = new CollectionTreeNode(conn, children[i]);
				add(node);

				switch (children[i].Id) {
				case Collection.CLIPBOARD_ID:	theClipboard = children [i]; break;
				case Collection.TRASH_ID:		theTrash = children [i]; break;
				}
			}
		}

		public void setUserObject(Object userObject) {
			/*	might be called from a TreeCellEditor with a String value	*/
			if (userObject==null)
				;
			else if (userObject instanceof Collection)
				super.setUserObject(userObject);
			else if (userObject instanceof String)
				try {
					Collection coll = getCollection();
					coll.renameTo((String)userObject);
				} catch (Exception ex) {
					Application.error(ex);
				}
			else
				throw new IllegalStateException();
		}
	}

	class CollectionCellRenderer extends DefaultTreeCellRenderer
	{
		CollectionCellRenderer()
		{
			super();
			super.setBorder(new EmptyBorder(2,2,2,2));
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
		                                              boolean sel,
		                                              boolean expanded,
		                                              boolean leaf, int row,
		                                              boolean hasFocus)
		{
			setIcons(value,sel,expanded,leaf);

			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}

		protected void setIcons(Object value, boolean sel, boolean expanded, boolean leaf)
		{
			Collection coll = ((CollectionTreeNode)value).getCollection();
			int cid = (coll!=null) ? coll.Id : 0;

			switch (cid) {
			case Collection.TRASH_ID:			setIcon(TRASH_ICON,leaf,expanded); break;
			case Collection.AUTOSAVE_ID:		setIcon(AUTOSAVE_ICON,leaf,expanded); break;
			case Collection.CLIPBOARD_ID:		setIcon(CLIPBOARD_ICON,leaf,expanded); break;
			default:
					if (coll!=null && !coll.hasGames())
						setIcon(EMPTY_ICON,leaf,expanded);
					else if (sel)
						setIcon(OPEN_ICON,leaf,expanded);
					else
						setIcon(CLOSED_ICON,leaf,expanded);
					/**	we could use instead:
							setIcon(getDefaultOpenIcon(),leaf,expanded);
							setIcon(getDefaultClosedIcon(),leaf,expanded);
					 	but some LnFs do not distinguish open and closed folders
					 	and most look ugly, anyway ;-)
					 */
					break;
			}
		}

		private void setIcon(Icon icon, boolean leaf, boolean expanded)
		{
			if (leaf)
				setLeafIcon(icon);
			else if (expanded)
				setOpenIcon(icon);
			else
				setClosedIcon(icon);
		}
	}

	class CollectionCellEditor extends DefaultTreeCellEditor
	{
		CollectionCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
		{
			super(tree,renderer);
		}

		public Component getTreeCellEditorComponent(JTree tree, Object value,
		                                            boolean isSelected,
		                                            boolean expanded,
		                                            boolean leaf, int row)
		{
			/**	reset the renderer's icons	*/
			CollectionCellRenderer rend = (CollectionCellRenderer)renderer;
			rend.setIcons(value,isSelected,expanded,leaf);

			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		}
	}

	//-------------------------------------------------------------------------------
	//	Interface JoComponent
	//-------------------------------------------------------------------------------

	public void adjustContextMenu(java.util.Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);
		list.add(ContextMenu.SEPARATOR);

		//	find selected collection
		Collection currentCollection = null;
		GameSource source = null;
		TreePath path = tree.getPathForLocation(event.getX(),event.getY());
		if (path != null) {
			currentCollection = getCollection(path);
			if (selectedIds.contains(currentCollection.Id))
				source = GameSource.collectionSelection(this);	//	applies to selection
			else
				source = GameSource.singleCollection(currentCollection.Id);	//	applies to one row
		}

		if (source!=null) {
			list.add("menu.file.print");
			list.add(source);
			list.add("menu.file.save.as");
			list.add(source);
			list.add(ContextMenu.SEPARATOR);
		}

		if (currentCollection!=null && !currentCollection.isSystem())
		{
			if (currentCollection.isInTrash() || currentCollection.isInClipboard()) {
				list.add("menu.edit.restore");
				list.add(source);
			}
			else {
				list.add("menu.edit.cut");
				list.add(source);

				list.add("menu.edit.copy");
				list.add(source);

                try {
                    if (Collection.hasContents(Collection.CLIPBOARD_ID)) {
                        list.add("menu.edit.paste");
                        list.add(source);

//					list.add("menu.edit.paste.copy");
//					list.add(source);
//					list.add("menu.edit.paste.same");
//					list.add(source);
                    }
                } catch (SQLException e) {
                    Application.error(e);
                }

                list.add("menu.edit.clear");
				list.add(source);
			}
		}

		if (currentCollection!=null) {
			list.add("menu.edit.collection.rename");
			list.add(path);
			list.add("menu.edit.collection.crunch");
			list.add(source);
		}

		list.add(ContextMenu.SEPARATOR);

		if (currentCollection==null || !currentCollection.isSystem())
		{
			list.add("menu.edit.collection.new");
			list.add(source);
		}

/*
		if (currentCollection!=null && currentCollection.isInTrash()) {
			list.add("menu.edit.erase");
			list.add(source);
		}
*/

        try {

            if (currentCollection!=null
                  && currentCollection.isTrash()
                  && Collection.hasContents(currentCollection.Id))
                list.add("menu.edit.empty.trash");

        } catch (SQLException e) {
            Application.error(e);
        }

        if (currentCollection!=null && !currentCollection.isTopLevel()) {
			list.add("dnd.move.top.level");
			list.add(source);
		}

        if (currentCollection != null)
        {
            list.add(null);

/*          TODO
	        if (Version.POSITION_INDEX) {
                list.add("menu.edit.position.index");
                list.add(source);
	        }
*/

            list.add("menu.edit.ecofy");
            list.add(source);
        }
	}

	//-------------------------------------------------------------------------------
    //	interface TreeSelectionListener
    //-------------------------------------------------------------------------------

	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath[] paths = e.getPaths();
		for (int i=0; i<paths.length; i++)
		{
			int cid = getId(paths[i]);
			if (cid > 0) {
				if (e.isAddedPath(i))
					selectedIds.add(cid);
				else
					selectedIds.remove(cid);
			}
		}

		//	sending this event will automatically adjust the ListPanel
		//	don't do it during DnD
		if (Version.java14orLater && !GameTransferHandler.isDragging()) {
            //  show/hide the looking glass icon
			Command cmd = new Command("collection.selection.changed",e,selectedIds);
			Application.theApplication.broadcast(cmd);
		}
	}

	//-------------------------------------------------------------------------------
    //	interface TreeExpansionListener
    //-------------------------------------------------------------------------------

	public void treeExpanded(TreeExpansionEvent event) {
		int cid = getId(event.getPath());
		if (cid > 0)
			expandedIds.add(cid);
	}

	public void treeCollapsed(TreeExpansionEvent event) {
		int cid = getId(event.getPath());
		if (cid > 0)
			expandedIds.remove(cid);
	}

	//-------------------------------------------------------------------------------
	//	interface DBSelectionModel
	//-------------------------------------------------------------------------------

	public int getMinSelectionIndex() {
		return tree.getSelectionModel().getMinSelectionRow();
	}

	public int getMaxSelectionIndex() {
		return tree.getSelectionModel().getMaxSelectionRow();
	}

	public boolean isSelectedIndex(int index) {
		return tree.getSelectionModel().isRowSelected(index);
	}

	public int getDBId(int index) {
		TreePath path = tree.getPathForRow(index);
		return getId(path);
	}

	public boolean hasSelection()		{
		return tree!=null && ! tree.getSelectionModel().isSelectionEmpty();
	}

	//-------------------------------------------------------------------------------
	//	interface TreeModelListener
	//-------------------------------------------------------------------------------

	public void treeNodesChanged(TreeModelEvent e) {
		/**	this event is fired after a node has been edited	*/
		try {
			synch();
		} catch (Exception ex) {
			Application.error(ex);
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
	}

	public void treeNodesRemoved(TreeModelEvent e) {
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	/**
	 * synchronize tree view with database;
	 */
	public void synch() throws Exception
	{
		if (tree==null) return;
		
		/**	restore the original expansion and selection state:
		 */
		IntHashSet selected = selectedIds;
		IntHashSet expanded = expandedIds;

		tree.removeTreeExpansionListener(this);
		tree.removeTreeSelectionListener(this);

		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		CollectionTreeNode root = newRootNode();
		/**
		 * if a tree node is currently edited, abort it now (workaround!!)
		 */
		if (tree.isEditing()) tree.stopEditing();
		model.setRoot(root);

		selectedIds = new IntHashSet(64,0.8f);
		expandedIds = new IntHashSet(64,0.8f);

		//	traverse the tree
		Enumeration en = root.depthFirstEnumeration();
		while (en.hasMoreElements()) {
			CollectionTreeNode node = (CollectionTreeNode)en.nextElement();
			int cid = getId(node);
			if (cid==0) continue;

			TreePath path=null;
			if (expanded.contains(cid)) {
				if (path==null) path = new TreePath(node.getPath());
				expandedIds.add(cid);
				tree.expandPath(path);
				/**	unfortunately, this would expand all parents, too
				 * 	even if they should be collapsed.
				 * 	so we have to collapse them again ;-(
				 */
				for (TreePath parent = path.getParentPath(); parent != null; parent = parent.getParentPath())
				{
					CollectionTreeNode pnode = (CollectionTreeNode)parent.getLastPathComponent();
					if (pnode.isRoot())
						break;
					if (! expandedIds.contains(getId(pnode)))
						tree.collapsePath(parent);
				}
			}

			if (selected.contains(cid)) {
				if (path==null) path = new TreePath(node.getPath());
				selectedIds.add(cid);
				tree.addSelectionPath(path);
			}
		}

		tree.addTreeExpansionListener(this);
		tree.addTreeSelectionListener(this);
	}

	public TreePath getPathById(int CId)
	{
		if (tree==null || tree.getModel()==null) return null;

		CollectionTreeNode root = (CollectionTreeNode)tree.getModel().getRoot();
		if (root==null) return null;

		Enumeration en = root.depthFirstEnumeration();
		while (en.hasMoreElements()) {
			CollectionTreeNode node = (CollectionTreeNode)en.nextElement();
			if (getId(node) == CId)
				return new TreePath(node.getPath());
		}
		return null;
	}

	public static final int getId(TreePath path)
	{
		return getId((TreeNode)path.getLastPathComponent());
	}

	public static final Collection getCollection(TreePath path)
	{
		CollectionTreeNode node = (CollectionTreeNode)path.getLastPathComponent();
		return node.getCollection();
	}

	public static final int getId(TreeNode node)
	{
		Collection coll = ((CollectionTreeNode)node).getCollection();
		if (coll==null)
			return 0;
		else
			return coll.Id;
	}

	public void expand(int CId)
	{
		/*	find path	*/
		TreePath path = getPathById(CId);
		if (path!=null)
			tree.expandPath(path);
	}

	public void select(int CId)
	{
		/*	find path	*/
		TreePath path = getPathById(CId);
		if (path!=null)
			tree.setSelectionPath(path);
	}

	public void unselect(int CId)
	{
		TreePath path = getPathById(CId);
		if (path!=null)
			tree.getSelectionModel().removeSelectionPath(path);
	}

	public void unselect(int[] CId)
	{
		for (int i=0; i<CId.length; i++)
			unselect(CId[i]);
	}

	public void edit(int CId)
	{
		TreePath path = getPathById(CId);
		if (path!=null)
			tree.startEditingAtPath(path);
	}

	public void setupActionMap(Map map)
	{
		super.setupActionMap(map);

		CommandAction action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				if (inited) {
					synch();
					//  after import, select the newly created collection
					if (cmd.data!=null) {
						int CId = Util.toint(cmd.data);
						select(CId);
					}
				}
				//  kick auto importer
				if (autoImport!=null) autoImport.next();
			}
		};
		map.put(DBTask.COMMAND_AFTER_UPDATE, action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				String taskName = (String)cmd.data;
				if (taskName.equals("Erase"))
				{
					//  erasing trash
					//  DON'T show games from trash while data is deleted
					if (hasSelection())
					{
						IntArray trashIds = new IntArray();
						for (int i=getMinSelectionIndex(); i <= getMaxSelectionIndex(); i++)
							if (isSelectedIndex(i)) {
								int CId = getDBId(i);
								if ((Collection.TRASH_ID==CId) || Collection.isInTrash(CId))
									trashIds.add(CId);
							}
						if (!trashIds.isEmpty())
							unselect(trashIds.toArray());
					}
				}
			}
		};
		map.put(DBTask.COMMAND_BEFORE_UPDATE, action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				TreePath path = (TreePath)cmd.data;
				tree.startEditingAtPath(path);
			}
		};
		map.put("menu.edit.collection.rename", action);

		//	adjust selection AFTER drag&drop (NOT during drag&drop)
		action = new CommandAction() {
			public void Do(Command cmd) {
				cmd = new Command("collection.selection.changed",null,selectedIds);
				Application.theApplication.broadcast(cmd);
			}
		};
		map.put("dnd.drag.stop", action);
	}

    public void updateLanguage()
    {
        tree.repaint();
    }
}
