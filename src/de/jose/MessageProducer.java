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

package de.jose;

import javax.swing.*;
import java.util.Vector;

/**
 */

public class MessageProducer
{
	/** set of listeners    */
	protected Vector messageListeners;
	/** source object (this by default) */
	protected Object source;


	public MessageProducer()
	{
		messageListeners = new Vector();
		source = this;
	}

	public MessageProducer(Object src)
	{
		this();
		source = src;
	}

	public final void addMessageListener(MessageListener list)
	{
		messageListeners.add(list);
	}

	public final void removeMessageListener(MessageListener list)
	{
		messageListeners.remove(list);
	}

    public final boolean isMessageListener(MessageListener list)
    {
        return messageListeners.contains(list);
    }

	public final void removeAllMessageListeners()
	{
		messageListeners.clear();
	}
	
	public final int countMessageListeners()
	{
		return messageListeners.size();
	}
	
	public final MessageListener getMessageListener(int i)
	{
		return (MessageListener)messageListeners.get(i);
	}
	
	public void sendMessage(int what, Object data)
	{
		for (int i=0; i<countMessageListeners(); i++)
		{
			MessageListener listener = getMessageListener(i);
			if ((listener instanceof DeferredMessageListener)
					&& !SwingUtilities.isEventDispatchThread())
			{
				//  invoke later (in AWT thread)
				DeferredMessage msg = new DeferredMessage(listener,what,data);
				SwingUtilities.invokeLater(msg);
			}
			else
				listener.handleMessage(source,what,data);	//	handle immediately
		}
	}
	
	public final void sendMessage(int what)
	{
		sendMessage(what,null);
	}

	protected class DeferredMessage implements Runnable
	{
		MessageListener receiver;
		int what;
		Object data;

		DeferredMessage(MessageListener receiver, int what, Object data)
		{
			this.receiver = receiver;
			this.what = what;
			this.data = data;
		}
		 /**
		  * called by the AWT event dispatcher
		  */
		 public void run()
		 {
			 receiver.handleMessage(MessageProducer.this.source, what, data);
		 }
	}
}
