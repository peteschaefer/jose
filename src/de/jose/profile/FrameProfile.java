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

import de.jose.window.JoFrame;
import de.jose.Util;
import de.jose.util.xml.XMLUtil;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * serializable object that describes a Frame Layout
 */

public class FrameProfile
		implements Serializable
{
	/**	serial version UID	 */
	static final long serialVersionUID = 6031831917184109065L;

    /** this constant is used to indicate the Help Window   */
    public static final int HELP_FRAME = -1;

	public Rectangle bounds;
	public Rectangle userBounds;
	public int state;
	public LayoutProfile componentLayout;

	private FrameProfile(Rectangle bnds, Rectangle userBnds,
						int aState,
						LayoutProfile compLayout)
	{
		bounds = bnds;
		userBounds = userBnds;
		state = aState;
		componentLayout = compLayout;
	}

	public FrameProfile(Rectangle bnds,
						int aState,
						LayoutProfile compLayout)
	{
		this(bnds,bnds,aState,compLayout);
	}

	public boolean containsComponent(String name)
	{
		return (componentLayout!=null) && componentLayout.containsComponent(name);
	}

	public LayoutProfile getComponentProfile(String name)
	{
		if (componentLayout==null)
			return null;
		else
			return componentLayout.getComponent(name);
	}

	protected void replace(String name, LayoutProfile newProfile)
	{
		if (componentLayout!=null)
			componentLayout = componentLayout.replaceComponent(name,newProfile);
	}

	public static LayoutProfile vsplit(LayoutProfile top, LayoutProfile bottom)
	{
		return new LayoutProfile(JSplitPane.VERTICAL_SPLIT,top,bottom);
	}

	public static LayoutProfile hsplit(LayoutProfile left, LayoutProfile right)
	{
		return new LayoutProfile(JSplitPane.HORIZONTAL_SPLIT,left,right);
	}

    public static void serializeXml(Vector profiles, PrintWriter out)
    {
        out.println("<layout>");

        for (Iterator i = profiles.iterator(); i.hasNext(); )
            ((FrameProfile)i.next()).serializeXml(out);

        out.println("</layout>");
    }


    public static Vector deserializeXml(Element element)
    {
        Vector result = new Vector();
        String id = element.getAttribute("id");
        int override = Util.toint(element.getAttribute("override"));

        NodeList frames = element.getElementsByTagName("frame");
        for (int i=0; i < frames.getLength(); i++)
        {
            Element frame = (Element)frames.item(i);
            FrameProfile profile = new FrameProfile(frame);
            result.add(profile);
        }
        return result;
    }

    public FrameProfile(Element element)
    {
        Element bounds = XMLUtil.getChild(element,"bounds");
//        Element user_bounds = XMLUtil.getChild(element,"user-bounds");
        Element state = XMLUtil.getChild(element,"state");

        this.bounds = new Rectangle(
                Util.toint(bounds.getAttribute("x")),
                Util.toint(bounds.getAttribute("y")),
                Util.toint(bounds.getAttribute("width")),
                Util.toint(bounds.getAttribute("height")));
        this.userBounds = new Rectangle(this.bounds);

        this.state = 0;
        if (state.hasAttribute("HELP"))
            this.state = HELP_FRAME;
        else {
            if (state.hasAttribute("VISIBLE"))  this.state |= JoFrame.VISIBLE;
            if (state.hasAttribute("ICONIFIED"))  this.state |= JoFrame.ICONIFIED;
            if (state.hasAttribute("FULLSCREEN"))  this.state |= JoFrame.FULLSCREEN;
            if (state.hasAttribute("MAXIMIZED_BOTH"))  this.state |= JoFrame.MAXIMIZED_BOTH;
            if (state.hasAttribute("MAXIMIZED_HORIZ"))  this.state |= JoFrame.MAXIMIZED_HORIZ;
            if (state.hasAttribute("MAXIMIZED_VERT"))  this.state |= JoFrame.MAXIMIZED_VERT;
        }

        Element component = XMLUtil.getChild(element,"panel");
        if (component!=null)
            this.componentLayout = new LayoutProfile(component);
    }

    public void serializeXml(PrintWriter out)
    {
        out.println ("  <frame>");

        out.print   ("  <bounds ");
        out.print   ("  x='"); out.print(bounds.x);
        out.print   ("'  y='"); out.print(bounds.y);
        out.print   ("'  width='"); out.print(bounds.width);
        out.print   ("'  height='"); out.print(bounds.height);
        out.println("'/>");
/*
        out.print   ("  <user-bounds ");
        out.print   ("  x='"); out.print(userBounds.x);
        out.print   ("'  y='"); out.print(userBounds.y);
        out.print   ("'  width='"); out.print(userBounds.width);
        out.print   ("'  height='"); out.print(userBounds.height);
        out.println("'/>");
*/
        out.print   ("  <state ");
            if (state==HELP_FRAME)
                out.print(" HELP='1' ");  //  indicates help frame; state is managed by Help System
            else {
                if (Util.allOf(state,JoFrame.VISIBLE))
                    out.print(" VISIBLE='1' ");
                if (Util.allOf(state,JoFrame.ICONIFIED))
                    out.print(" ICONIFIED='1' ");
                if (Util.allOf(state,JoFrame.FULLSCREEN))
                    out.print(" FULLSCREEN='1' ");
                if (Util.allOf(state,JoFrame.MAXIMIZED_BOTH))
                    out.print(" MAXIMIZED_BOTH='1' ");
                else if (Util.allOf(state,JoFrame.MAXIMIZED_HORIZ))
                    out.print(" MAXIMIZED_HORIZ='1' ");
                else if (Util.allOf(state,JoFrame.MAXIMIZED_VERT))
                    out.print(" MAXIMIZED_VERT='1' ");
            }
        out.println  ("/>");

        if (componentLayout!=null)
            componentLayout.serializeXml(out);

        out.println ("   </frame>");
    }

	/**
	 * the initial "factory" window layout
	 */
	
	public static final LayoutProfile PANEL_LAYOUT_BOARD =          new LayoutProfile("window.board",3.0f,2.0f,true);

	public static final LayoutProfile PANEL_LAYOUT_CLOCK =          new LayoutProfile("window.clock",1.0f,0.4f,true);
	public static final LayoutProfile PANEL_LAYOUT_GAME =           new LayoutProfile("window.game",1.0f,2.0f,true);
	public static final LayoutProfile PANEL_LAYOUT_EVAL =           new LayoutProfile("window.eval",1.0f,0.2f,true,false);
	public static final LayoutProfile PANEL_LAYOUT_ENGINE =			new LayoutProfile("window.engine",1.0f,0.3f,true,false);

	public static final LayoutProfile PANEL_LAYOUT_CONSOLE =        new LayoutProfile("window.console",1.0f,1.0f,true);

	public static final LayoutProfile PANEL_LAYOUT_COLLECTION = 	new LayoutProfile("window.collectionlist",1.0f,1.0f,true);
	public static final LayoutProfile PANEL_LAYOUT_QUERY =	        new LayoutProfile("window.query",1.0f,1.0f,true);
	public static final LayoutProfile PANEL_LAYOUT_GAMELIST =       new LayoutProfile("window.gamelist",1.0f,1.6f,true);

	public static final LayoutProfile PANEL_LAYOUT_PREVIEW =        new LayoutProfile("window.print.preview",1.0f,1.0f,true);

	public static final LayoutProfile PANEL_LAYOUT_TOOL_1 =         new LayoutProfile("window.toolbar.1",0.0f,0.0f,false);
	public static final LayoutProfile PANEL_LAYOUT_TOOL_2 =	        new LayoutProfile("window.toolbar.2",0.0f,0.0f,false);
	public static final LayoutProfile PANEL_LAYOUT_SYMBOLS =        new LayoutProfile("window.toolbar.symbols",1.0f,1.0f,false);

	public static final FrameProfile FRAME_LAYOUT_HELP =
	        new FrameProfile(new Rectangle(100,100,-100,-140), HELP_FRAME, null);
	
	public static final FrameProfile FRAME_LAYOUT_MAIN =
	        new FrameProfile(new Rectangle(20,40,-4,-40), JoFrame.VISIBLE,
				  hsplit(
				        vsplit(PANEL_LAYOUT_TOOL_1,PANEL_LAYOUT_BOARD),
						vsplit(PANEL_LAYOUT_CLOCK,
							vsplit(PANEL_LAYOUT_GAME,
								vsplit(PANEL_LAYOUT_EVAL, PANEL_LAYOUT_ENGINE)))));
	
	public static final FrameProfile FRAME_LAYOUT_DATABASE = 
	        new FrameProfile(new Rectangle(20,60,-4,-40), 0,
				vsplit(
					hsplit(
				        vsplit(PANEL_LAYOUT_COLLECTION, PANEL_LAYOUT_TOOL_2),
						PANEL_LAYOUT_QUERY),
					PANEL_LAYOUT_GAMELIST));
	
	public static final FrameProfile FRAME_LAYOUT_PREVIEW =
	        new FrameProfile(new Rectangle(40,40,-40,-40), 0,
		        PANEL_LAYOUT_PREVIEW);
	
	public static final FrameProfile FRAME_LAYOUT_CONSOLE =
	        new FrameProfile(new Rectangle(20,80, 480,260), 0,
				PANEL_LAYOUT_CONSOLE);
	
	public static final FrameProfile FRAME_LAYOUT_SYMBOLS =
	        new FrameProfile(new Rectangle(20,100, 400,560), 0,
				PANEL_LAYOUT_SYMBOLS);
	
	public static final FrameProfile[] FACTORY_LAYOUT = {
		/** this profile is used for the Help Window
		 *  the Help window is actually controlled by HelpBroker
		 *  but we use this entry to store the window location.
		 */
		FRAME_LAYOUT_HELP,
		FRAME_LAYOUT_MAIN,
		FRAME_LAYOUT_DATABASE,
		FRAME_LAYOUT_PREVIEW,
		FRAME_LAYOUT_CONSOLE,
		FRAME_LAYOUT_SYMBOLS,
	};
}
