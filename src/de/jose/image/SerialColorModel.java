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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.io.Serializable;
import java.util.Arrays;

/**
 * a serializable version of ColorModel
 */

public class SerialColorModel implements Serializable
{
	static final long serialVersionUID = 5459117102602880665L;

	ColorSpace cspace;
	int pixelStride, scanlineStride;
	int[] bandOffsets;
	boolean hasAlpha, alphaPre;
	int transferType, transparency;
		
	public SerialColorModel(BufferedImage img) {
		this((ComponentColorModel)img.getColorModel(), 
			 (ComponentSampleModel)img.getRaster().getSampleModel());
	}
		
	public SerialColorModel(ComponentColorModel cmodel, ComponentSampleModel smodel) {
		cspace = cmodel.getColorSpace();	//	is serializable
		pixelStride = smodel.getPixelStride();
		scanlineStride = smodel.getScanlineStride();
		bandOffsets = smodel.getBandOffsets();
		hasAlpha = cmodel.hasAlpha();
		alphaPre = cmodel.isAlphaPremultiplied();
		transferType = cmodel.getTransferType();
		transparency = cmodel.getTransparency();
	}
		
	public ColorModel createColorModel() {
		int[] bits;
		if (hasAlpha)
			bits = new int[cspace.getNumComponents()+1];
		else
			bits = new int[cspace.getNumComponents()];
		Arrays.fill(bits,8);
			
		return new ComponentColorModel(cspace, bits, hasAlpha, alphaPre, transparency, transferType);
	}
}
