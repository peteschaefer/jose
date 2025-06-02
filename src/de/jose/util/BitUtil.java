package de.jose.util;

/**
 * Simple bit arithmetic utils
 *
 */
public class BitUtil
{

    public static boolean is(long mat, long flag)    { return (mat&flag) != 0L; }

    public static boolean get1(long mat, int offset) { return is(mat, 1L<<offset); }
    public static int get2(long mat, int offset)     { return (int)(mat >> offset) & 0x0003; }
    public static int get3(long mat, int offset)     { return (int)(mat >> offset) & 0x0007; }
    public static int get4(long mat, int offset)     { return (int)(mat >> offset) & 0x000f; }
    public static int get5(long mat, int offset)     { return (int)(mat >> offset) & 0x001f; }
    public static int get6(long mat, int offset)     { return (int)(mat >> offset) & 0x003f; }
    public static int get8(long mat, int offset)     { return (int)(mat >> offset) & 0x00ff; }

    public static long set1(int value, int offset)     { return ((long)(value & 0x001)) << offset; }
    public static long set2(int value, int offset)     { return ((long)(value & 0x003)) << offset; }
    public static long set3(int value, int offset)     { return ((long)(value & 0x007)) << offset; }
    public static long set6(int value, int offset)     { return ((long)(value & 0x03f)) << offset; }

    public static long clip2(int value, int offset)     { return set2(Math.min(value,3),offset); }

    public static long minus8(long l1, long l2) {
        return (l1 & ~l2) & 0x0ffL;
    }

    public static long clear1(long value, int offset) {
        return (value & ~(0x01L<<offset));
    }
    public static long clear2(long value, int offset) {
        return (value & ~(0x03L<<offset));
    }
    public static long clear3(long value, int offset) {
        return (value & ~(0x07L<<offset));
    }
    public static long clear6(long value, int offset) {
        return (value & ~(0x3fL<<offset));
    }

    public static long clear(long value, long mask) {
        return value & ~mask;
    }

    public static long least(long f) {
        return Long.lowestOneBit(f);
    }

    public static long next(long f, long f0) {
        f &= -(f0 << 1);
        return least(f);
    }

    public static long prev(long f, long f0) {
        f &= f0-1;
        return Long.highestOneBit(f);
    }

    public static int indexOf(long bit) {
        return Long.numberOfTrailingZeros(bit);
    }

    public static long reverse32(long x) {
        return    ((x & 0xffffffff00000000L) >> 32)
                | ((x & 0x00000000ffffffffL) << 32);
    }
    public static long reverse16(long x) {
        return    ((x & 0xffff0000ffff0000L) >> 16)
                | ((x & 0x0000ffff0000ffffL) << 16);
    }
    public static long reverse8(long x) {
        return    ((x & 0xff00ff00ff00ff00L) >> 8)
                | ((x & 0x00ff00ff00ff00ffL) << 8);
    }
    public static long reverse4(long x) {
        return    ((x & 0xf0f0f0f0f0f0f0f0L) >> 4)
                | ((x & 0x0f0f0f0f0f0f0f0fL) << 4);
    }
    public static long reverse2(long x) {
        return    ((x & 0xccccccccccccccccL) >> 2)
                | ((x & 0x3333333333333333L) << 2);
    }
    public static long reverse1(long x) {
        return    ((x & 0xaaaaaaaaaaaaaaaaL) >> 1)
                | ((x & 0x5555555555555555L) << 1);
    }
    public static long reverseBits(long x) {
        x = reverse32(x);
        x = reverse16(x);
        x = reverse8(x);
        x = reverse4(x);
        x = reverse2(x);
        x = reverse1(x);
        return x;
    }
}
