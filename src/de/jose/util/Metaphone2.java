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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * A class to generate phonetic code
 * reference: Computer Language of Dec. 1990, p 39
 *  "Hanging on the Metaphone" by Lawrence Philips
 *
 * This Java implementation, Copyright 1997, William B. Brogden
 * is hereby released for all uses. I would appreciate hearing about it
 * if you find a good use for the class.  December, 1997
 *  wbrogden@bga.com    CompuServe  75415,610
 */

/*

The Metaphone Rules

Metaphone reduces the alphabet to 16 consonant sounds:

B X S K J T F H L M N P R 0 W Y

That isn't an O but a zero - representing the 'th' sound.

Transformations
Metaphone uses the following transformation rules:
Doubled letters except "c" -> drop 2nd letter.
Vowels are only kept when they are the first letter.

B -> B   unless at the end of a word after "m" as in "dumb"
C -> X    (sh) if -cia- or -ch-
     S   if -ci-, -ce- or -cy-
     K   otherwise, including -sch-
D -> J   if in -dge-, -dgy- or -dgi-
     T   otherwise
F -> F
G ->     silent if in -gh- and not at end or before a vowel
         in -gn- or -gned- (also see dge etc. above)
     J   if before i or e or y if not double gg
     K   otherwise
H ->     silent if after vowel and no vowel follows
     H   otherwise
J -> J
K ->     silent if after "c"
     K   otherwise
L -> L
M -> M
N -> N
P -> F   if before "h"
     P   otherwise
Q -> K
R -> R
S -> X   (sh) if before "h" or in -sio- or -sia-
     S   otherwise
T -> X   (sh) if -tia- or -tio-
     0   (th) if before "h"
         silent if in -tch-
     T   otherwise
V -> F
W ->     silent if not followed by a vowel
     W   if followed by a vowel
X -> KS
Y ->     silent if not followed by a vowel
     Y   if followed by a vowel
Z -> S

Initial Letter Exceptions

Initial  kn-, gn- pn, ac- or wr-      -> drop first letter
Initial  x-                           -> change to "s"
Initial  wh-                          -> change to "w"

The code is truncated at 4 characters in this example, but more could be used.

Original algorithm published by:
 Lawrence Philips in an article entitled "Hanging on the Metaphone"
 Computer Language v7 n12, December 1990, pp39-43.

*/

/* Notes:
 * The method metaPhone converts an input String into a code.
 *   All input is converted to upper case.
 *   Limitations: Input format is expected to be a single ASCII word
 *   with only characters, no punctuation or numbers.
 */

/**
 * @author William Brogden (with some modifications by Peter Schäfer)
 */

public class Metaphone2
{
	static char[] CIA    = "CIA".toCharArray();
	static char[] SCH    = "SCH".toCharArray();
	static char[] CH     = "CH".toCharArray();
	static char[] GN     = "GN".toCharArray();
	static char[] GNED   = "GNED".toCharArray();
	static char[] SH     = "SH".toCharArray();
	static char[] SIO    = "SIO".toCharArray();
	static char[] SIA    = "SIA".toCharArray();
	static char[] TIA    = "TIA".toCharArray();
	static char[] TIO    = "TIO".toCharArray();
	static char[] TCH    = "TCH".toCharArray();
	static char[] TH     = "TH".toCharArray();

	private int maxCodeLen;

	public Metaphone2() {
		this(32768);
	}

	public Metaphone2(int max) {
		maxCodeLen = max;
	}

	public String encode(String s) {
		return encode(s.toCharArray(),0,s.length());
	}

	public void encode(StringBuffer buf) {
		char[] chars = new char[buf.length()];
		buf.getChars(0,buf.length(), chars,0);
		buf.setLength(0);
		buf.append(encode(chars,0,chars.length));
	}

	private static boolean startsWith(char[] word, int start, int end, char[] comp)
	{
		if ((end-start) < comp.length)
			return false;    //  too short
		else
			end = start+comp.length;
		int i=0;
		while (start < end)
			if (word[start++] != comp[i++]) return false;
		return true;
	}

	private static boolean isfrontv(char c)
	{
		return (c=='E') || (c=='I') || (c=='Y');
	}

	private static boolean isvowel(char c)
	{
		return (c=='A') || (c=='E') || (c=='I') || (c=='O') || (c=='U');
	}

	private static boolean isvarson(char c)
	{
		return (c=='C') || (c=='S') || (c=='P') || (c=='T') || (c=='G');
	}


