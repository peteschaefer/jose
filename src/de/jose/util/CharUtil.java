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

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 * utilities for Character processing
 *
 * this file must be interpreted with "Windows-1252" encoding.
 * don't forget this when compiling!!
 *
 * @author Peter Schäfer
 */

public class CharUtil
{
 
 private static String[] kEscape = {
	 "  `´^~\"_uv.           c ,   jk -ne ° /   s      ",
	 "   '                               o            ",
 };
								 
 private static String[] kDiacritics = {
  // latin:
  "A ÀÁÂÃÄ\u0100\u0102\u01cd\u1ea0\u1ea2\u1ea4\u1ea6\u1ea8\u1eaa\u1eac\u1eae\u1eb0\u1eb2\u1eb4\u1eb6\u0104          Æ\u01fcÅ           \u01fa",
  "a àáâãä\u0101\u0103\u01ce\u1ea1\u1ea3\u1ea5\u1ea7\u1ea9\u1eab\u1ead\u1eaf\u1eb1\u1eb3\u1eb5\u1eb7\u0105          æ\u01fdå           \u01fb",
  "O ÒÓÔÕÖ\u014c\u014e\u01d1\u1ecc\u1ece\u1ed0\u1ed2\u1ed4\u1ed6\u1ed8\u1eda\u1edc\u1ede\u1ee0\u1ee2           \u0152  \u01a0Ø\u01fe\u0150       ",
  "o òóôõö\u014d\u014f\u01d2\u1ecd\u1ecf\u1ed1\u1ed3\u1ed5\u1ed7\u1ed9\u1edb\u1edd\u1edf\u1ee1\u1ee3           \u0153  \u01a1ø\u01ff\u0151       ",
  "U ÙÚÛ\u0168Ü\u016a\u016c\u01d3\u1ee4\u1ee6     \u1ee8\u1eea\u1eec\u1eee\u1ef0\u0172            \u016e\u01af  \u0170   \u01d5\u01d7\u01d9\u01db",
  "u ùúû\u0169ü\u016b\u016d\u01d4\u1ee5\u1ee7     \u1ee9\u1eeb\u1eed\u1eef\u1ef1\u0173            \u016f\u01b0  \u0171   \u01d6\u01d8\u01da\u01dc",
  "C  \u0106\u0108    \u010c            Ç\u010a                   ",
  "c  \u0107\u0109    \u010d            ç\u010b                   ",
  "D        \u010e                                 ",
  "d                       \u010f                  ",
  "E ÈÉÊ\u1ebcË\u0112\u0114\u011a\u1eb8\u1eba\u1ebe\u1ec0\u1ec2\u1ec4\u1ec6     \u0118\u0116                   ",
  "e èéê\u1ebdë\u0113\u0115\u011b\u1eb9\u1ebb\u1ebf\u1ec1\u1ec3\u1ec5\u1ec7     \u0119\u0117                   ",
  "f                        \u0192                 ",
  "G   \u011c   \u011e             \u0122\u0120                   ",
  "g   \u011d   \u011f              \u0121\u0123                  ",
  "H   \u0124                     \u0126                ",
  "h   \u0125                     \u0127                ",
  "I ÌÍÎ\u0128Ï\u012a\u012c\u01cf\u1eca\u1ec8          \u012e\u0130   \u0131\u0132              ",
  "i ìíî\u0129ï\u012b\u012d\u01d0\u1ecb\u1ec9          \u012f     \u0133              ",
  "J   \u0134                                      ",
  "j   \u0135                                      ",
  "K                     \u0136      \u0138             ",
  "k                     \u0137                    ",
  "L  \u0139                  \u013b \u013d     \u013f\u0141           ",
  "l  \u013a                  \u013c \u013e     \u0140\u0142           ",
  "N  \u0143 Ñ   \u0147            \u0145 \u0149       \u014a          ",
  "n  \u0144 ñ   \u0148            \u0146         \u014b          ",
  "R  \u0154     \u0158            \u0156                    ",
  "r  \u0155     \u0159            \u0157                    ",
  "S  \u015a\u015c    \u0160            \u015e                  ß ",
  "s  \u015b\u015d    \u0161            \u015f                    ",
  "T        \u0164                \u0166               \u0162",
  "t                       \u0165 \u0167               \u0163",
  "W \u1e80\u1e82\u0174 \u1e84                                    ",						    
  "w \u1e81\u1e83\u0175 \u1e85                                    ",						    
  "Y \u1ef2Ý\u0176\u1ef8\u0178   \u1ef4\u1ef6                               ",					    
  "y \u1ef3ý\u0177\u1ef9   \u1ef5\u1ef7                               ",					    
  "Z  \u0179     \u017d             \u017b                   ",	    
  "z  \u017a     \u017e             \u017c                   ",	    
 };

