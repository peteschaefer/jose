package de.jose.db.crossover;

import de.jose.Application;
import de.jose.Config;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.MySQLAdapter;
import de.jose.db.Setup;
import org.w3c.dom.Element;

import java.io.File;
import java.sql.*;
import java.util.Properties;

public class Crossover1008
{
    public static int crossOver(int version, JoConnection conn, Config config) throws Exception
    {
        if (version < 1008)
        {
            if (Setup.getTableVersion(conn,"MAIN","MoreGame") < 102)
            {
                conn.executeUpdate(
                        "ALTER TABLE MoreGame " +
                            " ADD COLUMN (Eval LONG VARBINARY)");
                Setup.setTableVersion(conn,"MAIN","MoreGame",102);
            }

        }

        Setup.setSchemaVersion(conn,"MAIN",version=1008);
        return version;
    }

    private static void initDatabaseProps(Properties props)
    {
        props.put("library.path","lib/Windows");
        props.put("database","jose");
        props.put("--basedir","C:\\dev\\jose\\work\\bin");
        props.put("--datadir","C:\\dev\\jose\\work\\database\\mysql");
        props.put("--default-character-set","utf8");
        props.put("--default-collation","utf8_general_ci");
        props.put("--skip-bdb","");
        props.put("--skip-innodb","");
        props.put("--skip-networking","");
        props.put("--skip-name-resolve","");
        props.put("--skip-grant-tables","");
        props.put("--skip-locking","");
        props.put("--skip-external-locking","");
        props.put("--lower_case_table_names","0");   //  means: always use exact case
        props.put("--key_buffer",   "16M");
        props.put("--max_allowed_packet",   "1M");
        props.put("--table_cache",  "64");
        props.put("--sort_buffer_size", "512K");
        props.put("--net_buffer_length",    "8K");
        props.put("--read_buffer_size", "256K");
        props.put("--read_rnd_buffer_size", "512K");
        props.put("--myisam_sort_buffer_size",  "8M");
        props.put("--myisam-recover","FORCE");  //  always check for corrupted index files, etc.
        props.put("--delay_key_write","ALL");
    }

    /**
     * add IO_Game.Eval to archive files
     * currently, this has to be done manually. There should be a better way in ArchiveImport, though.
     *
     * 1. extract (unrar) file into database/mysql/tempdb
     * 2. run Crossover1008.main
     * 3. notice how IO_Game.MYD greq in size
     * 4. zip the tables
     */
    public static void main(String[] args) throws SQLException
    {
        Properties connectionProps = new Properties();
        initDatabaseProps(connectionProps);

        DriverManager.registerDriver(new com.mysql.embedded.jdbc.MySqlEmbeddedDriver());
        Connection connection = DriverManager.getConnection("jdbc:mysql-embedded",connectionProps);

        Statement statement = connection.createStatement();
        statement.executeUpdate(
                "ALTER TABLE tempdb.IO_Game " +
                    " ADD COLUMN (Eval LONG VARBINARY)");
        statement.close();
        connection.close();
    }
}
