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

import de.jose.task.MaintenanceTask;
import de.jose.task.GameSource;
import de.jose.util.AWTUtil;
import de.jose.pgn.SearchRecord;

/**
 * @author Peter Schäfer
 */

public class CrunchTask
        extends MaintenanceTask
{
    private SearchRecord srec;

	public CrunchTask(GameSource source, SearchRecord srec) throws Exception
	{
		super("Crunch",true);
		setSource(source);
        this.srec = srec;
	}


	public void processGame(int GId) throws Exception
	{
		AWTUtil.beep(null); //  makes no sense, does it ?
	}

	public void processGames(int[] GId, int from, int to) throws Exception
	{
        gutil.crunchGames(GId,from,to);
	}

	public void processCollection(int CId) throws Exception
	{
        if (srec!=null && srec.hasSortOrder())
            gutil.crunchCollection(CId,srec);
        else
		    gutil.crunchCollection(CId);
	}

	public void processCollectionContents(int CId) throws Exception
	{
		processCollection(CId);
	}
}
