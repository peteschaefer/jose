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

import de.jose.Config;
import de.jose.Application;
import de.jose.db.JoConnection;
import de.jose.db.Setup;
import de.jose.pgn.Collection;
import de.jose.window.JoDialog;

import java.awt.*;

/**
 * CrossOver1003
 *
 * @author Peter Schäfer
 */

public class CrossOver1004
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			if (version < 1003) {
				// ----------------------------------------------------
				//  New Inde Game_16 on Game(CId,Idx,Id)
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
				        "jose will now update the database structure.\n" +
				        "This may take up to some minutes.",
				        false);
				dlg.show();
				dlg.paint(dlg.getGraphics());

				Setup setup = new Setup(config,"MAIN",conn);


				System.err.print("[create index Game_16");
				try {
					conn.executeUpdate("DROP INDEX Game_16 ON Game");
				} catch (Exception e) {
				}
				try {
					conn.executeUpdate("CREATE INDEX Game_16 ON Game (CId,Idx,Id)");
				} catch (Exception e) {
					Application.error(e);   //
				}
				System.err.println("]");

				Setup.setTableVersion(conn,"MAIN","Game",102);
				Setup.setSchemaVersion(conn,"MAIN",version=1003);
			}

            if (version < 1004) {
                Collection.updatePath(conn,0,true);
                Setup.setSchemaVersion(conn,"MAIN",version=1004);
            }

			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}
}