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

package de.jose;

import de.jose.chess.Clock;
import de.jose.chess.Constants;
import de.jose.pgn.Game;
import de.jose.plugin.EnginePlugin;
import de.jose.profile.UserProfile;
import de.jose.util.StringUtil;
import de.jose.view.Animation;
import de.jose.view.BoardPanel;
import de.jose.view.JoPanel;
import de.jose.view.input.JoStyledLabel;
import de.jose.window.JoDialog;

import java.applet.Applet;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

abstract public class AbstractApplication
        	extends Applet
        	implements CommandListener, Constants
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	static reference to the one and only Application instance	 */
	public static AbstractApplication	theAbstractApplication = null;

	/**	working directory	 */
	public static File			theWorkingDirectory;
	/**	map command codes to command handlers	*/
	public static CommandDispatcher	theCommandDispatcher;
	/**	user profile	 */
	public static UserProfile	theUserProfile;
	/**	log exceptions to file */
	public static boolean		logErrors = true;
	/**	log version info (logged only once per session) */
	public static boolean		logVersion = true;
	/**	show error dialog ?	*/
	public static boolean		showErrors = true;
	/**	true if this object is running as an Applet	*/
	public boolean				isApplet = false;
	/**	icon image (for window frames, etc.)	 */
	public Image				theIconImage;
	/**	show 3d frame rate on std out	*/
	public 	boolean				showFrameRate = false;
	/**	Current Game	 */
	public Game					theGame;
	/**	Clock	 */
	public Clock				theClock;

	protected static long		lastError = 0L;
	protected static long		ERROR_DIALOG_TIMEOUT	= 2*60*1000;
	/** animation thread    */
	protected Animation         animation;

	/**	engine plugin	 */
	protected EnginePlugin		engine;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	public AbstractApplication()
	{
		theAbstractApplication = this;
	}

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	/**
	 * get a resource file. the resource is searched for:
	 * 1. in the current JAR file
	 * 2. in the local file system
	 * 3. if running as an applet, at the applet's location
	 */
	public URL getResource(String fileName)
	{
	    /*  1. look in jar */
        ClassLoader cl = getClass().getClassLoader();
	    URL result = cl.getResource(fileName);
	    if (result != null) return result;

	    /*  if running as an Applet, look in Web    */
	    try {
	        if (theWorkingDirectory != null) {
	            /*  2. look in local file system    */
	            File file = new File(theWorkingDirectory,fileName);
	            if (file.exists())
	                return new URL("file",null,file.getAbsolutePath());
	        }
	        if (isApplet) {
	            /*  3. look in Applet's base location    */
	            URL base = getDocumentBase();
	            return new URL(base,fileName);
	        }
	    } catch (MalformedURLException muex) {
	        //  must not happen
	        Application.error(muex);
	    }

	    /* not found ...    */
	    return null;
	}

	/**
     * get a resource stream. the resource is searched for:
     * 1. in the current JAR file
     * 2. in the local file system
     * 3. if running as an applet, at the applet's location
     */
    public InputStream getResourceStream(String fileName)
        throws IOException
    {
        URL url = getResource(fileName);
        if (url != null)
            return url.openStream();
       else
            return null;
    }

	public static void error(Throwable thrw)
	{
		reportError(thrw,logErrors,showErrors);
	}

	public static void warning(Throwable thrw)
	{
		reportError(thrw,logErrors,false);
	}

	public static void logError(Throwable thrw)
	{
		reportError(thrw,true,false);
	}

	public static void reportError(Throwable thrw, boolean log, boolean show)
	{
		thrw.printStackTrace(System.err);

		if (log)
			try {
				File logfile = new File("error.log");
				boolean append = logfile.length() < 32*1024;

				PrintStream errStream = new PrintStream(new FileOutputStream(logfile,append));

				if (logVersion) {
					errStream.println(" ---- Version Info ----");
					errStream.print  (" jose: ");
						errStream.println(Version.jose);
					errStream.print  (" OS:   ");
						errStream.print(Version.osName);
						errStream.print(" ");
						errStream.println(Version.osVersion);
					errStream.print  (" Arch:  ");
						errStream.println(Version.arch);
					errStream.print  (" Java: ");
						errStream.println(Version.javaRuntime);
					errStream.println(" ----------------------");
					logVersion = false;	// log only once per session
				}

				errStream.print(" ---- ");
				errStream.print(new Date());
				errStream.println(" ---- ");

				thrw.printStackTrace(errStream);
				errStream.flush();
				errStream.close();
			} catch (IOException ioex) {
				//	couldn't write to error log - can't help it
			}

		if (show)
			try {
				showErrorDialog(thrw);
			} catch (Exception ex) {
				//	can't help it
			}
	}

    public static void warning(String message)
    {
        if (logErrors)
            try {
                PrintStream errStream = new PrintStream(new FileOutputStream("error.log",true));
                errStream.println(message);
                errStream.flush();
                errStream.close();
            } catch (IOException ioex) {
                //	couldn't write to error log - can't help it
            }
        else
            System.err.println(message);
    }

	public static void fatalError(Throwable thrw, int resultCode)
	{
		error(thrw);
		System.exit(resultCode);
 	}

	public static void showErrorDialog(Throwable thrw)
	{
		synchronized (theAbstractApplication) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime-lastError) > ERROR_DIALOG_TIMEOUT)
				lastError = currentTime;
			else
				return;
		}

		String message = Language.get("error.bug");
		File logFile = new File(Application.theWorkingDirectory,"error.log");
		message = StringUtil.replace(message,"%error.log%",logFile.getAbsolutePath());

		JoDialog dialog = new JoDialog("dialog.error",true) {
			public void setupActionMap(Map map) {
				super.setupActionMap(map);

				CommandAction action = new CommandAction() {
					public void Do(Command cmd) throws Exception {
						hide();
						wasCancelled = false;
						cmd.data = new URL(Application.theApplication.theConfig.getURL("tracker-add"));
						Application.theCommandDispatcher.forward(cmd,Application.theApplication);
					}
				};
				map.put("menu.web.report",action);
			}
		};

		dialog.center(360,240);
		dialog.getElementPane().add(new JoStyledLabel(message), JoDialog.ELEMENT_REMAINDER);

		dialog.addButton("menu.web.report");
		if (thrw instanceof SQLException)
			dialog.addButton("menu.help.repair");
		dialog.addButton(JoDialog.CANCEL);


		dialog.show();
	}

	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public CommandListener getCommandParent()
	{
		return null;
	}

	public final BoardPanel boardPanel()		{ return (BoardPanel)JoPanel.get("window.board"); }

	public Animation getAnimation() {
		if (animation==null)
			animation = new Animation(theUserProfile.getInt("animation.speed"));
		return animation;
	}

	public EnginePlugin getEnginePlugin() {
		return engine;
	}


}
