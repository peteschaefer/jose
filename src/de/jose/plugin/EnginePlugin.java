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

import de.jose.*;
import de.jose.chess.*;
import de.jose.pgn.Parser;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;
import de.jose.util.*;
import de.jose.util.xml.XMLPrettyPrint;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.file.ImageFileFilter;
import de.jose.view.input.PluginListModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author Peter Sch�fer
 */

abstract public class EnginePlugin
		extends Plugin
        implements ActionListener
{
    /**	the engine has offered a draw to the user	*/
	protected boolean engineOfferedDraw;
	/**	the user offered a draw to the engine	*/
	protected boolean userOfferedDraw;
	/**	user requested a hint */
	protected boolean userRequestedHint;
	/**	called upon launch */
	protected Runnable launchHook = null;

    /**	current mode	 */
	protected int mode;
	/** calculation analysis */
	protected AnalysisRecord analysis;

	/**
	 * evaluations reported by the engine are from the engine's point of view
	 * (i.e. from the current color's point of view
	 */
	public static final int POINT_OF_VIEW_CURRENT   = 0;
	/**
	 * evaluattions reported by the engine are always from white's point of view.
	 * Note that this ifno is relevant for the *interpretation* of the values,
	 * not for display to the user. jose present the evaluation *always* from the current
	 * color's point of view.
	 */
	public static final int POINT_OF_VIEW_WHITE     = Constants.WHITE;
	/**
	 * evaluattions reported by the engine are always from black's point of view
	 * (doesn't happen in real life)
	 */
	public static final int POINT_OF_VIEW_BLACK     = Constants.BLACK;


	/**
	 * resign threshold: if the evaluation drops below this value,
	 * ask user to adjudicate the game
	 */
	public static final double RESIGN_THRESHOLD    = 0.01;  //= 1 % estimated result
	/**
	 * draw threshold: if the evaluation stays within this interval,
	 * ask ust to adjudicate the game
	 */
	public static final double DRAW_WDL_THRESHOLD   = 0.99;    //= 98 % draw probability
	public static final double DRAW_THRESHOLD     	= 0.01;    //= 1 % evaluation diff
	/**
	 * minimum game ply when DRAW_THRESHOLD becomes effective.
	 * don't accept early draws
	 */
	public static final int MIN_DRAW_PLY        = 60;   // don't draw before move 30
	/**
	 * number of moves before asking user to adjudicate
	 */
	public static final int ADJUDICATE_MOVES    = 5;


	/**	mode: paused (engine not in use)
	 *	engine position not in synch with application
	 * */
	public static final int PAUSED		= 0;
	/**	mode: thinking (calculating the next computer move)	 */
	public static final int THINKING	= 1;
	/**	mode: waiting for user move, pondering (permanent brain)	 */
	public static final int PONDERING	= 2;
	/**	mode: anylizing (i.e. thinking but not moving automatically)	 */
	public static final int ANALYZING	= 3;

	/** return value from updateDirtyElements()
	 *  no options have been modified
	 */
	public static final byte OPTIONS_CLEAN          = 0x00;
	/** return value from updateDirtyElements()
	 *  some options have been modified, engine restart is not required
	 */
	public static final byte OPTIONS_DIRTY          = 0x01;
	/** return value from updateDirtyElements()
	 *  some options have been modified, engine must be restarted
	 */
	public static final byte OPTIONS_RESTART        = 0x02;
	/** return value from updateDirtyElements()
	 *  a new engine has been selected
	 */
	public static final byte OPTIONS_NEW_ENGINE     = 0x04;

	//	numeric format for centipawn scores
	protected static final DecimalFormat CENTIPAWN_FORMAT = new DecimalFormat("+###0.00;-###0.00" );
	protected static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("##0 '%';-##0 '%'" );

	//	number format for Game counts
	private static DecimalFormat NUM_GAME = new DecimalFormat("###");


	/**	used to parse input moves	 */
	protected Parser moveParser;
	protected Position enginePosition;
	/** number of legal move in current position    */
	protected int legalMoveCount;

	/** time when the mode was changed */
	protected long modeTime;
	/** timer */
	protected javax.swing.Timer modeTimer;

	public EnginePlugin()
	{
		engineOfferedDraw = false;
		userOfferedDraw = false;
		analysis = new AnalysisRecord();
	}

	public static String getProtocol(Element config)
	{
		String prot = config.getAttribute("type");
		if (prot==null) prot = "xboard";    //  default
		return prot;
	}

    public static boolean isXBoard(Element config)
    {
	    return getProtocol(config).equals("xboard");
    }

    public static boolean isUci(Element config)
    {
        return getProtocol(config).equals("uci");
    }

	public static void setUci(Element config)
	{
		config.setAttribute("type","uci");
	}

	public static void setXBoard(Element config)
	{
		config.setAttribute("type","xboard");
	}

    public File getExecutable(String osName, File dir)
    {
        return getExecutable(config,osName,dir);
    }

    public static File getExecutable(Element config, String osName, File dir)
	{
		String result = getValue(config,osName,"COMMAND");
		if (result==null) return null;
		
		String fileName = StringUtil.replace(result,"%dir%",dir.getAbsolutePath());
		if (fileName!=null)
			return new File(fileName);
		else
			return null;
	}

	public static boolean setExecutable(Element config, String osName, String value)
	{
		boolean dirty = false;
		Element exec = getExecElement(config,osName,false);
		if (exec==null) {
			exec = getExecElement(config,osName,true);
			dirty = true;
		}
		if (XMLUtil.setChildValue(exec,"COMMAND",value)) dirty = true;
		return dirty;
	}


	protected List getCmdArgs(String osName)
	{
		List result = new ArrayList();
		String text = getValue(osName,"ARGS");
		if (text != null) {
			StringTokenizer tok = new StringTokenizer(text);
			while (tok.hasMoreTokens())
				result.add(tok.nextToken());
		}
		return result;
	}

	/**
	 * when evaluations are reported from the engine, which point of view ?
	 * @return
	 */
	abstract public int getEvaluationPointOfView();

	public static boolean setPaths(Element cfg, String os,
	                            File dir, File exe, File logo,
	                            File workingDir) throws FileNotFoundException
	{
		boolean dirty = false;

		//  dirPath is either relative to workingDir, or absolute
		String dirPath = getLocalPath(dir,workingDir,"%local%");

		//  exePath is either relative to dir, or absolute
		String exePath = getLocalPath(exe,dir,"%dir%");

		//  same with logo
		String logoPath = getLocalPath(logo,dir,"%dir%");

		//  apply
		if (getExecElement(cfg,os,false)==null) {
			//  create if necessary
			getExecElement(cfg,os,true);
			dirty = true;
		}

		if (setDir(cfg,dirPath)) dirty = true;
		if (setExecutable(cfg,os,exePath)) dirty = true;
		if (setLogo(cfg,logoPath)) dirty = true;
		return dirty;
	}

	private static String getLocalPath(File file, File baseDir, String prefix)
		throws FileNotFoundException
	{
        if (file==null) return null;
		if (file.exists())
		{
			//  absolute path; could be relativ to baseDir
			if (FileUtil.isChildOf(file,baseDir)) {
				if (prefix!=null)
					return prefix+File.separator+FileUtil.getRelativePath(baseDir,file,File.separator);
				else
					return FileUtil.getRelativePath(baseDir,file,File.separator);
			}
			else
				return file.getAbsolutePath();    //  absolute
		}
		else
			throw new FileNotFoundException(file.toString());
	}

	public void init(Position pos, String osName) throws IOException
	{
		super.init(pos, osName);
		addInputListener(this,1);

		File dir = getDir(osName);
        if ((dir==null) || !dir.exists())
            throw new IOException("can't run on "+osName);

        File executable = getExecutable(osName,dir);

        if (!dir.exists() || !dir.isDirectory())
            throw new FileNotFoundException("home directory "+dir+" not found");
        if (executable==null)
            throw new FileNotFoundException("not executable");
        else if (!executable.exists())
            throw new FileNotFoundException("executable "+executable+" not found");

		Vector command = new Vector();
		command.add(executable.getAbsolutePath());
		command.addAll(getCmdArgs(osName));

        String[] commandArray = StringUtil.toArray(command);
		String[] env = StringUtil.separateLines(getValue(osName,"ENV"));

		nativeProcess = Runtime.getRuntime().exec(commandArray, env, dir);
		stdIn = nativeProcess.getInputStream();
		stdInThread.setInputStream(stdIn);
		stdInThread.start();

		stdErr = nativeProcess.getErrorStream();
		stdErrThread.setInputStream(stdErr);
		stdErrThread.start();

		stdOut = nativeProcess.getOutputStream();
		Writer wout = new OutputStreamWriter(stdOut,"ISO-8859-1");
		printOut = new LineWriter(wout, "\n");   // flush each line
        printOut.synchronizeTo(this);   //  synchronize output with input

		enginePosition = new Position();
		moveParser = new Parser(enginePosition,0, true);

		Runtime.getRuntime().addShutdownHook(new KillProcess(nativeProcess));

		modeTimer = new javax.swing.Timer(1000,this);
		modeTimer.stop();
	}

    abstract public String getEngineDisplayName();

	public String getDisplayName(String os)
	{
		String name = getEngineDisplayName();	//	as returned by engine itself
		if (name!=null)
			return name;
		else
			return super.getDisplayName(os);	
	}

    public String getStartup(String os)
    {
        return getStartup(config,os);
    }

	public static String getStartup(Element config, String os)
	{
		return getValue(config,os,"STARTUP");
	}

	public static boolean setStartup(Element config, String value)
	{
		return XMLUtil.setChildValue(config,"STARTUP",value);
	}

	public String getDisplayName()
	{
		String engName = getEngineDisplayName();
		String cfgName = getName();
		if (engName==null || engName.equalsIgnoreCase(cfgName))
			return cfgName;
		else
			return engName+" ("+cfgName+")";
	}

	public static Element getOptionSet(Element cfg)
	{
		return XMLUtil.getChild(cfg,"OPTIONS");
	}

    public String[] getOptions(boolean dirtyOnly)
    {
        return getOptions(config,dirtyOnly);
    }

	public static String[] getOptions(Element config, boolean dirtyOnly)
	{
        Element optionSet = getOptionSet(config);
		if (optionSet==null) return new String[0];

        NodeList options = optionSet.getElementsByTagName("OPTION");

        String[] result = new String[options.getLength()*2];
        for (int i=0; i < options.getLength(); i++)
        {
            Element option = (Element)options.item(i);
	        if (!dirtyOnly || Config.isDirtyElement(option)) {
                result[2*i] = option.getAttribute("name");
                result[2*i+1] = XMLUtil.getTextValue(option);
	        }
        }
        return result;
    }

	public static boolean isOptionDirty(Element config, String optionName)
	{
        Element optionSet = getOptionSet(config);
        NodeList options = optionSet.getElementsByTagName("OPTION");

        for (int i=0; i < options.getLength(); i++)
        {
            Element option = (Element)options.item(i);
	        if (optionName.equals(option.getAttribute("name")))
				return Config.isDirtyElement(option);
        }
        return false;
    }

    public String getOptionValue(String option)
    {
        return getOptionValue(config,option);
    }

    public static String getOptionValue(Element config, String option)
    {
        Element optionSet = XMLUtil.getChild(config,"OPTIONS");
	    if (optionSet==null) return null;

        NodeList options = optionSet.getElementsByTagName("OPTION");
        if (options==null) return null;

	    for (int i=0; i < options.getLength(); i++)
        {
            Element optionElm = (Element)options.item(i);
            if (option.equalsIgnoreCase(optionElm.getAttribute("name")))
                return XMLUtil.getTextValue(optionElm);
        }
        return null;
    }


    public static Boolean getBooleanValue(Element config, String option)
    {
        String textValue = getOptionValue(config,option);
        return Util.toBoolean(textValue);
    }

    public String getOptionValue(String option, String defaultValue)
    {
        return getOptionValue(config,option,defaultValue);
    }

    public static String getOptionValue(Element config, String option, String defaultValue)
    {
        String result = getOptionValue(config,option);
        if (result==null) result = defaultValue;
        return result;
    }

	public static boolean setOptionValue(Element config, String option, Object value)
	{
		Element optionSet = XMLUtil.getChild(config,"OPTIONS",true);

		NodeList options = optionSet.getElementsByTagName("OPTION");
		if (options!=null)
			for (int i=0; i < options.getLength(); i++)
			{
				Element optionElm = (Element)options.item(i);
				if (option.equalsIgnoreCase(optionElm.getAttribute("name"))) {
					boolean dirty = XMLUtil.setTextValue(optionElm,value);
					if (dirty) Config.setDirtyElement(optionElm,true);
					return dirty;
				}
			}
		//  create new
		Element optionElm = config.getOwnerDocument().createElement("OPTION");
		optionElm.setAttribute("name",option);
		Config.setDirtyElement(optionElm,true);
		XMLUtil.setTextValue(optionElm,value);
		optionSet.appendChild(optionElm);
		return true;
	}


	public void setDebugMode(boolean on)
	{
		//  no-op implemented by UCI, for example
	}

	public void close()
	{
		try {
			printOut.close();
		} catch (Exception e) {/*ignore*/}

		try {
			stdInThread.close();
			stdInThread = null;
		} catch (Exception e) { /* ignore */ }

		try {
			stdErrThread.close();
			stdErrThread = null;
		} catch (Exception e) { /* ignore */ }

		if (nativeProcess!=null)
		try {
			nativeProcess.destroy();
			nativeProcess = null;
		} catch (Exception e) { /* ignore */ }
		try {
			if (modeTimer!=null) {
				modeTimer.stop();
				modeTimer = null;
			}
		} catch (Exception e) { /* ignore */ }
	}

	public static Vector getEngineNames(String osName)
	{
		if (!configured) config();
	    Vector result = new Vector();
	    for (int i=0; i<thePlugins.size(); i++) {
	        Plugin plug = (Plugin)thePlugins.get(i);
	        if (plug instanceof EnginePlugin && (osName==null || plug.canRunOn(osName)))
	            result.add(plug.getName());
	    }
	    return result;
	}

	public static Vector getPlugins()
	{
		if (!configured) config();
		return thePlugins;
	}

	public static byte updateDirtyElements(PluginListModel model,
	                                       Plugin currentPlugin) throws TransformerException, IOException, ParserConfigurationException, SAXException
	{
		Iterator i = model.currentIterator();
		Set dirtyDocs = new HashSet();
		byte result = OPTIONS_CLEAN;

		//  examine dirty elements
		for (int j=0; i.hasNext(); j++)
		{
			PluginListModel.Record rec = (PluginListModel.Record)i.next();
			if (!Config.isDirtyElement(rec.cfg)) continue;
			
			Plugin plugin = rec.plugin;
			Element optionSet = EnginePlugin.getOptionSet(rec.cfg);
			boolean optionsDirty = false;
			if (optionSet!=null) {
				optionsDirty = Config.isDirtyElement(optionSet);
				Config.setDirtyElement(optionSet,false);
			}

			if (plugin==currentPlugin && optionsDirty)
                result |= OPTIONS_DIRTY;
			//  TODO which options require an engine restart ?

			//  pretty-print the element (with readable indentation)
			Element prettyCfg = XMLPrettyPrint.pretty(rec.cfg,0);

			if (plugin==null) {
				//  new element
				Config.setDirtyElement(prettyCfg,false);    //  remove dirty marker
				//  save document to disk
				Document doc = prettyCfg.getOwnerDocument();
				rec.cfg.getParentNode().replaceChild(prettyCfg,rec.cfg);
				dirtyDocs.add(doc);
				//  create new Plugin
				plugin = Plugin.createPlugin(prettyCfg);
/*
				if (isUci(prettyCfg))
					plugin = new UciPlugin();
				else
					plugin = new XBoardPlugin();
*/
				plugin.setConfig(prettyCfg);
				thePlugins.add(plugin);
				model.update(rec, plugin);
				continue;
			}

			if (Config.isDirtyElement(prettyCfg))
			{
				//  modified element
				Config.setDirtyElement(prettyCfg,false);    //  remove dirty marker
				Document doc = prettyCfg.getOwnerDocument();
				dirtyDocs.add(doc);
				//  replace original config element
				Element oldCfg = plugin.config;
				oldCfg.getParentNode().replaceChild(prettyCfg,oldCfg);
				plugin.config = prettyCfg;
				model.update(rec,plugin);
				continue;
			}
		}

		i = model.deletedIterator();
		while (i.hasNext())
		{
			PluginListModel.Record rec = (PluginListModel.Record)i.next();
			Plugin plugin = rec.plugin;

			//  delete element
			if (plugin!=null) {
				thePlugins.remove(plugin);
				Element oldCfg = plugin.config;
				oldCfg.getParentNode().removeChild(oldCfg);
				Document doc = oldCfg.getOwnerDocument();
				dirtyDocs.add(doc);
			}
		}
		model.commitDelete();


		//  write modified documents to disk
		Config config = Application.theApplication.theConfig;
		i = dirtyDocs.iterator();
		while (i.hasNext()) {
			org.w3c.dom.Document doc = (org.w3c.dom.Document)i.next();
			if (XMLUtil.isEmpty(doc))
				config.deleteDocument(doc);
			else
				config.writeDocument(doc);
		}

		return result;
	}

	public static Element duplicateConfig(Element oldCfg) throws IOException, ParserConfigurationException
	{
		Document doc = XMLUtil.newDocument();

		Element docelm = doc.createElement("APPLICATION_SETTINGS");
		doc.appendChild(docelm);
		docelm.appendChild(doc.createTextNode("\n"));

		File configDir = new File(Application.theWorkingDirectory,"config");
		File xmlfile = FileUtil.uniqueFile(configDir, getName(oldCfg),"xml");
		Config.setFile(doc, xmlfile.getAbsolutePath());

		Element newCfg = (Element)doc.importNode(oldCfg,true);
		docelm.appendChild(newCfg);
		docelm.appendChild(doc.createTextNode("\n"));
		return newCfg;
	}

	public static File findLogo(File dir, String name)
	{
		String[] files = dir.list(new ImageFileFilter());
		for (int i=0; i<files.length; i++)
			if (files[i].startsWith(name))
				return new File(dir,files[i]);
		return null;
	}


	public static Vector getEngineDisplayNames(String osName)
	{
		if (!configured) config();
		Vector result = new Vector();
		for (int i=0; i<thePlugins.size(); i++) {
			Plugin plug = (Plugin)thePlugins.get(i);
			if (plug instanceof EnginePlugin && (osName==null || plug.canRunOn(osName)))
				result.add(plug.getDisplayName(osName));
		}
		return result;
	}

	public final int getMode()				{ return mode; }

	public final long getElapsedTime()      { return System.currentTimeMillis()-modeTime; }

	public final boolean isPaused()			{ return mode == PAUSED; }

	public final boolean isThinking()		{ return mode == THINKING; }

	public final boolean isPondering()		{ return mode == PONDERING; }

	public final boolean isAnalyzing()		{ return mode == ANALYZING; }


	public final void restartWithOptions(boolean dirtyOnly) throws IOException
	{
		switch(mode)
		{
			case PAUSED:
				setOptions(dirtyOnly);
				break;
			case THINKING:
				pause();
				setOptions(dirtyOnly);
				// todo ?
				break;
			case PONDERING:
				pause();
				setOptions(dirtyOnly);
				//	todo ?
				break;
			case ANALYZING:
				pause();
				setOptions(dirtyOnly);
				analyze(applPosition);
				break;
		}
	}

    abstract public boolean canPonder();

    abstract public boolean isActivelyPondering();

	protected void setMode(int newMode)
	{
		mode = newMode;

		modeTime = System.currentTimeMillis();
		if (mode==PAUSED) {
			if (modeTimer!=null) modeTimer.stop();
		}
		else if (modeTimer!=null)
			modeTimer.restart();
		else {
			modeTimer = new javax.swing.Timer(1000,this);
			modeTimer.start();
		}

		analysis.modified = AnalysisRecord.NEW_MOVE;
		//  clear analysis content for next
		sendMessage(mode);
	}

	public void setLaunchHook(Runnable lambda) {
		launchHook = lambda;
	}


	public AnalysisRecord getAnalysis()     { return analysis; }

	public int getMaxPVLines()              { return 1; }

	public enum SearchType {
		TIME_CONTROL, TIME, DEPTH, NODES
	}

	public static Element getSearchControls(Element root)
	{
		Element controls = XMLUtil.getChild(root,"SEARCH");
		if (controls==null)
			controls = XMLUtil.appendChild(root, "SEARCH");
		return controls;
	}

	public static SearchType getSelectedSearchControl(Element search)
	{
		for(SearchType type : SearchType.values())
			if (isSelectedSearchControl(search,type))
				return type;
		return SearchType.TIME_CONTROL;
	}

	public static boolean isSelectedSearchControl(Element search, SearchType type)
	{
		return XMLUtil.getChildBooleanAttributeValue(search,type.name(),"selected");
	}

	public static Element getSearchControlElement(Element search, SearchType type)
	{
		Element child = XMLUtil.getChild(search,type.name());
		if (child==null)
			child = XMLUtil.appendChild(search,type.name());
		return child;
	}

	static int[] searchDefaultValues = { 0, 10, 10, 10000 };

	public static int getSearchControlArgument(Element search, SearchType type)
	{
		Element child = getSearchControlElement(search,type);
		return XMLUtil.intValue(XMLUtil.getTextValue(child), searchDefaultValues[type.ordinal()]);
	}

	public static boolean setSearchControlArgument(Element search, SearchType type, SearchType selected, Long value)
	{
		boolean changed = false;
		Element child = XMLUtil.getChild(search,type.name());
		if (child==null) {
			child = getSearchControlElement(search,type);
			changed = true;
		}

		if (type==selected) {
			if (XMLUtil.setAttribute(child,"selected",Boolean.toString(true)))
				changed = true;
		}
		else {
			if (XMLUtil.removeAttribute(child,"selected"))
				changed = true;
		}

		if (value!=null && XMLUtil.setTextValue(child,Long.toString(value)))
			changed = true;
		return changed;
	}


	public void parseAnalysis(String input, AnalysisRecord rec)
	{
		/**	can not parse it; derived classes (like UciPlugin) can	*/
		rec.clear();
		rec.getLine(0).append(input);
		rec.setPvModified(0);  //  first PV has bee modified
		rec.ply = enginePosition.gamePly();
		rec.white_next = enginePosition.whiteMovesNext();
		rec.engineMode = mode;
	}

	/** @return the number of legal moves in the current engine position    */
	public int countLegalMoves()        { return legalMoveCount; }

    abstract public boolean canOfferDraw();

    abstract public boolean canAcceptDraw();

	abstract public boolean canResign();

	abstract public boolean supportsFRC();

	abstract public void enableFRC(boolean on);

	abstract public void offerDrawToEngine();

	public boolean hasOfferedDraw()	{ return engineOfferedDraw; }

	public boolean wasOfferedDraw()	{ return userOfferedDraw; }

	public void declineDraw()		{ engineOfferedDraw = false; }


	abstract public boolean isBookEnabled();

	abstract public void disableBook();


	public class FormattedMove extends Move
	{
		String text;

		FormattedMove (Move mv, boolean withNumber, String alternateText)
		{
			super(mv);
			text = StringMoveFormatter.formatMove(enginePosition, mv,withNumber);
			if (text==null) text = alternateText;
			if (text==null) text = mv.toString();
		}

		public String toString()    { return text; }
	}

	public static class EvaluatedMove extends Move
	{
		public int ply;
		public Score score = new Score();

		public EvaluatedMove(Move move, int ply, Score ascore, EnginePlugin plugin)
		{
			super(move);
			this.ply = ply;
			this.score.copy(ascore);
			if (plugin!=null)
				plugin.mapUnit(score);
		}

		protected EvaluatedMove(Move move, AnalysisRecord a, EnginePlugin plugin)
		{
			this(move,a.ply, a.eval[0], plugin);
		}

		public int getPly()             { return ply; }
	}


	public void actionPerformed(ActionEvent e)
	{
		//  call back from clock tick
		sendMessage(PLUGIN_ELAPSED_TIME, new Long(getElapsedTime()));
	}

	public boolean canAnalyze()
	{
		return canAnalyze(applPosition);
	}

	public boolean canAnalyze(Position pos)
	{
		return !pos.isGameFinished(true);
	}


	public boolean shouldResign(Game game, int engineColor, int ply, MoveNode node)
	{
		for (int i=0; i < ADJUDICATE_MOVES; i++)
		{
			Score value = node.engineValue;
			//  value is from white's point of view !
			if (value==null)
				return false; //  unknown value
			//	todo mapUnit(Score) !!

			double dont_lose;
			if (EngUtil.isBlack(engineColor))
				dont_lose = 1.0f - value.rel(value.win);
			else
				dont_lose = value.rel(value.win+value.draw);
			//	todo correct perspective ???
			if (dont_lose >= EnginePlugin.RESIGN_THRESHOLD)
				return false; //  above threshold; no reason to resign

			//  go to previous (full) moves
			node = node.previousMove();
			if (node==null) break;
			node = node.previousMove();
			if (node==null) break;
		}
		return true;
	}

	public boolean shouldDraw(Game game, int ply, MoveNode node)
	{
		if (ply <= MIN_DRAW_PLY) return false;
		//  don't draw in opening phase

		for (int i=0; i < ADJUDICATE_MOVES; i++)
		{
			Score value = node.engineValue;
			//	todo mapUnitWDL(Score) !!

			if (value == null)
				return false; //  unknown value

			if (value.draw != 0) {
				if (value.rel(value.draw) < DRAW_WDL_THRESHOLD)
					return false;
			}
			else
			{
				if (Math.abs(value.rel(2*value.win + value.draw) - 1.0f) > DRAW_THRESHOLD)
					return false;
			}

			//  go to previous (full) moves
			node = node.previousMove();
			if (node==null) break;
			node = node.previousMove();
			if (node==null) break;
		}
		return true;
	}

	//
	//	Score handling
	//


	/**
	 * todo needs overhaul.
	 * It does not work for win-percentage scores, wdl scores shouold be adjusted, too.
	 *
	 * @param score from the engine's point of view
	 * @return the score from WHITES point of view
	 */
	public void adjustPointOfView(Score score, boolean white_next)
	{
		score.cp_current = score.cp;
		switch (getEvaluationPointOfView())
		{
			case POINT_OF_VIEW_WHITE:
				break;
			case POINT_OF_VIEW_CURRENT:
				if (white_next) break;
				//	otherwise: fall-through intended
			case POINT_OF_VIEW_BLACK:
				score.cp = -score.cp;
				score.swapWDL();
				break;
			default:
				throw new IllegalStateException("point of view not defined");
		}
	}

	protected String prepareCentipawnScore(Score score, HashMap pmap, boolean white_pov)
	{
		String cptext;
		if (score.cp==0)
			cptext = "0";
		else {
			double cpd = (double)(white_pov ? score.cp:score.cp_current)/100.0;
			cptext = CENTIPAWN_FORMAT.format(cpd);
		}
		switch (score.flags)
		{
			case Score.EVAL_LOWER_BOUND:     cptext = "\u2265 "+cptext; break;  //  >=
			case Score.EVAL_UPPER_BOUND:     cptext = "\u2264 "+cptext; break;  //  <=
		}

		pmap.put("eval",cptext);
		return "plugin.evaluation";
	}

	private static String printScoreText(Score score, EnginePlugin plug, boolean tooltip, boolean with_wdl, boolean white_pov)
	{
		if (score.cp <=  Score.UNKNOWN) {
			return "";
		}

		HashMap pmap = new HashMap();
		String key;
		if (score.flags==Score.EVAL_GAME_COUNT)
		{
			//  book move, game count
			if (score.cp<=0)
				pmap.put("count","-");
			else
				pmap.put("count", StringUtil.formatLargeInt(score.cp,NUM_GAME));
			key = "plugin.gamecount";
		}
		else if (score.cp > Score.WHITE_MATES)
		{
			int plies = score.cp-Score.WHITE_MATES;
			pmap.put("eval",String.valueOf((plies+1)/2));
			key = "plugin.white.mates";
		}
		else if (score.cp < Score.BLACK_MATES)
		{
			int plies = Score.BLACK_MATES-score.cp;
			pmap.put("eval",String.valueOf((plies+1)/2));
			key = "plugin.black.mates";
		}
		else if (plug!=null) {
			key = plug.prepareCentipawnScore(score,pmap,white_pov);
		}
		else {
			key = "?";
		}

		String result = tooltip ? Language.argsTip(key,pmap) : Language.args(key,pmap);

		if (with_wdl && score.hasWDL()) {
			pmap.put("win",Integer.toString(score.win));
			pmap.put("draw",Integer.toString(score.draw));
			pmap.put("lose",Integer.toString(score.lose));
			if (tooltip) {
				result += "<br>" + Language.argsTip("plugin.wdl", pmap);
			}
			else {
				result += "\n" + Language.args("plugin.wdl", pmap);
			}
		}

		if (tooltip)
			return "<html>"+result+"</html>";
		else
			return result;
	}

	public static String printScore(Score score, EnginePlugin plug, boolean with_wdl, boolean white_pov) {
		return printScoreText(score, plug, false, with_wdl, white_pov);
	}

	public static String printScoreTooltip(Score score, EnginePlugin plug, boolean with_wdl) {
		return printScoreText(score, plug, true, with_wdl, true);
	}

	/**
	 * map centipawn/win percentage to interval [0..1]
	 * @param sc
	 * @return value in [0..1]
	 */
	public void mapUnit(Score sc) {
		assert(sc.cp >= Score.UNKNOWN);
		if (sc.cp==Score.UNKNOWN)
			sc.win = sc.draw = sc.lose = 0;
		else {
			//	we assume arbitrary, but practicable, upper limits for centipawns
			//	fitting the eval bar (4 squares = 4 pawn units = 400 centipawns
			mapUnit(sc, sc.cp, -400, +400);
		}
	}

	protected void mapUnit(Score sc, int cp, int cpmin, int cpmax) {
		int span = cpmax-cpmin;
		if (cp >= cpmax)
			sc.win = span;
		else if (cp <= cpmin)
			sc.win = 0;
		else
			sc.win = cp-cpmin;
		sc.draw = 0;
		sc.lose = span-sc.win;
	}

}
