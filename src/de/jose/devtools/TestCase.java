package de.jose.devtools;

import java.util.Properties;
import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: peterschafer
 * Date: 13.11.2005
 * Time: 12:38:27
 * To change this template use File | Settings | File Templates.
 */
public class TestCase
{
    public static final int COUNT = 10;

    public static void main(String[] args) throws SQLException, ClassNotFoundException
    {
        Properties props = new Properties();
        props.put("--basedir","/Users/peterschafer/Development/jose/bin");
        props.put("--datadir","/Users/peterschafer/Development/jose/database/mysql");

        props.put("library.path","/Users/peterschafer/Development/jose/lib/Mac");
        props.put("database","jose");

        props.put("--default-character-set","utf8");
        props.put("--default-collation","utf8_general_ci");

        //  most of the following are already defined in my.ini
        //  doesn't hurt to define them twice:
        props.put("--skip-bdb","");
        props.put("--skip-innodb","");
        props.put("--skip-networking","");
        props.put("--skip-name-resolve","");
        props.put("--skip-grant-tables","");
        props.put("--skip-locking","");
        props.put("--skip-external-locking","");
        props.put("--lower_case_table_names","0");   //  means: always use exact case

        props.put("--debug","O,/Users/peterschafer/Development/jose/debug.log");//"d:D,20:O,debug.log");
        props.put("--log-error","/Users/peterschafer/Development/jose/error.log");
        props.put("--log","/Users/peterschafer/Development/jose/query.log");
        props.put("--console","");

        /** fine tuning */
        props.put("--key_buffer",   "16M");
        props.put("--max_allowed_packet",   "1M");
        props.put("--table_cache",  "64");
        props.put("--sort_buffer_size", "512K");
        props.put("--net_buffer_length",    "8K");
        props.put("--read_buffer_size", "256K");
        props.put("--read_rnd_buffer_size", "512K");
        props.put("--myisam_sort_buffer_size",  "8M");
        props.put("--myisam-recover","FORCE");  //  always check for corrupted index files, etc.

        Connection conn[] = new Connection[COUNT];
        try {
            Class.forName("com.mysql.embedded.jdbc.MySqlEmbeddedDriver");
            for (int j=0; j < COUNT; j++)
                conn[j] = DriverManager.getConnection("jdbc:mysql-embedded",props);

            String sql = "SELECT Dirty FROM MetaInfo "+
                " WHERE BINARY Property = BINARY 'TABLE_VERSION' "+
                "  AND BINARY SchemaName = BINARY 'MAIN' " +
                "  AND BINARY TableName = BINARY 'MoreGame'";

            Statement stm = conn[0].createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stm.setMaxRows(1);
            ResultSet res = stm.executeQuery(sql);

            if (res.next()) {
//                System.out.println(res.getInt(1));
//                System.out.println(res.getString(1));
//                System.out.println(res.getObject(1));
//                System.out.println(res.wasNull());
            }
            else
                System.out.println("ResultSet was empty!");

            stm = conn[1].createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stm.setMaxRows(1);
            res = stm.executeQuery("SELECT MIN(Id), MAX(Id) FROM Game");
            if (res.next())
                System.out.println("MIN(Id)="+res.getInt(1)+", MAX(Id)="+res.getInt(2));


            sql = "SELECT Game.Id, Game.CId, Game.Idx, "
			+"  White.Name, MoreGame.WhiteTitle, Game.WhiteELO, "
			+"  Black.Name, MoreGame.BlackTitle, Game.BlackELO, "
			+"  Game.Result, "
			+"  Event.Name, Site.Name, MoreGame.Round, MoreGame.Board, Game.GameDate, "
			+"  Game.ECO, Opening.Name, Game.DateFlags, Annotator.Name,"
			+"  Game.PlyCount, Game.Attributes"
            +" FROM Game, Player White, Player Black, Event, Site, Opening, Player Annotator, MoreGame "
            +" WHERE White.Id = Game.WhiteId "
			+"   AND Black.Id = Game.BlackId "
			+"   AND Event.Id = Game.EventId "
			+"   AND Site.Id = Game.SiteId "
			+"   AND Opening.Id = Game.OpeningId "
            +"   AND Annotator.Id = Game.AnnotatorId "
            +"   AND MoreGame.GId = Game.Id"
            +"   AND Game.Id = ?";

            PreparedStatement pstm[] = new PreparedStatement[COUNT];
            for (int j=0; j < COUNT; j++)
                    pstm[j] = conn[j].prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSetMetaData meta = null;

 outer:     for (int pk=1001; pk <= 1200; pk++)
                for (int j=0; j < COUNT; j++)
                {
                    pstm[j].setInt(1,pk);
                    boolean has_result = pstm[j].execute();

                    res = pstm[j].getResultSet();
                    if (meta==null) meta = res.getMetaData();

                    if (res.next()) {
                        for (int i=1; i <= meta.getColumnCount(); i++)
                            res.getObject(i);
                    }
                    else {
                        System.out.println("missing data ["+j+"], "+pk);
                        break outer;
                    }
                    res.close();
                }

        }
        catch (SQLException e) {
            e.printStackTrace();
        } finally {
            for (int j=0; j < 10; j++)
                if (conn[j]!=null) conn[j].close();
        }
    }
}
