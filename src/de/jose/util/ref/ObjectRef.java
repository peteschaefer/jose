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
 
public class ObjectRef 
{
    public Object value;

    public ObjectRef(Object val)                { value = val; }

    public final Object get()                   { return value; }

    public final void set(Object newValue)      { value = newValue; }

    public final boolean isNull()               { return value==null; }

    public final boolean equals(Object that)
    {
        return equals(this,that);
    }

    public final int hashCode()
    {
        if (value==null)
            return 0;
        else
            return value.hashCode();
    }

    public static final boolean equals(Object a, Object b)
    {
        while (a instanceof ObjectRef) a = ((ObjectRef)a).value;
        while (b instanceof ObjectRef) b = ((ObjectRef)b).value;

        if (a==null)
            return b==null;
        else
            return a.equals(b);
    }
}
