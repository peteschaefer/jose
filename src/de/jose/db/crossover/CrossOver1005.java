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
import de.jose.Application;
import de.jose.pgn.Collection;
import de.jose.window.JoDialog;

import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class CrossOver1005
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			if (version < 1005) {
				// ----------------------------------------------------
				//  Define Charset and collation
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
				        "jose will now update the database structure.\n" +
				        "This may take up to some minutes.",
				        false);
				dlg.show();
				dlg.paint(dlg.getGraphics());

				Setup setup = new Setup(config,"MAIN",conn);
				setup.setCharset(Setup.DEFAULT_CHARSET,Setup.DEFAULT_COLLATE);
				Setup.setSchemaVersion(conn,"MAIN",version=1005);

				setup = new Setup(config,"IO",conn);
				setup.drop(true);   //  will be re-created on demand

				setup = new Setup(config,"IO_MAP",conn);
				setup.drop(true);   //  will be re-created on demand
			}

			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}
}
