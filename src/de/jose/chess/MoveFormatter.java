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

import de.jose.Language;

import java.util.List;

abstract public class MoveFormatter
                implements Constants
{
    /** current format  */
    protected int format;


    /** kingside castling string   */
    public String    kingsideCastling   = "O-O";      //  these are Oh's not zero's !!
    /** queenside castling string   */
    public String    queensideCastling  = "O-O-O";    //  these are Oh's not zero's !!

    /** separator char (for long notation)  */
    public String   separator           = "-";
    /** capture separator   */
    public String   captureSeparator    = "x";
    /** promotion separator */
    public String   promotionSeparator  = "=";

    /** en passant string   */
    public String   enPassant           = " e.p.";

    /** check indicator */
    public String   check               = "+";
	/**	mate indicator	*/
	public String	mate				= "#";
	/**	stalemate indicator	*/
	public String	stalemate			= "=";
	/**	draw by threefold repetition	*/
	public String	draw3				= "=3";
	/**	draw by 50-moves-rule	*/
	public String	draw50				= "=50";
	/**	draw by insufficient material (not displayed)	*/
	public String	drawMat				= "";

	/** nullmove string */
	public String   nullmove            = "...";//"\u2026";//"\u205e";	// vertical dots?

    /** telegraphic files (lower half)  */
    public static final String TELEGRAPHIC_FILES_LOWER  = "BCDFGHKL";
    public static final String TELEGRAPHIC_FILES_UPPER  = "MNPRSTWZ";
    public static final String TELEGRAPHIC_ROWS         = "AEIO";

	/**	English piece descriptions	*/
	public String[] englishPieces = {
		null,
		"P", "Kt", "B", "R", "Q", "K",
	};

    /** long formatting   */
    public static final int LONG            = 1;
    /** short formatting  */
    public static final int SHORT           = 2;
    /** algebraic formatting    */
    public static final int ALGEBRAIC       = 3;
    /** correspondce chess formatting   */
    public static final int CORRESPONDENCE  = 4;
    /** telegraphic formatting   */
    public static final int TELEGRAPHIC     = 5;
    /** old English formatting  */
    public static final int ENGLISH         = 6;



    /** return values from isAmbiguous  */
    protected static final int  FILE_AMBIGUOUS  	= 0x01;
    protected static final int  ROW_AMBIGUOUS   	= 0x02;
    protected static final int  BOTH_AMBIGUOUS  	= 0x03;
	protected String[] pieceChars;

	public MoveFormatter()
    {
        this(LONG);
    }

    public MoveFormatter(int aFormat)
    {
        setFormat(aFormat);
    }

    public int getFormat()                  { return format; }

    public void setFormat(int aFormat)      { format = aFormat; }

	public String getPieceCharacter(int piece) {
		return pieceChars[EngUtil.uncolored(piece)];
	}

    /** callback methods    */
    abstract public void figurine(int pc, boolean promotion);
    /** text    */
    abstract public void text(String str, int castling);
    abstract public void text(char chr);

	public final void text(String str)						{ text(str,0); }

    public final void format(Move mv)                       { format(format,mv,null); }

    public final void format(int format, Move mv)           { format(format,mv,null); }

    public final void format(Move mv, Position pos)         { format(format,mv,pos); }

	public final void format(Move mv, Position pos, boolean chck)   { format(format,mv,pos,chck); }

	public final void formatCheck(Move mv)                  { formatCheck(format,mv); }

    public void format(int format, Move mv, Position pos)
    {
	    format(format,mv,pos,true);
    }

	public void format(int format, Move mv, Position pos, boolean chck)
	{
		if (mv.isNullMove()) {
			text(nullmove);
			return;
		}

        switch (format) {
        case LONG:              doFormat(mv,pos, LONG, 				true,chck,true); break;
        default:
        case SHORT:             doFormat(mv,pos, SHORT,				true,chck,true); break;
        case ALGEBRAIC:         doFormat(mv,pos, ALGEBRAIC,			false,chck,true); break;
        case CORRESPONDENCE:    doFormat(mv,pos, CORRESPONDENCE,	false,false,false); break;
        case TELEGRAPHIC:       doFormat(mv,pos, TELEGRAPHIC, 		false,false,false); break;
        case ENGLISH:           doFormat(mv,pos, ENGLISH, 			true,chck,true); break;
        }
    }

	public void formatCheck(int format, Move mv)
	{
		switch (format) {
		case LONG:
		default:
		case SHORT:
		case ALGEBRAIC:
		case ENGLISH:
				doCheck(mv); break;
		case CORRESPONDENCE:
		case TELEGRAPHIC:
				/* don't */ break;
		}
	}

	protected final void square(int sq, boolean origin)
	{
		square(sq,true,true,origin);
	}

	protected void square(int sq, boolean file, boolean row, boolean origin)
	{
		if (file) text(EngUtil.fileChar(EngUtil.fileOf(sq)));
		if (row) text(EngUtil.rowChar(EngUtil.rowOf(sq)));
	}

	protected void doFormat(Move mv, Position pos, int format, boolean castling, boolean chck, boolean ep)
	{
		if (!mv.isCastling()) castling = false;

		if	(castling) switch (mv.castlingMask()) {
		//  compatible with FRC castling !
			case WHITE_KINGS_CASTLING:
			case BLACK_KINGS_CASTLING:	text(kingsideCastling, mv.flags & CASTLING); break;
			case WHITE_QUEENS_CASTLING:
			case BLACK_QUEENS_CASTLING:	text(queensideCastling, mv.flags & CASTLING); break;
		}
		else switch (format) {
			case LONG:					doFormatLong(mv); break;
			default:
			case SHORT:					doFormatShort(mv,pos); break;
			case ALGEBRAIC:				doFormatAlgebraic(mv); break;
			case CORRESPONDENCE:		doFormatCorrespondence(mv); break;
			case TELEGRAPHIC:			doFormatTelegraphic(mv); break;
			case ENGLISH:				doFormatEnglish(mv,pos); break;
		}

		if (ep)
			if (mv.isEnPassant())
				text(enPassant,0);

		if (chck) doCheck(mv);
	}

	protected void doCheck(Move mv)
	{
		if (mv.isMate())
			text(mate);
		else if (mv.isStalemate())
			text(stalemate);
		else if (mv.isDraw3())
			text(draw3);
		else if (mv.isDraw50())
			text(draw50);
		else if (mv.isDrawMat())
			text(drawMat);
		else if (mv.isCheck())
			text(check);
	}


    protected void doFormatLong(Move mv)
    {
		if (mv.moving!=null) {
			int pc = EngUtil.uncolored(mv.moving.piece());
			if (pc!= PAWN)
				figurine(mv.moving.piece(),false);
		}

		square(mv.from,true);

		if (mv.isCapture())
			text(captureSeparator);
		else
			text(separator);

	    square(mv.to,false);

		if (mv.isPromotion()) {
			text(promotionSeparator);
			figurine(mv.getPromotionPiece(), true);
		}
    }

    protected void doFormatShort(Move mv, Position pos)
    {
		int pc = EngUtil.uncolored(mv.moving.piece());
		if (pc!= PAWN) {
			figurine(mv.moving.piece(),false);

			switch (isAmbiguous(pos,mv)) {
			case BOTH_AMBIGUOUS:
					square(mv.from, true,true, true);
					break;
			case FILE_AMBIGUOUS:
					square(mv.from, true,false, true);
					break;
			case ROW_AMBIGUOUS:
					square(mv.from, false,true, true);
					break;
			}
		}
		else {
			switch (isAmbiguous(pos,mv)) {
			case BOTH_AMBIGUOUS:    //  impossible !
					System.out.println("pawn move can not be ambiguous");
					//  fall-through intended
			case ROW_AMBIGUOUS:
					square(mv.from, true,true, true);
					break;
			case FILE_AMBIGUOUS:
					square(mv.from, true,false, true);
					break;
			default:
					if (mv.isCapture()) {
						square(mv.from, true,false, true);
					}
					break;
			}
		}

		if (mv.isCapture())
			text(captureSeparator);

	    square(mv.to, false);

		if (mv.isPromotion()) {
			text(promotionSeparator);
			figurine(mv.getPromotionPiece(), true);
		}
    }

    protected void doFormatAlgebraic(Move mv)
    {
	    square(mv.from,true);
	    square(mv.to,false);

        if (mv.isPromotion()) {
            text(promotionSeparator);
            figurine(mv.getPromotionPiece(), true);
        }
    }

    protected void doFormatCorrespondence(Move mv)
    {
        text((char)('1'+EngUtil.fileOf(mv.from)-FILE_A));
        text((char)('1'+EngUtil.rowOf(mv.from)-ROW_1));
        text((char)('1'+EngUtil.fileOf(mv.to)-FILE_A));
        text((char)('1'+EngUtil.rowOf(mv.to)-ROW_1));

        if (mv.isPromotion())
            switch (EngUtil.uncolored(mv.getPromotionPiece())) {
            case QUEEN:     text('1'); break;
            case ROOK:      text('2'); break;
            case BISHOP:    text('3'); break;
            case KNIGHT:    text('4'); break;
            }
    }

    protected void doFormatTelegraphic(Move mv)
    {
        int f1 = EngUtil.fileOf(mv.from)-FILE_A;
        int r1 = EngUtil.rowOf(mv.from)-ROW_1;
        int f2 = EngUtil.fileOf(mv.to)-FILE_A;
        int r2 = EngUtil.rowOf(mv.to)-ROW_1;

        if (r1<4) {
            text(TELEGRAPHIC_FILES_LOWER.charAt(f1));
            text(TELEGRAPHIC_ROWS.charAt(r1));
        }
        else {
            text(TELEGRAPHIC_FILES_UPPER.charAt(f1));
            text(TELEGRAPHIC_ROWS.charAt(7-r1));
        }

        if (r2<4) {
            text(TELEGRAPHIC_FILES_LOWER.charAt(f2));
            text(TELEGRAPHIC_ROWS.charAt(r2));
        }
        else {
            text(TELEGRAPHIC_FILES_UPPER.charAt(f2));
            text(TELEGRAPHIC_ROWS.charAt(7-r2));
        }

        if (mv.isPromotion())
            figurine(mv.getPromotionPiece(),true);
    }

    protected void doFormatEnglish(Move mv, Position pos)
    {
		int ambig = isAmbiguous(pos,mv);
		//	moving piece
		text(englishPieces[EngUtil.uncolored(mv.moving.piece())]);
		//	disambiguate, if necessary
		if (ambig!=0) {
			text('(');
			formatEnglishSquare(EngUtil.fileOf(mv.from), EngUtil.rowOf(mv.from), pos.whiteMovesNext());
			text(')');
		}

		if (mv.isCapture()) {
			int victim = mv.captured.piece();
			text(captureSeparator);
			text(englishPieces[EngUtil.uncolored(victim)]);

			if (isAmbiguousCapture(pos,mv)) {
				text('(');
				formatEnglishSquare(EngUtil.fileOf(mv.to), EngUtil.rowOf(mv.to), pos.whiteMovesNext());
				text(')');
			}
		}
		else {
			//	destination square
			text(separator);
			formatEnglishSquare(EngUtil.fileOf(mv.to), EngUtil.rowOf(mv.to), pos.whiteMovesNext());
		}

		//	promotion
		if (mv.isPromotion()) {
			text(promotionSeparator);
			text(englishPieces[mv.getPromotionPiece()]);
		}
    }

	protected void formatEnglishSquare(int file, int row, boolean whiteMoves)
	{
		switch (file) {
		case FILE_A:		//	queen's rook
							text(englishPieces[QUEEN]);
							text(englishPieces[ROOK]);
							break;
		case FILE_B:		//	queen's knight
							text(englishPieces[QUEEN]);
							text(englishPieces[KNIGHT]);
							break;
		case FILE_C:		//	queen's bishop
							text(englishPieces[QUEEN]);
							text(englishPieces[BISHOP]);
							break;
		case FILE_D:		//	queen
							text(englishPieces[QUEEN]);
							break;
		case FILE_E:		//	king
							text(englishPieces[KING]);
							break;
		case FILE_F:		//	kings's bishop
							text(englishPieces[KING]);
							text(englishPieces[BISHOP]);
							break;
		case FILE_G:		//	kings's knight
							text(englishPieces[KING]);
							text(englishPieces[KNIGHT]);
							break;
		case FILE_H:		//	kings's rook
							text(englishPieces[KING]);
							text(englishPieces[ROOK]);
							break;
		}

		if (whiteMoves)
			text((char)('1'+row-ROW_1));
		else
			text((char)('1'+ROW_8-row));
	}

    private static int isAmbiguous(Position pos, Move mv)
    {
		if ((EngUtil.uncolored(mv.moving.piece())==PAWN) && !mv.isCapture())
			return 0;	//	straight pawn moves can not be ambiguous

	    /*  examine fellow pieces   */
	    List pieces = pos.pieceList(mv.moving.piece());
		if (pieces.size() <= 1)
			return 0;	//	only one piece -> can not be ambiguous

        int result = 0;
        Move test = null;

        for (int i=pieces.size()-1; i >= 0; i--)
        {
            Piece pc = (Piece)pieces.get(i);
            if (pc.isVacant() || pc==mv.moving) continue;

	        if (test==null) test = new Move(mv);

            test.from = pc.square();
            if (pos.checkMove(test) && pc.checkMove(test)) {
                if (EngUtil.fileOf(test.from) == EngUtil.fileOf(mv.from))
                    result |= ROW_AMBIGUOUS;
                else
                    result |= FILE_AMBIGUOUS;
            }
        }

        return result;
    }

	private static boolean isAmbiguousCapture(Position pos, Move mv)
	{
		/*	examine potential victims	*/
		List victims = pos.pieceList(mv.captured.piece());
		if (victims.size() <= 1)
			return false;	//	only onec piece -> can not be ambiguous

		Move test = null;
		for (int i=victims.size()-1; i >= 0; i--)
		{
			Piece pc = (Piece)victims.get(i);
			if (pc.isVacant() || pc==mv.captured) continue;

			if (test==null) test = new Move(mv);

			test.captured = pc;
			test.to = pc.square();
			if (pos.checkMove(test) && pc.checkMove(test))
				return true;
		}
		return false;
	}

	public void setPieceChars(String chars)
    {
        if (chars==null)
            setPieceCharArray(null);
        else
			setPieceCharArray(parsePieceChars(chars));
    }

	public void setPieceCharArray(String[] chars)
	{
		if (chars==null)
			pieceChars = parsePieceChars(DEFAULT_PIECE_CHARACTERS);
		else
			pieceChars = chars;
	}

	public void setLanguage(String langCode)
    {
        if (langCode==null)
            setPieceChars(null);
        else
            setPieceChars(Language.get("fig."+langCode, null));
    }

	public String[] getPieceCharArray()
	{
		return pieceChars;
	}

	public static String[] parsePieceChars(String input)
	{
		String[] result = new String[KING+1];
		int i = 0;
		int p = PAWN;
		while (p <= KING) {
			if (input.charAt(i)=='"') {
				//	quoted
				int j = input.indexOf('"',i+1);
				result[p] = input.substring(i+1,j);
				i = j+1;
			}
			else {
				result[p] = input.substring(i,i+1);
				i++;
			}
			p++;
		}
		return result;
	}
}
