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

package de.jose.jo3d;

import javax.media.j3d.Switch;
import java.util.BitSet;
import java.util.Vector;

/**
 *	a switch behavior that sends callbacks to registered listeners
 *
 * 	listeners implement the ICallbackListener interface
 *
 *	@author Peter Schäfer
 */

public class CallbackSwitch
		extends Switch
{
	protected Vector listeners = new Vector();

	/**	message code when a node is switched on	*/
	public static final int SWITCH_ON		= 2001;
	/**	message code when a node is switched off	*/
	public static final int SWITCH_OFF		= 2002;

	public CallbackSwitch()
	{
		super();
		setCapability(Switch.ALLOW_CHILDREN_READ);
		setCapability(Switch.ALLOW_SWITCH_READ);
		setCapability(Switch.ALLOW_SWITCH_WRITE);
	}

	/**
	 * @param which
	 */
	public void setWhichChild(int which)
	{
		if (which >= numChildren()) which = numChildren()-1;

		BitSet current = getChildMask();

		super.setWhichChild(which);

		for (int i=0; i < current.size(); i++)
			if (i==which)
			{
				if (!current.get(i))	notifyListeners(SWITCH_ON, getChild(i));
			}
			else
			{
				if (current.get(i))		notifyListeners(SWITCH_OFF, getChild(i));
			}

	}

	public void setChildMask(BitSet which)
	{
		BitSet current = getChildMask();

		super.setChildMask(which);

		for (int i=0; i < current.size(); i++)
			if (which.get(i))
			{
				if (!current.get(i))	notifyListeners(SWITCH_ON, getChild(i));
			}
			else
			{
				if (current.get(i))		notifyListeners(SWITCH_OFF, getChild(i));
			}

	}

	public void addListener(ICallbackListener list)
	{
		if (!listeners.contains(list))
			listeners.add(list);
	}

	public void removeListener(ICallbackListener list)
	{
		listeners.remove(list);
	}

	public void notifyListeners(int actionCode, Object params)
	{
		for (int i=0; i<listeners.size(); i++)
			((ICallbackListener)listeners.get(i)).behaviorCallback(this, actionCode, params);
	}

}
