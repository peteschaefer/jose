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

/*                                                   
 * A class to generate phonetic code
 * reference: Computer Language of Dec. 1990, p 39
 *  "Hanging on the MetaphoneOld" by Lawrence Philips
 *
 * This Java implementation, Copyright 1997, William B. Brogden
 * is hereby released for all uses. I would appreciate hearing about it
 * if you find a good use for the class.  December, 1997
 *  wbrogden@bga.com    CompuServe  75415,610
 */

/*

The MetaphoneOld Rules

MetaphoneOld reduces the alphabet to 16 consonant sounds:

B X S K J T F H L M N P R 0 W Y

That isn't an O but a zero - representing the 'th' sound.

Transformations
MetaphoneOld uses the following transformation rules:
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
 Lawrence Philips in an article entitled "Hanging on the MetaphoneOld"
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

public class MetaphoneOld
{
	static String vowels = "AEIOU";
	static String frontv = "EIY";
	static String varson = "CSPTG";
	
	private int maxCodeLen;
	
	public MetaphoneOld() {
		this(32768);
	}
	
	public MetaphoneOld(int max) {
		maxCodeLen = max;
	}
	
	public String encode(String s) {
		return encode(s.toCharArray());
	}

	public void encode(StringBuffer buf) {
		char[] chars = new char[buf.length()];
		buf.getChars(0,buf.length(), chars,0);
		buf.setLength(0);
		buf.append(encode(chars));
	}

    public String encode(char[] inwd){
      int mtsz = 0  ;
      boolean hard = false ;
      if(( inwd == null ) ||
         ( inwd.length == 0 )) return "" ;
	  CharUtil.toUpperCase(inwd);
      // single character is itself
      if( inwd.length == 1 ) return new String(inwd);
      //
      //char[] inwd = txt.toUpperCase().toCharArray() ;
      //
      String tmpS ;
      StringBuffer local = new StringBuffer( 40 ); // manipulate
      StringBuffer code = new StringBuffer( 10 ) ; //   output
      // handle initial 2 characters exceptions
      switch( inwd[0] ){
        case 'K': case 'G' : case 'P' : /* looking for KN, etc*/
          if( inwd[1] == 'N')local.append(inwd, 1, inwd.length - 1 );
          else local.append( inwd );
          break;
        case 'A': /* looking for AE */
          if( inwd[1] == 'E' )local.append(inwd, 1, inwd.length - 1 );
          else local.append( inwd );
          break;
        case 'W' : /* looking for WR or WH */
          if( inwd[1] == 'R' ){   // WR -> R
            local.append(inwd, 1, inwd.length - 1 ); break ;
          }
          if( inwd[1] == 'H'){
            local.append(inwd, 1, inwd.length - 1 );
            local.setCharAt( 0,'W'); // WH -> W
          }
          else local.append( inwd );
          break;
        case 'X' : /* initial X becomes S */
          inwd[0] = 'S' ;local.append( inwd );
          break ;
        default :
          local.append( inwd );
      } // now local has working string with initials fixed
      int wdsz = local.length();
      int n = 0 ;
      while((mtsz < maxCodeLen ) && // max code size of 4 works well
            (n < wdsz ) ){
        char symb = local.charAt(n) ;
        // remove duplicate letters except C
        if(( symb != 'C' ) &&
           (n > 0 ) && ( local.charAt(n - 1 ) == symb )) n++ ;
        else{ // not dup
          switch( symb ){
            case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
              if( n == 0 ) { code.append(symb );mtsz++;
              }
              break ; // only use vowel if leading char
            case 'B' :
              if( (n > 0 ) &&
                  !(n + 1 == wdsz ) && // not MB at end of word
                  ( local.charAt(n - 1) == 'M')) {
                    code.append(symb);
                  }
              else code.append(symb);
              mtsz++ ;
              break ;
            case 'C' : // lots of C special cases
              /* discard if SCI, SCE or SCY */
              if( ( n > 0 ) &&
                  ( local.charAt(n-1) == 'S' ) &&
                  ( n + 1 < wdsz ) &&
                  ( frontv.indexOf( local.charAt(n + 1)) >= 0 )){ break ;}
              tmpS = local.toString();
              if( tmpS.indexOf("CIA", n ) == n ) { // "CIA" -> X
                 code.append('X' ); mtsz++; break ;
              }
              if( ( n + 1 < wdsz ) &&
                  (frontv.indexOf( local.charAt(n+1) )>= 0 )){
                 code.append('S');mtsz++; break ; // CI,CE,CY -> S
              }
              if(( n > 0) &&
                 ( tmpS.indexOf("SCH",n-1 )== n-1 )){ // SCH->sk
                 code.append('K') ; mtsz++;break ;
              }
              if( tmpS.indexOf("CH", n ) == n ){ // detect CH
                if((n == 0 ) &&
                   (wdsz >= 3 ) &&    // CH consonant -> K consonant
                   (vowels.indexOf( local.charAt( 2) ) < 0 )){
                     code.append('K');
                }
                else { code.append('X'); // CHvowel -> X
                }
                mtsz++;
              }
              else { code.append('K' );mtsz++;
              }
              break ;
            case 'D' :
              if(( n + 2 < wdsz )&&  // DGE DGI DGY -> J
                 ( local.charAt(n+1) == 'G' )&&
                 (frontv.indexOf( local.charAt(n+2) )>= 0)){
                    code.append('J' ); n += 2 ;
              }
              else { code.append( 'T' );
              }
              mtsz++;
              break ;
            case 'G' : // GH silent at end or before consonant
              if(( n + 2 == wdsz )&&
                 (local.charAt(n+1) == 'H' )) break ;
              if(( n + 2 < wdsz ) &&
                 (local.charAt(n+1) == 'H' )&&
                 (vowels.indexOf( local.charAt(n+2)) < 0 )) break ;
              tmpS = local.toString();
              if((n > 0) &&
                 ( tmpS.indexOf("GN", n ) == n)||
                 ( tmpS.indexOf("GNED",n) == n )) break ; // silent G
              if(( n > 0 ) &&
                 (local.charAt(n-1) == 'G')) hard = true ;
              else hard = false ;
              if((n+1 < wdsz) &&
                 (frontv.indexOf( local.charAt(n+1) ) >= 0 )&&
                 (!hard) ) code.append( 'J' );
              else code.append('K');
              mtsz++;
              break ;
            case 'H':
              if( n + 1 == wdsz ) break ; // terminal H
              if((n > 0) &&
                 (varson.indexOf( local.charAt(n-1)) >= 0)) break ;
              if( vowels.indexOf( local.charAt(n+1)) >=0 ){
                  code.append('H') ; mtsz++;// Hvowel
              }
              break;
            case 'F': case 'J' : case 'L' :
            case 'M': case 'N' : case 'R' :
              code.append( symb ); mtsz++; break ;
            case 'K' :
              if( n > 0 ){ // not initial
                if( local.charAt( n -1) != 'C' ) {
                     code.append(symb );
                }
              }
              else   code.append( symb ); // initial K
              mtsz++ ;
              break ;
            case 'P' :
              if((n + 1 < wdsz) &&  // PH -> F
                 (local.charAt( n+1) == 'H'))code.append('F');
              else code.append( symb );
              mtsz++;
              break ;
            case 'Q' :
              code.append('K' );mtsz++; break ;
            case 'S' :
              tmpS = local.toString();
              if((tmpS.indexOf("SH", n )== n) ||
                 (tmpS.indexOf("SIO",n )== n) ||
                 (tmpS.indexOf("SIA",n )== n)) code.append('X');
              else code.append( 'S' );
              mtsz++ ;
              break ;
            case 'T' :
              tmpS = local.toString(); // TIA TIO -> X
              if((tmpS.indexOf("TIA",n )== n)||
                 (tmpS.indexOf("TIO",n )== n) ){
                    code.append('X'); mtsz++; break;
              }
              if( tmpS.indexOf("TCH",n )==n) break;
              // substitute numeral 0 for TH (resembles theta after all)
              if( tmpS.indexOf("TH", n )==n) code.append('0');
              else code.append( 'T' );
              mtsz++ ;
              break ;
            case 'V' :
              code.append('F'); mtsz++;break ;
            case 'W' : case 'Y' : // silent if not followed by vowel
              if((n+1 < wdsz) &&
                 (vowels.indexOf( local.charAt(n+1))>=0)){
                    code.append( symb );mtsz++;
              }
              break ;
            case 'X' :
              code.append('K'); code.append('S');mtsz += 2;
              break ;
            case 'Z' :
              code.append('S'); mtsz++; break ;
          } // end switch
          n++ ;
        } // end else from symb != 'C'
        if( mtsz > maxCodeLen )code.setLength(maxCodeLen);
      }
      return code.toString();
    } // end static method metaPhone()

}
   
