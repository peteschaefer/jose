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

package de.jose.pgn;

import de.jose.Language;
import de.jose.util.StringUtil;
import de.jose.util.map.ObjIntMap;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PgnUtil
		implements PgnConstants
{
	public static Class getDataType(String tag)
	{
		if (tag.equalsIgnoreCase(TAG_DATE) ||
			tag.equalsIgnoreCase(TAG_EVENT_DATE))
			return PgnDate.class;
		if (tag.equalsIgnoreCase(TAG_WHITE_ELO) ||
			tag.equalsIgnoreCase(TAG_BLACK_ELO) ||
			tag.equalsIgnoreCase(TAG_PLY_COUNT))
			return Integer.class;
        if (tag.equalsIgnoreCase(TAG_FEN))
            return de.jose.chess.Board.class;
		//	all other use strings
		return String.class;
	}

	public static Object normalizeValue(Object value, String type)
		throws ParseException
	{
		return normalizeValue(value,getDataType(type));
	}

	public static boolean isSymbolicNag(int nag)
	{
		if (nag<=0) return false;

		String tip = Language.get("pgn.nag."+nag+".tip",null);
		return (tip==null);	//	text is already the textual representation
	}


	public static Object normalizeValue(Object value, Class type)
		throws ParseException
	{
		if (value==null) return null;	//	easy
		if (type==PgnDate.class)
			return toPgnDate(value);
		if (type==Integer.class)
			return toInteger(value);
		if (type==String.class)
			return toString(value);
        if (type==de.jose.chess.Board.class)
            return toString(value); //  FEN
		throw new IllegalArgumentException(type.getName());
	}

	public static Object toPgnDate(Object value)
			throws ParseException
	{
		if (value==null) return null;	//	easy
		if (value instanceof PgnDate)
			return (PgnDate)value;
		if (value instanceof Date)
			return new PgnDate((Date)value);
		if (value instanceof Number)
			return new PgnDate(((Number)value).longValue());

		String svalue = toString(value);
		if (svalue==null)
			return null;
		else
			return PgnDate.parseDate(svalue);
	}

	public static Integer toInteger(Object value)
	{
		if (value==null) return null;	//	easy
		if (value instanceof Integer)
			return (Integer)value;
		if (value instanceof Number)
			return ((Number)value).intValue();

		String svalue = toString(value);
		if (svalue==null)
			return null;
		else
			return Integer.valueOf(svalue);
	}

	public static String toString(Object value)
	{
		if (value==null) return null;	//	easy

		String svalue = value.toString();
		if (svalue==null) return null;
		svalue = svalue.trim();
		if (svalue.length()==0) return null;
		return svalue;
	}


	public static Date currentDate() {
		long time = System.currentTimeMillis();
		time -= (time%1000);
		return new Date(time);
	}
	
	public static byte parseResult(String s)
	{
		if (s==null) return RESULT_UNKNOWN;
		if (StringUtil.startsWithIgnoreCase(STRING_UNKNOWN,s))		return RESULT_UNKNOWN;
		if (StringUtil.startsWithIgnoreCase(STRING_BLACK_WINS,s))	return BLACK_WINS;
		if (StringUtil.startsWithIgnoreCase(STRING_DRAW,s))			return DRAW;
		if (StringUtil.startsWithIgnoreCase(STRING_WHITE_WINS,s))	return WHITE_WINS;
		return RESULT_UNKNOWN;
	}
	
	public static byte parseResult(char[] s, int offset, int len)
	{
		if (s==null) return RESULT_UNKNOWN;
		if (StringUtil.startsWithIgnoreCase(STRING_UNKNOWN,s,offset,len))		return RESULT_UNKNOWN;
		if (StringUtil.startsWithIgnoreCase(STRING_BLACK_WINS,s,offset,len))	return BLACK_WINS;
		if (StringUtil.startsWithIgnoreCase(STRING_DRAW,s,offset,len))			return DRAW;
		if (StringUtil.startsWithIgnoreCase(STRING_WHITE_WINS,s,offset,len))	return WHITE_WINS;
		return RESULT_UNKNOWN;
	}
	
	public static final String resultString(int result)
	{
		switch (result) {
		case PgnConstants.RESULT_UNKNOWN:	return "*";
		case PgnConstants.WHITE_WINS:		return "1-0";
		case PgnConstants.BLACK_WINS:		return "0-1";
		case PgnConstants.DRAW:				return "1/2";
		default:                            return "?"+result;
				//	throw new IllegalStateException();
		}
	}

	/**	maps language codes to map (string <-> code)	*/
	protected static Map annotationMap = new HashMap();

	protected static ObjIntMap createAnnotationMap(Language language)
	{
		ObjIntMap map = new ObjIntMap(2*NAG_MAX,0.6f);
		annotationMap.put(language.langCode,map);

		for (int i=0; i<=NAG_MAX; i++)
			map.put(annotationString(i,language),i);

		return map;
	}

    public static final String annotationString(int code, Language language)
	{
        return language.get1("pgn.nag."+code,null);
    }

	public static final String annotationTip(int code, Language language)
	{
		return language.get1("pgn.nag."+code+".tip",null);
	}

	public static final String annotationString(int code)
	{
		return annotationString(code, Language.theLanguage);
	}

	public static final String annotationTip(int code)
	{
		return annotationTip(code, Language.theLanguage);
	}

	public static final int annotationCode(String text, Language language)
	{
		if (text.startsWith("$")) {
			text = text.substring(1);
			if (text.length()==0)
				return 0;
			else if (StringUtil.isInteger(text))
				return Integer.parseInt(text);
			else
				return -1;
		}
		if (text.length()==0)
			return -1;
		//	else
		ObjIntMap map = (ObjIntMap)annotationMap.get(language.langCode);
		if (map==null)
			map = createAnnotationMap(language);

		int result = map.getInt(text);
		if (result==ObjIntMap.NOT_FOUND)
			return -1;
		else
			return result;
	}

	public static final int annotationCode(String text)
	{
		return annotationCode(text, Language.theLanguage);
	}

    public static int readText(StringBuffer buffer, byte[] bin, int offset)
    {
        byte b1,b2,b3;
	    char c=0;

        for(;;) {
            b1 = bin[offset++];

            if (b1==0) break;    //  marks end if string

            if (b1 > 0)
                c = (char)b1;
            else {
                //  UTF-8 encoded
                switch (b1 & 0xe0)
                {
                case 0xc0:  //  two-byte
                    b2 = bin[offset++];
                    c = (char)((((char)b1 & 0x1f) << 6) | (b2 & 0x3f));
                    break;
                case 0xe0:  //  three byte
                    b2 = bin[offset++];
                    b3 = bin[offset++];
                    c = (char)(((char)b1 & 0x0f) << 12 | ((char)b2 & 0x3f) << 6 | (b3 & 0x3f));
                    break;
                }
            }

	        buffer.append(c);
        }
        return offset;
    }

}
