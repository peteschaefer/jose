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

import java.util.StringTokenizer;
/**
  * Encodes words using the soundex phonetic algorithm.
  * The primary method to call is Soundex.encode(String).<p>
  * The main method encodes arguments to System.out.
  * @author Aaron Hansen
 */

public class Soundex
{

  // Public Fields
  // -------------

	/**	default code lenght */
  public static final int		DEFAULT_LENGTH		= 4;
	/**	unlimited code length	 */
  public static final int		NO_MAX				= -1;
  
  public static final boolean	DROP_LAST_S			= true;
  public static final boolean	DONT_DROP_LAST_S	= false;
  
  // Protected Fields
  // ----------------
  
  /**	If true, the final 's' of the word being encoded is dropped.   */
  protected boolean fDropLastS;

  /**	Length of code to build. (< 0 means no limit)   */
  protected int fLength;
  /**	Soundex code table.  */
  protected static int[] fSoundex = {
      -1, //a 
       1, //b
       2, //c 
       3, //d
      -1, //e 
       1, //f
       2, //g 
      -1, //h
      -1, //i 
       2, //j
       2, //k
       4, //l
       5, //m
       5, //n
      -1, //o
       1, //p
       2, //q
       6, //r
       2, //s
       3, //t
      -1, //u
       1, //v
      -1, //w
       2, //x
      -1, //y
       2  //z
   };
  
  /**
   * creates a new Soundex encoder
   * 
   * @param codeLength	length of generated code (< 0 means no limit)
   * @param dropLastS	ignore "s" at the end of words
   */
  public Soundex(int codeLength, boolean dropLastS)
  {
	fLength = codeLength;
	fDropLastS = dropLastS;
  }

	/**
	 * creates a new soundex encoder with default settings
	 */
  public Soundex() {
	  this(DEFAULT_LENGTH, DROP_LAST_S);
  }
 
  // Public Methods
  // --------------
   /**
    * Returns the soundex code for the specified word.
    * @param word The word to encode.
    */
  public String encode(String word) 
  {
	  char[] wordc = word.toCharArray(); 
	  int len = encode(wordc, wordc.length);
	  return new String(wordc,0,len);
  }
  
    /**
    * Returns the soundex code for the specified word.
    * works on the passed cahracter array
    * @param word The word to encode.
    */
  public int encode(char[] word) 
  {
	  return encode(word, word.length);
  }
  
  /**
   * Retuns a list of soundex codes for a specified phrase
   */
  public String encodePhrase(String phrase) {
	  StringBuffer buf = new StringBuffer();
	  
	  StringTokenizer tok = new StringTokenizer(phrase, " -");
	  while (tok.hasMoreTokens()) {
		if (buf.length() > 0) buf.append(" ");
		buf.append(encode(tok.nextToken()));
	  }
	
	  return buf.toString();
  }
 
  private int encode(char[] word, int len)
  {
	//	trim
	while (len > 0 && Character.isWhitespace(word[0]))
		len = skip1(word,0,len);
	while (len > 0 && Character.isWhitespace(word[len-1]))
		len--;
		   
    if (fDropLastS && len > 0
		&& (word[len-1] == 's' || word[len-1] == 'S'))
		len--;
	
	CharUtil.toLowerCase(word);
	
	len = reduce(word,len);
    
	int max = fLength - 1; //max codes to create (less the first char)
    if (fLength < 0) //if NO_MAX
		max = len; //wordLength was the max possible size.
	
	//	first char remains unchanged !
    int sofar = 1; //how many codes have been created
    int code = 0; 
	int current = 1;
    while ((current < len) && (sofar < max)) {
      code = getCode(word[current++]);
      if (code > 0)
        word[sofar++] = Character.forDigit(code,10);
    }
	return sofar;
  }

    /**
    * Returns the Soundex code for the specified character.
    * @param ch Should be between A-Z or a-z
    * @return -1 if the character has no phonetic code.
    */
  private final int getCode(char ch) {
    int arrayidx = -1;
    if (('a' <= ch) || (ch <= 'z'))
      arrayidx = (int)ch - (int)'a';
    else if (('A' <= ch) || (ch <= 'Z'))
      arrayidx = (int)ch - (int)'A';
    if ((arrayidx >= 0) && (arrayidx < fSoundex.length))
      return fSoundex[arrayidx];
    else
      return -1;
  }

    /**
    * Encodes the args to stdout.
    
  public static void main(String[] strings) {
    if ((strings == null) || (strings.length == 0)) {
      System.out.println(
        "Specify some words and this will display a soundex code for each.");
      System.exit(0);
      }
    Soundex sndx = new Soundex();
    for (int i = 0; i < strings.length; i++)
      System.out.println(sndx.encode(strings[i]));
    }
*/
  
  // Protected Methods
  // -----------------
  
  private int skip1(char[] word, int at, int len) {
	if ((len-at) > 1)
		System.arraycopy(word,at+1, word,at, len-at-1);
	return len-1;
  }
  
    /**
    * Removes adjacent sounds.
    */
  protected int reduce(char[] word, int len) {
	char ch = word[0];
    int currentCode = getCode(ch);
	int lastCode = currentCode;

	int current = 1;
	while (current < len) {
      ch = word[current];
      currentCode = getCode(ch);
      
	  if ((currentCode != lastCode) && (currentCode >= 0)) {
        lastCode = currentCode;
		current++;
      }
	  else
		len = skip1(word,current,len);
    }
    
     return len;
  }
}//Soundex
