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

package de.jose.view.input;

import de.jose.Util;
import de.jose.Version;
import de.jose.image.ImgUtil;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.Plugin;
import de.jose.util.SoftCache;
import de.jose.util.StringUtil;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.File;
import java.util.Vector;

/**
 *  a list of available Engine Plugins
 *
 * @author Peter Schäfer
 */

public class PluginList
        extends JList
        implements ValueHolder
{
	/** Icon width and heigth
	 *  note that the original icon size is usually 100x50
	 * */
	protected static Dimension ICON_SIZE = new Dimension(76,38);
	protected static Dimension CELL_SIZE = new Dimension(ICON_SIZE.width+32, ICON_SIZE.height+4);

	protected static Dimension MIN_SIZE = new Dimension(CELL_SIZE.width,CELL_SIZE.height);
	protected static Dimension PREF_SIZE = new Dimension(CELL_SIZE.width,CELL_SIZE.height);
	protected static Dimension MAX_SIZE = new Dimension(CELL_SIZE.width,Integer.MAX_VALUE);

	protected static Color TRANSPARENT = new Color(0xc0,0xc0,0xc0, 0xa0);

	protected boolean showLogo = false;

	public PluginList()
	{
		super();
		setModel(new PluginListModel());
		setLayoutOrientation(JList.VERTICAL);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer(new PluginCellRenderer());
		showLogo(true);
//        putClientProperty("Quaqua.List.style",Version.getSystemProperty("Quaqua.List.style"));

		setMinimumSize(MIN_SIZE);
		setMaximumSize(MAX_SIZE);
	}

	public Dimension getPreferredSize()
	{
        if (showLogo) {
		    PREF_SIZE.height = CELL_SIZE.height * getModel().getSize();
            return PREF_SIZE;
        }
        else
		    return super.getPreferredSize();
	}

	public void showLogo(boolean on)
	{
        if (showLogo==on) return;
		showLogo = on;
		if (showLogo)  {
			setFixedCellWidth(CELL_SIZE.width);
			setFixedCellHeight(CELL_SIZE.height);
		}
		else {
			setFixedCellWidth(-1);
			setFixedCellHeight(-1);
		}
	}

	public void toggleLogo()
	{
		showLogo(!showLogo);
	}

	public int countElements()
	{
		return getModel().getSize();
	}

	/**
	 * @return String
	 */
	public Object getValue()
	{
		PluginListModel.Record rec = (PluginListModel.Record)getSelectedValue();
		if (rec==null)
			return null;
		else
			return EnginePlugin.getId(rec.cfg);
	}

	public Element getSelectedConfig()
	{
		PluginListModel.Record rec = (PluginListModel.Record)getSelectedValue();
		if (rec==null)
			return null;
		else
			return rec.cfg;
	}

	/**
	 * String value
	 * @param value
	 */
	public void setValue(Object value)
	{
		setPlugin(StringUtil.valueOf(value));
	}

	public void setPlugins(Vector plugins)
	{
		((PluginListModel)getModel()).setPlugins(plugins);
	}

	public void addNewConfig (Element cfg)
	{
		/** assign unique ID    */
		PluginListModel model = (PluginListModel)getModel();
		String newID = model.makeNewID(cfg);
		Plugin.setId(cfg,newID);

		PluginListModel.Record rec = model.addNewConfig(cfg);
		setSelectedValue(rec,true);
	}

	public void deleteSelected()
	{
		PluginListModel model = (PluginListModel)getModel();
		PluginListModel.Record rec = (PluginListModel.Record)getSelectedValue();
		int i = model.delete(rec);
		if (i >= getModel().getSize())
			setSelectedIndex(i-1);
		else
			setSelectedIndex(i);
	}

	public boolean setPlugin(String value)
	{
		if (value != null)
			for (int i=getModel().getSize()-1; i>=0; i--)
			{
				PluginListModel.Record rec = (PluginListModel.Record)getModel().getElementAt(i);
				if (EnginePlugin.getId(rec.cfg).equalsIgnoreCase(value)) {
					setSelectedIndex(i);
					return true;
				}
			}
		setSelectedValue(null,false);
		return false;
	}

	public static ImageIcon getImage(File file, boolean selected) throws Exception
	{
		ImageIcon img;
		if (!selected) {
			img = (ImageIcon)SoftCache.gInstance.get(file+".grey");
			if (img==null) {
				img = getImage(file,true);
				if (img!=null) {
					img = ImgUtil.createDisabledIcon(img);
					SoftCache.gInstance.put(file+".grey",img);
				}
			}
		}
		else {
			img = (ImageIcon)SoftCache.gInstance.get(file);
			if (img==null) {
				img = ImgUtil.getIcon(file);
				SoftCache.gInstance.put(file,img);
			}
		}
		return img;
	}

	class PluginCellRenderer extends JLabel implements ListCellRenderer
	{
		private Element cfg;
		private boolean selected;
		private BevelBorder border = new BevelBorder(BevelBorder.RAISED);


		public Component getListCellRendererComponent(JList list, Object value, int index,
		                                              boolean isSelected, boolean cellHasFocus)
		{
			PluginListModel.Record rec = (PluginListModel.Record)value;
			cfg = rec.cfg;
			selected = isSelected;
			setText(EnginePlugin.getDisplayName(rec.cfg,Version.osDir));
			return this;
		}

		public Dimension getPreferredSize()
		{
			if (showLogo)
				return CELL_SIZE;
			else
				return super.getPreferredSize();    //To change body of overriden methods use Options | File Templates.
		}

		public Dimension getMaxSize()
		{
			return MAX_SIZE;
		}

		protected void paintComponent(Graphics g)
		{
			if (selected)
				g.setColor(getSelectionBackground());
			else
				g.setColor(Color.white);
			g.fillRect(0,0,getWidth(),getHeight());


			if (showLogo)
				paintLogo(g,selected);
			else
				super.paintComponent(g);
			//  strike-through
/*
			boolean deleted = Util.toboolean(cfg.getAttribute("deleted"));
			if (deleted) {
				g.setColor(Color.red);
				g.drawLine(0,0, getWidth(),getHeight());
				g.drawLine(0,getHeight(), getWidth(),0);
			}
*/
		}

		protected void paintLogo(Graphics g, boolean selected)
		{
			File logo = EnginePlugin.getLogo(cfg);
			Rectangle inner = new Rectangle(
			            (getWidth()-ICON_SIZE.width)/2, (getHeight()-ICON_SIZE.height)/2,
			            ICON_SIZE.width, ICON_SIZE.height);

			if (logo!=null && logo.exists())
				try {
					ImageIcon img = getImage(logo,true);
					g.drawImage(img.getImage(), inner.x,inner.y, inner.width,inner.height, null);
				} catch (Exception ex) {
					//  image unreadable; can't help it...
					ex.printStackTrace();
				}
			else {
				//  else: display text
				String name = EnginePlugin.getName(cfg);
				String version = EnginePlugin.getVersion(cfg,Version.osDir);

				g.setColor(Color.lightGray);
				g.fillRect(inner.x,inner.y, inner.width,inner.height);

				g.setColor(Color.black);
				border.paintBorder(this,g, inner.x,inner.y, inner.width,inner.height);

				Font font = g.getFont();
				font = font.deriveFont(10.0f);   //  8 pt
				g.setFont(font);

                Shape oldClip = g.getClip();
				g.clipRect(inner.x+4,inner.y+4, inner.width-8,inner.height-8);
				if (name!=null) {
					int w = g.getFontMetrics().stringWidth(name);
					g.drawString(name, (getWidth()-w)/2, getHeight()/2-4);
				}
				if (version!=null) {
					int w = g.getFontMetrics().stringWidth(version);
					g.drawString(version, (getWidth()-w)/2, getHeight()/2+8);
				}
                g.setClip(oldClip);
			}

			if (!selected) {
				g.setColor(TRANSPARENT);
				g.fillRect(inner.x,inner.y, inner.width,inner.height);
			}
		}
	}
}
