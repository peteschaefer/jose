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

package de.jose.plugin;

import de.jose.Application;
import de.jose.Config;
import de.jose.comm.msg.MessageProducer;
import de.jose.Version;
import de.jose.profile.UserProfile;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.chess.TimeControl;
import de.jose.util.StringUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

abstract public class Plugin
		extends MessageProducer
		implements InputListener
{
	//-------------------------------------------------------------------------------
	//	Static Fields
	//-------------------------------------------------------------------------------

	/**	holds a list of all registered plug-ins	 */
	public static Vector thePlugins = new Vector();

	/**	message that is sent when the engine moves	 */
	public static final int PLUGIN_MOVE				= 99;
	/**	message that is sent when the engine accepts or offers a draw	*/
	public static final int PLUGIN_DRAW				= 98;
	public static final int PLUGIN_ACCEPT_DRAW		= 97;
	public static final int PLUGIN_RESIGNS			= 96;
	public static final int PLUGIN_COMMENT			= 95;
	public static final int PLUGIN_HINT				= 94;
	public static final int PLUGIN_REQUESTED_HINT	= 93;
	/** message sent in regular interval (to update elapsed time) */
	public static final int PLUGIN_ELAPSED_TIME     = 92;
	//  error condition in plugin; new setup required
	public static final int PLUGIN_ERROR                            = 91;
	//  fatal error condition in plugin; restart required
	public static final int PLUGIN_FATAL_ERROR                = 90;
	//  illegal move in opening book
	public static final int BOOK_ERROR                            = 89;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/** configuration (XML element) */
	public Element config;

	/**	input stream	 */
	protected InputStream stdIn;
	/**	error input stream	 */
	protected InputStream stdErr;
	/**	output stream	 */
	protected OutputStream stdOut;
	/**	output print stream	 */
	protected LineWriter printOut;

	/**	the native engine process	 */
	protected Process nativeProcess;

	/**	input threads	 */
	protected InputThread stdInThread;
	protected InputThread stdErrThread;

	/**	the application position
	 *	(which is not always in synch with the engine position)
	 */
	public Position applPosition;

	protected static boolean configured = false;

	//-------------------------------------------------------------------------------
	//	Basic Access Methods
	//-------------------------------------------------------------------------------

	public void init(Position pos, String osDir)
		throws IOException
	{
		applPosition = pos;
		removeAllListeners();
		initIO();
	}

	public void initIO()
	{
		if (stdInThread==null)  stdInThread = new InputThread();
		if (stdErrThread==null) stdErrThread = new InputThread();

        stdInThread.synchronizeTo(this);    //  synchronize input with output
        stdErrThread.synchronizeTo(this);
	}

	public void setConfig(Element cfg)
	{
		config = cfg;
	}

	public final File getConfigFile()   { return Config.getFile(config.getOwnerDocument()); }

	public static final String getId(Element config)
	{
		String result = config.getAttribute("ID");
		if ((result==null) || StringUtil.isWhitespace(result))
			result = config.getAttribute("id"); //  backward compatiblity (=bug;)
		if ((result==null) || StringUtil.isWhitespace(result))
			result = getName(config);
		if ((result==null) || StringUtil.isWhitespace(result))
			result = "?";
		return result;
	}

	public final String getId()       { return getId(config);	}

	public static final void setId(Element config, String id)
	{
		config.setAttribute("ID",id);
		if (config.getAttribute("id")!=null)
			config.removeAttribute("id");   //  backward compatibilty
	}

	public static final String getName(Element config)		{ return XMLUtil.getChildValue(config,"NAME"); }
	public final String getName()		                    { return getName(config); }

	public static boolean setName(Element config, String name) {
		return XMLUtil.setChildValue(config,"NAME",name); 
	}

	public static void setNameVersion(Element config, String nameAndVersion)
	{
		int k = nameAndVersion.lastIndexOf(' ');
		int k2 = nameAndVersion.lastIndexOf('_');
		int k3 = nameAndVersion.lastIndexOf('.');

		if (k2>=0 && k2<k) k = k2;
		if (k3>=0 && k3<k) k = k3;

		if (k > 0) {
			setName(config, nameAndVersion.substring(0,k));
			setVersion(config, nameAndVersion.substring(k+1));
		}
		else
			setName(config, nameAndVersion);
	}

	protected String getValue(String os, String tagName)
	{
		return getValue(config,os,tagName);
	}

	protected static Element getExecElement(Element config, String os)
	{
		NodeList execs = config.getElementsByTagName("EXEC");
		for (int i=0; i<execs.getLength(); i++)
		{
			Element exec = (Element)execs.item(i);
			if (os.equalsIgnoreCase(exec.getAttribute("os")))
				return exec;
		}
		//	cross-over rule for Mac_intel (was "Mac")
		if (os.equalsIgnoreCase("mac_intel")) {
			Element result = getExecElement(config, "mac");
			result.setAttribute("os",os);
			return result;
		}
		return null;
	}

	protected static Element getExecElement(Element config, String os, boolean create)
	{
		Element exec = getExecElement(config,os);
		if (exec==null && create) {
			exec = config.getOwnerDocument().createElement("EXEC");
			exec.setAttribute("os",os);
			config.appendChild(exec);
		}
		return exec;
	}

	protected static String getValue(Element config, String os, String tagName)
	{
		if (os!=null) {
			Element exec = getExecElement(config,os);
			if (exec!=null) {
				String result = XMLUtil.getChildValue(exec,tagName);
				if (result!=null) return result;
			}
		}
		//  else: return default value
		return XMLUtil.getChildValue(config,tagName);
	}

	public final String getVersion(String os)   { return getVersion(config,os); }

	public static final String getVersion(Element config, String os)   { return getValue(config,os,"VERSION"); }

	public static boolean setVersion(Element config, String name) { return XMLUtil.setChildValue(config,"VERSION",name); }

	public String getDisplayName(String os)
	{
		return getDisplayName(config,os);
	}

	public static String getDisplayName(Element config, String os)
	{
		String name = getName(config);
		if (os==null) os = Version.osDir;
		String version = getVersion(config,os);
		if (version != null)
			return name+" "+version;
		else
			return name;
	}

	public static String getAuthor(Element config)		{ return XMLUtil.getChildValue(config,"AUTHOR"); }
    public final String getAuthor()		                { return getAuthor(config); }

	public static boolean setAuthor(Element config, String value)  {
		return XMLUtil.setChildValue(config,"AUTHOR",value);
	}

	public final File getLogo()
	{
		return getLogo(config);
	}

	public static final File getLogo(Element config)
	{
		HashMap pmap = new HashMap();
		pmap.put("local", Application.theWorkingDirectory.getAbsolutePath());

		String logo = XMLUtil.getChildValue(config,"LOGO");
		if (logo != null) {
			String defaultDir = getValue(config,null,"DIR");
			if (defaultDir != null) {
				defaultDir = StringUtil.replace(defaultDir,pmap);
				pmap.put("dir",defaultDir);
			}
			logo = StringUtil.replace(logo,pmap);
			logo = FileUtil.fixSeparators(logo);
			return new File(logo);
		}
		else
			return null;
	}

	public static boolean setLogo(Element config, String value) { return XMLUtil.setChildValue(config,"LOGO",value); }

	public static String getArgs(Element config)        { return XMLUtil.getChildValue(config,"ARGS"); }

	public String getArgs()                             { return getArgs(config); }

	public static boolean setArgs(Element config, String value)        { return XMLUtil.setChildValue(config,"ARGS",value); }

	public final InputStream in()		{ return stdIn; }

	public final InputStream err()		{ return stdErr; }

	public final OutputStream out()		{ return stdOut; }
	
	public final LineWriter print()	    { return printOut; }

	public final Process getNativeProcess()		{ return nativeProcess; }

    public static File getDir(Element config, String os)
    {
        HashMap pmap = new HashMap();
        pmap.put("local", Application.theWorkingDirectory.getAbsolutePath());

        String defaultDir = getValue(config,null,"DIR");
        if (defaultDir!=null) {
            defaultDir = StringUtil.replace(defaultDir,pmap);
            pmap.put("dir",defaultDir);
        }

        String dir = getValue(config,os,"DIR");
        if (dir!=null) {
            dir = StringUtil.replace(dir,pmap);
	        dir = FileUtil.fixSeparators(dir);
	        return new File(dir);
        }
		else
	        return null;
    }

    public final File getDir(String os)
	{
        return getDir(config,os);
	}

	public static boolean setDir(Element config, String value)  { return XMLUtil.setChildValue(config,"DIR",value); }

    /** can run on this operating system ?  */
    public final boolean canRunOn(String os)
	{
	    File dir = getDir(os);
		if (dir==null || !dir.exists()) return false;

		File exe = EnginePlugin.getExecutable(config,os,dir);
	    return (exe!=null && exe.exists());
	}

	public boolean isOpen()	{
		return (stdInThread!=null) && stdInThread.isAlive();
	}

	/**	set up the plugin and start the application	 */
	abstract public boolean open(String osName)
		throws IOException;

	abstract public void setOptions(boolean dirtyOnly) throws IOException;

	abstract public boolean restartRequired() throws IOException;

	/**	shut it down	 */
	abstract public void close();

	/**	start a new game	 */
	abstract public void newGame();
	
	/**	sends a user move to the engine	 */
	abstract public void userMove(Move mv, boolean go);

	/**	starts analyzing a position	*/
	abstract public void analyze(Position pos);

	/** keep analyzing after a user move    */
	abstract public void analyze(Position pos, Move userMove);

	/**	start thinkig about the next move	 */
	abstract public void go();

	/**	make the next move immediately	 */
	abstract public void moveNow();

	/**	stop thinking	 */
	abstract public void pause();

	/**	request a hint from the engine */
	abstract public void getHint();

    /**	set the time controls	 */
	abstract public void setTimeControls(int moves, long millis, long increment);

	public final void setTimeControls(TimeControl.Record tc)
	{
		setTimeControls(tc.moves, tc.millis, tc.increment);
	}
	
	/**	set the plugin's time	 */
	abstract public void setTime(long millis);

	
	//	interface InputListener:
	
	/**	called when a line of input is received from the plugin
     * @param chars
     * @param offset
     * @param len     */
	abstract public void readLine(char[] chars, int offset, int len);
	
	/**	called when the end of input is reached	 */
	public void readEOF()
	{
//		System.out.println("Plugin: unexpected end of input");
	}
	
	/**	called when a I/O is caused by the plugin	 */
	public void readError(Throwable ex)
	{
		Application.error(ex);
	}
	
	public void addInputListener(InputListener l, int priority)
	{
		stdInThread.addInputListener(l,priority);
		stdErrThread.addInputListener(l,priority);
	}
	
	public void removeInputListener(InputListener l)
	{
		stdInThread.removeInputListener(l);
		stdErrThread.removeInputListener(l);
	}
	
	public void addOutputListener(OutputListener l, int priority)
	{
		printOut.addOutputListener(l,priority);
	}
	
	public void removeOutputListener(OutputListener l)
	{
		printOut.removeOutputListener(l);
	}

	public void removeAllListeners()
	{
		super.removeAllMessageListeners();

		if (stdInThread!=null) stdInThread.removeAllListeners();
		if (stdErrThread!=null) stdErrThread.removeAllListeners();
		if (printOut!=null) printOut.removeAllListeners();
	}

	public static final Plugin getPlugin(String id, String os, boolean executable)
	{
		if (id==null) return null;
		StringTokenizer tok = new StringTokenizer(id,";,");
		while (tok.hasMoreTokens()) {
			String id1 = tok.nextToken();
			Plugin result = get1Plugin(id1,os,executable);
			if (result!=null) return result;
		}
		return null;
	}

	public static final String getPluginId(String id, String os, boolean executable)
	{
		if (id==null) return null;
		Plugin plug = getPlugin(id,os,executable);
		if (plug!=null)
			return plug.getId();
		else
			return null;
	}

	private static final Plugin get1Plugin(String id, String os, boolean executable)
	{
		if (!configured) config();

		//  try exact matches
		for (int i=0; i<thePlugins.size(); i++) {
			Plugin p = (Plugin)thePlugins.get(i);
			if (p.matches(id,os,true,executable))
				return p;
		}
		//  try sloppy matches
		for (int i=0; i<thePlugins.size(); i++) {
			Plugin p = (Plugin)thePlugins.get(i);
			if (p.matches(id,os,false,executable))
				return p;
		}
		return null;
	}


	public static final Plugin getDefaultPlugin(String os, boolean executable)
	{
		return getPlugin(UserProfile.FACTORY_ENGINES,os,executable);
	}


	public static final String getDefaultPluginName(String os)
	{
		Plugin defaultPlugin = getDefaultPlugin(os,false);
		if (defaultPlugin==null)
			return null;
		else
			return defaultPlugin.getName();
	}

	public boolean matches(String anId, String anOs, boolean exact, boolean executable)
	{
		/** 1. try ID   */
		String id = getId();
		if (anId!=null) {
			if (exact && !id.equalsIgnoreCase(anId))
				return false;
			if (!exact && !id.regionMatches(true,0,anId,0, Math.min(id.length(),anId.length())))
				return false;
		}

		if (anOs!=null && executable && !canRunOn(anOs))
            return false;

		return true;
	}

	public int getParseCapabilities()
	{
		return 1;   //  1 PV
	}

	//-------------------------------------------------------------------------------
	//	static methods
	//-------------------------------------------------------------------------------

	public static void config()
	{
		Plugin.config(Application.theApplication.theConfig.enumerateElements("PLUGIN"));
	}

	public static void config(Enumeration en)
	{
		configured = true;
		while (en.hasMoreElements())
		{
			Element elm = (Element)en.nextElement();
			//String type = elm.getAttribute("type");		//	currently ignored
			Plugin plugin = createPlugin(elm);
			plugin.setConfig(elm);
			thePlugins.add(plugin);
		}
	}

	public static Plugin createPlugin(Element config)
	{
		if (!configured) config();

		String className = XMLUtil.getChildValue(config,"ADAPTER-CLASS");
		String type = config.getAttribute("type");

		/** for the time being, UCI has precedence over Adapter-Class
		 *  (this is to avoid running a UCI engine with CraftyPlugin)
		 *  might need to change, as soon as we have custom UCI adapters
		 */
		if ("uci".equalsIgnoreCase(type))
			return new UciPlugin();

		if (className != null) {
			//  custom adapter class
			Class clazz = null;
			try {
				clazz = Class.forName(className);
				return (Plugin)clazz.newInstance();
			} catch (Exception e) {
				e.printStackTrace();  //To change body of catch statement use Options | File Templates.
			}
		}
        //  else
		return new XBoardPlugin();  //  XBoard is default

/*		else if ("ics".equalsIgnoreCase(type))
			return new ICSPlugin();
*/
	}

	public static String[] parseArgs(String s) {
		Vector vresult = new Vector();
		StringTokenizer toks = new StringTokenizer(s, " \t",false);
		while (toks.hasMoreTokens())
			vresult.add(toks.nextElement());

		String[] result = new String[vresult.size()];
		vresult.toArray(result);
		return result;
	}
}
