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

package de.jose.util;

import de.jose.Version;
import de.jose.Util;
import de.jose.util.file.FileUtil;
import de.jose.util.print.Triplet;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.awt.*;

/**
 *  @author Peter Schäfer
 */
public class TrueTypeInfo
{
	/** String encodings    */
	public static final int ENCODING_UNICODE_10         = 0;
	public static final int ENCODING_UNICODE_11         = 1;
	public static final int ENCODING_ISO                = 2;
	public static final int ENCODING_UNICODE_20_BMP     = 3;
	public static final int ENCODING_UNICODE_20         = 4;

	public static final int PLATFORM_UNICODE            = 0;
	public static final int PLATFORM_MACINTOSH          = 1;
	public static final int PLATFORM_ISO                = 2;
	public static final int PLATFORM_MICROSOFT          = 3;
	public static final int PLATFORM_CUSTOM             = 4;

	public static final int NAME_COPYRIGHT              = 0;
	public static final int NAME_FONT_FAMILY            = 1;
	public static final int NAME_FONT_SUB_FAMILY        = 2;
	public static final int NAME_IDENTIFIER             = 3;
	public static final int NAME_FULL_NAME              = 4;
	public static final int NAME_VERSION                = 5;
	public static final int NAME_POSTSCRIPT             = 6;

	public static final int NAME_PREFERRED_FAMILY       = 16;
	public static final int NAME_PREFERRED_SUBFAMILY    = 17;


	/** input stream    */
	protected RandomAccessFile in;

	/** file on disk (*.ttf)    */
	public File     file;

	/** font family */
	public String   family;
	public String   subFamily;
	public String   identifier;
	public String   fullName;

	public boolean  bold;
	public boolean  italic;

	public TrueTypeInfo(String ttfFile) throws IOException
	{
		this(new File(ttfFile));
	}

	public TrueTypeInfo(File ttfFile) throws IOException
	{
		file = ttfFile;
		in = new RandomAccessFile(file,"r");
		parse();
		in.close();
	}


	public String toString() {
		return Triplet.toString(family,bold,italic);
	}

	public boolean equals(String family, boolean bold, boolean italic)
	{
		return family.equalsIgnoreCase(this.family) && (bold==this.bold) && (italic==this.italic);
	}


	public boolean equals(String family, int style)
	{
		return equals(family, Util.allOf(style,Font.BOLD), Util.allOf(style,Font.ITALIC));
	}

	public boolean equals(Font font)
	{
		return equals(font.getFamily(), font.getStyle());
	}



	private void parse() throws IOException
	{
		/** Offset Table    */
		int sfntVersion      = in.readInt();
		short numTables     = in.readShort();
		short searchRange   = in.readShort();
		short entrySelector = in.readShort();
		short rangeShift    = in.readShort();

		/** Table Directory */
		int nameOffset = -1;
		int headOffset = -1;

		for (int tableIdx = 0; tableIdx < numTables; tableIdx++)
		{
			String tag      = intToString(in.readInt());
			int checkSum    = in.readInt();
			int offset      = in.readInt();
			int length      = in.readInt();

			if (tag.equals("name")) 	nameOffset = offset;
			if (tag.equals("head"))     headOffset = offset;

			if ((nameOffset>=0) && (headOffset>=0)) break;    //  no need to go further
		}

		if (nameOffset >= 0) parseNamingTable(nameOffset,-1);
		if (headOffset >= 0) parseHeadTable(headOffset,-1);
	}

	private void parseNamingTable(int tableOffset, int tableLength) throws IOException
	{
		/**  Name Table */
		in.seek(tableOffset);

		short format        = in.readShort();
		short count         = in.readShort();
		short stringOffset  = in.readShort();

		for (int nameRecIdx=0; nameRecIdx < count; nameRecIdx++)
		{
			/**  Name Record    */
			short platformID    = in.readShort();
			short encodingID    = in.readShort();
			short languageID    = in.readShort();
			short nameID        = in.readShort();
			short length        = in.readShort();
			short offset        = in.readShort();

			int off = tableOffset+stringOffset+offset;

			switch (nameID)
			{
			case NAME_FONT_FAMILY:          family      = readString(off,length, platformID); break;
			case NAME_FONT_SUB_FAMILY:      subFamily   = readString(off,length, platformID); break;
			case NAME_FULL_NAME:            fullName    = readString(off,length, platformID); break;
			case NAME_IDENTIFIER:           identifier  = readString(off,length, platformID); break;
			}
		}
	}

	private void parseHeadTable(int tableOffset, int tableLength) throws IOException
	{
		/** Header Table    */
		in.seek(tableOffset+44);

		short macStyle = in.readShort();

		bold = (macStyle & 0x0001) != 0;
		italic = (macStyle & 0x0002) != 0;
	}

	private String readString(int offset, int len, int platformID) throws IOException
	{
		long pos = in.getFilePointer();

		byte[] bytes = new byte[len];
		in.seek(offset);
		in.readFully(bytes);

		in.seek(pos);

		switch (platformID)
		{
		case PLATFORM_MACINTOSH:
				return new String(bytes,"UTF-8");
		default:
		case PLATFORM_UNICODE:
		case PLATFORM_MICROSOFT:
				return new String(bytes,"UTF-16");
		}
	}

	public static final String intToString(int x)
	{
		byte[] bytes = new byte[4];
		bytes[3] = (byte)(x & 0x000000ff);
		bytes[2] = (byte)((x>>8) & 0x000000ff);
		bytes[1] = (byte)((x>>16) & 0x000000ff);
		bytes[0] = (byte)((x>>24) & 0x000000ff);
		return new String(bytes);
	}


	private static final void process(File file) throws IOException
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			for (int i=0; i<files.length; i++) process(files[i]);
		}
		else if (file.getName().toLowerCase().endsWith(".ttf"))
		{
			System.out.println(file.getName());
			TrueTypeInfo ttf = new TrueTypeInfo(file);

			System.out.println("Family: "+ttf.family);
			System.out.println("Subfamily: "+ttf.subFamily);
//			System.out.println("Identifier: "+ttf.identifier);
//			System.out.println("Full Name: "+ttf.fullName);
			System.out.println();
		}
	}
/*

	public static void main(String[] args) throws IOException
	{
		for (int i=0; i<args.length; i++)
		{
			File file = new File(args[i]);
			process(file);
		}
	}
*/
/*
	public static void main(String[] args)
	{
		File dir = null;
		if (args.length > 0)
			dir = new File(args[0]);
		else if (Version.windows)
			dir = new File(WinUtils.getFontPath());

		System.out.println(dir.getAbsolutePath());
		System.out.println();

		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++)
			if (files[i].isFile() && FileUtil.hasExtension(files[i].getName(),"ttf"))
			try {
				TrueTypeInfo tti = new TrueTypeInfo(files[i]);
				System.out.print(tti.file.getName());
				if (tti.bold) System.out.print(" BOLD ");
				if (tti.italic) System.out.print(" ITALIC ");
				System.out.print(" ");
                System.out.println(tti.family);
//				System.out.print(" ");
//              System.out.println(tti.fullName);
			} catch (Exception e) {
				System.out.println("ERROR reading "+files[i].getName());
			}
	}
*/
}
