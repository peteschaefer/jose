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

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator 
        implements Iterator
{
    protected Enumeration en;

    public EnumerationIterator(Enumeration anEnum)
    {
        en = anEnum;
    }

    public boolean hasNext() {
        return en.hasMoreElements();
    }

    public Object next() {
        return en.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
