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

package de.jose.util;

import de.jose.Version;
import de.jose.Util;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Native Utilities for Windows
 *
 *	setProcessPriority()	controls the priority of native processes
 *							which is useful for controlling CPU hogs
 *
 *	setTopMost(Window)		puts a window in front of the screen (and it stays there)
 *
 * NOTE requires Java 1.5 to compile
 *
 * @author Peter Sch�fer
 */
public class WinUtils
{
	//------------------------------------------------------------------
	//	Thread utils
	//------------------------------------------------------------------

	public static final int CSIDL_WINDOWS   =   0x0024;
	public static final int CSIDL_FONTS     =   0x0014;

	//------------------------------------------------------------------
	//	Window utils
	//------------------------------------------------------------------

	/**
	 * put a window in front of all other windows
	 * the window will remain there
	 */
	public static boolean setTopMost(Frame win)
	{
		win.setAlwaysOnTop(true);
		return true;    //  no way to detect success !?
	}

	public static boolean setTopMost(Dialog dlg)
	{
            dlg.setAlwaysOnTop(true);
            return true;
	}

	//------------------------------------------------------------------
	//	Windows Paths
	//------------------------------------------------------------------

	/**
	 * get the path to the Windows directory (usually C:\Windows)
	 * @return
	 */
	public static String getWindowsPath()
	{
		loadLib();
		return getSystemDirectory(CSIDL_WINDOWS);
	}

	/**
	 * get the path to the Windows Font directory (usually C:\Windows\Fonts)
	 * @return
	 */
	public static String getFontPath()
	{
		loadLib();
		return getSystemDirectory(CSIDL_FONTS);
	}


	//------------------------------------------------------------------
	//	Shared Memory Utils
	//------------------------------------------------------------------

	//  TODO not implemented
	public static int openFileMapping(String fileName)
	{
		loadLib();
		return OpenFileMapping(fileName);
	}

	//  TODO not implemented
	public static native java.nio.ByteBuffer getFileMappingBuffer(int mapHandle);

	//  TODO not implemented
	public static void closeFileMapping(int mapHandle)
	{
		UnmapViewOfFile(mapHandle);
	}

	//------------------------------------------------------------------
	//	Locking Utils
	//------------------------------------------------------------------

	//  TODO not implemented
	public static native int createEvent();

	//  TODO not implemented
	public static native boolean waitFor(int eventHandle, long timeout);

	//  TODO not implemented
	public static native void setEvent(int eventHandle);

    //------------------------------------------------------------------
    //	File Type associations
    //------------------------------------------------------------------


	/**
	 * register jose in the Windows registry
	 * @param progId program Id (usually "jose.exe")
	 * @param exePath path to executable
	 */
    public static void registerApplication(String progId, String exePath)
    {
        if (Version.windows)
        {
            String root = "HKEY_CURRENT_USER/Software/Classes/"+progId;

            WinRegistry.createKey(root);
            WinRegistry.setValue(root,null,"jose Chess Database");
            WinRegistry.setValue(root+"/DefaultIcon", null, exePath+",0");
            WinRegistry.setValue(root+"/Shell", null, "Open");
            WinRegistry.setValue(root+"/Shell/Open/command", null, "\""+exePath+"\" \"%1\" ");
        }
    }

	/**
	 * register a file type (extension) with jose
	 *
	 * @param extension
	 * @param progId
	 * @return the previously associated application
	 */
    public static Object associateFileExtension(String extension, String progId)
    {
        if (Version.windows)
        try {
            String root = "HKEY_CURRENT_USER/Software/Classes/."+extension;

            WinRegistry.createKey(root);

            Object oldAssociation = WinRegistry.getValue(root,null);
            WinRegistry.setValue(root,null,progId);     //  default association
            WinRegistry.createKey(root+"/OpenWithProgids/"+progId);

            root = "HKEY_CURRENT_USER/Software/Microsoft/Windows/CurrentVersion/Explorer/FileExts/."+extension;
            WinRegistry.setValue(root,null,progId);
            WinRegistry.setValue(root,"Application",progId);

	        if (WinRegistry.existsKey(root+"/OpenWithList"))
	        {
				Map values = WinRegistry.getAllValues(root+"/OpenWithList");
				/**
				 * values contains mappings from alphabet characters to applications,
				 * plus a mapping "MRUList"
				 *
				 * maybe, this progId is alread present
				 */
	        }
	        else
	        {

	        }

	        //String mru = (String)WinRegistry.getValue(root+"/OpenWithList","MRUList");
	        //  TODO prone to fail.

            return oldAssociation;
	        
        } catch (Throwable error) {
	        return null;
        }
        /*
            if (Version.mac)
            {
                File Type Associations are defined in Contents/Info.plist
            }
        */
        //  else
        return null;
    }

    public static void removeFileExtension(String extension, String progId, String oldAssociation)
    {
        if (Version.windows)
        {
            String root = "HKEY_CURRENT_USER/Software/Classes/+."+extension;    //  depends...

            if (!WinRegistry.existsKey(root)) return;

            WinRegistry.setValue(root,null,oldAssociation); //  may be null
            WinRegistry.deleteKey(root+"/OpenWithProgids/"+progId);
        }
    }

	//------------------------------------------------------------------
	//	private parts
	//------------------------------------------------------------------


	private static boolean libLoaded = false;

	private static native int getPriorityClass(long procHandle);

	private static native boolean setPriorityClass(long procHandle, int priority);

	private static native boolean setTopMost(long winHandle);

	private static native int findWindow(String name);

	private static native String getWindowsDirectory();

	private static native String getSystemDirectory(int csidl);


	//  TODO not implemented
	private static native int OpenFileMapping(String mapFileName);

	//  TODO not implemented
	private static native void UnmapViewOfFile(int mapHandle);


	protected static void loadLib()
	{
		if (!libLoaded) {
			try {
				System.loadLibrary("winUtils");
				libLoaded = true;
				return;
			} catch (UnsatisfiedLinkError e) { }
		}
	}


	private static long getProcessHandle(Process proc)
	{
// jdk 9 has:		return proc.pid();
		Number handle = null;
		try {
			Class clazz = Class.forName("java.lang.ProcessImpl");
			Field field = clazz.getDeclaredField("handle");
			field.setAccessible(true);
			handle = (Number)field.get(proc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
		return handle.longValue();
	}

}