    public String encode(char[] word, int start, int end)
    {
      boolean hard = false;

      if ((word == null) || (start >= end))
	      return "";
	  CharUtil.toUpperCase(word);       //  TODO eliminate

      // single character is itself
      if ((start+1) == end)
	      return new String(word,start,1);

	  char[] code = new char[10];
	  int len = 0;

      // handle initial 2 characters exceptions
      switch (word[start])
      {
        case 'K': case 'G' : case 'P' : /* looking for KN, etc*/
          if (word[start+1] == 'N') start++;
          break;
        case 'A': /* looking for AE */
          if (word[start+1] == 'E') start++;
          break;
        case 'W' : /* looking for WR or WH */
          if (word[start+1] == 'R')   // WR -> R
            start++;
          else if (word[start+1] == 'H')
	        word[++start] = 'W';   // WH -> W
          break;
        case 'X' : /* initial X becomes S */
          word[start] = 'S';
          break ;
      } // now initials fixed

      int n = start;
      while((len < maxCodeLen) && // max code size of 4 works well
            (n < end))
      {
        char symb = word[n];
        // remove duplicate letters except C
        if ((symb != 'C') &&
           (n > start) && (word[n-1] == symb))
        {
	        n++;
	        continue;
        }

         switch( symb )
         {
            case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
              if (n == start) code[len++] = symb;
              break ; // only use vowel if leading char

            case 'B' :
              if ((n > start) &&
                  !(n+1 == end) && // not MB at end of word
                  (word[n-1] == 'M'))
                  code[len++] = symb;
              else
	              code[len++] = symb;  //  TODO this makes no sense
              break ;

            case 'C' : // lots of C special cases
              /* discard if SCI, SCE or SCY */
              if( ( n > 0 ) &&
                  ( word[n-1] == 'S' ) &&
                  ( n + 1 < end ) &&
                  isfrontv(word[n + 1]))
                break;

	         if( startsWith(word, n,end, CIA)) { // "CIA" -> X
                 code[len++] = 'X';
		         break;
	         }
	         if( ( n + 1 < end ) &&
                  isfrontv(word[n+1])) {
                code[len++] = 'S';  // CI,CE,CY -> S
                break;
             }
             if(( n > start) &&
                 startsWith(word,n-1,end,SCH)) { // SCH->sk
                 code[len++] = 'K';
	             break;
             }
             if( startsWith(word,n,end,CH))
              { // detect CH
                if((n == start) &&
                   (end-start >= 3) &&    // CH consonant -> K consonant
                   ! isvowel(word[start+2]))
                     code[len++]='K';
                else
	                code[len++] = 'X'; // CHvowel -> X
              }
              else
	              code[len++] = 'K';
             break;

            case 'D' :
              if(( n + 2 < end )&&  // DGE DGI DGY -> J
                 ( word[n+1] == 'G' )&&
                 isfrontv(word[n+2]))
              {
                  code[len++] = 'J';
	              n += 2 ;
              }
              else
	              code[len++] = 'T';
              break;

            case 'G' : // GH silent at end or before consonant
              if(( n + 2 == end )&&
                 (word[n+1] == 'H' ))
	              break;
              if(( n + 2 < end ) &&
                 (word[n+1] == 'H' )&&
                 ! isvowel(word[n+2]))
	             break;

              if((n > start) &&
                 startsWith(word,n,end,GN)||
                 startsWith(word,n,end,GNED))
	              break; // silent G

              if(( n > 0 ) &&
                 (word[n-1] == 'G')) hard = true;
              else
	              hard = false;

              if((n+1 < end) &&
                 isfrontv(word[n+1])&&
                 (!hard) )
	              code[len++] = 'J';
              else
	              code[len++] = 'K';
              break ;

            case 'H':
              if( n + 1 == end )
	              break; // terminal H
              if((n > 0) &&
                 isvarson(word[n-1]))
	              break;

              if( isvowel(word[n+1]))
                  code[len++] = 'H';// Hvowel
              break;

            case 'F': case 'J' : case 'L' :
            case 'M': case 'N' : case 'R' :
              code[len++] = symb;
	          break ;

            case 'K' :
              if( n > start) { // not initial
                if( word[n -1] != 'C' )
                     code[len++] = symb;
              }
              else
	              code[len++] = symb; // initial K
              break ;

            case 'P' :
              if((n + 1 < end) &&  // PH -> F
                 (word[n+1] == 'H'))
	              code[len++] = 'F';
              else
	              code[len++] = symb;
              break ;

            case 'Q' :
             code[len++] = 'K';
	         break;

            case 'S' :

              if(startsWith(word,n,end,SH) ||
                 startsWith(word,n,end,SIO) ||
                 startsWith(word,n,end,SIA))
	              code[len++] = 'X';
              else
	              code[len++] = 'S';
              break ;

            case 'T' :
              if(startsWith(word,n,end,TIA) ||
                 startsWith(word,n,end,TIO) )
              {
                    code[len++] = 'X'; break;
              }

	         if( startsWith(word,n,end,TCH))
		         break;

              // substitute numeral 0 for TH (resembles theta after all)
              if( startsWith(word,n,end,TH))
	              code[len++] = '0';     //  ZERO (not OH)
              else
	              code[len++] = 'T';
              break ;

            case 'V' :
              code[len++] = 'F';
	          break ;

            case 'W' : case 'Y' : // silent if not followed by vowel
              if((n+1 < end) &&
                 isvowel(word[n+1]))
                   code[len++] = symb;
              break ;

            case 'X' :
	         code[len++] = 'K';
	         code[len++] = 'S';
             break ;

            case 'Z' :
             code[len++] = 'S';
	         break ;
          } // end switch

          n++;
      }

	  if (len > maxCodeLen) len = maxCodeLen;
      return new String(code,0,len);
    } // end static method metaPhone()


	public static void main(String[] args) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		Metaphone m1 = new Metaphone(6);
		Metaphone2 m2 = new Metaphone2(6);

		for (;;) {
			String line = in.readLine();

			System.out.print ("old ");
			System.out.println (m1.encode(line));
			System.out.print ("new ");
			System.out.println (m2.encode(line));
		}
	}
}

