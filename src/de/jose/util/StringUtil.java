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

import de.jose.Util;
import de.jose.util.map.IntHashSet;

import java.text.DecimalFormat;
import java.util.*;

public class StringUtil
{
	public static final int length(Object string)
	{
		if (string instanceof String)
			return ((String)string).length();
		if (string instanceof StringBuffer)
			return ((StringBuffer)string).length();
		throw new IllegalArgumentException("expecting String or StringBuffer");
	}
	
	/**
	 * similar to the String.regionMatches but works on both String and StringBuffer
	 */
	public static final boolean regionMatches(Object string,
								 boolean ignoreCase, int toffset, Object other, 
								 int ooffset, int len) 
	{
		if (string instanceof String) {
			if (other instanceof String)
				return ((String)string).regionMatches(ignoreCase,toffset, (String)other, ooffset,len);
			if (other instanceof StringBuffer) {
				if (ignoreCase)
					return regionMatchesIgnoreCase((String)string,toffset, (StringBuffer)other, ooffset,len);
				else
					return regionMatches((String)string,toffset, (StringBuffer)other, ooffset,len);
			}
		}
		if (string instanceof StringBuffer) {
			if (other instanceof String) {
				if (ignoreCase)
					return regionMatchesIgnoreCase((StringBuffer)string, toffset, (String)other, ooffset, len);
				else
					return regionMatches((StringBuffer)string, toffset, (String)other, ooffset, len);
			}
			if (other instanceof StringBuffer) {
				if (ignoreCase)
					return regionMatchesIgnoreCase((StringBuffer)string, toffset, (StringBuffer)other, ooffset, len);
				else
					return regionMatches((StringBuffer)string, toffset, (StringBuffer)other, ooffset, len);
			}
		}
        if (string instanceof char[]) {
            if (other instanceof String) {
                if (ignoreCase)
                    return regionMatchesIgnoreCase((char[])string, toffset, (String)other, ooffset, len);
                else
                    return regionMatches((char[])string, toffset, (String)other, ooffset, len);
            }
            else
                throw new IllegalArgumentException("expecting String or StringBuffer");
        }
		throw new IllegalArgumentException("expecting String or StringBuffer");
	}

    private static boolean regionMatchesIgnoreCase(char[] string, int toffset, String other, int ooffset, int len)
    {
        if (toffset<0 || (toffset+len) > string.length)
            return false;
        if (ooffset<0 || (ooffset+len) > other.length())
            return false;

        while (len-- > 0) {
            if (Character.toUpperCase(string[toffset++]) != Character.toUpperCase(other.charAt(ooffset++)))
                return false;
        }

        return true;
    }


    public static final boolean regionMatches(char[] string, int toffset, String other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length)
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;

		while (len-- > 0) {
			if (string[toffset++] != other.charAt(ooffset++))
				return false;
		}

