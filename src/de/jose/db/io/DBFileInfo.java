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

package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.Setup;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Header info at the start of a database dump file
 *
 * @author Peter Schäfer
 */

public class DBFileInfo
           implements Serializable
{
    /** serial version   */
	static final long serialVersionUID = 8546803464514708892L;

    /** current file version    */
    public static final int FILE_VERSION    = 1000;

    /** file format version */
    private int              fileVersion;

    /** content type    */
    private String           contentType;

    /** custom info */
    private Serializable     customInfo;

    /** table version info: maps table names to meta data version (Integer)  */
    private HashMap          tableVersionInfo;

    /** list of ResultSetInfo   */
    private ArrayList        resultSetInfo;

    /** are primary keys normalized ?   */
    private boolean         normalPK;

    public DBFileInfo(String type, boolean normalize)
    {
        fileVersion = FILE_VERSION;
        contentType = type;
        tableVersionInfo = new HashMap();
        resultSetInfo = new ArrayList();
        normalPK = normalize;
    }

	public String getContentType()                           { return contentType; }

    public void setCustomInfo(Serializable object)          { customInfo = object; }

    public Object getCustomInfo()                           { return customInfo; }

    public int getFileVersion()                             { return fileVersion; }

    public int countResultSets()                            { return resultSetInfo.size(); }

    public ResultSetInfo getResultSetInfo(int i)            { return (ResultSetInfo)resultSetInfo.get(i); }

	public Set getTableNames()
	{
		return tableVersionInfo.keySet();
	}

    public void addTableVersion(String schemaName, String tableName, int version)
    {
        if (schemaName==null) schemaName="MAIN";
        tableVersionInfo.put(schemaName+"."+tableName, new Integer(version));
    }

    public int getTableVersion(String schemaName, String tableName)
    {
        if (schemaName==null) schemaName="MAIN";
        Integer i = (Integer)tableVersionInfo.get(schemaName+"."+tableName);
        if (i!=null)
            return i.intValue();
        else
            return 0;
    }

    public int addTableVersion(String schemaName,String tableName, JoConnection conn)
        throws SQLException
    {
        int version = getTableVersion(schemaName,tableName);
        if (version > 0) return version;  //  that was easy

        //  query MetaInfo table
        version = Setup.getTableVersion(conn,schemaName, tableName);
        if (version > 0)
            addTableVersion(schemaName,tableName,version);
        return version;
    }

    public void addTableVersions(ResultSetInfo ri, JoConnection conn)
        throws SQLException
    {
        for (int i=1; i <= ri.countColumns(); i++)
            addTableVersion(ri.getSchemaName(i),ri.getTableName(i), conn);
    }

	public ResultSetInfo getResultInfo(String zipName)
	{
		for (int i=0; i<resultSetInfo.size(); i++)
		{
			ResultSetInfo resi = (ResultSetInfo)resultSetInfo.get(i);
			if (resi.getZipName().equalsIgnoreCase(zipName))
				return resi;
		}
		return null;
	}

    public void addResultSet(ResultSetInfo ri)
    {
        resultSetInfo.add(ri);
    }

    public void finish(JoConnection conn)
        throws SQLException
    {
        for (int i=0; i < countResultSets(); i++)
            addTableVersions(getResultSetInfo(i), conn);
    }
}
