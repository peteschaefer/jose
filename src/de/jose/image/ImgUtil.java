/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.image;

import de.jose.Application;
import de.jose.util.SoftCache;
import de.jose.util.file.FileUtil;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.*;
import java.io.*;
import java.util.Random;
import java.util.Vector;
import java.lang.String;

public class ImgUtil
{
/*
		copy(theBackground, current.x, current.y, 
			 theBuffer, 0,0, theBuffer.getWidth(), theBuffer.getHeight());
		copy(theSprite, theBuffer);
	
*/	
	
	public static final Color lightBlue = new Color(0xdd,0xdd,0xff);

	/** image sizes
	 * SoftCache<File,Dimension>
	 *  */
	protected static SoftCache gImageSize = new SoftCache();

	public static final boolean isDark(Color col)
	{
		return isDark(col.getRed(),col.getGreen(),col.getBlue(), 127);
	}

	public static final boolean isDark(int red, int green, int blue, int thresh)
	{
		return (red+green+blue) <= (3*thresh);
	}

	public static final boolean isDark(BufferedImage img)
	{
		Raster rst = img.getRaster();
		Random rnd = new Random();
		int[] pixel = new int[4];
		int[] sum = new int[4];

		int samples = 5;
		for (int i=samples-1; i>=0; i--)
		{
			int x = Math.abs(rnd.nextInt()) % img.getWidth();
			int y = Math.abs(rnd.nextInt()) % img.getHeight();
			rst.getPixel(x,y,pixel);
			for (int j=0; j<pixel.length; j++)
				sum[j] += pixel[j];
		}

		return isDark(sum[0],sum[1],sum[2], 127*samples);
	}

	public static final void copy(Image source, int sourcex, int sourcey,
							Graphics dest, int destx, int desty, 
							int width, int height)
	{
		if (sourcex < 0) {
			destx -= sourcex;
			width += sourcex;
			sourcex = 0;
		}
		if (sourcey < 0) {
			desty -= sourcey;
			height += sourcey;
			sourcey = 0;
		}
		if (destx < 0) {
			sourcex -= destx;
			width += destx;
			destx = 0;
		}
		if (desty < 0) {
			sourcey -= desty;
			height += desty;
			desty = 0;
		}

		if (width > 0 && height > 0)
			dest.drawImage(source, destx, desty, destx+width, desty+height,
                          sourcex, sourcey, sourcex+width, sourcey+height, null);
	}

	public static final void copy(Image source,
	                        int sourcex, int sourcey, int sourcewidth, int sourceheight,
							Graphics dest,
	                        int destx, int desty, int destwidth, int destheight)
	{
		double hscale = (double)destwidth/(double)sourcewidth;
		double vscale = (double)destheight/(double)sourceheight;

		if (sourcex < 0) {
			destx -= sourcex*hscale;
			sourcewidth += sourcex;
			destwidth += sourcex*hscale;
			sourcex = 0;
		}
		if (sourcey < 0) {
			desty -= sourcey*vscale;
			sourceheight += sourcey;
			destheight += sourcey*vscale;
			sourcey = 0;
		}
		if (destx < 0) {
			sourcex -= destx/hscale;
			destwidth += destx;
			sourcewidth += destx/hscale;
			destx = 0;
		}
		if (desty < 0) {
			sourcey -= desty/vscale;
			destheight += desty;
			sourceheight += desty/vscale;
			desty = 0;
		}

		if (sourcewidth > 0 && sourceheight > 0 && destwidth > 0 && destheight > 0)
			dest.drawImage(source, destx, desty, destx+destwidth, desty+destheight,
                          sourcex, sourcey, sourcex+sourcewidth, sourcey+sourceheight, null);
	}

	public static final void copy(BufferedImage source, Graphics dest)
	{
		copy(source, 0,0, dest, 0,0, source.getWidth(), source.getHeight());
	}
	
	public static final void copy(BufferedImage source, Graphics dest, int destx, int desty)
	{
		copy(source, 0,0, dest, destx,desty, source.getWidth(), source.getHeight());
	}


	public static BufferedImage toBufferedImage(Image img, int type, ImageObserver observer)
	{
		BufferedImage bimg = new BufferedImage(img.getWidth(observer),img.getHeight(observer), type);
		Graphics g = bimg.getGraphics();
		g.drawImage(img,0,0, observer);
		return bimg;
	}

