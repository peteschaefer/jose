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

package de.jose.task;

import de.jose.Application;
import de.jose.view.list.IDBTableModel;
import de.jose.util.ListUtil;
import de.jose.util.IntArray;
import de.jose.pgn.Collection;
import de.jose.pgn.Game;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.InputSource;

public class GameSource
        extends InputSource
        implements Transferable
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	source types */
	public static DataFlavor NOT_SET				= newDataFlavor("not-set");

	/**	single game: 				(Integer)data					*/
	public static DataFlavor SINGLE_GAME			= newDataFlavor("single-game");
	/**	selection of games: 		(DBSelectionModel)data		*/
	public static DataFlavor GAME_SELECTION			= newDataFlavor("game-selection");

	/**	single collection:			(Integer)data					*/
	public static DataFlavor SINGLE_COLLECTION		= newDataFlavor("single-collection");
	/**	contents of collection:		(Integer)data					*/
	public static DataFlavor COLLECTION_CONTENTS	= newDataFlavor("collection-contents");
	/**	selection of collections:	(DBSelectionModel)data	*/
	public static DataFlavor COLLECTION_SELECTION	= newDataFlavor("collection-selection");

	/**	array of Game Ids:	(int[])data	*/
	public static DataFlavor GAME_ARRAY				= newDataFlavor("game-array");
	/**	array of Collection Ids:	(int[])data	*/
	public static DataFlavor COLLECTION_ARRAY		= newDataFlavor("collection-array");

	/** Game Object:  (Game)data    */
	public static DataFlavor OBJECT                 = newDataFlavor("game-object");
	/** List of Game objects: (List)data    */
	public static DataFlavor LIST                   = newDataFlavor("object-list");

	/** DB Result Set   */
	public static DataFlavor RESULT_SET             = newDataFlavor("result-set");

	public static DataFlavor newDataFlavor(String identifier)
	{
		try {
			DataFlavor result = new DataFlavor("application/jose-"+identifier);
			return result;
		} catch (ClassNotFoundException cnfex) {
			//	how can this happen ?
			Application.error(cnfex);
			return null;
		}
	}

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	source type	*/
	public DataFlavor flavor;
	/**	data	*/
	public Object data;
	/** size; Integer.MIN_VALUE wen not (yet) calculated)   */
	public int size;

	public GameSource(DataFlavor sourceFlavor, Object d)
	{
		flavor = sourceFlavor;
		data = d;
		size = Integer.MIN_VALUE;
	}

	public GameSource(DataFlavor sourceFlavor, int d)
	{
		this (sourceFlavor, new Integer(d));
	}


	public final int getId()												{ return ((Integer)data).intValue(); }

	public final DBSelectionModel getSelection()							{ return (DBSelectionModel)data; }

	public final int[] getIds()												{ return (int[])data; }


	public final Game getObject()                                           { return (Game)data; }

	public final Iterator getIterator()                                     { return ListUtil.iterator(data); }


	public static GameSource singleGame(int GId)							{ return new GameSource(SINGLE_GAME, GId);	}

	public static GameSource singleCollection(int CId)						{ return new GameSource(SINGLE_COLLECTION, CId); }

	public static GameSource collectionContents(int CId)					{ return new GameSource(COLLECTION_CONTENTS, CId); }

	public static GameSource gameSelection(DBSelectionModel select)			{ return new GameSource(GAME_SELECTION,select); }

	public static GameSource collectionSelection(DBSelectionModel select)	{ return new GameSource(COLLECTION_SELECTION,select); }

	public static GameSource gameArray(int[] gids)							{ return new GameSource(GAME_ARRAY,gids); }

	public static GameSource collectionArray(int[] cids)					{ return new GameSource(COLLECTION_ARRAY,cids); }

	public static GameSource gameObject(Game game)                          { return new GameSource(OBJECT,game); }

	public static GameSource gameList(Object games)                         { return new GameSource(LIST,games); }

    /** @deprecated never properly implemented in the first place !! */
	public static GameSource databaseResultSet(IDBTableModel games)         { return new GameSource(RESULT_SET,games); }

	public final boolean isNotSet()					{ return flavor.equals(NOT_SET); }
	public final boolean isSingleGame()				{ return flavor.equals(SINGLE_GAME); }
	public final boolean isSingleCollection()		{ return flavor.equals(SINGLE_COLLECTION); }
	public final boolean isCollectionContents()		{ return flavor.equals(COLLECTION_CONTENTS); }
	public final boolean isGameSelection()			{ return flavor.equals(GAME_SELECTION); }
	public final boolean isCollectionSelection()	{ return flavor.equals(COLLECTION_SELECTION); }
	public final boolean isGameArray()				{ return flavor.equals(GAME_ARRAY); }
	public final boolean isCollectionArray()		{ return flavor.equals(COLLECTION_ARRAY); }

	public final boolean isGame()					{ return isSingleGame() || isGameSelection() || isGameArray(); }
	public final boolean isCollection()				{ return isSingleCollection() || isCollectionSelection() || isCollectionArray(); }

	public final boolean isSingle()					{ return isSingleGame() || isSingleCollection(); }
	public final boolean isSelection()				{ return isGameSelection() || isCollectionSelection(); }
	public final boolean isArray()					{ return isGameArray() || isCollectionArray(); }

	public final boolean isObject()                 { return flavor.equals(OBJECT); }
	public final boolean isList()                   { return flavor.equals(LIST); }
	public final boolean isResultSet()		        { return flavor.equals(RESULT_SET); }


	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { flavor, DataFlavor.stringFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor aflavor) {
		return flavor.equals(aflavor) || DataFlavor.stringFlavor.equals(aflavor);
	}

	public Object getTransferData(DataFlavor flavor)
	        throws UnsupportedFlavorException, IOException
	{
		if (DataFlavor.stringFlavor.equals(flavor))
			return toString();
		else if (isDataFlavorSupported(flavor))
			return data;
		else
			throw new UnsupportedFlavorException(flavor);
	}

	public static boolean isGameSourceFlavor(DataFlavor aflavor)
	{
		return aflavor.getMimeType().startsWith("application/jose-");
	}

	public static boolean isGameSourceFlavor(DataFlavor[] flavors)
	{
		for (int i=0; i<flavors.length; i++)
			if (isGameSourceFlavor(flavors[i]))
				return true;
		return false;
	}

	public int firstId()
	{
		if (isNotSet())
			return 0;

		if (isSingle() || isCollectionContents())
			return getId();

		if (isSelection()) {
			int i = getSelection().getMinSelectionIndex();
			return getSelection().getDBId(i);
		}

		if (isArray())
			return getIds()[0];

		if (isObject()) {
			return getObject().getId();
		}

		if (isList()) {
			Iterator i = getIterator();
			if (i.hasNext())
				return ((Game)i.next()).getId();
			else
				return 0;   //  unknown
		}

		throw new UnsupportedOperationException();
	}

	public int size()
	{
		if (size==Integer.MIN_VALUE)
			 size = calcSize();
		return size;
	}

	private int calcSize()
	{
		if (isNotSet())				return 0;
		if (isSingleGame())			return 1;
		if (isArray())				return getIds().length;

		if (isGameSelection()) {
			int result = 0;
			DBSelectionModel sel = getSelection();
			int min = sel.getMinSelectionIndex();
			int max = sel.getMaxSelectionIndex();
			for (int i = min; i <= max; i++)
				if (sel.isSelectedIndex(i)) result++;
			return result;
		}

		if (isCollectionSelection()) {
			IntArray CIds = new IntArray();
			DBSelectionModel select = getSelection();
			for (int i=select.getMinSelectionIndex(); i <= select.getMaxSelectionIndex(); i++)
				if (select.isSelectedIndex(i)) {
					CIds.add(select.getDBId(i));
				}
			try {
				return Collection.countGames(CIds.toArray());
			} catch (Exception e) {
				Application.error(e);
				return Integer.MIN_VALUE;
			}
		}

		if (isCollectionContents() || isSingleCollection())
			try {
				return Collection.countGames(getId());
			} catch (Exception e) {
				Application.error(e);
			}

		if (isObject()) return 1;

		if (isList()) {
			if (ListUtil.hasSize(data))
				return ListUtil.size(data);
			else
				return -1;  //  unknown
		}

		if (isResultSet()) {
			IDBTableModel model = (IDBTableModel)data;
			return model.getRowCount();
		}

		throw new IllegalStateException("unknown flavor "+flavor);
	}
}
