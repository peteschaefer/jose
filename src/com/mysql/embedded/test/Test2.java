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
import com.mysql.embedded.jdbc.MyConnection;
import com.mysql.embedded.jdbc.MyPreparedStatement;
import com.mysql.embedded.api.res;
import com.mysql.embedded.api.api;
import com.mysql.embedded.api.stmt;
import com.mysql.embedded.api.enum_field_types;

import java.sql.*;
import java.util.Properties;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Peter Schäfer
 */

public class Test2
{
    public static Properties info;

	static class Task extends Thread
	{
		public void run()
		{
			try {
                System.err.println("go");
				Connection conn1 = DriverManager.getConnection("jdbc:mysql-embedded",info);

                System.err.println("db-thread id="+api.mysql_thread_id(MySqlEmbeddedDriver.databaseHandle));
                System.err.println("thread id="+api.mysql_thread_id(((MyConnection)conn1).connectionHandle));

				Statement cr = conn1.createStatement();

/*
			for (int i=0; i<10; i++) {

					stm1.setInt(1,2);
					ResultSet res1 = stm1.executeQuery();

					stm2.setString(1,"aardvark");

					if (res1.next())
						printLine(res1) ;

					while (res1.next())
							printLine(res1) ;

					System.out.println("---------------");

					ResultSet res2 = stm2.executeQuery();

					sleep(10);

					if (res2.next())
						printLine(res2) ;


					System.out.println("---------------");

					while (res2.next())
							printLine(res2) ;

					res2.close();
					res1.close();
				}
*/

				conn1.close();

                System.err.println("done");
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
        info = new Properties();
        /** path to libmysql.dll   */
	info.put("library.path","lib");
        /** database */
        info.put("database","test");

        /** server parameters   */
        /** mysql base directory */
	        info.put("--basedir",".");
        /** data directory */
        info.put("--datadir","data");

        info.put("--skip-bdb","");
        info.put("--skip-innodb","");
//        info.put("--skip-networking","");
//        info.put("--skip-name-resolve","");
        info.put("--skip-grant-tables","");
        info.put("--skip-external-locking","");

//        info.put("--debug","d:D,20:O,debug.log");
//        info.put("--log-error","/windows/D/jose/work/error.log");
//        info.put("--log","/windows/D/jose/work/query.log");
        info.put("--console","");


        try {
            MySqlEmbeddedDriver.loadLibraries(info);

            for (int i=0; i<3; i++)
                new Task().start();

        } finally {
        }
    }

	protected static void printLine(ResultSet res) throws SQLException
	{
		int id = res.getInt(1);
		String text = res.getString(2);
		System.out.println(id+" "+text);

		java.sql.Date date = res.getDate(3);
		String sdate = res.getString(3);
		System.out.println(" "+sdate+" "+date);

		java.sql.Time time = res.getTime(4);
		String stime = res.getString(4);
		System.out.println(" "+stime+" "+time);

		java.sql.Timestamp tstamp = res.getTimestamp(5);
		String ststamp = res.getString(5);
		System.out.println(" "+ststamp+" "+tstamp);

		java.sql.Timestamp datetime = res.getTimestamp(6);
		String sdatetime = res.getString(6);
		System.out.println(" "+sdatetime+" "+datetime);

		java.sql.Date year = res.getDate(7);
		String syear = res.getString(7);
		System.out.println(" "+syear+" "+year);

		java.sql.Blob blob = res.getBlob(8);
		byte[] bbytes = res.getBytes(8);
		String btext = res.getString(8);

		System.out.println(btext+" "+new String(bbytes));

		java.sql.Clob clob = res.getClob(9);
		byte[] cbytes = res.getBytes(9);
		String ctext = res.getString(9);

		try {
			System.out.println(ctext+" "+new String(cbytes,"ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