	public static void computeExposedArea(Rectangle from, Rectangle to,
										   Rectangle result1, Rectangle result2)
	{
		result1.x = from.x;
		result1.y = from.y;
		result1.width = from.width;
		result1.height = from.height;
		result2.width = result2.height = 0;
		
		if (to.x > from.x) 
		{
			if (to.y > from.y)
			{	//	III
				if (to.x < (from.x+from.width) &&
					to.y < (from.y+from.height))
				{
					result1.width = to.x-from.x;
					result2.x = to.x;
					result2.y = from.y;
					result2.width = from.x+from.width-to.x;
					result2.height = to.y-from.y;
				}
			}
			else 
			{	//	IV
				if (to.x < (from.x+from.width) &&
					(to.y+to.height) > from.y)
				{	
					result1.width = to.x-from.x;
					result2.x = to.x;
					result2.y = to.y+to.height;
					result2.width = from.x+from.width-to.x;
					result2.height = from.y+from.height-(to.y+to.height);
				}
			}
		}
		else
		{
			if (to.y > from.y)
			{	//	II
				if ((to.x+to.width) > from.x &&
					to.y < (from.y+from.height))
				{
					result1.height = to.y-from.y;
					result2.x = to.x+to.width;
					result2.y = to.y;
					result2.width = from.x+from.width-(to.x+to.width);
					result2.height = from.y+from.height-to.y;
				}
					
			}
			else
			{	//	I
				if ((to.x+to.width) > from.x &&
					(to.y+to.height) > from.y)
				{
					result1.y = to.y+to.height;
					result1.height = from.y+from.height-(to.y+to.height);
					result2.x = to.x+to.width;
					result2.y = from.y;
					result2.width = from.x+from.width-(to.x+to.width);
					result2.height = to.y+to.height-from.y;
				}
			}
		}
	}
	
	/**
	 * set optimal rendering
	 */
	public static void setRenderingHints(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,	RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,		RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_DITHERING,			RenderingHints.VALUE_DITHER_ENABLE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,	RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,		RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,			RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,		RenderingHints.VALUE_STROKE_DEFAULT);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,	RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	public static void setTextAntialiasing(Graphics2D g, boolean on)
	{
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		        on ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	}


	public static BufferedImage readJpeg(File f)
		throws Exception
	{
		InputStream src = null;
		try {
			src = new FileInputStream(f);
			src = new BufferedInputStream(src,4096);
			return readJpeg(src);
		} finally {
			if (src!=null) src.close();
		}
	}

	public static BufferedImage readJpeg(InputStream src)
		throws Exception
	{
		return ImageIO.read(src);
	}

	public static void writeJpeg(BufferedImage img, File f, float quality)
		throws IOException
	{
		ImageIO.write(img, "jpeg", f);
		setImageSize(f, img.getWidth(),img.getHeight());
	}

	public static void writePng(BufferedImage img, File f) throws Exception
	{
		ImageIO.write(img, "png", f);
		setImageSize(f, img.getWidth(),img.getHeight());
	}

	//-------------------------------------------------------------------------------
	//	private
	//-------------------------------------------------------------------------------

	public static ImageIcon getMenuIcon(String name)
	{
		return getMenuIcon(name,false);
	}

	public static ImageIcon[] getNavigationIcons(String name)
	{
		ImageIcon[] icons = new ImageIcon[7];
		if ((icons[0] = getIcon("nav", name+".off"))==null) return null;
		if ((icons[1] = getIcon("nav", name+".cold"))==null) return null;
		if ((icons[2] = getIcon("nav", name+".hot"))==null) return null;
		if ((icons[3] = getIcon("nav", name+".pressed"))==null) return null;
		icons[4] = getIcon("nav", name+".selected.cold");
		icons[5] = getIcon("nav", name+".selected.hot");
		icons[6] = getIcon("nav", name+".selected.pressed");
		return icons;
	}

    public static ImageIcon getMenuIcon(String name, boolean pressed)
	{
        if (pressed)    name += ".pressed";
		return getIcon("menu",name);
	}

	public static ImageIcon getIcon(String folder, String name)
	{
		String key = folder+"/"+name;
		ImageIcon icon = (ImageIcon)SoftCache.gInstance.get(key);
		if (icon!=null) return icon;

		String file = getImageFile(folder,name);
		if (file != null && FileUtil.exists(file)) {
			icon = new ImageIcon(file);
			SoftCache.gInstance.put(folder+"/"+name,icon);
			return icon;
		}
		else
			return null;
	}

