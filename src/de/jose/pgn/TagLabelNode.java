/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;



public class TagLabelNode
		extends StaticTextNode
{
	public static final int	PREFIX	 = 1;
	public static final int	SUFFIX	 = 2;
	public static final int	BETWEEN	 = 3;
	public static final int	LINEEND	=  4;

	protected int location;
	/**	associated Tag Node	*/
	protected TagNode tag1;
	/**	second associated Tag Node	*/
	protected TagNode tag2;


	protected TagLabelNode(String text, int location)
	{
		super(text,null,null);
		this.location = location;
	}

	protected void setTagNode(TagNode tag)						{ tag1 = tag; }
	protected void setTagNodes(TagNode tag1, TagNode tag2)		{ this.tag1 = tag1; this.tag2 = tag2; }

	public String toString()
	{
		if (isVisible())
			return text;
		else
			return "";
	}

	protected boolean attachTo(TagNode tag)
	{
		if ((tag==null) || tag.isEmpty()) return false;

		style = null;
		styleName = tag.getStyleName();
		altStyleName = tag.getAltStyleName();
		return true;
	}

	protected boolean isVisible()
	{
		switch (location) {
		case PREFIX:	//	visible, if associated TagNode is not empty
			if (tag1==null) tag1 = (TagNode)next(TAG_NODE);
			return attachTo(tag1);

		case SUFFIX:	//	visible, if associated TagNode ist not empty, and there is contents on this line
			if (tag1==null) tag1 = (TagNode)previous(TAG_NODE);
			if (!attachTo(tag1)) return false;

			for (Node nd=next(); nd!=null; nd=nd.next())
				switch (nd.type()) {
				case TAG_NODE:
						if (!((TagNode)nd).isEmpty()) return true;
						break;
				case STATIC_TEXT_NODE:
						if (nd instanceof TagLabelNode) {
							TagLabelNode lab = (TagLabelNode)nd;
							if (lab.location==LINEEND) return false;
						}
						break;
				default:
						return false;
				}
			return false;

		case BETWEEN:	//	visible, if both associated TagNodes are not empty
			if (tag1==null) tag1 = (TagNode)previous(TAG_NODE);
			if (tag2==null) tag2 = (TagNode)next(TAG_NODE);
			return attachTo(tag2) && attachTo(tag1);

		case LINEEND:	//	visible, if at least one TagNode is not empty
			style = null;
			altStyleName = "header";

			for (Node nd=previous(); nd!=null; nd=nd.previous())
				switch (nd.type()) {
				case TAG_NODE:
					if (!((TagNode)nd).isEmpty()) return true;
					break;
				case STATIC_TEXT_NODE:
					if (nd instanceof TagLabelNode) {
						TagLabelNode lab = (TagLabelNode)nd;
						if (lab.location==LINEEND) return false;
					}
					break;
				default:
					return true;
				}
			return false;
		}

		return true;
	}

}
