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

package de.jose.view.colorchooser;

import de.jose.Language;
import de.jose.image.Surface;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Map;

/**
 * a preview panel for a JoSurfaceChooser
 *
 * @author Peter Schäfer
 */
public class ChessPreviewPanel
		extends JPanel
		implements ChangeListener
{
	private JoSurfaceChooser chooser;

    private Map surfaces;
    private String currentKey;

	public ChessPreviewPanel(JoSurfaceChooser ch, Map surfs, String current)
	{
		chooser = ch;
		chooser.getSelectionModel().addChangeListener(this);
		surfaces = surfs;
        currentKey = current;

		setBorder(new TitledBorder(Language.get("colorchooser.preview")));
		setLayout(new BorderLayout());
		add(new PreviewLabel(), BorderLayout.CENTER);
	}

	//	callback if selection changes
	public void stateChanged(ChangeEvent evt)
	{
		repaint();
	}

	class PreviewLabel extends JComponent
	{

		public Dimension getPreferredSize()		{ return new Dimension(172,64); }

		public void paintComponent(Graphics g)
		{
            Graphics2D g2 = (Graphics2D)g;
            /*  paint background    */
            fillRect(g2,"board.surface.background", 0,0,getWidth(),getHeight());

            /*  paint board frame   */
            Border b = new SoftBevelBorder(BevelBorder.RAISED);
            b.paintBorder(this,g, 10,-10, getWidth()+10, getHeight());

            /*  paint squares   */
            for (int x=12; x <= getWidth(); x += 48)
                for (int y = getHeight()-12; y >= 0; y -= 48)
                {
                    int i = (x-12)/48;
                    int j = (y-getHeight()+12)/48;
                    if ((i+j)%2 == 0)
                        fillRect(g2,"board.surface.dark", x,y-48,48,48);
                    else
                        fillRect(g2,"board.surface.light", x,y-48,48,48);
                }
        }

	}

    protected void fillRect(Graphics2D g2, String key, int x, int y, int width, int height)
    {
        Surface srf = getSurface(key);
        Paint pnt = srf.getPaint(getWidth(),getHeight());
        g2.setPaint(pnt);
        g2.fillRect(0,0,getWidth(),getHeight());
    }

    protected Surface getSurface(String key)
    {
        if (key.equals(currentKey))
            return chooser.getSurface();
        else
            return (Surface)surfaces.get(key);
    }
}
