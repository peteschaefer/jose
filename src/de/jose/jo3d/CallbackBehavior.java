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

import javax.media.j3d.Behavior;
import javax.swing.*;
import java.util.Vector;

/**
 * a behaviour that sends callback messages to a set of Listeners
 */
abstract public class CallbackBehavior
		extends Behavior
{
	protected Vector listeners = new Vector();

	
	public void addListener(ICallbackListener list)
	{
		if (!listeners.contains(list))
			listeners.add(list);
	}
	
	public void removeListener(ICallbackListener list)
	{
		listeners.remove(list);
	}
	
	public void notifyListeners(int actionCode, Object params, boolean delayed)
	{
		if (delayed)
			SwingUtilities.invokeLater(new NotifyLater(actionCode,params));
		else
			notifyListeners(actionCode,params);
	}

	public void notifyListeners(int actionCode, Object params)
	{
		for (int i=0; i<listeners.size(); i++)
			((ICallbackListener)listeners.get(i)).behaviorCallback(this, actionCode, params);
	}

	class NotifyLater implements Runnable
	{
		int actionCode;
		Object params;

		NotifyLater(int actionCode, Object params)
		{
			this.actionCode = actionCode;
			this.params = params;
		}

		public void run() {
			notifyListeners(actionCode,params);
		}
	}
}
