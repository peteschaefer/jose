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

import javax.swing.*;

public class CommandAction
{
	protected int flags;

	/** has a DO method */
	public static final int	DO			    = 0x01;
	/** can be undone once  */
	public static final int	UNDO_ONE	    = 0x02;
	/** can be undone many times    */
	public static final int	UNDO_MANY	    = 0x04;
	/** can be redone   */
	public static final int	REDO		    = 0x08;
	/** execute at end of event loop    */
	public static final int INVOKE_LATER    = 0x10;
	/** execute in new thread (for long-running actions)    */
	public static final int NEW_THREAD      = 0x20;

	public static final int UNDO_MANY_REDO	= DO+UNDO_MANY+REDO;
	public static final int UNDO_ONE_REDO	= DO+UNDO_ONE+REDO;

	public CommandAction()					{ this(DO);	}

	public CommandAction(int flags)			{ this.flags = flags; }

	public boolean canDo()					{ return Util.anyOf(flags,DO); }

	public boolean canUndo()				{ return Util.anyOf(flags,UNDO_ONE+UNDO_ONE); }

	public boolean canUndoMany()			{ return Util.anyOf(flags,UNDO_MANY); }

	public boolean canRedo()				{ return Util.anyOf(flags,REDO); }

	/**
	 * if handling of an action should be passed to another agent...
	 * note that this method should NOT handle the command
	 * (because it may be called more frequently)
	 *
	 * the forward a command after some processing has been done, use CommandDispatcher.forward()
	 *
	 * @return  (1) current, if the current CommandListener should handle the command
	 *          (2) null if the command should be aborted (not handled at all),
	 *          (3) a forwarded command listener
	 */
	public CommandListener forward(CommandListener current)
	{
		/** by default, handle command now  */
		return current;
	}


	/**
	 * this method returns whether this command is enabled at all
	 *
	 * @return true if the command is available
	 */
	public boolean isEnabled(String cmd)			{ return true; }



	/**	this method is used by JoMenuBar to determine if a menu items is checked
	 *
	 * @return true is the menu item is selected (checked)
	 */
	public boolean isSelected(String cmd)			{ return false; }


	/**	this method is called by Command Listener to handle a command	*/
	public void Do(Command cmd)	throws Exception	{ /*	overwrite !	*/ }

	/**	this method is called by Command Listener to undo a command	*/
	public void Undo(Command cmd) throws Exception	{ /*	overwrite !	*/ }

	public void execute(Command cmd) throws Exception
	{
		if (Util.anyOf(flags,INVOKE_LATER))
			DoLater(cmd);
		else if (Util.anyOf(flags,INVOKE_LATER))
			DoThread(cmd);
		else
			Do(cmd);
	}

	public void DoLater(final Command cmd)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					Do(cmd);
				} catch (Exception ex) {
					/** where to throw to ? */
					Application.error(ex);
				}
			}
		});
	}

	public void DoThread(final Command cmd)
	{
		Thread thr = new Thread() {
			public void run() {
				try {
					Do(cmd);
				} catch (Exception ex) {
					/** where to throw to ? */
					Application.error(ex);
				}
			}
		};
		thr.start();
	}

	/**	called when an undoable action is not used anymore	 */
	public void finish() throws Exception			{ }

	public String getDisplayText(String code)		{ return Language.get(code); }

	public String getToolTipText(String code)		{ return getDisplayText(code); }
}
