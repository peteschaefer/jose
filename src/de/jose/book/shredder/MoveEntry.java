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

package de.jose.book.shredder;

import de.jose.book.shredder.Move;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;

/**
 * @author Peter Schäfer
 */

public class MoveEntry extends Move
{

	public String a, k;
	public double avgScore;
	public int avgElo, avgPerformance, avgYear;

	public MoveEntry(Move move, String s, double score, int countTotal,
	                 double avgScore,
	                 int avgElo, int avgPerformance, int avgYear,
	                 int countWhite, int countDraws, int countBlack, String s1)
	{
		super(move.from, move.to, move.piece, move.victim, move.isPromotion, move.promotionPiece);
		a = s;
		this.userValue = score;
		this.count = countTotal;
		this.avgScore = avgScore;
		this.avgElo = avgElo;
		this.avgPerformance = avgPerformance;
		this.avgYear = avgYear;
		this.countWhite = countWhite;
		this.countDraw = countDraws;
		this.countBlack = countBlack;
		this.isTransposedColor = false;
		k = s1;
		this.move = createMove();
	}

	private de.jose.chess.Move createMove()
	{
		int from = squareFrom64(super.from);
		int to = squareFrom64(super.to);

		de.jose.chess.Move move = new de.jose.chess.Move(from,to);

		if (isPromotion)
			switch (promotionPiece)
			{
			case Util.WHITE_QUEEN:
			case Util.BLACK_QUEEN:
			default:
				move.setPromotionPiece(Constants.QUEEN); break;
			case Util.WHITE_ROOK:
			case Util.BLACK_ROOK:
				move.setPromotionPiece(Constants.ROOK); break;
			case Util.WHITE_BISHOP:
			case Util.BLACK_BISHOP:
				move.setPromotionPiece(Constants.BISHOP); break;
			case Util.WHITE_KNIGHT:
			case Util.BLACK_KNIGHT:
				move.setPromotionPiece(Constants.KNIGHT); break;
			}

		return move;
	}



	public String toString()
	{
		return move.toString();
	}

	private static int squareFrom64(int square64)
	{
		int file0 = square64%8;
		int row0 = square64/8;
		return EngUtil.square(Constants.FILE_A+file0, Constants.ROW_1+row0);
	}
}
