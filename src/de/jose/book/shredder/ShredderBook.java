// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packfields(3) packimports(7) fieldsfirst splitstr(64) nonlb space lnc radix(10) lradix(10) 

package de.jose.book.shredder;

import de.jose.book.*;
import de.jose.book.BookEntry;
import de.jose.chess.Board;
import de.jose.chess.Position;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.Random;

// Referenced classes of package com.shredderchess.book:
//			a, b, e

public class ShredderBook extends OpeningBook
{

	private static final char remapPiece[] = {   /* ??? */
			Util.EMPTY,
			Util.WHITE_KING,       //  1
			Util.WHITE_PAWN,       //  2
			Util.WHITE_KNIGHT,     //  3

			Util.EMPTY,
			Util.WHITE_BISHOP,     //  5
			Util.WHITE_ROOK,       //  6
			Util.WHITE_QUEEN,      //  7

			Util.EMPTY,
			Util.BLACK_KING,       //  9
			Util.BLACK_PAWN,       //  10
			Util.BLACK_KNIGHT,     //  11

			Util.EMPTY,
			Util.BLACK_BISHOP,     //  13
			Util.BLACK_ROOK,       //  14
			Util.BLACK_QUEEN,      //  15
	};

	public static final int SELECT_MODE_MAX = 1;
	public static final int SCORE_MODE_2 = 2;
	public static final int SCORE_MODE_3 = 3;
	public static final int SCORE_MODE_4 = 4;

	private int selectMode;
	private String bookFilePath;

	public ShredderBook()
	{ }

	public boolean canTranspose()                               { return true; }
	public boolean canTransposeColor()                         { return true; }
	public boolean canTransposeIntoBook()                   { return false; }

	public boolean open(RandomAccessFile file)
	{
		disk = file;
//		selectMode = SELECT_MODE_MAX;  //  score only best move
//		selectMode = SCORE_MODE_2;  //
		selectMode = SCORE_MODE_3;  //
//		selectMode = SCORE_MODE_4;  //  equal probabilities for each move
		//  check integrity ?!
		Position pos = new Position();
		pos.setupInitial();
		List moveEntries = readMoveEntries(pos);
		return ! moveEntries.isEmpty();
	}

	public boolean getBookMoves(Position pos, boolean withTransposedColors, boolean deep, List result)
	{
		if (!canTransposeColor()) withTransposedColors = false;  //  no use looking for transposed colors
		boolean res1 = getBookMovesColored(pos, false, result);
		boolean res2 = false;
		if (withTransposedColors) res2 = getBookMovesColored(pos, true, result);
		return res1||res2;
	}

	private boolean getBookMovesColored(Position pos, boolean reverse, List result)
	{
		List moveEntries = readMoveEntries(pos);
		result.addAll(moveEntries);
		return ! moveEntries.isEmpty();
	}


	public ShredderBook(String filePath) {
		bookFilePath = filePath;
		selectMode = 3;
	}

	public ShredderBook(String filePath, int scoreMode) {
		bookFilePath = filePath;
		this.selectMode = scoreMode;
	}

	public void setSelectMode(int selectMode) {
		this.selectMode = selectMode;
	}

	public void setFileName(String filePath) {
		bookFilePath = filePath;
	}

	private ShredderBookEntry readBookEntry(BookFile bookFile) throws IOException
	{
		int unknownValue = bookFile.readInt();
		byte byte0 = bookFile.readByte();
		byte byte1 = bookFile.readByte();
		byte score17 = bookFile.readByte(); //  position score, 1..7
		byte byte3 = bookFile.readByte();
		int whiteElo = bookFile.readInt();
		int backElo = bookFile.readInt();
		int year = bookFile.readInt();
		float unknownBookEntryFlag = bookFile.readFloat();
		int countWins = bookFile.readInt();
		int countDraws = bookFile.readInt();
		int countLost = bookFile.readInt();
		int j2 = bookFile.readInt();
		int lowByte = (unknownValue & 0x7f) - 1;
		int hiByte = (unknownValue >> 7 & 0x7f) - 1;
		char piece1 = remapPiece[unknownValue >> 22 & 0xf];
		char piece2 = remapPiece[unknownValue >> 18 & 0xf];
		boolean flag = (unknownValue & 0x4000000) != 0;
		char piece3 = remapPiece[unknownValue >> 14 & 0xf];
		Move move = new Move(lowByte, hiByte, piece1, piece2, flag, piece3);
		return new ShredderBookEntry(move, score17, whiteElo, backElo, year, unknownBookEntryFlag,
				countWins, countDraws, countLost);
	}

	private int mapScore(char scoreAG) {
		switch (selectMode) {
		case SELECT_MODE_MAX:
			switch (scoreAG) {
			case 'a': return 4;
			case 'b': return 3;
			case 'c': return 2;
			case 'd': return 1;
			}
			return 0;

		case SCORE_MODE_2:
			switch (scoreAG) {
			case 'a': return 16;    //  strongly prefer 'a'
			case 'b': return 8;
			case 'c': return 4;
			case 'd': return 1;
			}
			return 0;

		case SCORE_MODE_3:
			switch (scoreAG) {
			case 'a': return 4;     //  prefer 'a'
			case 'b': return 3;
			case 'c': return 2;
			case 'd': return 1;
			}
			return 0;

		case SCORE_MODE_4:
			switch (scoreAG) {
			case 'a': return 1;     //  treat all the same
			case 'b': return 1;
			case 'c': return 1;
			case 'd': return 1;
			case 'e': return 1;
			}
			return 0;
		}
		return 0;
	}

