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

/**
 * models a game
 */

import de.jose.Application;
import de.jose.Util;
import de.jose.Version;
import de.jose.Language;
import de.jose.sax.JoContentHandler;
import de.jose.chess.*;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.ParamStatement;
import de.jose.task.db.GameUtil;
import de.jose.util.ReflectionUtil;
import de.jose.util.StringUtil;
import de.jose.view.style.JoStyleContext;

import java.util.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xml.sax.SAXException;

public class Game
		extends de.jose.Document
		implements PgnConstants, Constants, BinaryConstants, INodeConstants
{
	//-------------------------------------------------------------------------------
	//	inner class
	//-------------------------------------------------------------------------------

	/**
	 * this DocumentEvent is fired whenver a tag value changes
	 */
	public class TagChangeEvent implements DocumentEvent
	{
		protected TagNode tagNode;

		public TagChangeEvent (TagNode t) {
			tagNode = t;
		}

		public javax.swing.text.Document getDocument() 	{ return Game.this; }

		public TagNode getTagNode() 					{ return tagNode; }

		public String getKey()							{ return tagNode.getKey(); }

		public Object getValue()						{ return tagNode.getValue(); }


		public int getLength() 							{ return tagNode.getLength(); }

		public int getOffset() 							{ return tagNode.getStartOffset(); }

		public DocumentEvent.EventType getType() 		{ return DocumentEvent.EventType.CHANGE; }

		public DocumentEvent.ElementChange getChange(Element elem) { return null; }
	}

	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	When inserting a new move ...	*/
	/**	create a new variation	*/
	public static final int NEW_LINE		= 1;
	/**	create a new main variation	*/
	public static final int NEW_MAIN_LINE	= 2;
	/**	overwrite the current line	*/
	public static final int OVERWRITE		= 3;
	/**	always ask the user	*/
	public static final int ASK				= 4;
	/**	returned by user	*/
	public static final int CANCEL			= -1;

	/** return value from insertMove(): a new node was inserted into the document    */
	public static int  INSERT_NEW_NODE     = +1;
	/** return value from insertMove(): the move already exists in the variation tree */
	public static int  INSERT_EXISTS       =  0;
	/** return value from insertMove(): insert was aborted by user  */
	public static int  INSERT_USER_ABORT   = -1;

	/**	SQL for retrieving one game row for display	 */
	public static final String DISPLAY_SELECT =
//             " %STRAIGHT_JOIN% "
             "       Game.Id,Game.CId,Game.Idx,Attributes,PlyCount, "
            +"       WhiteId, White.Name, WhiteTitle, WhiteELO, "
            +"       BlackId, Black.Name, BlackTitle, BlackELO, "
            +"       Result, GameDate, EventDate, DateFlags, "
			+"		 EventId, Event.Name, SiteId, Site.Name, Round, Board, "
            +"       Game.ECO, OpeningId, Opening.Name, AnnotatorId, Annotator.Name, FEN, MoreGame.Info, "
            +"       MoreGame.Bin, MoreGame.Comments, MoreGame.Eval ";
	public static final String DISPLAY_FROM =
             " Game, MoreGame, Player White, Player Black, Event, Site, Opening, Player Annotator ";
	public static final String DISPLAY_WHERE =
             " Game.Id = MoreGame.GId "
            +"   AND White.Id = WhiteId "
            +"   AND Black.Id = BlackId "
            +"   AND Event.Id = EventId "
            +"   AND Site.Id = SiteId "
            +"   AND Opening.Id = OpeningId "
            +"   AND Annotator.Id = AnnotatorId ";

	public static final String DISPLAY_SQL =
	        "SELECT "+DISPLAY_SELECT+
	        " FROM "+DISPLAY_FROM+
	        " WHERE "+DISPLAY_WHERE+
	        "   AND Game.Id = ? ";

    /** STRAIGHT_JOIN is a hint to MySQL to not use the query optimizer
     *  because the MySQL "optimizer" will *never* find a good plan
     */

    /**	SQL for retrieving one game row for export	 *
    public static final String EXPORT_SQL_SELECT =
           " %STRAIGHT_JOIN% "+
           " Game.Id, Game.Idx, Game.PlyCount, Game.Result, "+
           " White.Name, Black.Name, Game.WhiteELO, Game.BlackELO, MoreGame.WhiteTitle, MoreGame.BlackTitle, "+
           " Event.Name, Site.Name, Game.GameDate, Game.EventDate, Game.DateFlags, Opening.ECO, Opening.Name, Game.ECO, "+
           " Annotator.Name, MoreGame.Round, MoreGame.Board, MoreGame.FEN, "+
           " MoreGame.Info, MoreGame.Bin, MoreGame.Comments ";
    public static final String EXPORT_SQL_FROM =
            " Game, Player White, Player Black, Event, Site, Opening, Player Annotator, MoreGame ";
	public static final String EXPORT_SQL_WHERE =
           " Game.WhiteId = White.Id "+
           "   AND Game.BlackId = Black.Id "+
           "   AND Game.EventId = Event.Id "+
           "   AND Game.SiteId = Site.Id "+
           "   AND Game.OpeningId = Opening.Id "+
           "   AND Game.AnnotatorId = Annotator.Id "+
           "   AND MoreGame.GId = Game.Id ";
*/
    /** Attribute bits  */
    /** this game contains variation line   */
    public static final short   HAS_VARIATIONS  = 0x0001;
    /** this game contains used comments    */
    public static final short   HAS_COMMENTS    = 0x0002;
    /** this game contains parse errors */
    public static final short   HAS_ERRORS      = 0x0004;
	/** this is a FRC/Chess960 game */
	public static final short   IS_FRC          = 0x0008;

	/**	user assisgned flags	*/
	public static final short	USER_FLAG1		= 0x0010;
	public static final short	USER_FLAG2		= 0x0020;
	public static final short	USER_FLAG3		= 0x0030;
	public static final short	USER_FLAG4		= 0x0040;

    /** this attribute indicates that there is a position index for this game
     *  (see MoreGame.PosMain and MoreGame.PosVar)
     * */
    public static final int POS_INDEX           = 0x0100;

	public static final DateFormat YEAR_FORMAT	= new SimpleDateFormat("yyyy");

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

    /** the document tree; contains exactly three elements: "tags", "mainLine", "result"    */
    protected LineNode  root;

	/**	list of tag nodes */
	protected LineNode	tags;

	/**	the main line	*/
	protected LineNode	mainLine;

	/**	the result	*/
	protected ResultNode result;


    /** reference to Collection */
    protected int   collectionId;
    /** index in collection */
    protected int   gameIndex;
	/**	attributes (if mainLinae is not available	*/
	protected int 	origAttributes;
	/**	ply count (if mainLine is not available) */
	protected int	origPlyCount;

	/**	current position	 */
	protected de.jose.chess.Position	position;
	/**	most recent move  */
	protected MoveNode	currentMove;
	/**	flag that is set during some edit operations
	 * 	CaretUpdates are the result of internal edits, NOT of user interaction
	 *  and should be ignored.
	 */
	public boolean ignoreCaretUpdate = false;
	/**
	 * true if the usr adjudictaed the result; only set once per session
	 */
	public boolean askedAdjudicated = false;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public Game()
	{
		tags = new LineNode(this);
		mainLine = new LineNode(this);
//		setupDoc();
		dirty = false;
	}

	public Game(JoStyleContext styles,
				String white, String black,
				Date date, String fen, Position pos)
	{
		this(styles,pos);
		setTagValue(TAG_WHITE,white);
		setTagValue(TAG_BLACK,black);
		setTagValue(TAG_DATE, PgnDate.toPgnDate(date));
		if (fen!=null && !fen.equals(START_POSITION))
			setTagValue(TAG_FEN, fen);
		position.setup(fen);
		dirty = false;
	}

	public Game(JoStyleContext styles, Position pos)
	{
		super((styles!=null) ? styles:new JoStyleContext());

		tags = new LineNode(this);

		newTagNode(null,	TAG_EVENT,", ");
		newTagNode(null, 	TAG_EVENT_DATE,", ");
		newTagNode(null, 	TAG_SITE,", ");
		newTagNode(Language.get("column.game.round")+" ",TAG_ROUND,", ");
		newTagNode(Language.get("column.game.board")+" ",TAG_BOARD,", ");
		newTagNode(null, 	TAG_DATE,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		newTagNode(null,	TAG_WHITE_TITLE," ");
		TagNode whiteName = newTagNode(null,	TAG_WHITE,null);
		newTagNode(" (",	TAG_WHITE_ELO,") ");

		TagLabelNode dash = newTagLabel(" - ",	TagLabelNode.BETWEEN);

		newTagNode(null,	TAG_BLACK_TITLE," ");
		TagNode blackName = newTagNode(null,	TAG_BLACK,null);
		newTagNode(" (",	TAG_BLACK_ELO,") ");
		newTagNode("  ",	TAG_RESULT,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		dash.setTagNodes(whiteName,blackName);

		newTagNode(null,	TAG_ECO," ");
		newTagNode(null,	TAG_OPENING,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		newTagNode("\n"+Language.get("column.game.fen")+": ", TAG_FEN,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		newTagNode("\n",    TAG_VARIANT,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		newTagNode("\n"+Language.get("column.game.annotator")+": ", TAG_ANNOTATOR,null);
		newTagLabel("\n", 	TagLabelNode.LINEEND);

		mainLine = new LineNode(this);
		origAttributes = 0;
		origPlyCount = 0;
		currentMove = null;

        setupDoc();

		setPosition(pos);

		dirty = false;
	}

	public final int getCollectionId()		{ return collectionId; }

	public void setPosition(Position pos)
	{
		if (pos!=null)
			position = pos;
		else {
			position = new Position();
			position.setOptions(Position.DETECT_ALL);
		}
	}

	public void clear(String fen)
	{
		for (TagNode t = (TagNode)tags.first(TAG_NODE); t != null; t = (TagNode)t.next(TAG_NODE))
		   t.clear();

		result.setResult(RESULT_UNKNOWN);
		mainLine.clear(this);
		result.insertLast(mainLine);

		currentMove = null;
		position.setup(fen);
		if (fen!=null && !fen.equals(START_POSITION))
			setTagValue(PgnConstants.TAG_FEN, fen);
		
		reformat();

		clearId();
		askedAdjudicated = false;
		dirty = false;
	}

    private void setupDoc()
    {
        root = new LineNode(this);
		tags.insertLast(root);
	    tags.setKeepTogether(true);

//        StaticTextNode spacer = new StaticTextNode("\n","header","header");
//		spacer.insertLast(root);
//        spacer.insertFirst(mainLine);

		result = (ResultNode)mainLine.last(Node.RESULT_NODE);
	    String pgnResult = (String)getTagValue(TAG_RESULT);
	    int pgnCode = RESULT_UNKNOWN;
	    if (pgnResult!=null) pgnCode = PgnUtil.parseResult(pgnResult);

		if (result==null)
		{
			//  result node missing; create from PGN tag
			result = new ResultNode(pgnCode);
			result.insertLast(mainLine);
			setResult(pgnCode);
		}
	    else if (result.getResult()!=pgnCode)
	    {
		    //  result node differs from PGN tag
		    //  (1) prefer PGN tag, if present; otherwise prefer result node
		    if (pgnResult==null)
			    setResult(result.getResult());
		    else
			    setResult(pgnCode);
	    }

        mainLine.insertLast(root);
    }

	public void reformat()
	{
		ignoreCaretUpdate = true;
		try {
			synchronized (this) {
				try {
					replace(0,getLength(),"",null);
				} catch (BadLocationException e) {
					//	don't mind
				}
				getStyleContext().assertCustomFonts();
				position.reset();
				insertNode(0,root);
				currentMove = null;
			}
		} finally {
			ignoreCaretUpdate = false;
		}
	}

	public final LineNode getMainLine()	{ return mainLine; }

	public final de.jose.chess.Position getPosition()	{ return position; }


	public final boolean isMainLine()
	{
		return isMainLine(currentMove);
	}

	public final boolean isMainLine(Node node)
	{
		return (node!=null) && (node.parent()==mainLine);
	}

	public final boolean isMainTree(Node node)
	{
		return node.isDescendantOf(mainLine);
	}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public void setup(String fen)
	{
		if (!isEmpty()) throw new IllegalStateException();

		if (fen==null || fen.equals(START_POSITION))
			setTagValue(PgnConstants.TAG_FEN, null);
		else
			setTagValue(PgnConstants.TAG_FEN, fen);
		position.setup(fen);
	}

	protected final TagNode getTag(String key) {
		for (Node t = tags.first(TAG_NODE); t != null; t = t.next(TAG_NODE))
			if (((TagNode)t).getKey().equals(key))
				return (TagNode)t;
		return null;
	}

	public final Object getTagValue(String key) {
		TagNode t = getTag(key);
		if (t!=null)
			return t.getValue();
		else
			return null;
	}

	public final void getTagValues(Map values)
	{
		for (TagNode t = (TagNode)tags.first(TAG_NODE); t!=null; t = (TagNode)t.next(TAG_NODE))
			values.put(t.getKey(), t.getValue());
	}

	public TagNode newTagNode(String prefix, String key, String suffix)
	{
		TagNode tag = new TagNode(key,null);
		tag.insertBefore(tags.last());	//	skip suffix

		if (prefix!=null) {
			TagLabelNode labelNode = new TagLabelNode(prefix, TagLabelNode.PREFIX);
			labelNode.insertBefore(tag);
		}
		if (suffix!=null) {
			TagLabelNode labelNode = new TagLabelNode(suffix, TagLabelNode.SUFFIX);
			labelNode.insertAfter(tag);
		}

		return tag;
	}

	protected TagLabelNode newTagLabel(String text, int location)
	{
		TagLabelNode labelNode = new TagLabelNode(text,location);
		labelNode.insertBefore(tags.last());	//	skip suffix
		return labelNode;
	}


	public final boolean setTagValue(String key, Object value)
	{
//		setTagValue(key,value, "%n%%key%: %value%\n");
		try {
			return setTagValue(key,value, null);
		} catch (Exception e) {
			Application.error(e);
		}
		return false;
	}

	public final boolean setTagValue(String key, Object value, StyledDocument doc)
		throws BadLocationException, ParseException
	{
		TagNode t = getTag(key);

		if (t==null)
			throw new IllegalArgumentException(key);

		value = PgnUtil.normalizeValue(value, t.getDataType());
		if (! Util.equals(value,t.getValue())) {
			t.setValue(value);
			if (doc!=null) updateTag(doc,t);
			setDirty(t);
			return true;
		}
		else
			return false;
	}

	public final void setTagValues(Map map)
	{
		try {
			setTagValues(map,null);
		} catch (Exception e) {
			Application.error(e);
		}
	}

	public final void setTagValues(Map map, StyledDocument doc)
			throws BadLocationException, ParseException
	{
		java.util.Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry ety = (Map.Entry)i.next();
			String key = (String) ety.getKey();
			TagNode t = getTag(key);

			if (t==null) {
				newTagNode("\n"+key+": ",    key, null);
				newTagLabel("\n", 	TagLabelNode.LINEEND);
			}

			setTagValue(key, ety.getValue(), doc);
		}
	}

	protected final void updateTag(StyledDocument doc, TagNode t) throws BadLocationException
	{
		t.update(doc);
		updateTagLabels(doc);
	}

	public final boolean updateTagLabels(StyledDocument doc)  throws BadLocationException
	{
		boolean wasIgnoreCaretUpdate = ignoreCaretUpdate;
		try {
			ignoreCaretUpdate = true;
			boolean result = false;
			for (Node nd = tags.first(); nd!=null; nd=nd.next())
				if (nd.update(doc)) result = true;
			return result;
		} finally {
			ignoreCaretUpdate = wasIgnoreCaretUpdate;
		}
	}

	public void removeTag(String key)
	{
		TagNode t = getTag(key);
		if (t!=null) t.remove();
	}


	public String getTabTitle(int maxLength)
	{
		StringBuffer buf = new StringBuffer();

		String white = (String)getTagValue(TAG_WHITE);
		if (white != null) buf.append(white);

		String black = (String)getTagValue(TAG_BLACK);
		if (black != null) {
			if (buf.length() > 0) buf.append("-");
			buf.append(black);
		}

		if (maxLength > 3 && buf.length() > maxLength) {
			buf.setLength(maxLength-3);
			buf.append("...");
		}

		return buf.toString();
	}

	public String getTabToolTip()
	{
		StringBuffer buf = new StringBuffer();
		String title = getTabTitle(-1);
		if (title.length() > 0) {
			buf.append("<b>");
			buf.append(title);
			buf.append("</b>");
		}

		String event = (String)getTagValue(TAG_EVENT);
		String site = (String)getTagValue(TAG_SITE);
		Date date = (Date)getTagValue(TAG_DATE);
		if (event!=null || site != null || date != null) {
			if (buf.length() > 0) buf.append("<br>");
			if (event!=null) {
				buf.append(event);
				if (site!=null || date!=null) buf.append(", ");
			}

			if (site != null) {
				buf.append(site);
				if (date != null) buf.append(" ");
			}

			if (date != null) buf.append(YEAR_FORMAT.format(date));
		}

		String eco = (String)getTagValue(TAG_ECO);
		String opening = (String)getTagValue(TAG_OPENING);
		if (eco != null || opening != null) {
			if (buf.length() > 0) buf.append("<br>");
			if (eco != null) buf.append(eco);
			if (eco != null && opening != null) buf.append(" ");
			if (opening != null) buf.append(opening);
		}

		return buf.toString();
	}


	public final int getResult()
	{
		return result.getResult();
	}

	public final int getResultFor(int color)
	{
		return getResultFor(getResult(),color);
	}

	public static final int getResultFor(int result, int color)
	{
		if (EngUtil.isWhite(color) || result < 0)
			return result;
		else
			return 2-result;
	}

	public final boolean setResult(int res)
	{
		String str = (res==PgnConstants.RESULT_UNKNOWN) ? null:PgnUtil.resultString(res);
		boolean dirty = setTagValue(TAG_RESULT, str) || (result.getResult()!=res);
		if (dirty) result.setResult(res);
		return dirty;
	}

	public final boolean setResult(int res, Game doc)
			throws BadLocationException, ParseException
	{
		try {
			doc.ignoreCaretUpdate = true;

			return setResult(res);

		} finally {
			doc.ignoreCaretUpdate = false;
		}
	}

	public final void setDirty(TagNode tag) {
		super.setDirty(true);
		if (fireEvents) fireChangedUpdate(new TagChangeEvent(tag));
	}

    public static void setAttribute(JoConnection conn, int CId, int attribute, boolean on)
        throws SQLException
    {
        String sql;
        if (on)
            sql = "UPDATE Game SET Attributes = Attributes | "+attribute+" WHERE CId = ?";
        else
            sql = "UPDATE Game SET Attributes = Attributes & ~"+attribute+" WHERE CId = ?";

        JoPreparedStatement pstm = conn.getPreparedStatement(sql);
        pstm.setInt(1,CId);
        pstm.execute();
    }

    public static void updatePositionIndex(JoConnection conn, int GId,
                                           byte[] posMain, int posMainLen,
                                           byte[] posVar, int posVarLen)
        throws SQLException
    {
        String sql =
                "UPDATE Game,MoreGame " +
                " SET Game.Attributes = Game.Attributes | "+Game.POS_INDEX+"," +
                "     MoreGame.PosMain = ?," +
                "     MoreGame.PosVar = ?" +
                " WHERE Game.Id = ? " +
                "   AND Game.Id = MoreGame.GId";

        JoPreparedStatement pstm = conn.getPreparedStatement(sql);
        pstm.setBytes(1,posMain,0,posMainLen);
        pstm.setBytes(2,posVar,0,posVarLen);
        pstm.setInt(3,GId);
        pstm.execute();
    }


	protected MoveNode nextMove() {
		if (currentMove==null)
			return mainLine.firstMove();
		else
			return currentMove.nextMove();
	}

	protected void insertIntoCurrentLine(Node newNode, MoveNode after)
	{
		Node nextMove = null;
		if (after!=null)
			nextMove = after.nextMove();
		if (nextMove != null)	//	insert in front of next move
			newNode.insertBefore(nextMove);
		else {
			Node label;
			if (after!=null)
				label = after.next(STATIC_TEXT_NODE,RESULT_NODE);
			else
				label = mainLine.last(STATIC_TEXT_NODE,RESULT_NODE);

			if (label!=null)
				newNode.insertBefore(label);	//	insert in front of suffix label
			else {
				//	insert at end of line
				LineNode line;
				if (after==null)
					line = mainLine;
				else
					line = after.parent();
				newNode.insertBefore(line.last());	//	skip suffix
			}
		}
	}

	/**
	 * if a line is cut at a spcific node, is the current move deleted ?
	 *
	 * @param node
	 * @return true if the current move is dependent from 'node'
	 */
	public boolean cutBeforeCurrent(Node node)
	{
		if (currentMove==null) return false;
		LineNode line = node.parent();
		return line.containsAfter(node,currentMove);
	}

	public synchronized MoveNode cutBefore(Node node) throws BadLocationException
	{
		boolean wasIgnoreCaretUpdate = ignoreCaretUpdate;
		try {
			ignoreCaretUpdate = true;

		boolean cutCurrent = cutBeforeCurrent(node);
		MoveNode closeMove = null;
		if (cutCurrent)	//	currentMove will be deleted; look for a close replacement
			closeMove = node.previousMove();

		LineNode line = node.parent();
		line.cutBefore(this, node, line.level() > 1);

		if (line.previous()!=null && line.previous().is(LINE_NODE))
			((LineNode)line.previous()).updateLabels(this);
		if (line.next()!=null && line.next().is(LINE_NODE))
			((LineNode)line.next()).updateLabels(this);

		updateMoveCount(line);
		setDirty();
		//	current move was deleted; goto closest available
		if (cutCurrent)
			gotoMove(closeMove);

		return closeMove;
		} finally {
			ignoreCaretUpdate = wasIgnoreCaretUpdate;
	}
	}

	public synchronized int insertMove(int ply, Move mv, int writeMode) throws BadLocationException
	{
		if (ply < 0) ply = getPosition().gamePly();
		boolean wasIgnoreCaretUpdate = ignoreCaretUpdate;
		try {
			ignoreCaretUpdate = true;
			return doInsertMove(ply,mv,writeMode);
		} finally {
			ignoreCaretUpdate = wasIgnoreCaretUpdate;
		}
	}

	public synchronized int doInsertMove(int ply, Move mv, int writeMode) throws BadLocationException
	{
		MoveNode move = new MoveNode(ply,mv);

		/*	check if move is equal to next move in current line	*/
		MoveNode next = nextMove();
		if (next==null) {
			/*	insert at end of current line (but not after Result Node!)	*/
			insertIntoCurrentLine(move,currentMove);
			insertNode(move);

			currentMove = move;  //  DON'T replay. already done by insertNode() !
            setDirty();
			return INSERT_NEW_NODE;
		}
		else if (next.move.equals(mv)) {
			/*	follow current line	*/
			(currentMove = next).play(position);
			//currentMove = next;
			return INSERT_EXISTS;
		}

		/**	check if move is equal to start of variation
		*	(if so, enter it)
		*/
		Node nd;
		for (nd = next.next(); nd!=null; nd = nd.next())
			if (nd.type()==MOVE_NODE)
				break;
			else if (nd.type()==LINE_NODE) {
				MoveNode first = ((LineNode)nd).firstMove();
				if (first!=null && first.move.equals(mv)) {
					/*	enter variation	 */
					(currentMove = first).play(position);
					//currentMove = first;
					return INSERT_EXISTS;
				}
            }

		if (writeMode <= 0) writeMode = Application.theApplication.getInsertMoveWriteMode(mv);

		switch (writeMode)
		{
		default:
		case OVERWRITE:
			//	cut variation
			LineNode line;
			Node cut = next.previous(); //	note that there MUST be nextMove() - otherwise we wouldn't start a variation
			line = cut.parent();
			line.cutBefore(this, next, false);
			//	append move (in element hierarchy)
			move.insertAfter(cut);

				//	insert into document
			insertNode(move);
			currentMove = move;  //  DON'T replay. already done by insertNode() !
			updateLabels(line);
			updateMoveCount(line);
			setDirty();
			return INSERT_NEW_NODE;

		case NEW_MAIN_LINE:
			//	todo this should be effectively NEW_LINE, followed by promotoLine()
			/*LineNode variation = new LineNode(this);
			move.insertAfter(variation.first());
			insertIntoCurrentLine(variation, next);
			(currentMove = move).play(position);

			promoteLine(variation);
			reformat(); //  side effect: currentMove=null
			gotoMove(move);*/
			LineNode variation = createNewLine(move,next);
			promoteLine(variation);
			return INSERT_NEW_NODE;

		case NEW_LINE:
			createNewLine(move, next);
			return INSERT_NEW_NODE;

		case CANCEL:
			return INSERT_USER_ABORT;
		}
	}

	private LineNode createNewLine(MoveNode move, MoveNode next)
	{
		LineNode variation = new LineNode(this);
		move.insertAfter(variation.first());	//	skip prefix
		insertIntoCurrentLine(variation, next);	//	insert into structure
		//	note that there MUST be nextMove() - otherwise we wouldn't start a variation
		/**	insert variation at end of current line	*/
		insertNode(variation);	//	insert into document
		currentMove = move;  //  DON'T replay. already done by insertNode() !
		if (variation.level()>=2) currentMove.play(position);

		updateLabels(variation);
		updateMoveCount(variation);
		setDirty();
		return variation;
	}

	public MoveNode getCurrentMove()	{ return currentMove; }

	public void updateMoveCount(Node nd)
	{
		if (!nd.is(MOVE_NODE)) nd = nd.next(MOVE_NODE);
		try {
			if (nd!=null)
				((MoveNode)nd).updateMoveCount(this);
		} catch (BadLocationException blex) {
			Application.error(blex);
		}
	}


	protected void insertNode(Node node)
	{
        insertNode(-1,node);
    }


	public void promoteLine (LineNode V)
	{
		if (V.level()==0)
			throw new IllegalArgumentException("can't promote main line");

		LineNode P = V.parent();
		Node a = V.previous(LINE_NODE,MOVE_NODE);
		if (a.type==LINE_NODE) {
			//	swap with sibling
			V.swap(a);

			/* todo necessary ?
			updateLabels(V);
			updateLabels((LineNode)a);

			//updateMoveCount(V.first());
			//updateMoveCount(a);
			*/
			return;
		}

		/**
		 * see doc/Promotion.drawio.svg
		 *
		 * identify four sections
		 */
		NodeSection A,B, C=null,D=null;
		assert(a.type==MOVE_NODE && a instanceof MoveNode);

		A = new NodeSection(a,V.previous());
		B = new NodeSection(V.first(),V.last());

		MoveNode d = (MoveNode)V.next(MOVE_NODE);
		if (d!=null) {
			D = new NodeSection(d,P.last());

			MoveNode c = V.secondMove();
			if (c!=null) {
				assert(c.ply==d.ply);
				C = new NodeSection(c, V.last());
				B.setLast(c.previous());
			}
		}

		A.trim(STATIC_TEXT_NODE);
		B.trim(STATIC_TEXT_NODE);
		if (C!=null) C.trim(STATIC_TEXT_NODE);
		if (D!=null) {
			D.trim(STATIC_TEXT_NODE);
			D.trim(RESULT_NODE);
			D.trim(STATIC_TEXT_NODE);
		}

		//	swap A with B
		A.swap(B);

		//	swap C with D
		if (C!=null && D!=null)
			C.swap(D);
		else if (D!=null)
			A.append(D);

		/*	todo necessary ?
		updateLabels(P);
		updateLabels(V);
		updateMoveCount(B.first);
		updateMoveCount(A.first);
		*/

		setDirty();
/*
		/**	is there a sibling line ?	* /
		LineNode sibling = line.previousSibling();
		if (sibling!=null) {
			/**	if so, change order	* /
			line.remove();
			line.insertBefore(sibling);
		}
		else {
			LineNode parent = line.parent();
			/**	replace parent with this line (more complicated)	* /
			MoveNode pmv = (MoveNode)line.previous(MOVE_NODE);
			//	this move node must exist
			Node cut = pmv.previous();
			if (cut==null) throw new NullPointerException();    //  must not happen
			/**	extract all siblings	* /
			Node[] siblings = parent.extractSubLines(line.next());
			line.remove();
			LineNode oldMainLine = parent.extractLine(pmv);
			/**	insert new main line (line) * /
			Node after = line.first(MOVE_NODE);
			while (after.next()!=null && after.next().is(ANNOTATION_NODE)) after = after.next();
			//  keep annotation with move
			parent.moveLine(cut,line);
			for (int i=siblings.length-1; i >= 0; i--)
				siblings[i].insertAfter(after);
			oldMainLine.insertAfter(after);
		}
		setDirty();
*/
	}


	protected void updateLabels(LineNode var)
	{
		try {
			Node prev = var.previous();
			if (prev!=null && prev.is(LINE_NODE))
				((LineNode)prev).updateLabels(this);
			Node next = var.next();
			if (next!=null && next.is(LINE_NODE))
				((LineNode)next).updateLabels(this);
		} catch (BadLocationException e) {
			Application.error(e);  // what's this ?
		}
	}

    /**
     * update the text document with the contents of a node
     * @param at text location; < 0 to calculate automatically
     * @param node
     */
	protected void insertNode(int at, Node node)
	{
		if (at < 0) {
			if (node.hasPrevious())
				at = node.previous().getEndOffset();
			else if (node.hasNext())
				at = node.next().getStartOffset();
			else
				at = 0;
		}

		try {
			node.insert(this, at);
		} catch (BadLocationException blex) {
			Application.error(blex);
//			throw new RuntimeException(blex.getMessage());
		}
	}
/*
	protected void newTree(int at, Node tree)
	{
		if (tree.is(LINE_NODE))
        {
			for (Node nd = ((LineNode)tree).first(); nd != null; nd = nd.next()) {
				newTree(at, nd);
                at += nd.getLength();
            }
		}
		else
			insertNode(at,tree);	//	actually leaf
	}
*/
    public Node findNode(int pos)
    {
        return root.findNode(pos);
    }

	public boolean gotoMove(PositionFilter filter)
	{
		if (filter==null || filter.targetKey==0L)
			return false;
		else
			return gotoMove(filter.targetKey, filter.targetKeyReversed, filter.searchVariations);
	}

	public boolean gotoMove(long targetKey, long targetKeyReversed,
	                         boolean searchVariations)
	{
		position.reset();
		position.setOption(Position.INCREMENT_HASH,true);
		position.setOption(Position.INCREMENT_REVERSED_HASH,true);
		position.setOption(Position.IGNORE_FLAGS_ON_HASH,true);

		boolean result = gotoMove(mainLine, targetKey,targetKeyReversed, searchVariations);

		position.setOption(Position.IGNORE_FLAGS_ON_HASH,false);

		return result;
	}

	private boolean gotoMove(LineNode line,
	                      long targetKey,
	                      long targetKeyReversed,
	                      boolean searchVariations)
	{
		for (Node node = line.first(); node != null; node = node.next())
		{
			if (node.is(Node.MOVE_NODE))
			{
				MoveNode mnode = (MoveNode)node;
				mnode.play(position);
				if (position.getHashKey().equals(targetKey) ||
				    position.getReversedHashKey().equals(targetKeyReversed))
				{
					currentMove=mnode;
					return true;
				}
			}
			else if (node.is(Node.LINE_NODE) && searchVariations)
			{
				Move mv = position.undoMove();
				position.startVariation();

				if (gotoMove((LineNode)node,targetKey, targetKeyReversed, searchVariations))
					return true;
				else {
					position.undoVariation();
					position.doMove(mv);
				}
			}
		}
		return false;
	}

	public boolean isFirst()
	{
		return currentMove==null;
	}

	public synchronized boolean first()
	{
		if (currentMove==null)
			return false;
		position.reset();
		currentMove = null;
		return true;
	}

	public synchronized Move backward() {
		if (currentMove==null)
			return null;
        Move result = currentMove.move;
		currentMove.undo(position);
		currentMove = currentMove.previousMove();
		return result;
	}

	public boolean canDelete() {
		if (currentMove==null) return false;
		Node next = currentMove.next(MOVE_NODE);
		if (next!=null) return false;	//	use cut line instead
		next = currentMove.next(LINE_NODE);
		if (next!=null) return false;	//	there are variations
		//	last move in line: can delete
		return true;
	}

	public boolean isLast()
	{
		return nextMove() == null;
	}

	public synchronized Move forward() {
		MoveNode next = nextMove();
		if (next==null)
			return null;
		currentMove = next;
		next.play(position);
		return currentMove.move;
	}

	public synchronized boolean last() {
		//	move up to main line
		first();
		while (forward() != null)
			;
		return true;
	}

	public synchronized void resetPosition()
	{
		String fen = (String)getTagValue(TAG_FEN);
		position.setup(fen);
	}


    public synchronized void gotoMove(MoveNode mv, boolean force)
    {
        if (force) {
            position.reset();
            currentMove = null;
        }
        gotoMove(mv);
    }

    public synchronized void gotoMove(MoveNode mv)
	{
		if (currentMove==mv) return;

		/*	traverse the tree	*/
		position.reset();

		if (mv==null) {
            currentMove = null;
            return;
        }

		Node current = mainLine;

		while (current != null) {
            switch (current.type())
            {
            case LINE_NODE:
                if (mv.isDescendantOf((LineNode)current))
                {
                    /*	enter line	*/
                    position.undoMove();
                    current = ((LineNode)current).first();
                    continue;
                }
                break;
            case MOVE_NODE:
                ((MoveNode)current).play(position);
                break;
            }

			if (current==mv) {
			    currentMove = (MoveNode)current;
				return;	//	reached the destination
            }
			else
				current = current.next();
		}
		throw new RuntimeException("tree position not found");
	}

	/**
	 * get preferred page break inside (of before) a node
	 */
	public int getPreferredPageBreak(int pos)
	{
		Node node = findNode(pos);
		//  climb up hierarchy
		while (node!=null) {
			if (node.keepTogether()) pos = node.getStartOffset();
			node = node.parent();
		}
		return pos;
	}



    /**
     * create an statement for exporting one game
     *
    public static JoPreparedStatement getExportStatement(JoConnection conn, int GId)
        throws Exception
    {
	    ParamStatement pstm = new ParamStatement();
	    pstm.select.append(EXPORT_SQL_SELECT);
	    pstm.from.append(EXPORT_SQL_FROM);
	    pstm.where.append(EXPORT_SQL_WHERE);
	    pstm.where.append(" AND Game.Id = ?");
	    pstm.addIntParameter(GId);

        return pstm.toPreparedStatement(conn);
    }
*/
    /**
     * create an statement for exporting one collection
     *
    public static JoPreparedStatement getCollectionExportStatement(JoConnection conn, int CId,
                                                                   SearchRecord srec)
        throws Exception
    {
	    ParamStatement pstm = new ParamStatement();
	    pstm.select.append(EXPORT_SQL_SELECT);
	    pstm.from.append(EXPORT_SQL_FROM);
	    pstm.where.append(EXPORT_SQL_WHERE);
	    pstm.where.append(" AND Game.CId = ?");
	    pstm.addIntParameter(CId);

	    if (srec!=null) srec.makeOrder(pstm);

        return pstm.toPreparedStatement(conn);
    }
*/
    protected void setGameParameters(JoPreparedStatement stm, BinWriter writer,
									 int whiteId, int blackId, boolean withIdx)       throws SQLException
    {
         String annotator = (String)getTagValue(TAG_ANNOTATOR);
         String event     = (String)getTagValue(TAG_EVENT);
         String site      = (String)getTagValue(TAG_SITE);
         String opening   = (String)getTagValue(TAG_OPENING);

         Integer whiteELO = (Integer)getTagValue(TAG_WHITE_ELO);
         Integer blackELO = (Integer)getTagValue(TAG_BLACK_ELO);

         PgnDate gameDate = (PgnDate)getTagValue(TAG_DATE);
         PgnDate eventDate = (PgnDate)getTagValue(TAG_EVENT_DATE);

         int attributes = origAttributes;
		 int plyCount = origPlyCount;

		 if (writer != null) {
			 attributes = 0;
			 if (writer.hasVariations()) attributes |= HAS_VARIATIONS;
			 if (writer.hasComments()) attributes |= HAS_COMMENTS;
			 if (writer.hasErrors()) attributes |= HAS_ERRORS;
             if (writer.hasPositions()) attributes |= POS_INDEX;
			 if (!writer.isClassic()) attributes |= IS_FRC;
			 plyCount = mainLine.countMoves();
		 }

         int i = 1;
         stm.setInt(i++,        collectionId);
         if (withIdx)
            stm.setInt(i++,     gameIndex);
	 	 stm.setInt(i++,        attributes);
         stm.setInt(i++,        plyCount);
         stm.setInt(i++,        getResult());

         stm.setInt(i++,    	whiteId);
         stm.setInt(i++,   		blackId);

         if (whiteELO != null)
             stm.setInt(i++,    whiteELO.intValue());
         else
             stm.setNull(i++,   Types.INTEGER);
         if (blackELO != null)
             stm.setInt(i++,    blackELO.intValue());
         else
             stm.setNull(i++,   Types.INTEGER);

         stm.setInt(i++,    	GameUtil.resolveEvent(stm.getConnection(),event,true));
         stm.setInt(i++,     	GameUtil.resolveSite(stm.getConnection(),site,true));

         stm.setDate(i++,       gameDate);
         stm.setDate(i++,       eventDate);

		 short dateFlags = 0;
		 if (gameDate!=null) dateFlags |= gameDate.getDateFlags();
		 if (eventDate!=null) dateFlags |= (eventDate.getDateFlags() << 8);

         stm.setInt(i++,        dateFlags);

         stm.setInt(i++,    	GameUtil.resolveOpening(stm.getConnection(),opening,true));
         stm.setString(i++,     (String)getTagValue(TAG_ECO));

         stm.setInt(i++,    	GameUtil.resolvePlayer(stm.getConnection(),annotator,true));

         stm.setInt(i++, dbId);
    }

    protected void setMoreGameParameters(JoPreparedStatement stm, BinWriter writer)
			throws SQLException
    {
        int i = 1;
        stm.setString(i++,     (String)getTagValue(TAG_WHITE_TITLE));
        stm.setString(i++,     (String)getTagValue(TAG_BLACK_TITLE));
        stm.setString(i++,     (String)getTagValue(TAG_ROUND));
        stm.setString(i++,     (String)getTagValue(TAG_BOARD));
        stm.setString(i++,     (String)getTagValue(TAG_FEN));
        stm.setString(i++,     getMoreInfo());
		if (writer != null) {
			stm.setBytes(i++,      writer.getText());
			stm.setBytes(i++,      writer.getComments());

			byte[] pos = writer.getMainPositions();
			stm.setBytes(i++,      (pos!=null&&pos.length>0) ? pos:null);

			pos = writer.getVarPositions();
			stm.setBytes(i++,      (pos!=null&&pos.length>0) ? pos:null);

			byte[] eval = mainLine.containsEval() ? mainLine.writeEval() : null;
			stm.setBytes(i++, 		(eval!=null&&eval.length>0) ? eval:null);
		}
        stm.setInt(i++,        dbId);
    }

    public boolean isNew()
    {
        return dbId == 0;
    }

	public boolean isEmpty()
	{
//		return currentMove==null;
		return ((mainLine==null) || (mainLine.firstMove()==null)) &&
		        (getTagValue(TAG_FEN)==null);
	}

	public final void save()	throws Exception
	{
		save(true);
	}

	public void saveInfo(boolean resultDirty)	throws Exception
	{
		save(false);
		if (resultDirty) {
			//  adjust result node in database ?
		}
	}


    public void save(boolean withData)  throws Exception
    {
        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            save(conn,withData);
        } finally {
            JoConnection.release(conn);
        }
    }

	public BinWriter getBinaryData()
	{
		BinWriter writer = null;
		MoveNode oldMove = currentMove;
		try {
			gotoMove((MoveNode)null);
			//  make sure that the position is replayed from the start
			//  otherwise move encoding could go wrong
			writer = new BinWriter(position);
			mainLine.writeBinaryContents(writer);	//	don't write start and end of line; just write contents
			writer.endOfData();
		} finally {
			gotoMove((MoveNode)null);
			gotoMove(oldMove);
		}
		return writer;
	}

    public void save(JoConnection conn, boolean withData)       throws SQLException
    {
        if (isNew()) throw new IllegalArgumentException("can't save new game");

		BinWriter writer = null;
		if (withData) writer = getBinaryData();

		String white = (String)getTagValue(TAG_WHITE);
		String black = (String)getTagValue(TAG_BLACK);

		int whiteId = GameUtil.resolvePlayer(conn,white,true);
		int blackId = GameUtil.resolvePlayer(conn,black,true);

		String sql1 =   "UPDATE Game "+
					   " SET CId=?,Attributes=?,PlyCount=?,Result=?,WhiteId=?,BlackId=?, WhiteELO=?,BlackELO=?,"+
					   "  EventId=?,SiteId=?, GameDate=?,EventDate=?,DateFlags=?, OpeningId=?, ECO=?, AnnotatorId=?"+
					   " WHERE Id = ?";
        //  don't overwrite Game.Idx in DB (DB version is more relevant)
		JoPreparedStatement stm1 = conn.getPreparedStatement(sql1);
		setGameParameters(stm1,writer, whiteId,blackId,false);
		stm1.execute();

        String sql2 =   "UPDATE MoreGame "+
                        " SET WhiteTitle=?,BlackTitle=?,Round=?,Board=?,FEN=?,Info=? ";
		if (withData) {
			sql2 += ",Bin=?,Comments=?,PosMain=?,PosVar=?,Eval=? ";
		}
		sql2	+=      " WHERE GId = ?";

        JoPreparedStatement stm2 = conn.getPreparedStatement(sql2);
        setMoreGameParameters(stm2,writer);
        stm2.execute();

        clearDirty();
    }

    public void saveAs(int collId, int gameIdx)  throws Exception
    {
        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            saveAs(conn, collId,gameIdx);
        } finally {
            JoConnection.release(conn);
        }
    }

	public static int getSequence(JoConnection conn) throws SQLException
	{
		/**	account for a bug in early versions:
		 * 	deletion of dependent tables didn't work correctly, leaving
		 * 	dangling foreign keys in the database.
		 *
		 * 	as a workaround, we choose the sequene from MoreGame, rather than Game
		 */
		return conn.getSequence("MoreGame","GId");
	}

	public static int getSequence(JoConnection conn, int count) throws SQLException
	{
		return conn.getSequence("MoreGame","GId",count);
	}

	public static void resetSequence(JoConnection conn) throws SQLException
	{
		conn.resetSequence("MoreGame","GId");
	}

    public final void saveAs(JoConnection conn, int collId, int gameIdx)      throws Exception
    {
        dbId = Game.getSequence(conn);

        collectionId = collId;
        gameIndex = gameIdx;
        if (gameIndex <= 0)
            gameIndex = Collection.getMaxIndex(conn,collId)+1;

		BinWriter writer = getBinaryData();

		String white = (String)getTagValue(TAG_WHITE);
		String black = (String)getTagValue(TAG_BLACK);

		int whiteId = GameUtil.resolvePlayer(conn,white,true);
		int blackId = GameUtil.resolvePlayer(conn,black,true);

        String sql1 =   "INSERT INTO Game (CId,Idx,Attributes,PlyCount,Result, WhiteId,BlackId, WhiteELO,BlackELO," +
                            "EventId,SiteId, GameDate,EventDate,DateFlags, OpeningId, ECO, AnnotatorId, Id) "+
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        JoPreparedStatement stm1 = conn.getPreparedStatement(sql1);
        setGameParameters(stm1,writer, whiteId, blackId,true);
        stm1.execute();


        String sql2 =   "INSERT INTO MoreGame (WhiteTitle,BlackTitle,Round,Board,FEN,Info,Bin,Comments,PosMain,PosVar,Eval,GId) "+
                        " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        JoPreparedStatement stm2 = conn.getPreparedStatement(sql2);
        setMoreGameParameters(stm2,writer);
        stm2.execute();

        String sql4 =   "UPDATE Collection SET GameCount = GameCount+1, LastModified = ? " +
						" WHERE Id = ? ";
        JoPreparedStatement stm4 = conn.getPreparedStatement(sql4);
        stm4.setTimestamp(1, new Date());
		stm4.setInt(2,collId);
        stm4.execute();

        clearDirty();
    }

    public void setMoreInfo(String info)
    {
        if (info==null) return;
        StringTokenizer tok = new StringTokenizer(info, ";");
        while (tok.hasMoreTokens()) {
            String str = tok.nextToken();
            int k = str.indexOf("=");

            if (k > 0) {
                String key = str.substring(0,k).trim();
                String value = str.substring(k+1);

	            TagNode node = getTag(key);
				if (node==null) {
					node = newTagNode(key+":", key,"\n");
	                node.setValue(value);
                    if (key.equalsIgnoreCase("SetUp"))
                        node.setVisible(false);
				}
				else
                	setTagValue(key,value);
            }
        }
    }

	public void moreInfoToSAX(String info, JoContentHandler handler)
		throws SAXException
	{
	    if (info==null) return;
	    StringTokenizer tok = new StringTokenizer(info, ";");
	    while (tok.hasMoreTokens()) {
	        String str = tok.nextToken();
	        int k = str.indexOf("=");

	        if (k > 0) {
	            String key = str.substring(0,k).trim();
	            String value = str.substring(k+1);

		        TagNode.toSAX(key,value,handler);
	        }
	    }
	}

	/**
	 * get all non-standard PGN tags
	 * @return List<TagNode>
	 */
	public List getMoreTags()
	{
		Vector result = new Vector();
		for (TagNode nd = (TagNode)tags.first(TAG_NODE); nd != null; nd = (TagNode)nd.next(TAG_NODE))
		{
		    String key = nd.getKey();
		    if (!DEFAULT_TAGS.contains(key)) result.add(nd);
		}
		return result;
	}

    public String getMoreInfo()
    {
        StringBuffer result = null;
        for (TagNode nd = (TagNode)tags.first(TAG_NODE); nd != null; nd = (TagNode)nd.next(TAG_NODE))
        {
            String key = nd.getKey();
            if (DEFAULT_TAGS.contains(key)) continue;

			Object value = nd.getValue();
			if (value==null) continue;

            if (result==null) result = new StringBuffer();
            if (result.length() > 0) result.append(";");

            result.append(key);
            result.append("=");
            result.append(value.toString());
        }

        if (result==null)
            return null;
        else
            return result.toString();
    }

	public static final boolean exists(int GId) throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			JoPreparedStatement pstm = conn.getPreparedStatement("SELECT Id FROM Game WHERE Id = ?");
			pstm.setInt(1,GId);
			return pstm.exists();
		} finally {
			JoConnection.release(conn);
		}
	}

	public final void read(int GId) throws Exception
	{
		read(GId,true);
	}

	public final void reread(int GId) throws Exception
	{
		fireEvents = false;
		ignoreCaretUpdate = true;
		askedAdjudicated = false;

		String fen = (String)getTagValue(TAG_FEN);
		clear(fen);
		read(GId,true);
		clearDirty();

		//  fire events
		fireChangedUpdate(new DirtyEvent());
	}

	public final void readInfo(int GId) throws Exception
	{
		read(GId,false);
	}

	protected void read(int GId, boolean withData)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			read(conn,GId,withData);
		} finally {
			JoConnection.release(conn);
		}
	}

	public static void readTagValues(int GId, Map values)
		throws Exception
	{
		JoConnection conn = null;
		JoPreparedStatement pstm = null;

		try {
			conn = JoConnection.get();

			pstm = conn.getPreparedStatement(DISPLAY_SQL);
			pstm.setInt(1, GId);
			pstm.execute();

			if (!pstm.next())
				throw new IllegalStateException("Game.Id = "+GId+" not found");

			Game game = new Game();
			game.dbId = GId;
			game.tags = new LineNode(game);
			game.readInfo(pstm.getResultSet(),1);
			game.getTagValues(values);

		} finally {
			if (pstm!=null) pstm.closeResult();
			JoConnection.release(conn);
		}
	}

	/**
	 * read from database
	 */
	public void read(JoConnection conn, int GId, boolean withData)
		throws Exception
	{
		JoPreparedStatement pstm = null;

		try {
			pstm = conn.getPreparedStatement(DISPLAY_SQL);
			pstm.setInt(1, GId);
			pstm.execute();

			if (!pstm.next())
				throw new IllegalArgumentException("Game.Id = "+GId+" not found");

			read(pstm.getResultSet(),withData);

		} finally {
			if (pstm!=null)	pstm.closeResult();
		}
	}


	/**
	 * read from database
	 */
	public void read(ResultSet res, boolean withData)
		throws Exception
	{
		clearId();
		fireEvents = false;
		ignoreCaretUpdate = true;

		try {
			int i = readInfo(res,1);
			if (withData) readData(res,i);

		} finally {
			fireEvents = true;
			ignoreCaretUpdate = false;
			dirty = false;	//	explicitly set to avoid Events being fired!
		}
	}

	/**
	 * read from GameBuffer
	 */
	public void read(GameBuffer.Row buf, boolean withData)
		throws Exception
	{
		clearId();
		fireEvents = false;
		ignoreCaretUpdate = true;

		try {

			readInfo(buf);
			if (withData) readData(buf);

		} finally {
			fireEvents = true;
			ignoreCaretUpdate = false;
			dirty = true;	//	explicitly set to avoid Events being fired!
		}
	}



	protected int readInfo(ResultSet res, int i)
		throws SQLException
	{
		dbId = res.getInt(i++);
		collectionId = res.getInt(i++);
		gameIndex = res.getInt(i++);
		origAttributes = res.getInt(i++);
		origPlyCount = res.getInt(i++);

		setIfNotNull(TAG_WHITE, res.getInt(i++),res.getString(i++));
		setTagValue(TAG_WHITE_TITLE, res.getString(i++));

		int welo = res.getInt(i++);
		if (welo > 0) setTagValue(TAG_WHITE_ELO, new Integer(welo));

		setIfNotNull(TAG_BLACK, res.getInt(i++),res.getString(i++));
		setTagValue(TAG_BLACK_TITLE, res.getString(i++));

		int belo = res.getInt(i++);
		if (belo > 0) setTagValue(TAG_BLACK_ELO, new Integer(belo));

		setResult(res.getInt(i++));

		Date gameDate = res.getDate(i++);
		Date eventDate = res.getDate(i++);
		short flags = res.getShort(i++);

		setTagValue(TAG_DATE, (gameDate==null) ? null : new PgnDate(gameDate, (short)(flags&0xff)));
		setTagValue(TAG_EVENT_DATE, (eventDate==null) ? null : new PgnDate(eventDate, (short)((flags>>8)&0xff)));
		setIfNotNull(TAG_EVENT, res.getInt(i++),res.getString(i++));
		setIfNotNull(TAG_SITE, res.getInt(i++),res.getString(i++));
		setTagValue(TAG_ROUND, res.getString(i++));
		setTagValue(TAG_BOARD, res.getString(i++));
		setTagValue(TAG_ECO, res.getString(i++));
		setIfNotNull(TAG_OPENING, res.getInt(i++), res.getString(i++));
		setIfNotNull(TAG_ANNOTATOR, res.getInt(i++), res.getString(i++));

		setTagValue(TAG_FEN, res.getString(i++));

		setMoreInfo(res.getString(i++));

		return i;
	}


	public int infoToSAX(ResultSet res, int i, JoContentHandler handler)
		throws SQLException, SAXException
	{
		dbId = res.getInt(i++);
		collectionId = res.getInt(i++);
		gameIndex = res.getInt(i++);
		origAttributes = res.getInt(i++);
		origPlyCount = res.getInt(i++);

		int whiteId = res.getInt(i++); // ignored

		TagNode.toSAX(TAG_WHITE, res.getString(i++), handler);
		TagNode.toSAX(TAG_WHITE_TITLE, res.getString(i++), handler);

		int welo = res.getInt(i++);
		if (welo > 0) TagNode.toSAX(TAG_WHITE_ELO, new Integer(welo), handler);

		int blackId = res.getInt(i++); //  ignore

		TagNode.toSAX(TAG_BLACK, res.getString(i++), handler);
		TagNode.toSAX(TAG_BLACK_TITLE, res.getString(i++), handler);

		int belo = res.getInt(i++);
		if (belo > 0) TagNode.toSAX(TAG_BLACK_ELO, new Integer(belo), handler);

		int result = res.getInt(i++);
		TagNode.toSAX(TAG_RESULT, PgnUtil.resultString(result), handler);

		Date gameDate = res.getDate(i++);
		Date eventDate = res.getDate(i++);
		short flags = res.getShort(i++);

		if (gameDate != null) {
			PgnDate pgnDate = new PgnDate(gameDate, (short)(flags&0xff));
			TagNode.toSAX(TAG_DATE, pgnDate, handler);
		}
		if (eventDate != null) {
			PgnDate pgnDate = new PgnDate(eventDate, (short)((flags>>8)&0xff));
			TagNode.toSAX(TAG_EVENT_DATE, pgnDate, handler);
		}

		int eventId = res.getInt(i++); //  ignored
		TagNode.toSAX(TAG_EVENT, res.getString(i++), handler);

		int siteId = res.getInt(i++);  //  ignored
		TagNode.toSAX(TAG_SITE, res.getString(i++), handler);

		TagNode.toSAX(TAG_ROUND, res.getString(i++), handler);
		TagNode.toSAX(TAG_BOARD, res.getString(i++), handler);
		TagNode.toSAX(TAG_ECO, res.getString(i++), handler);

		int openingId = res.getInt(i++);  //  ignored
		TagNode.toSAX(TAG_OPENING, res.getString(i++), handler);

		int annoId = res.getInt(i++);  //  ignored
		TagNode.toSAX(TAG_ANNOTATOR, res.getString(i++), handler);

		String fen = res.getString(i++);
		TagNode.toSAX(TAG_FEN, fen, handler);
		//  store FEN
		setTagValue(TAG_FEN,fen);

		moreInfoToSAX(res.getString(i++),handler);

		return i;
	}

	protected void readInfo(GameBuffer.Row buf)
		throws SQLException
	{
		collectionId = buf.CId; //  not set, usually
		gameIndex = buf.Idx;
		origAttributes = buf.Attributes;
		origPlyCount = buf.PlyCount;

		setTagValue(TAG_WHITE, buf.sval[GameBuffer.IWHITE]);
		setTagValue(TAG_WHITE_TITLE, buf.WhiteTitle);

		int welo = buf.WhiteELO;
		if (welo > 0) setTagValue(TAG_WHITE_ELO, new Integer(welo));

		setTagValue(TAG_BLACK, buf.sval[GameBuffer.IBLACK]);
		setTagValue(TAG_BLACK_TITLE, buf.BlackTitle);

		int belo = buf.BlackELO;
		if (belo > 0) setTagValue(TAG_BLACK_ELO, new Integer(belo));

		setResult(buf.Result);

		PgnDate gameDate = buf.GameDate;
		PgnDate eventDate = buf.EventDate;

		setTagValue(TAG_DATE, gameDate);
		setTagValue(TAG_EVENT_DATE, eventDate);
		setTagValue(TAG_EVENT, buf.sval[GameBuffer.IEVENT]);
		setTagValue(TAG_SITE, buf.sval[GameBuffer.ISITE]);
		setTagValue(TAG_ROUND, buf.Round);
		setTagValue(TAG_BOARD, buf.Board);
		setTagValue(TAG_ECO, buf.ECO);
		setTagValue(TAG_OPENING, buf.sval[GameBuffer.IOPEN]);
		setTagValue(TAG_ANNOTATOR, buf.sval[GameBuffer.IANNOTATOR]);

		setTagValue(TAG_FEN, buf.FEN);

		if (buf.More!=null && buf.More.length() > 0)
			setMoreInfo(buf.More.toString());
	}

	protected void setIfNotNull(String tag, int strId, String str)
	{
		if (strId != 0)
			setTagValue(tag,str);
	}

	protected int readData(ResultSet res, int i)
		throws SQLException
	{
		byte[] bin      = res.getBytes(i++);
		byte[] comments = res.getBytes(i++);
		byte[] eval		= res.getBytes(i++);

		String fen = (String)getTagValue(TAG_FEN);

		readData(bin,comments,eval,fen);

		return i;
	}

	protected void readData(GameBuffer.Row buf)
	{
		readData(buf.Bin, buf.Comments, null, buf.FEN);
	}

	protected void readData(byte[] bin, byte[] comments, byte[] eval, String fen)
	{
		mainLine = new LineNode(this, bin,0, comments,0, eval, fen, true);
		setupDoc();

		position.reset();
		insertNode(0, root);

		position.reset();
		currentMove = null;
	}


	/**
	 * read from database
	 */
	public void parse(String fen, String text)
		throws Exception
	{
		parse(fen, text.toCharArray(), 0,text.length());
	}

	/**
	 * read from database
	 */
	public void parse(String fen, char[] text, int offset, int len)
		throws Exception
	{
		dbId = 0;

		position.setup(fen);

		byte[] bin = new byte[len];
        byte[] comments = new byte[len];

		parseText(text,offset,len, fen, bin, comments, true);

		mainLine = new LineNode(this, bin,0, comments, 0, null, fen, true);
		setupDoc();

		insertNode(0, root);

		position.reset();
		currentMove = null;
	}

	public boolean ecofy(ECOClassificator classificator, boolean setECO, boolean setName)
	{
		//  TODO
		if (!setECO && !setName) return false;    //  that was easy

		MoveNode oldCurrent = currentMove;
		int oldOptions = position.getOptions();
		int result = ECOClassificator.NOT_FOUND;

		//  follow the main line
		int key;
		try {
			position.reset();
			position.setOption(Position.INCREMENT_HASH+Position.INCREMENT_REVERSED_HASH, true);
			key = classificator.lookup(position);
			if (key!=ECOClassificator.NOT_FOUND) result = key;

			for (MoveNode mvn = mainLine.firstMove(); mvn!=null; mvn = mvn.nextMove())
			{
				mvn.play(position);
				key = classificator.lookup(position);
				if (key!=ECOClassificator.NOT_FOUND) result = key;
			}
		} finally {
			position.setOptions(oldOptions);
			gotoMove(oldCurrent);
		}

		if (result!=ECOClassificator.NOT_FOUND) {
			String code = classificator.getEcoCode(result,3);
			String name = classificator.getOpeningName(result);

			try {
				if (code!=null && setECO)
					setTagValue(TAG_ECO,code,this);
				if (name!=null && setName)
					setTagValue(TAG_OPENING,name,this);
			} catch (Exception e) {
				Application.error(e);
			}

			return true;
		}
		else
			return false;
	}

	public void parseText(byte[] in, String fen, byte[] binResult, byte[] commentResult, boolean reset)
	{
		parseText(StringUtil.byte2char(in,0,in.length), 0,in.length, fen, binResult, commentResult,reset);
	}

	public void parseText(char[] in, int offset, int len, String fen,
                         byte[] binResult, byte[] commentResult, boolean reset)
	{
		position.reset();

		Parser p = new Parser(position, position.getOptions(), true);
		if (fen!=null) p.setPosition(fen);

        int[] blen = new int[2];
		p.parse(in,offset,len, binResult,0, commentResult,0,reset);
        blen[0] = p.getBinLength();
        blen[1] = p.getCommentsLength();
		binResult[blen[0]++] = (byte)SHORT_END_OF_DATA;
	}

	public void printDocStructure(PrintWriter out, StyledDocument doc)
	{
		if (out==null) out = new PrintWriter(System.out,true);

		printLineStructure(out,0, root,doc);
	}

	protected void indent(PrintWriter out, int i)
	{
		while (i-- > 0) out.print(" ");
	}


	public void toSAX(JoContentHandler handler)  throws SAXException
	{
		handler.startElement("game");

			handler.element("id",getId());

			tags.toSAX("head",handler);

			MoveNode oldMove = currentMove;
			try {
				position.reset();   //  reset

				if (position.hasStartFEN())
					DiagramNode.toSAX(position,handler);

	//			position.setup(fen);
				handler.startElement("body");
				//  temporarily detach mainLine, so that nest depth is 0
//				mainLine.setParent(null);
				mainLine.toSAX(handler);
//				mainLine.setParent(root);
				handler.endElement("body");

			} catch (ReplayException rpex) {

				rpex.printStackTrace();
				throw rpex;

			} finally {
				//  important: get currentMove in synch with position
				gotoMove((MoveNode)null);
				gotoMove(oldMove);
			}

		handler.endElement("game");
	}

	/**
	 * create SAX event stream immediately from database
	 */
	public void toSAX(JoConnection conn, int GId, JoContentHandler handler)
		throws SQLException, SAXException
	{
		JoPreparedStatement pstm = null;

		try {
			pstm = conn.getPreparedStatement(DISPLAY_SQL);
			pstm.setInt(1, GId);
			pstm.execute();

			if (!pstm.next())
				throw new IllegalArgumentException("Game.Id = "+GId+" not found");

			toSAX(pstm.getResultSet(),handler);

		} finally {
			if (pstm!=null)	pstm.closeResult();
		}
	}

	public void toSAX(ResultSet res, JoContentHandler handler) throws SAXException, SQLException
	{
		clearId();
		fireEvents = false;
		ignoreCaretUpdate = true;

		try {
			dbId = res.getInt(1);

			handler.startElement("game");
			handler.element("id",dbId);

			handler.startElement("head");

				int i = infoToSAX(res,1,handler);

			handler.endElement("head");

			String fen = (String)getTagValue(TAG_FEN);

			if (fen!=null) {
				position.setup(fen);
				DiagramNode.toSAX(position,handler);
			}

			handler.startElement("body");

				byte[] bin      = res.getBytes(i++);
				byte[] comments = res.getBytes(i++);

				LineNode.toSAX(bin,0, comments,0, position, fen, handler);

			handler.endElement("body");

			handler.endElement("game");

		} finally {
			fireEvents = true;
			ignoreCaretUpdate = false;
			dirty = false;	//	explicitly set to avoid Events being fired!
		}
	}

	protected void printLineStructure(PrintWriter out, int ind, LineNode line, StyledDocument doc)
	{
		indent(out,ind);
		out.print("[");
		out.print(ReflectionUtil.nameOfConstant(INodeConstants.class,line.type()));
		out.print(" ");
		out.print(line.getStartOffset());
		out.print(", ");
		out.println(line.getLength());

		for (Node nd = line.first(); nd != null; nd = nd.next())
			if (nd.is(LINE_NODE))
				printLineStructure(out,ind+1, (LineNode)nd, doc);
			else
				printNodeStructure(out,ind+1,nd, doc);

		indent(out,ind);
		out.println("]");
	}

	protected void printNodeStructure(PrintWriter out, int ind, Node node, StyledDocument doc)
	{
		indent(out,ind);
		out.print("[");
		out.print(ReflectionUtil.nameOfConstant(INodeConstants.class,node.type()));
		out.print(" ");
		out.print(node.getStartOffset());
		out.print(", ");
		out.print(node.getLength());
		out.print(" \"");
		try {
			out.print(doc.getText(node.getStartOffset(),node.getLength()));
		} catch (BadLocationException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		out.println("\"]");
	}

}


