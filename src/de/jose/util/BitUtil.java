package de.jose.util;

/**
 * Simple bit arithmetic utils
 *
 */
public class BitUtil
{

    public static boolean is(long mat, long flag)    { return (mat&flag) != 0L; }

    public static int get2(long mat, int offset)     { return (int)(mat >> offset) & 0x0003; }
    public static int get3(long mat, int offset)     { return (int)(mat >> offset) & 0x0007; }
    public static int get4(long mat, int offset)     { return (int)(mat >> offset) & 0x000f; }
    public static int get5(long mat, int offset)     { return (int)(mat >> offset) & 0x001f; }
    public static int get6(long mat, int offset)     { return (int)(mat >> offset) & 0x003f; }
    public static int get8(long mat, int offset)     { return (int)(mat >> offset) & 0x00ff; }

    public static long set2(int value, int offset)     { return ((long)(value & 0x003)) << offset; }
    public static long set6(int value, int offset)     { return ((long)(value & 0x03f)) << offset; }

    public static long clip2(int value, int offset)     { return set2(Math.min(value,3),offset); }

    public static long minus8(long l1, long l2) {
        return (l1 & ~l2) & 0x0ffL;
    }
}
