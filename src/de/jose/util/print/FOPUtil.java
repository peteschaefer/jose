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

package de.jose.util.print;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.FontInfo;
import org.apache.fop.configuration.FontTriplet;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Options;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.TTFFile;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.svg.SVGRenderer;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.render.mif.MIFRenderer;
import org.apache.fop.render.txt.TXTRenderer;
import org.apache.fop.render.ps.PSRenderer;
import org.apache.fop.render.pcl.PCLRenderer;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.viewer.SecureResourceBundle;
import org.apache.fop.viewer.Translator;
import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.messaging.MessageHandler;
import org.apache.avalon.framework.logger.Logger;
//import org.apache.batik.svggen.SVGGeneratorContext;
//import org.apache.batik.svggen.SVGGraphics2DIOException;
//import org.apache.batik.svggen.SVGGraphics2D;
import de.jose.Application;
import de.jose.view.style.JoStyleContext;
import de.jose.view.ConsolePanel;
import de.jose.view.JoPanel;
import de.jose.task.io.XSLFOExport;
import de.jose.util.SoftCache;
import de.jose.util.FontUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.Document;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;

/**
 * setup fop Configuration
 * @author Peter Schäfer
 */

public class FOPUtil
{
	public static File theFopDirectory;

	private static boolean inited = false;
	private static boolean fontScanned = false;

	private static List awtFontConfig = new ArrayList();
	private static List pdfFontConfig_Embed = new ArrayList();
	private static List pdfFontConfig_NoEmbed = new ArrayList();

	private static final String TRANSLATION_PATH =   "/org/apache/fop/viewer/resources/";

	/** stores a FOP driver and several FOP renderers */
	protected static SoftCache gCache = new SoftCache();

	public static JoConsoleLogger gConsoleLogger = new JoConsoleLogger();

	private static Object mutex = new Object();


	public static void config() throws FOPException, IOException
	{
		if (inited) return;

		synchronized(mutex) {
			if (inited) return;

			theFopDirectory = new File(Application.theWorkingDirectory,"fop");
			Configuration.put("fontBaseDir",theFopDirectory.getAbsolutePath());

			//  pick up additional configuration
			File userconfig = new File(theFopDirectory,"userconfig.xml");
			if (userconfig.exists())
				new Options(userconfig);

			//  font configs are created later, dynamically

			inited = true;
		}
	}

	public static Driver getDriver() throws IOException, FOPException
	{
		FOPUtil.config();

		Driver driver = (Driver)gCache.pop(Driver.class.getName());
		if (driver==null)
			driver = new Driver();
		else
			driver.reset();

		driver.setLogger(gConsoleLogger);
		MessageHandler.setScreenLogger(gConsoleLogger);
		return driver;
	}

	public static Driver getDriver(int rend) throws IOException, FOPException
	{
		Driver driver = getDriver();
		Renderer renderer = getRenderer(rend);
		driver.setRenderer(renderer);
		return driver;
	}

	public static Renderer getRenderer(int rend) throws IOException
	{
		Renderer renderer = (Renderer)gCache.pop(getRendererClassName(rend));
		if (renderer==null)
			renderer = createRenderer(rend);
		else if (renderer instanceof AWTRenderer)
			renderer = resetAWTRenderer((AWTRenderer)renderer);

		renderer.setLogger(gConsoleLogger);
		return renderer;
	}

	public static void release(Object resource)
	{
		gCache.push(resource.getClass().getName(),resource,true);  //  make available for reuse
	}


	protected static AWTRenderer resetAWTRenderer(AWTRenderer renderer)
	{
		/** when reusing AWTRenderers, flush the existing pages */
		while (renderer.getNumberOfPages() > 0)
			renderer.removePage(renderer.getNumberOfPages()-1);
		return renderer;
	}

	protected static Renderer createRenderer(int rend) throws IOException
	{
		Renderer result;
		switch (rend) {
		case Driver.RENDER_PDF:     result = new PDFRenderer(); break;
		case Driver.RENDER_PCL:     result = new PCLRenderer(); break;
		case Driver.RENDER_PS:      result = new PSRenderer(); break;
		case Driver.RENDER_TXT:     result = new TXTRenderer(); break;
		case Driver.RENDER_MIF:     result = new MIFRenderer(); break;
		case Driver.RENDER_XML:     result = new XMLRenderer(); break;
		case Driver.RENDER_SVG:     result = new SVGRenderer(); break;

		case Driver.RENDER_AWT:
		case Driver.RENDER_PRINT:
			Translator translator = getResourceBundle(TRANSLATION_PATH + "resources." + "en");
			result = new AWTRenderer(translator);
			break;

		default:
			throw new IllegalArgumentException();
		}

		gCache.push(result.getClass().getName(), result, true);
		return result;
	}

