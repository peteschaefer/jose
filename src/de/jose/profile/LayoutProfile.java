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

package de.jose.profile;

import de.jose.view.JoComponent;
import de.jose.view.JoPanel;
import de.jose.view.JoSplitPane;
import de.jose.util.xml.XMLUtil;
import de.jose.Util;

import java.awt.*;
import java.io.Serializable;
import java.io.PrintWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *	serializable object that describes a Component Layout
 *	(either a JoPanel or a JoSplitPane)
 */

public class LayoutProfile
		implements Serializable
{
	/**	serial version UID	 */
	static final long serialVersionUID = 5316944584556446373L;

	public String name;
	public float weightX, weightY;
	public Point visibleLocation;
    public boolean showBorder;
	public Dimension size;

	public FrameProfile frameProfile;
	public String dockingPath;
	public FrameProfile dockFrame;

	public int orientation;
	public int dividerLocation;
	public LayoutProfile firstComponent;
	public LayoutProfile secondComponent;
	public boolean hide;	//	only set by factory; unused later

	public LayoutProfile(String nm)
	{
		this(nm, 1.0f,1.0f,true,true);
	}

	public LayoutProfile(String nm, float wghtx, float wghty, boolean border)
	{
		this(nm,wghtx,wghty,border,true);
	}

	public LayoutProfile(String nm, float wghtx, float wghty, boolean border, boolean visible)
	{
		name = nm;
		weightX = wghtx;
		weightY = wghty;
        showBorder = border;
		hide = !visible;
	}
	
	public LayoutProfile(int orient, 
						 LayoutProfile left, LayoutProfile right)
	{
		orientation = orient;
		dividerLocation = JoSplitPane.DIVIDE_WEIGHT;
		firstComponent = left;
		secondComponent = right;
		calcWeights();
	}

	
	protected LayoutProfile(JoComponent comp) {
		size = comp.getSize();
		name = comp.getName();
		weightX = comp.getWeightX();
		weightY = comp.getWeightY();
	}

	public LayoutProfile(JoSplitPane pane)
	{
		this((JoComponent)pane);
		orientation = pane.getOrientation();
		dividerLocation = pane.getDividerLocation();
		firstComponent = create(pane.firstComponent());
		secondComponent = create(pane.secondComponent());
        showBorder = false;
	}
	
	public static LayoutProfile create(Component comp)
	{
		LayoutProfile result;
		if (comp instanceof JoPanel)
			result = ((JoPanel)comp).getProfile();	//	Panels carry their own profile - always
		else if (comp instanceof JoSplitPane)
			result = new LayoutProfile((JoSplitPane)comp);
		else
			result = null;
		return result;
	}

	public boolean containsComponent(String name)
	{
		if (name.equals(this.name))
			 return true;
		if (firstComponent!=null && firstComponent.containsComponent(name))
			return true;
		if (secondComponent!=null && secondComponent.containsComponent(name))
			return true;
		return false;
	}

	public LayoutProfile getComponent(String name)
	{
		if (name.equals(this.name))
			return this;
		if (firstComponent!=null) {
			LayoutProfile result = firstComponent.getComponent(name);
			if (result!=null) return result;
		}
		if (secondComponent!=null) {
			LayoutProfile result = secondComponent.getComponent(name);
			if (result!=null) return result;
		}
		return null;
	}

	public LayoutProfile replaceComponent(String name, LayoutProfile profile)
	{
		if (name.equals(this.name))
			return profile;
		if (firstComponent!=null)
			firstComponent = firstComponent.replaceComponent(name,profile);
		if (secondComponent!=null)
			secondComponent = secondComponent.replaceComponent(name,profile);
		return this;
	}


	public String appendDockPath(String path, int pos)
	{
		if (path==null) path="";
		if (pos <= 1) {
			if (orientation==JoSplitPane.HORIZONTAL_SPLIT)
				return path+JoSplitPane.DOCK_WEST;
			else
				return path+JoSplitPane.DOCK_NORTH;
		}
		else {
			if (orientation==JoSplitPane.HORIZONTAL_SPLIT)
				return path+JoSplitPane.DOCK_EAST;
			else
				return path+JoSplitPane.DOCK_SOUTH;
		}
	}

	public boolean isPanelProfile() {
		return firstComponent==null;
	}

    public LayoutProfile(Element element)
    {
        String type = element.getAttribute("type");
        if ("view".equalsIgnoreCase(type))
        {
            Element weight = XMLUtil.getChild(element,"weight");

            name = element.getAttribute("name");
            weightX = (float)Util.todouble(weight.getAttribute("x"));
            weightY = (float)Util.todouble(weight.getAttribute("y"));
            showBorder = Util.toboolean(element.getAttribute("border"));
            hide = ! Util.toboolean(element.getAttribute("visible"));
        }
        else if ("split".equalsIgnoreCase(type))
        {
            if ("HORIZ".equalsIgnoreCase(element.getAttribute("orientation")))
                orientation = JoSplitPane.HORIZONTAL_SPLIT;
            else
                orientation = JoSplitPane.VERTICAL_SPLIT;
            dividerLocation = JoSplitPane.DIVIDE_WEIGHT;

            Element first = XMLUtil.getChild(element,"panel",0);
            Element second = XMLUtil.getChild(element,"panel",1);

            firstComponent = new LayoutProfile(first);
            secondComponent = new LayoutProfile(second);

            calcWeights();
        }
        else
            throw new IllegalArgumentException();
    }

    private void calcWeights() {
        if (orientation==JoSplitPane.HORIZONTAL_SPLIT) {
            weightX = firstComponent.weightX+secondComponent.weightX;
            weightY = Math.max(firstComponent.weightY,secondComponent.weightY);
        }
        else {
            weightX = Math.max(firstComponent.weightX,secondComponent.weightX);
            weightY = firstComponent.weightY+secondComponent.weightY;
        }
    }

    public void serializeXml(PrintWriter out)
    {
        if (isPanelProfile())
        {
            out.print   ("  <panel type='view' ");
            out.print   (" name='");
            out.print   (name);
            out.print   ("'");

            out.print   (" visible='");
            out.print   (!hide);
            out.print   ("'");

            out.print   (" border='");
            out.print   (showBorder);
            out.println ("'>");
/*
            if (size!=null) {
                out.print   ("      <size ");
                out.print   (" width='"); out.print(size.width);
                out.print   ("' height='"); out.print(size.height);
                out.println ("'/>");
            }
*/
            out.print   ("      <weight ");
            out.print   (" x='"); out.print(weightX);
            out.print   ("' y='"); out.print(weightY);
            out.println ("'/>");

            out.println ("   </panel>");
        }
        else
        {
            out.print   ("  <panel type='split' ");

            out.print   (" visible='");
            out.print   (!hide);
            out.println("'");

            out.print   ("  orientation='");
            if (orientation==JoSplitPane.HORIZONTAL_SPLIT)
                out.print("HORIZ");
            else
                out.print("VERT");
            out.println ("'>");

            firstComponent.serializeXml(out);
            secondComponent.serializeXml(out);

            out.println   ("  </panel>");
        }
    }

}
