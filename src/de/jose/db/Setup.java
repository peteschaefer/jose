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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 *	sets up the jose database
 */

public class Setup
{
	protected Properties props;
	protected Element schema;
	protected JoConnection connection;
	protected PrintWriter out;
    protected Config config;

    /** indicates a full-text index */
    private static final int FULLTEXT_INDEX = -1;

	public static final boolean WITH_CONSTRAINTS        = true;
	public static final boolean WITHOUT_CONSTRAINTS     = false;

	public static final boolean IGNORE_ERRORS           = true;
	public static final boolean REPORT_ERRORS           = false;

	public static String DEFAULT_CHARSET = "utf8";
	public static String DEFAULT_COLLATE = "utf8_general_ci";

	public Setup(Config cfg, String schemaName,
				 File workingDirectory, File dataDirectory,
				 String datasource)
		throws Exception
	{
		JoConnection.init(cfg,workingDirectory,dataDirectory,datasource,1);
		init(cfg, schemaName, JoConnection.get());
	}

	public Setup(Config cfg, String schemaName, JoConnection conn)
	{
		init(cfg,schemaName,conn);
	}

    public Setup(Config cfg, JoConnection conn)
    {
        init(cfg,null,conn);
    }

	public void setOutput(PrintWriter output)
	{
		out = output;
	}

	protected void init(Config cfg, String schemaName, JoConnection conn)
	{
		if (schemaName == null) schemaName = "MAIN";
        config = cfg;
		schema = config.getSchema(schemaName);

		connection = conn;
		props = JoConnection.getAdapter().getProperties();
		setOutput(new PrintWriter(System.err,true));
	}

	public void put(String key, Object value)
	{
		props.put(key,value);
	}

    public void setup(String schemaName, boolean withConstraints) throws Exception
    {
        setSchema(schemaName);
        createTables(null,null,withConstraints);

	    //	store meta information
	    NodeList tables = schema.getElementsByTagName("TABLE");
	    NodeList views = schema.getElementsByTagName("VIEW");

	    insertVersion(connection,"SCHEMA_VERSION",
				      schemaName,
				      null,
				      XMLUtil.getChildIntValue(schema,"VERSION"));

	    for (int i=0; i<tables.getLength(); i++) {
		    insertVersion(connection,"TABLE_VERSION",
					      schemaName,
					      XMLUtil.getChildValue((Element)tables.item(i),"NAME"),
					      XMLUtil.getChildIntValue((Element)tables.item(i), "VERSION"));
	    }
	    if (JoConnection.getAdapter().canView()) {
		    for (int i=0; i<views.getLength(); i++) {
			    insertVersion(connection,"VIEW_VERSION",
						      schemaName,
						      XMLUtil.getChildValue((Element)views.item(i),"NAME"),
						      XMLUtil.getChildIntValue((Element)views.item(i), "VERSION"));
		    }
	    }
    }

	public void createTables(String catalogName, String format, boolean withConstraints)
		throws Exception
	{
		NodeList tables = schema.getElementsByTagName("TABLE");
		//	create the tables
		for (int i=0; i<tables.getLength(); i++)
			createTable(catalogName, (Element)tables.item(i), format, withConstraints);

		NodeList views = schema.getElementsByTagName("VIEW");
		if (JoConnection.getAdapter().canView()) {
			//	create the views
			for (int i=0; i<views.getLength(); i++)
				createView(catalogName, (Element)views.item(i));
		}
	}

    public List getTables(String schemaName, boolean withViews)
    {
        if (schemaName == null) schemaName = "MAIN";
        schema = config.getSchema(schemaName);

        List result = new ArrayList();
        NodeList tables = schema.getElementsByTagName("TABLE");
        //	create the tables
        for (int i=0; i<tables.getLength(); i++) {
            Element elem = (Element)tables.item(i);
            String name = XMLUtil.getChildValue(elem, "NAME");
            result.add(name);
        }

        if (withViews) {
            NodeList views = schema.getElementsByTagName("VIEW");
            if (JoConnection.getAdapter().canView()) {
                //	create the views
                for (int i=0; i<views.getLength(); i++) {
                    Element elem = (Element)tables.item(i);
                    String name = XMLUtil.getChildValue(elem, "NAME");
                    result.add(name);
                }
            }
        }

        return result;
    }

    public void clear(String schemaName)
        throws Exception
    {
        if (schemaName == null) schemaName = "MAIN";
        schema = config.getSchema(schemaName);

        NodeList tables = schema.getElementsByTagName("TABLE");
        //	clear the tables
        for (int i=0; i<tables.getLength(); i++)
            clearTable((Element)tables.item(i));
    }

