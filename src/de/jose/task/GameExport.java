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

import de.jose.chess.Position;
import de.jose.sax.AbstractObjectReader;
import de.jose.sax.GameXMLReader;
import de.jose.pgn.Game;
import de.jose.profile.UserProfile;
import de.jose.export.ExportContext;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract public class GameExport
        extends GameTask
        /**	consider deriving from MaintenanceTask	TODO	*/
{
    /** output file */
    protected Object output;
    /** file type   */
    protected int type;
    /** replay position */
//    protected Position pos;


	public GameExport(String name, Object outputFile, int fileType)
        throws Exception
    {
        super(name,true);
        output = outputFile;
        type = fileType;
/*
        pos = new Position();
	    pos.setOption(Position.INCREMENT_HASH, false);
	    pos.setOption(Position.INCREMENT_REVERSED_HASH, false);
	    pos.setOption(Position.EXPOSED_CHECK, false);
	    pos.setOption(Position.STALEMATE, false);
	    pos.setOption(Position.DRAW_3, false);
	    pos.setOption(Position.DRAW_50, false);
*/  }

	public Source createSAXSource(ExportContext context)
	{
		if (context.source==null) throw new IllegalArgumentException(); //  would crash the XSLT transformer
		return new SAXSource(new GameXMLReader(getConnection(),context,this), context.source);
	}

	/**
	 * create a DOM model
	 * @return the number of games (=GAME elements in the Document)
	 * @throws Exception
	 *
    public int createDOM() throws Exception
    {
		/** create the source DOM   *
		if (source.isSingleGame()) {
			doc.addGame(source.getId());
			return 1;
		}

		if (source.isSingleCollection())
			return doc.addAllDeferredGames(source.getId(),0);

		if (source.isGameSelection()) {
			DBSelectionModel select = source.getSelection();
			int i = select.getMinSelectionIndex();
			int i1 = select.getMaxSelectionIndex();
			int total = 0;

			for ( ; i <= i1; i++)
				if (select.isSelectedIndex(i)) {
					int id = select.getDBId(i);
					doc.addDeferredGame(id,total);
					total++;
				}

			return total;
		}

		if (source.isCollectionSelection()) {
			DBSelectionModel select = source.getSelection();
			int i = select.getMinSelectionIndex();
			int i1 = select.getMaxSelectionIndex();
			int total = 0;

			for ( ; i <= i1; i++)
				if (select.isSelectedIndex(i)) {
					int id = select.getDBId(i);
					total += doc.addAllDeferredGames(id,total);
				}

			return total;
		}

		throw new IllegalStateException();
    }
*/
}
