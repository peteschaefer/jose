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

package de.jose.db.io;

import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.tar.TarOutputStream;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarEntry;

import java.io.*;
import java.util.zip.*;

import de.jose.util.file.FileUtil;
import de.jose.util.StringUtil;
import de.jose.Application;
import de.jose.Version;

/**
 * @author Peter Schäfer
 */
public class ArchiveFile
{
    public static final int FILE_VERSION            = 1000;

	// MySQL table format
	public static final int TABLE_MYISAM            = 11;
	/** @deprecated needs myisampack.exe, yields no significant advantage */
	public static final int TABLE_MYISAM_PACKED     = 12;
	/** @deprecated needs embedded library with Archive engine enabled; yields no significant advantage */
	public static final int TABLE_ARCHIVE           = 13;

	//  Pack algorithm
	public static final int PACK_TAR                = 21;
	/** @deprecated doesn't work yet; yields no significant advantage over tar */
	public static final int PACK_ZIP_STORE          = 22;
	public static final int PACK_ZIP                = 23;

	//  File compression algorithm
	public static final int COMPRESS_NONE           = 31;
	public static final int COMPRESS_GZIP           = 32;
	/** @deprecated compressing is far too slow; yiels some advantage over gzip */
	public static final int COMPRESS_BZIP2          = 33;
	/** @deprecated needs unrar.exe; yields small advantages over gzip */
	public static final int COMPRESS_RAR                = 34;


	public static final int TABLE_DEFAULT      = TABLE_MYISAM;
	public static final int PACK_DEFAULT         = PACK_TAR;
	public static final int COMPRESS_DEFAULT = COMPRESS_GZIP;


	// -------- Fields ------------------------

    protected File file;
	protected File tableDir;

	protected int tableFormat = TABLE_DEFAULT;
	protected int packMethod = PACK_DEFAULT;
	protected int compressMethod = COMPRESS_DEFAULT;


    // -------- Output Streams ------------------------
	/** file output stream  */
    protected FileOutputStream file_out;
	/** buffer output stream (may be null) */
	protected BufferedOutputStream buf_out;
	/** compression output stream (or equal to pout) */
    protected OutputStream compress_out;
	protected GZIPOutputStream gzip_out;
	protected CBZip2OutputStream bzip_out;
	/** pack output stream (zip or tar) */
    protected OutputStream pack_out;
	protected ZipOutputStream zip_out;
	protected TarOutputStream tar_out;

    // -------- Input Streams ------------------------
	/** file input stream (or URL input stream!)   */
    protected InputStream file_in;
	protected BufferedInputStream buf_in;
	protected InputStream hd1_in;
	/** uncompression input stream (or equal to pin) */
    protected InputStream uncompress_in;
	protected GZIPInputStream gzip_in;
	protected CBZip2InputStream bzip_in;
	protected InputStream hd2_in;
	/** pack input stream (zip or tar) */
    protected InputStream unpack_in;
	protected ZipInputStream zip_in;
	protected TarInputStream tar_in;


    public ArchiveFile(File file)
    {
        this.file = file;
    }


	public int getTableFormat()                        { return tableFormat; }
	public int getPackMethod()                         { return packMethod; }
	public int getCompressMethod()                 { return compressMethod; }

	public File getFile()                                    { return file; }
	public File getTableDir()                           	{ return tableDir; }

	public long getFileSize()
	{
		if (file!=null)
			return file.length();
		else
			return 0;
	}

    public void create(int tableFormat, int packMethod, int compressMethod,
                       File tableDirectory)
            throws IOException
    {
        this.tableFormat = tableFormat;
	    this.packMethod = packMethod;
	    this.compressMethod = compressMethod;

        //  open for output
        file_out = new FileOutputStream(file);

	    switch (compressMethod)
	    {
	    case COMPRESS_NONE:
		    compress_out = buf_out = new BufferedOutputStream(file_out,4096); //  right ?
		    break;
	    default:
	    case COMPRESS_GZIP:
		    compress_out = new GZIPOutputStream(file_out,4096);
		    break;
	    case COMPRESS_BZIP2:
		    compress_out = new CBZip2OutputStream(file_out,4096);
		    break;
	    }

	    switch (packMethod)
	    {
	    case PACK_ZIP_STORE:
		    pack_out = zip_out = new ZipOutputStream(compress_out);
		    zip_out.setMethod(ZipOutputStream.STORED);
		    zip_out.setComment("jose Database Archive");
		    break;
		case PACK_ZIP:
			pack_out = zip_out = new ZipOutputStream(compress_out);
			zip_out.setMethod(ZipOutputStream.DEFLATED);
			zip_out.setLevel(8);
			zip_out.setComment("jose Database Archive");
			break;
	    default:
	    case PACK_TAR:
		    //  note that TarOutputStream is already buffered so we don't need buf_out !!
		    pack_out = tar_out = new TarOutputStream(compress_out);
		    break;
	    }
    }

