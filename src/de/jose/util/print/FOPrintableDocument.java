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

import org.apache.fop.render.awt.AWTRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import de.jose.export.ExportContext;
import de.jose.task.io.XSLFOExport;
import de.jose.Application;
import de.jose.Language;
import de.jose.Command;

/**
 * FOPrintableDocument
 * 
 * @author Peter Schäfer
 */

public class FOPrintableDocument
        extends PrintableDocument
{
	protected ExportContext context;
	protected AWTRenderer renderer;
	protected XSLFOExport.Preview fopreview;

	protected PageFormat oldFormat;
	protected Rectangle2D.Double clipRect = new Rectangle2D.Double();

	public FOPrintableDocument(ExportContext context)
	{
		super(context.profile.getPageFormat(true));
		oldFormat = (PageFormat)this.format.clone();

		this.context = context;
		newRenderer();
/*
		gd = new DelegateGraphics2D() {
			public void setFont(Font font)
			{
				font = FontUtil.newFont(font.getFamily(),font.getStyle(),font.getSize2D());
				g2.setFont(font);
			}
		};
*/
	}

	public void render()
	{
		dispose();
		//  got to set up a new renderer ;-(  (or do we ?)
		newRenderer();
	}

	public void setPageFormat(PageFormat format)
	{
		super.setPageFormat(format);
		if (! SerializablePageFormat.equals(format,oldFormat)) render();
		oldFormat = (PageFormat)format.clone();
	}

	public int print(Graphics g, int pageNumber, int screenX, int screenY, double scaleX, double scaleY) throws PrinterException
	{

		Graphics2D g2 = (Graphics2D)g;
		AffineTransform oldtf = g2.getTransform();
		Shape oldclip = g2.getClip();

		clipRect.setFrame(format.getImageableX(), format.getImageableY(),
		        format.getImageableWidth(), format.getImageableHeight());

		try {
			//  transform to screen coordinates
			g2.translate(screenX, screenY);
			g2.scale(scaleX, scaleY);
//			renderer.setScaleFactor(scaleX*100.0);   //  in Percent !!

//			g2.clip(clipRect);
			//  clipRect and paintRect are still in Print coordinates, right ?
			if (renderer==null) {
				//  wait for renderer to become available
				Font font = g.getFont().deriveFont(24.0f);
				g.setFont(font);
				g.drawString(Language.get("preview.wait"), (int)(clipRect.x+4), (int)(clipRect.height/2+12));
				return NOT_AVAILABLE;
			}
			else {
				return renderer.print(g2, getPageFormat(), pageNumber);
			}

		} finally {
			g2.setTransform(oldtf);
			g2.setClip(oldclip);
		}

		/** or ?
		 */
/*
		renderer.render(pageNumber);

		BufferedImage bimg = renderer.getLastRenderedPage();
		g.drawImage(bimg,
		        screenX,screenY, screenX+bimg.getWidth(), screenX+bimg.getHeight(),
		        0,0, bimg.getWidth(), bimg.getHeight(), null);
*/
	}

	//  implements Pageable
	public int getNumberOfPages()
	{
		if (renderer==null)
			return-1;
		else
			return renderer.getNumberOfPages();
	}

	public void dispose()
	{
		if (renderer!=null) FOPUtil.release(renderer);
		renderer = null;
	}


	public void newRenderer()
	{
		synchronized (this) {
			if (renderer!=null) return;     //  already finished, even better
			if (fopreview!=null) return;         //  already creating

			Runnable result = () -> {
                FOPrintableDocument.this.renderer = fopreview.renderer;
                FOPrintableDocument.this.fopreview = null;
            };

			Command command = new Command("doc.preview.refresh",null,FOPrintableDocument.this);

			//  else: start creating a new Renderer; don't wait for the result
			//  XSLFOPreview will eventually call back
			try {
				fopreview = new XSLFOExport.Preview(context,result,command);
				fopreview.start();
			} catch (Exception ex) {
				Application.error(ex);
			}
		}
	}

}