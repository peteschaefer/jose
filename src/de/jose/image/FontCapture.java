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

import de.jose.util.SoftCache;
import de.jose.util.FontUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
/**
 *
  * @author Peter Sch�fer
 */

public class FontCapture
{
	public static final boolean LOCK = true;
	public static final boolean TRANSIENT = false;
	
	protected static final Color TRANSPARENT = new Color(0,0,0,0);
	protected static final Color SHADOW_64	= new Color(0,0,0, 64);

	protected static final Random rand = new Random();

	public static class MapEntry implements Serializable {
		static final long serialVersionUID = 6075689053759430093L;

		public SerialImage img;
		public Rectangle bounds;
	}

	protected static SoftCache cache = new SoftCache();

	public static BufferedImage getImage(String font, int size, String c,
										 Surface white, Surface black,
										 Rectangle bounds,
										 boolean locked)
		throws FileNotFoundException
	{
		String hashkey = hashKey(font,size,c, white,black);
		MapEntry ety = (MapEntry)cache.get(hashkey,locked);
		if (ety==null) {
            ety = capture(font,size,c,white,black, true, true);
			cache.put(hashkey, ety, locked);
		}

		if (bounds!=null)
			bounds.setBounds(ety.bounds);
		
		return ety.img.img;
	}

	private static String hashKey(String font, int size, String c, Surface white, Surface black)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(font);
		buf.append("/");
		buf.append(size);
		if (white!=null) {
			buf.append("/");
            white.appendHashString(buf);
		}
		if (black!=null) {
			buf.append("/");
            black.appendHashString(buf);
		}
		if (c!=null) {
			buf.append("/");
			buf.append(c);
		}
		return buf.toString();
	}

	public static void add(String key, BufferedImage bimg,
						   int minX, int minY, boolean locked)
	{
		MapEntry ety = new MapEntry();
		ety.img = new SerialImage(bimg);
		ety.bounds = new Rectangle(minX,minY, bimg.getWidth(), bimg.getHeight());
		add(key, ety, locked);
	}
	
	public static void add(String key, MapEntry ety, boolean locked)
	{
		cache.put(key,ety,locked);
	}
	
	/**	make previously locked objects available for garbage collection	 */
	public static void unlock()
	{
		cache.unlockAll();
	}
	
	/**
	 * get all images matching
	 * @return a Map (maps String to FontCapture.MapEntry)
	 */
	public static Map getAllImages(String font, int size, String c, Surface white, Surface black)
	{
		String prefix = hashKey(font,size,c, white,black);
		Map result = new HashMap();
		Iterator keys = cache.keySet().iterator();
		while (keys.hasNext())
		{
			String key = (String)keys.next();
			if (key.startsWith(prefix)) {
				MapEntry bety = (MapEntry)cache.get(key);
				if (bety != null)
					result.put(key, bety);
			}
		}
		return result;
	}

	public static BufferedImage capture1(String fontName, int fontSize, String c,
	                                     Surface white, Surface black, Rectangle bounds,
	                                     boolean transparent, boolean shadow)
	        throws FileNotFoundException
	{
		MapEntry ety = capture(fontName,fontSize,c,white,black, transparent, shadow);

		if (bounds!=null)
			bounds.setBounds(ety.bounds);

		return ety.img.img;
	}

	private static MapEntry capture(String fontName, int fontSize, String c,
	                                Surface white, Surface black,
	                                boolean transparent, boolean shadow)
		throws FileNotFoundException
	{
		MapEntry ety = new MapEntry();
		
		Font font = FontUtil.newFont(fontName, Font.PLAIN, fontSize);
		c = checkPrintable(c,font);
		draw(ety,font,c,white,black, transparent, shadow);
		return ety;
	}
	
	
	public static String checkPrintable(String s, String fontName)
	{
		Font font = FontUtil.newFont(fontName, Font.PLAIN, 1);
		return checkPrintable(s,font);
	}
	
	public static String checkPrintable(String s, Font fnt)
	{
/*		StringBuffer b = null;
		
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (!fnt.canDisplay(c)) {
				if (b==null) b = new StringBuffer(s);
				//	unprintable character, try swapping msb
				c ^= 0xf000;
				if (fnt.canDisplay(c))
					b.setCharAt(i,c);
			}
		}
		if (b != null)
			return b.toString();
		else
*/			return s;
	}

	public static void draw(MapEntry ety, Font font, String text, Surface white, Surface black,
	                           boolean transparent, boolean useShadow)
		throws FileNotFoundException
	{
		Shape glyph = ImgUtil.getOutline(font,text);
		Shape strokedGlyph;
		Shape[] mask;

		//	width of surrounding (white) stroke
		int strokeWidth = (int)Math.round(font.getSize()/24.0f);
		if (strokeWidth > 0) {
			Stroke str = new BasicStroke(strokeWidth);
			strokedGlyph = str.createStrokedShape(glyph);
			mask = ImgUtil.getMask(strokedGlyph,true);
		}
		else {
			mask = ImgUtil.getMask(strokedGlyph = glyph,true);
		}

		//	width of drop shadow
		int shadow = useShadow ?(int)Math.round(font.getSize()/36.0f) : 0;

		Rectangle glyphBounds = glyph.getBounds();
		int width = glyphBounds.width + shadow+2*strokeWidth;
		int height = glyphBounds.height + shadow+2*strokeWidth;

		if (width<=0) width=1;
		if (height<=0) height=1;    //  required by BufferedImage!

		BufferedImage bimg;
		if (transparent)
			bimg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		else
			bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		ety.img = new SerialImage(bimg);

		Graphics2D g = (Graphics2D)bimg.getGraphics();
		if (!transparent) {
			g.setColor(Color.WHITE);
			g.fillRect(0,0,width,height);
		}

		ImgUtil.setRenderingHints(g);

		int xoff = glyphBounds.x-strokeWidth;
		int yoff = glyphBounds.y-strokeWidth;
		g.translate(-xoff, -yoff);

		if (shadow > 0) {
			g.translate(shadow,shadow);
			g.setColor(SHADOW_64);
			g.fill(strokedGlyph);
			g.translate(-shadow,-shadow);
		}

		BufferedImage txtMask = null;
		Graphics2D g2 = null;
		if (white.useTexture() || black.useTexture())
		{
			txtMask = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			g2 = (Graphics2D)txtMask.getGraphics();
			g2.translate(-xoff, -yoff);
			ImgUtil.setRenderingHints(g2);
		}

        switch (white.mode)
        {
        case Surface.COLOR:
        case Surface.GRADIENT:
            g.setPaint(white.getPaint(width,height));
			ImgUtil.fill(g,mask);
            break;

        case Surface.TEXTURE:
            g2.setColor(TRANSPARENT);
            g2.fillRect(xoff, yoff,width,height);

            g2.setColor(Color.white);
            ImgUtil.fill(g2,mask);

            TextureBlendOp op = new TextureBlendOp(white.texture, TextureCache.LEVEL_MAX,
                                    TextureBlendOp.REPLACE,
                                    rand.nextInt(), rand.nextInt());
            op.filter(txtMask, bimg);
			/**	TODO fix BUG: drop shadow must not be erased	*/
//			g.drawImage(txtMask, op, xoff, yoff);
            /*  we could use a TexturePaint instead
                but it's memory usage is proportional to the size of the *texture* image
                the memory usage of TextureBlendOp is proportional to the size of the *target* image
                (which is supposed to be small, compared to the texture image)
            */
            break;
        }

		switch (black.mode)
        {
        case Surface.COLOR:
        case Surface.GRADIENT:
            g.setPaint(black.getPaint(width,height));
			g.fill(glyph);
            break;

        case Surface.TEXTURE:
            g2.setColor(TRANSPARENT);
            g2.setComposite(AlphaComposite.Clear);
            g2.clearRect(xoff, yoff,width,height);

            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Color.white);
            g2.fill(glyph);

            TextureBlendOp op = new TextureBlendOp(black.texture, TextureCache.LEVEL_MAX,
                                TextureBlendOp.DECAL,
                                rand.nextInt(), rand.nextInt());
            op.filter(txtMask, bimg);
            break;
        }

		FontMetrics fmx = g.getFontMetrics(font);
		LineMetrics lmx = fmx.getLineMetrics(text,g);
		float ascent = lmx.getAscent();
		if (ascent < 0) {
			/** workaround for bad metrics data.
			 * DiagramTTHabsburg contains wrong metrics data for platforms other than windows.
			 * */
			ascent = -3*ascent;
		}
		else
			ascent = Math.min(ascent,font.getSize2D());
			//  what is more appropriate: ascent or nominal font size ?!
			//  usually, they are (almost) identical

		ety.bounds = new Rectangle(xoff, (int)Math.round(ascent+yoff), width, height);
	}

	public static void draw(BufferedImage bimg, int x0, int y0,
	                        Font font, String text,
	                        Surface white, Surface black, boolean useShadow)
	{
		Shape glyph = ImgUtil.getOutline(font,text);
		Shape strokedGlyph;
		Shape[] mask;

		//	width of surrounding (white) stroke
		int strokeWidth = (int)Math.round(font.getSize()/24.0f);
		if (strokeWidth > 0) {
			Stroke str = new BasicStroke(strokeWidth);
			strokedGlyph = str.createStrokedShape(glyph);
			mask = ImgUtil.getMask(strokedGlyph,true);
		}
		else {
			mask = ImgUtil.getMask(strokedGlyph = glyph,true);
		}

		//	width of drop shadow
		int shadow = useShadow ?(int)Math.round(font.getSize()/36.0f) : 0;

		Rectangle glyphBounds = glyph.getBounds();
		int width = glyphBounds.width + shadow+2*strokeWidth;
		int height = glyphBounds.height + shadow+2*strokeWidth;

		if (width<=0) width=1;
		if (height<=0) height=1;    //  required by BufferedImage!

		Graphics2D g = (Graphics2D)bimg.getGraphics();
		ImgUtil.setRenderingHints(g);

		g.translate(x0, y0);

		int xoff = glyphBounds.x-strokeWidth;
		int yoff = glyphBounds.y-strokeWidth;

		if (shadow > 0) {
			g.translate(shadow,shadow);
			g.setColor(SHADOW_64);
			g.fill(strokedGlyph);
			g.translate(-shadow,-shadow);
		}

		BufferedImage txtMask = null;
		Graphics2D g2 = null;
		if (white.useTexture() || black.useTexture())
		{
			txtMask = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			g2 = (Graphics2D)txtMask.getGraphics();
			g2.translate(-xoff, -yoff);
			ImgUtil.setRenderingHints(g2);
		}

        switch (white.mode)
        {
        case Surface.COLOR:
        case Surface.GRADIENT:
            g.setPaint(white.getPaint(width,height));
			ImgUtil.fill(g,mask);
            break;

        case Surface.TEXTURE:
            g2.setColor(TRANSPARENT);
            g2.fillRect(xoff, yoff,width,height);

            g2.setColor(Color.white);
            ImgUtil.fill(g2,mask);

            TextureBlendOp op = new TextureBlendOp(white.texture, TextureCache.LEVEL_MAX,
                                    TextureBlendOp.REPLACE,
                                    rand.nextInt(), rand.nextInt());
            op.filter(txtMask, bimg);
			/**	TODO fix BUG: drop shadow must not be erased	*/
//			g.drawImage(txtMask, op, xoff, yoff);
            /*  we could use a TexturePaint instead
                but it's memory usage is proportional to the size of the *texture* image
                the memory usage of TextureBlendOp is proportional to the size of the *target* image
                (which is supposed to be small, compared to the texture image)
            */
            break;
        }

		switch (black.mode)
        {
        case Surface.COLOR:
        case Surface.GRADIENT:
            g.setPaint(black.getPaint(width,height));
			g.fill(glyph);
            break;

        case Surface.TEXTURE:
            g2.setColor(TRANSPARENT);
            g2.setComposite(AlphaComposite.Clear);
            g2.clearRect(xoff, yoff,width,height);

            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(Color.white);
            g2.fill(glyph);

            TextureBlendOp op = new TextureBlendOp(black.texture, TextureCache.LEVEL_MAX,
                                TextureBlendOp.DECAL,
                                rand.nextInt(), rand.nextInt());
            op.filter(txtMask, bimg);
            break;
        }
	}

	//-------------------------------------------------------------------------------
	//	Rendering Thread
	//-------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------
	//	Font Sampling
	//-------------------------------------------------------------------------------
	public static final void main(String[] args)
	{
		try {

            File ff = new File(args[0]);
            Font f;

            if (ff.exists())
            {
                f = Font.createFont(Font.TRUETYPE_FONT,new FileInputStream(ff));
                f = f.deriveFont(Font.PLAIN, 80);
            }
            else
                f = new Font(args[0],Font.PLAIN,80);

            File out = new File(f.getName()+".png");

            //printFontSample(f,out);
			printFontCapture(f,args[1],80,out);

		} catch (Throwable thw) {
			thw.printStackTrace();
		}
	}
