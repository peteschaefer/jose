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

package de.jose;

import de.jose.chess.*;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.export.ExportConfig;
import de.jose.export.ExportContext;
import de.jose.export.HtmlUtil;
import de.jose.help.HelpSystem;
import de.jose.image.TextureCache;
import de.jose.pgn.Collection;
import de.jose.pgn.*;
import de.jose.plugin.EnginePlugin;
import de.jose.plugin.InputListener;
import de.jose.plugin.Plugin;
import de.jose.profile.FrameProfile;
import de.jose.profile.LayoutProfile;
import de.jose.profile.UserProfile;
import de.jose.task.DBTask;
import de.jose.task.GameSource;
import de.jose.task.GameTask;
import de.jose.task.NalimovOnlineQuery;
import de.jose.task.db.*;
import de.jose.task.io.*;
import de.jose.util.AWTUtil;
import de.jose.util.ClipboardUtil;
import de.jose.util.StringUtil;
import de.jose.util.WinUtils;
import de.jose.util.file.FileUtil;
import de.jose.util.print.PrintableDocument;
import de.jose.util.xml.XMLUtil;
import de.jose.view.*;
import de.jose.view.input.LookAndFeelList;
import de.jose.view.input.WriteModeDialog;
import de.jose.view.input.JoStyledLabel;
import de.jose.view.style.JoStyleContext;
import de.jose.window.*;
import de.jose.book.OpeningLibrary;
import de.jose.book.BookEntry;
import net.roydesign.mac.MRJAdapter;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.*;
import java.net.*;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.*;

/**
 *	the main application class
 *
 *  (it is NOT intended to be run as an Applet; this is just for derived classes like FlyBy)
 */

public class Application
        extends AbstractApplication
		implements ActionListener, AWTEventListener, DeferredMessageListener, InputListener, ClipboardOwner
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------


    /**	window icon
	 */
	public static final String ICON_IMAGE = "images"+File.separator+"jose32.gif";

	public static final String USER_PROFILE = ".jose.user.preferences";

	public static final String DEFAULT_DATABASE = "MySQL";

	/**	Game Mode	*/

	/**	User input, no engine analysis (default)	*/
	public static final int USER_INPUT		= 1;
	/**	User input with background analysis	*/
	public static final int ANALYSIS		= 2;
	/**	User vs. Rngine	*/
	public static final int USER_ENGINE		= 3;
	/**	Engine 1 vs. Rngine 2 (not implemented)	*/
	public static final int ENGINE_ENGINE	= 4;


	/**
	 * the following message is printed to std out if the application appears to be locked
	 * this can either be caused by running two instances of jose at the same time
	 * or by a previous crash
	 *
	 * it is definitely not recommended to run two instances at the same time to avoid
	 * an inconsistent database
	 *
	private static final String LOCKED_MESSAGE =
			"-------------------------------------------------------\n"+
			" if another instance of jose is running on your system,\n"+
			" please quit it now\n"+
			"\n"+
			" if no other instance is running, \n"+
			" you should ignore this message\n"+
			"-------------------------------------------------------\n"+
			" please note: you should never run two instances at the \n"+
			" same time to ensure the integrity of the database \n"+
			"-------------------------------------------------------\n";
*/

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	public static Application	theApplication;

	/**	directory where language.properties files are stored	 */
	public File					theLanguageDirectory;

	/**	database Id	 */
	public String				theDatabaseId;
//	public int					theDatabaseMode;

	/**	game mode	*/
	public int					theMode;

	/**	database directory (for embeded databases only)
	 * 	default is <working directory> / database
	 */
	public static File			theDatabaseDirectory;

    /** game history    */
    public static History       theHistory;

	/**	global context menu	 */
//	public ContextMenu			theContextMenu;

	/**	globals configuration	 */
	public Config				theConfig;

	/** export/print config; created on demand */
	public ExportConfig         theExportConfig;

    /** the component that has the keyboard focus (may be null) */
    public Component            theFocus;

	/**	ECO classificator	*/
	public ECOClassificator		theClassificator;
	static Object loadClassificator = new Object(); // lock

	/** Move Announcements (by sound)   */
	public SoundMoveFormatter   theSoundFormatter;

	/** opening books   */
	public OpeningLibrary theOpeningLibrary;

    private ApplicationListener applListener;

	/**	context menu (when user clicks right mouse button)	*/
	private ContextMenu			contextMenu;

	/** performs a clean shutdown on System.exit()
	 *  e.g. when called externally
	 */
	private Thread              shutdownHook;

	/** help system */
	protected HelpSystem        helpSystem;
	protected Rectangle			helpBounds;

	protected boolean shownFRCWarning=false;

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------


	public Application()
		throws Exception
	{
		super();

		if (theApplication != null)
			throw new IllegalStateException("Application must not be initialized twice");

		theApplication	        = this;

        theWorkingDirectory     = getWorkingDirectory();
//		System.out.println("working dir="+theWorkingDirectory);
/*
		try {
			//  chess fonts that are distributed with jose
			File joseFontPath = new File(theWorkingDirectory,"fonts");
			//	IMPORTANT: make this call before the graphics system is initialized
			ClassPathUtil.setUserFontPath(joseFontPath.getAbsolutePath());
			//  @deprecated custom fonts are loaded on demand by FontEncoding
		} catch (Exception e) {
			error(e);
		}
*/
		if (!Version.getSystemProperty("jose.console.output",false))
		{	//  send standard output to error.log
			PrintStream out = new PrintStream(new FileOutputStream("error.log"));
			System.setOut(out);
			System.setErr(out);
		}

		theIconImage = Toolkit.getDefaultToolkit().createImage(
							theWorkingDirectory+File.separator+ICON_IMAGE);

        if (Version.getSystemProperty("jose.splash",true))
        {
            SplashScreen splash = SplashScreen.open();
            splash.setImageDir(new File(theWorkingDirectory,"images"));
            WinUtils.setTopMost(splash);
        }

		if (!Version.getSystemProperty("jose.3d",true))
			Version.disable3d();

//		theDatabaseMode		= JoConnection.READ_WRITE;
		boolean showSystemProperties    = Version.getSystemProperty("jose.debug.properties",false);

        theDatabaseId           = Version.getSystemProperty("jose.db",theDatabaseId);

        String dataDir          = Version.getSystemProperty("jose.datadir",null);
        if (dataDir != null)    theDatabaseDirectory = new File(dataDir).getCanonicalFile();

        showFrameRate           = Version.getSystemProperty("jose.framerate",false);
        logErrors               = Version.getSystemProperty("jose.log",true);
		showErrors				= Version.getSystemProperty("jose.show.errors",true);

		theLanguageDirectory = new File(theWorkingDirectory,"config");

		theConfig = new Config(new File(theWorkingDirectory,"config"));

        Toolkit.getDefaultToolkit().setDynamicLayout(true);
		//  improved screen repainting for JDK 1.6 (?)
		Version.setSystemProperty("sun.awt.noerasebackground", "true");
		//  improved text antialising for JDK 1.6
//		Version.setSystemProperty("swing.aatext","true");
//		antialiasing can be set by the user. don't override it.
		//  improved drag & drop gestures, since JDK 1.5
		Version.setSystemProperty("sun.swing.enableImprovedDragGesture","true");
		//  some Type1 fonts cause crashes
		Version.setSystemProperty("sun.java2d.noType1Font","true");

        if (Version.mac) {
            //  Mac OS X properties

            //	set Mac OS menu bar (menu bar on top of screen, not inside each window)
            Version.setDefaultSystemProperty("apple.laf.useScreenMenuBar","true");

            //  show grow box in lower-right corner ?
            //  I think this is the default for Aqua lnf, so we let it be...
            Version.setDefaultSystemProperty("apple.awt.showGrowBox","true");

            Version.setDefaultSystemProperty("apple.awt.brushMetalLook","false");
            Version.setDefaultSystemProperty("apple.awt.graphics.EnableLazyDrawing","true");

            //	turn on dynamic repainting during resize (still needed ?)
            Version.setDefaultSystemProperty("com.apple.mrj.application.live-resize","true");

            //  does this work ?
            Version.setSystemProperty("com.apple.mrj.application.apple.menu.about.name",
                    Version.getSystemProperty("jose.about.name","jose"));

            //  avoid too many disk reads in File Chooser
            Version.setDefaultSystemProperty("Quaqua.FileChooser.autovalidate","false");

//            Version.setDefaultSystemProperty("Quaqua.design","jaguar");
//            Version.setDefaultSystemProperty("Quaqua.TabbedPane.design","jaguar");
            Version.setDefaultSystemProperty("Quaqua.List.style","striped");
            Version.setDefaultSystemProperty("Quaqua.Table.style","striped");

            //  hint to file chooser: navigate into application bundles
            //  important since, among others, engines are stored in jose's bundle
            UIManager.put("JFileChooser.appBundleIsTraversable","always");
//          UIManager.put("JFileChooser.packageIsTraversable","always"); // don't navigate in packages

//		System.out.println("apple.laf.useScreenMenuBar="+Version.getSystemProperty("apple.laf.useScreenMenuBar"));
//		System.out.println("apple.awt.showGrowBox="+Version.getSystemProperty("apple.awt.showGrowBox"));
//		System.out.println("com.apple.mrj.application.apple.menu.about.name="+Version.getSystemProperty("com.apple.mrj.application.apple.menu.about.name"));
        }

        if (Version.java13) {
            if (! XMLUtil.preferImplementation(XMLUtil.CAUCHO))
            {
                XMLUtil.preferImplementation(XMLUtil.XERCES);
                XMLUtil.preferImplementation(XMLUtil.XALAN);
                //  otherwise: use the default implementation (whatever this may be ...)
            }
        }
        //  else: Java 1.4 has built-in XML support

		if (theDatabaseId == null)
			theDatabaseId = theConfig.getDefaultDataSource();
		if (theDatabaseId == null)
			theDatabaseId = DEFAULT_DATABASE;
		if (theDatabaseDirectory==null)
			theDatabaseDirectory = new File(theWorkingDirectory,"database");

		initSplashscreen(SplashScreen.get());

//		splashToFront();

		if (showSystemProperties) {
			System.getProperties().list(System.out);
//			System.out.println(System.getProperty("java.awt.fonts"));
//			UIDefaults def = UIManager.getLookAndFeelDefaults();
//			System.out.println(def);
		}

        /** register for FcousEvents   */
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.FOCUS_EVENT_MASK);

		if (Version.getSystemProperty("jose.debug.sql",false))
