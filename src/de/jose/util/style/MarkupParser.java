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

package de.jose.util.style;

import de.jose.Application;
import de.jose.view.style.JoStyleContext;
import de.jose.view.style.JoFontConstants;
import de.jose.util.AWTUtil;
import de.jose.sax.JoContentHandler;

import javax.swing.text.*;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.CSS;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.awt.*;
import java.text.ParseException;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * HtmlParser
 *
 * @author Peter Sch�fer
 */

public class MarkupParser
{
    private static ParserDelegator parser = new ParserDelegator();;
    private static CallbackStyledDoc styledDocCallback = new CallbackStyledDoc();
    private static CallbackSAX saxCallback = new CallbackSAX();

	private static AttributesImpl RIGHT_ALIGN_ATTRS = new AttributesImpl();
	static {
		RIGHT_ALIGN_ATTRS.addAttribute("","","align","CDATA","right");
	}

    /**
     *  parse hmtl input, insert text into a StyledDocument
     * */
    public static int insertMarkup(StyledDocument doc, int at,
                           AttributeSet baseStyle,
                           StringBuffer markup)
    {
        /** set up a markup parser    */
        Reader rd = new StringReader(markup.toString());
        styledDocCallback.reset(doc,at,baseStyle);

        try {
            parser.parse(rd, styledDocCallback, true);
        } catch (IOException e) {
            //  not thrown at all
        }
        //  revert to default style (especially paragraph!)
        styledDocCallback.finish();

        return styledDocCallback.segmentLength();
    }

    /**
     *  parse hmtl input, insert text into a StyledDocument
     * */
    public static boolean isMarkup(char ch)
    {
        switch (ch)
        {
            case '<': case '>': case '&': case '\n': case '\t':
                return true;
            default:
                return false;
        }
    }

    public static boolean hadMarkup()
    {
        return styledDocCallback.hasMarkup;
    }

    public static void insertSAX(StringBuffer markup, JoContentHandler sax)
    {
        /** set up a markup parser    */
        Reader rd = new StringReader(markup.toString());
        saxCallback.reset(sax);

        try {
            parser.parse(rd, saxCallback, true);
        } catch (IOException e) {
            //  not thrown at all
        }
    }



    private static class CallbackSAX extends HTMLEditorKit.ParserCallback
    {
        private JoContentHandler handler;

        public void reset(JoContentHandler saxHandler)
        {
            this.handler = saxHandler;
        }


	    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
	    {
		    try {

			    if (Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)))
			        return;
			    if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
			        return;

