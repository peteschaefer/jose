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

public class Test4
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
	    props.put("--basedir","/Users/peterschafer/Development/mysql-5.0.13");
		/** data directory */
		props.put("--datadir","/Users/peterschafer/Development/mysql-je/data");

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


	public static void main(String[] args)
	{
		try {

			DriverManager.registerDriver(new MySqlEmbeddedDriver());

			Connection conn = DriverManager.getConnection("jdbc:mysql-embedded/test",props);

            CallableStatement call = conn.prepareCall("CALL simpleproc(?,@pout,@pinout)");

            call.setInt(1,4);
            call.registerOutParameter("pout",Types.INTEGER);
            call.registerOutParameter("pinout",Types.INTEGER);
            call.setInt("pinout",5);
            call.execute();

            ParameterMetaData pmd = call.getParameterMetaData();

//            System.out.println("pout="+call.getInt(2));
            System.out.println("pinout="+call.getInt(2));

/*

			final PreparedStatement select = conn.prepareStatement(
			        "SELECT GId, length(Comments), Comments" +
			        " FROM MoreGame WHERE GId IN (?,?,?) ");
			select.setInt(1,532952);
			select.setInt(2,532953);
			select.setInt(3,532954);

            Thread cross = new Thread()
            {
                public void run()
                {
                    try {
                        ResultSet res = select.executeQuery();
                        while (res.next())
                        {
                            System.out.println("Id="+res.getInt(1));
                            System.out.println("length(Comments)="+res.getInt(2));

                            byte[] bytes = res.getBytes(3);
				System.out.println(new String(bytes));
                            System.out.println();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            };

            cross.start();
            cross.join();

			select.close();
*/
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