/*

	protected static void printFontList(java.util.List list, int sampleSize)
		throws IOException
	{
		String fontnames[];
		if (list==null || list.size()==0) {
			GraphicsEnvironment grafenv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		 	fontnames = grafenv.getAvailableFontFamilyNames();
		}
			fontnames = StringUtil.toArray(list);

		for (int i=0; i<fontnames.length; i++) {
			System.out.println(fontnames[i]);
			printFontSample(fontnames[i], sampleSize);
		}
	}
*/
	protected static void printFontCapture(Font font, String piece, int size, File out) throws IOException
	{
		Surface white = Surface.newColor(Color.white);
		Surface black = Surface.newColor(Color.black);
		Rectangle bounds = new Rectangle(0,0,size,size);

		MapEntry ety = new MapEntry();
		FontCapture.draw(ety,font,piece,white,black,true,false);

		ImageIO.write(ety.img.img,"png",out);
	}

	protected static void printFontSample(Font f, File output)
		throws IOException
	{
		int width = 0;
		int count = 0;

		//BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		Frame proof = new Frame();
		proof.setBounds(780,80,200,200);
		proof.show();

		Graphics pg = proof.getGraphics();
		pg.translate(20,60);

		Font pf = new Font("SansSerif", Font.PLAIN, 12);
//		Font f = FontUtil.newFont(fontName, Font.PLAIN, sampleSize);
		FontMetrics fm = pg.getFontMetrics(f);

		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int height = ascent+descent;

		for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
			char c = (char)i;
			if (f.canDisplay(c))  {
				count ++;
				int w = fm.charWidth(c);
				if (w > width) width = w;
			}
		}

		System.out.println(count);

		if (count > 1000) {
			System.out.println();
			System.out.println("font too large");
			System.out.println();
			proof.dispose();
			return;
		}

		int total_width = 16 * (width+4);
		int total_height = ((count+15)/16) * (height+20);

		BufferedImage img = new BufferedImage(total_width,total_height, BufferedImage.TYPE_INT_RGB);
		//	proof.getGraphicsConfiguration().createCompatibleImage(total_width,total_height);
		Graphics g = img.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,total_width,total_height);
		//((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		count = 0;
		for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
			char c = (char)i;
			if (f.canDisplay(c))  {
				int x = (count%16)*(width+4) + 2;
				int y = (count/16)*(height+20) + 2;
				int w = fm.charWidth(c);

				g.setColor(Color.blue);
				g.drawRect(x,y, w,height);
				g.drawLine(0,ascent,w,ascent);

				g.setFont(pf);
				g.drawString(toString(c), x+2, y+height+12);
				g.drawString(Integer.toHexString(c), x+14, y+height+12);

				g.setFont(f);
				g.setColor(Color.black);
				g.drawString(toString(c), x, y+ascent);
				//try { in.readLine(); } catch (IOException ex) { }
				//pg.drawImage(img, 20,60, width+4,height+20, x,y, width+4,height+20, null);

				count++;
			}

		}
		proof.dispose();

		//	store
		ImageIO.write(img,"jpeg",output);
//		new Acme.JPM.Encoders.GifEncoder(img,out,false).encode();
	}


	private static final String toString(char c)
	{
		return new Character(c).toString();
	}
}
