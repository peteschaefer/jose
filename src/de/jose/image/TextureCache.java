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

import de.jose.AbstractApplication;
import de.jose.Application;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

/**
 * cache for textures, used by various (2D) graphics
 *
 * (note that TextureCache3D is used for Java3D - requires a different format)
 *
 * @author Peter Schäfer
 */

public class TextureCache
{
	//-------------------------------------------------------------------------------
	//	Static Fields
	//-------------------------------------------------------------------------------
	

	/**	max. size of a texture */
	protected static final int MAX_SIZE		= 1024;

	public static final int	LEVEL_MIN		= 0;
	public static final int	LEVEL_64		= 6;
	public static final int	LEVEL_256		= 8;
	public static final int	LEVEL_512		= 9;
	public static final int LEVEL_MAX		= 10;

	/**
	 * the base directory
	 */
	protected static File directory		= null;
	protected static boolean scanned 	= true;	//	unless set

	/**	hashtables with texture for each mipmap level
	 *  maps names to SoftRecence[11]
	 *
	 *  0 = 1x1 pixels
	 *  1 = 2x2 pixels
	 *  ...
	 * 10 = 1024x1024 pixels
	 * */
	protected static Hashtable textures		= new Hashtable();

	/**
	 * contains locked references (not GC'ed)
	 */
	protected static HashSet locks			= new HashSet();

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public static void setDirectory(File dir)
	{
		directory = dir;
		scanned = false;
	}

	private static void scan()
	{
		//	scan directory
		scanned = true;
		for (int level = LEVEL_MAX, size = MAX_SIZE; level >= 0; level--, size /= 2)
		{
			File subdir = new File(directory, String.valueOf(size));
			if (!subdir.exists()) continue;

			String[] names = subdir.list();
			if (names == null) continue;

			for (int i=0; i<names.length; i++)
				if (names[i].endsWith(".jpg")) {
					SoftReference[] refs = (SoftReference[])textures.get(names[i]);
					if (refs==null) {
						refs = new SoftReference[LEVEL_MAX+1];
						textures.put(names[i],refs);
					}
					refs[level] = new SoftReference(null);
				}
		}
	}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public static int getSize(int level)
	{
		int size = MAX_SIZE;
		int lev = LEVEL_MAX;

		while (lev > level)
		{
			lev--;
			size /= 2;
		}
		return size;
	}

	public static int getLevel(int size)
	{
		int sz = MAX_SIZE;
		int lev = LEVEL_MAX;

		while (sz > size)
		{
			lev--;
			sz /= 2;
		}
		return lev;

	}

	public static File getDirectory()		{ return directory; }

	protected static SoftReference[] getSoftReferences(String name)
	{
		if (!scanned) scan();
		SoftReference[] refs = (SoftReference[])textures.get(name);
		if (refs==null) {
			refs = new SoftReference[LEVEL_MAX+1];
			textures.put(name,refs);
		}
		return refs;
	}

	protected static SoftReference getSoftReference(String name, int level)
	{
		SoftReference[] refs = getSoftReferences(name);
		return refs[level];
	}

	protected static BufferedImage getImage(String name, int level)
	{
		SoftReference ref = getSoftReference(name,level);
		if (ref==null)
			return null;
		BufferedImage img = (BufferedImage)ref.get();
		if (img==null) {
			img = loadImage(name,level);
			getSoftReferences(name)[level] = new SoftReference(img);
		}
		return img;
	}

	protected static BufferedImage loadImage(String name, int level)
	{
		if (!scanned) scan();
		int size = 1<<level;
		File f = new File(directory, size+"/"+name);
		try {
			return ImgUtil.readJpeg(f);
		} catch (Exception ex) {
//			System.out.println(size+"/"+name);
			Application.error(ex);
			return null;
		}
	}


	/**
	 * add an image that was read from a resource (e.g. contained in a JAR file)
 	 */
	public static void addFromResource(String name, int level, boolean locked)
		throws Exception
	{
		InputStream src = AbstractApplication.theAbstractApplication.getResourceStream("images/textures/"+getSize(level)+"/"+name);
		addFromStream(name,level,src);
		if (! lock(name,level))
			throw new RuntimeException("lock failed");
		src.close();
	}

	/**
	 * add an image that was read from a resource (e.g. contained in a JAR file)
 	 */
	public static void addFromStream(String name, int level, InputStream source)
		throws Exception
	{
		SoftReference[] refs = getSoftReferences(name);
		BufferedImage img = ImgUtil.readJpeg(source);
		refs[level] = new SoftReference(img);
	}

