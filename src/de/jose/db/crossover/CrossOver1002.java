package de.jose.db.crossover;

import de.jose.Application;
import de.jose.Config;
import de.jose.db.JoConnection;
import de.jose.db.Setup;
import de.jose.window.JoDialog;

import java.awt.*;

/**
 * Database Cross-Over for meta version 1002
 *
 * introduces two new columns
 *      GamePlayer.Color        (0=black, 1=white)
 *      GamePlayer.Result       (result from player's perspectiv, i.e. reversed for black)
 *
 * these columns are necessary for correct color-insensistive searches
 */
public class CrossOver1002
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			if (version < 1002) {
				// ----------------------------------------------------
				//  New Columns GamePlayer.Color and GamePlayer.Result
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
				        "jose will now update the database structure.\n" +
				        "This may take up to some minutes.",
				        false);
				dlg.show();
				dlg.paint(dlg.getGraphics());

				Setup setup = new Setup(config,"MAIN",conn);
/*
				setup.addColumn("GamePlayer","Color",3);
				setup.addColumn("GamePlayer","Result",4);
/*
					alter table gameplayer
					modify column PId integer not null,
					add column Color tinyint not null,
					add index GamePlayer_3 (Color),
					add column Result tinyint not null,
					add index GamePlayer_4 (Result)
* /
				//  fill in values
				String upd1 =
				        "UPDATE GamePlayer, Game" +
				        " SET GamePlayer.Color = 1, GamePlayer.Result = Game.Result" +
				        " WHERE GamePlayer.GId = Game.Id" +
				        "   AND GamePlayer.PId = Game.WhiteId";
				/** Color = 1 = White
				 *  Result = Game.Result (from white's perspective)
				 * /
				conn.executeUpdate(upd1);

				String upd2 =
				        "UPDATE GamePlayer, Game" +
				        " SET GamePlayer.Result = CASE WHEN Game.Result < 0 THEN Game.Result ELSE 2-Game.Result END" +
				        " WHERE GamePlayer.GId = Game.Id" +
				        "   AND GamePlayer.PId = Game.BlackId";
				/** Color = 0 = Black
				 *  Result = 2-Game.Result (from black's perspective)
				 * /
				conn.executeUpdate(upd2);
*/
				System.err.print("[create GamePlayer");
				try {
					setup.dropTable("GamePlayer");
				} catch (Exception e) {
					Application.error(e);
				}

				setup.createTable(null,setup.getTable("GamePlayer"),null,true);
				JoConnection.getAdapter().disableConstraints("GamePlayer",conn);

				String ins =
				"INSERT INTO GamePlayer" +
				" SELECT Id, WhiteId, 1, Result FROM Game " +
				" UNION" +
				" SELECT Id, BlackId, 0, CASE WHEN Result<0 THEN Result ELSE 2-Result END FROM Game";

				conn.executeUpdate(ins);
				JoConnection.getAdapter().enableConstraints("GamePlayer",conn);
				System.err.println("]");
				
				Setup.setTableVersion(conn,"MAIN","GamePlayer",102);
				Setup.setSchemaVersion(conn,"MAIN",version=1002);
			}

			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}
}
