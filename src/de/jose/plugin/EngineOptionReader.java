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

import de.jose.*;
import de.jose.view.ConsolePanel;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.print.DocFlavor;
import javax.xml.parsers.ParserConfigurationException;

/**
 * EngineOptionReader
 *
 * retrieve engine options from an XBoard, or UCI engine.
 * Since this is  a lengthy operation, it is moved to a separate thread.
 * Communicates with OptionDialog via messages.
 *
 * @author Peter Schäfer
 */
public class EngineOptionReader
		extends MessageProducer
		implements Runnable
{
	public static final int NEW_CONFIG = 1001;
	public static final int READ_CONFIG = 1002;
	public static final int ABORTED = 1003;
	public static final int DEFAULT_CONFIG = 1004;
	public static final int ERROR = 1099;

	private int what;
	private File executable;
	private Element cfg;
	private boolean stopped = false;

	public EngineOptionReader(MessageListener listener)
	{
		super.addMessageListener(listener);
	}

	public void createNewConfig(File exeFile)
	{
		what = NEW_CONFIG;
		this.executable = exeFile;
		new Thread(this).start();
	}

	public void readOptions(Element cfg)
	{
		what = READ_CONFIG;
		this.cfg = cfg;
		new Thread(this).start();
	}

	public void setDefaultConfig(Element cfg)
	{
		what = DEFAULT_CONFIG;
		this.cfg = cfg;
		new Thread(this).start();
	}

	public void stop()
	{
		stopped = true;
	}


	public synchronized void run()
	{
		try {
			switch (what) {
			case NEW_CONFIG:
				Element newConfig = createNewConfig();
				if (!stopped) sendMessage(NEW_CONFIG, newConfig);
				break;

			case READ_CONFIG:
				UciPlugin uciPlugin = readUciOptions(cfg);
				if (!stopped) sendMessage(READ_CONFIG, uciPlugin.getUciOptions().clone());
				break;

			case DEFAULT_CONFIG:
				setDefaultUciValues(cfg);
				if (!stopped) sendMessage(DEFAULT_CONFIG, cfg);
				break;
			}
		} catch (Throwable thrw) {
			if (!stopped) sendMessage(ERROR,thrw);
		}
	}

	private Element createNewConfig()
			throws ParserConfigurationException, IOException
	{
		Document doc = XMLUtil.newDocument();

		Element docelm = doc.createElement("APPLICATION_SETTINGS");
		doc.appendChild(docelm);
		docelm.appendChild(doc.createTextNode("\n"));

		Element new_cfg = XMLUtil.getChild(docelm,"PLUGIN",true);
		docelm.appendChild(doc.createTextNode("\n"));

		File dir = executable.getParentFile();
		String name = FileUtil.trimExtension(executable.getName());
		EnginePlugin.setNameVersion(new_cfg,name);

		File configDir = new File(Application.theWorkingDirectory,"config");
		File xmlfile = FileUtil.uniqueFile(configDir, name,"xml");
		Config.setFile(doc, xmlfile.getAbsolutePath());

		EnginePlugin.setPaths(new_cfg, Version.osDir, dir, executable,
				EnginePlugin.findLogo(dir,name),
		        Application.theWorkingDirectory);

		//  launch exe and retrieve UCI info (if available)
		try {

			UciPlugin uciplug = readUciOptions(new_cfg);
			EnginePlugin.setUci(new_cfg);
			EnginePlugin.setNameVersion(new_cfg,uciplug.getEngineName());
			EnginePlugin.setAuthor(new_cfg,uciplug.getEngineAuthor());

			Vector ucioptions = uciplug.getUciOptions();
			for (int i=0; i<ucioptions.size(); i++)
			{
				UciPlugin.Option option = (UciPlugin.Option)ucioptions.get(i);
				if (option.type!=UciPlugin.BUTTON)
					EnginePlugin.setOptionValue(new_cfg, option.name, option.defaultValue);
			}

			EnginePlugin.setOptionValue(new_cfg,"Ponder","true");
			if (uciplug.supportsOption("OwnBook"))
				EnginePlugin.setOptionValue(new_cfg,"OwnBook","true");

		} catch (IllegalArgumentException ex) {
			//  not a UCI plugin - OK
			EnginePlugin.setXBoard(new_cfg);
			EnginePlugin.setOptionValue(new_cfg,"Ponder","true");
		}
		return new_cfg;
	}

	private UciPlugin readUciOptions(Element cfg)
			throws IOException
	{
		/** let's go:   */
		UciPlugin plug = null;
		try {
			plug = new UciPlugin();

			plug.setConfig(cfg);
			plug.initIO();  //  must be called before addInputListener

		//			plug.addInputListener(new DebugInputListener(),2);
			plug.readOptions = true;
			plug.hasAnalyseOption = plug.supportsOption("UCI_AnalyseMode");

			plug.init(null, Version.osDir); //  launch executable
			ProcessUtil.setPriority(plug.nativeProcess, ProcessUtil.NORM_PRIORITY);
			//  don't let this process starve; it's supposed to run quick, anyway

			//  debug output goes to console panel
//			ConsolePanel console = Application.theApplication.consolePanel();
//			if (console != null) console.connectTo(plug);

			if (! plug.waitFor("uci","uciok",10000))
				throw new IllegalArgumentException("this is not a UCI plugin");
		//			plug.waitFor("uci","uciok",3000);
		} finally {
			if (plug!=null) plug.close();
		}
		return plug;
	}

	private void setDefaultUciValues(Element cfg) throws IOException
	{
		UciPlugin plug = readUciOptions(cfg);
		Vector options = plug.getUciOptions();
		for (int i=0; i<options.size(); i++)
		{
			UciPlugin.Option option = (UciPlugin.Option)options.get(i);
			switch (option.type)
			{
			case UciPlugin.CHECKBOX:
			case UciPlugin.COMBO:
			case UciPlugin.SPIN:
			case UciPlugin.STRING:
				UciPlugin.setOptionValue(cfg,option.name,option.defaultValue);
				break;

			case UciPlugin.BUTTON:    /* ignore */ break;
			}
		}
	}
}
