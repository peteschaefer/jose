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

package de.jose.util.ref;
 
public class IntRef 
        extends Number
{
    public int value;

    public IntRef(int val)                   { value = val; }

    public final int get()                   { return value; }

    public final void set(int newValue)      { value = newValue; }

    public final void add(int add)           { value += add; }

    public final void sub(int sub)           { value -= sub; }

    public final boolean isZero()            { return value == 0; }

    public final int hashCode()              { return value; }

    public final boolean equals(Object obj)
    {
        if (obj instanceof Number)
            return ((Number)obj).intValue() == value;
        else
            throw new IllegalArgumentException("Number expected");
    }

    //  implements Number:

    public final int intValue()              { return value; }

    public final long longValue()            { return value; }

    public final float floatValue()          { return value; }

    public final double doubleValue()        { return value; }
}
