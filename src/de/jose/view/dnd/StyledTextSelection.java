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

package de.jose.view.dnd;

import sun.awt.datatransfer.SunClipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;

import de.jose.util.ClipboardUtil;
import de.jose.util.StringUtil;
import de.jose.util.ListUtil;

/**
 * @author Peter Schäfer
 */
// This class is used to hold an image while on the clipboard.
public class StyledTextSelection
        extends sun.awt.datatransfer.ClipboardTransferable
        implements Transferable
{
	// ---------------------------------------------------
	//  Constants
	// ---------------------------------------------------

	public static DataFlavor htmlFlavor;
	public static DataFlavor htmlFlavorStr;
	public static DataFlavor rtfFlavor;
	public static DataFlavor rtfFlavorStr;

	static {
		try {
			StyledTextSelection.rtfFlavor = new DataFlavor("text/rtf");
            StyledTextSelection.rtfFlavorStr = new DataFlavor(String.class,"text/rtf");
			StyledTextSelection.htmlFlavor = new DataFlavor("text/html");
            StyledTextSelection.htmlFlavorStr = new DataFlavor(String.class,"text/html");
		} catch (ClassNotFoundException cnfex) {
			//  must not happen at all
		}
	}

	// ---------------------------------------------------
	//  Fields
	// ---------------------------------------------------

	protected DataFlavor[] flavors;
	protected String plainText, htmlText, rtfText;

	// ---------------------------------------------------
	//  Ctor
	// ---------------------------------------------------


	public StyledTextSelection(SunClipboard cb, String plainText, String htmlText, String rtfText)
    {
        super(cb);
        this.plainText = plainText;
        this.htmlText = htmlText;
        this.rtfText = rtfText;
        this.flavors = createTextFlavors(plainText!=null,htmlText!=null,rtfText!=null);
    }


    public StyledTextSelection(SunClipboard cb, String plainText, String fontFamily, int size)
    {
        this(cb, plainText, "","");

        this.htmlText = "<font face=\""+fontFamily+"\">" // size=\""+size+"\">"
                        + StringUtil.replace(plainText,"\n","<br>") +
                        "</font>";

        this.rtfText = "{\\rtf1\\ansi" +
                       "{\\fonttbl{\\f0\\froman "+fontFamily+";}}"+
        //			       "{\\stylesheet{\\fs"+(2*size)+"\\lang1033 \\snext0 Normal;}}"+
                       "\\f0\\fs"+(2*size)+" \\par"+
                       StringUtil.replace(plainText,"\n","\n\\par ")+"}";
    }

    // Returns supported flavors
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    // Returns true if flavor is supported
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        for (int i=0; i<flavors.length; i++)
            if (flavor.equals(flavors[i])) return true;
        return false;
    }

    // Returns text
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (flavor.equals(htmlFlavor) && htmlText!=null) {
            String html = "<html><body>" + htmlText +"</body></html>";
            return new StringBufferInputStream(html);
        }
        if (flavor.equals(htmlFlavorStr) && htmlText!=null) {
            return "<html><body>" + htmlText +"</body></html>";
        }

		if (flavor.equals(rtfFlavor) && rtfText!=null) {
			return new StringBufferInputStream(rtfText);
		}
        if (flavor.equals(rtfFlavorStr) && rtfText!=null) {
            return rtfText;
        }

	    if (flavor.equals(DataFlavor.plainTextFlavor) && plainText!=null) {
	        return new StringReader(plainText);
	    }
	    if (flavor.equals(DataFlavor.stringFlavor)) {
	        return (plainText!=null) ? plainText:"";
	    }

        throw new UnsupportedFlavorException(flavor);
	}

	private static DataFlavor[] createTextFlavors(boolean plain, boolean html, boolean rtf)
	{
	    ArrayList result = new ArrayList();
	    if (plain) {
	        result.add(DataFlavor.stringFlavor);
	        result.add(DataFlavor.plainTextFlavor);
	    }
	    if (html) {
	        result.add(htmlFlavor);
	        result.add(htmlFlavorStr);
	    }
	    if (rtf) {
	        result.add(rtfFlavor);
	        result.add(rtfFlavorStr);
	    }
	    return (DataFlavor[])ListUtil.toArray(result,DataFlavor.class);
	}
}
