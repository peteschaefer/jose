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

import de.jose.util.file.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Peter Schäfer
 */
public class Robocopy
{
    protected static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyMMdd");

    protected static File newDiff(File dir)
    {
        Date now = new Date();
        return FileUtil.uniqueFile(dir, "diff-"+DATEFORMAT.format(now), "zip");
    }

    public static void synchDirect(File source, File target)  throws IOException
    {
        if (source.isFile())
        {
            if (!target.exists())
            {
                //  new file
                System.out.println("create "+target);
                FileUtil.copyFile(source,target);
                target.setLastModified(source.lastModified());
            }
            else if (target.isDirectory())
            {
                System.out.println("can't compare file"+source+" to directory "+target);
            }
            else
            {
                //  compare Files
                if (Diff.compareFiles(source,target))
                       /* up to date */ ;
                else
                {
                    //  different
                    if (source.lastModified() > target.lastModified())
                    {
                        //  source is newer: overwrite without asking
                        System.out.println("update "+target);
                        FileUtil.copyFile(source,target);
                        target.setLastModified(source.lastModified());
                    }
                    else
                    {
                        System.out.println("target "+target+" is newer!");
                    }
                }
            }
        }
        else
        {
            if (target.isFile())
            {
                System.out.println("can't compare directory "+source+" to file "+target);
            }
            else
            {
                if (!target.exists())
                {
                    //  create directory
                    System.out.println("create "+target);
                    target.mkdirs();
                }

                //  compare directories
                String[] sourceFiles = source.list();

                for (int i=0; i<sourceFiles.length; i++)
                {
                    File s = new File(source,sourceFiles[i]);
                    File t = new File(target,sourceFiles[i]);
                    synchDirect(s,t);
                }

                //  loook for deleted files
                String[] targetFiles = target.list();
                for (int i=0; i<targetFiles.length; i++)
                {
                    File s = new File(source,targetFiles[i]);
                    File t = new File(target,targetFiles[i]);
                    if (!s.exists())
                    {
                        //  file has been deleted
                        if (t.isDirectory())
                        {
                            System.out.println("delete "+t);
                            FileUtil.deleteDirectory(t,true);
                        }
                        else
                        {
                            System.out.println("delete "+t);
                            t.delete();
                        }
                    }
                }
            }
        }

    }

    public static void createDiff(File source, File diff)    throws IOException
    {
        ZipOutputStream zip = null;
        HashMap log = new HashMap();

        try {
            zip = new ZipOutputStream(new FileOutputStream(diff));
            add(source, source,zip, true, log);
        } finally {
            writeLog(log,zip);
            zip.close();
        }
    }

    public static void add(File root, File source, ZipOutputStream zip,
                           boolean recursive, HashMap log)
            throws IOException
    {
        String relativePath = FileUtil.getRelativePath(root,source,"/");
        FileUtil.addToZip(zip,source,relativePath);

        log.put(relativePath,new Date(source.lastModified()));

        if (source.isDirectory() && recursive)
        {
            File[] children = source.listFiles();
            for (int i=0; i < children.length; i++)
                add(root,children[i],zip,recursive,log);
        }
    }

    public static void synchTarget(File diff, File target, File newDiff)
            throws IOException
    {
        ZipFile zipin = null;
        ZipOutputStream zipout = null;

        try {
            zipin = new ZipFile(diff);
            zipout = new ZipOutputStream(new FileOutputStream(newDiff));

            //  read log
            HashMap log = readLog(zipin);

            synchFromZip(target, zipin, zipout, log);

            zipin.close();

            synchToZip(target, target,zipout,log);

            writeLog(log,zipout);

        } finally {
            zipout.close();
        }
    }


