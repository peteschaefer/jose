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

import de.jose.image.ImgUtil;
import de.jose.window.BrowserWindow;
import de.jose.Application;
import de.jose.Util;
import de.jose.util.StringUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTML;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;

/**
 * a read-only text label that can display styled text
 *
 * @author Peter Schäfer
 */

public class JoStyledLabel
		extends JTextPane implements HyperlinkListener, FocusListener
{
	protected static HTMLEditorKit htmlKit = null;
	protected static StyleSheet theStyleSheet = null;
	protected Popup tooltip = null;

	protected HTMLDocument doc;
    protected ArrayList actionListeners;

	public JoStyledLabel(String text)
	{
		super();
		setEditable(false);

		Color bgColor = UIManager.getColor("Panel.background");
		setBackground(bgColor);

		if (htmlKit==null) {
			htmlKit = new HTMLEditorKit();
			theStyleSheet = htmlKit.getStyleSheet();
			theStyleSheet.addRule("body { font-family: sans-serif; font-size: 12; }");
		}

		setEditorKit(htmlKit);
		doc = (HTMLDocument)htmlKit.createDefaultDocument();
		setStyledDocument(doc);

        if (text==null || text.length()==0)
            text = "<html><body id='body'></body></html>";

		setText(text);
		setName(text);
		addHyperlinkListener(this);
        addFocusListener(this);
	}

	public static void appendLink(StringBuffer text, String iconPath, String label, String href)
	{
		if (href!=null) {
			text.append("<a href=\"");
			text.append(href);
			text.append("\">");
		}
		if (iconPath!=null) {
			text.append("<img src=\"file:///");
			text.append(iconPath);
			text.append("\" align=center border=0>");
		}
        if (label!=null) {
            if (iconPath!=null) text.append("&nbsp;");
            text.append(label);
        }
		if (href!=null) {
			text.append("</a>");
		}
	}

    public void addActionListener(ActionListener listener)
    {
        if (actionListeners==null) actionListeners = new ArrayList();
        if (!actionListeners.contains(listener))
            actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener)
    {
        if (actionListeners!=null)
            actionListeners.remove(listener);
    }


	public void setText(String text)
	{
		super.setText(text);
/*
		try {
			htmlKit.insertHTML(doc,0,text,0,0,null);
		} catch (Exception e) {
			Application.error(e);
		}
*/
	}

    public void appendText(String text)
    {
        try {
            //  find the <body> tag
            Element body = doc.getElement("body");
            doc.insertBeforeEnd(body,text);

        } catch (Exception e) {
            Application.error(e);
        }
    }

	public void paintComponent(Graphics g)
	{
		//  enable antialiasing & other useful stuff
		ImgUtil.setTextAntialiasing((Graphics2D)g, true);
		super.paintComponent(g);
	}

	protected void showToolTip(String text, Rectangle loc)
	{
		JToolTip tip = new JToolTip();
		tip.setTipText(text);
//		tip.setBackground(color);

		Point p;
        if (loc==null)
            p = new Point((getWidth()-tip.getWidth())/2, (getHeight()-tip.getHeight())/2);
        else
            p = new Point(loc.x/*+loc.width-tip.getWidth()*/, loc.y+loc.height+4/*-tip.getHeight()*/);

		SwingUtilities.convertPointToScreen(p,this);
		PopupFactory popf = PopupFactory.getSharedInstance();
		tooltip = popf.getPopup(this, tip, p.x,p.y);		
		tooltip.show();
	}

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
	    URL url = e.getURL();

        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
            try {
                if (url.getProtocol().equalsIgnoreCase("verbatim")) {
                    //  triggers action event, just like a JButton does
	                String actionUrl = url.getFile();
                    ActionEvent action = new ActionEvent(this,0,actionUrl);
                    fireActionEvent(action);
                }
	            else if (url!=null) {
                    //  show URL in browser
                    BrowserWindow.showWindow(url);
                }
            } catch (IOException e1) {
                //  can't help it
            }
	    else if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
		    if (tooltip!=null) tooltip.hide();
            setCursor(Cursor.getDefaultCursor());
        }
		else if (e.getEventType()==HyperlinkEvent.EventType.ENTERED)
		{
			if (url!=null && !Util.equals(url.getProtocol(),"verbatim")) {
                Element source = e.getSourceElement();
                //  find location on screen
                try {

                    Rectangle r1 = modelToView(source.getStartOffset());
                    Rectangle r2 = modelToView(source.getEndOffset());
                    Rectangle r = r1.union(r2);

                    showToolTip(url.toExternalForm(),r);

                } catch (BadLocationException e1) {
                    Application.error(e1);
                }
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
    }


    public int setPreferredHeight(int preferredWidth)
    {
        Insets ins = getInsets();
        setSize(preferredWidth, Integer.MAX_VALUE);
        Rectangle r;
        try {
            r = modelToView(getDocument().getLength());
        } catch (BadLocationException blex) {
            return 0;
        }
        if (ins!=null)
            setSize(preferredWidth+ins.right, r.y+r.height+ins.bottom);
        else
            setSize(preferredWidth, r.y+r.height);
        return getHeight();
    }


    public int setFixedWidth(int width)
    {
        setMinimumSize(new Dimension(width,2));
        setMaximumSize(new Dimension(width,Integer.MAX_VALUE));
        setPreferredSize(new Dimension(width, setPreferredHeight(width)));
        return getHeight();
    }

    protected void fireActionEvent(ActionEvent event)
    {
        if (actionListeners==null || actionListeners.isEmpty()) return;
        for (int i=0; i<actionListeners.size(); i++)
        {
            ActionListener listener = (ActionListener)actionListeners.get(i);
            listener.actionPerformed(event);
        }
    }

    public void focusGained(FocusEvent e) {

    }

    public void focusLost(FocusEvent e) {

    }

    public void setVisible(boolean aFlag)
    {
        if (tooltip!=null) tooltip.hide();
        super.setVisible(aFlag);
    }


}
