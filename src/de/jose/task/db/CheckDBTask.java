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

import de.jose.task.DBTask;
import de.jose.task.io.PGNImport;
import de.jose.db.JoConnection;
import de.jose.db.Setup;
import de.jose.Application;
import de.jose.Config;

import java.sql.SQLException;

/**
 * @author Peter Schäfer
 */

public class CheckDBTask
        extends DBTask
{

	public CheckDBTask(JoConnection conn) throws SQLException
	{
		super("CheckIntegrity", conn);
		shared = false; //  do release this connection upon completion
	}

	public int work() throws Exception
	{
		//  update meta versions
		connection.setAutoCommit(true);
		checkMetaVersion(Application.theApplication.theConfig);

		setProgress(0.33);

		//  update index statistics
		Setup setup = new Setup(Application.theApplication.theConfig,"MAIN",connection);
		setup.analyzeTables(false);  //  if leftover from previous session...

		setProgress(0.66);

		//  enable keys (in case enabling failed the last time)
		PGNImport.enableKeys(connection);

		setProgress(1.0);
		return SUCCESS;
	}

	protected void checkMetaVersion(Config config)
		throws SQLException
	{
		int metaversion = Setup.getSchemaVersion(connection,"META");
		if (metaversion < config.getSchemaVersion("META"))
			JoConnection.getAdapter().crossoverSchema(connection,"META", metaversion, config);

		int dbversion = Setup.getSchemaVersion(connection,"MAIN");
		if (dbversion < config.getSchemaVersion("MAIN"))
			JoConnection.getAdapter().crossoverSchema(connection, "MAIN", dbversion, config);

        int ioversion = Setup.getSchemaVersion(connection,"IO");
        if (ioversion > 0)
        {
	        // schemas IO and IO_MAP need not be maintained; we just create them from scratch
	        Setup setup = new Setup(config,"IO",connection);
	        try {
	            setup.drop(true);
	        } catch (Exception e) {
	            Application.error(e);
	        }
        }

        int mapversion = Setup.getSchemaVersion(connection,"IO_MAP");
        if (mapversion > 0)
        {
	        // schemas IO and IO_MAP need not be maintained; we just create them from scratch
	        Setup setup = new Setup(config,"IO_MAP",connection);
	        try {
	            setup.drop(true);
	        } catch (Exception e) {
	            Application.error(e);
	        }
        }
	}

}
