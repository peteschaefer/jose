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

package com.mysql.embedded.jdbc;


import com.mysql.embedded.api.api;
import com.mysql.embedded.util.Files;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Peter Sch�fer
 */

public class MySqlEmbeddedDriver
        implements java.sql.Driver
{
	/**
	 * two options for setting BLOB paraemeters in prepared statements
	 * * bind buffer
	 * * explicitly call send_long_data (allowing to send blob chunk-wise)
	 */
	public static final boolean SEND_LONG_DATA =
	        "true".equalsIgnoreCase(System.getProperty("mysql-je.send-long-data"));

	/**
	 * number of live connections
	 * the server is initialized with the first connection,
	 * and shut down when the last connection closes
	 */
	protected static int liveConnections = 0;
	/**
	 * handle to server instance
	 * (a MYSQL* in C)
	 */
	public static long databaseHandle = 0;
	/**
	 * indicates if native libraries are loaded
	 */
	private static Object init_lock = new Object();
	/**
	 * shut down server when the last connection is closed ?
	 * otherwise, MySqlEmbeddedDriver.shutdown() must be called explicitly
	 */
	private static boolean auto_shutdown = true;

    public static final String VERSION_NAME = "MySQL Embedded JDBC";
	public static final int MAJOR_VERSION   = 1;
	public static final int MINOR_VERSION   = 3;

	public static final String OS_NAME = System.getProperty("os.name");
	protected static boolean windows = OS_NAME.startsWith("Windows");
	protected static boolean unix = !windows;
    protected static boolean macosx = OS_NAME.startsWith("Mac OS X");

	static {
		try {
			DriverManager.registerDriver(new MySqlEmbeddedDriver());
		} catch (SQLException e) {

		}
	}

	public MySqlEmbeddedDriver() throws SQLException
	{
		DriverManager.registerDriver(this);
	}


	public java.sql.Connection connect(String url, Properties info) throws SQLException
	{
		if (! acceptsURL(url)) return null;

        parseUrlArgs(url, info);

        if (databaseHandle==0)
            synchronized (init_lock)
            {   //  very first call should be synchronized; subsequent calls need not
                if (databaseHandle==0)
                {
                    loadLibraries(info);

	                String shutdown = (String)info.get("shutdown");
	                if ("auto".equalsIgnoreCase(shutdown))
	                    setAutoShutdown(true);
					if ("false".equalsIgnoreCase(shutdown))
						setAutoShutdown(false);
	                //  default: auto_shutdown = true

	                String unzip = (String)info.get("unzip");
	                if (unzip!=null) {
		                File zipf = new File(unzip);
		                if (! zipf.exists()) throw new SQLException(unzip+" not found");

		                String data = (String) info.get("--datadir");
		                if (data==null) {
			                String base = (String) info.get("--basedir");
			                if (base==null) throw new SQLException("--datadir or --basedir expected");
			                data = base+File.separator+"data";
		                }

		                try {
			                File datadir = new File(data);
			                //  unzip
			                restoreArchive(zipf, datadir);
		                }
		                catch (IOException e) {
			                throw new SQLException("during unzip: "+e.getMessage());
		                }
	                }

                    databaseHandle = init_server(url,info);

	                Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                }
            }

        liveConnections++;
        Connection conn = new MyConnection(info);
        return conn;
    }

	private void restoreArchive(File zip, File datadir) throws IOException
	{
		if (datadir.isDirectory())
			Files.deleteContent(datadir);
		else if (datadir.exists()) {
			datadir.delete();
			datadir.mkdirs();
		}
		else
			datadir.mkdirs();

		Files.unzip(zip, datadir);
	}

	public boolean acceptsURL(String url) throws SQLException
	{
		return url.startsWith("jdbc:mysql-embedded");
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException
	{
		DriverPropertyInfo[] dinfo = new DriverPropertyInfo[6];

		dinfo[0] = new DriverPropertyInfo("user", info.getProperty("user"));
		dinfo[0].description = "Account name for connection. Not needed when connecting with --skip-grant-tables";

		dinfo[1] = new DriverPropertyInfo("password", info.getProperty("password"));
		dinfo[1].description = "Account password for connection. Not needed when connecting with --skip-grant-tables";

		dinfo[2] = new DriverPropertyInfo("libmysqlje", info.getProperty("libmysqlje"));
		dinfo[2].description = "Path to native library libmysqlje. Alternative, the path may be specified with java.library.path";

		dinfo[3] = new DriverPropertyInfo("libmysqld", info.getProperty("libmysqld"));
		dinfo[3].description = "Path to native library libmysqld. Alternative, the path may be specified with java.library.path";

		dinfo[4] = new DriverPropertyInfo("shutdown", info.getProperty("shutdown"));
		dinfo[4].description = "shutdown=auto: shutdown with last connection\nshutdown=false: explicit SHUTDOWN required";

		dinfo[5] = new DriverPropertyInfo("MySQL  Property", "...");
		dinfo[5].description = "You can pass most command line properties for MySQL, e.g." +
		                        "--basedir, --datadir. See MySQL manual for details";

		return dinfo;
	}

	public int getMajorVersion()
	{
		return MAJOR_VERSION;
	}

	public int getMinorVersion()
	{
		return MINOR_VERSION;
	}

	public boolean jdbcCompliant()
	{
		return true;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}


	/**
	 * load the native libraries
	 */
	public static void loadLibraries(Properties info)
		throws UnsatisfiedLinkError
	{
        /**
         * On Windows, we need two libraries: libmysqld.dll and libmysqljni.dll
         * on Unix, libmysqld is linked with libmysqlje.so, so we need to load
         * only one file
         *
         * Locations can be passed as properties. By default, we load them from
         * java.library.path
         */
		Object userPath = info.get("library.path");
		Object userLibs = info.get("library");

		if (userLibs!=null)
        {
		    //  additional libraries, explicitly passed
		    if (userLibs instanceof File)
			    loadLibrary((File)userLibs, userPath, "","");
		    else {
			    StringTokenizer tok = new StringTokenizer(userLibs.toString(),File.pathSeparator,false);
			    while (tok.hasMoreTokens()) {
			        String fileName = tok.nextToken();
			        loadLibrary(fileName, userPath, fileName, "");
			    }
		    }
		}

        if (windows)
        {
            //  the embedded mysql server library
	        loadLibrary(info.get("libmysqld"), userPath, "libmysqld.dll", "libmysqld");
            //  our JNI wrapper
	        loadLibrary(info.get("libmysqlje"), userPath, "libmysqlje.dll", "libmysqlje");
        }
        else if (macosx)
        {
            //  on Mac OS X, we have just one static library
            loadLibrary(info.get("libmysqlje"), userPath, "libmysqlje.jnilib", "mysqlje");
        } else if (unix)
        {
	        //  on Linux, we have just one shared library
	        loadLibrary(info.get("libmysqlje"), userPath, "libmysqlje.so", "mysqlje");
        }
	}

	protected static boolean loadLibrary(Object userName, Object userPath,
	                                     String fileName, String libName)
	{
		if (userName!=null) {
			//  file path explicitly set ?
			File f;
			if (userName instanceof File)
				f = (File)userName;
			else
				f = new File(userName.toString());
			if (f.exists()) {
				System.load(f.getAbsolutePath());
				return true;
			}
		}

		if (userPath!=null) {
			//  directory set ?
			if (userPath instanceof File) {
				File f = new File((File)userPath,fileName);
				if (f.exists()) {
					System.load(f.getAbsolutePath());
					return true;
				}
			}
			else {
				StringTokenizer tok = new StringTokenizer(userPath.toString(),File.pathSeparator,false);
				while (tok.hasMoreTokens()) {
					File f = new File(tok.nextToken(),fileName);
					if (f.exists()) {
						System.load(f.getAbsolutePath());
						return true;
					}
				}
			}
		}
		//  use system default
		System.loadLibrary(libName);
		return true;
	}

	private static long init_server(String url, Properties info)
		throws SQLException
	{
		//  the following groups are read from option files
		//  use --defaults-file=... to use s specific option file
		String[] groups = {
			"mysql-je", "embedded", "mysqld", "mysqld-5.0"
		};

        //  assemble "command line" args
        String[] argv = createArgs(info);
        //  group settings (from config file) not yet implemented
		if (argv==null) argv = new String[0];

		//  initialize server
		api.mysql_server_init(argv,groups);

		long result = api.mysql_init(0);
		if (result==0)
			throw new SQLException("could not allocate connection");

		return result;
	}

	private static void parseUrlArgs(String url, Properties info)
	{
		//  args passed through url ?
		int k0 = url.indexOf('/');
		int k1 = url.indexOf('?',k0+1);
		if (k1 < 0) k1 = url.length();

		if (k0 > 0) {
		    String database = url.substring(k0+1,k1);
		    if (database.length() > 0) info.put("database",database);
		}

		if (k1 < url.length())
		    parseArgs(url.substring(k1+1),info);
	}

	protected static void notifyClosed(MyConnection connection)
		throws SQLException
	{
		if (--liveConnections <= 0 && databaseHandle!=0 && auto_shutdown) {
			shutdown();
		}
	}

	public static void setAutoShutdown(boolean auto)
	{
		auto_shutdown = auto;
	}

	class ShutdownHook extends Thread
	{
		public void run()
		{
			shutdown();
		}
	}

	public static void shutdown()
	{
			synchronized (init_lock)
			{
				if (databaseHandle!=0)
					try {
/*
						try {
							api.mysql_shutdown(databaseHandle,
									enum_shutdown_level.SHUTDOWN_WAIT_TRANSACTIONS);
									//  doesn't work in embedded mode, why ?
	//								enum_shutdown_level.SHUTDOWN_DEFAULT);
						}
						catch (SQLException e) {
							e.printStackTrace();
						}
*/
						api.mysql_close(databaseHandle);
						api.mysql_server_end();
						//  crashes when thread_init is used, too - why ?
					}
					finally {
						databaseHandle = 0;
					}
			}
	}


	private static String[] createArgs(Properties info)
	{
		ArrayList args = new ArrayList();
		Iterator props = info.entrySet().iterator();
		while (props.hasNext())
		{
			Map.Entry ety = (Map.Entry)props.next();
			String key = (String)ety.getKey();
			Object value = ety.getValue();

			if (key.startsWith("--"))
			{
				//  assume this is a command line arg for mysqld
				String arg = key;

				if (value!=null) {
					String stringValue;
					if (value instanceof File) {
						stringValue = ((File)value).getAbsolutePath();
                        stringValue = stripSlash(stringValue);
                    }
					else if (key.endsWith("dir") || key.endsWith("file"))
					{
						File f = new File(value.toString());
						stringValue = f.getAbsolutePath();
                        stringValue = stripSlash(stringValue);
					}
					else
						stringValue = value.toString();
					if (stringValue.length()>0)
						arg += "="+stringValue;
				}

				if (arg.startsWith("--defaults-file"))
					args.add(0,arg);    //  MUST be first
				else
					args.add(arg);
			}

		}

		//  make a String[]
//		if (args.isEmpty()) return null;

		String[] result = new String[args.size()+1];
		result[0] = ""; //  first argument is ignored
		for (int i=0; i < args.size(); i++)
			result[i+1] = (String)args.get(i);

		return result;
	}

    private static void parseArgs(String url_args, Properties info)
    {
        StringTokenizer tok = new StringTokenizer(url_args,"&",false);
        while (tok.hasMoreTokens())
        {
            String arg = tok.nextToken();
            int k1 = arg.indexOf('=');
            String key;
            String value;
            if (k1 > 0) {
                key = arg.substring(0,k1);
                value = arg.substring(k1+1);
            }
            else {
                key = arg;
                value = "";
            }

	        key = key.trim();
	        value = value.trim();

            info.put(key,value);
        }
    }

    private static String stripSlash(String path)
    {
        int len = path.length();
        while (len > 0 && (path.charAt(len-1)=='/' || path.charAt(len-1)=='\\'))
            len--;
        if (len==path.length())
            return path;
        else
            return path.substring(0,len);
    }
}
