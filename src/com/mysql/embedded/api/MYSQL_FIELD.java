/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package com.mysql.embedded.api;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Java wrapper for the MYSQL_FIELD structure
 * @author Peter Sch�fer
 */

/*
		typedef struct st_mysql_field {
		  char *name;                 /* Name of column * /
		char *org_name;             /* Original column name, if an alias * /
		char *table;                /* Table of column if column was a field * /
		char *org_table;            /* Org table name, if table was an alias * /
		char *db;                   /* Database for table * /
		char *catalog;	      /* Catalog for table * /
		char *def;                  /* Default value (set by mysql_list_fields) * /
		unsigned long length;       /* Width of column (create length) * /
		unsigned long max_length;   /* Max width for selected set * /
		unsigned int name_length;
		unsigned int org_name_length;
		unsigned int table_length;
		unsigned int org_table_length;
		unsigned int db_length;
		unsigned int catalog_length;
		unsigned int def_length;
		unsigned int flags;         /* Div flags * /
		unsigned int decimals;      /* Number of decimals in field * /
		unsigned int charsetnr;     /* Character set * /
		enum enum_field_types type; /* Type of field. See mysql_com.h for types * /
		  } MYSQL_FIELD;

*/
public class MYSQL_FIELD
        extends StructWrapper
{
	protected String name;
	protected String org_name;
	protected String table;
	protected String org_table;
	protected String db;
	protected String catalog;
	protected String def;

	//  ----------------------------
	//      ctor
	//  ----------------------------

	public MYSQL_FIELD(ByteBuffer b, long nativeAddress, int offset)
	{
		attach(b,nativeAddress,offset);
	}

	public static MYSQL_FIELD[] newArray(ByteBuffer buffer, int count)
	{
		buffer.order(ByteOrder.nativeOrder());
		long nativeAddress = res.get_address(buffer);

		MYSQL_FIELD[] fields = new MYSQL_FIELD[count];
		for (int i=0; i < fields.length; i++)
			fields[i] = new MYSQL_FIELD(buffer,nativeAddress, i*MYSQL_FIELD.SIZE);

		return fields;
	}


	//  ----------------------------
	//      accessors
	//  ----------------------------

	public String getName() {
		if (name==null) name = res.to_string(getPointer(OFFSET_NAME), getInt(OFFSET_NAME_LENGTH));
		return name;
	}

	public String getOrgName() {
		if (org_name==null) org_name = res.to_string(getPointer(OFFSET_ORG_NAME), getInt(OFFSET_ORG_NAME));
		return org_name;
	}

	public String getTable() {
		if (table==null) table = res.to_string(getPointer(OFFSET_TABLE), getInt(OFFSET_TABLE_LENGTH));
		return table;
	}

	public String getOrgTable() {
		if (org_table==null) org_table = res.to_string(getPointer(OFFSET_ORG_TABLE), getInt(OFFSET_ORG_TABLE_LENGTH));
		return org_table;
	}

	public String getDatabase() {
		if (db==null) db = res.to_string(getPointer(OFFSET_DB), getInt(OFFSET_DB));
		return db;
	}

	public String getCatalog() {
		if (catalog==null) catalog = res.to_string(getPointer(OFFSET_CATALOG), getInt(OFFSET_CATALOG));
		return catalog;
	}

	public String getDefault() {
		if (def==null) def = res.to_string(getPointer(OFFSET_DEF), getInt(OFFSET_DEF));
		return def;
	}

	public int getLength()                  { return (int)getLong(OFFSET_LENGTH); }

	public int getMaxLength()               { return (int)getLong(OFFSET_MAX_LENGTH); }

	public int getFlags()                   { return getInt(OFFSET_FLAGS); }

	public int getDecimals()                { return getInt(OFFSET_DECIMALS); }

	public int getCharsetNr()               { return getInt(OFFSET_CHARSETNR); }

	public int getType()                    { return getInt(OFFSET_TYPE); }

	//  ----------------------------
	//      memory layout
	//  ----------------------------

	public static int SIZE;
	protected static int OFFSET_NAME;
	protected static int OFFSET_ORG_NAME;
	protected static int OFFSET_TABLE;
	protected static int OFFSET_ORG_TABLE;
	protected static int OFFSET_DB;
	protected static int OFFSET_CATALOG;
	protected static int OFFSET_DEF;
	protected static int OFFSET_LENGTH;
	protected static int OFFSET_MAX_LENGTH;
	protected static int OFFSET_NAME_LENGTH;
	protected static int OFFSET_ORG_NAME_LENGTH;
	protected static int OFFSET_TABLE_LENGTH;
	protected static int OFFSET_ORG_TABLE_LENGTH;
	protected static int OFFSET_DB_LENGTH;
	protected static int OFFSET_CATALOG_LENGTH;
	protected static int OFFSET_DEF_LENGTH;
	protected static int OFFSET_FLAGS;
	protected static int OFFSET_DECIMALS;
	protected static int OFFSET_CHARSETNR;
	protected static int OFFSET_TYPE;

	protected static native int sizeof_MYSQL_FIELD();
	protected static native int offset_name();
	protected static native int offset_org_name();
	protected static native int offset_table();
	protected static native int offset_org_table();
	protected static native int offset_db();
	protected static native int offset_catalog();
	protected static native int offset_def();
	protected static native int offset_length();
	protected static native int offset_max_length();
	protected static native int offset_name_length();
	protected static native int offset_org_name_length();
	protected static native int offset_table_length();
	protected static native int offset_org_table_length();
	protected static native int offset_db_length();
	protected static native int offset_catalog_length();
	protected static native int offset_def_length();
	protected static native int offset_flags();
	protected static native int offset_decimals();
	protected static native int offset_charsetnr();
	protected static native int offset_type();

	static
	{
		SIZE = sizeof_MYSQL_FIELD();
		OFFSET_NAME = offset_name();
		OFFSET_ORG_NAME = offset_org_name();
		OFFSET_TABLE = offset_table();
		OFFSET_ORG_TABLE = offset_org_table();
		OFFSET_DB = offset_db();
		OFFSET_CATALOG = offset_catalog();
		OFFSET_DEF = offset_def();
		OFFSET_LENGTH = offset_length();
		OFFSET_MAX_LENGTH = offset_max_length();
		OFFSET_NAME_LENGTH = offset_name_length();
		OFFSET_ORG_NAME_LENGTH = offset_org_name_length();
		OFFSET_TABLE_LENGTH = offset_table_length();
		OFFSET_ORG_TABLE_LENGTH = offset_org_table_length();
		OFFSET_DB_LENGTH = offset_db_length();
		OFFSET_CATALOG_LENGTH = offset_catalog_length();
		OFFSET_DEF_LENGTH = offset_def_length();
		OFFSET_FLAGS = offset_flags();
		OFFSET_DECIMALS = offset_decimals();
		OFFSET_CHARSETNR = offset_charsetnr();
		OFFSET_TYPE = offset_type();
	}

}
