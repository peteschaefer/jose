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

package de.jose.util.xml;

import de.jose.Application;
import de.jose.Util;
import de.jose.Version;
import de.jose.task.io.XMLExport;
import de.jose.task.io.XSLFOExport;
import de.jose.export.ExportConfig;
import de.jose.util.StringUtil;
import de.jose.util.SoftCache;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.AttributeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.AccessControlException;
import java.util.Date;

public class XMLUtil
{

	/**
	 * @return true if a child exists with a certain name
	 */
	public static final boolean existsChild(Element el, String childName)
	{
		for (Node child = el.getFirstChild(); child!=null; child = child.getNextSibling())
			if (childName.equals(getTagName(child))) return true;
		return false;
	}

	public static String getTagName(Node node)
	{
		if (node instanceof Element)
			return ((Element)node).getTagName();
		else
			return null;
	}

	/**
	 * @return the number of children matching a certain name
	 */
	public static final int countChildren(Element el, String childName)
	{
		int result = 0;
		for (Node child = el.getFirstChild(); child!=null; child = child.getNextSibling())
			if (childName.equals(getTagName(child))) result++;
		return result;
	}
	
	/**
	 * @param n index of child (starting at 0)
	 * @return the n-th child element with a given name
	 */
	public static final Element getChild(Element parent, String childName, int n)
	{
		for (Node child = parent.getFirstChild(); child!=null; child = child.getNextSibling())
			if (childName.equals(getTagName(child)) && n--==0)
				return (Element)child;
		return null;
	}
	
	/**
	 * @param childName name of the child
	 * @return the child element with a given name
	 */
	public static final Element getChild(Element parent, String childName)
	{
		return getChild(parent,childName,0);
	}

	/**
	 * @param childName name of the child
	 * @return the child element with a given name
	 */
	public static final Element getChild(Element parent, String childName, boolean create)
	{
		Element result = getChild(parent,childName);
		if (result==null && create)
			result = appendChild(parent,childName);

		return result;
	}

	/**
	 * @param childName name of the child
	 * @return the child element with a given name
	 */
	public static final Element appendChild(Element parent, String childName)
	{
		Element result = parent.getOwnerDocument().createElement(childName);
		parent.appendChild(result);
		return result;
	}

	/**
	 * @return the String value of the first child with a certain name
	 */
	public static final String getChildValue(Element el, String childName, int n)
	{
		Element child = getChild(el,childName,n);
		if (child==null)
			return null;
		else
			return getTextValue(child);
	}
	
	/**
	 * @return the String value of the first child with a certain name
	 */
	public static final String getChildValue(Element el, String childName)
	{
		return getChildValue(el,childName,0);
	}
	
	/**
	 * @return a child node's attribute value; null if not found
	 */
	public static final String getChildAttributeValue(Element el, String childName, int n, String attributeName)
	{
		Element child = getChild(el,childName,n);
		if (child==null)
			return null;
		else
			return child.getAttribute(attributeName);
	}
	
	/**
	 * @return a child node's attribute value; null if not found
	 */
	public static final String getChildAttributeValue(Element el, String childName, String attributeName)
	{
		return getChildAttributeValue(el,childName,0,attributeName);													  
	}
	
	/**
	 * @return te integer value of the first child with a certain name
	 */
	public static final int getChildIntValue(Element el, String childName, int n)
	{
		return intValue(getChildValue(el,childName,n), Integer.MIN_VALUE);
	}

	public static final boolean getChildBooleanAttributeValue(Element el, String childName, String attributeName)
	{
		return booleanValue(getChildAttributeValue(el,childName,attributeName),Boolean.FALSE);
	}

	public static final int getIntAttribute(Element el, String attrName, int nullValue)
	{
		String text = el.getAttribute(attrName);
		return intValue(text,nullValue);
	}

	public static final boolean getBooleanAttribute(Element el, String attrName, boolean nullValue)
	{
		String text = el.getAttribute(attrName);
		return booleanValue(text,nullValue);
	}

	/**
	 * @return te integer value of the first child with a certain name
	 */
	public static final int getChildIntValue(Element el, String childName)
	{
		return getChildIntValue(el,childName,0);
	}

	/**
	 * @return te integer value of the first child with a certain name
	 */
	public static final double getChildDoubleValue(Element el, String childName, int n)
	{
		return doubleValue(getChildValue(el,childName,n), Integer.MIN_VALUE);
	}

	/**
	 * @return te integer value of the first child with a certain name
	 */
	public static final double getChildDoubleValue(Element el, String childName)
	{
		return getChildDoubleValue(el,childName,0);
	}

