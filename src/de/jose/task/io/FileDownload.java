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

package de.jose.task.io;

import de.jose.Language;
import de.jose.task.Task;
import de.jose.util.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Task for downloading a file from a Web Server
 *
 *
 * @author Peter Sch?fer 
 */
public class FileDownload
        extends Task
{
	protected URL source;
	protected File target;
	protected long expectedSize;
	protected long totalSize;

	protected InputStream input;
	protected FileOutputStream output;
	protected byte[] buffer;

	public FileDownload(URL source, File target, long size)
	{
		super("download.file.title");
		this.source = source;
		this.target = target;
		this.expectedSize = size;
		this.reportErrors = false;	//	errors are shown but not reported to the application

		setSilentTime(1000);
		String text = Language.get("download.file.progress");
		text = StringUtil.replace(text,"%file%",target.getName());
		setProgressText(text);
	}

	/*	@return the approximate progress state (0.0 = just started, 1.0 = finished),
			< 0 if unknown
			should be thread-safe
	*/
	public double getProgress ()
	{
		if (expectedSize <= 0)
			return PROGRESS_UNKNOWN;
		else
			return (double)totalSize/expectedSize;
	}

	public int init () throws Exception
	{
		URLConnection conn = source.openConnection();
		if (expectedSize <= 0)	//  retrieve size from connection ?
			expectedSize = source.openConnection().getContentLength();

		input = conn.getInputStream();
		output = new FileOutputStream(target);
		buffer = new byte[4096];
		totalSize = 0L;

		return RUNNING;
	}

	public int work () throws Exception
	{
		//  download a single chunk
		int bytesRead = input.read(buffer);

		if (bytesRead==0)   //  blocked ?
			return RUNNING;

		if (bytesRead < 0) { //  finished
			if ((expectedSize >= 0) && (totalSize!=expectedSize))
				return FAILURE;
			else
				return SUCCESS;
		}

		output.write(buffer,0,bytesRead);
		totalSize += bytesRead;
		return RUNNING;
	}

	public int done (int state)
	{
		try {
			if (input!=null) input.close();
		} catch (IOException ioex) { }
		try {
			if (output!=null) output.close();
		} catch (IOException ioex) { }

		switch (state) {
		case FAILURE:
		case ABORTED:
		case ERROR:
			target.delete();
			break;
		}
		
		return super.done(state);
	}

}
