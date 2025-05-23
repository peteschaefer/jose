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

package de.jose.db;

//import com.mysql.embedded.jdbc.MyConnection;
import de.jose.*;
import de.jose.comm.Command;
import de.jose.plugin.InputListener;
import de.jose.task.Task;
import de.jose.task.db.CheckDBTask;
import de.jose.util.KillProcess;
import de.jose.util.StringUtil;
import de.jose.util.file.FileUtil;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
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
	//protected static boolean bootstrap = false;
	protected static boolean init_server = false;
	protected FileWatch watch;

	public static final int ER_QUERY_INTERRUPTED = 1317;
	public static final int ER_SORT_ABORTED = 1028;

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

	@Override
	public Thread launchProcess() {
		MySqlLauncher launcher = new MySqlLauncher();
		launcher.start();
		return launcher;
	}

	@Override
	public boolean launchComplete() {
		return init_server;
	}

	public boolean waitForStandaloneServer() throws IOException {
		InputStream in1 = serverProcess.getInputStream();
		InputStream in2 = serverProcess.getErrorStream();
		BufferedReader bin = new BufferedReader(new InputStreamReader(in2));
		for(int i=0; i<100; ++i) {
			if (! serverProcess.isAlive())
				return false;
			String line = bin.readLine();
			if (line!=null) {
				System.out.println(line);
				if (serverIsReady(line))
					return true;
			}
		}
		return false;
	}

	protected Process createServerProcess(Vector command, Vector env, boolean printCommandLine) throws IOException
	{
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

		Runtime runtime = Runtime.getRuntime();
		Process result = runtime.exec(commandArray,envArray);
		if (killProcess!=null)
			runtime.removeShutdownHook(killProcess);
		runtime.addShutdownHook(killProcess = new KillMySqlProcess(serverProcess));
		return result;
	}

	protected boolean serverIsReady(String line) {
		return line.contains("ready for connections");
	}

	public static void upgradeTables(JoConnection conn, String schema) throws SQLException
	{
		DatabaseMetaData dbmeta = conn.jdbcConnection.getMetaData();
		ResultSet res = dbmeta.getTables("jose", null, null, new String[]{"TABLE"});
		while (res.next()) {
			String tableName = res.getString("TABLE_NAME");
			conn.executeUpdate("ALTER TABLE " + tableName + " FORCE");
		}
	}

	/**
	 * Background thread to start the mysqld server process
	 * and setup the first Connection
	 */
	protected class MySqlLauncher extends Thread {
		public MySqlLauncher() {
			super("MySql Launcher");
		}

		@Override
		public void run() {
			if (false)
            try {
                sleep(40000);	//	artificial delay; for testing only
            } catch (InterruptedException e) {
			}

			boolean bootstrap = false;
			File mysqldir = getDataDir();

            switch(getServerMode()) {
				case MODE_STANDALONE:
					//props.put("user","");
					//props.put("password","");
					//props.put("characterEncoding","UTF8");
					String portno = Version.getSystemProperty("jose.db.port");
					if (portno!=null)
						props.put("port-no",portno);

					try {
                        serverProcess = startStandaloneServer(true);
						if (!waitForStandaloneServer())
							throw new SQLException("Server failed to respond. giving up.");
                    } catch (Exception e) {
						Application.error(e);
					}
					break;

				case MODE_EMBEDDED:
					//	deprecated but still workable
					initEmbeddedServer();
					break;
			}

            try {
				//	stock connection pool with at least one connection
				JoConnection connection=null;

				switch(getServerMode()) {
					case MODE_STANDALONE:
					case MODE_EMBEDDED:
						bootstrap = askBootstrap(mysqldir);
						watchDirectory();
						connection = JoConnection.get();	// call AFTER askBootstrap, s.t. directory exists
						break;
					case MODE_EXTERNAL:
						connection = JoConnection.get();	// call BEFORE bootsctrap, /bc we need to look for
						bootstrap = ! existsMetaInfo(connection);
						break;
				}

				if (bootstrap)
					bootstrap(connection.getJdbcConnection());

				Task checkIntegrity = new CheckDBTask(connection);
				checkIntegrity.setSilentTime(5000);
				checkIntegrity.run();
				if (checkIntegrity.wasSuccess())
					init_server = true;

				//JoConnection.release(connection);
            } catch (SQLException e) {
                Application.error(e);
            }

			for(Command cmd : deferredActions)
				postAfterLaunch(cmd);	// this should now post into the event loop
			deferredActions.clear();
		}
	}

	public Connection createConnection(int mode)
		throws SQLException
	{
/*		if (Util.allOf(mode, JoConnection.RECOVER))
			props.put("force","true");
		if (Util.allOf(mode, JoConnection.CREATE))
			props.put("create","true");
*/
		props.put("clobberStreamingResults","true");
		Connection conn = super.createConnection(mode);
		return conn;
	}

	public int getConnectionId(Connection conn) throws SQLException {
		Statement stm = null;
		ResultSet res = null;
		try {
			stm = conn.createStatement();
			res = stm.executeQuery("SELECT Connection_ID()");
			if (res.next()) {
				int connId = res.getInt(1);
				return connId;
			}
		} finally {
			if (res!=null) res.close();
			if (stm!=null) stm.close();
		}
		return -1;
	}

	private void initEmbeddedServer()
	{
		File mysqldir = getDataDir();
		File bindir = new File(Application.theWorkingDirectory, "bin");
		File libdir = new File(Application.theWorkingDirectory, "lib/"+Version.osDir);
		File tmpdir = new File(Application.theDatabaseDirectory, "tmp");
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
		props.put("--tmpdir",tmpdir);
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
	}

	private void watchDirectory()
	{
		/**	do not run two embedded servers on the same directory	*/
		try {
			File watchFile = new File(getDataDir(), "db.lock");
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
		abs.put("index_key_size",	255);
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
		abs.put("delayed_key_write", Boolean.TRUE/*Util.toBoolean(props.getProperty("delayed_key_write"))*/);
	}

	public String escapeSql(String sql)
	{
		/** noop    */
		return sql;
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

	public Process repairIndexes(String[] tables, String[] switches) throws IOException, InterruptedException
	{
		File mysqldir = getDataDir();
		File tmpdir = new File(Application.theDatabaseDirectory, "tmp");
		File lockFile = new File(Application.theDatabaseDirectory, "db.lock");

		Vector command = new Vector();
		Vector env = new Vector();
		String binPath = Application.theWorkingDirectory.getAbsolutePath()+File.separator+"bin";
		String execPath = binPath+File.separator+Version.osDir+File.separator+"myisamchk";

		command.add(execPath);
		for(String sw:switches)
			command.add(sw);

		for(String table : tables)
			command.add(table);

		String[] args = (String[])new String[command.size()];
		command.toArray(args);

		ProcessBuilder pb = new ProcessBuilder(args);
		pb.directory(new File(mysqldir,"jose"));
		pb.redirectErrorStream(true);
		pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
		return pb.start();
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
		File mysqldir = getDataDir();
		File tmpdir = new File(Application.theDatabaseDirectory, "tmp");

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
		command.add("--character-set-server=utf8");
		command.add("--collation-server=utf8_general_ci");
		command.add("--console");	// do write to std-out

		//	MyISAM parameters; bump up default values to accomodate GIGA databases
		command.add("--key-buffer-size=64M");
		command.add("--max-allowed-packet=1M");
		command.add("--table-cache=64");
		command.add("--net-buffer-length=8K");
		command.add("--read-buffer-size=16M");
		command.add("--read-rnd-buffer_size=128M");
		command.add("--sort-buffer-size=512M");
		command.add("--bulk-insert-buffer-size=256M");	//	for import?
		command.add("--myisam-sort-buffer-size=256M");
		command.add("--myisam-recover=FORCE"); //  always check for corrupted index files, etc.
		//command.add("--myisam-use-mmap=ON");	//	since 5.1 !
		//	default table size for tmp and memory tables is 16MB. Not enough.
		//	huge database have around 3GB, or more.
		command.add("--concurrent-insert=2");	//2=ALWAYS
		command.add("--tmp-table-size=16G");
		command.add("--max-heap-table-size=16G");
		//command.add("--default-time-zone='+00:00'"); does not work


		//	for server-side operation: set connection timeout as high as possible:
		String infTimeout = Version.windows ? "2147483" : "31536000";
//		command.add("--wait-timeout=" + infTimeout);
//		command.add("--interactive-timeout=" + infTimeout);
		// don't rely on automatic timeout. use connection pool watchdog instead

		//	MySQL 8.0.x
//		command.add("--upgrade=NONE");	//	don't upgrad old MyISAM tables
//		command.add("--mysqld="+execPath);	//	used for "mysqld_safe"
//		command.add("--log-error=error.log");
//		command.add("--secure-file-priv="+Application.theDatabaseDirectory);

		props.put("--default-character-set","utf8");
		props.put("--default-collation","utf8_general_ci");


		/** delayed key write is optional; makes sense when importing large files   */
		if (can("delayed_key_write"))
		{
			command.add("--delay-key-write=ALL");
			props.put("--delay_key_write","ALL");
			/** when delayed key writing is enabled,
			 *  "myisam-recover" is especially important
			 */
		}

		if (!Version.MYSQL_UDF) command.add("--skip-external-locking");
		command.add("--skip-locking");

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
			(it would matter if we used mixed casing, but we don't)
		*/
		if (Version.windows)
			command.add("--lower_case_table_names=1");
		else if (Version.linux)
			command.add("--lower_case_table_names=2");
		else if (Version.mac)
			command.add("--lower_case_table_names=2");
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
			System.err.println(props.get("url"));
		}

		//	set data directory
		command.add("--datadir");
		command.add(mysqldir.getAbsolutePath());

		command.add("--tmpdir");
		command.add(tmpdir.getAbsolutePath());
		tmpdir.mkdirs();

		//	set base directory
		command.add("--basedir");
		command.add(binPath);

		return createServerProcess(command,env, printCommandLine);
	}

	public File getDataDir() {
		return new File(Application.theDatabaseDirectory, "mysql");
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

	public boolean existsMetaInfo(JoConnection connection)
	{
        try {
            int rowCount = connection.selectInt("SELECT count(*) FROM MetaInfo");
			return (rowCount > 0);
        } catch (SQLException e) {
            return false;
        }
	}

	public boolean askBootstrap(File mysqldir)
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
			return true;
		}
		else if (FileUtil.isEmptyDir(dbdir)) {
			//	setup database (without asking)
			return true;
		}
		return false;
	}

	public void shutDown(Connection conn) throws Exception {
		//	(1) com.mysql.jdbc.MiniAdmin.shutDown1()
		//	(2) com.mysql.jdbc.Connection.shutdownServer()

		//	note however, that both classes are not in the system classpath
		//	use urlClassloaded instead, with some reflection
		Class myclass = urlClassLoader.loadClass("com.mysql.jdbc.Connection");
		Object myconn = myclass.cast(conn);
		Method shutdown = myclass.getMethod("shutdownServer");
		shutdown.invoke(myconn);
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
				//mysqladmin("shutdown");
				if (conn==null)	conn = JoConnection.get();
				if (conn.jdbcConnection==null) conn = JoConnection.theConnections.create(MySQLAdapter.this);
				shutDown(conn.jdbcConnection);
				//	driver version 8 has a similar function.
				//	but we stick with version 5 for the standalone case, don't we?
				done = true;
				//144serverProcess.waitFor();	// not necessary !?
			} catch (Throwable thr) {
				//	our last resort:
				thr.printStackTrace();
				super.run();
			}
            serverProcess = null;
		}
	}

	/**	test if mysql server is running*/
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

	/**
	 * shut down the database
	 */
	public Thread shutDown(JoConnection conn)
	{
		/*	mysqladmin shutdown
		*/
		if (killProcess!=null) {
			killProcess.conn = conn;
			killProcess.run();
		}
		return killProcess;
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
		/*	embedded library not in use anymore
			if (init_server && getServerMode()==MySQLAdapter.MODE_EMBEDDED)
			try {
			((MyConnection)conn.jdbcConnection).killQuery();
			return true;
			} catch (Exception e) {
				return false;
		}
		else*/ {
			JoConnection adminConn = null;
			int connId = conn.connectionId;
			if (connId >= 0) try {
				adminConn = JoConnection.get();
				adminConn.executeUpdate("KILL QUERY "+connId);
				//adminConn.selectString("KILL QUERY "+connId);
			} finally {
				if (adminConn!=null) adminConn.release();
			}
			return true;
		}
		//return false;
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
