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

package de.jose.plugin;

import de.jose.Application;
import de.jose.util.file.XBufferedReader;
import de.jose.util.file.XStringBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.ArrayList;

/**
 * this thread listens to input from an InputStream
 */

public class InputThread
		extends Thread
{
	/**	the input stream	 */
	protected InputStream in;
	/**	buffered reader on top	 */
	protected XBufferedReader rin;
	/**	input line buffer	 */
	protected XStringBuffer line;
	/**	vector of listeners	 */
	protected Vector listeners;
	/**	close requested	 */
	protected boolean closeRequested;
    /** used for synchronisation    */
    protected Object lock;
    /** */
    protected Vector caughtErrors;

	public InputThread ()
	{
		line = new XStringBuffer();
		listeners = new Vector();
		setName("jose.plugin-input-reader");
		setDaemon(true);
        lock = this;
        caughtErrors = new Vector();
	}

    public void synchronizeTo(Object lock)
    {
        if (lock==null)
            this.lock = this;
        else
            this.lock = lock;
    }

	public InputThread (InputStream aStream)
	{
		this();
		setInputStream(aStream);
	}
	
	public void setInputStream(InputStream aStream)
	{
		in = aStream;
		rin = new XBufferedReader(new InputStreamReader(in));
	}
	
	public void addInputListener(InputListener l, int prio)
    {
        synchronized (lock) {
            if (prio < 0 || prio >= listeners.size())
                listeners.add(l);
            else
                listeners.insertElementAt(l,prio);
        }
    }
	
	public void removeInputListener(InputListener l)	{
        synchronized (lock) {
            listeners.remove(l);
        }
    }

	public void removeAllListeners()
	{
		synchronized (lock) {
			listeners.clear();
		}
	}

	public void close()
	{
        synchronized (lock) {
            closeRequested = true;
            interrupt();
        }
    }
	
	public void run() 
	{
		while (!closeRequested) {
			try {
				line.setLength(0);
				if (! rin.readLine(line)) {	//	may block
					notifyEOF();
					return;
				}
				//	else:
				notifyLine(line);
			} catch (IOException ioex) {
				notifyError(ioex);
				return;		/* or should we continue ? */
			} catch (Throwable ex) {
				notifyError(ex);
				/*continue*/
			}
		}
	}
	
	private void notifyLine(XStringBuffer line)
	{
        synchronized (lock) {
            for (int i=0; i<listeners.size(); i++)
                try {
                    listener(i).readLine(line.getValue(), 0, line.length());
                } catch (Exception e) {
                    caughtErrors.add(e);
                }
        }
        
        while (!caughtErrors.isEmpty())
            notifyError((Throwable)caughtErrors.remove(0));
    }

	private void notifyEOF()
	{
        for (int i=0; i<listeners.size(); i++)
            try {
                listener(i).readEOF();
            } catch (Exception e) {
                notifyError(e);
            }
    }
	
	public void notifyError(Throwable ex)
	{
        if (listeners.size()==0)
            Application.error(ex);	//	don't let the error go unnoticed
        for (int i=0; i<listeners.size(); i++)
            try {
                listener(i).readError(ex);
            } catch (Exception e) {
                notifyError(e);
            }
    }
	
	private InputListener listener(int i)	{ return (InputListener)listeners.get(i); }
}
