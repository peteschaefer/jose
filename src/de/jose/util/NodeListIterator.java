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

package de.jose.util;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.util.Iterator;

/**
 * @author Peter Schäfer
 */

public class NodeListIterator
        implements Iterator
{
	protected NodeList list;
	protected int current;


	public NodeListIterator(NodeList list)
	{
		this.list = list;
		this.current = 0;
	}

	public NodeListIterator(Node node)
	{
		this(node.getChildNodes());
	}

	public boolean hasNext()
	{
		return current < list.getLength();
	}

	public Object next()
	{
		Object result = list.item(current);
		current++;
		return result;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
