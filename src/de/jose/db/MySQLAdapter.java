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

package de.jose.db;

import com.mysql.jdbc.MiniAdmin;
import com.mysql.embedded.jdbc.MyConnection;
import de.jose.*;
import de.jose.plugin.InputListener;
import de.jose.util.KillProcess;
import de.jose.util.ProcessUtil;
import de.jose.util.StringUtil;
import de.jose.util.file.FileUtil;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;


/**
 * Database Adapter for MySQL database
 *
 * the preferred driver for FORWARD_ONLY (JDBC 1.0) ResultSets is com.caucho.jdbc.mysql.Driver
 *	it has better response times
 *
 * the preferred driver for SCROLLABLE (JDBC 2.0) ResultSets is org.gjt.mm.mysql.Driver
 *	please note however, that it uses a client-side cursor so that it is
 *	recommended to keep the ResultSet small
 *
 */

public class MySQLAdapter
		extends DBAdapter
{
	protected static String PRODUCT_VERSION = null;
	protected static boolean bootstrap = false;
	protected static boolean init_embedded = false;
	protected FileWatch watch;

	/**	default ctor	*/
	protected MySQLAdapter()
	{ }

	protected static Process serverProcess = null;
	protected static KillMySqlProcess killProcess = null;
/*
	public String getDatabaseProductName(Connection jdbcConnection)
		throws SQLException
	{
		return "MySQL";
	}
*/
	public String getDatabaseProductVersion(Connection jdbcConnection)
		throws SQLException
	{
		if (PRODUCT_VERSION==null)
			PRODUCT_VERSION = getProductInfo();
		if (PRODUCT_VERSION.length()==0)
			return super.getDatabaseProductVersion(jdbcConnection);
		return PRODUCT_VERSION;
	}

    public boolean isVersion41(Connection jdbcConnection) throws SQLException
    {
        return (StringUtil.compareVersion(getDatabaseProductVersion(jdbcConnection),"4.1") >= 0);
    }

	public Connection createConnection(int mode)
		throws SQLException
	{
/*		if (Util.allOf(mode, JoConnection.RECOVER))
			props.put("force","true");
		if (Util.allOf(mode, JoConnection.CREATE))
			props.put("create","true");
*/
		switch (getServerMode())
		{
		case MODE_STANDALONE:
			if (serverProcess==null)
				synchronized (this) {
					if (serverProcess==null && killProcess==null)
						try {
							serverProcess = startStandaloneServer(false);
							/**	since the server is running in a separate process,
							 * 	this may take some time to complete.
							 * 	to account for that, we sleep a bit, then try several connect requests
							 */
							try {
								Thread.sleep(300);
							} catch (InterruptedException e) {
							}

							int repeat = 4;
							int sleep = 2000;
							SQLException ex = null;

							Connection result = null;
							while (result == null && repeat-- > 0)
								try {
                                    props.put("user","");
                                    props.put("password","");
									result = super.createConnection(mode);
								} catch (SQLException sqlex) {
									//	server not yet up ? try again ...
									ex = sqlex;
									try {
										System.err.print(".");
										Thread.sleep(sleep);
									} catch (InterruptedException iex) {
										//	don't care
									}
								}

							watchDirectory();

							if (result!=null) {
								if (bootstrap) {
									//	bootstrap new database
									bootstrap(result);
								}
								return result;
							}

							//	timed out !
							throw ex;

						} catch (IOException ioex) {
							throw new SQLException("failed to start MySQL server: "+
								ioex.getLocalizedMessage());
						}
				}
			break;

		case MODE_EMBEDDED:
			if (!init_embedded)
			synchronized (this)
			{
				if (!init_embedded) {
					File mysqldir = initEmbeddedServer();

					init_embedded = true;

					if (askBootstrap(mysqldir)) {
						//	bootstrap new database
						Connection result = super.createConnection(mode);
						bootstrap(result);
						return result;
					}
				}
			}
			break;

		case MODE_EXTERNAL:
			//  just as usual
			break;
		}
		//	else
		return super.createConnection(mode);
	}

	private File initEmbeddedServer()
	{
		File mysqldir = new File(Application.theDatabaseDirectory, "mysql");
		File bindir = new File(Application.theWorkingDirectory, "bin");
		File libdir = new File(Application.theWorkingDirectory, "lib/"+Version.osDir);
//		File defaultsFile = new File(Application.theWorkingDirectory, "config/mysql.ini");

		// setup parameters for embedded driver
		props.put("library.path",libdir);
		/** database */
		props.put("database","jose");

		/** server parameters   */
		/** mysql base directory */
		props.put("--basedir",bindir);
		/** data directory */
		props.put("--datadir",mysqldir);
		//  more config parameters are read from my.ini
		//  groups: mysqld embedded
//		props.put("--defaults-file",defaultsFile);

		props.put("--default-character-set","utf8");
		props.put("--default-collation","utf8_general_ci");

		//  most of the following are already defined in my.ini
		//  doesn't hurt to define them twice:
		props.put("--skip-bdb","");
		props.put("--skip-innodb","");
		props.put("--skip-networking","");
		props.put("--skip-name-resolve","");
		props.put("--skip-grant-tables","");
		props.put("--skip-locking","");
		props.put("--skip-external-locking","");
		props.put("--lower_case_table_names","0");   //  means: always use exact case

//					props.put("--debug","O,debug.log");//"d:D,20:O,debug.log");
//					props.put("--log-error","/windows/D/jose/work/error.log");
//					props.put("--log","/windows/D/jose/work/query.log");
//					props.put("--console","");

		/** fine tuning */
		props.put("--key_buffer",   "16M");
		props.put("--max_allowed_packet",   "1M");
		props.put("--table_cache",  "64");
		props.put("--sort_buffer_size", "512K");
		props.put("--net_buffer_length",    "8K");
		props.put("--read_buffer_size", "256K");
		props.put("--read_rnd_buffer_size", "512K");
		props.put("--myisam_sort_buffer_size",  "8M");
		props.put("--myisam-recover","FORCE");  //  always check for corrupted index files, etc.

		/** delayed key write is optional   */
		if (can("delayed_key_write"))
		{
			props.put("--delay_key_write","ALL");
			/** when delayed key writing is enabled,
			 *  "myisam-recover" is especially important
			 */

		}
		watchDirectory();
		return mysqldir;
	}

	private void watchDirectory()
	{
		/**	do not run two embedded servers on the same directory	*/
		try {
			File watchFile = new File(Application.theDatabaseDirectory, "mysql/db.lock");
			watch = new FileWatch(watchFile,"error.duplicate.database.access");
		} catch (IOException e) {
			//  maybe we are reading from a read-only medium ?
			//  ignore it ...
			watch = null;
		}
	}

	/**	overwrite fo specific databases !	 */
	public String getDBType(String sqlType, String size, String precision)
	{
/*		if (sqlType.equalsIgnoreCase("BIGINT"))
			return "LONGINT";
*/		if (sqlType.equalsIgnoreCase("LONGVARCHAR"))
			return "MEDIUMTEXT";	//	TEXT can store up to 2^16 characters; use MEDIUMTEXT or LONGTEXT for more
        if (sqlType.equalsIgnoreCase("LONGVARCHAR"))
            return "MEIDUMBLOB";	//	BLOB can store up to 2^16 characters; use MEDIUMBLOB or LONGBLOB for more
		//	else:
		return super.getDBType(sqlType,size,precision);
	}

	protected void setAbilities(Properties abs)
	{
		super.setAbilities(abs);
        /** can't use JDBC batch updates */
//		abs.put("batch_update",		Boolean.TRUE);	//	or shouldn't we ?
		abs.put("batch_update",	Boolean.valueOf(getServerMode()!=MODE_EMBEDDED));
        /** but therea are multirow updates ! */
		abs.put("insert_multirow",	Boolean.TRUE);
//		abs.put("insert_multirow",	Boolean.valueOf(getServerMode()!=MODE_EMBEDDED));
		/** with external servers, use multirow and batching to save roundtrips
		 *  with embedded servers, roundtrips are cheap
		 */
        /** cascading delete is not supported with MyISAM tables (or is it?) */
        abs.put("cascading_delete",	Boolean.FALSE);
        /**	VIEWs are not yet supported by MySQL ;-(((		 */
        abs.put("view",				Boolean.FALSE);
        /** we can use CREATE FULLTEXT INDEX */
        abs.put("fulltext_index",   Boolean.TRUE);
        /** no server-side cursors  */
		abs.put("server_cursor",	Boolean.FALSE);
		abs.put("prefer_max_aggregate", Boolean.TRUE);
		/**	SELECT MAX(Id) is faster than SELECT Id ORDER BY Id DESC */
//		abs.put("STRAIGHT_JOIN",	"STRAIGHT_JOIN");
		/** don't enable STRAIGHT_JOIN optimizer hint */
		/**	DELAYED is a hint for delayed input processing */
		if (Version.getSystemProperty("jose.delayed.insert",false))
			abs.put("DELAYED",			"DELAYED");
		/** INSERT DELAYED can improve the performance of bulk inserts
		 *  but it tends to fill up internal MySQL buffers.
		 *  With huge inserts (e.g. PGN import), performance
		 *  could drop dramatically at some point.
		 *   */
		abs.put("multiple_results",	Boolean.FALSE);
		/**	can not handle multiple result sets over the same connection;
		 *	separate connections must be allocated
		 */
		abs.put("subselect",		Boolean.FALSE);
		/**	MySQL can not use nested select statements (one of the biggest drawbacks of MySQL ;-(	*/
		abs.put("multi_table_delete", Boolean.TRUE);
		abs.put("multi_table_update", Boolean.TRUE);
		/**	as a substitute, we can use multi-table deleted statements	*/
		abs.put("result_limit",		Boolean.TRUE);
		/**	closing a huge result set can become very expensive (more expensive than the query itself)
		 * 	better use limits
		 */
		abs.put("index_key_size",	new Integer(255));
		/**	indexes on LONGVARCHAR columns must be given a key size,
		 * 	like CREATE INDEX ... ON Collection(Path(255))
		 */
		abs.put("like_case_sensitive", Boolean.FALSE);
		/**	LIKE comparisons are not case sensitive by default
		 */
		if (getServerMode()!=MODE_EXTERNAL) {
			abs.put("quick_dump",			Boolean.TRUE);
			/**	file format for quick dump	*/
		}
		/** do we support delayed key write ?   */
		abs.put("delayed_key_write", Util.toBoolean(props.getProperty("delayed_key_write")));
	}

	public String escapeSql(String sql)
	{
		/** noop    */
		return sql;
	}

	public void setProcessPriority(int prio)
	{
		if ((prio != processPriority) && (serverProcess != null))
			ProcessUtil.setPriority(serverProcess,prio);
		super.setProcessPriority(prio);
	}


	/**	retrieve the character encoding that is used by this database
	 * @return a character set identifier
	 */
	public String getCharacterSet(JoConnection conn)
		throws SQLException
	{
		return "UTF-16";
	}
/*

	public static boolean isDynamicResultSet(ResultSet res)
	{
		try {
			RowData rowData = (RowData)ReflectionUtil.getValue(res,"rowData");
			return rowData.isDynamic();
		} catch (Exception ex) {
			return false;
		}
	}
*/

	public static void main(String[] args)
	{
		try {
			Version.setSystemProperty("jose.db","MySQL-standalone");
			Version.setSystemProperty("jose.console.output","on");
			Version.setSystemProperty("jose.splash","off");
			Application.parseProperties(args);

			new Application();

			MySQLAdapter adapter = (MySQLAdapter)
			        DBAdapter.get(Application.theApplication.theDatabaseId,
			                Application.theApplication.theConfig,
			                Application.theWorkingDirectory,
			                Application.theDatabaseDirectory);

			adapter.startStandaloneServer(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String myisampack(File dataDir, String commandLine) throws IOException
	{
		String binPath = Application.theWorkingDirectory.getAbsolutePath()+File.separator+"bin";
		String execPath = binPath+File.separator+Version.osDir+File.separator+"myisampack";

		Process proc = Runtime.getRuntime().exec(execPath+" -vw "+commandLine, null, dataDir);

		InputStream stdout = proc.getInputStream();
		InputStream stderr = proc.getErrorStream();
		StringBuffer result = new StringBuffer();

		int c;
		while ((c=stdout.read())>=0)
			result.append((char)c);
		while ((c=stderr.read())>=0)
			result.append((char)c);

		for (;;)
			try {
				int exitValue = proc.waitFor();
				result.append("Exit Value: ");
				result.append(exitValue);
				break;
			} catch (InterruptedException e) {
				continue;
			}

		System.out.println(result.toString());
		return result.toString();
	}

	public Process startStandaloneServer(boolean printCommandLine)
		throws IOException
	{
		/*		setup the MySQL server
		*/

		/**	.../mysqld
		 *	--port=...
		 *  --socket=...
		 *  --skip-bdb --skip-innodb
		 *  --datadir=...
		 *  --basedir=...
		 *  -u root
		 */
		File mysqldir = new File(Application.theDatabaseDirectory, "mysql");
        askBootstrap(mysqldir);

		Vector command = new Vector();
		Vector env = new Vector();
		String binPath = Application.theWorkingDirectory.getAbsolutePath()+File.separator+"bin";
		String execPath = binPath+File.separator+Version.osDir+File.separator+"mysqld";
//		String defaultsPath = Application.theWorkingDirectory.getAbsolutePath()+
//		                    File.separator+"config"+File.separator+"mysql.ini";

		command.add(execPath);
		//  more config parameters are read from my.ini
		//  groups: mysqld server mysqld-4.1
//		command.add("--defaults-file="+defaultsPath);

		//  most of the following are already defined in my.ini
		//  doesn't hurt to define them twice:
		command.add("--skip-bdb");
		command.add("--skip-innodb");
		command.add("--skip-grant-tables");
		command.add("--skip-name-resolve");

		command.add("--default-character-set=utf8");
		command.add("--default-collation=utf8_general_ci");

		if (!Version.MYSQL_UDF) command.add("--skip-external-locking");
		// only connect to local host; skip DNS name resolve
//		if (Version.mysql40) {
//			command.add("--skip-thread-priority");
//			command.add("--console");   //  don't write error log
//			//	does this option improve response times ?
//		}

		/*  use exact lettercase for table names
			this is already the default for Linux but we have to eplicitly
			force it on OS X (which may or may not be case sensitive)
			on Windows, it doesn't matter anyway
		*/
		command.add("--lower_case_table_names=0");   //  means: always use exact case

		if (Version.mysql40)
			command.add("--key_buffer_size=64M");
//		large key buffer is useful(?) for bulk inserts

		boolean tcpConnect = false;

		if (Version.unix && Version.MYSQL_UDF) {
			//	set library path fo UDF
			String libPath = Application.theWorkingDirectory.getAbsolutePath()+
							"/lib/"+Version.osDir;
			env.add("LD_LIBRARY_PATH="+libPath);
		}

		if (! Version.getSystemProperty("jose.pipe",true))
			tcpConnect = true;
        else if (Version.linuxIntel && (props.getProperty("socket-file")!=null)) {
	        //	UNIX: use sockets
	        //  note that Mac OS X is UNIX, too
	        String socket = props.getProperty("socket-file");
        	command.add("--socket="+socket.trim());
			//	if current user is root, we have to supply -u
			String userName = Version.getSystemProperty("user.name");
			if ("root".equals(userName)) {
				command.add("-u");
				command.add("root");
			}

			//	(UnixSocketFactory is currently only implemented for Linux/Intel platform
			//	 however, porting to other Unixes should be easy)
			command.add("--skip-networking");		//	disable TCP/IP for external connections

			props.put("socketFactory","de.jose.db.UnixSocketFactory");
			props.put("socketPath",socket);

			props.put("url", "jdbc:mysql://./jose");
        }
		else if (Version.winNTfamily && (props.getProperty("pipe-name")!=null)) {
			//	Win NT: use named pipes
			String pipe = props.getProperty("pipe-name");

			//	params to mysqld
			command.add("--enable-named-pipe");
			command.add("--skip-networking");		//	disable TCP/IP
			command.add("--socket="+pipe.trim());
			//	params to JDBC driver
			props.put("socketFactory","com.mysql.jdbc.NamedPipeSocketFactory");
			props.put("namedPipePath","\\\\.\\pipe\\"+pipe);
			props.put("url", "jdbc:mysql://./jose");
//			File pipefile = new File("\"\\\\\\\\.\\\\pipe\\\\\"+pipe");
//			System.out.println("pipe exists: "+pipefile.exists());
		}
		else
			tcpConnect = true;

		if (tcpConnect) {
			//	else: use TCP/IP. choose a random port
			//	avoid conflicting ports with other mySql servers
			//	note that an open TCP/IP port constitutes a security risk, unless there is a firewall
			String portno = (String)props.get("port-no");
			if (portno==null) {
				//  choose a random port number from the private range (i.e. 49152 through 65535)
				Random rnd = new Random();
				int pno = 49152 + Math.abs(rnd.nextInt()) % (65535-49152);
				portno = String.valueOf(pno);
				props.put("port-no",portno);
			}

			command.add("--port="+portno.trim());

			props.put("url", "jdbc:mysql://localhost:"+portno+"/jose");
			System.out.println(props.get("url"));
		}

		//	set data directory
		command.add("--datadir");
		command.add(mysqldir.getAbsolutePath());

		//	set base directory
		command.add("--basedir");
		command.add(binPath);

		String[] commandArray = StringUtil.toArray(command);
		String[] envArray = StringUtil.toArray(env);

		if (printCommandLine) {    //  print command line
			for (int i=0; i<envArray.length; i++)
				System.err.println(envArray[i]);
			System.err.println();
			for (int i=0; i<commandArray.length; i++) {
				System.err.print(commandArray[i]);
				System.err.print(" ");
			}
			System.err.println();
		}

		Process result = Runtime.getRuntime().exec(commandArray);
		Runtime.getRuntime().addShutdownHook(killProcess = new KillMySqlProcess(serverProcess));
		return result;
	}
/*

	public static String[] getEnv()
	{
		Map env = System.getenv();
		String[] envArray = new String[env.size()];
		Iterator i = env.entrySet().iterator();
		for (int j=0; i.hasNext(); j++)
		{
		    Map.Entry entry = (Map.Entry)i.next();
		    envArray[j] = entry.getKey()+"="+entry.getValue();
		}
		return envArray;
	}
*/
/*

    public static void main(String[] args)
    {
        String[] commandArray = {
            "D:\\jose\\work\\bin\\Windows\\mysqld",
            "--skip-bdb", "--skip-innodb", "--skip-grant-tables",
            "--skip-name-resolve", "--skip-external-locking", "--skip-thread-priority",
            "--console", "--lower_case_table_names=0", "--key_buffer_size=64M",
            "--port=54853",
            "--datadir", "D:\\jose\\work\\database\\mysql",
            "--basedir", "D:\\jose\\work\\bin",
        };
        String[] envArray;
        try {

			envArray = getEnv();

            String command =  "D:\\jose\\work\\bin\\Windows\\mysqld "+
                    "--skip-bdb --skip-innodb --skip-grant-tables "+
                    "--skip-name-resolve --skip-external-locking "+
                    "--lower_case_table_names=0 "+
//                    "--port=54853 "+
                    "--enable-named-pipe --skip-networking --socket=mysql.jose "+
                    "--datadir D:\\jose\\work\\database\\mysql "+
                    "--basedir D:\\jose\\work\\bin";

            System.err.println(command);
            System.err.println("");

            Process server = Runtime.getRuntime().exec(command,envArray,new File("C:\\mysql-4.1.7"));

            int result = server.waitFor();
            System.err.println("exit code="+result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    static class DebugListener implements InputListener
    {

        public void readLine(char[] chars, int offset, int len) throws IOException {
            System.err.print(new String(chars,offset,len));
        }

        public void readEOF() throws IOException {

        }

        public void readError(Throwable ex) throws IOException {
            ex.printStackTrace(System.err);
        }
    }

	private boolean askBootstrap(File mysqldir)
	{
		File dbdir = new File(mysqldir, "jose");

		if (!mysqldir.exists()) {
			//	ask user to init database
            String question = Language.get("bootstrap.confirm");
            question = StringUtil.replace(question,"%datadir%",mysqldir.getAbsolutePath());
			int choice = JoDialog.showYesNoDialog(question,Language.get("bootstrap.create"),
			        "Create","Quit", JOptionPane.YES_OPTION);

			if (choice != JOptionPane.YES_OPTION)
				System.exit(+1);
        }

		if (!dbdir.exists()) {
					dbdir.mkdirs();
					bootstrap = true;
				}
				else if (FileUtil.isEmptyDir(dbdir)) {
					//	setup database (without asking)
					bootstrap = true;
				}
		return bootstrap;
	}

	class KillMySqlProcess extends KillProcess
	{
		JoConnection conn = null;

		KillMySqlProcess(Process process) {
			super(process);
		}

		public void run()
		{
			if (done) return;
			/**	this can happen if shutDown is called explicitly and
			 * 	then again from a shutdown hook (don't mind)
			 */
			try {
				if (watch!=null) watch.finish();
//				mysqladmin("shutdown");
				if (conn==null)	conn = JoConnection.get();
				MiniAdmin admin = new MiniAdmin(conn.jdbcConnection);
				admin.shutdown();
				done = true;
			} catch (Throwable thr) {
				//	our last resort:
				thr.printStackTrace();
				super.run();
			}
			//	serverProcess.waitFor();	// not necessary !?
			serverProcess = null;
		}
	}

	/**	test if mysql server is running
	public boolean pingServer(Process serverProcess) throws IOException
	{
		Process proc = mysqladmin("ping");
		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String result = in.readLine();
		return result.endsWith("is alive");
	}


	protected Process mysqladmin(String param) throws IOException
	{

		StringBuffer command = new StringBuffer();
		//	default path is <work dir>/bin/<os>/mysqladmin
		command.append(Application.theWorkingDirectory.getAbsolutePath());
		command.append(File.separator);
		command.append("bin");
		command.append(File.separator);
		command.append(Version.osDir);
		command.append(File.separator);
		command.append("mysqladmin");
		command.append(" --skip-name-resolve");

		boolean tcpConnect = false;
		if (Version.getSystemProperty("jose.no.pipe",false))
			tcpConnect = true;
        else if (Version.unix && (props.getProperty("socket-file")!=null)) {
			//	UNIX: use sockets
			String socket = props.getProperty("socket-file");
			command.append(" --socket=");
			command.append(socket);
		}
		else if (Version.winNTfamily && (props.getProperty("pipe-name")!=null)) {
			//	Win NT: use named pipes
			String pipe = props.getProperty("pipe-name");
			command.append(" --socket=");
			command.append(pipe);
		}
		else
			tcpConnect = true;

		if (tcpConnect) {
			//	else: TCP-IP
            String portno = props.getProperty("port-no");
            if (portno!=null) {
                command.append(" --port=");
                command.append(portno);
            }
        }

        command.append(" ");
		command.append(param);
		String commandStr = command.toString();
//		System.err.println(commandStr);
		return Runtime.getRuntime().exec(commandStr);
	}
	*/

	/**
	 * shut down the database
	 */
	public void shutDown(JoConnection conn)
	{
		/*	mysqladmin shutdown
		*/
		if (killProcess!=null) {
			killProcess.conn = conn;
			killProcess.run();
		}
	}

	public void disableConstraints(String table, JoConnection conn) throws SQLException {
		conn.executeUpdate("ALTER TABLE "+table+" DISABLE KEYS");
	}

	public void enableConstraints(String table, JoConnection conn) throws SQLException {
		conn.executeUpdate("ALTER TABLE "+table+" ENABLE KEYS");
	}

	public void flushResources(JoConnection conn) throws SQLException
	{
//		conn.executeUpdate("FLUSH TABLES");
	}

	public boolean cancelQuery(JoConnection conn) throws SQLException
	{
		if (init_embedded)
			try {
			((MyConnection)conn.jdbcConnection).killQuery();
			return true;
			} catch (Exception e) {
				return false;
		}
		else {
//			conn.executeUpdate("KILL QUERY");   //  TODO
//			return true;
		}
		return false;
	}


	protected String getProductInfo()
		throws SQLException
	{
/*
		Process proc = mysqladmin("version");
		if (proc==null)
			return "";

		BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		StringBuffer result = new StringBuffer();
		//	skip first line
		in.readLine();
		for (;;) {
			String line = in.readLine();
			if (line==null) break;
			result.append(line);
			result.append('\n');
		}
		return result.toString();
*/
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			DatabaseMetaData meta = conn.jdbcConnection.getMetaData();
			return meta.getDatabaseProductVersion()+"\n\n"+
					meta.getDriverName()+" "+meta.getDriverVersion();
		} finally {
			JoConnection.release(conn);
		}
	}

	/**
	 * append an bitwise OR operation
	 * the MySQL implementation is:
	 *		(a | b)
	 */
	public StringBuffer appendBitwiseOr(StringBuffer buf, String a, int b)
	{
		buf.append(a);
		buf.append("|");
		buf.append(b);
		return buf;
	}

	/**
	 * append an bitwise OR operation
	 * the MySQL implementation is:
	 *		(a & ~b)
	 */
	public StringBuffer appendBitwiseNot(StringBuffer buf, String a, int b)
	{
		buf.append(a);
		buf.append("&");
		buf.append(~b);
		return buf;
	}

	/**
	 * append an bitwise test operation
	 * the MySQL implementation is
	 * 		(a&b) != 0
	 */
	public StringBuffer appendBitwiseTest(StringBuffer buf, String a, int b, boolean testTrue)
	{
		buf.append("(");
		buf.append(a);
		buf.append("&");
		buf.append(b);
		buf.append(")");
		if (testTrue)
			buf.append("!=0");
		else
			buf.append("=0");
		return buf;
	}

	/**
	 * this method is needed for databases where the LIKE clause is not case sensitive
	 * see isLikeCaseSensitive()
	 *
	public String makeLikeClause(String a, String b, char escapeChar, boolean caseSensitive)
	{
		if (caseSensitive)
			return super.makeLikeClause(" BINARY "+a, " BINARY "+b, escapeChar, true);
		else
			return super.makeLikeClause(a,b, escapeChar, false);
	}
	 */

	public static void defineUDFs(JoConnection conn) throws SQLException
	{
		/**	get path to library	*/
		String lib;
		if (Version.windows)
			lib = Application.theWorkingDirectory.getAbsolutePath() +
				"\\lib\\"+Version.osDir+"\\metaphone.dll";
		else
			lib = "metaphone.so";
			//	NOTE: LD_LIBRARY_PATH must be set to <work-dir>/lib/Linux_i386

		defineUDF(conn,"metaphone",lib);
		defineUDF(conn,"jucase",lib);
	}

	public static boolean defineUDF(JoConnection conn, String name, String path) throws SQLException
	{
		/**	(1)	check if function already exists	*/
		String dbpath = conn.selectString("SELECT dl FROM mysql.func WHERE name = '"+name+"'");

		if (dbpath!=null && dbpath.equals(path))
			return false;	//	already defined

		if (dbpath!=null)	//	defined but wrong path - delete
			conn.executeUpdate("DROP FUNCTION "+name);

		path = StringUtil.replace(path,"\\","\\\\");

		String create = "CREATE FUNCTION "+name+
						" RETURNS String "+
						" SONAME '"+path+"'";
		conn.executeUpdate(create);
		return true;
	}

}
