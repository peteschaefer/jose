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
 * Common Interface for XPathUtil14 and 15
 * @author Peter Schäfer
 */

public interface IXPathAdapter
{

	/**
	 *  get one Node
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public Node selectSingleNode(Node contextNode, String path) throws TransformerException;

	/**
	 * get a NodeList
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public NodeList selectNodeList(Node contextNode, String path) throws TransformerException;

	/**
	 * get a String value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public String stringValue(Node contextNode, String path) throws TransformerException;

	/**
	 * get a Double value
	 * @param contextNode
	 * @param path
	 * @return
	 */
	public double doubleValue(Node contextNode, String path) throws TransformerException;


}
