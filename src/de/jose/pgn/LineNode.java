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

import de.jose.chess.BinaryConstants;
import de.jose.chess.Constants;
import de.jose.chess.Move;
import de.jose.chess.Board;
import de.jose.plugin.Score;
import de.jose.util.ListUtil;
import de.jose.sax.JoContentHandler;

import javax.swing.text.*;
import java.io.*;
import java.util.Vector;

import org.xml.sax.SAXException;

import static de.jose.pgn.INodeConstants.*;

public class LineNode
		extends Node
		implements BinaryConstants, Constants
{
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------


	/**	first node in line	 */
	private Node firstNode;
	/**	last node in line	 */
	private Node lastNode;
	/**	reference to Game document	 */
	private Game game;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public LineNode(Game aGame)
	{
        super(LINE_NODE);
		game = aGame;
        clear(aGame);
	}
	
	public LineNode(Game aGame, byte[] bin, int boffset, byte[] comments, int coffset, byte[] eval, String fen, boolean replay)
	{
		this(aGame);
		if (bin != null)
			readBinary(bin,boffset, comments,coffset, fen, replay);
		if (eval != null)
			readEval(eval);
	}


	public final int level()		{
		int lev = 0;
		for (LineNode p = parent(); p != null; p = p.parent())
			lev++;
		return lev;
	}
	
	public final Game getGame()		{ return game; }
	
	public final boolean isEmpty()	{ return firstNode==null; }
	
	public final Node first()		{ return firstNode; }
	public final Node last()		{ return lastNode; }
	
	public final Node first(INodeConstants nodeClass) {
		for (Node n = first(); n != null; n = n.next())
			if (n.is(nodeClass))
				return n;
		return null;
	}

	public final Node first(INodeConstants nodeClass1, INodeConstants nodeClass2) {
		for (Node n = first(); n != null; n = n.next())
			if (n.is(nodeClass1) || n.is(nodeClass2))
				return n;
		return null;
	}


	public final Node last(INodeConstants nodeClass) {
		for (Node n = last(); n != null; n = n.previous())
			if (n.is(nodeClass))
				return n;
		return null;
	}
	
	public final Node last(INodeConstants nodeClass1, INodeConstants nodeClass2) {
		Node result1 = null;
		Node result2 = null;
		for (Node n = last(); n != null; n = n.previous())
		{
			if (n.is(nodeClass1) && result1==null) {
				if (result2!=null) return n;
				result1 = n;
			}
			if (n.is(nodeClass2) && result2==null) {
				if (result1!=null) return n;
				result2 = n;
			}
		}
		return null;
	}

	public final MoveNode firstMove()	{ return (MoveNode)first(MOVE_NODE); }
	public final MoveNode secondMove()	{
		MoveNode m = firstMove();
		if (m==null)
			return null;
		else
			return (MoveNode)m.next(MOVE_NODE);
	}

	public final MoveNode lastMove()	{ return (MoveNode)last(MOVE_NODE); }

	public final boolean contains(Node nd)
	{
		return containsAfter(first(),nd);
	}

	public final boolean containsAfter(Node after, Node nd)
	{
		for (Node n = after; n != null; n = n.next()) {
			if (n==nd) return true;
			if ((n.type()==LINE_NODE) && ((LineNode)n).contains(nd)) return true;
		}
		return false;
	}


    public final Node firstLeaf()
    {
        for (Node n = first(); n != null; n = n.next()) {
            if (n.is(LINE_NODE)) {
                Node r = ((LineNode)n).firstLeaf();
                if (r!=null) return r;
            }
            else
                return n;
        }
        return null;
    }

    public final Node lastLeaf()
    {
        for (Node n = last(); n != null; n = n.previous()) {
            if (n.is(LINE_NODE)) {
                Node r = ((LineNode)n).lastLeaf();
                if (r!=null) return r;
            }
            else
                return n;
        }
        return null;
    }

    public Node findNode(int relativePosition)
    {
        if (relativePosition < 0 || relativePosition >= getLength())
            return null;

        for (Node nd = first(); nd != null; nd = nd.next())
        {
            int ndlen = nd.getLength();
            if (relativePosition < nd.getLength())
                return nd.findNode(relativePosition);
            relativePosition -= ndlen;
        }
        return this;
    }

	public void cutBefore(StyledDocument doc, Node from, boolean canDelete) throws BadLocationException
	{
		if ((from.previous()==first()) && canDelete) {
			remove(doc);
			this.remove();	//	remove completely
		}
		else {
			Node last = last(RESULT_NODE);
			if (last!=null)
				last = last.previous();//.previous();      //  skip result (Q: and line suffix ??)
			else
				last = last().previous();       //  skip line suffix

			int offset = from.getStartOffset();
			int len = last.getEndOffset()-offset;

			doc.remove(offset, len);
			this.remove(from,last);
		}
	}

    public final int countMoves() {
        int count = 0;
        for (Node nd = first(); nd != null; nd = nd.next())
            if (nd.is(MOVE_NODE)) count++;
        return count;
    }

	public final void clear(StyledDocument doc)			{
		firstNode = lastNode = null;	/*	that was easy ;-) */
        new LineLabelNode(LineLabelNode.PREFIX).insertFirst(this);
        new LineLabelNode(LineLabelNode.SUFFIX).insertLast(this);
		setLength(0);
	}

	public Style getStyle(StyledDocument doc, String prefix) {
		int level = Math.min(4,level())-1;
		Style result = doc.getStyle(prefix+"."+level);
		if (result==null)
			result = doc.getStyle(prefix);
		return result;
	}

	public void removeComments()
	{
		for (Node nd = first(); nd!=null; )
		{
			Node nn = nd.next();
			switch (nd.type()) {
				case COMMENT_NODE:
				case ANNOTATION_NODE:
				case DIAGRAM_NODE:
					nd.remove();	//	nd.next() is still valid. but better not rely on it
					break;
				case LINE_NODE:
					((LineNode) nd).removeComments();
					break;
			}
			nd = nn;
		}
	}

	public void updateLabels(Game doc) throws BadLocationException
	{
		/** avoid firing caret updates from here	 */
		doc.ignoreCaretUpdate = true;

		try {
			LineLabelNode prefix = (LineLabelNode)first();
			String oldText = doc.getText(prefix.getStartOffset(),prefix.getLength());
			String newText = prefix.toString();
			if (!oldText.equals(newText)) {
				doc.remove(prefix.getStartOffset(),oldText.length());
				prefix.insert(doc,prefix.getStartOffset());
			}

			LineLabelNode suffix = (LineLabelNode)last();
			oldText = doc.getText(suffix.getStartOffset(),suffix.getLength());
			newText = suffix.toString();
			if (!oldText.equals(newText)) {
				doc.remove(suffix.getStartOffset(),oldText.length());
				suffix.insert(doc,suffix.getStartOffset());
			}
		} finally {
			doc.ignoreCaretUpdate = false;
		}
	}



	//-------------------------------------------------------------------------------
	//	protecetd Part
	//-------------------------------------------------------------------------------
	
	protected final void setFirst(Node node)		{ firstNode = node; }
	protected final void setLast(Node node)			{ lastNode = node; }
	
	
	//-------------------------------------------------------------------------------
	//	interface Element
	//-------------------------------------------------------------------------------

	public Document getDocument() {
		return getGame();
	}

	public Element getParentElement() {
		return getGame().getDefaultRootElement();
	}

	public int getElementIndex(int offset)
    {
		int i = 0;
        offset -= getStartOffset();

		for (Node n = first(); n!=null; n = n.next(), i++)
        {
		    int ndlen = n.getLength();
            if (offset < ndlen)
                return i;
            offset -= ndlen;
        }
		return -1;
	}

	public int getElementCount() {
		int i = 0;
		for (Node n = first(); n!=null; n = n.next())
			i++;
		return i;
	}
	
	public Element getElement(int index) {
		for (Node n = first(); n!=null; n = n.next())
			if (index-- == 0)
				return n;
		return null;
	}
	
	public boolean isLeaf() {
		return first() != null;
	}


	/**
	 * reads binary move data and inserts Nodes into this line
	 */
    class LineNodeBinReader extends BinReader
    {
 		Node current = LineNode.this;
		boolean startOfLine = true;

        LineNodeBinReader() {
            super(game.position);
        }

        public void insert(Node next)
        {
            if (startOfLine) {
                LineNode lnd = (LineNode)current;
                next.insertAfter(lnd.first());	//	skip prefix
            }
            else
                next.insertAfter(current);

            startOfLine = false;
            current = next;
        }

        public void annotation(int nagCode) {
	        switch (nagCode)
	        {
		    case PgnConstants.NAG_DIAGRAM:
			case PgnConstants.NAG_DIAGRAM_DEPRECATED:
			        insert(new DiagramNode(pos.toString()));
			        break;
			default:
			        insert(new AnnotationNode(nagCode));
			        break;
	        }
        }

        public void result(int resultCode) {
            insert(new ResultNode(resultCode));
        }

        public void startOfLine(int nestLevel) {
			if (nestLevel > 0) {
            	insert(new LineNode(game));
            	startOfLine = true;
			}
        }

        public void endOfLine(int nestLevel) {
			if (nestLevel > 0) {
            	current = current.parent();
            	startOfLine = false;
			}
        }

        public void beforeMove(Move mv, int ply, boolean displayHint) {

        }

	    public void replayError(StringBuffer text, Move mv, int ply)
	    {
  	        System.out.println(LineNode.this.toString());
		    super.replayError(text, mv, ply);
	    }

        public void afterMove(Move mv, int ply) {
            insert(new MoveNode(ply,mv));
        }

        public void endOfFile() {   }

        public void comment(StringBuffer text) {
            insert(new CommentNode(text));
        }
    }


	/**
	 * reads binary move data and generates SAX events
	 * (without creating Nodes)
	 */
	public static class SAXBinReader extends BinReader
	{
		JoContentHandler handler;

	    SAXBinReader(de.jose.chess.Position pos, JoContentHandler handler)
	    {
	        super(pos);
		    this.handler = handler;
	    }

	    public void annotation(int nagCode) {
		    try {
			    if (nagCode==PgnConstants.NAG_DIAGRAM)
				    DiagramNode.toSAX(pos,handler);
			    else {
				    AnnotationNode.toSAX(nagCode,null, handler);
			        //  wasMove untouched !
			    }
		    } catch (SAXException e) {
			    e.printStackTrace();
		    }
	    }

	    public void result(int resultCode) {
		    try {
			    ResultNode.toSAX(resultCode,null,handler);
		    } catch (SAXException e) {
			    e.printStackTrace();
		    }
	    }

	    public void startOfLine(int nestLevel) {
			try {
				LineNode.saxStartOfLine(pos, nestLevel+1,handler);
			} catch (SAXException e) {
				e.printStackTrace();
			}
	    }

	    public void endOfLine(int nestLevel) {
			try {
		        LineNode.saxEndOfLine(pos, nestLevel+1,handler);
			} catch (SAXException e) {
				e.printStackTrace();
			}
	    }

	    public void beforeMove(Move mv, int ply, boolean displayHint) {
		    try {
			    MoveNode.saxBeforeMove(pos, ply,mv,displayHint, handler);
		    } catch (SAXException e) {
			    e.printStackTrace();
		    }
	    }

	    public void afterMove(Move mv, int ply) {
		    try {
			    MoveNode.saxAfterMove(pos, mv, handler);
		    } catch (SAXException e) {
			    e.printStackTrace();
		    }
	    }

		public void replayError(StringBuffer text, Move mv, int ply)
		{
			super.replayError(text, mv, ply);
		}

	    public void endOfFile() {   }

	    public void comment(StringBuffer text) {
		    try {
			    CommentNode.toSAX(text,handler,true);   //  could be markup, we don't know
		    } catch (SAXException e) {
			    e.printStackTrace();
		    }
	    }
	}

    private void readBinary(byte[] bin, int boffset, byte[] comments, int coffset, String fen, boolean replay)
    {
        BinReader reader = new LineNodeBinReader();

	    int oldOptions = reader.pos.getOptions();
        reader.pos.setOption(Board.CHECK, true);
	    reader.pos.setOption(Board.STALEMATE, true);

	    reader.read(bin,boffset, comments,coffset, fen,replay);

	    reader.pos.setOptions(oldOptions);
    }

	private void readEval(byte[] eval)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(eval);
		DataInputStream in = new DataInputStream(bais);

		//	iterate nodes in pre-order
		GameMoveIterator it = new GameMoveIterator(this);
		for(MoveNode move = it.next(); move != null; move = it.next())
		try {

			short sw = in.readShort();
			short sd = in.readShort();

//			float fw = (sw==Short.MIN_VALUE) ? Float.MAX_VALUE : (float)sw/32767.0f;
//			float fd = (sw==Short.MIN_VALUE) ? Float.MAX_VALUE : (float)sd/32767.0f;

			if (sw!=Short.MIN_VALUE) {
				move.engineValue = new Score();
				move.engineValue.win = sw;
				move.engineValue.draw = (sd!=Short.MIN_VALUE) ? sd : 0;
				move.engineValue.lose = 32767-move.engineValue.win-move.engineValue.draw;
			}

		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean containsEval()
	{
		GameMoveIterator it = new GameMoveIterator(this);
		for(MoveNode move = it.next(); move != null; move = it.next())
			if (move.engineValue!=null && move.engineValue.hasWDL())
				return true;
		return false;
	}

	public byte[] writeEval()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		GameMoveIterator it = new GameMoveIterator(this);

		for(MoveNode move = it.next(); move != null; move = it.next())
		try {
			Score val = move.engineValue;

			float fw = (val!=null && val.hasWDL()) ? val.rel(val.win) : Float.MAX_VALUE;
			float fd = (val!=null && val.hasWDL()) ? val.rel(val.draw) : Float.MAX_VALUE;

			short sw = (fw==Float.MAX_VALUE) ? Short.MIN_VALUE : (short)(fw * 32767.0f);
			short sd = (fd==Float.MAX_VALUE) ? Short.MIN_VALUE : (short)(fd * 32767.0f);

			out.writeShort(sw);
			out.writeShort(sd);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}



	public static void  toSAX(byte[] bin, int boffset, byte[] comments, int coffset,
	                          de.jose.chess.Position pos, String fen,
	                          JoContentHandler handler)
	{
		BinReader reader = new SAXBinReader(pos,handler);

		int oldOptions = pos.getOptions();
	    pos.setOption(Board.CHECK, true);
		pos.setOption(Board.STALEMATE, true);

		reader.read(bin,boffset, comments,coffset, fen,true);

		pos.setOptions(oldOptions);
	}

    /** write binary data   */
    void writeBinary(BinWriter writer)
    {
        int lev = level();
		writer.startOfLine(lev);
		writeBinaryContents(writer);
		writer.endOfLine(lev);
	}

    /** write binary data   */
    void writeBinaryContents(BinWriter writer)
    {
		for (Node nd = first(); nd != null; nd = nd.next())
			nd.writeBinary(writer);
	}

	public Style getDefaultStyle(StyledDocument doc)
	{
		return getStyle(doc,"body.line");
	}

	/**	insert into text document	  */
	public void insert(StyledDocument doc, int at)
		throws BadLocationException
	{
		Style style = getDefaultStyle(doc);
		de.jose.chess.Position pos = getGame().getPosition();

        if (level() >= 2)
		    pos.startVariation();

		for (Node nd = first(); nd  != null; nd = nd.next()) {
		    nd.insert(doc,at);
            at += nd.getLength();
        }
        if (level() >= 2)
    		pos.undoVariation();
	}

	public String debugString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		for (Node nd = first(); nd!=null; nd = nd.next()) {
			buf.append(nd.debugString());
			buf.append(", ");
		}
		buf.append(")");
		return buf.toString();
	}

	public void toSAX(JoContentHandler handler) throws SAXException
	{
		toSAX(null, handler);
	}

	public void toSAX(String name, JoContentHandler handler) throws SAXException
	{
		de.jose.chess.Position pos = getGame().getPosition();
		int l = level();

		if (name==null) {
			saxStartOfLine(pos, l,handler);

			if (l >= 2) {
			    pos.startVariation();
				System.err.println();
				System.err.print(" (");
			}
		}
		else
			handler.startElement(name);

		for (Node nd = first(); nd != null; nd = nd.next())
			nd.toSAX(handler);

		if (name==null) {
			if (l >= 2) {
				pos.undoVariation();
				System.err.println(") ");
			}

			handler.endElement("v");
		}
		else
			handler.endElement(name);
	}

	public static void saxStartOfLine(de.jose.chess.Position pos, int level,
	                                  JoContentHandler handler) throws SAXException
	{
		handler.startElement("v");
		handler.element("depth",level-1);
	}

	public static void saxEndOfLine(de.jose.chess.Position pos, int level,
	                                JoContentHandler handler) throws SAXException
	{
		handler.endElement("v");
	}

}