	public static final String toString(Node node)
	{
		try {
			Transformer tf = getTransformer("empty",null);
			Source src = new DOMSource(node);
			StringWriter sw = new StringWriter();
			Result dst = new StreamResult(sw);
			tf.transform(src,dst);
			releaseTransformer("empty",tf);
			return sw.toString();
		} catch (TransformerException e) {
			Application.warning(e);
			return node.toString();
		}
	}

	/**
	 * @return the text value of a node
	 */
	public static final String getTextValue(Node node)
	{
		StringBuffer buf = new StringBuffer();

		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			buf.append(child.getNodeValue());

		StringUtil.trim(buf,StringUtil.TRIM_BOTH);
		if (buf.length()==0)
			return null;
		else
			return buf.toString();
	}

	public static boolean setAttribute(Element node, String attr, String newValue)
	{
		String oldValue = node.getAttribute(attr);
		if (newValue.equals(oldValue))
			return false;
		//else {
		node.setAttribute(attr, newValue);
		return true;
	}

	public static boolean removeAttribute(Element node, String attr)
	{
		String oldValue = node.getAttribute(attr);
		if (oldValue==null)
			return false;
		//	else
		node.removeAttribute(attr);
		return true;
	}

	public static boolean setTextValue(Node node, Object value)
	{
		String oldValue = getTextValue(node);
		String newValue = Util.toString(value);

		if (StringUtil.isWhitespace(oldValue)) oldValue = null;
		if (StringUtil.isWhitespace(newValue)) newValue = null;

		if (Util.equals(oldValue,newValue))
			return false;   //  nothing changed

		if (newValue==null)
			return removeAllChildren(node);
		else {
			removeAllChildren(node);

			Node textNode = node.getOwnerDocument().createTextNode(newValue);
			node.appendChild(textNode);
			return true;    //  was modified
		}
	}

	public static boolean removeAllChildren(Node node)
	{
		Node child = node.getFirstChild();
		if (child==null)
			return false;   //  already empty
		
		while (child != null)
		{
			Node nextChild = child.getNextSibling();
			node.removeChild(child);
			child = nextChild;
		}
		return true;
	}

	public static final int getIntValue(Node node)
	{
		return intValue(getTextValue(node), Integer.MIN_VALUE);
	}
	
	public static final int intValue(String text, int nullValue)
	{
		if (text==null)
			return nullValue;
		else
			return Util.toint(text);
	}

	public static final double doubleValue(String text, double nullValue)
	{
		if (text==null)
			return nullValue;
		else
			return Util.todouble(text);
	}

	public static final boolean booleanValue(String text, boolean nullValue)
	{
		if (text==null)
			return nullValue;
		else
			return Util.toboolean(text);
	}

    /**
     * available XML,XSL implementations
     */
    public static final int CAUCHO          = 1;
    public static final int XERCES          = 2;
    public static final int XALAN           = 3;

