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

package de.jose.view.style;

import de.jose.profile.FontEncoding;
import de.jose.util.AWTUtil;
import de.jose.util.ListUtil;
import de.jose.util.style.StyleUtil;
import de.jose.view.input.ValueHolder;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * a popup for displaying fonts (with samples)
 */
public class FontList
        extends JList
        implements ValueHolder
{
	protected CellRenderer rend;

    public FontList(FontSample[] entries, Dimension preferredCellSize)
    {
        super(entries);
//        putClientProperty("Quaqua.List.style",Version.getSystemProperty("Quaqua.List.style"));
		setCellRenderer(rend = new CellRenderer());
		rend.setPreferredSize(preferredCellSize);
	    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

	public FontList(Dimension preferredCellSize)
	{
		super();
		setCellRenderer(rend = new CellRenderer());
		rend.setPreferredSize(preferredCellSize);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

    public String getSelectedFont()
	{
		int idx = getSelectedIndex();
		if (idx < 0) return null;
	    FontSample ety = (FontSample)getModel().getElementAt(idx);
		if (ety != null)
			return ety.fontName;
		else
			return null;
	}

	public void setSelectedFont(String fontName)
	{
		ListModel lm = getModel();
		for (int i=0; i<lm.getSize(); i++) {
			FontSample ety = (FontSample)lm.getElementAt(i);
			if (ety.fontName.equals(fontName)) {
				setSelectedIndex(i);
				Rectangle r = getCellBounds(i,i);
				AWTUtil.scrollRectToVisible(this,r);
				return;
			}
		}
	}

	//  implements ValueHolder
	public Object getValue()            { return getSelectedFont(); }

	public void setValue(Object value)  { setSelectedFont((String)value); }

	
	public static FontSample[] getDiagramFontSamples(int size, boolean installed)
	{
		java.util.List fontNames = FontEncoding.getDiagramFonts(installed);
		ListUtil.sort(fontNames,null);
		FontSample[] entries = new FontSample[fontNames.size()];
		Iterator i = fontNames.iterator();
		for (int j=0; j < entries.length; j++) {
			String fontName = (String)i.next();
			entries[j] = new FontSample(fontName,fontName, null, size,FontSample.showDiagramSample);
		}
		return entries;
	}

	public static DefaultComboBoxModel createDiagramFontModel(int size, boolean installed)
	{
		return new DefaultComboBoxModel(getDiagramFontSamples(size, installed));
	}

    public static FontList createDiagramFontList(int size, boolean installed)
    {
        return new FontList(getDiagramFontSamples(size, installed),new Dimension(100,28));
    }


	public static FontSample[] getTextFontSamples(int size, boolean useFont)
	{
		java.util.List fontNames = FontEncoding.getTextFonts();
		ListUtil.sort(fontNames,null);
		FontSample[] entries = new FontSample[fontNames.size()];
		Iterator i = fontNames.iterator();
		for (int j=0; i.hasNext(); j++) {
			String fontName = (String)i.next();
			entries[j] = new FontSample(fontName,fontName, null, size, useFont ? FontSample.showFont:0);
		}
		return entries;
	}

	public static DefaultComboBoxModel createTextFontModel(int size, boolean useFont)
	{
		return new DefaultComboBoxModel(getTextFontSamples(size,useFont));
	}

    public static FontList createTextFontList(int size, boolean useFont)
    {
        return new FontList(getTextFontSamples(size,useFont),new Dimension(100,18));
    }


	public static FontSample[] getFigurineFontSamples(int size, boolean installed)
	{
		java.util.List fontNames = FontEncoding.getFigurineFonts(installed);
		ListUtil.sort(fontNames,null);
		FontSample[] entries = new FontSample[fontNames.size()];
		Iterator i = fontNames.iterator();
		for (int j=0; i.hasNext(); j++) {
			String fontName = (String)i.next();
			entries[j] = new FontSample(fontName,fontName, null, size, FontSample.showFigurineSample);
		}
		return entries;
	}

	public static DefaultComboBoxModel createFigurineFontModel(int size, boolean installed)
	{
		return new DefaultComboBoxModel(getFigurineFontSamples(size,installed));
	}

    public static FontList createFigurineFontList(int size, boolean installed)
    {
        return new FontList(getFigurineFontSamples(size,installed),new Dimension(100,28));
    }



	public static FontSample[] getInlineFontSamples(int size, boolean installed)
	{
		java.util.List fontNames = FontEncoding.getInlineFonts(installed);
		ListUtil.sort(fontNames,null);
		FontSample[] entries = new FontSample[fontNames.size()];
		Iterator i = fontNames.iterator();
		for (int j=0; i.hasNext(); j++) {
			String fontName = (String)i.next();
			entries[j] = new FontSample(fontName,fontName, null, size, FontSample.showDiagramSample);
		}
		return entries;
	}

	public static DefaultComboBoxModel createInlineFontModel(int size, boolean installed)
	{
		return new DefaultComboBoxModel(getInlineFontSamples(size,installed));
	}

    public static FontList createInlineDiagramFontList(int size,boolean installed)
    {
        return new FontList(getInlineFontSamples(size,installed),new Dimension(100,28));
    }


	public static FontSample[] getSymbolFontSamples(int size, boolean installed)
	{
		java.util.List fontNames = FontEncoding.getSymbolFonts(installed);
		ListUtil.sort(fontNames,null);
		FontSample[] entries = new FontSample[fontNames.size()];
		Iterator i = fontNames.iterator();
		for (int j=0; i.hasNext(); j++)  {
			String fontName = (String)i.next();
			entries[j] = new FontSample(fontName,fontName, null, size,0);
		}
		return entries;
	}

	public static DefaultComboBoxModel createSymbolFontModel(int size, boolean installed)
	{
		return new DefaultComboBoxModel(getSymbolFontSamples(size,installed));
	}

    public static FontList createSymbolFontList(int size, boolean installed)
    {
        return new FontList(getSymbolFontSamples(size,installed),new Dimension(100,18));
    }


    private static class CellRenderer
			extends JComponent
			implements ListCellRenderer
    {
        protected FontSample current;

        public Component getListCellRendererComponent(JList list,
                                              Object value,
                                              int index,
                                              boolean isSelected,
                                              boolean cellHasFocus)
        {
            current = (FontSample)value;

            if (isSelected) {
                 setBackground(list.getSelectionBackground());
                 setForeground(list.getSelectionForeground());
             }
             else {
                 setBackground(list.getBackground());
                 setForeground(list.getForeground());
             }

           return this;
        }

		public void paintComponent(Graphics g)
		{
			Color bg = getBackground();
			g.setColor(bg);
			g.fillRect(0,0,getWidth(),getHeight());

			if (current!=null) {
				current.fontColor = StyleUtil.contrast(bg, Color.black, Color.lightGray);
				current.paint(g, getBounds(), RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			}
		}

    }
}
