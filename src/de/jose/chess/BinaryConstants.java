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

/*

Binary Representation of Move Data
----------------------------------

Move and annotation data are stored as a sequence of bytes, terminated by 0xff.
The data is stored in the database column Game.GameText.

A halfmove is identified by
- the moving piece (a-pawn to h-pawn, queen's knigth, king's knight etc.)
- an offset that indicates the direction of the move (see 2. Move Offsets)

Promotion moves and moves by promoted pieces are encoded by TWO bytes.

This encoding was chosen to encode most moves with one byte.



1. Byte Codes

0x00 .. 0x03	move of the a-pawn (see 2.1)
0x04 .. 0x07	move of the b-pawn (see 2.1)
0x08 .. 0x0b	move of the c-pawn (see 2.1)
0x0c .. 0x0f	move of the d-pawn (see 2.1)
0x10 .. 0x13	move of the e-pawn (see 2.1)
0x14 .. 0x17	move of the f-pawn (see 2.1)
0x18 .. 0x1b	move of the g-pawn (see 2.1)
0x1c .. 0x1f	move of the h-pawn (see 2.1)

0x20 .. 0x27	move of the queen's knight (see 2.2)
0x28 .. 0x2f	move of the king's knight (see 2.2)

0x30 .. 0x3f	move of the queen's bishop (see 2.3)
0x40 .. 0x4f	move of the king's bishop (see 2.3)

0x50 .. 0x5f	move of the queen's rook (see 2.4)
0x60 .. 0x6f	move of the king's rook (see 2.4)

0x70 .. 0x8f	move of the queen (see 2.5)
0x90 .. 0x97	move of the king (see 2.6)

0x98			kingside castling
0x99			queenside castling

0x9a .. 0x9f	reserved

0xa0 [+]		promotion move: a-pawn
				additional data is stored in second byte (see 2.7)
0xa1 [+]		promotion move: b-pawn
0xa2 [+]		promotion move: c-pawn
0xa3 [+]		promotion move: d-pawn
0xa4 [+]		promotion move: e-pawn
0xa5 [+]		promotion move: f-pawn
0xa6 [+]		promotion move: g-pawn
0xa7 [+]		promotion move: h-pawn

0xa8 [+]		promoted piece move: "a" promotion piece
				additional data is stored in second byte  (see 2.8)
0xa9 [+]		promoted piece move: "b" promotion piece
0xaa [+]		promoted piece move: "c" promotion piece
0xab [+]		promoted piece move: "d" promotion piece
0xac [+]		promoted piece move: "e" promotion piece
0xad [+]		promoted piece move: "f" promotion piece
0xae [+]		promoted piece move: "g" promotion piece
0xaf [+]		promoted piece move: "h" promotion piece

0xb0 [+]		annotation
				NAG code stored in second byte

0xb1 .. 0xcf	annotation (NAG codes 1 to 31)

0xd0			result: black wins
0xd1			result: draw
0xd2			result: white wins
0xd3			result unknown

0xd4 .. 0xdf	reserved

0xe0 [+]		text comment
				followed by text data (see 3.)

0xe1 [+]		error: move not readable				followed by text data
0xe2 [+]		error: illegal move     				followed by text data
0xe3 [+]		error: ambiguous move					followed by text data
0xe4 [+]		error: unrecgonized input   			followed by text data

0xe5 .. 0xef	reserved

0xf0			start of variation:
				following move data is a variation to the previous move
0xf1			end of variation

0xf2 .. 0xfe	reserved

0xff			end of data


2. Encoding of Moves


2.1 Offsets of Pawn Moves

+---+---+---+
|   | 3 |   |
+---+---+---+
| 2 | 0 | 1 |
+---+---+---+
|   | x |   |
+---+---+---+


2.2 Offsets of Knight Moves

+---+---+---+---+---+
|   | 1 |   | 0 |   |
+---+---+---+---+---+
| 2 |   |   |   | 7 |
+---+---+---+---+---+
|   |   | x |   |   |
+---+---+---+---+---+
| 3 |   |   |   | 6 |
+---+---+---+---+---+
|   | 4 |   | 5 |   |
+---+---+---+---+---+


2.3 Offsets of Bishop Moves

+---+---+---+---+---+---+---+---+---+
|   | f |   |   |   |   |   | 7 |   |
+---+---+---+---+---+---+---+---+---+
|   |   | e |   |   |   | 6 |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   |   | d |   | 5 |   |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   |   |   | x |   |   |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   |   | 3 |   | b |   |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   | 2 |   |   |   | a |   |   |
+---+---+---+---+---+---+---+---+---+
|   | 1 |   |   |   |   |   | 9 |   |
+---+---+---+---+---+---+---+---+---+
| 0 |   |   |   |   |   |   |   | 8 |
+---+---+---+---+---+---+---+---+---+


2.4 Offsets of Rook Moves

+---+---+---+---+---+---+---+---+
|   |   |   |   | 7 |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 6 |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 5 |   |   |   |
+---+---+---+---+---+---+---+---+
| 8 | 9 | a | b | x | d | e | f |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 3 |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 2 |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 1 |   |   |   |
+---+---+---+---+---+---+---+---+
|   |   |   |   | 0 |   |   |   |
+---+---+---+---+---+---+---+---+


2.5 Offsets of Queen Moves

+---+---+---+---+---+---+---+---+---+
|   | f |   |   | 17|   |   | 7 |   |
+---+---+---+---+---+---+---+---+---+
|   |   | e |   | 16|   | 6 |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   |   | d | 15| 5 |   |   |   |
+---+---+---+---+---+---+---+---+---+
| 18| 19| 1a| 1b| x | 1d| 1e| 1f|   |
+---+---+---+---+---+---+---+---+---+
|   |   |   | 3 | 13| b |   |   |   |
+---+---+---+---+---+---+---+---+---+
|   |   | 2 |   | 12|   | a |   |   |
+---+---+---+---+---+---+---+---+---+
|   | 1 |   |   | 11|   |   | 9 |   |
+---+---+---+---+---+---+---+---+---+
| 0 |   |   |   | 10|   |   |   | 8 |
+---+---+---+---+---+---+---+---+---+


2.6 Offsets of King Moves

+---+---+---+
| 1 | 4 | 0 |
+---+---+---+
| 6 | x | 7 |
+---+---+---+
| 2 | 5 | 3 |
+---+---+---+

Kingside Castling = 8
Queenside Castling = 9

2.7 Promotion info stored in second byte

| . . . . |		uppermost 4 bits are reserved
| x x |			move offset (see 2.1)
| y y |			promotion piece (0=knight ..  3=queen)


2.8 Move info stored in second byte (promoted piece move)

| . . . |		uppermost 3 bits are reserved
| x x x x x |	move offset (see 2.2 to 2.5)



3. Encoding of text data

Comment text is stored as a sequence of bytes, terminated by 0x00

| b1 | b2 | ... | 0x00 |

characters are currently encoded in ISO-8859-1
but UTF-8 would be better

*/

