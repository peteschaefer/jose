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

package de.jose.view.input;

import com.l2fprod.gui.plaf.skin.SkinLookAndFeel;
import de.jose.Application;
import de.jose.Version;
import de.jose.util.ClassPathUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import ch.randelshofer.quaqua.QuaquaManager;

/**
 * a dropdown list with look and feels
 *
 * @author Peter Schäfer
 */

public class LookAndFeelList
		extends JComboBox
        implements ValueHolder
{
	public static LookAndFeelEntry[] getDefinedLookAndFeels()
	{
		File plafDir = new File(Application.theWorkingDirectory,"lib/plaf");
		Vector collect = new Vector();
		Enumeration en = Application.theApplication.theConfig.enumerateElements("look-and-feel");
		while (en.hasMoreElements()) {
			Element elem = (Element)en.nextElement();
			String name = XMLUtil.getChildValue(elem,"name");
			String className = XMLUtil.getChildValue(elem,"class");
			String classPath = XMLUtil.getChildValue(elem,"class-path");
			String theme = XMLUtil.getChildValue(elem,"theme");

			if (ClassPathUtil.existsClass(className) ||
				(classPath!=null) && FileUtil.exists(plafDir,classPath)) {
				LookAndFeelEntry ety = new LookAndFeelEntry(name,className,theme);
				collect.add(ety);
			}
		}

		LookAndFeelEntry[] result = new LookAndFeelEntry[collect.size()];
		collect.toArray(result);
		return result;
	}


	public static String loadLookAndFeel(String lookAndFeel)
		throws Exception
	{
		String className = null;
		String themepack = null;

		if (lookAndFeel==null) lookAndFeel = getDefaultClassName();

		int k = lookAndFeel.lastIndexOf("+");
		if (k > 0) {
			className = lookAndFeel.substring(0,k);
			themepack = lookAndFeel.substring(k+1);
		}
		else
			className = lookAndFeel;

		if (!ClassPathUtil.existsClass(className) && !loadClass(className))
			return null;

		if (className.equals("com.l2fprod.gui.plaf.skin.SkinLookAndFeel"))
		{
			//	special care for Skin L&F
			if (themepack != null) {
				themepack = Application.theWorkingDirectory.getAbsolutePath()+
						File.separator+"lib"+File.separator+"plaf"+File.separator+themepack;
				if (FileUtil.exists(themepack)) {
					SkinLookAndFeel.setSkin(SkinLookAndFeel.loadThemePack(themepack));
					SkinLookAndFeel.enable();
				}
			}
		}
        if (className.equals("ch.randelshofer.quaqua.QuaquaManager"))
        {
            //  instantiate actual look and feel from QuaquaManager
            if (themepack!=null)
                System.setProperty("Quaqua.design", themepack);
            className = QuaquaManager.getLookAndFeelClassName();

        }
		if (className.equals("javax.swing.plaf.metal.MetalLookAndFeel"))
		{
			Version.setSystemProperty("swing.metalTheme",themepack);    //  steel or ocean, 1.5
			//  TODO this has no effect when applied in program
			//  restart is necessary ;-(
		}
		return className;
	}

	protected static boolean loadClass(String className)
	{
		Enumeration en = Application.theApplication.theConfig.enumerateElements("look-and-feel");
		while (en.hasMoreElements()) {
			Element elem = (Element)en.nextElement();
			String otherClassName = XMLUtil.getChildValue(elem,"class");

			if (className.equals(otherClassName))
				try {
					//	add jar file to class path !
					String classpath = XMLUtil.getChildValue(elem,"class-path");
                    if (classpath!=null) {
						File plafDir = new File(Application.theWorkingDirectory,"lib/plaf");
                        ClassPathUtil.addAllToClassPath(plafDir,classpath);
                    }
					Class.forName(className);
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
		}
		return false;
	}

	public LookAndFeelList()
	{
		super (getInstalledLookAndFeels());
		setEditable(false);
	}

	public void setSelectedClassName(String className)
	{
		if (className==null) className = getDefaultClassName();

		String themepack = null;
		int k = className.lastIndexOf("+");
		if (k > 0) {
			themepack = className.substring(k+1);
			className = className.substring(0,k);
		}
		ListModel lm = getModel();
		for (int i=0; i<lm.getSize(); i++) {
			LookAndFeelEntry ety = (LookAndFeelEntry)lm.getElementAt(i);
			if (ety.info.getClassName().equals(className)) {
				if (themepack==null || themepack.equals(ety.themepack)) {
					setSelectedIndex(i);
					return;
				}
			}
		}
	}

	public String getSelectedClassName()
	{
		LookAndFeelEntry ety = (LookAndFeelEntry)getSelectedItem();
		if (ety != null) {
			if (ety.themepack != null)
				return ety.info.getClassName()+"+"+ety.themepack;
			else
				return ety.info.getClassName();
		}
		else
			return null;
	}

	//  implements ValueHolder
	public Object getValue()                { return getSelectedClassName(); }

	public void setValue(Object value)      { setSelectedClassName((String)value); }

	public static String getThemepack(String lnf)
	{
        LookAndFeelEntry[] factory = getDefinedLookAndFeels();
		for (int i=0; i < factory.length; i++)
			if (factory[i].info.getName().equals(lnf))
				return factory[i].themepack;
		return null;
	}

	//-------------------------------------------------------------------------------
	//	Private Parts
	//-------------------------------------------------------------------------------

	private static class LookAndFeelEntry
	{
		UIManager.LookAndFeelInfo info;
		//  display Name
		String displayName;
		//	additional themepack for skin L&F and Metal
		String themepack;

		LookAndFeelEntry (UIManager.LookAndFeelInfo i) {
			this(i,null,null);
		}

		LookAndFeelEntry (UIManager.LookAndFeelInfo i, String pack, String name) {
			info = i;
			themepack = pack;
			if (name!=null)
				displayName = name;
			else
				displayName = i.getName();
		}

		LookAndFeelEntry (String name, String className, String pack) {
			this(new UIManager.LookAndFeelInfo(name,className),pack,null);
		}

		public String toString()	{ return displayName; }

		public boolean equals(Object obj)
		{
			if (obj.getClass() != LookAndFeelEntry.class) return false;

			LookAndFeelEntry ety = (LookAndFeelEntry)obj;
			if (! ety.info.getClassName().equals(info.getClassName())) return false;

			if (ety.themepack==null)
				return themepack==null;
			else
				return ety.themepack.equals(themepack);
		}
	}

    public static String getDefaultClassName()
    {
		return UIManager.getSystemLookAndFeelClassName();
    }

	private static Vector getInstalledLookAndFeels()
	{
		//	get the look & feels that are installed with the JDK
		UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
		Vector result = new Vector();
		for (int i=0; i<info.length; i++) {
			if (Version.java15orLater &&
			        info[i].getClassName().equals("javax.swing.plaf.metal.MetalLookAndFeel"))
			{
				result.add(new LookAndFeelEntry(info[i],"ocean", "Ocean"));
				result.add(new LookAndFeelEntry(info[i],"steel", null));
			}
			else
				result.add(new LookAndFeelEntry(info[i]));
			//  TODO what about Synth (simce JDK 1.5)
		}

		//	add our own look & feels, as defined in the Config
        LookAndFeelEntry[] factory = getDefinedLookAndFeels();
		for (int i=0; i < factory.length; i++)
			if (!result.contains(factory[i]))
				result.add(factory[i]);
		return result;
	}



}
