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

import de.jose.AbstractApplication;
import de.jose.Language;
import de.jose.Command;
import de.jose.MessageListener;
import de.jose.chess.*;
import de.jose.plugin.Score;
import de.jose.profile.UserProfile;
import de.jose.view.input.StyledToolTip;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * abstract base class for BoardView2D and BoardView3D
 *
 */
abstract public class BoardView
		extends JComponent
		implements Constants, PopupMenuListener, ActionListener
{
	/** after the view has captured an image, it sends a message to the BoardPanel */
	public static final int MESSAGE_CAPTURE_IMAGE = 4001;

	/**	current state (will be synched with 'position')	 */
	protected int[] pieces;

	/**	first mouse click (mouse down)	 */
	protected Point mouseStartPoint = null;
	/**	square where mouse click appeared */
	protected int mouseStartSquare = 0;
	/**	current mouse move	 */
	protected Move mouseMove;

	/**	is the promotion popup showing ?	 */
	protected boolean promoPopupShowing;

	protected IBoardAdapter board;

	protected boolean flipped;
	protected boolean showCoords;
	protected boolean showEvalbar;
    protected boolean hiliteSquares;
	/** current hints
	 *  Vector<Hint>
	 * */
	protected Vector<Hint> hints;

	protected class Hint
	        extends Timer
	{
		int from;
		int to;
		Color color;
		Object implData;

		Hint(int delay, int from, int to, Color color)
		{
			super(delay,BoardView.this);
			this.from = from;
			this.to = to;
			this.color = color;
		}
	}


	public static final Color   ENGINE_HINT_COLOR   = new Color(255,255,0, 64); //  yellow (transparent)
	public static final Color   ANIM_HINT_COLOR     = new Color(0,255,0, 64); //  green (transparent)
	/** show hint arrows during animation ? */
	protected boolean showAnimationHints;

	//	score for eval bard
	protected Score score = null;

	public BoardView(IBoardAdapter theBoard)
	{
		pieces = new int[OUTER_BOARD_SIZE];
		board = theBoard;

		setMinimumSize(new Dimension(48,48));
		setOpaque(true);
		hints = new Vector();
	}

	public final Graphics2D getGraphics2D()		{ return (Graphics2D)getGraphics(); }

	/**
	 * @return the piece that is currently displayed at square
	 */
	public int pieceAt(int square)	{ return pieces[square];}

	/**
	 * @return the piece that is currently displayed at square
	 */
	public final int pieceAt(int file, int row) { return pieceAt(EngUtil.square(file,row)); }

	abstract public void activate(boolean active);

	/**	called when the user profile is modified
	 */
	abstract public void updateProfile(UserProfile prf) throws Exception;

	/**	called when the application quits
	 *	this method can be used to store temporary data in the user profile
	 */
	abstract public void storeProfile(UserProfile prf);

	/**	called when the view is resized
	 *	to recalc buffer sizes, etc.
	 */
	abstract public void recalcSize();

	/**
	 * capture an image asyncrhoneously
	 */
	abstract public void captureImage(MessageListener callbackTarget, boolean transparent);

	/**	paint one square	 */
	public final void updateOne(int file, int row)
	{
		updateOne(getGraphics2D(), file,row);
	}

	/**	paint one square	 */
	abstract public void updateOne(Graphics2D g, int file, int row);

	public void refresh(boolean stopAnimation)	{ super.repaint(); }

	public void flip(boolean on)
	{
		if (on==flipped) return;
		doFlip(flipped = on);
		refresh(true);
	}

	public void showCoords(boolean on)
	{
		if (on==showCoords) return;
		doShowCoords(showCoords = on);
		refresh(true);
	}

	public void showEvalbar(boolean on)
	{
		if (on==showEvalbar) return;
		doShowEvalbar(showEvalbar = on);
		refresh(true);
	}

	abstract public Point getScreenLocation(int square);

	abstract public void doFlip(boolean on);

	abstract public void doShowCoords(boolean on);

	abstract public void doShowEvalbar(boolean on);

	/**
	 * set a piece at a given square and draw it immediately
	 */
	public final void set(int piece, int square)
	{
		set(getBufferGraphics(), piece, square);
	}

	abstract public Graphics2D getBufferGraphics();

	/**
	 * set a piece at a given square and draw it immediately
	 */
	abstract public void set(Graphics2D g, int piece, int square);

	/**
	 *	move (slide) a piece from one square to another
	 *
	 * @param frameRate if > 0: desired frames per second;
	 *					if <= 0: draw synchronously
	 * */
	abstract public void move(int startSquare, int piece, int endSquare, float time, int frameRate);

	/**
	 *	move (slide) a piece from one square to another
	 *
	 * */
	abstract public void move(Move mv, float time);

	/**
	 * show move hint (colored arrow)
	 */
	public final void showHint(Move mv, long millis, Color color)
	{
		Hint hnt = getHint(mv);
		if (hnt==null)
		{
			hnt = new Hint((int)millis, mv.from,mv.to, color);
			hnt.start();

			hints.add(hnt);
			doShowHint(hnt);
		}
    }

	/**
	 * show text hint (as popup window)
	 */
	public final void showHint(String text, long millis, Color color)
	{
		Hint hnt = new Hint((int)millis,0,0,null);

		StyledToolTip tip = new StyledToolTip();
		tip.setTipText("<font size=+5><b>"+text+"</b></font>");
		tip.setBackground(color);

		Point p = new Point((getWidth()-tip.getWidth())/2, (getHeight()-tip.getHeight())/2);
		SwingUtilities.convertPointToScreen(p,this);
		PopupFactory popf = PopupFactory.getSharedInstance();
		Popup pop = popf.getPopup(this, tip, p.x,p.y);
		pop.show();
		hnt.implData = pop;
		hnt.start();
	}

	protected final void hideHint(Hint hnt)
	{
		if (hnt.from==0) {
			//  text hint
			((Popup)hnt.implData).hide();
			hints.remove(hnt);
		}
		else if (hints.remove(hnt)) //  hide Move hint implemented by subclass
			doHideHint(hnt);
	}

	protected void hideAllHints()
	{
		for (int i=hints.size()-1; i>=0; i--)
		{
			Hint hnt = (Hint)hints.get(i);
			if (hnt.from==0) {
				//  text hint
				((Popup)hnt.implData).hide();
				hints.remove(i);
			}
		}
		//  hide Move hints (implemented by subclass)
		if (hints.size() > 0) {
			hints.clear();
			doHideAllHints(hints.size());
		}
	}


	protected Hint getHint(Move mv)
	{
		if (mv==null)
			return null;
		else
			return getHint(mv.from,mv.to);
	}

	protected Hint getHint(int from, int to)
	{
		for (int i=0; i<hints.size(); i++)
		{
			Hint hnt = (Hint)hints.get(i);
			if (hnt.from==from && hnt.to==to)
				return hnt;
		}
		return null;
	}

	abstract protected void doShowHint(Hint hnt);

	abstract protected void doHideHint(Hint hnt);

	abstract protected void doHideAllHints(int count);

	public void setScore(Score sc, boolean white_next)
	{
		if (sc!=null && sc.cp==Score.UNKNOWN && !sc.hasWDL() )
			sc = null;	//	Score object contains no useful info. Ignore.
		if (sc==null) {
			this.score = null;
		}
		else {
			this.score = new Score(sc);
			if (!white_next)
				this.score.swapWDL();
			//	WDL scores are given from the perspective of the current moving color
			//	for the eval bar, we want WDL scores from whites perspective only.
		}
		//	todo find the score range, depending on Leela ScoreType (centipawn, win%, etc.)
	}

	/**
	 * @return a millisecond value, relative to the current animation speed
	 */
	public long calcMillis(float time)
	{
		long base = AbstractApplication.theAbstractApplication.getAnimation().getSpeed();
        if (time > 1.0f) time = 1.0f;
        return (long)(base*time);
	}

	/**
	 * called when the user select a piece from the promotion popup
	 */
	abstract public void promotionPopup(int piece);

	public synchronized void synch(boolean redraw)
	{
		/*	synch with actual position	*/
		Graphics2D g = getBufferGraphics();

		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				int newPiece = board.pieceAt(square);
				if (redraw || newPiece != pieceAt(square))
					set(g,newPiece,square);
			}
	}


	public boolean couldBePromotion(Move mv)
	{
		if (mv.isPromotion()) return false;
		Position pos = board.getPosition();
		int piece = board.pieceAt(mv.from);

		switch (piece) {
		case WHITE_PAWN:	return Pawn.couldBePromotion(pos,mv,WHITE);
		case BLACK_PAWN:	return Pawn.couldBePromotion(pos,mv,BLACK);
		default:			return false;
		}
	}

	abstract public ImageIcon getPopupIcon(int piece);

	protected void addPopupItem(JPopupMenu pop,
								ImageIcon icon, int maxWidth,
								String command)
	{
		JMenuItem item = new JMenuItem(icon);
		item.setActionCommand(command);
//		item.setHorizontalTextPosition(SwingConstants.LEFT);
//		item.setIconTextGap(maxWidth-icon.getIconWidth());
		item.setHorizontalAlignment(SwingConstants.CENTER);
		pop.add(item);
	}

	protected void showPromotionPopup(int color, Point where)
	{
		JPopupMenu pop = new JPopupMenu(Language.get("popup.promote"));

		ImageIcon[] icon = new ImageIcon[4];

		int width;
		icon[0] = getPopupIcon(color+QUEEN);
		width = icon[0].getIconWidth();

		icon[1] = getPopupIcon(color+ROOK);
		width = Math.max(width,icon[1].getIconWidth());

		icon[2] = getPopupIcon(color+BISHOP);
		width = Math.max(width,icon[2].getIconWidth());

		icon[3] = getPopupIcon(color+KNIGHT);
		width = Math.max(width,icon[3].getIconWidth());

		addPopupItem(pop, icon[0], width, "promote."+QUEEN);
		addPopupItem(pop, icon[1], width, "promote."+ROOK);
		addPopupItem(pop, icon[2], width, "promote."+BISHOP);
		addPopupItem(pop, icon[3], width, "promote."+KNIGHT);

		JoMenuBar.addMenuItemListener(pop, this);
		pop.addPopupMenuListener(this);
		pop.show(getParent(), where.x,where.y);

		promoPopupShowing = true;
	}

	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() instanceof Hint) {
			Hint hnt = (Hint)evt.getSource();
			hideHint(hnt);
			hnt.stop();
		}
		else if (evt.getActionCommand().startsWith("promote.")) {
			/*	selection in promotion popup */
			int k = evt.getActionCommand().lastIndexOf(".");
			int promoPiece = Integer.parseInt(evt.getActionCommand().substring(k+1));
			promotionPopup(EngUtil.uncolored(promoPiece));
			promoPopupShowing = false;
		}
	}

	//-------------------------------------------------------------------------------
	//	Interface PopupMenuListener
	//-------------------------------------------------------------------------------

	public void popupMenuCanceled(PopupMenuEvent e)
	{
		/*	promotion dropped	*/
		/*	this method is never called ?
			why ?
		 */
		promotionPopup(0);
		promoPopupShowing = false;
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e)
	{
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
	{
		promoPopupShowing = false;
	}

}
