/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose;

import de.jose.util.xml.XMLUtil;
import de.jose.util.ListUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.*;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;

public class Config
{
	/**	Vector of Element
	 *
	 *  TODO use org.w3c.Document that contains all the elements.
	 *  Use XPathUtil to navigate.
	 *  Problem: how to put several files into one Document ?
	 *  importNode() probably involves lots of copying. Can the DOM parser do it ?
	 *  Remember: engine config elements need to be editable !
	 *
	 * ArrayList<Element>
	 *  */
	protected ArrayList roots;

	public Config()
	{
		roots = new ArrayList();
	}
	
	public Config(File file)
		throws Exception
	{
		this();
		read(file);
	}


	public void read(File file)
		throws Exception
	{
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int j=0; j<files.length; j++)
				read(files[j]);	//	may recurse
		}
		else if (file.isFile() && FileUtil.hasExtension(file.getName(),"xml"))
		{
			org.w3c.dom.Document doc = XMLUtil.parse(file);
			setFile(doc,file.getAbsolutePath());
			roots.add(doc.getDocumentElement());
		}
	}

	public Enumeration enumerateElements(String tagName)
	{
		return new ElementEnumeration(tagName);
	}


	public Element getFirstElement(String tagName)
	{
		for (int j=0; j<roots.size(); j++)
		{
			Element root = (Element)roots.get(j);
			Element child = XMLUtil.getChild(root,tagName);
			if (child!=null)
				return child;
		}
		return null;
	}
	
	public Vector getElements(String tagName)
	{
		Vector result = new Vector();
		Enumeration en = enumerateElements(tagName);
		while (en.hasMoreElements())
			result.add(en.nextElement());
		return result;
	}
	
	public Element[] getElementArray(String tagName)
	{
		Vector v = getElements(tagName);
		Element[] result = new Element[v.size()];
		v.toArray(result);
		return result;
	}
	
	
	public String[] getAllDataSourceNames()
	{
		Vector collect = new Vector();
		Enumeration sources = enumerateElements("data-source");
		while (sources.hasMoreElements()) {
			Element elm = (Element)sources.nextElement();
			String name = XMLUtil.getChildValue(elm, "display-name");
			if (name==null) name = XMLUtil.getChildValue(elm, "jndi-name");
			collect.add(name);
		}	
		
		String[] result = new String[collect.size()];
		collect.toArray(result);
		return result;
	}
	
	public Element getSchema(String schemaName)
	{
		Enumeration schemas = enumerateElements("SCHEMA");
		while (schemas.hasMoreElements())
		{
			Element sch = (Element)schemas.nextElement();
			String schName = XMLUtil.getChildValue(sch,"NAME");
			if (schName!=null && schName.equalsIgnoreCase(schemaName))
				return sch;
		}
		return null;
	}


	public static Element getTable(Element schema, String tableName)
	{
		NodeList tables = schema.getElementsByTagName("TABLE");
		for (int i=0; i<tables.getLength(); i++)
		{
			Element table = (Element)tables.item(i);
			String name = XMLUtil.getChildValue(table,"NAME");
			if (name!=null && name.equalsIgnoreCase(tableName))
				return table;
		}
		return null;
	}

	public int getSchemaVersion(String schemaName)
	{
		Element schema = getSchema(schemaName);
		if (schema==null) return Integer.MIN_VALUE;
		return XMLUtil.getChildIntValue(schema,"VERSION");
	}

	public int getTableVersion(String schemaName, String tableName)
	{
		Element schema = getSchema(schemaName);
		if (schema==null) return Integer.MIN_VALUE;
		Element table = getTable(schema,tableName);
		if (table==null) return Integer.MIN_VALUE;
		return XMLUtil.getChildIntValue(table,"VERSION");
	}

	public Element getDataSource(String databaseId)
	{
		Enumeration en = enumerateElements("data-source");
		while (en.hasMoreElements())
		{
			Element ds = (Element)en.nextElement();
			if (databaseId.equalsIgnoreCase(XMLUtil.getChildValue(ds,"jndi-name")) ||
				databaseId.equalsIgnoreCase(XMLUtil.getChildValue(ds,"display-name")))
				return ds;
		}
		return null;
	}

	public String getDefaultDataSource()
	{
		Enumeration en = enumerateElements("data-source");
		while (en.hasMoreElements())
		{
			Element ds = (Element)en.nextElement();
			if ("true".equalsIgnoreCase(ds.getAttribute("default")))
				return XMLUtil.getChildValue(ds,"display-name");
		}
		return null;
	}

	public String getDefaultWebBrowser(String os)
	{
        Enumeration enm = enumerateElements("web");
        while (enm.hasMoreElements())
        {
            Element webSettings = (Element)enm.nextElement();
            NodeList browsers = webSettings.getElementsByTagName("browser");
            for (int i=0; i < browsers.getLength(); i++) {
                Element browser = (Element)browsers.item(i);
                if (os.equalsIgnoreCase(browser.getAttribute("os")))
                    return XMLUtil.getTextValue(browser);
            }
        }
		return null;
	}

	public String getURL(String type)
	{
        Enumeration enm = enumerateElements("web");
        while (enm.hasMoreElements())
        {
            Element webSettings = (Element)enm.nextElement();
            String result = XMLUtil.getChildValue(webSettings,type);
            if (result!=null) return result;
        }
		return null;
	}

	public static File getFile(org.w3c.dom.Document doc)
	{
		Element root = doc.getDocumentElement();
		return getFile(root);
	}

	public static File getFile(org.w3c.dom.Element root)
	{
		String fileName = root.getAttribute("file-name");
		if (fileName==null)
			return null;
		else
			return new File(fileName);
	}

	public static void setFile(org.w3c.dom.Document doc, String fileName)
	{
		Element root = doc.getDocumentElement();
		if (fileName==null)
			root.removeAttribute("file-name");
		else
			root.setAttribute("file-name",fileName);
	}

	public static boolean isDirtyDocument(org.w3c.dom.Document doc)
	{
		Element docelm = doc.getDocumentElement();
		return isDirtyElement(docelm);
	}

	public static void setDirtyDocument(org.w3c.dom.Node node, boolean isDirty)
	{
		setDirtyDocument(node.getOwnerDocument(),isDirty);
	}

	public static void setDirtyDocument(org.w3c.dom.Document doc, boolean isDirty)
	{
		Element docelm = doc.getDocumentElement();
		setDirtyElement(docelm,isDirty);
	}

	public static boolean isDirtyElement(org.w3c.dom.Element elm)
	{
		return "true".equals(elm.getAttribute("dirty"));
	}

	public static void setDirtyElement(org.w3c.dom.Element elm, boolean isDirty)
	{
		if (isDirty)
			elm.setAttribute("dirty","true");
		else
			elm.removeAttribute("dirty");
	}

	public static void cleanAll(org.w3c.dom.Element elm)
	{
		setDirtyElement(elm,false);
		for (Node child = elm.getFirstChild(); child!=null; child = child.getNextSibling())
			if (child instanceof Element)
				cleanAll((Element)child);
	}

	public Document getDocument(String fileName, boolean create)
	{
		for (int i=0; i < roots.size(); i++)
		{
			Element rootElement = (Element) roots.get(i);
			Document doc = rootElement.getOwnerDocument();

			File xmlfile = getFile(doc);
			if (xmlfile!=null && fileName.equals(xmlfile.getName())) return doc;
		}
		if (create) {
			Document doc = null;
			try {
				doc = XMLUtil.newDocument();
			} catch (ParserConfigurationException e) {
				Application.error(e);
			}
			Element root = doc.createElement("APPLICATION_SETTINGS");
			doc.appendChild(root);
			setFile(doc,fileName);
			roots.add(root);
			return doc;
		}
		else
			return null;
	}

	public void writeDocument(org.w3c.dom.Document doc) throws TransformerException, IOException
	{
		File xmlfile = getFile(doc);
		Config.setDirtyDocument(doc,false);
		Config.setFile(doc,null);

		XMLUtil.print(doc,xmlfile);

		Config.setFile(doc,xmlfile.getAbsolutePath());

		//  update roots list
/*			for (int i=0; i < roots.size(); i++)
		{
			Element root = (Element)roots.get(i);
			if (getFile(root).equals(xmlfile)) {
				//  replace
				roots.set(i, doc.getDocumentElement());
			}
		}
*/
	}

    public String[] getPaths(String id)
    {
        ArrayList result = new ArrayList();
        Enumeration elms = enumerateElements("path");
        while (elms.hasMoreElements())
        {
            Element elm = (Element)elms.nextElement();
            if (id.equalsIgnoreCase(elm.getAttribute("id")))
                result.add(XMLUtil.getTextValue(elm));
        }
        return (String[])ListUtil.toArray(result,String.class);
    }

    public Element getFactoryLayout()
    {
        Element result = null;
        int max = -1;

        Enumeration layouts = enumerateElements("layout");
        while (layouts.hasMoreElements())
        {
            Element lay = (Element)layouts.nextElement();
            int ovr = Util.toint(lay.getAttribute("override"));
            if (ovr > max) {
                result = lay;
                max = ovr;
            }
        }
        return result;
    }

	public boolean deleteDocument(org.w3c.dom.Document doc)
	{
		File xmlfile = getFile(doc);
		return (xmlfile!=null) && xmlfile.delete();
	}

	public static void updateFile(Element elm) throws IOException, TransformerException {
		org.w3c.dom.Document doc = elm.getOwnerDocument();
		File target = getFile(doc);
		if (target==null) throw new IllegalArgumentException("no source file specified");

		File temp = File.createTempFile("temp",".xml", target.getParentFile());
		XMLUtil.print(doc,temp);
		FileUtil.restoreFrom(target, temp);
	}

	protected class ElementEnumeration
		implements Enumeration
	{
		protected String tagName;
		/**	current index in roots		 */
		protected int i1;
		/**	current child node		 */
		protected Node child;

		public ElementEnumeration(String tag)
		{
			tagName = tag;
			i1 = -1;
			child = null;
			fetchNext();
		}
		
		public boolean hasMoreElements()
		{
			return child != null;
		}
		
		public Object nextElement()
		{
			Object result = child;
			if (child != null) fetchNext();
			return result;
		}
		
		protected void fetchNext()
		{
			for (;;) {
				if (child!=null) {
					child = child.getNextSibling();
					while (child!=null) {
						if (tagName.equals(XMLUtil.getTagName(child)))
							return;
						child = child.getNextSibling();
					}
				}
				//	advance to next root
				i1++;
				if (i1 >= roots.size())
					return;	//	no more roots
				Element root = (Element)roots.get(i1);
				child = root.getFirstChild();
				if (child!=null && tagName.equals(XMLUtil.getTagName(child)))
					return;
			}
		}
	}


}