	protected Vector readBookEntries(Board board) {
		/*synchronized (board)*/ {
		Vector vector = new Vector();
		try {
			BookFile bookFile = new BookFile(disk/*bookFilePath, "r"*/);
			ShredderHashKey hashKey = new ShredderHashKey();
			board.computeHashKey(hashKey);
//			int hashKeyPart1 = ShredderHashKey.calculateHashKeyPart1(board);
//			int hashKeyPart2 = ShredderHashKey.calculateHashKeyPart2(board);
			long blockOffset = hashKey.value() & 127;
			if (!board.whiteMovesNext())
				blockOffset += 128L;    //  set of black move entries
			blockOffset *= 0x610L;   //  block-size = 32*book-entry-size ?
			bookFile.seek(blockOffset);
			readBookEntries(bookFile, vector, hashKey);
//			bookFile.close();    don't
			return vector;
		}
		catch (FileNotFoundException filenotfoundexception) {
//			break MISSING_BLOCK_LABEL_104;
		}
		catch (IOException ioexception) { }
		return vector;
		}
	}


	public BookEntry selectBookMove(Position pos, boolean ignoreColors, Random random)
	{
		//  TODO implement reversed color transposition
		synchronized (pos) {
		double selectScore;
		double scoreSum;
		Iterator moveEntryIterator;
		List moveEntries = readMoveEntries(pos);
		selectScore = random.nextDouble() * 100D;
		scoreSum = 0.0D;

		moveEntryIterator = moveEntries.iterator();
		while (moveEntryIterator.hasNext()) {
			MoveEntry moveEntry = (MoveEntry)moveEntryIterator.next();
			scoreSum += moveEntry.userValue;
			if (scoreSum > selectScore)
				return moveEntry;
		}
		return null;
		}
	}

	public List readMoveEntries(Position board)
	{
		/*synchronized (board)*/ {
		List moveEntries = new Vector();
		Vector bookEntries = readBookEntries(board);
		int bookScoreSum = 0;
		int bookScoreMax = 1;
		for (Iterator bookEntryIterator = bookEntries.iterator(); bookEntryIterator.hasNext();)
		{
			ShredderBookEntry bookEntry = (ShredderBookEntry)bookEntryIterator.next();
			int book14 = mapScore(bookEntry.getScoreAG());
			if (book14 >= bookScoreMax) {
				if (book14 > bookScoreMax && selectMode == SELECT_MODE_MAX) {
					//  consider only max. scores
					bookScoreSum = 0;
					bookScoreMax = book14;
				}
				bookScoreSum += book14;
			}
		}

		MoveEntry moveEntry;
		for (Iterator bookEntryIterator = bookEntries.iterator(); bookEntryIterator.hasNext(); moveEntries.add(moveEntry))
		{
			ShredderBookEntry bookEntry = (ShredderBookEntry) bookEntryIterator.next();
			int bookScore = mapScore(bookEntry.getScoreAG());
			double moveScore;
			if (bookScoreSum == 0 || bookScore < bookScoreMax) {
				moveScore = 0.0D;   //  = don't select this move
			} else {
				moveScore = (100D * (double) bookScore) / (double) bookScoreSum;
			}

			moveEntry = new MoveEntry(bookEntry.getMove(),
					"" + bookEntry.getScoreAG(),
					moveScore,
					bookEntry.countTotal(),
					100D * bookEntry.getAverageScore(board.whiteMovesNext()),
					board.whiteMovesNext() ? bookEntry.getWhiteEloAverage() : bookEntry.getBlackEloAverage(),
					bookEntry.getEloPerformance(board.whiteMovesNext()),
					bookEntry.getYearAverage(),
					board.whiteMovesNext() ? bookEntry.countWins() : bookEntry.countLost(),
					bookEntry.countDraws(),
					board.whiteMovesNext() ? bookEntry.countLost() : bookEntry.countWins(), "");
		}

		return moveEntries;
		}
	}

	private void readBookEntries(BookFile bookFile, Vector vector, ShredderHashKey hashKey) throws IOException
	{
		//  scan a block of 32 book entries
		long baseFilePointer = bookFile.getFilePointer();
		for (int ientry = 1; ientry <= 32; ientry++)
		{
			int bookKeyPart1 = bookFile.readInt();
			int bookKeyPart2 = bookFile.readInt();
			if (hashKey.value1() == bookKeyPart1 && hashKey.value2() == bookKeyPart2) {
				ShredderBookEntry bookEntry = readBookEntry(bookFile);
				vector.add(bookEntry);
			} else {
				bookFile.seek(baseFilePointer + (long)(ientry * 48));   //  skip this entry
			}
		}

		// follow link to next block of book entries...
		int j1 = bookFile.readInt();    //  what is this good for ?
		int i2 = bookFile.readInt();    //  what is this good for ?
		int extendPtr2 = bookFile.readInt();    //  pointer to another block of book entries
		int extendPtr1 = bookFile.readInt();    //  pointer to another block of book entries
		int extendPtr;
		if ((hashKey.value2() & i2) != 0)
			extendPtr = extendPtr1;
		else
			extendPtr = extendPtr2;
		if (extendPtr != 0) {
			bookFile.seek(extendPtr);
			readBookEntries(bookFile, vector, hashKey);
		}
	}

	public String getFileName() {
		if (bookFilePath == null || bookFilePath.length() <= 1)
			return "";
		int i = bookFilePath.lastIndexOf(File.separator);
		if (i <= 0)
			return bookFilePath;
		else
			return bookFilePath.substring(i + 1);
	}

}
