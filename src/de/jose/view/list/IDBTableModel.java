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
 
package de.jose.view.list;

import de.jose.view.JoTableModel;

import java.awt.*;

public interface IDBTableModel
        extends JoTableModel
{
    void open()  throws Exception;

    void close(boolean delete) throws Exception;

    void refresh(boolean scrollTop);

    Component getDisplayComponent();

    void setDisplayComponent(Component displayComponent);

    public int getDBId(int rowIndex);
	public void invalidateRowByPK(int pk);

    public void singleCell(String header, Object content, int type);

    public boolean isSingleCell();
    public boolean isSortable(int columnIndex);
    public boolean isWorking();
    
    public void fireTableStructureChanged();
    public void fireTableDataChanged();
}
