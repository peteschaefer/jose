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

package de.jose.util.file;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 *
 * @author Peter Schäfer
 */
public class LinePrintWriter
        extends PrintWriter
{
	/** current line    */
	protected int line;
	/** current column  */
	protected int column;

	public LinePrintWriter(OutputStream out) {
		super(out);
	}

	public LinePrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	public LinePrintWriter(Writer out) {
		super(out);
	}

	public LinePrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}


	public final int line()         { return line; }
	public final int column()       { return column; }

	public boolean breakIf(int cols) {
		if (column >= cols) {
			println();
			return true;
		}
		else
			return false;
	}

    public boolean newLine() {
        return breakIf(1);
    }

	public void println() {
		super.println();
		column = 0;
		line++;
	}

	public void write(int c) {
		super.write(c);
		column++;
	}

	public void write(char buf[], int off, int len) {
		super.write(buf, off, len);
		column += len;
	}

	public void write(String s, int off, int len) {
		super.write(s, off, len);
		column += len;
	}
}
