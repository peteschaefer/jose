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

package de.jose.db.io;

/**
 * bits flags providing information about a single column
 * in a result set. for each column there a four bits that specifiy:
 * - whether a column IS NULL
 * - whether an integer value is negative
 * - and the number of bytes that are used to encode an integer
 *
 */
public interface IDumpFlags
{
	/**	indicates a NULL column	*/
	public static final byte	NULL		= 0x0f;
	/**	indicates the value 0	*/
	public static final byte	ZERO		= 0x00;
	/**	indicates the value 1	*/
	public static final byte	ONE			= 0x07;

	/**	indicates an unsigned integer that occupies 1 byte */
	public static final byte	UNSIGNED1	= 0x01;
	/**	indicates an unsigned integer that occupies 2 bytes */
	public static final byte	UNSIGNED2	= 0x02;
	/**	indicates an unsigned integer that occupies 3 bytes */
	public static final byte	UNSIGNED3	= 0x03;
	/**	indicates an unsigned integer that occupies 4 bytes */
	public static final byte	UNSIGNED4	= 0x04;
	/**	indicates an unsigned integer that occupies 5 bytes */
	public static final byte	UNSIGNED5	= 0x05;
	/**	indicates an unsigned integer that occupies 6 bytes */
	public static final byte	UNSIGNED6	= 0x06;

	/**	indicates a signed integer that occupies 1 byte	*/
	public static final byte	SIGNED1		= 0x09;
	/**	indicates a signed integer that occupies 2 bytes	*/
	public static final byte	SIGNED2		= 0x0a;
	/**	indicates a signed integer that occupies 3 bytes	*/
	public static final byte	SIGNED3		= 0x0b;
	/**	indicates a signed integer that occupies 4 bytes	*/
	public static final byte	SIGNED4		= 0x0c;
	/**	indicates a signed integer that occupies 5 bytes	*/
	public static final byte	SIGNED5		= 0x0d;
	/**	indicates a signed integer that occupies 6 bytes	*/
	public static final byte	SIGNED6		= 0x0e;

	/**	indicates an integer that occupies 8 bytes (always signed)	*/
	public static final byte	LONG		= 0x08;

}
