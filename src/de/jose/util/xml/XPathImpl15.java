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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * XPath utilities for JDK 1.5
 * @author Peter Schäfer
 */

public class XPathImpl15 implements IXPathAdapter
{

	private XPathFactory xpf = XPathFactory.newInstance();
	private XPath xp = xpf.newXPath();

	/**
	 *  get one Node
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public Node selectSingleNode(Node contextNode, String path) throws TransformerException
	{
		try {
			return (Node)xp.evaluate(path,contextNode,XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new TransformerException(e);
		}
//		return XPathAPI.selectSingleNode(contextNode,path);
	}


	/**
	 * get a NodeList
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public NodeList selectNodeList(Node contextNode, String path) throws TransformerException
	{
		try {
			return (NodeList)xp.evaluate(path,contextNode,XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new TransformerException(e);
		}
//		return XPathAPI.selectNodeList(contextNode,path);
	}


	/**
	 * get a String value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public String stringValue(Node contextNode, String path) throws TransformerException
	{
		try {
			return (String)xp.evaluate(path,contextNode,XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			throw new TransformerException(e);
		}
//		XObject xobj = XPathAPI.eval(contextNode,path);
//		return (xobj!=null) ? xobj.str():null;
	}

	/**
	 * get a Double value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public double doubleValue(Node contextNode, String path) throws TransformerException
	{
		try {

			Number num = (Number)xp.evaluate(path,contextNode,XPathConstants.NUMBER);
			return (num!=null) ? num.doubleValue() : Double.MIN_VALUE;

		} catch (XPathExpressionException e) {
			throw new TransformerException(e);
		}
//		XObject xobj = XPathAPI.eval(contextNode,path);
//		return (xobj!=null) ? xobj.num():Double.MIN_VALUE;
	}

}
