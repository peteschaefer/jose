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

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;

/**
 * ByteUtil
 *
 * @author Peter Schäfer
 */

public class ByteUtil
{


    public static void writeShort(byte[] b, int i, short v)
    {
        b[i]    =  (byte)((v >>> 8) & 0xFF);
        b[i+1]  =  (byte)(v & 0xFF);
    }

	public static void writeShort(ByteArrayOutputStream b, short v)
	{
	    b.write((v >>> 8) & 0xFF);
	    b.write(v & 0xFF);
	}

    public static void writeInt(byte[] b, int i, int v)
    {
        b[i]    =  (byte)((v >>> 24) & 0xFF);
        b[i+1]  =  (byte)((v >>> 16) & 0xFF);
        b[i+2]  =  (byte)((v >>> 8) & 0xFF);
        b[i+3]  =  (byte)(v & 0xFF);
    }

	public static void writeInt(ByteArrayOutputStream b, int v)
	{
	    b.write((v >>> 24) & 0xFF);
	    b.write((v >>> 16) & 0xFF);
	    b.write((v >>> 8) & 0xFF);
	    b.write(v & 0xFF);
	}

    public static short readShort(byte[] b, int i)
    {
	    int i1 = ((int)b[i] & 0xFF) << 8;
	    int i2 = ((int)b[i+1] & 0xFF);
        return (short)(i1 | i2);
    }

    public static int readInt(byte[] b, int i)
    {
	    int i1 = ((int)b[i] & 0xFF) << 24;
	    int i2 = ((int)b[i+1] & 0xFF) << 16;
	    int i3 = ((int)b[i+2] & 0xFF) << 8;
	    int i4 = ((int)b[i+3] & 0xFF);

	    return (i1 | i2 | i3 | i4);
    }
}