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


package de.jose.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * MySqlEmbeddedConnectionPool
 *
 * @author Peter Schäfer
 */

public class MySqlEmbeddedConnectionPool
        extends ConnectionPool
{
    public MySqlEmbeddedConnectionPool(String url, Properties props) {
        super(url, props);
    }


    public MySqlEmbeddedConnectionPool(String url, String user, String password) {
        super(url, user, password);
    }


    /**
     * get a connection for the currently running thread !
     * @return
     * @throws SQLException
     */
    public Connection get() throws SQLException
    {
        return get(Thread.currentThread());
    }
}