    public static final boolean preferImplementation(int impl)
    {
        switch (impl) {
        case CAUCHO:
            try {
                Class.forName("com.caucho.xml.parsers.XmlDocumentBuilderFactory");
                Class.forName("com.caucho.xml.parsers.XmlSAXParserFactory");
                Class.forName("com.caucho.xsl.Xsl");
            } catch (ClassNotFoundException cnfex) {
                return false;
            }
            try {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory",  "com.caucho.xml.parsers.XmlDocumentBuilderFactory");
                System.setProperty("javax.xml.parsers.SAXParserFactory",        "com.caucho.xml.parsers.XmlSAXParserFactory");
                System.setProperty("javax.xml.transform.TransformerFactory",    "com.caucho.xsl.Xsl");
            } catch (AccessControlException acex) {
                //	restrictive security ;-(
                return false;
            }
            return true;

        case XERCES:
            try {
                Class.forName("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
                Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl");
            } catch (ClassNotFoundException cnfex) {
                return false;
            }
            try {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory",  "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
                System.setProperty("javax.xml.parsers.SAXParserFactory",        "org.apache.xerces.jaxp.SAXParserFactoryImpl");
            } catch (AccessControlException acex) {
                //	restrictive security ;-(
                return false;
            }
            return true;

        case XALAN:
            try {
                Class.forName("org.apache.xalan.processor.TransformerFactoryImpl");
            } catch (ClassNotFoundException cnfex) {
                return false;
            }
            try {
                System.setProperty("javax.xml.transform.TransformerFactory",    "org.apache.xalan.processor.TransformerFactoryImpl");
            } catch (AccessControlException acex) {
                //	restrictive security ;-(
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * get the XML parser factory
     */
    protected static DocumentBuilderFactory gDomFactory = null;
    /**
     * the default DOM parser
     */
    protected static DocumentBuilder gDomBuilder = null;

	/** XSL transformer factory */
	protected static TransformerFactory gTransformerFactory = null;
	/** cache with transformers */
	protected static SoftCache gTransformers = new SoftCache();


    public static DocumentBuilderFactory getDocumentBuilderFactory()
    {
        if (gDomFactory==null) {
            gDomFactory = DocumentBuilderFactory.newInstance();
	        gDomFactory.setValidating(false);
        }
        return gDomFactory;
    }

    public static DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        if (gDomBuilder==null) {
            gDomBuilder = getDocumentBuilderFactory().newDocumentBuilder();
        }
        return gDomBuilder;
    }

	public static TransformerFactory getTransformerFactory()
	{
		if (gTransformerFactory==null)
			gTransformerFactory = TransformerFactory.newInstance();
		return gTransformerFactory;
	}

	public static Transformer getTransformer(Object hash, Source xsl) throws TransformerConfigurationException
	{
		Transformer tf = (Transformer)gTransformers.pop(hash);
		if (tf==null) {
			if (xsl==null)
				tf = getTransformerFactory().newTransformer();
			else
				tf = getTransformerFactory().newTransformer(xsl);
		}
		return tf;
	}

	public static Transformer getTransformer(File xsl) throws TransformerConfigurationException
	{
		StreamSource source = new StreamSource(xsl);
		Date fileMod = new Date(xsl.lastModified());
		for (;;) {
			Transformer tf = getTransformer(xsl,source);
			//   check file mod. date
			Date lastMod = (Date)tf.getParameter("file_last_mod");
			if (lastMod== null || ! lastMod.before(fileMod)) {
				tf.setParameter("file_last_mod",fileMod);
				return tf;
			}
			// else continue;       //  file has changed on disk
		}
	}

	public static void releaseTransformer(Object hash, Transformer tf)
	{
		gTransformers.push(hash,tf,false);
	}

	/**
	 * clear cached transformers (e.g. when they change on disk)
	 */
	public static void clearTransformers() {
		gTransformers.clear();
	}

	/**
	 * insert a DOM document into a SAX stream
	 */
	public static void insertDOMintoSAX(Document dom, ContentHandler sax) throws TransformerException
	{
		//  get a pass-throught transformer
		Transformer tf = XMLUtil.getTransformer("empty",null);

		//  use DOM source
		Source source = new DOMSource(dom);
		//  pipe to SAX result
		Result result = new SAXResult(sax);
		//  pump it
		tf.transform(source,result);

		XMLUtil.releaseTransformer("empty",tf);
	}


    public static Document parse(InputSource source)
        throws ParserConfigurationException, SAXException, IOException
    {
        return getDocumentBuilder().parse(source);
    }


	public static Document newDocument() throws ParserConfigurationException
	{
		return getDocumentBuilder().newDocument();
	}

    public static Document parse(File file)
        throws ParserConfigurationException, SAXException, IOException
    {
	    InputSource source = new InputSource(new FileReader(file));
        return getDocumentBuilder().parse(source);
    }

    public static Document parse(InputStream stream)
        throws ParserConfigurationException, SAXException, IOException
    {
	    InputSource source = new InputSource(new InputStreamReader(stream));
        return getDocumentBuilder().parse(source);
    }

	public static Document parse(Reader stream)
	    throws ParserConfigurationException, SAXException, IOException
	{
		InputSource source = new InputSource(stream);
	    return getDocumentBuilder().parse(source);
	}


	public static boolean isEmpty(Document doc)
	{
		Element elm = doc.getDocumentElement();
		return (elm==null) || isEmpty(elm);
	}

	public static boolean isEmpty(Element elm)
	{
//		NamedNodeMap attrs = elm.getAttributes();
//		if (attrs.getLength() > 0) return false;

		NodeList children = elm.getChildNodes();
		for (int i=0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			switch (child.getNodeType())
			{
			case Node.TEXT_NODE:
					String text = child.getNodeValue();
					if (!StringUtil.isWhitespace(text)) return false;
					break;
			case Node.ELEMENT_NODE:
					return false;
			}
		}
		return true;
	}


	public static String escapeText(String text)
	{
		StringBuffer buf = new StringBuffer(text);
		for (int i = buf.length()-1; i >= 0; i--)
			switch (buf.charAt(i)) {
			case '&':   buf.replace(i,i+1,"&amp;"); break;
			case '\'':  buf.replace(i,i+1,"&#39;"); break;
			case '"':   buf.replace(i,i+1,"&quot;"); break;
			case '<':   buf.replace(i,i+1,"&lt;"); break;
			case '>':   buf.replace(i,i+1,"&gt;"); break;
			}
		return buf.toString();
	}

	public static boolean setChildValue(Element parent, String childName, Object value)
	{
		boolean modified = false;
		Element child = getChild(parent,childName,false);
		if (child==null) {
			child = getChild(parent,childName,true);
			modified = true;
		}

		if (setTextValue(child,value)) 
			modified = true;
		return modified;
	}


	public static void print(Document doc, File out) throws TransformerException, IOException
	{
		print(doc, new FileWriter(out));
	}

	public static void print(Document doc, Writer out) throws TransformerException
	{
		Transformer tf = getTransformer("empty",null);
		Source src = new DOMSource(doc.getDocumentElement());
		Result dst = new StreamResult(out);
		tf.transform(src,dst);
		releaseTransformer("empty",tf);
	}

	public static Source getXslSource(String name) throws IOException, ParserConfigurationException, SAXException
	{
		File file = new File(Application.theWorkingDirectory,"xsl"+File.separator+name+".xsl");
		return getXslSource(file);
	}

	private static Source getXslSource(File file) throws IOException, ParserConfigurationException, SAXException
	{
		Source src = (Source)SoftCache.gInstance.get(file);
		if (src==null) {
			Document doc = parse(file);
			src = new DOMSource(doc);
			SoftCache.gInstance.put(file,src);
		}
		return src;
	}

	public static Node transform(Node node, Source xslSource, boolean in_place) throws TransformerException
	{
		Transformer trans = getTransformer("empty",null);

		Source input = new DOMSource(node);
//		Node tempNode = node.getOwnerDocument().createElement("temp");
		Document doc = node.getOwnerDocument();
		DocumentFragment fragment = doc.createDocumentFragment();
		DOMResult output = new DOMResult(fragment);

		trans.transform(input,output);

		releaseTransformer("empty",trans);

		Node result = output.getNode().getFirstChild();
		if (in_place)
			node.getParentNode().replaceChild(result,node);
		return result;
	}

	public static Node transform(Node input, File xslFile, boolean in_place) throws IOException, ParserConfigurationException, SAXException, TransformerException
	{
		return transform(input,getXslSource(xslFile),in_place);
	}

	public static Node transform(Node input, String xslFile, boolean in_place) throws IOException, ParserConfigurationException, SAXException, TransformerException
	{
		return transform(input,getXslSource(xslFile),in_place);
	}

	/**
	 * command line tool for performing XSL transformations (including FO)
	 * @param args
	 */
	public static void main(String[] args)
	{
		try {

			File input = null;
			File xsl = null;
			File output = null;
			boolean fo = false;

			for (int i=0; i<args.length; i++)
				if (args[i].equalsIgnoreCase("-fo"))
					fo = true;
				else if (input==null)
					input = new File(args[i]);
				else if (xsl==null)
					xsl = new File(args[i]);
				else if (output==null)
					output = new File(args[i]);
				else
					System.err.println("unexpected parameter: "+args[i]);

			if (input==null || xsl==null || output==null)
			{
				System.err.println("java -cp jose.jar;lib/fop-plus.jar de.jose.util.xml.XMLUtil <input.xml> <transform.xsl> <output>");
				System.err.println("");
				System.err.println("Command line tool for performing XSL transformations.");
				System.err.println("");
				System.err.println("To perform FO transformations, specify -fo");
				System.err.println("Output files are detected by file extension (e.g. pdf).");
				return;
			}

			Source source = new StreamSource(input);

			if (fo)
			{
				//  XSL-FO transformation with FOP (optional)
				OutputStream outputStream = new FileOutputStream(output);
				XSLFOExport.process(source, xsl, outputStream,
				            output.getName(), null, true);
				outputStream.close();
			}
			else
			{
				//  ordinary XSLT transformation
				Result result = new StreamResult(output);
				XMLExport.process(source, xsl, result);
			}

		} catch (Throwable e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

    public static Element createElement(Document doc, String name, AttributeList attributes)
    {
        Element elem = doc.createElement(name);
        if (attributes!=null)
            for (int i=0; i < attributes.getLength(); i++)
            {
                String key = attributes.getName(i);
                String value = attributes.getValue(i);
                elem.setAttribute(key,value);
            }
        return elem;
    }

	public static void removeElement(Element elem, boolean trimLeadingSpace, boolean trimTrailingSpace)
	{
		if (trimLeadingSpace)
		{
			Node ws = elem.getPreviousSibling();
			while (ws!=null && ws.getNodeType()==Node.TEXT_NODE && getTextValue(ws)==null)
			{
				Node rem = ws;
				ws = ws.getPreviousSibling();
				rem.getParentNode().removeChild(rem);
}
		}

		if (trimTrailingSpace)
		{
			Node ws = elem.getNextSibling();
			while (ws!=null && ws.getNodeType()==Node.TEXT_NODE && getTextValue(ws)==null)
			{
				Node rem = ws;
				ws = ws.getNextSibling();
				rem.getParentNode().removeChild(rem);
			}
		}

		elem.getParentNode().removeChild(elem);
	}
}
