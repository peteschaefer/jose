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
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.db.*;
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

public class Crossover1010
{
	public static int crossOver(int version, JoConnection conn, Config config) throws Exception
	{
		Dialog dlg = null;
		try {
			Setup setup = new Setup(config,"MAIN",conn);
			if (version < 1010) {


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

				String sql1 = "ALTER TABLE MoreGame ADD COLUMN WhiteSignature BIGINT NOT NULL DEFAULT 0 AFTER INFO";
				String sql2 = "ALTER TABLE MoreGame ADD COLUMN BlackSignature BIGINT NOT NULL DEFAULT 0 AFTER WhiteSignature";

				DBAdapter adapter = JoConnection.getAdapter();
				if (!adapter.existsColumn("MAIN","MoreGame","WhiteSignature"))
					conn.executeUpdate(sql1);
				if (!adapter.existsColumn("MAIN","MoreGame","BlackSignature"))
					conn.executeUpdate(sql2);
				//	fill it
				fillMatSignatures(conn);
			}

			setup.setTableVersion(conn,"MAIN","MoreGame",103);
			setup.setSchemaVersion(conn,"MAIN",version=1010);
			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}

	public static void fillMatSignatures(JoConnection conn) throws SQLException {
		String select = "SELECT GId, FEN, Bin, WhiteSignature, BlackSignature from MoreGame"
						+" WHERE WhiteSignature=0 OR BlackSignature=0 "
						+" FOR UPDATE";
		JoStatement stm = new JoStatement(conn,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
		ResultSet res = stm.executeQuery(select);
		while (res.next()) {
			int GId = res.getInt(1);
			String fen = res.getString(2);
			byte[] bin = res.getBytes(3);

			MatSignature matsig;
			try {
				matsig = computeMatSignature(fen, bin);
			} catch(Throwable e) {
				matsig = new MatSignature(0,0);
				System.err.println("[dropped mat signature "+GId+"]");
			}

			res.updateLong(4, matsig.wsig);
			res.updateLong(5, matsig.bsig);
			res.updateRow();
		}
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
			//	no need for incremental signature
			pos.setOption(Position.INCREMENT_SIGNATURE,false);
			//	TODO Pawn Hash
			//  don't calculate castling & ep privileges (cause they are not known in the target position)
		}
	};

	private static MatSignature computeMatSignature(String fen, byte[] bin)
	{
		posf.read(bin,0, null,0, fen,true,false);
		return posf.getMatSig();
	}
}
