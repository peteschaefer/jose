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

package de.jose.export;

import de.jose.util.file.FileUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XPathUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XPathUtil;
import de.jose.Language;
import de.jose.Util;
import de.jose.Application;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import javax.xml.transform.TransformerException;

/**
 * ExportConfig
 * 
 * @author Peter Schäfer
 */

public class ExportConfig
{
	/** export config elements  */
	protected org.w3c.dom.Document exportDoc;
	/** maps file names to elements */
	protected HashMap map;

	public static final String  INPUT_LIST_OF_GAMES     = "list-of-games";

	/** TODO unify with JoFileChooser constants */

	public static final int  OUTPUT_ARCH             = 0;
	public static final int  OUTPUT_PGN              = 1;
	public static final int  OUTPUT_AWT              = 2;
	public static final int  OUTPUT_XSL_FO           = 3;
	public static final int  OUTPUT_HTML             = 4;
	public static final int  OUTPUT_TEXT             = 5;
	public static final int  OUTPUT_XML              = 6;
	public static final int  OUTPUT_TEX              = 7;

	/**
	 * output capabilities
	 */
	public static final int CAN_PRINT                   = 0x01;
	public static final int CAN_EXPORT                  = 0x02;
	public static final int CAN_PREVIEW                 = 0x04;
	public static final int CAN_BROWSER_PREVIEW         = 0x08;

	public static final String[] OUTPUT_STRINGS = {
		"archive","pgn","awt","xsl-fo","html","text","xml","tex",
	};

	/** preferred file extensions   */
	public static final String[] FILE_EXTENSION = {
		"jose","pgn",null,"pdf","html","txt","xml","tex",
	};

    public static final String DEFAULT_EXPORT   = "export.archive";
    public static final String DEFAULT_PRINT    = "print.awt";


	private static final int[] OUTPUT_CAPABILITES = {
	  CAN_EXPORT,   //  can't print archive files
	  CAN_EXPORT,                                //  can't print PGN (or can we ?)
	  CAN_PREVIEW + CAN_PRINT,                   //  can't export, can print AWT
	  CAN_EXPORT + /*CAN_BROWSER_PREVIEW +*/ CAN_PREVIEW + CAN_PRINT,       //  can print XSL-FO
	  CAN_EXPORT + /*CAN_PREVIEW +*/ CAN_BROWSER_PREVIEW /*+ CAN_PRINT*/,       //  can print HTML
	  CAN_EXPORT + CAN_PREVIEW + CAN_PRINT,        //  can print plain text
	  CAN_EXPORT /*+ CAN_BROWSER_PREVIEW*/,       //  can't print XML
	  CAN_EXPORT,                               //  can't print LaTeX
	};

	protected static final String BUILTIN =
			"<export-config>" +
			"  <jose:export>" +
			"     <jose:title>export.archive</jose:title>" +
			"     <jose:input>list-of-games</jose:input>" +
			"     <jose:output>archive</jose:output>" +
			"  </jose:export>" +
			""+
	        "  <jose:export>" +
	        "     <jose:title>export.pgn</jose:title>" +
	        "     <jose:input>list-of-games</jose:input>" +
	        "     <jose:output>pgn</jose:output>" +
	        "  </jose:export>" +
	        ""+
	        "  <jose:export>" +
	        "     <jose:title>print.awt</jose:title>" +
	        "     <jose:input>list-of-games</jose:input>" +
	        "     <jose:output>awt</jose:output>" +
	        "  </jose:export>" +
	        "</export-config>";


	public ExportConfig(File file) throws Exception
	{
		map = new HashMap();
		exportDoc = XMLUtil.parse(new StringReader(BUILTIN));

		NodeList elems = exportDoc.getElementsByTagName("jose:export");
		for (int i=0; i < elems.getLength(); i++) {
			Element elm = (Element)elems.item(i);
			map.put(getTitle(elm),elm);
		}

		read(file);
	}

	public Element getConfig(String key)                        { return (Element)map.get(key); }

	public static String getTitle(Element info)                 { return XMLUtil.getChildValue(info,"jose:title"); }
	public static String getDisplayTitle(Element info)          { return Language.get(getTitle(info)); }

	public static String getInput(Element info)                 { return XMLUtil.getChildValue(info,"jose:input"); }
	public static int getOutput(Element info)                   { return parseOutput(XMLUtil.getChildValue(info,"jose:output")); }

	public static boolean canPrint(Element info)                { return canPrint(getOutput(info)); }
	public static boolean canExport(Element info)               { return canExport(getOutput(info)); }
	public static boolean canPreview(Element info)              { return canPreview(getOutput(info)); }
	public static boolean canBrowserPreview(Element info)       { return canBrowserPreview(getOutput(info)); }

	public static File getFile(Element info) {
		String path = info.getAttribute("file");
		return new File(path);
	}

	public NodeList getExportConfigs()
	{
		return exportDoc.getDocumentElement().getElementsByTagName("jose:export");
	}

	public static boolean canPrint(int output)               { return Util.anyOf(getOutputCapabilities(output), CAN_PRINT); }
	public static boolean canExport(int output)              { return Util.anyOf(getOutputCapabilities(output), CAN_EXPORT); }
	public static boolean canPreview(int output)             { return Util.anyOf(getOutputCapabilities(output), CAN_PREVIEW); }
	public static boolean canBrowserPreview(int output)      { return Util.anyOf(getOutputCapabilities(output), CAN_BROWSER_PREVIEW); }

	public static int getOutputCapabilities(int output) 	 { return OUTPUT_CAPABILITES[output]; }

	public static String getFileExtension(int type)         { return FILE_EXTENSION[type];  }

	protected static int parseOutput(String str)
	{
		for (int i=1; i < OUTPUT_STRINGS.length; i++)
			if (OUTPUT_STRINGS[i].equalsIgnoreCase(str)) return i;
		return 0;
	}

	protected void read(File file)
		throws Exception
	{
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int j=0; j<files.length; j++)
				read(files[j]);	//	may recurse
		}
		else if (file.isFile() &&
		        (FileUtil.hasExtension(file.getName(),"xsl") || FileUtil.hasExtension(file.getName(),"xslt")))
		{
            /** we are only interested in the <jose:export> part at the top of the file
             *  it is not necessary to parse the file completely
             */
			Document doc = XSLHeaderParser.getExportInfo(file);
            if (doc!=null)
			    importDoc(doc,file);
		}
	}

	public static String getParam(Element elem, String key) throws TransformerException
	{
		return XPathUtil.getString(elem,"param[key='"+key+"']/value");
	}

	public static String[] getParams(Element elem, String key) throws TransformerException
	{
		return XPathUtil.getStringArray(elem,"param[key='"+key+"']/value");
	}

	public static boolean getBooleanParam(Element elem, String key, boolean defaultValue) throws TransformerException
	{
		String text = getParam(elem,key);
		if (text==null)
			return defaultValue;
		else
			return Util.toboolean(text);
	}

	protected void importDoc (Document doc, File file)
	{
        NodeList nodes = doc.getElementsByTagName("jose:export");
		for (int i=0; i < nodes.getLength(); i++)
		{
			Element info = (Element)nodes.item(i);
			info = (Element)exportDoc.importNode(info,true);

			if (file != null)
				info.setAttribute("file",file.getAbsolutePath());

			exportDoc.getDocumentElement().appendChild(info);

			String key = getTitle(info);
			map.put(key,info);
		}
	}

}