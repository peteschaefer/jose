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

package de.jose.chess;

import de.jose.Util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Random;

/**
 * represents the Chess Board
 */

public class Board
		implements Constants, BinaryConstants
{
	//-------------------------------------------------------------------------------
	//	fields
	//-------------------------------------------------------------------------------

	/**	the board 	 */
	protected Piece[] theBoard;
	/*	the pieces	*/
	protected ArrayList[] theWhitePieces;
	protected ArrayList[] theBlackPieces;
    /** white promotion pieces  */
    protected Piece[]   theWhitePromoPieces;
    protected Piece[]   theBlackPromoPieces;
	/** rooks for castling
	 *  initialized from FEN or FRC index, never changes
	 * */
	protected int[]    theCastlingRook;
	/**	position flags	 */
	protected int theFlags;
	/**	current ply	 */
	protected int thePly;
	/**	number of silent plies
	 *	(w/out capturing and moving a pawn)
	 */
	protected int theSilentPlies;
	/**	move count	*/
	protected int firstPly;

	protected static Random randomFRC = new Random();

	/*
	 *  Shuffle Chess variants
	 */
	public static final int CLASSIC_CHESS   = 0;
	public static final int FISCHER_RANDOM  = 1;
	public static final int SHUFFLE_CHESS   = 2;

	/*  FEN variants
		all variants are accepted as input.
		For output it depends where we have ot use them
		- FRC enabled UCI engines accept only Shredder-FEN
		- not FRC enabled engines accept only classic FEN
		- we put XFEN into the clipboard, because it's the most compatible variant
	*/
	//  stricly classic FEN, no FRC castlings
	public static final int FEN_CLASSIC     = 1;
	//  XFEN: FRC castlings are identified with KQ
	public static final int XFEN                    = 2;
	//  ShredderFEN: FRC castlings are always identifier by file
	public static final int SHREDDER_FEN    = 3;
	public static final int SIMPLE_FEN    = 4;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public Board()
	{
		theBoard = new Piece[OUTER_BOARD_SIZE];
		theFlags = 0;
		thePly = 0;
		theSilentPlies = 0;
		firstPly = 0;

		theWhitePieces = new ArrayList[KING+1];
		theBlackPieces = new ArrayList[KING+1];
		for (int p=PAWN; p<=KING; p++) {
			theWhitePieces[p] = new ArrayList((p==PAWN) ? 8:2);
			theBlackPieces[p] = new ArrayList((p==PAWN) ? 8:2);
		}

        theWhitePromoPieces = new Piece[FILE_H+1];
        theBlackPromoPieces = new Piece[FILE_H+1];
		theCastlingRook = new int[4];

		initBorder();
	}

	//-------------------------------------------------------------------------------
	//	basic access
	//-------------------------------------------------------------------------------

	public final Piece piece(int idx)				{ return theBoard[idx]; }
	public final Piece piece(int file, int row)		{ return piece(EngUtil.square(file,row)); }

	public final int pieceAt(int idx) {
		Piece piece = theBoard[idx];
		return (piece!=null) ? piece.piece() : EMPTY;
	}

	public final int pieceAt(int file, int row)		{ return pieceAt(EngUtil.square(file,row)); }

	public final boolean isEmpty(int idx)			{ return theBoard[idx] == null; }
	public final boolean isEmpty(int file, int row)	{ return isEmpty(EngUtil.square(file,row)); }

	public final List pieceList(int p) {
		if (EngUtil.isWhite(p))
			return theWhitePieces[EngUtil.uncolored(p)];
		else
			return theBlackPieces[EngUtil.uncolored(p)];
	}

	public final Piece getPiece(int pc, int index)
	{
		List v = pieceList(pc);
		return (Piece)v.get(index);
	}


	public final King whiteKing()
	{
		List list = pieceList(WHITE_KING);
		if (list.isEmpty())
			return null;
		else
			return (King)list.get(0);
	}

	public final King blackKing()
	{
		List list = pieceList(BLACK_KING);
		if (list.isEmpty())
			return null;
		else
			return (King)list.get(0);
	}

	public final King king(int color)
	{
		if (EngUtil.isWhite(color))
			return whiteKing();
		else
			return blackKing();
	}

	public final int kingSquare(int color)
	{
		King king = king(color);
		if (king==null)
			return 0;
		else
			return king.square();
	}


	public void setCastlingRookSquare(int castling, int square)
	{
		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
		case WHITE_KING_ROOK_HOME:      theCastlingRook[0]=square; break;
		case WHITE_QUEENS_CASTLING:
		case WHITE_QUEEN_ROOK_HOME:     theCastlingRook[1]=square; break;
		case BLACK_KINGS_CASTLING:
		case BLACK_KING_ROOK_HOME:      theCastlingRook[2]=square; break;
		case BLACK_QUEENS_CASTLING:
		case BLACK_QUEEN_ROOK_HOME:     theCastlingRook[3]=square; break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public void setCastlingRookFile(int castling, int file)
	{
		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
		case WHITE_KING_ROOK_HOME:
		case WHITE_QUEENS_CASTLING:
		case WHITE_QUEEN_ROOK_HOME:     setCastlingRookSquare(castling, EngUtil.square(file,ROW_1)); break;
		case BLACK_KINGS_CASTLING:
		case BLACK_KING_ROOK_HOME:
		case BLACK_QUEENS_CASTLING:
		case BLACK_QUEEN_ROOK_HOME:     setCastlingRookSquare(castling, EngUtil.square(file,ROW_8)); break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public final int castlingRookSquare(int castling)
	{
		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
		case WHITE_KING_ROOK_HOME:      return theCastlingRook[0];
		case WHITE_QUEENS_CASTLING:
		case WHITE_QUEEN_ROOK_HOME:     return theCastlingRook[1];
		case BLACK_KINGS_CASTLING:
		case BLACK_KING_ROOK_HOME:      return theCastlingRook[2];
		case BLACK_QUEENS_CASTLING:
		case BLACK_QUEEN_ROOK_HOME:     return theCastlingRook[3];
		default:
			throw new IllegalArgumentException();
		}
	}

	public final King oppositeKing(int color)
	{
		if (EngUtil.isBlack(color))
			return whiteKing();
		else
			return blackKing();
	}

	public final List[] allPieces(int color)
	{
		if (EngUtil.isWhite(color))
			return theWhitePieces;
		else
			return theBlackPieces;
	}

	public final List[] allOppositePieces(int color)
	{
		if (EngUtil.isBlack(color))
			return theWhitePieces;
		else
			return theBlackPieces;
	}

	/**
	 * sets a piece at a given square
	 */
	public void addPiece(int square, int p)
	{
		List list = pieceList(p);
		Piece piece = null;
		for (int i=0; i<list.size(); i++) {
			Piece pc = (Piece)list.get(i);
			if (pc.isVacant()) {
				piece = pc;
				break;
			}
		}

		if (piece==null) {
			piece = Piece.newPiece(p,this);
			piece.setListIndex(list.size());
			list.add(piece);
		}

		piece.setSquare(square);
		theBoard[square] = piece;
	}

	public final void addPiece(int file, int row, int piece)		{
		addPiece(EngUtil.square(file,row), piece);
	}

	public final void addPiece(Piece thatPiece)
	{
		addPiece(thatPiece.square,thatPiece.piece);
		if (thatPiece.pawnFile > 0) {
			//  copy promotion status
			Piece thisPiece = theBoard[thatPiece.square];
			thisPiece.setPawnFile(thatPiece.pawnFile);
			setPromoPiece(thisPiece.color(),thisPiece.pawnFile,thisPiece);
		}
	}

    public Piece getPromoPiece(int color, int file)
    {
        if (EngUtil.isWhite(color))
           return theWhitePromoPieces[file];
        else
           return theBlackPromoPieces[file];
    }

    public void setPromoPiece(int color, int file, Piece piece)
    {
        if (EngUtil.isWhite(color))
            theWhitePromoPieces[file] = piece;
        else
            theBlackPromoPieces[file] = piece;
    }

	public final Piece getUnpromotedPiece(int pc) {
		List list = pieceList(pc);
		Piece piece;
		for (int i=list.size()-1; i>=0; i--) {
			piece = (Piece)list.get(i);
			if (piece.isVacant())
				return piece;
		}

        //  else: add new, promoted piece
		piece = Piece.newPiece(pc,this);
        piece.setListIndex(list.size());
        list.add(piece);
        return piece;
	}

	public void movePiece(Piece piece, int from, int to)
	{
		if (piece==null) piece = piece(from);

		piece.setSquare(to);
		theBoard[from] = null;
		theBoard[to] = piece;
		//  make sure to assign not null, in case from==to
	}

	public void move2Pieces(Piece a_piece, int a_from, int a_to,
	                                    Piece b_piece, int b_from, int b_to)
	{
		if (a_piece==null) a_piece = piece(a_from);
		if (b_piece==null) b_piece = piece(b_from);

		a_piece.setSquare(a_to);
		b_piece.setSquare(b_to);

		theBoard[a_from] = null;
		theBoard[b_from] = null;

		theBoard[a_to] = a_piece;
		theBoard[b_to] = b_piece;
		//  make sure to assign not null, in case from==to
	}

	public void deletePiece(int square)
	{
		Piece piece = piece(square);
		if (piece != null)
			piece.setVacant();
		theBoard[square] = null;
	}

	public void deletePiece(Piece piece)
	{
		theBoard[piece.square()] = null;
		piece.setVacant();
	}

	protected void restorePiece(int square, Piece piece)
	{
		theBoard[square] = piece;
		piece.setSquare(square);
	}


	public final int flags()						{ return theFlags; }

	public final int movesNext()					{ return theFlags & NEXT_MOVE; }
	public final int movedLast()					{ return (theFlags & NEXT_MOVE) ^ NEXT_MOVE; }

	public final boolean whiteMovesNext()			{ return EngUtil.allOf(theFlags, WHITE); }
	public final boolean blackMovesNext()			{ return EngUtil.allOf(theFlags, BLACK); }

	public final boolean movesNext(int color)		{ return EngUtil.allOf(theFlags, EngUtil.colorOf(color));	}
	public final boolean movedLast(int color)		{ return EngUtil.noneOf(theFlags, EngUtil.colorOf(color));	}

	public final void setMovesNext(int color)		{ theFlags = EngUtil.setMask(theFlags, NEXT_MOVE, color); }

	public final void toggleNext()					{ theFlags ^= NEXT_MOVE; };

	public final int enPassantFile()				{ return theFlags & EN_PASSANT_FILE; }

	public final boolean canEnPassant()				{ return EngUtil.anyOf(theFlags,EN_PASSANT_FILE); }

	public final boolean canEnPassant(int pawnSquare, int targetPawn)
	{
		int oppositePawn = EngUtil.oppositeColor(targetPawn);
		return (pieceAt(pawnSquare+1)==oppositePawn) ||
			   (pieceAt(pawnSquare-1)==oppositePawn);
	}

	public final boolean canCastle(int castling)	{ return EngUtil.canCastle(theFlags,castling); }

	public final boolean canCastle()				{ return EngUtil.anyOf(theFlags,CASTLING); }

	public final int ply()							{ return thePly; }

	public final int silentPlies()					{ return theSilentPlies; }

	public final void setSilentPlies(int plies)		{ theSilentPlies = plies; }

	public final int firstPly()						{ return firstPly; }

	public final void setFirstPly(int ply)			{ firstPly = ply; }

	public final void setFirstMove(int move, int color)
	{
		setMovesNext(color);

		if (EngUtil.isWhite(color))
			setFirstPly(2*(move-1));
		else
			setFirstPly(2*(move-1)+1);

	}

	public final void userSetCastling(boolean whiteKingSide, boolean whiteQueenSide,
	                                  boolean blackKingSide, boolean blackQueenSide,
	                                  boolean classicRules)
	{
		King whiteKing = whiteKing();
		King blackKing = blackKing();

		theFlags = Util.set(theFlags, WHITE_KING_HOME,
				(whiteKing!=null)
				&& (EngUtil.rowOf(whiteKing.square())==ROW_1)
				&& (whiteKingSide || whiteQueenSide));

		theFlags = Util.set(theFlags, BLACK_KING_HOME,
				(blackKing!=null)
				&& (EngUtil.rowOf(blackKing.square())==ROW_8)
				&& (blackKingSide || blackQueenSide));

		//  FRC
		//  by default, use the outermost rooks for castling
		//  TODO there's currently no option in the UI to change this
		setCastlingRookSquare(WHITE_KINGS_CASTLING, getOuterMostRook(WHITE_KINGS_CASTLING,classicRules));
		setCastlingRookSquare(WHITE_QUEENS_CASTLING, getOuterMostRook(WHITE_QUEENS_CASTLING,classicRules));

		setCastlingRookSquare(BLACK_KINGS_CASTLING, getOuterMostRook(BLACK_KINGS_CASTLING,classicRules));
		setCastlingRookSquare(BLACK_QUEENS_CASTLING, getOuterMostRook(BLACK_QUEENS_CASTLING,classicRules));

		theFlags = Util.set(theFlags, WHITE_KING_ROOK_HOME, (castlingRookSquare(WHITE_KINGS_CASTLING)!=0) && whiteKingSide);
		theFlags = Util.set(theFlags, WHITE_QUEEN_ROOK_HOME, (castlingRookSquare(WHITE_QUEENS_CASTLING)!=0) && whiteQueenSide);

		theFlags = Util.set(theFlags, BLACK_KING_ROOK_HOME, (castlingRookSquare(BLACK_KINGS_CASTLING)!=0) && blackKingSide);
		theFlags = Util.set(theFlags, BLACK_QUEEN_ROOK_HOME, (castlingRookSquare(BLACK_QUEENS_CASTLING)!=0) && blackQueenSide);
	}

	public boolean isEmptyForCastling(int castling)
	{
		//  FRC
		int rook_square = castlingRookSquare(castling);
		int king_square;
		int sqmin, sqmax;

		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
			king_square = kingSquare(WHITE);
			sqmin = Math.min(king_square+1,F1);
			sqmax = Math.max(rook_square-1,G1);
			break;
		case WHITE_QUEENS_CASTLING:
			king_square = kingSquare(WHITE);
			sqmin = Math.min(rook_square+1,C1);
			sqmax = Math.max(king_square-1,D1);
			break;
		case BLACK_KINGS_CASTLING:
			king_square = kingSquare(BLACK);
			sqmin = Math.min(king_square+1,F8);
			sqmax = Math.max(rook_square-1,G8);
			break;
		case BLACK_QUEENS_CASTLING:
			king_square = kingSquare(BLACK);
			sqmin = Math.min(rook_square+1,C8);
			sqmax = Math.max(king_square-1,D8);
			break;
		default:
			throw new IllegalArgumentException();
		}

		for (int sq=sqmin; sq<=sqmax; sq++)
			if (sq!=rook_square &&  sq!=king_square &&
					! isEmpty(sq))
						return false; //  other piece - no castling
		return true;
	}

	public final int gamePly()						{ return firstPly+thePly; }

	/**	@return the move number (starting at 1)	 */
	public final int gameMove()						{ return gamePly()/2 + 1; }

	public final boolean isCheck()					{ return EngUtil.isCheck(theFlags); }
	public final boolean isStalemate()				{ return EngUtil.isStalemate(theFlags); }
	public final boolean isMate()					{ return EngUtil.isMate(theFlags); }
	public final boolean isDraw3()					{ return EngUtil.isDraw3(theFlags); }
	public final boolean isDraw50()					{ return EngUtil.isDraw50(theFlags); }
	public final boolean isDrawMat()			    { return EngUtil.isDrawMat(theFlags); }

	public final boolean isGameFinished()			{ return EngUtil.isGameFinished(theFlags); }


	//-------------------------------------------------------------------------------
	//	setting up positions
	//-------------------------------------------------------------------------------

	/**
	 * completely clears the board
	 */
	public void clear()
	{
		for (int row=ROW_1; row<=ROW_8; row++)
			for (int file=FILE_A; file<=FILE_H; file++)
				theBoard[EngUtil.square(file,row)] = null;

		clearLists();

		for (int p=PAWN; p<=KING; p++) {
			trimList(theWhitePieces[p],0,10);
			trimList(theBlackPieces[p],0,10);
		}

		theFlags = 0;
		thePly = 0;
		firstPly = 0;
		theSilentPlies = 0;
	}


	protected void clearLists()
	{
		for (int file=FILE_A; file>=FILE_H; file++) {
			if (theWhitePromoPieces[file]!=null) theWhitePromoPieces[file].setPawnFile(0);
			if (theBlackPromoPieces[file]!=null) theBlackPromoPieces[file].setPawnFile(0);
			theWhitePromoPieces[file] = null;
			theBlackPromoPieces[file] = null;
		}

		for (int i=0; i < 4; i++) theCastlingRook[i]=0;
	}

	public int setupInitial()
	{
		clearLists();

		//  empty squares in the middle
		for (int row=ROW_3; row<=ROW_6; row++)
			for (int file=FILE_A; file<=FILE_H; file++)
				theBoard[EngUtil.square(file,row)] = null;

		//  white pawns
		for (int i=0; i<8; i++)
			setPiece(A2+i, WHITE_PAWN, i);

		//  black pawns
		for (int i=0; i<8; i++)
			setPiece(A7+i, BLACK_PAWN, i);

		setPiece(A1, WHITE_ROOK, 0);
		setPiece(B1, WHITE_KNIGHT, 0);
		setPiece(C1, WHITE_BISHOP, 0);
		setPiece(D1, WHITE_QUEEN, 0);
		setPiece(E1, WHITE_KING, 0);
		setPiece(F1, WHITE_BISHOP, 1);
		setPiece(G1, WHITE_KNIGHT, 1);
		setPiece(H1, WHITE_ROOK, 1);

		setPiece(A8, BLACK_ROOK, 0);
		setPiece(B8, BLACK_KNIGHT, 0);
		setPiece(C8, BLACK_BISHOP, 0);
		setPiece(D8, BLACK_QUEEN, 0);
		setPiece(E8, BLACK_KING, 0);
		setPiece(F8, BLACK_BISHOP, 1);
		setPiece(G8, BLACK_KNIGHT, 1);
		setPiece(H8, BLACK_ROOK, 1);

		setCastlingRookSquare(WHITE_QUEENS_CASTLING, A1);
		setCastlingRookSquare(WHITE_KINGS_CASTLING, H1);

		setCastlingRookSquare(BLACK_QUEENS_CASTLING, A8);
		setCastlingRookSquare(BLACK_KINGS_CASTLING, H8);

		trimList(theWhitePieces[PAWN],8,8);
		trimList(theWhitePieces[KNIGHT],2,10);
		trimList(theWhitePieces[BISHOP],2,10);
		trimList(theWhitePieces[ROOK],2,10);
		trimList(theWhitePieces[QUEEN],1,9);
		trimList(theWhitePieces[KING],1,1);

		trimList(theBlackPieces[PAWN],8,8);
		trimList(theBlackPieces[KNIGHT],2,10);
		trimList(theBlackPieces[BISHOP],2,10);
		trimList(theBlackPieces[ROOK],2,10);
		trimList(theBlackPieces[QUEEN],1,9);
		trimList(theBlackPieces[KING],1,1);

		//  all castlings allowed
		theFlags = 	WHITE |
					WHITE_KINGS_CASTLING | WHITE_QUEENS_CASTLING |
					BLACK_KINGS_CASTLING | BLACK_QUEENS_CASTLING;

		thePly = 0;
		theSilentPlies = 0;
		firstPly = 0;

		return 518;  //  the FRC index for classic chess
	}

	public static int randomFRC(int frcVariant)
	{
		switch (frcVariant)
		{
		default:
		case CLASSIC_CHESS:
			return 518;
		case SHUFFLE_CHESS:
			return randomFRC.nextInt(2880)+1;
		case FISCHER_RANDOM:
			return randomFRC.nextInt(960)+1;
		}
	}

	/** FRC */
	public int setupInitial(int frcVariant, int frcIndex)
	{
		if (frcIndex<0) frcIndex = randomFRC(frcVariant);

		if (frcIndex==518)
			return setupInitial();  //  classic chess start position
		else {
			String fen = initialFen(frcVariant,frcIndex);
			setupFEN(fen);
			return frcIndex;
		}
	}

	private static int firstFreeFile(StringBuffer buf, int offset, int count)
	{
		for (int file = 0; file <= 7; file++)
		{
			char c = buf.charAt(offset+file);
			if ((c=='.' || c=='*') && count--==0) return file;
		}
		return -1;
	}

	public final void setup(Object obj)
	{
		if (obj==null)
			setupInitial();
		else if (obj instanceof Board)
			setupBoard((Board)obj);
		else if (obj instanceof Number) {
			int frcIndex = ((Number)obj).intValue();
			if (frcIndex > 960)
				setupInitial(SHUFFLE_CHESS, frcIndex);
			else
				setupInitial(FISCHER_RANDOM, frcIndex); //  FRC index
		}
		else
			setupFEN(obj.toString());
	}

	/**
	 */
	protected void setupFEN(String fenString)
	{
		if (fenString==null || fenString.equals(START_POSITION)) {
			setupInitial();
			return;
		}

		clear();

		if (fenString.length()==0 || fenString.equals(EMPTY_POSITION))
			return;

		/*	the board	*/
		int file = FILE_A;
		int row = ROW_8;

		StringTokenizer tokens = new StringTokenizer(fenString," _\r\n\t");
		String s;

		s = tokens.nextToken();
		for(int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
			if(c == '/' || c==':') {
		        row--;
		        file = FILE_A;
		    }
		    else if(c >= '1' && c <= '8')
		        file += c - '0';
		    else {
		        int p = EngUtil.char2Piece(c);
		        if(p != EMPTY)
		            addPiece(EngUtil.square(file++,row), p);
		    }
		}

		/*	who moves next	*/
		if (tokens.hasMoreTokens())
			switch(tokens.nextToken().charAt(0)) {
			case 'w':	theFlags = EngUtil.plus(theFlags,WHITE); break;
			case 'b':	theFlags = EngUtil.plus(theFlags,BLACK); break;
			}

		if (tokens.hasMoreTokens()) {
			s = tokens.nextToken();
			for(int i = 0; i < s.length(); i++) {
			    char c = s.charAt(i);
				if (c>='a' && c<='h') {
					//  black FRC castling
					file = EngUtil.char2File(c);
					int kingFile = EngUtil.fileOf(blackKing().square());
					if (file < kingFile)
						setCastling(BLACK_QUEENS_CASTLING,file);
					else if (file > kingFile)
						setCastling(BLACK_KINGS_CASTLING,file);
				}
				else if (c>='A' && c<='H') {
					//  white FRC castling
					file = EngUtil.char2File(Character.toLowerCase(c));
					int kingFile = EngUtil.fileOf(kingSquare(WHITE));
					if (kingFile>0) {
						if (file < kingFile)
							setCastling(WHITE_QUEENS_CASTLING,file);
						else if (file > kingFile)
							setCastling(WHITE_KINGS_CASTLING,file);
					}
				}
			    else switch(c) {
				case LOWER_KING_CHAR:
					setCastling(BLACK_KINGS_CASTLING,0);
					break;
				case UPPER_KING_CHAR:
			        setCastling(WHITE_KINGS_CASTLING,0);
					break;
				case LOWER_QUEEN_CHAR:
			        setCastling(BLACK_QUEENS_CASTLING,0);
					break;
				case UPPER_QUEEN_CHAR:
			        setCastling(WHITE_QUEENS_CASTLING,0);
					break;
			    }
			}
		}

		/*	en-passant square	*/
		if (tokens.hasMoreTokens()) {
			char c = tokens.nextToken().charAt(0);
			if(c >= 'a' && c <= 'h')
			    theFlags = EngUtil.plus(theFlags, FILE_A + (c-'a'));
		}

		/*	silent plies	*/
		if (tokens.hasMoreTokens())
			setSilentPlies(Integer.parseInt(tokens.nextToken()));

		/*	move count plies	*/
		if (tokens.hasMoreTokens()) {
			int move = Integer.parseInt(tokens.nextToken());
			if (whiteMovesNext())
				setFirstPly((move-1)*2);
			else
				setFirstPly((move-1)*2 + 1);
		}
	}

	/** FRC */
	private boolean setCastling(int castling, int rook_file)
	{
		int row=0;
		King king=null;

		switch (castling) {
		case WHITE_KINGS_CASTLING:
		case WHITE_QUEENS_CASTLING:
			row = ROW_1;
			king = king(WHITE);
			break;
		case BLACK_KINGS_CASTLING:
		case BLACK_QUEENS_CASTLING:
			row = ROW_8;
			king = king(BLACK);
			break;
		}

		if (king==null || EngUtil.rowOf(king.square())!=row)
			return false;

		int rook_square;
		if (rook_file <=0)
			rook_square = getOuterMostRook(castling,false);
		else
			rook_square = EngUtil.square(rook_file,row);

		if (rook_square<=0) return false;

		Piece pc = piece(rook_square);
		if (pc==null || !pc.isRook()) return false;

		switch (castling) {
		case WHITE_KINGS_CASTLING:
		case BLACK_KINGS_CASTLING:
			if (king.square()>=rook_square) return false;
			break;
		case WHITE_QUEENS_CASTLING:
		case BLACK_QUEENS_CASTLING:
			if (king.square()<=rook_square) return false;
			break;
		}

		theFlags = EngUtil.plus(theFlags, castling);
		setCastlingRookSquare(castling, rook_square);
		return true;
	}

	/** FRC */
	public boolean isClassic()
	{
		if (canCastle(WHITE_KINGS_CASTLING))
		{
			if (kingSquare(WHITE)!=E1 || castlingRookSquare(WHITE_KINGS_CASTLING)!=H1) return false;
		}
		if (canCastle(WHITE_QUEENS_CASTLING))
		{
			if (kingSquare(WHITE)!=E1 || castlingRookSquare(WHITE_QUEENS_CASTLING)!=A1) return false;
		}
		if (canCastle(BLACK_KINGS_CASTLING))
		{
			if (kingSquare(BLACK)!=E8 || castlingRookSquare(BLACK_KINGS_CASTLING)!=H8) return false;
		}
		if (canCastle(BLACK_QUEENS_CASTLING))
		{
			if (kingSquare(BLACK)!=E8 || castlingRookSquare(BLACK_QUEENS_CASTLING)!=A8) return false;
		}
		return true;
	}

	/** FRC */
	public int getOuterMostRook(int castling, boolean classicRules)
	{
		int kingfile,file;
		Piece pc;

		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
			if (classicRules) {
				pc = piece(H1);
				if (pc!=null && pc.isRook() && pc.isWhite()) return pc.square();
			}
			else {
				kingfile = EngUtil.fileOf(whiteKing().square());
				for (file = FILE_H; file > kingfile; file--)
				{
					pc = piece(file,ROW_1);
					if (pc!=null && pc.isRook() && pc.isWhite()) return pc.square();
				}
			}
			break;

		case WHITE_QUEENS_CASTLING:
			if (classicRules) {
				pc = piece(A1);
				if (pc!=null && pc.isRook() && pc.isWhite()) return pc.square();
			}
			else {
				kingfile = EngUtil.fileOf(whiteKing().square());
				for (file = FILE_A; file < kingfile; file++)
				{
					pc = piece(file,ROW_1);
					if (pc!=null && pc.isRook() && pc.isWhite()) return pc.square();
				}
			}
			break;

		case BLACK_KINGS_CASTLING:
			if (classicRules) {
				pc = piece(H8);
				if (pc!=null && pc.isRook() && pc.isBlack()) return pc.square();
			}
			else {
				kingfile = EngUtil.fileOf(blackKing().square());
				for (file = FILE_H; file > kingfile; file--)
				{
					pc = piece(file,ROW_8);
					if (pc!=null && pc.isRook() && pc.isBlack()) return pc.square();
				}
			}
			break;

		case BLACK_QUEENS_CASTLING:
			if (classicRules) {
				pc = piece(A8);
				if (pc!=null && pc.isRook() && pc.isBlack()) return pc.square();
			}
			else {
				kingfile = EngUtil.fileOf(blackKing().square());
				for (file = FILE_A; file < kingfile; file++)
				{
					pc = piece(file,ROW_8);
					if (pc!=null && pc.isRook() && pc.isBlack()) return pc.square();
				}
			}
			break;
		}
		return 0;
	}


	/**
	 */
	protected void setupBoard(Board that)
	{
		if (that==null) {
			setupInitial();
			return;
		}

		clear();

		/*	the board	*/
		for (int pc = PAWN; pc <= KING; pc++)
		{
			List pl = that.pieceList(WHITE+pc);
			for (int i=0; i < pl.size(); i++)
			{
				Piece p = (Piece)pl.get(i);
				if (!p.isVacant())
					this.addPiece(p);
			}
			pl = that.pieceList(BLACK+pc);
			for (int i=0; i < pl.size(); i++)
			{
				Piece p = (Piece)pl.get(i);
				if (!p.isVacant())
					this.addPiece(p);
			}
		}

		this.theFlags = that.theFlags;
		this.theSilentPlies = that.theSilentPlies;
		this.firstPly = that.firstPly;

		for (int i=0; i < 4; i++)
			this.theCastlingRook[i] = that.theCastlingRook[i];
	}



	private Piece thisCopyOf(Piece thatPiece)
	{
		if (thatPiece==null)
			return null;
		else
			return this.piece(thatPiece.square());
	}

	public boolean equals(Object that)
	{
		if (that==null)
			return this.equals((String)null);
		else if (that instanceof Board)
			return this.equalsBoard((Board)that);
		else
			return this.equalsFEN(that.toString());
	}

	protected boolean equalsBoard(Board that)
	{
		/*	the board	*/
		for (int row = ROW_1; row <= ROW_8; row++)
			for (int file = FILE_A; file <= FILE_H; file++)
				if (this.pieceAt(file,row) != that.pieceAt(file,row)) return false;

		if (canCastle(WHITE_KINGS_CASTLING) && this.castlingRookSquare(WHITE_KINGS_CASTLING) != that.castlingRookSquare(WHITE_KINGS_CASTLING)) return false;
		if (canCastle(WHITE_QUEENS_CASTLING) && this.castlingRookSquare(WHITE_QUEENS_CASTLING) != that.castlingRookSquare(WHITE_QUEENS_CASTLING)) return false;
		if (canCastle(BLACK_KINGS_CASTLING) && this.castlingRookSquare(BLACK_KINGS_CASTLING) != that.castlingRookSquare(BLACK_KINGS_CASTLING)) return false;
		if (canCastle(BLACK_QUEENS_CASTLING) && this.castlingRookSquare(BLACK_QUEENS_CASTLING) != that.castlingRookSquare(BLACK_QUEENS_CASTLING)) return false;

		return  (this.theFlags == that.theFlags) &&
		        (this.theSilentPlies == that.theSilentPlies) &&
		        (this.firstPly == that.firstPly);
	}

	protected boolean equalsFEN(String fenString)
	{
		if (fenString==null) fenString = START_POSITION;

		/*	the board	*/
		int file = FILE_A;
		int row = ROW_8;

		StringTokenizer tokens = new StringTokenizer(fenString," _");
		String s;

		s = tokens.nextToken();
		for(int i = 0; i < s.length(); i++) {
		    char c = s.charAt(i);
			if(c == '/' || c==':') {
		        row--;
		        file = FILE_A;
		    }
		    else if(c >= '1' && c <= '8') {
			    int skip = c-'0';
			    while (skip-- > 0)
				    if (!this.isEmpty(file++,row)) return false;
		    }
		    else {
		        int p = EngUtil.char2Piece(c);
		        if(p != EMPTY) {
			        if (this.pieceAt(file++,row) != p)
				        return false;
		        }
		    }
		}

		/*	who moves next	*/
		if (tokens.hasMoreTokens())
			switch(tokens.nextToken().charAt(0)) {
			case 'w':	if (!this.whiteMovesNext()) return false; break;
			case 'b':	if (!this.blackMovesNext()) return false; break;
			}

		/*	castling priviliges	*/
		int thatFlags = 0;
		int king_file,rook_file,rook_square;
		if (tokens.hasMoreTokens()) {
			s = tokens.nextToken();
			for(int i = 0; i < s.length(); i++) {
			    char c = s.charAt(i);
				if (c>='a' || c<='h') {
					//  FRC FEN
					king_file = kingSquare(BLACK);
					rook_file = EngUtil.char2File(c);
					rook_square = EngUtil.square(rook_file,ROW_1);
					if (rook_file < king_file) {
						if (rook_square!=castlingRookSquare(BLACK_QUEENS_CASTLING)) return false;
						thatFlags = EngUtil.plus(thatFlags,BLACK_QUEENS_CASTLING);
					}
					else {
						if (rook_square!=castlingRookSquare(BLACK_KINGS_CASTLING)) return false;
						thatFlags = EngUtil.plus(thatFlags,BLACK_KINGS_CASTLING);
					}
				}
				else if (c>='A' || c<='H') {
					//  TODO FRC FEN
					king_file = kingSquare(WHITE);
					rook_file = EngUtil.char2File(Character.toLowerCase(c));
					if (rook_file < king_file)
						thatFlags = EngUtil.plus(thatFlags,BLACK_QUEENS_CASTLING);
					else
						thatFlags = EngUtil.plus(thatFlags,BLACK_KINGS_CASTLING);
				}
			    switch(c) {
				case LOWER_KING_CHAR:
					rook_file = getOuterMostRook(BLACK_KINGS_CASTLING,false);
					rook_square = EngUtil.square(rook_file,ROW_8);
					if (rook_square!=castlingRookSquare(BLACK_KINGS_CASTLING)) return false;
					thatFlags = EngUtil.plus(thatFlags, BLACK_KINGS_CASTLING);
					break;
				case UPPER_KING_CHAR:
					rook_file = getOuterMostRook(WHITE_KINGS_CASTLING,false);
					rook_square = EngUtil.square(rook_file,ROW_1);
					if (rook_square!=castlingRookSquare(WHITE_KINGS_CASTLING)) return false;
			        thatFlags = EngUtil.plus(thatFlags, WHITE_KINGS_CASTLING);
					break;
				case LOWER_QUEEN_CHAR:
					rook_file = getOuterMostRook(BLACK_QUEENS_CASTLING,false);
					rook_square = EngUtil.square(rook_file,ROW_8);
					if (rook_square!=castlingRookSquare(BLACK_QUEENS_CASTLING)) return false;
			        thatFlags = EngUtil.plus(thatFlags, BLACK_QUEENS_CASTLING);
					break;
				case UPPER_QUEEN_CHAR:
					rook_file = getOuterMostRook(WHITE_QUEENS_CASTLING,false);
					rook_square = EngUtil.square(rook_file,ROW_1);
					if (rook_square!=castlingRookSquare(WHITE_QUEENS_CASTLING)) return false;
			        thatFlags = EngUtil.plus(thatFlags, WHITE_QUEENS_CASTLING);
					break;
			    }
			}
		}

		/*	en-passant square	*/
		if (tokens.hasMoreTokens()) {
			char c = tokens.nextToken().charAt(0);
			if(c >= 'a' && c <= 'h')
			    thatFlags = EngUtil.plus(thatFlags, FILE_A + (c-'a'));
		}
		if (this.theFlags != thatFlags) return false;

		/*	silent plies	*/
		int thatSilentPlies = 0;
		if (tokens.hasMoreTokens())
			thatSilentPlies = Integer.parseInt(tokens.nextToken());
		if (thatSilentPlies != this.silentPlies()) return false;

		/*	move count plies	*/
		int thatFirstPly = 0;
		if (tokens.hasMoreTokens()) {
			int move = Integer.parseInt(tokens.nextToken());
			if (whiteMovesNext())
				thatFirstPly = (move-1)*2;
			else
				thatFirstPly = (move-1)*2 + 1;
		}
		if (this.firstPly() != thatFirstPly) return false;

		//  else:
		return true;
	}

	private Piece setPiece(int square, int p, int index)
	{
		List pieceList = pieceList(p);
		while (pieceList.size() <= index) {
			Piece pc = Piece.newPiece(p,this);
			pc.setListIndex(pieceList.size());
			pc.setVacant();
			pieceList.add(pc);
		}

		Piece piece = (Piece)pieceList.get(index);
		piece.setSquare(square);
		piece.setPawnFile(0);
		return (theBoard[square] = piece);
	}

	private void trimList(List list, int from, int max)
	{
		while (from < list.size()) {
			Piece p = (Piece)list.get(from++);
			p.setVacant();
			p.setPawnFile(0);
		}
		if (list.size() > max)
			throw new IllegalStateException("warning: piece list exceeds max. length, "+(list.size())+" > "+max);
			//	indicates inconsistency in piece list
	}

	/** FRC */
	public static String initialFen(int frcVariant, int frc_index)
	{
		if (frcVariant==CLASSIC_CHESS) return START_POSITION;

		if (frc_index < 0) frc_index = randomFRC(frcVariant);

		StringBuffer buf = new StringBuffer(POSITION_TEMPLATE);

		int b0 = buf.indexOf(".");  //  placeholder for black pieces
		int w0 = buf.indexOf("*");  //  placeholder for white pieces
		int c0 = buf.indexOf("-");  //  placeholder for castling privileges

		//  FRC
		//  light square bishops
		int lb_file = 1+2*(frc_index%4);   // file of light bishop
		buf.setCharAt(b0+lb_file, EngUtil.coloredPieceCharacter(BLACK_BISHOP));
		buf.setCharAt(w0+lb_file, EngUtil.coloredPieceCharacter(WHITE_BISHOP));

		//  dark square bishops
		int db_file = 2*((frc_index/4)%4);  // file of dark bishop
		buf.setCharAt(b0+db_file, EngUtil.coloredPieceCharacter(BLACK_BISHOP));
		buf.setCharAt(w0+db_file, EngUtil.coloredPieceCharacter(WHITE_BISHOP));

		//  queen
		int q_idx = (frc_index/16)%6;    // index of queen 0..5
		int q_file = firstFreeFile(buf,b0,q_idx);

		buf.setCharAt(b0+q_file, EngUtil.coloredPieceCharacter(BLACK_QUEEN));
		buf.setCharAt(w0+q_file, EngUtil.coloredPieceCharacter(WHITE_QUEEN));

		//  knights
		int rest = (frc_index/96)%10;
		int n1_idx,n2_idx;  //  index of knights 0..4
		if (rest<4) {
			n1_idx=0;
			n2_idx=1+rest;
		}
		else if (rest<7) {
			n1_idx=1;
			n2_idx=rest-2;
		}
		else if (rest<9) {
			n1_idx=2;
			n2_idx=rest-4;
		}
		else {
			n1_idx=3;
			n2_idx=4;
		}

		int n1_file = firstFreeFile(buf,b0,n1_idx);
		int n2_file = firstFreeFile(buf,b0,n2_idx);

		buf.setCharAt(b0+n1_file, EngUtil.coloredPieceCharacter(BLACK_KNIGHT));
		buf.setCharAt(b0+n2_file, EngUtil.coloredPieceCharacter(BLACK_KNIGHT));

		buf.setCharAt(w0+n1_file, EngUtil.coloredPieceCharacter(WHITE_KNIGHT));
		buf.setCharAt(w0+n2_file, EngUtil.coloredPieceCharacter(WHITE_KNIGHT));

		//  king
		int k_file;
		switch (frcVariant)
		{
		default:
		case FISCHER_RANDOM:
			//  king must be placed between the rooks
			k_file = firstFreeFile(buf,b0,1);
			break;
		case SHUFFLE_CHESS:
			//  king can be placed on one of the three remaining files
			rest = (frc_index/960+1)%3;
			k_file = firstFreeFile(buf,b0,rest);
			break;
		}

		buf.setCharAt(b0+k_file, EngUtil.coloredPieceCharacter(BLACK_KING));
		buf.setCharAt(w0+k_file, EngUtil.coloredPieceCharacter(WHITE_KING));

		//  rooks on two remaining squares
		int r1_file = firstFreeFile(buf,b0,0);
		int r2_file = firstFreeFile(buf,b0,1);

		buf.setCharAt(b0+r1_file, EngUtil.coloredPieceCharacter(BLACK_ROOK));
		buf.setCharAt(b0+r2_file, EngUtil.coloredPieceCharacter(BLACK_ROOK));

		buf.setCharAt(w0+r1_file, EngUtil.coloredPieceCharacter(WHITE_ROOK));
		buf.setCharAt(w0+r2_file, EngUtil.coloredPieceCharacter(WHITE_ROOK));

		//  castling privileges
		switch (frcVariant)
		{
		default:
		case FISCHER_RANDOM:
			//  all castlings are allowed in the initial pos of FRC
			buf.replace(c0,c0+1,"KQkq");    //  this is XFEN convention
			break;
		case SHUFFLE_CHESS:
			//  castling privileges depend on king and rook placement
			if (k_file!=(FILE_E-FILE_A)) break;
			boolean queen_side = (FILE_A+r1_file)==FILE_A;
			boolean king_side = (FILE_A+r2_file)==FILE_H;
			if (queen_side&&king_side)
				buf.replace(c0,c0+1,"KQkq");
			else if (king_side)
				buf.replace(c0,c0+1,"Kk");
			else if (queen_side)
				buf.replace(c0,c0+1,"Qq");
			break;
		}

		return buf.toString();
	}

	/**
	 * @return a FEN representation of the current position
	 */
	public String toString() {
		return toString(XFEN);
	}

	public String toString(int fenVariant)
	{
		/*	the board	*/
		StringBuffer buf = new StringBuffer();
		int empty_count = 0;
		int row = ROW_8;
		int file = FILE_A;

		while (row >= ROW_1) {
			if (isEmpty(file,row))
				empty_count++;
			else {
				empty_count = flushEmptyCount(buf,empty_count);
				buf.append(EngUtil.coloredPieceCharacter(pieceAt(file,row)));
			}

			file++;

			if (file > FILE_H) {
				/*	reached right border	*/
				empty_count = flushEmptyCount(buf,empty_count);
				row--;
				file = FILE_A;
				if (row >= ROW_1)
					buf.append('/');
			}
		}

		if (fenVariant==SIMPLE_FEN)
			return buf.toString();

		/*	who moves next ?	*/
		buf.append(' ');
		if (whiteMovesNext())
			buf.append(WHITE_CHAR);
		else if (blackMovesNext())
			buf.append(BLACK_CHAR);
		else
			buf.append('-');	//	no proper FEN, but well...

		/*	which castling privileges ?	*/
		buf.append(' ');
		boolean anyCastle = false;
		int rsq;
		if (canCastle(WHITE_KINGS_CASTLING)) 	{
			rsq = castlingRookSquare(WHITE_KINGS_CASTLING);
			switch (fenVariant)
			{
			case FEN_CLASSIC:
				if (rsq==H1 && kingSquare(WHITE)==E1) {
					buf.append(UPPER_PIECE_CHARACTERS[KING]);
					anyCastle = true;
				}
				//  else: ignore this castling !
				break;
			case XFEN:
				if (rsq==getOuterMostRook(WHITE_KINGS_CASTLING,false))
					buf.append(UPPER_PIECE_CHARACTERS[KING]);   //  XFEN
				else
					buf.append(Character.toUpperCase(EngUtil.fileChar(EngUtil.fileOf(rsq))));   //  SHREDDER_FEN
				anyCastle=true;
				break;
			case SHREDDER_FEN:
				buf.append(Character.toUpperCase(EngUtil.fileChar(EngUtil.fileOf(rsq))));   //  SHREDDER_FEN
				anyCastle=true;
				break;
			}
		}
		if (canCastle(WHITE_QUEENS_CASTLING)) 	{
			rsq = castlingRookSquare(WHITE_QUEENS_CASTLING);
			switch (fenVariant)
			{
			case FEN_CLASSIC:
				if (rsq==A1 && kingSquare(WHITE)==E1) {
					buf.append(UPPER_PIECE_CHARACTERS[QUEEN]);
					anyCastle = true;
				}
				break;
			default:
			case XFEN:
				if (rsq==getOuterMostRook(WHITE_QUEENS_CASTLING,false))
					buf.append(UPPER_PIECE_CHARACTERS[QUEEN]);
				else
					buf.append(Character.toUpperCase(EngUtil.fileChar(EngUtil.fileOf(rsq))));
			//  FRC
				anyCastle=true;
				break;
			case SHREDDER_FEN:
				buf.append(Character.toUpperCase(EngUtil.fileChar(EngUtil.fileOf(rsq))));
				anyCastle=true;
				break;
			}
		}
		if (canCastle(BLACK_KINGS_CASTLING)) 	{
			rsq = castlingRookSquare(BLACK_KINGS_CASTLING);
			switch (fenVariant)
			{
			case FEN_CLASSIC:
				if (rsq==H8 && kingSquare(BLACK)==E8) {
					buf.append(LOWER_PIECE_CHARACTERS[KING]);
					anyCastle = true;
				}
				break;
			default:
			case XFEN:
				if (rsq==getOuterMostRook(BLACK_KINGS_CASTLING,false))
					buf.append(LOWER_PIECE_CHARACTERS[KING]);
				else
					buf.append(EngUtil.fileChar(EngUtil.fileOf(rsq)));
			//  FRC
				anyCastle=true;
				break;
			case SHREDDER_FEN:
				buf.append(EngUtil.fileChar(EngUtil.fileOf(rsq)));
				anyCastle=true;
				break;
			}
		}
		if (canCastle(BLACK_QUEENS_CASTLING)) 	{
			rsq = castlingRookSquare(BLACK_QUEENS_CASTLING);
			switch (fenVariant)
			{
			case FEN_CLASSIC:
				if (rsq==A8 && kingSquare(BLACK)==E8) {
					buf.append(LOWER_PIECE_CHARACTERS[QUEEN]);
					anyCastle = true;
				}
				break;
			default:
			case XFEN:
				if (rsq==getOuterMostRook(BLACK_QUEENS_CASTLING,false))
					buf.append(LOWER_PIECE_CHARACTERS[QUEEN]);
				else
					buf.append(EngUtil.fileChar(EngUtil.fileOf(rsq)));
			//  FRC
				anyCastle=true;
				break;
			case SHREDDER_FEN:
				buf.append(EngUtil.fileChar(EngUtil.fileOf(rsq)));
				anyCastle=true;
				break;
			}
		}

		if (!anyCastle)
			buf.append('-');

		/*	en passant square	*/
		buf.append(' ');
		if (!canEnPassant())
			buf.append('-');
		else {
			buf.append(EngUtil.fileChar(enPassantFile()));
			if (whiteMovesNext())
				buf.append(EngUtil.rowChar(ROW_6));
			else
				buf.append(EngUtil.rowChar(ROW_3));
		}

		/*	silent plies (w/out moving pawn)	*/
		buf.append(' ');
		buf.append(silentPlies());

		/*	move counter	*/
		buf.append(' ');
		buf.append(gameMove());

		return buf.toString();
	}

	public HashKey computeHashKey(boolean reversed)
	{
		HashKey key = new JoseHashKey(reversed);
		computeHashKey(key);
		return key;
	}

	public HashKey computeHashKey(HashKey key)
	{
		key.clear();
		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				if (!isEmpty(square))
					key.set(square, pieceAt(square));
			}

        key.set(theFlags);
		return key;
	}


	public boolean canMove(int from)
	{
		if (!EngUtil.innerSquare(from))
			return false;

		Piece moving = piece(from);
		if (moving==null) return false;

		if (whiteMovesNext())
			return moving.isWhite();
		if (blackMovesNext())
			return moving.isBlack();
		// else:
		throw new IllegalStateException();
	}

	/**
	 * fill out some field in 'mv' and
	 * @return if the move is geometrically correct
	 */
	public boolean checkMove(Move mv)
	{
		if (mv==Move.NULLMOVE) return true;

		if (!EngUtil.innerSquare(mv.from))
			return false;

		mv.moving = piece(mv.from);
		if (mv.moving==null) return false;

		if (mv.moving.isKing() && ((King)mv.moving).isCastlingGesture(this,mv))
		{
			//  FRC castling gesture; mv.to and flags is already set
			mv.captured = null;
		}
		else
		{
			//  regular move
			if (!EngUtil.innerSquare(mv.to))
				return false;

			mv.captured = piece(mv.to);
			if (mv.captured!=null && !mv.moving.canCapture(mv.captured.piece()))
					return false;
		}

		if (whiteMovesNext())
			return mv.moving.isWhite();
		if (blackMovesNext())
			return mv.moving.isBlack();
		// else:
		throw new IllegalStateException();
	}

	/**
	 * change owner of a move
	 * @param mv
	 */
	public void chown(Move mv)
	{
		checkMove(mv);
	}

	protected static int countValid(List pieceList)
	{
		if (pieceList==null)	return 0;
		int count = 0;
		for (int i=pieceList.size()-1; i>=0; i--) {
            Piece p = (Piece)pieceList.get(i);
            if (! p.isVacant())
                count++;
        }
		return count;
	}

	protected static boolean countLargerOrEqual(List pieceList, int min)
	{
		if (pieceList==null)	return false;
		for (int i=pieceList.size()-1; i>=0; i--) {
            Piece p = (Piece)pieceList.get(i);
            if (! p.isVacant()) {
                if (--min <= 0) return true;
			}
        }
		return false;
	}

    protected static int countPromoted(List pieceList)
    {
        if (pieceList==null)	return 0;
        int count = 0;
        for (int i=pieceList.size()-1; i>=0; i--) {
            Piece p = (Piece)pieceList.get(i);
            if (! p.isVacant() && p.isPromotionPiece())
                count++;
        }
        return count;
    }

	protected static int countValid(List[] pieceList)
	{
		int total = 0;
		for (int j=pieceList.length-1; j>=0; j--)
			total += countValid(pieceList[j]);
		return total;
	}

	protected int countValid(int piece)
	{
		return countValid(pieceList(piece));
	}

	protected boolean countLargerOrEqual(int piece, int min)
	{
		return countLargerOrEqual(pieceList(piece),min);
	}

	public boolean couldCastle(int castling, boolean classicRules)
	{
		//  FRC
		int ksq=0;
		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
		case WHITE_QUEENS_CASTLING:
			ksq = kingSquare(WHITE);
			if (classicRules && (ksq!=E1)) return false;
			if (EngUtil.rowOf(ksq)!=ROW_1) return false;
			break;
		case BLACK_KINGS_CASTLING:
		case BLACK_QUEENS_CASTLING:
			ksq = kingSquare(BLACK);
			if (classicRules && (ksq!=E8)) return false;
			if (EngUtil.rowOf(ksq)!=ROW_8) return false;
			break;
		default:
			throw new IllegalArgumentException();
		}

		int rsq = getOuterMostRook(castling,classicRules);
		switch (castling)
		{
		case WHITE_KINGS_CASTLING:
			if (classicRules && (rsq!=H1)) return false;
			if (EngUtil.rowOf(rsq)!=ROW_1) return false;
			if (rsq <= ksq) return false;
			break;
		case WHITE_QUEENS_CASTLING:
			if (classicRules && (rsq!=A1)) return false;
			if (EngUtil.rowOf(rsq)!=ROW_1) return false;
			if (rsq >= ksq) return false;
			break;
		case BLACK_KINGS_CASTLING:
			if (classicRules && (rsq!=H8)) return false;
			if (EngUtil.rowOf(rsq)!=ROW_8) return false;
			if (rsq <= ksq) return false;
			break;
		case BLACK_QUEENS_CASTLING:
			if (classicRules && (rsq!=A8)) return false;
			if (EngUtil.rowOf(rsq)!=ROW_8) return false;
			if (rsq >= ksq) return false;
			break;
		}

		return true;
	}

	public boolean canMate(int color)
	{
		color = EngUtil.colorOf(color);

		if (countLargerOrEqual(color+PAWN,1))
			return true;		//	1 pawn can mate
		if ((countValid(color+KNIGHT)+countValid(color+BISHOP)) >= 2)
			return true;		//	2 officers can mate
		if (countLargerOrEqual(color+ROOK,1))
			return true;		//	1 roook can mate
		if (countLargerOrEqual(color+QUEEN,1))
			return true;		//	1 queen can mate
		//	else
		return false;
	}



	/**
	 * return a list of pseudo legal moves, given some constraints
	 *
	 * @param movingPiece type of movingPiece, uncolored, required
	 * @param orig_file origin file, optional
	 * @param orig_row origin row, optional
	 * @param destination square, required
	 * @param promo promotion piece, uncolored, required
	 * @return the number of moves
	 */
	public int getCandidateMoves(int movingPiece,
	                             int orig_file, int orig_row,
	                             int destination,
	                             int promo,
	                             Move[] moves)
	{
		int result = 0;
		boolean isPawn = (movingPiece==PAWN);
		movingPiece += movesNext();

		if ((orig_file > 0) && (orig_row > 0))
		{
			int sq = EngUtil.square(orig_file,orig_row);
			Piece pc = piece(sq);
			if (pc==null || pc.isVacant()) return 0;

			moves[result].from = sq;
			moves[result].to = destination;
			moves[result].flags = 0;

			if (pc.isPawn() && EngUtil.isPromotionRow(EngUtil.rowOf(destination), movesNext()))
				moves[result].flags = promo;

			checkMove(moves[result]);
			if (pc.checkMove(moves[result]))
				result++;
		}
		else {
			List pcs = pieceList(movingPiece);
			for (int i=0; i<pcs.size(); i++) {
				Piece pc = (Piece)pcs.get(i);

				if (pc.isVacant()) continue;

				int sq = pc.square();

				if (orig_file > 0 && EngUtil.fileOf(sq) != orig_file) continue;
				if (orig_row > 0 && EngUtil.rowOf(sq) != orig_row) continue;

				moves[result].from = sq;
				moves[result].to = destination;
				moves[result].flags = 0;

				if (isPawn && EngUtil.isPromotionRow(EngUtil.rowOf(destination), movesNext()))
					moves[result].flags = promo;

				if (checkMove(moves[result]) && pc.checkMove(moves[result]))
					result++;
			}
		}
		return result;
	}

	public Move decodeMove(short code)
	{
		//	find the moving piece
		Piece p = null;
		switch (code & 0x00f0)
		{
		case 0x0000:
		case 0x0010:	//	pawn
			p = getPiece(PAWN+movesNext(), (code>>2) & 0x0007);
			break;
		case 0x0020:	//	knight
			p = getPiece(KNIGHT+movesNext(), (code>>3) & 0x0001);
			break;
		case 0x0030:	//	king's bishop
			p = getPiece(BISHOP+movesNext(), 0);
			break;
		case 0x0040:	//	queen's bishop
			p = getPiece(BISHOP+movesNext(), 1);
			break;
		case 0x0050:	//	king's rook
			p = getPiece(ROOK+movesNext(), 0);
			break;
		case 0x0060:	//	queen's rook
			p = getPiece(ROOK+movesNext(), 1);
			break;
		case 0x0070:
		case 0x0080:	//	queen
			p = getPiece(QUEEN+movesNext(), 0);
			break;
		case 0x0090:	//	king
			p = getPiece(KING+movesNext(), 0);
			break;
		case 0x00a0:	//	promoting or promoted piece move
			if ((code & 0x0008) == 0x0008) {
				//	promoted piece
                int pawnFile = FILE_A + (code & 0x0007);
				p = getPromoPiece(movesNext(), pawnFile);
            }
            else {
                //  promoting pawn
				p = getPiece(PAWN+movesNext(), code & 0x0007);
			}
			break;
		case 0x009a:    //  null move
			return Move.NULLMOVE;
		}

		if (p==null)    //  error in move encoding; couldn't find appropriate piece !
			return null;

		Move result = new Move();
		p.decodeMove(code, result);
		return result;
	}


    public boolean checkBishopColors(int color)
    {
		//	strange colored bishops ?
        List pl = pieceList(color+BISHOP);
        boolean wasLight = false;
        int count = 0;;

        for (int i=pl.size()-1; i>=0; i--) {
            Bishop bp = (Bishop)pl.get(i);
            if (bp.isVacant()) continue;

            count++;
            boolean isLight = EngUtil.isLightSquare(bp.square());
            if (count==1)
                wasLight = isLight;
            else if (wasLight != isLight)
                return true;    //  there are two differently colored bishops - fine
        }
        return (count < 2);   // all bishops have the same color -> there must be underpromotions
	}

	public static void printBoard(String fen, PrintStream out)
	{
		int file = 0;
		int row = 7;
		int i=0;

		out.println("+----------------+");
		out.print("|");

		while (row>=0) {
			char c = fen.charAt(i++);
			if (c>='1' && c<='8') {
				file += (c-'0');
				while (c-- > '0') out.print(". ");
			}
			else if (c=='/') {
				/* noop */
			}
			else {
				out.print(c);
				out.print(" ");
				file++;
			}

			if (file > 7) {
				out.println("|");
				file = 0;
				row--;
				if (row >= 0) out.print("|");
			}
		}
		out.println("+----------------+");
	}

	public void printBoard()
	{
		printBoard(this.toString(),System.out);
	}

	//-------------------------------------------------------------------------------
	//	private parts
	//-------------------------------------------------------------------------------

	private void initBorder()
	{
		Piece block = Piece.newPiece(BLOCK,this);
		for (int row = 0; row < BORDER_HEIGHT; row++)
			for (int file=0; file < OUTER_BOARD_WIDTH; file++) {
				theBoard[EngUtil.square(file,row)] = block;
				theBoard[EngUtil.square(file,OUTER_BOARD_HEIGHT-1-row)] = block;
			}
		for (int row = ROW_1; row <= ROW_8; row++)
			for (int file=0; file < BORDER_WIDTH; file++) {
				theBoard[EngUtil.square(file,row)] = block;
				theBoard[EngUtil.square(OUTER_BOARD_WIDTH-1-file,row)] = block;
			}
	}

	private int flushEmptyCount(StringBuffer buf, int empty_count)
	{
		if (empty_count > 0)
			buf.append(empty_count);
		return 0;
	}
}