//			DriverManager.setLogWriter(new PrintWriter(new FileWriter(new File(theWorkingDirectory,"sql.log")),true));
            DriverManager.setLogWriter(new PrintWriter(System.err,true));

		/**	light weight menus do not mix well with Java3D
		 *	(the menus appear behind the J3D canvas :-(
		 */
		JPopupMenu.setDefaultLightWeightPopupEnabled(
				Version.getSystemProperty("jose.2d.light.popup",
						!Version.hasJava3d(false,false)));
	}

    protected void initSplashscreen(SplashScreen splash)
    {
        if (splash != null) {
            splash.setDatabase(theDatabaseId);
            splash.set3d(Version.hasJava3d(false,false));
            String[] logos = theConfig.getPaths("sponsor-splash");
            for (int i=0; i < logos.length; i++)
                splash.showSponsor(logos[i]);
        }
    }


    public static void parseProperties(String[] args)
    {
	    StringBuffer files = new StringBuffer();

        for (int i=0; i<args.length; i++)
        {
            String key = args[i];
            String value = null;

	        if (FileUtil.exists(key) || Util.isValidURL(key)) {
	            if (files.length() > 0) files.append(File.pathSeparator);
		        files.append(key);
		        continue;
	        }

            if (key.startsWith("-D") || key.startsWith("-d"))
                key = key.substring(2);
            else {
				if (key.startsWith("-"))
	                key = key.substring(1);
				if (!key.startsWith("jose."))
					key = "jose."+key;
			}

            int k = key.indexOf("=");
            if (k < 0) {
                if ((i+1) < args.length && args[i+1].indexOf("=")<=0)
                {
                    value = args[++i];
                    if (value.equals("=")) {
                        if ((i+1) < args.length)
                            value = args[++i];
                        //  key = value
                    }
                    else if (value.startsWith("=")) {
                        value = value.substring(1);
                        //  key =value
	                } else {
                        //  value might be a file or url
	                    if (FileUtil.exists(value) || Util.isValidURL(value)) {
		                    if (files.length() > 0) files.append(File.pathSeparator);
	                        files.append(value);
		                    value = null;
		                    continue;
	                    }
                    }
                    // else: key value
                }
	            //  else: key only
            }
            else if ((k+1)==key.length()) {
                if ((i+1) < args.length)
                    value = args[++i];
                key = key.substring(0,k);
                //  key= value
            }
            else {
                value = key.substring(k+1);
                key = key.substring(0,k);
                //  key=value
            }

	        if (key.equalsIgnoreCase("jose.file") || key.equalsIgnoreCase("jose.url")) {
		        //  add file
		        if (files.length() > 0) files.append(File.pathSeparator);
		        files.append(value);
		        continue;
	        }
			else {
				if (value==null) value = "true";
				Version.setSystemProperty(key,value);
	        }
        }

	    if (files.length() > 0)
		    Version.setSystemProperty("jose.file", files.toString());

    }

	public void splashToFront()
	{
        if (Version.windows)
            /* WinUtils already deals with it */ ;
        else {
            SplashScreen splash = SplashScreen.get();
            if (splash!=null && splash.isShowing()) splash.toFront();
        }
	}

    static class ApplicationListener extends Thread
    {
        protected ServerSocket inBound;

		ApplicationListener() {
			super("jose.appl-listener");
			setPriority(Thread.MIN_PRIORITY);
			setDaemon(true);
		}

        public void close() {
            try {
                if (inBound!=null) inBound.close();
                inBound = null;
            } catch (IOException e) {
                //  ignore
            }
        }

        public void run()
        {
            try {
				inBound = new ServerSocket(0x105E);

                while (inBound != null)
                    try {

                        Socket incoming = inBound.accept();
                        //  ping from other application; bring this application to the front
//						incoming.getInputStream().read();
                        JoFrame.activeToFront();

                        incoming.close();

                    }  catch (Exception ex) {
                        continue;
                    }

            } catch (Throwable e1) {
//              Application.error(e1);
				// couldn't create server socket (strict firewall ?)
            }
        }

		/**
		  * is there another instance running on the local machine ?
		  */
		 protected static boolean searchApplication()
		 {
			 /** try to connect  */
			 try {
				 byte[] localip = { 127,0,0,1 };
				 Socket outBound = new Socket(InetAddress.getByAddress(localip),0x105E);
//					 outBound.sendUrgentData(0x01);

				 try { outBound.close(); } catch (IOException e) { }

				 return true;    //  another running application was detected

			 } catch (Throwable e) {
				 //  connection failed = there is no other application = fine !
			 }
			 //  no other application was detected
			 return false;
		 }
	}

	//-------------------------------------------------------------------------------
	//	main entry point
	//-------------------------------------------------------------------------------


	public static void main(String[] args)
		throws Exception
	{
		parseProperties(args);

//		JoDialog.showMessageDialog(Util.toString(args));

		new Application().open();

/*
        String workDir = Version.getSystemProperty("jose.workdir", ".");
        workDir = new File(workDir).getCanonicalPath();

		/**	build our own class path !	* /
		Vector cp = new Vector();
		cp.add(ClassPathUtil.makeAllURLs(new File(workDir,"lib")));
		cp.add(ClassPathUtil.makeAllURLs(new File(workDir,"lib/jdbc")));
		cp.add(ClassPathUtil.makeAllURLs(new File(workDir,"lib/plaf")));
		cp.add(ClassPathUtil.makeAllURLs(new File(workDir,"lib/"+Version.osDir)));

		URL[] urls = (URL[])ListUtil.toFlatArray(cp, URL.class);

		ClassLoader myClassLoader = new URLClassLoader(urls,null);
		Class appClass = myClassLoader.loadClass("de.jose.Application");

		Application app = (Application)appClass.newInstance();
		app.open();
*/
	}

	//-------------------------------------------------------------------------------
	//	basic access
	//-------------------------------------------------------------------------------

	public final DocumentPanel docPanel()		{ return (DocumentPanel)JoPanel.get("window.game"); }
	public final ConsolePanel consolePanel()	{ return (ConsolePanel)JoPanel.get("window.console"); }
	public final ClockPanel clockPanel()		{ return (ClockPanel)JoPanel.get("window.clock"); }
    public final ListPanel listPanel()          { return (ListPanel)JoPanel.get("window.gamelist"); }
    public final QueryPanel queryPanel()          { return (QueryPanel)JoPanel.get("window.query"); }
    public final CollectionPanel collectionPanel()  { return (CollectionPanel)JoPanel.get("window.collectionlist"); }
	public final SymbolBar symbolToolbar()		{ return (SymbolBar)JoPanel.get("window.toolbar.symbols"); }
	public final EnginePanel enginePanel()      { return (EnginePanel) JoPanel.get("window.engine"); }

	public final JoDialog openDialog(String name) throws Exception
	{
		return openDialog(name,-1);
	}

	public final JoDialog openDialog(String name, int tab) throws Exception
	{
		JoDialog dialog = getDialog(name);
		openDialog(dialog,tab);
		return dialog;
	}

	public void openDialog(JoDialog dialog, int tab) throws Exception
	{
		dialog.read();

		if (dialog instanceof JoTabDialog && tab >= 0)
			((JoTabDialog)dialog).setTab(tab);

		dialog.show();
		dialog.toFront();
//		dialog.updateLanguage();    //  this will mess up label with contents; why was is needed ???
	}

	public final JoDialog getDialog(String name)
	{
		JoDialog dialog = JoDialog.getDialog(name);
		if (dialog==null)
			dialog = JoDialog.create(name);
		return dialog;
	}

	//-------------------------------------------------------------------------------
	//	Complex Methods
	//-------------------------------------------------------------------------------

	public final void setLanguage(String lang)
		throws IOException
	{
		Language.setLanguage(theLanguageDirectory,lang);
		if (theClassificator!=null) theClassificator.setLanguage(theLanguageDirectory,lang);

		broadcast(new Command("update.language", null, lang));
	}

	public final void setLookAndFeel(String lookAndFeel)
		throws Exception
	{
		String className = LookAndFeelList.loadLookAndFeel(lookAndFeel);

		if (className==null)
			JoDialog.showErrorDialog("error.lnf.not.supported");
		else
			try {
				UIManager.setLookAndFeel(className);
				broadcast(new Command("update.ui", null, lookAndFeel));
			} catch (UnsupportedLookAndFeelException usex) {
				JoDialog.showErrorDialog("error.lnf.not.supported");
			}
	}

	//-------------------------------------------------------------------------------
	//	interface ActionListener
	//-------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)
	{
		theCommandDispatcher.handle(e,this);
	}

    //-------------------------------------------------------------------------------
    //	interface AWTEventListener
    //-------------------------------------------------------------------------------

    public void eventDispatched(AWTEvent event)
    {
        /** we are only registered for FocusEvents  */
        FocusEvent fevt = (FocusEvent)event;

        if (!fevt.isTemporary())
            switch (event.getID()) {
//            case FocusEvent.FOCUS_LOST:
//                    theFocus = fevt.getOppositeComponent(); break;	//	since 1.4
            case FocusEvent.FOCUS_GAINED:
	                JoPanel oldFocusPanel = getFocusPanel();
//	                Component oldFocus = theFocus;

                    theFocus = fevt.getComponent();
	                JoPanel newFocusPanel = getFocusPanel();

//	                if (oldFocus!=theFocus)
//	                    broadcast(new Command("focus.changed",event,oldFocus,theFocus));
	            //  at the time, we don't need such fine grained broadcasts
	                if (oldFocusPanel!=newFocusPanel)
	                    broadcast(new Command("focus.panel.changed",event,oldFocusPanel,newFocusPanel));
	            //  it is good enough to notify changes in the panel (toolbars may adjust their buttons)
	                break;
            }
    }

	public ContextMenu getContextMenu()
	{
		if (contextMenu==null)
			synchronized (this)
			{
				if (contextMenu==null)
					contextMenu = new ContextMenu();
			}
		return contextMenu;
	}

	public boolean isContextMenuShowing()
	{
		return contextMenu!=null && contextMenu.isShowing();
	}

	public ECOClassificator getClassificator()
	{
		if (theClassificator==null)
			try {
				synchronized(loadClassificator) {
					if (theClassificator==null) {
						ECOClassificator classificator = new ECOClassificator(false);
						classificator.open(new File(theLanguageDirectory,"eco.key"));
						classificator.setLanguage(theLanguageDirectory, Language.theLanguage.langCode);
						theClassificator = classificator;
                    }
				}
			} catch (Exception e) {
				Application.error(e);
			}
		return theClassificator;
	}

	public ECOClassificator getClassificator(String language)
	{
		if (language.equals(Language.theLanguage.langCode))
			return getClassificator();

		ECOClassificator result = null;
		try {
			if (theClassificator != null)
				result = new ECOClassificator(theClassificator);
			else {
				result = new ECOClassificator(false);
				result.open(new File(theLanguageDirectory,"eco.key"));
			}
			result.setLanguage(theLanguageDirectory, language);
		} catch (Exception e) {
			Application.error(e);
		}

		return result;
	}

	public SoundMoveFormatter getSoundFormatter()
	{
		if (theSoundFormatter==null)
			try {
				File dir = (File)theUserProfile.get("sound.moves.dir");
				if (dir!=null && dir.exists()) {
					SoundMoveFormatter sform = new SoundMoveFormatter();
					sform.setDirectory(dir);
					sform.setPronounceMate(false);
					theSoundFormatter = sform;
				}
			} catch (IOException ioex) {
				Application.error(ioex);
			}

		return theSoundFormatter;
	}

	public void updateSoundFormatter(UserProfile profile) throws IOException
	{
		if (theSoundFormatter!=null)
		{
			File dir = (File)profile.get("sound.moves.dir");
			if (dir!=null && dir.exists())
				theSoundFormatter.setDirectory(dir);
			else
				theSoundFormatter = null;
		}
	}

	public void speakMove(int format, Move mv, Position pos)
	{
		getSoundFormatter();
		if (theSoundFormatter!=null)
			theSoundFormatter.format(format,mv,pos);
	}

	public void speakAcknowledge()
	{
		getSoundFormatter();
		if (theSoundFormatter==null || !theSoundFormatter.play("Oke.wav"))
			Sound.play("sound.notify");
	}

    /**
     * @return the currently focused component (may be null)
     */
    public Component getFocus() { return theFocus; }

    /**
     * @return the panel that contains the current focus; null if there is no focus,
     *  or if the focused compoment does not belong to a JoPanel
     */
    public JoPanel getFocusPanel()
    {
        if (theFocus==null) {
            //  get the frontmost panel
            JoFrame jframe = JoFrame.getActiveFrame();
            if (jframe!=null)
                return jframe.getAnchorPanel();
        }
        else {
            for (Component comp = theFocus; comp != null; comp = comp.getParent())
                if ((comp instanceof JoPanel) && ((JoPanel)comp).isFocusable())
                    return (JoPanel)comp;
        }
        //  no focus set, or not part of a JoPanel
        return null;
    }

	public void updateClock()
	{
		theUserProfile.getTimeControl().update(theClock,
										  theGame.getPosition().gameMove(),
										  theGame.getPosition().movedLast());
		theClock.setCurrent(theGame.getPosition().movesNext());
	}

	//-------------------------------------------------------------------------------
	//	interface CommandListener
	//-------------------------------------------------------------------------------

	public int numCommandChildren()
	{
		return JoFrame.countFrames() + JoDialog.countAllDialogs();
	}
	public CommandListener getCommandChild(int i)
	{
		if (i < JoFrame.countFrames())
			return JoFrame.getFrame(i);
		else
			return JoDialog.getAllDialogs() [i-JoFrame.countFrames()];
	}

	public void broadcast(Command cmd)
	{
		theCommandDispatcher.broadcast(cmd,this);
	}

	public void broadcast(String code)
	{
		broadcast(new Command(code,null,null));
	}

	public void setupActionMap(Map map)
	{
		CommandAction action;

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return AbstractApplication.theCommandDispatcher.canUndo();
			}

			public String getDisplayText(String code) {
				if (AbstractApplication.theCommandDispatcher.canUndo()) {
					HashMap params = new HashMap();
					CommandAction undoAction = AbstractApplication.theCommandDispatcher.getUndoAction();
					Command cmd = AbstractApplication.theCommandDispatcher.getUndoCommand();
					params.put("action", undoAction.getDisplayText(cmd.code));
					return StringUtil.replace(Language.get("menu.edit.undo"), params);
				}
				else
					return Language.get("menu.edit.cant.undo");
			}

			public void Do(Command cmd) {
				theCommandDispatcher.Undo();
			}
		};
		map.put("menu.edit.undo", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return AbstractApplication.theCommandDispatcher.canRedo();
			}

			public String getDisplayText(String code) {
				if (AbstractApplication.theCommandDispatcher.canRedo()) {
					HashMap params = new HashMap();
					CommandAction redoAction = AbstractApplication.theCommandDispatcher.getRedoAction();
					Command cmd = AbstractApplication.theCommandDispatcher.getRedoCommand();
					params.put("action", redoAction.getDisplayText(cmd.code));
					return StringUtil.replace(Language.get("menu.edit.redo"), params);
				}
				else
					return Language.get("menu.edit.cant.redo");
			}

			public void Do(Command cmd) {
				theCommandDispatcher.Redo();
			}
		};
		map.put("menu.edit.redo", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				try {
					gameMaintenance(cmd,null,null);
				} catch (Exception ex) {
					error(ex);
				}
			}
		};
		map.put("menu.edit.empty.trash", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				newCollection(cmd);
			}
		};
		map.put("menu.edit.collection.new", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				//  forward to collection panel
                GameSource src;
                Collection target = (Collection)cmd.moreData;   //  optional

                if (cmd.data instanceof GameSource)
                    src = (GameSource)cmd.data;    //  game source explicitly passed
                else if (getFocusPanel() instanceof ClipboardOwner)
                    src = null;   //  focused panel can handle clipboard
                else
                    src = getGameSource(cmd,false);
                //  collectionOnly==false: all sources can act as copy sources

				if (src!=null) {
					//	game/collection (DB) operation
					gameMaintenance(cmd, src, target);
				}
				else {
					//	system clipboard operation
					theCommandDispatcher.forward(cmd, getFocusPanel());
				}
			}
		};
		map.put("menu.edit.cut", action);
		map.put("menu.edit.copy", action);
		map.put("menu.edit.clear", action);
		map.put("menu.edit.restore", action);
		map.put("menu.edit.erase", action);
		map.put("menu.edit.collection.crunch", action);


        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                //  forward to collection panel
                GameSource src;
                Collection target = (Collection)cmd.moreData;   //  optional

                if (cmd.data instanceof GameSource)
                    src = (GameSource)cmd.data;    //  game source explicitly passed
                else if (getFocusPanel() instanceof ClipboardOwner)
                    src = null;   //  focused panel can handle clipboard
                else
                    src = getGameSource(cmd,target==null);
                //  collectionOnly==true: only collection can act as paste targets

                if (src!=null) {
                    //	game/collection (DB) operation
                    gameMaintenance(cmd, src, target);
                }
                else {
                    //	system clipboard operation
                    theCommandDispatcher.forward(cmd, getFocusPanel());
                }
            }
        };
        map.put("menu.edit.paste", action);
        map.put("menu.edit.paste.copy", action);
        map.put("menu.edit.paste.same", action);


		/**
		 * forward to the document panel
		 * but keep here for accessibility
		 */
		action = new CommandAction() {
			public CommandListener forward(CommandListener current)
			{
				JoPanel doc = docPanel();
				if (doc!=null && doc.isShowing())
					return doc;
				else
					return null;
			}
		};
		map.put("menu.edit.bold",action);
		map.put("menu.edit.italic",action);
		map.put("menu.edit.underline",action);
		map.put("menu.edit.plain",action);

		map.put("menu.edit.larger",action);
		map.put("menu.edit.smaller",action);
		map.put("menu.edit.color",action);

		map.put("menu.edit.left",action);
		map.put("menu.edit.center",action);
		map.put("menu.edit.right",action);

		map.put("move.format.short",action);
		map.put("move.format.long",action);
		map.put("move.format.algebraic",action);
		map.put("move.format.correspondence",action);
		map.put("move.format.english",action);
		map.put("move.format.telegraphic",action);

        map.put("figurine.usefont.true",action);
        map.put("figurine.usefont.false",action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				//  paste a variation line into the current document
				MoveNode mvnd = null;
				if (cmd.data instanceof NalimovOnlineQuery) {
					NalimovOnlineQuery nq = (NalimovOnlineQuery)cmd.data;
					mvnd = GameUtil.pasteLine(nq.getGame(), nq.getMoveNode(), nq.getText());
				}
				else
					mvnd = GameUtil.pasteLine(theGame, theGame.getCurrentMove(), cmd.data.toString());

				if (Util.toboolean(cmd.moreData) && mvnd!=null) {
					//  ... and replay the line
					cmd = new Command("move.goto", null, mvnd);
					Application.theCommandDispatcher.forward(cmd, Application.this);
				}
				else {
					//  notify, though position was not changed
				cmd = new Command("move.notify",null,null,Boolean.TRUE);
				broadcast(cmd);
			}
			}
		};
		map.put("menu.game.paste.line",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                GameSource src = getGameSource(cmd,false);
                CreatePositionIndex2 task = new CreatePositionIndex2();
                task.setSource(src);
                task.start();
            }
        };
        map.put("menu.edit.position.index",action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                GameSource src = getGameSource(cmd,false);
                if (src!=null) {
					EcofyTask task = new EcofyTask();
					task.setSource(src);
					if (task.askParameters(JoFrame.theActiveFrame, theUserProfile))
						task.start();
                }
            }
        };
        map.put("menu.edit.ecofy",action);


        
		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				//  forward to collection panel
				GameSource src = (GameSource)cmd.data;
				Collection target = (Collection)cmd.moreData;

				//	game/collection related
				gameMaintenance(cmd, src, target);
			}
		};
		map.put("dnd.move.games",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				//	move collection to top level
				GameSource src = (GameSource)cmd.data;
                Collection target = (Collection)cmd.moreData;   //  not needed here, but who knows...
				gameMaintenance(cmd, src, target);
			}
		};
		map.put("dnd.move.top.level",action);


        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                getHelpSystem().init();

                Component focus = (Component)cmd.data;
				if (focus==null) getFocus();
                if (focus==null) focus = getFocusPanel();

                getHelpSystem().show(focus);
            }
        };
        map.put("menu.help.context", action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
                getHelpSystem().init();
                getHelpSystem().showHome();
            }
        };
        map.put("menu.help.manual", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws IOException {
				URL url = (URL)cmd.data;
				BrowserWindow.showWindow(url);
			}
		};
		map.put("menu.web.home",action);
		map.put("menu.web.download",action);
		map.put("menu.web.report",action);
		map.put("menu.web.feature",action);
		map.put("menu.web.support",action);
		map.put("menu.web.forum",action);
        map.put("menu.web.donate",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws IOException
			{
				//  show donwload page in browser
				String url = theConfig.getURL("book-download");
				if (url!=null)
					BrowserWindow.showWindow(url);
			}
		};
		map.put("book.list.download",action);

		action = new CommandAction() {
			public void Do(Command cmd)
					throws Exception
			{
				//  download one file
				URL url = new URL((String) cmd.data);

				final File dir = new File(theWorkingDirectory, "books");
				dir.mkdirs();
				final File file = FileUtil.uniqueFile(dir, FileUtil.getFileName(url));
				final OptionDialog dialog = (OptionDialog)openDialog("dialog.option",7);

				Runnable addbook = new Runnable() {
					public void run()
					{
						try {

							File[] files;
							if (FileUtil.hasExtension(file.getName(),"zip")) {      //  unzip
								files = FileUtil.unzip(file, dir);
								file.delete();
							}
							else
								files = new File[] { file };

							dialog.show(5);
							dialog.addBooks(files);

						} catch (IOException e) {
							Application.error(e);
						}
					}
				};

				FileDownload fd = new FileDownload(url, file, -1);
				fd.setOnSuccess(addbook);

				fd.start();
			}
		};
		map.put("book.file.download",action);

		/**
		 * check for Online-Update
		 */
		action = new CommandAction() {
		    public void Do(Command cmd)
		    {
		        OnlineUpdate.check();
		    }
		};
		map.put("menu.web.update", action);

		/**
		 * Online-Bug Reports
		 *
		action = new CommandAction() {
		    public void Do(Command cmd) throws IOException
		    {
		        OnlineReport report = new OnlineReport();
				report.setType(OnlineReport.BUG_REPORT);
				report.setSubject(Language.get("online.report.default.subject"));
				report.setDescription(Language.get("online.report.default.description"));
				report.setEmail(Language.get("online.report.default.email"));

				File log = new File(theWorkingDirectory,"error.log");
				if (log.exists() && log.length() > 20)
					report.addAttachment(log);

				report.show();
		    }
		};
		map.put("menu.web.report", action);
		*/

		/**
		 * perform Online-Update !
		 */
		action = new CommandAction() {
		    public void Do(Command cmd) throws Exception
		    {
			    //  shut down the program (without quitting the JVM)
			    Thread oldHook = shutdownHook;
			    shutdownHook = null;

			    if (!quit(cmd)) {
				    shutdownHook = oldHook;
				    return;   //  user cancelled quit - allright
			    }

			    //  let's do the update
			    File zipFile = (File)cmd.data;
			    String newVersion = (String)cmd.moreData;

			    OnlineUpdate.update(zipFile,newVersion);

			    //  and exit
			    System.exit(+2);
		    }
		};
		map.put("system.update", action);


		action = new CommandAction() {
			public void Do(Command cmd) {
				BrowserWindow.getBrowser(BrowserWindow.ALWAYS_ASK);
			}
		};
		map.put("menu.web.browser",action);


        action = new CommandAction() {
            public boolean isSelected(String code) {
                return getHelpSystem().isShowing();
            }

            public void Do(Command cmd) throws Exception {
                getHelpSystem().init();
                getHelpSystem().show();
            }
        };
        map.put("window.help", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				openDialog("dialog.about");
//				initSplashscreen(SplashScreen.open());
//              splashToFront();
			}
		};
		map.put("menu.help.splash", action);
		map.put("menu.help.about", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				openDialog("dialog.about",5);
			}
		};
		map.put("menu.help.license", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				if (cmd.data instanceof Number)
					openDialog("dialog.option", ((Number)cmd.data).intValue());		//	go to specific tab
				else
					openDialog("dialog.option");
			}
		};
		map.put("menu.edit.option", action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				String fen = null;
				if (cmd.data!=null) fen = (String)cmd.data;

				SetupDialog setup = (SetupDialog)openDialog("dialog.setup");
				if (fen!=null)
					try {
						setup.setFEN(fen.trim());
					} catch (Throwable thr) {
						//  invalid FEN
						JoDialog.showErrorDialog("dialog.setup.invalid.fen");
					}
			}
		};
		map.put("menu.game.setup", action);

		action = new CommandAction() {
			public boolean isEnabled(String cmd) {
				return 	(theGame.getResult()==Game.RESULT_UNKNOWN) &&
						(getEnginePlugin()!=null);
			}

			public void Do(Command cmd) throws Exception
			{
				EnginePlugin xplug = getEnginePlugin();
				if (xplug.hasOfferedDraw()) {
					//	accept draw from engine
					gameDraw();
				}
				else {
					xplug.offerDrawToEngine();
				}
			}
		};
		map.put("menu.game.draw", action);

		action = new CommandAction() {
			public boolean isEnabled(String cmd) {
				return (theGame.getResult()==Game.RESULT_UNKNOWN);
			}

			public void Do(Command cmd)  throws Exception
			{
				int whichColor = theGame.getPosition().movesNext();
				if (getEnginePlugin()!=null) {
					if (getEnginePlugin().isThinking())
						whichColor = theGame.getPosition().movedLast();
					getEnginePlugin().pause();
				}
				theClock.halt();

				if (EngUtil.isWhite(whichColor))
					theGame.setResult(Game.BLACK_WINS,theGame);
				else
					theGame.setResult(Game.WHITE_WINS,theGame);
//				if (docPanel()!=null) docPanel().reformat();
			}
		};
		map.put("menu.game.resign", action);

		action = new CommandAction() {
			public boolean isEnabled(String cmd) { return true; }

			public boolean isSelected(String cmd) { return boardPanel()!=null && boardPanel().is2d(); }

			public void Do(Command cmd) {
				showPanel("window.board");
				if (!boardPanel().is2d()) {
					boardPanel().set2d();
					boardPanel().getView().refresh(true);
				}
			}
		};
		map.put("menu.game.2d", action);

		action = new CommandAction() {
			public boolean isEnabled(String cmd) { return Version.hasJava3d(false,false); }

			public boolean isSelected(String cmd) {	return boardPanel()!=null && boardPanel().is3d(); }

			public void Do(Command cmd) {
				showPanel("window.board");
				if (!boardPanel().is3d()) {
					try {
						boardPanel().set3d();
					} catch (Exception ex) {
						Application.error(ex);
						boardPanel().set2d();
					}
					boardPanel().getView().refresh(true);
				}
			}
		};
		map.put("menu.game.3d", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  command from BoardPanel
				Move mv = (Move)cmd.data;
				handleUserMove(mv,false);
			}
		};
		map.put("move.user", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return ! Application.theApplication.theGame.isFirst();
			}

			public void Do(Command cmd) {
				if (theGame.first()) {
					updateClock();
	                getAnimation().pause();
					pausePlugin(theMode==ANALYSIS);
					cmd.code = "move.notify";
					cmd.moreData = null;
					broadcast(cmd);
				}
			}
		};
		map.put("move.first", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return ! Application.theApplication.theGame.isFirst();
			}

			public void Do(Command cmd) {
				Move mv = theGame.backward();
				updateClock();
				if (mv != null) {
					getAnimation().pause();
					pausePlugin(theMode==ANALYSIS);
					cmd.code = "move.notify";
					cmd.moreData = null;
					broadcast(cmd);
				}
			}
		};
		map.put("move.backward", action);

        action = new CommandAction() {
            public void Do(Command cmd) {
                MoveNode mv = (MoveNode)cmd.data;
                theGame.gotoMove(mv);
                theClock.halt();
                getAnimation().pause();
                pausePlugin(theMode==ANALYSIS);

                cmd = new Command("move.notify",null,mv.getMove());
                broadcast(cmd);
            }
        };
        map.put("move.goto", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return theGame.canDelete();
			}

			public void Do(Command cmd) throws Exception {
				cutLine(theGame.getCurrentMove());
			}
		};
		map.put("move.delete", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				int speed = Util.toint(cmd.data);
				boolean hints = Util.toboolean(cmd.moreData);

				theUserProfile.set("animation.speed",speed);
				theUserProfile.set("board.animation.hints",hints);

				getAnimation().setSpeed(speed);
			}
		};
		map.put("change.animation.settings",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws BadLocationException {
				cutLine((Node)cmd.data);
			}
		};
		map.put("doc.menu.line.delete",action);
		map.put("doc.menu.line.cut",action);

		action = new CommandAction(CommandAction.UNDO_MANY_REDO) {
			public boolean isEnabled(String code) {
				return ! Application.theApplication.theGame.isLast();
			}
			public void Do(Command cmd) {
				Move mv = theGame.forward();
				updateClock();

				if (mv != null) {
					 pausePlugin(theMode==ANALYSIS);
				     if (boardPanel() != null) {
				         float speed = (float)(mv.distance()*0.2);
				         boardPanel().move(mv, speed);
				     }
					cmd.code = "move.notify";
					cmd.moreData = null;
					broadcast(cmd);
				 }
			}
/*			public void Undo(Command cmd) {		//	EXPERIMENTAL
				Move mv = theGame.backward();
				updateClock();
				if (mv != null) {
					 pausePlugin();
				     if (boardPanel() != null) {
				         float speed = (float)(mv.distance()*0.2);
				         boardPanel().move(mv, speed);
				     }
					cmd.code = "move.notify";
					broadcast(cmd);
				 }
			}
*/		};
		map.put("move.forward", action);

		action = new CommandAction() {
/*			public boolean isEnabled(String code) {
				return Application.theApplication.thePlugin==null ||
				       ! Application.theApplication.thePlugin.isThinking();
			}
*/
			public void Do(Command cmd) throws Exception
			{
				updateClock();
				getAnimation().pause();

				if (getEnginePlugin()!=null && getEnginePlugin().isThinking())
					getEnginePlugin().moveNow();
				else if (theGame.getPosition().isMate() || theGame.getPosition().isStalemate())
					Sound.play("sound.error");
				else if (selectBookMove()) {
					/* move chosen from book,alright */
					setGameDefaultInfo();
					theMode = USER_ENGINE;
				}
				else
				{
				invokeWithPlugin(new Runnable() {
					public void run() {
						if (!theGame.getPosition().isClassic() && !getEnginePlugin().supportsFRC())
							showFRCWarning(false);
							//  but keep on playing (you have been warned ;-)
						if (getEnginePlugin().isThinking())
							getEnginePlugin().moveNow();
						else {
							//	thePlugin.setTime(clockPanel().getWhite/BlackTime());
							//	adjust time ? or rely on engine's time keeping ?
							setGameDefaultInfo();
							getEnginePlugin().go();
							theMode = USER_ENGINE;
						}
					}
				});
			}
			}
		};
		map.put("move.start", action);

		action = new CommandAction() {
			/**	this command has tow meanings: stop te engine and/or stop the animation	*/
			public boolean isEnabled(String code) {
				return 	getAnimation().isRunning() ||
						(getEnginePlugin()!=null) && !getEnginePlugin().isPaused();
			}

			public void Do(Command cmd) {
				getAnimation().pause();
				pausePlugin(false);
				theMode = USER_INPUT;
			}
		};
		map.put("engine.stop", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return ! Application.theApplication.theGame.isLast();
			}

			public void Do(Command cmd) throws Exception {
				openDialog("dialog.animate");
				pausePlugin(false);
				getAnimation().start();
			}
		};
		map.put("move.animate", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return ! Application.theApplication.theGame.isLast();
			}

			public void Do(Command cmd) {
				if (theGame.last()) {
					updateClock();
	                getAnimation().pause();
					pausePlugin(theMode==ANALYSIS);
					cmd.code = "move.notify";
					cmd.moreData = null;
					broadcast(cmd);
				}
			}
		};
		map.put("move.last", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				openDialog("dialog.animate");
			}
		};
		map.put("menu.game.animate", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				prepareNewGame(null);
				switchGame(theHistory.currentIndex());

				cmd.code = "move.notify";
				cmd.moreData = null;
				broadcast(cmd);
			}
		};
		map.put("menu.file.new", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  show setup dialog with random FRC (?)
/*
				SetupDialog setup = (SetupDialog)openDialog("dialog.setup");
				setup.enableAllCastlings();
				setup.setFRCIndex(-1);
*/
				String randomFen = Board.initialFen(Board.FISCHER_RANDOM,-1);
				Command newCmd = new Command("new.game.setup",null,randomFen);
				theCommandDispatcher.forward(newCmd, Application.this);
			}
		};
		map.put("menu.file.new.frc", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				//  show setup dialog with random FRC (?)
				String randomFen = Board.initialFen(Board.SHUFFLE_CHESS, -1);
				Command newCmd = new Command("new.game.setup",null,randomFen);
				theCommandDispatcher.forward(newCmd, Application.this);
			}
		};
		map.put("menu.file.new.shuffle", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				String fen = cmd.data.toString();
				if (! theGame.isEmpty()) {
					prepareNewGame(fen);
					switchGame(theHistory.currentIndex());
				}
				else
					theGame.setup(fen);

				if (!theGame.getPosition().isClassic())
					theGame.setTagValue(PgnConstants.TAG_VARIANT,"Fischer Random Chess");

				theGame.reformat();
				cmd.code = "move.notify";
				cmd.moreData = Boolean.TRUE;
				broadcast(cmd);
			}
		};
		map.put("new.game.setup", action);

		action = new CommandAction() {
			public void Do(Command cmd)
			{
				openFile();
			}
		};
		map.put("menu.file.open", action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                openURL();
            }
        };
        map.put("menu.file.open.url", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception
			{
				showPanel("window.collectionlist");
				showPanel("window.gamelist");

				StringTokenizer tokens = new StringTokenizer((String)cmd.data,File.pathSeparator);
				while (tokens.hasMoreTokens())
				try {

					String tk = tokens.nextToken();
					if (FileUtil.exists(tk)) {
						if (FileUtil.hasExtension(tk,"jos") || FileUtil.hasExtension(tk,"jose"))
							new ArchiveImport(new File(tk)).start();
						else
							PGNImport.openFile(new File(tk));
					}
					else if (Util.isValidURL(tk)) {
						if (FileUtil.hasExtension(tk,"jos") || FileUtil.hasExtension(tk,"jose"))
							new ArchiveImport(new URL(tk)).start();
						else
							PGNImport.openURL(new URL(tk));
					}
					else
						JoDialog.showErrorDialog(null,"download.error.invalid.url","p",tk);

				} catch (FileNotFoundException ex) {
					JoDialog.showErrorDialog("File not found: "+ex.getLocalizedMessage());
				} catch (Exception ex) {
					Application.error(ex);
					JoDialog.showErrorDialog(ex.getLocalizedMessage());
				}
			}
		};
		map.put("menu.file.open.all",action);


		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				PrintableDocument prdoc = null;

				if (cmd.data instanceof PrintableDocument)
					prdoc = (PrintableDocument)cmd.data;
				else if (cmd.data instanceof ExportContext) {
					ExportContext context = (ExportContext)cmd.data;

					if (context.preview!=null)
						prdoc = context.preview;   // called from preview; print this document
					else {
						ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
						if (dlg.confirmPrint(context.source,25)) {
							prdoc = context.createPrintableDocument();   // create document then print
                            dlg.hide();
                        }
					}
				}
				else
					throw new IllegalArgumentException();

				if (prdoc!=null)
					prdoc.print();
			}
		};
		map.put("export.print",action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				ExportContext context = (ExportContext)cmd.data;

				ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
				if (!dlg.confirmExport(context.source)) return;

                dlg.hide();

				switch (context.getOutput())
				{
				case ExportConfig.OUTPUT_HTML:
					HtmlUtil.createCollateral(context);
					HtmlUtil.exportFile(context,true);
					break;

				case ExportConfig.OUTPUT_XML:
				case ExportConfig.OUTPUT_TEX:
					HtmlUtil.exportFile(context,true);
					break;
				case ExportConfig.OUTPUT_XSL_FO:
					//  setup XSL-FO exporter with appropriate style sheet
					Version.loadFop();
					XSLFOExport fotask = new XSLFOExport(context);
					fotask.start();   //  don't wait for task to complete
					break;
                case ExportConfig.OUTPUT_PGN:
	                PGNExport pgntask = new PGNExport(context.target);
	                pgntask.setSource(context.source);
	                pgntask.start();
	                break;
				case ExportConfig.OUTPUT_ARCH:
					ArchiveExport arctask = new ArchiveExport((File)context.target);
					arctask.setSource(context.source);
					arctask.start();
				    break;
				default:
					throw new IllegalArgumentException();   //  TODO
				}
			}
		};
		map.put("export.disk",action);

		action = new CommandAction() {
            public boolean isEnabled(String code) {
                return /*theGame.isNew() ||*/ theGame.isDirty();
            }
			public void Do(Command cmd) throws Exception {
				saveGame(theGame);
			}
		};
		map.put("menu.file.save", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return hasGameSource(null,false);
			}

			public void Do(Command cmd) throws Exception {
				//  export selected files

				ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
				dlg.forExport(getGameSource(cmd,false));
				openDialog(dlg,0);
			}
		};
		map.put("menu.file.save.as", action);

        action = new CommandAction()  {
            public boolean isEnabled(String code) {
                return theHistory.isDirty();
            }
            public void Do(Command cmd) throws Exception {
                theHistory.saveAll();
            }
        };
        map.put("menu.file.save.all", action);

		action = new CommandAction()  {
			//  CommandAction.NEW_THREAD if long-running
			public boolean isEnabled(String code) {
				return hasGameSource(null,false);
			}
			public void Do(Command cmd) throws Exception
			{
				ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
				dlg.forPrint(getGameSource(cmd,false));
				openDialog(dlg,0);
			}
		};
		map.put("menu.file.print", action);

		action = new CommandAction()  {
			public boolean isEnabled(String code) {
				return hasGameSource(null,false);
			}
			public void Do(Command cmd) throws Exception
			{
				PrintPreviewDialog prvdlg;

				if (cmd.data!=null && cmd.data instanceof ExportContext) {
					//  called rom ExportDialog
					ExportContext context = (ExportContext)cmd.data;
					boolean preferInternal = Util.toboolean(cmd.moreData);

					ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
					if (!dlg.confirmPreview(context.source,25)) return;

                    dlg.hide();

					boolean internal = ExportConfig.canPreview(context.config);
					boolean external = ExportConfig.canBrowserPreview(context.config);

					if (internal && external) {
						if (preferInternal)
							external = false;
						else
							internal = false;
					}

					if (internal) {
						//  internal preview
						prvdlg = (PrintPreviewDialog)createPanel("window.print.preview");
						prvdlg.setContext(context);
						showPanel(prvdlg);
						prvdlg.reset();
					}
					else if (external) {
						//  preview in Web Browser
						HtmlUtil.exportTemporary(context,false);
						URL url = new URL("file",null,((File)context.target).getAbsolutePath());
						BrowserWindow.showWindow(url);
					}
					else
						throw new IllegalArgumentException();
				}
				else {
					//  called from menu
					ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
					dlg.forPrint(getGameSource(cmd,false));
					openDialog(dlg,0);
				}
			}
		};
		map.put("menu.file.print.preview", action);
		map.put("window.print.preview", action);

		action = new CommandAction()  {
			public boolean isEnabled(String code) {
				return true;
			}
			public void Do(Command cmd) throws Exception
			{
				ExportDialog dlg = (ExportDialog)getDialog("dialog.export");
				dlg.forPrint(getGameSource(cmd,false));
				openDialog(dlg,1);
			}
		};
		map.put("menu.file.print.setup", action);

		action = new CommandAction() {
            public void Do(Command cmd) throws Exception {
				GameDetailsDialog dialog = (GameDetailsDialog)getDialog("dialog.game");
				JoPanel parentPanel = null;

				if (cmd.data==null && theGame!=null) {
					//	edit current game
					dialog.setGame(theGame);
				}
				else if (cmd.data instanceof GameSource) {
					//	edit database game
					int GId = ((GameSource)cmd.data).firstId();
					Game gm = theHistory.getById(GId);
					if (gm!=null) {
						//	game is open: switch
						if (gm!=theGame) switchGame(theHistory.indexOf(gm));
						dialog.setGame(gm);
					}
					else {
						//	game is not open: just edit
						dialog.setGameId(GId);
						parentPanel = listPanel();
					}
				}
				else
					throw new IllegalArgumentException(String.valueOf(cmd.data));

				if (parentPanel == null)
					parentPanel = docPanel();
				if (parentPanel!=null && parentPanel.isShowing())
					dialog.stagger(parentPanel, 10,10);

                openDialog(dialog,1);
            }
        };
        map.put("menu.game.details", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				invokeWithPlugin(new Runnable() {
					public void run()
					{
						boolean engine_analysis = false;
						EnginePanel eng_panel = enginePanel();
						eng_panel.setVisible(true);

						if (eng_panel.inBook)
							engine_analysis = true; //  already in book, switch to engine mode
						else try {
							//  (1) fetch book moves
							engine_analysis = ! eng_panel.updateBook();
						} catch (IOException e) {
							error(e);
					}

						//  (2) enter engine analysis mode
						pausePlugin(engine_analysis);
						theMode = engine_analysis ? ANALYSIS : USER_INPUT;
					}
				});
			}
		};
		map.put("menu.game.analysis",action);

        action = new CommandAction() {
            public boolean isEnabled(String code) {
                return theHistory.hasPrevious();
            }

            public void Do(Command cmd) {
                if (theHistory.hasPrevious())
                    switchGame(theHistory.currentIndex()-1);
            }
        };
        map.put("menu.game.previous", action);

        action = new CommandAction() {
            public boolean isEnabled(String code) {
                return theHistory.hasNext();
            }

            public void Do(Command cmd) {
                if (theHistory.hasNext())
					switchGame(theHistory.currentIndex()+1);
            }
        };
        map.put("menu.game.next", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return theHistory.size() > 0;
			}

			public void Do(Command cmd) throws Exception
			{
				Game g;
				if (cmd.data != null && (cmd.data instanceof Integer)) {
					int gidx = ((Integer)cmd.data).intValue();
					g = theHistory.get(gidx);
				}
				else
					g = theGame;

				if (theGame.isDirty())
					switch (confirmSaveOne()) {
					case JOptionPane.YES_OPTION:	saveGame(g); break;
					case JOptionPane.NO_OPTION:		g.clearDirty();
                                                    //  important to adjust dirty indicators
                                                    break;
					case JOptionPane.CANCEL_OPTION:	return;
					}

				theHistory.remove(g);
				if (theHistory.size()== 0)
					prepareNewGame(null);
				switchGame(theHistory.currentIndex());
			}
		};
		map.put("menu.game.close", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return theHistory.size() > 1;
			}

			public void Do(Command cmd) throws Exception {
				if (theHistory.isDirty())
					switch (confirmSaveAll()) {
					case JOptionPane.YES_OPTION:	theHistory.saveAll(); break;
					case JOptionPane.NO_OPTION:		theHistory.clearDirty(); break;
					case JOptionPane.CANCEL_OPTION:	return;
					}

				theHistory.removeAll();
//				theGame = new Game(theUserProfile.getStyleContext(),
//								   null,null, PgnUtil.currentDate(), null, null);
				prepareNewGame(null);
				switchGame(0);
			}
		};
		map.put("menu.game.close.all", action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return theHistory.size() > 1;
			}

			public void Do(Command cmd) throws Exception {
				int gidx;
				if (cmd.data != null && (cmd.data instanceof Integer))
					gidx = ((Integer)cmd.data).intValue();
				else
					gidx = theHistory.currentIndex();

				if (theHistory.isDirtyBut(gidx))
					switch (confirmSaveAll()) {
					case JOptionPane.YES_OPTION:	theHistory.saveAllBut(gidx); break;
					case JOptionPane.NO_OPTION:		break;
					case JOptionPane.CANCEL_OPTION:	return;
					}

				theHistory.removeAllBut(gidx);
				theGame = theHistory.get(0);
				switchGame(0);
			}
		};
		map.put("menu.game.close.all.but", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws IOException {
				restartPlugin();
			}
		};
		map.put("restart.plugin",action);

		action = new CommandAction() {
			public boolean isEnabled(String code) {
				return getEnginePlugin() != null;
			}
			public void Do(Command cmd)
					throws IOException
			{
				Position pos = theGame.getPosition();
				BookEntry hint = theOpeningLibrary.selectMove(pos, true, pos.whiteMovesNext());
				if (hint!=null) {
					//  (1) Hint from Opening Library
					Application.this.handleMessage(theOpeningLibrary, Plugin.PLUGIN_REQUESTED_HINT, hint.move);
					if (enginePanel()!=null)
						enginePanel().handleMessage(theOpeningLibrary, Plugin.PLUGIN_REQUESTED_HINT, hint.move);
				}
				else if (getEnginePlugin()!=null)
				{
					//  (2) Hint from Plugin
					Runnable gethint = new Runnable() {
						public void run() { getEnginePlugin().getHint(); }
					};
					invokeWithPlugin(gethint);
					//	plugin will eventually respond with a Hint message
				}
			}
		};
		map.put("menu.game.hint",action);

		action = new CommandAction()
		{
			public boolean isSelected(String code) {
				return AbstractApplication.theUserProfile.getBoolean("board.flip");
			}
			public void Do(Command cmd) {
				boolean flipped = theUserProfile.getBoolean("board.flip");
				flipped = !flipped;
				theUserProfile.set("board.flip", flipped);
				broadcast(new Command("broadcast.board.flip", cmd.event, Util.toBoolean(flipped)));
			}
		};
		map.put("menu.game.flip", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				int idx = ((Integer)cmd.data).intValue();
				if (idx != theUserProfile.getTimeControlIdx())
				{
					theUserProfile.setTimeControlIdx(idx);
					TimeControl control = theUserProfile.getTimeControl();
					control.resetTime(theClock);
					if (getEnginePlugin()!=null)
						getEnginePlugin().setTimeControls(control.getPhase(0));
				}
			}
		};
		map.put("menu.game.time.control", action);

		action = new CommandAction() {
			public boolean isSelected(String code) {
				return AbstractApplication.theUserProfile.getBoolean("board.coords");
			}
			public void Do(Command cmd) {
				boolean showCoords = theUserProfile.getBoolean("board.coords");
				showCoords = !showCoords;
				theUserProfile.set("board.coords", showCoords);
				broadcast(new Command("broadcast.board.coords", Util.toBoolean(showCoords)));
			}
		};
		map.put("menu.game.coords", action);

		action = new CommandAction() {
			public CommandListener forward(CommandListener current)
			{
				return JoFrame.getActiveFrame();
			}
		};
		map.put("menu.window.fullscreen", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				/** Restore Factory Layout  */
				//  close all open frames
				JoFrame.closeAll();
				theUserProfile.createFactoryFrameLayout(null);
				JoPanel.updateAllPanels(theUserProfile);
				openFrames(theUserProfile.getFrameProfiles());
			}
		};
		map.put("menu.window.reset",action);

		action = new CommandAction() {
			public boolean isSelected(String code) {
				return JoPanel.isShowing(code);
			}

			public void Do(Command cmd) {
				showPanel(cmd.code);
			}
		};
		map.put("window.board", action);
		map.put("window.list", action);
		map.put("window.clock", action);
		map.put("window.game", action);
		map.put("window.toolbar.1", action);
		map.put("window.toolbar.2", action);
		map.put("window.toolbar.3", action);
		map.put("window.console", action);
		map.put("window.gamelist", action);
		map.put("window.collectionlist", action);
		map.put("window.query", action);
		map.put("window.sqlquery", action);
		map.put("window.sqllist", action);
		map.put("window.engine", action);
		map.put("window.eval",action);
        map.put("window.toolbar.symbols", action);

		action = new CommandAction() {
			public void Do(Command cmd)	throws Exception
			{
				Thread oldHook = shutdownHook;
				shutdownHook = null;

				if (!quit(cmd)) {
					shutdownHook = oldHook;
					return; //  user cancelled - allright
				}

				System.exit(+1);
			}
		};
		map.put("menu.file.quit", action);

		action = new CommandAction() {
			public void Do(Command cmd) throws Exception {
				GameSource src = getGameSource(cmd,false);
				PositionFilter posFilter = null;
				if (listPanel()!=null)
					posFilter = listPanel().getSearchRecord().makePositionFilter();

				boolean posChanged;
                if (cmd.code.equals("edit.game")) {
				    int GId = src.firstId();
                    posChanged = prepareEditGame(GId,posFilter);
                }
                else if (cmd.code.equals("menu.edit.paste.pgn")) {
	                String pgnText = ClipboardUtil.getPlainText(this);
	                if (pgnText!=null) pgnText = pgnText.trim();
	                if (pgnText==null || pgnText.length()==0) {
		                AWTUtil.beep(docPanel());
	                    return;
	                }
	                posChanged = prepareEditGame(pgnText,null);
                }
                else {
                    posChanged = prepareEditGames(src,posFilter);
                }

				boolean showPanel = (cmd.moreData==null) || ((Boolean)cmd.moreData).booleanValue();
				if (showPanel) showPanel("window.game");
				switchGame(theHistory.currentIndex());

				cmd.code = "broadcast.edit.game";
                broadcast(cmd);

				if (posChanged) {
					//	new game: goto last move
//                    cmd = new Command("move.last", null, null);
                    switch (theMode)
                    {
	                case USER_ENGINE:
		            case ENGINE_ENGINE:
		                    theMode=USER_INPUT;
		                    break;
                    }
					pausePlugin(theMode==ANALYSIS);
//                    broadcast(cmd);
				}
			}
		};
		map.put("edit.game", action);
        map.put("edit.all", action);
		map.put("menu.edit.paste.pgn", action);


		action = new CommandAction() {
            public boolean isEnabled(String code) {
                return !theGame.isNew() && theGame.isDirty();
            }
			public void Do(Command cmd) throws Exception
			{
				int GId = theGame.getId();
				theGame.reread(GId);

				pausePlugin(theMode==ANALYSIS);
				//  forward to ourself
				theCommandDispatcher.forward(new Command("move.last"),Application.this);
			}
		};
		map.put("menu.file.revert", action);

        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                //  saerch current position in database
                showPanel("window.query");
                queryPanel().searchCurrentPosition();
            }
        };
        map.put("menu.edit.search.current",action);


        action = new CommandAction() {
            public void Do(Command cmd) throws Exception
            {
                //  dump current frame layout
                File dumpFile = new File(theWorkingDirectory,"config/layout-dump.xml");
                PrintWriter pout = new PrintWriter(new FileWriter(dumpFile));
	            /** PrintWriter(File) since 1.5 !! */
                FrameProfile.serializeXml(theUserProfile.frameLayout, pout);
                pout.close();
            }
        };
        map.put("debug.layout.dump",action);
	}

	private void handleUserMove(Move move, boolean animate)
	        throws BadLocationException, ParseException
	{
		final Move mv = new Move(move,theGame.getPosition());

		if (theUserProfile.getBoolean("sound.moves.ack.user"))
			speakAcknowledge();
		if (theUserProfile.getBoolean("sound.moves.user")) {
			int format = theUserProfile.getInt("doc.move.format",MoveFormatter.SHORT);
			speakMove(format,mv,theGame.getPosition());
		}

		if (theGame.insertMove(-1, mv, 0) == Game.INSERT_USER_ABORT) {
			//  user aborted insert of new line
			//  update board panel
			if (boardPanel()!=null) boardPanel().getView().refresh(true);
			return;
		}

		updateClock();

		if (animate && boardPanel() != null)
			boardPanel().move(mv, (float)(mv.distance()*0.2));

		Command cmd = new Command("move.notify",null,mv);
		broadcast(cmd);

		if (mv.isGameFinished()) {
			gameFinished(mv.flags, theGame.getPosition().movedLast(), theGame.isMainLine());
			theClock.halt();
		}
		else switch (theMode) {
		case ENGINE_ENGINE:
				//	TODO not yet implemented
				break;

		case USER_ENGINE:
			boolean wasBook = false;
			try {
				wasBook = selectBookMove();
			} catch (Exception e) {
				wasBook = false;
			}

			if (!wasBook)
				invokeWithPlugin(new Runnable() {
					public void run() {
						if (mv.isFRCCastling() && !getEnginePlugin().supportsFRC()) {
							showFRCWarning(true);
							pausePlugin(false);
						}
						else {
							if (!theGame.getPosition().isClassic() && !getEnginePlugin().supportsFRC())
								showFRCWarning(false);
								//  but keep on playing (you have been warned ;-)
							getEnginePlugin().userMove(mv,true);
    					}
	                };
				});
				break;

		case ANALYSIS:
				invokeWithPlugin(new Runnable() {
					public void run() {
						if (mv.isFRCCastling() && !getEnginePlugin().supportsFRC()) {
							showFRCWarning(true);
							pausePlugin(false);
						}
						else {
							if (!theGame.getPosition().isClassic() && !getEnginePlugin().supportsFRC())
								showFRCWarning(false);
								//  but keep on playing (you have been warned ;-)
							getEnginePlugin().analyze(theGame.getPosition(), mv);
						}
					}
				});
				break;
		}

		classifyOpening();
	}

	private void cutLine(Node node) throws BadLocationException
	{
		//	delete/cut line
		boolean cutCurrent = theGame.cutBeforeCurrent(node);
		MoveNode closeMove = theGame.cutBefore(node);

		theClock.halt();
		getAnimation().pause();
		pausePlugin(theMode==ANALYSIS);

		if (cutCurrent) {
			Command cmd = new Command("move.notify",null,closeMove,Boolean.TRUE);
			broadcast(cmd);
		}
	}

	private void classifyOpening()
	{
		if (theGame.isMainLine() &&
			theGame.isLast() &&
			theUserProfile.getBoolean("doc.classify.eco"))
		{
			theGame.getPosition().updateHashKeys();
			ECOClassificator classificator = getClassificator();
			int result = classificator.lookup(theGame.getPosition());
			if (result != ECOClassificator.NOT_FOUND) {
				theGame.setTagValue(PgnConstants.TAG_ECO, classificator.getEcoCode(result,3));
				theGame.setTagValue(PgnConstants.TAG_OPENING, classificator.getOpeningName(result));
				if (docPanel()!=null) docPanel().reformat();
			}
		}
	}

	public JoPanel createPanel(String name)
	{
		JoPanel panel = JoPanel.get(name);
		if (panel==null) {
			LayoutProfile profile = Application.theUserProfile.getPanelProfile(name);
			profile.hide = false;
			panel = JoPanel.create(profile,true);
		}
		return panel;
	}

	/**
	 * show a panel
	 * @param name
	 */
	public JoPanel showPanel(String name)
	{
		JoPanel panel = createPanel(name);
		showPanel(panel);
		return panel;
	}

    /**
     * show a panel
     */
	public JoPanel showPanel(JoPanel panel)
	{
		JoFrame frame = panel.getParentFrame();
		if (frame==null) {
			frame = JoFrame.getFrame(panel.getProfile().frameProfile);
			if (frame==null)
				frame = new JoFrame(panel.getProfile().frameProfile);
			JoFrame.dock(panel, frame, panel.getProfile().dockingPath);
		}

		showFrame(frame);
		return panel;
	}

    /**
     * show the frame, a panel belongs to
     * (son't show the panel itself)
     * @param name
     */
    public JoFrame showPanelFrame(String name)
    {
        JoPanel panel = createPanel(name);
        return showPanelFrame(panel);
    }

    /**
     * show the frame, a panel belongs to
     * (don't show the panel itself)
     */
    public JoFrame showPanelFrame(JoPanel panel)
    {
        JoFrame frame = panel.getParentFrame();
        if (frame==null) {
            frame = JoFrame.getFrame(panel.getProfile().frameProfile);
            if (frame==null) {
	            frame = new JoFrame(panel.getProfile().frameProfile);
                //  DON'T JoFrame.dock
            }
        }
        showFrame(frame);
        return frame;
    }


	protected void showFrame(JoFrame frame)
	{
		frame.setComponentsVisible(true);
		frame.setVisible(true);
		frame.toFront();
	}

	public void switchGame(int tabIndex)
	{
		theGame = theHistory.get(tabIndex);
        theHistory.setCurrent(tabIndex);
		theGame.resetPosition();
        theGame.gotoMove(theGame.getCurrentMove(),true);
		//  TODO each of the above calls Board.setupFEN(); three times in a row.
		//  potential for optimisation...
		pausePlugin(theMode==ANALYSIS);

		Command cmd = new Command("switch.game",null, theGame, new Integer(tabIndex));
		broadcast(cmd);
	}

	protected void saveGame(Game g)	throws Exception
	{
		if (g != null)
			if (g.isNew()) {
				if (!g.isEmpty() && (g.getTagValue(PgnConstants.TAG_DATE)==null))
				{
					//  when saving a new, non-empty game, fill in default date
					g.setTagValue(PgnConstants.TAG_DATE, PgnUtil.currentDate(), g);
				}
				g.saveAs(Collection.AUTOSAVE_ID,0);
				//	adjust database panel
				broadcast(new Command("collection.modified",null,new Integer(Collection.AUTOSAVE_ID)));
			}
			else {
				g.save();
				broadcast(new Command("game.modified",null,new Integer(g.getId())));
				//	adjust database panel
			}
	}

    protected boolean prepareEditGame(int GId, PositionFilter posFilter) throws Exception
    {
		broadcast("prepare.game");

		int index = theHistory.indexOf(GId);
        if (index >= 0) {
			theGame = theHistory.get(index);
            theHistory.setCurrent(index);
			return theGame.gotoMove(posFilter);
		}
		else if (!Game.exists(GId))
			return false;
        else {
            prepareNewGame(null);
			try {
				theGame.read(GId);
			} catch (IllegalArgumentException ise) {
				//	not found in database !
				//	(may happen if Ids are stored in profile
				error(ise);
			}
			theGame.clearDirty();

			if (!theGame.gotoMove(posFilter)) {
				if (theGame.getTagValue(Game.TAG_FEN)!=null)
					theGame.first();
				else
					theGame.last();
			}
            return true;    //  = new editor opened
		}
    }

	protected boolean prepareEditGame(String pgnText, PositionFilter posFilter) throws Exception
	{
		broadcast("prepare.game");

		prepareNewGame(null);

		//  note that this need not be a valid PGN text
		try {
			GameUtil.pastePGN(theGame,pgnText);
		} catch (Throwable e) {
			//  parse error in PGN ? - don't mind
			CommentNode errorComment = new CommentNode("Error ("+e.getMessage()+") in PGN text:\n\n");
			//  TODO style
			errorComment.insertAfter(theGame.getMainLine().first());

			CommentNode pgnComment = new CommentNode(pgnText);
			pgnComment.insertAfter(errorComment);

			AWTUtil.beep(docPanel());  //  "beep"
		}

		//  goto last move
		if (theGame.getTagValue(Game.TAG_FEN)!=null)
			theGame.first();
		else
			theGame.last();
		return true;
	}

    protected boolean prepareEditGames(GameSource src, PositionFilter posFilter) throws Exception
    {
        int count = 0;
		//	don't open more than 24 tabs

        if (src.isSingleGame()) {
            if (prepareEditGame(src.firstId(),posFilter)) count++;
		}
        else if (src.isGameArray()) {
            int[] ids = src.getIds();
            for (int i=0; i < ids.length; i++)
                if (prepareEditGame(ids[i],posFilter))
                    if (++count >= 24) break;
        }
        else if (src.isGameSelection()) {
            int idx1 = src.getSelection().getMinSelectionIndex();
            int idx2 = src.getSelection().getMaxSelectionIndex();
            for ( ; idx1 <= idx2; idx1++)
                if (src.getSelection().isSelectedIndex(idx1)) {
                    int id = src.getSelection().getDBId(idx1);
                    if (prepareEditGame(id,posFilter))
                        if (++count >= 24) break;
                }
        }
        else
            throw new IllegalArgumentException("GameSource type not supported here");

        return (count > 0);
    }

    protected void prepareNewGame(String setup) throws Exception
    {
	    boolean createNew=true;
		/*	save new game into autosave ?	*/
		if (theGame != null && theGame.isNew()) {
			if (theGame.isDirty())
				/*saveGame(theGame)*/ ;
			else
				createNew = false;  //	reuse current game
		}

	    if (createNew)
	    {
		    //  create new Game object
			theGame = new Game(theUserProfile.getStyleContext(),
							   null,null, null/*PgnUtil.currentDate()*/, setup,
							   (theGame!=null) ? theGame.getPosition() : null);

			theHistory.add(theGame);
	    }
	    else
	    {
		    //  recycle existing
		    theGame.clear(setup);
		    if (!theHistory.contains(theGame))
			    theHistory.add(theGame);
	    }

		TimeControl tc = theUserProfile.getTimeControl();
		tc.reset(theClock);
		if (getEnginePlugin()!=null) {
			getEnginePlugin().newGame();
			getEnginePlugin().setTimeControls(tc.getPhase(0));
		}
    }

    protected int confirmSaveOne()
    {
        return JoDialog.showYesNoCancelDialog(
                    "confirm.save.one", "confirm",
                    "dialog.confirm.save","dialog.confirm.dont.save",
                    JOptionPane.YES_OPTION);
    }

    protected int confirmSaveAll()
    {
        return JoDialog.showYesNoCancelDialog(
                    "confirm.save.all", "confirm",
                    "dialog.confirm.save","dialog.confirm.dont.save",
                    JOptionPane.YES_OPTION);
    }

	protected boolean hasGameSource(Command cmd, boolean collectionOnly)
	{
		if (cmd!=null && cmd.data instanceof GameSource)
			return true;
			/*	source passed with command; fine */

		/**	else: collection or list panel in frontmost window ?	*/
		CollectionPanel collPanel = collectionPanel();
		ListPanel listPanel = listPanel();

		if (collPanel!=null && collPanel.isInFront() ||
			listPanel!=null && listPanel.isInFront())
		{
			if (!collectionOnly && (listPanel!=null)) {
				if (listPanel.hasSelection())
					return true;
					//  set of selected games
				SearchRecord srec = listPanel.getSearchRecord();
				if (srec.hasFilter() || srec.hasSortOrder())
					return true;
					//  current result set
			}

			if ((collPanel!=null) && collPanel.hasSelection())
				return true;
				//  set of selected collections
		}

		/** else: document panel in front ? */
		DocumentPanel docPanel = docPanel();
		if (docPanel!=null && docPanel.isInFront()) {
			return theGame.getLength() > 0;  //  one open game
		}

		//  alternative: use the complete database !?!
		return false;
	}

    protected SearchRecord getSearchRecord()
    {
        /**	collection or list panel in frontmost window ?	*/
        ListPanel listPanel = listPanel();
        if (listPanel!=null)
            return listPanel.getSearchRecord();
        else
            return null;
    }

	protected GameSource getGameSource(Command cmd, boolean collectionOnly)
	{
		if (cmd!=null && cmd.data instanceof GameSource)
			return ((GameSource)cmd.data);
			/*	source passed with command; fine */

		/**	else: collection or list panel in frontmost window ?	*/
		CollectionPanel collPanel = collectionPanel();
		ListPanel listPanel = listPanel();
        DocumentPanel docPanel = docPanel();

        JoPanel preferredPanel = null;

        if (collPanel!=null && collPanel.isFocusInside() && collPanel.hasSelection())
            preferredPanel = collPanel;
        else if (listPanel!=null && listPanel.isFocusInside() && !collectionOnly)
            preferredPanel = listPanel;
        else if (docPanel!=null && docPanel.isFocusInside() && !collectionOnly)
            preferredPanel = docPanel;
        else if (collPanel!=null && collPanel.isInFront() && collPanel.hasSelection())
            preferredPanel = collPanel;
        else if (listPanel!=null && listPanel.isInFront() && !collectionOnly)
            preferredPanel = listPanel;
        else if (docPanel!=null && docPanel.isInFront() && !collectionOnly)
            preferredPanel = docPanel;
        else {
            //  can't help it
            return null;
        }

        if (preferredPanel==listPanel)
        {
            if (listPanel.getCurrentSelection().hasSelection())
                return GameSource.gameSelection(listPanel.getCurrentSelection());
            else
                return GameSource.gameSelection(listPanel.getCompleteResult());
            //  note that this may not be the most efficient implementation,
            //  since we have to iterate over the complete list
            //  TODO think about a more elegant solution that retrieves the result set
            //  through the current filter settings
        }

		if (preferredPanel==collPanel)
		{
            return GameSource.collectionSelection(collPanel);
			//  set of selected collections
		}

		/** else: document panel in front ? */
		if (preferredPanel==docPanel)
        {
			if (theHistory.size()==1) {
				if (!theGame.isEmpty())
					return GameSource.gameObject(theGame);  //  one open game
			}
			else {
				Game[] array = theHistory.getArray(false);
				if (array!=null && array.length > 0)
					return GameSource.gameList(array);  //  all open games
			}
		}

		//  alternative: use the complete database !?!
		return null;
	}

	protected void newCollection(Command cmd) throws Exception
	{
		showPanel("window.collectionlist");

        int parentId;
        Collection coll = null;

        try {
            DBTask.broadcastOnUpdate("collection.new");

            GameSource src = getGameSource(cmd,true);
            if (src!=null)
                parentId = src.firstId();
            else
                parentId = 0;

            JoConnection conn = null;
            coll = null;
            try {
                conn = JoConnection.get();
                String name = Collection.makeUniqueName(parentId,Language.get("collection.new"),conn);
                coll = Collection.newCollection(parentId, name, conn);
                coll.insert(conn);
            } finally {
                if (conn!=null) conn.release();
            }


        } finally {
            DBTask.broadcastAfterUpdate((coll==null) ? 0:coll.Id);
        }

        if (collectionPanel() != null) {
			collectionPanel().expand(parentId);
			collectionPanel().edit(coll.Id);
		}
	}

    protected int confirmDBPaste(GameSource src)
    {
        return JoDialog.showYesNoCancelDialog("dialog.paste.message", "dialog.paste.title",
                              "dialog.paste.copy", "dialog.paste.same",
                              JOptionPane.YES_OPTION);
    }

	public void gameMaintenance(Command cmd, GameSource src, Collection target)
		throws Exception
	{
		GameTask task = null;

        if (cmd.code.equals("menu.edit.paste")) {
            //  replaced by either menu.edit.paste.same or menu.edit.paste.copy
            if (!Collection.hasContents(Collection.CLIPBOARD_ID)) {
                AWTUtil.beep(this);
                return;
            }
            else switch (confirmDBPaste(src))
            {
            case JOptionPane.YES_OPTION:    cmd.code = "menu.edit.paste.copy"; break;
            case JOptionPane.NO_OPTION:     cmd.code = "menu.edit.paste.same"; break;
            default:
            case JOptionPane.CANCEL_OPTION: return; //  user cancelled
            }
        }

		if (cmd.code.equals("menu.edit.cut")) {
			//	Cut = move to clipboard
			task = new MoveToClipboardTask(src, Collection.CLIPBOARD_ID, Collection.TRASH_ID,  true);
		}
		else if (cmd.code.equals("menu.edit.copy")) {
			//	Copy = copy to clipboard
			task = new CopyToClipboardTask(src, Collection.CLIPBOARD_ID, Collection.TRASH_ID);
		}
        else if (cmd.code.equals("menu.edit.paste.copy")) {
        	//	Paste = copy from clipboard
            if (target!=null)
                task = new CopyTask(src, target.Id, true);
            else {
                GameSource clip = GameSource.collectionContents(Collection.CLIPBOARD_ID);
			    task = new CopyTask(clip, src.firstId(),true);
            }
		}
		else if (cmd.code.equals("menu.edit.paste.same")) {
			//	Paste Same = move from clipboard
            if (target!=null)
                task = new MoveTask(src, target.Id, false,true);
            else {
                GameSource clip = GameSource.collectionContents(Collection.CLIPBOARD_ID);
                task = new MoveTask(clip, src.firstId(),false,true);
            }
		}
		else if (cmd.code.equals("menu.edit.clear")) {
			//	Clear = move to trash
			task = new MoveTask(src, Collection.TRASH_ID,true,false);
		}
		else if (cmd.code.equals("menu.edit.restore")) {
			//	Restore = move from trash
			task = new RestoreTask(src);
		}
		else if (cmd.code.equals("menu.edit.erase")) {
			//	Restore = move from trash
			task = new EraseTask(src);
		}
		else if (cmd.code.equals("menu.edit.empty.trash")) {
			//	Empty Trash = erase trash
			GameSource trash = GameSource.collectionContents(Collection.TRASH_ID);
			task = new EraseTask(trash);
		}
		else if (cmd.code.equals("menu.edit.collection.crunch")) {
			//  Crunch collection = update index column
            task = new CrunchTask(src,getSearchRecord());
		}
		else if (cmd.code.equalsIgnoreCase("dnd.move.games")) {
			boolean setOId = target.isInTrash() || target.isInClipboard();
			boolean calcIdx = !target.isInTrash() && !target.isInClipboard();
			task = new MoveTask(src, target.Id, setOId,calcIdx);
		}
		else if (cmd.code.equalsIgnoreCase("dnd.move.top.level")) {
			task = new MoveTask(src, 0, false,true);
		}

		task.setDisplayComponent((JoPanel)Util.nvl(listPanel(),collectionPanel(),getFocusPanel()));
//		task.run();	//	snychroneous operation
		task.start();	// for asynchronesous operation
	}

	/**
	 * messages from the plugin (and from the clock come from a separate thread
	 * but since this affects the GUI quite a bit, we better keep them in synch with
	 * the event dispatching thread. DeferredMessageListeners will be called after
	 * all GUI events have been processed.
	 */
	public void handleMessage(Object who, int what, Object data)
	{
		try {
			if (who==getEnginePlugin() || who==theOpeningLibrary)
				handlePluginMessage(what, data);
			if (who==theClock)
				handleClockMessage(what);
			if (who==docPanel())
				handleDocMessage(what,data);
		} catch (Exception e) {
			Application.error(e);
		}
	}

	public void handleClockMessage(int what) throws Exception
	{
		switch (what) {
		case Clock.TIME_ELAPSED:
				if (theGame.getResult()==Game.RESULT_UNKNOWN)
					gameFinished(Clock.TIME_ELAPSED,0,true);
				break;
		}
	}

	public void handleDocMessage(int what, Object data) throws BadLocationException, ParseException
	{
		switch (what) {
		case DocumentPanel.EVENT_USER_MOVE:
				//  move input through keyboard
				//  message from DocumentEditor
				Object[] params = (Object[])data;
				MoveNode after = (MoveNode)params[0];
				Move mv = (Move)params[1];
				theGame.gotoMove(after);
				//  TODO think about more general copy/paste with PGN fragments
				handleUserMove(mv,true);
				break;
		}
	}

	private void setGameDefaultInfo()
	{
		if (getEnginePlugin()==null)
			return;

		boolean modified = false;
		if (theGame.getPosition().gameMove()<=1) {
			if (theGame.getTagValue(PgnConstants.TAG_WHITE)==null &&
				theGame.getTagValue(PgnConstants.TAG_BLACK)==null)
			try {
				if (theGame.getPosition().whiteMovesNext()) {
					theGame.setTagValue(PgnConstants.TAG_WHITE, getEnginePlugin().getName(), theGame);
					theGame.setTagValue(PgnConstants.TAG_BLACK, theUserProfile.get("user.name"), theGame);
					modified = true;
				}
				else {
					theGame.setTagValue(PgnConstants.TAG_BLACK, getEnginePlugin().getName(), theGame);
					theGame.setTagValue(PgnConstants.TAG_WHITE, theUserProfile.get("user.name"), theGame);
					modified = true;
				}
			} catch (Exception ex) {
				error(ex);
			}
			if (modified && docPanel()!=null)
				docPanel().reformat();
		}
	}

	public void handlePluginMessage(int what, Object data) throws Exception
	{
		Position pos = theGame.getPosition();
		switch (what) {
		case Plugin.PLUGIN_MOVE:
			if (pos.isMate() || pos.isStalemate()) {
				handleEngineError(EnginePlugin.PLUGIN_ERROR, "game is already finished");
				return;    //  protocol error !?
			}

			EnginePlugin.EvaluatedMove emv = (EnginePlugin.EvaluatedMove)data;
			if (emv==null) {
				handleEngineError(EnginePlugin.PLUGIN_ERROR,"");
				return;    //  protocol error !?
			}
			Move mv = new Move(emv,pos);  //  assert correct owner
			MoveNode node = null;

			synchronized (theGame) {
				int oldOptions = pos.getOptions();
				pos.setOption(Position.CHECK+Position.STALEMATE, true);
//                System.err.println("engine move "+mv.toString());
				try {
					if (!pos.tryMove(mv)) {
						/*  throw new IllegalArgumentException("illegal move from engine");
							plugin got out of synch
						*/
						handleEngineError(EnginePlugin.PLUGIN_ERROR,mv.toString());
						return;
					}
					else
						pos.undoMove();
				} finally {
					pos.setOptions(oldOptions);
				}

				setGameDefaultInfo();

				if (theUserProfile.getBoolean("sound.moves.engine")) {
					int format = theUserProfile.getInt("doc.move.format",MoveFormatter.SHORT);
					speakMove(format, mv, pos);
				}

				theGame.insertMove(-1, mv, Game.NEW_LINE);
				node = theGame.getCurrentMove();
			}
			theClock.setCurrent(pos.movesNext());

			if (boardPanel() != null)
				boardPanel().move(mv, (float)(mv.distance()*0.2));

			theCommandDispatcher.broadcast(new Command("move.notify", null, mv), this);

			if (mv.isGameFinished())
				gameFinished(mv.flags,pos.movedLast(), theGame.isMainLine());

			classifyOpening();

			if (node!=null && emv!=null) {
				//  update move evaluation history
				if (theGame.isMainLine(node))
					node.setEngineValue(emv.getValue());
				/** UCI engines can't resign or offer draws (stupid gits)
				 *  we got to track the evaluation of recent moves and allow the user to adjudicate the game
				 */
				adjudicate(theGame,pos.movedLast(),pos.gamePly(), node,emv,getEnginePlugin());
			}
			break;

		case Plugin.PLUGIN_ACCEPT_DRAW:
			EnginePlugin xplug = getEnginePlugin();
			if (xplug.wasOfferedDraw()) {
				//	engine accepts draw request from user
				gameDraw();
			}
			break;

		case Plugin.PLUGIN_DRAW:
			xplug = getEnginePlugin();
			//	engine offers draw to user
            String message = Language.get("dialog.engine.offers.draw");
            message = StringUtil.replace(message,"%engine%",getEnginePlugin().getDisplayName());

            int result = JoDialog.showYesNoCancelDialog(
                    message, "confirm",
                    "dialog.accept.draw","dialog.decline.draw",
                    JOptionPane.NO_OPTION);
			switch (result) {
			case JOptionPane.YES_OPTION:
					gameDraw(); break;
			case JOptionPane.NO_OPTION:
					xplug.declineDraw(); break;
			}
			break;

		case EnginePlugin.PLUGIN_ERROR:
		case EnginePlugin.PLUGIN_FATAL_ERROR:
			handleEngineError(what, data.toString());
			break;

		case Plugin.PLUGIN_RESIGNS:
			gameFinished(Plugin.PLUGIN_RESIGNS,pos.movesNext(),true);
			break;

		case Plugin.PLUGIN_REQUESTED_HINT:
			if (boardPanel() != null)
				boardPanel().showHint(data);
			break;
		}
	}

	protected void handleEngineError(int errorMessage, String errorText)
	{
		String dialogText;
		Map placeholders = new HashMap();
		placeholders.put("engine",getEnginePlugin().getDisplayName(null));
		placeholders.put("message",errorText);

		switch (errorMessage)
		{
		case EnginePlugin.BOOK_ERROR:
			dialogText = Language.get("error.book");
			break;
		case EnginePlugin.PLUGIN_ERROR:
			dialogText = Language.get("error.engine");
			//  recoverable error, stop calculating
			pausePlugin(false);
			break;
		default:
		case EnginePlugin.PLUGIN_FATAL_ERROR:
			dialogText = Language.get("error.engine.fatal");
			//  unrecoverable error. shut down the plugin.
//			restartPlugin();
			closePlugin();
			break;
		}

		dialogText = StringUtil.replace(dialogText,placeholders);

		JOptionPane.showMessageDialog(JoFrame.getActiveFrame(),
			  dialogText, Language.get("error.engine.title"),
			  JOptionPane.ERROR_MESSAGE);
	}


	protected boolean adjudicate(Game game, int engineColor, int gamePly, MoveNode node,
	                                  EnginePlugin.EvaluatedMove move, EnginePlugin engine)
	        throws BadLocationException, ParseException
	{
//		System.err.println(move.toString()+"="+move.getValue());
		if (!move.isValid() || game==null || engine==null
		        || (game.getResult()!=PgnConstants.RESULT_UNKNOWN) || game.askedAdjudicated
		        || !game.isMainLine(node)) return false;

		if (!engine.canResign()
		        && engine.shouldResign(game,engineColor,gamePly,node))
		{
			//  if last 5 moves are below resignation threshold, resign
			theGame.askedAdjudicated = true;   //  ask only once per session and game
			gameFinished(Plugin.PLUGIN_RESIGNS,engineColor,true);
			return true;
		}

		if (!engine.canAcceptDraw() && engine.wasOfferedDraw()
		        && engine.shouldDraw(game,gamePly,node))
		{
			//  engine was offered draw, but can't decide
			//  if last 5 moves are within draw threshold, accept draw without asking
			theGame.askedAdjudicated = true;   //  ask only once per session and game
			gameDraw();
			return true;
		}

		if (!engine.canOfferDraw()
		        && engine.shouldDraw(game,gamePly,node))
		{
			//  if last 5 moves are within draw threshold, ask to adjudicate
			theGame.askedAdjudicated = true;   //  ask only once per session and game
			String message = Language.get("dialog.engine.offers.draw");
			message = StringUtil.replace(message,"%engine%",getEnginePlugin().getDisplayName());

			int result = JoDialog.showYesNoCancelDialog(
			        message, "confirm",
			        "dialog.accept.draw","dialog.decline.draw",
			        JOptionPane.NO_OPTION);
			if (result==JOptionPane.YES_OPTION) {
				gameDraw();
				return true;
			}
		}

		return false;
	}


	//-------------------------------------------------------------------------------
	//	useful stuff
	//-------------------------------------------------------------------------------

	/**
	 * open the application
	 * restore layout from user profile
	 */
	public void open()
		throws Exception
	{
		/**	load user profile		 */
		readProfile();

		/**	load language		 */
		Language.setLanguage(theLanguageDirectory, theUserProfile.getString("user.language"));

		theGame = new Game(theUserProfile.getStyleContext(),
						   null,null, null/*PgnUtil.currentDate()*/, null, null);
		theClock = new Clock();
		theClock.addMessageListener(this);

        theHistory = new History();
        theHistory.add(theGame);
// 		switchGame(0);

		theCommandDispatcher = new CommandDispatcher();
		theCommandDispatcher.addCommandListener(this);

        if (Version.mac)
            MRJAdapter.setFramelessJMenuBar(new JoMenuBar(null));
        //  call after Config, Language, Command Dispatcher are present

		TimeControl tc = theUserProfile.getTimeControl();
		tc.reset(theClock);

		/*	setup textures	*/
		TextureCache.setDirectory(new File(theWorkingDirectory, "images/textures"));

		/**	set look & feel	 */
        String lnfClassName = Version.getSystemProperty("jose.look.and.feel");
		if ("default".equalsIgnoreCase(lnfClassName))
			lnfClassName = LookAndFeelList.getDefaultClassName();
		if (lnfClassName==null)
            lnfClassName = theUserProfile.getString("ui.look.and.feel");
        if (lnfClassName==null) {
            lnfClassName = UserProfile.getFactoryLookAndFeel();
            if (lnfClassName==null)
                lnfClassName = LookAndFeelList.getDefaultClassName();
            theUserProfile.set("ui.look.and.feel",lnfClassName);
        }
		setLookAndFeel(lnfClassName);

		if (Version.mac) new MacAdapter();      //  listens to application menu

		theMode = theUserProfile.getInt("game.mode",USER_ENGINE);

		if (theUserProfile.getBoolean("doc.load.history"))
			openHistory();

		theOpeningLibrary = new OpeningLibrary();
		theOpeningLibrary.open(theUserProfile,theConfig);

		/*	setup windows	*/
		openFrames(theUserProfile.getFrameProfiles());

		if (JoFrame.countVisibleFrames()==0) {
			/*	corrupted profile ? should never happen	*/
			System.err.println("corrupted layout ?");
			openFrames(FrameProfile.FACTORY_LAYOUT);
		}

//		theCommandDispatcher.handle(new Command("menu.file.new",null,null),this);
		SplashScreen.close();

//		showErrorDialog(new IOException("shit happens"));

		Runtime.getRuntime().addShutdownHook(shutdownHook = new DirtyShutdown());

		//  open files ?
		String filePath = Version.getSystemProperty("jose.file");
		if (filePath!=null) {
			Command cmd = new Command("menu.file.open.all",null,filePath);
			theCommandDispatcher.handle (cmd,this);
		}

		if (Version.windows) {
			Object assoc = theUserProfile.get("jos.associate.old");
			if (assoc==null) {
				assoc = WinUtils.associateFileExtension("jos","jose.exe");
				assoc = WinUtils.associateFileExtension("jose","jose.exe");
				theUserProfile.set("jos.associate.old",assoc);
			}
		}

		SwingUtilities.invokeLater(new Startup());
	}


	protected class Startup implements Runnable, ActionListener
	{
		public void run() {

			getContextMenu();

			//  deferred loading of ECO classificator & additional fonts
			Thread deferredLoader = new DeferredStartup();
			deferredLoader.setPriority(Thread.MIN_PRIORITY);
			deferredLoader.start();

			Sound.initialize(theConfig);	//	will start a background thread (that initially sleeps)

			if (Version.getSystemProperty("jose.detect",false))
			{
				if (ApplicationListener.searchApplication()) {
					//	another instance detected - exit immediately
					System.err.println("another running instance detected");
					System.exit(+2);
				}

				applListener = new ApplicationListener();
				applListener.start();
			}

		}

		public void actionPerformed(ActionEvent e)
		{
		}
	}

	protected class DeferredStartup extends Thread
	{
		public void run()
		{
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				//
			}
			JoStyleContext styles = (JoStyleContext)theUserProfile.getStyleContext();
			styles.assertCustomFonts();
			getClassificator();
		}
	}

	/**
	 * usually, a shutdown is performed throuh File/Quit and issuing "menu.file.quit"
	 * however, if the programs is shut down otherwise (application menu on OS X ?)
	 * we attempt to perform a shutdown as good as possible
	 * note that such a dirty shutdown cannot be cancelled by the user
	 */
	protected class DirtyShutdown extends Thread
	{
		public void run()
		{
			try {
				if (shutdownHook!=null) {
					Command cmd = new Command("menu.file.quit");
					quit(cmd);
				}
			} catch (Throwable e) {
				error(e);
			}
		}
	}

	private void openFrames(FrameProfile[] frameProfiles)
	{
		Vector openFrames = new Vector();
		for (int i=0; i<frameProfiles.length; i++)
		{
			if (frameProfiles[i].state == FrameProfile.HELP_FRAME)
				helpBounds = frameProfiles[i].bounds;
			else if (JoFrame.isVisible(frameProfiles[i].state))
            {
				JoFrame frame = new JoFrame(frameProfiles[i]);
                frame.setComponentsVisible(true);
 				openFrames.add(frame);
			}
		}

		for (int i = openFrames.size()-1; i >= 0; i--) {
			((JoFrame)openFrames.get(i)).setVisible(true);
        }
			/**	ATTENTION don't call JFrame.setVisible() from the constructor
			 * 	leads to strange lock-ups
			 *
			 * 	TODO think about invokeLater() or something
			 */

	}


