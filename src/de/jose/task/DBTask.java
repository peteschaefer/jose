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

package de.jose.task;

import java.sql.SQLException; 

import de.jose.Application;
import de.jose.comm.Command;
import de.jose.db.JoConnection;

/**
 * a Task that works on a separaetd database connection
 */

public class DBTask
		extends Task
{
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------
	
	/**	the database connection
	 *	(either shared or exclusively used by this Thread)
	 */
	protected JoConnection connection;
	
	/**	is the connection shared, or exclusively used by this thread	 */
	protected boolean shared;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------
	
	/**	
	 *	create a Task that automatically allocates a new database connection,
	 *	and closes it upon completion
	 */
	public DBTask(String name, boolean autoCommit)
		throws SQLException
	{
		super(name);
		shared = false;
		connection = JoConnection.get();
		/* make sure the name is unique	*/
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * create a Task that works on the given database connection
	 * 
	 * (please note that additional care must be taken to ensure
	 *	transaction isolation & rollback)
	 */
	public DBTask(String name, JoConnection conn)
	{
		super(name);
		connection = conn;
		shared = true;
	}
	
	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------
	
	/**	@return the associated database connection
	 */
	public final JoConnection getConnection()	{ return connection; }

	public void requestAbort() throws SQLException
	{
		super.requestAbort();
		if (connection!=null) connection.cancelQuery();
	}

	public int init() throws Exception
	{
//        if (connection!=null) connection.initThread();
		return super.init();
	}

	/**	commit work after successful execution
	 */
	public void commit()
		throws Exception
	{
		if (connection!=null) connection.commit();
	}
	
	
	/**	rollback work after abortion
	 */
	public void rollback()
		throws Exception
	{
		if (connection!=null) connection.rollback();
	}
	
	/**	rollback and ignore errors
	 */
	public void tryRollback()
	{
		try {
			rollback();
		} catch (Exception ex) {
			/*	can't help it	*/
		}
	}
	
	/**
	 * perform any necessary cleanup
	 * @param state the state of the task

     */
	public int done(int state)
	{
		try {
			if (state==SUCCESS)
				commit();
			else
				rollback();

            state = super.done(state);

		} catch (Exception ex) {
			state = ERROR;	
			tryRollback();
		} finally {		
			if (!shared) {
				try {
					connection.cancelQuery();
				} catch (SQLException e) {					
				}
				connection.release();
				connection = null;
			}
		}
        return state;
	}


    //  update counter
    private static int runningUpdates = 0;

    public static String    COMMAND_BEFORE_UPDATE       = "before.db.update";
    public static String    COMMAND_AFTER_UPDATE        = "after.db.update";

    public static int broadcastOnUpdate(String taskName)
    {
        sendBroadcast(COMMAND_BEFORE_UPDATE, taskName, new Integer(++runningUpdates));
        return runningUpdates;
    }

    public static int currentUpdates()
    {
        return runningUpdates;
    }

    public static int broadcastAfterUpdate(int collectionId)
    {
        sendBroadcast(COMMAND_AFTER_UPDATE,
                (collectionId <= 0) ? null : new Integer(collectionId),
                new Integer(--runningUpdates));
        return runningUpdates;
    }


	protected static void sendBroadcast(String message, Object data, Object moredata)
	{
		Command cmd = new Command(message, null, data, moredata);
		Application.theApplication.broadcast(cmd);
	}

}
