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

package de.jose.book.crafty;

/**
 * Crafty Random number generator
 * 
 * @author Robert Hyatt
 */

public class CraftyRandom
{
/*
 random numbers from Mathematica 2.0.
 SeedRandom = 1;
 Table[Random[Integer, {0, 2^32 - 1}]
 */
	static final long x[] = {
		1410651636L, 3012776752L, 3497475623L, 2892145026L, 1571949714L,
		3253082284L, 3489895018L, 387949491L, 2597396737L, 1981903553L,
		3160251843L, 129444464L, 1851443344L, 4156445905L, 224604922L,
		1455067070L, 3953493484L, 1460937157L, 2528362617L, 317430674L,
		3229354360L, 117491133L, 832845075L, 1961600170L, 1321557429L,
		747750121L, 545747446L, 810476036L, 503334515L, 4088144633L,
		2824216555L, 3738252341L, 3493754131L, 3672533954L, 29494241L,
		1180928407L, 4213624418L, 33062851L, 3221315737L, 1145213552L,
		2957984897L, 4078668503L, 2262661702L, 65478801L, 2527208841L,
		1960622036L, 315685891L, 1196037864L, 804614524L, 1421733266L,
		2017105031L, 3882325900L, 810735053L, 384606609L, 2393861397L
	};

	long y[] = new long[55];
	int j, k;

	public CraftyRandom()
	{
		int       i;
		for (i = 0; i < 55; i++)
			y[i] = x[i];
		j = 24 - 1;
		k = 55 - 1;
	}

	public long Random32()
	{
		long ul = (y[k] += y[j]);
		if (--j < 0)
			j = 55 - 1;
		if (--k < 0)
			k = 55 - 1;
		return ul & 0x00000000ffffffffL;
	}

	public long Random64()
	{
		long r1, r2;

		r1 = Random32();
		r2 = Random32();
		return r1 | (r2 << 32);
	}
}