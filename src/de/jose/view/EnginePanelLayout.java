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

import de.jose.view.input.JoBigLabel;

import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class EnginePanelLayout
        implements LayoutManager2
{
	protected static final int MIN_HEIGHT       = 20;
	protected static final int MIN_EVAL_WIDTH   = 80;
	protected static final int MIN_LINE_WIDTH   = 48;

	protected EnginePanel engPanel;

	public EnginePanelLayout(EnginePanel engpanel)
	{
		engPanel = engpanel;
	}

    protected int getPreferredLineHeight(JoBigLabel evalLabel, JoBigLabel pvLabel, int width)
    {
        assert(evalLabel!=null);
        assert(pvLabel!=null);
        return Math.max(
                getPreferredHeight(evalLabel,MIN_EVAL_WIDTH),
                getPreferredHeight(pvLabel,width-MIN_EVAL_WIDTH));
    }

	public void layoutContainer(Container parent)
	{
        if (engPanel.showHistory) {
            //  history layout
            Dimension pref = engPanel.tPVHistory.getPreferredSize();

            engPanel.tPVHistory.setBounds(0,0,
                    Math.max(pref.width,parent.getWidth()),
                    Math.max(pref.height,parent.getHeight()));
        }
        else {
            //  PV line layout
            int y = 0;
            int infoheight = 0;

            int max = engPanel.countPvLines();
            int width = Math.max(parent.getWidth(), MIN_EVAL_WIDTH+MIN_LINE_WIDTH);

            /** lay out primary variation lines */
            for (int i=0; i < (max-1); i++)
            {
                JoBigLabel evalLabel = engPanel.getEvalLabel(i,false, false);
                JoBigLabel pvLabel = engPanel.getPvLabel(i,false, false);

                if (evalLabel != null && pvLabel != null) {
                    int linewidth = width-MIN_EVAL_WIDTH;
                    int lineheight = getPreferredLineHeight(evalLabel,pvLabel,width);

                    evalLabel.setBounds(0,y, MIN_EVAL_WIDTH,lineheight);
                    pvLabel.setBounds(MIN_EVAL_WIDTH,y, linewidth,lineheight);
                    y += lineheight;
                }
            }

            /** measure info line   */
            if (engPanel.showInfoLabel())
            {
                JoBigLabel infoLabel = engPanel.getInfoLabel(false);
                if (infoLabel != null)
                infoheight = getPreferredHeight(infoLabel,width);
            }

            /** expend remaining space for last pv   */
            if (max > 0) {
                JoBigLabel evalLabel = engPanel.getEvalLabel(max-1,false,false);
                JoBigLabel pvLabel = engPanel.getPvLabel(max-1,false,false);

                if (evalLabel != null && pvLabel != null) {
                    int linewidth = width - MIN_EVAL_WIDTH;
                    int lineheight = Math.max(getPreferredLineHeight(evalLabel, pvLabel,width), parent.getHeight() - infoheight - y);

                    evalLabel.setBounds(0, y, MIN_EVAL_WIDTH, lineheight);
                    pvLabel.setBounds(MIN_EVAL_WIDTH, y, linewidth, lineheight);
                    y += lineheight;
                }
            }

            /** lay out info line   */
            if (engPanel.showInfoLabel())
            {
                JoBigLabel infoLabel = engPanel.getInfoLabel(false);
                if (infoLabel != null) {
                    infoLabel.setBounds(0, y, width, infoheight);
                    y += infoheight;
                }
            }
        }
	}

	protected int getPreferredHeight(JoBigLabel label, int width)
	{
        if (label != null)
		    return Math.max(label.setPreferredHeight(width), MIN_HEIGHT);
        else
            return MIN_HEIGHT;
	}

	public Dimension minimumLayoutSize(Container parent)
	{
		return calcSize(MIN_EVAL_WIDTH+MIN_LINE_WIDTH,0);
	}


	public Dimension preferredLayoutSize(Container parent)
	{
//		return calcSize(Math.max(parent.getWidth(), MIN_EVAL_WIDTH+MIN_LINE_WIDTH), parent.getHeight());
		//  parent = JPanel
		//  parent.getParent() = JViewPort
		//  prefer the viewport width
		int preferredWidth = parent.getParent().getWidth();
		return calcSize(Math.max(preferredWidth, MIN_EVAL_WIDTH+MIN_LINE_WIDTH), 0);
	}

	public Dimension maximumLayoutSize(Container target)
	{
		return calcSize(target.getWidth(),target.getHeight());
	}


	private Dimension calcSize(int containerWidth, int containerHeight)
	{
        if (engPanel.showHistory) {
            //  history layout
            Dimension pref = engPanel.tPVHistory.getPreferredSize();
            return new Dimension(
                    Math.max(pref.width,containerWidth),
                    Math.max(pref.height,containerHeight));
        }
        else {
            int height = 0;
            int max = engPanel.countPvLines();

            /** lay out info line   */
            if (engPanel.showInfoLabel())
            {
                JoBigLabel infoLabel = engPanel.getInfoLabel(false);
                if (infoLabel!=null)
                    height += getPreferredHeight(infoLabel,containerWidth);
            }

            /** lay out primary variation lines */
            for (int i=0; i < max; i++)
            {
                JoBigLabel evalLabel = engPanel.getEvalLabel(i,false, false);
                JoBigLabel pvLabel = engPanel.getPvLabel(i,false, false);
                if (evalLabel!=null && pvLabel!=null)
                    height += getPreferredLineHeight(evalLabel,pvLabel,containerWidth);
            }

            /** expend remaining space for the bottom PV */
            height = Math.max(height,containerHeight);

            return new Dimension(containerWidth,height);
        }
	}

	public void invalidateLayout(Container target)              	{	/** no-op   */	}

	public void removeLayoutComponent(Component comp)	            {	/** no-op   */	}

	public void addLayoutComponent(String name, Component comp) 	{   /** no-op   */	}

	public void addLayoutComponent(Component comp, Object constraints)	{	/** no-op   */	}

	public float getLayoutAlignmentX(Container target)	            {	return 0;	}

	public float getLayoutAlignmentY(Container target)          	{	return 0;	}

}