    public void clearTable(Element table) throws Exception
    {
        /** create if not already exists    */
        if (!exists(table)) {
            createTable(null,table,null,true);
        }
        else {
            String name = XMLUtil.getChildValue(table, "NAME");

            /** empty   */
            String sql = "DELETE FROM "+name;
            connection.executeUpdate(sql);
        }
    }

    public void dropIndexes(String schemaName)
        throws Exception
    {
        if (schemaName == null) schemaName = "MAIN";
        schema = config.getSchema(schemaName);

        NodeList tables = schema.getElementsByTagName("TABLE");
        //	clear the tables
        for (int i=0; i<tables.getLength(); i++)
            dropIndexes(null,(Element)tables.item(i));
    }

	public void dropIndexes(String catalogName, String tableName)
			throws Exception
    {
		Element table = getTable(tableName);
		dropIndexes(catalogName, table);
	}

    public void dropIndexes(String catalogName, Element table) throws Exception
    {
        String tableName = XMLUtil.getChildValue(table, "NAME");
	    if (catalogName!=null) tableName = catalogName+"."+tableName;

        int idxCount = 1;

        NodeList entries = table.getChildNodes();
        for (int i=0; i<entries.getLength(); i++) {
            Node entry = entries.item(i);
            if (entry.getNodeName().equals("COLUMN")) {
                Element singleIndex = XMLUtil.getChild((Element)entry,"INDEX");
                if (singleIndex != null)
                    dropIndex(tableName,idxCount++);
            }
        }

        entries = table.getChildNodes();
        for (int i=0; i<entries.getLength(); i++) {
            Node entry = entries.item(i);
            if (entry.getNodeName().equals("INDEX")) {
                dropIndex(tableName,idxCount++);
            }
        }

	    connection.executeUpdate("ALTER TABLE "+tableName+" DROP PRIMARY KEY");
    }

    public void createIndexes(String schemaName)
        throws Exception
    {
        if (schemaName == null) schemaName = "MAIN";
        schema = config.getSchema(schemaName);

        NodeList tables = schema.getElementsByTagName("TABLE");
        //	clear the tables
        for (int i=0; i<tables.getLength(); i++)
            createIndexes((Element)tables.item(i));
    }


    protected void createIndexes(Element table)
        throws SQLException, SAXException
    {
        String tableName = XMLUtil.getChildValue(table, "NAME");

        int idxCount = 1;
        NodeList entries = table.getChildNodes();
        for (int i=0; i<entries.getLength(); i++) {
            Node entry = entries.item(i);
            if (entry.getNodeName().equals("COLUMN")) {
                Element column = (Element)entry;
                Element singleIndex = XMLUtil.getChild(column,"INDEX");
                if (singleIndex != null) {
                    String columnName = XMLUtil.getChildValue(column,"NAME");

                    int keySize=0;
                    String keyStr = singleIndex.getAttribute("keysize");
                    if ((keyStr != null) && (keyStr.length() > 0))
                        keySize = Integer.parseInt(keyStr);

                    String type = singleIndex.getAttribute("type");
                    if ((type != null) && "full-text".equalsIgnoreCase(type))
                        keySize = FULLTEXT_INDEX;

                    createSingleIndex(null, tableName, columnName, idxCount++, keySize);
                }
            }
        }

        entries = table.getChildNodes();
        for (int i=0; i<entries.getLength(); i++) {
            Node entry = entries.item(i);
            if (entry.getNodeName().equals("INDEX")) {
                createIndex(null,tableName, (Element)entry, idxCount++);
            }
        }
    }

	public void createIndex(Element table, int idxOffset)
	    throws SQLException, SAXException
	{
	    String tableName = XMLUtil.getChildValue(table, "NAME");

	    int idxCount = 1;
	    NodeList entries = table.getChildNodes();
	    for (int i=0; i<entries.getLength(); i++) {
	        Node entry = entries.item(i);
	        if (entry.getNodeName().equals("COLUMN")) {
	            Element column = (Element)entry;
	            Element singleIndex = XMLUtil.getChild(column,"INDEX");
	            if (singleIndex != null) {
	                String columnName = XMLUtil.getChildValue(column,"NAME");

	                int keySize=0;
	                String keyStr = singleIndex.getAttribute("keysize");
	                if ((keyStr != null) && (keyStr.length() > 0))
	                    keySize = Integer.parseInt(keyStr);

	                String type = singleIndex.getAttribute("type");
	                if ((type != null) && "full-text".equalsIgnoreCase(type))
	                    keySize = FULLTEXT_INDEX;

		            if (idxCount++==idxOffset)
		                createSingleIndex(null, tableName, columnName, idxCount, keySize);
	            }
	        }
	    }

	    entries = table.getChildNodes();
	    for (int i=0; i<entries.getLength(); i++) {
	        Node entry = entries.item(i);
		    if (entry.getNodeName().equals("INDEX") && idxCount++ == idxOffset)
			    createIndex(null, tableName, (Element) entry, idxCount);
	    }
	}

