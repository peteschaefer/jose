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

package de.jose.view;

import de.jose.Application;
import de.jose.Command;
import de.jose.Util;
import de.jose.view.style.JoStyleContext;
import de.jose.chess.Move;
import de.jose.image.ImgUtil;
import de.jose.pgn.*;
import de.jose.util.AWTUtil;
import de.jose.util.StringUtil;
import de.jose.util.ClipboardUtil;
import de.jose.util.style.StyleUtil;
import de.jose.util.style.MarkupWriter;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;


/**
 *
 *
 *
 * @author Peter Schäfer
 */
public class DocumentEditor
		extends JTextPane
		implements CaretListener, INodeConstants
{
	/**	Color used to hilite the current move
	 *	(do not confuse with text hilite for copy & paste)
	 */
	protected static final Color MOVE_HILITE_COLOR = ImgUtil.lightBlue;

	protected static final int PADDING_NONE         = 0x00;
	protected static final int PADDING_LEADING      = 0x01;
	protected static final int PADDING_TRAILING     = 0x02;
	protected static final int PADDING_BOTH         = 0x03;


	/** reference to owner  */
	protected DocumentPanel docPanel;
	/**	the document (= super.getDocument())	*/
	protected Game theGame;
	/**	hilite tag for current move	*/
	protected Object hiliteCurrentMove;
	/** use font antialising    */
	protected boolean antialias;
    /** is the caret listener on ?  */
    protected boolean caretListen;
	protected boolean updateCaretAfterEdit;
	protected static Game emptyGame = new Game();
	/** used to parse keyboard move input   */
	protected Parser moveParser;

	//  document position, relative to a node
	public class NodePosition
	{
		Node node;
		int offset;

		NodePosition(Node node, int offset)
		{
			this.node = node;
			this.offset = offset;
		}

		NodePosition(int docpos)
		{
			this.node = getNode(docpos);
			if (this.node!=null)
				this.offset = docpos-this.node.getStartOffset();
			else
				this.offset = docpos;
		}

		//  to document position
		int docPosition()
		{
			if (node==null) {
				if (offset >= 0)
					return offset;
				else
					return theGame.getLength()-offset;
			}
			else if (offset >= 0)
				return node.getStartOffset() + offset;
			else
				return node.getEndOffset() + offset;
		}
	}


	public DocumentEditor(Game doc, DocumentPanel owner)
	{
		super(doc);
		theGame = doc;
		setEditable(true);
		addCaretListener(this);
        caretListen = true;
		antialias = true;

		docPanel = owner;
		docPanel.getMessageProducer().addMessageListener(Application.theApplication);

        setSelectionColor(UIManager.getColor("TextPane.selectionBackground"));

        try {
            Highlighter.HighlightPainter painter =
					new DefaultHighlighter.DefaultHighlightPainter(MOVE_HILITE_COLOR);
            hiliteCurrentMove = getHighlighter().addHighlight(0,0, painter);
        } catch (BadLocationException e) {
            Application.error(e);
        }

        setupActions();
	}

	public void setDocument(Game doc)
	{
		boolean wasListen = caretListen;
		try {
			caretListen = false;
			super.setDocument(theGame=doc);
			if (moveParser!=null)
				moveParser.setPosition(theGame.getPosition());
		} finally {
			caretListen = wasListen;
		}
	}

    /**
     * find a node from screen location
     */
    public final Node getNode(Point p)
    {
//		if (!getBounds().contains(p)) return null;
        int pos = viewToModel(p);
        return getNode(pos);
    }

    public final Node getNode(int pos)
    {
        return theGame.findNode(pos);
    }

    public final Node getFirstSelectedNode()
    {
        int pos = getSelectionStart();
        return getNode(pos);
    }

    public final Node getLastSelectedNode()
    {
        int pos = getSelectionEnd();
        return getNode(pos);
    }

	public void setTextAntialising(boolean on)
	{
		if (on!=antialias) {
			antialias = on;
			repaint();
		}
	}

	public void reformat()
	{
		try {
			caretListen = false;
			Game game = theGame;

			MoveNode current = game.getCurrentMove();
			NodePosition selStart = new NodePosition(getSelectionStart());
			NodePosition selEnd = new NodePosition(getSelectionEnd());

			setDocument(emptyGame);
			game.reformat();
			setDocument(game);

			if (current==null)
				game.getPosition().reset();       //  take care to restore the original position
			else
			game.gotoMove(current);
			adjustHighlight(selStart.docPosition(), selEnd.docPosition());

		} finally {
			caretListen = true;
		}
	}

	public void paintComponent(Graphics g)
	{
		//  enable antialiasing & other useful stuff
		ImgUtil.setTextAntialiasing((Graphics2D)g, antialias);
		super.paintComponent(g);
	}

  	public void scrollTo(int p0,int p1)
		throws BadLocationException
	{
		if (isShowing()) {
			int max = getDocument().getLength();
			p0 = Util.inBounds(0,p0,max-1);
			p1 = Util.inBounds(0,p1,max-1);
			
			Rectangle r0 = modelToView(p0);
			Rectangle r1 = modelToView(p1);
			if (r0==null || r1==null)
				return;

			Rectangle r = r0.union(r1);

			AWTUtil.scrollRectToVisible(this,r);
		}
	}

    protected void adjustHighlight()
    {
	    adjustHighlight(-1,-1);
    }

	protected void adjustHighlight(int selectStart, int selectEnd)
	{
		if (theGame.getCurrentMove()==null) {
			scrollLater.p0 = scrollLater.p1 = -1;
			scrollLater.s0 = selectStart;
			scrollLater.s1 = selectEnd;
		}
		else {
			scrollLater.p0 = theGame.getCurrentMove().getStartOffset();
			scrollLater.p1 = theGame.getCurrentMove().getEndOffset()-1;     //  trim off trailing space

			if (selectStart >= 0)
				scrollLater.s0 = selectStart;
			else
				scrollLater.s0 = scrollLater.p0;
			if (selectEnd >= 0)
				scrollLater.s1 = selectEnd;
			else
				scrollLater.s1 = scrollLater.p1;
		}
		SwingUtilities.invokeLater(scrollLater);
    }

	protected ScrollLaterAction scrollLater = new ScrollLaterAction();

    protected class ScrollLaterAction implements Runnable
 	{
		int p0, p1;
	    int s0, s1;

		public void run()
		{
			boolean wasCaretListen = caretListen;
			try {
				caretListen = false;
				if (p0>=0 && p1>=0)
					getHighlighter().changeHighlight(hiliteCurrentMove, p0,p1);
				else
					getHighlighter().changeHighlight(hiliteCurrentMove, 0,0);

				if (s0>=0 && s1>=0)	{
					select(s0,s1);
					scrollTo(s0,s1);
				}
				else if (p0>=0 && p1>=0)
					scrollTo(p0,p1);

			} catch (Throwable e) {
				Application.error(e);
			} finally {
				caretListen = wasCaretListen;
			}
		}
	};

    protected void setupActions()
    {
        ActionMap am = getActionMap();

        am.put(DefaultEditorKit.backwardAction,
                new PreviousCharAction(am, DefaultEditorKit.backwardAction));
        am.put(DefaultEditorKit.previousWordAction,
                new PreviousCharAction(am, DefaultEditorKit.previousWordAction));

        am.put(DefaultEditorKit.forwardAction,
                new NextCharAction(am, DefaultEditorKit.forwardAction));
        am.put(DefaultEditorKit.nextWordAction,
                new NextCharAction(am, DefaultEditorKit.nextWordAction));

        am.put(DefaultEditorKit.deleteNextCharAction,
                new DeleteCharAction(am, +1, DefaultEditorKit.deleteNextCharAction));
        am.put(DefaultEditorKit.deletePrevCharAction,
                new DeleteCharAction(am, -1, DefaultEditorKit.deletePrevCharAction));

        //  don't set DefaultKeyTypedAction
        //  we overwrite replaceSelection() instead !!

//        am.put(DefaultEditorKit.defaultKeyTypedAction,
//              new KeyTypedAction(am, DefaultEditorKit.defaultKeyTypedAction));

//        am.put(DefaultEditorKit.copyAction, ?);
			//	copy is unmodified
//        am.put(DefaultEditorKit.pasteAction, ?);
			//	paste delegates to replaceSelection() - fine
//        am.put(DefaultEditorKit.cutAction, ?);
			//	cut is
//        am.put(DefaultEditorKit.insertBreakAction, ?);
//        am.put(DefaultEditorKit.insertTabAction, ?);
    }

	public void cut()
	{
		/**	the code behind cut() is actually very tricky
		 * it eventually delegates to BasicTextUI.removeText()
		 * which manipulates the document without our notice ;-(
		 *
		 * so let's use the simple approach: first copy, then delete
		 */
		int pos1 = getSelectionStart();
	    int pos2 = getSelectionEnd();

		if (pos1==pos2)
			super.cut();	//	does nothing
		else {
			copy();
			replaceSelection("");
		}
	}

    public void copy()
    {
        int pos1 = getSelectionStart();
        int pos2 = getSelectionEnd();

        if (pos1==pos2)
            super.copy();   //  does nothing
        else try {
            //  copy html formatted
            // ClipboardUtil.setHtmlText(plainText,htmlText, this.docPanel);
            String plainText = theGame.getText(pos1,pos2-pos1);

            StringBuffer htmlText = new StringBuffer();
            MarkupWriter.writeMarkup(theGame,pos1,pos2-pos1,StyleUtil.plainStyle(Color.black,null,12),htmlText, true);
	        /** TODO figurines are encoded as HTML entities (&#131;) but are lost when pasting the clipboard.
	         *  Why ? Entities work OK in generated HMTL files, why not on the Clipboard ?
	         */
            String rtfText = ClipboardUtil.getRtfText(theGame,pos1,pos2-pos1);

            ClipboardUtil.setStyledText(plainText, htmlText.toString(), rtfText, docPanel);
//            super.copy();
        } catch (BadLocationException e) {
            Application.error(e);
        }
    }

    public void paste()
    {
        //  paste rtf formatted ? (ugly but widely used)
        String text = ClipboardUtil.getRtfText(docPanel);
        if (text!=null) {
	        text = StringUtil.replace(text, "\\line","\n\\par");
            text = ClipboardUtil.rtfToHtml(text);
            //  \\line comes from MS-Word
            //  but we expect \n\\par
        }

        if (text==null) //  paste html formatted
            text = ClipboardUtil.getHtmlText(docPanel);
        if (text==null) //  paste plain text
            text = ClipboardUtil.getPlainText(docPanel);

        if (text!=null)
            replaceSelection(text);
        else
            AWTUtil.beep(docPanel);
    }

    abstract class KeyAction extends TextAction
    {
        protected Action parentAction;

        KeyAction(String name)                     { this(name,null); }
        KeyAction(ActionMap map, String name)      { this(name,map.get(name)); }

        KeyAction(String name, Action action)
        {
            super(name);
            parentAction = action;
        }

        public void superActionPerformed(ActionEvent e) {
            if (parentAction != null) parentAction.actionPerformed(e);
        }
    }

    class PreviousCharAction extends KeyAction
    {
        PreviousCharAction(ActionMap map, String name)  { super(map,name); }

        public void actionPerformed(ActionEvent e)
        {
            Node nd = getFirstSelectedNode();
			if ((nd!=null) && (nd.type()==MOVE_NODE)) {
				int p = nd.getStartOffset()-1;
				select(p,p);
				//  will eventually trigger caretUpdate()
			}
			else
				superActionPerformed(e);
				//  will eventually trigger caretUpdate()
        }
    }

    class DeleteCharAction extends KeyAction
    {
        int direction;

        DeleteCharAction(ActionMap map, int dir, String name) {
            super(map,name);
            direction = dir;
        }

        public void actionPerformed(ActionEvent e)
        {
            int p1 = getSelectionStart();
            int p2 = getSelectionEnd();
			if (p1==p2) {
				if (direction > 0)
					p2 = Math.max(p2 + direction, 0);
				else
					p1 = Math.min(p1 + direction, theGame.getLength());
			}
			
            doReplace(p1,p2, "");
        }
    }

    class NextCharAction extends KeyAction
    {
        NextCharAction(ActionMap map, String name)  { super(map,name); }

        public void actionPerformed(ActionEvent e)
        {
            Node nd = getLastSelectedNode();
			if ((nd!=null) && (nd.type()==MOVE_NODE)) {
				int p = nd.getEndOffset();
				select(p,p);
				//  will eventually trigger caretUpdate()
			}
			else
				superActionPerformed(e);
				//  will eventually trigger caretUpdate()
        }
    }


    public void replaceSelection(String newText)
    {
        int pos1 = getSelectionStart();
        int pos2 = getSelectionEnd();

        doReplace(pos1,pos2, newText);
    }

    protected void doReplace(int pos1, int pos2, String newText)
    {
        Node node1 = getNode(pos1);
        Node node2;
		updateCaretAfterEdit = true;

        if ((pos1 != pos2) && (node1!=null) && (pos2!=node1.getEndOffset())) {
           	node2 = getNode(pos2);
			if ((node2==node1.next()) && (node2!=null) && (pos1==(node2.getStartOffset()-1))) {
				pos1++;
				node1 = node2;
			}
		}
        else
            node2 = node1;

	    try {
			if (node1==null) {
				if (pos1==0) {
					//  very first input into an empty document
					insertCommentAfter(theGame.getMainLine().first(),newText,PADDING_NONE);
                    return;
				}
				else if (pos1>=theGame.getLength()) {
					//  insert text at end of document, but in front of invisible(!) result node
                    //  (which must always be the last node)
                    Node result = theGame.getMainLine().last(Node.RESULT_NODE);
                    if (result.getLength()==0) {
                        Node prev = result.prevEditable(false);
                        if (prev!=null){
                            pos1 = pos2 = prev.getEndOffset();
                            doReplaceNode(prev,pos1,pos2,newText, PADDING_NONE);  //  does this make sense ?
                        }
                        else {
                            pos1 = pos2 = result.getStartOffset();
                            doReplaceNode(result,pos1,pos2,newText, PADDING_NONE);
                        }
                        return;
                    }
				}
			}
			else if (node1 != node2) {
				/*  selection spans several nodes; can't edit, or can we ? */
			}
			else {
                //  the normal case
				doReplaceNode(node1,pos1,pos2,newText, PADDING_NONE);
                return;
			}

            //  if all fails...
            AWTUtil.beep(this);

        } catch (BadLocationException blex) {
            Application.error(blex);
            AWTUtil.beep(this);
        } finally {
			if (updateCaretAfterEdit) caretUpdate();
            caretListen = true;
        }
    }

	protected void doReplaceNode(Node node1, int pos1, int pos2, String newText, int padding)
	        throws BadLocationException
	{
		/** selection within 1 node */
		caretListen = false;
		switch (node1.type())
		{
		case TAG_NODE:			doReplaceTag((TagNode)node1, pos1,pos2, newText, padding); return;
		case MOVE_NODE:			doReplaceMove((MoveNode)node1, pos1,pos2, newText, padding); return;
		case COMMENT_NODE:		doReplaceComment((CommentNode)node1, pos1,pos2, newText, padding); return;
		case ANNOTATION_NODE:	doReplaceAnnotation((AnnotationNode)node1, pos1,pos2, newText, padding); return;

        case STATIC_TEXT_NODE:  if (!doReplaceStaticText(node1, pos1,pos2, newText, padding) &&
                                    node1.isDescendantOf(theGame.getMainLine()))
                                {
                                    if (node1.next()==null)
                                        insertCommentBefore(node1,newText,padding);
                                    else
                                        insertCommentAfter(node1,newText,padding);  //  OK?
                                    return;
                                }
                                break;

        case RESULT_NODE:       if (!doReplaceStaticText(node1, pos1,pos2, newText, padding)) {
                                    //  insert comment (before)
                                    insertCommentBefore(node1,newText,padding);
                                }
                                return;
		case DIAGRAM_NODE:		doReplaceDiagram((DiagramNode)node1, pos1,pos2, newText, padding); return;

		}
        //   else
        AWTUtil.beep(this); //  not yet implemented ...
	}

	/**
	 *	text edit in Move Node
	 * 	(append/insert comment)
	 */
	protected void doReplaceMove(MoveNode node, int pos1, int pos2,
	                             String newText, int padding)
	        throws BadLocationException
	{
		if (newText.length()==0) {
			//	can't delete move (TODO ?)
			AWTUtil.beep(this);
		}
		if (newText.length() > 0) {
			boolean inFront = pos2 < (node.getEndOffset()-1);

			if (inFront)
				doReplaceBeforeMove(node,newText,padding);
			else
				doReplaceAfterMove(node,newText,padding);
		}
	}

	protected void doReplaceBeforeMove(MoveNode node, String newText, int padding) throws BadLocationException
	{
		Node prev = node.previousLeaf(true);
		int nagCode = getAnnotationCode(newText);
		if ((nagCode==PgnConstants.NAG_DIAGRAM) || (nagCode==PgnConstants.NAG_DIAGRAM_DEPRECATED)) {
			//	insert diagram
//			MoveNode currentMove = theGame.getCurrentMove();
			theGame.gotoMove(node);
			theGame.getPosition().undoMove();

			DiagramNode diagram = new DiagramNode(theGame.getPosition().toString());
			theGame.gotoMove(node);

//			if (currentMove!=null) theGame.gotoMove(currentMove);

			diagram.insertBefore(node);
			diagram.insert(theGame,diagram.getStartOffset());
			int p = diagram.getEndOffset();
			select(p,p);
		}
		else if (nagCode >= 0)
			insertAnnotationBefore(node,nagCode,padding);
		else if (prev!=null && prev.type()==COMMENT_NODE)
			insertAtEndOfComment((CommentNode)prev,newText,padding);
		else
			insertCommentBefore(node,newText,padding);
		theGame.setDirty();
	}

    public boolean insertDiagram(Node node) throws BadLocationException
    {
        if (!node.is(Node.MOVE_NODE)) {
            node = node.previousMove();
            if (node==null) {
                AWTUtil.beep(this);
                return false;
            }
        }

//			MoveNode currentMove = theGame.getCurrentMove();
        theGame.gotoMove((MoveNode)node);

        DiagramNode diagram = new DiagramNode(theGame.getPosition().toString());

//			if (currentMove!=null) theGame.gotoMove(currentMove);

        Node after = insertDiagramAfter(node);
        diagram.insertAfter(after);
        diagram.insert(theGame,after.getEndOffset());
        int p = diagram.getEndOffset();
        theGame.updateMoveCount(diagram);
        select(p,p);
        return true;
    }

	protected void doReplaceAfterMove(MoveNode node, String newText, int padding) throws BadLocationException
	{
		Node next = node.nextLeaf(true);
		int nagCode = getAnnotationCode(newText);
		if ((nagCode==PgnConstants.NAG_DIAGRAM) || (nagCode==PgnConstants.NAG_DIAGRAM_DEPRECATED)) {
			//	append Diagram
            insertDiagram(node);
		}
		else if (nagCode >= 0) //	append Annotation
			insertAnnotationAfter(node,nagCode,padding);
		else if (next!=null && next.type()==COMMENT_NODE)	//	insert in front of existing comment
			insertAtStartOfComment((CommentNode)next, newText, padding);
		else	//	append new comment
			insertCommentAfter(node,newText,padding);
		theGame.setDirty();
	}

	protected Node insertDiagramAfter(Node after)
	{
		//  skip annotations directly after the move
		if (after.nextIs(Node.ANNOTATION_NODE))
			do
				after = after.next();
			while (after.nextIs(Node.ANNOTATION_NODE));
		//  if there are variations, skip them too
		Node lastline = null;
		for (Node n = after; n!=null; n=n.next())
			if (n.is(Node.MOVE_NODE))
				break;
			else if (n.is(Node.LINE_NODE))
				lastline = n;

		if (lastline!=null)
			return lastline;
		else
			return after;
	}

	protected void insertAtStartOfComment(CommentNode node, String newText, int padding) throws BadLocationException
	{
		node.replace(theGame, 0,0, pad(newText,padding));
		int p = node.getStartOffset()+leadingPadding(newText,padding)+newText.length();
		select(p,p);
	}

	protected void insertAtEndOfComment(CommentNode node, String newText, int padding) throws BadLocationException
	{
		int offset = node.getLength()-1;
		node.replace(theGame, offset,offset, pad(newText,padding));
		int p = node.getStartOffset()+offset+leadingPadding(newText,padding)+newText.length();
		select(p,p);
	}

	protected void insertCommentAfter(Node after, String newText, int padding) throws BadLocationException
	{
		padding = 0;    //  no need for padding
		CommentNode comment = new CommentNode(pad(newText,padding));
		comment.insertAfter(after);	//	insert into Node hierarchy
		comment.insert(theGame,after.getEndOffset());	//	insert into text document
		int p = comment.getEndOffset()-trailingPadding(newText,padding)-1;
		theGame.updateMoveCount(comment);
		select(p,p);
	}

	protected void insertCommentBefore(Node before, String newText, int padding) throws BadLocationException
	{
		padding = 0;    //  no need for padding
		CommentNode comment = new CommentNode(pad(newText,padding));
		comment.insertBefore(before);	//	insert into Node hierarchy
		comment.insert(theGame,comment.getStartOffset());	//	insert into text document
		int p = comment.getEndOffset()-trailingPadding(newText,padding)-1;
		theGame.updateMoveCount(comment);
		select(p,p);
	}

	protected void insertAnnotationAfter(Node after, int nagCode, int padding_ignored) throws BadLocationException
	{
		AnnotationNode annotation = new AnnotationNode(nagCode);
		annotation.insertAfter(after);
		annotation.insert(theGame,after.getEndOffset());
		int p = annotation.getEndOffset()-1;
		select(p,p);
	}

	protected void insertAnnotationBefore(Node before, int nagCode, int padding_ignored) throws BadLocationException
	{
		AnnotationNode annotation = new AnnotationNode(nagCode);
		annotation.insertBefore(before);
		annotation.insert(theGame,before.getStartOffset());
		int p = annotation.getEndOffset()-1;
		select(p,p);
	}

	protected static int leadingPadding(String text, int padding)
	{
		if (Util.anyOf(padding,PADDING_LEADING)) {
			if (text.length()==0 || !StringUtil.startsWithWhitespace(text))
				return 1;
		}
		//  else:
		return 0;
	}


	protected static int trailingPadding(String text, int padding)
	{
		if (Util.anyOf(padding,PADDING_TRAILING)) {
			if (text.length()==0 || !StringUtil.endsWithWhitespace(text))
				return 1;
		}
		//  else:
		return 0;
	}

	protected static String pad(String text, int padding)
	{
		if (padding==PADDING_NONE) return text;
		int leading = leadingPadding(text,padding);
		int trailing = trailingPadding(text,padding);

		if ((leading+trailing) == 0)
			return text;
		else {
			StringBuffer buf = new StringBuffer(text);
			while (leading-- > 0) buf.insert(0,' ');
			while (trailing-- > 0) buf.append(' ');
			return buf.toString();
		}
	}

	protected int getAnnotationCode(String text)
	{
		//  (unless it is a piece char, e.g. "N" is a NAG (novelty) but most likely it is used fo kNight
		if (getMoveParser().is1PieceChar(text,true))
			return -1;
		else
			return PgnUtil.annotationCode(text);		
	}

	/**
	 * text edit in Comment Node
	 */
	protected void doReplaceComment(CommentNode node, int pos1, int pos2,
	                                String newText, int padding)
	        throws BadLocationException
	{
		int offset = node.getStartOffset();
		int nagCode;
		if ((newText.length()==0) && node.isCoveredBy(pos1-offset,pos2-offset))
		{
			//	delete comment
			node.remove(theGame);	//	remove from Text Document
			node.remove();	//	remove from Node hierarchy
			select(pos1,pos1);
		}
		else if (pos1==offset && (nagCode=getAnnotationCode(newText)) > 0)
		{
			//	annotation at the beginning of a comment; make an annotation out of it
			insertAnnotationBefore(node, nagCode, padding);
		}
		else if (newText.equals("\n") /*&& (node.next(MOVE_NODE)==null)*/) {
			//  keyboard move input ?
			MoveNode previous = node.previousMove();
			String moveText = theGame.getText(offset,pos1-offset);

			if (!StringUtil.isWhitespace(moveText) &&
			    keyboardMove(node,previous,offset,moveText,pos2,padding))
				return;
			else {
				//  invalid move, or just inserting a newline ... ?
				//AWTUtil.beep(this);
				editComment(node,offset,pos1,pos2,newText,padding);
			}
		}
		else {
			//  edit comment
			editComment(node,offset,pos1,pos2,newText,padding);
		}
		theGame.updateMoveCount(node);
		theGame.setDirty();
	}

	private void editComment(CommentNode node, int offset, int pos1, int pos2,
	                         String newText, int padding)
		throws BadLocationException
	{
		//
		int oldLength = node.getLength();
		node.replace(theGame, pos1-offset,pos2-offset, pad(newText,padding));
		int textLength = node.getLength()-oldLength;        //  need not be identical to newText.length() !!!!

		int p;
		if (textLength >= 0)
			p = pos1+leadingPadding(newText,padding)+textLength;
		else
			p = pos1;   //  deletion. stay at selection
		//  account for trailing space (in EVERY comment) (TODO think about using padding instead)
		if (p >= node.getEndOffset()) p--;
		select(p,p);
	}

	private Parser getMoveParser()
	{
		if (moveParser==null)
			moveParser = new Parser(theGame.getPosition(), 0,true);

		moveParser.setLanguage(Application.theUserProfile.getFigurineLanguage(),false);
		return moveParser;
	}

	private boolean keyboardMove(CommentNode node, MoveNode after,
	                             int offset, String moveText, int pos2,
	                             int padding)
		throws BadLocationException
	{
		getMoveParser();    //  init

		MoveNode oldmove = theGame.getCurrentMove();
		try {
			theGame.gotoMove(after);

			//  moveParser expetcs a SAN formatted text; let's be a bit more tolerant..
			//  but don't confuse comment text with parsable input !!
			Move mv = moveParser.parseMove(moveText);
			if (mv==null)
				mv = moveParser.parseMove(parseLenient(moveText,moveParser.pieceChars));

			if (mv!=null) {
				//  legal move !
				//  delete text
				doReplaceComment(node,offset,pos2,"",padding);
				/** when deleting the text, avoid selecting another move ! */
				updateCaretAfterEdit = false;

				//  send message (with source=DocumentPanel)
				Object[] params = { after, new Move(mv), };
				docPanel.getMessageProducer().sendMessage(DocumentPanel.EVENT_USER_MOVE, params);
				/**
				 * note that Application is a DeferredMessageListener
				 * that means that this message will only be handled after the current event cycle,
				 * which is exactly what we want...
				 *
				 * also note that MoveParser holds a fixed pool of Move objects.
				 * better create a fresh object
				 */
				return true;
			}

			return false;
		} finally {
			theGame.gotoMove(oldmove);
		}
	}

	private String parseLenient(String text, String pieceChars)
	{
		text = text.replace('0','O');   //  accept 0 (zero) return O (oh)
		if (text.equalsIgnoreCase("oo")) text = "O-O";
		if (text.equalsIgnoreCase("ooo")) text = "O-O-O";

		//  disambiguate piece chars and file chars
		//  for example: in German "dd3" should become "Dd3" (buf "dxe3" remains "dxe3")
		char c1 = (text.length()>0) ? text.charAt(0) : 0;
		
		if (c1>='a' && c1<='h') {
			//  if c1 is a piece char, uppercase it (unless it could be a pawn move)
			c1 = Character.toUpperCase(c1);
			if (pieceChars.indexOf(c1) >= 0) {
				StringBuffer buf = new StringBuffer(text);
				buf.setCharAt(0,c1);
				text = buf.toString();
			}
		}
		return text;
	}

	/**
	 * text edit in Comment Node
	 */
	protected void doReplaceTag(TagNode node, int pos1, int pos2,
	                            String newText, int padding)
	        throws BadLocationException
	{
		int offset = node.getStartOffset();
		if ((newText.length()==0) && node.isCoveredBy(pos1-offset,pos2-offset))
		{
			//	delete tag (DON'T remove from hierarchy, just set an empty value)
			node.setEmpty(theGame);
			select(pos1,pos1);
			theGame.updateTagLabels(theGame);
			theGame.setDirty(node);
		}
		else if (node.isEditable()) {
			//  edit tag
			node.replace(theGame, pos1-offset,pos2-offset, pad(newText,padding));
			int p = pos1+leadingPadding(newText,padding)+newText.length();
			select(p,p);
			theGame.setDirty(node);
//			theGame.updateTagLabels(theGame);
		}
		else //	no string tag; can't edit
			AWTUtil.beep(this);
	}

	/**
	 * text edit in Annotation Node
	 */
	protected void doReplaceAnnotation(AnnotationNode node, int pos1, int pos2,
	                                   String newText, int padding) throws BadLocationException
	{
		int offset = node.getStartOffset();
		if ((newText.length()==0) && node.isCoveredBy(pos1-offset,pos2-offset))
		{
			//	delete annotation
			node.remove(theGame);	//	remove from Text Document
			node.remove();	//	remove from Node hierarchy
			select(offset,offset);
			theGame.setDirty();
		}
		else {
			//  edit annotation
			//	TODO
			int newCode = node.canReplace(pos1-offset,pos2-offset,newText);
			Node next = node.next();
			if (newCode > 0) {
				node.replace(theGame,newCode);
				int p = offset+node.getLength()-1;
				select(p,p);
				theGame.setDirty();
			}
			else if (next!=null && next.type()==COMMENT_NODE)	//	insert in front of existing comment
				insertAtStartOfComment((CommentNode)next, newText, padding);
			else	//	append new comment
				insertCommentAfter(node,newText, padding);
		}
	}

	/**
	 * text edit in Annotation Node
	 */
	protected void doReplaceDiagram(DiagramNode node, int pos1, int pos2,
	                                String newText, int padding) throws BadLocationException
	{
		if (newText.length()==0) {
			//	delete
			int offset = node.getStartOffset();
			node.remove(theGame);
			node.remove();
			select(offset,offset);
			theGame.setDirty();
            return;
		}

		int start = node.getStartOffset();
        int end = node.getEndOffset();

		if (pos2 <= (start+1) && doReplaceStaticText(node, start, start, newText, padding)) 
			return; //  delegate to previous
        if (doReplaceStaticText(node, end, end, newText, padding))
            return;   //  delegate to next
        //  else:
        insertCommentAfter(node,newText,padding);

	}

	private boolean isWhiteSpace(int pos1, int pos2)
	{
		if (pos2 <= pos1) return true;  //  empty segment
		try {

			String segment = theGame.getText(pos1,pos2-pos1);
			return StringUtil.isWhitespace(segment);

		} catch (BadLocationException e) {
			//  can't help it
		}
		return false;
	}

	protected boolean doReplaceStaticText(Node node, int pos1, int pos2,
	                                   String newText, int padding)
		throws BadLocationException
	{
		if (newText.length() > 0) {
			//	delegate to previous or next node
			if (pos1 <= node.getStartOffset()) {
				Node prev = node.prevEditable(false);
				if (prev != null) {
					pos2 = prev.getEndOffset();   //  trim off trailing space
					pos1 = Math.min(pos1,pos2);
					doReplaceNode(prev, pos1,pos2,newText,padding);
					return true;
				}
			}
			if (isWhiteSpace(node.getStartOffset(),pos1)) {
				Node prev = node.prevEditable(false);
				if (prev != null) {
					pos2 = prev.getEndOffset();   //  trim off trailing space
					pos1 = Math.min(pos1,pos2);
					doReplaceNode(prev, pos1,pos2,newText, Util.plus(padding,PADDING_LEADING));
					return true;
				}
			}

			if (pos2 >= node.getEndOffset()) {
				Node next = node.nextEditable(false);
				if (next != null) {
					pos1 = next.getStartOffset();
					pos2 = Math.max(pos1,pos2);
					doReplaceNode(next, pos1,pos2,newText, padding);
					return true;
				}
			}

			if (isWhiteSpace(pos2,node.getEndOffset())) {
				Node next = node.nextEditable(false);
				if (next != null) {
					pos1 = next.getStartOffset();
					pos2 = Math.max(pos1,pos2); 
					doReplaceNode(next, pos1,pos2,newText, Util.plus(padding,PADDING_TRAILING));
					return true;
				}
			}
		}
		//  else: can't edit
        return false;
	}

    //  implements CaretListener

    /**
     * notify caret movements
     */
    public void caretUpdate(CaretEvent e)
    {
        if (!caretListen) return;
		if (theGame.ignoreCaretUpdate) return;
	    if (Application.theApplication.isContextMenuShowing()) return;
			//  why was that ?
	    caretUpdate(e.getDot(), e.getMark());
	}

	public void caretUpdate()
	{
		caretUpdate(getSelectionStart(),getSelectionEnd());
	}

	public void caretUpdate(int startPos, int endPos)
	{
        Node firstNode = getNode(startPos);
        Node lastNode;

        if (endPos != startPos) {
            //  selection, how do we handle it ?
            lastNode = getNode(endPos);
        } else
            lastNode = firstNode;

        if (firstNode == null) {
            /* */;
        } else if (firstNode != lastNode) {
            //  selection spans several nodes
            /* */;
        } else {
            //  selection applies to exactly one node
            switch (firstNode.type()) {
                case MOVE_NODE:
                    //  move clicked: goto that move
                    Command cmd = new Command("move.goto", null, firstNode);
                    Application.theCommandDispatcher.forward(cmd, Application.theApplication);
                    //  will eventually respond with "move.notify"
                    //  which triggers adjustHighlight()
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @return true if the complete document is selected
     */
    public boolean allSelected()
    {
        return (getSelectionStart()==0) && (getSelectionEnd()>=theGame.getLength());
    }

    public boolean noneSelected()
    {
        return getSelectionStart() >= getSelectionEnd();
    }


    public boolean plainAttributeLocal()
    {
        boolean  result = false;
        Iterator i = getCommentNodeIterator(theGame,getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            CommentNode node = (CommentNode)i.next();
            int p3 = Math.max(getSelectionStart(),node.getStartOffset());
            int p4 = Math.min(getSelectionEnd(),node.getEndOffset());
            if (p4 > p3) {
                node.toggleAttribute(theGame, p3,p4, null);
                theGame.setDirty(true);
                result = true;
            }
        }
        return result;
    }

    public boolean plainAttributeGlobal(JoStyleContext styleContext)
    {
        boolean result = false;
        Iterator i = getDefaultStyleIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            Style style = (Style)i.next();
            style.removeAttribute(StyleConstants.Bold);
            style.removeAttribute(StyleConstants.Italic);
            style.removeAttribute(StyleConstants.Underline);
            style.removeAttribute(StyleConstants.Foreground);

            theGame.setDirty(true);
            result = true;
        }
        return result;
    }


	public boolean toggleAttributeLocal(Object attribute)
	{
        boolean  result = false;
        Iterator i = getCommentNodeIterator(theGame,getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            CommentNode node = (CommentNode)i.next();
            int p3 = Math.max(getSelectionStart(),node.getStartOffset());
            int p4 = Math.min(getSelectionEnd(),node.getEndOffset());
            if (p4 > p3) {
                node.toggleAttribute(theGame, p3,p4,attribute);
                theGame.setDirty(true);
                result = true;
            }
        }
        return result;
	}

    public boolean toggleAttributeGlobal(JoStyleContext styleContext, Object attribute)
    {
        boolean result = false;
        Iterator i = getDefaultStyleIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            Style style = (Style)i.next();
            Boolean value = (Boolean)JoStyleContext.get1Attribute(style,attribute);
            if (value==null || !value.booleanValue())
                style.addAttribute(attribute,Boolean.TRUE);
            else
                style.addAttribute(attribute,Boolean.FALSE);
            theGame.setDirty(true);
            result = true;
        }
        return result;
    }

    public boolean toggleAttributeRoot(JoStyleContext styleContext, Object attribute)
    {
        Style style = styleContext.getStyle("base");
        if (attribute==null) {
            style.removeAttribute(StyleConstants.Bold);
            style.removeAttribute(StyleConstants.Italic);
            style.removeAttribute(StyleConstants.Underline);
        }
        else {
            Boolean value = (Boolean)JoStyleContext.get1Attribute(style,attribute);
            if (value==null || !value.booleanValue())
                style.addAttribute(attribute,Boolean.TRUE);
            else
                style.addAttribute(attribute,Boolean.FALSE);
        }
        return true;
    }

	public boolean alignParagraphLocal(int align)
	{
        boolean result = false;
        Iterator i = getCommentNodeIterator(theGame,getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            CommentNode node = (CommentNode)i.next();
            int p3 = Math.max(getSelectionStart(), node.getStartOffset());
            int p4 = Math.min(getSelectionEnd(), node.getEndOffset());

            node.alignParagraph(theGame, p3,p4,align);
            theGame.setDirty(true);
            result = true;
        }
        return result;
	}

    public boolean alignParagraphGlobal(StyleContext context, int align)
    {
        boolean result = false;
        Iterator i = getDefaultStyleIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            Style style = (Style)i.next();
            StyleConstants.setAlignment(style,align);
            theGame.setDirty(true);
            result = true;
        }
        return result;
    }

    public boolean alignParagraphRoot(StyleContext context, int align)
    {
        Style style = context.getStyle("base");
        StyleConstants.setAlignment(style,align);
        return true;
    }

    public boolean setColorLocal(Color color)
    {
        boolean result = false;
        Iterator i = getCommentNodeIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            CommentNode node = (CommentNode)i.next();
            int p3 = Math.max(getSelectionStart(), node.getStartOffset());
            int p4 = Math.min(getSelectionEnd(), node.getEndOffset());
            if (p4 > p3) {
                node.setColor(theGame,p3,p4,color);
                theGame.setDirty(true);
                result = true;
            }
        }
        return result;
    }

    public boolean setColorGlobal(StyleContext context, Color color)
    {
        boolean result = false;
        Iterator i = getDefaultStyleIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            Style style = (Style)i.next();
            StyleConstants.setForeground(style,color);
            theGame.setDirty(true);
            result = true;
        }
        return result;
    }

    public boolean setColorRoot(StyleContext context, Color color)
    {
        Style style = context.getStyle("base");
        StyleConstants.setForeground(style,color);
        return true;
    }

    /**
     * increment font size by "factor", but at least by "min" points
     * @param factor
     * @param min
     */
	public boolean modifyFontSizeLocal(float factor, int min)
	{
        boolean result = false;
        Iterator i = getCommentNodeIterator(theGame, getSelectionStart(), getSelectionEnd());
        if (i!=null) while (i.hasNext())
        {
            CommentNode node = (CommentNode)i.next();
            int p3 = Math.max(getSelectionStart(), node.getStartOffset());
            int p4 = Math.min(getSelectionEnd(), node.getEndOffset());
            if (p4 > p3) {
                node.modifyFontSize(theGame,p3,p4,factor,min);
                theGame.setDirty(true);
                result = true;
            }
        }
        return result;
	}
    /**
      * increment font size by "factor", but at least by "min" points
      * @param factor
      * @param min
      */
     public boolean modifyFontSizeGlobal(JoStyleContext context, float factor, int min)
     {
        //   get all affected nodes/styles
        boolean result = false;
        Iterator i = getDefaultStyleIterator(theGame, getSelectionStart(), getSelectionEnd(),
                                                context, StyleConstants.FontSize);
        if (i!=null) while (i.hasNext())
        {
            Style style = (Style)i.next();
            if (style!=null) {
				context.modifyFontSize(style,factor,min);
				theGame.setDirty(true);
				result = true;
            }
        }
        return result;
     }
    /**
       * increment font size by "factor", but at least by "min" points
       * @param factor
       * @param min
       */
    public boolean modifyFontSizeRoot(JoStyleContext context, float factor, int min)
    {
        context.modifyFontSize(null,factor,min);
        theGame.setDirty(true);
        return true;
    }


    public Set getDefaultStyles(StyledDocument doc, int from, int to)
    {
        Node firstNode = getNode(from);
        Node lastNode = getNode(to);

        if (firstNode==null) return null;

        Set result = new HashSet();
        for (Node node = firstNode; node != null; node = node.next())
        {
            Style style = node.getDefaultStyle(doc);
            if (style!=null)
                result.add(style);
            if (node==lastNode) break;
        }
        return result;
    }

    public Set getCommentNodes(StyledDocument doc, int from, int to)
    {
        Node firstNode = getNode(from);
        Node lastNode = getNode(to);

        if (firstNode==null) return null;

        Set result = new HashSet();
        for (Node node = firstNode; node != null; node = node.next())
        {
            if (node.is(Node.COMMENT_NODE))
                result.add(node);
            if (node==lastNode) break;
        }
        return result;
    }

    public Iterator getDefaultStyleIterator(StyledDocument doc, int from, int to)
    {
        return getDefaultStyleIterator(doc,from,to, null,null);
    }

    public Iterator getDefaultStyleIterator(StyledDocument doc, int from, int to,
                                            JoStyleContext context, Object definingAttribute)
    {
        Set set = getDefaultStyles(doc,from,to);
        if (set==null || set.isEmpty())
            return null;
        if (definingAttribute!=null)
            set = context.getDefiningParents(set,definingAttribute);
        if (set==null || set.isEmpty())
            return null;
        else
            return set.iterator();
    }


    public Iterator getCommentNodeIterator(StyledDocument doc, int from, int to)
    {
        Set set = getCommentNodes(doc,from,to);
        if (set==null || set.isEmpty())
            return null;
        else
            return set.iterator();
    }
}
