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

package de.jose.comm;

import de.jose.Application;
import de.jose.Version;
import de.jose.pgn.ReplayException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Vector;

public class CommandDispatcher
{
	/**	maps command listener to their CommandActions	*/
	protected HashMap listenerMap;
	protected boolean trace;

	/**	list of undo actions	*/
	protected Vector undoList;
	protected int currentUndo;

	public CommandDispatcher()
	{
		listenerMap = new HashMap();
		undoList = new Vector();
		currentUndo = -1;
		trace = Version.getSystemProperty("jose.debug.commands",false);
	}

	public void trace(boolean on)
	{
		trace = on;
	}

	public void addCommandListener(CommandListener list)
	{
		if (!listenerMap.containsKey(list))
		{
			HashMap map = new HashMap();
			list.setupActionMap(map);
			listenerMap.put(list,map);
		}
	}

	public void removeCommandListener(CommandListener list)
	{
		listenerMap.remove(list);
	}

	public boolean handle(Command cmd, CommandListener list)
	{
		cmd.target = list;

		return forward(cmd,list,true);
	}

	public boolean handle(ActionEvent event, CommandListener list)
	{
		/*	forward menu events to CommandListener	*/
		Command cmd = new Command(event.getActionCommand(), event, null);
		cmd.event = event;
		if (event.getSource() instanceof JComponent) {
			JComponent jcmp = (JComponent)event.getSource();
			cmd.data = jcmp.getClientProperty("action.data");
			cmd.moreData = jcmp.getClientProperty("action.more.data");
		}

		return handle(cmd, list);
	}

	public Object[] findTarget(Command cmd, CommandListener list, boolean onlyEnabled)
	{
		return findTarget(cmd.code, list, cmd.handler, onlyEnabled);
	}


	public Object[] findTarget(String code, CommandListener list, CommandListener source, boolean onlyEnabled)
	{
		while (list != null)
		{
			HashMap actionMap = (HashMap)listenerMap.get(list);
			CommandAction action = (CommandAction)actionMap.get(code);
			if (action != null && (!onlyEnabled || action.isEnabled(code)) && source != list)
			{
				CommandListener other = action.forward(list);
				if (other==null) {
					/**/;    //  don't handle
				}
				else if (other!=list) {
					list = other;   //  forward
					continue;
				}
				else
					return new Object[] { list,action };    //  handle by this action
			}

			//	climb up the hierarchy
			list = list.getCommandParent();
		}
		return null;
	}

	public boolean forward(Command cmd, CommandListener list)
	{
		/** if explicitly forwarded, don't check for isEnabled()
		 *  could lead to events not being handled
		 *  (though one could argue...)
		 */
		return forward(cmd,list,false);
	}

	public boolean forward(Command cmd, CommandListener list, boolean onlyEnabled)
	{
		Object[] target = findTarget(cmd,list,onlyEnabled);

		if (target!=null)
		{
			DoIt(cmd,target);
			return true;
		}
		else
			return false;
	}

	public int broadcast(Command cmd, CommandListener list)
	{
		cmd.target = list;

		return doBroadcast(cmd,list);
	}

	private int doBroadcast(Command cmd, CommandListener list)
	{
		int result = 0;

		if (cmd.handler != list)
		{
			/*	handle at root (don't percolate upwards)	*/
			if (broadcast1(list,cmd))
				result++;
		}

		/**	broadcast to children	*/
		int numChildren = list.numCommandChildren();
		for (int i=0; i<numChildren; i++)
		{
			CommandListener child = list.getCommandChild(i);
			if (child != null)
				result += doBroadcast(cmd,child);
		}

		return result;
	}

	private boolean broadcast1(CommandListener list, Command cmd)
	{
		HashMap actionMap = (HashMap)listenerMap.get(list);
		CommandAction action = null;

		if (actionMap!=null)
			action = (CommandAction)actionMap.get(cmd.code);

		if (action != null && action.isEnabled(cmd.code) && action.canDo())
		{
			DoIt(cmd,list,action);
		}
		else
			action = null;

		CommandAction onAction = null;
		if (actionMap!=null)
			onAction = (CommandAction)actionMap.get("on.broadcast");
		else
			onAction = null;

		if (onAction != null && onAction.isEnabled(cmd.code))
			try {
				onAction.execute(cmd);
			} catch (Exception ex) {
				Application.error(ex);
			}

		return action!=null;
	}


	public CommandAction findTargetAction(String code, CommandListener list)
	{
		Object[] target = findTarget(code,list,null,false);
		if (target!=null)
			return (CommandAction)target[1];
		else
			return null;
	}

