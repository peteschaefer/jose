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

package de.jose.view;

import de.jose.Util;
import de.jose.Application;
import de.jose.pgn.LineNode;
import de.jose.plugin.Score;
import de.jose.profile.FontEncoding;
import de.jose.util.FontUtil;
import de.jose.chess.Constants;
import de.jose.image.Surface;
import de.jose.image.ImgUtil;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Peter Sch�fer
 */

public class EvalView
        extends JComponent
{
	/** array of evaulations    */
	// @deprecated retrieve evaluations directly from Game tree (branch, below)
	//protected EvalArray       values;
	/** current game    */
	protected Game          game;
	protected LineNode		branch;
	//	last hilited move
	protected MoveNode		last1=null;
	//	for passinf info to immediate painting
	protected MoveNode		next0, next1;
	protected boolean		nexthi;
	/** y-value of middle line  */
	//protected int            middle;
	/** size of one pawn unit, in pixels    */
	//protected int            pawnUnit;
	/** positive max, in pawnUnits  */
	//protected double         maxPawn;
	/** negiatvie min, int pawnUnits */
	//protected double         minPawn;


	/** height of horizontal tick marks */
	protected static final int TICK_HEIGHT  = 6;
	/** width of one bar    */
	protected static final int BAR_WIDTH    = 12;

	/** visual style: evaluation is good for user   */
	protected static final Surface STYLE_GOOD       = Surface.newGradient(Color.yellow,Color.green);
	protected static final Surface STYLE_BAD        = Surface.newGradient(Color.yellow,Color.red);

	protected Paint goodPaint, badPaint;

	protected static final Color BACKGROUND_COLOR  = new Color(0xff,0xff,0xee);

	public EvalView()
	{
		setDoubleBuffered(true);
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setFocusable(false);    //  don't request keyboard focus (or should we ?)

		//values = new EvalArray(0);
		game = null;

		clear();
	}

	public void clear()
	{
		//values.clear();

		Dimension minsize = getMinimumSize();
		setMinimumSize(new Dimension(20, minsize.height));
		setPreferredSize(new Dimension(10*BAR_WIDTH, (int)getPreferredSize().getHeight()));

		repaint();
	}

	/**
	 * initialize from game main line
	 * @param gm
	 */
	public void setGame(Game gm)
	{
		game = gm;
		branch = gm.getMainLine();
		//values.setAdjustMax(EvalArray.ADJUST_LOW_HIGH);
		//values.setGame(gm);

		Dimension minsize = getMinimumSize();
		int minwidth = moveCount()*BAR_WIDTH;
		setMinimumSize(new Dimension(minwidth, minsize.height));
		setPreferredSize(new Dimension(minwidth, (int)getPreferredSize().getHeight()));

		repaint();
	}

	public int moveCount()
	{
		if (branch==null) return 0;
		MoveNode last = branch.lastMove();
		return (last==null) ? 0 : (last.getMoveNo()+1);
	}

	public void updateGame()
	{
		if (game!=null) setGame(game);
	}




	protected void adjustWidth()
	{
		int minWidth = moveCount()*BAR_WIDTH;
		Dimension minsize = getMinimumSize();
		if (minWidth > minsize.width)  {
			setMinimumSize(new Dimension(minWidth, minsize.height));
			setPreferredSize(new Dimension(minWidth, (int)getPreferredSize().getHeight()));
		}

		if (minWidth > getWidth())
			revalidate();   //  right ?
	}

	protected Rectangle moveRect(MoveNode mv)
	{
		int x = mv.getPly() * BAR_WIDTH/2;
		return new Rectangle(x,0,BAR_WIDTH/2,getHeight());
	}

	protected void repaint1(MoveNode mv)
	{
		//repaint(); is too slow and too expensive
		//  think of something more efficient:
		//	Graphics.paintImmediately does this trick
		//	but needs to be called from the main event thread:
		if (mv==next0) return; //	already scheduled for drawing
		next0=mv;
		if (javax.swing.SwingUtilities.isEventDispatchThread())
			paintImmediately();
		else SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				paintImmediately();
			}
		});
	}

	protected void paintImmediately()
	{
		if (next0!=null) {
			if (last1 != null && last1 != next0)
				paintImmediately(last1, false);
			paintImmediately(next0, true);
		}
		next0=null;
	}

	public void paintImmediately(MoveNode mv, boolean hilite)
	{
		next1 = mv;
		nexthi = hilite;
		paintImmediately(moveRect(mv));
		//	will call back to paint, below
	}

	public void paint(Graphics g)
	{
		if (next1!=null) {
			Rectangle rect = moveRect(next1);
			g.setClip(rect);
			g.setColor(Color.WHITE);
			paint1Move(g,next1,nexthi);
			next1=null;
		}
		else
			super.paint(g);
	}

	protected void paint1Move(Graphics g, MoveNode mv, boolean hilite)
	{
		Rectangle rect = moveRect(mv);
		paint1Value(g, mv, hilite);
		/*if (!hilite)*/ drawVerticalGrid(g, rect.x, rect.x+rect.width);
	}

	protected void scrollVisible(int move)
	{
		int x = move * BAR_WIDTH;
		scrollRectToVisible(new Rectangle(x,0,BAR_WIDTH,getHeight()));
	}

	protected void paintComponent(Graphics g)
	{
		//  clear background
		int width = getWidth();
		int height = getHeight();

		ImgUtil.setTextAntialiasing((Graphics2D)g, true);
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0,0,width,height);

		//paintBackground(g);
		paintValues(g);
		paintHorizontalTickMarks(g);
		paintVerticalAxis(g);
	}

	protected void paintHorizontalTickMarks(Graphics g)
	{
		int width = getWidth();

		Font textFont  = FontUtil.newFont("SansSerif",Font.PLAIN, (float)Util.inBounds(9,getHeight()/48,24));

		g.setFont(textFont);
		g.setColor(Color.darkGray);
		Rectangle2D textBounds = g.getFontMetrics().getStringBounds("5",g);

		//  paint horizontal tick marks
		int first = 0;
		int p = first-first%5+5;
		int height = getHeight();
		for (;; p += 5)
		{
			int x = (p-first-1)*BAR_WIDTH;
			if (x > width) break;

			g.drawLine(x,(height-TICK_HEIGHT)/2, x,(height+TICK_HEIGHT)/2);
			if (x > 0)
				g.drawString(String.valueOf(p),
				        x, height/2+(int)textBounds.getHeight());
		}
	}

	protected void paintVerticalAxis(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		String figFontName = Application.theUserProfile.getStyleAttribute("body.figurine",
		                            StyleConstants.FontConstants.Family);
		//  TODO use CSS.Attributes
		FontEncoding enc = FontEncoding.getEncoding(figFontName);
		Font figFont = FontUtil.newFont(figFontName,Font.PLAIN, (float)Util.inBounds(6,(int)height/16,24));

		Rectangle2D pawnBounds = g.getFontMetrics(figFont).getStringBounds("p",g);
		int tickInset = (int)(pawnBounds.getWidth()/4);

		//  paint vertical tick marks
		g.setFont(figFont);

		for (int i=-4; i <= +4; i++)
		{
			if (i==0)
				g.setColor(Color.darkGray);
			else
				g.setColor(Color.lightGray);

			int y = height/2 - i*height/8;
			//  pawns
			int figWidth = drawFigs(0,y,getFigText(i,enc), g,figFont);
			//  ticks
			g.drawLine(0,y, tickInset,y);
			g.drawLine((int)(figWidth-tickInset),y, width,y);
		}

	}

	protected void drawVerticalGrid(Graphics g, int x0, int x1)
	{
		int height = getHeight();
		for (int i=-4; i <= +4; i++)
		{
			if (i==0)
				g.setColor(Color.darkGray);
			else
				g.setColor(Color.lightGray);

			int y = height/2 - i*height/8;
			g.drawLine(x0,y, x1,y);
		}
	}

	protected int drawFigs(int x, int y, String text, Graphics g, Font font)
	{
		g.setFont(font);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(text,g);
		g.drawString(text,x, (int)(y+bounds.getHeight()/2));
		return (int) Math.round(bounds.getWidth());
	}

	protected String getFigText(int pawns, FontEncoding enc)
	{
		int abspawns = Math.abs(pawns);
		StringBuffer buf = new StringBuffer();

		while (abspawns > 0)
		{
			if (abspawns >= 9) {
				buf.append(enc.get((pawns<0) ? Constants.BLACK_QUEEN:Constants.WHITE_QUEEN));
				abspawns -= 9;
			}
			else if (abspawns >= 5) {
				buf.append(enc.get((pawns < 0) ? Constants.BLACK_ROOK:Constants.WHITE_ROOK));
				abspawns -= 5;
			}
			else if (abspawns >= 3) {
				buf.append(enc.get((pawns < 0) ? Constants.BLACK_KNIGHT:Constants.WHITE_KNIGHT));
				abspawns -= 3;
			}
			else {
				buf.append(enc.get((pawns < 0) ? Constants.BLACK_PAWN:Constants.WHITE_PAWN));
				abspawns -= 1;
			}
		}

		return buf.toString();
	}

	private static boolean hasWdl(MoveNode nd) {
		return nd!=null && nd.engineValue!=null && nd.engineValue.hasWDL();
	}

	protected void paintValues(Graphics g)
	{
		//  paint bars !
		MoveNode mv = branch.lastMove();
		MoveNode nxt = null;
		MoveNode current = game.getCurrentMove();
		last1=null;	// unless...

		while(mv!=null) {
			if (!hasWdl(mv) && hasWdl(nxt)) {
				//	engine values missing. supplement ?!
				mv.engineValue = new Score(nxt.engineValue);
			}
			if (hasWdl(mv) && (nxt!=null) && !hasWdl(nxt)) {
				nxt.engineValue = new Score(mv.engineValue);
				paint1Value(g, nxt,nxt==current);
			}

			paint1Value(g, mv,mv==current);

			nxt = mv;
			mv = mv.previousMove();	//	climbs the tree, if necessary
		}
	}

	protected void paintBackground(Graphics g)
	{
		paintBackground(g,0,moveCount()*BAR_WIDTH, NORMAL);
	}

	protected void paintBackground(Graphics g, int x0, int width, Color[] pal)
	{
		Graphics2D g2 = (Graphics2D) g;
		float[] fractions = new float[] { 0.0f, 0.35f, 0.5f,  0.65f, 1.0f };
		int height = getHeight();

		LinearGradientPaint gp = new LinearGradientPaint(
				new Point(0,0),new Point(0,height),
				fractions, pal);
		g2.setPaint(gp);
		//g.setColor(Color.lightGray);
		g2.fillRect(x0, 0, width, height);
	}

	private static final Color WHITE_HI = new Color(255, 210, 210);
	private static final Color BLACK_HI = new Color(45, 0, 0);
	private static final Color GREY_HI = new Color(183, 128, 128);

	private static final Color[] NORMAL = {
			Color.black, Color.darkGray, Color.gray, Color.lightGray, Color.white };

	private static final Color tinted (Color c1, Color c2, float f)
	{
		float r = c1.getRed()+c2.getRed()*f;
		float g = c1.getGreen()+c2.getGreen()*f;
		float b = c1.getBlue()+c2.getBlue()*f;
		float max = Math.max(r,Math.max(g,b));

		return new Color(r/max,g/max,b/max);
	}

	private static final Color[] HILITED = {
			tinted(Color.black,Color.red,0.3f),
			tinted(Color.darkGray,Color.red,0.3f),
			tinted(Color.gray,Color.red,0.3f),
			tinted(Color.lightGray,Color.red,0.3f),
			tinted(Color.white,Color.red,0.3f) };

	protected void paint1Value(Graphics g, MoveNode mvnd, boolean hilite)
	{
		Rectangle r = moveRect(mvnd);
		Score value = mvnd.engineValue;
		Color[] pal = /*hilite ? HILITED :*/ NORMAL;

		if (value==null || !value.hasWDL()) {
			paintBackground(g,r.x,r.width,pal);
		}
		else {
			int p1 = (int) (r.height * value.rel(value.lose));
			int p2 = (int) (r.height * value.rel(value.draw));

			g.setColor(pal[0]);	//	black
			g.fillRect(r.x, 0, r.width, p1);

			g.setColor(pal[2]);	//	grey
			g.fillRect(r.x, p1, r.width, p2);

			g.setColor(pal[4]);	//	white
			g.fillRect(r.x, p1+p2, r.width, r.height-p1-p2);
		}

		if (hilite) {
			g.setColor(Color.red);
			g.drawLine(r.x+r.width-4,0,r.x+r.width-4,r.height);
			//g.drawRect(x, 0, width, height);
		}
		if (hilite)
	 		last1 = mvnd;
	}

	protected void updateValue(MoveNode mvnd)
	{
		if (mvnd!=null) {
			LineNode new_branch = mvnd.parent();
			if (new_branch != branch) {
				branch = new_branch;
				repaint();
			} else if (isVisible()) {
				repaint1(mvnd);
			}
		}
	}
}

