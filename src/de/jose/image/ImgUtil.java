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

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
//import com.sun.media.jai.codec.*;
import de.jose.Application;
import de.jose.Version;
import de.jose.util.ReflectionUtil;
import de.jose.util.SoftCache;
import de.jose.util.ClassPathUtil;
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
		JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(src);
		return decoder.decodeAsBufferedImage();
	}

	public static void writeJpeg(BufferedImage img, File f)
		throws IOException
	{
		writeJpeg(img,f, 0.9f);
	}

	public static void writeJpeg(BufferedImage img, File f, float quality)
		throws IOException
	{
		FileOutputStream out = new FileOutputStream(f);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
		param.setQuality(quality,false); // 90% quality JPEG
		encoder.setJPEGEncodeParam(param);
		encoder.encode(img);
		out.close();

		setImageSize(f, img.getWidth(),img.getHeight());
	}

	public static byte[] createJpeg(BufferedImage img, float quality)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(img);
		param.setQuality(quality,false); // 90% quality JPEG
		encoder.setJPEGEncodeParam(param);
		encoder.encode(img);
		out.close();
		return out.toByteArray();
	}

	public static void writePng(BufferedImage img, File f) throws Exception
	{
		assertJAICodec();

		de.jose.util.PngUtil.writePng(img,f);
		setImageSize(f, img.getWidth(),img.getHeight());
	}


	public static byte[] createPng(BufferedImage img) throws Exception
	{
		assertJAICodec();
		return de.jose.util.PngUtil.createPng(img);
	}

