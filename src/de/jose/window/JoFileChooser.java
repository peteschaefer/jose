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

package de.jose.window;

import de.jose.Application;
import de.jose.Language;
import de.jose.Version;
import de.jose.Util;
import de.jose.book.OpeningBook;
import de.jose.book.OpeningBookFilter;
import de.jose.book.polyglot.PolyglotBook;
import de.jose.book.crafty.CraftyBook;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.pgn.PGNFileFilter;
import de.jose.task.io.ArchiveExport;
import de.jose.util.ListUtil;
import de.jose.util.StringUtil;
import de.jose.util.file.ExecutableFileFilter;
import de.jose.util.file.ExtensionFileFilter;
import de.jose.util.map.IntHashMap;
import de.jose.util.map.ObjIntMap;

import javax.swing.*;
import javax.swing.plaf.FileChooserUI;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JoFileChooser
        extends JFileChooser
{
    /** Filter Types    */
    /** All Files   */
    public static final int ALL             = 0;

    /** PGN */
    public static final int PGN             = 1;
    /** HTML    */
    public static final int HTML            = 2;
    /** PDF */
    public static final int PDF             = 3;
    /** Plain text  */
    public static final int TEXT            = 4;
    /** raw XML */
    public static final int XML             = 5;
	/** RTF */
	public static final int RTF             = 6;
	/** EPD/FEN */
	public static final int EPD             = 7;
	/** executables */
	public static final int EXE             = 8;
	/** jose archive  */
	public static final int ARCH             = 100;
	/** opening book file  */
	public static final int BOOK            = 200;
	/** Crafty opening book file  */
	public static final int CRAFTY_BOOK            = 201;
	public static final int POLYGLOT_BOOK            = 202;
//	public static final int SHREDDER_BOOK            = 203;


    private static IntHashMap INT_FILTER_MAP = new IntHashMap();
    private static ObjIntMap FILTER_INT_MAP = new ObjIntMap();



    public static final int[]   SAVE_FILTERS = new int[] {
        PGN,					//	PGN text
		ARCH, 		//	Jose MySQL dump
//		HTML,					//	HTML (not yet supported)
//		PDF,					//	PDF (not yet supported)
//		RTF,					//	RTF (not yet supported)
//		TEXT, 					//	plain text
		XML,					//	XML (for processing)
    };

    public static final int[]   OPEN_FILTERS = new int[] {
        PGN,
        EPD,
		ARCH,
        ALL,
    };

	public static final int[]   BOOK_FILTERS = new int[] {
		BOOK,
		POLYGLOT_BOOK,
		CRAFTY_BOOK,
//		SHREDDER_BOOK,
	};

	public static final int[]   EXECUTABLE_FILTERS = new int[] {
		EXE,
	};

    static {
        FileFilter filter = PGNFileFilter.newPGNFilter();
        INT_FILTER_MAP.put(PGN, filter);
        FILTER_INT_MAP.put(filter, PGN);

	    filter = PGNFileFilter.newEPDFilter();
	    INT_FILTER_MAP.put(EPD, filter);
	    FILTER_INT_MAP.put(filter, EPD);

        filter = new ExtensionFileFilter("html","htm");
        INT_FILTER_MAP.put(HTML, filter);
        FILTER_INT_MAP.put(filter, HTML);

        filter = new ExtensionFileFilter("txt");
        INT_FILTER_MAP.put(TEXT, filter);
        FILTER_INT_MAP.put(filter, TEXT);

        filter = new ExtensionFileFilter("pdf");
        INT_FILTER_MAP.put(PDF, filter);
        FILTER_INT_MAP.put(filter, PDF);

        filter = new ExtensionFileFilter("xml");
        INT_FILTER_MAP.put(XML, filter);
        FILTER_INT_MAP.put(filter, XML);

	    filter = new ExecutableFileFilter();
	    INT_FILTER_MAP.put(EXE, filter);
	    FILTER_INT_MAP.put(filter, EXE);

        filter = new ExtensionFileFilter("jos","jose");
        INT_FILTER_MAP.put(ARCH, filter);
        FILTER_INT_MAP.put(filter, ARCH);

	    filter = new OpeningBookFilter(null);
	    INT_FILTER_MAP.put(BOOK, filter);
	    FILTER_INT_MAP.put(filter, BOOK);

	    filter = new OpeningBookFilter(new CraftyBook());
	    INT_FILTER_MAP.put(CRAFTY_BOOK, filter);
	    FILTER_INT_MAP.put(filter, CRAFTY_BOOK);

	    filter = new OpeningBookFilter(new PolyglotBook());
	    INT_FILTER_MAP.put(POLYGLOT_BOOK, filter);
	    FILTER_INT_MAP.put(filter, POLYGLOT_BOOK);

//	    filter = new OpeningBookFilter(new ShredderBook());
//	    INT_FILTER_MAP.put(SHREDDER_BOOK, filter);
//	    FILTER_INT_MAP.put(filter, SHREDDER_BOOK);
    };


    public JoFileChooser(File directory, boolean showShellFolders)
    {
        super(directory);
        if (Version.windows)
        {
            //  todo use WindowsFileChooserUI, even with custom LnFs
/*
            FileChooserUI ui = getUI();
            if (! (ui instanceof com.sun.java.swing.plaf.windows.WindowsFileChooserUI))
            {
                ui = new com.sun.java.swing.plaf.windows.WindowsFileChooserUI(this);
                setFileView(ui.getFileView(this));  //  use correct icons
                setUI(ui);
            }
 */
        }
        showShellFolders(showShellFolders);
    }

    public void showShellFolders(boolean showShellFolders)
    {
        //  hint to WindowFileChooserUI
        putClientProperty("FileChooser.useShellFolder", Util.toBoolean(showShellFolders));
    }

    public void setPreferredDir(File[] dirs)
	{
		File dir = getPreferredDir(dirs);
		setCurrentDirectory(dir);
	}

    private static File getPreferredDir(File[] dirs)
    {
        for (int i=0; i < dirs.length; i++)
            if (dirs[i] != null && dirs[i].exists())
                return dirs[i];
        return Application.theWorkingDirectory;   //  last resort
    }

    public static int getPreferredFilter(int[] filters)
    {
        for (int i=0; i < filters.length; i++)
            if (filters[i] != 0)
                return filters[i];
        return TEXT;    //  last resort
    }

	public void addChoosableFileFilters(Object filters)
	{
		Iterator i = ListUtil.iterator(filters);
		while (i.hasNext())
			addChoosableFileFilter((FileFilter)i.next());
	}

	public void removeChoosableFileFilters(Object filters)
	{
		Iterator i = ListUtil.iterator(filters);
		while (i.hasNext())
			removeChoosableFileFilter((FileFilter)i.next());
	}

	public void setChoosableFileFilters(Object newFilters)
	{
		FileFilter[] oldFilters = getChoosableFileFilters();
		removeChoosableFileFilters(oldFilters);
		addChoosableFileFilters(newFilters);
	}

    /**
     * static ctor
     */
    public static JoFileChooser forOpen(File[] preferredDirs, int[] preferredFilters)
    {
        JoFileChooser chooser = new JoFileChooser(getPreferredDir(preferredDirs), true);
        chooser.setFilters(OPEN_FILTERS, getPreferredFilter(preferredFilters));

        return chooser;
    }

	/**
	 * static ctor
	 */
	public static JoFileChooser forOpenBook(File[] preferredDirs, int[] preferredFilters)
	{
	    JoFileChooser chooser = new JoFileChooser(getPreferredDir(preferredDirs), true);
		chooser.setMultiSelectionEnabled(true);
	    chooser.setFilters(BOOK_FILTERS, getPreferredFilter(preferredFilters));

	    return chooser;
	}

    public static JoFileChooser forSave(File[] preferredDirs, int[] preferredFilters,
										String preferredName)
    {
        JoFileChooser chooser = new JoFileChooser(getPreferredDir(preferredDirs), true);
		if (preferredFilters!=null)
        	chooser.setFilters(SAVE_FILTERS, getPreferredFilter(preferredFilters));
		if (preferredName != null)
			chooser.setFileName(preferredName);
        return chooser;
    }

	public static JoFileChooser forExecutable(File[] preferredDirs)
	{
		JoFileChooser chooser = new JoFileChooser(getPreferredDir(preferredDirs), true);
		chooser.setFilters(JoFileChooser.EXECUTABLE_FILTERS,JoFileChooser.EXE);
		return chooser;
	}

	public void setFileName(String fileName)
	{
		File file = new File(getCurrentDirectory(),fileName);
		setSelectedFile(file);
	}

    public static boolean confirmOverwrite(File file)
    {
        String message = Language.get("filechooser.overwrite");
        Map params = new HashMap();
        params.put("file.name", file.getName());
        params.put("file.absolute", file.getAbsolutePath());
        params.put("file.path", file.getPath());

        message = StringUtil.replace(message,params);

        int result = JoDialog.showYesNoDialog(message,"confirm",
                "filechooser.do.overwrite","dialog.button.cancel",
                JOptionPane.NO_OPTION);

        return (result == JOptionPane.YES_OPTION);
    }


	public static FileFilter getFilter(int key)
	{
		return (FileFilter)INT_FILTER_MAP.get(key);
	}

    public void setFilters(int[] types, int defaultType)
    {
        setAcceptAllFileFilterUsed(false);  //  don't accept "All Files"
        FileFilter defaultFilter = null;

        for (int i=0; i<types.length; i++)
            if (types[i]==ALL)
                setAcceptAllFileFilterUsed(true);
	        else if (types[i] <= 0)
                continue;
            else {
                FileFilter filter = getFilter(types[i]);
                if (filter==null) throw new IllegalArgumentException("unknown filter type "+types[i]);

				if (!isAvailable(types[i])) continue;

                addChoosableFileFilter(filter);
                if (types[i]==defaultType)
                    defaultFilter = filter;
            }
        if (defaultFilter != null)
            setFileFilter (defaultFilter);
    }


	public boolean isAvailable(int type)
	{
		switch (type) {
		case ARCH:
				DBAdapter adapter = JoConnection.getAdapter();
				return ArchiveExport.isAvailable(adapter);
		default:
				return true;
		}
	}

    public int getCurrentFilter()
    {
        FileFilter filter = getFileFilter();
        if (filter==null) return ALL;

        int type = FILTER_INT_MAP.getInt(filter);
        if (type==ObjIntMap.NOT_FOUND)
            return 0;
        return type;
    }


    public static String getFileExtension(int type)
    {
        switch (type) {
        case PGN:       return "pgn";
        case HTML:      return "html";
        case PDF:       return "pdf";
        case TEXT:      return "txt";
        case XML:       return "xml";
        case ARCH:      return "jose";
        }
        return null;
    }

}
