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

import de.jose.task.GameExport;
import de.jose.window.JoFileChooser;
import de.jose.export.ExportContext;
import de.jose.export.ExportConfig;
import de.jose.util.xml.XMLUtil;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * export to XML file
 */
public class XMLExport
        extends GameExport
{
	protected ExportContext context;
	protected Result result;

	protected XMLExport(String name, ExportContext context)
	    throws Exception
	{
		super(name,context.target,JoFileChooser.XML);
		this.context = context;
	    setSilentTime(500);

		setSource(context.source);
		pollProgress = 1000;
	}

    public XMLExport(ExportContext context)
        throws Exception
    {
        this("XML Export",context);
    }


	public void setTransformResult(Result result)
	{
		this.result = result;
	}

    public int work()
        throws Exception
    {
  	    Source source = createSAXSource(context);
	    OutputStream outputStream = null;
	    try {

		    if (result==null)
		    {
			    if (output instanceof File)
					try {
						outputStream = new FileOutputStream((File)output);
						result = new StreamResult(outputStream);
					} catch (FileNotFoundException e) {
						//  could not open file (write protected, in use...)
						setProgressText(e.getMessage());
						return FAILURE;
					}
			    else if (output instanceof OutputStream)
			        result = new StreamResult((OutputStream)output);
			    else if (output instanceof Writer)
			        result = new StreamResult((Writer)output);
		    }
		    else if (result instanceof StreamResult)
		        outputStream = ((StreamResult)result).getOutputStream();

			process(source, ExportConfig.getFile(context.config), result);

	    } finally {
			try {
				if (outputStream!=null) outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }

	    return SUCCESS;
    }

	public static void process(Source source, File xslFile, Result result) throws TransformerException
	{
		Transformer tf;

		if (xslFile==null) {
			tf = XMLUtil.getTransformer("empty",null);

			tf.transform(source,result);

			XMLUtil.releaseTransformer("empty",tf);
		}
		else {
			tf = XMLUtil.getTransformer(xslFile);

			tf.transform(source,result);

			XMLUtil.releaseTransformer(xslFile,tf);
		}
	}

}
