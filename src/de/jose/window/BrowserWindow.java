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

package de.jose.window;

import de.jose.Application;
import de.jose.Language;
import de.jose.Version;
import de.jose.SplashScreen;
import de.jose.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;


/**
 *
 *
 * @author Peter Schäfer
 */
public class BrowserWindow
            extends Thread
{
	public static final int DEFAULT 		= 1;
	public static final int ASK 			= 2;
	public static final int ALWAYS_ASK 		= 3;

	private URL url;


	public static void showWindow(String url) throws IOException
	{
		showWindow(new URL(url));
	}


	public static void showWindow(URL url) throws IOException
	{
		/**
		 * launching the default browser in Windows is accomplished
		 * by a call to url.dll
		 */
 		if(Version.windows)
			winBrowser(url);
		else if (Version.mac)
			de.jose.MacAdapter.openBrowser(url);
		else
			new BrowserWindow(url).start();
	}

	protected BrowserWindow(URL url)
	{
		this.url = url;
	}


	public static String getBrowser(int mode)
	{
		String result = Application.theUserProfile.getString("default.browser");
		try {
			if (result==null) {
				if (Version.windows)
					result = Application.theApplication.theConfig.getDefaultWebBrowser("Windows");
				else if (Version.mac)
					result = Application.theApplication.theConfig.getDefaultWebBrowser("Mac");
				else
					result = Application.theApplication.theConfig.getDefaultWebBrowser("Unix");

				if (mode==DEFAULT)
					return result;
			}
			else if (mode!=ALWAYS_ASK)
				return result;

            SplashScreen.close();
			result = (String)JOptionPane.showInputDialog((Component)null,
				Language.get("dialog.browser"),
				Language.get("dialog.browser.title"),
				JOptionPane.QUESTION_MESSAGE, (Icon)null,
				null,result);
			return result;
		} finally {
			Application.theUserProfile.set("default.browser",result);
		}
	}

	public void run()
	{
		/** Finding the default Web Browser with Linux is obviously more difficult;
		 *  we have to ask the user (Linux users are used to that kind of bullshit ;-)
		 */
		for (;;) {
			//  ask the user
			String command = getBrowser(ASK);
			if (command==null)
				return;

			if (unixBrowser(command,url))
				return;	//	succeeded
			else
				Application.theUserProfile.set("default.browser",null);	//	failed
		}
	}

	/**
	 * open the installed default browser (on Windows)
	 * @param url
	 */
	protected static boolean winBrowser(URL url)
		throws IOException
	{
		String command = getBrowser(DEFAULT);		//	no need to interact with user
		command = StringUtil.replace(command,"%url%",url.toExternalForm());
		Process proc = Runtime.getRuntime().exec(command);
		return true;
	}

	protected static boolean unixBrowser(String command, URL url)
	{
		try {
			if (isMozilla(command)) {
				/**
				 * mozilla needs special care:
				 * - first try mozilla -remote openURL("%u")
				 * - if this fails, try mozilla %url% "
				 */
				String command1 = StringUtil.replace(command,"%url%", " -remote openURL(%url%) ");
				command1 = StringUtil.replace(command1,"%url%",url.toExternalForm());
				Process proc = Runtime.getRuntime().exec(command1);

				// wait for exit code -- if it's 0, command worked,
				// otherwise we need to start the browser up.
				if (proc.waitFor()==0)
					return true;	//	allright
			}

			command = StringUtil.replace(command,"%url%",url.toExternalForm());
			Process proc = Runtime.getRuntime().exec(command);
			return proc.waitFor()==0;

		} catch (Exception ioex) {
			//	?
		}

		return false;
	}

	protected static boolean isMozilla(String command)
	{
		return 	(command.indexOf("mozilla") >= 0) ||
				(command.indexOf("Mozilla") >= 0) ||
                (command.indexOf("firefox") >= 0) ||
				(command.indexOf("netscape") >= 0) ||
				(command.indexOf("Netscape") >= 0);
	}
}
