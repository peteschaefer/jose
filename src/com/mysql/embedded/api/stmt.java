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

package com.mysql.embedded.api;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.sql.SQLException;

/**
 * JNI wrappers for prepared statement API
 * 
 * @author Peter Schäfer
 */
/*

/* statement handler * /
typedef struct st_mysql_stmt
{
  MEM_ROOT       mem_root;             /* root allocations * /
  LIST           list;                 /* list to keep track of all stmts * /
  MYSQL          *mysql;               /* connection handle * /
  MYSQL_BIND     *params;              /* input parameters * /
  MYSQL_BIND     *bind;                /* output parameters * /
  MYSQL_FIELD    *fields;              /* result set metadata * /
  MYSQL_DATA     result;               /* cached result set * /
  MYSQL_ROWS     *data_cursor;         /* current row in cached result * /
  /* copy of mysql->affected_rows after statement execution * /
  my_ulonglong   affected_rows;
  my_ulonglong   insert_id;            /* copy of mysql->insert_id * /
  /*
    mysql_stmt_fetch() calls this function to fetch one row (it's different
    for buffered, unbuffered and cursor fetch).
  * /
  int            (*read_row_func)(struct st_mysql_stmt *stmt,
                                  unsigned char **row);
  unsigned long	 stmt_id;	       /* Id for prepared statement * /
  unsigned int	 last_errno;	       /* error code * /
  unsigned int   param_count;          /* input parameter count * /
  unsigned int   field_count;          /* number of columns in result set * /
  enum enum_mysql_stmt_state state;    /* statement state * /
  char		 last_error[MYSQL_ERRMSG_SIZE]; /* error message * /
  char		 sqlstate[SQLSTATE_LENGTH+1];
  /* Types of input parameters should be sent to server * /
  my_bool        send_types_to_server;
  my_bool        bind_param_done;      /* input buffers were supplied * /
  my_bool        bind_result_done;     /* output buffers were supplied * /
  /* mysql_stmt_close() had to cancel this result * /
  my_bool       unbuffered_fetch_cancelled;
  /*
    Is set to true if we need to calculate field->max_length for
    metadata fields when doing mysql_stmt_store_result.
  * /
  my_bool       update_max_length;
} MYSQL_STMT;
*/

public class stmt
{
	public static final int MYSQL_STMT_INIT_DONE = 1;
	public static final int MYSQL_STMT_PREPARE_DONE = 2;
	public static final int MYSQL_STMT_EXECUTE_DONE = 3;
	public static final int MYSQL_STMT_FETCH_DONE = 4;