	public boolean isEnabled(String code, CommandListener list)
	{
		return findTarget(code,list,null,true) != null;
	}

/*

	public boolean isSelected(String code, CommandListener list)
	{
		Object[] target = findTarget(code,list,null,true);
		if (target!=null)
		{
			CommandAction action = (CommandAction)target[1];
			return action.isSelected(code);
		}
		else
			return false;
	}

	public String getDisplayText(String code, CommandListener list)
	{
		Object[] target = findTarget(code,list,null,false);
		if (target!=null)
		{
			CommandAction action = (CommandAction)target[1];
			return action.getDisplayText(code);
		}
		else
			return Language.get(code);
	}

	public String getToolTipText(String code, CommandListener list)
	{
		Object[] target = findTarget(code,list,null,false);
		if (target!=null)
		{
			CommandAction action = (CommandAction)target[1];
			return action.getToolTipText(code);
		}
		else
			return Language.getTip(code);
	}
*/


	protected void DoIt(Command cmd, Object[] array)
	{
		CommandListener handler = (CommandListener)array[0];
		CommandAction action = (CommandAction)array[1];
		DoIt(cmd,handler,action);
	}

	protected void DoIt(Command cmd, CommandListener handler, CommandAction action)
	{
		cmd.handler = handler;
		cmd.action = action;

		if (trace)
			traceAction(cmd,handler,action);

		try {
			action.execute(cmd);

			if (action.canUndo()) {
				// @deprecated
				if (! undoList.isEmpty()) {
					//	finish remaining items
					for (int i=currentUndo+1; i < undoList.size(); i++)
						try {
							((Command)undoList.get(i)).action.finish();
						} catch (Exception ex) {
							Application.error(ex);
						}
					undoList.setSize(currentUndo+1);
				}
				if (!action.canUndoMany()) {
					//	get rid of previous items
					for (int i=currentUndo; i>=0; i--) {
						cmd = (Command)undoList.get(i);
						if (cmd.action==action) {
							try {
								cmd.action.finish();
							} catch (Exception ex) {
								Application.error(ex);
							}
							undoList.remove(i);
						}
					}
				}

				undoList.add(cmd);
				currentUndo = undoList.size()-1;
			}

			if (cmd.event!=null && cmd.event instanceof ActionEvent) {
				Object source = ((ActionEvent)cmd.event).getSource();
				if (source instanceof JButton) {
					((JButton)source).setEnabled(action.isEnabled(cmd.code));
				}
				if (source instanceof JMenuItem) {
					((JMenuItem)source).setEnabled(action.isEnabled(cmd.code));
				}
			}

		} catch (ReplayException rpex) {
			Application.warning(rpex);  //  hack: don't report to GUI
		} catch (Throwable ex) {
			Application.error(ex);
		}
	}

	public boolean canUndo() {
		return currentUndo >= 0;
	}

	public boolean canRedo() {
		return currentUndo < (undoList.size()-1);
	}

	public Command getUndoCommand()
	{
		return (Command)undoList.get(currentUndo);
	}

	public CommandAction getUndoAction()
	{
		return getUndoCommand().action;
	}

	public Command getRedoCommand()
	{
		return (Command)undoList.get(currentUndo+1);
	}

	public CommandAction getRedoAction()
	{
		return getRedoCommand().action;
	}

	public void Undo() {
		if (!canUndo()) throw new IllegalStateException();

		Command cmd = getUndoCommand();
		try {
			getUndoAction().Undo(cmd);
		} catch (Exception ex) {
			Application.error(ex);
		}
		currentUndo--;
	}

	public void Redo() {
		if (!canRedo()) throw new IllegalStateException();

		Command cmd = getRedoCommand();
		try {
			getRedoAction().execute(cmd);
		} catch (Exception ex) {
			Application.error(ex);
		}
		currentUndo++;
	}

	protected void traceAction(Command cmd, CommandListener list, CommandAction action)
	{
		traceAction(cmd.code,cmd.data, list,action);
	}

	protected void traceAction(String code, Object data, CommandListener list, CommandAction action)
	{
		System.err.print("[");
		System.err.print(code);
		if (data!=null) {
			System.err.print("(");
			String dstr = data.toString();
			if (dstr.length() > 20)
				dstr = dstr.substring(0,17)+"...";
			System.err.print(dstr);
			System.err.print(")");
		}
		System.err.print(", ");
		System.err.print(list.getClass().getName());
		System.err.print(" -> ");
		System.err.print(action.getClass().getName());
		System.err.println("]");
	}
}
