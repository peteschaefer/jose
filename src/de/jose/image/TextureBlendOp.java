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

import de.jose.Util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.io.FileNotFoundException;

/**
 * an Image Filter for pasting a texture onto an image
 * the source image is interpreted as a "mask"
 *
 * 	source and destination are expected to be RGBA images
 * 	texture is expected RGB
 *
 * @author Peter Schäfer
 * @version "$Revision:  $","$Date:  $"
 */
public class TextureBlendOp
		implements BufferedImageOp, RasterOp
{
	//--------------------------------------------------------------------------
	// constants
	//--------------------------------------------------------------------------

	/**	texture mode: replace orignal pixel with texture pixel,
	 * 	multiply alpha	 */
	public static final int REPLACE		= 1;
	/**	texture mode: modulate texture pixels onto source pixels	 */
	public static final int DECAL		= 2;

	//--------------------------------------------------------------------------
	// field
	//--------------------------------------------------------------------------

	/**	raster containing the texture image */
	private Raster traster;
	/**	texture mode: one of REPLACE, BLEND, MODULATE */
	private int tmode;
	/**	texture offset */
	private int txoff, tyoff;

	/**	aux. variables (static for faster access)
	 * 	thus, we are NOT thread-safe
	 * */
	/**	size of texture raster */
	private static int twidth, theight;
	/**	contains a line of texture pixels */
	private static int[] tpix;
	/**	contais a line of source pixels */
	private static int[] spix;
	/**	contains a line of destination pixels */
	private static int[] dpix;
	/**	size of source raster */
	private static int swidth, sheight;
	/**	required texture width */
	private static int fwidth;
	/**	how many chunks are fetched for one line */
	private static int fetchMode;
	/**	loop variables */
	private static int sx, tx;
	private static int tmax, smax, smaxx, alpha,nalpha;

	//--------------------------------------------------------------------------
	// constructor
	//--------------------------------------------------------------------------

	/**
	 * creates a new Texture Blend filter
	 *
	 * @param text the texture image
	 * @param mode one of: REPLACE, BLEND, MODULATE
	 * @param xoff horizontal offset of the texture
	 * @param yoff vertical offset of the texture
	 */
	public TextureBlendOp(BufferedImage text, int mode, int xoff, int yoff)
	{
		traster = text.getRaster();
		if (traster.getNumBands() != 3)
			throw new IllegalArgumentException("RGB image expected");

		twidth = traster.getWidth();
		theight = traster.getHeight();

		tmode = mode;
		txoff = Util.abs(xoff) % twidth;
		tyoff = Util.abs(yoff) % theight;
	}


	public TextureBlendOp(String text, int preferredLevel,
						  int mode, int xoff, int yoff)
	{
		this (TextureCache.getTexture(text,preferredLevel), mode,xoff,yoff);
	}

	//--------------------------------------------------------------------------
	// methods
	//--------------------------------------------------------------------------

	public BufferedImage filter(BufferedImage src, BufferedImage dest)
	{
		if (dest==null) dest = createCompatibleDestImage(src, src.getColorModel());

		filter(src.getRaster(), dest.getRaster());

		return dest;
	}

	public WritableRaster filter(Raster src, WritableRaster dest)
	{
//		long time = com.sun.j3d.utils.timer.J3DTimer.getValue();

        if (dest==null) dest = createCompatibleDestRaster(src);

		twidth = traster.getWidth();
		theight = traster.getHeight();

		swidth = Util.max(src.getWidth(),dest.getWidth());
		sheight = Util.max(src.getHeight(),dest.getHeight());

		fwidth = Util.min(twidth,swidth);
		tpix = new int[fwidth*3];

		switch (tmode) {
		case REPLACE:	spix = new int[swidth*4]; break;
		case DECAL: 	spix = new int[swidth];	//	need only alpha component
						dpix = new int[swidth*4]; break;
		}

		smaxx = swidth*4;
		tmax = twidth*3;

		if (fwidth >= twidth) 				//	src broader than texture: fetch complete line
			fetchMode = 0;
		else if ((txoff+fwidth) <= twidth) 	//	texture broader than src, not wrapped: fetch one chunk
			fetchMode = 1;
		else 								//	texture broader than src: fetch two chunks
			fetchMode = 2;

		int ty = tyoff;
		for (int y=0; y < sheight; y++)
		{
			switch (tmode) {
			case REPLACE:	src.getPixels(0,y, swidth,1, spix); break;
			case DECAL:		src.getSamples(0,y, swidth,1, 0, spix);		//	need only one component
							dest.getPixels(0,y, swidth,1, dpix); break;
			}

			switch (fetchMode)
			{
			case 0:		//	one chunk, wrapped
				traster.getPixels(0,y, fwidth,1, tpix);
				sx = 0;
				tx = txoff*4;
				smax = twidth*4-tx;
				workLine();

				while (smax < smaxx) {
					tx = 0;
					smax += twidth*4;
					if (smax > smaxx) smax = smaxx;
					workLine();
				}
				break;

			case 1:		//	one chunk, not wrapped
				traster.getPixels(txoff,y, fwidth,1, tpix);
				sx = tx = 0;
				smax = smaxx;
				workLine();
				break;

			case 2:		//	two chunks
				traster.getPixels(txoff,y, twidth-txoff, 1, tpix);		//	upper chunk
				sx = tx = 0;
				smax = twidth*4-txoff*4;
				workLine();

				traster.getPixels(0,y, fwidth-twidth+txoff, 1, tpix);	//	lower chunk
				tx = 0;
				smax = smaxx;
				workLine();
				break;
			}

			switch (tmode) {
			case REPLACE:	dest.setPixels(0,y, swidth,1, spix); break;
			case DECAL:		dest.setPixels(0,y, swidth,1, dpix); break;
			}

			if (++ty >= theight) ty=0;
		}

		//	release memory
		tpix = null;
		spix = null;
		dpix = null;

//		time = com.sun.j3d.utils.timer.J3DTimer.getValue()-time;
//		System.out.println((time/1e6)%1000);
		return dest;
	}

	private void workLine()
	{
		switch (tmode)
		{
		case REPLACE:			replaceLine(); break;
		case DECAL:				decalLine(); break;
		}
	}

	private void replaceLine()
	{
		while (sx < smax)
		{
			spix[sx++] = tpix[tx++];		//	replace red
			spix[sx++] = tpix[tx++];		//	replace green
			spix[sx++] = tpix[tx++];		//	replace blue
			sx++;							// keep alpha
		}
	}

	private void decalLine()
	{
		while (sx < smax)
		{
			alpha = spix[sx/4];
			nalpha = 255-alpha;
			dpix[sx] = (dpix[sx]*nalpha + tpix[tx]*alpha)/255;		//	modulate red
			sx++; tx++;
			dpix[sx] = (dpix[sx]*nalpha + tpix[tx]*alpha)/255;		//	modulate green
			sx++; tx++;
			dpix[sx] = (dpix[sx]*nalpha + tpix[tx]*alpha)/255;		//	modulate blue
			sx++; tx++;
			sx++;									//	keep alpha
		}
	}

	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM)
	{
		return new BufferedImage(destCM, createCompatibleDestRaster(src.getRaster()), false, null);
	}

	public WritableRaster createCompatibleDestRaster(Raster src)
	{
		return src.createCompatibleWritableRaster();
	}


	public Rectangle2D getBounds2D(BufferedImage src)
	{
		return new Rectangle2D.Float(0.0f,0.0f, src.getWidth(),src.getHeight());
	}

	public Rectangle2D getBounds2D(Raster src)
	{
		return src.getBounds();
	}

    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt)
	{
		if (dstPt==null) dstPt = new Point2D.Double();
		dstPt.setLocation(srcPt);
		return dstPt;
	}

	public RenderingHints getRenderingHints()
	{
		return null;
	}

} // class TextureBlendOp

/*
 * $Log: $
 *
 */

