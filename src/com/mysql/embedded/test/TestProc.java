package com.mysql.embedded.test;

import com.mysql.embedded.jdbc.MySqlEmbeddedDriver;

import java.sql.*;

/**
 * Test1
 *
 * @author Peter Schäfer
 */
public class TestProc
{
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
		String url = "jose:mysql-embedded/test" +
		        "?library.path=lib" +
		        "&--defaults-file=examples/mysql.ini" +
				"&--datadir=data" +
				"&--basedir=D:/jose/mysql-5.0.18";
		//  note that relative paths are resolved correctly

		System.out.println("Java version "+System.getProperty("java.version"));
		System.out.println(System.getProperty("os.name")+" "
				   +System.getProperty("os.version")+" ("
				   +System.getProperty("os.arch")+") ");
		System.out.println();

		Connection connection = null;
		PreparedStatement stm1, stm2;
		ResultSet res1, res2;

		try {
			/**
			 * Register the JDBC driver
			 */
			DriverManager.registerDriver(new MySqlEmbeddedDriver());

			connection = DriverManager.getConnection(url);

			DatabaseMetaData meta = connection.getMetaData();
			System.out.println(meta.getDatabaseProductName()+" "
					   +meta.getDatabaseProductVersion());
            System.out.println(meta.getDriverName()+" "
                        +meta.getDriverVersion());
            System.out.println();

			String sql_create = "CREATE TABLE IF NOT EXISTS Test " +
					" (a int, b varchar(500), c int, d mediumblob)";

			connection.createStatement().execute("DROP TABLE IF EXISTS Test");
			connection.createStatement().execute(sql_create);

			String sql_insert = "INSERT INTO Test (a,b) VALUES (1,'Hello'), (2,'Hello World')";
			connection.createStatement().execute(sql_insert);

			PreparedStatement pins = connection.prepareStatement(
					"INSERT INTO Test (a,b) VALUES (?,BINARY ?)");
			pins.setInt(1,-1234567);
			pins.setString(2,"Hello World");
			pins.execute();

			PreparedStatement psum = connection.prepareStatement(
					"SELECT SUM(a) FROM Test");
			ResultSet res = psum.executeQuery();
			if (res.next())
				System.out.println(res.getInt(1));

			connection.createStatement().execute("DROP PROCEDURE IF EXISTS simpleproc");
			connection.createStatement().execute("DROP FUNCTION IF EXISTS simplefunc");

			String sql_create_func =
					"CREATE FUNCTION simplefunc (p1 INT, p2 INT) RETURNS INT" +
					" BEGIN" +
					" DECLARE p3 INT;"+
					" SET p3 = p1+p2;"+
					" SET p2 = p1+p2+p3;"+
					" RETURN p1+p2+p3;"+
					" END;";
//					"CREATE FUNCTION simplepi() RETURNS DOUBLE RETURN 3.141";

			String sql_create_proc =
					"CREATE PROCEDURE simpleproc (IN pin INT, INOUT pinout INT, OUT pout INT)" +
					" BEGIN" +
					" SET pout = pin+pinout;"+
					" SET pinout = pin+pout+pinout;"+
//					" SELECT a,b FROM Test;"+
                    " SELECT b,a FROM Test;"+
                    " END;";

			connection.createStatement().execute(sql_create_func);
			connection.createStatement().execute(sql_create_proc);


			//  Call function
			String sql_call_func = "SELECT simplefunc(?,?)";

			PreparedStatement call_func = connection.prepareStatement(sql_call_func);
			call_func.setInt(1,4);
			call_func.setInt(2,5);

			ResultSet res3 = call_func.executeQuery();
			if (res3.next())
				System.out.println("simplefunc returned: "+res3.getInt(1));
			res3.close();

			//  Call procedure

			String sql_call_proc = "CALL simpleproc(?,@v1,@v2)";
			CallableStatement call_proc = connection.prepareCall(sql_call_proc,
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			call_proc.setInt(1,7);
			call_proc.setInt("@v1",5);
			call_proc.setInt("@v2",4);

			boolean has_result = call_proc.execute();
			if (has_result) {
				res = call_proc.getResultSet();
				System.out.println();
				while (res.next())
					System.out.println(res.getObject(1)+" "+res.getObject(2));
				res.close();
			}
			else
				System.out.println("affected rows: "+call_proc.getUpdateCount());

            System.out.println("v1 = "+call_proc.getInt("@v1"));
			System.out.println("v2 = "+call_proc.getInt("@v2"));

            Statement stm = connection.createStatement();
//            pstm.setFetchSize(2);
            res = stm.executeQuery(" SELECT a,b FROM Test ; " +
                                   " SELECT b,a FROM Test ");
            for(;;) {
                while (res.next())
                    System.out.println(res.getObject(1)+" "+res.getObject(2));
                if (stm.getMoreResults())
                    res = stm.getResultSet();
                else
                    break;
            }

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
