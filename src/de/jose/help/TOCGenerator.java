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

package de.jose.help;

import de.jose.Language;
import de.jose.Version;
import de.jose.util.IntArray;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;

import java.io.*;
import java.util.*;

/**
 * Creates Map and TOC files for JavaHelp
 *
 * writing map and toc files for JavaHelp is just too tedious to do it manually ;-)
 * here's how we do it:
 *
 * - each HTML file contains an header entry
 *     <meta name="help" content="topic">
 *  that is the help topic for the file
 *
 * - topic are marked with
 *      <a name="topic">
 *          <h1> title </h1>
 *      </a>
 *  the a element sets the help topic, the following <h1>...<h4> element is the toc title
 *
 *  HTMLParser parsers the HTML files for these entries, TOCGenerator writes map.jhm and toc.xml files
 *  that are used by JavaHelp.
 *
 *  @author Peter Schäfer
 */

public class TOCGenerator
{
    public static final String[]  INDEX_FILES = {
        "index.html","index.htm","default.html","default.htm",
    };

	/**	icon used for folder (null=default)	*/
	public static final String FOLDER_ICON =	"img.folder";
	/**	icon used for file (null=default)	*/
	public static final String FILE_ICON = 	"img.file";
	/**	icon used for anchors (null=default)	*/
	public static final String ANCHOR_ICON = "img.empty";

    /** the directory that contains the HTML files  */
    protected File  rootDir;
    /** the resulting Map file  */
    protected File  mapFile;
    /** the resulting TOC file  */
    protected File  tocFile;
	/**	the help set file	*/
	protected File	setFile;
	/**	don't create toc entries for these directories	*/
	protected Set notoc = new TreeSet();
	/**	maps IDs to URLs	*/
	protected HashMap urlMap;
	/**	maps IDs to TOC titles	*/
	protected HashMap titleMap;
	/**	toc list (of Ids) */
	protected List	tocList;
	/**	toc item level	*/
	protected IntArray tocLevels;
	protected int	tocIdx;
	/**	cursor index	*/

    protected PrintWriter   mapWriter;
    protected PrintWriter   tocWriter;
    protected int           tocLevel;

    public TOCGenerator()
    { }

    public String setArgs(String[] args)
    {
        String root = null;
        String map = null;
        String toc = null;
		String set = null;

        for (int i=0; i<args.length; i++) {
            if (args[i].equalsIgnoreCase("-map") && ((i+1) < args.length))
                map = args[++i];
            else if (args[i].equalsIgnoreCase("-toc") && ((i+1) < args.length))
                toc = args[++i];
            else if (args[i].equalsIgnoreCase("-root") && ((i+1) < args.length))
                root = args[++i];
			else if (args[i].equalsIgnoreCase("-set") && ((i+1) < args.length))
				set = args[++i];
			else if (args[i].equalsIgnoreCase("-notoc") && ((i+1) < args.length))
				notoc.add(args[++i]);
            else
                root = args[i];
        }

        if (root==null)
            return "Root Directory expected";
        else
            rootDir = new File(root);
        if (!rootDir.exists())
            return "Root Directory not found: "+rootDir;

        if (map != null)
            mapFile = new File(rootDir,map);
        else
            mapFile = new File(rootDir,"map.jhm");

        if (toc != null)
            tocFile = new File(rootDir,toc);
        else
            tocFile = new File(rootDir,"toc.xml");

		if (set != null)
			setFile = new File(rootDir,set);
		else
			setFile = new File(rootDir,"help-en.hs");

        return null;    //  indicates no error
    }

    public void run() throws IOException
    {
		urlMap = new HashMap();
		tocList = new Vector();
		tocLevels = new IntArray();
		titleMap = new HashMap();

        openMap();
        openTOC();

        /** traverse all available files    */
        tocLevel = 0;
        readDir(rootDir,true);

        closeMap();
        closeTOC();

		/**	replace macros	*/
		replaceMacros();
    }

