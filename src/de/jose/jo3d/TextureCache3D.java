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

package de.jose.jo3d;

import com.sun.j3d.utils.image.TextureLoader;
import de.jose.AbstractApplication;
import de.jose.Util;
import de.jose.image.TextureCache;
import de.jose.util.SoftCache;

import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * 	this is a central storage for J3D textures
 *
 *	@author Peter Schäfer
 */

public class TextureCache3D
{
	/**	always create base level texture (1 image)	*/
	public static final int BASE		= 0x01;
	/**	always create mipmap	*/
	public static final int MIPMAP		= 0x02;
	/**	if 1 image is available: create base level texture,
	 *  otherwise create mipmap
	 */
	public static final int BEST		= 0x03;

	/**	texture by reference ?
	 */
	public static final int REFERENCE	= 0x04;
	/**	y up ?
	 */
	public static final int Y_UP		= 0x08;

	private static SoftCache cache = new SoftCache();

	/**
	 * get a Texture from the storage
	 */
	public static Texture2D getTexture(String name)
	{
		return getTexture(name, MIPMAP + REFERENCE + Y_UP);
	}

	public boolean lock(String name)
	{
		return cache.lock(name);
	}

	public boolean unlock(String name)
	{
		return cache.unlockKey(name);
	}


	/**
	 * get a Texture from the storage
	 */
	public static Texture2D getTexture(String name, int mode)
	{
		Texture2D result = (Texture2D)cache.get(name);
		if (result != null)
			return result;
		//	else: create new
		switch (mode & 0x03)
		{
		case BASE:
			result = createBaseLevel(TextureCache.getTexture(name,TextureCache.LEVEL_MAX), Util.allOf(mode,REFERENCE), Util.allOf(mode,Y_UP));
			break;
		case MIPMAP:
			BufferedImage[] imgs = TextureCache.getAllTextures(name);
			if (imgs.length==0)
				throw new RuntimeException("texture "+name+" not found");
			result = createMipMap(imgs, Util.allOf(mode,REFERENCE), Util.allOf(mode,Y_UP));
			break;
		case BEST:
			imgs = TextureCache.getAllTextures(name);
			if (imgs.length==0)
				throw new RuntimeException("texture "+name+" not found");
			else if (imgs.length==1)
				result = createBaseLevel(imgs[0], Util.allOf(mode,REFERENCE), Util.allOf(mode,Y_UP));
			else
				result = createMipMap(imgs, Util.allOf(mode,REFERENCE), Util.allOf(mode,Y_UP));
			break;
		}
        result.setCapability(Texture.ALLOW_ANISOTROPIC_FILTER_READ);
		cache.put(name,result);
		return result;
	}

	/**
	 * decal tetxures are stored as PNG files
	 * @return
	 */
	public static final Texture2D getDecalTexture(String name, Component observer)
	{
		name = "decals/"+name;
		Texture2D result = (Texture2D)cache.get(name);
		if (result != null)
			return result;
		//	else: create new
		File f = new File(TextureCache.getDirectory(), name);
		TextureLoader loader = new TextureLoader(f.getAbsolutePath(), "RGBA",
										TextureLoader.BY_REFERENCE+TextureLoader.Y_UP, observer);
		ImageComponent2D img = loader.getImage();

		result = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img.getWidth(), img.getHeight());
		result.setImage(0, img);
		result.setCapability(Texture2D.ALLOW_SIZE_READ);
		result.setCapability(Texture2D.ALLOW_IMAGE_READ);
		cache.put(name,result);
		return result;
	}


    public static final Texture2D addDecalFromResource(String path, String name, boolean lock, Component observer)
	{
        return addDecalFromResource(AbstractApplication.theAbstractApplication.getResource(path+"/"+name), name, lock, observer);
    }

	public static final Texture2D addDecalFromResource(URL url, String name, boolean lock, Component observer)
	{
		TextureLoader loader = new TextureLoader(url, "RGBA", TextureLoader.BY_REFERENCE+TextureLoader.Y_UP, observer);
		ImageComponent2D img = loader.getImage();

		Texture2D result = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img.getWidth(), img.getHeight());
		result.setImage(0, img);
		result.setCapability(Texture2D.ALLOW_SIZE_READ);
		result.setCapability(Texture2D.ALLOW_IMAGE_READ);

		cache.put("decals/"+name, result, true);
//		System.out.println(name+" loaded");
		return result;
	}

	public static ImageComponent2D getImage(String name)
	{
		Texture txt = getTexture(name);
		return (ImageComponent2D)txt.getImage(0);
	}

	private static Texture2D createBaseLevel(BufferedImage img, boolean byReference, boolean yup)
	{
		Texture2D t2d = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGB, img.getWidth(), img.getHeight());
		t2d.setImage(0, new ImageComponent2D(ImageComponent2D.FORMAT_RGB, img, byReference,yup));
		t2d.setCapability(Texture2D.ALLOW_SIZE_READ);
		t2d.setCapability(Texture2D.ALLOW_IMAGE_READ);
		return t2d;
	}

	private static Texture2D createMipMap(BufferedImage[] imgs, boolean byReference, boolean yup)
	{
		int size = Util.min(imgs[0].getWidth(),imgs[0].getHeight());
		Texture2D t2d = new Texture2D(Texture2D.MULTI_LEVEL_MIPMAP, Texture2D.RGB, size, size);
		t2d.setCapability(Texture2D.ALLOW_SIZE_READ);
		t2d.setCapability(Texture2D.ALLOW_IMAGE_READ);

		int idx = 0;
		ImageComponent2D i2d = null;

		for (int level = 0; size >= 1; level++, size/=2)
		{
			if (idx < imgs.length && imgs[idx].getWidth()==size) {
				i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGB, imgs[idx], byReference,yup);
				idx++;
			}
			i2d = scaleImage(i2d, size,size);	//	scale to fit
			t2d.setImage(level, i2d);
		}
		return t2d;
	}

	/**
	 * decal textures get a very special treatment
	 *
	private static Texture2D createDecal(File f)
	{
		BufferedImage img = null;
		try {
			img = ImgUtil.readJpeg(f);
		} catch (Exception ioex) {
			ioex.printStackTrace();
			return null;
		}
		/*	the image contains gray scale values that must be copied to the alpha channel
			the RGB values are left black
		*
		BufferedImage aimg = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		GrayToAlphaOp op = new GrayToAlphaOp(-0.3f);
		op.filter(img,aimg);

		Texture2D t2d = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, aimg.getWidth(), aimg.getHeight());
//		Texture2D t2d = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.ALPHA, aimg.getWidth(), aimg.getHeight());
		t2d.setImage(0, new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, aimg, true,true));
		return t2d;
	}
*/
	private static ImageComponent2D scaleImage(ImageComponent2D img, int width, int height)
	{
		if (img.getWidth()==width && img.getHeight()==height)
			return img;	//	nothing to do

		BufferedImage src = img.getImage();
		AffineTransform tf = AffineTransform.getScaleInstance((double)width / src.getWidth(),
											(double)height / src.getHeight());
		AffineTransformOp scale = new AffineTransformOp(tf, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage dst = scale.filter(src,null);
		return new ImageComponent2D(ImageComponent2D.FORMAT_RGB, dst,true,true);
	}
}
