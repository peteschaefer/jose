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

package de.jose.plugin;

import de.jose.Application;
import de.jose.Config;
import de.jose.Util;
import de.jose.Version;
import de.jose.chess.*;
import de.jose.pgn.Parser;
import de.jose.pgn.Game;
import de.jose.pgn.MoveNode;
import de.jose.pgn.EvalArray;
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
import java.util.*;

/**
 *
 * @author Peter Schäfer
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
	public static final int RESIGN_THRESHOLD    = -800;  //= 8 pawn units
	/**
	 * draw threshold: if the evaluation stays within this interval,
	 * ask ust to adjudicate the game
	 */
	public static final int DRAW_THRESHOLD      = 2;    //= +/-0.02 pawn units
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
	/**	process priorities
	 * 	setting the process priority can control "CPU hogs"
	 * */
	public static final int[] PROCESS_PRIORITY = {
		ProcessUtil.MIN_PRIORITY,		//	when PAUSED
		ProcessUtil.NORM_PRIORITY-1,	//	when THINKKING
		ProcessUtil.NORM_PRIORITY-1,	//	when PONDERING
		ProcessUtil.NORM_PRIORITY-1,	//	when ANALYZING
	};

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

	/**
	 *
	 * @param score from the engine's point of view
	 * @return the score from WHITES point of view
	 */
	public int adjustPointOfView(int score)
	{
		switch (getEvaluationPointOfView())
		{
		case POINT_OF_VIEW_CURRENT:
				if (enginePosition.blackMovesNext()) return -score;
		case POINT_OF_VIEW_WHITE:
				return score;
		case POINT_OF_VIEW_BLACK:
				return -score;
		default:
				throw new IllegalStateException("point of view not defined");
		}
	}

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

		ProcessUtil.setPriority(nativeProcess, PROCESS_PRIORITY[mode]);
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
		String name = getEngineDisplayName();
		if (name!=null)
			return name;					//	as returned by "feature"
		else
			return getName();	//	as defined in properties.xml
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


    abstract public boolean canPonder();

    abstract public boolean isActivelyPondering();

	protected void setMode(int newMode)
	{
		if (PROCESS_PRIORITY[newMode] != PROCESS_PRIORITY[mode])
			ProcessUtil.setPriority(getNativeProcess(),PROCESS_PRIORITY[newMode]);

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


	public AnalysisRecord getAnalysis()     { return analysis; }

	public int getMaxPVLines()              { return 1; }

	public void parseAnalysis(String input, AnalysisRecord rec)
	{
		/**	can not parse it; derived classes (like UciPlugin) can	*/
		rec.clear();
		rec.getLine(0).append(input);
		rec.setPvModified(0);  //  first PV has bee modified
		rec.ply = enginePosition.gamePly();
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
		int ply;
		int value;
		int flags;

		public EvaluatedMove(Move move, int ply, int value, int flags)
		{
			super(move);
			this.ply = ply;
			this.value = value;
			this.flags = flags;
		}

		protected EvaluatedMove(Move move, AnalysisRecord a)
		{
			this(move,a.ply, a.eval[0],a.evalFlags[0]);
		}

		public int getPly()             { return ply; }
		public int getValue()           { return value; }
		public boolean isValid()        { return value > AnalysisRecord.UNKNOWN; }
		public boolean isExact()        { return flags == AnalysisRecord.EVAL_EXACT; }
		public boolean isLowerBound()   { return flags == AnalysisRecord.EVAL_LOWER_BOUND; }
		public boolean isUpperBound()   { return flags == AnalysisRecord.EVAL_UPPER_BOUND; }
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
		return !pos.isGameFinished();
	}


	public boolean shouldResign(Game game, int engineColor, int ply, MoveNode node)
	{
		for (int i=0; i < ADJUDICATE_MOVES; i++)
		{
			int value = node.getEngineValue();
			//  value is from white's point of view !
			if (value <= AnalysisRecord.UNKNOWN)
				return false; //  unknown value
			if (EngUtil.isBlack(engineColor)) value = -value;
			//  now value is from the engine's point of view
			if (value > EnginePlugin.RESIGN_THRESHOLD)
				return false;   //  above threshold; no reason to resign

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
			int value = node.getEngineValue();
			if (value <= AnalysisRecord.UNKNOWN)
				return false; //  unknown value
			if ((value < -EnginePlugin.DRAW_THRESHOLD) || (value > EnginePlugin.DRAW_THRESHOLD))
				return false;   //  out of interval; no reason for draw

			//  go to previous (full) moves
			node = node.previousMove();
			if (node==null) break;
			node = node.previousMove();
			if (node==null) break;
		}
		return true;
	}


}