	class TOCMacroProcessor extends MacroProcessor
	{
		TOCMacroProcessor() {
			super();
			defineMacro("path", "expandPath", 1);
			defineMacro("previous-file", "getPreviousFile", 0);
			defineMacro("next-file", "getNextFile", 0);
			defineMacro("chapter-file", "getChapterFile", 0);
			defineMacro("toc-path", "getTOCPath", 1);
			defineMacro("toc-title", "getTOCTitle", 1);
			defineMacro("toc", "createToc",2);
			defineMacro("lang", "translate",1);
			defineMacro("version", "getVersion",0);
			defineMacro("include", "includeFile",1);
		}

		public String getPreviousFile()
		{
			File thisFile = getFile(tocIdx);
			File targetFile = null;
			for (int i=tocIdx-1; i>=0; i--) {
				targetFile = getFile(i);
				if (targetFile!=null)
					return FileUtil.getRelativePath(thisFile.getParentFile(),targetFile,"/");
			}
			return "";
		}

		public String getNextFile()
		{
			File thisFile = getFile(tocIdx);
			File targetFile = null;
			for (int i=tocIdx+1; i<tocList.size(); i++) {
				targetFile = getFile(i);
				if (targetFile!=null)
					return FileUtil.getRelativePath(thisFile.getParentFile(),targetFile,"/");
			}
			return "";
		}

		public String getChapterFile()
		{
			File thisFile = getFile(tocIdx);
			int thisLevel = tocLevels.get(tocIdx);
			for (int i=tocIdx-1; i >= 0; i--)
				if (tocLevels.get(i) < thisLevel) {
					File targetFile = getFile(i);
					if (targetFile!=null)
						return FileUtil.getRelativePath(thisFile.getParentFile(),targetFile,"/");
				}
			return "";
		}

		public String getVersion()
		{
			return Version.jose;
		}

		public String getTOCPath(String id)
		{
			File thisFile = getFile(tocIdx);
			File targetFile = getFile(id);
			String targetAnchor = getAnchor(id);

			String path = FileUtil.getRelativePath(thisFile.getParentFile(),targetFile,"/");
			if (targetAnchor!=null)
				return path+"#"+targetAnchor;
			else
				return path;
		}

		public String getTOCTitle(String id)
		{
			return (String)titleMap.get(id);
		}

		public String includeFile (String path)
		{
			try {

				File file = new File(rootDir,path);
				return FileUtil.readTextFile(file);

			} catch (IOException e) {
				return e.getLocalizedMessage();
			}
		}

		public String createToc(String rootId, String depthS)
		{
			int depth = Integer.parseInt(depthS);
			int i;
			for (i=0; i<tocList.size(); i++)
				if (tocList.get(i).equals(rootId))
					break;
			if (i >= tocList.size()) i = 0;
			int rootLevel = tocLevels.get(i);

			StringWriter sout = new StringWriter();
			PrintWriter out = new PrintWriter(sout);

			for (i++; i < tocList.size(); i++)
			{
				int level = tocLevels.get(i);
				if (level<=rootLevel) break;
				if (level>(rootLevel+depth)) continue;

				writeIndent(out, level);
				int h = Math.min(level-rootLevel,4);
				out.print("<h"+h+" class=\"toc\">");
				out.print(expand("@@see:"+tocList.get(i)+"@",false));
				out.println("</h"+h+">");
			}

			out.close();
			return sout.toString();
		}

		public String translate(String text)
		{
			return Language.get(text);
		}
	}

	protected File getFile(int tocidx)
	{
		String fileId = (String)tocList.get(tocidx);
		return getFile(fileId);
	}

	protected File getFile(String fileId)
	{
		String relPath = (String)urlMap.get(fileId);
		if (relPath==null) return null;

		int k1 = relPath.indexOf("#");
		if (k1 < 0)
			return new File(rootDir,relPath);
		else
			return new File(rootDir,relPath.substring(0,k1));
	}


	protected String getAnchor(String fileId)
	{
		String relPath = (String)urlMap.get(fileId);
		if (relPath==null) return null;

		int k1 = relPath.indexOf("#");
		if (k1 < 0)
			return null;
		else
			return relPath.substring(k1+1);
	}