 /**	rows/columns of diacritic chars  */
 protected static char[][] gDiacriticMap;
 /**	maps escape characters to column indexes	 */
 protected static short[] gEscapeMap;
 /**	maps characters to rows/columns  */
 protected static short[] gCharacterMap;
 
 static {
	gDiacriticMap = new char[256][];
	gEscapeMap = new short[512];
	gCharacterMap = new short[Character.MAX_VALUE];
	
	for (int i=0; i<kEscape.length; i++)
		for (short j=0; j<kEscape[i].length(); j++) {
			char c = kEscape[i].charAt(j);
			if (c != ' ')
				gEscapeMap[c] = j;
		}
	
	for (int i=0; i<kDiacritics.length; i++) {
		char base = kDiacritics[i].charAt(0);
		gDiacriticMap[base] = new char[60];
		for (short j=1; j<kDiacritics[i].length(); j++) {
			char c = kDiacritics[i].charAt(j);
			if (c != ' ') {
				gDiacriticMap[base][j] = c;
				gCharacterMap[c] = (short)((j<<8) + base);
			}
		}
	}
		
 }
 
	public static final char stripDiacritic(char c) 
	{
		short ety = gCharacterMap[c];
		if (ety==0) 
			return c;
		else
			return (char)(ety & 0x00ff);
	}

	
	public static final boolean isDiacritic(char c)
	{
		return gCharacterMap[c] != 0;
	}
	
	public static final boolean canEscape(char c) 
	{
		short ety = gCharacterMap[c];
		if (ety==0) return false;
		
		int col = (ety >> 8);
		return kEscape[0].charAt(col) != ' ';
	}
	
	public static final void escape(char c, StringBuffer buf)
	{
		short ety = gCharacterMap[c];
		if (ety==0)
			buf.append(c);
		else {
			int col = (ety >> 8);
			char base = (char)(ety & 0x00ff);
			char escChar = kEscape[0].charAt(col);
			if (escChar != ' ') {
				buf.append("\\");
				buf.append(escChar);
			}
			buf.append(base);
		}
	}

	public static final String escape(String text, boolean forceEscape)
	{
		for (int i=0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c=='\\')
				return CharUtil.doEscape(text,forceEscape);
			if (gCharacterMap[c]!=0 && (forceEscape || c>=256))
				return CharUtil.doEscape(text,forceEscape);
		}
		return text;    //  unmodified
	}

	private static final String doEscape(String text, boolean forceEscape)
	{
		StringBuffer buffer = new StringBuffer(text.length()+2);
		for (int i=0; i<text.length(); i++)
		{
			char c = text.charAt(i);
			if (c=='\\')
				buffer.append("\\\\");
			else if (gCharacterMap[c]!=0 && (forceEscape || c>=256))
				escape(c,buffer);
			else
				buffer.append(c);
		}
		return buffer.toString();
	}

	public static final char unescape(char c1, char c2)
	{
		char[] bmap =  gDiacriticMap[c2];
		if (bmap == null)
			return c2;
		else
			return bmap[gEscapeMap[c1]];
	}
	
	
	public static final char toLowerCase(char c)
	{
		return Character.toLowerCase(stripDiacritic(c));
	}
	
	public static final char toUpperCase(char c)
	{
		return Character.toUpperCase(stripDiacritic(c));
	}
	
	public static final void toLowerCase(char[] c)
	{
		for (int i=c.length-1; i>=0; i--) c[i] = toLowerCase(c[i]);
	}
	
	public static final void toUpperCase(char[] c)
	{
		for (int i=c.length-1; i>=0; i--) c[i] = toUpperCase(c[i]);
	}
