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

import de.jose.*;
import de.jose.db.sqltrace.TraceDriverManager;
import de.jose.db.crossover.*;
import de.jose.pgn.Collection;
import de.jose.task.io.PGNImport;
import de.jose.task.db.CheckDBTask;
import de.jose.task.Task;
import de.jose.util.ClassPathUtil;
import de.jose.util.StringUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import de.jose.util.file.FileUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * a layer between the application and JDBC
 * that deals with databse specific stuff
 *
 */

abstract public class DBAdapter
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	connection modes	 */
	public static final int READ			= 0x0001;
	public static final int WRITE			= 0x0002;
	public static final	int READ_WRITE		= READ+WRITE;
	public static final int RECOVER			= 0x0080;
	public static final int CREATE			= 0x0100;


	/** server is running as an external process (possible on a remote machine)    */
	public static final int MODE_EXTERNAL   = 1;
	/** server is running as a separated process, controlled by this application    */
	public static final int MODE_STANDALONE = 2;
	/** server is running as an embedded server (with a special jdbc driver)    */
	public static final int MODE_EMBEDDED   = 3;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	connection properties	 */
	protected Properties props;

	/**	abilities	 */
	protected Properties abilities;

	protected static Hashtable adapterMap = new Hashtable();

	//-------------------------------------------------------------------------------
	//	Constructor
	//-------------------------------------------------------------------------------

	protected void init(Properties properties, int serverMode)
		throws Exception
	{
		props = properties;

		abilities = new Properties();
		abilities.put("mode",new Integer(serverMode));
		setAbilities(abilities);
	}

	protected void setAbilities(Properties abs)
	{
		abs.put("unique",						Boolean.TRUE);
		abs.put("reference",					Boolean.TRUE);
		abs.put("default",						Boolean.TRUE);
		abs.put("cascading_delete",				Boolean.FALSE);
		abs.put("auto_increment",				Boolean.FALSE);
		abs.put("insert_multirow",				Boolean.FALSE);
		abs.put("functional_index",				Boolean.FALSE);
		abs.put("set_max_rows",					Boolean.TRUE);
        abs.put("set_fetch_size",				Boolean.TRUE);
		abs.put("batch_update",					Boolean.TRUE);
		abs.put("subselect",					Boolean.TRUE);
		abs.put("prefer_max_aggregate",			Boolean.FALSE);
		abs.put("absolute",						Boolean.TRUE);
		abs.put("multiple_results",				Boolean.TRUE);
		abs.put("scroll_autocommit",			Boolean.TRUE);
		abs.put("view",							Boolean.TRUE);
		abs.put("server_cursor",				Boolean.TRUE);
		abs.put("result_limit",					Boolean.FALSE);
        abs.put("order_without_select",         Boolean.TRUE);
		abs.put("multi_table_delete",			Boolean.FALSE);
		abs.put("multi_table_update",			Boolean.FALSE);
		abs.put("index_key_size",				new Integer(0));
		abs.put("like_case_sensitive",			Boolean.TRUE);
        abs.put("fulltext_index",               Boolean.FALSE);
        abs.put("can_set_auto_commit",          Boolean.TRUE);
        abs.put("param_in_in",                  Boolean.TRUE);
	}

	/**	@return an adapter object that is suitable for the given sub-protocol
	 */
	public static DBAdapter get(String databaseId,
								Config config,
								File workingDirectory,
								File dataDirectory)
		throws SQLException
	{
		/**	get property set from global config file		 */
		DBAdapter adapter = (DBAdapter)adapterMap.get(databaseId);
		if (adapter==null) {
			adapter = createAdapter(databaseId,config,workingDirectory,dataDirectory);
			adapterMap.put(databaseId,adapter);
		}
		return adapter;
	}

	private static DBAdapter createAdapter(String databaseId,
										   Config config,
										   File workingDirectory,
										   File dataDirectory)
		throws SQLException
	{
		/*		note that datasources.xml complies JNDI but we need more information from it	*/
		Element ds = config.getDataSource(databaseId);
		if (ds!=null)
			return createAdapter(ds, workingDirectory, dataDirectory);
		else
			throw new RuntimeException("data source "+databaseId+" not found");
	}

	protected static DBAdapter createAdapter(Element dataSource,
											 File workingDirectory, File dataDirectory)
		throws SQLException
	{
		HashMap pmap = new HashMap();
		pmap.put("local", workingDirectory.getAbsolutePath());
		pmap.put("data", dataDirectory.getAbsolutePath());
		pmap.put("osdir", Version.osDir);
		pmap.put("user.name", Version.getSystemProperty("user.name"));

		Properties props = new Properties();

		String displayName = XMLUtil.getChildValue(dataSource,"display-name");
		String jndiName = XMLUtil.getChildValue(dataSource,"jndi-name");
		String adapterClassName = XMLUtil.getChildValue(dataSource,"adapter-class");
		String embedded = XMLUtil.getChildAttributeValue(dataSource,"adapter-class","embedded");
		String mode = XMLUtil.getChildAttributeValue(dataSource,"adapter-class","mode");
		String driverClassName = XMLUtil.getChildValue(dataSource,"driver-class");
        String classPath = XMLUtil.getChildValue(dataSource,"class-path");
		String url = XMLUtil.getChildValue(dataSource,"jdbc-url");

		props.put("display-name",displayName);
		props.put("jndi-name",jndiName);

		NodeList params = dataSource.getElementsByTagName("connection-param");
		for (int i=0; i<params.getLength(); i++) {
			Element param = (Element)params.item(i);
			String name = XMLUtil.getChildValue(param, "param-name");
			String value = XMLUtil.getChildValue(param, "param-value");
			props.put(name, StringUtil.replace(value, pmap));
		}

        props.put("url",StringUtil.replace(url, pmap));

		DBAdapter adapter = null;
		try {

			if (!ClassPathUtil.existsClass(driverClassName) && (classPath!=null))
			{	//	driver not in classpath; don't mind...
				File libDir = new File(Application.theWorkingDirectory,"lib/jdbc");
				ClassPathUtil.addAllToClassPath(libDir, classPath);
			}

			Class adapterClass = Class.forName(adapterClassName);
			adapter = (DBAdapter)adapterClass.newInstance();

			int serverMode = DBAdapter.MODE_EXTERNAL;
			//  for backward compatibility
			if (embedded!=null && embedded.equalsIgnoreCase("true"))    serverMode=DBAdapter.MODE_STANDALONE;
			if (embedded!=null && embedded.equalsIgnoreCase("false"))    serverMode=DBAdapter.MODE_EXTERNAL;
			//  better:
			if (mode!=null && mode.equalsIgnoreCase("embedded"))    serverMode=DBAdapter.MODE_EMBEDDED;
			if (mode!=null && mode.equalsIgnoreCase("standalone"))    serverMode=DBAdapter.MODE_STANDALONE;
			if (mode!=null && mode.equalsIgnoreCase("external"))    serverMode=DBAdapter.MODE_EXTERNAL;

			adapter.init(props, serverMode);

			/**	embedded instances are shut down when the appplication exits	*/

			Class clazz = Class.forName(driverClassName);

		} catch (Exception e) {
			throw new SQLException(e.getClass().getName()+": "+e.getMessage());
		}

		return adapter;
/*
		usually, the driver should be registered with the DriverManager
		but some have problems with newInstance()

		luckily, all drivers registers themselves with a static method
		so that loading the class does the trick ...

//		Driver d = (Driver)clazz.newInstance();
//		DriverManager.registerDriver(d);
*/
	}

	/**
	 * set the process priority
	 */

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	/**
	 * get the Database URL
	 */
	public String getURL()
	{
		return (String)props.get("url");
	}

	/**	create a connection
	 *	@param mode connection mode (READ / READ_WRITE / RECOVER ...)
	 */
	public Connection createConnection(int mode)
		throws SQLException
	{
		if (Version.getSystemProperty("jose.debug.sql",false)) {
            TraceDriverManager.enableTrace(true);
            return TraceDriverManager.getConnection(getURL(),props);
        }
        else
            return DriverManager.getConnection(getURL(), props);
	}

	public int getConnectionId(Connection conn) throws SQLException {
		return -1;
	}

	public String getDatabaseProductName(Connection jdbcConnection)
		throws SQLException
	{
		DatabaseMetaData meta = jdbcConnection.getMetaData();
		return meta.getDatabaseProductName();
	}

	public String getDatabaseProductVersion(Connection jdbcConnection)
		throws SQLException
	{
		DatabaseMetaData meta = jdbcConnection.getMetaData();
		return meta.getDatabaseProductVersion();
	}

	/**	overwrite fo specific databases !	 */
	public String getDBType(String sqlType, String size, String precision)
	{
		if (sqlType.equalsIgnoreCase("CHAR"))
			return "CHAR ("+size+")";
		if (sqlType.equalsIgnoreCase("VARCHAR"))
			return "VARCHAR ("+size+")";
		if (sqlType.equalsIgnoreCase("BIT"))
			return "TINYINT";
			/*	conventions for type BIT are too diverse;
				(Oracle doesn't support it at all)
				use TINYINT instead
			*/
		//	else:
		return sqlType;
	}

	public Properties getProperties()
	{
		return props;
	}

	/**	append additional parameters to CREATE TABLE or INDEX commands
	 */
	public void appendExtras(StringBuffer sql, Properties props)
	{
	}

	/**	retrieve the character encoding that is used by this database
	 * @return a character set identifier
	 */
	abstract public String getCharacterSet(JoConnection conn)
		throws SQLException;

	/**
	 * set whether this database instance is embeded, or if it is an external server
	 */
	public void setServerMode(int mode) {
		abilities.put("mode", new Integer(mode));
	}

	/**
	 * set if the database will be modified
	 */
	public void setDirty(boolean dirty) {
		abilities.put("dirty", new Boolean(dirty));
	}

	/**
	 * shut down the database
	 */
	public void shutDown(JoConnection conn)
	{
		/*	override */
	}

	public final Object getProperty(Object key)		{ return props.get(key); }

	public final String getDisplayName()			{ return (String)getProperty("display-name"); }

	public final String getJNDIName()				{ return (String)getProperty("jndi-name"); }

	/**	test for a certain ability	 */
	public final boolean can(String ability)	{
		Boolean result = (Boolean)abilities.get(ability);
		if (result==null)
			return false;
		else
			return result.booleanValue();
	}

	/**	does the database understand the UNIQUE constraint ?	 */
	public final boolean canUnique()					{ return can("unique"); }
	/**	does the database understand the REFERNCES constraint ?	 */
	public final boolean canReference()					{ return can("reference"); }
	/**	does the database understand the DEFAULT clause 	 */
	public final boolean canDefault()					{ return can("default"); }
	/**	does the database support VIEWs (all but MySQL)	 */
	public final boolean canView()						{ return can("view"); }
	/**	does the database understand the ON DELETE CASCADE constraint ?	 */
	public final boolean canCascadingDelete()			{ return can("cascading_delete"); }
	/**	does the database support DEFAULT AUTOINCREMENT ?	 */
	public boolean canAutoincrement()					{ return can("auto_increment"); }
	/**	can multiple rows be inserted with a single INSERT ?	 */
	public final boolean canInsertMultiRow()			{ return can("insert_multirow"); }
	/**	can the database create function bases indexes ? (only Oracle)	 */
	public final boolean canFunctionalIndex()			{ return can("functional_index"); }
	/**	can the JDBC driver setMaxRows() ? (all but QED and FirstSQL)	 */
	public final boolean canMaxRows()					{ return can("set_max_rows"); }
    /**	can the JDBC driver setFetchSize() ? (all but QED and FirstSQL)	 */
    public final boolean canFetchSize()					{ return can("set_fetch_size"); }
	/**	can the JDBC driver handle batch updates ?	 */
	public final boolean canBatchUpdate()				{ return can("batch_update"); }
	/**	is the method ResultSet.absolute() supported (and not buggy ;-) ?	 */
	public final boolean canAbsolute()					{ return can("absolute"); }
	/**	when looking for the MAX of a column; use aggregate function ? or ORDER BY DESC ?	 */
	public final boolean preferMaxAggregate()			{ return can("prefer_max_aggregate"); }
	/**	is it safe to have multiple result sets opened over the same connection ?
	 *	should be true for every good driver	 */
	public final boolean canMultipleResultSets()		{ return can("multiple_results"); }
    /** is setAutoCommit() supported ?
     *  (all but FirstSQL)
     * */
    public final boolean canSetAutoCommit()             { return can("can_set_auto_commit"); }
	/**	do scrollable result sets work in AutoCommit mode ?
	 *	(this is true for all but Cloudscape)
	 */
	public final boolean canScrollAutocommit() { return can("scroll_autocommit"); }
	/** can we use nested SELECT statements ? (all but MySQL)	*/
	public final boolean canSubselect()			{ return can("subselect"); }
	/**	can we use mult-table delete statements ? (MySQL features, substitue for missing subselects)	*/
	public final boolean canMultiTableDelete()	{ return can("multi_table_delete"); }
	public final boolean canMultiTableUpdate()	{ return can("multi_table_update"); }

	/**	can we use parameter placeholder (?) in IN clauses ?	*/
	public final boolean canParamInIn()     	{ return can("param_in_in"); }

	/**	is it allowed to use columns in ORDER BY that are not in the SELECT clause ? (all but QED)	*/
	public final boolean canOrderWithoutSelect()	{ return can("order_without_select"); }

	/**	is there a quick database dump available
	 * 	(access to embedded server's data files ?)
	 */
	public final boolean canQuickDump()				{ return can("quick_dump"); }

	/**	is the LIKE comparison case sensitive ?
	 * 	true for most databases except MySQl and MS-SQL
	 */
	public final boolean isLikeCaseSensitive()		{ return can("like_case_sensitive"); }

	/**	what is the max. size of index keys ? (0 = n/a)	*/
	public final int getIndexKeySize() {
		Number n = (Number)abilities.get("index_key_size");
		return (n==null) ? 0 : n.intValue();
	}

    /**
     * escape a String literal to be used in a SQL query
     */
    public String escapeForSQL(String str)
    {
        int i = str.indexOf("'");
        if (i >= 0) throw new IllegalArgumentException("don't know how to escape quotes");
        return "'"+str+"'";
    }

    /** can we user CREATE FULLTEXT INDEX
     *  (only MySQL, currently)
     * */
    public final boolean canFulltextIndex()             { return can("fulltext_index"); }

	/**	is this databse instance embedded ? or is it an standalone server ?	 */
	public final int getServerMode()
	{
		Integer serverMode = (Integer)abilities.get("mode");
		if (serverMode==null)
			return MODE_EXTERNAL;
		else
			return serverMode.intValue();
	}


	/**	does the JDBC driver use server-side cursors ? (preferred)	 */
	public final boolean isServerCursor()				{ return can("server_cursor"); }
	public final boolean isClientCursor()				{ return !can("server_cursor"); }

	/**	should result sets be limited ?
	 * 	(set for MySQL)
	 */
	public final boolean useResultLimit()				{ return can("result_limit"); }

	/**	is the databae likel to be modified ?	 */
	public final boolean isDirty()						{ return can("dirty"); }

	public String escapeSql(String sql)
	{
		return StringUtil.replace(sql, abilities);
	}

	/**
	 * disable constraints for a table (e.g. before bulk inserts)
	 */
	public void disableConstraints(String table, JoConnection conn) throws SQLException {
		/**	overwrite !	*/
	}

	/**
	 * enable constraints for a table (e.g. after bulk inserts)
	 */
	public void enableConstraints(String table, JoConnection conn) throws SQLException {
		/**	overwrite !	*/
	}

	/**
	 * flush: release database resources as much as possible
	 * this is a noop, overwritten by MySQLAdapter
	 */
	public void flushResources(JoConnection conn) throws SQLException
	{
		/** no-op   */
	}

	public boolean cancelQuery(JoConnection conn) throws SQLException
	{
		return false;
	}

	/**
	 * append an bitwise OR operation
	 * the default implementation is:
	 * 		FLOOR(a / 2*b)*2*b + b + MOD(a,b)
	 * overwrite if the database has Bitwise functions (most have, but the syntax differs)
	 */
	public StringBuffer appendBitwiseOr(StringBuffer buf, String a, int b)
	{
		buf.append("FLOOR(");
		buf.append(a);
		buf.append("/");
		buf.append(2*b);
		buf.append(")*");
		buf.append(2*b);
		buf.append("+");
		buf.append(b);
		buf.append("+MOD(");
		buf.append(a);
		buf.append(",");
		buf.append(b);
		buf.append(")");
		return buf;

	}

	/**
	 * append an bitwise OR operation
	 * the default implementation is:
	 * 		FLOOR(a / 2*b)*2*b + MOD(a,b)
	 * overwrite if the database has Bitwise functions (most have, but the syntax differs)
	 */
	public StringBuffer appendBitwiseNot(StringBuffer buf, String a, int b)
	{
		buf.append("FLOOR(");
		buf.append(a);
		buf.append("/");
		buf.append(2*b);
		buf.append(")*");
		buf.append(2*b);
		buf.append("+MOD(");
		buf.append(a);
		buf.append(",");
		buf.append(b);
		buf.append(")");
		return buf;

	}

	/**
	 * append an bitwise test operation
	 * the default implementation is
	 * 		MOD(a,2*b) >= b
	 * 		MOD(a,2*b) < b
	 * overwrite if the database has Bitwise functions (most have, but the syntax differs)
	 */
	public StringBuffer appendBitwiseTest(StringBuffer buf, String a, int b, boolean testTrue)
	{
		buf.append("MOD(");
		buf.append(a);
		buf.append(",");
		buf.append(2*b);
		buf.append(") ");
		if (testTrue)
			buf.append(">=");
		else
			buf.append("<");
		buf.append(b);
		return buf;
	}


	public synchronized void bootstrap(Connection conn) throws SQLException
	{
		JoConnection jconn = new JoConnection(conn,"bootstrap");
		Setup setup = new Setup(Application.theApplication.theConfig, jconn);
		try {
			/** create database */
			setup.setup("META",Setup.WITH_CONSTRAINTS);
            setup.setup("MAIN",Setup.WITH_CONSTRAINTS);
//            setup.setup("IO",Setup.WITH_CONSTRAINTS);
//            setup.setup("IO_MAP",Setup.WITH_CONSTRAINTS);

            if (Version.unix) {
                /** make sure that database files are globally accessible    */
	            FileUtil.chmod("777",Application.theDatabaseDirectory+"/mysql");
                FileUtil.chmod("777",Application.theDatabaseDirectory+"/mysql/jose");
                FileUtil.chmodAll("666",Application.theDatabaseDirectory+"/mysql/jose");
            }

			File starter = new File(Application.theDatabaseDirectory,"starter.pgn");
			if (starter.exists()) {
				PGNImport importer = PGNImport.openFile(starter,Integer.MAX_VALUE); 
				String starterName = Language.get("collection.starter");

				Collection coll = Collection.readCollection(jconn,importer.getCollectionId());
				coll.renameTo(starterName);
			}

        } catch (Exception ex) {
			if (ex instanceof SQLException)
				throw (SQLException)ex;
			else
				throw new SQLException(ex.getMessage());
		} 
	}

	public synchronized void checkIntegrity(JoConnection conn) throws SQLException
	{
		Task task = new CheckDBTask(conn);
		task.setSilentTime(5000);
		task.startLater(2000);
	}

	public synchronized void crossoverSchema(JoConnection connection, String schema, int version, Config config) throws SQLException
    {
        if (schema.equalsIgnoreCase("MAIN"))
        {
	        if (version < 1002)
		        try {
			        version = CrossOver1002.crossOver(version,connection,config);
		        } catch (Exception e) {
			        Application.error(e);
		        }
            if (version < 1004)
                try {
                    version = CrossOver1004.crossOver(version,connection,config);
                } catch (Exception e) {
                    Application.error(e);
                }
	        if (version < 1005)
		        try {
			        version = CrossOver1005.crossOver(version,connection,config);
		        } catch (Exception e) {
			        Application.error(e);
		        }
	        if (version < 1006)
	            try {
		            version = CrossOver1006.crossOver(version,connection,config);
	            } catch (Exception e) {
		            Application.error(e);
	            }
	        if (version < 1007)
	            try {
		            version = CrossOver1007.crossOver(version,connection,config);
	            } catch (Exception e) {
		            Application.error(e);
	            }
			if (version < 1008)
				try {
					version = Crossover1008.crossOver(version,connection,config);
				} catch (Exception e) {
					Application.error(e);
				}
        }

	    if (schema.equalsIgnoreCase("META"))
	    {
		    if (version < 1001)
		    try {
			    version = CrossOverMeta1001.crossOver(version,connection,config);
		    } catch (Exception ex) {
			    Application.error(ex);
		    }
	    }

        if (schema.equalsIgnoreCase("IO") || schema.equalsIgnoreCase("IO_MAP"))
        {
            // schemas IO and IO_MAP need not be maintained; we just create them from scratch
            Setup setup = new Setup(config,schema,connection);
            try {
                setup.drop(true);
            } catch (Exception e) {
                Application.error(e);
            }
        }
	}

	/**
	 * Launch process
	 * @return
	 */
	protected java.util.List<Command> deferredActions = new ArrayList<>();

	public void launchProcess(boolean boostrap) {
	}

	public boolean launchComplete() {
		return true;
	}

	public boolean postAfterLaunch(Command cmd) {
		if (launchComplete()) {
			// dispatch Command to Application event handler
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					CommandListener target = cmd.target;
					if (target==null) target = Application.theApplication;
					Application.theCommandDispatcher.handle(cmd,target);
				}
			});
			return true;
		}
		else{
			//	put into deferrred actions. Try again when the server is launched.
			deferredActions.add(cmd);
			return false;
		}
	}
}