/*
	public static int[] getPixels(BufferedImage img)
	{
		WritableRaster r = img.getRaster();
		return r.getPixels(0,0,img.getWidth(),img.getHeight(), (int[])null);
	}
	
	public static void setPixels(BufferedImage img, int[] pixels)
	{
		WritableRaster r = img.getRaster();
		r.setPixels(0,0,img.getWidth(),img.getHeight(), pixels);
	}
	
	protected static final int sq = 255*255;
	/**	
	 * set the RGB components to white; retain the Alpha component
	 *
	public static final void dropShadow(int[] px, int width, int height, int dropx, int dropy, int shadowAlpha)
	{
		int off;
		int dropoff;
		
		for (int y = 0; y < (height-dropy); y++) {
			off = width*y*4;
			dropoff = (width*(y+dropy)+dropx)*4;
			
			for (int x = 0; x < (width-dropx); x++)
			{
				int f = px[off+3] * (255-px[dropoff+3]);
				if (f > 0)
					px[dropoff+3] += shadowAlpha*f/sq;
				
				off += 4;
				dropoff += 4;
			}
		}
	}
	
	/**	
	 * make all white pixels transparent
	 *
	public static final void translucentBackground(int[] px, int width, int height, 
											 int startX, int startY)
	{
		int pxwidth = width*4;
		int pxsize = pxwidth*height;
		
		int[] todo = new int[width*height];
		int top = 0;
		int off0, w;
		int limit = Math.max(255-(int)Math.sqrt(width*height)/10,1);
		int off = startY*pxwidth + startX*4;
		px[off+3]	= (255-px[off+3]);		//	alpha
		px[off]		= 0;					//	red
		px[off+1]	= 0;					//	green
		px[off+2]	= 0;					//	blue
		todo[top++] = off;
		
		while (top > 0) {
			off0 = todo[--top];
			
			off = off0+4;
			if (off < pxsize && px[off+3]==255) {
				w = px[off];
				if (w > 0) {
					if (w >= limit)
						todo[top++] = off;
					px[off+3]	= (255-w);		//	alpha
					px[off]		= 0;			//	red
					px[off+1]	= 0;			//	green
					px[off+2]	= 0;			//	blue
				}
			}
					
			off = off0-4;
			if (off >= 0 && px[off+3]==255) {
				w = px[off];
				if (w > 0) {
					if (w >= limit)
						todo[top++] = off;
					px[off+3]	= (255-w);		//	alpha
					px[off]		= 0;			//	red
					px[off+1]	= 0;			//	green
					px[off+2]	= 0;			//	blue
				}
			}
				
			off = off0+pxwidth;
			if (off < pxsize && px[off+3]==255) {
				w = px[off];
				if (w > 0) {
					if (w >= limit)
						todo[top++] = off;
					px[off+3]	= (255-w);		//	alpha
					px[off]		= 0;			//	red
					px[off+1]	= 0;			//	green
					px[off+2]	= 0;			//	blue
				}
			}
				
			off = off0-pxwidth;
			if (off >= 0 && px[off+3]==255) {
				w = px[off];
				if (w > 0) {
					if (w >= limit)
						todo[top++] = off;
					px[off+3]	= (255-w);		//	alpha
					px[off]		= 0;			//	red
					px[off+1]	= 0;			//	green
					px[off+2]	= 0;			//	blue
				}
			}
		}
	}
*/	
	
	//-------------------------------------------------------------------------------
	//	private
	//-------------------------------------------------------------------------------

	protected static class NoTrackIcon extends ImageIcon
	{
		NoTrackIcon(String filename)
		{
			super(Toolkit.getDefaultToolkit().createImage(filename));
		}

		public void loadImage(Image image) {
			/* don't */
			try {
				ReflectionUtil.setValue(this,"width", new Integer(image.getWidth(null)));
				ReflectionUtil.setValue(this,"height", new Integer(image.getHeight(null)));
			} catch (Exception e) {
				Application.error(e);
			}
		}
	}

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

	public static ImageIcon getIcon(File file)
	{
		ImageIcon icon = (ImageIcon)SoftCache.gInstance.get(file);
		if (icon!=null) return icon;

		if (FileUtil.hasExtension(file.getName(),"bmp"))
		{
			Image img = readBmp(file);
			icon = new ImageIcon(img);
		}
		else
			icon = new ImageIcon(file.getAbsolutePath());

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
				assertJAICodec();
				RenderedImage img = de.jose.util.PngUtil.readPng(file);
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
		//   TODO migrate to 1.5
		osbaldeston.image.BMP bmp = new osbaldeston.image.BMP(file);
		return bmp.getImage();
	}

	public static Image readBmp(InputStream input) throws IOException
	{
		//   TODO migrate to 1.5
		osbaldeston.image.BMP bmp = new osbaldeston.image.BMP(input);
		return bmp.getImage();
	}

	public static void writeBmp(Image img, File file)
	{
		if (Version.java15orLater)
		{

		}
		else {
			osbaldeston.image.BMP bmp = new osbaldeston.image.BMP(img);
			bmp.write(file);
			ImgUtil.setImageSize(file, img.getWidth(null),img.getHeight(null));
		}
	}


	public static byte[] createBmp(Image img)
	{
		osbaldeston.image.BMP bmp = new osbaldeston.image.BMP(img);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bmp.write(out);
		return out.toByteArray();
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
			case PathIterator.SEG_CLOSE:	
				current.closePath();
				result.add(current);
				current = new GeneralPath(pi.getWindingRule());
				break;
			case PathIterator.SEG_CUBICTO:
				current.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
				break;
			case PathIterator.SEG_LINETO:
				current.lineTo(coords[0],coords[1]);
				break;
			case PathIterator.SEG_MOVETO:
				current.moveTo(coords[0],coords[1]);
				break;
			case PathIterator.SEG_QUADTO:
				current.quadTo(coords[0],coords[1],coords[2],coords[3]);
				break;
			}
			pi.next();
		}
		
		if (current.getCurrentPoint()!=null)
			result.add(current);	//	remaining, unclosed shape
		
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
		for (int i=0; i<shapes.length; i++)
			g.fill(shapes[i]);
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

    public static BufferedImage createScaledImage(Image src, int width, int height)
    {
        BufferedImage result = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        result.getGraphics().drawImage(src,0,0,width,height,null);
        return result;
    }

	public static ImageIcon createDisabledIcon(ImageIcon icon)
	{
		Image image = icon.getImage();
		image = createDisabledImage(toBufferedImage(image));
		return new ImageIcon(image);
	}

    public static ImageIcon createScaledIcon(ImageIcon icon, double scale)
    {
        Image image = icon.getImage();
        image = createScaledImage(image,
                (int)Math.round(icon.getIconWidth()*scale),
                (int)Math.round(icon.getIconHeight()*scale));
        return new ImageIcon(image);
    }

	public static void assertJAICodec() throws Exception
	{
		if (!ClassPathUtil.existsClass("com.sun.media.jai.codec.ImageCodec"))
			ClassPathUtil.addToClassPath(new File(Application.theWorkingDirectory,"lib/jai_codec.jar"));
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
