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

package de.jose.util.xml;

import de.jose.Version;
import de.jose.util.ReflectionUtil;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.transform.TransformerException;

/**
 * @author Peter Schäfer
 */

public class XPathUtil
{
	private static IXPathAdapter impl;

	static {
		if (Version.java15orLater)
			impl = new XPathImpl15();
		else
			impl = new XPathImpl14();
	}

	/**
	 *  get one Node
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static Node getNode(Node contextNode, String path) throws TransformerException
	{
		return impl.selectSingleNode(contextNode,path);
	}

	/**
	 * get one Element node
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static Element getElement(Node contextNode, String path) throws TransformerException
	{
		return (Element)getNode(contextNode,path);
	}

	/**
	 * get a NodeList
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static NodeList getList(Node contextNode, String path) throws TransformerException
	{
		return impl.selectNodeList(contextNode,path);
	}

	public static String[] getStringArray(Node contextNode, String path) throws TransformerException
	{
		NodeList nodes = getList(contextNode,path);
		String[] result = new String[nodes.getLength()];
		for (int i=0; i < result.length; i++)
			result[i] = XMLUtil.getTextValue(nodes.item(i));
		return result;
	}

	/**
	 * get a String value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static String getString(Node contextNode, String path) throws TransformerException
	{
		String str = impl.stringValue(contextNode,path);

		str = str.trim();
		if (str.length()==0) return null;

		return str;
	}

	/**
	 * get a Double value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static double getDouble(Node contextNode, String path) throws TransformerException
	{
		return impl.doubleValue(contextNode,path);
	}

	/**
	 * get an int value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static int getInt(Node contextNode, String path) throws TransformerException
	{
		return (int)impl.doubleValue(contextNode,path);
	}

	/**
	 * test for existance
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public static boolean exists(Node contextNode, String path) throws TransformerException
	{
		return getNode(contextNode,path) != null;
	}
}