		return true;
	}

    public static final boolean regionMatches(String string, int toffset, StringBuffer other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (string.charAt(toffset++) != other.charAt(ooffset++))
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatchesIgnoreCase(String string, int toffset, StringBuffer other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (Character.toUpperCase(string.charAt(toffset++)) != Character.toUpperCase(other.charAt(ooffset++)))
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatches(StringBuffer string, int toffset, String other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (string.charAt(toffset++) != other.charAt(ooffset++))
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatchesIgnoreCase(StringBuffer string, int toffset, String other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (Character.toUpperCase(string.charAt(toffset++)) != Character.toUpperCase(other.charAt(ooffset++)))
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatches(StringBuffer string, int toffset, StringBuffer other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (string.charAt(toffset++) != other.charAt(ooffset++))
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatchesIgnoreCase(StringBuffer string, int toffset, StringBuffer other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length())
			return false;
		
		while (len-- > 0) {
			if (Character.toUpperCase(string.charAt(toffset++)) != Character.toUpperCase(other.charAt(ooffset++)))
				return false;
		}
		
		return true;
	}
	
	public static final boolean startsWith(Object string, Object prefix, int toffset, boolean ignoreCase)
	{
		if (string instanceof String) {
			if (prefix instanceof String) {
				if (ignoreCase)
					return startsWithIgnoreCase((String)string, (String)prefix, toffset);
				else
					return ((String)string).startsWith((String)prefix, toffset);
			}
			if (prefix instanceof StringBuffer) {
				if (ignoreCase)
					return startsWithIgnoreCase((String)string, (StringBuffer)prefix, toffset);
				else
					return startsWith((String)string, (StringBuffer)prefix, toffset);
			}
		}
		if (string instanceof StringBuffer)  {
			if (prefix instanceof String) {
				if (ignoreCase)
					return startsWithIgnoreCase((StringBuffer)string, (String)prefix, toffset);
				else
					return startsWith((StringBuffer)string, (String)prefix, toffset);
			}
			if (prefix instanceof StringBuffer) {
				if (ignoreCase)
					return startsWithIgnoreCase((StringBuffer)string, (StringBuffer)prefix, toffset);
				else
					return startsWith((StringBuffer)string, (StringBuffer)prefix, toffset);
			}
		}
        if (string instanceof char[]) {
            if (prefix instanceof String)
            {
                if (ignoreCase)
                    return startsWithIgnoreCase((char[])string, (String)prefix, toffset);
                else
                    return startsWith((char[])string, (String)prefix, toffset);
            }
            else
                throw new IllegalArgumentException("not implemented");
        }
		throw new IllegalArgumentException("expecting String or StringBuffer");
	}
	
	public static final boolean startsWith(Object string, Object prefix, boolean ignoreCase)
	{
		return startsWith(string,prefix, 0, ignoreCase);
	}
	
	public static final boolean startsWithIgnoreCase(Object string, Object prefix)
	{
		return startsWith(string,prefix, 0, true);
	}
	
	
	public static final boolean startsWithIgnoreCase(String string, String prefix, int toffset)
	{
		return string.regionMatches(true,toffset, prefix,0, prefix.length());
	}

	public static final boolean startsWith(String string, StringBuffer prefix, int toffset)
	{
		return regionMatches(string,toffset, prefix,0, prefix.length());
	}
	
	public static final boolean startsWithIgnoreCase(String string, StringBuffer prefix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset, prefix,0, prefix.length());
	}

	public static final boolean startsWith(StringBuffer string, String prefix, int toffset)
	{
		return regionMatches(string,toffset, prefix,0, prefix.length());
	}
	
	public static final boolean startsWithIgnoreCase(StringBuffer string, String prefix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset, prefix,0, prefix.length());
	}

	public static final boolean startsWith(StringBuffer string, StringBuffer prefix, int toffset)
	{
		return regionMatches(string,toffset, prefix,0, prefix.length());
	}
	
	public static final boolean startsWithIgnoreCase(StringBuffer string, StringBuffer prefix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset, prefix,0, prefix.length());
	}

    public static final boolean startsWithIgnoreCase(char[] string, String prefix, int toffset)
    {
        return regionMatchesIgnoreCase(string,toffset, prefix,0, prefix.length());
    }

    public static final boolean startsWith(char[] string, String prefix, int toffset)
    {
        return regionMatches(string,toffset, prefix,0, prefix.length());
    }

	public static final boolean endsWith(Object string, Object suffix, int toffset, boolean ignoreCase)
	{
		if (string instanceof String) {
			if (suffix instanceof String) {
				if (ignoreCase)
					return endsWithIgnoreCase((String)string, (String)suffix, toffset);
				else
					return endsWith((String)string, (String)suffix, toffset);
			}
			if (suffix instanceof StringBuffer) {
				if (ignoreCase)
					return endsWithIgnoreCase((String)string, (StringBuffer)suffix, toffset);
				else
					return endsWith((String)string, (StringBuffer)suffix, toffset);
			}
		}
		if (string instanceof StringBuffer)  {
			if (suffix instanceof String) {
				if (ignoreCase)
					return endsWithIgnoreCase((StringBuffer)string, (String)suffix, toffset);
				else
					return endsWith((StringBuffer)string, (String)suffix, toffset);
			}
			if (suffix instanceof StringBuffer) {
				if (ignoreCase)
					return endsWithIgnoreCase((StringBuffer)string, (StringBuffer)suffix, toffset);
				else
					return endsWith((StringBuffer)string, (StringBuffer)suffix, toffset);
			}
		}
        if (string instanceof char[]) {
            if (suffix instanceof String) {
                if (ignoreCase)
                    if (ignoreCase)
                        return endsWithIgnoreCase((StringBuffer)string, (StringBuffer)suffix, toffset);
                    else
                        return endsWith((StringBuffer)string, (StringBuffer)suffix, toffset);
            }
            else
                throw new IllegalArgumentException("not implemented");
        }
		throw new IllegalArgumentException("expecting String or StringBuffer");
	}
	
	public static final boolean endsWith(Object string, Object suffix, boolean ignoreCase)
	{
		return endsWith(string,suffix, length(string), ignoreCase);
	}
	
	public static final boolean endsWithIgnoreCase(Object string, Object suffix)
	{
		return endsWith(string,suffix, length(string), true);
	}
	
	
	public static final boolean endsWith(String string, String suffix, int toffset)
	{
		return string.regionMatches(false,toffset-suffix.length(), suffix,0, suffix.length());
	}
	
	public static final boolean endsWithIgnoreCase(String string, String suffix, int toffset)
	{
		return string.regionMatches(true,toffset-suffix.length(), suffix,0, suffix.length());
	}

	public static final boolean endsWith(String string, StringBuffer suffix, int toffset)
	{
		return regionMatches(string,toffset-suffix.length(), suffix,0, suffix.length());
	}
	
	public static final boolean endsWithIgnoreCase(String string, StringBuffer suffix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset-suffix.length(), suffix,0, suffix.length());
	}

	public static final boolean endsWith(StringBuffer string, String suffix, int toffset)
	{
		return regionMatches(string,toffset-suffix.length(), suffix,0, suffix.length());
	}
	
	public static final boolean endsWithIgnoreCase(StringBuffer string, String suffix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset-suffix.length(), suffix,0, suffix.length());
	}

	public static final boolean endsWith(StringBuffer string, StringBuffer suffix, int toffset)
	{
		return regionMatches(string,toffset-suffix.length(), suffix,0, suffix.length());
	}
	
	public static final boolean endsWithIgnoreCase(StringBuffer string, StringBuffer suffix, int toffset)
	{
		return regionMatchesIgnoreCase(string,toffset-suffix.length(), suffix,0, suffix.length());
	}

    public static final boolean endsWith(char[] string, String suffix, int toffset)
    {
        return regionMatches(string,toffset-suffix.length(), suffix,0, suffix.length());
    }

    public static final boolean endsWithIgnoreCase(char[] string, String suffix, int toffset)
    {
        return regionMatchesIgnoreCase(string,toffset-suffix.length(), suffix,0, suffix.length());
    }

	public static final boolean equals(String string, boolean ignoreCase, char[] other, int ooffset, int olen)
	{
		if (ignoreCase)
			return equalsIgnoreCase(string, other, ooffset, olen);
		else
			return equals(string, other, ooffset, olen);
	}
	
	
	public static final boolean equals(String string, char[] other, int ooffset, int olen)
	{
		return string.length()==olen && regionMatches(string, 0, other, ooffset,olen);
	}
	
	public static final boolean equalsIgnoreCase(String string, char[] other, int ooffset, int olen)
	{
		return string.length()==olen && regionMatchesIgnoreCase(string, 0, other, ooffset,olen);
	}
	
	public static final boolean startsWith(String string, char[] other, int ooffset, int olen)
	{
		return regionMatches(string, 0, other, ooffset,olen);
	}
	
	public static final boolean startsWithIgnoreCase(String string, char[] other, int ooffset, int olen)
	{
		return regionMatchesIgnoreCase(string, 0, other, ooffset,olen);
	}

	public static final boolean isInteger(String text)
	{
		if (text==null) return false;

		int min = 0;
		int max = text.length();
		//  skip whitespace
		while (min < max && Character.isWhitespace(text.charAt(min))) min++;
		while (max > min && Character.isWhitespace(text.charAt(max-1))) max--;

		if (min >= max) return false;   //  empty

		if (text.charAt(min)=='-') min++;
		if (min >= max) return false;   //  empty

		for ( ; min < max; min++) {
			char c = text.charAt(min);
			if (c < '0' || c > '9') return false;
		}
		return true;
	}

	public static final boolean regionMatches(String string, int toffset,
											  char[] other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length)
			return false;
		
		while (len-- > 0) {
			if (string.charAt(toffset++) != other[ooffset++])
				return false;
		}
		
		return true;
	}
	
	public static final boolean regionMatchesIgnoreCase(String string, int toffset, 
											  char[] other, int ooffset, int len)
	{
		if (toffset<0 || (toffset+len) > string.length())
			return false;
		if (ooffset<0 || (ooffset+len) > other.length)
			return false;
		
		while (len-- > 0) {
			if (Character.toUpperCase(string.charAt(toffset++)) != Character.toUpperCase(other[ooffset++]))
				return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------------
	//	String Utilities
	//-------------------------------------------------------------------------------
	
	/**	parameter to trim: trim left	*/
	public static final int TRIM_LEFT	= 0x1;
	/**	parameter to trim: trim right	*/
	public static final int TRIM_RIGHT	= 0x2;
	/**	parameter to trim: trim both	*/
	public static final int TRIM_BOTH	= 0x3;
    /** parameter to trim: remove all whitespace (even within text) */
    public static final int TRIM_INSIDE = 0x4;
    public static final int TRIM_ALL    = 0x7;
	public static final int COLLAPSE_WHITESPACE = 0x8;

	/**
	 * either left or right
	 */
	public static void trim(StringBuffer s, int what)
	{
		if (Util.allOf(what,TRIM_LEFT)) {
			int len = s.length();
			int i=0;
			while (i < len && Character.isWhitespace(s.charAt(i)))
				i++;
			if (i > 0)
				s.delete(0,i);
		}
		if (Util.allOf(what,TRIM_RIGHT)) {
			int i = s.length();
			while (i > 0 && Character.isWhitespace(s.charAt(i-1)))
				i--;
			if (i < s.length())
				s.delete(i,s.length());
		}
        if (Util.allOf(what,TRIM_INSIDE)) {
            for (int i = s.length()-1; i >= 0; i--)
                if (Character.isWhitespace(s.charAt(i)))
                    s.delete(i,i+1);
        }
		else if (Util.allOf(what,COLLAPSE_WHITESPACE)) {
	        for (int i = s.length()-1; i >= 0; i--)
                if (Character.isWhitespace(s.charAt(i)))
                {
	                int j = i;
	                while (j >= 1 && Character.isWhitespace(s.charAt(j-1))) j--;
	                if (j < i)
	                    s.delete(j,i);
	                i = j;
	}
        }
	}

    public static String trim(String s, int what)
    {
        StringBuffer buf = new StringBuffer(s);
        trim(buf,what);
        return buf.toString();
    }

	public static String trimNull(String s)
	{
		if (s!=null) {
			s = trim(s,TRIM_BOTH);
			if (s.length()==0) s = null;
		}
		return s;
	}

	public static void escape(StringBuffer s)
	{
		for (int i=s.length()-1; i>=0; i--) {
			char c = s.charAt(i);
			if (c < 20 || c >= 128) {
				s.setCharAt(i,'\\');
				s.insert(i+1,'u');
				insertUnicode(s,i+2,c);
			}
			else switch (c) {
			case '\\':		s.insert(i,'\\'); break;
			case '\t':		s.setCharAt(i,'\\'); 
							s.insert(i+1,'t');
							break;
			case '\n':		s.setCharAt(i,'\\'); 
							s.insert(i+1,'n');
							break;
			case '\r':		s.setCharAt(i,'\\'); 
							s.insert(i+1,'r');
							break;
			}
		}
	}

	public static String escape(String s)
	{
		StringBuffer buf = new StringBuffer(s);
		escape(buf);
		return buf.toString();
	}

	public static void escapeUnprintable(StringBuffer s, int offset, int len)
	{
		if ((offset+len) > s.length())
			len = s.length()-offset;
		for ( ; len-- > 0; offset++)
			if (s.charAt(offset) < 32) 
				s.setCharAt(offset,' ');
	}
	
	public static String escapeUnprintable(String s)
	{
		for (int i=s.length()-1; i>=0; i--)
			if (s.charAt(i) < 32) {
				StringBuffer buf = new StringBuffer(s);
				escapeUnprintable(buf,0,i+1);
				return buf.toString();
			}
		return s;
	}


	/**
	 * un-escapes \\u and other symbols
	 */
	public static void unescape(StringBuffer s)
	{
		for (int i=0; i < s.length(); i++) 
			if (s.charAt(i) == '\\') {
				s.deleteCharAt(i);
				
				switch (s.charAt(i)) {
				default:	break;
				case 't':	s.setCharAt(i, '\t'); break;
				case 'n':	s.setCharAt(i, '\n'); break;
				case 'r':	s.setCharAt(i, '\r'); break;
				case 'u':	char c;
                            char c1 = (s.length() > (i+1)) ? s.charAt(i+1):0;
                            if (isHexDigit(c1)) {
                                char c2 = (s.length() > (i+2)) ? s.charAt(i+2):0;
                                if (isHexDigit(c2)) {
                                    char c3 = (s.length() > (i+3)) ? s.charAt(i+3):0;
                                    if (isHexDigit(c3)) {
                                        char c4 = (s.length() > (i+4)) ? s.charAt(i+4):0;
                                        if (isHexDigit(c4)) {
                                            c = unicodeChar(c1,c2,c3,c4);
                                            s.setCharAt(i,c);
                                            s.delete(i+1,i+5);
                                        }
                                        else {
                                           c = unicodeChar('0',c1,c2,c3);
                                           s.setCharAt(i,c);
                                           s.delete(i+1,i+4);
                                        }
                                    }
                                    else {
                                        c = unicodeChar('0','0',c1,c2);
                                        s.setCharAt(i,c);
                                        s.delete(i+1,i+3);
                                    }
                                }
                                else {
                                    c = unicodeChar('0','0','0',c1);
                                    s.setCharAt(i,c);
                                    s.delete(i+1,i+2);
                                }
                            }
							break;
				}
			}
	}

	public static String unescape(String s)
	{
		StringBuffer buf = new StringBuffer(s);
		unescape(buf);
		return buf.toString();
	}

    public static String[] separateLines(String text)
    {
        if (text==null) return null;

        Vector collect = new Vector();
        int j=0;
        for (int i=0; i<text.length(); i++)
            if (text.charAt(i)=='\n' || text.charAt(i)=='\r') {
                if (i>j) {
                    String line = text.substring(j,i).trim();
                    if (line.length() > 0) collect.add(line);
                }
                j = i+1;
            }
        if (j<text.length()) {
            String line = text.substring(j).trim();
            if (line.length() > 0) collect.add(line);
        }

        if (collect.isEmpty())
            return null;
        String[] result = new String[collect.size()];
        collect.toArray(result);
        return result;
    }

	public static Vector separate(StringBuffer text)
	{
		Vector result = new Vector();
		for (int i=0; i<text.length(); i++)
			result.add(text.substring(i,i+1));
		return result;
	}
	
	public static Vector separate(StringBuffer text, char separator)
	{
		Vector result = new Vector();
		int j=0;
		for (int i=0; i<text.length(); i++)
			if (text.charAt(i)==separator) {
				if (i>j) result.add(text.substring(j,i));
				j = i+1;
			}
		if (j<text.length())
			result.add(text.substring(j));
		return result;
	}

	public static String toString(Object array, String separators)
	{
		if (separators.length() < 3)
			return toString(array,"",separators,"");
		else
			return toString(array,
			                separators.substring(0,1),
			                separators.substring(1,separators.length()-1),
			                separators.substring(separators.length()-1));
	}

	public static String toString(Object array, String prefix, String separator, String suffix)
	{
		if (array==null) return null;

		StringBuffer buf = new StringBuffer();
		buf.append(prefix);
		Iterator i = ListUtil.iterator(array);
		if (i.hasNext()) buf.append(i.next());
		while (i.hasNext()) {
			buf.append(separator);
			buf.append(i.next());
		}
		buf.append(suffix);
		return buf.toString();
	}

	public static void append(StringBuffer text, String str, int count)
	{
		while (count-- > 0)
			text.append(str);
	}

    private static final boolean isHexDigit(char a)
    {
        return (a>='0' && a<='9') || (a>='a' && a<='f') || (a>='A' && a<='F');
    }

	private static final char unicodeChar(char a, char b, char c, char d)
	{
		return (char)((c2i(a)<<12) + (c2i(b)<<8) + (c2i(c)<<4) + c2i(d));
	}
	
	private static final void insertUnicode(StringBuffer buf, int i, char c)
	{
		buf.insert(i,	i2c(((int)c>>12) & 0x000f));
		buf.insert(i+1, i2c(((int)c>>8) & 0x000f));
		buf.insert(i+2, i2c(((int)c>>4) & 0x000f));
		buf.insert(i+3, i2c((int)c & 0x000f));
	}

	private static final int c2i (char c)
	{
		if (c>='a' && c<='f')
			return 10+(c-'a');
		else if (c>='A' && c<='F')
			return 10+(c-'A');
		else
			return (c-'0');
	}
	
	private static final char i2c (int i)
	{
		if (i<10) 
			return (char)('0'+i);
		else
			return (char)('A'+i-10);
	}
	
	public static final char[] byte2char(byte[] bytes, int offset, int len)
	{
		char[] result = new char[len];
		byte2char(bytes,offset,len, result,0);
		return result;
	}
	
	public static final void byte2char(byte[] bytes, int offset, int len, char[] result, int roffset)
	{
		for (int i=0; i<len; i++)
			result[roffset+i] = (char)bytes[offset+i];
	}
	
	public static final byte[] char2byte(char[] chars, int offset, int len)
	{
		byte[] result = new byte[len];
		char2byte(chars,offset,len, result,0);
		return result;
	}
	
	public static final void char2byte(char[] chars, int offset, int len, byte[] result, int roffset)
	{
		for (int i=0; i<len; i++)
			result[roffset+i] = (byte)chars[offset+i];
	}

	public static String blanks(int count, char c)
	{
		char[] cs = new char[count];
		Arrays.fill(cs,c);
		return new String(cs);
	}

	/**
	 * turns string to uppercase
	 * strips diacritics, replaces non-letters
	 */
	public static String jucase(String s)
	{
		char[] cs = s.toCharArray();
		int len = jucase(cs,0,cs.length,true,' ',false);
		return new String(cs,0,len);
	}

	public static void jucase(StringBuffer buf, boolean withWildcards)
	{
		jucase(buf,true,' ',withWildcards);
	}

	public static void jucase(StringBuffer buf,
	                             boolean toUpperCase, char replaceNonLetters, boolean withWildcards)
	{
		char[] chars = new char[buf.length()];
		buf.getChars(0,buf.length(), chars, 0);
		jucase(chars,0,chars.length, toUpperCase,replaceNonLetters, withWildcards);
		buf.setLength(0);
		buf.append(chars);
	}

	public static int jucase(char[] c, int start, int len,
	                            boolean toUpperCase, char replaceNonLetters, boolean withWildcards)
	{
		int i = start;
		int end = start+len;
		
		while (i < end) {
			if (withWildcards) {
				if (c[i]=='?' || c[i]=='*') { i++; continue; }
			}

			if (toUpperCase)
				c[i] = CharUtil.toUpperCase(c[i]);

			if ((replaceNonLetters!=0) && !Character.isLetterOrDigit(c[i])) {
				//	remove non-letters
				c[i] = replaceNonLetters;
				while ((i+1) < end && !Character.isLetterOrDigit(c[i+1])) {
					//	collapse non-letters to one character
					System.arraycopy(c,i+1, c,i,end-i-1);
					end--;
				}
			}
			i++;
		}
		return end-start;
	}
	
	/**
	 * replaces all occurrences of one string with another
	 */
	public static final String replace(String source, Map placeholders)
	{
        if (source==null) return null;

		int k = source.lastIndexOf("%");
		if (k<0) return source;		//	shortcut

		StringBuffer buf = new StringBuffer();
		replace(source, placeholders, buf, k);
		return buf.toString();
	}
	
	/**
	 * replaces all occurrences of one string with another
	 */
	public static final void replace(String source, Map placeholders, StringBuffer buf)
	{
		int k = source.lastIndexOf("%");
		replace(source, placeholders, buf, k);
	}
	
	
	/**
	 * replaces all occurrences of one string with another
	 */
	private static final void replace(String source, Map placeholders, StringBuffer buf, int k)
	{
		int i0 = buf.length();
		buf.append(source);
		
		do {
			int l = source.lastIndexOf("%",k-1);
			if (l<0) break;

            if (l+1 == k) {
                //  %%
                buf.delete(i0+k,i0+k+1);
            }
            else {
				buf.delete(i0+l,i0+k+1);
				String key = source.substring(l+1,k);
				Object value = placeholders.get(key);
				if (value!=null)
					buf.insert(i0+l,value.toString());
            }
			k = source.lastIndexOf("%",l-1);
		} while (k>=0);
	}

	public static final String replace(String source, String pattern, String replace)
	{
		if (source==null) return null;

		int k = source.lastIndexOf(pattern);
		if (k<0) return source;

		StringBuffer buf = new StringBuffer();
		replace(source,pattern,replace, buf,k);
		return buf.toString();
	}

	private static final void replace(String source, String pattern, String replace, StringBuffer buf, int k)
	{
		int i0 = buf.length();
		buf.append(source);

		do {
			buf.delete(i0+k,i0+k+pattern.length());
			buf.insert(i0+k,replace);

			k = source.lastIndexOf(pattern,k-1);
		} while (k>=0);
	}

	/**
	 * append SQL placeholders (?) 
	 */
	public static final void appendParams(StringBuffer sql, int count)
	{
		for (int i=0; i < count; i++) {
			if (i>0) sql.append(",");
			sql.append("?");
		}
	}
	
	public static final String nvl(String x, String y)
	{
		if (x==null || x.length()==0)
			return y;
		else
			return x;
	}


	/**
	 * convert a Vector of String into a String[]
	 */
	public static final String[] toArray(List list)
	{
		String[] result = new String[list.size()];
		list.toArray(result);
		return result;
	}

	/**
	 * convert a Vector of String into a String[]
	 */
	public static final String[] toSortedArray(List list, boolean caseSensitive)
	{
		String[] result = toArray(list);
		if (caseSensitive)
			Arrays.sort(result);
		else
			Arrays.sort(result,CASE_INSENSITIVE_COMPARATOR);
		return result;
	}

    public static String toMixedCase(String str)
    {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }

	/**
	 * replaces a number of characters with one char
	 */
	public static String replace(String s, String pattern, char replace)
	{
		IntHashSet cset = new IntHashSet(pattern.length()*2,0.8f);
		for (int i=0; i<pattern.length(); i++)
			cset.add(pattern.charAt(i));

		char[] buf = s.toCharArray();
		for (int i=0; i<buf.length; i++)
			if (cset.contains(buf[i]))
				buf[i] = replace;

		return new String(buf);
	}

    public static int[] parseIntList(String s)
    {
        IntArray collect = new IntArray();
        StringTokenizer tok = new StringTokenizer(s, ",-",true);
        int i = Integer.parseInt(tok.nextToken());
        collect.add(i);

        while (tok.hasMoreTokens()) {
            String delim = tok.nextToken();
            int j = Integer.parseInt(tok.nextToken());
            if (delim.equals(","))
                collect.add(i=j);
            else if (delim.equals("-"))
                while (i < j) collect.add(++i);
        }

        return collect.toArray();
    }

	public static boolean isWhitespaceOrNewline(char c)
	{
		return Character.isWhitespace(c) || (c=='\n') || (c=='\r');
	}

	public static int indexOfWhitespace(String s, int start)
	{
		while (start < s.length())
			if (isWhitespaceOrNewline(s.charAt(start)))
				return start;
			else
				start++;
		return -1;
	}

	public static boolean isEmpty(String s)
	{
		return (s==null) || isWhitespace(s);
	}

	public static boolean isWhitespace(String s)
	{
		if (s==null) return false;
		for (int i=s.length()-1; i>=0; i--)
		{
			char c = s.charAt(i);
			if (!Character.isWhitespace(c)) return false;
		}
		return true;
	}

	public static boolean isWhitespaceOrNewline(String s)
	{
		if (s==null) return false;
		for (int i=s.length()-1; i>=0; i--)
		{
			char c = s.charAt(i);
			if (! isWhitespaceOrNewline(c)) return false;
		}
		return true;
	}

	public static boolean endsWithWhitespace(String s)
	{
		return (s.length() > 0) && Character.isWhitespace(s.charAt(s.length()-1));
	}

	public static boolean startsWithWhitespace(String s)
	{
		return (s.length() > 0) && Character.isWhitespace(s.charAt(0));
	}

	public static String trimNewline(String s)
	{
		int k0 = 0;
		int k1 = s.length();

		while (k0 < k1 && isWhitespaceOrNewline(s.charAt(k0))) k0++;
		while (k1 > k0 && isWhitespaceOrNewline(s.charAt(k1-1))) k1--;

		if (k0==0 && k1==s.length())
			return s;
		else
			return s.substring(k0,k1);
	}

	public static StringBuffer replace(StringBuffer buffer, String pattern, String repl)
	{
		int j = buffer.length();
		for (;;) {
			j = buffer.lastIndexOf(pattern,j);
			if (j<0) break;
			buffer.replace(j,j+pattern.length(),repl);
			j--;
		}
		return buffer;
	}

	public static String fillLeft(String str, int len, String pattern)
	{
		if (str.length() >= len) return str;

		StringBuffer buf = new StringBuffer(str);
		while (buf.length() < len)
		{
			int chunk = len-buf.length();
			if (chunk >= pattern.length())
				buf.insert(0,pattern);
			else
				buf.insert(0,pattern.substring(pattern.length()-chunk));
		}
		return buf.toString();
	}

	public static String nullValueOf(Object object)
	{
		if (object==null)
			return null;
		else
			return object.toString();
	}

	public static String valueOf(Object object)
	{
		if (object==null)
			return null;
		else
			return object.toString();
	}

	public static String swapCase(String str) {
		StringBuilder bld = new StringBuilder(str);
		swapCase(bld);
		return bld.toString();
	}

	public static void swapCase(StringBuilder b) {
		for(int i=0; i<b.length(); i++) {
			char c = b.charAt(i);
			if (Character.isLowerCase(c))
				b.setCharAt(i,Character.toUpperCase(c));
			else if (Character.isUpperCase(c))
				b.setCharAt(i,Character.toLowerCase(c));
		}
	}

	public static String swap(String str, char a, char b) {
		StringBuilder bld = new StringBuilder(str);
		swap(bld,a,b);
		return bld.toString();
	}

	public static void swap(StringBuilder bld, char a, char b) {
		for(int i=0; i<bld.length(); i++) {
			char c = bld.charAt(i);
			if (c==a)
				bld.setCharAt(i,b);
			else if (c==b)
				bld.setCharAt(i,a);
		}
	}

	/**
	 * a String Comparator that is not case sensitive
	 * null values are compared, too
	 */
	public static class CaseInsensitiveStringComparator implements Comparator
	{
		public int compare(Object a, Object b)
		{
			if (a==null) {
				if (b==null)
					return 0;
				else
					return Integer.MIN_VALUE;
			}

			if (b==null)	//	a != null asserted
				return Integer.MAX_VALUE;

			String sa = a.toString();
			String sb = b.toString();

			return sa.compareToIgnoreCase(sb);
		}
	}

	public static final CaseInsensitiveStringComparator CASE_INSENSITIVE_COMPARATOR
			= new CaseInsensitiveStringComparator();


    private static final String BASE64_STRING =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789+/";

    private static final byte[] BASE64 = BASE64_STRING.getBytes();

    public static final byte encodeBase64(short value)
    {
        return BASE64[value & 0x3f];
    }
    

    private static final String BASE_ISO_STRING =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "0123456789" +
            "\u00c0"+"\u00c1"+"\u00c2"+"\u00c3"+"\u00c4"+"\u00c5"+"\u00c6"+"\u00c7"+
            "\u00c8"+"\u00c9"+"\u00ca"+"\u00cb"+"\u00cc"+"\u00cd"+"\u00ce"+"\u00cf"+
            "\u00d0"+"\u00d1"+"\u00d2"+"\u00d3"+"\u00d4"+"\u00d5"+"\u00d6"+
            "\u00d9"+"\u00da"+"\u00db"+"\u00dc"+"\u00dd";

    public static final byte[] BASE_ISO = BASE_ISO_STRING.getBytes();

    public static final int BASE_ISO_MOD = BASE_ISO.length;


	
	public static final String rest(String s)
	{
		return rest(s,1);
	}

	public static final String rest(String s, int skip)
	{
		int k = -1;
		while (skip-- > 0 && k < s.length()) {
			k = s.indexOf(' ',k+1);
			while ((k+1) < s.length() && s.charAt(k+1)==' ')
				k++;
		}
		if (k <= 0)
			return s;
		else
			return s.substring(k);
	}

	/**
	 * some UCI engines have the bad habit of passing excessively large numbers
	 * that's why need out own parsing method
	 * @param obj
	 * @return
	 */
	public static int parseInt(Object obj)
	{
		if (obj==null)
			return 0;
		else {
			String str = obj.toString();
			return parseInt(str.toCharArray(),0,str.length());
		}
	}

	public static final int parseInt(char[] s, int offset, int len)
	{
		if (len==0 || (len==1 && (s[0]=='?' || s[0]=='-')))
			return 0;   //  empty

		int result = 0;
		boolean negative = false;
		int max = offset+len;

		while (offset < max && Character.isWhitespace(s[offset]))
			offset++;

		if (offset < max && s[offset]=='-') {
			negative = true;
			offset++;
		}

		while (offset < max) {
			int c = s[offset++]-'0';
			if (c >= 0 && c <= 9)
				result = 10*result + c;
			else
				break;
		}

		if (negative)
			return -result;
		else
			return result;
	}

	/**
	 * some UCI engines have the bad habit of passing excessively large numbers
	 * that's why need out own parsing method
	 * @param obj
	 * @return
	 */
	public static long parseLong(Object obj)
	{
		if (obj==null)
			return 0L;
		else {
			String str = obj.toString();
			return parseLong(str.toCharArray(),0,str.length());
		}
	}

	public static final long parseLong(char[] s, int offset, int len)
	{
		long result = 0;
		boolean negative = false;
		int max = offset+len;

		while (offset < max && Character.isWhitespace(s[offset]))
			offset++;

		if (offset < max && s[offset]=='-') {
			negative = true;
			offset++;
		}

		while (offset < max) {
			int c = s[offset++]-'0';
			if (c >= 0 && c <= 9)
				result = 10*result + c;
			else
				break;
		}

		if (negative)
			return -result;
		else
			return result;
	}

	public static long parseLong(String line, String prefix, String suffix)
	{
		int i1 = line.indexOf(prefix);
		if (i1<0) return Long.MIN_VALUE;
		i1 += prefix.length();
		int i2 = line.indexOf(suffix,i1);
		if (i2<0) throw new IllegalArgumentException("expected "+suffix+" in "+line);
		return Long.parseLong(line.substring(i1,i2).trim());
	}

	public static double parseDouble(String line, String prefix, String suffix)
	{
		int i1 = line.indexOf(prefix);
		if (i1<0) return Double.NaN;
		i1 += prefix.length();
		int i2 = line.indexOf(suffix,i1);
		if (i2<0) throw new IllegalArgumentException("expected "+suffix+" in "+line);
		try {
			return Double.parseDouble(line.substring(i1, i2).trim());
		} catch(NumberFormatException e) {
			return Double.NaN;
		}
	}

	public static String formatLargeInt(long num, DecimalFormat format) {
		if (Math.abs(num) < 1000)
			return format.format(num);
		double dnum = (double)num;
		dnum /= 1000.0;
		if (dnum < 1000.0)
			return format.format(dnum)+"k";
		dnum /= 1000.0;
		if (dnum < 1000.0)
			return format.format(dnum)+"M";
		dnum /= 1000.0;
		return format.format(dnum)+"G";
	}


    public static int nextWordBreak(String text, int i)
    {
        //  skip leading whitespace
        while (i < text.length() && text.charAt(i)==' ')
            i++;
        //  find next white space
        return text.indexOf(' ',i);
    }

	public static int compare(StringBuilder a, StringBuilder b)
	{
		int i=0;
		int j=0;

		for( ; i < a.length() && j < b.length(); ++i,++j)
		{
			char c = a.charAt(i);
			char d = b.charAt(j);
			if (c!=d)
				return c-d;
		}
		if (a.length() < b.length())
			return -1;
		if (b.length() < a.length())
			return +1;
		return 0;
	}

	public static int compareVersion(String va, String vb)
	{
	    StringTokenizer toka = new StringTokenizer(va,". -/_");
	    StringTokenizer tokb = new StringTokenizer(vb,". -/_");

	    while (toka.hasMoreTokens())
	    {
	        String na = toka.nextToken();
	        if (!tokb.hasMoreTokens()) return +1;

	        String nb = tokb.nextToken();

	        int comp;
	        if (isInteger(na) && isInteger(nb))
	            comp = Util.toint(na)-Util.toint(nb);
	        else
	            comp = na.compareTo(nb);

	        if (comp != 0)
	            return comp;
	    }

	    if (tokb.hasMoreTokens())
	        return -1;
	    else
	        return 0;
	}

	public static int countWhitespace(String s, int i, int j)
	{
		int count=0;
		for( ; i < j; ++i)
			if (Character.isWhitespace(s.charAt(i)))
				++count;
		return count;
	}

	public static String reverse(String str)
	{
		return new StringBuilder(str).reverse().toString();
	}
}
