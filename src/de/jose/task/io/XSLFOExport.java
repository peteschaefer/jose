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
import de.jose.util.xml.XMLUtil;
import de.jose.util.print.FOPUtil;
import de.jose.comm.Command;
import de.jose.view.style.JoStyleContext;

import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.render.awt.AWTRenderer;

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
	        throws TransformerException, IOException, FOPException
	{
		/*  XSLT transformer */
		XMLUtil.getTransformerFactory().setErrorListener(FOPUtil.gConsoleLogger);
		Transformer tf = XMLUtil.getTransformer(xslFile);
		tf.setErrorListener(FOPUtil.gConsoleLogger);

		Driver driver = null;
		Result result;

		FOPUtil.config();
		if (styles!=null)
			FOPUtil.assertFontMetrics(styles,true,embed_fonts);
        /* note that this doesn't work for inlined styles !
         *  inlined styles are parsed directly from the DB, so there's little chance to fetch the fonts
         *  before processing them...
         *  @see de.jose.util.style.MarkupParser
         * */

		/* transform source via XSL into XSL-FO    */
		if (FileUtil.hasExtension(targetName,"txt"))
			driver = FOPUtil.getDriver(Driver.RENDER_TXT);
		else if (FileUtil.hasExtension(targetName,"ps"))
			driver = FOPUtil.getDriver(Driver.RENDER_PS);      //  PostScript
		else if (FileUtil.hasExtension(targetName,"svg"))
			driver = FOPUtil.getDriver(Driver.RENDER_SVG);     //  SVG requires Batik ! (not included with jose)
		else if (FileUtil.hasExtension(targetName,"xml"))
			driver = FOPUtil.getDriver(Driver.RENDER_XML);     //  internal XML (for debugging)
		else if (FileUtil.hasExtension(targetName,"fo")) //  create XSL-FO only
			driver = null;
		else
			driver = FOPUtil.getDriver(Driver.RENDER_PDF);

		if (driver != null) {
			//Setup the OutputStream for FOP
			driver.setOutputStream(outputStream);

			//Make sure the XSL transformation's result is piped through to FOP
			result = new SAXResult(driver.getContentHandler());
			tf.transform(source,result);

			FOPUtil.release(driver);
		}
		else {
			result = new StreamResult(outputStream);  //  XSL-FO, not rendered (for debugging)
			tf.transform(source,result);
		}

		XMLUtil.releaseTransformer(xslFile,tf);
		return result;
	}


	public static class Preview extends XSLFOExport
	{
		public AWTRenderer renderer;

		public Preview (ExportContext context, Runnable onComplete, Command onSuccess) throws Exception
		{
			super(context);
			setOnSuccess(onComplete);
			setOnFailure(onComplete);
			setOnSuccess(onSuccess);
			pollProgress = 1000;
		}

		public int work() throws TransformerException, IOException, FOPException
		{
			Source source = createSAXSource(context);

			/*  XSLT transformer */
			File xslFile = ExportConfig.getFile(context.config);
			XMLUtil.getTransformerFactory().setErrorListener(FOPUtil.gConsoleLogger);
			Transformer tf = XMLUtil.getTransformer(xslFile);
			tf.setErrorListener(FOPUtil.gConsoleLogger);

			//  create XSL-FO
			FOPUtil.config();
			FOPUtil.assertFontMetrics(context.styles,false,false);

			Driver driver = FOPUtil.getDriver(Driver.RENDER_AWT);
			//Setup logging here: driver.setLogger(...

			//Make sure the XSL transformation's result is piped through to FOP
			Result result = new SAXResult(driver.getContentHandler());
			tf.transform(source,result);

			XMLUtil.releaseTransformer(xslFile,tf);
			FOPUtil.release(driver);

			renderer = (AWTRenderer)driver.getRenderer();
			return SUCCESS;
		}
	}

}