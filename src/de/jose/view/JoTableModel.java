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

import javax.swing.table.TableModel;

/**
 * extends javax.swing.table.JTableModel
 * by an additional method to retrieve the preferred width of a column
 * 
 */

public interface JoTableModel
		extends TableModel
{
	/**	@return an identifier for a column	 */
	public Object getIdentifier(int columnIndex);
	/** @return the preferred width of a column	 */
	public int getPreferredColumnWidth(int columnIndex);
	/** @return the minimum width of a column	 */
	public int getMinColumnWidth(int columnIndex);
	/** @return the maximum width of a column	 */
	public int getMaxColumnWidth(int columnIndex);
    /** @return true if the given column can be sorted  */
    public boolean isSortable(int columnIndex);

    /** get the current sort column (< 0: sort descending)   */
    public int getSortOrder();

    /** set the current sort order  */
    public void setSortOrder(int columnIndex1);
}
