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

package de.jose.pgn;

import de.jose.chess.*;
import de.jose.util.concurrent.BatchThreadPool;
import de.jose.util.concurrent.QueueThreadPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.IntConsumer;


/**
 *
 * @author Peter Sch�fer
 */
public class PositionFilter
        extends BinReader
        implements Cloneable
{
	public long queryKey, queryKeyReversed;
	public boolean searchVariations;

	public MatSignature querySig;

//	protected HashKey searchKey, searchKeyReversed;
//	protected MatSignature searchSig;

	protected boolean inLine,ignoreLine;
	protected Result result;

	public enum Result  {
		REJECT, ACCEPT, WAIT
	};

	public static PositionFilter PASS_FILTER = new PositionFilter(true) {
		public Result accept(ResultSet res, IntConsumer callback) throws SQLException		{ return Result.ACCEPT; }
	};

	public static QueueThreadPool executorPool = new BatchThreadPool<PosFilterJob>(200000,80);
	//	note: using a batched pool drastically increases throughtput and reduces thread pool overhead
	//	we assume that tasks (PosFilterJob) are small and run quickly. Batching 80 of them into one is ok.
	private static ThreadLocal<PositionFilter> pooledFilter = new ThreadLocal<PositionFilter>() {
		@Override
		protected PositionFilter initialValue() { return new PositionFilter(); }
	};



	private PositionFilter(boolean privateCtor) {
		super(null);
	}

	public PositionFilter()
	{
		super(new Position(JoseHashKey.class, MatSignatureV2.class));

//		searchKey = pos.getHashKey();
//		searchKeyReversed = pos.getReversedHashKey();
//		searchSig = pos.getMatSig();

		setPosOptions();
	}

	protected void setPosOptions()
	{
		//  calculate hash keys & material signature
		pos.setOption(Position.INCREMENT_HASH,true);
		pos.setOption(Position.INCREMENT_REVERSED_HASH,true);
		pos.setOption(Position.INCREMENT_SIGNATURE,true);
		//	TODO Pawn Hash
		//  don't calculate castling & ep privileges (cause they are not known in the target position)
		pos.setOption(Position.IGNORE_FLAGS_ON_HASH, true);

		//  don't calculate checks etc.
		pos.setOption(Position.EXPOSED_CHECK, false);
		pos.setOption(Position.STALEMATE, false);
		pos.setOption(Position.DRAW_3, false);
		pos.setOption(Position.DRAW_50, false);
		pos.setOption(Position.DRAW_MAT, false);
		pos.setOption(Position.CHECK, false);
	}


	public Object clone()
    {
        PositionFilter that = new PositionFilter(false);
		that.pos = this.pos;	//	don't clone Position, right?? or not?
		this.copySearchParams(that);
		return that;
    }

	public void copySearchParams(PositionFilter that)
	{
		that.querySig = this.querySig; //(this.targetSig==null) ? null : (MatSignature)this.targetSig.clone();
		that.queryKey = this.queryKey;
		that.queryKeyReversed = this.queryKeyReversed;
		that.searchVariations = this.searchVariations;
		that.inLine = this.inLine;
		that.ignoreLine = this.ignoreLine;
		that.result = this.result;
	}

	public void clear() {
		queryKey = queryKeyReversed = 0L;
		searchVariations = false;
	}

	public PositionFilter getFilterLike()
	{
		PositionFilter pf = pooledFilter.get();
		this.copySearchParams(pf);
		return pf;
	}

	public boolean isEmpty()
	{
		return (queryKey ==0L) && (queryKeyReversed ==0L);
	}

	public void setTargetPosition(String fen, boolean calcReversed)
	{
		clear();
		pos.setup(fen);

        pos.computeHashKeys();
		pos.computeMatSig();

        queryKey = pos.getHashKey().value();
		querySig = pos.getMatSig().cloneSig();

        if (calcReversed) {
            queryKeyReversed = pos.getReversedHashKey().value();
        }
	}

	public void setVariations(boolean on)       { searchVariations = on; }

	public boolean hasVariations()              { return searchVariations; }

	private static class PosFilterJob implements Runnable
	{
		PositionFilter query;
		int GId;
		String fen;
		byte[] bin;
		IntConsumer callback;

		public PosFilterJob(PositionFilter query, int GId, String fen, byte[] bin, IntConsumer callback) {
			this.query = query;
			this.bin = bin;
			this.fen = fen;
			this.GId = GId;
			this.callback = callback;
		}

		@Override
		public void run() {
			PositionFilter pf = query.getFilterLike();
			Result rs = pf.accept(fen, bin, null);	//	note MatSignature already checked
			if (rs == Result.ACCEPT) callback.accept(GId);
		}
	}


	public Result accept(ResultSet res, IntConsumer asyncCallback) throws SQLException
	{
		MatSignature gameEndSig = new MatSignature(res.getLong(4),res.getLong(5));
		if (!querySig.canReach(gameEndSig)) return Result.REJECT;

		int GId = res.getInt(1);
		String fen = res.getString(2);
		byte[] bin = res.getBytes(3);

		if (bin == null) return Result.REJECT;    //	todo why can this happen at all?

		if (asyncCallback!=null) {
			//	submit job to executor pool
			PosFilterJob job = new PosFilterJob(this,GId,fen,bin,asyncCallback);
			executorPool.submit(job);
			return Result.WAIT;
		}
		else {
			//	do it now
			return accept(fen, bin, null);	//	note: MatSignature already checked synchroneously, above
		}
 	}

	public Result accept(String fen, byte[] bin, MatSignature gameEndSig)
	{
		if (bin == null) return Result.REJECT;    //	todo why can this happen at all?
		if (gameEndSig!=null && !querySig.canReach(gameEndSig)) return Result.REJECT;

		result = Result.REJECT;	// unless...
		ignoreLine = inLine = false;
		read(bin,0, null,0, fen,true,true);
		//  read will call back to (BinReader)this
		//	note: reset==false keeps the final position
		return result;
	}

	private void compareKeys()
	{
		/** check hash key  */
        if (pos.getHashKey().equals(queryKey) || pos.getReversedHashKey().equals(queryKeyReversed)) {
			eof = true; //  this will terminate the read() method
            result = Result.ACCEPT;
		}
	}

	private void checkCutOff()
	{
		/** check material signature for early cut-off
		 * 	note: only noisy moves modify the MatSignature */
		if (!pos.wasSilent()) {
			pos.updateMatSig();
			if (!pos.getMatSig().canReach(querySig) &&
				(queryKeyReversed == 0L || !pos.getMatSig().canReachReversed(querySig))) {
				eof = true; //  signature cut-off
				result = Result.REJECT;
			}
		}
	}

	public MatSignature getMatSig()
	{
		pos.updateMatSig();
		return pos.getMatSig();
	}


	//  BinReader callback methods:

	public void afterMove (Move mv, int ply)
	{
		if (!ignoreLine) compareKeys();
		if (!inLine) checkCutOff();
//		if (!inLine && (ply%10==0)) checkCutOff();
//		if (eof && !result) System.err.println("cut-off after "+ply);
	}

	public void startOfLine (int nestLevel) 	{
		if (nestLevel==0)
			compareKeys();		//	start of game
		else if (!searchVariations)
			ignoreLine = true;
		inLine = nestLevel >= 1;
	}

	public void endOfLine (int nestLevel) {
		inLine = nestLevel < 1;
		if (!inLine) ignoreLine = false;
	}


	public void result (int resultCode)                                 { /* ignored  */ }

	public void beforeMove (Move mv, int ply, boolean displayHint)      { /* ignored  */ }

	public void comment (StringBuffer text)                  { /* ignored  */ }

	public void annotation (int nagCode)                                { /* ignored  */ }
}