	public static ImageIcon getIcon(File file, Dimension sz)
	{
		ImageIcon icon = (ImageIcon)SoftCache.gInstance.get(file);
		if (icon!=null) return icon;

		if (FileUtil.hasExtension(file.getName(),"bmp"))
		{
			Image img = readBmp(file);
			sz = scaledAspectRatio(img,sz);
			img = img.getScaledInstance((int)sz.getWidth(), (int)sz.getHeight(), Image.SCALE_SMOOTH);
			icon = new ImageIcon(img);
		}
		else {
			icon = new ImageIcon(file.getAbsolutePath());
			Image img = icon.getImage();
			sz = scaledAspectRatio(img,sz);
			img = img.getScaledInstance((int)sz.getWidth(), (int)sz.getHeight(),Image.SCALE_SMOOTH);
			icon.setImage(img);
		}

		SoftCache.gInstance.put(file,icon);
		return icon;
	}

	public static ImageIcon getIcon(String fileName, InputStream input) throws Exception
	{
		ImageIcon icon = (ImageIcon)SoftCache.gInstance.get(fileName);
		if (icon!=null) return icon;

		Image img = getImage(input,fileName);
		icon = new ImageIcon(fileName);

		SoftCache.gInstance.put(fileName,icon);
		return icon;
	}

	public static String getImageFile(String folder, String name)
	{
		String path = Application.theWorkingDirectory + File.separator +
					  "images"+ File.separator;
        if (folder != null)
            path +=  folder + File.separator;
        path += name;

		String s;
		File f = new File(s = path);
        if (f.exists())
            return s;

		f = new File(s = path+".png");
		if (f.exists())
		    return s;

		f = new File(s = path+".gif");
		if (f.exists())
			return s;

		f = new File(s = path+".jpg");
		if (f.exists())
			return s;

/*		f = new File(s = path+".bmp");
		if (f.exists())
			return s;
*/		
		return null;
	}

	public static Dimension getImageSize(File file)
	{
		//  query cache first
		Dimension dim = (Dimension)gImageSize.get(file);
		if (dim!=null) return dim;

		try {
			String fileName = file.getName();
			if (FileUtil.hasExtension(fileName,"png")) {
				RenderedImage img = ImageIO.read(file);
				return setImageSize(file, img.getWidth(), img.getHeight());
			}
			if (FileUtil.hasExtension(fileName,"gif")) {
				return null;    //  TODO
			}
			if (FileUtil.hasExtension(fileName,"jpg") || FileUtil.hasExtension(fileName,"jpeg")) {
				BufferedImage img = readJpeg(file);
				return setImageSize(file, img.getWidth(), img.getHeight());
			}
			if (FileUtil.hasExtension(fileName,"bmp")) {
				Image img = readBmp(file);
				return setImageSize(file, img.getWidth(null), img.getHeight(null));
			}
			throw new IllegalArgumentException("unknown image format");
		} catch (Exception e) {
			return null;
		}
	}

	public static Dimension setImageSize(File file, int width, int height)
	{
		Dimension dim = new Dimension(width, height);
		gImageSize.put(file,dim);
		return dim;
	}

	public static boolean existsIcon(String folder, String name)
	{
		String fileName = getImageFile(folder,name);
		return (fileName!=null) && FileUtil.exists(fileName);
	}

	public static Image getImage(File file) throws Exception
	{
		String fileName = file.getName();
		if (FileUtil.hasExtension(fileName,"gif") || FileUtil.hasExtension(fileName,"png"))
			return Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
		if (FileUtil.hasExtension(fileName,"jpg") || FileUtil.hasExtension(fileName,"jpeg"))
			return readJpeg(file);
		if (FileUtil.hasExtension(fileName,"bmp"))
			return readBmp(file);
		throw new IllegalArgumentException("unknown image format");
	}

	public static Image getImage(InputStream input, String fileName) throws Exception
	{
		if (FileUtil.hasExtension(fileName,"gif") || FileUtil.hasExtension(fileName,"png"))
			return createImage(input);
		if (FileUtil.hasExtension(fileName,"jpg") || FileUtil.hasExtension(fileName,"jpeg"))
			return readJpeg(input);
		if (FileUtil.hasExtension(fileName,"bmp"))
			return readBmp(input);
		throw new IllegalArgumentException("unknown image format");
	}

	public static Image createImage(InputStream input) throws IOException
	{
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		FileUtil.copyStream(input,buffer);
		return Toolkit.getDefaultToolkit().createImage(buffer.toByteArray());
	}

