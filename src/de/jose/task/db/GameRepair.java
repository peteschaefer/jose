package de.jose.task.db;

import de.jose.Application;
import de.jose.Main;
import de.jose.db.JoConnection;
import de.jose.db.MySQLAdapter;
import de.jose.pgn.Collection;
import de.jose.task.DBTask;
import de.jose.task.MaintenanceTask;
import de.jose.window.JoDialog;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;


/**
 *  Game.Id <-> MoreGame.GId
 *  is a 1:1, not-nullable, relation
 *
 *  however, for the sake of efficiency, referential integrity is not guarantueed.
 *  if, for example, a game import fails, or games are only partially deleted,
 *  referential integrity gets broken.
 *
 *  we fix it on application launch by inserting dummy records
 *  (which are of little use, but make sure that the 1:1 relation is restored)
 *
 */
public class GameRepair
    extends DBTask
{

    /**
     *
     */
    public GameRepair() throws Exception {
        super("GameRepair",false);
        setSilentTime(1000);
    }

    public static boolean checkOnStart() throws SQLException
    {
        JoConnection conn = null;
            try {
                conn = JoConnection.get();
                //  restore missing System folders
                Collection.makeAutoSave(conn);
                Collection.makeClipboard(conn);
                Collection.makeInTray(conn);
                //  Downloads is re-created on demand
                Collection.makeTrash(conn);

                //  restore missing pairs of Game <-> MoreGame
                int maxId = conn.selectMaxIntValue("Game", "Id");
                int maxGId = conn.selectMaxIntValue("MoreGame", "GId");
                if (maxId != maxGId) {
                    String message = "Database: Game.Id "
                            + " and MoreGame.GId "
                            + " are out of synch (" + maxId + " != " + maxGId + ")."
                            + "\n\n"
                            + "Repairing Game Table. ";
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JoDialog.showErrorDialog(message);
                        }
                    });
                    return false;
                }
                return true;
            } finally {
                conn.release();
            }
        //return true;
    }

    private static final String MISSING_MOREGAME =
            "SELECT Game.Id "
            + "  FROM Game LEFT OUTER JOIN MoreGame"
            + "  ON Game.Id=MoreGame.GId "
            + "  WHERE MoreGame.GId IS NULL";
    private static final String INSERT_MOREGAME =
            "INSERT INTO MoreGame (GId) " + MISSING_MOREGAME;

    private static final String FROM_RIGHT_OUTER =
            "FROM Game RIGHT OUTER JOIN MoreGame"
            + "  ON Game.Id= MoreGame.GId "
            + "  WHERE Game.Id IS NULL";
    private static final String MISSING_GAME =
            "SELECT MoreGame.GId "+FROM_RIGHT_OUTER;

    private static final String INSERT_GAME =
            "INSERT INTO Game (Id,CId,Idx, WhiteId,BlackId,EventId,SiteId,OpeningId,AnnotatorId) " +
            "  SELECT MoreGame.GId, @collId, @newIdx:=@newIdx+1, 0,0,0,0,0,0 "
            +FROM_RIGHT_OUTER;

    public int work() throws SQLException {
        /*  (1) Game without MoreGame */
        int repairedRows = 0;
        if (connection.exists(MISSING_MOREGAME)) {
            //  that should re-populate MoreGame
            repairedRows += connection.executeUpdate(INSERT_MOREGAME);
        }

        /*  (2) MoreGame without Game */
        if (connection.exists(MISSING_GAME)) {
            Collection coll = Collection.newCollection(0, "Recovered Games", connection);
            connection.executeUpdate("set @newIdx=1");
            connection.executeUpdate("set @collId="+coll.Id);
            //  that should re-populate Game
            repairedRows += connection.executeUpdate(INSERT_GAME);
        }

        System.err.println(repairedRows+" Game rows repaired.");
        return SUCCESS;
    }
}
