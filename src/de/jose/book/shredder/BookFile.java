// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packfields(3) packimports(7) fieldsfirst splitstr(64) nonlb space lnc radix(10) lradix(10) 

package de.jose.book.shredder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BookFile
	implements DataInput, DataOutput {

	protected RandomAccessFile file;
	byte buffer[];

	public BookFile(String fileName, String mode) throws IOException {
		this (new RandomAccessFile(fileName, mode));
	}

	public BookFile(RandomAccessFile file) {
		this.file = file;
		buffer = new byte[8];
	}

	public final short readShort() throws IOException {
		file.readFully(buffer, 0, 2);
		return (short)((buffer[1] & 255) << 8 | buffer[0] & 255);
	}

	public final int readUnsignedShort() throws IOException {
		file.readFully(buffer, 0, 2);
		return (buffer[1] & 255) << 8 | buffer[0] & 255;
	}

	public final char readChar() throws IOException {
		file.readFully(buffer, 0, 2);
		return (char)((buffer[1] & 255) << 8 | buffer[0] & 255);
	}

	public final int readInt() throws IOException {
		file.readFully(buffer, 0, 4);
		return buffer[3] << 24 | (buffer[2] & 255) << 16 | (buffer[1] & 255) << 8 | buffer[0] & 255;
	}

	public final long readLong() throws IOException {
		file.readFully(buffer, 0, 8);
		return (long)buffer[7] << 56 | (long)(buffer[6] & 255) << 48 | (long)(buffer[5] & 255) << 40 | (long)(buffer[4] & 255) << 32 | (long)(buffer[3] & 255) << 24 | (long)(buffer[2] & 255) << 16 | (long)(buffer[1] & 255) << 8 | (long)(buffer[0] & 255);
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public final void writeShort(int i) throws IOException {
		buffer[0] = (byte)i;
		buffer[1] = (byte)(i >> 8);
		file.write(buffer, 0, 2);
	}

	public final void writeChar(int i) throws IOException {
		buffer[0] = (byte)i;
		buffer[1] = (byte)(i >> 8);
		file.write(buffer, 0, 2);
	}

	public final void writeInt(int i) throws IOException {
		buffer[0] = (byte)i;
		buffer[1] = (byte)(i >> 8);
		buffer[2] = (byte)(i >> 16);
		buffer[3] = (byte)(i >> 24);
		file.write(buffer, 0, 4);
	}

	public final void writeLong(long l) throws IOException {
		buffer[0] = (byte)(int)l;
		buffer[1] = (byte)(int)(l >> 8);
		buffer[2] = (byte)(int)(l >> 16);
		buffer[3] = (byte)(int)(l >> 24);
		buffer[4] = (byte)(int)(l >> 32);
		buffer[5] = (byte)(int)(l >> 40);
		buffer[6] = (byte)(int)(l >> 48);
		buffer[7] = (byte)(int)(l >> 56);
		file.write(buffer, 0, 8);
	}

	public final void writeFloat(float f) throws IOException {
		writeInt(Float.floatToIntBits(f));
	}

	public final void writeDouble(double d) throws IOException {
		writeLong(Double.doubleToLongBits(d));
	}

	public final void writeChars(String s) throws IOException {
		int i = s.length();
		for (int j = 0; j < i; j++)
			writeChar(s.charAt(j));

	}

	public final long getFilePointer() throws IOException {
		return file.getFilePointer();
	}

	public final void readFully(byte abyte0[]) throws IOException {
		file.readFully(abyte0, 0, abyte0.length);
	}

	public final void readFully(byte abyte0[], int i, int j) throws IOException {
		file.readFully(abyte0, i, j);
	}

	public final int skipBytes(int i) throws IOException {
		return file.skipBytes(i);
	}

	public final boolean readBoolean() throws IOException {
		return file.readBoolean();
	}

	public final byte readByte() throws IOException {
		return file.readByte();
	}

	public final int readUnsignedByte() throws IOException {
		return file.readUnsignedByte();
	}

	public final String readLine() throws IOException {
		return file.readLine();
	}

	public final String readUTF() throws IOException {
		return file.readUTF();
	}

	public final void seek(long l) throws IOException {
		file.seek(l);
	}

	public final synchronized void write(int i) throws IOException {
		file.write(i);
	}

	public final synchronized void write(byte abyte0[], int i, int j) throws IOException {
		file.write(abyte0, i, j);
	}

	public final void writeBoolean(boolean flag) throws IOException {
		file.writeBoolean(flag);
	}

	public final void writeByte(int i) throws IOException {
		file.writeByte(i);
	}

	public final void writeBytes(String s) throws IOException {
		file.writeBytes(s);
	}

	public final void writeUTF(String s) throws IOException {
		file.writeUTF(s);
	}

	public final void write(byte abyte0[]) throws IOException {
		file.write(abyte0, 0, abyte0.length);
	}

	public final void close() throws IOException {
		file.close();
	}
}
