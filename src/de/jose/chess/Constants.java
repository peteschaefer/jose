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

/**
 * constants for chess pieces and colors
 */
public interface Constants
{
	//-------------------------------------------------------------------------------
	//	pieces
	//-------------------------------------------------------------------------------
	
	/**	the empty square	 */
	public static final int	EMPTY			= 0x00;
	
	/**	the Pawn (uncolored)	 */
	public static final int	PAWN			= 0x01;
	/**	the Knight (uncolored)	 */
	public static final int	KNIGHT			= 0x02;
	/**	the Bishop (uncolored)	 */
	public static final int	BISHOP			= 0x03;
	/**	the Rook (uncolored)	 */
	public static final int	ROOK			= 0x04;
	/**	the Queen (uncolored)	 */
	public static final int	QUEEN			= 0x05;
	/**	the King (uncolored)	 */
	public static final int	KING			= 0x06;
	
	/**	the color white	*/
	public static final int WHITE			= 0x10;
	/**	the color black	*/
	public static final int BLACK			= 0x20;
	
	/**	bit mask for colors	 */
	public static final int COLORS			= 0x30;
	/**	bit mask for uncolored pieces	 */
	public static final int UNCOLORED		= 0x0f;
	
	/**	a white pawn	*/
	public static final int WHITE_PAWN		= PAWN + WHITE;
	/**	a white knight	*/
	public static final int WHITE_KNIGHT	= KNIGHT + WHITE;
	/**	a white bishop	*/
	public static final int WHITE_BISHOP	= BISHOP + WHITE;
	/**	a white rook	*/
	public static final int WHITE_ROOK		= ROOK + WHITE;
	/**	a white queen	*/
	public static final int WHITE_QUEEN		= QUEEN + WHITE;
	/**	a white king	*/
	public static final int WHITE_KING		= KING + WHITE;
	
	/**	a black pawn	*/
	public static final int BLACK_PAWN		= PAWN + BLACK;
	/**	a black knight	*/
	public static final int BLACK_KNIGHT	= KNIGHT + BLACK;
	/**	a black bishop	*/
	public static final int BLACK_BISHOP	= BISHOP + BLACK;
	/**	a black rook	*/
	public static final int BLACK_ROOK		= ROOK + BLACK;
	/**	a black queen	*/
	public static final int BLACK_QUEEN		= QUEEN + BLACK;
	/**	a black king	*/
	public static final int BLACK_KING		= KING + BLACK;
	
	/**	a blocked square (indicates the border of the chess board) 
	 *	it is a pseudo piece that neither white nor black can capture
	 * */
	public static final int BLOCK			= WHITE + BLACK;
	
	/**	characters	 */
	/**	standard (english) piece characters	 */
	
	public static final char LOWER_PAWN_CHAR	= 'p';
	public static final char LOWER_KNIGHT_CHAR	= 'n';
	public static final char LOWER_BISHOP_CHAR	= 'b';
	public static final char LOWER_ROOK_CHAR	= 'r';
	public static final char LOWER_QUEEN_CHAR	= 'q';
	public static final char LOWER_KING_CHAR	= 'k';
	
	public static final char UPPER_PAWN_CHAR	= 'P';
	public static final char UPPER_KNIGHT_CHAR	= 'N';
	public static final char UPPER_BISHOP_CHAR	= 'B';
	public static final char UPPER_ROOK_CHAR	= 'R';
	public static final char UPPER_QUEEN_CHAR	= 'Q';
	public static final char UPPER_KING_CHAR	= 'K';
	
	public static final char[] LOWER_PIECE_CHARACTERS	= { ' ','p','n','b','r','q','k' };
	public static final char[] UPPER_PIECE_CHARACTERS	= { ' ','P','N','B','R','Q','K' };

	public static final String DEFAULT_PIECE_CHARACTERS	=	"PNBRQK";

	/**	white and black characters	 */
	public static final char WHITE_CHAR					= 'w';
	public static final char BLACK_CHAR					= 'b';
	
	//-------------------------------------------------------------------------------
	//	board layout and squares
	//-------------------------------------------------------------------------------
	
	/**	width of the vertical border	 */
	public static final int BORDER_WIDTH			= 1;
	/**	height of the horizontal border	 */
	public static final int BORDER_HEIGHT			= 2;
	
