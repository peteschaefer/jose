/*
 * @(#)AbstractFilter.java	1.11 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package de.jose.util.rtf;

import java.io.*;
import java.lang.*;

/**
 * A generic superclass for streams which read and parse text
 * consisting of runs of characters interspersed with occasional
 * ``specials'' (formatting characters).
 *
 * <p> Most of the functionality
 * of this class would be redundant except that the
 * <code>ByteToChar</code> converters
 * are suddenly private API. Presumably this class will disappear
 * when the API is made public again. (sigh) That will also let us handle
 * multibyte character sets...
 *
 * <P> A subclass should override at least <code>write(char)</code>
 * and <code>writeSpecial(int)</code>. For efficiency's sake it's a
 * good idea to override <code>write(String)</code> as well. The subclass'
 * initializer may also install appropriate translation and specials tables.
 *
 * @see OutputStream
 */
abstract class AbstractFilter extends OutputStream
{
    /** A table mapping bytes to characters */
    protected char translationTable[];
    /** A table indicating which byte values should be interpreted as
     *  characters and which should be treated as formatting codes */
    protected boolean specialsTable[];

    /** A translation table which does ISO Latin-1 (trivial) */
    static final char latin1TranslationTable[];
    /** A specials table which indicates that no characters are special */
    static final boolean noSpecialsTable[];
    /** A specials table which indicates that all characters are special */
    static final boolean allSpecialsTable[];

    static {
      int i;

      noSpecialsTable = new boolean[256];
      for (i = 0; i < 256; i++)
	noSpecialsTable[i] = false;

      allSpecialsTable = new boolean[256];
      for (i = 0; i < 256; i++)
	allSpecialsTable[i] = true;

      latin1TranslationTable = new char[256];
      for (i = 0; i < 256; i++)
	latin1TranslationTable[i] = (char)i;
    }

    /**
     * A convenience method that reads text from a FileInputStream
     * and writes it to the receiver.
     * The format in which the file
     * is read is determined by the concrete subclass of
     * AbstractFilter to which this method is sent.
     * <p>This method does not close the receiver after reaching EOF on
     * the input stream.
     * The user must call <code>close()</code> to ensure that all
     * data are processed.
     *
     * @param in      An InputStream providing text.
     */
    public void readFromStream(InputStream in)
      throws IOException
    {
        byte buf[];
        int count;

	buf = new byte[16384];

	while(true) {
	    count = in.read(buf);
	    if (count < 0)
	        break;

	    this.write(buf, 0, count);
	}
    }

    public void readFromReader(Reader in)
      throws IOException
    {
        char buf[];
        int count;

	buf = new char[2048];

	while(true) {
	    count = in.read(buf);
	    if (count < 0)
	        break;
	    for (int i = 0; i < count; i++) {
	      this.write(buf[i]);
	    }
	}
    }

    public AbstractFilter()
    {
        translationTable = latin1TranslationTable;
	specialsTable = noSpecialsTable;
    }

    /**
     * Implements the abstract method of OutputStream, of which this class
     * is a subclass.
     */
    public void write(int b)
      throws IOException
    {
      if (b < 0)
	b += 256;
      if (specialsTable[b])
	writeSpecial(b);
      else {
	char ch = translationTable[b];
	if (ch != (char)0)
	  write(ch);
      }
    }

    /**
     * Implements the buffer-at-a-time write method for greater
     * efficiency.
     *
     * <p> <strong>PENDING:</strong> Does <code>write(byte[])</code>
     * call <code>write(byte[], int, int)</code> or is it the other way
     * around?
     */
    public void write(byte[] buf, int off, int len)
      throws IOException
    {
      StringBuffer accumulator = null;
      while (len > 0) {
        short b = (short)buf[off];

	// stupid signed bytes
        if (b < 0)
            b += 256;

	if (specialsTable[b]) {
	  if (accumulator != null) {
	    write(accumulator.toString());
	    accumulator = null;
	  }
	  writeSpecial(b);
	} else {
	  char ch = translationTable[b];
	  if (ch != (char)0) {
	    if (accumulator == null)
	      accumulator = new StringBuffer();
	    accumulator.append(ch);
	  }
	}

	len --;
	off ++;
      }

      if (accumulator != null)
	write(accumulator.toString());
    }

    /**
     * Hopefully, all subclasses will override this method to accept strings
     * of text, but if they don't, AbstractFilter's implementation
     * will spoon-feed them via <code>write(char)</code>.
     *
     * @param s The string of non-special characters written to the
     *          OutputStream.
     */
    public void write(String s)
      throws IOException
    {
      int index, length;

      length = s.length();
      for(index = 0; index < length; index ++) {
	write(s.charAt(index));
      }
    }

    /**
     * Subclasses must provide an implementation of this method which
     * accepts a single (non-special) character.
     *
     * @param ch The character written to the OutputStream.
     */
    protected abstract void write(char ch) throws IOException;

    /**
     * Subclasses must provide an implementation of this method which
     * accepts a single special byte. No translation is performed
     * on specials.
     *
     * @param b The byte written to the OutputStream.
     */
    protected abstract void writeSpecial(int b) throws IOException;
}


