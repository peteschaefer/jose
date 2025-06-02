/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.db.crossover;

import de.jose.Config;
import de.jose.chess.MatSignature;
import de.jose.chess.MatSignatureV1;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.JoStatement;
import de.jose.db.Setup;
import de.jose.pgn.PositionFilter;
import de.jose.window.JoDialog;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database cross-over for Meta Version 1009
 *
 * more indexes on Game. helpful for GIGA databases.
 *
 * @author Peter Sch�fer
 */

public class Crossover1011
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			Setup setup = new Setup(config,"MAIN",conn);
			if (version < 1011) {


				// ----------------------------------------------------
				//  Create a bunch of Indexes on Game
				// ----------------------------------------------------

				dlg = JoDialog.createMessageDialog("Database Update",
						"jose will now update the database structure for \n"+
						"improved performance.\n" +
				        "This may take 30 minutes or more. \n"+
						"Please be patient. Don't kill this process.",
				        false);
				dlg.setVisible(true);
				dlg.paint(dlg.getGraphics());

			}

			updateMatSignatureV2(conn,"jose.MoreGame");

			setup.setTableVersion(conn,"MAIN","MoreGame",104);
			setup.setSchemaVersion(conn,"MAIN",version=1011);
			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}

	public static void updateMatSignatureV2(JoConnection conn, String tableName)
	{
		String getall = "SELECT GId, FEN,Bin FROM "+tableName+" LIMIT 1000";	// todo no limit
		//	todo store results in memory temp table; update bulk at last
	}


	private static PositionFilter posf = new PositionFilter() {
		@Override
		public void afterMove(Move mv, int ply) {
			//	don't look for hash keys
		}

		@Override
		public void startOfLine(int nestLevel) {
			ignoreLine = true;
			inLine = nestLevel >= 1;
		}

		@Override
		protected void setPosOptions() {
			//  no need for hash keys
			pos.setOption(Position.INCREMENT_HASH,false);
			pos.setOption(Position.INCREMENT_REVERSED_HASH,false);
			//	need for incremental signature
			pos.setOption(Position.INCREMENT_SIGNATURE,true);
		}
	};

	private static MatSignature computeMatSignature(String fen, byte[] bin)
	{
		posf.read(bin,0, null,0, fen,true,false);
		return posf.getMatSig();
	}
}
