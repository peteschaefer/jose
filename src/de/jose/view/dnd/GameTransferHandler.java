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

package de.jose.view.dnd;

import de.jose.Application;
import de.jose.Command;
import de.jose.Util;
import de.jose.image.ImgUtil;
import de.jose.pgn.Collection;
import de.jose.task.GameSource;
import de.jose.util.IntArray;
import de.jose.util.ClipboardUtil;
import de.jose.util.AWTUtil;
import de.jose.view.CollectionPanel;
import de.jose.view.list.IntervalCacheModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

/**
 *	Drag&Drop transfer handler
 * 	this handler is used by ListPanel
 *
 * 	we assume that the source Compnonent is a JTable that belongs to a ListPanel
 */

public class GameTransferHandler
        extends TransferHandler
{
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	public static final Icon	DOC_ICON = ImgUtil.getIcon("menu","file");
	public static final Icon	FOLDER_ICON = ImgUtil.getIcon("menu","folder");

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	protected GameSource src;
	protected static boolean isDragging = false;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public GameTransferHandler()
	{
		super();
	}

	public static boolean isDragging() {
		return isDragging;
	}

	protected GameSource getSource(JComponent comp)
	{
		if (comp instanceof JTree){
			JTree tree = (JTree)comp;
			TreePath[] paths = tree.getSelectionPaths();
			IntArray collect = new IntArray(paths.length);

			for (int i=0; i<paths.length; i++) {
				int CId = CollectionPanel.getId(paths[i]);
				if (!Collection.isSystem(CId))
					collect.add(CId);
			}

			if (collect.isEmpty())
				return null;
			else
				return GameSource.collectionArray(collect.toArray());
			/*	DON'T use GameSource.collectionSelection(collectionPanel())
				because the tree selection will change during Drag&Drop
			*/
		}
		else if (comp instanceof JTable) {
			JTable table = (JTable)comp;
			ListSelectionModel select = table.getSelectionModel();
			IntervalCacheModel dbModel = (IntervalCacheModel)table.getModel();

			int i = select.getMinSelectionIndex();
			int i1 = select.getMaxSelectionIndex();
			IntArray array = new IntArray(i1-i+1);

			for ( ; i <= i1; i++)
				if (select.isSelectedIndex(i))
					array.add(dbModel.getDBId(i));

			return GameSource.gameArray(array.toArray());
			/*	DON'T use GameSource.gameSelection(collectionPanel())
				because the selection might change during Drag&Drop
			*/
		}
		return null;
	}

	public void exportAsDrag(JComponent comp, InputEvent e, int action)
	{
		src = getSource(comp);
		isDragging = (src!=null);
		if (isDragging)
			Application.theApplication.broadcast(new Command("dnd.drag.start",e,src));

		super.exportAsDrag(comp,e,action);
	}

	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
	{
		src = getSource(comp);

		switch (action) {
		case COPY:
			Command cmd = new Command("menu.edit.copy",null, src);
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			return;

		case MOVE:
			cmd = new Command("menu.edit.cut",null, src);
			Application.theCommandDispatcher.handle(cmd,Application.theApplication);
			return;
		}
	}

	public int getSourceActions(JComponent c)
	{
		return COPY+MOVE;
	}

	protected Transferable createTransferable(JComponent c)
	{
		return src;
	}

	/**
	 * has this any effect ? I doubt ....
	 * */
	public Icon getVisualRepresentation(Transferable t) {
//		return new GameTransferIcon(32,32);
		if (src.isGame())
			return DOC_ICON;
		else
			return FOLDER_ICON;
	}

	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		if (comp instanceof JTree) {
			//	expects a GameSource flavor
			return GameSource.isGameSourceFlavor(transferFlavors);
		}
		return super.canImport(comp, transferFlavors);
	}

	protected void exportDone(JComponent source, Transferable data, int action) {
		if (isDragging)
			Application.theApplication.broadcast(new Command("dnd.drag.stop",null,data));
		isDragging = false;
		super.exportDone(source, data, action);
	}

	public boolean importData(JComponent comp, Transferable proxy)
	{
		if (comp instanceof JTree)
			try {
				//	move to currently selected folder !
				JTree tree = (JTree)comp;
				TreePath targetPath = tree.getSelectionPath();
				Collection target = CollectionPanel.getCollection(targetPath);

				/**	be aware that "proxy" is usually not the original object created
				 * 	by ourselves; it is a "proxy" from which we have to reconstruct
				 *  the original data
 				 */
                if (proxy.isDataFlavorSupported(GameSource.COLLECTION_CONTENTS))
                {
                    //  this flavor indicates that we move data from the DATABASE clipboard
                    //  the system clipboard does not contain actual data
                    int CId = Util.toint(proxy.getTransferData(GameSource.COLLECTION_CONTENTS));
                    GameSource src = GameSource.collectionContents(CId);

                    Command cmd;
                    if (CId==Collection.CLIPBOARD_ID)
                        cmd = new Command("menu.edit.paste",null, src, target);
                    else
                        cmd = new Command("dnd.move.games",null, src, target);
                    Application.theCommandDispatcher.handle(cmd,Application.theApplication);
                    return true;
                }
                else {
                    DataFlavor preferredFlavor = proxy.getTransferDataFlavors()[0];
                    /*	the preferred data flavor is first */
                    if (GameSource.isGameSourceFlavor(preferredFlavor)) {
                        Object data = proxy.getTransferData(preferredFlavor);
                        GameSource src = new GameSource(preferredFlavor,data);

                        Command cmd = new Command("dnd.move.games",null, src, target);
                        Application.theCommandDispatcher.handle(cmd,Application.theApplication);
                        return true;
                    }
                    //  else: paste PGN from external sources
                }

                //  could not handle
                AWTUtil.beep(comp);

			} catch (Exception ufex) {
				//	shouldn't happen
				Application.error(ufex);
			}

		return super.importData(comp,proxy);
	}

}
