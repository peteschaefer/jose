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

package de.jose.util.print;

import de.jose.util.file.FileUtil;
import de.jose.Application;
import de.jose.sax.AbstractObjectReader;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.fonts.TTFFile;
import org.apache.fop.fonts.TTFCmapEntry;
//import org.apache.fop.fonts.TTFMtxEntry;

/**
 * AWTReader
 *
 * creates font metrics files for FOP.
 * Similar to org.apache.fop.fonts.apps.TTFReader but metrics info is extracted
 * from java.awt.Font objects (rather than from the TTF file).
 *
 * Note that the generated metrics files are identical to the metrics info use by AWTRenderer
 * (i.e. if you use PDFRenderer with these files you should get exactly the same layout).
 * 
 * @author Peter Sch�fer
 */

public class AWTReader
        extends AbstractObjectReader
{
	protected TransformerFactory tfFactory;
	protected Transformer copyTransformer;

	protected File diskFile;
	protected File metricsFile;
	protected Graphics2D g;
	protected URL url;
	protected TTFFile ttf;

	protected Font font;
	protected FontMetrics fmx;


	protected static final int BASE_SIZE = 1000;
	/**
	 * This factor multiplies the calculated values to scale
	 * to FOP internal measurements
	 */
	public static final int FONT_FACTOR = 1000 / BASE_SIZE;


	public AWTReader() throws TransformerConfigurationException
	{
		tfFactory = TransformerFactory.newInstance();
		copyTransformer = tfFactory.newTransformer();
	}

	public void process(File ttfFile, File targetFile) throws FontFormatException, IOException, TransformerException
	{
		//  setup AWT stuff
		Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ttfFile));
		//  in case the font is not loaded

		BufferedImage bimg = new BufferedImage(1,1, BufferedImage.TYPE_4BYTE_ABGR);
		g = (Graphics2D)bimg.getGraphics();

		diskFile = ttfFile;
		metricsFile = targetFile;

		ttf = new TTFReader().loadTTF(ttfFile.getAbsolutePath(),null);
		url = new URL("file",null,ttfFile.getAbsolutePath());

		System.out.print("[");
		System.out.print(metricsFile.getName());

		Source source = new SAXSource(this,new InputSource());
		Result result = new StreamResult(metricsFile);

		copyTransformer.transform(source,result);

		System.out.println("]");
	}

	private int getCapHeight() {
		return getAscender();
	}

	private int getAscender() {
		int realAscent = fmx.getAscent()
		                  - (fmx.getDescent() + fmx.getLeading());
		return FONT_FACTOR * realAscent;
	}

	private int getDescender() {
		return (-1 * FONT_FACTOR * fmx.getDescent());
	}

	private int getXHeight() {
		Rectangle2D bounds = fmx.getStringBounds("m",0,1,g);
		return (int)bounds.getHeight();
	}


	public void parse(InputSource input) throws IOException, SAXException
	{
//		Font font = metrics.getFont(ttf.getFamilyName(),0,BASE_SIZE);
		font = new Font(ttf.getFamilyName(),0,BASE_SIZE);
		g.setFont(font);
		fmx = g.getFontMetrics();

		//  create metrics XML
		AttributesImpl root_attrs = newAttributesImpl();
		root_attrs.addAttribute("","","type","CDATA","TYPE0");

		handler.startDocument();
		handler.startElement("font-metrics",root_attrs);

			handler.element("font-name", font.getName());
			handler.element("embed", diskFile.getAbsolutePath());

			handler.element("cap-height", getCapHeight());
			handler.element("x-height", getXHeight());
			handler.element("ascender", getAscender());
			handler.element("descender", getDescender());

			handler.startElement("bbox");
        //        int[] bb = ttf.getFontBBox();
				Rectangle2D bb = font.getMaxCharBounds(g.getFontRenderContext());

				double left = Math.min(bb.getX(), bb.getX()+bb.getWidth());
				double top = Math.min(bb.getY(), bb.getY()+bb.getHeight());
				double width = Math.abs(bb.getWidth());
				double height = Math.abs(bb.getHeight());

				handler.element("left", (int)left*FONT_FACTOR);
				handler.element("bottom", -(int)(top+height)*FONT_FACTOR);
				handler.element("right", (int)(left+width)*FONT_FACTOR);
				handler.element("top", -(int)top*FONT_FACTOR);
			handler.endElement("bbox");

			handler.element("flags", ttf.getFlags());
			handler.element("stemv", ttf.getStemV());
			handler.element("italicangle", (int)font.getItalicAngle());

			//  CID is assumed
			handler.element("subtype","TYPE0");
			handler.startElement("multibyte-extras");
				handler.element("cid-type","CIDFontType2");
				handler.element("default-width",0);

				createBFRanges();
				createCIDWidths();
				createKerning();

			handler.endElement("multibyte-extras");
		handler.endElement("font-metrics");
		handler.endDocument();
	}

	protected void createBFRanges() throws SAXException
	{
		AttributesImpl bf_attrs = newAttributesImpl();
		bf_attrs.addAttribute("","","us","CDATA", "");
		bf_attrs.addAttribute("","","ue","CDATA", "");
		bf_attrs.addAttribute("","","gi","CDATA", "");

		handler.startElement("bfranges");
			ArrayList cmaps = ttf.getCMaps();
			for (int i=0; i < cmaps.size(); i++)
			{
				TTFCmapEntry ce = (TTFCmapEntry)cmaps.get(i);
				bf_attrs.setAttribute(0,"","","us","CDATA", String.valueOf(ce.unicodeStart));
				bf_attrs.setAttribute(1,"","","ue","CDATA", String.valueOf(ce.unicodeEnd));
				bf_attrs.setAttribute(2,"","","gi","CDATA", String.valueOf(ce.glyphStartIndex));

				handler.element("bf",null,bf_attrs);
			}
		handler.endElement("bfranges");
	}

	protected void createCIDWidths() throws SAXException
	{
		AttributesImpl cid_width_attrs = newAttributesImpl();
		cid_width_attrs.addAttribute("","","startIndex","CDATA","0");

		AttributesImpl wx_attrs = newAttributesImpl();
		wx_attrs.addAttribute("","","w","CDATA","0");

		handler.startElement("cid-widths",cid_width_attrs);

//			int[] wx = ttf.getWidths();
		/*
			TTFMtxEntry[] gmx = ttf.getGlyphMetrics();
			for (int i = 0; i < gmx.length; i++) {
				int width = gmx[i].getWidth();

				ArrayList unicodes = gmx[i].getUnicodes();
				if (!unicodes.isEmpty()) {
					char unicode = (char)Util.toint(unicodes.get(0));
					Rectangle2D box = fmx.getStringBounds(String.valueOf(unicode),0,1,g);
					int awt_width = (int)box.getWidth()*FONT_FACTOR;
					if (awt_width!=0) width = awt_width;
				}

				wx_attrs.setAttribute(0,"","","w","CDATA", String.valueOf(width));
				handler.element("wx",null,wx_attrs);
				//  TODO get width from fmx; but in which order ?
			}
		*/
		handler.endElement("cid-widths");
	}

	protected void createKerning() throws SAXException
	{
		AttributesImpl kerning_attrs = newAttributesImpl();
		kerning_attrs.addAttribute("","","kpx1","CDATA","0");

		AttributesImpl pair_attrs = newAttributesImpl();
		pair_attrs.addAttribute("","","kpx2","CDATA","0");
		pair_attrs.addAttribute("","","kern","CDATA","0");

		// Get kerning
		Iterator en = ttf.getKerning().keySet().iterator();

	    while (en.hasNext()) {
			Integer kpx1 = (Integer)en.next();

		    kerning_attrs.setAttribute(0,"","","kpx1","CDATA",kpx1.toString());
		    handler.startElement("kerning",kerning_attrs);

		    HashMap h2 = (HashMap)ttf.getKerning().get(kpx1);
			for (Iterator enum2 = h2.keySet().iterator(); enum2.hasNext(); ) {
		            Integer kpx2 = (Integer)enum2.next();
					Integer val = (Integer)h2.get(kpx2);

					pair_attrs.setAttribute(0,"","","kpx2","CDATA",kpx2.toString());
					pair_attrs.setAttribute(1,"","","kern","CDATA",val.toString());
					handler.element("pair",null,pair_attrs);
		       }

		    handler.endElement("kerning");
		 }
	}

	public static int fatalError(String message)
	{
		System.err.println(message);
		System.err.println();
		System.err.println("java de.jose.util.print.AWTReader <font.ttf> [metrics.xml]");
		return -1;
	}

	public static void main(String[] args)
	{
		try {
			File ttfFile = null;
			File metricsFile = null;

			if (args.length >= 1)
				ttfFile = new File(args[0]);
			if (args.length >= 2)
				metricsFile = new File(args[1]);

			if (ttfFile==null) {
				fatalError("path to TTF file expected");
				return;
			}
			if (!ttfFile.exists()) {
				fatalError("no file specified");
				return;
			}

			if (metricsFile==null)
				metricsFile = new File(ttfFile.getParentFile(), FileUtil.trimExtension(ttfFile.getName())+".XML");

			File metricsDir = metricsFile.getParentFile();
			if (!metricsDir.exists())
				metricsDir.mkdirs();

			new AWTReader().process(ttfFile,metricsFile);

		} catch (Throwable e) {
			e.printStackTrace();
			fatalError(e.getClass()+": "+e.getLocalizedMessage());

		}
	}

	private static AttributesImpl newAttributesImpl()
	{
		/**
		 * package has moved in JDK 1.5 got to use reflection
		 */
		String className = "com.sun.org.apache.xml.internal.utils.MutableAttrListImpl";
		try {

			return (AttributesImpl)Class.forName(className).newInstance();

		} catch (Exception e) {
			Application.error(e);
			return null;
		}
	}
}