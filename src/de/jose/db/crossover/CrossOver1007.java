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

package de.jose.db.crossover;

import de.jose.db.JoConnection;
import de.jose.db.Setup;
import de.jose.db.JoStatement;
import de.jose.Config;
import de.jose.pgn.Game;
import de.jose.window.JoDialog;

import java.awt.*;
import java.sql.SQLException;

import org.xml.sax.SAXException;

/**
 * Database cross-over for Meta Version 100
 *
 * * Fix bug. Games could be created without MoreGame.
 *
 * @author Peter Schäfer
 */

public class CrossOver1007
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			if (version < 1007) {


				// ----------------------------------------------------
				//  Drop orphaned Game rows
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
				        "jose will now update the database structure.\n" +
				        "This may take up to some minutes.",
				        false);
				dlg.show();
				dlg.paint(dlg.getGraphics());

				conn.executeUpdate(
						"DELETE Game" +
						" FROM Game LEFT OUTER JOIN MoreGame ON Game.Id = MoreGame.GId" +
						" WHERE MoreGame.GId IS NULL");
			}

			Setup.setSchemaVersion(conn,"MAIN",version=1007);
			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}

	protected static void dropUpperName(JoConnection conn, Setup setup,
	                                    String tableName, int index)
	{
		//  Drop index
		try {
			setup.dropIndex(tableName,index);
		} catch (SQLException e) { }

		//  Drop column
		String alter = "ALTER TABLE "+tableName+
				" DROP COLUMN UpperName ";
		try {
			conn.executeUpdate(alter);
		} catch (SQLException e) { }
	}

}
