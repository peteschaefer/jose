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
import de.jose.plugin.EnginePlugin;
import de.jose.Application;
import de.jose.book.BookFile;
import de.jose.book.OpeningLibrary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.*;
import java.io.File;

/**
 *
 * @author Peter Schäfer
 */

public class EngineToolTip
        extends JToolTip
{
	private EnginePanel panel;

	public EngineToolTip(EnginePanel panel)
	{
		this.panel = panel;
		updateUI();
		setFont(new Font("sansserif",Font.PLAIN,16));
	}

	public void updateUI() {
		setUI(EngineToolTipUI.createUI(this));
	}


	static class EngineToolTipUI extends BasicToolTipUI
	{
		static EngineToolTipUI sharedInstance = null;
		static JLabel label;
		static ImageIcon icon;

		public static ComponentUI createUI(JComponent c)
		{
			if (sharedInstance==null) {
				sharedInstance = new EngineToolTipUI();
				label = new JLabel();
				label.setBorder(new EmptyBorder(8,8,8,8));
			}
	        return sharedInstance;
		}

		public void paint(Graphics g, JComponent c)
		{
			setProps((EngineToolTip)c);
			label.paint(g);
		}

		public Dimension getPreferredSize(JComponent c)
		{
			setProps((EngineToolTip)c);
			return label.getPreferredSize();
		}

		private void setProps(EngineToolTip tt)
		{
			StringBuffer text = new StringBuffer("<html>");

			if (tt.panel.inBook)
			{
				OpeningLibrary lib = Application.theApplication.theOpeningLibrary;
				boolean any = false;
				for (int i=0; i < lib.size(); i++)
				{
					BookFile bf = (BookFile)lib.get(i);
					if (bf.isOpen()) {
						if (any) text.append("<br>");
						text.append(bf.getInfoText());
						any = true;
					}
				}
			}
			else {
				EnginePlugin plugin = tt.panel.getPlugin();
				if (plugin == null) return;

				File logoFile = plugin.getLogo();
				if (logoFile != null) {
					icon = ImgUtil.getIcon(logoFile,100,50);
				}
				else {
					icon = null;
				}

				label.setIcon(icon);

				String name = plugin.getDisplayName();
				String author = plugin.getAuthor();

				if (name != null) {
					text.append("<b>");
					text.append(name);
					text.append("</b>");
				}
				if (name != null && author != null)
					text.append("<br>");
				if (author != null)
					text.append(author);

			}

			label.setText(text.toString());

			label.setSize(tt.getSize());
			label.setBackground(tt.getBackground());
			label.setFont(tt.getFont());
		}
	}
}