	/**
	 * locka a texture (i.e. it will not be garbage collected)
	 */
	public static boolean lock(String name, int level)
	{
		SoftReference ref = getSoftReference(name,level);
		Object img = ref.get();
		if (img != null) {
			locks.add(img);
			return true;
		}
		else
			return false;
	}

	/**
	 * locka a texture (i.e. it will not be garbage collected)
	 */
	public static boolean unlock(String name, int level)
	{
		SoftReference ref = getSoftReference(name,level);
		Object img = ref.get();
		if (img != null) {
			locks.remove(img);
			return true;
		}
		else
			return false;
	}

	/**
	 * read a texture image from disk
	 *
	 * @param name
	 * @return
	 */
	public static BufferedImage getTexture(String name, int preferredLevel)
	{
		if (!scanned) scan();
		for (int level=preferredLevel; level<=LEVEL_MAX; level++)
		{
			BufferedImage img = getImage(name,level);
			if (img != null) return img;
		}

		for (int level=preferredLevel-1; level>=0; level--)
		{
			BufferedImage img = getImage(name,level);
			if (img != null) return img;
		}

		return null;
	}

	/**
	 * read all texture images from disk
	 *
	 * @param name
	 * @return
	 */
	public static BufferedImage[] getAllTextures(String name)
	{
		if (!scanned) scan();
		ArrayList collect = new ArrayList();
		for (int level = LEVEL_MAX; level>=0; level--)
		{
			BufferedImage img = getImage(name,level);
			if (img!=null) collect.add(img);
		}

		BufferedImage[] result = new BufferedImage[collect.size()];
		collect.toArray(result);
		return result;
	}

	/**
	 * @return an array of file names the images/textures folder
	 */
	public static String[] getInstalledTextures()
	{
		if (!scanned) scan();
		ArrayList collect = new ArrayList();
		Enumeration en = textures.keys();
		while (en.hasMoreElements())
			collect.add(en.nextElement());

		String[] result = new String[collect.size()];
		collect.toArray(result);
		return result;
	}
	

	public static void paintTexture(Graphics g, int x, int y, int width, int height,
								   Image img, int xoffset, int yoffset)
	{
		if (img==null) return;

//		g.drawImage(img, x,y,x+width,y+height, 0,0,width,height, null);
		int twidth = img.getWidth(null);
		int theight = img.getHeight(null);
		
		while (xoffset < 0) xoffset += twidth;
		while (yoffset < 0) yoffset += theight;
		
		xoffset %= twidth;
		yoffset %= theight;
		
		//	partial tile top-left
		paintTile(img, xoffset,yoffset, 
				  Math.min(twidth-xoffset,width), 
				  Math.min(theight-yoffset,height),
				  g, x,y);
		//	partial tiles on top
		for (int rx = x+twidth-xoffset; rx < (x+width); rx += twidth)
			paintTile(img, 0,yoffset,
					  Math.min(twidth, x+width-rx),
					  Math.min(theight-yoffset,height),
					  g, rx,y);
		
		for (int ry = y+theight-yoffset; ry < (y+height); ry += theight) {
			//	partial tile left
			paintTile(img, xoffset,0,
					  Math.min(twidth-xoffset,width),
					  Math.min(theight, y+height-ry),
					  g, x,ry);
			//	full tiles
			for (int rx = x+twidth-xoffset; rx < (x+width); rx += twidth)
				paintTile(img, 0,0,
						  Math.min(twidth, x+width-rx),
						  Math.min(theight, y+height-ry),
						  g, rx,ry);
			
		}
	}

	public static final boolean isDark(String name, int preferredLevel)
	{
		BufferedImage bimg = getTexture(name, preferredLevel);
		return bimg!=null && ImgUtil.isDark(bimg);
	}

	public static final void paintTexture(Graphics g, int x, int y, int width, int height, Image img)
	{
		paintTexture(g,x,y,width,height,img, x,y);
	}
	
	protected static final void paintTile(Image img, int x, int y, int width, int height,
									Graphics g, int dx, int dy)
	{
		g.drawImage(img, dx,dy,dx+width,dy+height, x,y, x+width,y+height, null);
	}

	public static final void paintTexture(Graphics g, int x, int y, int width, int height,
								   String texture, int preferredLevel,
								   int xoffset, int yoffset)
		throws FileNotFoundException
	{
		paintTexture(g,x,y,width,height, getTexture(texture, preferredLevel), xoffset, yoffset);
	}
	
	public static final void paintTexture(Graphics g, int x, int y, int width, int height,
										  String texture, int preferredLevel)
		throws FileNotFoundException
	{
		paintTexture(g,x,y,width,height, getTexture(texture, preferredLevel), x,y);
	}
	

}