    public void open(InputStream in)
        throws IOException
    {
	    if (in==null && file!=null)
	    {
		    file_in = new FileInputStream(file);
		    compressMethod = detectCompressMethod(file_in,false);
		    file_in.close();

		    if (compressMethod==COMPRESS_RAR) return;

		    hd1_in = file_in = new FileInputStream(file);
	    }
	    else
	    {
        file_in = in;
	    if (file_in.markSupported())
	        hd1_in = file_in;
	    else
			hd1_in = new PushbackInputStream(file_in,8);

			compressMethod = detectCompressMethod(hd1_in,true);
	    }

	    switch (compressMethod)
	    {
	    case COMPRESS_RAR:
	        return; //  can't open stream on RAR files; but can use extract()
	    case COMPRESS_NONE:
		    uncompress_in = buf_in = new BufferedInputStream(hd1_in,4096);
		    break;
	    default:
	    case COMPRESS_GZIP:
		    uncompress_in = new GZIPInputStream(hd1_in,4096);
		    break;
	    case COMPRESS_BZIP2:
		    uncompress_in = new CBZip2InputStream(hd1_in);
		    break;
	    }

	    if (uncompress_in.markSupported())
	        hd2_in = uncompress_in;
	    else
	        hd2_in = new PushbackInputStream(uncompress_in,4);

	    packMethod = detectPackMethod(hd2_in,true);
	    switch (packMethod)
	    {
	    case PACK_ZIP_STORE:
	    case PACK_ZIP:
		    unpack_in = zip_in = new ZipInputStream(hd2_in);
		    break;
	    default:
	    case PACK_TAR:
		    unpack_in = tar_in = new TarInputStream(hd2_in);
		    break;
	    }
    }

	private int detectCompressMethod(InputStream in, boolean pushback)
			throws IOException
	{
		byte[] bytes = new byte[3];

		if (!pushback)
			in.read(bytes);
		else if (in.markSupported())
		{
			in.mark(3);
			in.read(bytes);
			in.reset();
		}
		else if (in instanceof PushbackInputStream)
		{
			PushbackInputStream pbin = (PushbackInputStream)in;
			pbin.read(bytes);
			pbin.unread(bytes);
		}
		else
			throw new IllegalArgumentException();

		//  GZIP starts with 0x1f 0x8b
		//  BZIP2 starts with "h1" ... "h9"
		if ((bytes[0]==(byte)0x1f) && (bytes[1]==(byte)0x8b))
			return COMPRESS_GZIP;
		else if (bytes[0]=='h' && (bytes[1]>='1' && bytes[1]<='9'))
			return COMPRESS_BZIP2;
		else if (bytes[0]=='R' && bytes[1]=='a' && bytes[2]=='r')
			return COMPRESS_RAR;
		else
			return COMPRESS_NONE;
	}

	private int detectPackMethod(InputStream in, boolean pushback)
			throws IOException
	{
		byte[] bytes = new byte[4];

		if (!pushback)
			in.read(bytes);
		else if (in.markSupported())
		{
			in.mark(4);
			in.read(bytes);
			in.reset();
		}
		else if (in instanceof PushbackInputStream)
		{
			PushbackInputStream pbin = (PushbackInputStream)in;
			pbin.read(bytes);
			pbin.unread(bytes);
		}
		else
			throw new IllegalArgumentException();

		//  ZIP files start with ""PK\003\004""
		//  TAR file starts with file name
		if (bytes[0]=='P' && bytes[1]=='K' && bytes[2]==3 && bytes[3]==4)
			return PACK_ZIP;
		else
			return PACK_TAR;
	}

