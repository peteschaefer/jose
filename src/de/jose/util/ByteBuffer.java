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

import java.util.Arrays;

/**
 *
 * @author Peter Schäfer
 */

public class ByteBuffer
{
    protected byte[] data;
    int len;

    public ByteBuffer(int capacity)
    {
        data = new byte[capacity];
        len = 0;
    }

    public int length()                             { return len; }

    public byte[] getValue()                        { return data; }

    public byte getByte(int idx)                    { return data[idx]; }

    public void setByte(int idx, byte value)        { data[idx] = value; }


    public void append(byte b)      {
        ensureCapacity(len+1);
        data[len++] = b;
    }

    public void append(int i) {
        ensureCapacity(len+1);
        data[len++] = (byte)i;
    }

    public void append(char c) {
        ensureCapacity(len+1);
        data[len++] = (byte)c;
    }

    public void append(byte[] b, int from, int slen)
    {
        ensureCapacity(len+slen);
        System.arraycopy(b,from, data,len, slen);
        len += slen;
    }

    public void setLength(int newLen)
    {
        if (newLen > data.length) {
            ensureCapacity(newLen);
            Arrays.fill(data,len,newLen,(byte)0);
        }
        len = newLen;
    }

    public void ensureCapacity(int cap)
    {
        if (cap > data.length) {
            cap = nextPow2(cap);
            byte[] newData = new byte[cap];
            System.arraycopy(data,0, newData,0, len);
            data = newData;
        }
    }

    private int nextPow2(int x)
    {
        int y = 1;
        while (y < x)
            y <<= 1;
        return y;
    }
}
