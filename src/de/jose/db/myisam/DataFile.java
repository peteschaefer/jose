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


package de.jose.db.myisam;

import de.jose.util.map.IntHashMap;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * DataFile
 * 
 * @author Peter Schäfer
 */

public class DataFile
{
	protected RandomAccessFile file;
	protected int[] col_types;
	protected int nullable;

	protected static final Integer ZERO = 0;

	public static final int INT1    = 1;
	public static final int INT2    = 2;
	public static final int INT3    = 3;
	public static final int INT4    = 4;

	public static final int VARCHAR    = 10;
	public static final int CHAR       = 11;

	public static final int BLOB2    = 22;
	public static final int BLOB3    = 23;
	public static final int BLOB4    = 24;

	public static final int CLOB2    = 32;
	public static final int CLOB3    = 33;
	public static final int CLOB4    = 34;

	protected byte[] string_bytes = new byte[256];

	public DataFile(File file, int[] types, int nullable) throws FileNotFoundException
	{
		this.file = new RandomAccessFile(file,"r");
		this.col_types = types;
		this.nullable = nullable;
	}

	public void close() throws IOException
	{
		this.file.close();
	}

	public boolean eof() throws IOException
	{
		return file.getFilePointer() >= file.length();
	}

	protected Object[] read_record() throws IOException
	{
		Object[] result = new Object[col_types.length];

		int block_type = file.readByte();
		int rec_len = 0;
		int data_len = 0;
		int block_len = 0;
		long next_pos,prev_pos;

		switch (block_type) {
		case 0: //  DELETED BLOCK
			block_len = read_low_int(INT3);
			next_pos = file.readLong();
			prev_pos = file.readLong();
			file.skipBytes(block_len);
			return null;

		case 1:
			rec_len = data_len = block_len = file.readShort();
			//  data starts at 3
			break;
		case 3:
			rec_len = data_len = file.readShort(); //  header+1
			block_len = rec_len+file.read();    //  header[3]
			//  data starts at 4
			break;
		case 5:
			//  TODO split records
			rec_len = file.readShort();     //  header+1
			block_len = data_len = file.readShort();    //  header+3
			next_pos = file.readLong();     //  header+5
			//  data starts at 13 and continues ?
			break;
		default:
			throw new UnsupportedOperationException("block type not implemented: "+block_type);
		}

		//  bit flags indicating zero
		int col_count = col_types.length;
		int nullable_count = countBits(nullable);

		int zero_flags,null_flags;

		if (col_count<=8)
			zero_flags = read_low_int(INT1);
		else if (col_count<=16)
			zero_flags = read_low_int(INT2);
		else if (col_count<=24)
			zero_flags = read_low_int(INT3);
		else if (col_count<=32)
			zero_flags = read_low_int(INT4);
		else
			throw new UnsupportedOperationException();

		if (nullable_count<=8)
			null_flags = read_low_int(INT1);
		else if (nullable_count<=16)
			null_flags = read_low_int(INT2);
		else if (nullable_count<=24)
			null_flags = read_low_int(INT3);
		else if (nullable_count<=32)
			null_flags = read_low_int(INT4);
		else
			throw new UnsupportedOperationException();

		int nullable_flags = nullable;
		zero_flags = 0;

		//  now comes the data
		for (int i=0; i<col_types.length; i++)
		{
			boolean is_zero = (zero_flags & 0x01) != 0;
			zero_flags >>= 1;

			boolean is_nullable = (nullable_flags & 0x01) != 0;
			nullable_flags >>= 1;

			boolean is_null = false;
			if (is_nullable) {
				is_null = (null_flags & 0x01) != 0;
				null_flags >>= 1;
			}

			switch (col_types[i]) {
			case VARCHAR:
				result[i] = read_string();
				break;

			case INT1:
			case INT2:
			case INT3:
			case INT4:
				if (is_zero)
					result[i] = ZERO;
				else
					result[i] = read_low_int(col_types[i]);
				break;

			case BLOB2:
			case BLOB3:
			case BLOB4:
				if (!is_null)
					result[i] = read_blob(col_types[i]-BLOB2+INT2);
				break;

			case CLOB2:
			case CLOB3:
			case CLOB4:
				if (!is_null) {
					byte[] bytes = read_blob(col_types[i]-CLOB2+INT2);
					switch (bytes.length%2) {
					case 1: file.read();
					}
					result[i] = new String(bytes);
				}
				break;

			default:
				throw new UnsupportedOperationException();
			}

			if (is_null)
				result[i] = null;
		}

		//  skip empty bytes
		file.skipBytes(block_len-rec_len);
		return result;
	}

