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
import de.jose.image.TextureCache;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.FileNotFoundException;

/**
 * a preview panel for a JoSurfaceChooser
 *
 * @author Peter Schäfer
 */
public class SurfacePreviewPanel
		extends JPanel
		implements ChangeListener
{
	private JoSurfaceChooser chooser;
	
	public SurfacePreviewPanel(JoSurfaceChooser ch)
	{
		chooser = ch;
		chooser.getSelectionModel().addChangeListener(this);
		
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
	
		public Dimension getPreferredSize()		{ return new Dimension(64,64); }
		
		public void paintComponent(Graphics g)
		{
			int x = (getWidth()-64) / 2;
			int y = (getHeight()-64) / 2;

			switch (chooser.getSurfaceMode()) {
			case Surface.TEXTURE:
				try {
					String texture = chooser.getTexture();
					if (texture!=null)
						TextureCache.paintTexture(g, x,y, 64,64, texture, TextureCache.LEVEL_64);
				} catch (FileNotFoundException fnfex) {
					g.drawString(chooser.getTexture()+" not found", x,y);
				}
				break;
			case Surface.COLOR:
				g.setColor(chooser.getColor());
				g.fillRect(x,y,64,64);
				break;
			case Surface.GRADIENT:
				Graphics2D g2 = (Graphics2D)g;
				g2.setPaint(chooser.getGradientPaint(x,y, 64,64));
				g2.fillRect(x,y,64,64);
				break;
			}
		}
	}
}
