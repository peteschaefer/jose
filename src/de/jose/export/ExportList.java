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

package de.jose.export;

import de.jose.view.input.ValueHolder;
import de.jose.Config;
import de.jose.Application;
import de.jose.Version;
import de.jose.Util;
import de.jose.window.ExportDialog;
import de.jose.image.ImgUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.SoftCache;
import de.jose.util.FontUtil;
import de.jose.util.ListUtil;
import de.jose.util.AWTUtil;
import de.jose.util.map.IntHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Vector;
import java.util.Comparator;
import java.awt.*;
import java.io.File;

/**
 * @author Peter Schäfer
 */

public class ExportList
        extends JList
        implements ValueHolder
{
	/** Vector<ExportListElement> */
	protected Vector listElements = new Vector();

	protected static Dimension CELL_SIZE = new Dimension(100, 100);

	protected static Dimension MIN_SIZE = new Dimension(CELL_SIZE.width,CELL_SIZE.height);
	protected static Dimension PREF_SIZE = new Dimension(CELL_SIZE.width,CELL_SIZE.height);
	protected static Dimension MAX_SIZE = new Dimension(CELL_SIZE.width,Integer.MAX_VALUE);

	public ExportList(int orientation)
	{
		//  get XSL config
		super();
//        putClientProperty("Quaqua.List.style",Version.getSystemProperty("Quaqua.List.style"));

		ExportConfig expconf = Application.theApplication.getExportConfig();
		NodeList elems = expconf.getExportConfigs();

		boolean isCustom = false;
		listElements = new Vector(elems.getLength());
		for (int i=0; i < elems.getLength(); i++)
		{
			Element elm = (Element)elems.item(i);
			if  (ExportConfig.getFile(elm) != null && !isCustom) {
				//listElements.add(null); // add separator (how ?)
				isCustom = true;
			}

			listElements.add(new ExportListElement(elm));
		}

		ListUtil.sort(listElements,null);
		super.setListData(listElements);

		setLayoutOrientation(orientation);
		setFixedCellWidth(CELL_SIZE.width);
		setFixedCellHeight(CELL_SIZE.height);
		if (orientation==HORIZONTAL_WRAP) {
			setVisibleRowCount(1);  //  means: no wrap
		}

		setBackground(ExportDialog.BACKGROUND);//UIManager.getColor("Panel.background"));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer(new ExportCellRenderer());

		setMinimumSize(MIN_SIZE);
		setMaximumSize(MAX_SIZE);
	}

	public Element getExportConfig()
	{
		ExportListElement elm = (ExportListElement)getSelectedValue();
		return elm.exportConfig;
	}


	public Object getValue()
	{
		ExportListElement elm = (ExportListElement)getSelectedValue();
		return elm.getKey();
	}

	public void setValue(Object value)
	{
        if (value==null) return;

		for (int i=0; i<listElements.size(); i++) {
			ExportListElement elm = (ExportListElement)listElements.get(i);
			if (elm.getKey().equals(value)) {
				setSelectedIndex(i);
				ensureIndexIsVisible(i);
				break;
			}
		}
	}


	private class ExportListElement implements Comparable
	{
		org.w3c.dom.Element exportConfig;

		ExportListElement(Element xslElement)         { this.exportConfig = xslElement; }

		public String toString()                    { return ExportConfig.getDisplayTitle(exportConfig); }
		public String getKey()                      { return ExportConfig.getTitle(exportConfig); }

		public int compareTo(Object obj)
		{
			ExportListElement that = (ExportListElement)obj;
			int thisOutput = ExportConfig.getOutput(this.exportConfig);
			int thatOutput = ExportConfig.getOutput(that.exportConfig);

			if (thisOutput!=thatOutput)
				return thisOutput-thatOutput;
			else
				return this.toString().compareTo(that.toString());
		}
	}

	class ExportCellRenderer extends DefaultListCellRenderer implements ListCellRenderer<Object>
	{
		private Element cfg;

		public ExportCellRenderer()
		{
			super();

//			setHorizontalTextPosition(JLabel.RIGHT);
//			setVerticalTextPosition(JLabel.CENTER);
//			setHorizontalAlignment(JLabel.LEFT);

            setHorizontalTextPosition(JLabel.CENTER);
            setVerticalTextPosition(JLabel.BOTTOM);
            setHorizontalAlignment(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
            setBorder(new EmptyBorder(4,0,4,8));

			setIconTextGap(4);
			setOpaque(true);
			setFont(FontUtil.newFont("Arial",Font.PLAIN,12));

			setEnabled(true);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus)
		{
//			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

			ExportListElement rec = (ExportListElement)value;
			cfg = rec.exportConfig;

			if (isSelected)
				setBackground(ExportDialog.SELECTED);
			else
				setBackground(Color.white);//UIManager.getColor("Panel.background"));//ExportDialog.BACKGROUND);


			setIcon(ExportList.this.getIcon(ExportConfig.getOutput(cfg)));

			String title = ExportConfig.getDisplayTitle(cfg);
			setText("<html>"+title);    //  word wrapping is important
			setSize(CELL_SIZE);

			return this;
		}
	}

	protected Icon getIcon(int output)
	{
		String iconName = "file";

		switch (output) {
		case ExportConfig.OUTPUT_AWT:      iconName = "printer"; break;
		case ExportConfig.OUTPUT_HTML:     iconName = "html"; break;
		case ExportConfig.OUTPUT_XSL_FO:   iconName = "pdf_printer"; break; //  "pdf"
		case ExportConfig.OUTPUT_XML:      iconName = "xml"; break; //  provisional
		case ExportConfig.OUTPUT_TEX:      iconName = "tex"; break;  //  provisional
		case ExportConfig.OUTPUT_PGN:      iconName = "pgn"; break;
		case ExportConfig.OUTPUT_ARCH:     iconName = "jos"; break;
		case ExportConfig.OUTPUT_TEXT:     iconName = "txt"; break;
		}

		ImageIcon icon = ImgUtil.getIcon("types/large",iconName);
//        return ImgUtil.createScaledIcon(icon,0.5);
        return icon;
	}

}
