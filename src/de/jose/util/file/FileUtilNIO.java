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

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File and stream utilities for Java 1.4
 * uses NIO
 *
 * @author Peter Schäfer
 */
public class FileUtilNIO 
{

    /**
	 * copies the contents of a file
	 */
	public static void copyFile(File src, File dst)
		throws IOException
	{
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);

        copyChannel(in.getChannel(), src.length(), (WritableByteChannel)out.getChannel());

        in.close();
        out.close();
	}

	/**
	 * copies the contents of a file
	 */
	public static long copyFile(File src, OutputStream out)
		throws IOException
	{
		FileInputStream in = new FileInputStream(src);

        WritableByteChannel cout = Channels.newChannel(out);
		long result = copyChannel(in.getChannel(), src.length(), cout);

		in.close();
		return result;
	}

    /**
	 * copies the contens of a stream to a file
	 */
	public static long copyStream(InputStream in, long length, File out)
		throws IOException
	{
        FileOutputStream stream = new FileOutputStream(out);

        ReadableByteChannel cin = Channels.newChannel(in);
        long result = copyChannel(cin,length,stream.getChannel());

        stream.close();
	    return result;
	}

    public static long copyChannel(FileChannel in, long length, WritableByteChannel out)
        throws IOException
    {
	    if (length < 0L || length >= Integer.MAX_VALUE)
	        return in.transferTo(0L,length, out);

	    long result = 0L;
	    while (result<length)
		    result += in.transferTo(result, length-result, out);
	    return result;
    }

    public static long copyChannel(ReadableByteChannel in, long length, FileChannel out)
        throws IOException
    {
		if (length < 0L || length >= Integer.MAX_VALUE)
            return out.transferFrom(in,0L,length);

	    long result = 0L;
	    while (result<length)
	        result += out.transferFrom(in,result,length-result);
	    return result;
    }

    public static void addToZip(ZipOutputStream zip, FileChannel in, String name,
                                long size, long time)
        throws IOException
    {
        ZipEntry zety = new ZipEntry(name);
        zety.setSize(size);
        zety.setTime(time);
        zip.putNextEntry(zety);
        if (in!=null) {
            WritableByteChannel cout = Channels.newChannel(zip);
            copyChannel(in,size,cout);
//        cout.close();
        }
        zip.closeEntry();
    }
}
