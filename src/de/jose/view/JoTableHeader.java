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

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class JoTableHeader
            extends JTableHeader
            implements MouseListener, MouseMotionListener
{
    protected TableColumn pressedColumn;
    protected boolean dragging;

    public JoTableHeader(TableColumnModel cmodel)
    {
        super(cmodel);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public boolean isPressedColumn(TableColumn aColumn) {
        return (aColumn==pressedColumn);
    }

    public void setResizingColumn(TableColumn aColumn) {
        if (aColumn != null) {
            pressedColumn = null;
            repaint();
        }
        super.setResizingColumn(aColumn);
    }

    public void mousePressed(MouseEvent evt) {
        if (resizingColumn==null) {
            int columnIndex = columnAtPoint(evt.getPoint());
            if (!ContextMenu.isTrigger(evt) && columnIndex >= 0 &&
                (getTable().getModel() instanceof JoTableModel) &&
                ((JoTableModel)getTable().getModel()).isSortable(columnIndex))
                pressedColumn = getColumnModel().getColumn(columnIndex);
            else
                pressedColumn = null;
            repaint();
        }
        dragging = false;   //  unless...
    }

    public void mouseReleased(MouseEvent evt) {
        if (pressedColumn != null) {
            if (!dragging && (getTable().getModel() instanceof JoTableModel))
            {
                int columnIndex = pressedColumn.getModelIndex();
                toggleSort((JoTableModel)getTable().getModel(), columnIndex);
            }
            pressedColumn = null;
            repaint();
        }
    }

    public void toggleSort(JoTableModel model, int index)
    {
        int order = model.getSortOrder();
        if (order == (index+1))
            order = -(index+1);
        else if (order == -(index+1))
            order = 0;
        else
            order = index+1;

        model.setSortOrder(order);
    }

    public void mouseDragged(MouseEvent evt) {
        dragging = true;
    }

    public void mouseClicked(MouseEvent evt)    {  }
    public void mouseMoved(MouseEvent evt)      {  }
    public void mouseEntered(MouseEvent evt)    {  }
    public void mouseExited(MouseEvent evt)     {  }
}
