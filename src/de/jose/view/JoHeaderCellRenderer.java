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

import de.jose.image.ImgUtil;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class JoHeaderCellRenderer
        extends DefaultTableCellRenderer
        implements TableCellRenderer
{
    protected static Icon downArrow = null;
    protected static Icon upArrow = null;
    protected static Border headerBorder = new BevelBorder(BevelBorder.RAISED);
    protected static Border pressedBorder = new BevelBorder(BevelBorder.LOWERED);

    public JoHeaderCellRenderer()
    {
        super();
        setHorizontalAlignment(JLabel.LEFT);
        setHorizontalTextPosition(JLabel.LEADING);
        setIconTextGap(8);
        setPreferredSize(new Dimension(100,18));

        if (downArrow==null)
            downArrow = JoToolBar.create1AwesomeIcon("\uf107:flat",12f);//ImgUtil.getIcon(null,"down8");
        if (upArrow==null)
            upArrow = JoToolBar.create1AwesomeIcon("\uf106:flat",12f);//ImgUtil.getIcon(null,"up8");
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {
        setFont(table.getFont());

        JTableHeader header = table.getTableHeader();
        if (header instanceof JoTableHeader &&
            ((JoTableHeader)header).isPressedColumn(table.getColumnModel().getColumn(column)))
            setBorder(pressedBorder);
        else
            setBorder(headerBorder);

        setValue(value);

        //  header row; get current sort order
        JoTableModel model = (JoTableModel)table.getModel();
        int midx = table.convertColumnIndexToModel(column);

        if (model.getSortOrder() == (midx+1))
            setIcon(downArrow);
        else if (model.getSortOrder() == -(midx+1))
            setIcon(upArrow);
        else
            setIcon(null);

        return this;
    }

}
