/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util.print;

import de.jose.util.AWTUtil;
import de.jose.view.style.JoStyleContext;
import de.jose.window.PrintPreviewDialog;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 */
public class PrintPreview
        extends JComponent
{
	public static final double FIT_PAGE_WIDTH   = -1.0;
	public static final double FIT_TEXT_WIDTH   = -2.0;
	public static final double FIT_PAGE         = -3.0;

	/** gutter between two pages    */
	public static final int GUTTER = 12;
	/** soze of border & drop shadow    */
	public static final int SHADOW = 8;

	/** scaling factor 1.0 = 100%
	 *  <= 0 = fit screen
	 *  */
	protected double scale = 1.0;
	/** current page no. (first = 1) */
	protected int current = 1;
	/** print two pages */
	protected boolean twoPage = false;

	protected JComponent container;

	/** document    */
	protected PrintableDocument doc;

	/** describes page in local coordinates */
	protected Rectangle page1, page2;
	/** describes printable area in local coordinates,
	 *  relative to page1 and page2   */
	protected Rectangle area;

	public PrintPreview()
	{
		setBackground(new Color(0xbb,0xbb,0xbb));
		setOpaque(true);

		page1 = new Rectangle();
		page2 = new Rectangle();
		area = new Rectangle();
	}

	public void setParent(JComponent comp)
	{
		container = comp;
	}

	public PrintableDocument getPrintableDocument()     { return doc; }

	public double getNormX()                            { return AWTUtil.getNormalizingTransform().getScaleX(); }
	public double getNormY()                            { return AWTUtil.getNormalizingTransform().getScaleY(); }

	public PageFormat getFormat()
	{
		if (doc==null)
			return null;
		else
			return doc.getPageFormat();
	}

	public double getPaperPixelWidth()
	{
		if (doc==null)
			return 480*getNormX();
		else
			return getFormat().getWidth() * getNormX();
	}

	public double getPaperPixelHeight()
	{
		if (doc==null)
			return 640*getNormY();
		else
			return getFormat().getHeight() * getNormY();
	}


	public void setDocument(PrintableDocument adoc)
	{
		doc = adoc;
	}

	public void disposeDocument()
	{
		doc.dispose();
		doc = null;
	}

	/**
	 *
	 * @return the number of pages, -1 if not (yet) known
	 */
	public int countPages() {
		if (doc==null)
			return -1;
		else
			return doc.getNumberOfPages();
	}

	public void setPageFormat(PageFormat format)
	{
		doc.setPageFormat(format);
		revalidate();
		repaint();
	}

	public void setOrientation(int orientation)
	{
		if (orientation!=getFormat().getOrientation())
		{
			getFormat().setOrientation(orientation);
			setPageFormat(getFormat());
		}
	}

	public int getOrientation()     { return getFormat().getOrientation(); }

	public boolean isLandscape()    { return (getOrientation()==PageFormat.LANDSCAPE) || (getOrientation()==PageFormat.REVERSE_LANDSCAPE); }
	public boolean isPortrait()     { return (getOrientation()==PageFormat.PORTRAIT); }

	public void setScale(double scale)
	{
		if (scale!=this.scale) {
			revalidate();
			repaint();
		}
		this.scale = scale;
	}

	public void setCurrentPage(int current)
	{
		if (current!=this.current) {
//			invalidate();
			repaint();
		}
		this.current = current;
	}

	public void setTwoPage(boolean twoPage)
	{
		if (twoPage!=this.twoPage) {
			revalidate();
			repaint();
		}
		this.twoPage = twoPage;
	}


	protected double getPreferredHeight()
	{
		if (doc==null)
			return 640;
		else if (twoPage && isLandscape())
			return 2*(getPaperPixelHeight()+SHADOW)+GUTTER;
		else
			return getPaperPixelHeight()+SHADOW;
	}

	protected double getPreferredWidth() {
		if (doc==null)
			return 480;
		else if (twoPage && isPortrait())
			return 2*(getPaperPixelWidth()+SHADOW)+GUTTER;
		else
			return getPaperPixelWidth()+SHADOW;
	}

	public double getEffectiveScale()
	{
		return getEffectiveScale(scale,container.getWidth(),container.getHeight());
	}

	public double getEffectiveScale(double scale)
	{
		return getEffectiveScale(scale,container.getWidth(),container.getHeight());
	}

	public double getEffectiveScale(double scale, int componentWidth, int componentHeight)
	{
		if (scale > 0.0)
			return scale;
		else if (scale==FIT_PAGE_WIDTH) {
			return componentWidth / getPreferredWidth();
		}
		else if (scale==FIT_TEXT_WIDTH) {
			double textPixelWidth = getFormat().getImageableWidth()*getNormX();
			return componentWidth / textPixelWidth;
		}
		else /*if (scale==FIT_PAGE)*/ {
			double scaley = componentHeight / getPreferredHeight();
			double scalex = componentWidth / getPreferredWidth();
			return Math.min(scalex,scaley);
		}
	}

	public Dimension getPreferredSize()
	{
		double scale = getEffectiveScale();
		return new Dimension(
				(int)Math.round(getPreferredWidth()*scale),
				(int)Math.round(getPreferredHeight()*scale));
	}

	public Rectangle getPaper(Dimension dim, boolean firstPage)
	{
		calcRectangles(dim.width,dim.height,getEffectiveScale());

		if (firstPage || !twoPage)
			return page1;
		else
			return page2;
	}

	public Rectangle getImageArea(Dimension dim, boolean firstPage)
	{
		calcRectangles(dim.width,dim.height,getEffectiveScale());

		Rectangle result = new Rectangle(area);
		if (firstPage || !twoPage)
			result.translate(page1.x,page1.y);
		else
			result.translate(page2.x,page2.y);
		return result;
	}

	protected void calcRectangles(int view_width, int view_height, double sc)
	{
		page1.width = (int)Math.round(getPaperPixelWidth()*sc);
		page1.height = (int)Math.round(getPaperPixelHeight()*sc);

		if (twoPage && isPortrait())
			page1.x = (view_width-GUTTER)/2-page1.width;
		else
			page1.x = (view_width-page1.width)/2;

		if (twoPage && isLandscape())
			page1.y = (view_height-GUTTER)/2-page1.height;
		else
			page1.y = (view_height-page1.height)/2;

		//  printable area (relative to page1, page2)
		area.x = (int)Math.round(getFormat().getImageableX()*getNormX()*sc);
		area.y = (int)Math.round(getFormat().getImageableY()*getNormY()*sc);
		area.width = (int)Math.round(getFormat().getImageableWidth()*getNormX()*sc);
		area.height = (int)Math.round(getFormat().getImageableHeight()*getNormX()*sc);

		if (twoPage) {
			if (isLandscape()) {
				page2.x = page1.x;
				page2.y = (view_height+GUTTER)/2;
			}
			else {
				page2.x = (view_width+GUTTER)/2;
				page2.y = page1.y;
			}

			page2.width = page1.width;
			page2.height = page1.height;
		}
	}

	public void paintComponent(Graphics g)
	{
		if (doc==null) return;

		//  fill background
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setColor(this.getBackground());
		g.fillRect(0,0,getWidth(),getHeight());
		//  calc rects
		double sc = getEffectiveScale();
		calcRectangles(getWidth(),getHeight(),sc);

		//  paint paper
		paintPaper(g,page1);
		//  paint content
		paintContent(g,page1,sc, current);		//  currently not available; repaint later

		if (twoPage) {
			paintPaper(g,page2);
			paintContent(g,page2,sc, current+1);
		}
	}

	protected void paintPaper(Graphics g, Rectangle r)
	{
		paintPaper(g,r,area,SHADOW);
	}

	public static void paintPaper(Graphics g, Rectangle r, Rectangle margin, int shadow)
	{
		//  drop shadow
		g.setColor(new Color(0x80,0x80,0x80,0x80));
		g.fillRect(r.x+shadow,r.y+r.height,r.width,shadow);
		g.fillRect(r.x+r.width,r.y+shadow,shadow,r.height-shadow);
		//  empty page
		g.setColor(Color.white);
		g.fillRect(r.x,r.y,r.width,r.height);
		//  border
		g.setColor(Color.black);
		g.drawRect(r.x,r.y,r.width,r.height);
		//  page margins
		g.setColor(Color.lightGray);
		g.drawRect(r.x+margin.x,r.y+margin.y, margin.width,margin.height);
	}

	protected int paintContent(Graphics g, Rectangle paper, double userScale, int pageno)
	{
		try {

			return doc.print(g, pageno-1, paper.x, paper.y, getNormX()*userScale, getNormY()*userScale);

		} catch (PrinterException e) {
			e.printStackTrace();
		}

		return PrintableDocument.NOT_AVAILABLE;
	}

}