public interface BinaryConstants
{
	/**
	 *	move codes are stored as bytes
	 * 	but constants are all short to avoid trouble with signed values
	 */

	public static final short SHORT_A_PAWN			= (short)0x00;
	public static final short SHORT_B_PAWN			= (short)0x04;
	public static final short SHORT_C_PAWN			= (short)0x08;
	public static final short SHORT_D_PAWN			= (short)0x0c;
	public static final short SHORT_E_PAWN			= (short)0x10;
	public static final short SHORT_F_PAWN			= (short)0x14;
	public static final short SHORT_G_PAWN			= (short)0x18;
	public static final short SHORT_H_PAWN			= (short)0x1c;

	public static final short SHORT_Q_KNIGHT		= (short)0x20;
	public static final short SHORT_K_KNIGHT		= (short)0x28;

	public static final short SHORT_Q_BISHOP		= (short)0x30;
	public static final short SHORT_K_BISHOP		= (short)0x40;

	public static final short SHORT_Q_ROOK			= (short)0x50;
	public static final short SHORT_K_ROOK			= (short)0x60;

	public static final short SHORT_QUEEN			= (short)0x70;
	public static final short SHORT_KING			= (short)0x90;

	public static final short SHORT_K_CASTLING		= (short)0x98;
	public static final short SHORT_Q_CASTLING		= (short)0x99;

	public static final short SHORT_NULLMOVE        = (short)0x9a;

	public static final short SHORT_A_PROMOTION		= (short)0xa0;
	public static final short SHORT_B_PROMOTION		= (short)0xa1;
	public static final short SHORT_C_PROMOTION		= (short)0xa2;
	public static final short SHORT_D_PROMOTION		= (short)0xa3;
	public static final short SHORT_E_PROMOTION		= (short)0xa4;
	public static final short SHORT_F_PROMOTION		= (short)0xa5;
	public static final short SHORT_G_PROMOTION		= (short)0xa6;
	public static final short SHORT_H_PROMOTION		= (short)0xa7;

	public static final short SHORT_A_PROMOTED		= (short)0xa8;
	public static final short SHORT_B_PROMOTED		= (short)0xa9;
	public static final short SHORT_C_PROMOTED		= (short)0xaa;
	public static final short SHORT_D_PROMOTED		= (short)0xab;
	public static final short SHORT_E_PROMOTED		= (short)0xac;
	public static final short SHORT_F_PROMOTED		= (short)0xad;
	public static final short SHORT_G_PROMOTED		= (short)0xae;
	public static final short SHORT_H_PROMOTED		= (short)0xaf;

	public static final short SHORT_MOVE_MAX		= (short)0xaf;

	public static final short SHORT_ANNOTATION		= (short)0xb0;
	public static final short SHORT_ANNOTATION_MAX	= (short)0xcf;

	public static final short SHORT_BLACK_WINS		= (short)0xd0;
	public static final short SHORT_DRAW			= (short)0xd1;
	public static final short SHORT_WHITE_WINS		= (short)0xd2;
	public static final short SHORT_UNKNOWN			= (short)0xd3;

	public static final short SHORT_COMMENT				= (short)0xe0;
	public static final short SHORT_ERROR_UNREADABLE	= (short)0xe1;
	public static final short SHORT_ERROR_ILLEGAL		= (short)0xe2;
	public static final short SHORT_ERROR_AMBIGUOUS		= (short)0xe3;
	public static final short SHORT_ERROR_UNRECOGNIZED	= (short)0xe4;
	public static final short SHORT_ERROR_EMPTY			= (short)0xe5;

	public static final short SHORT_ERROR_MAX			= (short)0xef;

	public static final short SHORT_START_OF_LINE		= (short)0xf0;
	public static final short SHORT_END_OF_LINE			= (short)0xf1;


	public static final short SHORT_END_OF_DATA			= (short)0xff;

	/**	vectors of rider pieces
	 */
	public static final byte BYTE_HORIZONTAL		= (byte)0x01;
	public static final byte BYTE_VERTICAL			= (byte)0x02;
	public static final byte BYTE_DIAGONAL_UP		= (byte)0x03;
	public static final byte BYTE_DIAGONAL_DOWN		= (byte)0x04;

}
