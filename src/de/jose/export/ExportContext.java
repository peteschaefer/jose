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

package de.jose.export;

import de.jose.task.GameSource;
import de.jose.profile.UserProfile;
import de.jose.util.print.PrintableDocument;
import de.jose.util.print.AWTPrintableDocument;
import de.jose.util.print.HTMLPrintableDocument;
import de.jose.util.print.FOPrintableDocument;
import de.jose.Application;
import de.jose.Version;
import de.jose.view.style.JoStyleContext;

import java.io.File;
import java.awt.print.PageFormat;

import org.w3c.dom.Element;

/**
 * collects all necessary information for printing/exporting a document
 *
 * @author Peter Schäfer
 */

public class ExportContext implements Cloneable
{
	/** input source (set of Games)    */
	public GameSource           source;
	/** export config (describes output format, XSL transform, etc.)   */
	public ExportConfig         theConfig;
	public Element              config;

	/** user settings   */
	public UserProfile          profile;

	/** styles  */
	public JoStyleContext       styles;

	/** target file, or output stream */
	public Object                 target;
	/** directory for collateral files (images,CSS) */
	public File                 collateral;

	/** preview document (optional) */
	public PrintableDocument    preview;


	public ExportContext()
	{
		this.profile = Application.theUserProfile;
		this.theConfig = Application.theApplication.getExportConfig();
	}


	public int getOutput()      { return ExportConfig.getOutput(config); }

	public Object clone()
	{
		return clone(false);
	}

	public ExportContext clone(boolean stylesDeep)
	{
		ExportContext that = new ExportContext();
		that.source = this.source;
		that.theConfig = this.theConfig;
		that.config = this.config;
		that.target = this.target;
		that.profile = this.profile;
		if (stylesDeep)
			that.styles = (JoStyleContext)this.styles.clone();
		else
			that.styles = this.styles;
		that.preview = this.preview;
		return that;
	}


	public PrintableDocument createPrintableDocument() throws Exception
	{
		switch (getOutput()) {
		case ExportConfig.OUTPUT_AWT:
            PageFormat pf = profile.getPageFormat(true);
			AWTPrintableDocument prdoc = AWTPrintableDocument.newDocument(source, pf, styles);
			prdoc.setPageFormat(pf); //  = explicitly calculate page breaks
			return prdoc;

		case ExportConfig.OUTPUT_HTML:
		case ExportConfig.OUTPUT_TEXT:
			return new HTMLPrintableDocument(this);

		case ExportConfig.OUTPUT_XSL_FO:
			//  create PrintableDocuments from XSL-FO
			Version.loadFop();
			return new FOPrintableDocument(this);

		default:
			//  other output formats not supported
			throw new IllegalArgumentException();
		}
	}

}
