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

import de.jose.Application;
import de.jose.Config;
import de.jose.util.xml.XMLUtil;
import de.jose.util.xml.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Copies data from one database schema to another
 * 
 */

public class Copy
		implements Comparator
{
	protected Element schema;
	protected JoConnection source;
	protected JoConnection target;
	
	protected HashMap refMap;
	protected SortedSet tables;
	
	public void init (Config cfg, Element sch,
					  File workingDirectory,
					  File dataDirectory,
					  String datasrc, String datatrg)
		throws Exception
	{
		schema = sch;
		DBAdapter srcAdapter = DBAdapter.get(datasrc,cfg,workingDirectory,dataDirectory);
		DBAdapter trgAdapter = DBAdapter.get(datatrg,cfg,workingDirectory,dataDirectory);

		source = new JoConnection(srcAdapter, "db.read");
		target = new JoConnection(trgAdapter, "db.write");
		
		refMap = referenceMap(schema);
		//	get a list of tables, sorted by references
		tables = new TreeSet(this);
		tables.addAll(refMap.keySet());
	}

	public void copyAllTables()
		throws SQLException
	{
		Iterator i = tables.iterator();
		while (i.hasNext())
			copyTable((String)i.next());
	}
	
	public void postProcess()
		throws SQLException
	{
	}
	
	public void copyTable(String tableName)
		throws SQLException
	{
		System.out.println("start copying "+tableName);
		
		Element table = Config.getTable(schema,tableName);
		int columnCount = XMLUtil.countChildren(table,"COLUMN");
		
		String sqlSelect = "SELECT * FROM "+tableName;
		
		StringBuffer sqlInsert = new StringBuffer();
		sqlInsert.append("INSERT INTO ");
		sqlInsert.append(tableName);
		sqlInsert.append(" VALUES (");
		for (int i=0; i<columnCount; i++) {
			if (i>0) sqlInsert.append(",");
			sqlInsert.append("?");
		}
		sqlInsert.append(")");
		
		JoPreparedStatement select = source.getPreparedStatement(sqlSelect);
		JoPreparedStatement insert = target.getPreparedStatement(sqlInsert.toString());
		int count = 0;
		
		//	we don't bother about batch and multi-row updates ...
		try {
			select.execute();
			
			ResultSet res = select.getResultSet();
			ResultSetMetaData rmd = null;
		
			for (count=0; res.next(); count++)
			{
				if (rmd==null) rmd = res.getMetaData();
				insert.clearParameters();
				
				for (int j=1; j<=columnCount; j++)
				{
					Object obj = res.getObject(j);
					insert.setObject(j, obj, rmd.getColumnType(j));
				}
					
				insert.execute();
			}
		} finally {
			select.closeResult();
		}
		
		System.out.println(count+" rows copied from "+tableName);
	}
	
	public void close()
	{
		if (source!=null) source.close();
		if (target!=null) target.close();
	}
	
	//	implements comparator
	public int compare(Object obj1, Object obj2)
	{
		String a = obj1.toString();
		String b = obj2.toString();
		
		if (references(a,b)) return Integer.MAX_VALUE;
		if (references(b,a)) return Integer.MIN_VALUE;
		
		return a.compareTo(b);
	}
	
	protected boolean references(String a, String b)
	{
		Set s = (Set)refMap.get(a);
		return (s!=null) && s.contains(b);
	}

	public static void main(String[] args)
		throws Exception
	{
		new Copy().process(args);
	}
	
	public void process(String[] args)
	{
		try {
			//	working directory
			long time = System.currentTimeMillis();
			File wd = Application.getWorkingDirectory();
			File dd = new File("database");
			boolean copy = true;
			boolean post = true;
			
			String datasrc = null;		//	data source
			String datatrg = null;		//	data target
			String schemaName = "MAIN"; // default

			for (int i=0; i<args.length; i++) 
				if (args[i].equalsIgnoreCase("-wd"))
					wd = new File(args[++i]);
				else if (args[i].equalsIgnoreCase("-dd"))
					dd = new File(args[++i]);
				else if (args[i].equalsIgnoreCase("-from"))
					datasrc = args[++i];
				else if (args[i].equalsIgnoreCase("-to"))
					datatrg = args[++i];
				else if (args[i].equalsIgnoreCase("-schema"))
					schemaName = args[++i];
				else if (args[i].equalsIgnoreCase("-copy")) {
					copy = true;
					post = false;
				}
				else if (args[i].equalsIgnoreCase("-post")) {
					copy = false;
					post = true;
				}
		
			if (datasrc==null || datatrg==null)
				printHelp();
		
			Config config = new Config(new File(wd,"config"));
			Element schema = config.getSchema(schemaName);

			init(config, schema, wd,dd, datasrc, datatrg);
		
			System.out.println("connected to "+source.getAdapter().getURL());
			System.out.println("and "+target.getAdapter().getURL());
		
			System.out.println(tables);
			
			if (copy)
				copyAllTables();
			if (post)
				postProcess();
			
			time = System.currentTimeMillis()-time;
			System.out.println("finished "+(time/60000)+" minutes");
			
		} catch (SQLException sqex) {
			for ( ; sqex!=null; sqex = sqex.getNextException())
				Application.error(sqex);
		} catch (Exception ex) {
			Application.error(ex);
		} finally {
			close();
			System.exit(0);
		}
	}
	
	public static final void printHelp() 
	{
		PrintStream o = System.out;
		o.print("java ");
		o.print(Copy.class.getName());
		o.println(" [-wd] -from -to [-schema]");
		o.println("  Utilitiy for copying a Jose Database ");
		o.println();
		o.println(" -wd <working directory>     default = current directory ");
		o.println(" -from <data source>         data source (as defined in datasources.xml)");
		o.println(" -to <target>                data source (as defined in datasources.xml)");
		o.println(" -schema <schema>            database schema ");
		System.exit(-1);
	}
	
	protected static HashMap referenceMap(Element schema)
	{
		HashMap map = new HashMap();
		NodeList tables = schema.getElementsByTagName("TABLE");
		for (int i=0; i<tables.getLength(); i++)
		{
			Element table = (Element)tables.item(i);
			String tableName = XMLUtil.getChildValue(table, "NAME");
			
			HashSet foreign = new HashSet();
			map.put(tableName,foreign);
			
			//	look for foreign keys
			NodeList columns = table.getElementsByTagName("COLUMN");
			for (int j=0; j<columns.getLength(); j++)
			{
				Element column = (Element)columns.item(j);
				String ref = XMLUtil.getChildValue(column, "REFERENCES");
				if (ref != null && ref.length() > 0)
				{
					int k = ref.indexOf("(");
					if (k<0) k = ref.length();
					String foreignTableName = ref.substring(0,k).trim();
					foreign.add(foreignTableName);
				}
			}
		}
		return map;
	}
}
