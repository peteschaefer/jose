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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;


/** these packages are 1.4 specific !
 *  compile against xpath14-stub.jar
 *  runs only with 1.4 runtime
 *  */
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;


/**
 * XPath utilities for JDK 1.4 or earlier
 * @author Peter Schäfer
 */

public class XPathImpl14 implements IXPathAdapter
{
	/**
	 *  get one Node
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public Node selectSingleNode(Node contextNode, String path) throws TransformerException
	{
		return XPathAPI.selectSingleNode(contextNode,path);
	}


	/**
	 * get a NodeList
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public NodeList selectNodeList(Node contextNode, String path) throws TransformerException
	{
		return XPathAPI.selectNodeList(contextNode,path);
	}

	/**
	 * get a String value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public String stringValue(Node contextNode, String path) throws TransformerException
	{
		XObject xobj = XPathAPI.eval(contextNode,path);
		return (xobj!=null) ? xobj.str():null;
	}

	/**
	 * get a Double value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public double doubleValue(Node contextNode, String path) throws TransformerException
	{
		XObject xobj = XPathAPI.eval(contextNode,path);
		return (xobj!=null) ? xobj.num():Double.MIN_VALUE;
	}

}
