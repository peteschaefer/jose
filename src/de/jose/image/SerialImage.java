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

package de.jose.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.zip.*;

/**
 * a serializable version of BufferedImage
 */

public class SerialImage implements Serializable
{
	static final long serialVersionUID = 4750121708962873954L;

	static final int PLAIN	 = 1;
	static final int ZIP	 = 2;
	static final int GZIP	 = 3;

	public transient BufferedImage img;
		
	public SerialImage(BufferedImage image) {
		img = image;
	}
	
	private void writeObject(ObjectOutputStream out)
	    throws IOException
	{
		out.writeInt(img.getType());
		out.writeInt(img.getWidth());
		out.writeInt(img.getHeight());
		
		DataBufferByte dbuffer = (DataBufferByte)img.getRaster().getDataBuffer();
		byte[][] data = dbuffer.getBankData();

		int format = ZIP;
		out.writeInt(format);

		switch (format) {
		case GZIP:
			GZIPOutputStream gout = new GZIPOutputStream(out);
			writeData(data,gout);
			gout.finish();
			break;
		case ZIP:
			ZipOutputStream zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("image"));
			writeData(data,zout);
			zout.finish();
			break;
		case PLAIN:
			writeData(data,out);
			break;
		}
	}

	private void writeData(byte[][] data, OutputStream out) throws IOException
	{
		for (int i=0; i<data.length; i++)
			out.write(data[i], 0,data[i].length);
	}


	private void readObject(ObjectInputStream in)
	    throws IOException, ClassNotFoundException
	{
		int imgType = in.readInt();
		int width = in.readInt();
		int height = in.readInt();

		img = new BufferedImage(width, height, imgType);
		DataBufferByte dbuffer = (DataBufferByte)img.getRaster().getDataBuffer();
		byte[][] data = dbuffer.getBankData();

		int format = in.readInt();

		switch (format) {
		case GZIP:
			GZIPInputStream gin = new GZIPInputStream(in);
			readData(data,gin);
			break;
		case ZIP:
			ZipInputStream zin = new ZipInputStream(in);
			zin.getNextEntry();
			readData(data,zin);
			break;
		case PLAIN:
			readData(data,in);
			break;
		}
	}

	private void readData(byte[][] data, InputStream in) throws IOException
	{
		for (int i=0; i<data.length; i++) {
			int count = 0;
			while (count < data[i].length)
				count += in.read(data[i], count,data[i].length-count);
		}
	}
}
