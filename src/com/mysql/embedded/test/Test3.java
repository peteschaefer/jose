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

package com.mysql.embedded.test;

import com.mysql.embedded.jdbc.MySqlEmbeddedDriver;

import java.sql.*;
import java.util.Properties;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;

/**
 *
 * UTF-8 test
 * @author Peter Schäfer
 */

public class Test3
{
	public static Properties props = new Properties();

	static
	{
		/** path to libmysql.dll   */
		props.put("library.path","lib");
		/** database */
//		props.put("database","utf");

		/** server parameters   */
		/** mysql base directory */
	    props.put("--basedir","/home/nightrider/mysql50");
		/** data directory */
		props.put("--datadir","/windows/D/jose/mysql-je/data");

		props.put("--skip-bdb","");
		props.put("--skip-innodb","");
//        info.put("--skip-networking","");
//        info.put("--skip-name-resolve","");
		props.put("--skip-grant-tables","");
		props.put("--skip-external-locking","");

//        info.put("--debug","d:D,20:O,debug.log");
//        info.put("--log-error","/windows/D/jose/work/error.log");
//        info.put("--log","/windows/D/jose/work/query.log");
		props.put("--console","");

		props.put("--default-character-set","utf8");
		props.put("--default-collation","utf8_general_ci");
	}


	public static void main(String[] args) throws SQLException, UnsupportedEncodingException
	{
		DriverManager.registerDriver(new MySqlEmbeddedDriver());

		Connection conn = DriverManager.getConnection("jdbc:mysql-embedded",props);

		System.out.println(conn.getMetaData().getDatabaseProductName());
		System.out.println(conn.getMetaData().getDatabaseProductVersion());

		Statement create = conn.createStatement();

		create.executeUpdate("CREATE DATABASE IF NOT EXISTS utf" +
		        " CHARACTER SET utf8" +
		        " COLLATE utf8_general_ci");

		create.executeUpdate("USE utf");

		create.executeUpdate("CREATE TABLE IF NOT EXISTS Test" +
		        " (a VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_general_ci, " +
		        "  b MEDIUMTEXT   CHARACTER SET utf8 COLLATE utf8_general_ci, " +
		        "  c CHAR(16)     CHARACTER SET utf8 COLLATE utf8_general_ci ) " +
		        " CHARACTER SET utf8" +
		        " COLLATE utf8_general_ci");

		create.close();

		PreparedStatement insert = conn.prepareStatement("INSERT INTO Test VALUES (?,?,?) ");
		insert.setString(1,"ä");
		insert.setString(2,"ä");
		insert.setString(3,"ä");
		insert.executeUpdate();

		byte[] oe = "ö".getBytes("UTF-8");
		insert.setBytes(1,oe);
		insert.setBytes(2,oe);
		insert.setBytes(3,oe);
		insert.executeUpdate();

		insert.setCharacterStream(1,new StringReader("ü"),1);
		insert.setCharacterStream(2,new StringReader("ü"),1);
		insert.setCharacterStream(3,new StringReader("ü"),1);
		insert.executeUpdate();

		insert.close();

		PreparedStatement select = conn.prepareStatement("SELECT a, length(a), b, length(b), c, length(c) FROM Test");

		ResultSet res = select.executeQuery();
		while (res.next())
		{
			System.out.println("> VARCHAR");
			System.out.println("length               "+res.getInt(2));
			System.out.println("getString()          "+res.getString(1));
			System.out.println("getBytes()           "+new String(res.getBytes(1),"UTF-8"));
//			System.out.println("getCharacterStream() "+res.getCharacterStream(1));

			System.out.println("> MEDIUMTEXT");
			System.out.println("length               "+res.getInt(4));
			System.out.println("getString()          "+res.getString(3));
			System.out.println("getBytes()           "+new String(res.getBytes(3),"UTF-8"));
//			System.out.println("getCharacterStream() "+res.getCharacterStream(3));

			System.out.println("> VARCHAR");
			System.out.println("length               "+res.getInt(6));
			System.out.println("getString()          "+res.getString(5));
			System.out.println("getBytes()           "+new String(res.getBytes(5),"UTF-8"));
//			System.out.println("getCharacterStream() "+res.getCharacterStream(5));
		}
		select.close();
		conn.close();
	}
}
