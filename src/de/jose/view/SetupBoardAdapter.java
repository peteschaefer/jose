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

package de.jose.view;

import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.chess.Move;
import de.jose.chess.Position;


/**
 *
 * @author Peter Schäfer
 */
public class SetupBoardAdapter
        implements IBoardAdapter
{
	public Position pos;


	public SetupBoardAdapter(Position pos) {
		this.pos = pos;
	}


	public Position getPosition() { return pos;	}

	public int pieceAt(int square)
	{
		return pieceAt(EngUtil.fileOf(square),EngUtil.rowOf(square));
	}

	public int pieceAt(int file, int row)
	{
		switch (row) {
		case 0:		return Constants.WHITE+file;		//	white BOX piece
		case 1:		return Constants.BLACK+file;		//	black BOX piece
		default:	return pos.pieceAt(file,row);		//	board piece
		}
	}

	public int movesNext() 						{ return pos.movesNext(); }

	public boolean canMove(int square)
	{
		switch (EngUtil.rowOf(square)) {
		case 0:		//	BOX piece
		case 1:		int pc = EngUtil.fileOf(square);
					return (pc>=Constants.PAWN) && (pc<=Constants.KING);

		default:	return EngUtil.innerSquare(square) && !pos.isEmpty(square);	//	board piece
		}
	}

	public boolean isLegal(Move mv)
	{
		if (EngUtil.innerSquare(mv.to))
			mv.captured = pos.piece(mv.to);

		switch (EngUtil.rowOf(mv.from)) {
		case 0:		// 	BOX piece must move to board
		case 1:		return EngUtil.innerSquare(mv.to);

		default:	mv.moving = pos.piece(mv.from);
					if (!EngUtil.innerSquare(mv.to)) {
						//	delete piece; animate to BOX
						int pc = EngUtil.uncolored(mv.moving.piece());
						mv.to = EngUtil.square(pc, mv.moving.isWhite() ? 0:1);
					}
					return true;
		}
	}

	public void userMove(Move mv)
	{
		if (mv==null) return;   //  indicates that the position was reset completely
								//  derived classes might intercept

		//	no legality check, etc.
		if (mv.captured != null)
			pos.deletePiece(mv.captured);

		switch (EngUtil.rowOf(mv.from)) {
		case 0:		//	insert from BOX
					int pc = EngUtil.fileOf(mv.from);
					pos.addPiece(mv.to, Constants.WHITE+pc);
					break;

		case 1:		//	insert from BOX
					pc = EngUtil.fileOf(mv.from);
					pos.addPiece(mv.to, Constants.BLACK+pc);
					break;

		default:	if (EngUtil.innerSquare(mv.to))
						pos.movePiece(mv.from,mv.to);	//	move on board
					else
						pos.deletePiece(mv.moving);		//	move off board
					break;
		}
	}
	
}