    protected static void synchFromZip(File root, ZipFile zipin,
                                       ZipOutputStream zipout, HashMap log)
        throws IOException
    {
        for (Iterator i = log.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry mety = (Map.Entry)i.next();
            String path = (String)mety.getKey();
            Date time = (Date)mety.getValue();
            ZipEntry zety = zipin.getEntry(path);

            File target = new File(root,path);

            if (time==null)
            {
                //  delete file
                if (target.exists())
                {
                    System.out.println("delete "+target);
                    if (target.isDirectory())
                        FileUtil.deleteDirectory(target,true);
                    else
                        target.delete();
                }
            }
            else
            {
                if (!target.exists())
                {
                    //  create from zip
                    System.out.println("create "+target);

                    if (zety.isDirectory())
                        target.mkdirs();
                    else
                    {
//                        FileUtil.extract(zety, target);
                        //  TODO
                    }

                    target.setLastModified(zety.getTime());
                }
                else if (time.getTime() > target.lastModified())
                {
                    //  update
                    //  TODO
                }
                else
                {
                    //  target is newer !
                    System.out.println("");
                    //  TODO
                }
            }

        }
    }


    protected static void synchToZip(File root, File file,
                                       ZipOutputStream zipout, HashMap log)
    {

    }

    protected static HashMap readLog(ZipFile zip)
            throws IOException
    {
        HashMap map = new HashMap();
        ZipEntry ety = zip.getEntry("robocopy.log");
        InputStream zin = null;
        try {
            zin = zip.getInputStream(ety);
            BufferedReader rd = new BufferedReader(new InputStreamReader(zin,"UTF-8"));
            for (;;)
            {
                String line = rd.readLine();
                if (line==null) break;
                int k1 = line.indexOf(" ");
                long time = Long.parseLong(line.substring(0,k1),16);
                String path = line.substring(k1+1);

                if (time==0)
                    map.put(path,null); //  indicates a deleted file
                else
                    map.put(path,new Date(time));
            }

        } finally {
            zin.close();
        }
        return map;
    }

    protected static void writeLog(HashMap map, ZipOutputStream zip) throws IOException
    {
        ZipEntry ety = new ZipEntry("robocopy.log");
        zip.putNextEntry(ety);

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(zip,"UTF-8"));
        for (Iterator i = map.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry mety = (Map.Entry)i.next();
            String path = (String)mety.getKey();
            Date date = (Date)mety.getValue();
            pw.print(Long.toHexString(date.getTime()));
            pw.print(" ");
            pw.println(path);
        }
        pw.flush();
        zip.closeEntry();
    }


    public static void main(String[] args)
    {
        try {
            File source = null;
            File target = null;
            File diff = null;

            for (int i=0; i < args.length; i++)
            {
                File f = new File(args[i]);
                if (!f.exists())
                {
                    target = f;
                }
                else if ( f.isFile())
                {
                    if (diff!=null) throw new IllegalArgumentException();
                    diff = f;
                }
                else if (f.isDirectory())
                {
                    if (source==null)
                    {
                        if (source!=null) throw new IllegalArgumentException();
                        source = f;
                    }
                    else
                    {
                        if (target!=null) throw new IllegalArgumentException();
                        target = f;
                    }
                }
            }

            if (source!=null && target!=null)
            {
                //  synch source to target
                System.out.println("synch "+source+" to "+target);
                synchDirect(source,target);
            }
            else if (source!=null && diff==null && target==null)
            {
                //  create a new diff from source
                diff = newDiff(source.getParentFile());

                System.out.println("record diffs from "+source+" to "+diff);
                createDiff(source,diff);
            }
            else if (source==null && diff!=null && target!=null)
            {
                //  synch diff with target
                System.out.println("synch diffs from "+diff+" to "+target);
                //  keep new diffs
                File new_diff = newDiff(diff.getParentFile());
                synchTarget(diff,target,new_diff);
            }
            else
                throw new IllegalArgumentException();

        } catch (Throwable e)
        {
            System.out.println("java de.jose.devtools.Robocopy <source-directory> <target-direcory>");
            System.out.println("        synchronize two directories");
            System.out.println("java de.jose.devtools.Robocopy <source-directory>");
            System.out.println("        record directory status");
            System.out.println("java de.jose.devtools.Robocopy <status>.zip <target-direcory>");
            System.out.println("        synchronize target with status");
            System.out.println("");

            e.printStackTrace();
        }
    }
}
