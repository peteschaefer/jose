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

import de.jose.util.StringUtil;
import de.jose.util.ByteBuffer;

import java.util.Random;
import java.io.ByteArrayOutputStream;

/**
 * JoseHashKey
 * 
 * @author Peter Schäfer
 */

public class JoseHashKey
        extends HashKey
{
	//-------------------------------------------------------------------------------
	//	static fields
	//-------------------------------------------------------------------------------

	static final long serialVersionUID = 4875011349899303833L;

	/**	maps squares & colors & pieces to random (but constants) values	 */
	protected static long [] randMap;

	static {
		Random rand = new Random(0xfafafaL);
		int size = OUTER_BOARD_SIZE * 2 * KING;
		randMap = new long[size];
		for (int i=0; i<size; i++)
			randMap[i] = rand.nextLong();
	}

    //-------------------------------------------------------------------------------
    //	commonly used values
    //-------------------------------------------------------------------------------

    public static final long START_POSITION                         = 0xa065945577a5f54eL;
    public static final long START_POSITION_REVERSED                = 0xa065945577a5f57eL;
    public static final long START_POSITION_IGNORE_FLAGS            = 0xa065945577a5825eL;

    public long getInitialValue(boolean ignoreFlags, boolean reversed)
    {
        if (ignoreFlags)
            return START_POSITION_IGNORE_FLAGS;
        else if (reversed)
            return START_POSITION_REVERSED;
        else
            return START_POSITION;
    }

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public JoseHashKey(boolean reversed)
	{
		super(reversed);
	}


	//-------------------------------------------------------------------------------
	//	implement abstract methods
	//-------------------------------------------------------------------------------


	public void set(int square, int piece)
	{
		theValue ^= randValue(square,piece);
	}

	public void set(int flags)
	{
        if (!ignoreFlags) {
            if (isReversed)
                flags = EngUtil.reverseFlags(flags);
            theValue ^= flags;
        }
	}

	public void clear(int square, int piece)
	{
		//	XOR is reversible, of course
		set(square,piece);
	}

	public void clear(int flags)
	{
		//	XOR is reversible, of course
		set(flags);
	}

	public void clear()
	{
		theValue = 0L;
	}

	//-------------------------------------------------------------------------------
	//	private parts
	//-------------------------------------------------------------------------------

	private long randValue(int square, int piece)
	{
		if (isReversed) {
			square = EngUtil.mirrorSquare(square);
			piece = EngUtil.oppositeColor(piece);
		}

		int i = square;
		int j = EngUtil.isWhite(piece) ? 0:1;
		int k = EngUtil.uncolored(piece)-PAWN;

		return randMap[(i*2 + j)*KING + k];
	}

    public static void main(String[] args)
    {
        Position pos = new Position();
        pos.setup(Board.START_POSITION);

        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,false);
        pos.computeHashKeys();

        System.out.print("public static final long START_POSITION = 0x");
        System.out.print(Long.toHexString(pos.getHashKey().value()));
        System.out.println("L;");

        System.out.print("public static final long START_POSITION_REVERSED = 0x");
        System.out.print(Long.toHexString(pos.getReversedHashKey().value()));
        System.out.println("L;");

        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,true);
        pos.computeHashKeys();

        System.out.print("public static final long START_POSITION_IGNORE_FLAGS = 0x");
        System.out.print(Long.toHexString(pos.getHashKey().value()));
        System.out.println("L;");

        System.out.print("public static final long START_POSITION_REVERSED_IGNORE_FLAGS = 0x");
        System.out.print(Long.toHexString(pos.getReversedHashKey().value()));
        System.out.println("L;");


        pos.setup(Board.EMPTY_POSITION);

        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,false);
        pos.computeHashKeys();

        System.out.print("public static final long EMPTY_POSITION = 0x");
        System.out.print(Long.toHexString(pos.getHashKey().value()));
        System.out.println("L;");

        System.out.print("public static final long EMPTY_POSITION_REVERSED = 0x");
        System.out.print(Long.toHexString(pos.getReversedHashKey().value()));
        System.out.println("L;");

        pos.setOption(Position.IGNORE_FLAGS_ON_HASH,true);
        pos.computeHashKeys();

        System.out.print("public static final long EMPTY_POSITION_IGNORE_FLAGS = 0x");
        System.out.print(Long.toHexString(pos.getHashKey().value()));
        System.out.println("L;");

        System.out.print("public static final long EMPTY_POSITION_REVERSED_IGNORE_FLAGS = 0x");
        System.out.print(Long.toHexString(pos.getReversedHashKey().value()));
        System.out.println("L;");

    }
}