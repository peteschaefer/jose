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

import de.jose.util.ReflectionUtil;
import de.jose.view.input.StyledToolTip;
import org.jetbrains.annotations.NotNull;
import org.violetlib.aqua.AquaAppearance;
import org.violetlib.aqua.AquaComponentUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * a tabbed pane that filters right mouse clicks
 * right mouse clicks will be handled by the parent panel, NOT by the tabbed pane itself
 *
 * unfortunately, mouse handling is buried deep in the hierarchy, so we have to do some hacking
 *
 *
 * @author Peter Sch�fer 
 */
public class JoTabbedPane
		extends JTabbedPane
{
	public JoTabbedPane()
	{
		super();

//		setUI(getUI());
//		setBorder(new EmptyBorder(0,0,0,15));
		//  will be compensated by contentBorderInsets
		//  as an effect, the tab labels are inset to make room for the close button
		//  TODO this is suboptimal with JRE 1.5
		//  use Jide stuff instead ?!

		/*	note that we have to bypass platform specific Tab UIs here
			but it works (so far ;-)
		 */
/*        MouseListener origMouseListener = getUIMouseListener();
        System.out.println("origMouseListener = " + origMouseListener);
        if (origMouseListener != null)
            setUIMouseListener(new FilterMouseListener(origMouseListener));
*/	}

    public void setUI(TabbedPaneUI ui)
    {
        //  JoTabbedPane overrides a few methods and delegates the rest to the orignal TabbedPaneUI
        super.setUI(new JoTabbedPaneUI(ui));
    }

	public JToolTip createToolTip() {
		return new StyledToolTip();
	}

	/**
	 * find the tab that has been clicked
	 * @param pt a point
	 */
	public final int findIndex(Point pt)
	{
		return getUI().tabForCoordinate(this, pt.x,pt.y);
	}

}
