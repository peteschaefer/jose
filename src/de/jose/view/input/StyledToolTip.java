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

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;
import java.awt.*;

/**
 *
 * @author Peter Schäfer
 */

public class StyledToolTip
        extends JToolTip
{
	public StyledToolTip() {
		updateUI();
	}

	public void updateUI() {
		setUI(StyledToolTipUI.createUI(this));
	}


	static class StyledToolTipUI extends BasicToolTipUI
	{
		static StyledToolTipUI sharedInstance = null;
		static JoStyledLabel label = null;

		public static ComponentUI createUI(JComponent c)
		{
			if (sharedInstance==null) {
				sharedInstance = new StyledToolTipUI();
				label = new JoStyledLabel("");
			}
	        return sharedInstance;
		}

		public void paint(Graphics g, JComponent c)
		{
	        copyProps((JToolTip)c);
			g.clearRect(0,0, c.getWidth(),c.getHeight());
			label.paintComponent(g);
		}

		public Dimension getPreferredSize(JComponent c) {
			copyProps((JToolTip)c);
			return label.getPreferredSize();
		}

		private void copyProps(JToolTip c)
		{
			label.setText(c.getTipText());
			label.setSize(c.getSize());
			label.setBackground(c.getBackground());
			label.setFont(c.getFont());
		}
/*
		public Dimension getMinimumSize(JComponent c) {
		    return getPreferredSize(c);
		}

		public Dimension getMaximumSize(JComponent c) {
	        return getPreferredSize(c);
		}
*/
	}
}
