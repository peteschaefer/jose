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

import de.jose.util.StringUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Language
		extends de.jose.util.Properties
{
	//-------------------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------------------

	/**	file prefix	 */
	public static final String PROP_FILE = "lang.properties";
	public static final String ECO_PROP_FILE = "eco.properties";

	/**	default language (English)	 */
	public static final String DEFAULT = "en";

    /** default file encoding
     *  note that this is NOT the platform default
     * */
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

	//-------------------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------------------

	/**	the one and only global instance	 */
	public static Language theLanguage;

	/**	current language code	 */
	public String langCode;

	/** fallback langauge (created only on demand)  */
	protected Language fallBackLang;
	/** use fall back ? */
	protected File fallBackFile;

    /** reported errors */
    protected static Set missingKeys;


	//-------------------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------------------

	public Language(File directory, String lang)
		throws IOException
	{
		this(directory,PROP_FILE,lang,true);
	}

	public Language(File directory, String baseName, String lang, boolean fallBack)
		throws IOException
	{
		File fbfile = null;
		if (fallBack && !isDefault(lang))
			fbfile = getFile(directory,baseName,null);

		init(getFile(directory,baseName,lang), lang, fbfile);
	}

	private Language(File file) throws IOException
	{
		init(file,DEFAULT, null);
	}

	public static String getPieceChars(String langCode) {
		String str = Language.get("fig."+langCode,null);
		if (str!=null) str = StringUtil.unescape(str);
		return str;
	}

	private void init(File file, String code, File fallBackFile) throws IOException
	{
		if (file!=null && file.exists()) {
			loadFile(file);
			this.langCode = isDefault(code) ? DEFAULT:code;

			this.fallBackFile = fallBackFile;
			this.fallBackLang = null;   //  will be inited on demand
		}
		else if (fallBackFile!=null && fallBackFile.exists()) {
			loadFile(fallBackFile);
			this.langCode = DEFAULT;

			this.fallBackFile = null;
			this.fallBackLang = null;
		}
		else
			throw new FileNotFoundException(file+" and "+fallBackFile+" are missing");
	}

	private void loadFile(File file) throws IOException
	{
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),DEFAULT_ENCODING));

			//  first line may contain file encoding
			reader.mark(80);

			String firstLine = reader.readLine();
			if (firstLine.startsWith("file.encoding"))
			{
				int k = firstLine.indexOf("=");
				String encoding = firstLine.substring(k+1).trim();
				reader.close();

				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));
			}
			else
				reader.reset(); //  start again

			load(reader);

		} finally {
			if (reader!=null) reader.close();
		}
	}

	//-------------------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------------------

	private static File getFile(File directory, String baseName, String lang)
	{
		if (isDefault(lang))
			return new File(directory, baseName);
		else
			return new File(directory, baseName+"."+lang);
	}

	public static boolean exists(File directory, String baseName, String lang)
	{
		return getFile(directory,baseName,lang).exists();
	}

	/**
	 * set the current language (loading it from file, if necessary)
	 */
	public static Language setLanguage(File directory, String lang)
		throws IOException
	{
		if (theLanguage==null)
			theLanguage = new Language(directory, lang);
		else if (!equals(lang, theLanguage.langCode))
			theLanguage = new Language(directory, lang);
		return theLanguage;
	}

	/**
	 * @return the translated string with the given key
	 */
	public static final String get(String key)
	{
		return get(key,key);
	}

	/**
	 * @return the translated string with the given key
	 */
	public static final String get(String key, String defaultValue)
	{
		if (theLanguage==null)
			return defaultValue;
		else
            return theLanguage.get1(key,defaultValue);
	}

    public String get1(String key, String defaultValue)
    {
        Object value = super.get(key);
        if (value==null) {
	        if (fallBackLang==null && fallBackFile!=null)
		        try {
			        fallBackLang = new Language(fallBackFile);
		        } catch (IOException e) {
			        Application.error(e);
			        fallBackFile = null;    //  don't try again
		        }

	        if (fallBackLang!=null)
				value = fallBackLang.get1(key,null);
        }
        if (value!=null)
            return (String)value;
        else {
//            warnMissing(key, langCode);
            return defaultValue;
        }
    }

	/**
	 * @return the translated string with the given key
	 */
	public static final String getTip(String key)
	{
		String result = theLanguage.get1(key+".tip",null);
		if (result == null) {
//            theLanguage.missing(key+".tip");
            result = theLanguage.get1(key, null);
        }
		return result;
	}

	public static String args(String key, HashMap args) {
		if (key==null) return "";
		String text = Language.get(key);
		if (args!=null)
			text = StringUtil.replace(text,args);
		return text;
	}

	public static String argsTip(String key, HashMap args) {
		if (key==null) return "";
		String text = Language.getTip(key);
		if (args!=null)
			text = StringUtil.replace(text,args);
		return text;
	}

    /**
     * @return a mnemonic character, or 0 if not found
     */
    public static final char getMnemonic(String key)
    {
        String s = get(key,null);
        if (s==null || s.length() < 1)
            return (char)0;
        else
            return s.charAt(1);
    }


    /**
     * @return the Key Code for a mnemonic, or 0 if not found
     */
    public static final int getMnemonicCharIndex(String key)
    {
        String text = get(key).toLowerCase();
        char mn = Character.toLowerCase(getMnemonic(key));
        return text.indexOf(mn);
    }

	public static final Vector getList(String key)
	{
		String s = get(key);
		if (s==null) return null;

		return StringUtil.separate(new StringBuffer(s), ',');
	}

	public static Vector getAvailableLanguages(File dir)
	{
		Vector result = new Vector();
		String[] files = dir.list();
		for (int i=0; i<files.length; i++)
			if (files[i].startsWith(PROP_FILE)) {
				String langCode = null;
				if (files[i].length() > PROP_FILE.length())
					langCode = files[i].substring(PROP_FILE.length()+1);
				if (langCode==null || langCode.length()==0)
					langCode = DEFAULT;
				result.add(langCode);
			}
		return result;
	}

	public static Vector getAvailableEcoLanguages(File dir)
	{
		Vector result = new Vector();
		String[] files = dir.list();
		for (int i=0; i<files.length; i++)
			if (files[i].startsWith(ECO_PROP_FILE)) {
				String langCode = null;
				if (files[i].length() > ECO_PROP_FILE.length())
					langCode = files[i].substring(ECO_PROP_FILE.length()+1);
				if (langCode==null || langCode.length()==0)
					langCode = DEFAULT;
				result.add(langCode);
			}
		return result;
	}


	public static void update(AbstractButton button, String defaultTitle)
	{
		if (button != null && button.getName() != null) {
			String text = Language.get(button.getName(),defaultTitle);
			if (button.getText() != null)
				button.setText(text);

			String tooltip = Language.getTip(button.getName());
			if (tooltip!=null)
				button.setToolTipText(tooltip);
		}
	}

	public static void update(JLabel label, String defaultTitle)
	{
		if (label != null && label.getName() != null) {
			String text = Language.get(label.getName(),defaultTitle);
			String tooltip = Language.getTip(label.getName());
			label.setText(text);
			label.setToolTipText(tooltip);
		}
	}

	public static void update(JTabbedPane pane)
	{
		if (pane != null) {
			for (int i=0; i<pane.getTabCount(); i++) {
				String name = pane.getComponent(i).getName();
				if (name==null) continue;

				String text = Language.get(name);
				String tooltip = Language.getTip(name);

				pane.setTitleAt(i,text);
				pane.setToolTipTextAt(i,tooltip);

                update(pane.getComponentAt(i));
			}
		}
	}

	public static void update(Component comp)
	{
		update(comp,null);
	}

	public static void update(Component comp, String defaultTitle)
	{
		if (defaultTitle==null && comp!=null) defaultTitle = comp.getName();

		if (comp instanceof JLabel)
			update((JLabel)comp, defaultTitle);
		else if (comp instanceof AbstractButton)
			update((AbstractButton)comp, defaultTitle);
		else if (comp instanceof JTabbedPane)
			update((JTabbedPane)comp);

		if (comp instanceof Container) {
			Container cont = (Container)comp;
			for (int i=0; i<cont.getComponentCount(); i++)
				update(cont.getComponent(i));
		}
		if (comp instanceof JComponent && comp.getName()!=null) {
			Border border = ((JComponent)comp).getBorder();
			if (border!=null && border instanceof TitledBorder)
				((TitledBorder)border).setTitle(Language.get(comp.getName(),defaultTitle));
		}
	}

	public static void update(JMenuBar menubar)
	{
		if (menubar==null) return;
		for (int j=0; j < menubar.getMenuCount(); j++)
			update(menubar.getMenu(j));
	}

	public static void update(JMenu menu)
	{
		update((AbstractButton)menu);

		for (int i=0; i < menu.getItemCount(); i++)
			update(menu.getItem(i));
	}

	//-------------------------------------------------------------------------------------------
	//	Private Methods
	//-------------------------------------------------------------------------------------------

	private static boolean isDefault(String code)
	{
		return code==null || code.equals(DEFAULT);
	}

	private static boolean equals(String a, String b)
	{
		if (isDefault(a))
			return isDefault(b);
		if (isDefault(b))
			return isDefault(a);
		return a.equals(b);
	}

    protected void warnMissing(String key, String lang)
    {
		if (missingKeys==null)
			missingKeys = new HashSet();
		if (lang!=null)
			key = lang+", "+key;
		if (missingKeys.contains(key)) return;   //  already reported
		Application.warning("missing translation: "+key);
		missingKeys.add(key);
    }
}
