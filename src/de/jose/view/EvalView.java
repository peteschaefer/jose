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

import de.jose.MessageListener;
import de.jose.Util;
import de.jose.Application;
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
import java.awt.geom.Rectangle2D;

/**
 * @author Peter Schäfer
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
	protected Game            game;


	/** y-value of middle line  */
	protected int            middle;
	/** size of one pawn unit, in pixels    */
	protected int            pawnUnit;
	/** positive max, in pawnUnits  */
	protected double         maxPawn;
	/** negiatvie min, int pawnUnits */
	protected double         minPawn;


	/** height of horizontal tick marks */
	protected static final int TICK_HEIGHT  = 6;
	/** width of one bar    */
	protected static final int BAR_WIDTH    = 12;

	/** visual style: evaluation is good for user   */
	protected static final Surface STYLE_GOOD       = Surface.newGradient(Color.yellow,Color.green);
	protected static final Surface STYLE_BAD        = Surface.newGradient(Color.yellow,Color.red);

	protected Paint goodPaint, badPaint;

	public EvalView()
	{
		setBackground(Color.white);
		setOpaque(true);
		setFocusable(false);    //  don't request keyboard focus (or should we ?)

		values = new EvalArray(0);
		game = null;

		clear();
	}

	public void clear()
	{
		values.clear();
		calcUnits();

		Dimension minsize = getMinimumSize();
		setMinimumSize(new Dimension(20, minsize.height));
		setPreferredSize(new Dimension(10*BAR_WIDTH, (int)(pawnUnit*(maxPawn-minPawn))));

		repaint();
	}

	public void setGame(Game gm)
	{
		game = gm;
		values.setAdjustMax(EvalArray.ADJUST_LOW_HIGH);
		values.setGame(gm);
		calcUnits();

		Dimension minsize = getMinimumSize();
		int minwidth = (values.moveCount()-values.firstMove())*BAR_WIDTH;
		setMinimumSize(new Dimension(minwidth, minsize.height));
		setPreferredSize(new Dimension(minwidth, (int)(pawnUnit*(maxPawn-minPawn))));

		revalidate();
	}


	public void updateGame()
	{
		if (game!=null) setGame(game);
	}

	public void setValue(int move, int value, boolean adjustLow)
	{
		if (adjustLow)
			values.setAdjustMax(EvalArray.ADJUST_LOW_HIGH);
		else
			values.setAdjustMax(EvalArray.ADJUST_HIGH);

        if (move >= 0)
		    values.setMoveValue(move,Constants.WHITE,value);

		adjustWidth();
		scrollVisible(move);

		if (values.isMaxDirty() && calcUnits())
			repaint();
		else
			repaint1(move);
	}

	protected boolean calcUnits()
	{
		int height = getHeight();

		int oldPawnUnit = pawnUnit;
		int oldMiddle = middle;
		double oldMaxPawn = maxPawn;
		double oldMinPawn = minPawn;

		if (game==null) {
			maxPawn = +1.0;
			minPawn = -1.0;
		}
		else {
			maxPawn = Math.max((double)values.getMaximum()/100.0, 1.0);
			minPawn = Math.min((double)values.getMinimum()/100.0, -1.0);
		}

		//  round up to next 1.5 pawn
		maxPawn = Math.floor(2*maxPawn+1.0)/2.0;
		minPawn = Math.floor(2*minPawn-1.0)/2.0;

		pawnUnit = (int)((double)height/(maxPawn-minPawn));
		if (pawnUnit < 1) pawnUnit = 1;
		middle = (int)(maxPawn*pawnUnit);

		maxPawn = (double)middle/pawnUnit;
		minPawn = (double)(middle-height)/pawnUnit;

		boolean dirty =
		        (oldPawnUnit!=pawnUnit) || (oldMiddle!=middle) ||
		        (oldMaxPawn!=maxPawn) || (oldMinPawn!=minPawn);

		if (dirty) {
			goodPaint = STYLE_GOOD.getPaint(0,middle, 0.0f, -(float)(3*pawnUnit));
			badPaint = STYLE_BAD.getPaint(0,middle, 0.0f, (float)(3*pawnUnit));
		}

		return dirty;
	}

	protected void adjustWidth()
	{
		int minWidth = (values.moveCount()-values.firstMove())*BAR_WIDTH;
		Dimension minsize = getMinimumSize();
		if (minWidth > minsize.width)  {
			setMinimumSize(new Dimension(minWidth, minsize.height));
			setPreferredSize(new Dimension(minWidth, (int)(pawnUnit*(maxPawn-minPawn))));
		}

		if (minWidth > getWidth())
			revalidate();   //  right ?
	}

	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		calcUnits();
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
		g.clearRect(0,0,width,height);

		//  paint horitzontal axis
		g.setColor(Color.black);
		g.drawLine(0,middle, width,middle);

		paintValues(g);

		paintHorizontalTickMarks(g);
		paintVerticalAxis(g);
	}

	protected void paintHorizontalTickMarks(Graphics g)
	{
		int width = getWidth();

		Font textFont  = FontUtil.newFont("SansSerif",Font.PLAIN, (float)Util.inBounds(9,pawnUnit/6,24));

		g.setFont(textFont);
		g.setColor(Color.black);
		Rectangle2D textBounds = g.getFontMetrics().getStringBounds("5",g);

		//  paint horizontal tick marks
		int first = values.firstMove();
		int p = first-first%5+5;

		for (;; p += 5)
		{
			int x = (p-first-1)*BAR_WIDTH;
			if (x > width) break;

			g.drawLine(x,middle-TICK_HEIGHT/2, x,middle+TICK_HEIGHT/2);
			if (x > 0)
				g.drawString(String.valueOf(p),
				        x, middle+(int)textBounds.getHeight());
		}
	}

	protected void paintVerticalAxis(Graphics g)
	{
		int width = getWidth();

		String figFontName = Application.theUserProfile.getStyleAttribute("body.figurine",
		                            StyleConstants.FontConstants.Family);
		//  TODO use CSS.Attributes
		FontEncoding enc = FontEncoding.getEncoding(figFontName);
		Font figFont = FontUtil.newFont(figFontName,Font.PLAIN, (float)Util.inBounds(6,(int)pawnUnit/2,24));

		Rectangle2D pawnBounds = g.getFontMetrics(figFont).getStringBounds("p",g);
		int tickInset = (int)(pawnBounds.getWidth()/4);

		//  paint vertical tick marks
		g.setFont(figFont);
		g.setColor(Color.lightGray);

		int step = 1;
		if (maxPawn >= 8) step = 2;
		if (maxPawn >= 16) step = 4;

		for (int i=step; i <= maxPawn; i += step)
		{
			int y = middle - (int)Math.round(i*pawnUnit);
			//  pawns
			int figWidth = drawFigs(0,y,getFigText(i,enc), g,figFont);
			//  ticks
			g.drawLine(0,y, tickInset,y);
			g.drawLine((int)(figWidth-tickInset),y, width,y);
		}

		step = -1;
		if (maxPawn <= -8) step = -2;
		if (maxPawn <= -16) step = -4;

		for (int i=step; i >= minPawn; i += step)
		{
			int y = middle - (int)Math.round(i*pawnUnit);
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
		int height = getHeight();

		//  paint bars !
		int first = values.firstMove();
		int last = values.moveCount();

		for (int p = first; p < last; p++)
		{
			int value = values.moveValue(p);
			if (value==AnalysisRecord.UNKNOWN) continue;

			double barheight;
			String text = null;

			if (value >= AnalysisRecord.WHITE_MATES) {
				text = "#"+((value-AnalysisRecord.WHITE_MATES+1)/2);
				barheight = middle;
			}
			else if (value <= AnalysisRecord.BLACK_MATES) {
				text = "#"+((AnalysisRecord.BLACK_MATES-value+1)/2);
				barheight = middle-height;
			}
			else
				barheight = (double)value/100.0*pawnUnit;

			paint1Value((Graphics2D)g, p-first,
			        (int)Math.floor(barheight+0.5), text);
		}
	}

	protected void paint1Value(Graphics2D g, int offset, int height, String text)
	{
		int x = offset*BAR_WIDTH;

		if (height >= 0)
		{
			g.setPaint(goodPaint);
			g.fillRect(x, middle-height, BAR_WIDTH, height+1);
		}
		else
		{
			g.setPaint(badPaint);
			g.fillRect(x, middle, BAR_WIDTH, -height+1);
		}
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

	public void handleMessage(Object source, int what, Object data)
	{
		switch (what)
		{
		case EnginePlugin.THINKING:
//		case EnginePlugin.PONDERING:
		case EnginePlugin.ANALYZING:
			AnalysisRecord a = (AnalysisRecord)data;
			if (a!=null) setValue(a.ply/2, a.eval[0],false);
			break;

		case EnginePlugin.PLUGIN_MOVE:
			EnginePlugin.EvaluatedMove emv = (EnginePlugin.EvaluatedMove)data;
			int value  = emv.getValue();
			int ply = emv.getPly();

			setValue(ply/2,value,true);

			if (game!=null) {
				//  is this the right place to do this ??
				MoveNode mvnd = game.getCurrentMove();
				if (mvnd!=null && game.isMainLine(mvnd))
					mvnd.setEngineValue(value);
			}
			break;
		}

	}
}

