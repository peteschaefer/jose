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


package de.jose.help;

import de.jose.window.BrowserWindow;
import de.jose.util.ReflectionUtil;

import javax.swing.*;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.net.URL;
import java.io.IOException;

/**
 * CustomKit displays HTML help pages
 * intercepts hyperlink clicks and opens an external browser
 * 
 * @author Peter Sch�fer
 */

public class CustomKit
        extends com.sun.java.help.impl.CustomKit implements HyperlinkListener
//      extends javax.swing.text.html.HTMLEditorKit
{
	protected JEditorPane editor;

	public CustomKit() {
		super();
		StyleSheet styles = getStyleSheet();
		//  don't underline links
		Style style = styles.getStyle("a");
		StyleConstants.setUnderline(style,false);
//		StyleConstants.setForeground(style, Color.red);
	}

	public void install(JEditorPane jEditorPane)
	{
		editor = jEditorPane;
		editor.addHyperlinkListener(this);
		super.install(jEditorPane);
	}

	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {

			URL url = e.getURL();
			if ("file".equals(url.getProtocol()) && !"ext".equals(url.getRef())) {
				//  internal link; let JavaHelp handle it
			}
			else try {
//				System.err.println(url.toExternalForm());
				//  external link; show in web browser
				if ("ext".equals(url.getRef()))
					url = new URL(url.getProtocol(), url.getHost(), url.getPort(),  url.getFile());
				BrowserWindow.showWindow(url);

				//  prevent JavaHelp from handling it !!!
				ReflectionUtil.setValue(e,"type", HyperlinkEvent.EventType.EXITED);
			} catch (Exception ioex) {

			}
		}
	}

	/**
	 * clone() must be overwritten; otherwise JavaHelp will clone() a com.sun.java.CustomKit
	 * and all our customizing is in vain. Of course, this is nowhere document !! what a shitty  design !!
	 */
	public Object clone()
	{
		//  don't ask why....
		return new CustomKit();
	}
}