/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.pgn;

import de.jose.chess.HashKey;
import de.jose.chess.MatSignature;
import de.jose.chess.Move;
import de.jose.chess.Position;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 * @author Peter Schäfer
 */
public class PositionFilter
        extends BinReader
        implements Cloneable
{
	public long targetKey, targetKeyReversed;
	public boolean searchVariations;

	protected MatSignature targetSig, targetSigReversed;

	protected HashKey searchKey, searchKeyReversed;
	protected MatSignature searchSig;

	protected boolean inLine,ignoreLine;
	protected boolean result;

	public static PositionFilter PASS_FILTER = new PositionFilter(true) {
		public boolean accept(ResultSet res) throws SQLException		{ return true; }
	};

	private PositionFilter(boolean privateCtor) {
		super(null);
	}

	public PositionFilter()
	{
		super(new Position());

		searchKey = pos.getHashKey();
		searchKeyReversed = pos.getReversedHashKey();
		searchSig = pos.getMatSig();

		//  calculate hash keys & material signature
		pos.setOption(Position.INCREMENT_HASH,true);
		pos.setOption(Position.INCREMENT_REVERSED_HASH,true);
		pos.setOption(Position.INCREMENT_SIGNATURE,true);
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
        that.pos = this.pos;
        that.searchKey = (this.searchKey==null) ? null : (HashKey)this.searchKey.clone();
        that.searchKeyReversed = (this.searchKeyReversed==null) ? null : (HashKey)this.searchKeyReversed.clone();
        that.targetSig = (this.targetSig==null) ? null : (MatSignature)this.targetSig.clone();
        that.targetSigReversed = (this.targetSigReversed==null) ? null : (MatSignature)this.targetSigReversed.clone();
        that.searchSig = (this.searchSig==null) ? null : (MatSignature)this.searchSig.clone();
        that.targetKey = this.targetKey;
        that.targetKeyReversed = this.targetKeyReversed;
        that.searchVariations = this.searchVariations;
        that.inLine = this.inLine;
        that.ignoreLine = this.ignoreLine;
        that.result = this.result;
        return that;
    }

	public void clear() {
		targetKey = targetKeyReversed = 0L;
		searchVariations = false;
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

	public boolean accept(ResultSet res) throws SQLException
	{
		result = false;

		String fen = res.getString(2);
		byte[] bin = res.getBytes(3);

		ignoreLine = inLine = false;
		read(bin,0, null,0, fen,true);
		//  read will call back

		return result;
	}

	private void compareKeys()
	{
		/** check hash key  */
        if (searchKey.equals(targetKey))
            result = eof = true;	 //  this will terminate the read() method
        else if (searchKeyReversed.equals(targetKeyReversed))
            result = eof = true;	 //  this will terminate the read() method
	}

	private void checkCutOff()
	{
		/** check material signature for cut-off */
		if (!searchSig.canReach(targetSig) &&
		    (targetSigReversed==null || !searchSig.canReach(targetSig)))
			eof = true; //  signature cut-off
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
