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

import de.jose.util.file.XStringBuffer;
import de.jose.Application;

import javax.swing.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;
import java.util.LinkedList;

/**
 * @author Peter Schäfer
 */

public class LineWriter extends Thread
{
	protected Writer out;
	protected String lineSeparator;
	protected XStringBuffer line;
	/**	Vector of OutputListener	 */
	protected Vector listeners;
    /** may be used for synchronizing */
    protected Object lock;
	protected LinkedList outputQueue = new LinkedList();
	protected boolean isSleeping;

	protected final static String EOF = "eof";

	public LineWriter(Writer out, String lineSeparator)
	{
		this.out = out;
		this.lineSeparator = lineSeparator;
		listeners = new Vector();
		line = new XStringBuffer(256);
        lock = this;
		setPriority(Thread.MAX_PRIORITY);   //  make sure that messages are written asap
		start();
	}

    public void synchronizeTo(Object lock)
    {
        if (lock==null)
            this.lock = this;
        else
            this.lock = lock;   //  external synch
    }

	public void print(String text)
	{
        synchronized (lock) {
            line.append(text);
        }
    }

	public void print(long value)
	{
        synchronized (lock) {
            print(Long.toString(value));
        }
    }

	public void print(int value)
	{
        synchronized (lock) {
            print(Integer.toString(value));
        }
    }


	public void print(char value)
	{
        synchronized (lock) {
            line.append(value);
        }
    }


	public void println(String text)
	{
        synchronized (lock) {
            print(text);
            println();
        }
    }

	public void println()
	{
		synchronized (lock) {
			outputQueue.add(line.toString());
			line.setLength(0);
			if (isSleeping) interrupt();
		}
	}

	public void close()
	{
		synchronized (lock) {
			outputQueue.add(EOF);
			if (isSleeping) interrupt();
		}
	}

	public void run()
	{
		try {
			for (;;)
		{
			try {
				isSleeping = true;
				sleep(5000);
			} catch (InterruptedException e) {
				//  continue
			} finally {
				isSleeping = false;
			}

				while (! outputQueue.isEmpty())
			{
				try {
					String line = (String)outputQueue.removeFirst();

						if (line==EOF) return;

					out.write(line);
			        out.write(lineSeparator);
			        out.flush();
					/**
					 * this call may block if the subprocess has gone kerplonk
					 * what can we do to return from flush() with a reasonable timeout ?
					 */
			        notifyLine(line);
			    } catch (IOException e) {
			        notifyError(e);
			    }
			}
		}
		} finally {
			listeners.clear();  //  avoid stale references
			try { out.close(); } catch (IOException e) { }
			out = null;
	}
	}


	public void notifyLine(String line)
	{
        synchronized (lock) {
            if (line.length() > 0) {
                for (int i=0; i<listeners.size(); i++)
                    try {
                        listener(i).writeLine(line.toCharArray(),0,line.length());
                    } catch (IOException e) {
                        Application.error(e);
                    }
            }
        }
    }


	public void notifyEOF()
	{
        synchronized (lock) {
            for (int i=0; i<listeners.size(); i++)
                try {
                    listener(i).writeEOF();
                } catch (IOException e) {
                    Application.error(e);
                }
        }
    }

	public void notifyError(IOException ex)
	{
        synchronized (lock) {
            for (int i=0; i<listeners.size(); i++)
                try {
                    listener(i).writeError(ex);
                } catch (IOException e) {
                    Application.error(e);
                }
        }
    }


	public OutputListener listener(int i)
	{
		return (OutputListener)listeners.get(i);
	}

	public void addOutputListener(OutputListener l, int prio)
	{
        synchronized (lock) {
            if (prio < 0 || prio >= listeners.size())
                listeners.add(l);
            else
                listeners.insertElementAt(l,prio);
        }
    }

	public void removeOutputListener(OutputListener l)
	{
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
}
