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

package de.jose.profile;

import de.jose.Application;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.util.*;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;


/**
 * stores font encoding information
 * (indices of chess characters)
 */

public class FontEncoding
		implements Constants
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------
	
	protected static final int WHITE_ON_LIGHT	= 0;
	protected static final int BLACK_ON_LIGHT	= 1;
	protected static final int WHITE_ON_DARK	= 2;
	protected static final int BLACK_ON_DARK	= 3;
	protected static final int WHITE_FIGURINES	= 4;
	protected static final int BLACK_FIGURINES	= 5;
	protected static final int SYMBOLS			= 6;
	protected static final int BORDER			= 7;
	protected static final int BORDER_DOUBLE    = 8;

	protected static final int MAX				= 9;
	protected static final int MAX_SYMBOL		= 255;
	
	/**	border symbols	 */
	public static final int BORDER_TOP_LEFT		= 0;
	public static final int BORDER_TOP			= 1;
	public static final int BORDER_TOP_RIGHT	= 2;
	public static final int BORDER_LEFT			= 3;
	public static final int BORDER_RIGHT		= 4;
	public static final int BORDER_BOTTOM_LEFT	= 5;
	public static final int BORDER_BOTTOM		= 6;
	public static final int BORDER_BOTTOM_RIGHT	= 7;

	public static final int BORDER_LEFT_1		= 8;
	//	...
	public static final int BORDER_LEFT_8		= 15;
	public static final int BORDER_BOTTOM_A		= 16;
	//	...
	public static final int BORDER_BOTTOM_H		= 23;

	public static final int MAX_BORDER			= 24;

	protected static int[] EDGE_OFFSETS	= { BORDER_TOP_LEFT, BORDER_TOP_RIGHT, BORDER_BOTTOM_LEFT, BORDER_BOTTOM_RIGHT };

	protected static int[] LENS =  { KING+1, KING+1, KING+1, KING+1, KING+1, KING+1, MAX_SYMBOL, MAX_BORDER, MAX_BORDER };
	
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	maps symbol characters to ASCII codes	*/
	protected String[][] chars;
	/**	does the font contain normal text letters ?	*/
	protected boolean textLetters;

	/** maps fonts families to FontEncoding records */
	protected static HashMap fontMap = new HashMap();

	/** set when the XML config has been initialized */
	protected static boolean inited = false;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	private FontEncoding()		{	chars = new String[MAX][];	}
	
	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public final boolean hasDiagram()				{ return chars[WHITE_ON_LIGHT] != null &&
															 chars[BLACK_ON_LIGHT] != null; }
	
	public final boolean hasDarkDiagram()			{ return chars[WHITE_ON_DARK] != null && 
															 chars[BLACK_ON_DARK] != null; }

	public final boolean hasFigurines()				{ return chars[WHITE_FIGURINES] != null; }
	public final boolean hasBlackFigurines()		{ return chars[BLACK_FIGURINES] != null; }

	public final boolean hasSymbols()				{ return chars[SYMBOLS] != null; }
	public final boolean hasBorder()				{ return chars[BORDER] != null; }

	public final boolean hasText()					{ return textLetters; }

	public final String get(int piece, int background)
	{
		int color = EngUtil.colorOf(piece);
		int uncolored = EngUtil.uncolored(piece);
		
		for (;;) {
			int idx = indexOf(color,background);
		
			if (chars[idx]!=null && chars[idx][uncolored]!=null)
				return chars[idx][uncolored];
			
			//	else: fall-back
			switch (background) {
			case LIGHT_SQUARE:
				switch (color) {
				default:		return null;
				case BLACK:		color = WHITE; break;
				}
				break;
			case DARK_SQUARE:
				background = LIGHT_SQUARE; break;
			case FIGURINE:
				switch (color) {
				default:		background = LIGHT_SQUARE; break;
				case BLACK:		color = WHITE; break;
				}
				break;
			}
		}
	}
		
	public final String get(int piece) {
		return get(piece,LIGHT_SQUARE);											
	}		
		
	public final String getFigurine(int piece) {
		return get(piece,FIGURINE);											
	}		
	
	public final String getSymbol(int nagCode) {
		if (chars[SYMBOLS]!=null)
			return chars[SYMBOLS][nagCode];
		else
			return null;
	}

	public final String getBorder(boolean doubleBorder, int idx) {
		int offset = doubleBorder ? BORDER_DOUBLE:BORDER;
		if (chars[offset]==null) offset = BORDER;
		if (chars[offset]==null) offset = BORDER_DOUBLE;

		if (chars[offset]!=null)
			return chars[offset][idx];
		else
			return null;	
	}
		
	
	//-------------------------------------------------------------------------------
	//	Static Methods
	//-------------------------------------------------------------------------------
	
	public static final FontEncoding getEncoding(String fontName)
	{
		if (!inited) config();
		return (FontEncoding)fontMap.get(fontName);
	}
	
	public static final String get(String fontName, int piece, int background)
	{
		FontEncoding enc = getEncoding(fontName);
		if (enc!=null)
			return enc.get(piece,background);
		else
			return null;
	}
	
	public static final String get(String fontName, int piece)
	{
		FontEncoding enc = getEncoding(fontName);
		if (enc!=null)
			return enc.get(piece);
		else
			return null;
	}
	
	public static final String getFigurine(String fontName, int piece)
	{
		FontEncoding enc = getEncoding(fontName);
		if (enc!=null)
			return enc.getFigurine(piece);
		else
			return null;
	}
	
	public static final String getSymbol(String fontName, int nagCode)
	{
		FontEncoding enc = getEncoding(fontName);
		if (enc!=null)
			return enc.getSymbol(nagCode);
		else
			return null;
	}

	/**
	 * @return a list of all fonts that are not diagram or symbol fonts
	 */
	public static List getTextFonts()
	{
		if (!inited) config();

		Vector vresult = new Vector();
		Iterator installed = FontUtil.getInstalledFonts(true).keySet().iterator();
		while (installed.hasNext())
		{
			String family = (String)installed.next();
			FontEncoding enc = getEncoding(family);
			if (enc==null || enc.hasText())
				vresult.add(family);
		}

		return vresult;
	}

	/**
	 * from a list of fonts, get the first one that is installed
	 */
	public static String firstOf(String fontList)
	{
		Map installed = FontUtil.getInstalledFonts(true);
		StringTokenizer tok = new StringTokenizer(fontList,",");
		while (tok.hasMoreTokens()) {
			String family = tok.nextToken().trim();
			if (installed.containsKey(family))
				return family;
		}
		return null;
	}

	/**
	 * @param installed if true list only fonts that are actually available,
	 * 	if false list all fonts that are defined in config/fonts.xml
	 * @return a list of all diagram fonts
	 */
	public static List getDiagramFonts(boolean installed)
	{
		if (!inited) config();
		Vector vresult = new Vector();
		Iterator i = fontMap.keySet().iterator();
		while (i.hasNext()) {
			String fontName = (String)i.next();
			FontEncoding enc = getEncoding(fontName);
			if (enc.hasDiagram()) {
				if (installed==false || FontUtil.isInstalled(fontName,Font.PLAIN))
					vresult.add(fontName);
			}
		}
		
		return vresult;
	}
	
	/**
	 * @param installed if true list only fonts that are actually available,
	 * 	if false list all fonts that are defined in config/fonts.xml
	 * @return a list of all figurine fonts
	 */
	public static List getFigurineFonts(boolean installed)
	{
		if (!inited) config();
		Vector vresult = new Vector();
		Iterator i = fontMap.keySet().iterator();
		while (i.hasNext()) {
			String fontName = (String)i.next();
			FontEncoding enc = getEncoding(fontName);
			if (enc.hasFigurines()) {
				if (installed==false || FontUtil.isInstalled(fontName,Font.PLAIN))
					vresult.add(fontName);
			}
		}
		
		return vresult;
	}
	
	/**
	 * @param installed if true list only fonts that are actually available,
	 * 	if false list all fonts that are defined in config/fonts.xml
	 * @return a list of all symbold fonts
	 */
	public static List getSymbolFonts(boolean installed)
	{
		if (!inited) config();
		Vector vresult = new Vector();
		Iterator i = fontMap.keySet().iterator();
		while (i.hasNext()) {
			String fontName = (String)i.next();
			FontEncoding enc = getEncoding(fontName);
			if (enc.hasSymbols()) {
				if (installed==false || FontUtil.isInstalled(fontName,Font.PLAIN))
					vresult.add(fontName);
			}
		}
		
		return vresult;
	}
	/**
	 * @param installed if true list only fonts that are actually available,
	 * 	if false list all fonts that are defined in config/fonts.xml
	 * @return a list of all inline-diagram fonts
	 */
	public static List getInlineFonts(boolean installed)
	{
		if (!inited) config();
		Vector vresult = new Vector();
		Iterator i = fontMap.keySet().iterator();
		while (i.hasNext()) {
			String fontName = (String)i.next();
			FontEncoding enc = getEncoding(fontName);
			if (enc.hasDiagram() && enc.hasDarkDiagram()) {
				if (installed==false || FontUtil.isInstalled(fontName,Font.PLAIN))
					vresult.add(fontName);
			}
		}
		
		return vresult;
	}

    /**
     * get a String that can be used for displaying a sample of a given String
     */
    public String getFigurineSampleString()
    {
        if (hasFigurines()) {
            StringBuffer sample = new StringBuffer();
            for (int pc = KING; pc >= PAWN; pc--)
                sample.append(getFigurine(pc+WHITE));
            return sample.toString();
        }
        else
            return null;
    }

    /**
     * get a String that can be used for displaying a sample of a given String
     */
    public String getDiagramSampleString(boolean mixedColors)
    {
        if (hasDiagram()) {
            StringBuffer sample = new StringBuffer();
            for (int pc = KING; pc >= PAWN; pc--)
            {
                if (mixedColors)
                    sample.append(get(pc + ((pc%2==0) ? WHITE:BLACK), LIGHT_SQUARE));
                else
                    sample.append(get(pc+WHITE));
            }
            return sample.toString();
        }
        else
            return null;
    }

	protected static final int indexOf(int color, int background)
	{
		switch (background) {
		default:
			switch (color) {
			default:	return WHITE_ON_LIGHT;
			case BLACK:	return BLACK_ON_LIGHT;
			}
				
		case DARK_SQUARE:
			switch (color) {
			default:	return WHITE_ON_DARK;
			case BLACK:	return BLACK_ON_DARK;
			}
				
		case FIGURINE:
			switch (color) {
			default:	return WHITE_FIGURINES;
			case BLACK:	return BLACK_FIGURINES;
			}
		}
	}
	
	//-------------------------------------------------------------------------------
	//	Parse Methods
	//-------------------------------------------------------------------------------

	public static void config()
	{
		config(Application.theApplication.theConfig.enumerateElements("FONT_ENCODING"));
	}

	public static void config(Enumeration en)
	{
		while (en.hasMoreElements()) {
			FontEncoding fe = new FontEncoding();
			fe.config((Element)en.nextElement());
		}
		inited = true;
	}

	protected void config(Element elm)
	{
		NodeList fonts = elm.getElementsByTagName("FONT");
		for (int i=0; i<fonts.getLength(); i++)
			configFont((Element)fonts.item(i));

		NodeList chars = elm.getElementsByTagName("CHARS");
		for (int i=0; i<chars.getLength(); i++)
			configChars((Element)chars.item(i));

        NodeList extraChars = elm.getElementsByTagName("CHAR");
        for (int i=0; i<extraChars.getLength(); i++)
            configExtraChars((Element)extraChars.item(i));

        NodeList frames = elm.getElementsByTagName("FRAME");
        for (int i=0; i<frames.getLength(); i++)
            configFrame((Element)frames.item(i));

        NodeList syms = elm.getElementsByTagName("SYM");
        for (int i=0; i<syms.getLength(); i++)
            configSymbol((Element)syms.item(i));
	}
	
	protected void configFont(Element elm)
	{
		fontMap.put(elm.getAttribute("name"), this);
	}
	
	protected void configChars(Element elm)
	{
		String colorAttr = elm.getAttribute("color");
		int color;
		if (colorAttr==null|| colorAttr.length()==0  || colorAttr.equalsIgnoreCase("white"))
			color = WHITE;
		else if (colorAttr.equalsIgnoreCase("black"))
			color = BLACK;
		else
			throw new DOMException(DOMException.SYNTAX_ERR, 
							"unexpected attribute value: "+colorAttr);
				
		String bgAttr = elm.getAttribute("bg");
		int background;
		if (bgAttr==null || bgAttr.length()==0 || bgAttr.equalsIgnoreCase("light"))
			background = LIGHT_SQUARE;
		else if (bgAttr.equalsIgnoreCase("dark"))
			background = DARK_SQUARE;
		else if (bgAttr.equalsIgnoreCase("fig"))
			background = FIGURINE;
		else
			throw new DOMException(DOMException.SYNTAX_ERR, 
							"unexpected attribute value: "+bgAttr);
				
		String separator = elm.getAttribute("sep");

		String letter = elm.getAttribute("letter");
		if (letter!=null && letter.equalsIgnoreCase("true"))
			textLetters = true;

        String orientAttr = elm.getAttribute("orientation");
        int orientation;
        if (orientAttr==null|| orientAttr.length()==0  || orientAttr.equalsIgnoreCase("up"))
            orientation = 0;
        else if (orientAttr.equalsIgnoreCase("right"))
            orientation = 1;
        else if (orientAttr.equalsIgnoreCase("down"))
            orientation = 2;
        else if (orientAttr.equalsIgnoreCase("left"))
            orientation = 3;
        else
            throw new DOMException(DOMException.SYNTAX_ERR,
                            "unexpected attribute value: "+orientAttr);

		String elmText = XMLUtil.getTextValue(elm);
		if (elmText!=null) {
			StringBuffer text = new StringBuffer(elmText);
			StringUtil.trim(text, StringUtil.TRIM_BOTH);
			StringUtil.unescape(text);

			int idx = indexOf(color,background);
			Vector values;
			if (separator!=null && separator.length() > 0)
				values = StringUtil.separate(text, separator.charAt(0));
			else
				values = StringUtil.separate(text);

			if (values.size() < LENS[idx])	//	first char may be omitted
				values.add(0,null);

			set(idx,orientation,values);
		}
	}

    protected void configSymbol(Element elm)
    {
        String nagCodes  = elm.getAttribute("nag");
        if (nagCodes==null || nagCodes.length()==0) return; //  can't map to nags ;-(
        int[] nags = StringUtil.parseIntList(nagCodes);

        String elmText = XMLUtil.getTextValue(elm);
        if (elmText==null) return;
        elmText = elmText.trim();
        if (elmText.length()==0) return;  //  empty value ?!?

        StringBuffer value = new StringBuffer(elmText);
        StringUtil.unescape(value);

        for (int i=0; i<nags.length; i++)
            setSymbol(nags[i],value.toString());
    }

    protected void configExtraChars(Element elm)
    {
        //  TODO
    }

    protected void configFrame(Element elm)
    {
		StringBuffer text = new StringBuffer(XMLUtil.getTextValue(elm));
		StringUtil.unescape(text);
		String part = elm.getAttribute("part");
		String type = elm.getAttribute("type");
	    boolean isDouble = type.indexOf("double") >= 0;

		if ("rows".equalsIgnoreCase(part)) {
			int offset = BORDER_LEFT_1;
			for (int i=0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!Character.isWhitespace(c))
					setBorder(isDouble,offset++,String.valueOf(c));
			}
		} else if ("files".equalsIgnoreCase(part)) {
			int offset = BORDER_BOTTOM_A;
			for (int i=0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!Character.isWhitespace(c))
					setBorder(isDouble,offset++,String.valueOf(c));
			}
		} else if ("edges".equalsIgnoreCase(part)) {
			int ioffset = 0;
			for (int i=0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!Character.isWhitespace(c))
					setBorder(isDouble,EDGE_OFFSETS[ioffset++],String.valueOf(c));
			}
		}
		else {
			//	<FRAME type="single"> 123 45 789 </FRAME>
			int offset = BORDER_TOP_LEFT;
			for (int i=0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!Character.isWhitespace(c))
					setBorder(isDouble,offset++,String.valueOf(c));
			}
		}
    }

	protected void set(int color, int background, int piece, String value)
	{
		set (indexOf(color,background), EngUtil.uncolored(piece), value);
	}
	
	protected void set(int idx, int offset, String value)
	{
		if (chars[idx]==null)
			chars[idx] = new String[LENS[idx]];
		chars[idx][offset] = value;
	}
	
	protected void setSymbol(int offset, String value)
	{
		set(SYMBOLS,offset,value);
	}
	
	protected void setBorder(boolean doubleBorder, int offset, String value)
	{
		set(doubleBorder ? BORDER_DOUBLE:BORDER,offset,value);
	}
	
	protected void set(int idx, int orientation, Vector values)
	{
        switch (orientation) {
        case 0:
                if (chars[idx]==null)
                    chars[idx] = new String[LENS[idx]];
                for (int i=0; i<LENS[idx] && i<values.size(); i++)
                    chars[idx][i] = (String)values.get(i);
                break;
        default:
                //  TODO
                break;
        }
	}

    public static void main(String[] args)
    {
        //  scan system fonts for Unicode figurines
        //  code points: 2654 - 2659 (king,queen,rook,bishop,knight,pawn)
        //  265A - 265F (black figurines)
        String[] sysfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        char[] code_points = {
            0x2659, 0x2658, 0x2657, 0x2656, 0x2655, 0x2654,
            0x265f, 0x265e, 0x265d, 0x265c, 0x265b, 0x265a,
        };
        String[] code_names = {
            "white_pawn", "white_knight", "white_bishop", "white_rook", "white_queen", "white_king",
            "black_pawn", "black_knight", "black_bishop", "black_rook", "black_queen", "black_king",
        };

        for (int i=0; i < sysfonts.length; i++)
        {
            Font font = new Font(sysfonts[i],Font.PLAIN,12);

            int bits = 0;
            for (int p=0; p < code_points.length; p++)
                if (font.canDisplay(code_points[p]))
                    bits |= (1<<p);
	            /** FontEncoding.canDisplay(int) since 1.5 !! */

            if (bits!=0)
            {
                System.out.print("[");
                System.out.print(sysfonts[i]);
                System.out.print(": ");
                for (int p=0; p < code_names.length; p++)
                    if ((bits & (1<<p)) != 0) {
                        System.out.print(code_names[p]);
                        System.out.print(" ");
                    }
                System.out.println("]");
            }
        }
    }

}
