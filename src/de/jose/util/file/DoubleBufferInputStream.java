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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * EXPERIMENTAL
 */
public class DoubleBufferInputStream
        extends FilterInputStream
{
    /** 2 buffers  */
    private byte[][]    buffer;
    /** buffer fill */
    private int[]       fill;
    /** current buffer  */
    private int         current;
    /** current position    */
    private int         pos;
    /** fetch thread    */
    private FetchThread fetch;
    /** at EOF ?    */
    private boolean     eof;

    public DoubleBufferInputStream(InputStream input, int bufferSize)
    {
        super(input);

        buffer = new byte[2][];
        buffer[0] = new byte[bufferSize/2];
        buffer[1] = new byte[bufferSize/2];

        fill = new int[2];
        fill[0] = fill[1] = 0;

        current = 0;
        pos = 0;
        eof = false;

        fetch = new FetchThread();
        fetch.setPriority(Thread.MIN_PRIORITY);
        fetch.start();
    }

    public int read()
    {
        if (eof) return -1;

        synchronized (buffer[current]) {
            if (pos < fill[current])
                return buffer[current][pos++];
        }
        //  else: switch buffers
        switchBuffers();

        synchronized (buffer[current]) {
            if (pos < fill[current])
                return buffer[current][pos++];
        }

        //  else:
        eof = true;
        return -1;
    }

    private void switchBuffers()
    {
        current = 1-current;
        pos = 0;
        fill[1-current] = 0;    //  indicator for FetchThread
        fetch.interrupt();
    }

    public int read(byte[] result, int offset, int len)
    {
        if (eof) return -1;

        int total = 0;
        while (len > 0)
        {
            int count = readChunk(result,offset,len);
            if (count < 0) break;

            len -= count;
            offset += count;
            total += count;
        }

        return total;
    }

    private int readChunk(byte[] result, int offset, int len)
    {
        if (eof) return -1;

         synchronized (buffer[current]) {
             if (pos < fill[current]) {
                int count = fill[current]-pos;
                if (len < count) {
                    System.arraycopy(buffer[current], pos, result, offset, len);
                    pos += len;
                    return len;
                }
                else {
                    System.arraycopy(buffer[current], pos, result,offset,count);
                    switchBuffers();
                    return count;
                }
             }
         }
         //  else: switch buffers
         switchBuffers();

         synchronized (buffer[current]) {
             if (pos < fill[current]) {
                int count = fill[current]-pos;
                if (len < count) {
                    System.arraycopy(buffer[current], pos, result, offset, len);
                    pos += len;
                    return len;
                }
                else {
                    System.arraycopy(buffer[current], pos, result,offset,count);
                    switchBuffers();
                    return count;
                }
             }
         }

         //  else:
         eof = true;
         return -1;
    }

    private class FetchThread extends Thread
    {
        protected IOException exception;

        public void run()
        {
            try {
                for(;;) {
                    //  fill the unused buffer
                    while (fill[1-current] != 0)
                    {
                        if (eof) return;
                        try {
                            sleep(1000);
                        } catch (InterruptedException intex) {

                        }
                    }

                    synchronized (buffer[1-current]) {
                        fill[1-current] = in.read(buffer[1-current],0,buffer[1-current].length);
                    }
                }
            } catch (IOException ioex) {
                exception = ioex;
            }
        }
    }
}
