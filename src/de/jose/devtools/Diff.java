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

package de.jose.devtools;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Peter Schäfer
 */

public class Diff
{
	public static final int CHUNK_SIZE = 4096;

	private static ZipOutputStream zout = null;
	private static File zbase = null;
	private static byte[] buffer = new byte[CHUNK_SIZE];

	public static void error(String message)
	{
		if (message != null)
			System.out.println("\n"+message+"\n\n");
		System.out.println("java -cp jose.jar Diff <source file> <dest file> [-zip target]");
		System.exit(-1);
	}

	public static boolean compareFiles(File a, File b)
		throws IOException
	{
		long lena = a.length();
		long lenb = b.length();

		if (lena!=lenb) return false;

		//	compare contents
		FileInputStream ina = null;
		FileInputStream inb = null;
		MappedByteBuffer mapa;
		MappedByteBuffer mapb;

		try {
			ina = new FileInputStream(a);
			inb = new FileInputStream(b);

			for (long pos = 0; pos < lena; pos += CHUNK_SIZE)
			{
				long chunk = CHUNK_SIZE;
				if ((pos+chunk) > lena) chunk = lena-pos;

				mapa = ina.getChannel().map(FileChannel.MapMode.READ_ONLY, pos,chunk);
				mapb = inb.getChannel().map(FileChannel.MapMode.READ_ONLY, pos,chunk);

				if (mapa.compareTo(mapb) != 0)
					return false;
			}

		} finally {
			ina.close();
			inb.close();
		}

		return true;
	}

	public static void compareDirectories(File a, File b, File zipFile)
		throws IOException
	{
		if (zipFile != null) {
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			zout.setLevel(9);
			zbase = b;
		}

		compareDirectory(a,b);

		if (zout!=null) {
			zout.close();
			System.out.println(zipFile.getName()+" created.");
		}
	}


	private static void compareDirectory(File a, File b)
		throws IOException
	{
		String[] lista = a.list();
		String[] listb = b.list();

		HashSet seta = new HashSet();
		HashSet setb = new HashSet();

		for (int i=0; i<lista.length; i++) seta.add(lista[i]);
		for (int i=0; i<listb.length; i++) setb.add(listb[i]);

		for (int i=0; i<listb.length; i++)
			if (!seta.contains(listb[i])) {
				System.out.println(b+File.separator+listb[i]+" is new");
				addZip(new File(b,listb[i]));
			}

		for (int i=0; i<lista.length; i++)
		{
			if (!setb.contains(lista[i]))
				System.out.println(a+File.separator+lista[i]+" deleted");
			else {
				//	compare
				File a1 = new File(a,lista[i]);
				File b1 = new File(b,lista[i]);

				boolean isDira = a1.isDirectory();
				boolean isDirb = b1.isDirectory();

				if (isDira!=isDirb)
					System.out.println("can't compare "+a1+" to "+b1);
				else if (isDira)
					compareDirectory(a1,b1);
				else if (!compareFiles(a1,b1)) {
					System.out.println(b1+" differs");
					addZip(b1);
				}
			}
		}
	}

	private static void addZip(File f)
		throws IOException
	{
		if (zout==null) return;

		if (f.isDirectory()) {
			ZipEntry e = new ZipEntry(getRelativeName(zbase,f,true)+"/");
			e.setTime(f.lastModified());
			zout.putNextEntry(e);
			zout.closeEntry();

			File[] files = f.listFiles();
			for (int i=0; i<files.length; i++)
				addZip(files[i]);
		}
		else {
			long len = f.length();
			ZipEntry e = new ZipEntry(getRelativeName(zbase,f,true));
			e.setSize(len);
			e.setTime(f.lastModified());
			zout.putNextEntry(e);

			FileInputStream fin = new FileInputStream(f);

			for (long pos = 0; pos < len; pos += CHUNK_SIZE)
			{
				int chunk =fin.read(buffer,0,CHUNK_SIZE);
				zout.write(buffer,0,chunk);
			}

			zout.closeEntry();
		}
	}

	private static String getRelativeName(File base, File target, boolean normalise)
	{
		StringBuffer buf = new StringBuffer();
		while (!target.equals(base)) {
			if (buf.length() > 0) {
				if (normalise)
					buf.insert(0,"/");
				else
					buf.insert(0,File.separator);
			}
			buf.insert(0,target.getName());
			target = target.getParentFile();
		}
		return buf.toString();
	}


	public static void main(String[] args)
	{
		File source = null;
		File dest = null;
		File zip = null;

		for (int i=0; i<args.length; i++)
			if (args[i].equalsIgnoreCase("-zip") && (i+1) < args.length)
				zip = new File(args[++i]);
			else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("-help"))
				error(null);
			else if (source==null)
				source = new File(args[i]);
			else if (dest==null)
				dest = new File(args[i]);
			else
				error("unexpected argument "+args[i]);

		if (source==null)
			error("source file expected");
		if (dest==null)
			error("destination file expected");

		if (zip!=null && zip.exists() && !zip.isFile())
			error("invalid zip file");

		boolean sourceDir = source.isDirectory();
		boolean destDir = dest.isDirectory();

		if (sourceDir!=destDir)
			error("can't compare file "+source+" to directory "+dest);

		try {

			if (sourceDir)
				compareDirectories(source,dest,zip);
			else {
				if (compareFiles(source,dest))
					System.out.println("files are identical");
				else
					System.out.println("files are different");
			}

		} catch (IOException e) {
			error(e.getMessage());
		}
	}
}
