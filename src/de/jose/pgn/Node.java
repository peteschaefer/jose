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

package de.jose.pgn;

import org.xml.sax.SAXException;

import javax.swing.text.*;
import java.io.PrintWriter;

import de.jose.sax.JoContentHandler;

import static de.jose.pgn.INodeConstants.*;

/**
 * abstract base class for elements
 * implements double linked list
 */

abstract public class Node
			implements javax.swing.text.Element
{
	/**	link to previous node	 */
	protected Node previousNode;
	/**	link to next node	 */
	protected Node nextNode;
	/**	reference to parent line	 */
	protected LineNode parentLine;
    /** length (number of characters) in document   */
    protected int length;
    /** node type (= class) */
    protected INodeConstants type;
	/** keep-together in print
	 *  (that is: avoid column breaks inside this node
	 */
	protected boolean keepTogether;

    public final INodeConstants type()    			{ return type; }
	public final boolean is(INodeConstants atype)   { return type==atype; }

	public final Node previous()			{ return previousNode; }
	public final Node next()				{ return nextNode; }
	
	public final boolean hasNext()			{ return nextNode!=null; }
	public final boolean hasPrevious()		{ return previousNode!=null; }
	
	public final boolean isFirst()			{ return !hasPrevious(); }
	public final boolean isLast()			{ return !hasNext(); }

	public final boolean keepTogether()     { return keepTogether; }
	public final void setKeepTogether(boolean keep) { this.keepTogether = keep; }

    protected Node(INodeConstants atype) {
        type = atype;
    }

	public final Node previous(INodeConstants nodeClass) {
		for (Node n = previous(); n != null; n = n.previous())
			if (n.is(nodeClass))
				return n;
		return null;
	}

	public final Node previous(INodeConstants nodeClass1, INodeConstants nodeClass2) {
		for (Node n = previous(); n != null; n = n.previous())
			if (n.is(nodeClass1) || n.is(nodeClass2))
				return n;
		return null;
	}

	public final Node next(INodeConstants nodeClass) {
		for (Node n = next(); n != null; n = n.next())
			if (n.is(nodeClass))
				return n;
		return null;
	}

	public final Node next(INodeConstants nodeClass1, INodeConstants nodeClass2) {
		for (Node n = next(); n != null; n = n.next())
			if (n.is(nodeClass1) || n.is(nodeClass2))
				return n;
		return null;
	}

	public boolean nextIs(INodeConstants nodeType)
	{
		Node next = next();
		return (next!=null) && next.is(nodeType);
	}

	public Node skip(INodeConstants nodeType)
	{
		Node node = next();
		while (node!=null && node.is(nodeType)) node = node.next();
		return node;
	}

	public LineNode parent()	{
		return parentLine; 
	}

	public boolean isDescendantOf(LineNode line)
	{
		for (LineNode p = parent(); p != null; p = p.parent())
			if (p==line)
				return true;
		return false;
	}

    /** return the next leaf node in text order
     * */
    public Node nextLeaf(boolean thisLine)
    {
        Node n = next();
        if (n==null) {
            if (thisLine) return null;
            //  search sibling line
            LineNode p = this.parent();
            do {
                if (p==null) return null;
                n = p.next();
                p = p.parent();
            } while (n==null);

            if (n.type()==LINE_NODE)
                return ((LineNode)n).firstLeaf();
            else
                return n;
        }

        if (n.type()== LINE_NODE)
            return ((LineNode)n).firstLeaf();
        else
            return n;
    }

    /** return the previous leaf node in text order
     * */
    public Node previousLeaf(boolean thisLine)
    {
        Node n = previous();
        if (n==null) {
            if (thisLine) return null;
            //  search sibling line
            LineNode p = this.parent();
            do {
                if (p==null) return null;
                n = p.previous();
                p = p.parent();
            } while (n==null);

            if (n.type()== LINE_NODE)
                return ((LineNode)n).lastLeaf();
            else
                return n;
        }

        if (n.type()== LINE_NODE)
            return ((LineNode)n).lastLeaf();
        else
            return n;
    }

    public Node findNode(int relativePosition)
    {
        if (relativePosition >= 0 && relativePosition < getLength())
            return this;
        else
            return null;
    }

	public void remove()
	{
		if (parentLine!=null) {
			if (this==parentLine.first())
				parentLine.setFirst(next());
			if (this==parentLine.last())
				parentLine.setLast(previous());
			parentLine.subLength(this.getLength());
			parentLine = null;
		}
		
		if (hasPrevious())
			previous().nextNode = next();
		if (hasNext())
			next().previousNode = previous();
	}

	public void swap(Node that)
	{
		if (this.next()==that) {
			that.remove();
			that.insertBefore(this);
		}
		else if (that.next()==this) {
			that.remove();
			that.insertAfter(this);
		}
		else {
			Node a = this.previous();
			Node b = this.next();
			Node c = that.previous();
			Node d = that.next();

			this.remove();
			that.remove();

			that.insertBetween(a,b);
			this.insertBetween(c,d);
		}
	}

	private void insertBetween(Node a, Node b)
	{
		assert(a==null || b==null || a.parent()==b.parent());
		assert(a==null || b==null || a.next()==b && a==b.previous());

		if (a!=null)
			this.insertAfter(a);
		else if (b!=null)
			this.insertBefore(b);
		else {
			setPrevious(null);
			setNext(null);
		}
	}

	/**
	 * remove a sequence of nodes
	 * @param from first node (inclusive)
	 * @param to last node (inclusive)
	 */
	public LineNode remove(Node from, Node to)
	{
		LineNode collect = new LineNode(getGame());
		while (to!=null && to!=from) {
			Node prev = to.previous();
			to.remove();
			to.insertAfter(collect.first());
			to = prev;
		}
		if (to!=null) {
			to.remove();
			to.insertAfter(collect.first());
		}
		return collect;
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		throw new AbstractMethodError();
	}

	public void replace(StyledDocument doc, int from, int to, String newText) throws BadLocationException
	{
		throw new AbstractMethodError();
	}

	public boolean update(StyledDocument doc)
		throws BadLocationException
	{
		int newLength = toString(doc).length();
		if (newLength != getLength()) {
			remove(doc);
			insert(doc,getStartOffset());	//	sets new length
			return true;
		}
		else
			return false;
	}

	public String toString(StyledDocument ignore)
	{
		return toString();
	}

	/**	remove from text document	  */
	public void remove(StyledDocument doc)
		throws BadLocationException
	{
		doc.remove(getStartOffset(),getLength());
	}


	/**	remove the current node and its succesors
	 */
	public void cutBefore() 
	{
		if (!isFirst()) {
			previous().setNext(null);
			setPrevious(null);
		}
	}
	
	public void cutAfter() 
	{
		if (!isLast()) {
			next().setPrevious(null);
			setNext(null);
		}
	}
	
	public void insertFirst(LineNode line)
	{
		setParent(line);
		if (parent().isEmpty()) {
			parent().setFirst(this);
			parent().setLast(this);
		}
		else
			insertBefore(parent().first());
	}
	
	public void insertLast(LineNode line)
	{
		setParent(line);
		if (parent().isEmpty()) {
			parent().setFirst(this);
			parent().setLast(this);
		}
		else
			insertAfter(parent().last());
	}
	
	public void insertAfter(Node node)
	{
		setParent(node.parent());
		
		setNext(node.next());
		setPrevious(node);

		if (next()!=null)
			next().setPrevious(this);
		node.setNext(this);
		
		if (parentLine!=null) {
			parentLine.addLength(this.getLength());
			if (parentLine.last()==node)
				parentLine.setLast(this);
		}
	}
	
	public void insertBefore(Node node)
	{
		setParent(node.parent());
		
		setNext(node);
		setPrevious(node.previous());
		
		if (previous()!=null)
			previous().setNext(this);
		node.setPrevious(this);
		
		if (parentLine!=null) {
			parentLine.addLength(this.getLength());
			if (parentLine.first()==node)
				parentLine.setFirst(this);
		}
	}

	
	protected void setParent(LineNode line)				{ parentLine = line; }
	protected final void setNext(Node node)				{ nextNode = node; }
	protected final void setPrevious(Node node)			{ previousNode = node; }
	
	/**	print PGN text	 */
	public void printPGN(PrintWriter out) {
		throw new AbstractMethodError();
	}
	
	/**	print XML text 	 */
	public void printXML(PrintWriter out) {
		throw new AbstractMethodError();
	}

    /** write binary data   */
    abstract void writeBinary(BinWriter writer);


	//-------------------------------------------------------------------------------
	//	interface Element
	//-------------------------------------------------------------------------------

	public Document getDocument() {
		return parent().getGame();
	}

	public Game getGame() {
		return parent().getGame();
	}
	
	public Element getParentElement() {
		return parent();
	}
	
	public String getName() {
		return getClass().getName();
	}
	
	public AttributeSet getAttributes() {
		return null;
	}

	abstract public Style getDefaultStyle(StyledDocument doc);

	public int getStartOffset() {
		int pos = 0;
        if (parentLine != null) {
            pos = parentLine.getStartOffset();
            for (Node nd = parentLine.first(); nd != this && nd != null; nd = nd.next())
                pos += nd.getLength();
        }
        return pos;
	}
	
	public int getEndOffset() {
		return getStartOffset()+getLength();
	}

	public final int getLength() {
		return length;
	}
	
	public final void setLength(int newLen)
    {
        if (length != newLen && parentLine != null)
            parentLine.setLength(parentLine.getLength() + newLen-length);            
        length = newLen;
    }

	public final void addLength(int add)
	{
		setLength(getLength()+add);
	}

	public final void subLength(int sub)
	{
		setLength(getLength()-sub);
	}
	
	public int getElementIndex(int offset)	{
		return -1;
	}

	public int getElementCount() {
		return 0;
	}
	
	public Element getElement(int index) {
		return null;
	}
	
	public boolean isLeaf() {
		return true;
	}

	public String debugString() {
		return toString();
	}

	public Node nextEditable(boolean returnEmpty)
	{
		for (Node node = next(); node != null; node = node.next())
		{
			if (node.is(STATIC_TEXT_NODE)) continue;
			if (node.is(RESULT_NODE)) continue;
			if (node.is(TAG_NODE) && !returnEmpty && ((TagNode)node).isEmpty()) continue;
			//  else:
			return node;
		}
		// else
		return null;
	}

	public Node prevEditable(boolean returnEmpty)
	{
		for (Node node = previous(); node != null; node = node.previous())
		{
			if (node.is(STATIC_TEXT_NODE)) continue;
			if (node.is(RESULT_NODE)) continue;
			if (node.is(TAG_NODE) && !returnEmpty && ((TagNode)node).isEmpty()) continue;
			//  else:
			return node;
		}
		// else
		return null;
	}

	/**	@return the previous move in the game tree
	 */
	public MoveNode previousMove()
	{
		Node previous = this.previous(MOVE_NODE);
		if (previous!=null)
			return (MoveNode)previous;

		//	else: climb up to parent; undo the last move (i.e. look for the previous-previous move)
		Node line = parent();
		while (line!=null) {
			previous = line.previous(MOVE_NODE);
			if (previous!=null)
				break;
			else
				line = line.parent();
		}

		Node nd = previous;
		while (nd != null) {
			previous = nd.previous(MOVE_NODE);
			if (previous!=null)
				return (MoveNode)previous;
			else
				nd = nd.parent();
		}

		return null;
	}

	abstract public void toSAX(JoContentHandler handler) throws SAXException;

}