	public void dropPrimaryKey(String tableName) throws SQLException
	{
		connection.executeUpdate("ALTER IGNORE TABLE "+tableName+" DROP PRIMARY KEY");
	}


    public void dropIndex(String tableName, int idx) throws SQLException
    {
        connection.executeUpdate("ALTER IGNORE TABLE "+tableName+
		                        " DROP INDEX "+tableName+"_"+idx);
    }


	public void close()
	{
		if (connection!=null) connection.close();
	}

	public void setSchema(String schemaName)
	{
		if (schemaName == null) schemaName = "MAIN";
		schema = config.getSchema(schemaName);
	}

    public void drop(String schemaName, boolean ignoreErrors) throws Exception
    {
	    setSchema(schemaName);
        drop(ignoreErrors);
    }

	public void drop(boolean ignoreErrors)
		throws Exception
	{
		NodeList views = schema.getElementsByTagName("VIEW");
		//	drop the views
		for (int i=views.getLength()-1; i>=0; i--)
			if (JoConnection.getAdapter().canView())
				try {
					dropView((Element)views.item(i));
				} catch (SQLException sqlex) {
					if (!ignoreErrors) Application.error(sqlex);
				}

		NodeList tables = schema.getElementsByTagName("TABLE");
		//	drop the tables
		for (int i=tables.getLength()-1; i>=0; i--)
			try {
				dropTable((Element)tables.item(i));
			} catch (SQLException sqlex) {
				if (!ignoreErrors) Application.error(sqlex);
			}
        //  drop metainfo entry
        dropVersion(connection,"SCHEMA_VERSION",XMLUtil.getChildValue(schema,"NAME"),null);
	}

    protected boolean exists(Element table)
    {
        String name = XMLUtil.getChildValue(table, "NAME");
        JoStatement stm = null;
        try {
            stm = new JoStatement(connection);
            stm.execute("SELECT 1 FROM "+name+" WHERE 1=0");
            return true;
        } catch (SQLException ex) {
            //  table does not exist
        } finally {
            if (stm!=null) stm.close();
        }
        return false;
    }

	public String createTable(String catalogName, String tableName, String format, boolean withConstraints)
			throws SQLException, SAXException
	{
		Element table = getTable(tableName);
		return createTable(catalogName,table,format,withConstraints);
	}

	public String createTable(String catalogName,  Element table, String format, boolean withConstraints)
		throws SQLException, SAXException
	{
		StringBuffer sql = new StringBuffer();
		String tableName = XMLUtil.getChildValue(table, "NAME");

		sql.append("CREATE TABLE ");
		if (catalogName!=null) {
			sql.append(catalogName);
			sql.append(".");
		}
		sql.append(tableName);

		sql.append(" (");

		Vector singleColumnIndex = new Vector();	//	of String
		int idxCount = 1;

		boolean any = false;
		NodeList entries = table.getChildNodes();
		for (int i=0; i<entries.getLength(); i++) {
			Node entry = entries.item(i);
			if (entry.getNodeName().equals("COLUMN")) {
				if (any) sql.append(", ");
				appendColumnDefinition(sql, (Element)entry, singleColumnIndex, withConstraints, null,null);
				any = true;
			}
			else if (withConstraints &&
			         entry.getNodeName().equals("UNIQUE") &&
			         JoConnection.getAdapter().canUnique())
			{
                if (any) sql.append(", ");
                sql.append(" UNIQUE (");
                sql.append(XMLUtil.getTextValue(entry));
                sql.append(")");
                any = true;
			}
		}

		sql.append(") ");

		if (format!=null) {
			sql.append(" ENGINE=");
			sql.append(format);
		}

		sql.append(" CHARACTER SET "+DEFAULT_CHARSET);
		sql.append(" COLLATE "+DEFAULT_COLLATE);

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
//		out.println("--------------------------------------------------------");
		connection.executeUpdate(sql.toString());

		if (withConstraints)
			for (int i=0; i<singleColumnIndex.size(); i++) {
				String columnName = (String)singleColumnIndex.get(i);
				int keySize = 0;
				if ((i+1) < singleColumnIndex.size()) {
					Object o = singleColumnIndex.get(i+1);
					if (o instanceof Number) {
						keySize = ((Number)o).intValue();
						i++;
					}
				}
				createSingleIndex(catalogName,tableName,columnName,  idxCount++, keySize);
			}

        entries = table.getChildNodes();
        for (int i=0; i<entries.getLength(); i++) {
            Node entry = entries.item(i);
            if (entry.getNodeName().equals("INITIAL")) {

                sql = new StringBuffer();
                sql.append("INSERT INTO ");
	            if (catalogName!=null) {
					sql.append(catalogName);
					sql.append(".");
	            }
                sql.append(tableName);
                sql.append(" VALUES (");
                sql.append(XMLUtil.getTextValue(entry));
                sql.append(")");

                out.println(sql.toString());
                connection.executeUpdate(sql.toString());
            }
            else if (withConstraints && entry.getNodeName().equals("INDEX")) {
                createIndex(catalogName,tableName, (Element)entry, idxCount++);
            }
        }
//		out.println("table "+name+" sucessfully created");
        return tableName;
	}

