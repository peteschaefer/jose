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
import de.jose.Config;
import de.jose.window.JoDialog;

import java.awt.*;
import java.sql.SQLException;

import org.xml.sax.SAXException;

/**
 * Database cross-over for Meta Version 1006
 *
 * * GamePlayer dropped
 *   (MySQL 5.0 has an improved optimizer, which makes this old kludge unnecessary)
 *
 * * new columns MoreGame.PosMain and PosVar
 *   for positional indexes (not yet in use, but will be soon)
 *
 * @author Peter Schäfer
 */

public class CrossOver1006
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			if (version < 1006) {
				Setup setup = new Setup(config,"MAIN",conn);

				// ----------------------------------------------------
				//  Drop GamePlayer
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
				        "jose will now update the database structure.\n" +
				        "This may take up to some minutes.",
				        false);
				dlg.show();
				dlg.paint(dlg.getGraphics());

				setup.dropTable("GamePlayer");

				// ----------------------------------------------------
				//  Drop Column UpperName
				// ----------------------------------------------------

				dropUpperName(conn, setup, "Opening", 4);
				dropUpperName(conn, setup, "Player", 2);
				dropUpperName(conn, setup, "Event", 2);
				dropUpperName(conn, setup, "Site", 2);

				// ----------------------------------------------------
				//  New Columns MoreGame.PosMain, MoreGame.PosVar
				// ----------------------------------------------------

				if (Setup.getTableVersion(conn,"MAIN","MoreTable") < 101) {
					try {
						setup.dropColumn("MoreGame","PosMain");
					} catch (SQLException e) { }
					try {
						setup.dropColumn("MoreGame","PosVar");
					} catch (SQLException e) { }
//					setup.addColumn("MoreGame","PosMain", 7);
//					setup.addColumn("MoreGame","PosVar", 8);
					conn.executeUpdate(
							"ALTER TABLE MoreGame " +
							" ADD COLUMN (PosMain LONG VARCHAR)," +
							" ADD COLUMN (PosVar LONG VARCHAR)," +
							" ADD FULLTEXT INDEX MoreGame_7 (PosMain)," +
							" ADD FULLTEXT INDEX MoreGame_8 (PosVar)");
					conn.executeUpdate(
							"ALTER TABLE MoreGame " +
							" CHARSET "+Setup.DEFAULT_CHARSET +
							" COLLATE "+Setup.DEFAULT_COLLATE);

				Setup.setTableVersion(conn,"MAIN","MoreGame",101);
				}

				//  drop IO_MoreGame
				Setup iosetup = new Setup(config,"IO",conn);
				iosetup.dropTable("IO_MoreGame");
			}

			Setup.setSchemaVersion(conn,"MAIN",version=1006);
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
