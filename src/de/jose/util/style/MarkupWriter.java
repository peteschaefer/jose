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

package de.jose.util.style;

import de.jose.Application;
import de.jose.Util;
import de.jose.view.style.JoFontConstants;
import de.jose.util.AWTUtil;

import javax.swing.text.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.awt.*;

/**
 * MarkupWriter
 *
 * @author Peter Schäfer
 */

public class MarkupWriter
{
    private static MarkupWriter theMarkupWriter = null;

    public static void writeMarkup(StyledDocument doc, int start, int length,
                                 AttributeSet baseStyle, StringBuffer out, boolean escapeDiacritics)
    {
        if (theMarkupWriter==null)
            theMarkupWriter = new MarkupWriter(doc, start, length, baseStyle, escapeDiacritics);
        else
            theMarkupWriter.reset(doc, start, length, baseStyle, escapeDiacritics);
        theMarkupWriter.writeTo(out);
    }

    public static boolean hadMarkup()
    {
        return theMarkupWriter.hasMarkup;
    }

    protected static NumberFormat percentFormat = new DecimalFormat("+###0.##%;-###0.##%", new DecimalFormatSymbols(Locale.US));


    private StyledDocument doc;
    private int start;
    private int end;
    private AttributeSet baseStyle;

    private boolean escapeDiacritics;
    private boolean hasMarkup;


    public MarkupWriter(StyledDocument doc, int start, int length,
                                 AttributeSet baseStyle, boolean escapeDiacritics)
    {
        reset(doc,start,length,baseStyle,escapeDiacritics);
    }


    public void reset(StyledDocument doc, int start, int length, AttributeSet baseStyle, boolean escapeDiacritics)
    {
        this.doc = doc;
        this.start = start;
        this.end = start+length;
        this.baseStyle = baseStyle;
        this.hasMarkup = false;
        this.escapeDiacritics = escapeDiacritics;
    }

    public void writeTo(StringBuffer out)
    {
        //  traverse elements
        AttributeSet prevStyle = baseStyle;

        for (int pos=start; pos < end; )
        {
            Element elm = doc.getCharacterElement(pos);
            int p1 = elm.getStartOffset();
            int p2 = elm.getEndOffset();

            AttributeSet nextStyle = elm.getAttributes();

            styleBreak(prevStyle,nextStyle, out);
            prevStyle = nextStyle;

            text(Math.max(p1,pos), Math.min(p2,end),out);

            pos = p2;
        }

        styleBreak(prevStyle,baseStyle, out);
    }

    private void styleBreak(AttributeSet prevStyle, AttributeSet nextStyle, StringBuffer out)
    {
        boolean prevBold = StyleConstants.isBold(prevStyle);
        boolean prevItalic = StyleConstants.isItalic(prevStyle);
        boolean prevUnderline = StyleConstants.isUnderline(prevStyle);

        boolean nextBold = StyleConstants.isBold(nextStyle);
        boolean nextItalic = StyleConstants.isItalic(nextStyle);
        boolean nextUnderline = StyleConstants.isUnderline(nextStyle);

        int prevSize = StyleConstants.getFontSize(prevStyle);
        int nextSize = StyleConstants.getFontSize(nextStyle);

        Color prevColor = StyleConstants.getForeground(prevStyle);
        Color nextColor = StyleConstants.getForeground(nextStyle);

        String prevFamily = StyleConstants.getFontFamily(prevStyle);
        String nextFamily = StyleConstants.getFontFamily(nextStyle);

        int prevAlign = StyleConstants.getAlignment(prevStyle);
        int nextAlign = StyleConstants.getAlignment(nextStyle);

        boolean closeSize = prevSize!=nextSize && StyleUtil.differsSize(prevStyle,baseStyle);
        boolean closeColor = !Util.equals(prevColor,nextColor) && StyleUtil.differsColor(prevStyle,baseStyle);
        boolean closeFace = !Util.equals(prevFamily,nextFamily) && StyleUtil.differsFamily(prevStyle,baseStyle);

        boolean openSize = prevSize!=nextSize && StyleUtil.differsSize(nextStyle,baseStyle);
        boolean openColor = !Util.equals(prevColor,nextColor) && StyleUtil.differsColor(nextStyle,baseStyle);
        boolean openFace = !Util.equals(prevFamily,nextFamily) && StyleUtil.differsFamily(nextStyle,baseStyle);


        //  close
        if (prevBold && !nextBold) {
            out.append("</b>");
            hasMarkup = true;
        }
        if (prevItalic && !nextItalic) {
            out.append("</i>");
            hasMarkup = true;
        }
        if (prevUnderline && !nextUnderline) {
            out.append("</u>");
            hasMarkup = true;
        }
        if (closeSize || closeColor || closeFace) {
            out.append("</font>");
            hasMarkup = true;
        }
        if (prevAlign!=nextAlign) {
            switch (prevAlign) {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    out.append("</center>");
                    hasMarkup = true;
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    out.append("</div>");
                    hasMarkup = true;
                    break;
            }
        }


        //  open
        if (prevAlign!=nextAlign) {
            switch (nextAlign) {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    out.append("<center>");
                    hasMarkup = true;
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    out.append("<div align=\"right\">");
                    hasMarkup = true;
                    break;
            }
        }
        if (openSize || openColor || openFace)
        {
            out.append("<font");
            if (openFace) {
                out.append(" face=\"");
                out.append(nextFamily);
                out.append("\"");
            }
            if (openColor) {
                out.append(" color=\"");
                out.append(AWTUtil.toString(nextColor,false));
                out.append("\"");
            }
            if (openSize) {
                out.append(" size=\"");
                double scale = (double)nextSize / (double)StyleConstants.getFontSize(baseStyle);
                out.append(percentFormat.format(scale-1.0));
                out.append("\"");
            }
            out.append(">");
            hasMarkup = true;
        }

        if (!prevUnderline && nextUnderline) {
            out.append("<u>");
            hasMarkup = true;
        }
        if (!prevItalic && nextItalic) {
            out.append("<i>");
            hasMarkup = true;
        }
        if (!prevBold && nextBold) {
            out.append("<b>");
            hasMarkup = true;
        }
    }

    private int text(int from, int to, StringBuffer out)
    {
        try {
            int p0 = out.length();
            out.append(doc.getText(from,to-from));

            for (int p=out.length()-1; p >= p0; p--)
            {
                char c = out.charAt(p);
                switch (c)
                {
                case '\n': out.replace(p,p+1,"<br>"); hasMarkup=true; break;
	            case '\t': out.replace(p,p+1,"&#09;"); hasMarkup=true; break;
                case '<': out.replace(p,p+1,"&lt;"); hasMarkup=true; break;
                case '>': out.replace(p,p+1,"&gt;"); hasMarkup=true; break;
                case '&': out.replace(p,p+1,"&amp;"); hasMarkup=true; break;

                default:
                    if (c >= 127 && escapeDiacritics) {
                        out.replace(p,p+1,"&#;");
                        out.insert(p+2,Integer.toString((int)c));
                        hasMarkup=true;
                    }
                    break;
                }
            }
        } catch (BadLocationException e) {
            Application.error(e);
        }
        return to;
    }

}