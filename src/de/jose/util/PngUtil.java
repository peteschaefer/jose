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

import de.jose.Application;
import de.jose.image.ImgUtil;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.sun.media.jai.codec.*;

/**
 * @author Peter Schäfer
 */

public class PngUtil
{

	public static void writePng(BufferedImage img, File f) throws Exception
	{
		FileOutputStream output = new FileOutputStream(f);
		PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(img);
		ImageEncoder encoder = ImageCodec.createImageEncoder("png", output, param);
		encoder.encode(img);
		output.close();

		ImgUtil.setImageSize(f, img.getWidth(),img.getHeight());
	}


	public static byte[] createPng(BufferedImage img) throws Exception
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PNGEncodeParam param = PNGEncodeParam.getDefaultEncodeParam(img);
		ImageEncoder encoder = ImageCodec.createImageEncoder("png", output, param);
		encoder.encode(img);
		output.close();
		return output.toByteArray();
	}

	public static RenderedImage readPng(File file) throws Exception
	{
		PNGDecodeParam param = new PNGDecodeParam();
		ImageDecoder decoder = ImageCodec.createImageDecoder("png",file,param);
		return decoder.decodeAsRenderedImage();
	}
	
}