	public void flush() throws IOException
	{
		if (pack_out!=null) pack_out.flush();
		if (compress_out!=null) compress_out.flush();
		if (file_out!=null) file_out.flush();
	}

    public void close()
    {
	    close(pack_out);
		close(compress_out);
        close(file_out);

	    close(buf_out);
	    close(zip_out);
	    close(tar_out);
	    close(gzip_out);
	    close(bzip_out);

	    compress_out = null;
	    pack_out = null;
	    buf_out = null;
	    file_out = null;
	    zip_out = null;
	    tar_out = null;
	    gzip_out = null;
	    bzip_out = null;

	    close(uncompress_in);
	    close(unpack_in);
	    close(buf_in);
	    close(hd1_in);
	    close(hd2_in);
	    close(file_in);

	    close(zip_in);
	    close(gzip_in);
	    close(bzip_in);
	    close(tar_in);

	    uncompress_in = null;
	    unpack_in = null;
	    buf_in = null;
	    hd1_in = null;
	    hd2_in = null;
	    file_in = null;
	    zip_in = null;
	    gzip_in = null;
	    bzip_in = null;
	    tar_in = null;
    }

	private void close(OutputStream out)
	{
		if (out!=null)
			try {
				out.close();
			} catch (IOException e) {
				//  ignore
			}
	}

	private void close(InputStream in)
	{
		if (in!=null)
			try {
				in.close();
			} catch (IOException e) {

			}
	}



    public boolean delete()
    {
        close();
        return file.delete();
    }

    public void storeFiles(File tableDir, String[] tableNames)
            throws IOException
    {
		File[] dataFiles = tableDir.listFiles(new PrefixFilter(tableNames));
	    for (int i=0; i < dataFiles.length; i++)
	        storeFile(dataFiles[i]);
    }

    public void storeFile(File dataFile)
            throws IOException
    {
	    System.err.print("["+dataFile.getName());

	    switch (packMethod)
	    {
	    case PACK_ZIP_STORE:
		    ZipEntry zipEntry = new ZipEntry(dataFile.getName());
		    zipEntry.setMethod(ZipEntry.STORED);
		    zipEntry.setSize(dataFile.length());
		    zipEntry.setTime(dataFile.lastModified());
		    zip_out.putNextEntry(zipEntry);
		    break;
		case PACK_ZIP:
			zipEntry = new ZipEntry(dataFile.getName());
			zipEntry.setMethod(ZipEntry.DEFLATED);
			zipEntry.setSize(dataFile.length());
			zipEntry.setTime(dataFile.lastModified());
			zip_out.putNextEntry(zipEntry);
			break;
	    default:
	    case PACK_TAR:
		    TarEntry tarEntry = new TarEntry(dataFile);
		    tarEntry.setName(dataFile.getName());   //  no need to store path info
		    tar_out.putNextEntry(tarEntry);
		    break;
	    }

	    long bytesCopied = FileUtil.copyFile(dataFile, pack_out);
	    System.err.print(" "+bytesCopied);

	    switch (packMethod)
	    {
	    case PACK_ZIP_STORE:
	    case PACK_ZIP:
		    zip_out.closeEntry();
		    break;
	    default:
	    case PACK_TAR:
		    tar_out.closeEntry();
		    break;
	    }
	    System.err.println("]");
    }

	public File extractNextFile(File destDir)
			throws IOException
	{
		File result;

		switch (packMethod)
		{
		case PACK_ZIP_STORE:
		case PACK_ZIP:
			ZipEntry zipEntry = zip_in.getNextEntry();
			if (zipEntry==null) return null;

			result = new File(destDir,zipEntry.getName());
			FileUtil.copyStream(zip_in,zipEntry.getSize(), result);
			result.setLastModified(zipEntry.getTime());
			return result;
		default:
		case PACK_TAR:
			TarEntry tarEntry = tar_in.getNextEntry();
			if (tarEntry==null) return null;

			result = new File(destDir, tarEntry.getName());
			FileUtil.copyStream(tar_in,tarEntry.getSize(), result);
			result.setLastModified(tarEntry.getModTime().getTime());
			return result;
		}
	}

	public void extractAllFiles(File destDir) throws IOException
	{
		if (compressMethod==COMPRESS_RAR)
			extractRarFile(destDir);
		else {
		while (extractNextFile(destDir) != null)
			;
	}
	}

