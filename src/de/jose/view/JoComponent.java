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

    char DOCK_NORTH	= 'N';
	char DOCK_EAST	= 'E';
	char DOCK_SOUTH	= 'S';
	char DOCK_WEST	= 'W';
	char DOCK_CENTER	= '0';

	String DOCK_HORIZONTAL	= "WE";
	String DOCK_VERTICAL		= "NS";
	String DOCK_ALL			= "NESW";
	String DOCK_NONE			= "";
	
	/**
	 * the name of the component
	 *	(split panes get the name of the left / top component)
	 */
	String getName();
	
	/**
	 * the parent frame
	 */
	int getWidth();
	int getHeight();
	
	Dimension getSize();
	
	JoFrame getParentFrame();
	
	/**
	 *	can this component be continuously resized ?
	 */
	boolean isContinuousLayout();

	/**
	 * callback from JoSplitPane: the split pane is about to be resized
	 */
	void startContinuousResize();
	/**
	 * callback from JoSplitPane: the split pane is about to be resized
	 */
	void finishContinuousResize();


	/**
	 * get the max. size if laid out in a JSplitPane
	 * @param orientation of the split pane
	 */
	Dimension getMaximumSize(int orientation);

	/**
	 * do we show a context menu? 
	 */
    boolean showContextMenu();

	/**
	 *	currently not in use 
	 * @return ?
	 */
    boolean showControls();

	/**
	 * insert items into context menu
	 */
    void adjustContextMenu(Collection<Object> commands, MouseEvent event);
	
	/**
	 * relative weight (horizontal
	 */
    float getWeightX();
	
	/**
	 * relative weigth (vertical)
	 */
    float getWeightY();
	
	/**
	 * @return a String indicating the available docking zones
	 */
    String getDockingSpots();

	/**
	* @return the location of a dockign handle
	*/
    Point getDockingSpot(char orientation);
	
}