/*
	public static void main(String[] args) 
		throws java.io.IOException
	{
		try {
			java.io.BufferedReader in = 
				new java.io.BufferedReader(
				new java.io.InputStreamReader(
				new java.io.FileInputStream(args[0]), "UTF-16"));

			java.io.OutputStreamWriter out;
			if (args.length > 1)
				out =
					new java.io.OutputStreamWriter(
					new java.io.FileOutputStream(args[1]));
			else
				out =
					new java.io.OutputStreamWriter(System.out);
			
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				for (int j=0; j < line.length(); j++) {
					int c = (int)line.charAt(j);
					if (c < 256)
						out.write(c);
					else {
						out.write("\\u");
						out.write(toHexString(c));
					}
				}
				out.write('\n');
			}
			
			out.close();
		} catch (Exception ex) {
			Application.error(ex);
		}
	}
*/
	protected static String toHexString(int i) {
		String zeros = "00000000000";
		String hex = Integer.toHexString(i);
		int len = hex.length();
		if (len < 4)
			return zeros.substring(0,4-len)+hex;
		else
			return hex;
	}

	protected static String toHexString2(int i) {
		String zeros = "00000000000";
		String hex = Integer.toHexString(i);
		int len = hex.length();
		if (len < 2)
			return zeros.substring(0,2-len)+hex;
		else
			return hex.substring(0,2);
	}

	public static int utf8Len(char[] bytes, int offset, int maxlen) {
		if ((maxlen >= 4)
			&& ((bytes[offset]   & 0xf8) == 0xf0)
			&& ((bytes[offset+1] & 0xc0) == 0x80)
			&& ((bytes[offset+2] & 0xc0) == 0x80)
			&& ((bytes[offset+3] & 0xc0) == 0x80))
			return 4;
		if ((maxlen >= 3)
			&& ((bytes[offset]   & 0xf0) == 0xe0)
			&& ((bytes[offset+1] & 0xc0) == 0x80)
			&& ((bytes[offset+2] & 0xc0) == 0x80))
			return 3;
		if ((maxlen >= 2)
			&& ((bytes[offset]   & 0xe0) == 0xc0)
			&& ((bytes[offset+1] & 0xc0) == 0x80))
			return 2;
		if (maxlen >= 1)
			return 1;
		//else
		return 0;
	}

	public static char decodeUtf8(char[] bytes, int offset, int ulen) {
		int i = 0;
		switch(ulen) {
			case 4:
				i |= (bytes[offset] & 0x07) << 18;
				i |= (bytes[offset+1] & 0x3f) << 12;
				i |= (bytes[offset+2] & 0x3f) << 6;
				i |= (bytes[offset+3] & 0x3f);
				break;
			case 3:
				i |= (bytes[offset] & 0x0f) << 12;
				i |= (bytes[offset+1] & 0x3f) << 6;
				i |= (bytes[offset+2] & 0x3f);
				break;
			case 2:
				i |= (bytes[offset] & 0x1f) << 6;
				i |= (bytes[offset+1] & 0x3f);
				break;
			default:
				i = bytes[offset];
				break;
		}
		return (char)i;
	}

	public static int encodeUtf8(char chr, byte[] bytes, int offset) {
		if (chr < 0x0080) {
			bytes[offset++] = (byte)(chr & 0x007f);
			return 1;
		}
		if (chr < 0x0800) {
			/** encode UTF-8    */
			bytes[offset++] = (byte)(0xc0 | (chr>>6) & 0x1f);
			bytes[offset++] = (byte)(0x80 | chr & 0x3f);
			return 2;
		}
		//else {
		bytes[offset++] = (byte)(0xe0 | (chr >> 12) & 0x0f);
		bytes[offset++] = (byte)(0x80 | (chr >> 6) & 0x3f);
		bytes[offset++] = (byte)(0x80 | chr & 0x3f);
		return 3;
	}

	public static int encodeUtf8(char chr, ByteArrayOutputStream out) {
		if (chr < 0x0080) {
			out.write((byte) (chr & 0x007f));
			return 1;
		}
		if (chr < 0x0800) {
			out.write((byte)(0xc0 | (chr>>6) & 0x1f));
			out.write((byte)(0x80 | chr & 0x3f));
			return 2;
		}
		// else {
		out.write((byte)(0xe0 | (chr >> 12) & 0x0f));
		out.write((byte)(0x80 | (chr >> 6) & 0x3f));
		out.write((byte)(0x80 | chr & 0x3f));
		return 3;
	}


	public static void main(String[] args) throws IOException
	{
		String encoding = args[0];

		PrintStream out = null;

		if (args.length > 1)
			out = new PrintStream(new FileOutputStream(args[1]));
		else
			out = System.out;

		out.println("/**");
		out.println("  *  character maps for "+encoding);
		out.println(" */");
		out.println();

		byte[] bytes = new byte[256];
		for (int i=0; i<256; i++) bytes[i] = (byte)i;

		String str = new String(bytes,encoding);
		char[] chars = str.toCharArray();

		out.println("char isLetter[] = {");
		for (int i=0; i<16; i++) {
			out.print("\t");
			for (int j=0; j<16; j++) {
				out.print(Character.isLetterOrDigit(chars[16*i+j]) ? "1":"0");
				out.print(",");
			}
			out.println();
		}
		out.println("};");
		out.println();

		out.println("unsigned char stripDiacritics[] = {");
		for (int i=0; i<16; i++) {
			out.print("\t");
			for (int j=0; j<16; j++) {
				out.print("0x");
				char c = CharUtil.stripDiacritic(chars[16*i+j]);
				out.print(toHexString2(c));
				out.print(",");
			}
			out.println();
		}
		out.println("};");
		out.println();

		out.println("unsigned char stripDiacriticsToUpper[] = {");
		for (int i=0; i<16; i++) {
			out.print("\t");
			for (int j=0; j<16; j++) {
				out.print("0x");
				char c = CharUtil.stripDiacritic(chars[i*16+j]);
				c = Character.toUpperCase(c);
				        
				out.print(toHexString2(c));
				out.print(",");
			}
			out.println();
		}
		out.println("};");
		out.println();
	}
}