	private void extractRarFile(File destDir) throws IOException
	{
		if (file==null && hd1_in!=null) {
			//  put stream into temp file
			File tempRar = File.createTempFile("temp",".rar",destDir);
			FileUtil.copyStream(hd1_in,Long.MAX_VALUE, tempRar);
			close();

			extractRarFile(tempRar,destDir);
			tempRar.delete();
		}
		else if (file!=null)
			extractRarFile(file,destDir);
		else
			throw new IllegalStateException("file or input stream expected");
	}

	public static String extractRarFile(File rarFile, File destDir)
			throws IOException
	{
		String binPath = Application.theWorkingDirectory.getAbsolutePath()+File.separator+"bin";
		String execPath = binPath+File.separator+ Version.osDir+File.separator+"unrar";

		String[] command = {
			execPath, "e", rarFile.getAbsolutePath(),
		};		
		Process proc = Runtime.getRuntime().exec(command, null, destDir);

		InputStream stdout = proc.getInputStream();
		InputStream stderr = proc.getErrorStream();
		StringBuffer result = new StringBuffer();

		int c;
		while ((c=stdout.read())>=0)
			result.append((char)c);
		while ((c=stderr.read())>=0)
			result.append((char)c);

		for (;;)
			try {
				int exitValue = proc.waitFor();
				result.append("Exit Value: ");
				result.append(exitValue);
				break;
			} catch (InterruptedException e) {
				continue;
			}

		System.out.println(result.toString());
		return result.toString();
	}


	static class PrefixFilter implements FilenameFilter
	{
		protected String[] prefixes;

		public PrefixFilter(String[] prefixes)
		{
			this.prefixes = prefixes;
		}

		public boolean accept(File dir, String name)
		{
			for (int j=0; j < prefixes.length; j++)
				if (StringUtil.startsWithIgnoreCase(name,prefixes[j]))
					return true;
			return false;
		}
	}

	public void compress(File input, File output, int compressMethod)
			throws IOException
	{
		file_out = new FileOutputStream(output);

		switch (compressMethod)
		{
		case COMPRESS_GZIP:
			compress_out = gzip_out = new GZIPOutputStream(file_out,4096);
			FileUtil.copyFile(input,gzip_out);
			flush();
			break;
		case COMPRESS_BZIP2:
			compress_out = bzip_out = new CBZip2OutputStream(file_out,4096);
			FileUtil.copyFile(input,bzip_out);
			flush();
			break;
		default:
			throw new IllegalArgumentException();
}
	}

	public void uncompress(File input, File output)
			throws IOException
	{
		file_in = new FileInputStream(input);
		int compressMethod = detectCompressMethod(file_in,false);
		file_in.close();

		file_in = new FileInputStream(input);
		switch (compressMethod)
		{
		case COMPRESS_GZIP:
			gzip_in = new GZIPInputStream(file_in);
			FileUtil.copyStream(gzip_in, Integer.MAX_VALUE, output);
			break;
		case COMPRESS_BZIP2:
			bzip_in = new CBZip2InputStream(file_in);
			FileUtil.copyStream(bzip_in, Integer.MAX_VALUE, output);
			break;
		}
	}


	public static void main(String[] args)
	{
		File file = new File(args[1]);
		ArchiveFile ar = new ArchiveFile(file);

		try {
			if ("x".equalsIgnoreCase(args[0]))
			{
				//  uncompress
				File target = new File(file.getParentFile(), file.getName()+".tar");
				System.err.print("["+file+" --> "+target);
				ar.uncompress(file, target);
				ar.close();
				System.err.println("]");
			}
			if ("g".equalsIgnoreCase(args[0]))
			{
				//  compress
				File target = new File(file.getParentFile(), file.getName()+".gzip");
				System.err.print("["+file+" --> "+target);
				ar.compress(file,target, COMPRESS_GZIP);
				ar.close();
				System.err.println("]");
			}
			if ("b".equalsIgnoreCase(args[0]))
			{
				//  compress
				File target = new File(file.getParentFile(), file.getName()+".bzip2");
				System.err.print("["+file+" --> "+target);
				ar.compress(file,target, COMPRESS_BZIP2);
				ar.close();
				System.err.println("]");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
