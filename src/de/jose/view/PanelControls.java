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

import de.jose.Language;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Peter Schäfer
 */

public class PanelControls
        extends JComponent
{
    protected static final int      FRAME_HEIGHT    = 16;

    protected static Icon   frameIcon = null;
    protected static Icon   closeIcon = null;
    protected static Icon   dockIcon = null;
    protected static Icon   undockIcon = null;
    protected static Icon   moveIcon = null;

    protected static Font   textFont = null;
    protected static Color  fgColor = null;
    protected static Color  bgColor = null;

    /** the associated panel    */
    protected JoPanel   panel;

    public PanelControls()
    {
		setVisible(false);
        setDefaults();
    }

	public JoPanel getPanel()
	{
		return panel;
	}

	public void setPanel(JoPanel panel)
	{
		if (panel!=this.panel) {
			this.panel = panel;
			Point topRight = new Point(panel.getWidth(),0);
			topRight = ViewUtil.localPoint(topRight,panel,this.getParent());
			Rectangle bounds = new Rectangle(topRight.x-48,topRight.y, 48,16);
			setBounds(bounds);
		}
	}

    public static Icon getFrameIcon() {
        if (frameIcon==null) setDefaults();
        return frameIcon;
    }

    public static Icon getCloseIcon() {
        if (closeIcon==null) setDefaults();
        return closeIcon;
    }

    public static Icon getDockIcon() {
        if (dockIcon==null) setDefaults();
        return dockIcon;
    }

    public static Icon getMoveIcon() {
        if (moveIcon==null) setDefaults();
        return moveIcon;
    }

    public static Icon getUndockIcon() {
        if (undockIcon==null) setDefaults();
        return undockIcon;
    }

    public String getTitle()
    {
        String name = panel.getName();
        if (name == null)
            return null;
        else
            return Language.get(name);
    }

	protected void paintComponent(Graphics g)
	{
		int x = panel.isDocked() ? 0:16;

		g.setColor(new Color(1.0f,1.0f,1.0f, 0.5f));
		g.fillRect(x,0, 48-x,16);

		moveIcon.paintIcon(panel,g, x,0);

        if (panel.isDocked()) {
			undockIcon.paintIcon(panel,g, x+16,0);
            closeIcon.paintIcon(panel,g, x+32,0);
        }
        else {
            dockIcon.paintIcon(panel,g, x+16,0);
        }
    }

    protected static void setDefaults()
    {
        if (frameIcon == null)
            frameIcon = UIManager.getIcon("InternalFrame.icon");
        if (closeIcon == null)
            closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
        if (dockIcon == null)
            dockIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
        if (undockIcon == null)
            undockIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
        if (moveIcon == null)
            moveIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
        if (fgColor == null)
            fgColor = UIManager.getColor("InternalFrame.activeTitleForeground");
        if (bgColor == null)
            bgColor = UIManager.getColor("OptionPane.questionDialog.titlePane.background");
//            bgColor = UIManager.getColor("InternalFrame.activeTitleBackground");
        if (textFont == null)
            textFont = UIManager.getFont("InternalFrame.titleFont");
    }

}
