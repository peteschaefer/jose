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

package de.jose.help;

import de.jose.Application;
import de.jose.window.JoFrame;

import javax.help.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 *  Wrapper for JavaHelp stuff: HelpSet and HelpBroker, in particular
 *
 *  @author Peter Schäfer
 */

public class HelpSystem
{
    /** the Java Help set   */
    public HelpSet      set;
    /** the Help Broker */
    public HelpBroker   broker;

    /** default help set file  */
    protected File hsFile;
    /** window bounds   */
    protected Rectangle windowBounds;

    public HelpSystem(File workDir, String fileName, Rectangle bounds)
    {
        this(new File(workDir,fileName), bounds);
    }

    public HelpSystem(File helpFile, Rectangle bounds)
    {
        hsFile = helpFile;
		windowBounds = bounds;
        /** initialized on demand   */
    }

    /**
     * @return bounds of the Help window
     */
    public Rectangle getWindowBounds() {
        if (broker!=null)
            windowBounds = new Rectangle(broker.getLocation(), broker.getSize());
        return windowBounds;
    }

    /**
     * close Help window
     */
    public void close()
    {
        if (broker!=null) broker.setDisplayed(false);
    }

	public static HelpSet openHelpSet(File file)
		throws IOException, HelpSetException
	{
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());

		String fileStr = file.getCanonicalPath();
		fileStr = fileStr.replace('\\','/');
		URL hsURL = new URL("file",null,fileStr);
		return new HelpSet(null,hsURL);
	}

    /**
     * initialize Help system, if necessary
     * @throws Exception
     */
    public void init() throws Exception
    {
        if (set==null)
			set = openHelpSet(hsFile);
        if (broker==null) {
            broker = set.createHelpBroker("MainWindow");
            broker.initPresentation();

            JoFrame.adjustBounds(windowBounds,true);
            broker.setLocation(windowBounds.getLocation());
            broker.setSize(windowBounds.getSize());
        }
    }

    /**
     * show a help topic
     * @param id a help topic
     */
    public void show(String id)
    {
        if (id==null)
            try {
                broker.setCurrentID(set.getHomeID());
            } catch (InvalidHelpSetContextException ihscex) {
                Application.error(ihscex);
            }
        else
            broker.setCurrentID(id);
        show();
    }

    /**
     * show the help home page
     */
    public void showHome() {
        show((String)null);
    }

    /**
     * show a context help topic, appropriate for some component
     * @param focus
     */
    public void show(Component focus) {
        String id = getContextHelpID(focus);
        show(id);
    }

    /**
     * show the help window
     */
    public void show()
    {
        broker.setDisplayed(true);
    }

    /**
     * @return true if the Help window is showing
     */
    public boolean isShowing()
    {
        return (broker!=null) && broker.isDisplayed();
    }

    /**
     * @return true if the given help topic is defined
     */
    public boolean isValid(String id)
    {
        if (id==null) return false;
        Map map = set.getCombinedMap();
        return map.isValidID(id,set);
    }

    /**
     * The JavaHelp system associates help topic IDs with components
     * Our approach is much simpler: each component's name IS the help topic.
     */
    public String getContextHelpID(Component component)
    {
        while (component != null) {
            String key = component.getName();
            if (isValid(key))
                return key;
            component = component.getParent();
        }
        return null;
    }


}
