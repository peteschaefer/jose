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

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import de.jose.MessageListener;
import de.jose.Util;
import de.jose.Application;
import de.jose.pgn.LineNode;
import de.jose.plugin.Score;
import de.jose.profile.FontEncoding;
import de.jose.util.FontUtil;
import de.jose.util.IntArray;
import de.jose.chess.Constants;
import de.jose.image.Surface;
import de.jose.image.ImgUtil;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;
import de.jose.pgn.EvalArray;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.AnalysisRecord;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Peter Sch�fer
 */

public class EvalView
        extends JComponent
        implements MessageListener
{
	/** array of evaulations    */
	protected EvalArray       values;
	/**	the plugin engine	*/
	protected EnginePlugin    engine;
	/** current game    */
	protected Game          game;
	protected LineNode		branch;


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
		setBackground(BACKGROUND_COLOR);
		setOpaque(true);
		setFocusable(false);    //  don't request keyboard focus (or should we ?)

		values = new EvalArray(0);
		game = null;

		clear();
	}

	public void clear()
	{
		values.clear();

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
		values.setGame(gm);

		Dimension minsize = getMinimumSize();
		int minwidth = (values.moveCount()-values.firstMove())*BAR_WIDTH;
		setMinimumSize(new Dimension(minwidth, minsize.height));
		setPreferredSize(new Dimension(minwidth, (int)getPreferredSize().getHeight()));

		repaint();
	}


	public void updateGame()
	{
		if (game!=null) setGame(game);
	}

	public void setValue(int ply, Score value)
	{
        if (ply >= 0)
		    values.setPlyValue(ply,value);

		adjustWidth();
		scrollVisible(ply/2);

		repaint1(ply/2);
	}



	protected void adjustWidth()
	{
		int minWidth = (values.moveCount()-values.firstMove())*BAR_WIDTH;
		Dimension minsize = getMinimumSize();
		if (minWidth > minsize.width)  {
			setMinimumSize(new Dimension(minWidth, minsize.height));
			setPreferredSize(new Dimension(minWidth, (int)getPreferredSize().getHeight()));
		}

		if (minWidth > getWidth())
			revalidate();   //  right ?
	}

	protected void repaint1(int move)
	{
		repaint();
		//  TODO think of something more efficient
	}

	protected void scrollVisible(int move)
	{
		int x = (move-values.firstMove()) * BAR_WIDTH;
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
		int first = values.firstMove();
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

	protected void paintValues(Graphics g)
	{
		//  paint bars !
		int first = values.firstMove();
		int last = first + values.moveCount();
		float[] wvalue = new float[2];
		float[] bvalue = new float[2];

		for (int p = first; p < last; p++)
		{
			float[] value1 = values.moveValue(p,Constants.WHITE,wvalue);
			float[] value2 = values.moveValue(p,Constants.BLACK,bvalue);

			if (EvalArray.isValid(value1) && EvalArray.isValid(value2)) {
				paint1Value(g, (int)(p-first)*BAR_WIDTH, BAR_WIDTH/2, value1);
				paint1Value(g, (int)((p-first+0.5f)*BAR_WIDTH), BAR_WIDTH/2, value2);
			}
			else if (EvalArray.isValid(value1)) {
				paint1Value(g, (int)(p-first)*BAR_WIDTH, BAR_WIDTH, value1);
			}
			else if (EvalArray.isValid(value2)) {
				paint1Value(g, (int)(p-first)*BAR_WIDTH, BAR_WIDTH, value2);
			}
			else {
				paint1Value(g, (int)(p-first)*BAR_WIDTH, BAR_WIDTH, null);
			}
		}
	}

	protected void paint1Value(Graphics g, int x, int width, float[] value)
	{
		int height = getHeight();

		if (!EvalArray.isValid(value)) {
			Graphics2D g2 = (Graphics2D) g;
			float[] fractions = new float[] { 0.0f, 0.35f, 0.65f, 1.0f };
			Color[] colors = new Color[] { Color.black, Color.darkGray, Color.lightGray, Color.white };

			LinearGradientPaint gp = new LinearGradientPaint(
					new Point(0,0),new Point(0,height),
					fractions, colors);
			g2.setPaint(gp);
			//g.setColor(Color.lightGray);
			g2.fillRect(x, 0, width, height);
			return;
		}

		int p1 = (int)(height*(1.0f - value[0]-value[1]));
		int p2 = (int)(height*(1.0f - value[0]));

		g.setColor(Color.black);
		g.fillRect(x, 0, width, p1);

		g.setColor(Color.gray);
		g.fillRect(x, p1, width, p2);

		g.setColor(Color.white);
		g.fillRect(x, p2, width, height);
	}


	protected void connectTo(EnginePlugin plugin)
	{
		if (engine!=plugin) {
			disconnect();
			engine = plugin;
			if (engine!=null)
				engine.addMessageListener(this);
		}
	}

	protected void disconnect()
	{
		if (engine!=null) engine.removeMessageListener(this);
		engine = null;
	}

	private static float[] svalue = new float[2];

	public void handleMessage(Object source, int what, Object data)
	{
		switch (what)
		{
		case EnginePlugin.THINKING:
//		case EnginePlugin.PONDERING:
		case EnginePlugin.ANALYZING:
			EnginePlugin plugin = (EnginePlugin)source;
			AnalysisRecord a = (AnalysisRecord)data;
			if (a!=null && a.ply>=0) {
				Score sc = a.eval[0];
				if (!sc.hasWDL()) plugin.mapUnit(sc); // todo alread done. remove?
				updateMoveNode(a.ply-1, sc);
			}
			break;

		case EnginePlugin.PLUGIN_MOVE:
			EnginePlugin.EvaluatedMove emv = (EnginePlugin.EvaluatedMove)data;
			updateMoveNode(emv.getPly(), emv.score);
			break;
		}
	}

	private void updateMoveNode(int ply, Score value)
	{
		setValue(ply, value);

		if (game==null) return;
		if (value==null || !value.hasWDL()) return;

		MoveNode mvnd = game.getCurrentMove();
		if (mvnd==null) return;

		LineNode new_branch = mvnd.parent();
		if (new_branch!=branch) {
			/**
			 * whenever the tree branch changes
			 * - trace back tree path from new_branch to top
			 * - update the eval data accordingly.
			 */
			values.setBranch(branch=new_branch);
		}

		if (! value.equals(mvnd.engineValue)) {
			mvnd.engineValue = new Score(value);
			if (this.isVisible())
				game.setDirty(true);
		}
	}
}

