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



/**
 * interface for a listener to CallbackBehavior
 * 
 */
public interface ICallbackListener
{
	
	/**	called when the behavior wakes up	 */
	public static final int ACTIVATE	= 1;
	
	/**	called when the behavior finishes	 */
	public static final int DEACTIVATE	= 2;
	
	/**	called by a CallbackBehavior
	 */
	public void behaviorCallback(Object source, int actionCode, Object params);
}