/*
	private JoFrame openOneFrame(FrameProfile frameProfile)
	{
		if (frameProfile.state == FrameProfile.HELP_FRAME) {
			helpBounds = frameProfile.bounds;
			return null;
		}
		else {
			JoFrame frame = new JoFrame(frameProfile);
			frame.setComponentsVisible(true);
			frame.setVisible(true);
			frame.toFront();
			return frame;
		}
	}
*/

	public void invokeWithPlugin(final Runnable work)
	{
		if (getEnginePlugin()!=null) {
			//  invoke at once
			work.run();
		}
		else {
			//  open plugin and invoke later
			Thread thread = new Thread() {
				public void run() {
					try {
						if (openEnginePlugin())
							SwingUtilities.invokeLater(work);
					} catch (IOException e) {
						Application.error(e);
					}
				}
			};
			thread.start();
		}
	}

	public boolean selectBookMove() throws Exception
	{
		switch (theOpeningLibrary.engineMode)
		{
		case OpeningLibrary.PREFER_ENGINE_BOOK:
			if (getEnginePlugin()!=null && getEnginePlugin().isBookEnabled())
				return false;
			//  else: intended fall-through
		default:
		case OpeningLibrary.GUI_BOOK_ONLY:
		case OpeningLibrary.PREFER_GUI_BOOK:
			BookEntry bookEntry = theOpeningLibrary.selectMove(theGame.getPosition(), true,
												theGame.getPosition().whiteMovesNext());
			return (bookEntry!=null) && playBookMove(bookEntry);

		case OpeningLibrary.NO_BOOK:
			return false;   //  pretty easy
		}
	}

	protected boolean playBookMove(BookEntry entry)
			throws Exception
	{
		//  play the book move
		Position pos = theGame.getPosition();
		MoveNode node = null;

		synchronized (theGame) {
			int oldOptions = pos.getOptions();
			pos.setOption(Position.CHECK+Position.STALEMATE, true);
//                System.err.println("engine move "+mv.toString());
			try {
				if (!pos.tryMove(entry.move)) {
					/*  throw new IllegalArgumentException("illegal move from book!");			*/
					handleEngineError(EnginePlugin.BOOK_ERROR,entry.move.toString());
					return false;
				}
				else
					pos.undoMove();
			} finally {
				pos.setOptions(oldOptions);
			}

			if (theUserProfile.getBoolean("sound.moves.engine")) {
				int format = theUserProfile.getInt("doc.move.format",MoveFormatter.SHORT);
				speakMove(format, entry.move, pos);
			}

			theGame.insertMove(-1, entry.move, Game.NEW_LINE);
			node = theGame.getCurrentMove();
		}
		theClock.setCurrent(pos.movesNext());

		if (boardPanel() != null)
			boardPanel().move(entry.move, (float)(entry.move.distance()*0.2));

		theCommandDispatcher.broadcast(new Command("move.notify", null, entry.move), this);

		if (entry.move.isGameFinished())
			gameFinished(entry.move.flags,pos.movedLast(), theGame.isMainLine());

		classifyOpening();

		if (node!=null && entry.move!=null) {
			//  update move evaluation history
			if (theGame.isMainLine(node))
				node.setEngineValue(entry.centipawnValue());   //  TODO which value is more appropriate
//					adjudicate(theGame,pos.movedLast(),pos.gamePly(), node,emv,getEnginePlugin());
		}
		return true;
	}

	protected boolean showFRCWarning(boolean atCastling)
	{
		if (!atCastling) {
			if (shownFRCWarning) return false;
			if (! theUserProfile.getBoolean("show.frc.warning",true)) return false;
		}
		String title = Language.get("warning.engine");
		String message = "<html>"+Language.get("warning.engine.no.frc")+"<br><br>";

		JCheckBox dontShowAgain;
		Box box = Box.createVerticalBox();
		box.add(new JoStyledLabel(message));

		if (atCastling) {
			JOptionPane.showMessageDialog(JoFrame.theActiveFrame,
					box, title, JOptionPane.WARNING_MESSAGE);
		}
		else {
			box.add(dontShowAgain = new JCheckBox(Language.get("warning.engine.off")));
			JOptionPane.showMessageDialog(JoFrame.theActiveFrame,
					box, title, JOptionPane.WARNING_MESSAGE);
			theUserProfile.set("show.frc.warning", !dontShowAgain.isSelected());
		}

		return (shownFRCWarning=true);
	}

	public boolean openEnginePlugin()
		throws IOException
	{
		if (getEnginePlugin() == null) {
			/**	setup plugin		 */
            String name = theUserProfile.getString("plugin.1");
			engine = (EnginePlugin)Plugin.getPlugin(name,Version.osDir,true);

            if (getEnginePlugin() == null) {
				EnginePlugin defaultPlugin = (EnginePlugin)Plugin.getDefaultPlugin(Version.osDir,true);
				if (defaultPlugin==null) {
					//	no plugin available !
					JoDialog.showErrorDialog(null,"error.plugin.not.found", "plugin.1",name);
					theMode = USER_INPUT;   //  no use bothering the user with more errors
					return false;
				}
				else {
					//	use default plugin instead
					engine = defaultPlugin;
					JoDialog.showErrorDialog(null, "error.plugin.revert.default",
							"plugin.1",name,
							"plugin.2",defaultPlugin.getName());
				}
			}

            try {
                getEnginePlugin().init(theGame.getPosition(), Version.osDir);
				engine.addInputListener(this,1);

                //	set real time
                broadcast(new Command("new.plugin", null, getEnginePlugin()));

//	            showPanel("window.eval");
				showPanel("window.engine");

                getEnginePlugin().open(Version.osDir);
                getEnginePlugin().addMessageListener(this);

                TimeControl tc = theUserProfile.getTimeControl();
                getEnginePlugin().setTimeControls(tc.getPhase(0));
            } catch (IOException ioex) {
                if (getEnginePlugin().print()!=null)
                    getEnginePlugin().print().println(ioex.getMessage());
                throw ioex;
            }
		}

		//  adjust book settings
		switch (theOpeningLibrary.engineMode)
		{
		case OpeningLibrary.GUI_BOOK_ONLY:
		case OpeningLibrary.NO_BOOK:
			//  disable the engine book
			getEnginePlugin().disableBook();
			break;
		}

		return getEnginePlugin() != null;
	}

	private void pausePlugin(boolean analyze)
	{
		if (getEnginePlugin() != null) {
			if (analyze && !theGame.getPosition().isGameFinished())
				getEnginePlugin().analyze(theGame.getPosition());
			else if (!getEnginePlugin().isPaused())
				getEnginePlugin().pause();
		}
	}

    public void closePlugin()
    {
        if (getEnginePlugin() != null) {
			Plugin oldPlugin = getEnginePlugin();
            oldPlugin.close();
			engine = null;
			broadcast(new Command("close.plugin", null, oldPlugin));
	        shownFRCWarning=false;
        }
    }

	public void switchPlugin()
		throws IOException
	{
		if (getEnginePlugin()!=null) {
			closePlugin();
			openEnginePlugin();
		}
	}

	public void restartPlugin() throws IOException
	{
		closePlugin();
		openEnginePlugin();
	}

	public void askRestartPlugin() throws IOException
	{
		int answer = JoDialog.showYesNoDialog("plugin.restart.ask","",null,null, JOptionPane.YES_OPTION);
		if (answer == JOptionPane.YES_OPTION)
			restartPlugin();
	}

	public void askSwitchPlugin() throws IOException
	{
		if (getEnginePlugin()==null)    //  no need to ask
			return;
		else {
			int answer = JoDialog.showYesNoDialog("plugin.switch.ask","",null,null, JOptionPane.YES_OPTION);
			if (answer == JOptionPane.YES_OPTION)
				switchPlugin();
		}
	}

	public int getInsertMoveWriteMode(Move mv)
	{
		int writeMode = theUserProfile.getInt("doc.write.mode",Game.ASK);
		if (writeMode != Game.ASK) return writeMode;

//		Point location;
		WriteModeDialog dialog = (WriteModeDialog)getDialog("dialog.write.mode");
		if (boardPanel() != null)
			dialog.setLocation(boardPanel().getView().getScreenLocation(mv.to));
		else
			dialog.stagger(JoFrame.getActiveFrame(),10,10);

		dialog.show(writeMode);
		writeMode = dialog.getWriteMode();

		if (writeMode==Game.CANCEL)
			return Game.CANCEL;

		if (dialog.askUser())
			theUserProfile.set("doc.write.mode",Game.ASK);
		else
			theUserProfile.set("doc.write.mode",writeMode);

		return writeMode;
	}

	protected void gameDraw()
	{
		try {
			theGame.setResult(Game.DRAW,theGame);
//			if (docPanel()!=null) docPanel().reformat();
		} catch (Exception e) {
			Application.error(e);
		}
		if (getEnginePlugin()!=null) getEnginePlugin().pause();
		theClock.halt();
	}

	protected String getPlayerName(int color)
	{
		String name = (String)theGame.getTagValue(EngUtil.isWhite(color) ? PgnConstants.TAG_WHITE:PgnConstants.TAG_BLACK);
		if (name == null)
			name = Language.get(EngUtil.isWhite(color) ? "message.white":"message.black");
		return name;
	}

	protected String getNextPlayerName()
	{
		return getPlayerName(theGame.getPosition().movesNext());
	}

	protected String getLastPlayerName()
	{
		return getPlayerName(theGame.getPosition().movedLast());
	}

	protected void gameFinished(int flags, int color, boolean mainLine)
			throws BadLocationException, ParseException
	{
		String message = null;
		boolean resultDirty = false;
		if (flags==Plugin.PLUGIN_RESIGNS) {
			message = Language.get("message.resign");
			message = StringUtil.replace(message,"%player%",getPlayerName(color));
			if (mainLine) {
				if (EngUtil.isWhite(color))
					resultDirty = theGame.setResult(Game.BLACK_WINS,theGame);
				else
					resultDirty = theGame.setResult(Game.WHITE_WINS,theGame);
			}
		}
		else if (flags==Clock.TIME_ELAPSED) {
			boolean whiteLose = (theClock.getWhiteTime() < 0) || !theGame.getPosition().canMate(WHITE);
			boolean blackLose = (theClock.getBlackTime() < 0) || !theGame.getPosition().canMate(BLACK);

			if (whiteLose && blackLose) {
				message = Language.get("message.time.draw");
				if (mainLine)
					resultDirty = theGame.setResult(Game.DRAW,theGame);
			}
			else if (whiteLose) {
				message = Language.get("message.time.lose");
				message = StringUtil.replace(message,"%player%",getLastPlayerName());
				if (mainLine)
					resultDirty = theGame.setResult(Game.BLACK_WINS,theGame);
			}
			else {
				message = Language.get("message.time.lose");
				message = StringUtil.replace(message,"%player%",getLastPlayerName());
				if (mainLine)
					resultDirty = theGame.setResult(Game.WHITE_WINS,theGame);
			}
		}
		else if (EngUtil.isMate(flags)) {
			if (EngUtil.isWhite(color)) {
				message = Language.get("message.mate");
				message = StringUtil.replace(message,"%player%",getPlayerName(Game.WHITE));
				if (mainLine)
					resultDirty = theGame.setResult(Game.WHITE_WINS,theGame);
			}
			else {
				message = Language.get("message.mate");
				message = StringUtil.replace(message,"%player%",getPlayerName(Game.BLACK));
				if (mainLine)
					resultDirty = theGame.setResult(Game.BLACK_WINS,theGame);
			}
		}
		else if (EngUtil.isStalemate(flags)) {
			message = Language.get("message.stalemate");
			if (mainLine)
				resultDirty = theGame.setResult(Game.DRAW,theGame);
		}
		else if (EngUtil.isDraw3(flags)) {
			message = Language.get("message.draw3");
			if (mainLine)
				resultDirty = theGame.setResult(Game.DRAW,theGame);
		}
		else if (EngUtil.isDraw50(flags)) {
			message = Language.get("message.draw50");
			if (mainLine)
				resultDirty = theGame.setResult(Game.DRAW,theGame);
		}
		else if (EngUtil.isDrawMat(flags)) {
			message = Language.get("message.drawmat");
			if (mainLine)
				resultDirty = theGame.setResult(Game.DRAW,theGame);
		}
		else
			return;

		if (resultDirty && docPanel()!=null) docPanel().reformat();

		if (getEnginePlugin()!=null)
			getEnginePlugin().pause();
		theClock.halt();

		getSoundFormatter();
		switch (theGame.getResult()) {
		case Game.DRAW:
				if (!EngUtil.isStalemate(flags) || theSoundFormatter==null ||
				        !theSoundFormatter.play("Stalemate.wav"))
					Sound.play("sound.draw");
				break;
		default:
		case Game.WHITE_WINS:
		case Game.BLACK_WINS:
				if (!EngUtil.isMate(flags) || theSoundFormatter==null ||
				        !theSoundFormatter.play("Checkmate.wav"))
					Sound.play("sound.mate");
				break;
		}
		SplashScreen.close();
        JOptionPane.showMessageDialog(JoFrame.getActiveFrame(),
				message, Language.get("message.result"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * opens a PGN file for reading
	 */
	protected void openFile()
	{
        File[] preferredDirs = new File[] {
            (File)theUserProfile.get("filechooser.open.dir"),
            new File(Application.theWorkingDirectory, "pgn"),
            Application.theWorkingDirectory,
        };

        int[] preferredFilters = new int[] {
            theUserProfile.getInt("filechooser.open.filter"),
		    JoFileChooser.ARCH,
            JoFileChooser.PGN,
            JoFileChooser.EPD,
        };

		JoFileChooser chooser = JoFileChooser.forOpen(preferredDirs, preferredFilters);

		if (chooser.showOpenDialog(JoFrame.getActiveFrame()) != JFileChooser.APPROVE_OPTION)
            return; //  cancelled

        File file = chooser.getSelectedFile();
        theUserProfile.set("filechooser.open.dir", chooser.getCurrentDirectory());
        theUserProfile.set("filechooser.open.filter", chooser.getCurrentFilter());

		try {
			int type = chooser.getCurrentFilter();
			if (FileUtil.hasExtension(file.getName(),"jos") || FileUtil.hasExtension(file.getName(),"jose"))
				type = JoFileChooser.ARCH;

			switch (type) {
			default:
			case JoFileChooser.PGN:
			case JoFileChooser.EPD:
                    showPanelFrame("window.collectionlist");
                    showPanelFrame("window.gamelist");

					PGNImport.openFile(file);
					break;

			case JoFileChooser.ARCH:
                    showPanelFrame("window.collectionlist");
                    showPanelFrame("window.gamelist");

					ArchiveImport task = new ArchiveImport(file);
					task.start();
					break;
			}
		} catch (FileNotFoundException ex) {
		    JoDialog.showErrorDialog("File not found: "+ex.getLocalizedMessage());
		} catch (IOException ex) {
		    JoDialog.showErrorDialog(ex.getLocalizedMessage());
        } catch (Exception ex) {
            Application.error(ex);
            JoDialog.showErrorDialog(ex.getLocalizedMessage());
        }
	}

	/**
	 * opens a PGN file for reading
	 */
	protected void openURL()
	{
        SplashScreen.close();
        String urlStr = JOptionPane.showInputDialog(Language.get("menu.file.open.url"));

        if (urlStr==null) return;
        urlStr = urlStr.trim();
        if (urlStr.length()==0) return;

        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException mex) {
            JoDialog.showErrorDialog(mex.getLocalizedMessage());
            return;
        }

//        theUserProfile.add("filechooser.url.history", url.toExternalForm());

        //  determine type
        String fileName = FileUtil.getFilePath(url);
		Map pmap = new HashMap();
		pmap.put("p",url.toExternalForm());

        try {
            if (FileUtil.hasExtension(fileName,"pgn") ||
                FileUtil.hasExtension(fileName,"epd") ||
                FileUtil.hasExtension(fileName,"fen") ||
                FileUtil.hasExtension(fileName,"zip") ||
                FileUtil.hasExtension(fileName,"gzip"))
            {
                showPanelFrame("window.collectionlist");
                showPanelFrame("window.gamelist");

                PGNImport.openURL(url);
            }
            else if (FileUtil.hasExtension(fileName,"jose"))
            {
                showPanelFrame("window.collectionlist");
                showPanelFrame("window.gamelist");

                ArchiveImport task = new ArchiveImport(url);
                task.start();
            }
            else {
                JoDialog.showErrorDialog(null,"download.error.invalid.url",pmap);
                return;
            }
        } catch (Exception e) {
            JoDialog.showErrorDialog(null,"download.error.invalid.url",pmap);
        }
    }

/*
    protected JoFileChooser showSaveDialog(String preferredName, int preferredType)
    {
        File[] preferredDirs = new File[] {
             (File)theUserProfile.get("filechooser.save.dir"),
//             new File(Application.theWorkingDirectory, "pgn"),
			 new File (Version.getSystemProperty("user.home")),
             Application.theWorkingDirectory,
         };

         int[] preferredFilters = new int[] {
             theUserProfile.getInt("filechooser.save.filter"),
             JoFileChooser.PGN,
			 JoFileChooser.JOSE_GAMES,
			 JoFileChooser.JOSE_MYSQL_GAMES,
			 //	TODO choose the best of these two
         };

        JoFileChooser chooser = JoFileChooser.forSave(preferredDirs, preferredFilters,
														preferredName);

        if (chooser.showSaveDialog(JoFrame.theActiveFrame) != JFileChooser.APPROVE_OPTION)
            return null; //  cancelled

        File file = chooser.getSelectedFile();
        theUserProfile.set("filechooser.save.dir",  chooser.getCurrentDirectory());
        theUserProfile.set("filechooser.save.filter",   chooser.getCurrentFilter());

        String defExt = JoFileChooser.getFileExtension(chooser.getCurrentFilter());
        if (defExt != null)
             file = FileUtil.appendExtension(file,defExt);
	    else if (! FileUtil.hasExtension(file.getName())) {
	        //  append preferred extension
	        String newName = FileUtil.setExtension(file.getName(),
	                                    JoFileChooser.getFileExtension(preferredType));
	        file = new File(file.getParentFile(), newName);
	    }

        if (file.exists() && !JoFileChooser.confirmOverwrite(file))
            return null; //  don't overwrite
        //  else:
        chooser.setSelectedFile(file);
        return chooser;
    }
*/
	public String getPreferredFileName(GameSource src)
	{
		int firstId = src.firstId();

		if (src.isCollection())
		try {
			Collection coll = Collection.readCollection(firstId);
			return coll.Name;
		} catch (Exception ex) {

		}

		return null;
	}
/*
    public void exportGames(GameSource src)
        throws Exception
	{
        JoFileChooser chooser = showSaveDialog(getPreferredFileName(src),JoFileChooser.PGN);
        File xslFile = null;
        if (chooser==null) return;

        File file = chooser.getSelectedFile();
        int type = chooser.getCurrentFilter();
		if (type==0) type = JoFileChooser.PGN;

        DBTask task;
        switch (type) {
	    default:
        case JoFileChooser.PGN:
                task = new PGNExport(file);
                break;

        case JoFileChooser.JOSE_GAMES:
                task = new GenericBinaryExport(file);
                break;

		case JoFileChooser.JOSE_MYSQL_GAMES:
				task = new ArchiveExport(file);
				break;

        case JoFileChooser.HTML:
                task = null;// TODO new XSLExport(file,type, new File(Application.theWorkingDirectory,"xsl/html.xsl"));
                break;
        case JoFileChooser.PDF:
                task = null;// TODO new XSLExport(file,type, new File(Application.theWorkingDirectory,"xsl/pdf.xsl"));
                break;
        case JoFileChooser.TEXT:
                task = null;// TODO new XSLExport(file,type, new File(Application.theWorkingDirectory,"xsl/text.xsl"));
                break;
        case JoFileChooser.XML:
		        /** @deprecated * /
		        ExportContext context = new ExportContext();
		        context.source = src;
		        context.profile = theUserProfile;
	            context.styles = (JoStyleContext)theUserProfile.getStyleContext().clone();
		        context.target = file;
                task = new XMLExport(context);
                break;
        }

		if (task instanceof GameTask)
			((GameTask)task).setSource(src);
		else if (task instanceof MaintenanceTask)
			((MaintenanceTask)task).setSource(src);
		//	TODO unify

        task.start();
        return;
    }
*/

	protected void openHistory()
	{
		int[] gids = theUserProfile.getHistory();
		if (gids != null && gids.length > 0)
		{
			GameSource src = GameSource.gameArray(gids);
			theCommandDispatcher.forward(new Command("edit.all", null,src,Boolean.FALSE), theApplication);
		}
	}

	public synchronized boolean quit(Command cmd) throws Exception
	{
		//  close modeless dialogs
        SplashScreen.close();
		JoDialog.closeAll();

		//  ask for dirty docs
		if (theHistory.isDirty()) {
			switch (confirmSaveAll()) {
			case JOptionPane.YES_OPTION:	theHistory.saveAll(); break;
			case JOptionPane.NO_OPTION:		break;
			default:
			case JOptionPane.CANCEL_OPTION:	return false;
			}
		}

		broadcast(cmd);

		try {
			if (applListener!=null) applListener.close();
		} catch (Throwable e) {
		    Application.error(e);
		}

		//  save user profile
		try {
			if (helpSystem!=null)
				helpBounds = getHelpSystem().getWindowBounds();

			theUserProfile.update(helpBounds);

			writeProfile();

		    if (helpSystem!=null)
				getHelpSystem().close();
			JoFrame.closeAll();
		} catch (Throwable thr) {
			Application.error(thr);
		}

		//  close connection pool
		try {
			DBAdapter ad = JoConnection.getAdapter(false);
			if (ad!=null && (ad.getServerMode()==DBAdapter.MODE_STANDALONE) && JoConnection.isConnected()) {
				ad.shutDown(JoConnection.get());
			}

			JoConnection.closeAll();

		} catch (Throwable thr) {
			Application.error(thr);
		}
/*
        try {
            File lockfile = new File(theWorkingDirectory,"lock."+theDatabaseId);
            lockfile.delete();
        } catch (Exception e) {
            /* can't help it *
        }
*/
		//  close engine plugin
		try {
			closePlugin();
		} catch (Throwable thr) {
			Application.error(thr);
		}

		return true;
	}


	/**
	 *	read application & user profile from disk,
	 *	or revert to factory settings
	 */
	public void readProfile()
		throws IOException, ClassNotFoundException
	{
        /** list of preferred profile locations */
        ArrayList search_path = new ArrayList();

		String profilePath = Version.getSystemProperty("jose.profile");
		if (profilePath!=null) {
			//	1. path to profile explicitly set on command line
			File profile = new File(profilePath);
			if (profile.getParentFile().exists()) search_path.add(profile);
		}

        String homePath = Version.getSystemProperty("user.home");
        //  2. Library/Preferences Path (on Macs)
        File prefDir = new File(homePath,"Library/Preferences");
        if (prefDir.exists())
        {
            search_path.add(new File(prefDir,USER_PROFILE.substring(1)));  //  without "."
            search_path.add(new File(prefDir,USER_PROFILE));
        }

        //	3. default: user home directory
        File homeDir = new File(homePath);
        if (homeDir.exists())
            search_path.add(new File(homeDir,USER_PROFILE));

        //	4. default: jose installation directory
	    search_path.add(new File(theWorkingDirectory,USER_PROFILE));

        //  5. default: current directory
        search_path.add(new File(USER_PROFILE));

		theUserProfile = UserProfile.open(search_path);
		//	DON'T launch 3D window (it's just not reliable)
		theUserProfile.set("board.3d", Boolean.FALSE);
	}

	/**
	 *	write application & user profile to disk
	 */
	public void writeProfile()
		throws IOException
	{
		UserProfile.write(theUserProfile,UserProfile.searchPath,true);
	}

	public HelpSystem getHelpSystem()
	{
		if (helpSystem==null) {
			helpBounds = JoFrame.adjustBounds(helpBounds,true);
			helpSystem = new HelpSystem(theWorkingDirectory, "doc/man/help-en.hs", helpBounds);
		}
		return helpSystem;
	}

	public static File getWorkingDirectory() throws IOException
	{
		String workDir = Version.getSystemProperty("jose.workdir");
		if (workDir==null) workDir = Version.getSystemProperty("user.dir");
		if (workDir==null) workDir = ".";

        return new File(workDir).getCanonicalFile();
	}


	public ExportConfig getExportConfig()
	{
		if (theExportConfig==null)
			try {
				theExportConfig = new ExportConfig(new File(theWorkingDirectory,"xsl"));
			} catch (Exception e) {
				Application.error(e);
			}
		return theExportConfig;
	}

	//		implements plugin InputListener
	public void readLine(char[] chars, int offset, int len) { }

	//		implements plugin InputListener
	public void readEOF() {	}

	//		implements plugin InputListener
	public void readError(Throwable ex) {
		closePlugin();
		try {
			openEnginePlugin();
			//  hm ... this will most likely lead to follow-up errors...
			//  but there's not much else we can do.
		} catch (IOException e) {
			Application.error(e);
		}
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		//  implements ClipboardOwner
	}


}
