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
import de.jose.view.dnd.ImageSelection;
import de.jose.view.dnd.DatabaseGameSelection;
import de.jose.view.dnd.StyledTextSelection;
import de.jose.util.file.FileUtil;
import de.jose.util.style.StyleUtil;
import de.jose.util.style.MarkupWriter;
import de.jose.util.rtf.RTFGenerator;

import java.awt.datatransfer.*;
import java.awt.*;
import java.io.*;

import java.awt.datatransfer.Clipboard;

import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.text.*;

/**
 * ClipboardUtil
 * 
 * @author Peter Schäfer
 */

public class ClipboardUtil
{

    private static RTFEditorKit theRtfEditorKit = null;
    private static StyledDocument theRtfTempDoc = null;

	public static String toString(Object object)
    {
        if (object==null)
            return "";

        if (object instanceof InputStream)
        {
            InputStreamReader sin = new InputStreamReader((InputStream)object);
            StringWriter sout = new StringWriter();
            try {
                FileUtil.copyReader(sin,sout);
            } catch (IOException e) {
                //  can't happen, or can it ?
            }
            return sout.toString();
        }

        if (object instanceof ByteArrayOutputStream)
        try {
            byte[] bytes = ((ByteArrayOutputStream)object).toByteArray();
            return new String(bytes,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            //  must not happen
        }

        if (object instanceof byte[])
        try {
            return new String((byte[])object,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            //  must not happen
        }

       //   else
       return object.toString();
    }


    public static String getRtfText(StyledDocument doc, int pos, int length)
    {
        ByteArrayOutputStream rtfText = new ByteArrayOutputStream();

	    try {
		    RTFGenerator.writeDocument(doc, rtfText, pos, length);
        } catch (IOException e) {
            Application.error(e);
        }
        return toString(rtfText);
    }

	private static void initRtfDoc()
			throws BadLocationException
    {
            if (theRtfTempDoc==null)
                theRtfTempDoc = new DefaultStyledDocument();
            else
                theRtfTempDoc.remove(0,theRtfTempDoc.getLength());

		//  make sure there is no trailing \n at the end of the document !!

            if (theRtfEditorKit==null)
                theRtfEditorKit = new RTFEditorKit();
	}


	public static String rtfToHtml(String rtf)
	{
	    StringBuffer buf = new StringBuffer();
	    try {
	        //  transform RTF into HTML -- PROVISIONAL --
		    initRtfDoc();

            theRtfEditorKit.read(new StringReader(rtf),theRtfTempDoc,0);
		    //  remove trailing line break
		    if (theRtfTempDoc.getLength() > 0)
		        theRtfTempDoc.remove(theRtfTempDoc.getLength()-1, 1);

            AttributeSet style = StyleUtil.plainStyle(Color.black,null,12);
            MarkupWriter.writeMarkup(theRtfTempDoc,0,theRtfTempDoc.getLength(), style,buf,true);

            theRtfTempDoc.remove(0,theRtfTempDoc.getLength());

        } catch (BadLocationException e) {
            Application.error(e);
        } catch (IOException e) {
            Application.error(e);
        }

        return buf.toString();
    }


	public static void setPlainText(Object object, ClipboardOwner owner)
	{
		Clipboard sysclip = Toolkit.getDefaultToolkit().getSystemClipboard();

		StringSelection contents = new StringSelection(toString(object));
		sysclip.setContents(contents,owner);
	}

    public static void setDatabaseGames(String asText, ClipboardOwner owner)
    {
        Clipboard sysclip = Toolkit.getDefaultToolkit().getSystemClipboard();

        DatabaseGameSelection contents = new DatabaseGameSelection(asText);
        sysclip.setContents(contents,owner);
    }


    private static String getClipboardText(Object requestor, DataFlavor flavor1, DataFlavor flavor2)
    {
        Object content = null;
        try {

            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clip.getContents(requestor);

            if (contents!=null) {
                if (contents.isDataFlavorSupported(flavor1))
                    content = contents.getTransferData(flavor1);
                else if (contents.isDataFlavorSupported(flavor2))
                    content = contents.getTransferData(flavor2);
            }

        } catch (UnsupportedFlavorException e) {
            /*  we already checked against it - should not happen at all */
        } catch (IOException e) {
            Application.error(e);
        }
        //  else
        return toString(content);
    }


	public static String getPlainText(Object requestor)
	{
        return getClipboardText(requestor, DataFlavor.stringFlavor, DataFlavor.plainTextFlavor);
	}

    public static String getHtmlText(Object requestor)
    {
        return getClipboardText(requestor, StyledTextSelection.htmlFlavor, StyledTextSelection.htmlFlavorStr);
    }

    public static String getRtfText(Object requestor)
    {
        return getClipboardText(requestor, StyledTextSelection.rtfFlavor, StyledTextSelection.rtfFlavorStr);
    }

	public static void setStyledText(String text, String fontFamily, int size, ClipboardOwner owner)
	{
		Clipboard sysclip = Toolkit.getDefaultToolkit().getSystemClipboard();

		StyledTextSelection contents = new StyledTextSelection(text,fontFamily,size);
		sysclip.setContents(contents,owner);
	}

    public static void setStyledText(String plainText, String htmlText, String rtfText, ClipboardOwner owner)
    {
        Clipboard sysclip = Toolkit.getDefaultToolkit().getSystemClipboard();

        StyledTextSelection contents = new StyledTextSelection(plainText,htmlText,rtfText);
        sysclip.setContents(contents,owner);
    }

	public static void setImage(Image image, ClipboardOwner owner) throws Exception
	{
		Clipboard sysclip = Toolkit.getDefaultToolkit().getSystemClipboard();

		ImageSelection contents = new ImageSelection(image);
		sysclip.setContents(contents, null);
	}
}