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

package de.jose.util.file;

import java.io.*;
import java.lang.reflect.*;

/**
 *	overwrites ObjectInputStream
 *	so that evolved Classes can be read from a stream
 *	<b>bypassing</b> the default serial version id mechanism
 *
 *	@author Peter Schäfer
 */

public class XObjectInputStream
		extends ObjectInputStream
{
	public XObjectInputStream(InputStream in)
		throws IOException, StreamCorruptedException
	{
		super(in);
	}

	protected XObjectInputStream()
		throws IOException, SecurityException
	{
		super();
	}

	protected ObjectStreamClass readClassDescriptor()
		throws IOException, ClassNotFoundException
	{
		ObjectStreamClass streamDesc = super.readClassDescriptor();
		/*	the <b>stream</b> descriptor is replaced by the <b>local</b> descriptor	*/
		Class localClass = Class.forName(streamDesc.getName());
		ObjectStreamClass localDesc = ObjectStreamClass.lookup(localClass);

		if (streamDesc.getSerialVersionUID() != localDesc.getSerialVersionUID()) {
			/*	usually, this would cause an exception to be thrown
				by setting the <b>local</b> descriptor we effectively disable the version check
			*/
			System.out.println(" serial version mismatch for "+localClass.getName()+" (no reason to worry) ");
			setSerialVersionUID(streamDesc, localDesc.getSerialVersionUID());
		}

		return streamDesc;
	}

	/**	kind of dirty, but what can you do...	 */
	private void setSerialVersionUID(ObjectStreamClass desc, long uid)
	{
		try {
			Field field = ObjectStreamClass.class.getDeclaredField("suid");
			field.setAccessible(true);
			field.set(desc, new Long(uid));
		} catch (Exception ex) {
			/*	at least we tried ... */
			System.out.println(" could not set suid (now you have a reason to worry)");
		}
	}
}
