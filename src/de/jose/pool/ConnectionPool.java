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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * ConnectionPool
 * maintains a pool of named connections.
 * Connections be allocated by keys.
 *
 * @author Peter Schäfer
 */

public class ConnectionPool
{
    private static final Object DEFAULT_KEY = new Object();

    class Pool {
        Object key;
        LinkedList freeList;
        LinkedList activeList;
    }
    
    /** connection url  */
    protected String connectionUrl;
    /** connection properties   */
    protected Properties connectionProperties;
    
    /** maps keys to pools  */
    protected Hashtable poolTable;


    // ------------------------------------------------------------------
    //      Constructor
    // ------------------------------------------------------------------

    public ConnectionPool(String url, Properties props)
    {
        connectionUrl = url;
        connectionProperties = props;
        poolTable = new Hashtable();
    }

    public ConnectionPool(String url, String user, String password)
    {
        this(url,new Properties());
        connectionProperties.put("user",user);
        connectionProperties.put("password",password);
    }


    public Connection get(Object key)
        throws SQLException
    {
        Pool pool = (Pool)poolTable.get(key);
        if (pool==null) {
            pool = new Pool();
            pool.key = key;
            pool.freeList = new LinkedList();
            pool.activeList = new LinkedList();
        }
        if (pool.freeList.isEmpty())
            pool.freeList.addLast(newConnection(key));

        Connection connection = (Connection)pool.freeList.removeFirst();
        pool.activeList.addLast(connection);
        return connection;
    }

    public Connection get()
        throws SQLException
    {
        return get(DEFAULT_KEY);
    }

    public static void release(Connection conn)
    {
        if (conn instanceof PooledConnection)
            ((PooledConnection)conn).release();
        else
            throw new IllegalArgumentException("strange connection; does not belong to this pool");
    }

    public static void close(Connection conn) throws SQLException
    {
        conn.close();
    }

    public void closeAll(Object key)
    {
        Pool pool = (Pool)poolTable.get(key);
        if (pool!=null) closeAll(pool);
    }

    public void closeAll()
    {
        while (!poolTable.isEmpty())
        {
            Iterator i = poolTable.values().iterator();
            while (i.hasNext())
            {
                Pool pool = (Pool)i.next();
                i.remove();
                closeAll(pool);
            }
        }
    }

    // ------------------------------------------------------------------
    //      Private
    // ------------------------------------------------------------------

    protected PooledConnection newConnection(Object key)
        throws SQLException
    {
        Connection jdbcConnection = DriverManager.getConnection(connectionUrl, connectionProperties);
        PooledConnection pooledConnection = new PooledConnection(this,key,jdbcConnection);
        return pooledConnection;
    }

    public void releaseConnection(PooledConnection connection, Object key)
    {
        Pool pool = (Pool)poolTable.get(key);
        pool.activeList.remove(connection);
        pool.freeList.addLast(connection);
    }

    protected void finishConnection(PooledConnection connection, Object key)
    {
        Pool pool = (Pool)poolTable.get(key);
        pool.activeList.remove(connection);
        pool.freeList.remove(connection);
    }

    protected void closeAll(Pool pool)
    {
        if (pool!=null) {
            while (!pool.freeList.isEmpty())
            {
                PooledConnection conn = (PooledConnection)pool.freeList.removeFirst();
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            while (!pool.activeList.isEmpty())
            {
                PooledConnection conn = (PooledConnection)pool.activeList.removeFirst();
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            poolTable.remove(pool.key);
        }
    }
}