			    if (t==HTML.Tag.B)
			        handler.startElement("bold");
			    else if (t==HTML.Tag.I)
			        handler.startElement("italic");
			    else if (t==HTML.Tag.U)
			        handler.startElement("underline");
			    else if (t==HTML.Tag.CENTER)
			        handler.startElement("center");
			    else if (t==HTML.Tag.DIV)
			    {
				    String align = (String)a.getAttribute(HTML.Attribute.ALIGN);
                    if ("right".equalsIgnoreCase(align))
				        handler.startElement("right");
                    else
                        handler.startElement("right");  //  ignore, currently !! TODO
			    }
			    else if (t==HTML.Tag.FONT)
			    {
                    /** TODO when processing for FOP, we might need to register unknown fonts
                     * */
                    String size = (String)a.getAttribute(HTML.Attribute.SIZE);
                    String color = (String)a.getAttribute(HTML.Attribute.COLOR);
                    String face = (String)a.getAttribute(HTML.Attribute.FACE);

				    AttributesImpl attrs = new AttributesImpl();
                    if (size!=null)
				        attrs.addAttribute(null,"size","size","CDATA",size);
                    if (color!=null)
				        attrs.addAttribute(null,"color","color","CDATA",color);
                    if (face!=null)
                        attrs.addAttribute(null,"face","face","CDATA",face);

				    handler.startElement("font",attrs);
			    }
			    else
			        handler.characters("<"+t.toString()+">");

		    } catch (SAXException e) {
			    handleError(e.getMessage(),pos);
		    }
	    }

	    public void handleEndTag(HTML.Tag t, int pos)
	    {
		    try {

			    if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
			        return;

			    if (t==HTML.Tag.B)
			        handler.endElement("bold");
			    else if (t==HTML.Tag.I)
			        handler.endElement("italic");
			    else if (t==HTML.Tag.U)
			        handler.endElement("underline");
			    else if (t==HTML.Tag.CENTER)
			        handler.endElement("center");
			    else if (t==HTML.Tag.DIV)
				    handler.endElement("right");
			    else if (t==HTML.Tag.FONT)
				    handler.endElement("font");
			    else
			        handler.characters("</"+t.toString()+">");

		    } catch (SAXException e) {
			    handleError(e.getMessage(),pos);
		    }
	    }

	    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
	    {
		    try {

			    if (Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)))
			        return;
			    if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
			        return;

			    if (t==HTML.Tag.BR)
			        handler.element("br",null);     //  line break (right?)
			    else
			       handler.characters("<"+t.toString()+">");

		    } catch (SAXException e) {
			    handleError(e.getMessage(),pos);
		    }
	    }

	    public void handleComment(char[] data, int pos) {
	        /* do nothing, right ? */
	    }

	    public void handleEndOfLineString(String eol) {
	        /* ignore */
	    }

	    public void handleError(String errorMsg, int pos) {
	        /* what's this ? */
	        //text(errorMsg);
	    }

	    public void handleText(char[] data, int pos) {
		    try {
			    handler.characters(new String(data));
		    } catch (SAXException e) {
			    handleError(e.getMessage(),pos);
		    }
	    }

      }

    private static class CallbackStyledDoc extends HTMLEditorKit.ParserCallback
    {
        private StyledDocument doc;
        private int start;
        private AttributeSet baseStyle;

        private int current;
        private MutableAttributeSet characterStyle;
        private MutableAttributeSet paragraphStyle;
        private boolean hasMarkup;

        public void reset(StyledDocument doc, int start, AttributeSet baseStyle)
        {
            this.doc = doc;
            this.start = start;
            this.baseStyle = baseStyle;
            this.hasMarkup = false;

            current = start;
            characterStyle = new SimpleAttributeSet(baseStyle);
            paragraphStyle = new SimpleAttributeSet();
        }

        public int segmentLength()  { return current-start; }

        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos)
        {
	        if (Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)))
	            return;
	        if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
	            return;

            if (t==HTML.Tag.B) {
                StyleConstants.setBold(characterStyle,true);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.I) {
                StyleConstants.setItalic(characterStyle,true);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.U) {
                StyleConstants.setUnderline(characterStyle,true);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.CENTER) {
                StyleConstants.setAlignment(paragraphStyle,StyleConstants.ALIGN_CENTER);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.DIV)
            {
                String align = (String)a.getAttribute(HTML.Attribute.ALIGN);
                if ("right".equalsIgnoreCase(align))
                    StyleConstants.setAlignment(paragraphStyle, StyleConstants.ALIGN_RIGHT);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.FONT)
            {
                //  FONT size=... color=...
                if (a.isDefined(HTML.Attribute.SIZE))
                {
                    String sizeStr = (String)a.getAttribute(HTML.Attribute.SIZE);
                    //  parse into a usable value (example "+16.67%")
                    try {
                        float scale = 1.0f + MarkupWriter.percentFormat.parse(sizeStr).floatValue();
                        /** is font-scale already defined in style ?    */
                        int size = JoFontConstants.getFontSize(baseStyle);

                        int newSize = Math.round(JoFontConstants.getFontSize(baseStyle)*scale);
                        if (scale > 1.0)
                            newSize = Math.max(size+1,newSize);
                        else
                            newSize = Math.min(size-1,newSize);

                        JoFontConstants.setFontScaleFactor(characterStyle,scale);
                        StyleConstants.setFontSize(characterStyle,newSize);

                    } catch (ParseException e) {

                    }
                    hasMarkup = true;
                }
                if (a.isDefined(HTML.Attribute.COLOR))
                {
                    String colorStr = (String)a.getAttribute(HTML.Attribute.COLOR);
                    //  parse into a usable color (example "#ff0000")
                    try {
                        Color color = AWTUtil.parseColor(colorStr);
                        StyleConstants.setForeground(characterStyle,color);
                        hasMarkup = true;
                    } catch(IllegalArgumentException e) {
                        //  error in color string. Should that stop us? No.
                        System.err.println("Invalid color specified for "+colorStr);
                    }
                }
                if (a.isDefined(HTML.Attribute.FACE))
                {
                    String family = (String)a.getAttribute(HTML.Attribute.FACE);
                    StyleConstants.setFontFamily(characterStyle,family);
                    hasMarkup = true;
                }
            }
            else
                text("<"+t.toString()+">");
        }

        public void handleEndTag(HTML.Tag t, int pos)
        {
	        if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
	            return;

            if (t==HTML.Tag.B) {
                StyleConstants.setBold(characterStyle,false);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.I) {
                StyleConstants.setItalic(characterStyle,false);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.U) {
                StyleConstants.setUnderline(characterStyle,false);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.CENTER) {
                StyleConstants.setAlignment(paragraphStyle,StyleConstants.ALIGN_LEFT);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.DIV)
            {
                StyleConstants.setAlignment(paragraphStyle,StyleConstants.ALIGN_LEFT);
                hasMarkup = true;
            }
            else if (t==HTML.Tag.FONT)
            {
                //  which styles are closed ???
                characterStyle.removeAttribute(StyleConstants.Foreground);
                characterStyle.removeAttribute(StyleConstants.FontFamily);
                characterStyle.removeAttribute(StyleConstants.FontSize);
                characterStyle.removeAttribute(JoFontConstants.FontScaleFactor);
                hasMarkup = true;
            }
            else
                text("</"+t.toString()+">");
        }

        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos)
        {
	        if (Boolean.TRUE.equals(a.getAttribute(HTMLEditorKit.ParserCallback.IMPLIED)))
	            return;
	        if (t==HTML.Tag.HTML || t==HTML.Tag.HEAD || t==HTML.Tag.BODY)
	            return;

            if (t==HTML.Tag.BR) {
                text("\n");     //  line break
                hasMarkup = true;
            }
            else
               text("<"+t.toString()+">");
        }

        public void handleComment(char[] data, int pos) {
            /* do nothing, right ? */
        }

        public void handleEndOfLineString(String eol) {
            /* ignore */
        }

        public void handleError(String errorMsg, int pos) {
            /* what's this ? */
            //text(errorMsg);
        }

        public void handleText(char[] data, int pos) {
            text(new String(data));
        }

        protected void finish()
        {
            //  at end of section, revert to default paragrahp alignemnt
            StyleConstants.setAlignment(paragraphStyle,StyleConstants.ALIGN_LEFT);
            doc.setParagraphAttributes(current,0, paragraphStyle, false);
        }

        private void text(String str)
        {
            try {

                int from = current;
                doc.insertString(current,str,characterStyle);
                current += str.length();
                doc.setParagraphAttributes(from,current,paragraphStyle,false);

            } catch (BadLocationException e) {
                Application.error(e);
            }
        }
    }
}