	/**	width of the outer board	 */
	public static final int OUTER_BOARD_WIDTH		= BORDER_WIDTH + 8 + BORDER_WIDTH;
	/**	height of the outer board	 */
	public static final int OUTER_BOARD_HEIGHT		= BORDER_HEIGHT + 8 + BORDER_HEIGHT;
	/**	total size of the outer board	 */
	public static final int OUTER_BOARD_SIZE		= OUTER_BOARD_WIDTH * OUTER_BOARD_HEIGHT;
	
	/**	board files	 */
	/**	the a-file	 */
	public static final int FILE_A					= BORDER_WIDTH;
	/**	the b-file	 */
	public static final int FILE_B					= BORDER_WIDTH+1;
	/**	the c-file	 */
	public static final int FILE_C					= BORDER_WIDTH+2;
	/**	the d-file	 */
	public static final int FILE_D					= BORDER_WIDTH+3;
	/**	the e-file	 */
	public static final int FILE_E					= BORDER_WIDTH+4;
	/**	the f-file	 */
	public static final int FILE_F					= BORDER_WIDTH+5;
	/**	the g-file	 */
	public static final int FILE_G					= BORDER_WIDTH+6;
	/**	the h-file	 */
	public static final int FILE_H					= BORDER_WIDTH+7;
	
	/**	board rows (multiples of OUTER_BOARD_WIDTH)	 */
	/**	the first row	 */
	public static final int	ROW_1					= BORDER_HEIGHT;
	/**	the second row	 */
	public static final int	ROW_2					= BORDER_HEIGHT+1;
	/**	the third row	 */
	public static final int	ROW_3					= BORDER_HEIGHT+2;
	/**	the fourth row	 */
	public static final int	ROW_4					= BORDER_HEIGHT+3;
	/**	the fifth row	 */
	public static final int	ROW_5					= BORDER_HEIGHT+4;
	/**	the sixth row	 */
	public static final int	ROW_6					= BORDER_HEIGHT+5;
	/**	the seventh row	 */
	public static final int	ROW_7					= BORDER_HEIGHT+6;
	/**	the eight row	 */
	public static final int	ROW_8					= BORDER_HEIGHT+7;
	
	/**	typical rows	 */
	/**	the white base row (1)	 */
	public static final int WHITE_BASE_ROW			= ROW_1;
	/**	the white pawn row (2)	 */
	public static final int WHITE_PAWN_ROW			= ROW_2;
	/**	the white en-passant row (5)	 */
	public static final int WHITE_EN_PASSANT_ROW	= ROW_5;
	/**	the white promotion row (8)	 */
	public static final int WHITE_PROMOTION_ROW		= ROW_8;
	/**	the black base row (8)	 */
	public static final int BLACK_BASE_ROW			= ROW_8;
	/**	the black pawn row (7)	 */
	public static final int BLACK_PAWN_ROW			= ROW_7;
	/**	the black en-passant row (4)	 */
	public static final int BLACK_EN_PASSANT_ROW	= ROW_4;
	/**	the black promotion row (1)	 */
	public static final int BLACK_PROMOTION_ROW		= ROW_1;

	/**	some useful squares	 */
	public static final int A1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_A;
	public static final int B1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_B;
	public static final int C1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_C;
	public static final int D1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_D;
	public static final int E1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_E;
	public static final int F1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_F;
	public static final int G1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_G;
	public static final int H1						= ROW_1 * OUTER_BOARD_WIDTH + FILE_H;

	public static final int A2						= ROW_2 * OUTER_BOARD_WIDTH + FILE_A;
	public static final int A7						= ROW_7 * OUTER_BOARD_WIDTH + FILE_A;

	public static final int A8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_A;
	public static final int B8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_B;
	public static final int C8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_C;
	public static final int D8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_D;
	public static final int E8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_E;
	public static final int F8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_F;
	public static final int G8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_G;
	public static final int H8						= ROW_8 * OUTER_BOARD_WIDTH + FILE_H;
	
	/**	the row below the white base row	*/
	public static final int WHITE_BORDER_ROW		= BORDER_HEIGHT-1;
	
	//-------------------------------------------------------------------------------
	//	position flags
	//-------------------------------------------------------------------------------

	/**	which color moves next	 */
	public static final int NEXT_MOVE					= COLORS;	/* 0x0030	*/
	
	/**	bit mask for en-passant file	 */	
	public static final int EN_PASSANT_FILE				= 0x000f;
	
