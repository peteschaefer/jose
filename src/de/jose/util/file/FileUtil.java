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

package de.jose.util.file;

import de.jose.Language;
import de.jose.Version;
import de.jose.Util;
import de.jose.util.ListUtil;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.bzip2.CBZip2InputStream;

public class FileUtil
{
	public static final long KILO_BYTE		= 1024;
	public static final long MEGA_BYTE		= 1024*KILO_BYTE;
	public static final long GIGA_BYTE		= 1024*MEGA_BYTE;

	protected static NumberFormat BYTE_FORMAT;
	protected static NumberFormat KILO_BYTE_FORMAT;
	protected static NumberFormat MEGA_BYTE_FORMAT;

	/**
	 * deletes all files in a director
	 * @param recursive 
	 */
	public static void deleteDirectory(File dir, boolean recursive)
		throws IOException
	{
		if (!dir.isDirectory())
			throw new IllegalArgumentException("directory expected");
		
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++) 
		{
			if (files[i].isDirectory() && recursive)
				deleteDirectory(files[i],true);
			files[i].delete();
		}
	}

	/**
	 * swap two files
	 */
	public static boolean restoreFrom(File target, File temp)
	{
		File swap = new File(target.getParentFile(), target.getName()+".swap");
		target.renameTo(swap);
		if (temp.renameTo(target)) {
			swap.delete();
			return true;
		}
		else
			return false;
	}

	/**
	 * move all files from one directory to another
	 *
	 * @param srcDir
	 * @param destDir
	 * @throws IOException
	 */
	public static int moveAllFiles(File srcDir, File dstDir)
		throws IOException
	{
		String[] fileNames = srcDir.list();
		int count = 0;
		for (int i=0; i < fileNames.length; i++) {
			File src = new File(srcDir,fileNames[i]);
			File dst = new File(dstDir,fileNames[i]);
			if (src.renameTo(dst)) count++;
		}
		return count;
	}

	public static boolean isChildOf(File target, File root)
	{
		for (File parent = target.getParentFile(); parent != null; parent = parent.getParentFile())
			if (parent.equals(root))
				return true;
		return false;
	}

    public static String getRelativePath(File root, File target, String fileSeparator)
    {
        if (fileSeparator==null) fileSeparator = File.separator;

        Vector rootHierarchy = new Vector();
        for (File dir = root; dir!=null; dir = dir.getParentFile())
            rootHierarchy.add(dir);

        StringBuffer buf = new StringBuffer();
        for ( ; target!=null; target = target.getParentFile())
        {
            int idx = rootHierarchy.indexOf(target);
            if (idx == 0) {
                //  root is a direct parent of target
                if (buf.length()==0) buf.append(".");
                break;
            }
            else if (idx > 0) {
                //  there is a common parent
                while (idx-- > 0) {
                    if (buf.length() > 0) buf.insert(0,fileSeparator);
                    buf.insert(0,"..");
                }
                break;
            }
            else {
                //  climb one up
                if (buf.length() > 0) buf.insert(0,fileSeparator);
                buf.insert(0,target.getName());
            }
        }
        return buf.toString();
    }

    /**
     * @return a list of files in a directory
     */
    public static File[] listAllFiles(File dir, FileFilter filter)
    {
        ArrayList collect = new ArrayList();
        collectAllFiles(dir,filter,collect);

        File[] result = new File[collect.size()];
        collect.toArray(result);
        return result;
    }

    protected static void collectAllFiles(File dir, FileFilter filter, Collection result)
    {
        File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++)
        {
            if (filter.accept(files[i]))
                result.add(files[i]);
            if (files[i].isDirectory())
                collectAllFiles(files[i],filter,result);
        }
    }

	/**
	 * @return true if the directory is empty
	 */
	public static final boolean isEmptyDir(File dir)
	{
		if (! dir.isDirectory()) return false;
		String[] contents = dir.list();
		return (contents==null) || (contents.length==0);
	}

	public static boolean exists(String filePath)
	{
		return new File(filePath).exists();
	}

	public static boolean exists(File dir, String filePath)
	{
		return new File(dir,filePath).exists();
	}

	/**
	 * copies the contens of a stream
	 */
	public static long copyStream(InputStream in, OutputStream out)
		throws IOException
	{
		long copied = 0L;
		byte[] buffer = new byte[4096];
		for (;;) {
			int count = in.read(buffer,0,4096);
			if (count < 0) break;
			out.write(buffer,0,count);
			copied += count;
		}
		return copied;
	}
	
	/**
	 * copies the contens of a stream
	 */
	public static long copyStream(InputStream in, long length, OutputStream out)
		throws IOException
	{
		long reqLength = length;
		byte[] buffer = new byte[4096];
		while (length > 0) {
			int chunk = (int)Math.min(length,4096);
			int count = in.read(buffer,0, chunk);
			if (count < 0) break;
			out.write(buffer,0,count);
			length -= count;
		}
		return reqLength-length;
	}

	/**
	 * copies the contens of a stream to a file
	 */
	public static long copyStream(InputStream in, long length, File out)
		throws IOException
	{
        if (Version.java14orLater) {
            return FileUtilNIO.copyStream(in,length,out);
        }
        else {
            FileOutputStream stream = new FileOutputStream(out);
            long result = copyStream(in,length, stream);
            stream.close();
	        return result;
        }
	}

	/**
	 * copies the contens of a stream
	 */
	public static void copyReader(Reader in, Writer out)
		throws IOException
	{
		char[] buffer = new char[4096];
		for (;;) {
			int count = in.read(buffer,0,4096);
			if (count < 0) break;
			out.write(buffer,0,count);
		}
	}
	
	/**	
	 * copies the contents of a file
	 */
	public static void copyFile(File src, File dst)
		throws IOException
	{
		if (!src.isFile())
			throw new IllegalArgumentException("file expected");
		if (dst.isDirectory())
			dst = new File(dst,src.getName());

        if (Version.java14orLater)
            FileUtilNIO.copyFile(src,dst);
        else {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dst);

            copyStream(in,out);

            in.close();
            out.close();
        }
	}

	/**
	 * copies the contents of a file
	 */
	public static long copyFile(File src, OutputStream out)
		throws IOException
	{
		long bytesCopied=0;
        if (Version.java14orLater) {
            bytesCopied = FileUtilNIO.copyFile(src,out);
        }
        else {
            FileInputStream in = new FileInputStream(src);
            bytesCopied = copyStream(in,src.length(),out);
            in.close();
        }
		if (bytesCopied < src.length())
			throw new IOException(bytesCopied+" bytes copies. Expected "+src.length());
		return bytesCopied;
	}

	/**	
	 * copies the contents of a file
	 */
	public static void copyToZipFile(File src, File dst)
		throws IOException
	{
		ZipOutputStream out = new ZipOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(dst),4096));
		ZipEntry entry = new ZipEntry(src.getName());
		out.putNextEntry(entry);
		
		copyFile(src,out);
		
		out.closeEntry();
		out.close();
	}
	
	/**	
	 * copies the contents of a file
	 */
	public static void copyFromZipFile(File src, File dst)
		throws IOException
	{
		ZipInputStream in = new ZipInputStream(
                new BufferedInputStream(
                new FileInputStream(src), 4096));
		ZipEntry entry = in.getNextEntry();

		copyStream(in,-1,dst);
		
		in.close();
	}

	/**
	 * extract all files from a zip file
	 */
	public static File[] unzip(File zip, File dir)
		throws IOException
	{
       return unzipAll(new BufferedInputStream(new FileInputStream(zip),4096), dir);
    }

    public static void unzip(URL url, File dir)
        throws IOException
    {
        unzipAll (url.openStream(), dir);
    }

    public static File[] unzipAll(InputStream in, File dir)
            throws IOException
    {
	    ArrayList result = new ArrayList();
        ZipInputStream zin = new ZipInputStream(in);

		for (ZipEntry zety=zin.getNextEntry(); zety!=null; zety = zin.getNextEntry())
		{
			File dst = new File(dir, zety.getName());
			if (zety.isDirectory()) {
				dst.mkdirs();
			}
			else {
				long size = zety.getSize();
				if (size < 0) size = Long.MAX_VALUE;
				copyStream(zin,size, dst);
			}
			zin.closeEntry();
			result.add(dst);
		}

		in.close();
	    return (File[]) result.toArray(new File[result.size()]);
	}
	

    /**
     * extract one file from a zip file
     */
    public static boolean unzip(File zip, String fileName, File dst)
        throws IOException
    {
        ZipFile zfile = new ZipFile(zip,ZipFile.OPEN_READ);
        ZipEntry zety = zfile.getEntry(fileName);
        if (zety==null) return false;

        InputStream in = zfile.getInputStream(zety);
        copyStream(in,-1,dst);
        in.close();
        return true;
    }

	public static void addToZip(ZipOutputStream zip, InputStream in, String name,
                                long size, long time)
		throws IOException
	{
		ZipEntry zety = new ZipEntry(name);
        zety.setSize(size);
        zety.setTime(time);
		zip.putNextEntry(zety);
        if (in!=null)
		    copyStream(in,zip);
		zip.closeEntry();
	}


	public static void addToZip(ZipOutputStream zip, File fil, boolean longPath)
		throws IOException
	{
		if (longPath) {
            addToZip(zip,fil,fil.getAbsolutePath());
        }
		else {
            addToZip(zip,fil,fil.getName());
        }
	}

    public static void addToZip(ZipOutputStream zip, File fil, String path)
        throws IOException
    {
        if (fil.isDirectory())
        {
            if (!path.endsWith("/")) path += "/";
            addToZip(zip,(InputStream)null,path, 0,fil.lastModified());
        }
        else
        {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(fil);
                if (Version.java14orLater)
                    FileUtilNIO.addToZip(zip,stream.getChannel(), path, fil.length(),fil.lastModified());
                else
                    addToZip(zip,stream, path, fil.length(),fil.lastModified());
            } finally {
                stream.close();
            }
        }
    }

	public static void addToZip(ZipOutputStream zip, Object obj, String name)
		throws IOException
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(obj);
		oout.close();

		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		addToZip(zip,bin,name);
		bin.close();
	}


    /**
	 * read a text from a URL
	 */
	public static String readTextStream(URL url, String encoding)
		throws IOException
	{
		InputStream in = url.openStream();
        return readTextStream(in, encoding);
	}

    /**
	 * read a text input stream
	 */
	public static String readTextStream(InputStream input, String encoding)
		throws IOException
	{
		InputStreamReader in = new InputStreamReader(input,encoding);
		StringWriter out = new StringWriter();
		copyReader(in,out);
		in.close();
		out.close();

		return out.toString();
	}

	/**
	 * read a text file
	 */
	public static String readTextFile(File file)
		throws IOException
	{
		FileReader in = new FileReader(file);
		StringWriter out = new StringWriter();
		copyReader(in,out);
		in.close();
		out.close();
		
		return out.toString();
	}

	/**
	 * read a binary file
	 */
	public static byte[] readBinaryFile(File file)
		throws IOException
	{
		FileInputStream in = new FileInputStream(file);
		ByteArrayOutputStream out = new ByteArrayOutputStream((int)file.length());
		copyStream(in,out);
		in.close();
		out.close();

		return out.toByteArray();
	}

    private static int getExtIndex(String name)
    {
        int k0 = name.lastIndexOf(".");
        if (k0 <= 0) return -1;

        int k1 = name.lastIndexOf(File.separator);
        int k2 = name.lastIndexOf("/");
		if ((k0>(k1+1)) && (k0>(k2+1)))
            return k0;
        else
            return -1;
    }

	public static String getExtension(String name)
	{
		int k = getExtIndex(name);
		if (k > 0)
			return name.substring(k+1);
		else
			return null;
	}

	public static boolean hasExtension(String name)
	{
        return getExtIndex(name) > 0;
	}

	/**
	 * @return true if the file name has a certain extension
	 */
	public static boolean hasExtension(String name, String ext)
	{
		int k = getExtIndex(name);
		if (k < 0) return false;

		return name.regionMatches(true,k+1, ext,0, name.length()-k-1);
	}

    public static String trimExtension(String name)
    {
        int k = getExtIndex(name);
        if (k > 0)
            return name.substring(0,k);
        else
            return name;
    }




	/**
	 * @return a file name with a given extension
	 */
	public static String setExtension(String name, String ext)
	{
		int k = getExtIndex(name);
		if (k < 0) return name+"."+ext;

		if (name.regionMatches(true,k+1, ext,0, name.length()-k-1))
			return name;
		else
			return name.substring(0,k+1)+ext;
	}

    public static File appendExtension(File file, String ext)
    {
        if (hasExtension(file.getName()))
            return file;
        String newName = setExtension(file.getName(),ext);
        return new File(file.getParentFile(), newName);
    }



	public static String formatFileSize(double size)
	{
		if (BYTE_FORMAT==null)			BYTE_FORMAT			= new DecimalFormat(Language.get("format.byte"));
		if (KILO_BYTE_FORMAT==null)		KILO_BYTE_FORMAT	= new DecimalFormat(Language.get("format.kilobyte"));
		if (MEGA_BYTE_FORMAT==null)		MEGA_BYTE_FORMAT	= new DecimalFormat(Language.get("format.megabyte"));

		if (size < KILO_BYTE)
			return BYTE_FORMAT.format(size);
		else if (size < MEGA_BYTE)
			return KILO_BYTE_FORMAT.format(size/KILO_BYTE);
		else
			return MEGA_BYTE_FORMAT.format(size/MEGA_BYTE);
	}

	public static String getFilePath(URL url)
	{
		return URLDecoder.decode(url.getPath());
	}

	public static File getFile(URL url)
	{
		return new File(getFilePath(url));
	}

	public static String getFileName(URL url)
	{
		File file = getFile(url);
		return file.getName();
	}

	public static void escapeExec(StringBuffer buf)
	{
		if (Version.windows) {
			buf.insert(0,'"');
			buf.append('"');
		}
		else {
			for (int i = buf.lastIndexOf(" "); i>=0; i = buf.lastIndexOf(" ",i))
				buf.insert(i,'\\');
		}
	}

	public static String escapeExec(String path)
	{
		StringBuffer buf = new StringBuffer(path);
		escapeExec(buf);
		return buf.toString();
	}

	public static File uniqueFile(File dir, String name, String ext)
	{
		if (ext==null || ext.length()==0)
			ext = "";
		else
			ext = "."+ext;

		/** try plain   */
		File file = new File(dir,name+ext);
		if (!file.exists()) return file;

		/** keep counting ...		 */
		for (int copy=2; ; copy++)
		{
			file = new File(dir,name+"["+copy+"]"+ext);
			if (!file.exists()) return file;
		}
	}

	public static File uniqueFile(File dir, String fileName)
	{
		int k1 = fileName.lastIndexOf('.');
		if (k1 > 0)
			return uniqueFile(dir, fileName.substring(0,k1), fileName.substring(k1+1));
		else
			return uniqueFile(dir, fileName, "");
	}

    public static String fixSeparators(String path)
    {
        return fixSeparators(path,File.separatorChar);
    }

    public static String fixSeparators(String path, char fileSeparator)
    {
        for (int i=path.length()-1; i>=0; i--)
        {
            char c = path.charAt(i);
            if ((c=='/' || c=='\\') && (c != fileSeparator))
            {
                StringBuffer buf = new StringBuffer(path);
                buf.setCharAt(i,fileSeparator);
                for (i--; i >= 0; i--)
                {
                    c = path.charAt(i);
                    if (c=='/' || c=='\\') buf.setCharAt(i,fileSeparator);
                }
                return buf.toString();
            }
        }
        return path;
    }


	public static File[] scanPath(String path, File workDir, boolean existing)
	{
		ArrayList collect = new ArrayList();
		StringTokenizer tok = new StringTokenizer(path,";:",false);
		while (tok.hasMoreTokens())
		{
			File file = new File(workDir,tok.nextToken());
			if (!existing || file.exists())
				collect.add(file);
		}
		return (File[])ListUtil.toArray(collect,File.class);
	}

	public static void chmod(String mod, String file) throws IOException
	{
		if (Version.unix) {
			String cmd = "chmod "+mod+" "+file;
			Runtime.getRuntime().exec(cmd);
			System.err.println(cmd);
		}
	}

	public static void chmodAll(String mod, String path) throws IOException
	{
		if (Version.unix) {
			File dir = new File(path);
			String[] files = dir.list();
			for (int i=0; i<files.length; i++)
				chmod(mod, path+File.separator+files[i]);
		}
	}

	public static CBZip2OutputStream createBZipOutputStream(OutputStream output) throws IOException
	{
		//  write magic bytes
		output.write('B');
		output.write('Z');
		return new CBZip2OutputStream(output);
	}

    public static CBZip2OutputStream createBZipOutputStream(OutputStream output, int level) throws IOException
    {
        //  write magic bytes
        output.write('B');
        output.write('Z');
        return new CBZip2OutputStream(output,level);
    }

	public static CBZip2InputStream createBZipInputStream(InputStream input) throws IOException
	{
		//  read magic bytes
		if (input.read() != 'B' || input.read() != 'Z')
			throw new IOException("invalid bzip file");
		return new CBZip2InputStream(input);
	}

}