    public Element getTable(String tableName)
    {
        NodeList tables = schema.getElementsByTagName("TABLE");
        for (int i=0; i<tables.getLength(); i++) {
            Element elem = (Element)tables.item(i);
            String name = XMLUtil.getChildValue(elem, "NAME");
            if (name.equalsIgnoreCase(tableName))
                return elem;
        }
        return null;
    }

    public Element getColumn(Element table, String columnName)
    {
        NodeList columns = table.getElementsByTagName("COLUMN");
        for (int i=0; i<columns.getLength(); i++) {
            Element elem = (Element)columns.item(i);
            String name = XMLUtil.getChildValue(elem, "NAME");
            if (name.equalsIgnoreCase(columnName))
                return elem;
        }
        return null;
    }

	public String[] getColumnNames(String tableName)
	{
		Element table = getTable(tableName);
	    NodeList columns = table.getElementsByTagName("COLUMN");
		String[] result = new String[columns.getLength()];

	    for (int i=0; i<columns.getLength(); i++) {
	        Element elem = (Element)columns.item(i);
	        result[i] = XMLUtil.getChildValue(elem, "NAME");
	    }
	    return result;
	}

	public boolean existsColumn(String tableName, String columnName) throws SQLException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SHOW COLUMNS FROM ");
		sql.append(tableName);
		sql.append(" LIKE '");
		sql.append(columnName);
		sql.append("' ");

