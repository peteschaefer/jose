/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.db;

import de.jose.Application;
import de.jose.Config;

import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectionPool
{
    /** set of free connections */
    protected ArrayList free;
    /** set of occupied connections */
    protected ArrayList occupied;

    public ConnectionPool(int initialSize)
        throws SQLException
    {
	    free = new ArrayList(initialSize);
	    occupied = new ArrayList();

        while (initialSize-- > 0)
            free.add(create(null));
    }

    public int size()
    {
        return free.size()+occupied.size();
    }

	public boolean isEmpty()
	{
		return size()==0;
	}

    public synchronized JoConnection get()
        throws SQLException
    {
        JoConnection result;
        if (free.isEmpty())
            result = create(null);
        else {
            result = (JoConnection)free.remove(free.size()-1);
        }
//        result.initThread();
        occupied.add(result);
//	System.err.println("	pool: "+occupied.size()+" / "+free.size());
	    return result;
    }

    public synchronized void release(JoConnection conn)
    {
        if (conn != null) {
            occupied.remove(conn);
            free.add(conn);
        }
//	System.err.println("	pool: "+occupied.size()+" / "+free.size());
    }

    public void remove(JoConnection conn)
    {
        if (conn != null) {
            occupied.remove(conn);
            free.remove(conn);
        }
    }

    public void removeAll()
    {
       while (!occupied.isEmpty())
            remove((JoConnection)occupied.get(0));
       while (!free.isEmpty())
             remove((JoConnection)free.get(0));
    }

	public void closeAll()
	{
	   while (!occupied.isEmpty()) {
		   JoConnection conn = (JoConnection)occupied.get(0);
		   conn.close();
		   remove(conn);
	   }
	   while (!free.isEmpty()) {
		   JoConnection conn = (JoConnection)free.get(0);
		   conn.close();
		   remove(conn);
	   }
       occupied.clear();
       free.clear();
	}



    public JoConnection create(DBAdapter adapter)
        throws SQLException
    {
		JoConnection conn = new JoConnection(adapter,"pooled.connection");
		conn.setAutoCommit(false);
	    return conn;
    }
}
