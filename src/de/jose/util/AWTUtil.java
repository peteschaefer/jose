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

package de.jose.util;

import de.jose.Application;
import de.jose.Version;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;
import java.text.ParsePosition;

public class AWTUtil
{
	public static void setCursor(Component comp, Cursor cursor)
	{
		if (comp!=null && cursor!=null)
			comp.setCursor(cursor);
	}

	public static void setCursor(Component comp, int type)
	{
		setCursor(comp,Cursor.getPredefinedCursor(type));
	}

	public static void setWaitCursor(Component comp)
	{
		setCursor(comp,Cursor.WAIT_CURSOR);
	}

	public static void setDefaultCursor(Component comp)
	{
		setCursor(comp,Cursor.getDefaultCursor());
	}

    public static void centerOn(Component comp, Component parent)
    {
        Dimension csize = comp.getSize();
        Rectangle pbounds = parent.getBounds();
        comp.setLocation(
                /*pbounds.x+*/(pbounds.width-csize.width)/2,
                /*pbounds.y+*/(pbounds.height-csize.height)/2);
	    //  location is assumed to be relative to parent
    }

	public static Point convertPoint(MouseEvent evt, Component target)
	{
		return SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), target);
	}

	public static boolean isInside(MouseEvent evt, Component target)
	{
		return target.contains(convertPoint(evt,target));
	}

	/**
	 * set the text of a JTextArea without moving the caret
	 * normally, setting the text would trigger am event that scrolls the Text Area into view.
	 * If we don't want scrolling, we need to avoid the caret event.
	 *
	 * @param textArea
	 * @param value
	 */
	public static void setTextSafe(JTextArea textArea, String value)
	{
		Caret caret = textArea.getCaret();
		caret.setDot(0);
		textArea.setCaret(null);
		textArea.setText(value);
		textArea.setCaret(caret);
	}

	public static void beep(Component comp)
	{
		UIManager.getLookAndFeel().provideErrorFeedback(comp);
	}

    public static void scrollDown(JScrollPane scroller, final JComponent comp)
    {
        if (!scroller.getVerticalScrollBar().getModel().getValueIsAdjusting())
			SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Rectangle r = new Rectangle(0, comp.getHeight()-10, comp.getWidth(), comp.getHeight());
                    scrollRectToVisible(comp,r);
                } catch (Throwable e) {
                    Application.error(e);
                }
            }});
    }

	public static void scrollUp(JScrollPane scroller, final JComponent comp)
	{
	    if (!scroller.getVerticalScrollBar().getModel().getValueIsAdjusting())
			SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            try {
	                Rectangle r = new Rectangle(0, 0, comp.getWidth(), 10);
	                scrollRectToVisible(comp,r);
	            } catch (Throwable e) {
	                Application.error(e);
	            }
	        }});
	}

	/**
	 * get the screen resolution, i.e. the number of pixels per inch
	 */
	public static int getScreenResolution()
	{
        int result = Toolkit.getDefaultToolkit().getScreenResolution();
        if (Version.mac && result==72)
        {
            /**
             * getScreenResolution() is unreliable on OS X.
             * It will always return 72 ;-(
             * Best thing we can do is determine the screen size in pixels
             * and try to /guess/ the screen resolution...
             */
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            double diagonal = Math.sqrt(size.width*size.width + size.height*size.height);

            if (size.width <= 800)
                return (int)(diagonal/17);    //  looks like a 17 inch monitor ?!
            else if (size.width <= 1280)
                return (int)(diagonal/19);   //  looks like a 19 monitor
            else
                return (int)(diagonal/21);   //  assume a 21 inch monitor

        }
        return result;
	}

	private static AffineTransform normTransform = null;

	/**
	 * get the normalisation transform; applying this transform will
	 * print fonts in the correct size
	 *
	 *
	 */
	public static AffineTransform getNormalizingTransform()
	{
		if (normTransform==null) {
/*
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            normTransform = gc.getDefaultTransform();
            normTransform = gc.getNormalizingTransform();
/*
		nice try, but this doesn't work.
		on Windows, getGraphicsConfiguration().getNormalizingTransform()
        returns ALWAYS the identity	what the hell is it good for ????

        please note that getScreenResolution() isn't very reliable either.
        e.g. my actual screen resolution is 112 pixel per inch
        getScreenResolution() reports 90 on Windows, 80 on Linux, 72 on Mac OS X !
        but what can we do ?
*/
			int screenRes = getScreenResolution();
//          int screenRes = 112;
			normTransform = AffineTransform.getScaleInstance((double)screenRes/72.0,(double)screenRes/72.0);
		}
		return normTransform;

	}

	public static Rectangle getInsetBounds(JComponent comp)
	{
		Rectangle bounds = comp.getBounds();
		Insets ins = comp.getInsets();
		if (ins!=null) {
			bounds.x += ins.left;
			bounds.y += ins.top;
			bounds.width -= ins.right+ins.left;
			bounds.height -= ins.top+ins.bottom;
		}
		return bounds;
	}


    public static long parseHex(StringBuffer input, ParsePosition offset)
    {
        long result = 0;
        while (offset.getIndex() < input.length())
        {
            int i = offset.getIndex();
            char c = input.charAt(i);
            if (c>='0' && c<='9')
            {
                result = (result << 4) + (c-'0');
                offset.setIndex(i+1);
            }
            else if (c>='a' && c<='f')
            {
                result = (result << 4) + (c-'a'+10);
                offset.setIndex(i+1);
            }
            else if (c>='A' && c<='F')
            {
                result = (result << 4) + (c-'A'+10);
                offset.setIndex(i+1);
            }
            else
                break;
        }
        return result;
    }


    public static long parseHex(char[] input, ParsePosition offset, int length)
    {
        long result = 0;
        while (offset.getIndex() < length)
        {
            int i = offset.getIndex();
            char c = input[i];
            if (c>='0' && c<='9')
            {
                result = (result << 4) + (c-'0');
                offset.setIndex(i+1);
            }
            else if (c>='a' && c<='f')
            {
                result = (result << 4) + (c-'a'+10);
                offset.setIndex(i+1);
            }
            else if (c>='A' && c<='F')
            {
                result = (result << 4) + (c-'A'+10);
                offset.setIndex(i+1);
            }
            else
                break;
        }
        return result;
    }


    public static long parseInt(StringBuffer input, ParsePosition offset)
    {
        long result = 0;
        int i;
        while ((i=offset.getIndex()) < input.length())
        {
            char c = input.charAt(i);
            if (c>='0' && c<='9')
            {
                result = (result * 10) + (c-'0');
                offset.setIndex(i+1);
            }
            else
                break;
        }
        return result;
    }

    public static long parseInt(char[] input, ParsePosition offset, int length)
    {
        long result = 0;
        int i;
        while ((i=offset.getIndex()) < length)
        {
            char c = input[i];
            if (c>='0' && c<='9')
            {
                result = (result * 10) + (c-'0');
                offset.setIndex(i+1);
            }
            else
                break;
        }
        return result;
    }

	/**	please read the following comments on scrollRectToVisible
	 * 	(from JRE source code:)
	 *
	// NOTE: How JViewport currently works with the
	// backing store is not foolproof. The sequence of
	// events when setViewPosition
	// (scrollRectToVisible) is called is to reset the
	// views bounds, which causes a repaint on the
	// visible region and sets an ivar indicating
	// scrolling (scrollUnderway). When
	// JViewport.paint is invoked if scrollUnderway is
	// true, the backing store is blitted.  This fails
	// if between the time setViewPosition is invoked
	// and paint is received another repaint is queued
	// indicating part of the view is invalid. There
	// is no way for JViewport to notice another
	// repaint has occured and it ends up blitting
	// what is now a dirty region and the repaint is
	// never delivered.
	// It just so happens JTable encounters this
	// behavior by way of scrollRectToVisible, for
	// this reason scrollUnderway is set to false
	// here, which effectively disables the backing
	// store.

	* any idea how to solve this ?? 
	*/
	public static void scrollRectToVisible(JComponent comp, Rectangle r)
	{
		comp.scrollRectToVisible(r);
	}

    public static void scrollRectLater(final JComponent comp, final Rectangle r)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                comp.scrollRectToVisible(r);
            }
        });
    }


    public static String toString(Color color)
    {
        return toString(color,color.getAlpha() < 255);
    }

    public static String toString(Color color, boolean alpha)
    {
        StringBuffer buf = new StringBuffer();
        appendHexColor(buf,color,alpha);
        return buf.toString();
    }

    public static Color parseColor(String string)
    {
        int p0=0;
        if (string.charAt(0)=='#') p0++;    //  # is optional
        int red = parseHex(string,p0,2);
        int green = parseHex(string,p0+2,2);
        int blue = parseHex(string,p0+4,2);
        int alpha = 255;
        if (string.length() >= (p0+8))
            alpha = parseHex(string,p0+6,2);

        return new Color(red,green,blue,alpha);
    }

    public static void appendHexColor(StringBuffer buf, Color col)
	{
        appendHexColor(buf,col,col.getAlpha() < 255);
    }

    public static void appendHexColor(StringBuffer buf, Color col, boolean alpha)
	{
		buf.append("#");
		appendHex(buf, col.getRed());
		appendHex(buf, col.getGreen());
		appendHex(buf, col.getBlue());
        if (alpha) appendHex(buf, col.getAlpha());
	}

    public static void appendHex(StringBuffer buf, int x)
	{
		appendHexChar(buf, (x>>4) & 0x0f);
		appendHexChar(buf, x & 0x0f);
	}

    public static void appendHexChar(StringBuffer buf, int x)
	{
		if (x >= 10)
			buf.append((char)('a'+x-10));
		else
			buf.append((char)('0'+x));
	}

    public static int parseHex(String string, int from, int length)
    {
        int result = 0;
        int end = from+length;
        while (from < end)
            result = (result<<4) + Character.digit(string.charAt(from++),16);
        return result;
    }

    public static KeyStroke getMenuKeyStroke(String stroke)
    {
        KeyStroke key = KeyStroke.getKeyStroke(stroke);
        if (key!=null && (key.getModifiers() & Event.CTRL_MASK) != 0)
        {
            //  replace CTRL key with local preferred key (e.g. Command on Mac)
            key = KeyStroke.getKeyStroke(key.getKeyCode(),
                    (key.getModifiers() & ~(Event.CTRL_MASK+0x80))
                    | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
                    key.isOnKeyRelease());
        }
        return key;
    }



    public static final void centerOnScreen(Component comp)
	{
		centerOnScreen(comp, comp.getWidth(),comp.getHeight());
	}

    public static final void centerOnScreen(Component comp, int width, int height)
	{
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		comp.setBounds(p.x-width/2, p.y-height/2, width,height);
	}
}
