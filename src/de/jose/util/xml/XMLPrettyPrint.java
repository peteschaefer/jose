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

import org.w3c.dom.*;
import de.jose.util.StringUtil;

/**
 * @author Peter Schäfer
 */

public class XMLPrettyPrint
{
	public static Element pretty(Element from, int indent)
	{
		Element to = copyElement(from);

		/** copy children   */
		NodeList list = from.getChildNodes();
		boolean split = false;

		String text = getSingleText(list);
		if (text!=null) // single text line
		{
			text(to," ");
			text(to,text);
			text(to," ");
		}
		else for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			switch (node.getNodeType())
			{
			case Node.TEXT_NODE:
					text = node.getNodeValue();
					text = text.trim();
					if (!StringUtil.isWhitespace(text)) {
						if (!split) newLine(to);
						split = true;

						tabs(to,indent+1);
						text(to,text);
						newLine(to);
					}
					break;

			case Node.COMMENT_NODE:
					if (!split) newLine(to);
					split = true;

					tabs(to,indent+1);
					comment(to,((Comment)node).getNodeValue());
					newLine(to);
					break;

			case Node.ELEMENT_NODE:
					if (!split) newLine(to);
					split = true;

					tabs(to,indent+1);
					to.appendChild(pretty((Element)node,indent+1));
					newLine(to);
					break;
			}

		}

		if (split && indent > 0)
			tabs(to,indent);

		return to;
	}


	private static String getSingleText(NodeList list)
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<list.getLength(); i++)
		{
			Node node = list.item(i);
			switch (node.getNodeType())
			{
			case Node.TEXT_NODE:
					String text = node.getNodeValue();
					buf.append(text);
					break;
			case Node.COMMENT_NODE:
			case Node.ELEMENT_NODE:
					return null;
			}
		}
		String result = buf.toString();
		if (StringUtil.isWhitespace(result))
			return null;
		else
			return result.trim();
	}

	private static Element copyElement(Element node)
	{
		Element result = node.getOwnerDocument().createElement(node.getTagName());
		copyAttributes(node,result);
		return result;
	}

	private static void newLine(Element node)
	{
		text(node,"\n");
	}

	private static void tabs(Element node, int count)
	{
		text(node,StringUtil.blanks(count,'\t'));
	}

	private static void text(Element node, String value)
	{
		node.appendChild(node.getOwnerDocument().createTextNode(value));
	}

	private static void comment(Element node, String value)
	{
		node.appendChild(node.getOwnerDocument().createComment(value));
	}

	private static void copyAttributes(Element from, Element to)
	{
		NamedNodeMap attrs = from.getAttributes();
		for (int i=0; i < attrs.getLength(); i++)
		{
			Attr attr = (Attr)attrs.item(i);
			to.setAttribute(attr.getName(),attr.getValue());
		}
	}
}
