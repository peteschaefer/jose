package de.jose.task.db;

import de.jose.Application;
import de.jose.Main;
import de.jose.db.JoConnection;
import de.jose.pgn.Collection;
import de.jose.task.MaintenanceTask;

import java.sql.SQLException;

public class GameRepair
    extends MaintenanceTask
{

    /**
     *
     */
    public GameRepair() throws Exception {
        super("GameRepair",false);
    }

    public static boolean checkOnStart() throws SQLException
    {
        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            int maxId = conn.selectMaxIntValue("Game","Id");
            int maxGId = conn.selectMaxIntValue("MoreGame","GId");
            if (maxId != maxGId) {
                String message= "Database: Game.Id "
                        +" and MoreGame.GId "
                        +" are out of synch ("+maxId+" != "+maxGId+")."
                        +"\n\n"
                        +"Advice: Repair Game Table. ";
                Application.warning(message);
                return false;
            }
        } finally {
            conn.release();
        }
        return true;
    }

    @Override
    public void prepare() throws Exception
    {
        /*  (1) Game without MoreGame */
        String sql1 = "SELECT Game.Id "
                + "  FROM Game LEFT OUTER JOIN MoreGame"
                + "  ON Game.Id= MoreGame.GId "
                + "  WHERE MoreGame.GId IS NULL";

        String update1 = "INSERT INTO MoreGame (GId) VALUES ("+sql1+")";

        /*  (2) MoreGame without Game */
        String sql2 = "SELECT MoreGame.GId "
                + "FROM Game RIGHT OUTER JOIN MoreGame"
                + "  ON Game.Id= MoreGame.GId "
                + "  WHERE Game.Id IS NULL";

        Collection coll = Collection.newCollection(0,"Recovered Games",connection);
        String update2 = "INSERT INTO Game (GId,CId) VALUES ("+sql2+")";
    }

    @Override
    public void processGame(int GId) throws Exception {

    }

    @Override
    public void processGames(int[] GId, int from, int to) throws Exception {

    }

    @Override
    public void processCollection(int CId) throws Exception {

    }

    @Override
    public void processCollectionContents(int CId) throws Exception {

    }
}
