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

package de.jose.store;

public class IntBuffer
{
    /** size of a block */
    protected int blockSize;
    /** list of blocks  */
    protected int[][] data;


    public IntBuffer(int blockSz, int initialCapacity)
    {
        blockSize = blockSz;
        data = new int[(initialCapacity+blockSize-1)/blockSize][];
    }

    public final void ensureCapacity(int capacity)
    {
        int needed = (capacity+blockSize-1)/blockSize;
        if (needed > data.length) {
            int newSize = Math.max(2*data.length,32);
            while (newSize < needed) newSize *= 2;
            int[][] newData = new int[newSize][];
            System.arraycopy(data,0, newData,0, data.length);
            data = newData;
        }
    }

    public final int get(int index)
    {
        int bidx = index/blockSize;
        if (bidx >= data.length) return 0;

        int[] block = data[bidx];
        if (block==null)
            return 0;
        else
            return block[index%blockSize];
    }

    public final void set(int index, int value)
    {
        int bidx = index/blockSize;
        if (bidx >= data.length) ensureCapacity(index+1);

        int[] block = data[bidx];
        if (block==null)
            block = data[bidx] = new int[blockSize];
        block[index%blockSize] = value;
    }

    public final void clear() {
        data = new int[0][];
    }
}