		Statement stm = null;
		try {
			stm = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			ResultSet res = stm.executeQuery(sql.toString());
			return res.next();
		} finally {
			if (stm!=null) stm.close();
		}
	}


	public void dropColumn(String tableName, String columnName)
			throws SQLException
	{
		if (existsColumn(tableName,columnName)) {
			StringBuffer sql = new StringBuffer();
			sql.append("ALTER IGNORE TABLE ");
			sql.append(tableName);
			sql.append(" DROP COLUMN ");
			sql.append(columnName);
			connection.executeUpdate(sql.toString());
		}
	}

    public int addColumn(String tableName, String columnName, int idxCount) throws SQLException, SAXException
    {
        Element table = getTable(tableName);
        Element column = getColumn(table,columnName);

        StringBuffer sql = new StringBuffer();
        sql.append("ALTER TABLE ");
        sql.append(tableName);
        sql.append(" ADD COLUMN (");

        Vector singleColumnIndex = new Vector();	//	of String

        appendColumnDefinition(sql, column, singleColumnIndex, true, DEFAULT_CHARSET,DEFAULT_COLLATE);

        sql.append(")");

        JoConnection.getAdapter().appendExtras(sql, props);

        out.println(sql.toString());
//		out.println("--------------------------------------------------------");
        connection.executeUpdate(sql.toString());

        for (int i=0; i<singleColumnIndex.size(); i++) {
            int keySize = 0;
            if ((i+1) < singleColumnIndex.size()) {
                Object o = singleColumnIndex.get(i+1);
                if (o instanceof Number) {
                    keySize = ((Number)o).intValue();
                    i++;
                }
            }
            createSingleIndex(null, tableName, columnName,  idxCount++, keySize);
        }
        return idxCount;
    }

	protected String createView(String catalogName, Element view)
		throws SQLException, SAXException
	{
		StringBuffer sql = new StringBuffer();
		String name = XMLUtil.getChildValue(view, "NAME");
		String definition = XMLUtil.getChildValue(view, "DEFINITION");

		sql.append("CREATE VIEW ");
		if (catalogName!=null) {
			sql.append(catalogName);
			sql.append(".");
		}
		sql.append(name);
		sql.append(" AS (");
		sql.append(definition);
		sql.append(")");

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
//		out.println("--------------------------------------------------------");
		connection.executeUpdate(sql.toString());

//		out.println("table "+name+" sucessfully created");
        return name;
	}

	public String createIndex(String catalogName, String tableName, Element def, int idxName)
		throws SQLException
	{
		String functional = def.getAttribute("functional");
		if (functional != null
			&& functional.equalsIgnoreCase("true")
			&& !JoConnection.getAdapter().canFunctionalIndex())
			return null;
			/*	can't create functional index */

        String type = def.getAttribute("type");
        boolean full_text = (type!=null) && "full-text".equalsIgnoreCase(type);

		StringBuffer sql = new StringBuffer();
		sql.append("CREATE ");
        if (full_text)
            sql.append(" FULLTEXT ");
        sql.append(" INDEX ");
		sql.append(tableName);
		sql.append("_");
		sql.append(idxName);
		sql.append(" ON ");
		if (catalogName!=null) {
			sql.append(catalogName);
			sql.append(".");
		}
		sql.append(tableName);
		sql.append("(");

		NodeList columns = def.getElementsByTagName("COLUMN");
		for (int i=0; i<columns.getLength(); i++) {
			if (i>0) sql.append(",");
			String colName = XMLUtil.getTextValue(columns.item(i));
			sql.append(colName);
		}
		sql.append(")");

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
		connection.executeUpdate(sql.toString());
        return tableName+"_"+idxName;
	}

	protected void createSingleIndex(String catalogName, String tableName, String columnName, int idxName, int keySize)
		throws SQLException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE ");
        if (keySize==FULLTEXT_INDEX && JoConnection.getAdapter().canFulltextIndex())
            sql.append(" FULLTEXT ");
		//	TODO is this syntax supported with MySQL 4.1 ?
        sql.append(" INDEX ");
		sql.append(tableName);
		sql.append("_");
		sql.append(idxName);
		sql.append(" ON ");
		if (catalogName!=null) {
			sql.append(catalogName);
			sql.append(".");
		}
		sql.append(tableName);
		sql.append("(");
		sql.append(columnName);
		if (keySize > 0) {
			//	key size required
			int maxKeySize = JoConnection.getAdapter().getIndexKeySize();
			if (maxKeySize > 0) {
				sql.append("(");
				sql.append(Math.min(keySize,maxKeySize));
				sql.append(")");
			}
		}
		sql.append(")");

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
		connection.executeUpdate(sql.toString());
	}

	protected void dropTable(Element table)
		throws SQLException, SAXException
	{
		StringBuffer sql = new StringBuffer();
		String name = XMLUtil.getChildValue(table, "NAME");

		sql.append("DROP TABLE ");
		sql.append(name);

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
		connection.executeUpdate(sql.toString());

		dropVersion(connection,"TABLE_VERSION", XMLUtil.getChildValue(schema,"NAME"), name);
	}

	public void dropTable(String tableName)
		throws SQLException
	{
		connection.executeUpdate("DROP TABLE IF EXISTS "+tableName);
		dropVersion(connection,"TABLE_VERSION", XMLUtil.getChildValue(schema,"NAME"), tableName);
	}

	protected void dropView(Element view)
		throws SQLException, SAXException
	{
		StringBuffer sql = new StringBuffer();
		String name = XMLUtil.getChildValue(view, "NAME");

		sql.append("DROP VIEW ");
		sql.append(name);

		JoConnection.getAdapter().appendExtras(sql, props);

		out.println(sql.toString());
		connection.executeUpdate(sql.toString());

		dropVersion(connection,"VIEW_VERSION", XMLUtil.getChildValue(schema,"NAME"), name);
	}

	protected void appendColumnDefinition(StringBuffer sql, Element column,
	                                      Vector indexColumns, boolean withConstraints,
	                                      String charset, String collate)
		throws SAXException
	{
		String name = XMLUtil.getChildValue(column,"NAME");
		String type = XMLUtil.getChildValue(column,"TYPE");

		if (name==null) throw new SAXException("NAME expected");
		if (type==null) throw new SAXException("TYPE expected");

		String size = XMLUtil.getChildValue(column,"SIZE");	//	optional
		String precision = XMLUtil.getChildValue(column,"PRECISION"); // optional
		String defaultValue = XMLUtil.getChildValue(column,"DEFAULT"); // optional
		String references = XMLUtil.getChildValue(column,"REFERENCES"); // optional
		String column_collate = XMLUtil.getChildValue(column, "COLLATE");  //  optional
		if (column_collate!=null) collate = column_collate;

		boolean notNull = XMLUtil.existsChild(column,"NOT_NULL");
		boolean primaryKey = XMLUtil.existsChild(column,"PRIMARY_KEY");
		boolean unique = XMLUtil.existsChild(column,"UNIQUE");
		boolean onDeleteCascade = XMLUtil.existsChild(column,"ON_DELETE_CASCADE");

		sql.append(name);
		sql.append(" ");
		sql.append(JoConnection.getAdapter().getDBType(type.toUpperCase(), size, precision));
		if (withConstraints && defaultValue!=null && JoConnection.getAdapter().canDefault()) {
			sql.append(" DEFAULT ");
			sql.append(defaultValue);
		}
        if (withConstraints && notNull)
            sql.append(" NOT NULL ");
        if (withConstraints && primaryKey)
            sql.append(" PRIMARY KEY ");
        if (withConstraints && unique && JoConnection.getAdapter().canUnique())
            sql.append(" UNIQUE ");
        if (withConstraints && references!=null && JoConnection.getAdapter().canReference()) {
            sql.append(" REFERENCES ");
            sql.append(references);
        }
        if (withConstraints && onDeleteCascade && JoConnection.getAdapter().canCascadingDelete())
            sql.append(" ON DELETE CASCADE ");

		if (charset!=null &&
		      (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("CHAR") || type.equalsIgnoreCase("LONGVARCHAR")))
		{
			sql.append(" CHARACTER SET "+charset);
			sql.append(" COLLATE "+collate);
		}


        Element singleIndex = XMLUtil.getChild(column,"INDEX");
        if (withConstraints && singleIndex != null) {
            indexColumns.add(name);
            String keySize = singleIndex.getAttribute("keysize");
            if ((keySize != null) && (keySize.length() > 0))
                indexColumns.add(new Integer(keySize));

            type = singleIndex.getAttribute("type");
            if ((type != null) && "full-text".equalsIgnoreCase(type))
                indexColumns.add(new Integer(FULLTEXT_INDEX));
        }
	}

	/**
	 * insert version information
	 */
	protected static void insertVersion(JoConnection conn, String property, String schemaName, String tableName, int value)
		throws SQLException
	{
		try {
			JoPreparedStatement stm = conn.getPreparedStatement(
					"INSERT INTO MetaInfo (Property,SchemaName,TableName,Version) "+
					" VALUES (BINARY ?, BINARY ?, BINARY ?,  ?)");
			stm.setString(1,property);
			stm.setString(2,schemaName);
			stm.setString(3,tableName);
			stm.setInt(4,value);
			stm.execute();
		} catch (SQLException ex) {
			Application.error(ex);
		}
	}

	/**
	 * insert version information
	 */
	protected static void dropVersion(JoConnection conn, String property, String schemaName, String tableName)
		throws SQLException
	{
		try {
			//	delete from metainfo
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM MetaInfo ");

            sql.append(" WHERE BINARY Property = BINARY '");
			sql.append(property);
			sql.append("' ");

            sql.append("  AND BINARY SchemaName = BINARY '");
			sql.append(schemaName);
            sql.append("'");

            if (tableName!=null) {
                sql.append(" AND BINARY TableName = BINARY '");
                sql.append(tableName);
                sql.append("'");
            }

			conn.executeUpdate(sql.toString());
		} catch (SQLException ex) {
			Application.error(ex);
		}
	}

	public void markAllDirty() throws SQLException
	{
		String schemaName = XMLUtil.getChildValue(schema,"NAME");
		
		StringBuffer sql = new StringBuffer("UPDATE MetaInfo ");
		sql.append(" SET Dirty = ifnull(Dirty,0)+1 ");
		sql.append(" WHERE BINARY Property = BINARY 'TABLE_VERSION' ");
		sql.append("   AND BINARY SchemaName = BINARY '");
		sql.append(schemaName);
		sql.append("' ");

		connection.executeUpdate(sql.toString());
	}

	public void analyzeTables(boolean force)
	{
		String schemaName = XMLUtil.getChildValue(schema,"NAME");
		NodeList tables = schema.getElementsByTagName("TABLE");
		for (int i=0; i<tables.getLength(); i++)
		try {
			Element table = (Element)tables.item(i);
			String tableName = XMLUtil.getChildValue(table, "NAME");

			analyzeTable(schemaName,tableName,force);
			//  Question: why is MoreGame always analyzed ?
			//  Answer: character set must be defined
		} catch (SQLException ex) {
			Application.logError(ex);
		}
	}

	public boolean analyzeTable(String schemaName, String tableName, boolean force) throws SQLException
	{
		if (!force && !isDirty(schemaName,tableName))
			return false;   //  not necessary

		ResultSet result = null;
		Statement stm = null;

		try {
			stm = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			result = stm.executeQuery ("ANALYZE TABLE "+tableName);
			boolean wasAnalyzed = false;
			while (result.next()) {
				String message = result.getString("Msg_type");
				String status = result.getString("Msg_text");

				if (message.equalsIgnoreCase("status"))
					wasAnalyzed = status.equalsIgnoreCase("OK");
					//  else: table is up-to-date
				else if (message.equalsIgnoreCase("info"))
					/* noop */;
				else if (message.equalsIgnoreCase("error"))
					throw new SQLException(status);
			}
			if (wasAnalyzed) System.out.println(tableName+" was analyzed");
			markDirty(schemaName,tableName,false);
			return wasAnalyzed;

		} finally {
			if (stm!=null) try { stm.close(); } catch (SQLException e) { /* can't help it */ }
		}
	}
	/**
	 * @return an entry from the MetaInfo table
	 */
	public static int getSchemaVersion(JoConnection conn, String schema) throws SQLException
	{
	    if (schema==null) schema = "MAIN";
	    JoPreparedStatement pstm = conn.getPreparedStatement(
	            "SELECT Version" +
	            " FROM MetaInfo" +
	            " WHERE BINARY Property = BINARY 'SCHEMA_VERSION'" +
	            " AND BINARY SchemaName = BINARY ? ");
	    pstm.setString(1,schema);
	    return pstm.selectInt();
	}

	/**
	 */
	  public static void setSchemaVersion(JoConnection conn, String schema, int newVersion) throws SQLException
	  {
		if (schema==null) schema = "MAIN";
		JoPreparedStatement pstm = conn.getPreparedStatement(
			  "UPDATE MetaInfo" +
			  " SET Version = ?" +
			  " WHERE BINARY Property = BINARY 'SCHEMA_VERSION'" +
			  "   AND BINARY SchemaName = BINARY ? ");
		pstm.setInt(1,newVersion);
		pstm.setString(2,schema);
		pstm.execute();

		if (pstm.getUpdateCount()==0)
			insertVersion(conn,"SCHEMA_VERSION",schema,null,newVersion);

		if (getSchemaVersion(conn,schema)!=newVersion)
			throw new SQLException("Update Schema Version failed!");
	  }

	/**
	 * @return an entry from the MetaInfo table
	 */
	public static int getTableVersion(JoConnection conn, String schema, String table) throws SQLException
	{
	    if (schema==null) schema = "MAIN";
	    JoPreparedStatement pstm = conn.getPreparedStatement(
	            "SELECT Version" +
	            " FROM MetaInfo" +
	            " WHERE BINARY Property = BINARY 'TABLE_VERSION' "+
	            "   AND BINARY SchemaName = BINARY ? " +
	            "   AND BINARY TableName = BINARY ? ");
	    pstm.setString(1,schema);
	    pstm.setString(2,table);
	    return pstm.selectInt();
	}

	/**
	 */
	public static void setTableVersion(JoConnection conn, String schema, String table, int newVersion) throws SQLException
	{
	    if (schema==null) schema = "MAIN";
	    JoPreparedStatement pstm = conn.getPreparedStatement(
	            "UPDATE MetaInfo" +
	            " SET Version = ? " +
	            "WHERE BINARY Property = BINARY 'TABLE_VERSION' "+
	            "  AND BINARY SchemaName = BINARY ?" +
	            "  AND BINARY TableName = BINARY ? ");
	    pstm.setInt(1,newVersion);
		pstm.setString(2,schema);
	    pstm.setString(3,table);
	    pstm.execute();

		if (pstm.getUpdateCount()==0)
			insertVersion(conn,"TABLE_VERSION",schema,table,newVersion);

		if (getTableVersion(conn,schema,table)!=newVersion)
			throw new SQLException("Update Table Version failed!");
	}

	public void setCharset(String charset, String collation) throws Exception
	{
		NodeList tables = schema.getElementsByTagName("TABLE");
		for (int i=0; i<tables.getLength(); i++)
		{
			Element table = (Element)tables.item(i);
			setCharset(table,charset,collation);
		}
	}

	public boolean isDirty(String schemaName, String tableName) throws SQLException
	{
		String sql =
            "SELECT Dirty FROM MetaInfo "+
		    " WHERE Property = 'TABLE_VERSION' "+
		    "  AND SchemaName = ?"+
		    "  AND TableName = ?";

        JoPreparedStatement pstm = connection.getPreparedStatement(sql);
        pstm.setString(1,schemaName);
        pstm.setString(2,tableName);

        int value = pstm.selectInt();
//        System.out.println("isDirty("+schemaName+","+tableName+")="+value);
        return (value > 0);
	}

	public void markDirty(String schemaName, String tableName, boolean needsAnalyze) throws SQLException
	{
		StringBuffer sql = new StringBuffer("UPDATE MetaInfo ");
		if (needsAnalyze)
			sql.append(" SET Dirty = ifnull(Dirty,0)+1 ");
		else
			sql.append(" SET Dirty = NULL ");

		sql.append(" WHERE Property = 'TABLE_VERSION' ");
		sql.append("  AND SchemaName = ?");
		sql.append("  AND TableName = ?");

        JoPreparedStatement pstm = connection.getPreparedStatement(sql.toString());
        pstm.setString(1,schemaName);
        pstm.setString(2,tableName);
        pstm.execute();
	}

	public void setCharset(Element table, String charset, String collation)
		throws Exception
	{
		String tableName = XMLUtil.getChildValue(table, "NAME");
		String sql = "ALTER TABLE "+tableName+" CHARACTER SET "+charset;
		if (collation!=null)
			sql += " COLLATE "+collation;
		connection.executeUpdate(sql);
		/** update varchar columns, too */
		
		StringBuffer sqlb = new StringBuffer("ALTER TABLE "+tableName);
		boolean any = false;
		NodeList entries = table.getChildNodes();
		for (int i=0; i<entries.getLength(); i++) {
			Node entry = entries.item(i);
			if (entry.getNodeName().equals("COLUMN")) {
				Element column = (Element)entry;
				String type = XMLUtil.getChildValue(column,"TYPE");
				if (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("CHAR") || type.equalsIgnoreCase("LONGVARCHAR"))
				{
					if (any) sqlb.append(", ");
					sqlb.append(" MODIFY COLUMN ");
					appendColumnDefinition(sqlb, column, null,false, charset,collation);
					any = true;
				}
			}
		}
		if (any)
			connection.executeUpdate(sqlb.toString());
	}

	public static final void printHelp(PrintStream out)
	{
		out.print("java ");
		out.print(Setup.class.getName());
		out.println(" [-wd] [-mf] [-db] [-drop] [-export]");
		out.println("  Utilitiy for setting up a Jose Database ");
		out.println();
		out.println(" -wd <working directory>     default = current directory ");
		out.println(" -db <data source>           data source (as defined in datasources.xml)");
		out.println(" -schema <schema>            database schema ");
		out.println(" -drop                       clears an existing database ");
		out.println(" -export                     creates an export schema (w/out indexes and views) ");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException
	{
		Setup s = null;
		File wd = Application.getWorkingDirectory();
		File dd = new File("database");

		String datasource = "Quadcap";	//	default
		String schemaName = "MAIN"; // default
		try {

			for (int i=0; i<args.length; i++)
				if (args[i].equalsIgnoreCase("-wd"))
					wd = new File(args[++i]);
				else if (args[i].equalsIgnoreCase("-dd"))
					dd = new File(args[++i]);
				else if (args[i].equalsIgnoreCase("-db"))
					datasource = args[++i];
				else if (args[i].equalsIgnoreCase("-schema"))
					schemaName = args[++i];


			boolean create = true;
			boolean export = false;

			if (args.length < 2) printHelp(System.out);

			Config config = new Config(new File(wd,"config"));

			s = new Setup(config, schemaName, wd,dd, datasource);
			System.out.println("connected to "+JoConnection.getAdapter().getURL());

			for (int i=2; i<args.length; i+=2) {
				if (args[i].equalsIgnoreCase("-drop"))
					create = false;
				else if (args[i].equalsIgnoreCase("-export"))
					export = true;
				else
					s.put(args[i],args[i+1]);
			}

			if (create)
				s.createTables(null,null,true);
			else
				s.drop(false);

		} catch (SQLException sqex) {
			for ( ; sqex!=null; sqex = sqex.getNextException())
				Application.error(sqex);
		} catch (Throwable ex) {
			Application.error(ex);
		} finally {
			if (s!=null) s.close();
			System.exit(0);
		}
	}


}