	protected void replaceMacros() throws IOException
	{
		Language.setLanguage(new File("D:/jose/work/config"),"en");

		MacroProcessor mac = new TOCMacroProcessor();
		mac.setRootDirectory(rootDir);
		mac.readDefinition(new File(rootDir,"macros.html"));
//		mac.process(new File(dir,"01-menu/01-file.html"));

		for (tocIdx=0; tocIdx < tocList.size(); tocIdx++)
		{
			File file = getFile(tocIdx);
			if (file==null)
				continue;
			if (!file.exists()) {
				System.err.println(file.getAbsolutePath()+ "not found");
				continue;
			}

			System.err.print("["+file.getName());
			mac.process(file);
			System.err.println("]");
		}
	}

	class FileNameComparator
		implements Comparator
	{
		public int compare(Object a, Object b)
		{
			File fa = (File)a;
			File fb = (File)b;
			return fa.getName().compareToIgnoreCase(fb.getName());
		}
	}

    protected void readDir(File dir, boolean addToc) throws IOException
    {
		if (notoc.contains(dir.getName())) addToc = false;

        File indexFile = oneOf(dir,INDEX_FILES);
        if (indexFile != null)
            readFile(indexFile,addToc,true);
        else if (addToc) {
        	String id = FileUtil.getRelativePath(rootDir,dir,".");
			String title = dir.getName();
            openTocItem(id,title,FOLDER_ICON);
		}

        File[] files = dir.listFiles();
		Arrays.sort(files,new FileNameComparator());
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory())
                readDir(files[i],addToc);
            else if (!files[i].equals(indexFile)) {
				if (FileUtil.hasExtension(files[i].getName(),"html")
                     || FileUtil.hasExtension(files[i].getName(),"htm"))
					readFile(files[i],addToc,false);
				else if (FileUtil.hasExtension(files[i].getName(),"txt"))
					readTextFile(files[i],addToc);
				else if (FileUtil.hasExtension(files[i].getName(),"gif") ||
							FileUtil.hasExtension(files[i].getName(),"jpeg") ||
							FileUtil.hasExtension(files[i].getName(),"jpg"))
				{
					String id = FileUtil.getRelativePath(rootDir,files[i],".");
					id = FileUtil.trimExtension(id);

					String url = FileUtil.getRelativePath(rootDir,files[i],"/");
					addMapId(id,url);
				}
			}

        }
		if (addToc)
        	closeTocItem();
    }

    protected void readFile(File file, boolean addToc, boolean openToc) throws IOException
    {
		if (notoc.contains(file.getName())) addToc = false;

        HTMLParser parser = new HTMLParser();
        parser.parse(file);

        String mapId = parser.getHelpReference();
		if (mapId == null) {
			mapId = FileUtil.getRelativePath(rootDir,file,".");
			mapId = FileUtil.trimExtension(mapId);
		}

        String title = parser.getTitle();
		if (title==null)
			title = file.getName();
        String[] anchors = parser.getAnchors();

        String relPath = FileUtil.getRelativePath(rootDir,file,"/");
        mapWriter.println("   <!-- "+relPath+" -->");

        addMapId(mapId,relPath);

		if (!addToc)
			/* don't create TOC entry */ ;
        else if (openToc)
            openTocItem(mapId,title,FOLDER_ICON);
        else if (anchors != null)
            openTocItem(mapId,title,FILE_ICON);
        else
            writeTocItem(mapId,title,FILE_ICON);

        if (anchors != null) {
            for (int i=0; i < anchors.length; i++) {
                addMapId(anchors[i], relPath+"#"+anchors[i]);
				if (addToc)
                	writeTocItem(anchors[i], parser.getAnchorTitle(anchors[i]), ANCHOR_ICON);
            }
            if (addToc && !openToc)
                closeTocItem();
        }
    }

	protected void readTextFile(File file, boolean addToc) throws IOException
	{
		if (notoc.contains(file.getName())) addToc = false;

		String mapId = FileUtil.getRelativePath(rootDir,file,".");
		mapId = FileUtil.trimExtension(mapId);

		String title = file.getName();
		title = FileUtil.trimExtension(title);

		String relPath = FileUtil.getRelativePath(rootDir,file,"/");
		mapWriter.println("   <!-- "+relPath+" -->");

		addMapId(mapId,relPath);

		if (addToc)
			writeTocItem(mapId,title,FILE_ICON);
	}

    protected File oneOf(File dir, String[] fileNames)
    {
        for (int i=0; i<fileNames.length; i++) {
            File file = new File(dir,fileNames[i]);
            if (file.exists()) return file;
        }
        return null;
    }

    protected void openMap() throws IOException
    {
        System.out.println("creating map file: "+mapFile.getAbsolutePath());
        mapWriter = new PrintWriter(new FileWriter(mapFile));

        mapWriter.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
        mapWriter.println("<!DOCTYPE map");
        mapWriter.println(" PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp Map Version 2.0//EN\"");
        mapWriter.println("     \"http://java.sun.com/products/javahelp/map_2_0.dtd\">");
        mapWriter.println("<map version=\"2.0\">");
    }

    protected void addMapId(String id, String url)
    {
        mapWriter.print("   <mapID target=\"");
        mapWriter.print(id);
        mapWriter.print("\" url=\"");
        mapWriter.print(url);
        mapWriter.println("\"/>");

		if (urlMap.containsKey(id))
			System.err.println("duplicate id: "+id+" = "+urlMap.get(id)+" and "+url);
		urlMap.put(id,url);
    }

    protected void closeMap() throws IOException
    {
        mapWriter.println("</map>");
        mapWriter.close();
        System.out.println("map created.");
    }

    protected void openTOC() throws IOException
    {
        System.out.println("creating toc file: "+tocFile.getAbsolutePath());
        tocWriter = new PrintWriter(new FileWriter(tocFile));

        tocWriter.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
        tocWriter.println("<!DOCTYPE map");
        tocWriter.println(" PUBLIC \"-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 2.0//EN\"");
        tocWriter.println("     \"http://java.sun.com/products/javahelp/toc_2_0.dtd\">");
        tocWriter.println("<toc version=\"2.0\">");
    }

    protected void openTocItem(String target, String text, String image)
    {
        writeIndent(tocWriter,tocLevel++);
        tocWriter.print("<tocitem");
        if (target!=null) {
            tocWriter.print(" target=\"");
            tocWriter.print(target);
            tocWriter.print("\"");
        }
        if (text!=null) {
            tocWriter.print(" text=\"");
            tocWriter.print(XMLUtil.escapeText(text));
            tocWriter.print("\"");
        }
		if (image!=null) {
			tocWriter.print(" image=\"");
			tocWriter.print(image);
			tocWriter.print("\"");
		}
        tocWriter.println(">");

		tocList.add(target);
		tocLevels.add(tocLevel-1);
		titleMap.put(target,text);
    }

    protected void closeTocItem()
    {
        writeIndent(tocWriter,--tocLevel);
        tocWriter.println("</tocitem>");
    }

    protected void writeTocItem(String target, String text, String image)
    {
        writeIndent(tocWriter,tocLevel);
        tocWriter.print("<tocitem");
        if (target!=null) {
            tocWriter.print(" target=\"");
            tocWriter.print(target);
            tocWriter.print("\"");
        }
        if (text!=null) {
            tocWriter.print(" text=\"");
            tocWriter.print(XMLUtil.escapeText(text));
            tocWriter.print("\"");
        }
		if (image!=null) {
			tocWriter.print(" image=\"");
			tocWriter.print(image);
			tocWriter.print("\"");
		}
        tocWriter.println("/>");

		tocList.add(target);
		tocLevels.add(tocLevel);
		titleMap.put(target,text);
    }

    protected void closeTOC() throws IOException
    {
        tocWriter.println("</toc>");
        tocWriter.close();
        System.out.println("toc created.");
    }


    protected static void writeIndent(PrintWriter out, int indent)
    {
        while (indent-- > 0) out.print("\t");
    }

    public void printHelp(String error)
    {
        PrintStream out = System.out;

        out.println("java "+getClass().getName()+" [-root] <dir> [-map <map file>] [-toc <toc file>]");

        if (error != null) {
            out.println(error);
            out.println();
        }
    }


    public static void main(String[] args)
    {
        try {
            TOCGenerator toc = new TOCGenerator();
            String error = toc.setArgs(args);
            if (error!=null) {
                toc.printHelp(error);
                return;
            }
            toc.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
