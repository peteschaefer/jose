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

import de.jose.task.io.XMLExport;
import de.jose.export.ExportContext;
import de.jose.export.HtmlUtil;
import de.jose.Application;
import de.jose.comm.Command;
import de.jose.view.style.JoStyleSheet;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.StringWriter;
import java.io.File;
import java.io.StringReader;
import java.io.IOException;
import java.net.URL;

/**
 * HTMLPrintableDocument
 *
 * preview for HTML document.
 * Please note that rendering HTML in Swing produces poor results.
 * It is MUCH better to use an external browser for previewing.
 *
 * @author Peter Sch�fer
 */

public class HTMLPrintableDocument
        extends AWTPrintableDocument
{
	protected ExportContext context;
	protected XMLExport xmlpreview;

	public HTMLPrintableDocument(ExportContext context) throws Exception
	{
		super((DefaultStyledDocument)null, context.profile.getPageFormat(true));

		textPane.setEditorKit(new HTMLEditorKit());

		this.context = context;
		columnCount = 1;

		newDocument();
	}

	public void render()
	{
		dispose();
		//  got to set up a new renderer ;-(  (or do we ?)
		newDocument();
	}

	public int print(Graphics g, int pageNumber, int screenX, int screenY, double scaleX, double scaleY)
	{
		if (doc!=null)
			return super.print(g,pageNumber,screenX,screenY,scaleX,scaleY);
		else {
			//  wait for new doc to become available
			return NOT_AVAILABLE;
		}
	}


	protected HTMLDocument createDefaultDocument(HTMLEditorKit kit) {
		StyleSheet styles = kit.getStyleSheet();
		JoStyleSheet ss = new JoStyleSheet();

		ss.addStyleSheet(styles);
		ss.setFontScale(1.0f);  //  or what ?

		HTMLDocument doc = new HTMLDocument(ss);
		doc.setParser(new ParserDelegator());
		doc.setAsynchronousLoadPriority(4);
		doc.setTokenThreshold(100);
		return doc;
	}


/*

	protected void findPageBreaks()
	{
		if (format==null || doc==null || !textPane.isVisible()) {
			pageBreaks = null;
			return;
		}

		int pageHeight = (int)format.getImageableHeight();
		IntArray collect = new IntArray();
//		collect.add(0);
		collect.add(pageHeight);
		collect.add(2*pageHeight);

		pageBreaks = collect.toArray();
	}
*/

	protected void setNewDoc(StringWriter htmlSource, URL collateralURL) throws IOException, BadLocationException
	{
		StringReader htmlInput = new StringReader(htmlSource.toString());

		HTMLEditorKit kit = (HTMLEditorKit)textPane.getEditorKit();
		HTMLDocument doc = createDefaultDocument(kit);
		doc.putProperty("IgnoreCharsetDirective",Boolean.TRUE);
		doc.setBase(collateralURL);
		kit.read(htmlInput,doc,0);

		this.doc = doc;
		this.format = context.profile.getPageFormat(true);

		view.setSize((float)format.getImageableWidth(), Integer.MAX_VALUE);
		textPane.setBounds(0,0,(int)format.getImageableWidth(), Integer.MAX_VALUE);

		textPane.setDocument(doc);

		findPageBreaks();
	}

	public void newDocument()
	{
		synchronized (this) {
			if (xmlpreview!=null)  return;     //  already running, fine
			if (doc!=null && doc.getLength() > 0) return;     //  already finished, even better

			try {

				//  else: start creating a new Renderer; don't wait for the result
				//  create collateral files (CSS, images) in temp dir / or used configed dir
				//  not really used; but needed for determining temp dir
				context.target = File.createTempFile("jose","html");
				File collateralDir = HtmlUtil.createCollateral(context);
				final URL collateralURL = new URL("file",null,collateralDir.getAbsolutePath()+"/dummy.html");

				//  create HTML document from source
				final StringWriter htmlSource = new StringWriter();

				Runnable onComplete = new Runnable() {
					public void run() {
						try {
							setNewDoc(htmlSource,collateralURL);
							xmlpreview = null;
						} catch (Exception e) {
							Application.error(e);
						}
					}
				};

				Command command = new Command("doc.preview.refresh",null, HTMLPrintableDocument.this);

				//  start XMLExport task, dont  wait for result; will eventually call back
				xmlpreview = new XMLExport(context);
				xmlpreview.setTransformResult(new StreamResult(htmlSource));
				xmlpreview.setOnSuccess(onComplete);
				xmlpreview.setOnFailure(onComplete);
				xmlpreview.setOnSuccess(command);
				xmlpreview.start();    //  don't wait, will eventually call back

			} catch (Exception e) {
				Application.error(e);
			}
		}
	}

}