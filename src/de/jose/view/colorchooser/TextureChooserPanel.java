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

import de.jose.Language;
import de.jose.Version;
import de.jose.image.TextureCache;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * this panel selects a texture, rather than a color
 * 
 */
public class TextureChooserPanel
		extends AbstractColorChooserPanel
		implements ListSelectionListener
{
	private JoSurfaceChooser chooser;
	private JList list;
	
	public TextureChooserPanel(JoSurfaceChooser ch)
	{
		super();
		chooser = ch;
		
		String[] allTextures = TextureCache.getInstalledTextures();
		list = new JList(allTextures);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new TextureRenderer());
		list.setFixedCellWidth(48);
		list.setFixedCellHeight(48);
		list.setVisibleRowCount(4);
		list.addListSelectionListener(this);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		JScrollPane scrollPane = new JScrollPane(list,
											JScrollPane.VERTICAL_SCROLLBAR_NEVER,
											JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(384,208));
 		add(scrollPane);
	}
	
	public final String getTexture()				
	{ 
		return (String)list.getSelectedValue(); 
	}
	
	public final void setTexture(String text)		
	{
		list.setSelectedValue(text, true); 
	}

    //-------------------------------------------------------------------------------
	//	implements ListSelectionListener
	//-------------------------------------------------------------------------------

	public void valueChanged(ListSelectionEvent evt)
	{
		chooser.textureChanged();
	}


    //-------------------------------------------------------------------------------
	//	extends AbstractChooserPanel
	//-------------------------------------------------------------------------------

	public void buildChooser()
	{
		/*	what shall we do here ? */
	}
	
	/**	called when the model changes
	 */
	public void updateChooser()
	{
		/*	what shall we do here ? */
	}
	
	public String getDisplayName()
	{
		return Language.get("colorchooser.texture");
	}
	
	public Icon getSmallDisplayIcon()
	{
		return null;
	}
	
	public Icon getLargeDisplayIcon()
	{
		return null;
	}
	
    public int getDisplayedMnemonicIndex()
    {
        return Language.getMnemonicCharIndex("colorchooser.texture");
    }

    public int getMnemonic()
    {
        return (int)Language.getMnemonic("colorchooser.texture");
    }

/*
	public void paint(Graphics g)
	{
		super.paint(g);
	}
*/	
}
