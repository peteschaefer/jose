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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @deprecated
 */
public class MouseQueue
		implements MouseListener, MouseMotionListener
{
	protected MouseEvent[] events;
	
	public MouseQueue(Component comp)
	{
		events = new MouseEvent[7];
		comp.addMouseListener(this);
		comp.addMouseMotionListener(this);
	}
	
	public MouseEvent lastEvent(int id)
	{
		id = indexOf(id);
		return events[id];
	}
	
	public void clear(int id)
	{
		id = indexOf(id);
		events[id] = null;
	}
	
	public void mouseEntered(MouseEvent evt)		{ events[0] = evt; }
	public void mouseExited(MouseEvent evt)			{ events[1] = evt; }
	public void mouseClicked(MouseEvent evt)		{ events[2] = evt; }

	public void mousePressed(MouseEvent evt)		{ events[3] = evt; }
	public void mouseReleased(MouseEvent evt)		{ events[4] = evt; }
	
	public void mouseMoved(MouseEvent evt)			{ events[5] = evt; }
	public void mouseDragged(MouseEvent evt)		{ events[6] = evt; }
	
	protected static int indexOf(int eventId)
	{
		switch (eventId) {
		case MouseEvent.MOUSE_ENTERED:		return 0;
		case MouseEvent.MOUSE_EXITED:		return 1;
		case MouseEvent.MOUSE_CLICKED:		return 2;
		case MouseEvent.MOUSE_PRESSED:		return 3;
		case MouseEvent.MOUSE_RELEASED:		return 4;
		case MouseEvent.MOUSE_MOVED:		return 5;
		case MouseEvent.MOUSE_DRAGGED:		return 6;
		}
		throw new IllegalArgumentException();
	}
/*	
	protected void scanSystemQueue(int id)
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		EventQueue p = tk.getSystemEventQueue();
		EventQueue q = new EventQueue();
		id = indexOf(id);
		
		while (p.peekEvent() != null)
		{
			AWTEvent evt = null;
			try {
				evt = p.getNextEvent();
			} catch (InterruptedException iex) {
				//	not thrown, cause already tested with peekEvent
			}
			
			if (evt.getID()==id) 
			{
				if (events[id]==null || events[id].getWhen() < ((MouseEvent)evt).getWhen())
				{
					events[id] = (MouseEvent)evt;
					System.err.print(".");
				}
			}
			else
				q.postEvent(evt);
		}
		
		p.push(q);
	}
*/	
}
