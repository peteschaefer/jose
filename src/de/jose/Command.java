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

package de.jose;



public class Command
{
	/**	string identifying this command	 */
	public String			code;
	/**	event that triggered it	(optional) */
	public Object			event;
	/**	addtional data	 */
	public Object			data;
	public Object			moreData;

	/** target for this command	 */
	public CommandListener	target;
	/**	listener that actually handled it	*/
	public CommandListener	handler;
	/**	action that was triggered by this command	*/
	public CommandAction	action;

	public Command(String aCode)
	{
		this(aCode,null,null,null);
	}

	public Command(String aCode, Object anEvent)
	{
		this(aCode,anEvent,null,null);
	}

	public Command(String aCode, Object anEvent, Object aData)
	{
		this(aCode,anEvent,aData,null);
	}

	public Command(String aCode, Object anEvent, Object aData, Object anotherData)
	{
		code = aCode;
		event = anEvent;
		data = aData;
		moreData = anotherData;
	}
}
