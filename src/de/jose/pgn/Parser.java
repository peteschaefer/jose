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

package de.jose.pgn;


import de.jose.Application;
import de.jose.Util;
import de.jose.Language;
import de.jose.chess.*;
import de.jose.util.StringUtil;

import java.util.Arrays;

public class Parser
		implements Constants, PgnConstants, BinaryConstants
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------


	/**	token types	 */
	public static final int LEGAL_MOVE			= 0;		//	"Ne4"
    public static final int MOVE_NUMBER			= 1;		//	"17."
    public static final int ANNOTATION			= 2;		//	"!?", "+-"
	public static final int CHECK_ANNOTATION	= 3;		//	"+"
//  public static final int NAG					= 4;		//	"$14"
    public static final int RESULT				= 5;		//	"1-0"
	public static final int PASS				= 6;		//	?
	public static final int COMMENT				= 7;		//	" { ... } "
    public static final int START_VARIATION		= 8;		//	" ( ... "
    public static final int END_VARIATION		= 9;		//	" ) "
    public static final int INSTRUCTION         = 10;       //  " % "
    public static final int UNRECOGNIZED		= SHORT_ERROR_UNRECOGNIZED;
    public static final int EMPTY_INPUT			= SHORT_ERROR_EMPTY;
    public static final int ILLEGIBLE_MOVE		= SHORT_ERROR_UNREADABLE;
    public static final int IMPOSSIBLE_MOVE		= SHORT_ERROR_ILLEGAL;
    public static final int AMBIGUOUS_MOVE		= SHORT_ERROR_AMBIGUOUS;

	/**	castling strings     */
	protected static final String KINGSIDE_CASTLING	= "O-O";
	protected static final String QUEENSIDE_CASTLING = "O-O-O";
	
	protected static final int KINGSIDE_LEN = KINGSIDE_CASTLING.length();
	protected static final int QUEENSIDE_LEN = QUEENSIDE_CASTLING.length();

	protected static final byte PIECE_CHAR		= 0x01;
	protected static final byte MOVE_CHAR		= 0x02;
	protected static final byte DIGIT_CHAR		= 0x04;
	protected static final byte CHECK_CHAR		= 0x10;
	
	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	protected byte[] charTypes = null;
	protected byte[] char2piece = null;
	public String pieceChars;

	/**	character input	 */
    public char line[];
	/**	start of input	 */
    public int offset;
	/**	length of resulting token	 */
    public int len;
	/**	end of input	 */
    public int max;
	/**	token type (see above)	 */
    public int type;
	/**	level of nested variations (0 = main line)	 */
    public int variation_level;
	/**	result of number	 */
    public int number;
	/**	move results	  */
	public Move[] moves;
    /** set if some variation lines are present */
    public boolean hasVariations;
    /** set if some comments are present    */
    public boolean hasComments;
    /** set if some errors have been detected   */
    public boolean hasErrors;
	/**	set if a result element is present	*/
	public boolean hasResult;
	public int result;

	/**	check legal moves strictly ?	 */
	public boolean strictlyLegal;
	/**	Chess Engine	 */
	protected Position pos;

	/**	the binary result	 */
	byte[] bin;
	/**	current position	 */
	int boffset;

    /** the comment result */
    byte[] comments;
    /** current position    */
    int coffset;
	/** classic, or FRC ? */
	boolean classic=true;


	//-------------------------------------------------------------------------------
	//	Ctor
	//-------------------------------------------------------------------------------
	
	public Parser(boolean strict)
	{
		this(new Position(), 0, strict);
	}

	public Parser(Position p, int posOptions, boolean strict)
	{
		setPosition(p);
		pos.setOptions(posOptions);
		/*	turn off optional features:
			don't compute hash key
			don't detect check
			don't detect exposed check
			don't detect stalemate
			don't detect repetition
			don't detect 50 moves rule
		*/
		moves = new Move[16];
		for (int i=0; i<16; i++) moves[i] = new Move(0,0);
		
		strictlyLegal = strict;

		pieceChars = DEFAULT_PIECE_CHARACTERS;
		initCharTypes(false);
	}

	public void setPosition(Position pos)
	{
		this.pos = pos;
		classic = pos==null || pos.isClassic();
	}

	public void setPosition(String fen)
	{
		pos.setup(fen);
		classic = pos.isClassic();
	}

	public boolean isClassic()
	{
		return classic;
	}

	public void setStrictlyLegal(boolean strictlyLegal) {
		this.strictlyLegal = strictlyLegal;
	}

	public final void parse(char[] text, byte[] binResult, byte[] commentResult, boolean reset)
	{
		parse(text,0, text.length, binResult,0, commentResult,0,reset);
	}
	
	public void parse(char[] text, int start, int length,
					  byte[] binary, int bstart, byte[] comment,
	                  int cstart, boolean reset)
	{
		if (reset) pos.reset();
		
		line = text;
		offset = start;
		max = offset+length;
		len = 0;
		
		bin = binary;
		boffset = bstart;
        comments = comment;
        coffset = cstart;

		hasVariations = false;
        hasComments = false;
        hasErrors = false;
		hasResult = false;
		result = PgnConstants.RESULT_UNKNOWN;

		while (offset < max) {
			parseToken();
			
			switch (type) {
			case LEGAL_MOVE:		
				callbackBeforeMove(moves[0]);

//				pos.setOption(Position.INCREMENT_HASH|Position.INCREMENT_REVERSED_HASH,true);
				pos.doMove(moves[0]);
//				pos.setOption(Position.INCREMENT_HASH|Position.INCREMENT_REVERSED_HASH,false);

				callbackLegalMove(moves[0]);
				break;
				
			case MOVE_NUMBER:		/* currently ignored */ break;
			case CHECK_ANNOTATION:	/* currently ignored */break;
																				
			case ANNOTATION:
                    hasComments = true;
                    callbackAnnotation(number); break;
				
			case RESULT:
					hasResult = true;
					result = number;
                    callbackResult(number); break;
				
			case PASS:				break;	
									
			case COMMENT:
                    hasComments = true;
                    callbackComment(line,offset+1,len-2); break;

            case INSTRUCTION:
                    callbackInstruction(line,offset+1,len-1); break;

			case START_VARIATION:
				if (pos.ply() <= 0) {
					//  start variation at ply 0 not allowed
					hasErrors = true;
					callbackError(SHORT_ERROR_UNRECOGNIZED,line,offset,len);
				}
				else {
					hasVariations = true;
					pos.startVariation();
					callbackStartVariation();
				}
				break;
				
			case END_VARIATION:
				if (pos.getVariationLevel() <= 0) {
					//  close variation without opening
					hasErrors = true;
					callbackError(SHORT_ERROR_UNRECOGNIZED,line,offset,len);
				}
				else {
					callbackEndVariation();
					pos.undoVariation();
				}
				break;
				
			case ILLEGIBLE_MOVE:
                    hasErrors = true;
					strictlyLegal = true;   //  apply strict checking for subsequent moves !
                    callbackError(SHORT_ERROR_UNREADABLE, line, offset, len); break;
									
			case IMPOSSIBLE_MOVE:
                    hasErrors = true;
					strictlyLegal = true;   //  apply strict checking for subsequent moves !
                    callbackError(SHORT_ERROR_ILLEGAL, line,offset,len); break;
			case AMBIGUOUS_MOVE:
                    hasErrors = true;
                    callbackError(SHORT_ERROR_AMBIGUOUS, line,offset,len); break;

			case UNRECOGNIZED:
                    hasErrors = true;
					strictlyLegal = true;   //  apply strict checking for subsequent moves !
                    callbackError(SHORT_ERROR_UNRECOGNIZED, line, offset, len); break;
			
			case EMPTY_INPUT:		break;		
			
			default:				throw new IllegalStateException();
			}
			
			offset += len;
		}
	}

    /** @return true if the recent call to parse() detected variation lines */
	public final boolean hasVariations()                { return hasVariations; }

    /** @return true if the recent call to parse() detected user comments */
	public final boolean hasComments()                  { return hasComments; }

    /** @return true if the recent call to parse() detected user comments */
	public final boolean hasErrors()                    { return hasErrors; }

	/**	@return true if a result element was detected	*/
	public final boolean hasResult()					{ return hasResult; }

	public int getResult()								{ return result; }

    public int getBinLength()                           { return boffset; }

    public int getCommentsLength()                      { return coffset; }

	public final Move parseMove(String text)
	{
		return parseMove(text.toCharArray(), 0, text.length());
	}
	
	public Move parseMove(char[] text, int start, int length)
	{
		if (pos.isGameFinished()) return null;

		int oldOptions = pos.getOptions();
		pos.setOptions(0xffffffff);

		try {
			line = text;
			offset = start;
			max = offset+length;
			len = 0;

			while (offset < max) {
				parseToken();

				switch (type) {
				case LEGAL_MOVE:
					pos.setOptions(oldOptions);
					return moves[0];

				case MOVE_NUMBER:   //  ignore
				case ANNOTATION:
				case CHECK_ANNOTATION:
				case EMPTY_INPUT:
				case PASS:          //  ignore
					break;

				default:
					return null;
				}

				offset += len;
			}
		} finally {
			pos.setOptions(oldOptions);
		}

		return null;
	}

	protected void callbackBeforeMove(Move mv)
	{
		/* noop */
	}

	protected void callbackLegalMove (Move mv)
	{
		short code = mv.encode();
		
		bin[boffset++] = (byte)(code & 0x00ff);
		code >>= 8;
		if (code != 0)
			bin[boffset++] = (byte)(code & 0x00ff);
	}
	
	protected void callbackAnnotation (int nag)
	{
		if (nag >= 1 && nag <= (SHORT_ANNOTATION_MAX-SHORT_ANNOTATION))
			bin[boffset++] = (byte)(SHORT_ANNOTATION + nag);
		else
		{
			bin[boffset++] = (byte)SHORT_ANNOTATION;
			bin[boffset++] = (byte)nag;
		}
	}
	
	protected void callbackResult (int result)
	{
		switch (result)
		{
		case WHITE_WINS:		bin[boffset++] = (byte)SHORT_WHITE_WINS; break;
		case BLACK_WINS:		bin[boffset++] = (byte)SHORT_BLACK_WINS; break;
		case DRAW:				bin[boffset++] = (byte)SHORT_DRAW; break;
		case RESULT_UNKNOWN:	bin[boffset++] = (byte)SHORT_UNKNOWN; break;
		}
	}
	
	protected void callbackComment(char[] line, int start, int len)
	{
       bin[boffset++] = (byte)SHORT_COMMENT;
       appendComment(start,len);
	}

    protected void callbackInstruction(char[] line, int start, int len)
    {
/*
        if (StringUtil.startsWith(line,"jose:styles:",start,true))
        {
            stylerun = new StyleRun();
            stylerun.parse(line,start+12,len-12);
        }
*/
    }

	protected void callbackStartVariation()
	{
		bin[boffset++] = (byte)SHORT_START_OF_LINE;
	}
	
	protected void callbackEndVariation()
	{
		bin[boffset++] = (byte)SHORT_END_OF_LINE;
	}
	
	protected void callbackError(short errorCode, char[] line, int start, int len)
	{
		bin[boffset++] = (byte)errorCode;
        appendComment(start,len);

		StringBuffer message = new StringBuffer("Parse error: ");
		switch (errorCode) {
		case SHORT_ERROR_UNRECOGNIZED:  message.append("unrecognized token "); break;
		case SHORT_ERROR_EMPTY:         message.append("empty "); break;
		case SHORT_ERROR_UNREADABLE:    message.append("unreadable "); break;
		case SHORT_ERROR_ILLEGAL:       message.append("illegal move "); break;
		case SHORT_ERROR_AMBIGUOUS:		message.append("ambiguous move "); break;
		}

		message.append(" at col. ");
		message.append(start);
		message.append(",");
		message.append(len);
		message.append(" === ");
		message.append(line, 0, Math.min(start+len,line.length));
		Application.warning(message.toString());
	}

    protected void appendComment(int start, int len)
    {
	    len = Math.min(len,line.length-start);

	    //  encode UTF-8
        while (len-- > 0) {
            char c = line[start++];
	        if (c < 0x0080) {
                comments[coffset++] = (byte)(c & 0x007f);
            }
            else if (c < 0x0800) {
	            /** encode UTF-8    */
                comments[coffset++] = (byte)(0xc0 | (c>>6) & 0x1f);
                comments[coffset++] = (byte)(0x80 | c & 0x3f);
            }
            else {
                comments[coffset++] = (byte)(0xe0 | (c >> 12) & 0x0f);
                comments[coffset++] = (byte)(0x80 | (c >> 6) & 0x3f);
                comments[coffset++] = (byte)(0x80 | c & 0x3f);
            }
        }
        comments[coffset++] = 0;		//	marks end of comment
    }

	//-------------------------------------------------------------------------------
	//	Aux. Methods
	//-------------------------------------------------------------------------------

	protected void initCharTypes(boolean caseSensitive)
	{
		if (charTypes==null)
			charTypes = new byte[256];
		else
			Arrays.fill(charTypes,(byte)0);

		setBit(pieceChars, PIECE_CHAR);
		if (!caseSensitive) setBit(pieceChars.toLowerCase(), PIECE_CHAR);
		//  note that lower case piece characters can conflict with the following:
		setBit("abcdefgh-x:=p",		MOVE_CHAR);
		setBit("0123456789",		DIGIT_CHAR);
//		setBit("?!",				ANNOTATION_CHAR);
		setBit("+#",				CHECK_CHAR);

		if (char2piece==null)
			char2piece = new byte[256];
		else
			Arrays.fill(char2piece,(byte)0);

		for (int i=0; i<pieceChars.length(); i++)
		{
			char c = pieceChars.charAt(i);
			char2piece[c] = (byte)(PAWN+i);
			if (!caseSensitive) char2piece[Character.toLowerCase(c)] = (byte)(PAWN+i);
		}
	}
	
	protected void setBit(String s, byte bit)
	{
		for (int i=0; i<s.length(); i++)
			charTypes[s.charAt(i)] |= bit;
	}

	protected void clearBit(String s, byte bit)
	{
		for (int i=0; i<s.length(); i++)
			charTypes[s.charAt(i)] &= ~bit;
	}

	public void setLanguage(String lang, boolean caseSensitive)
	{
		String newchars = Language.get("fig."+lang,null);
		if (newchars==null) newchars = DEFAULT_PIECE_CHARACTERS;
		if (!Util.equals(pieceChars,newchars)) {
			pieceChars=newchars;
			initCharTypes(caseSensitive);
		}
	}

	public boolean is1PieceChar(char c, boolean caseSensitive)
	{
		if (!caseSensitive) c = Character.toUpperCase(c);
		return pieceChars.indexOf(c) >= 0;
	}

	public boolean is1PieceChar(String text, boolean caseSensitive)
	{
		return (text.length()==1) && is1PieceChar(text.charAt(0),caseSensitive);
	}

	/**
	 *	parses a number or result
	 */
    protected final void parseDigits()
    {
		type = MOVE_NUMBER;
        number = 0;
        
		for (;;) {
			int i = last() - '0';
            number = 10 * number + i;

			if (getCharType(next()) == DIGIT_CHAR)
				len++;
			else
				break;
		}

		switch (next()) {
		case '.':	do { len++; } while (next()=='.'); return;

		case '/':
		case '-':	parseResult(); return;
		}
		
    }
	
	protected final void parseResult()
	{
		if (StringUtil.equalsIgnoreCase(STRING_UNKNOWN, line, offset, STRING_UNKNOWN.length())) {
			type = RESULT;
			len = STRING_UNKNOWN.length();
			number = RESULT_UNKNOWN;
		}
		else if (StringUtil.equalsIgnoreCase(STRING_WHITE_WINS, line, offset,STRING_WHITE_WINS.length())) {
			type = RESULT;
			len = STRING_WHITE_WINS.length();
			number = WHITE_WINS;
		}
		else if (StringUtil.equalsIgnoreCase(STRING_DRAW, line, offset,STRING_DRAW.length())) {
			type = RESULT;
			len = STRING_DRAW.length();
			number = DRAW;
		}
		else if (StringUtil.equalsIgnoreCase(STRING_DRAW_SHORT, line, offset,STRING_DRAW_SHORT.length())) {
			type = RESULT;
			len = STRING_DRAW_SHORT.length();
			number = DRAW;
		}
		else if (StringUtil.equalsIgnoreCase(STRING_BLACK_WINS, line, offset,STRING_BLACK_WINS.length())) {
			type = RESULT;
			len = STRING_BLACK_WINS.length();
			number = BLACK_WINS;
		} 
		else {
			type = UNRECOGNIZED;
		}
	}
	
	protected final void skipWhite()
	{
		while(offset < max && Character.isWhitespace(peek(0)))
			offset++;
	}
	
	protected final int getCharType(char c)				        { return charTypes[c]; }

	protected final int getPiece(char c)                        { return char2piece[c]; }

    public final boolean inVariation()							{ return variation_level > 0; }

    protected final char peek(int i)							{ return line[i < 0 ? offset + len + i : offset + i]; }
	
    public final boolean atEnd()							    { return offset >= max; }

    protected final boolean more()							    { return offset + len < max; }

    protected final char last()									{ return line[offset+len-1]; }

	protected char next()										
	{ 
	   if(more()) 
		   return line[offset+len];
       else
           return '\0';
    }
    

    public void parseToken()
    {
		skipWhite();
		
        if(offset >= max)
        {
            type = EMPTY_INPUT;
            return;
        }
		
		len = 1;
		switch (last()) {
/*		case ';':	len = max - offset;
					type = COMMENT;		//	single line comment
					return;	
*/		
		case '{':	// skip to closing brace
                    type = COMMENT;
					while(more() && last() != '}')
						len++;
					return;
					
		case ';':	//  skip to end of line
                    type = COMMENT;
					len++;
					while (more() && next() != '\n')
						len++;
					return;

        case '%':   //  skip to end of line
                    type = INSTRUCTION;
                    len++;
					while (more() && next() != '\n')
						len++;
                    return;

		case '(':	variation_level++;
					type = START_VARIATION;
					return;
					
		case ')':	variation_level--;
				    type = END_VARIATION;
					return;
					
		case '$':	len++;
					parseDigits();
					type = ANNOTATION;
					return;
					
		case '?':
		case '!':	parseAnnotation();
					return;
					
		case 'o':
		case 'O':	parseCastling();
					return;

		case '.':   parseDigits();
					return;

		case '*':	type = RESULT;
					number = RESULT_UNKNOWN;
					return;
		}
		
		switch (getCharType(last()))
		{
		case MOVE_CHAR:
		case PIECE_CHAR:
		case MOVE_CHAR+PIECE_CHAR:
								parseMoveText();
								return;
								
		case DIGIT_CHAR:		parseDigits();
								return;
								
		case CHECK_CHAR:		type = CHECK_ANNOTATION;
								while(getCharType(next())==CHECK_CHAR) 
									len++;
								return;
		}
		
		//	else:
		type = UNRECOGNIZED;
    }

	protected void parseAnnotation()
	{
		switch (last()) {
		case '?':
			switch (next()) {
			case '?':	/* ?? */	len++; number = NAG_VERY_BAD; break;
			case '!':	/* ?! */	len++; number = NAG_DUBIOUS; break;
			default:	/* ?  */    number = NAG_BAD; break;
			}
			break;
			
		case '!':
			switch (next()) {
			case '?':	/* !? */	len++; number = NAG_INTERESTING; break;
			case '!':	/* !! */	len++; number = NAG_VERY_GOOD; break;
			default:	/* !  */	number = NAG_GOOD; break;
			}
			break;
		}
		type = ANNOTATION;
	}
	
	protected void parseCastling()
	{
		if((max-offset) >= QUEENSIDE_LEN &&
		   StringUtil.equalsIgnoreCase(QUEENSIDE_CASTLING, line, offset,QUEENSIDE_LEN))
		{
			len = QUEENSIDE_LEN;
				//  FRC
			if (pos.whiteMovesNext()) {
				moves[0].from = pos.kingSquare(WHITE); // E1;
				moves[0].to = moves[0].from-2;
			}
			else {
				moves[0].from = pos.kingSquare(BLACK); // E8;
				moves[0].to = moves[0].from-2;
			}

			moves[0].flags = 0;
				
			checkMoves(1, strictlyLegal, true);
			return;
		}

		if((max-offset) >= KINGSIDE_LEN &&
		   StringUtil.equalsIgnoreCase(KINGSIDE_CASTLING, line, offset,KINGSIDE_LEN))
		{
			len = KINGSIDE_LEN;
				//  FRC
			if (pos.whiteMovesNext()) {
				moves[0].from = pos.kingSquare(WHITE);// E1;
				moves[0].to = moves[0].from+2;
			}
			else {
				moves[0].from = pos.kingSquare(BLACK); // E8;
				moves[0].to = moves[0].from+2;
			}

			moves[0].flags = 0;
					
			checkMoves(1, strictlyLegal, true);
		    return;
		}
		//	else:
		type=ILLEGIBLE_MOVE;
		return;
	}
	
	
    protected void parseMoveText()
    {
        type = LEGAL_MOVE;
	
		while ((getCharType(next()) & (PIECE_CHAR+MOVE_CHAR+DIGIT_CHAR)) != 0)
			len++;
		
        int moving = -1;
        int file1 = -1;
        int row1 = -1;
        int file2 = -1;
        int row2 = -1;
        int promo = -1;
        int state = -1;
		
		/*
					N a 4 x c 6 = Q
		state:	   6 5 4   3 2   1 0
		*/
		
        for(int j = len - 1; j >= 0; j--)
        {
            char c = peek(j);
            switch(state)
            {
			case -1:			//	ep ?
				if (j > 0 && peek(j-1)=='e' && c == 'p') { 
					j--;
					state = 0; 
					break; 
				}
				//	fall through
            case 0:				//	promotion char
                int p = getPiece(c);
                if(p >= KNIGHT && p <= QUEEN)
                {
                    promo = p;
                    state = 1;
                    break;
                }
                // fall through
			case 1:				//	dest row
                if(c == '=' || c == ':')
                {
                    state = 1;
                    break;
                }
                int r = EngUtil.char2Row(c);
                if(r >= ROW_1 && r <= ROW_8)
                {
                    row2 = r;
                    state = 2;
                    break;
                }
                // fall through
            case 2:				//	dest file
                int f = EngUtil.char2File(c);
                if(f >= FILE_A && f <= FILE_H)
                {
                    file2 = f;
                    state = 3;
                    break;
                }
                // fall through
            case 3:				//	orig row
                if(c == 'x' || c == '-' || c == ':')
                {
                    state = 3;
                    break;
                }
                r = EngUtil.char2Row(c);
                if(r >= ROW_1 && r <= ROW_8)
                {
                    row1 = r;
                    state = 4;
                    break;
                }
                // fall through
            case 4:				//	orig file
                f = EngUtil.char2File(c);
                if(f >= FILE_A && f <= FILE_H)
                {
                    file1 = f;
                    state = 5;
                    break;
                }
                // fall through
            case 5:				//	moving piece
				p = getPiece(c);
                if(p >= PAWN && p <= KING)
                {
                    moving = p;
                    state = 6;
                    break;
                }
                // fall through
            case 6:				//	?
                type = ILLEGIBLE_MOVE;
                return;
            }
        }

        if(file2 < 0 || row2 < 0)
        {
            type = ILLEGIBLE_MOVE;
            return;
        }

		if (((file1 < 0) || (row1 < 0)) && (moving < 0))
        	moving = PAWN;
		if (promo < 0) promo = QUEEN;
		
        int destination = EngUtil.square(file2, row2);
		
        int count = pos.getCandidateMoves(moving, file1, row1, destination, promo, moves);
		
		count = checkMoves(count, strictlyLegal, false);
	}
	
	public int checkMoves(int count, boolean strict, boolean checkLegal)
	{
		//  TODO are FRC castling gestures recognized ?
		switch(count)
        {
        case 0:		type = IMPOSSIBLE_MOVE; return count;
        case 1:
			boolean legal;
			if (strict)
				legal = checkMoveStrict(moves[0]);	//	strict checking
			else if (checkLegal)
				legal = pos.checkMove(moves[0]) && moves[0].moving.checkMove(moves[0]);
            else
                legal = true;           //	loose legality check has already been done
			type = legal ? LEGAL_MOVE:IMPOSSIBLE_MOVE;
			return count;
			
		default:	
			//	have to apply strict legality check
			int newCount=0;
			for (int i=0; i<count; i++)
				if (checkMoveStrict(moves[i]))
					Util.swap(moves,i,newCount++);
				
			switch (newCount) {
			case 0:		type = IMPOSSIBLE_MOVE; return newCount;
			case 1:		type = LEGAL_MOVE; return newCount;
			default:	type = AMBIGUOUS_MOVE;
						number = newCount;
						return newCount;
			}
        }
    }
	
	public boolean checkMoveStrict(Move mv)
	{
		//	detect if own king is exposed to check
		int oldOptions = pos.getOptions();
		pos.setOptions(Position.DETECT_ALL);
		
		boolean result = pos.tryMove(mv);
		if (result) pos.undoMove();
		
		pos.setOptions(oldOptions);
		return result;
	}

}
