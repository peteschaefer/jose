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

import de.jose.task.GameSource;
import de.jose.pgn.Collection;
import de.jose.util.ListUtil;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * @author Peter Schäfer
 */
public class DatabaseGameSelection extends StringSelection
{
	public DatabaseGameSelection(String asText)
	{
		super(asText);
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(GameSource.COLLECTION_CONTENTS))
			return new ByteArrayInputStream(String.valueOf(Collection.CLIPBOARD_ID).getBytes());      //  compatibility with StringSelection ?!
		else
			return super.getTransferData(flavor);
	}

	public DataFlavor[] getTransferDataFlavors()
	{
		return (DataFlavor[]) ListUtil.appendArray(GameSource.COLLECTION_CONTENTS,
		        super.getTransferDataFlavors(), DataFlavor.class);
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
//  the flavor COLLECTION_CONTENTS serves as a reference to jose's DATABASE clipboard
//  it does not contain actual data
		return flavor.equals(GameSource.COLLECTION_CONTENTS) ||
		        super.isDataFlavorSupported(flavor);
	}
}
