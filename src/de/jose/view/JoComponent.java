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

import de.jose.window.JoFrame;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

public interface JoComponent
{
	/**	constants for docking handles	 */
	
	public char DOCK_NORTH	= 'N';
	public char DOCK_EAST	= 'E';
	public char DOCK_SOUTH	= 'S';
	public char DOCK_WEST	= 'W';
	public char DOCK_CENTER	= '0';

	public String DOCK_HORIZONTAL	= "WE";
	public String DOCK_VERTICAL		= "NS";
	public String DOCK_ALL			= "NESW";
	public String DOCK_NONE			= "";
	
	/**
	 * the name of the component
	 *	(split panes get the name of the left / top component)
	 */
	public String getName();
	
	/**
	 * the parent frame
	 */
	public int getWidth();
	public int getHeight();
	
	public Dimension getSize();
	
	public JoFrame getParentFrame();
	
	/**
	 *	can this component be continuously resized ?
	 */
	public boolean isContinuousLayout();

	/**
	 * callback from JoSplitPane: the split pane is about to be resized
	 */
	public void startContinuousResize();
	/**
	 * callback from JoSplitPane: the split pane is about to be resized
	 */
	public void finishContinuousResize();


	/**
	 * get the max. size if laid out in a JSplitPane
	 * @param orientation of the split pane
	 */
	public Dimension getMaximumSize(int orientation);

	/**
	 * do we show a context menu? 
	 */	
	public boolean showContextMenu();

	/**
	 *	currently not in use 
	 * @return
	 */
    public boolean showControls();

	/**
	 * insert items into context menu
	 */
	public void adjustContextMenu(Collection commands, MouseEvent event);
	
	/**
	 * relative weight (horizontal
	 */
	public float getWeightX();
	
	/**
	 * relative weigth (vertical)
	 */
	public float getWeightY();
	
	/**
	 * @return a String indicating the available docking zones
	 */
	public String getDockingSpots();

	/**
	* @return the location of a dockign handle
	*/	
	public Point getDockingSpot(char orientation);
	
}
