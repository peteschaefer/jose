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

package de.jose.task.io;

import de.jose.export.ExportContext;
import de.jose.export.ExportConfig;
import de.jose.util.file.FileUtil;
import de.jose.util.print.FOPUtil;
import de.jose.util.print.JoConsoleLogger;
import de.jose.util.print.Triplet;
import de.jose.util.xml.XMLUtil;
import de.jose.comm.Command;
import de.jose.view.style.JoStyleContext;

import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

//import org.apache.fop.apps.Driver;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.*;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOTreeBuilder;
import org.apache.fop.fonts.*;
import org.apache.fop.fonts.Font;
import org.apache.fop.render.awt.AWTRenderer;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XSLFOExport
 * 
 * @author Peter Sch�fer
 */

public class XSLFOExport
        extends XMLExport
{
	//	if true, print result immediately
	public Runnable printOnCompletion = null;

    public XSLFOExport(ExportContext context)
        throws Exception
    {
        super("XSL-FO Export",context);
    }


    public int work()
        throws Exception
    {
  	    Source source = createSAXSource(context);
	    OutputStream outputStream = null;
	    String fileName = "output";

	    if (context.target instanceof File)
			try {
				outputStream = new FileOutputStream((File)context.target);
				fileName = ((File)context.target).getName();
			} catch (FileNotFoundException e) {
				//  could not open file (write protected, in use...)
				setProgressText(e.getMessage());
				return FAILURE;
			}
		else if (context.target instanceof OutputStream) {
	        outputStream = (OutputStream)context.target;
	        fileName = "output.pdf";    //  export pdf by default TODO pass this as parameter, or something...
	    }
	    else if (context.target instanceof Writer)
	           throw new IllegalArgumentException();


		try {

			File xslFile = ExportConfig.getFile(context.config);
			boolean embed_fonts = true;
			//	there is no reason to disable it. We set it ALWAYS.
			//context.profile.getBoolean("xsl.pdf.embed",true);

			result = process(source, xslFile, outputStream,
			        fileName,
			        context.styles, embed_fonts);

			if (printOnCompletion != null)
				SwingUtilities.invokeLater(printOnCompletion);

		    return SUCCESS;

	    } finally {
		    try {
			    if (outputStream!=null) outputStream.close();
		    } catch (IOException e) {
			    //  ignore
		    }
	    }
    }

	public static Result process(Source source, File xslFile, OutputStream outputStream,
	                           String targetName,
	                           JoStyleContext styles, boolean embed_fonts)
            throws TransformerException, IOException, SAXException {
		/*  XSLT transformer */
		XMLUtil.getTransformerFactory().setErrorListener(FOPUtil.gConsoleLogger);
		Transformer tf = XMLUtil.getTransformer(xslFile);
		tf.setErrorListener(FOPUtil.gConsoleLogger);

		Fop fop;
		Result result;

	/*	FOPUtil.config();
		if (styles!=null)
			FOPUtil.assertFontMetrics(styles,true,embed_fonts);
      */  /* note that this doesn't work for inlined styles !
         *  inlined styles are parsed directly from the DB, so there's little chance to fetch the fonts
         *  before processing them...
         *  @see de.jose.util.style.MarkupParser
         * */
		outputStream = new BufferedOutputStream(outputStream);

		/* transform source via XSL into XSL-FO    */
		if (FileUtil.hasExtension(targetName,"txt"))
			fop = FOPUtil.newFop(MimeConstants.MIME_PLAIN_TEXT,outputStream);
		else if (FileUtil.hasExtension(targetName,"ps"))
			fop = FOPUtil.newFop(MimeConstants.MIME_POSTSCRIPT,outputStream);			//  PostScript
		else if (FileUtil.hasExtension(targetName,"rtf"))
			fop = FOPUtil.newFop(MimeConstants.MIME_RTF,outputStream);			//  PostScript
		else if (FileUtil.hasExtension(targetName,"svg"))
			fop = FOPUtil.newFop(MimeConstants.MIME_SVG,outputStream);     //  SVG requires Batik ! (not included with jose)
		else if (FileUtil.hasExtension(targetName,"xml"))
			fop = null;
		else if (FileUtil.hasExtension(targetName,"fo")) //  create XSL-FO only
			fop = null;	//	print xml (as xsl-fo)
		else if (FileUtil.hasExtension(targetName,"if")) //  create XSL-FO only
			fop = FOPUtil.newFop(MimeConstants.MIME_FOP_IF,outputStream);	//	print xml (as xsl-fo intermediate format) debugging only
		else if (FileUtil.hasExtension(targetName,"at")) //  create XSL-FO only
			fop = FOPUtil.newFop(MimeConstants.MIME_FOP_AREA_TREE,outputStream);	//	print xml (as xsl-fo) debugging only
		else
			fop = FOPUtil.newFop(MimeConstants.MIME_PDF, outputStream);

		try {
			if (fop != null) {
				//Make sure the XSL transformation's result is piped through to FOP
				result = new SAXResult(fop.getDefaultHandler());
				tf.transform(source, result);
/*
				FontInfo finfo = areaTreeHandler.getFontInfo();
				FontTriplet trip1 = finfo.fontLookup("Chess Berlin","normal",400);
				FontTriplet trip2 = finfo.fontLookup("Z003","normal",400);
				System.out.println(trip1.toString());
				System.out.println(trip2.toString());*/
//			FormattingResults results = fop.getResults();
			} else {
				result = new StreamResult(outputStream);  //  XSL-FO, not rendered (for debugging)
				tf.transform(source, result);
			}
		} finally {
			outputStream.flush();
			outputStream.close();
		}

		XMLUtil.releaseTransformer(xslFile,tf);
		return result;
	}


	public static class Preview extends XSLFOExport
	{
		//public AWTRenderer renderer;
		public AreaTreeHandler areaTreeHandler=null;

		public Preview (ExportContext context, Runnable onComplete, Command onSuccess) throws Exception
		{
			super(context);
			setOnSuccess(onComplete);
			setOnFailure(onComplete);
			setOnSuccess(onSuccess);
			pollProgress = 1000;
		}

		public int work() throws TransformerException, IOException, SAXException, URISyntaxException {
			Source source = createSAXSource(context);

			/*  XSLT transformer */
			File xslFile = ExportConfig.getFile(context.config);
			XMLUtil.getTransformerFactory().setErrorListener(FOPUtil.gConsoleLogger);
			Transformer tf = XMLUtil.getTransformer(xslFile);
			tf.setErrorListener(FOPUtil.gConsoleLogger);
/*
			Set<Triplet> fontInfo = context.styles.collectFontInfo();
			for(Triplet tp : fontInfo) {
				java.awt.Font font = de.jose.util.FontUtil.getFont(tp.family, java.awt.Font.PLAIN,false);
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
			}
 */
			//  create XSL-FO
			//FOPUtil.config();
			//FOPUtil.assertFontMetrics(context.styles,false,false);

			//FopFactory fopFactory = getFopFactory();
			//FOUserAgent agent = fopFactory.newFOUserAgent();
			//AWTRenderer renderer = new AWTRenderer(agent);
			//agent.setRendererOverride(renderer);

			Fop fop = FOPUtil.newPreviewFop();

			//	navigating through the FOP class hierarchy is a bit of a ... nuisance
			//	we need a Renderer to actually print pages; AreaTreeHandle has it.
			//	@see FOPrintableDocument
			this.areaTreeHandler = FOPUtil.getAreaTreeHandler(fop);
			//FontInfo previewFontInfo = this.areaTreeHandler.getFontInfo();
			//	copy font info from PDF renderer
/*
			Fop pdfFop = FOPUtil.newFop(MimeConstants.MIME_PDF,new NullOutputStream());
			FontInfo pdfFontInfo = FOPUtil.getAreaTreeHandler(pdfFop).getFontInfo();

			org.apache.fop.render.Renderer rend = fop.getUserAgent().getRendererOverride();
			rend.setupFontInfo(pdfFontInfo);

			previewFontInfo = FOPUtil.getAreaTreeHandler(fop).getFontInfo();
			FontTriplet trip1 = previewFontInfo.fontLookup("Chess Berlin","normal",400);
			FontTriplet trip2 = pdfFontInfo.fontLookup("Chess Berlin","normal",400);
			System.out.println(trip1.toString());
			System.out.println(trip2.toString());
*/
			/* 	for reasons beyond our imagination, PDF-FOP can pick up a list of custom fonts through the configuration file fop/fop.xconf
				AWT-FOP can't do this. why not?
				don't bother - just copy the font information NOW
			 */
			//FOPUtil.copyFontInfo(pdfFontInfo,previewFontInfo);

			//trip1 = previewFontInfo.fontLookup("Chess Berlin","normal",400);
			//System.out.println(trip1.toString());

/*			FontManager fontmgr = fopFactory.getFontManager();
			InternalResourceResolver uriResolver = ResourceResolverFactory.createInternalResourceResolver(
					URI.create("file:///home/schaefer/src/jose/fonts"),
					ResourceResolverFactory.createDefaultResourceResolver());
			FontCollection customFonts = new CustomFontCollection(uriResolver, Collections.emptyList(), false);
			fontmgr.setup(finfo, new FontCollection[]{customFonts});
			FontTriplet trip1 = finfo.fontLookup("Chess Berlin","normal",400);
			FontTriplet trip2 = finfo.fontLookup("Z003","normal",400);
*/
			Result result = new SAXResult(fop.getDefaultHandler());
			tf.transform(source,result);
/*
			finfo = areaTreeHandler.getFontInfo();
			FontTriplet trip3 = finfo.fontLookup("Chess Berlin","normal",400);
			FontTriplet trip4 = finfo.fontLookup("Z003","normal",400);
			System.out.println(trip3.toString());
			System.out.println(trip4.toString());
*/
			XMLUtil.releaseTransformer(xslFile,tf);
			//FOPUtil.release(driver);
			return SUCCESS;
		}
	}

}