	protected static String getRendererClassName(int rend) throws IOException
	{
		switch (rend) {
		case Driver.RENDER_PDF:     return PDFRenderer.class.getName();
		case Driver.RENDER_PCL:     return PCLRenderer.class.getName();
		case Driver.RENDER_PS:      return PSRenderer.class.getName();
		case Driver.RENDER_TXT:     return TXTRenderer.class.getName();
		case Driver.RENDER_MIF:     return MIFRenderer.class.getName();
		case Driver.RENDER_XML:     return XMLRenderer.class.getName();
		case Driver.RENDER_SVG:     return SVGRenderer.class.getName();
		case Driver.RENDER_AWT:
		case Driver.RENDER_PRINT:   return AWTRenderer.class.getName();
		default:                    throw new IllegalArgumentException();
		}
	}

	private static SecureResourceBundle getResourceBundle(String path) throws IOException {
	    URL url = XSLFOExport.class.getResource(path);
	    if (url == null) {
	        // if the given resource file not found, the english resource uses as default
	        path = path.substring(0, path.lastIndexOf(".")) + ".en";
	        url = XSLFOExport.class.getResource(path);
	    }
	    return new SecureResourceBundle(url.openStream());
	}


	/**
	 * configure font support. Lookup metrics files; create them, if necessary.
	 * @param styleContext
	 */
	public static void assertFontMetrics(JoStyleContext styleContext, 
	                                     boolean for_pdf, boolean embed_fonts)
	{
		//  make sure all custom fonts are loaded (from jose/fonts)
		//  (these are not in the java font path, and need to be loaded explicitly)
		styleContext.assertCustomFonts();
		//  collect font info from style context
		Set triplets = styleContext.collectFontInfo();

		//  remove already configured fonts
		Iterator i = awtFontConfig.iterator();
		while (i.hasNext()) {
			FontInfo finfo = (FontInfo)i.next();
			List cfgTriplets = finfo.getFontTriplets();
			Iterator j = cfgTriplets.iterator();
			while (j.hasNext()) {
				org.apache.fop.configuration.FontTriplet cfgTriplet = (FontTriplet)j.next();
				triplets.remove(FOPUtil.toTriplet(cfgTriplet));
				//  note that org.apache.fop.configuration.FontTriplet has no equals() method
				//  but FontUtil.Triplet has, so we can call remove() !
			}
		}

		//  search/create metrics files and register them with FOP Configuration
		i = triplets.iterator();
		while (i.hasNext()) {
			Triplet trp = (Triplet)i.next();
			//  get associated TTF file (if known)
			File ttf = lookupTrueTypeFile(trp);  //  lookup TTF file in font path
			if (ttf!=null) {
				//  is there already a metrics file ?
				File mtx = new File(theFopDirectory, FileUtil.trimExtension(ttf.getName())+".XML");
				if (!mtx.exists()) createFontMetrics(ttf, mtx);
				if (mtx.exists()) {
					//  add a new config entry
					FontInfo finfo = newFontConfig(trp, ttf, mtx);
					pdfFontConfig_Embed.add(finfo);
					pdfFontConfig_NoEmbed.add(newFontConfig(trp,null,mtx));

					if (FontUtil.isCustomFont(trp.family))
						awtFontConfig.add(finfo);
					else if (!trp.bold && !trp.italic)
						awtFontConfig.add(finfo);
					else {
						ttf = lookupTrueTypeFile(trp.family,false,false);
						//  do not "embed" derived files
						awtFontConfig.add(newFontConfig(trp, ttf, mtx));
					}
					/**
					 *
					 */
					i.remove();
				}
			}
		}

		if (!for_pdf)
			Configuration.put("fonts", awtFontConfig);
		else if (embed_fonts)
			Configuration.put("fonts", pdfFontConfig_Embed);
		else
			Configuration.put("fonts", pdfFontConfig_NoEmbed);
	}

	protected static FontInfo newFontConfig(Triplet triplet, File ttf, File metrics)
	{
		List fopTriplets = new ArrayList();
		fopTriplets.add(FOPUtil.toFopTriplet(triplet));

		String ttf_url = (ttf!=null) ? ttf.getAbsolutePath():null;
		String metrics_url = (metrics!=null) ? metrics.getAbsolutePath():null;

		FontInfo finfo = new FontInfo(triplet.toString(), metrics_url, true, fopTriplets, ttf_url);
		return finfo;
	}

	protected static File lookupTrueTypeFile(Triplet trp)
	{
		return lookupTrueTypeFile(trp.family,trp.bold,trp.italic);
	}

	protected static File lookupTrueTypeFile(String family, boolean bold, boolean italic)
	{
		//  look for known fonts
		File ttf = FontUtil.getTrueTypeFile(family,bold,italic,false);
		if (ttf!=null && ttf.exists()) return ttf;

		//  scan more directories
		if (!fontScanned) {
			List<String> systemFonts = FontUtil.getSystemFontPaths();
			for(String systemFont : systemFonts)
				FontUtil.scanFontDirectories(systemFont,false);

			String javaFonts = FontUtil.getJavaFontPath();
			FontUtil.scanFontDirectories(javaFonts,false);

			File customFonts = new File(Application.theWorkingDirectory,"fonts");
			FontUtil.scanFontDirectory(customFonts,true);

			fontScanned = true;

			//  try again
			ttf = FontUtil.getTrueTypeFile(family,bold,italic,false);
			if (ttf!=null && ttf.exists())
				return ttf;
		}

		//  TODO scan user-defined paths, too

		//  try again
		ttf = FontUtil.getTrueTypeFile(family,bold,italic,true);
		if (ttf!=null && ttf.exists())
			return ttf;
		else
			return null;
	}

