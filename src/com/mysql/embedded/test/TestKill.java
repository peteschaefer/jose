package com.mysql.embedded.test;

import com.mysql.embedded.jdbc.MySqlEmbeddedDriver;
import com.mysql.embedded.jdbc.MyConnection;

import java.sql.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Test1
 *
 * @author Peter Schäfer
 */
public class TestKill
{
	static Connection connection = null;

	static class LongRunner extends Thread
	{
		public void run()
		{
			try {
/*
				String url = "jose:mysql-embedded/jose" +
				        "?library.path=lib" +
				        "&--defaults-file=/home/nightrider/mysql50/mysql.ini" +
						"&--datadir=/windows/D/jose/work/database/mysql" +
						"&--basedir=/home/nightrider/mysql50";
*/
				String url = "jose:mysql-embedded/jose" +
				        "?library.path=lib" +
//				        "&--defaults-file=D:/jose/mysql-5.0.18/mysql.ini" +
						"&--datadir=D:/jose/work/database/mysql" +
						"&--basedir=.";

				connection = DriverManager.getConnection(url);

				DatabaseMetaData meta = connection.getMetaData();
				System.out.println(meta.getDatabaseProductName()+" "
						   +meta.getDatabaseProductVersion());
	            System.out.println(meta.getDriverName()+" "
	                        +meta.getDriverVersion());
	            System.out.println();

				BufferedReader linein = new BufferedReader(new InputStreamReader(System.in));

				PreparedStatement stm1 = connection.prepareStatement(
						"SELECT * FROM Game" +
								" JOIN MoreGame ON Game.Id = MoreGame.GId" +
								" JOIN Player White ON Game.WhiteId = White.Id" +
								" JOIN Player Black ON Game.BlackId = Black.Id" +
								" JOIN Event ON Game.EventId = Event.Id" +
								" JOIN Site ON Game.SiteId = Site.Id" +
								" JOIN Opening ON Game.OpeningId = OpeningId" +
								" LIMIT 300000",
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY);
				System.out.print("[execute"); System.out.flush();
				stm1.execute();
				System.out.println("]"); System.out.flush();

				System.out.print("["); System.out.flush();
				ResultSet res = stm1.getResultSet();
				for (int row=0; res.next(); row++)
				{
					if ((row%10000)==0) { System.out.print(".");  System.out.flush(); }
				}
				System.out.println("]"); System.out.flush();
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
				System.out.println(ex.getErrorCode());
				System.out.println(ex.getSQLState());
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	static class KillerThread extends Thread
	{
		public void run()
		{
			try {
				sleep(15000);
			} catch (InterruptedException e) {
			}
			System.out.println("kill");
			((MyConnection)connection).killQuery();
		}
	}

	public static void main(String[] args) throws SQLException
	{

		/**
		 * conection url:
		 *  jose:mysql-embedded[/catalog][?param=value]*
		 *
		 * alternatively you can pass parameters as connection properties:
		 *  properties.put("database","Test");
		 *  properties.put("--datadir","path-to-data-dir");
		 */
		//  note that relative paths are resolved correctly

		System.out.println("Java version "+System.getProperty("java.version"));
		System.out.println(System.getProperty("os.name")+" "
				   +System.getProperty("os.version")+" ("
				   +System.getProperty("os.arch")+") ");
		System.out.println();

		try {
			/**
			 * Register the JDBC driver
			 */
			DriverManager.registerDriver(new MySqlEmbeddedDriver());

			/** long-running query  */
			Thread longRunner = new LongRunner();
			longRunner.start();

			Thread killer = new KillerThread();
			killer.start();

			longRunner.join();

		} catch (Throwable ex) {
			ex.printStackTrace(System.out);
		} finally {
			/**
			 * closing the connection will also shut down the MySQL engine
			 */
			connection.close();
		}

	}
}