	protected int countBits(int x) {
		int count = 0;
		while (x!=0) {
			if ((x & 0x01) != 0) count++;
			x >>= 1;
		}
		return count;
	}

	protected int read_low_int(int type) throws IOException
	{
		switch (type) {
		case INT1:  return file.read();
		case INT2:	return file.read() | file.read() << 8;
		case INT3:	return file.read() | file.read() << 8 | file.read() << 16;
		case INT4:	return file.read() | file.read() << 8 | file.read() << 16 | file.read() << 24;
		default:    throw new IllegalArgumentException();
		}
	}


	protected int read_high_int(int type) throws IOException
	{
		switch (type) {
		case INT1:  return file.read();
		case INT2:	return file.read() << 8 | file.read();
		case INT3:	return file.read() << 16 | file.read() << 8 | file.read();
		case INT4:	return file.read() << 24 | file.read() << 16 | file.read() << 8 | file.read();
		default:    throw new IllegalArgumentException();
		}
	}

	protected String read_string() throws IOException
	{
		byte string_len = file.readByte();
		if (string_len==0) return "";

		file.readFully(string_bytes,0,string_len);
		return new String(string_bytes,0,string_len);
	}

	protected byte[] read_blob(int type) throws IOException
	{
		int blob_len = read_low_int(type);

		byte[] data = new byte[blob_len];
		file.readFully(data,0,blob_len);
		return data;
	}

	public static void main(String[] args) throws IOException
	{
		int[] col_types =  {
			INT4,
			VARCHAR, VARCHAR,
			VARCHAR, VARCHAR,
			VARCHAR,
			VARCHAR,
			BLOB3, CLOB2,// Types.CLOB, Types.CLOB,
		};
		//  all columns are nullable but the first
		int nullable = 0x01FE;

		File file = new File("D:/jose/work/database2/mysql/jose/MoreGame.MYD");
/*
 0, Field       , Type          , Null , Key  , Default, Extra
 1, 'GId'       , 'int(11)'     , ''   , 'PRI', '0'    , ''
 2, 'WhiteTitle', 'varchar(32)' , 'YES', 'MUL', null   , ''
 3, 'BlackTitle', 'varchar(32)' , 'YES', 'MUL', null   , ''
 4, 'Round'     , 'varchar(32)' , 'YES', 'MUL', null   , ''
 5, 'Board'     , 'varchar(32)' , 'YES', 'MUL', null   , ''
 6, 'FEN'       , 'varchar(128)', 'YES', ''   , null   , ''
 7, 'Info'      , 'varchar(255)', 'YES', 'MUL', null   , ''
 8, 'Bin'       , 'mediumblob'  , 'YES', ''   , null   , ''
 9, 'Comments'  , 'mediumtext'  , 'YES', 'MUL', null   , ''
10, 'PosMain'   , 'mediumtext'  , 'YES', 'MUL', null   , ''
11, 'PosVar'    , 'mediumtext'  , 'YES', 'MUL', null   , ''

*/
		DataFile df = new DataFile(file,col_types, nullable);
		while (!df.eof()) {
			Object[] data = df.read_record();
			if (data==null) continue;   //  deleted

			System.out.print("(");
			for (int i=0; i < data.length; i++) {
				if (i > 0) System.out.print(",");
				System.out.print(data[i]);
			}
			System.out.println(")");
		}
	}
}