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
import de.jose.util.map.ObjIntMap;
import de.jose.util.map.LongIntMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * current positions
 */

public class Position
		extends Board
{
	/**	compute hash key incrementally	 */
	public static final int INCREMENT_HASH				= 0x01000000;

	/** compute the reversed hash key incrementally */
	public static final int INCREMENT_REVERSED_HASH     = 0x02000000;

	/** when calculating hash keys, ignore en-passant status
	 *  (this is important because the Database search does not recogized ep status)
	 */
	public static final int IGNORE_FLAGS_ON_HASH        = 0x04000000;

	/** compute material signature incrementally    */
	public static final int INCREMENT_SIGNATURE         = 0x08000000;

	/**	look for exposed checks	 */
	public static final int EXPOSED_CHECK				= 0x10000000;

	public static final int DETECT_ALL					= 0xffffffff - IGNORE_FLAGS_ON_HASH;

	/**	max. stack size	 */
	protected static final int STACK_SIZE = 1024;

	/**	current hash key	 */
	protected HashKey theHashKey;

	/**	current reversed hash key	 */
	protected HashKey theReversedHashKey;

	/** current Material signature  */
	protected MatSignature theMatSignature;

	/**	enabled / disable hash key calculation	 */
	protected int option;

	/**	move stack	 */
	protected StackFrame[] theMoveStack;

	/** counts positions repetitions; maps hashkeys to counter  */
	protected LongIntMap hashCount;

	/**	move iterator	 */
	protected MoveIterator moveIterator;

	/**	move for check testing	 */
	protected Move testMove;

	protected String[] startFEN=new String[4];

	/**	variation stack	 */
	protected int[] variationPlies;
	protected Move[] variationMoves;
	protected int variationTop;

	/**	variaton stack size	 */
	protected static final int VAR_STACK_SIZE = 128;

	public Position()
	{
		this(JoseHashKey.class);
	}

	public Position (Class hashKeyClass)
	{
		super();
		theHashKey = HashKey.newHashKey(hashKeyClass,false);
		theReversedHashKey = HashKey.newHashKey(hashKeyClass,true);
		theMatSignature = new MatSignature();
		theMoveStack = new StackFrame[STACK_SIZE];
		hashCount = new LongIntMap();
		option = CHECK+/*STALEMATE+*/INCREMENT_HASH+EXPOSED_CHECK;
		moveIterator = new MoveIterator(this);
		testMove = new Move(0,0);

		variationPlies = new int[VAR_STACK_SIZE];
		variationMoves = new Move[VAR_STACK_SIZE];
		variationTop = 0;
	}

	public Position(Object initial)
	{
		this();
		setup(initial);
	}


	public void setOption(int newOption, boolean on)
	{
		setOptions(Util.set(option, newOption, on));
	}

	public int getOptions()
	{
		return option;
	}

	public void setOptions(int newOption)
	{
		boolean wasIncrementHash = hasOption(INCREMENT_HASH);
		boolean wasIncrementReversedHash = hasOption(INCREMENT_REVERSED_HASH);
		boolean wasIgnoreFlags = hasOption(IGNORE_FLAGS_ON_HASH);
		boolean wasIncrementSig = hasOption(INCREMENT_SIGNATURE);
		boolean was3Rep = hasOption(DRAW_3);

		option = newOption;

        boolean isIgnoreFlags = hasOption(IGNORE_FLAGS_ON_HASH);

        getHashKey().setIgnoreFlags(isIgnoreFlags);
        getReversedHashKey().setIgnoreFlags(isIgnoreFlags);

		if (hasOption(INCREMENT_HASH) &&
		   (!wasIncrementHash || (wasIgnoreFlags != isIgnoreFlags)))
				computeHashKey(theHashKey);
		if (hasOption(INCREMENT_REVERSED_HASH) &&
		   (!wasIncrementReversedHash || (wasIgnoreFlags != isIgnoreFlags)))
				computeHashKey(theReversedHashKey);
		if (hasOption(INCREMENT_SIGNATURE) && !wasIncrementSig)
				computeMatSig();
		if (hasOption(DRAW_3) && !was3Rep)
			computeHashCount();
	}


	public void computeHashKeys()
	{
		computeHashKey(theHashKey);
		computeHashKey(theReversedHashKey);
	}

	public void computeMatSig()
	{
		theMatSignature.setBoard(this);
	}

	public void computeHashCount()
	{
		hashCount.clear();
		for (int i=0; i < thePly; i++)
			incrHashCount(theMoveStack[i].hashValue);

		incrHashCount(theHashKey.value());
	}

	protected void incrHashCount(long key)
	{
		int current = hashCount.get(key);
		if (current <= 0)
			hashCount.put(key,1);
		else
			hashCount.put(key,current+1);
	}

	protected void decrHashCount(long key)
	{
		int current = hashCount.get(key);
		if (current > 1)
			hashCount.put(key,current-1);
		else if (current==1)
			hashCount.remove(key);
	}

	public void updateHashKeys()
	{
		if (!hasOption(INCREMENT_HASH))
			computeHashKey(theHashKey);
		if (!hasOption(INCREMENT_REVERSED_HASH))
			computeHashKey(theReversedHashKey);
		//  otherwise: the Hash Keys are incrementally kept up to date
	}

	public void updateMatSig()
	{
		if (!hasOption(INCREMENT_SIGNATURE))
			computeMatSig();
		//  otherwise: the Mat Sig is incrementally kept up to date
	}

	public final boolean hasOption(int anOption)
	{
		return Util.allOf(option,anOption);
	}

	public final Move move(int ply)
	{
		return theMoveStack[ply];
	}



	public void addPiece(int square, int p)	{
		if (hasOption(INCREMENT_HASH))
			theHashKey.set(square,p);
		if (hasOption(INCREMENT_REVERSED_HASH))
			theReversedHashKey.set(square,p);

		super.addPiece(square,p);
	}

	public void movePiece(Piece piece, int from, int to)
	{
		if (piece==null) piece = piece(from);

		if (hasOption(INCREMENT_HASH)) {
			theHashKey.clear(from, piece.piece());
			theHashKey.set(to, piece.piece());
		}
		if (hasOption(INCREMENT_REVERSED_HASH)) {
			theReversedHashKey.clear(from, piece.piece());
			theReversedHashKey.set(to, piece.piece());
		}

		super.movePiece(piece,from,to);
	}

	public void move2Pieces(Piece a_piece, int a_from, int a_to,
	                         Piece b_piece, int b_from, int b_to)
	{
		if (a_piece==null) a_piece = piece(a_from);
		if (b_piece==null) b_piece = piece(b_from);

		if (hasOption(INCREMENT_HASH)) {
			theHashKey.clear(a_from, a_piece.piece());
			theHashKey.set(a_to, a_piece.piece());

			theHashKey.clear(b_from, b_piece.piece());
			theHashKey.set(b_to, b_piece.piece());
		}
		if (hasOption(INCREMENT_REVERSED_HASH)) {
			theReversedHashKey.clear(a_from, a_piece.piece());
			theReversedHashKey.set(a_to, a_piece.piece());

			theReversedHashKey.clear(b_from, b_piece.piece());
			theReversedHashKey.set(b_to, b_piece.piece());
		}

		super.move2Pieces(a_piece,a_from,a_to, b_piece,b_from,b_to);
	}

	public final void movePiece(int from, int to)
	{
		movePiece(piece(from), from,to);
	}

	public void deletePiece(int square)
	{
		if (hasOption(INCREMENT_HASH))
			theHashKey.clear(square, pieceAt(square));
		if (hasOption(INCREMENT_REVERSED_HASH))
			theReversedHashKey.clear(square, pieceAt(square));

		super.deletePiece(square);
	}

	public void deletePiece(Piece piece)
	{
		if (hasOption(INCREMENT_HASH))
			theHashKey.clear(piece.square(), piece.piece());
		if (hasOption(INCREMENT_REVERSED_HASH))
			theReversedHashKey.clear(piece.square(), piece.piece());

		super.deletePiece(piece);
	}

	protected void restorePiece(int square, Piece piece)
	{
		if (hasOption(INCREMENT_HASH))
			theHashKey.set(square,piece.piece());
		if (hasOption(INCREMENT_REVERSED_HASH))
			theReversedHashKey.set(square,piece.piece());

		super.restorePiece(square,piece);
	}

	/**
	 * perform a pseudo-legal move
	 */
	public void doMove(Move move)
	{
		//	fill in new stack fram
		StackFrame frame = theMoveStack[thePly];
		if (frame==null) frame = theMoveStack[thePly] = new StackFrame();
		int oldFlags = theFlags;
		frame.copy(move,null);
		frame.positionFlags = theFlags;
		frame.silentPlies = theSilentPlies;
		frame.hashValue = theHashKey.value();
		frame.reversedHashValue = theReversedHashKey.value();
		frame.whiteSignature = theMatSignature.wsig;
		frame.blackSignature = theMatSignature.bsig;

		if (move.moving==null) {
			//  NULLMOVE
		}
		else {
			Piece piece = piece(move.from);

			boolean silent = !piece.isPawn();
			//	pawn moves and captures are considered not silent

			if (move.captured!=null) {
				silent = false;
				deletePiece(move.captured);
			}

			if (move.isCastling())
			{
				switch (move.castlingMask())
				{
					//  castling move is encoded with mv.from==king, mv.to==rook
					//  kings moves two square to the left or right, but at most to the destination rook
				case WHITE_KINGS_CASTLING:
					//  kingside castling FRC
					//  king ALWAYS moves to G1, rook ALWAYS moves to F1
					move2Pieces(piece,move.from,G1, null,move.to,F1);
					break;
				case BLACK_KINGS_CASTLING:
					move2Pieces(piece,move.from,G8, null,move.to,F8);
					break;
				case WHITE_QUEENS_CASTLING:
					move2Pieces(piece,move.from,C1, null,move.to,D1);
					break;
				case BLACK_QUEENS_CASTLING:
					move2Pieces(piece,move.from,C8, null,move.to,D8);
					break;
				}
			}
			else
				movePiece(piece, move.from, move.to); /*	actually make the move	*/

			if(move.isPromotion()) {
				frame.promoted = getUnpromotedPiece(movesNext()+move.getPromotionPiece());
				frame.promoted.setSquare(move.to);

	            int pawnFile = FILE_A+move.moving.listIndex();
				frame.promoted.setPawnFile(pawnFile);
	            setPromoPiece(movesNext(), pawnFile, frame.promoted);

				deletePiece(move.moving);
				restorePiece(move.to, frame.promoted);
			}
			else
				frame.promoted = null;

			/* adjust castling privileges */
			if (piece.isKing())
			{
				if (piece.isWhite())
					theFlags = Util.minus(theFlags, WHITE_KING_HOME);
				else
					theFlags = Util.minus(theFlags, BLACK_KING_HOME);
			}
			else if (piece.isRook())
			{       //  FRC
				if (move.from==castlingRookSquare(WHITE_KING_ROOK_HOME))
					theFlags = Util.minus(theFlags, WHITE_KING_ROOK_HOME);
				else if (move.from==castlingRookSquare(WHITE_QUEEN_ROOK_HOME))
					theFlags = Util.minus(theFlags, WHITE_QUEEN_ROOK_HOME);
				else if (move.from==castlingRookSquare(BLACK_KING_ROOK_HOME))
					theFlags = Util.minus(theFlags, BLACK_KING_ROOK_HOME);
				else if (move.from==castlingRookSquare(BLACK_QUEEN_ROOK_HOME))
					theFlags = Util.minus(theFlags, BLACK_QUEEN_ROOK_HOME);
			}

			theFlags = Util.minus(theFlags,EN_PASSANT_FILE);
			if (move.isPawnDouble() && canEnPassant(move.to,move.moving.piece()))
				theFlags += EngUtil.fileOf(move.from);

			if(silent)
			    theSilentPlies++;
			else
			    theSilentPlies = 0;
		}

		thePly++;
		toggleNext();

		if (hasOption(INCREMENT_HASH))	{
			theHashKey.clear(oldFlags);
			theHashKey.set(theFlags);

			if (hasOption(DRAW_3))
				incrHashCount(theHashKey.value());
		}

		if (hasOption(INCREMENT_REVERSED_HASH)) {
			theReversedHashKey.clear(oldFlags);
			theReversedHashKey.set(theFlags);
		}

		if (hasOption(INCREMENT_SIGNATURE)) {
			theMatSignature.update(move);
		}
	}


	public Move undoMove()
	{
		if (thePly==0)
			return null;

		if (hasOption(INCREMENT_HASH+DRAW_3))
			decrHashCount(theHashKey.value());

		int oldOption = option;
		setOption(INCREMENT_HASH|INCREMENT_REVERSED_HASH,false);
		/*	hash keys will be restored from stack; no need to calculate	*/

		/*	undo move	*/
		StackFrame frame = theMoveStack[--thePly];

		if (frame.moving==null) {
			//  NULLMOVE
		}
		else {
			if (frame.isPromotion()) {
				deletePiece(frame.promoted);
				restorePiece(frame.from, frame.moving);
				frame.promoted.setVacant();	//	mark for reuse

	            int pawnFile = frame.promoted.getPawnFile();
				frame.promoted.setPawnFile(0);
	            setPromoPiece(movedLast(), pawnFile, null);

				frame.promoted = null;
			}
			else if (frame.isCastling())
			{
				switch(frame.castlingMask()) {  //  FRC
				//  castling move is encoded with mv.from==king, mv.to==rook
				case WHITE_KINGS_CASTLING:
					move2Pieces(frame.moving,G1,frame.from, null,F1,frame.to);
					break;
				case WHITE_QUEENS_CASTLING:
					move2Pieces(frame.moving,C1,frame.from, null,D1,frame.to);
					break;
				case BLACK_KINGS_CASTLING:
					move2Pieces(frame.moving,G8,frame.from, null,F8,frame.to);
					break;
				case BLACK_QUEENS_CASTLING:
					move2Pieces(frame.moving,C8,frame.from, null,D8,frame.to);
					break;
				}
			}
			else
				movePiece(frame.moving, frame.to, frame.from);

			if (frame.captured != null) {
				if (frame.isEnPassant())
					restorePiece(frame.getEnPassantSquare(), frame.captured);
				else
					restorePiece(frame.to, frame.captured);
			}

		}

		theFlags = frame.positionFlags;
		theSilentPlies = frame.silentPlies;
		theHashKey.setValue(frame.hashValue);
		theReversedHashKey.setValue(frame.reversedHashValue);
		theMatSignature.wsig = frame.whiteSignature;
		theMatSignature.bsig = frame.blackSignature;

		option = oldOption;

		while (variationTop > 0 && thePly < variationPlies[variationTop-1])
			variationTop--;

		return frame;
	}

	public Move getExpectedMove()
	{
		if (thePly < 0 || thePly >= theMoveStack.length)
			return null;
		else
			return theMoveStack[thePly];
	}

	public Move getLastMove()
	{
		if (thePly < 1 || thePly >= theMoveStack.length)
			return null;
		else
			return theMoveStack[thePly-1];
	}

	/**	return to ply 0
	 */
	public final void reset()
	{
		reset(false);
	}

	/**	return to ply 0
	 */
	public void reset(boolean preferUndo)
	{
		if (preferUndo) {
			//  go to ply 0 by undoing all moves
			while (undoMove()!=null)
				;
		}
		else {
			//  go to ply 0 by resetting the position
			setup(startFEN[XFEN]);
		}
	}

	public boolean hasStartFEN() {
		return startFEN[XFEN]!=null &&
				!START_POSITION.equals(startFEN[XFEN]);
	}

	public void clear()
	{
		super.clear();
		theHashKey.clear();
		theReversedHashKey.clear();
		variationTop = 0;
		for (int i=0; i<startFEN.length; i++) startFEN[i++] = "";
		theMatSignature.clear();
		hashCount.clear();

		startFEN[FEN_CLASSIC] = startFEN[XFEN] = startFEN[SHREDDER_FEN] = EMPTY_POSITION;
	}

	public ArrayList<Move> snapshot()
	{
		ArrayList<Move> moves = new ArrayList<>();
		for(int i=0; i<thePly; i++)
			moves.add(new Move(theMoveStack[i]));
		return moves;
	}

	protected void setupFEN(String fenString)
	{
		if (START_POSITION.equals(fenString))
			fenString = null;
        else if (EMPTY_POSITION.equals(fenString))
            fenString = "";

		if (fenString==null) {
            //  INITIAL POSITION
			this.setupInitial();
        }
		else if (fenString.length()==0) {
            //  EMPTY POSITION
			clear();
        }
		else {
            //  OTHER POSITION
			super.setupFEN(fenString);

            if (hasOption(INCREMENT_HASH))
                computeHashKey(theHashKey);
            if (hasOption(INCREMENT_REVERSED_HASH))
                computeHashKey(theReversedHashKey);
			if (hasOption(INCREMENT_SIGNATURE))
				computeMatSig();

			startFEN[FEN_CLASSIC] = this.toString(FEN_CLASSIC);
			startFEN[XFEN] = this.toString(XFEN);
			startFEN[SHREDDER_FEN] = this.toString(SHREDDER_FEN);
        }
		variationTop = 0;
	}

	protected void setupBoard(Board that)
	{
		if (that==null) {
            //  INITIAL POSITION
			setupInitial();
        }
		else {
            //  OTHER POSITION
			super.setupBoard(that);

            if (hasOption(INCREMENT_HASH))
                computeHashKey(theHashKey);
            if (hasOption(INCREMENT_REVERSED_HASH))
                computeHashKey(theReversedHashKey);
			if (hasOption(INCREMENT_SIGNATURE))
				computeMatSig();
        }

		if (that instanceof Position) {
			System.arraycopy(((Position)that).startFEN,0, this.startFEN,0, this.startFEN.length);
		}
		this.variationTop = 0;  //  don't copy variations !!
	}

	public int setupInitial()
	{
		int frcIndex = super.setupInitial();

		getHashKey().setValue(getHashKey().getInitialValue(hasOption(IGNORE_FLAGS_ON_HASH), false));
		getReversedHashKey().setValue(getReversedHashKey().getInitialValue(hasOption(IGNORE_FLAGS_ON_HASH),true));
		getMatSig().setInitial();

		this.variationTop = 0;  //  don't copy variations !!

		startFEN[FEN_CLASSIC] = START_POSITION;
		startFEN[XFEN] = START_POSITION;
		startFEN[SHREDDER_FEN] = START_POSITION_SHREDDER_FEN;

		return frcIndex;
	}


	public final HashKey getHashKey()
	{
		return theHashKey;
	}

	public final HashKey getReversedHashKey()
	{
		return theReversedHashKey;
	}

	public final MatSignature getMatSig()
	{
		return theMatSignature;
	}

	public String getStartFEN(int fenVariant)
	{
		return startFEN[fenVariant];
	}

	public boolean prepareMove(Move mv)
	{
		if (mv==Move.NULLMOVE)
			return true;

		if (!checkMove(mv))
			return false;

		if (!mv.moving.checkMove(mv))
			return false;

		return true;
	}

	public boolean tryMove(Move mv)
	{
		/*	update flags	*/
		theFlags = Util.minus(theFlags, STALEMATE+CHECK+DRAW_50+DRAW_MAT);

		if (!prepareMove(mv))
			return false;

		doMove(mv);

		return detect(mv);
	}

	public boolean detect(Move mv)
	{
		if (hasOption(EXPOSED_CHECK) && wasExposed(mv)) {
			undoMove();
			return false;
		}

		int setFlags = 0;

		if (hasOption(CHECK)) {
			/*	detect if other king is checked	*/
			if (whiteMovesNext()) {
				if (underAttack(whiteKing().square(), theBlackPieces))
					setFlags |= CHECK;
			}
			else {
				if (underAttack(blackKing().square(), theWhitePieces))
					setFlags |= CHECK;
			}
		}

		if (hasOption(STALEMATE) && detectStalemate(EngUtil.isCheck(setFlags)))
			setFlags |= STALEMATE;

		if (hasOption(DRAW_3)) {
			/*	detect threefold repetition	*/
			if (hashCount.get(theHashKey.value()) >= 3)
				setFlags |= DRAW_3;
		}
		if (hasOption(DRAW_50)) {
			/*	detect 50 moves rule			 */
			if (silentPlies() >= 100)
				setFlags |= DRAW_50;
		}
		if (hasOption(DRAW_MAT)) {
			/*  detect missing material */
			if (!canMate(WHITE) && !canMate(BLACK))
				setFlags |= DRAW_MAT;
		}

		mv.flags |= setFlags;
		theFlags |= setFlags;

		return true;
	}

	protected boolean detectStalemate(boolean checked)
	{
		if (whiteMovesNext())
			moveIterator.reset(theWhitePieces);
		else
			moveIterator.reset(theBlackPieces);

		int oldOptions = getOptions();
		setOptions(oldOptions & INCREMENT_HASH);

		while (moveIterator.next()) {
			Move mv = moveIterator.getMove();

			setOption(EXPOSED_CHECK, true);

			if (!tryMove(mv)) {
				continue;	//	illegal move
			}

			if (checked && isStillChecked()) {
				//	move would keep king in check
				undoMove();
				continue;
			}
			//	else: legal move
			undoMove();
			setOptions(oldOptions);
			return false;	//	at least one legal move
		}

		setOptions(oldOptions);
		return true;	//	no legal move
	}

	protected boolean isStillChecked()
	{
		if (movedLast()==BLACK)
			return underAttack(blackKing().square(), theWhitePieces);
		else
			return underAttack(whiteKing().square(), theBlackPieces);
	}

	protected boolean wasExposed(Move mv)
	{
		/*	detect if own king is checked, if so return true	*/
		int target;
		ArrayList[] attackers;
		if (movedLast()==WHITE) {
			target = kingSquare(WHITE);
			attackers = theBlackPieces;
		}
		else {
			target = kingSquare(BLACK);
			attackers = theWhitePieces;
		}

		switch (mv.castlingMask()) {    //  FRC
		case WHITE_KINGS_CASTLING:
			return underAttack(mv.from,G1,attackers);
		case WHITE_QUEENS_CASTLING:
			return underAttack(C1,mv.from,attackers);
		case BLACK_KINGS_CASTLING:
			return underAttack(mv.from,G8,attackers);
		case BLACK_QUEENS_CASTLING:
			return underAttack(C8,mv.from,attackers);
		}
		//	check all black attackers
		return underAttack(target, attackers);
/*		}
		else if (mv.isEnPassant()) {
			//	check discovered attacks
			return discoveredAttack(mv.from, target, attackers) ||
				   discoveredAttack(mv.getEnPassantSquare(), target, attackers);
		}
		else {
			//	check discovered attacks
			return discoveredAttack(mv.from, target, attackers);
		}
*/	}

	protected boolean underAttack(int target, List[] attackers)
	{
		for (int i=PAWN; i <= KING; i++)
			if (underAttack(target, attackers[i]))
				return true;
		return false;
	}

	protected boolean underAttack(int target1, int target2, List[] attackers)
	{
		for ( ; target1 <= target2; target1++)
			if (underAttack(target1,attackers)) return true;
		return false;
	}

	protected boolean underAttack(int target, List attackers)
	{
		testMove.to = target;
		testMove.captured = piece(target);
		testMove.setPromotionPiece(QUEEN);		//	just in case

		for (int j=0; j<attackers.size(); j++) {
			Piece p = (Piece)attackers.get(j);
			if (!p.isVacant()) {
				testMove.moving = p;
				testMove.from = p.square();

				if (testMove.captured!=null &&
					!p.canCapture(testMove.captured.piece()))
					continue;

				if (p.checkMove(testMove))
					return true;
			}
		}
		return false;
	}

	protected boolean discoveredAttack(int from, int to, List[] attackers)
	{
		if (Queen.isDiagonal(from,to))
			return underAttack(to, attackers[BISHOP]) ||
				   underAttack(to, attackers[QUEEN]);

		if (Queen.isOrthogonal(from,to))
			return underAttack(to, attackers[ROOK]) ||
				   underAttack(to, attackers[QUEEN]);
		return false;
	}

	/**
	 * @return the number of (strictly legal) moves in this position
	 */
	public int countLegalMoves(boolean strict)
	{
		int oldOptions = getOptions();
		//  detect all kinds of checks, no need to detect draw (od do we ?)
		setOptions(0);
		setOption(EXPOSED_CHECK,strict);

		int count = 0;
		moveIterator.reset(whiteMovesNext() ? theWhitePieces:theBlackPieces);
		while (moveIterator.next())
			if (tryMove(moveIterator.getMove())) {
				undoMove();
				count++;
			}

		setOptions(oldOptions);
		return count;
	}

	/**
	 * @return the current variation nest level (0 = main line, 1 = variation, etc.)
	 */
	public int getVariationLevel()
	{
		return variationTop;
	}

	public Move[] getMoves(int startPly, int endPly)
	{
		if (endPly > thePly) endPly = thePly;
		if (startPly > endPly) startPly = endPly;

		int count = endPly-startPly;
		Move[] result = new Move[count];
		for (int i=0; i<count; i++)
			result[i] = new Move(theMoveStack[startPly+i]);

		return result;
	}

	/**
	 * start a new variation
	 */
	public void startVariation()
	{
		Move undo_mv = undoMove();
		if (undo_mv !=null)
		{
		Move mv = variationMoves[variationTop];
		if (mv==null) mv = variationMoves[variationTop] = new Move();
			mv.copy(undo_mv,null);
		}
		else
			variationMoves[variationTop] = null;    // = start of game

		variationPlies[variationTop] = ply();
		variationTop++;
	}

	public void undoVariation()
	{
		int ply = variationPlies[variationTop-1];
		while (ply() > ply) undoMove();
		//	redo original move
		if (variationMoves[variationTop-1] != null)
		doMove(variationMoves[variationTop-1]);
		variationTop--;
	}

	public boolean isEmpty()
	{
		return (countValid(theWhitePieces) + countValid(theBlackPieces)) == 0;
	}


	public String[] checkLegality()
	{
		Vector collect = new Vector();

		checkLegality(collect);

		if (collect.isEmpty())
			return null;	//	allright
		else {
			String[] result = new String[collect.size()];
			collect.toArray(result);
			return result;
		}
	}

	public void checkLegality(List collect)
	{
		checkLegality(WHITE,"white",collect);
		checkLegality(BLACK,"black",collect);
		checkCastlingLegality(collect);
	}

	protected void checkLegality(int color, String key, List collect)
	{
		//	exactly one king
		int kc = countValid(pieceList(color+KING));
		if (kc==0)
			collect.add("pos.error."+key+".king.missing");
		else if (kc >= 2)
			collect.add("pos.error.too.many."+key+".kings");

		//	moving king must not be checked
		if (movedLast(color) && (kc >= 1) &&
			underAttack(king(color).square(), allOppositePieces(color)))
			collect.add("pos.error."+key+".king.checked");

		//	no pawns on bad rows
		List pl = pieceList(color+PAWN);
		for (int i=pl.size()-1; i>=0; i--) {
			Pawn pawn = (Pawn)pl.get(i);
			if (pawn.isVacant()) continue;

			int row = EngUtil.rowOf(pawn.square());
			if (row==EngUtil.baseRow(color))
				collect.add("pos.error."+key+".pawn.base");
			else if (row==EngUtil.promotionRow(color))
				collect.add("pos.error."+key+".pawn.promo");
		}
	}

	private void checkCastlingLegality(List collect)
	{
		//  matching FRC castlings
		int whiteFile,blackFile;
		if (canCastle(WHITE_KING_HOME) && canCastle(BLACK_KING_HOME))
		{
			//  if castling is enabled, both kings must be on same file
			whiteFile = EngUtil.fileOf(kingSquare(WHITE));
			blackFile = EngUtil.fileOf(kingSquare(BLACK));
			if (whiteFile!=blackFile)
				collect.add("pos.error.castle.king");
		}
		if (canCastle(WHITE_KING_ROOK_HOME) && canCastle(BLACK_KING_ROOK_HOME))
		{
			//  if castling is enabled, both rooks must be on same file
			whiteFile = EngUtil.fileOf(castlingRookSquare(WHITE_KING_ROOK_HOME));
			blackFile = EngUtil.fileOf(castlingRookSquare(BLACK_KING_ROOK_HOME));
			if (whiteFile!=blackFile)
				collect.add("pos.error.castle.rook");
		}
		if (canCastle(WHITE_QUEEN_ROOK_HOME) && canCastle(BLACK_QUEEN_ROOK_HOME))
		{
			//  if castling is enabled, both rooks must be on same file
			whiteFile = EngUtil.fileOf(castlingRookSquare(WHITE_QUEEN_ROOK_HOME));
			blackFile = EngUtil.fileOf(castlingRookSquare(BLACK_QUEEN_ROOK_HOME));
			 if (whiteFile!=blackFile)
				collect.add("pos.error.castle.rook");
		}
	}

	public String[] checkPlausibility()
	{
		Vector collect = new Vector();

		checkPlausibility(collect);

		if (collect.isEmpty())
			return null;	//	allright
		else {
			String[] result = new String[collect.size()];
			collect.toArray(result);
			return result;
		}
	}

	public void checkPlausibility(List collect)
	{
		checkPlausibility(WHITE,"white",collect);
		checkPlausibility(BLACK,"black",collect);
	}

	protected void checkPlausibility(int color, String key, List collect)
	{
		//	no more than 8 pawns + promo piece
		int pc = countValid(pieceList(color+PAWN));

		int nc = countValid(pieceList(color+KNIGHT));
		int bc = countValid(pieceList(color+BISHOP));
		int rc = countValid(pieceList(color+ROOK));
		int qc = countValid(pieceList(color+QUEEN));
		int kc = countValid(pieceList(color+KING));

		if ((pc+nc+bc+rc+qc+kc) > 16)
			collect.add("pos.warning.too.many."+key+".pieces");
		else if (pc > 8)
			collect.add("pos.warning.too.many."+key+".pawns");
		else {
			if ((pc+nc) > 10) collect.add("pos.warning.too.many."+key+".knights");
			if ((pc+bc) > 10) collect.add("pos.warning.too.many."+key+".bishops");
			if ((pc+rc) > 10) collect.add("pos.warning.too.many."+key+".rooks");
			if ((pc+qc) > 9) collect.add("pos.warning.too.many."+key+".queens");
		}

        //	strange colored bishops ?
        if (!super.checkBishopColors(color))
            collect.add("pos.warning.strange."+key+".bishops");

        //	pawn structure
        //	TODO
    }

}
