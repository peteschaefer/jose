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

package de.jose.book;

import de.jose.Util;
import de.jose.book.lichess.LiChessOpeningExplorer;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.util.Enumeration;

import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.StringUtil;
import de.jose.Application;
import de.jose.Config;
import de.jose.Language;
import org.w3c.dom.NodeList;

/**
 * TODO select moves based on "style" (IF the implementation store some style info !?)
 */

public class BookFile /*implements Selectable*/
{
	/** XML config element  */
	public Element config;
	/** disk file, may not be available */
	public File file;
	/** the opening book    */
	public OpeningBook book;

	public BookFile(Element config)
	{
		this.config = config;
		this.file = getFile();
		this.book = null;   //  call open()
	}

	public BookFile(File file, Config config)
	{
		this.config = getBook(config, file.getName());
		if (this.config==null)
			this.config = config.getDocument("books.xml",false).createElement("BOOK"); //  make an empty, unused config element
		this.file = file;
		this.book = null;   //  call open()
	}

	private Element getBook(Config config, String fileName)
	{
		Enumeration books = config.enumerateElements("BOOK");
		while (books.hasMoreElements())
		{
			Element elem = (Element) books.nextElement();
			String configName = XMLUtil.getChildValue(elem,"FILE");
			if (configName.equals(fileName))
				return elem;
		}
		return null;
	}

	public BookFile shallowClone()
	{
		BookFile that = new BookFile(this.config);
		that.book = this.book; //  not cloned !
		that.file = this.file;
		return that;
	}

	public String format()
	{
		return (String)Util.nvl(
				XMLUtil.getChildValue(config,"FORMAT"),
				"Polyglot" );
	}


	public boolean open()
	{
		if (book!=null) return true;

	//	usually format=="Polyglot"
		if (format().equalsIgnoreCase("LiChess"))
		{
			//	LiChess online query
			book = new LiChessOpeningExplorer(config);
		}
		else
		{
			//	disk file
			if (!file.exists()) return false;
			try {
				book = OpeningBook.open(file);
			} catch (IOException e) {
				book = null;
			}
		}
		return book!=null;
	}

	public void open(boolean open)
	{
		if (open)
			open();
		else
			close();
	}

	public void close()
	{
		if (book != null) try {
			book.close();
		} catch (IOException e) {
		}
		book = null;
	}


	private File getFile()
	{
		String name = XMLUtil.getChildValue(config,"FILE");
		if (name==null)
			return null;
		else {
			File dir = new File(Application.theWorkingDirectory,"books");
			return new File(dir,name);
		}
	}


	public String getInfoText()
	{
		if (format().equalsIgnoreCase("LiChess"))
			return LiChessOpeningExplorer.getInfoText(config, this.isEnabled());

		StringBuffer buf = new StringBuffer();

		boolean enabled = this.isEnabled();

		if (!enabled) buf.append("<font color=#aaaaaa>");

		buf.append("<b>");
		buf.append(file.getName());
		buf.append("</b>");

		if (!enabled) buf.append("</font>");

		if (file.exists()) {
		String author = XMLUtil.getChildValue(config,"AUTHOR");
		if (author!=null) {
				String text = Language.get("book.author");
				text = StringUtil.replace(text,"%author%",author);

				buf.append(" - <font size=2>");
				buf.append(text);
				buf.append("</font>");
		}

		String comment = XMLUtil.getChildValue(config,"COMMENT");
		if (comment!=null) {
				buf.append("<br><font size=2>");
			buf.append(comment);
			buf.append("</font>");
		}
		}
		else {
			String url = XMLUtil.getChildValue(config,"URL");
			if (url != null) {
				//  TODO
				buf.append("&nbsp;<a href='verbatim:");
				buf.append(url);
				buf.append("'>");
				buf.append(Language.get("book.download"));
				buf.append("</a>");
			}
		}

		return buf.toString();
	}

	public boolean isOpen()
	{
		return book!=null;
	}

	public boolean isEnabled()
	{
		if (format().equalsIgnoreCase("LiChess")) {
			return true;    // Network is available?
		}
		else {
			return file != null && file.exists();
		}
	}

}
