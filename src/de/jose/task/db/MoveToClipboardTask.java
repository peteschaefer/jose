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

package de.jose.task.db;

import de.jose.task.GameSource;

import java.awt.datatransfer.Clipboard;
import java.awt.*;


/**
 * Move to clipboard
 * before copying, empty the clipboard
 *
 */
public class MoveToClipboardTask
        extends MoveTask
{
	protected int trashCId;

	public MoveToClipboardTask(GameSource src, int clip, int trash, boolean setOId)
	        throws Exception
	{
		super(src, clip, setOId, false);
		trashCId = trash;
	}

	public void prepare()
		throws Exception
	{
		super.prepare();

		/*	empty the clipboard	*/
		gutil.moveCollectionContents(targetCId,trashCId,setOId,calcIdx);
    }

    public int finish() throws Exception
    {
        /** put PGN text into system clipboard (but no more than 100 games) */
        gutil.copyPGNtoSystemClipboard(targetCId, 100);

        return super.finish();
	}
}
