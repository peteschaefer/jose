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

package de.jose.chess;

import de.jose.Util;

/**
 * utility functions for pieces and square
 */

public class EngUtil
		extends de.jose.Util
		implements Constants
{
	//-------------------------------------------------------------------------------
	//	pieces
	//-------------------------------------------------------------------------------

	/**	@param piece a colored piece
	 *	@return its color (BLACK or WHITE)
	 */
	public static final int colorOf(int piece)			{ return piece & COLORS; }

	public static final int oppositeColor(int piece)	{ return piece ^ COLORS; }

	/**	@param piece a piece
	 *	@retrun true if it is white, false otherwise
	 */
	public static final boolean isWhite(int piece)		{ return allOf(piece,WHITE); }

	/**	@param piece a piece
	 *	@retrun true if it is black, false otherwise
	 */
	public static final boolean isBlack(int piece)		{ return allOf(piece,BLACK); }
	
	public static final boolean isLightSquare(int square) { 
		return isLightSquare(fileOf(square),rowOf(square)); 
	}
	
	public static final boolean isLightSquare(int file, int row)	{ return ((file+row)%2) == 0; }

	public static final boolean isDarkSquare(int square) {
		return isDarkSquare(fileOf(square),rowOf(square)); 
	}
	
	public static final boolean isDarkSquare(int file, int row)		{ return ((file+row)%2) != 0; }
	
	/**	@param piece colored piece
	 *	@return its piece (PAWN ... KING)
	 */
	public static final int uncolored(int piece)		{ return piece & UNCOLORED; }
	
	/**
	 * @return true if a given (colored) piece can capture another
	 */
	public static final boolean canCapture(int piece1, int piece2)
	{
		return (piece1 & piece2 & COLORS) == 0;
	}
	
	/**	@return a uppercase piece character
	 */
	public static final char pieceCharacter(int piece)
	{
		return UPPER_PIECE_CHARACTERS[uncolored(piece)];
	}

	/**	@return a uppercase piece character
	 */
	public static final char lowerPieceCharacter(int piece)
	{
		return LOWER_PIECE_CHARACTERS[uncolored(piece)];
	}

	/**	@return an uppercase piece character for white pieces,
	 *			a lowercase piece character for black pieces
	 */
	public static final char coloredPieceCharacter(int piece)
	{
		if (isWhite(piece))
			return UPPER_PIECE_CHARACTERS[uncolored(piece)];
		else
			return LOWER_PIECE_CHARACTERS[uncolored(piece)];
	}
	
	/**
	 * given a Character
	 * @return the piece (with color)
	 */
	public static final int char2Piece(char c)
	{
		int color;
		if (Character.isUpperCase(c))
			color = WHITE;
		else
			color = BLACK;
		
		c = Character.toLowerCase(c);
		for (int p=PAWN; p<=KING; p++)
			if (LOWER_PIECE_CHARACTERS[p] == c)
				return color + p;
		
		return EMPTY;
	}
	
	
	/**
	 * given a Character
	 * @return the piece (without color)
	 */
	public static final int char2UncoloredPiece(char c)
	{
		c = Character.toLowerCase(c);
		for (int p=PAWN; p<=KING; p++)
			if (LOWER_PIECE_CHARACTERS[p] == c)
				return p;
		return EMPTY;
	}

	/**
	 * given a Character
	 * @return the piece (without color)
	 */
	public static final int char2UncoloredPiece(String chars, char c)
	{
		c = Character.toUpperCase(c);
		for (int p=PAWN; p<=KING; p++)
			if (chars.charAt(p-PAWN) == c)
				return p;
		return EMPTY;
	}

	//-------------------------------------------------------------------------------
	//	board layout and squares
	//-------------------------------------------------------------------------------

	/**	@param square index of a square
	 *	@return its file (0 .. OUTER_BOARD_WIDTH-1)
	 */
	public static final int fileOf(int square)			{ return square % OUTER_BOARD_WIDTH; }
	
	/**	@param square index of a square
	 *	@return its row (0 .. OUTER_BOARD_HEIGHT-1)
	 */
	public static final int rowOf(int square)			{ return square / OUTER_BOARD_WIDTH; }
	
	/**
	 * @return a square index
	 */
	public static final int square(int file, int row)	{ return row * OUTER_BOARD_WIDTH + file; }
	
	public static final int mirrorSquare(int file, int row)
	{
		return square(file,OUTER_BOARD_HEIGHT-1-row);
	}

	public static final int mirrorSquare(int square)
	{
		return mirrorSquare(fileOf(square),rowOf(square));
	}

	/**
	 * @return true if the given square is inside the proper board
	 */
	public static final boolean innerSquare(int square)
	{
		return	innerSquare(fileOf(square), rowOf(square));
	}
	
	/**
	 * @return true if the given square is inside the proper board
	 */
	public static final boolean innerSquare(int file, int row)
	{
		return	file >= FILE_A && file <= FILE_H 
				&& row >= ROW_1 && row <= ROW_8;
	}
		
	/**	@return the 6 bit representation of a square
	 */
	public static final byte toByte(int square)
	{
		return (byte)((rowOf(square)-ROW_1) << 3 | (fileOf(square)-FILE_A));
	}
	
	/**	@return the integer representation of a 6bit square
	 */
	public static final int toInt(byte sq)
	{
		return square(FILE_A + (sq & 0x07), ROW_1 + (sq >> 3));
	}
	
	/**
	 * @return true if the given square is on the border
	 */
	public static final boolean outerSquare(int square)
	{
		return !innerSquare(square);
	}
	
	public static final int fileDiff(int a, int b)
	{
		return fileOf(a)-fileOf(b);
	}
	
	public static final int rowDiff(int a, int b)
	{
		return rowOf(a)-rowOf(b);
	}

	public static final int euclidDistSq(int a, int b)
	{
		int dfile = fileDiff(a,b);
		int drow = rowDiff(a,b);
		return dfile*dfile+drow*drow;
	}

	public static final char fileChar(int file)
	{
		return (char)('a'+file-FILE_A);
	}
	
	public static final char rowChar(int row)
	{
		return (char)('1'+row-ROW_1);
	}

    public static final char[] square2Char(int square)
    {
        char[] result = new char[2];
        result[0] = fileChar(fileOf(square));
        result[1] = rowChar(rowOf(square));
        return result;
    }

    public static final String square2String(int square)
    {
        char[] ch = square2Char(square);
        return new String(ch);
    }

	public static final boolean isFileChar(char c)
	{
		return c>='a' && c<='h';
	}

	public static final int char2File(char c)
	{
		return c-'a'+FILE_A;
	}

	public static final boolean isRowChar(char c)
	{
		return c>='1' && c<='8';
	}

	public static final int char2Row(char c)
	{
		return c-'1'+ROW_1;
	}
	
	public static final int char2Square(char file, char row)
	{
		return square(char2File(file), char2Row(row));
	}
	
	//-------------------------------------------------------------------------------
	//	position flags
	//-------------------------------------------------------------------------------

	/**
	 * @param flags position flags
	 * @param castling a castling constant (WHITE_KINGS_CASTLING ... )
	 * @return true if the given castling is allowed
	 */
	public static final boolean canCastle(int flags, int castling)	{ return allOf(flags, castling); }


	public static final int castlingMask(int flags)				{ return flags & CASTLING; }
	public static final boolean isCastling(int flags)			{ return Util.anyOf(flags,CASTLING); }

	public static final boolean isPromotion(int flags)			{ return Util.anyOf(flags,PROMOTION_PIECE); }
	public static final int getPromotionPiece(int flags)		{ return flags & PROMOTION_PIECE; }

	public static final int setPromotionPiece(int flags, int p) {
		return Util.minus(flags,PROMOTION_PIECE)+EngUtil.uncolored(p);
	}

	public static final boolean isEnPassant(int flags)			{ return Util.allOf(flags,EN_PASSANT_MOVE); }
	public static final boolean isPawnDouble(int flags)			{ return Util.allOf(flags,PAWN_DOUBLE_MOVE); }
	public static final boolean isCheck(int flags)				{ return Util.allOf(flags,CHECK); }

	public static final boolean isMate(int flags)				{ return (flags&MATE)==MATE; }
	public static final boolean isStalemate(int flags)			{ return (flags&MATE)==STALEMATE; }

	public static final boolean isDraw3(int flags)				{ return Util.allOf(flags,DRAW_3); }
	public static final boolean isDraw50(int flags)				{ return Util.allOf(flags,DRAW_50); }
	public static final boolean isDrawMat(int flags)            { return Util.allOf(flags,DRAW_MAT); }

	public static int setEnPassant(int flags, boolean on)		{ return Util.set(flags,EN_PASSANT_MOVE,on); }
	public static int setPawnDouble(int flags, boolean on)		{ return Util.set(flags,PAWN_DOUBLE_MOVE,on); }

	public static final boolean isGameFinished(int flags,boolean strict)
	{
		if (strict)
			return Util.anyOf(flags,STALEMATE+DRAW_MAT);
		else
			return Util.anyOf(flags,STALEMATE+DRAW_3+DRAW_50+DRAW_MAT);
		//	Draw-3 and Draw-50 are optional, game can continue
	}

	/**
	 * @param flags position flags
	 * @return the ep-file
	 */
	public static final int enPassantFile(int flags)
	{
		return flags & EN_PASSANT_FILE;
	}

	public static final int enPassantTargetSquare(int flags)
	{
		int epFile = enPassantFile(flags);
		if (epFile==0) return 0;

		if (Util.allOf(flags,WHITE))
			return square(epFile,ROW_6);
		else
			return square(epFile,ROW_3);
	}

	public static final boolean isPromotionRow(int row, int color)
	{
		if (color==WHITE)
			return row == WHITE_PROMOTION_ROW;
		else
			return row == BLACK_PROMOTION_ROW;
	}
	
	public static final boolean isEnPassantRow(int row, int color)
	{
		if (color==WHITE)
			return row == WHITE_EN_PASSANT_ROW;
		else
			return row == BLACK_EN_PASSANT_ROW;
	}
	
	public static final int reverseFlags(int flags)
	{
		/*	reverse next move	*/
		int result = (flags & COLORS);
		if (result!=0) result ^= COLORS;
		
		/*	reverse castling	*/
		int w = flags & WHITE_CASTLING;
		int b = flags & BLACK_CASTLING;
		result |= w<<4;
		result |= b>>4;
		
		/*	DON'T reverse en passant file	*/
		int file = enPassantFile(flags);
		if (file != 0)
			result |= file; //  (BORDER_WIDTH+8-file);

		return result;
	}
	
	/**
	 */
	public static final int promotionPiece(int flags)
	{
		return flags & PROMOTION_PIECE;
	}

	public static final int promotionRow(int color)
	{
		if (isWhite(color))
			return WHITE_PROMOTION_ROW;
		else
			return BLACK_PROMOTION_ROW;
	}

	public static final int baseRow(int color)
	{
		if (isWhite(color))
			return WHITE_BASE_ROW;
		else
			return BLACK_BASE_ROW;
	}

	public static final int pawnRow(int color)
	{
		if (isWhite(color))
			return WHITE_PAWN_ROW;
		else
			return BLACK_PAWN_ROW;
	}

	/**
	 * @return the square of the geometric distance between two squares
	 */
	public static final int squareDistance(int a, int b)
	{
		int dx = fileDiff(a,b); 
		int dy = rowDiff(a,b);
		return dx*dx + dy*dy;
	}

	/**
	 * @return the square of the geometric distance between two squares
	 */
	public static final double distance(int a, int b)
	{
		return Math.sqrt(squareDistance(a,b));	
	}
}
