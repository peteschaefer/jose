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
import de.jose.db.ResultSetAdapter;
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
{
	protected PosSearchRecord query;
	protected Result result;

	public enum Result  {
		REJECT, ACCEPT, WAIT,
		REJECT_NEXT //	used during replay
	};
/*
	public static PositionFilter PASS_FILTER = new PositionFilter(true) {
		public Result accept(ResultSet res, IntConsumer callback) throws SQLException		{ return Result.ACCEPT; }
	};
*/
	public static QueueThreadPool executorPool = new BatchThreadPool<PosFilterJob>(200000,80);
	//	note: using a batched pool drastically increases throughtput and reduces thread pool overhead
	//	we assume that tasks (PosFilterJob) are small and run quickly. Batching 80 of them into one is ok.
	private static ThreadLocal<PositionFilter> pooledFilter = new ThreadLocal<PositionFilter>() {
		@Override
		protected PositionFilter initialValue() { return new PositionFilter(); }
	};



	private PositionFilter(boolean privateCtor) {
		super(new Position(JoseHashKey.class, MatSignatureV2.class));
	}

	public PositionFilter() {
		this(new PosSearchRecord());
	}

	public PositionFilter(PosSearchRecord q)
	{
		this(true);
		this.query = q;
		setPosOptions();
	}

	protected void setPosOptions()
	{
		//  calculate hash keys & material signature
		pos.setOption(Position.INCREMENT_HASH,true);
		pos.setOption(Position.INCREMENT_REVERSED_HASH,true);
		pos.setOption(Position.INCREMENT_SIGNATURE,true);
		pos.setOption(Position.IGNORE_FLAGS_ON_HASH, true);

		//  don't calculate checks etc.
		pos.setOption(Position.EXPOSED_CHECK, false);
		pos.setOption(Position.STALEMATE, false);
		pos.setOption(Position.DRAW_3, false);
		pos.setOption(Position.DRAW_50, false);
		pos.setOption(Position.DRAW_MAT, false);
		pos.setOption(Position.CHECK, false);
	}

/*
	public Object clone()
    {
        PositionFilter that = new PositionFilter(false);
		that.pos = this.pos;	//	don't clone Position, right?? or not?
		this.copySearchParams(that);
		return that;
    }
*/
	public void copySearchParams(PositionFilter that)
	{
		that.query= this.query; //(this.targetSig==null) ? null : (MatSignature)this.targetSig.clone();
		that.result = this.result;
	}

	public void setSearchParams(PosSearchRecord query)
	{
		this.query = query;
	}

	public void clear() {
		query.clear();
	}

	public PositionFilter getFilterLike()
	{
		PositionFilter pf = pooledFilter.get();
		this.copySearchParams(pf);
		return pf;
	}

	public boolean isEmpty()
	{
		return query.isEmpty();
	}
/*
	public void setTargetPosition(String fen, boolean calcReversed)
	{
		clear();
		pos.setup(fen);

        pos.computeHashKeys();
		pos.computeMatSig();

        queryKey = pos.getHashKey().value();
		querySig = (MatSignature) pos.getMatSig().clone();

        if (calcReversed) {
            queryKeyReversed = pos.getReversedHashKey().value();
        }
	}
*/
	public void setVariations(boolean on)       { query.variations = on; }

	public boolean hasVariations()              { return query.variations; }

	private static class PosFilterJob implements Runnable
	{
		PositionFilter query;
		int GId;
		String fen;
		byte[] bin;
		boolean hasVariations;
		IntConsumer callback;

		public PosFilterJob(PositionFilter query, int GId, String fen, byte[] bin, boolean hasVariations,
							IntConsumer callback) {
			this.query = query;
			this.bin = bin;
			this.fen = fen;
			this.hasVariations = hasVariations;
			this.GId = GId;
			this.callback = callback;
		}

		@Override
		public void run() {
			PositionFilter pf = query.getFilterLike();
			Result rs = pf.accept(fen, bin, null, hasVariations);	//	note MatSignature already checked
			if (rs == Result.ACCEPT) callback.accept(GId);
		}
	}


	public Result accept(ResultSet res, IntConsumer asyncCallback) throws SQLException
	{
		MatSignatureV2 gameEndSig = new MatSignatureV2(res.getLong(4),res.getLong(5));
		boolean hasVariations = res.getInt(6) > 0;

		if (query.earlyCutOff(gameEndSig,hasVariations))
			return Result.REJECT;

		int GId = res.getInt(1);
		String fen = res.getString(2);
		byte[] bin = res.getBytes(3);

		if (bin == null) return Result.REJECT;    //	todo why can this happen at all?

		if (asyncCallback!=null) {
			//	submit job to executor pool
			PosFilterJob job = new PosFilterJob(this,GId,fen,bin, hasVariations, asyncCallback);
			executorPool.submit(job);
			return Result.WAIT;
		}
		else {
			//	do it now
			return accept(fen, bin, null, hasVariations);	//	note: MatSignature already checked synchroneously, above
		}
 	}

	public Result accept(String fen, byte[] bin, MatSignature gameEndSig, boolean hasVariations)
	{
		if (bin == null) return Result.REJECT;    //	todo why can this happen at all?
		if ((gameEndSig!=null) && query.earlyCutOff(gameEndSig,hasVariations))
			return Result.REJECT;

		result = Result.REJECT;	// unless...

		int oldOptions = pos.getOptions();
		query.setPositionOptions(pos);

		int readOptions = REPLAY|RESET;
		if (!query.variations && hasVariations) readOptions |= SKIP_VARS;

		read(bin,0, null,0, fen, readOptions);
		//  read will call back to (BinReader)this
		//	note: reset==false keeps the final position
		pos.setOptions(oldOptions);
		return result;
	}

	public MatSignature getMatSig()
	{
		pos.updateMatSig();
		return pos.getMatSig();
	}


	//  BinReader callback methods:

	public void afterMove (Move mv, int ply)
	{
		if (result== Result.REJECT_NEXT) {
			//	cut-off was detected one move before; finalize it:
			eof = true; //  this will terminate the read() method
			result = Result.REJECT;
		}
		else if ((nestLevel == 0 || query.variations)
				&& query.matches(pos, !pos.wasSilent())) {
			eof = true; //  this will terminate the read() method
			result = Result.ACCEPT;
		}
		else if ((nestLevel==0) && query.cutOff(pos,!pos.wasSilent())) {
			//	note: if the search position is not reachable from the main line
			//	it won't from one of the later variations
			//	EXCEPT: in the variation *immediately* following this move (which will undo 'mv')
			//	in other words: don't cut-off if there is a variation following
			result = Result.REJECT_NEXT;
		}
//		if (!inLine && (ply%10==0)) checkCutOff();
//		if (eof && !result) System.err.println("cut-off after "+ply);
	}

	public void startOfLine (int nestLevel) {
		if (result == Result.REJECT_NEXT)
			result = Result.REJECT;
		//	undo reject; see above
	}
	public void endOfLine (int nestLevel) { }


	public void result (int resultCode)                                 { /* ignored  */ }
	public void beforeMove (Move mv, int ply, boolean displayHint)      { /* ignored  */ }
	public void comment (StringBuffer text)                  { /* ignored  */ }
	public void annotation (int nagCode)                                { /* ignored  */ }
}
