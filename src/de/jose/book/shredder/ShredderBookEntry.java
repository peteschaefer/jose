// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packfields(3) packimports(7) fieldsfirst splitstr(64) nonlb space lnc radix(10) lradix(10) 

package de.jose.book.shredder;

public class ShredderBookEntry {

	private Move move;
	private byte score17;
	private int whiteEloSum, blackEloSum, yearSum, countWins;
	private float unknownFlag;
	private int countDraws, countLost;

	public ShredderBookEntry(Move aMove, byte score17, int aWhiteElo, int aBlackElo,
	                         int aYear, float unknownFlag, int aCountWins,
	                         int aCountDraws, int aCountLost) {
		move = aMove;
		this.score17 = score17;
		whiteEloSum = aWhiteElo;
		blackEloSum = aBlackElo;
		yearSum = aYear;
		this.unknownFlag = unknownFlag;
		countWins = aCountWins;
		countDraws = aCountDraws;
		countLost = aCountLost;
	}

	public Move getMove() {
		return move;
	}

	public char getScoreAG() {
		switch (score17 & 0xf) {
		case '\001':    return 'a';
		case '\002':    return 'b';
		case '\003':    return 'c';
		case '\004':    return 'd';
		case '\005':    return 'e';
		case '\006':    return 'f';
		case '\007':    return 'g';
		}
		return '-';
	}

	public int countTotal() {
		return countWins + countDraws + countLost;
	}

	public int countWins() {
		return countWins;
	}

	public int countDraws() {
		return countDraws;
	}

	public int countLost() {
		return countLost;
	}

	public int getWhiteEloAverage() {
		if (countTotal() > 0)
			return 1600 + whiteEloSum / countTotal();
		else
			return 0;
	}

	public int getBlackEloAverage() {
		if (countTotal() > 0)
			return 1600 + blackEloSum / countTotal();
		else
			return 0;
	}

	public int getYearAverage() {
		if (countTotal() > 0)
			return 1600 + yearSum / countTotal();
		else
			return 0;
	}

	public double getAverageScore(boolean black) {
		if (countTotal() > 0)
			return ((double)(black ? countWins : countLost)
					+ 0.5D * (double)countDraws) / (double)countTotal();
		else
			return 0.0D;
	}

	private static int getEloExpected(double d1) {
		if (d1 == 1.0D)
			return 400;
		if (d1 == 0.0D)
			return -400;
		else
			return (int)((400D * Math.log(d1 / (1.0D - d1))) / Math.log(10D));
	}

	public int getEloPerformance(boolean black) {
		if (countTotal() > 0)
			return (black ? getBlackEloAverage() : getWhiteEloAverage())
					+ getEloExpected(getAverageScore(black));
		else
			return 0;
	}

	public String toString() {
		Move mv = getMove();
		String s1 = "Move:" + Util.pieceToString[mv.piece] + Util.squareToString[mv.from] + Util.squareToString[mv.to]
				+ " " + getScoreAG()
				+ " n=" + countTotal()
				+ " eloW=" + getWhiteEloAverage()
				+ " eloB=" + getBlackEloAverage()
				+ " year=" + getYearAverage()
				+ " w=" + countWins()
				+ " d=" + countDraws()
				+ " l=" + countLost();
		return s1;
	}
}
