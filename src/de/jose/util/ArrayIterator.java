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

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Iterator on an arbitrary Array
 * (Object[], or primitive array)
 *
 *
 */
public class ArrayIterator
        implements Iterator
{
    protected Object array;
    protected int current;
    protected int max;

    public ArrayIterator(Object anArray, int start, int end)
    {
        array = anArray;
        current = start;
        max = end;

        if (current < 0) throw new ArrayIndexOutOfBoundsException("start value < 0");
        if (max > Array.getLength(array)) throw new ArrayIndexOutOfBoundsException("end value too large");
        if (current > max) throw new ArrayIndexOutOfBoundsException("start value > end value");
    }

    public ArrayIterator(Object anArray)
    {
        this(anArray,0,Array.getLength(anArray));
    }

    public boolean hasNext() {
        return current < max;
    }

    public Object next() {
        return Array.get(array, current++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