	/**	bit flag indicating that the white king has not yet moved	 */
	public static final int	WHITE_KING_HOME				= 0x0100;
	/**	bit flag indicating that the white king's rook has not yet moved	 */
	public static final int	WHITE_KING_ROOK_HOME		= 0x0200;
	/**	bit flag indicating that the white queen's rook has not yet moved	 */
	public static final int	WHITE_QUEEN_ROOK_HOME		= 0x0400;

	/**	bit flag indicating that the black king has not yet moved	 */
	public static final int	BLACK_KING_HOME				= 0x1000;
	/**	bit flag indicating that the black king's rook has not yet moved	 */
	public static final int	BLACK_KING_ROOK_HOME		= 0x2000;
	/**	bit flag indicating that the black queen's rook has not yet moved	 */
	public static final int	BLACK_QUEEN_ROOK_HOME		= 0x4000;
	
	/**	bit mask indicating the white king's castling is allowed	 */
	public static final int WHITE_KINGS_CASTLING		= WHITE_KING_HOME + WHITE_KING_ROOK_HOME;
	/**	bit mask indicating the white queen's castling is allowed	 */
	public static final int WHITE_QUEENS_CASTLING		= WHITE_KING_HOME + WHITE_QUEEN_ROOK_HOME;

	/**	bit mask indicating the black king's castling is allowed	 */
	public static final int BLACK_KINGS_CASTLING		= BLACK_KING_HOME + BLACK_KING_ROOK_HOME;
	/**	bit mask indicating the black king's castling is allowed	 */
	public static final int BLACK_QUEENS_CASTLING		= BLACK_KING_HOME + BLACK_QUEEN_ROOK_HOME;
	
	/**	bit mask for castling	 */
	public static final int CASTLING					= 0xff00;
	/**	bit mask for white castling	 */
	public static final int WHITE_CASTLING				= 0x0f00;
	/**	bit mask for black castling	 */
	public static final int BLACK_CASTLING				= 0xf000;
	
	/**	king is in check	 */
	public static final int CHECK						= 0x00010000;
	/**	position is stalemate	 */						
	public static final int STALEMATE					= 0x00020000;
	/**	position is mate	*/				
	public static final int MATE						= STALEMATE+CHECK;	/* = 0x00030000	*/
	/**	position is drawn by threefold repetition	 */
	public static final int DRAW_3						= 0x00100000;
	/**	position is drawn by 50 moves rule	 */
	public static final int DRAW_50						= 0x00200000;
	/** position is drawn by missing material   */
	public static final int DRAW_MAT                    = 0x00400000;

	//-------------------------------------------------------------------------------
	//	move flags (includes castling flags, above)
	//-------------------------------------------------------------------------------
	
	/**	bit mask for promotion pieces	 */
	public static final int PROMOTION_PIECE				= 0x000f;
	
	/**	bit flag for en-passant moves	 */
	public static final int EN_PASSANT_MOVE				= 0x0010;
	/**	bit flag for pawn double moves	 */
	public static final int PAWN_DOUBLE_MOVE			= 0x0020;

	//-------------------------------------------------------------------------------
	//	FEN strings
	//-------------------------------------------------------------------------------

	/**	the default start position	 */
	public static final String START_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	/**	the empty board	 */
	public static final String EMPTY_POSITION = "8/8/8/8/8/8/8/8 w - - 0 1";
	public static final String START_POSITION_SHREDDER_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w HAha - 0 1";
	public static final String POSITION_TEMPLATE = "......../pppppppp/8/8/8/8/PPPPPPPP/******** w - - 0 1";

	//-------------------------------------------------------------------------------
	//	used by FontEncoding
	//-------------------------------------------------------------------------------
	
	/**	light square	 */
	public static final int LIGHT_SQUARE	= 0;
	/**	dark square	 */
	public static final int DARK_SQUARE		= 1;
	/**	figurine	 */
	public static final int FIGURINE		= 2;
	
	//-------------------------------------------------------------------------------
	//	Millisecond constants
	//-------------------------------------------------------------------------------

	public static final long MILLISECOND		= 1L;
	public static final long SECOND				= MILLISECOND*1000;
	public static final long MINUTE				= SECOND*60;
	public static final long HOUR				= MINUTE*60;
	public static final long HOUR12				= HOUR*12;

}