    /** parameters to mysql_stmt_attr_set */
    public static final int STMT_ATTR_UPDATE_MAX_LENGTH = 0;
    public static final int STMT_ATTR_CURSOR_TYPE = 1;
    public static final int STMT_ATTR_PREFETCH_ROWS = 2;

/*

MYSQL_STMT * STDCALL mysql_stmt_init(MYSQL *mysql);
int STDCALL mysql_stmt_prepare(MYSQL_STMT *stmt, const char *query,
                               unsigned long length);
*/
public static native long mysql_stmt_init(long connectionHandle) throws SQLException;
public static native void mysql_stmt_prepare(long stmtHandle, String query) throws SQLException;

/*
int STDCALL mysql_stmt_execute(MYSQL_STMT *stmt);
int STDCALL mysql_stmt_fetch(MYSQL_STMT *stmt);
int STDCALL mysql_stmt_fetch_column(MYSQL_STMT *stmt, MYSQL_BIND *bind,
                                    unsigned int column,
                                    unsigned long offset);
*/
public static native void mysql_stmt_execute(long stmtHandle) throws SQLException;
public static native boolean mysql_stmt_fetch(long stmtHandle) throws SQLException;
public static native void mysql_stmt_fetch_column(long stmtHandle, long bindHandle,
                                        int column, int srcOffset, int bufferOffset) throws SQLException;


/*
int STDCALL mysql_stmt_store_result(MYSQL_STMT *stmt);
*/
public static native void mysql_stmt_store_result(long stmtHandle) throws SQLException;

/*
unsigned long STDCALL mysql_stmt_param_count(MYSQL_STMT * stmt);
*/
public static native int mysql_stmt_param_count(long stmtHandle);

/*
my_bool STDCALL mysql_stmt_attr_set(MYSQL_STMT *stmt,
                                    enum enum_stmt_attr_type attr_type,
                                    const void *attr);
my_bool STDCALL mysql_stmt_attr_get(MYSQL_STMT *stmt,
                                    enum enum_stmt_attr_type attr_type,
                                    void *attr);
*/
public static native void mysql_stmt_attr_set(long stmtHandle, int attr_type, boolean value) throws SQLException;
public static native void mysql_stmt_attr_set(long stmtHandle, int attr_type, int value) throws SQLException;

/*
my_bool STDCALL mysql_stmt_bind_param(MYSQL_STMT * stmt, MYSQL_BIND * bnd);
my_bool STDCALL mysql_stmt_bind_result(MYSQL_STMT * stmt, MYSQL_BIND * bnd);
*/
public static native void mysql_stmt_bind_param(long stmtHandle, long bindHandle) throws SQLException;
public static native void mysql_stmt_bind_result(long stmtHandle, long bindHandle) throws SQLException;

/*
my_bool STDCALL mysql_stmt_close(MYSQL_STMT * stmt);
my_bool STDCALL mysql_stmt_reset(MYSQL_STMT * stmt);
*/
public static native void mysql_stmt_close(long stmtHandle) throws SQLException;
public static native void mysql_stmt_reset(long stmtHandle) throws SQLException;

/*
my_bool STDCALL mysql_stmt_free_result(MYSQL_STMT *stmt);
*/
public static native void mysql_stmt_free_result(long stmtHandle) throws SQLException;

/*
my_bool STDCALL mysql_stmt_send_long_data(MYSQL_STMT *stmt,
                                          unsigned int param_number,
                                          const char *data,
                                          unsigned long length);
*/
public static native void mysql_stmt_send_long_data(long stmtHandle,
							int param_number, ByteBuffer buffer, int len) throws SQLException;

/*
MYSQL_RES *STDCALL mysql_stmt_result_metadata(MYSQL_STMT *stmt);
MYSQL_RES *STDCALL mysql_stmt_param_metadata(MYSQL_STMT *stmt);
*/
public static native long mysql_stmt_result_metadata(long stmtHandle) throws SQLException;
public static native long mysql_stmt_param_metadata(long stmtHandle) throws SQLException;

/*
unsigned int STDCALL mysql_stmt_errno(MYSQL_STMT * stmt);
const char *STDCALL mysql_stmt_error(MYSQL_STMT * stmt);
const char *STDCALL mysql_stmt_sqlstate(MYSQL_STMT * stmt);
*/
public static native int mysql_stmt_errno(long stmtHandle);
public static native String mysql_stmt_error(long stmtHandle);
public static native String mysql_stmt_sqlstate(long stmtHandle);

/*
MYSQL_ROW_OFFSET STDCALL mysql_stmt_row_seek(MYSQL_STMT *stmt,
                                             MYSQL_ROW_OFFSET offset);
MYSQL_ROW_OFFSET STDCALL mysql_stmt_row_tell(MYSQL_STMT *stmt);
*/
public static native long mysql_stmt_row_seek(long stmtHandle, int offset);
public static native long mysql_stmt_row_tell(long stmtHandle);

/*
void STDCALL mysql_stmt_data_seek(MYSQL_STMT *stmt, my_ulonglong offset);
*/
public static native void mysql_stmt_data_seek(long stmtHandle, long offset);

/*
my_ulonglong STDCALL mysql_stmt_num_rows(MYSQL_STMT *stmt);
my_ulonglong STDCALL mysql_stmt_affected_rows(MYSQL_STMT *stmt);
my_ulonglong STDCALL mysql_stmt_insert_id(MYSQL_STMT *stmt);
unsigned int STDCALL mysql_stmt_field_count(MYSQL_STMT *stmt);
*/
public static native long mysql_stmt_num_rows(long stmtHandle);
public static native long mysql_stmt_affected_rows(long stmtHandle);
public static native long mysql_stmt_insert_id(long stmtHandle);
public static native int mysql_stmt_field_count(long stmtHandle);


/**
 * set MYSQL_BIND fields
 */
public static native void set_bind(long bindHandle, int type, int length, ByteBuffer buffer);

public static native void set_byte(long bindHandle, ByteBuffer buffer, byte value);
public static native void set_short(long bindHandle, ByteBuffer buffer, short value);
public static native void set_int(long bindHandle, ByteBuffer buffer, int value);
public static native void set_long(long bindHandle, ByteBuffer buffer, long value);
public static native void set_float(long bindHandle, ByteBuffer buffer, float value);
public static native void set_double(long bindHandle, ByteBuffer buffer, double value);
public static native void set_bytes(long bindHandle, ByteBuffer buffer, int type, byte[] value, int len);
public static native void set_date(long bindHandle, ByteBuffer buffer, int type, short year, short month, short day, short hour, short minute, short second, short millis);

}