	public static Image readBmp(File file)
	{
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public static Image readBmp(InputStream input) throws IOException
	{
		return ImageIO.read(input);
	}

	public static Shape getOutline(Font fnt, String text)
	{
		FontRenderContext frc = new FontRenderContext(null, true,true);
		GlyphVector gv = fnt.createGlyphVector(frc, text);
		return gv.getOutline();
	}
	
	public static Shape getOutline(Font fnt, String text, int x, int y)
	{
		FontRenderContext frc = new FontRenderContext(null, true,true);
		GlyphVector gv = fnt.createGlyphVector(frc, text);
		return gv.getOutline(x,y);
	}

	public static Shape[] getMask(Shape shape, boolean outline) 
	{
		Vector result = new Vector();
		
		float[] coords = new float[6];
		PathIterator pi = shape.getPathIterator(null);
		GeneralPath current = new GeneralPath(pi.getWindingRule());
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				if (current.getCurrentPoint()==null) {
					//	first move in a path, ok
					current.moveTo(coords[0], coords[1]);
				}
				else {
					//	next move -> split path in two
					current.closePath();
					//current.trimToSize(); // Java 10
					result.add(current);

					current = new GeneralPath(pi.getWindingRule());
					current.moveTo(coords[0], coords[1]);
				}
				break;
			case PathIterator.SEG_CLOSE:
				if (current.getCurrentPoint()!=null) {
					current.closePath();
					//current.trimToSize(); // Java 10
					result.add(current);
				}
				current = new GeneralPath(pi.getWindingRule());
				break;
			case PathIterator.SEG_CUBICTO:
				current.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
				break;
			case PathIterator.SEG_LINETO:
				current.lineTo(coords[0],coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				current.quadTo(coords[0],coords[1],coords[2],coords[3]);
				break;
			}
			pi.next();
		}
		
		if (current.getCurrentPoint()!=null) {
			current.closePath();
			//current.trimToSize(); // Java 10
			result.add(current);    //	remaining, unclosed shape
		}

		if (outline)
			for (int i=result.size()-1; i>=1; i--) {
				Shape a = (Shape)result.get(i);
				for (int j=0; j<i; j++) {
					Shape b = (Shape)result.get(j);
					if (contains(b,a)) {
						result.remove(i);
						break;
					}
				}
			}
		
		Shape[] shapes = new Shape[result.size()];
		result.toArray(shapes);
		return shapes;
	}
	
	public static boolean contains(Shape a, Shape b) 
	{
		return a.getBounds2D().contains(b.getBounds2D());
	}
	
	public static void fill(Graphics2D g, Shape[] shapes)
	{
		for (Shape shp : shapes)
			g.fill(shp);
	}

	public static BufferedImage toBufferedImage(Image src)
	{
		if (src instanceof BufferedImage)
			return (BufferedImage)src;
		else
			return copyImage(src);
	}

	public static BufferedImage copyImage(Image src)
	{
		int width = src.getWidth(null);
		int height = src.getHeight(null);
		BufferedImage dst = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		dst.getGraphics().drawImage(src,0,0, width,height, null);
		return dst;
	}

	public static BufferedImage createDisabledImage(BufferedImage src)
	{
		ColorSpace srcSpace = src.getColorModel().getColorSpace();
		ColorSpace destSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		ColorConvertOp op = new ColorConvertOp(srcSpace, destSpace, null);
		BufferedImage dest = op.createCompatibleDestImage(src,null);
		op.filter(src,dest);
		return dest;
	}

	public static ImageIcon createDisabledIcon(ImageIcon icon)
	{
		Image image = icon.getImage();
		image = createDisabledImage(toBufferedImage(image));
		return new ImageIcon(image);
	}

	public static Dimension scaledAspectRatio(Image img, Dimension r)
	{
		int iwidth = img.getWidth(null);
		int iheight = img.getHeight(null);

		int swidth = (int)(r.getHeight() * iwidth / iheight);
		int sheight = (int)(r.getWidth() * iheight / iwidth);
		if (swidth <= r.getWidth())
			return new Dimension(swidth,(int)r.getHeight());
		else
			return new Dimension((int)r.getWidth(),sheight);
	}


	public static void main(String[] args)
	{
		String[] formats = ImageIO.getReaderFormatNames();
		for (int i = 0; i < formats.length; i++) {
			System.out.println(formats[i]);
		}
		System.out.println();

		String[] mimes = ImageIO.getReaderMIMETypes();
		for (int i = 0; i < mimes.length; i++)
			System.out.println(mimes[i]);
	}
}
