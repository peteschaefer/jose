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

package de.jose.view.colorchooser;

import de.jose.image.Surface;
import de.jose.Version;

import javax.swing.*;
import javax.swing.plaf.ColorChooserUI;
import javax.swing.plaf.basic.BasicColorChooserUI;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * an extended Color Chooser that can select
 * either a color or a texture or a gradient
 *
 * @author Peter Sch�fer
 */

public class JoSurfaceChooser
		extends JColorChooser
		implements ChangeListener
{
    /**  the tab pane */
	private JTabbedPane tabPane;
    /**  index of texture pane */
	private int textChooserIdx;
    /** index of gradient pane */
	private int gradChooserIdx;
	/** texture pane */
    private TextureChooserPanel textChooser;
    /** gradient pane */
	private GradientChooserPanel gradChooser;

	public JoSurfaceChooser()
	{
		super();

        if (Version.mac) {
            //  Quaqua L&F has a nice color chooser
            //  but it doesn't work at all with our texture panels.
            //  Revert to default ColorChooser UI
            setUI(new BasicColorChooserUI());
        }

		gradChooserIdx = getChooserPanels().length;
		gradChooser = new GradientChooserPanel(this);
		addChooserPanel(gradChooser);

		textChooserIdx = getChooserPanels().length;
		textChooser = new TextureChooserPanel(this);
		addChooserPanel(textChooser);

        for (int i=0; i<countComponents(); i++) {
            Component comp = getComponent(i);
            if (comp instanceof JTabbedPane) {
		        tabPane = (JTabbedPane)getComponent(i);
                tabPane.addChangeListener(this);
                break;
            }
        }
		setPreviewPanel(new SurfacePreviewPanel(this));
	}
	
	public JoSurfaceChooser(Surface surf)
	{
		this();
		setSurface(surf);
	}
	
	public byte getSurfaceMode()
	{
		if (tabPane!=null && tabPane.getSelectedIndex()==textChooserIdx)
			return Surface.TEXTURE;
		if (tabPane!=null && tabPane.getSelectedIndex()==gradChooserIdx)
			return Surface.GRADIENT;
		//	else
		return Surface.COLOR;
	}

	public void setSurfaceMode(byte mode)
	{
        if (tabPane==null) return;

		switch (mode) {
		case Surface.GRADIENT:		tabPane.setSelectedIndex(gradChooserIdx); break;
		case Surface.TEXTURE:		tabPane.setSelectedIndex(textChooserIdx); break;
		default:					tabPane.setSelectedIndex(0); break;
		}
	}
	
	public String getTexture()	
	{
		return textChooser.getTexture();
	}
	
	public void setTexture(String text)
	{
		textChooser.setTexture(text);
	}

	public GradientPaint getGradientPaint(float zerox, float zeroy, float scalex, float scaley)
	{
		return gradChooser.getGradientPaint(zerox,zeroy, scalex,scaley);
	}

	public final Surface getSurface()
	{ 
		Surface srf = new Surface();
		srf.color = getColor();
		srf.texture = textChooser.getTexture();
		gradChooser.getGradient(srf);
		srf.mode = getSurfaceMode();
		return srf;
	}
	
	public final void setSurface(Surface surf)		
	{ 
		if (surf==null) 
		{
            setSurfaceMode(Surface.COLOR);
			setColor(Color.white);

            textChooser.setTexture(null);
			gradChooser.setGradient(null);
		}
		else 
		{
            setSurfaceMode(surf.mode);
			if (surf.color!=null)
				setColor(surf.color);
			else
				setColor(Color.white);
            if (surf.gradientColor==null) {
                if (surf.isDark())
                    surf.gradientColor = surf.color.brighter();
                else
                    surf.gradientColor = surf.color.darker();
            }

			textChooser.setTexture(surf.texture);
			gradChooser.setGradient(surf);
		}
        stateChanged(null);
	}
	
	/**	callback from TextureChooserPanel indicating a change
	 */
	public final void textureChanged()
	{
		getPreviewPanel().repaint();
	}

	/**	callback from GradientChooserPanel indicating a change
	 */
	public final void gradientChanged()
	{
//		setColor(gradChooser.getFirstColor());
		getPreviewPanel().repaint();
	}

	public void stateChanged(ChangeEvent evt)
	{
		//	callback when tabs are switched

        switch (getSurfaceMode())
        {
        case Surface.GRADIENT:
            gradChooser.update(); break;
        }
		getPreviewPanel().repaint();
	}

    public void switchTab(int tabIndex)
    {
        tabPane.setSelectedIndex(tabIndex);
    }

	public void setTexturesEnabled(boolean b) {
		tabPane.setEnabledAt(textChooserIdx,b);
	}

	public void setGradientsEnabled(boolean b) {
		tabPane.setEnabledAt(gradChooserIdx,b);
	}
}
