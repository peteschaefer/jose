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
 * represents a move
 */

public class Move
		implements Constants, Cloneable
{
	//-------------------------------------------------------------------------------
	//	fields
	//-------------------------------------------------------------------------------

	/**	the origin square	 */
	public int	from;
	/**	the destination square	 */
	public int	to;
	/**	bit flags for castling, promotion piece and en-passant	
	 *	this field is filled in by Move.checkMove()
	 * */
	public int	flags;
	
	/**	the following fields are filled in by Position.checkMove()	 */
	/**	moving piece	 */
	public Piece moving;
	/**	captured piece	 */
	public Piece captured;

	/** represents a skip move  */
	public static final Move NULLMOVE = new Move();

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	protected Move()
	{ }

    public Move (Move mv, Board newOwner)
    {
        this();
        copy(mv,newOwner);
    }

	public Move (Move mv)
	{
	    this();
	    copy(mv,null);
	}

	public Move(int aFrom, int aTo)
	{
		from = aFrom;
		to = aTo;
		flags = 0;
	}
	
	public void copy(Move mv, Board newOwner)
	{
		from = mv.from;
		to = mv.to;
		flags = mv.flags;
		if (newOwner==null || newOwner==getOwner())
		{
			moving = mv.moving;     //  keep existing owner
			captured = mv.captured;
		}
		else
			newOwner.chown(this);   //  assign to new owner
	}

	public Object clone()
	{
		return new Move(this,null);
	}

	public boolean isPrepared()
	{
		return (this.isNullMove()) || (moving!=null);
	}

 	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	public final int castlingMask()				{ return EngUtil.castlingMask(flags); }
	public final boolean isCastling()			{ return EngUtil.isCastling(flags); }

	public final boolean isFRCCastling()
	{
		switch (castlingMask())
		{
		case WHITE_KINGS_CASTLING:  return from!=E1 || to!=H1;
		case WHITE_QUEENS_CASTLING:  return from!=E1 || to!=A1;
		case BLACK_KINGS_CASTLING:  return from!=E8 || to!=H8;
		case BLACK_QUEENS_CASTLING:  return from!=E8 || to!=A8;
		}
		return false;
	}

	public final boolean isStationaryFRCCastling()
	{
		switch (castlingMask())
		{
			case WHITE_KINGS_CASTLING:  	return from==G1;
			case WHITE_QUEENS_CASTLING:  	return from==C1;
			case BLACK_KINGS_CASTLING:  	return from==G8;
			case BLACK_QUEENS_CASTLING:  	return from==C8;
		}
		return false;
	}


	public final boolean isPromotion()			{ return EngUtil.isPromotion(flags); }

	public final boolean isNullMove() {
		return from==0 && to==0;
	}

	public final int getPromotionPiece()		{
		return EngUtil.getPromotionPiece(flags);
	}
	public final int getColoredPromotionPiece() {
		int pc = getPromotionPiece();
		pc = EngUtil.uncolored(pc);
		int co = moving.color();
		return pc+co;
	}
	public final int getCapturedPiece() {
		if (captured==null)
			return EMPTY;
		else
			return captured.piece;
	}

	public final void setPromotionPiece(int p) 	{ flags = EngUtil.setPromotionPiece(flags,p); }
	
	public final boolean isEnPassant()			{ return EngUtil.isEnPassant(flags); }
	public final boolean isPawnDouble()			{ return EngUtil.isPawnDouble(flags); }
	public final boolean isCheck()				{ return EngUtil.isCheck(flags); }
	public final boolean isCapture()			{ return captured != null; }

	public final boolean isGameFinished(boolean strict)		{ return EngUtil.isGameFinished(flags,strict); }

	public final boolean isMate()				{ return EngUtil.isMate(flags); }
	public final boolean isStalemate()			{ return EngUtil.isStalemate(flags); }

	public final boolean isDraw3()				{ return EngUtil.isDraw3(flags); }
	public final boolean isDraw50()				{ return EngUtil.isDraw50(flags); }
	public final boolean isDrawMat()			{ return EngUtil.isDrawMat(flags); }

	public void setEnPassant(boolean on)		{ flags = EngUtil.setEnPassant(flags,on); }
	public void setPawnDouble(boolean on)		{ flags = EngUtil.setPawnDouble(flags,on); }

	public void setMate()						{ flags = Util.setMask(flags,MATE,MATE); }
	public void setStalemate()					{ flags = Util.setMask(flags,MATE,STALEMATE); }

	public Board getOwner()
	{
		if (moving!=null)
			return moving.getOwner();
		else
			return null;
	}

	/**
	 * @return the square of the captured pawn
	 */
	public int getEnPassantSquare()
	{
		return EngUtil.square(EngUtil.fileOf(to), EngUtil.rowOf(from));
	}
	
	/**	overwrites Object.equals()	*/
	public boolean equals(Object obj)
	{
		if (obj instanceof Move) {
			Move mv = (Move)obj;
			return (from==mv.from) && (to==mv.to) &&
                    (getPromotionPiece()==mv.getPromotionPiece());
		}
		else
			return false;
	}
	
	public double distance()
	{
		return EngUtil.distance(from, to);
	}

	public String toString()
	{
	    return StringMoveFormatter.getDefaultFormatter().toString(StringMoveFormatter.LONG, this);
	}

	public static Move fromString(String ucistr)
	{
		Move mv = new Move();
		mv.from = EngUtil.char2Square(ucistr.charAt(0),ucistr.charAt(1));
		mv.to = EngUtil.char2Square(ucistr.charAt(2),ucistr.charAt(3));
		if (ucistr.length() > 4)
			mv.setPromotionPiece(EngUtil.char2Piece(ucistr.charAt(4)));
		return mv;
	}

	public final short encode()
	{
		if (this.isNullMove())
			return BinaryConstants.SHORT_NULLMOVE;
		else
			return moving.encodeMove(this);
	}

}
