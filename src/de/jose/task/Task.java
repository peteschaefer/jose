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

package de.jose.task;

import de.jose.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/**
 *	models a Thread that displays its progress on a progress bar
 *	returns some info about successful execution
 *	CommandListeners may be notified
 * 
 */

abstract public class Task
				extends Thread
				implements ActionListener
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	mask for task state	 */
	protected static final int STATE_MASK		= 0x000f;

	/**	state:	task is running	 */
	protected static final int RUNNING			= 0x0001;
	/**	state:	task was succesfully finished	 */
	protected static final int SUCCESS			= 0x0002;
	/**	state:	task reported failure	 */
	protected static final int FAILURE			= 0x0003;
	/**	statet:	task produced an exception	 */
	protected static final int ERROR			= 0x0004;
	/**	state:	task was aborted by user or another instance	 */
	protected static final int ABORTED			= 0x0005;

	/**	state constant:	user requested abort	 */
	protected static final int ABORT_REQUESTED	= 0x0080;

	/**	returned by getProgress: just started	 */
	protected static final double PROGRESS_START	= 0.0;
	/**	returned by getProgress: finished	 */
	protected static final double PROGRESS_END		= 1.0;
	/**	returned by getProgress: not known	 */
	protected static final double PROGRESS_UNKNOWN	= -1.0;

	/**	parameter to setSilentTime: immediately show progress dialog	 */
	protected static final long	SHOW_PROGRESS			= 0L;
	/**	parameter to setSilentTime: never show progress dialog	 */
	protected static final long	DONT_SHOW_PROGRESS		= Long.MAX_VALUE;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	time when this task was started	 */
	protected long startTime;
	/**	command that will be issued when the task succesfully ends	 */
	protected Command onSuccess;
	protected Runnable runOnSuccess;
	/**	command that will be issued when the task aborts	 */
	protected Command onFailure;
	protected Runnable runOnFailure;
	/**	time to wait before showing a progress dialog	 */
	protected long silentTime;
	/**	state flags	 */
	protected int state;
	/**	current progress (0..1)	*/
	protected double progress;

	/**	progress dialog	 */
	protected ProgressDialog dialog;
	/**	text that will be shown in progress dialog (defaults to task name)	 */
	protected String progressText;
	/**	title of progress dialog (defaults to task name)	 */
	protected String progressTitle;

	/**	associated AWT component (e.g. for setting Wait cursor); optional	*/
	protected Component displayComponent;

	/**	report errors to the application ?
	 * 	errors are always displayed to the user. this flag indicates whether they ared logged, too
	 */
	protected boolean reportErrors;
	/**	poll progress in regular intervals ?
	 * 	default is false: progress is only updated after work()
	 * 	if true: progress is polled regularly
	 */
	protected int pollProgress;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public Task(String aName) {
		super(aName);
		state = 0;
		progressText = getName();
		progressTitle = getName();
		reportErrors = true;
		pollProgress = 0;
	}

	public final int getTaskState()						{ return state & STATE_MASK; }

	public final boolean isRunning()					{ return getTaskState()==RUNNING; }
	public final boolean wasSuccess()					{ return getTaskState()==SUCCESS; }
	public final boolean wasFailure()					{ return getTaskState()==FAILURE; }
	public final boolean wasError()						{ return getTaskState()==ERROR; }
	public final boolean wasAborted	()					{ return getTaskState()==ABORTED; }

	public void requestAbort() throws SQLException					        { state = Util.plus(state,ABORT_REQUESTED); }
	public final boolean isAbortRequested()             { return Util.allOf(state,ABORT_REQUESTED); }

	public final void setOnSuccess(Command cmd)			{ onSuccess = cmd; }
	public final void setOnFailure(Command cmd)			{ onFailure = cmd; }

	public final void setOnSuccess(Runnable run)		{ runOnSuccess = run; }
	public final void setOnFailure(Runnable run)		{ runOnFailure = run; }

	public final long getStartTime()					{ return startTime; }

	public final long getElapsedTime() 					{ return System.currentTimeMillis()-startTime; }


	public final void setSilentTime(long millis)		{ silentTime = millis; }

	public final void startLater(int delay)
	{
		Timer tm = new Timer(delay,this);
		tm.setRepeats(false);
		tm.setCoalesce(false);
		tm.start();
	}

	public final void throwAborted()
	{
		if (isAbortRequested()) throw new TaskAbortedException();
	}

	public final ProgressDialog getProgressDialog()		{ return dialog; }

	public void setProgressDialogVisible(boolean visible) {
		if (dialog!=null)
			dialog.setVisible(visible);
		else if (visible) {
			createProgressDialog(pollProgress);
			dialog.setVisible(visible);
		}
	}

	public final void showProgressDialog()			{ setProgressDialogVisible(true); }
	public final void hideProgressDialog()			{ setProgressDialogVisible(false); }

	public final void setProgressText(String text)	{
		progressText = text;
		if (dialog!=null)
			dialog.setText(text);
	}


	public final void setProgressTitle(String title)	{
		progressTitle = title;
		if (dialog!=null) {
			dialog.setName(title);
			dialog.setTitle(Language.get(title));
		}
	}

	public final Component getDisplayComponent() {
		return displayComponent;
	}

	public final void setDisplayComponent(Component displayComponent) {
		this.displayComponent = displayComponent;
	}

	public final void setWaitCursor()
	{
		if (displayComponent != null)
			displayComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	public final void setDefaultCursor()
	{
		if (displayComponent != null)
			displayComponent.setCursor(Cursor.getDefaultCursor());
	}


	/**	called when the task is started,
	 *	overwrite if you like
	 */
	public int init()
		throws Exception
	{
		progress = PROGRESS_UNKNOWN;
		return RUNNING;
	}

	/*	@return the approximate progress state (0.0 = just started, 1.0 = finished), 
			< 0 if unknown
			should be thread-safe
	*/
	public double getProgress()
	{	/* overwrite */
		return progress;
	}

	/**	@return text to show inside the progress bar
	 */
	public String getProgressText()
	{
		return null;
	}

	public void setProgress(double value)
	{
		progress = value;

		//	update progress dialog
		if (dialog==null && (System.currentTimeMillis() >= (startTime+silentTime)))
			createProgressDialog(pollProgress);

		if (dialog!=null && dialog.isShowing())
			dialog.updateBar();
	}

	/**
	 *	do a chunk of work	
	 *	@return RUNNING, SUCCESS, FAILURE, or ERROR
	 *  */
	public int work()
		throws Exception
	{
		/*	overwrite ! */
		return RUNNING;
	}

	/**
	 * perform any necessary cleanup
	 * @param state the state of the task

     */
	public int done(int state)
	{
		if (state==SUCCESS) {
			if (runOnSuccess!=null) try {
				runOnSuccess.run();
			} catch (Exception ex) {
				Application.error(ex);
			}
			if (onSuccess!=null) {
				if (onSuccess.target!=null)
					AbstractApplication.theCommandDispatcher.handle(onSuccess,onSuccess.target);
				else
					AbstractApplication.theCommandDispatcher.broadcast(onSuccess,Application.theApplication);
			}
		}
		else {
			if (runOnFailure!=null) try {
				runOnFailure.run();
			} catch (Exception ex) {
				Application.error(ex);
			}
			if (onFailure!=null) {
				if (onFailure.target!=null)
					AbstractApplication.theCommandDispatcher.handle(onFailure,onFailure.target);
				else
					AbstractApplication.theCommandDispatcher.broadcast(onFailure,Application.theApplication);
			}
		}
		return state;
	}


	public void run()
	{
		String errorText = "";
		Timer dialogTimer = null;

		startTime = System.currentTimeMillis();
		if (silentTime<=0)
			createProgressDialog(pollProgress);

		state = 0;

		try {
			try {
				state = init();
			} catch (TaskAbortedException abortex) {
				state = ABORTED;
			} catch (Throwable ex) {
				state = ERROR;
                if (reportErrors) Application.reportError(ex,true,false);
				errorText = ex.toString();
			}

			if (silentTime > 0 && silentTime < Integer.MAX_VALUE) {
				dialogTimer = new Timer((int)silentTime,this);
				dialogTimer.setInitialDelay((int)silentTime);
				dialogTimer.setRepeats(false);
				dialogTimer.start();
			}

			while (isRunning()) {
				int result;

				try {
					result = work();
				} catch (TaskAbortedException abortex) {
					result = state = ABORTED;
				} catch (Throwable ex) {
					state = ERROR;
					if (reportErrors) Application.reportError(ex,true,false);
					errorText = ex.toString();
					break;
				}

				if (result!=RUNNING) {
					state = result;
					break;
				}

				if (isAbortRequested()) {
					state = ABORTED;
					break;		//	we have been aborted
				}

				setProgress(progress);
			}


		} finally {
            closeDialog(dialogTimer,errorText);
			state = done(getTaskState());
//			Util.printTime(this.getName(),startTime);
		}
	}

    protected void closeDialog(Timer dialogTimer, final String errorText)
    {
        if (dialogTimer!=null)
            dialogTimer.stop();

	    if (dialog!=null) dialog.stopPoll();

        if (wasError()) {
            /*	error: leave dialog open to show error message	*/
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (dialog==null && silentTime<Integer.MAX_VALUE)
                        createProgressDialog(0);
                    dialog.showError(errorText);
                }
            });
        }
        else if (dialog!=null)
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        dialog.dispose();
                    }
                });
            } catch (Exception ex) {
                //	could be InterruptedException
            }
    }

	protected synchronized void createProgressDialog(int pollTime)
	{
		if (dialog!=null) return;

		dialog = new ProgressDialog(progressTitle, this);
		dialog.setTitle(Language.get(progressTitle));
		dialog.setText(progressText);
		dialog.setVisible(true);
		if (pollTime > 0) dialog.startPoll(pollTime);
	}

	public void actionPerformed(ActionEvent e)
	{
		//	callback from dialogTimer
        //  or from delay timer
		if (isRunning())
			createProgressDialog(pollProgress);
		else
			start();
	}

}
