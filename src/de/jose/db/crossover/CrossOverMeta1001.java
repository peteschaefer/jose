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

/**
 * @author Peter Schäfer
 */

public class CrossOverMeta1001
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		//  add new column META.Dirty
		conn.executeUpdate("ALTER IGNORE TABLE MetaInfo ADD COLUMN Dirty TINYINT");
		Setup.setSchemaVersion(conn,"META",version=1001);
		return version;
	}
}
