package de.jose.db.crossover;

import de.jose.Config;
import de.jose.db.JoConnection;
import de.jose.db.Setup;
import org.w3c.dom.Element;

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
}
