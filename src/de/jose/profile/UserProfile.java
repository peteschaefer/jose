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

package de.jose.profile;

import de.jose.Application;
import de.jose.comm.Command;
import de.jose.Util;
import de.jose.Version;
import de.jose.chess.MoveFormatter;
import de.jose.chess.TimeControl;
import de.jose.image.Surface;
import de.jose.pgn.Game;
import de.jose.util.style.StyleUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.XObjectInputStream;
import de.jose.util.print.SerializablePageFormat;
import de.jose.util.print.PrintableDocument;
import de.jose.view.ClockPanel;
import de.jose.view.JoPanel;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;
import de.jose.window.JoFrame;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.print.PageFormat;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;

import static de.jose.Application.AppMode.USER_ENGINE;
import static de.jose.Application.AppMode.USER_INPUT;

/**
 *	stores user specific sessions
 */

public class UserProfile
		implements Serializable, Cloneable
{
	/**	serial version UID	 */
	static final long serialVersionUID = 5147573197728040059L;

	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	current meta version	*/
	public static final int META_VERSION = 1008;
	public static final int FIRST_META_VERSION = 1000;

	/**	FILE format	*/
	public static final int PLAIN	= 1;
	public static final int ZIP		= 2;
	public static final int GZIP	= 3;

	public static String getFactoryLookAndFeel() {
		if (Version.mac) {
			if (StyleUtil.getSystemDarkMode())
				return "com.formdev.flatlaf.themes.FlatMacDarkLaf";
			else
				return "com.formdev.flatlaf.themes.FlatMacLightLaf";
		}
		else {
			if (StyleUtil.getSystemDarkMode())
				return "com.formdev.flatlaf.FlatDarkLaf";
			else
				return "com.formdev.flatlaf.FlatLightLaf";
		}
		/**
		 * Metouia on Windows & Linux
		 * Metouia doesn't work on OS X, that's why we use the default, Aqua
		 * (looks better anyway)
		 */
	}

	public static String getFactoryTextFont() {
		return "Arial";
	}

	public static String getFactoryDiagramFont() {
		return "Chess Berlin";
	}

	public static String getFactoryFigurineFont() {
		return "Chess Berlin";
	}

	public static String getFactorySymbolFont() { return "FigurineSymbol S2"; }

	public static final String FACTORY_ENGINES = "lc0;stockfish";

	public static final Object[] FACTORY_SETTINGS = {
		"meta.version",				META_VERSION,
		"clock.display",			ClockPanel.ANALOG,
		"font.diagram",             getFactoryDiagramFont(),

		"board.3d",					Boolean.FALSE,
		"board.flip",				Boolean.FALSE,
		"board.coords",				Boolean.TRUE,
		"board.evalbar",			Boolean.TRUE,
		"board.suggestions",		Boolean.TRUE,

//		"board.surface.light",		Surface.newColor(0xee,0xee,0xee, "wood01.jpg"),
//		"board.surface.dark",		Surface.newColor(0x88, 0x88, 0x88, "wood02.jpg"),
		"board.surface.light",		Surface.newTexture(0xee,0xee,0xee, "marble04.jpg"),
		"board.surface.dark",		Surface.newTexture(0x88, 0x88, 0x88, "marble13.jpg"),

		"board.surface.white",		Surface.newColor(Color.white, "wood03.jpg"),
		"board.surface.black",		Surface.newColor(Color.black, "wood04.jpg"),

		"board.surface.background",	Surface.newTexture(Color.white, "marble12.jpg"),	//	greenish marble
//		"board.surface.background",	new Surface(Surface.USE_TEXTURE, Color.white, "marble01.jpg"),	//	red marble
		"board.surface.coords",		Surface.newColor(Color.black, "marble02.jpg"),
        "board.hilite.squares",		Boolean.FALSE,
		"board.animation.hints",    Boolean.FALSE,
		"lnf.accent.color",			Surface.newColor(Color.decode("#2675BF")),

//		"board.3d.model",				"std2.j3df",
		"board.3d.model",				"fab100.j3df",
		"board.3d.camera.distance",		2.0,
		"board.3d.camera.latitude",		3*Math.PI/8,
        "board.3d.knight.angle",		0,

		"board.3d.surface.frame",		Surface.newTexture(66,45,0, "wood03.jpg"),
		"board.3d.light.ambient",		new Color(0.1f,0.1f,0.1f),
		"board.3d.light.directional",	new Color(0.9f,0.9f,0.9f),

        "board.3d.clock",               Boolean.FALSE,
		"board.3d.shadow",				Boolean.TRUE,
		"board.3d.reflection",			Boolean.FALSE,
        "board.3d.anisotropic",         Boolean.FALSE,
        "board.3d.fsaa",                Boolean.FALSE,

		"doc.panel.tab.placement",		JTabbedPane.TOP,
		"doc.panel.tab.layout",			JTabbedPane.WRAP_TAB_LAYOUT,
		"doc.panel.antialias",          Boolean.TRUE,
		"doc.classify.eco",				Boolean.TRUE,
		"plugin.pv.tooltips",			Boolean.TRUE,

		"game.mode",					USER_ENGINE.numval,
		"doc.write.mode",				Game.ASK,
		"doc.load.history",				Boolean.TRUE,
		"doc.move.format", 				MoveFormatter.SHORT,

		"plugin.1",						FACTORY_ENGINES,
		"plugin.2",						"stockfish",

		"time.controls",				TimeControl.FACTORY_SETTINGS,
		"time.control.current",			0,	//	index in "time.controls"

        "animation.speed",              800L,
	};


	//-------------------------------------------------------------------------------
	//	current settings
	//-------------------------------------------------------------------------------

	/**	current settings */
	public HashMap settings;

    /**	frame layout (Vector of FrameProfile)	 */
	public Vector frameLayout;

	/**	panel layout (Maps panel names to of LayoutProfile)	 */
	public HashMap panelLayout;

	/**	document history (database ids)	*/
	public int[] history;

    /** current style context
     *  (serial version of StyleContext depends on JDK;
     *   when changing the JDK, these information is lost,
     *   but at least not all settings are lost)
     */
    public JoStyleContext styles;

    /** search path for files on disk  */
    public static List searchPath;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public UserProfile(boolean init)
	{
		if (init)
			init();
		else {
			settings = new HashMap();
			frameLayout = new Vector();
			panelLayout = new HashMap();
			styles = new JoStyleContext();
			history = null;
		}
	}

	private void init()
	{
		panelLayout = new HashMap();
		createFactorySettings();
		createFactoryStyles();
        createFactoryFrameLayout(null);
		/*	initially empty	*/
		history = null;
	}

	public UserProfile(File file)
		throws IOException
	{
		if (Version.getSystemProperty("jose.discard.profile",false)) {
			init();
			return;
		}

		ObjectInputStream oin = null;
		FileInputStream fin = null;
		BufferedInputStream bin = null;

		try {
			fin = new FileInputStream(file);

			int format = fin.read();
			switch (format) {
            default:
			case PLAIN:
					bin = new BufferedInputStream(fin,4096);
					oin = new XObjectInputStream(bin);
					break;
			case GZIP:
					GZIPInputStream gin = new GZIPInputStream(fin,4096);
					oin = new XObjectInputStream(gin);
					break;
			case ZIP:
					bin = new BufferedInputStream(fin,4096);
					ZipInputStream zin = new ZipInputStream(bin);
					ZipEntry zety = zin.getNextEntry();
					oin = new XObjectInputStream(zin);
					break;
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			init();
			return;
		}

		try {
			settings = (HashMap)oin.readObject();
			if (Version.getSystemProperty("jose.discard.settings",false))
				createFactorySettings();	//	explicity drop settings
			else {
				if (getInt("meta.version") <= 1010)
					createFactoryToolbars();
				//	in versions up to ... toolbars are not editable (though they are stored in the preferences)
				//	always revert to factory settings
				if (Version.getSystemProperty("jose.discard.lnf",false))
					set("ui.look.and.feel2", getFactoryLookAndFeel());
				if (Version.getSystemProperty("jose.discard.font.map",false)) {
					set("font.map",new HashMap());
					set("sys.font.map",new HashMap());
				}
			}
		} catch (Exception ex) {
			/**	may happen when switching between JDKs with incompatible stream format
			 *	(e.g. javax.text.StyleContext)
			 */
			System.out.println(ex.getMessage());
			createFactorySettings();
		}

		try {
            //  does factory layout override current user layout ?
            //  if so, apply it
            boolean override = Version.getSystemProperty("jose.discard.layout",false);
            Element factory_layout = null;

            if (!override) {
                String current_id = getString("current.layout");
                int current_override = getInt("layout.override",0);

                factory_layout = Application.theApplication.theConfig.getFactoryLayout();
                if (factory_layout!=null) {
                    String factory_id = factory_layout.getAttribute("id");
                    int factory_override = Util.toint(factory_layout.getAttribute("override"));

                    if (!Util.equals(current_id,factory_id) && (factory_override > current_override))
                        override = true;
                    //  a new factory (or customer) layout was deployed that overrides the previous layout !!
                }
            }

            if (override)
                createFactoryFrameLayout(factory_layout);	//	explicity drop frame layout
            else  //  otherwise: keep current user layout
                frameLayout = (Vector)oin.readObject();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			createFactoryFrameLayout(null);
		}

		try {
			panelLayout = (HashMap)oin.readObject();
			if (Version.getSystemProperty("jose.discard.layout",false)) {
				panelLayout = new HashMap();	//	explicity drop panel layout
				copyPanelLayout(frameLayout);
			}
			else
				checkPanelLayout(frameLayout);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			panelLayout = new HashMap();
			copyPanelLayout(frameLayout);
		}

		try {
            if (Version.getSystemProperty("jose.discard.styles",false) || getInt("meta.version") < 1000)
                createFactoryStyles();	//	explicity drop styles
            else {
			    styles = (JoStyleContext)oin.readObject();
                setFixedStyles();
            }
            styles.setNormFontScale();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			createFactoryStyles();
		}

		try {
			history = (int[])oin.readObject();
			if (Version.getSystemProperty("jose.discard.history",false))
				history = null;	//	explicity drop styles
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			history = null;
		}

		oin.close();
		fin.close();

		//	crossover from old file version
		crossover();
	}

    public static UserProfile open(ArrayList path)
    {
        searchPath = (List)path.clone();
        /** find first existing file in search path */
        Iterator i = path.iterator();
        while (i.hasNext())
        {
            File file = (File)i.next();
            if (file.exists())
                try {
                    UserProfile profile = new UserProfile(file);
                    return profile;
                } catch (IOException ioex) {
                    //  try next
                }
        }
        //  otherwise: create a new one
        return new UserProfile(true);
    }

    public static File write(UserProfile profile, List path, boolean erase)
    {
        File result = null;
        Iterator i = path.iterator();
        while (i.hasNext())
        {
            File file = (File)i.next();
            try {
                profile.write(file);
                result = file;
                break;

            } catch (IOException e) {
                //  delete file, try next
                if (erase) file.delete();
            }
        }

        if (erase)
            while (i.hasNext())
            {
                File file = (File)i.next();
                file.delete();
            }

        return result;
    }



	public void crossover()
	{
		int oldVersion = getInt("meta.version");
		
		if (oldVersion < 0) {
			//	version info missing ?!
			set("meta.version", oldVersion=FIRST_META_VERSION);
		}

		if (oldVersion <= 1000)
			set("doc.classify.eco",true);
		if (oldVersion <= 1004) {
			//  new panel layout for preview window
			panelLayout.remove("window.print.preview");
			addPanelLayout(FrameProfile.FRAME_LAYOUT_PREVIEW);
		}
		if (oldVersion <= 1005) {
			//  clear font maps
			set("font.map",new HashMap());
			set("sys.font.map",new HashMap());
			//  inert new style "html.large"
			Style inline = styles.getStyle("body.inline");
			Style html_large = styles.addStyle("html.large", inline);   //  large HTML diagram
			JoFontConstants.setInline(html_large,true);
			StyleConstants.setFontSize(html_large,20);
		}
		if (oldVersion <= 1006) {
			//  insert layout profile for EvalProfile panel
			//  but hide it (will be introduced later)
			LayoutProfile oldLayout = (LayoutProfile)panelLayout.get("window.engine");
			LayoutProfile evalLayout = new LayoutProfile(
			        FrameProfile.PANEL_LAYOUT_EVAL.name,
			        FrameProfile.PANEL_LAYOUT_EVAL.weightX,
			        FrameProfile.PANEL_LAYOUT_EVAL.weightY,
			        FrameProfile.PANEL_LAYOUT_EVAL.showBorder,
			        false);
			evalLayout.frameProfile = oldLayout.frameProfile;
			panelLayout.put("window.eval",evalLayout);

			LayoutProfile newLayout = FrameProfile.vsplit(evalLayout,oldLayout);
			replacePanelProfile("window.engine",newLayout);
		}
        if (oldVersion <= 1007) {
            //  new default l6f on Macs
            if (Version.mac) {
                String lnf = getString("ui.look.and.feel2");
                if ("apple.laf.AquaLookAndFeel".equals(lnf) ||
                    "com.apple.mrj.swing.MacLookAndFeel".equals(lnf))
                {
                    lnf = getFactoryLookAndFeel();
                    set("ui.look.and.feel2",lnf);
                }
            }
        }

		if (oldVersion < META_VERSION)
			set("meta.version",META_VERSION);
	}

	private void replacePanelProfile(String name, LayoutProfile newLayout)
	{
		for (int i=0; i < frameLayout.size(); i++)
		{
			FrameProfile fpf = (FrameProfile)frameLayout.get(i);
			fpf.replace(name,newLayout);
		}
	}

	public void write(File file)
		throws IOException
	{
		int format = PLAIN;

		FileOutputStream fout = new FileOutputStream(file,false);
		BufferedOutputStream bout = null;

		ObjectOutputStream oout = null;
		GZIPOutputStream gout = null;
		ZipOutputStream zout = null;

		fout.write(format);

		switch (format) {
		case PLAIN:
				bout = new BufferedOutputStream(fout,4096);
				oout = new ObjectOutputStream(bout);
				break;
		case GZIP:
				gout = new GZIPOutputStream(fout,4096);
				oout = new ObjectOutputStream(gout);
				break;
		case ZIP:
				bout = new BufferedOutputStream(fout,4096);
				zout = new ZipOutputStream(bout);
				zout.putNextEntry(new ZipEntry("user.preferences"));
				oout = new ObjectOutputStream(zout);
				break;
		}

		oout.writeObject(settings);
		oout.writeObject(frameLayout);
		oout.writeObject(panelLayout);
		oout.writeObject(styles);
		oout.writeObject(history);
		oout.flush();

		switch (format) {
		case PLAIN:		fout.close();
						break;
		case ZIP:		zout.closeEntry();
						zout.finish();
						zout.close();
						break;
		case GZIP:		gout.finish();
						gout.close();
						break;
		}
	}

    private void createFactorySettings()
    {
        settings = new HashMap();
		Util.add(settings, FACTORY_SETTINGS);
		set("user.name", Version.getSystemProperty("user.name"));
		set("user.language", Version.getSystemProperty("user.language"));
//		set("ui.look.and.feel", LookAndFeelList.getDefaultClassName());
	    set("ui.look.and.feel", getFactoryLookAndFeel());
	    //  Metouia on Windows & Linux, Aqua on OS X
		//	use system default, or ...

		//	which is better ?
		//	Metouia certainly looks nice, but unusual to the first time user ?!
		createFactoryToolbars();
   }

	private void restoreFactorySettings(String key)
	{
		for (int i=0; i < FACTORY_SETTINGS.length; i += 2)
			if (FACTORY_SETTINGS[i].equals(key))
				set(key,FACTORY_SETTINGS[i+1]);
	}

    public void createFactoryFrameLayout(Element factory_layout)
    {
        //  examine config
        if (factory_layout==null)
            factory_layout = Application.theApplication.theConfig.getFactoryLayout();

        if (factory_layout != null) {
            //  use xml layout
            frameLayout = FrameProfile.deserializeXml(factory_layout);
            set("current.layout", factory_layout.getAttribute("id"));
            set("layout.override", Util.toint(factory_layout.getAttribute("override")));
        }
        else {
            //  use hardcoded factory layout
            frameLayout = new Vector();
            for (int i=0; i < FrameProfile.FACTORY_LAYOUT.length; i++)
                frameLayout.add(FrameProfile.FACTORY_LAYOUT[i]);
            set("current.layout",null);
            set("layout.override",0);
        }

        panelLayout = new HashMap(); //  this is just needed to setup the frame layout
		copyPanelLayout(frameLayout);
    }

	private void copyPanelLayout(Vector frames)
	{
		for (int i=0; i<frames.size(); i++) {
			FrameProfile fpf = (FrameProfile)frames.get(i);
			LayoutProfile ppf = fpf.componentLayout;
			if (ppf!=null)
				copyPanelLayout(ppf,fpf,"");
		}
	}

	private void addPanelLayout(FrameProfile fpf)
	{
		frameLayout.add(fpf);
		LayoutProfile ppf = fpf.componentLayout;
		if (ppf!=null)
			copyPanelLayout(ppf,fpf,"");
	}

	/**
	 * workaround for bug ?
	 */
	private void checkPanelLayout(Vector frames)
	{
		for (int i=0; i<frames.size(); i++) {
			FrameProfile fpf = (FrameProfile)frames.get(i);
			LayoutProfile ppf = fpf.componentLayout;
			if (ppf!=null)
				checkPanelLayout(ppf,fpf,"");
		}
	}

	private void copyPanelLayout(LayoutProfile ppf, FrameProfile fpf, String dockPath)
	{
		if (ppf==null)
			/*noop*/ ;
		else if (ppf.isPanelProfile())
		{
			if (!panelLayout.containsKey(ppf.name)) {
				ppf.frameProfile = fpf;
				ppf.dockingPath = dockPath;
				panelLayout.put(ppf.name,ppf);
			}
		}
		else {
			copyPanelLayout(ppf.firstComponent,fpf, ppf.appendDockPath(dockPath,1));
			copyPanelLayout(ppf.secondComponent,fpf, ppf.appendDockPath(dockPath,2));
		}
	}

	private void checkPanelLayout(LayoutProfile ppf, FrameProfile fpf, String dockPath)
	{
		if (ppf==null)
			/*noop*/ ;
		else if (ppf.isPanelProfile())
		{
			if (ppf.frameProfile != fpf) {
				System.out.println("correcting inconsistent  profile: "+ppf.name);
				ppf.frameProfile = fpf;
			}
		}
		else {
			checkPanelLayout(ppf.firstComponent,fpf, ppf.appendDockPath(dockPath,1));
			checkPanelLayout(ppf.secondComponent,fpf, ppf.appendDockPath(dockPath,2));
		}
	}

	private void createFactoryToolbars()
	{
		Enumeration bars = Application.theApplication.theConfig.enumerateElements("toolbar");
		while (bars.hasMoreElements()) {
			Element bar = (Element)bars.nextElement();
			String id = bar.getAttribute("id");
			set(id,createToolbar(bar));
		}
	}

	private List createToolbar(Element elm)
	{
		Vector result = new Vector();
		for (Node nd = elm.getFirstChild(); nd!=null; nd = nd.getNextSibling())
			if ("separator".equals(XMLUtil.getTagName(nd)))
				result.add(null);
			else if ("item".equals(XMLUtil.getTagName(nd))) {
				Element el = (Element)nd;
				result.add(el.getAttribute("id"));
			}
		return result;
	}

	public Object clone()
	{
		try {
			return super.clone();
		} catch (CloneNotSupportedException ex) {
			//	never thrown
			return null;
		}
	}

	public final Object get(String key)					{ return settings.get(key); }

	public final Object get(String key, Object defaultValue) { 
		Object result = settings.get(key);
		if (result==null) result = defaultValue;
		return result;
	}

	public final void set(String key, Object value)		{ settings.put(key,value); }

	public final String getString(String key)			{ return (String)get(key); }

	public final String getString(String key, String defaultValue) {
		return (String)get(key,defaultValue);
	}

	public final int getInt(String key)					{
		return getInt(key,Integer.MIN_VALUE);
	}

	public final int getInt(String key, int defaultValue)					{
		Object n = get(key);
		return (n!=null) ? Util.toint(n) : defaultValue;
	}

	public final double getDouble(String key)			{
		return getDouble(key,Double.MIN_VALUE);
	}

	public final double getDouble(String key, double defaultValue)			{
		Object d = get(key);
		return (d!=null) ? Util.todouble(d) : defaultValue;
	}

	public final void set(String key, int value)		{ set(key, (Integer)value); }

	public final boolean getBoolean(String key) {
		return getBoolean(key,false);
	}

	public final boolean getBoolean(String key, boolean defaultValue) {
		Object b = get(key);
		if (b==null)
			return defaultValue;
		else
			return Util.toboolean(b);
	}

	public final void set(String key, boolean value)	{ set(key, (Boolean)value); }


	public final Object getStyleAttr(String styleName, Object key)
	{
		return getStyleContext().getStyle(styleName).getAttribute(key);
	}

	public final void set(String styleName, Object key, Object value)
	{
		Style st = getStyleContext().getStyle(styleName);
		if (st==null)
			st = getStyleContext().addStyle(styleName,null);
		st.addAttribute(key,value);
	}

	public final String getStyleAttribute(String styleName, Object key)		{
		return (String)getStyleAttr(styleName,key);
	}
/*
	public final int getInt(String styleName, Object key)	{
		Integer i = (Integer)getStyleAttr(styleName,key);
		return (i!=null) ? i.intValue() : Integer.MIN_VALUE;
	}

	public final boolean getBoolean(String styleName, Object key)
	{
		return Util.toboolean(getStyleAttr(styleName,key));
	}
*/
	public final boolean changed(String key, UserProfile old)
	{
		Object oldObj = old.get(key);
		Object newObj = get(key);
		return !Util.equals(oldObj,newObj);
	}

	public final boolean changed(String styleName, String attrKey, UserProfile old)
	{
		Object oldObj = old.getStyleAttr(styleName,attrKey);
		Object newObj = getStyleAttr(styleName,attrKey);
		return !Util.equals(oldObj,newObj);
	}

	public final boolean changed(String key, Map old)
	{
		Object oldObj = old.get(key);
		Object newObj = get(key);
		return !Util.equals(oldObj,newObj);
	}

	public FrameProfile[] getFrameProfiles()
	{
		FrameProfile[] result = new FrameProfile[frameLayout.size()];
		frameLayout.toArray(result);
		return result;
	}

	public void addFrameProfile(FrameProfile profile)
	{
		frameLayout.add(profile);
	}

	public void removeFrameProfile(FrameProfile profile)
	{
		frameLayout.remove(profile);
	}

	public LayoutProfile getPanelProfile(String name)
	{
		return (LayoutProfile)panelLayout.get(name);
	}

	public void addPanelProfile(LayoutProfile profile)
	{
		panelLayout.put(profile.name,profile);
	}

	public JoStyleContext getStyleContext()
	{
		return styles;
	}

	public boolean useFigurineFont()
	{
		return getStyleContext().useFigurineFont();
//		return Util.toboolean(getStyleAttr("base","figurine.usefont"));
	}

	public boolean setFigurineFont(boolean useFigurineFont)
	{
		boolean dirty = useFigurineFont() != useFigurineFont;
		getStyleContext().setFigurineFont(useFigurineFont);
//		set("base","figurine.usefont", Util.toBoolean(useFigurineFont));
		return dirty;
	}

	public String getFigurineLanguage()
	{
//		String lang = (String)getStyleAttribute("base","figurine.language");
		String lang = getStyleContext().getFigurineLanguage();
		if (lang==null)
			lang = getString("user.language");
		if (lang==null)
			lang = Version.getSystemProperty("user.language");
		return lang;
	}

	public boolean setFigurineLanguage(String lang)
	{
		boolean dirty = !Util.equals(getFigurineLanguage(),lang);
//		set("base","figurine.language",lang);
		getStyleContext().setFigurineLanguage(lang);
		return dirty;
	}


	public int[] getHistory()
	{
		return history;
	}

	public void setHistory(int[] hist)
	{
		history = hist;
	}

	/**
	 * get the printer page format, or null if not yet defined
	 */
	public PageFormat getPageFormat(boolean create)
	{
		PageFormat result = (PageFormat)get("print.page.format");
		if (result!=null)
			result = PrintableDocument.validPageFormat(result);
		else if (create)
			result = PrintableDocument.validPageFormat(null);
		return result;
	}

	public void setPageFormat(PageFormat format)	{
		format = new SerializablePageFormat(format);
		set("print.page.format",format);
	}

	/**	@return a Vector of user defined Time Control settings
	 */
	public Vector<TimeControl> getTimeControls()		{
		Vector<TimeControl> controls = (Vector<TimeControl>)get("time.controls");
		if (controls==null)
			set("time.controls",controls = TimeControl.FACTORY_SETTINGS);
		return controls;
	}

	/**	@return the current Time Control
	 */
	public int getTimeControlIdx()	{
		int idx = getInt("time.control.current");
		Vector<TimeControl> controls = getTimeControls();
		if (idx < 0 || idx >= controls.size())
			idx = setTimeControlIdx(0);
		return idx;
	}

	/**	@return the current Time Control
	 */
	public TimeControl getTimeControl()	{
		Vector<TimeControl> controls = getTimeControls();
		int idx = getTimeControlIdx();
		return controls.get(idx);
	}

	public int setTimeControlIdx(int idx)
	{
		Vector<TimeControl> controls = getTimeControls();
		if (idx < 0 || idx >= controls.size())
			idx = 0;
		set("time.control.current",idx);
		return idx;
	}

   	public void update(Rectangle helpWindowBounds)
	{
		FrameProfile fpf = (FrameProfile)frameLayout.get(0);
		fpf.bounds = helpWindowBounds;
		fpf.state = FrameProfile.HELP_FRAME;

		//	store frame layouts
		for (int i=0; i < JoFrame.countFrames(); i++) {
			JoFrame frm = JoFrame.getFrame(i);
			frm.updateProfile();
		}

		//	store panel layouts
		Iterator i = JoPanel.getAllPanels().iterator();
		while (i.hasNext()) {
			JoPanel panel = (JoPanel)i.next();
			panel.calcLocation();
		}

		Application.AppMode appmode = (Application.AppMode)
				Util.nvl(Application.theApplication.theMode,USER_INPUT);
		set("game.mode", appmode.numval);
        set("animation.speed", (int)Application.theApplication.getAnimation().getSpeed());

		Command cmd = new Command("update.user.profile", null, this);
		Application.theApplication.broadcast(cmd);
	}

    /**
     * some style attributes are not yet editable,
     * they never change...(but could be in the future)
     */
    public void setFixedStyles()
    {
        Style line = styles.getStyle("body.line");
        line.addAttribute("variation.newline",Boolean.FALSE);

        Style line0 = styles.getStyle("body.line.0");
        line0.addAttribute("variation.prefix","\n");    //  spacer between header section and main line

        Style line1 = styles.getStyle("body.line.1");
        line1.addAttribute("variation.prefix"," [ ");
        line1.addAttribute("variation.suffix","]");
        line1.addAttribute("variation.newline",Boolean.TRUE);
		StyleConstants.setLeftIndent(line1,30);

        Style line2 = styles.getStyle("body.line.2");
        line2.addAttribute("variation.prefix"," [ ");
        line2.addAttribute("variation.suffix","] ");
	    line2.addAttribute("variation.newline",Boolean.TRUE);
		StyleConstants.setLeftIndent(line2,60);

        Style line3 = styles.getStyle("body.line.3");
        line3.addAttribute("variation.prefix"," { ");
        line3.addAttribute("variation.suffix","} ");

        Style line4 = styles.getStyle("body.line.4");
        line4.addAttribute("variation.prefix"," ( ");
        line4.addAttribute("variation.suffix",") ");

        Style inline = styles.getStyle("body.inline");
        JoFontConstants.setInline(inline,true);

        Style html_large = styles.getStyle("html.large");
        JoFontConstants.setInline(html_large,true);

        Style figurine = styles.getStyle("body.figurine");
		JoFontConstants.setFigurine(figurine,true);

        Style symbol = styles.getStyle("body.symbol");
		JoFontConstants.setSymbol(symbol,true);
    }

	public void createFactoryStyles()
	{
		styles = new JoStyleContext();

		Style base		= styles.addStyle("base",null);
		StyleConstants.setFontFamily(base, getFactoryTextFont());
		StyleConstants.setFontSize(base,12);

		/**	use figurine font ?		 */
		base.addAttribute("figurine.usefont",	Boolean.TRUE);
		/**	move output format		 */
		base.addAttribute("move.format",		MoveFormatter.SHORT);

		Style header	= styles.addStyle("header",base);
		Style body		= styles.addStyle("body",base);

		/*Style event	=*/ styles.addStyle("header.event", header);
		/*Style site	=*/ styles.addStyle("header.site", header);
		/*Style date	=*/ styles.addStyle("header.date", header);
		/*Style round	=*/ styles.addStyle("header.round", header);

		Style white		= styles.addStyle("header.white", header);
		StyleConstants.setBold(white,true);

		Style black		= styles.addStyle("header.black", header);
		StyleConstants.setBold(black,true);

		Style result	= styles.addStyle("header.result", header);
		StyleConstants.setBold(result,true);

		Style line		= styles.addStyle("body.line", body);
		Style line0		= styles.addStyle("body.line.0", line);	//	main line

		StyleConstants.setBold(line0,true);

		Style line1		= styles.addStyle("body.line.1", line);	//	variation
		StyleConstants.setForeground(line1, Color.gray);
		StyleConstants.setFirstLineIndent(line1,10.0f);

		Style line2		= styles.addStyle("body.line.2", line);	//	variation
		StyleConstants.setForeground(line2, Color.gray);
		StyleConstants.setFontSize(line2, 10);

		Style line3		= styles.addStyle("body.line.3", line);	//	variation
		StyleConstants.setForeground(line3, Color.gray);
		StyleConstants.setFontSize(line3, 10);

		Style line4		= styles.addStyle("body.line.4", line);	//	variation
		StyleConstants.setForeground(line4, Color.gray);
		StyleConstants.setFontSize(line4, 10);

		Style comment	= styles.addStyle("body.comment", body);
		StyleConstants.setForeground(comment, new Color(0,0,153));  //  = dark blue
		//StyleConstants.setItalic(comment,true);

		/*Style comment0=*/ styles.addStyle("body.comment.0", comment);
		/*Style comment1=*/ styles.addStyle("body.comment.1", comment);

		Style comment2	= styles.addStyle("body.comment.2", comment);
		StyleConstants.setFontSize(comment2, 10);
		Style comment3	= styles.addStyle("body.comment.3", comment);
		StyleConstants.setFontSize(comment3, 10);
		Style comment4	= styles.addStyle("body.comment.4", comment);
		StyleConstants.setFontSize(comment4, 10);

		Style inline	= styles.addStyle("body.inline", body);	//	inline diagram
		StyleConstants.setFontFamily(inline, getFactoryFigurineFont());

		Style html_large = styles.addStyle("html.large", inline);   //  large HTML diagram
		StyleConstants.setFontSize(html_large,20);

		Style bresult	= styles.addStyle("body.result", body);
		StyleConstants.setBold(bresult,true);

		Style figurine	= styles.addStyle("body.figurine", line);
		StyleConstants.setFontFamily(figurine, getFactoryFigurineFont());

		Style symbol = styles.addStyle("body.symbol", line);
		StyleConstants.setFontFamily(symbol, getFactorySymbolFont());

		Style figurine0	= styles.addStyle("body.figurine.0", figurine);
		StyleConstants.setBold(figurine0,true);

		Style figurine1	= styles.addStyle("body.figurine.1", figurine);
		StyleConstants.setForeground(figurine1, Color.gray);

		Style figurine2	= styles.addStyle("body.figurine.2", figurine);
		StyleConstants.setForeground(figurine2, Color.gray);
		StyleConstants.setFontSize(figurine2, 10);

		Style figurine3	= styles.addStyle("body.figurine.3", figurine);
		StyleConstants.setForeground(figurine3, Color.gray);
		StyleConstants.setFontSize(figurine3, 10);

		Style figurine4	= styles.addStyle("body.figurine.4", figurine);
		StyleConstants.setForeground(figurine4, Color.gray);
		StyleConstants.setFontSize(figurine4, 10);

       	base.addAttribute("figurine.language", Version.getSystemProperty("user.language"));

        setFixedStyles();
    }

	public void createMinimalStyles()
	{
		styles = new JoStyleContext();

		Style base		= styles.addStyle("base",null);
		StyleConstants.setFontFamily(base, "SansSerif");
		StyleConstants.setFontSize(base,14);

		/**	diagram font		 */
//		base.addAttribute("font.diagram",		getFactoryDiagramFont());
		/**	use figurine font ?		 */
		base.addAttribute("figurine.usefont",	Boolean.FALSE);
		/**	move output format		 */
		base.addAttribute("move.format",		MoveFormatter.SHORT);

		Style header	= styles.addStyle("header",base);
		Style body		= styles.addStyle("body",base);

		/*Style event	=*/ styles.addStyle("header.event", header);
		/*Style site	=*/ styles.addStyle("header.site", header);
		/*Style date	=*/ styles.addStyle("header.date", header);
		/*Style round	=*/ styles.addStyle("header.round", header);

		Style white		= styles.addStyle("header.white", header);
		StyleConstants.setBold(white,true);

		Style black		= styles.addStyle("header.black", header);
		StyleConstants.setBold(black,true);

		Style result	= styles.addStyle("header.result", header);
		StyleConstants.setBold(result,true);

		Style line		= styles.addStyle("body.line", body);
		Style line0		= styles.addStyle("body.line.0", line);	//	main line
		StyleConstants.setBold(line0,true);

		Style line1		= styles.addStyle("body.line.1", line);	//	variation
		StyleConstants.setForeground(line1, Color.gray);
		StyleConstants.setFirstLineIndent(line1,10.0f);

		Style line2		= styles.addStyle("body.line.2", line);	//	variation
		StyleConstants.setForeground(line2, Color.gray);
		StyleConstants.setFontSize(line2, 12);

		Style line3		= styles.addStyle("body.line.3", line);	//	variation
		StyleConstants.setForeground(line3, Color.gray);
		StyleConstants.setFontSize(line3, 12);

		Style line4		= styles.addStyle("body.line.4", line);	//	variation
		StyleConstants.setForeground(line4, Color.gray);
		StyleConstants.setFontSize(line4, 12);

		line.addAttribute("move.format", MoveFormatter.ALGEBRAIC);


		Style comment	= styles.addStyle("body.comment", body);
		StyleConstants.setForeground(comment, Color.blue);
		//StyleConstants.setItalic(comment,true);

		/*Style comment0=*/ styles.addStyle("body.comment.0", comment);
		/*Style comment1=*/ styles.addStyle("body.comment.1", comment);

		Style comment2	= styles.addStyle("body.comment.2", comment);
		StyleConstants.setFontSize(comment2, 12);
		Style comment3	= styles.addStyle("body.comment.3", comment);
		StyleConstants.setFontSize(comment3, 12);
		Style comment4	= styles.addStyle("body.comment.4", comment);
		StyleConstants.setFontSize(comment4, 12);

		Style inline	= styles.addStyle("body.inline", body);	//	inline diagram
//		StyleConstants.setFontFamily(inline, getFactoryDiagramFont());

		Style bresult	= styles.addStyle("body.result", body);
		StyleConstants.setBold(bresult,true);

		Style figurine	= styles.addStyle("body.figurine", line);
//		StyleConstants.setFontFamily(figurine, getFactoryDiagramFont());

		Style figurine0	= styles.addStyle("body.figurine.0", figurine);
		StyleConstants.setBold(figurine0,true);

		Style figurine1	= styles.addStyle("body.figurine.1", figurine);
		StyleConstants.setForeground(figurine1, Color.gray);

		Style figurine2	= styles.addStyle("body.figurine.2", figurine);
		StyleConstants.setForeground(figurine2, Color.gray);
		StyleConstants.setFontSize(figurine2, 8);

		Style figurine3	= styles.addStyle("body.figurine.3", figurine);
		StyleConstants.setForeground(figurine3, Color.gray);
		StyleConstants.setFontSize(figurine3, 12);

		Style figurine4	= styles.addStyle("body.figurine.4", figurine);
		StyleConstants.setForeground(figurine4, Color.gray);
		StyleConstants.setFontSize(figurine4, 12);

       	base.addAttribute("figurine.language", "");
	}

}

