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

import de.jose.Application;
import de.jose.util.AWTUtil;

import java.awt.print.*;
import java.awt.*;

/**
 * a Printable with a PageFormat
 *
 * @author Peter Schäfer
 */

abstract public class PrintableDocument
        implements Printable, Pageable
{
	/**
	 * return value from print(). page is currently not available.
	 */
	public static final int NOT_AVAILABLE   = 2;

	/**	the page format	*/
	protected PageFormat format;

	public PrintableDocument (PageFormat format)
	{
		this.format = format;
	}

	public PageFormat getPageFormat()                           { return format; }

	//  implements Pageable
	public PageFormat getPageFormat(int pageNumber)             { return format; }

	public void setPageFormat(PageFormat format)                { this.format = format; }

	//  implements Printable
	public int print(Graphics g, PageFormat format, int pageNumber) throws PrinterException
	{
		if (format != this.format) setPageFormat(format);

		return print(g,pageNumber, 0,0, 1.0,1.0);
	}

	abstract public int print(Graphics g, int pageNumber, int screenX, int screenY, double scaleX, double scaleY) throws PrinterException;

	//  implements Pageable
	/**
	 * @return the number of pages, -1 if not (yet) known
	 */
	abstract public int getNumberOfPages();

	abstract public void render();

	abstract public void dispose();

	//  implements Pageable
	public Printable getPrintable(int pageIndex)        { return this; }

	public boolean print()
	{
		PrinterJob job = PrinterJob.getPrinterJob();
		if (format==null)
			format = job.defaultPage();
		else
			format = job.validatePage(format);

		//  need no font scaling for printing; remember to reset factor after printing !
		job.setPageable(this);

		if (!job.printDialog())
			return false;

		try {
			job.print();
			return true;
		} catch (PrinterException e) {
			Application.error(e);
			return false;
		}
	}

	public static PageFormat validPageFormat(PageFormat format)
	{
		PrinterJob job = PrinterJob.getPrinterJob();
		format = validPageFormat(job,format);
		job.cancel();
		return format;
	}

	public static PageFormat validPageFormat(PrinterJob job, PageFormat format)
	{
		if (format==null)
			return job.defaultPage();
		else
			return job.validatePage(format);
	}

	public static PageFormat showPageSetupDialog(PageFormat format)
	{
		PrinterJob job = PrinterJob.getPrinterJob();
		format = validPageFormat(job,format);
		format = job.pageDialog(format);
		return format;
	}
}