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

import de.jose.chess.MatSignature;
import de.jose.chess.Move;
import de.jose.chess.Position;
import de.jose.util.JoThreadPool;

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
	public long targetKey, targetKeyReversed;
	public boolean searchVariations;

	protected MatSignature targetSig, targetSigReversed;

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

	private PositionFilter(boolean privateCtor) {
		super(null);
	}

	public PositionFilter()
	{
		super(new Position());

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
		that.targetSig = this.targetSig; //(this.targetSig==null) ? null : (MatSignature)this.targetSig.clone();
		that.targetSigReversed = this.targetSigReversed; //(this.targetSigReversed==null) ? null : (MatSignature)this.targetSigReversed.clone();
		that.targetKey = this.targetKey;
		that.targetKeyReversed = this.targetKeyReversed;
		that.searchVariations = this.searchVariations;
		that.inLine = this.inLine;
		that.ignoreLine = this.ignoreLine;
		that.result = this.result;
	}

	public void clear() {
		targetKey = targetKeyReversed = 0L;
		searchVariations = false;
	}

	public static JoThreadPool executorPool = new JoThreadPool<PosFilterJob>(64000);
	private static ThreadLocal<PositionFilter> pooledFilter = new ThreadLocal<PositionFilter>() {
		@Override
		protected PositionFilter initialValue() { return new PositionFilter(); }
	};

	public PositionFilter getFilterLike()
	{
		PositionFilter pf = pooledFilter.get();
		this.copySearchParams(pf);
		return pf;
	}

	public boolean isEmpty()
	{
		return (targetKey==0L) && (targetKeyReversed==0L);
	}

	public void setTargetPosition(String fen, boolean calcReversed)
	{
		clear();
		pos.setup(fen);

        pos.computeHashKeys();
		pos.computeMatSig();

        targetKey = pos.getHashKey().value();
		targetSig = pos.getMatSig().cloneSig();

        if (calcReversed) {
            targetKeyReversed = pos.getReversedHashKey().value();
			targetSigReversed = pos.getMatSig().cloneSigReversed();
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
		if (!targetSig.canReach(gameEndSig)) return Result.REJECT;

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
		if (gameEndSig!=null && !targetSig.canReach(gameEndSig)) return Result.REJECT;

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
        if (pos.getHashKey().equals(targetKey) || pos.getReversedHashKey().equals(targetKeyReversed)) {
			eof = true; //  this will terminate the read() method
            result = Result.ACCEPT;
		}
	}

	private void checkCutOff()
	{
		/** check material signature for early cut-off */
		if (!getMatSig().canReach(targetSig) &&
		    (targetSigReversed==null || !pos.getMatSig().canReach(targetSig))) {
			eof = true; //  signature cut-off
			result = Result.REJECT;
		}
	}

	public MatSignature getMatSig()
	{
		if (!pos.hasOption(Position.INCREMENT_SIGNATURE))
			pos.computeMatSig();
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