	protected static boolean createFontMetrics(File ttfFile, File mtxFile)
	{
		try {
			// [options] fontfile.ttf xmlfile.xml
//			String[] args = { ttfFile.getAbsolutePath(), /*mtx.getAbsolutePath()*/"fop/"+mtx.getName(), };
//			TTFReader.main(args);
//			AWTReader.main(args);

			//	TTFReader.main has a problem with windows file paths.
			//	let's do the processing step-by-step:
			TTFReader app = new TTFReader();
			TTFFile ttf = app.loadTTF(ttfFile.getAbsolutePath(), null);
			if (ttf==null) return false;

			Document doc = app.constructFontXML(ttf, null, null, null, null, true, null);
			FileOutputStream fos = new FileOutputStream(mtxFile.getAbsolutePath());
			TransformerFactory.newInstance().newTransformer().transform(
					new DOMSource(doc),
					new StreamResult(fos));
			fos.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public static Triplet toTriplet(FontTriplet fopTriplet) {
		return new Triplet(fopTriplet.getName(),
				!fopTriplet.getWeight().equals("normal"),
				!fopTriplet.getStyle().equals("normal"));
	}

	public static FontTriplet toFopTriplet(Triplet triplet) {
		return new FontTriplet(triplet.family,
		        triplet.bold ? "bold":"normal",
		        triplet.italic ? "italic":"normal");
	}

	/**
	 * create an SVG image
	 * we might need this in the future to insert images (e.g. charts) into PDF
	 *
	public Document drawSVG(Drawable draw)
	        throws ParserConfigurationException, SVGGraphics2DIOException
	{
		Rectangle2D bounds = draw.getBounds();
		Dimension dim = new Dimension((int)Math.ceil(bounds.getWidth()),(int)Math.ceil(bounds.getHeight()));

		//  create an XML Document
		Document dom = XMLUtil.newDocument();

		//  create an SVG Graphics context
		SVGGeneratorContext svgContext = SVGGeneratorContext.createDefault(dom);
		svgContext.setComment("Generated by jose with Batik SVG Generator");

		SVGGraphics2D svgg = new SVGGraphics2D(svgContext,false);
		svgg.setSVGCanvasSize(dim);

		//  paint to the SVG graphics
		draw.draw(svgg, bounds);

		return dom;
	}
*/
	/**
	 * insert an SVG image into a sax stream
	 *
	public void insertSVG(Drawable draw, ContentHandler sax) throws ParserConfigurationException, SVGGraphics2DIOException, TransformerException
	{
		Document dom = drawSVG(draw);
		XMLUtil.insertDOMintoSAX(dom,sax);
	}
*/

	public static class JoConsoleLogger
		implements Logger, ErrorListener
	{
		protected String toString(Throwable throwable)
		{
/*
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			pw.flush();
			return sw.toString();
*/          return throwable.getLocalizedMessage();
		}

		protected void println(String style, String text)
		{
			try {
				if (ConsolePanel.theConsole!=null)
					ConsolePanel.theConsole.println(style,text);
			} catch (IOException e) {
				Application.error(e);
			}
		}

		// ---------------------------------------------
		//      implements ErrorListener
		// ---------------------------------------------

		public void error(TransformerException exception) throws TransformerException
		{
			error(toString(exception));
//			exception.printStackTrace();
		}

		public void fatalError(TransformerException exception) throws TransformerException
		{
			fatalError(toString(exception));
//			exception.printStackTrace();
		}

		public void warning(TransformerException exception) throws TransformerException
		{
			warning(exception);
		}

		// ---------------------------------------------
		//      implements Logger
		// ---------------------------------------------

		public void debug(String s)             { println("info",s); }
		public void info(String s)          	{ println("output",s);	}
		public void warn(String s)      		{ println("error",s); }
		public void error(String s)         	{ println("error",s); }
		public void fatalError(String s)    	{ println("error",s); }

		public boolean isDebugEnabled()         { return true; }
		public boolean isInfoEnabled()          { return true; }
		public boolean isWarnEnabled()          { return true; }
		public boolean isErrorEnabled()     	{ return true; }
		public boolean isFatalErrorEnabled()	{ return true; }


		public void debug(String s, Throwable throwable)  {
			debug(s);
			debug(toString(throwable));
		}

		public void info(String s, Throwable throwable)	{
			info(s);
			info(toString(throwable));
		}

		public void warn(String s, Throwable throwable)	{
			warn(s);
			warn(toString(throwable));
		}

		public void error(String s, Throwable throwable) {
			error(s);
			error(toString(throwable));
		}

		public void fatalError(String s, Throwable throwable) {
			fatalError(s);
			fatalError(toString(throwable));
		}

		public Logger getChildLogger(String s) {
			return null;
		}
	}
}
