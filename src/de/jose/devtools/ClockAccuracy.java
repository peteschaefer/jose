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

package de.jose.devtools;

/**
 * tool to measure the accuracy of the system clock
 * for example:
 *  1 ms    on Linux
 *  55 ms   on Windows 98/ME
 *  10 ms   on Windows NT/2000/XP
 */

public class ClockAccuracy
{
    public static void main(String[] args)
    {
        long time = System.currentTimeMillis();
        for (;;) {
            long next = System.currentTimeMillis();
            if (next != time) {
                System.out.println(next-time);
                time = next;
            }
        }
    }
}
