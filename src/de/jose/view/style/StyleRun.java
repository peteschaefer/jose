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


package de.jose.view.style;

import de.jose.util.AWTUtil;
import de.jose.util.StringUtil;
import de.jose.util.ByteUtil;
import de.jose.util.style.StyleUtil;
import de.jose.util.file.LinePrintWriter;
import de.jose.chess.EngUtil;
import de.jose.sax.JoContentHandler;

import javax.swing.text.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.ParserDelegator;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.text.ParsePosition;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * StyleRun
 *
 * @author Peter Schäfer
 * @deprecated
 */

public class StyleRun
 {

    // ------------------------------------------------------------
    //  Inner Class
    // ------------------------------------------------------------

    public class Run
    {
        public boolean bold;
        public boolean italic;
        public boolean underline;
        public Color color;
        public int size;
        public int align = StyleConstants.ALIGN_LEFT;

        public int start;
        public int length;

        public int end()    { return start+length; }

        public Run(int start, int length)
        {
            this.start = start;
            this.length = length;
        }


        /**
         * apply to document
         */
        public void apply(StyledDocument doc, int offset)
        {
            boolean p = hasParagraphAttributes();
            boolean c = hasCharacterAttributes();

            if (p || c)
            {
                AttributeSet style = getStyle();
                if (p) doc.setParagraphAttributes(offset+start,length,style,false);
                if (c) doc.setCharacterAttributes(offset+start,length,style,false);
            }
        }

        public boolean hasParagraphAttributes()
        {
            return (align!=StyleConstants.ALIGN_LEFT);
        }

        public boolean hasCharacterAttributes()
        {
            return (bold || italic || underline || (size>0) || (color!=null));
        }

        /**
         * encode style run as string
         * @param buf
         */
        public void print(PrintWriter pout)
        {
            pout.print("[");
            switch (align)
            {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:   pout.print("j"); break;
            case StyleConstants.ALIGN_RIGHT:       pout.print("r"); break;
            }
            if (bold) pout.print("b");
            if (italic) pout.print("i");
            if (underline) pout.print("u");
            if (size > 0) {
                pout.print("s");
                pout.print(Integer.toString(size));
            }
            if (color!=null) {
                pout.print("c");
                pout.print(Integer.toHexString(color.getRGB()));
            }
            pout.print(":");
            pout.print(Integer.toString(start));
            pout.print(":");
            pout.print(Integer.toString(length));
            pout.print("]");
        }

        /**
         * write to binary
         * @param output
         * @param offset
         * @return
         */
        public void write(ByteArrayOutputStream bout)
        {
			byte b0 = 0x01;

			switch (align)
			{
			case StyleConstants.ALIGN_CENTER:
			case StyleConstants.ALIGN_JUSTIFIED:
					b0 |= 0x02; break;
			case StyleConstants.ALIGN_RIGHT:
					b0 |= 0x04; break;
			}

			if (bold)       b0 |= 0x08;
			if (italic)     b0 |= 0x10;
			if (underline)  b0 |= 0x20;

			if (size > 0)   b0 |= 0x40;
			if (color != null) b0 |= 0x80;

			bout.write(b0);

			if (size > 0) {
				ByteUtil.writeShort(bout, (short)size);
			}
			if (color!=null) {
				int rgb = color.getRGB();
				ByteUtil.writeInt(bout, rgb);
			}

			ByteUtil.writeShort(bout, (short)start);
			ByteUtil.writeShort(bout, (short)length);
       }

        public int write(byte[] bout, int offset)
        {
			byte b0 = 0x01;

			switch (align)
			{
			case StyleConstants.ALIGN_CENTER:
			case StyleConstants.ALIGN_JUSTIFIED:
					b0 |= 0x02; break;
			case StyleConstants.ALIGN_RIGHT:
					b0 |= 0x04; break;
			}

			if (bold)       b0 |= 0x08;
			if (italic)     b0 |= 0x10;
			if (underline)  b0 |= 0x20;

			if (size > 0)   b0 |= 0x40;
			if (color != null) b0 |= 0x80;

			bout[offset++] = b0;

			if (size > 0) {
				ByteUtil.writeShort(bout,offset, (short)size);
                offset += 2;
			}
			if (color!=null) {
				int rgb = color.getRGB();
				ByteUtil.writeInt(bout,offset, rgb);
                offset += 4;
			}

			ByteUtil.writeShort(bout,offset, (short)start);
			ByteUtil.writeShort(bout,offset+2, (short)length);
            return offset+4;
       }

        public int read(byte[] bin, int offset)
        {
            byte b0 = bin[offset++];

            if (EngUtil.anyOf(b0,0x02))
                align = StyleConstants.ALIGN_CENTER;
            else if (EngUtil.anyOf(b0,0x04))
                align = StyleConstants.ALIGN_RIGHT;

            bold = EngUtil.anyOf(b0,0x08);
            italic = EngUtil.anyOf(b0,0x10);
            underline = EngUtil.anyOf(b0,0x20);

            if (EngUtil.anyOf(b0,0x40))
            {
                size = ByteUtil.readShort(bin,offset);
                offset += 2;
            }

            if (EngUtil.anyOf(b0,0x80))
            {
                color = new Color(ByteUtil.readInt(bin,offset));
                offset += 4;
            }

            start = ByteUtil.readShort(bin,offset);
            length = ByteUtil.readShort(bin,offset+2);
            return offset+4;
        }

        /**
         * encode style run as string
         * @return
         */
        public String toString()
        {
            StringWriter sout = new StringWriter();
	        PrintWriter pout = new PrintWriter(sout);
            print(pout);
	        pout.close();
            return sout.toString();
        }

        /**
         * parse style run from string
         * @param buf
         * @param pp
         * @return
         */
        public boolean parse(char[] buf, ParsePosition pp, int length)
        {
            int offset=pp.getIndex();

            if (buf[offset]!='[') {
                pp.setErrorIndex(offset);
                return false; //  not recognized
            }
            boolean startset = false;

            while (offset < length)
                switch (buf[offset])
                {
                    case 'j': align = StyleConstants.ALIGN_CENTER; offset++; continue;
                    case 'r': align = StyleConstants.ALIGN_RIGHT; offset++; continue;

                    case 'b': bold = true; offset++; continue;
                    case 'i': italic = true; offset++; continue;
                    case 'u': underline = true; offset++; continue;
                    case 's':
                        pp.setIndex(offset+1);
                        size = (int)AWTUtil.parseInt(buf,pp,length);
                        offset = pp.getIndex();
                        continue;
                    case 'c':
                        pp.setIndex(offset+1);
                        color = new Color((int)AWTUtil.parseHex(buf,pp,length));
                        offset = pp.getIndex();
                        continue;
                    case ':':
                        pp.setIndex(offset+1);
                        if (!startset) {
                            start = (int)AWTUtil.parseInt(buf,pp,length);
                            startset = true;
                        }
                        else
                            length = (int)AWTUtil.parseInt(buf,pp,length);
                        offset = pp.getIndex();
                        continue;
                    default:
                        /* unexpected   */
                        pp.setIndex(offset);
                        pp.setErrorIndex(offset);
                        return false;
                    case ']':
                        pp.setIndex(offset+1);
                        pp.setErrorIndex(-1);
                        return true;
                }

            pp.setIndex(offset);
            pp.setErrorIndex(offset);
            return false;
        }



        public void setParagraphStyle(AttributeSet style)
        {
            align = StyleConstants.getAlignment(style);
        }

        public void setCharacterStyle(AttributeSet style)
        {
	        bold = style.isDefined(StyleConstants.Bold) && StyleConstants.isBold(style);
            italic = style.isDefined(StyleConstants.Italic) && StyleConstants.isItalic(style);
            underline = style.isDefined(StyleConstants.Underline) && StyleConstants.isUnderline(style);

	        if (style.isDefined(StyleConstants.FontSize))
		        size = StyleConstants.getFontSize(style);
	        else
	            size = 0;

	        if (style.isDefined(StyleConstants.Foreground))
                color = (Color)style.getAttribute(StyleConstants.Foreground);
	        else
		        color = null;
        }

        /**
         * create Style
         * @return
         */
        public AttributeSet getStyle()
        {
            AttributeSet r1 = getDefaultStyle();
            if (r1!=null) return r1;

            SimpleAttributeSet r2 = new SimpleAttributeSet();
            if (align!=StyleConstants.ALIGN_LEFT) StyleConstants.setAlignment(r2,align);
            if (bold) StyleConstants.setBold(r2,true);
            if (italic) StyleConstants.setItalic(r2,true);
            if (underline) StyleConstants.setUnderline(r2,true);
            if (size > 0) StyleConstants.setFontSize(r2,size);
            if (color!=null) StyleConstants.setForeground(r2,color);

            return r2;
        }

        protected AttributeSet getDefaultStyle()
        {
            if (color!=null) return null;
            if (size > 0) return null;

            if (bold && !italic && !underline && (align==StyleConstants.ALIGN_LEFT)) return StyleUtil.bold;
            if (!bold && italic && !underline && (align==StyleConstants.ALIGN_LEFT)) return StyleUtil.italic;
            if (!bold && !italic && underline && (align==StyleConstants.ALIGN_LEFT)) return StyleUtil.underline;

            if (!bold && !italic && !underline)
                switch (align) {
                case StyleConstants.ALIGN_CENTER:
                case StyleConstants.ALIGN_JUSTIFIED:
                        return StyleUtil.center;
                case StyleConstants.ALIGN_RIGHT:
                        return StyleUtil.right;
                }

            return null;    // indicates complex style
        }

        public void saxStart(JoContentHandler handler) throws SAXException
        {
            switch (align)
            {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    handler.startElement("center");
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    handler.startElement("right");
                    break;
            }

            if (size > 0 || color!=null)
            {
                AttributesImpl attr = new AttributesImpl();
                if (size > 0)
                    attr.addAttribute(null,null,"size",null,Integer.toString(size));
                if (color != null)
                    attr.addAttribute(null,null,"color",null,Integer.toHexString(color.getRGB()));

                handler.startElement("font",attr);
            }

            if (bold) handler.startElement("bold");
            if (italic) handler.startElement("italic");
            if (underline) handler.startElement("underline");
        }

        public void saxEnd(JoContentHandler handler) throws SAXException
        {
            if (underline) handler.endElement("underline");
            if (italic) handler.endElement("italic");
            if (bold) handler.endElement("bold");

            if (size > 0 || color!=null)
            {
                handler.endElement("font");
            }

            switch (align)
            {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    handler.endElement("center");
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    handler.endElement("right");
                    break;
            }
        }


        public void pgnStart(LinePrintWriter out)
        {
            switch (align)
            {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    out.print("<center>");
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    out.print("<right>");   //  pseudo-html ?!
                    break;
            }

            if (size > 0 || color!=null)
            {
                out.print("<font");
                if (size > 0) {
                    out.print(" size=");
                    out.print(Integer.toString(size));
                }
                if (color != null) {
                    out.print(" color=");
                    out.print(Integer.toHexString(color.getRGB()));
                }
                out.print(">");
            }

            if (bold) out.print("<b>");
            if (italic) out.print("<i>");
            if (underline) out.print("<u>");
        }

        public void pgnEnd(LinePrintWriter out)
        {
            if (underline) out.print("</u>");
            if (italic) out.print("</i>");
            if (bold) out.print("</b>");

            if (size > 0 || color!=null)
                out.print("</font>");

            switch (align)
            {
            case StyleConstants.ALIGN_CENTER:
            case StyleConstants.ALIGN_JUSTIFIED:
                    out.print("</center>");
                    break;
            case StyleConstants.ALIGN_RIGHT:
                    out.print("</right>");
                    break;
            }
        }

    }


    private static Comparator SORT_START = new Comparator()
    {
        public int compare(Object o1, Object o2) {
            Run a = (Run)o1;
            Run b = (Run)o2;
            if (a.start != b.start)
                return a.start-b.start;
            else
                return b.length-a.length;   //  longest run first
        }
    };

    private static Comparator SORT_END = new Comparator()
    {
        public int compare(Object o1, Object o2) {
            Run a = (Run)o1;
            Run b = (Run)o2;
            if (a.end() != b.end())
                return a.end()-b.end();
            else
                return b.length-a.length;   //  longest run first
        }
    };

    // ------------------------------------------------------------
    //  Inner Class
    // ------------------------------------------------------------

    protected abstract class RunIterator
    {
        /** total length of text    */
        protected int length;

        public RunIterator(int textLen)
        {
            length = textLen;
        }

        abstract public void begin(Run run, int pos) throws Exception;
        abstract public void content(int start, int length) throws Exception;
        abstract public void end(Run run, int pos) throws Exception;

        public void process() throws Exception {
            //  traverse start and end points
            Iterator is = runs_start.iterator();
            Iterator ie = runs_end.iterator();

            Run rs = is.hasNext() ? (Run)is.next():null;
            Run re = ie.hasNext() ? (Run)ie.next():null;

            int ptext = 0;

            while (rs!=null || re!=null)
            {
                //  find next break point
                if (rs==null || rs.start>=re.end())
                {
                     //  it's re.end()
                     if (re.end() > ptext) {
                         content(ptext,re.end()-ptext);
                         ptext = re.end();
                     }
                     end(re, re.end());
                     re = ie.hasNext() ? (Run)ie.next():null;
                 }
                 else
                 {
                     //  it's rs.start
                     if (rs.start > ptext) {
                         content(ptext,rs.start-ptext);
                         ptext = rs.start;
                     }
                     begin(rs,rs.start);
                     rs = is.hasNext() ? (Run)is.next():null;
                 }
             }

             if (ptext < length)
                 content(ptext,length-ptext);
         }
    }

     class PgnRunIterator extends RunIterator
     {
         public StringBuffer text;
         public LinePrintWriter out;
         public boolean markup;

         public PgnRunIterator(StringBuffer text, LinePrintWriter out)
         {
             super(text.length());
             this.text = text;
             this.out = out;
             this.markup = false;
         }

         public void begin(Run run, int pos)         { run.pgnStart(out); markup=true; }

         public void content(int start, int length)  { markup = pgnText(text,start,length,out); }

         public void end(Run run, int pos)           { run.pgnEnd(out); markup=true; }

         public void process()
         {
             try {
                 super.process();
             } catch (Exception e) {
                 // not thrown at all
             }
         }
    };

    class SaxRunIterator extends RunIterator
    {
          public StringBuffer text;
          public JoContentHandler handler;

          public SaxRunIterator(StringBuffer text, JoContentHandler handler)
          {
              super(text.length());
              this.text = text;
              this.handler = handler;
          }

          public void begin(Run run, int pos) throws SAXException         { run.saxStart(handler); }

          public void content(int start, int length) throws SAXException  { saxText(text,start,length,handler); }

          public void end(Run run, int pos) throws SAXException           { run.saxEnd(handler); }

          public void process() throws SAXException
          {
              try {
                  super.process();
              } catch (Exception e) {
                  throw (SAXException)e;
              }
          }
    }

    // ------------------------------------------------------------
    //  Fields
    // ------------------------------------------------------------

    /** list of runs; sorted by start points (ascending)
     *  ArrayList<Run>
     * */
    protected SortedSet runs_start;
    /** list of runs; sorted by end points (ascending)
     * */
    protected SortedSet runs_end;

    // ------------------------------------------------------------
    //  Constructor
    // ------------------------------------------------------------

	public StyleRun()
	{
		runs_start = new TreeSet(SORT_START);
        runs_end = new TreeSet(SORT_END);
	}

    public int size()           { return runs_start.size(); }
    public boolean isEmpty()    { return runs_start.isEmpty(); }

    public void add(Run run)
    {
        runs_start.add(run);
        runs_end.add(run);
    }

    public StyleRun(StyledDocument doc, int from, int len, AttributeSet baseStyle)
    {
	   this();
       //   examine styles in doc
       int end = from+len;
        /*  1: get paragraph styles */
        for (int i=from; i < end; )
        {
            Element pelm = doc.getParagraphElement(i);
            AttributeSet pstyle = pelm.getAttributes();
            if (hasParagraphAttributes(pstyle))
            {
                int rstart = Math.max(pelm.getStartOffset(),from);
                int rend = Math.min(pelm.getEndOffset(),end);
                Run run = new Run(rstart-from,rend-rstart);
                run.setParagraphStyle(pstyle);
                add(run);
            }
	        i = pelm.getEndOffset();
        }
        /*  2: get character styles */
        for (int i=from; i < end; )
        {
            Element pelm = doc.getCharacterElement(i);
            AttributeSet pstyle = pelm.getAttributes();
            if (StyleUtil.hasDifferentCharacterAttributes(pstyle,baseStyle))
            {
                int rstart = Math.max(pelm.getStartOffset(),from);
                int rend = Math.min(pelm.getEndOffset(),end);
                Run run = new Run(rstart-from,rend-rstart);
                run.setCharacterStyle(pstyle);
                add(run);
            }
	        i = pelm.getEndOffset();
        }
    }


	public void apply(StyledDocument doc, int offset)
	{
		for (Iterator i=runs_start.iterator(); i.hasNext(); )
		{
			Run run = (Run)i.next();
			AttributeSet style = run.getStyle();

            if (run.hasParagraphAttributes())
			    doc.setParagraphAttributes(offset+run.start, run.length, style, false);
            if (run.hasCharacterAttributes())
                doc.setCharacterAttributes(offset+run.start, run.length, style, false);
		}
	}


    /**
     * print to text writer, for PGN export
     */
    public void print(PrintWriter out)
    {
        for (Iterator i=runs_start.iterator(); i.hasNext(); )
        {
            Run run = (Run)i.next();
            run.print(out);
        }
   }

    public String toString()
    {
        StringWriter sout = new StringWriter();
        PrintWriter pout = new PrintWriter(sout);
        print(pout);
        pout.close();
        return sout.toString();
    }

    /**
     * print to binary
     */
    public void write(ByteArrayOutputStream bout) {
        for (Iterator i=runs_start.iterator(); i.hasNext(); )
        {
            Run run = (Run)i.next();
            run.write(bout);
        }
        //  mark end
	    bout.write((byte)0);
    }

    public int write(byte[] bout, int offset)
    {
        for (Iterator i=runs_start.iterator(); i.hasNext(); )
        {
             Run run = (Run)i.next();
             offset = run.write(bout,offset);
        }
        //  mark end
        bout[offset++] = 0;
        return offset;
     }

    /**
     * read from string
     */
    public boolean parse(char[] line, int start, int length)
    {
        ParsePosition pp = new ParsePosition(start);
        int end = start+length;

        while (pp.getIndex() < end && pp.getErrorIndex() < 0)
        {
            Run run = new Run(0,0);
            run.parse(line,pp,length);

            if (pp.getErrorIndex() >= 0) return false;

            add(run);
        }
        return true;
    }
    
    /**
     * read from binary
     */
    public int read(byte[] input, int offset)
    {
        while (input[offset] != 0)
        {
            Run run = new Run(0,0);
            offset = run.read(input,offset);
            add(run);
        }
        return offset+1;    //  skip end marker
    }

    /**
     * get paragraph style at one location
     * @param doc
     * @param pos
     * @return
     */
    public static Style getParagraphStyleAt(StyledDocument doc, int pos)
    {
        Element celm = doc.getParagraphElement(pos);
        if (celm!=null)
            return (Style)celm.getAttributes();
        else
            return null;
    }

    public static boolean hasParagraphAttributes(AttributeSet style)
    {
        return StyleConstants.getAlignment(style) != StyleConstants.ALIGN_LEFT;
    }

    public void toSAX(final StringBuffer text, final JoContentHandler handler) throws SAXException
    {
        SaxRunIterator ri = new SaxRunIterator(text,handler);
        ri.process();
    }


    public boolean toPGN(final StringBuffer text, final LinePrintWriter out)
    {
        PgnRunIterator ri = new PgnRunIterator(text,out);
        ri.process();
        return ri.markup;
    }

    public static void saxText(StringBuffer text, JoContentHandler handler) throws SAXException
    {
        saxText(text,0,text.length(), handler);
    }

    public static void saxText(StringBuffer text, int start, int length, JoContentHandler handler) throws SAXException
    {
        //  look for line breaks
        int p0 = start;
        int end = start+length;

        for (int p1=start; p1 < end; p1++)
            if (text.charAt(p1)=='\n')
            {
                if (p1 > p0) {
                    handler.characters(text,p0,p1-p0);
                    p0 = p1+1;  //  skip \n
                }
                handler.element("br",null);
            }

        if (p0 < end)
            handler.characters(text,p0,end-p0);
    }

    public static boolean pgnText(StringBuffer text, int start, int length, LinePrintWriter out)
    {
        //  TODO efficiency ?!
        boolean markup = false;
        for (int i=start; i < (start+length); i++)
        {
            char c = text.charAt(i);
            switch (c)
            {
            case '\n':  out.print("<br>"); markup=true; out.breakIf(80); break;  //  intentional line break
            case '{':   out.print("("); break;  //  not allowed within pgn comments!
            case '}':   out.print("}"); break;  //  not allowed within pgn comments!
            case '<':   out.print("&lt;"); markup=true; break;
            case '>':   out.print("&gt;"); markup=true; break;
            case '&':   out.print("&amp;"); markup=true; break;
            case '\t':
            case ' ':   out.print(' '); out.breakIf(80); break;
            default:    out.print(c); break;
            }
        }
        return markup;
    }

  }