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
import de.jose.util.AWTUtil;
import de.jose.util.StringUtil;
import de.jose.util.CharUtil;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;

import javax.swing.text.*;
import java.util.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * HtmlWriter
 *
 * @author Peter Schäfer
 * @deprecated
 */

public class MarkupWriter1
{
	private static MarkupWriter1 theMarkupWriter = null;

    public static void writeMarkup(StyledDocument doc, int start, int length,
                                 AttributeSet baseStyle, StringBuffer out, boolean escapeDiacritics)
    {
		if (theMarkupWriter==null)
			theMarkupWriter = new MarkupWriter1(doc, start, length, baseStyle, escapeDiacritics);
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

	private SortedSet sortByStart;
	private SortedSet sortByEnd;
    private boolean escapeDiacritics;
    private boolean hasMarkup;


	public MarkupWriter1(StyledDocument doc, int start, int length,
								 AttributeSet baseStyle, boolean escapeDiacritics)
	{
		sortByStart = new TreeSet(SORT_BY_START);
		sortByEnd = new TreeSet(SORT_BY_END);

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

		sortByStart.clear();
		sortByEnd.clear();

	    //  get elements, sorted by start offset and end offset
	    /** 1. collect paragraph elements  */
	    for (int p=start; p < end; )
	    {
		    Element elm = doc.getParagraphElement(p);
		    sortByStart.add(elm);
		    sortByEnd.add(elm);
		    p = elm.getEndOffset();
	    }
	    /** 2. collect character elements  */
	    for (int p=start; p < end; )
	    {
		    Element elm = doc.getCharacterElement(p);
		    sortByStart.add(elm);
		    sortByEnd.add(elm);
		    p = elm.getEndOffset();
	    }
	}

	public void writeTo(StringBuffer out)
	{
	    /** 3. iterate over elements !    */
	    Iterator sit = sortByStart.iterator();
	    Iterator eit = sortByEnd.iterator();

	    Element selm = sit.hasNext() ? (Element)sit.next():null;
	    Element eelm = eit.hasNext() ? (Element)eit.next():null;

		int p0 = start;
	    while (selm!=null || eelm!=null)
	    {
		    if (eelm==null || selm!=null && selm.getStartOffset() < eelm.getEndOffset())
		    {
			    //  open selm
			    int p = selm.getStartOffset();
			    if (p>=start && p<end && p > p0)
					    p0 = text(p0,p,out);

			    openStyle(selm.getAttributes(),out);
			    selm = sit.hasNext() ? (Element)sit.next():null;
		    }
		    else
		    {
			    //  close eelm
			    int p = eelm.getEndOffset();
			    if (p>=start && p<end && p > p0)
					    p0 = text(p0,p,out);

			    closeStyle(eelm.getAttributes(),out);
			    eelm = eit.hasNext() ? (Element)eit.next():null;
		    }
	    }

		if (end > p0) text(p0,end,out);
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
				case '<': out.replace(p,p+1,"&lt;"); hasMarkup=true; break;
				case '>': out.replace(p,p+1,"&gt;"); hasMarkup=true; break;
				case '&': out.replace(p,p+1,"&amp;"); hasMarkup=true; break;

                default:
                    if (c >= 127 && escapeDiacritics) {
                        out.replace(p,p+1, "&#"+Integer.toString((int)c)+";"); hasMarkup=true;
                    }
                    break;
				}
			}
		} catch (BadLocationException e) {
			Application.error(e);
		}
		return to;
	}

	private void openStyle(AttributeSet style, StringBuffer out)
	{
		if (style.isDefined(StyleConstants.Bold) &&
		        StyleConstants.isBold(style)!=StyleConstants.isBold(baseStyle))
        {
			out.append("<b>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Italic) &&
		        StyleConstants.isItalic(style)!=StyleConstants.isItalic(baseStyle))
        {
			out.append("<i>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Underline) &&
		        StyleConstants.isUnderline(style)!=StyleConstants.isUnderline(baseStyle))
        {
			out.append("<u>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Alignment))
		{
			switch (StyleConstants.getAlignment(style))
			{
			case StyleConstants.ALIGN_CENTER:
			case StyleConstants.ALIGN_JUSTIFIED:
				out.append("<center>");
                hasMarkup=true;
				break;
			case StyleConstants.ALIGN_RIGHT:
				out.append("<div align=right>");
                hasMarkup=true;
				break;
			}
		}

        boolean dcolor = StyleUtil.differsColor(style,baseStyle);
        boolean dsize = StyleUtil.differsSize(style,baseStyle);
        boolean dfamily = StyleUtil.differsFamily(style,baseStyle);

        if (dcolor || dsize || dfamily)
        {
            out.append("<font");
            if (dcolor) {
                Color color = StyleConstants.getForeground(style);
                out.append(" color=\"");
                out.append(AWTUtil.toString(color,false));
                out.append("\"");
            }
            if (dsize) {
                double scale = (double)StyleConstants.getFontSize(style) / (double)StyleConstants.getFontSize(baseStyle);
                out.append(" size=\"");
                out.append(percentFormat.format(scale-1.0));
                out.append("\"");
            }
            if (dfamily) {
                out.append(" face=\"");
                out.append(JoFontConstants.getFontFamily(style));
                out.append("\"");
            }
            out.append(">");
            hasMarkup=true;
        }

	}

	private void closeStyle(AttributeSet style, StringBuffer out)
	{
        if (StyleUtil.differsColor(style,baseStyle) ||
            StyleUtil.differsSize(style,baseStyle) ||
            StyleUtil.differsFamily(style,baseStyle))
        {
            out.append("</font>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Alignment))
		{
			switch (StyleConstants.getAlignment(style))
			{
			case StyleConstants.ALIGN_CENTER:
			case StyleConstants.ALIGN_JUSTIFIED:
				out.append("</center>");
                hasMarkup=true;
				break;
			case StyleConstants.ALIGN_RIGHT:
				out.append("</div>");
                hasMarkup=true;
				break;
			}
		}

		if (style.isDefined(StyleConstants.Underline) &&
		        StyleConstants.isUnderline(style)!=StyleConstants.isUnderline(baseStyle))
        {
			out.append("</u>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Italic) &&
		        StyleConstants.isItalic(style)!=StyleConstants.isItalic(baseStyle))
        {
            out.append("</i>");
            hasMarkup=true;
        }

		if (style.isDefined(StyleConstants.Bold) &&
		        StyleConstants.isBold(style)!=StyleConstants.isBold(baseStyle))
        {
			out.append("</b>");
            hasMarkup=true;
        }
	}


	private static Comparator SORT_BY_START = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			Element a = (Element)o1;
			Element b = (Element)o2;

			int diff = a.getStartOffset()-b.getStartOffset();
			if (diff==0)
				diff = b.getEndOffset()-a.getEndOffset();
			return diff;
		}
	};

	private static Comparator SORT_BY_END = new Comparator()
	{
		public int compare(Object o1, Object o2)
		{
			Element a = (Element)o1;
			Element b = (Element)o2;

			int diff = a.getEndOffset()-b.getEndOffset();
			if (diff==0)
				diff = b.getStartOffset()-a.getStartOffset();
			return diff;
		}
	};
}