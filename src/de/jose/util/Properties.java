/*
 * @(#)Properties.java	1.84 04/05/18
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package de.jose.util;

import java.io.*;
import java.util.HashMap;

/**
 * The <code>Properties</code> class represents a persistent set of
 * properties. The <code>Properties</code> can be saved to a stream
 * or loaded from a stream. Each key and its corresponding value in
 * the property list is a string.
 * <p>
 * A property list can contain another property list as its
 * "defaults"; this second property list is searched if
 * the property key is not found in the original property list.
 * <p>
 * Because <code>Properties</code> inherits from <code>Hashtable</code>, the
 * <code>put</code> and <code>putAll</code> methods can be applied to a
 * <code>Properties</code> object.  Their use is strongly discouraged as they
 * allow the caller to insert entries whose keys or values are not
 * <code>Strings</code>.  The <code>setProperty</code> method should be used
 * instead.  If the <code>store</code> or <code>save</code> method is called
 * on a "compromised" <code>Properties</code> object that contains a
 * non-<code>String</code> key or value, the call will fail.
 * <p>
 * <a name="encoding"></a>
 * <p> The {@link #load load} and {@link #store store} methods load and store
 * properties in a simple line-oriented format specified below.  This format
 * uses the ISO 8859-1 character encoding.  Characters that cannot be directly
 * represented in this encoding can be written using
 * <a href="http://java.sun.com/docs/books/jls/html/3.doc.html#100850">Unicode escapes</a>
 * ; only a single 'u' character is allowed in an escape
 * sequence. The native2ascii tool can be used to convert property files to and
 * from other character encodings.
 * <p>
* <p> The {@link #loadFromXML(InputStream)} and {@link
 * #storeToXML(OutputStream, String, String)} methods load and store properties
 * in a simple XML format.  By default the UTF-8 character encoding is used,
 * however a specific encoding may be specified if required.  An XML properties
 * document has the following DOCTYPE declaration:
 *
 * <pre>
 * &lt;!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt;
 * </pre>
 * Note that the system URI (http://java.sun.com/dtd/properties.dtd) is
 * <i>not</i> accessed when exporting or importing properties; it merely
 * serves as a string to uniquely identify the DTD, which is:
 * <pre>
 *    &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 *
 *    &lt;!-- DTD for properties --&gt;
 *
 *    &lt;!ELEMENT properties ( comment?, entry* ) &gt;
 *
 *    &lt;!ATTLIST properties version CDATA #FIXED "1.0"&gt;
 *
 *    &lt;!ELEMENT comment (#PCDATA) &gt;
 *
 *    &lt;!ELEMENT entry (#PCDATA) &gt;
 *
 *    &lt;!ATTLIST entry key CDATA #REQUIRED&gt;
 * </pre>
 *
 * @see <a href="../../../tooldocs/solaris/native2ascii.html">native2ascii tool for Solaris</a>
 * @see <a href="../../../tooldocs/windows/native2ascii.html">native2ascii tool for Windows</a>
 *
 * @author  Arthur van Hoff
 * @author  Michael McCloskey
 * @version 1.84, 05/18/04
 * @since   JDK1.0
 */
public class Properties extends java.util.Properties {

    /**
     * Creates an empty property list with no default values.
     */
    public Properties() {
	super();
    }

    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param   defaults   the defaults.
     */
    public Properties(Properties defaults) {
	super(defaults);
    }


    /**
     */
    public synchronized void load(InputStream inStream) throws IOException {
	    load(new InputStreamReader(inStream));
    }

    public synchronized void load(Reader inStream) throws IOException {
        char[] convtBuf = new char[1024];
        LineReader lr = new LineReader(inStream);

        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

	    //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' ||  c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' ||  c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' &&  c != '\f') {
                    if (!hasSep && (c == '=' ||  c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
	    put(key, value);
	}
    }

    /* read in a "logical line" from input stream, skip all comment and
     * blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    class LineReader {
        public LineReader(Reader inStream) {
            this.inStream = inStream;
	}
        char[] inBuf = new char[8192];
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        Reader inStream;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
	    boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = inStream.read(inBuf);
		    inOff = 0;
		    if (inLimit <= 0) {
			if (len == 0 || isCommentLine) {
			    return -1;
			}
			return len;
		    }
		}
                //The line below is equivalent to calling a
                //ISO8859-1 decoder.
		c = inBuf[inOff++];
                if (skipLF) {
                    skipLF = false;
		    if (c == '\n') {
		        continue;
		    }
		}
		if (skipWhiteSpace) {
		    if (c == ' ' || c == '\t' || c == '\f') {
			continue;
		    }
		    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
			continue;
		    }
		    skipWhiteSpace = false;
		    appendedLineBegin = false;
		}
		if (isNewLine) {
		    isNewLine = false;
		    if (c == '#' || c == '!') {
			isCommentLine = true;
			continue;
		    }
		}

		if (c != '\n' && c != '\r') {
		    lineBuf[len++] = c;
		    if (len == lineBuf.length) {
		        int newLength = lineBuf.length * 2;
		        if (newLength < 0) {
		            newLength = Integer.MAX_VALUE;
		        }
			char[] buf = new char[newLength];
			System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
			lineBuf = buf;
		    }
		    //flip the preceding backslash flag
		    if (c == '\\') {
			precedingBackslash = !precedingBackslash;
		    } else {
			precedingBackslash = false;
		    }
		}
		else {
		    // reached EOL
		    if (isCommentLine || len == 0) {
			isCommentLine = false;
			isNewLine = true;
			skipWhiteSpace = true;
			len = 0;
			continue;
		    }
		    if (inOff >= inLimit) {
			inLimit = inStream.read(inBuf);
			inOff = 0;
			if (inLimit <= 0) {
			    return len;
			}
		    }
		    if (precedingBackslash) {
			len -= 1;
			//skip the leading whitespace characters in following line
			skipWhiteSpace = true;
			appendedLineBegin = true;
			precedingBackslash = false;
			if (c == '\r') {
                            skipLF = true;
			}
		    } else {
			return len;
		    }
		}
	    }
	}
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private String loadConvert (char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
	        newLen = Integer.MAX_VALUE;
	    }
	    convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
		    for (int i=0; i<4; i++) {
		        aChar = in[off++];
		        switch (aChar) {
		          case '0': case '1': case '2': case '3': case '4':
		          case '5': case '6': case '7': case '8': case '9':
		             value = (value << 4) + aChar - '0';
			     break;
			  case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
			     value = (value << 4) + 10 + aChar - 'a';
			     break;
			  case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
			     value = (value << 4) + 10 + aChar - 'A';
			     break;
			  default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                     }
                    out[outLen++] = (char)value;
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    out[outLen++] = aChar;
                }
            } else {
	        out[outLen++] = (char)aChar;
            }
        }
        return new String (out, 0, outLen);
    }

}
