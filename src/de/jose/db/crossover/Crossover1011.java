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

import de.jose.Application;
import de.jose.Config;
import de.jose.Version;
import de.jose.chess.*;
import de.jose.db.*;
import de.jose.db.io.ArchiveFile;
import de.jose.pgn.PositionFilter;
import de.jose.util.concurrent.BatchThreadPool;
import de.jose.util.concurrent.QueueThreadPool;
import de.jose.window.JoDialog;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static de.jose.pgn.BinReader.REPLAY;
import static de.jose.pgn.BinReader.RESET;

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
								"new features.\n" +
								"This may take some minutes.",
						false);
				dlg.setVisible(true);
				dlg.paint(dlg.getGraphics());

			}

			int rows = updateMatSignatureV2(conn,"jose.MoreGame",0);
			System.out.println("["+rows+" rows updated]");

			setup.setTableVersion(conn,"MAIN","MoreGame",104);
			setup.setSchemaVersion(conn,"MAIN",version=1011);
			return version;

		} finally {
			if (dlg!=null) dlg.dispose();
		}
	}

	private static BatchThreadPool<UpdateMatSignatureJob> executorPool =
			new BatchThreadPool<UpdateMatSignatureJob>(8,2_000_000,200);

	static class UpdateMatSignatureJob implements Runnable
	{
		int GId;
		String fen;
		byte[] bin;
		long whiteSignature, blackSignature;

		public UpdateMatSignatureJob(int GId, String fen, byte[] bin) {
			this.GId = GId;
			this.fen = fen;
			this.bin = bin;
		}

		@Override
		public void run() {
			MatSignature matSignature = computeMatSignature(fen,bin);
            whiteSignature = matSignature.getWhiteSignature();
			blackSignature = matSignature.getBlackSignature();
			fen = null;
			bin = null;	// release memory
		}
	};

	static void updateMap(JoConnection conn, ArrayList<UpdateMatSignatureJob> batch)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO MapMatSignature " +
				" (GId,WhiteSignature,BlackSignature)" +
				" VALUES ");
		for (int i=0; i<batch.size(); i++) {
			if(i>0) sb.append(",");
			sb.append("(?,?,?)");
		}

		synchronized(conn) {
			try {
				JoPreparedStatement pstm = conn.getPreparedStatement(sb.toString());
				for (int i = 0; i < batch.size(); i++) {
					pstm.setInt(3 * i + 1, batch.get(i).GId);
					pstm.setLong(3 * i + 2, batch.get(i).whiteSignature);
					pstm.setLong(3 * i + 3, batch.get(i).blackSignature);
				}
				pstm.execute();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static int updateMatSignatureV2(JoConnection readConn, String tableName, int limit) throws SQLException, InterruptedException {
		String getAll = "SELECT GId, FEN,Bin FROM "+tableName;
		if (limit > 0) getAll += " LIMIT "+limit;
		//	store results in memory temp table; update bulk at last
		String createTemp =
				"CREATE TEMPORARY TABLE IF NOT EXISTS MapMatSignature " +
				" (GId INT PRIMARY KEY NOT NULL," +
				"  WhiteSignature BIGINT NOT NULL," +
				"  BlackSignature BIGINT NOT NULL)" +
				" ENGINE=MEMORY";

		long startTime = System.currentTimeMillis();
		JoConnection insertConn = JoConnection.get();
		insertConn.executeUpdate(createTemp);

		executorPool.setOnBatchFinished(
				(ArrayList<UpdateMatSignatureJob> batch) -> updateMap(insertConn,batch));
		JoPreparedStatement readAll = readConn.getPreparedStatement(getAll);

		if (!readAll.execute()) throw new SQLException();
		ResultSet rs = readAll.getResultSet();
		int rows = 0;
		while (rs.next()) {
			rows++;
			int GId = rs.getInt(1);
			String fen = rs.getString(2);
			byte[] bin = rs.getBytes(3);

			executorPool.submit(new UpdateMatSignatureJob(GId,fen,bin));
		}
		rs.close();
		System.out.println("["+rows+" rows read]");
		executorPool.finish();

		long time = System.currentTimeMillis() - startTime;
		System.out.println("["+time/1000+" s]");

		insertConn.executeUpdate("LOCK TABLES "+tableName+" WRITE, MapMatSignature READ");
		String copyAll =
				"UPDATE "+tableName+
				" JOIN MapMatSignature ON "+tableName+".GId = MapMatSignature.GId " +
				" SET "+tableName+".WhiteSignature = MapMatSignature.WhiteSignature," +
				"     "+tableName+".BlackSignature = MapMatSignature.BlackSignature";
		rows = insertConn.executeUpdate(copyAll);

		System.out.println("["+rows+" rows updated]");
		insertConn.executeUpdate("UNLOCK TABLES ");
		insertConn.executeUpdate("FLUSH TABLES");
		insertConn.executeUpdate("DROP TABLE IF EXISTS MapMatSignature");
		insertConn.release();

		time = System.currentTimeMillis() - startTime;
		System.out.println("["+time/1000+" s]");
		return rows;
	}

	static class ReplayFilter extends PositionFilter {
		@Override
		public void afterMove(Move mv, int ply) {
			//	don't look for hash keys
		}

		@Override
		public void startOfLine(int nestLevel) { }

		@Override
		protected void setPosOptions() {
			//  no need for hash keys
			super.setPosOptions();
			pos.setOption(Position.INCREMENT_HASH,false);
			pos.setOption(Position.INCREMENT_REVERSED_HASH,false);
			//	need for incremental signature
			pos.setOption(Position.INCREMENT_SIGNATURE,true);
		}
	};

	private static ThreadLocal<ReplayFilter> pooledFilter = new ThreadLocal<ReplayFilter>() {
		@Override
		protected ReplayFilter initialValue() { return new ReplayFilter(); }
	};

	private static MatSignature computeMatSignature(String fen, byte[] bin)
	{
		PositionFilter pf = pooledFilter.get();
		pf.read(bin,0, null,0, fen,REPLAY);
		MatSignature mat = pf.getMatSig();
		return mat;
	}


	public static void launchDBServer(String datadir) throws Exception {
		if (Version.linux) {
			System.setProperty("java.library.path", "lib/Linux_amd64");
			System.setProperty("jose.datadir",datadir);
		}
		if (Version.windows) {
			System.setProperty("java.library.path", ".;lib/Windows");
			System.setProperty("jose.datadir",datadir);
		}
		System.setProperty("jose.splash","off");
		System.setProperty("jose.console.output","true");
		System.setProperty("java.awt.headless","true");

		System.setProperty("jose.db","MySQL-standalone");
		System.setProperty("jose.db.port","3306");
		System.setProperty("jose.splash","false");
		System.setProperty("jose.console.output","true");
		Application app = new Application();

		MySQLAdapter adapter = (MySQLAdapter) JoConnection.getAdapter(true);
		Thread launcher = adapter.launchProcess();
		launcher.join();
		//adapter.waitForStandaloneServer();
	}

}
