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
import java.sql.SQLException;

/**
 * JNI wrappers for the MySQL C API, version 4.1.7
 *
 * see http://dev.mysql.com/doc/mysql/en/C.html
 *
 * @author Peter Schäfer
 */

public class api
{



/*
  Set up and bring down the server; to ensure that applications will
  work when linked against either the standard client library or the
  embedded server library, these functions should be called.

	long STDCALL mysql_server_init(int argc, char **argv, char **groups);
	void STDCALL mysql_server_end(void);
*/
public static native void mysql_server_init(String[] argv, String[] groups) throws SQLException;
public static native void mysql_server_end();
/*
	MYSQL_PARAMETERS *STDCALL mysql_get_parameters(void);
*/
public static native long mysql_get_parameters();

/*
  Set up and bring down a thread; these function should be called
  for each thread in an application which opens at least one MySQL
  connection.  All uses of the connection(s) should be between these
  function calls.

	my_bool STDCALL my_thread_init(void);
	void STDCALL my_thread_end(void);

*/
public static native void my_thread_init() throws SQLException;
public static native void my_thread_end();


/*
  Functions to get information from the MYSQL and MYSQL_RES structures
  Should definitely be used if one uses shared libraries.

	my_ulonglong STDCALL mysql_num_rows(MYSQL_RES *res);
	unsigned int STDCALL mysql_num_fields(MYSQL_RES *res);
*/
public static native long mysql_num_rows(long resultHandle);
public static native int mysql_num_fields(long resultHandle);

/*
	my_bool STDCALL mysql_eof(MYSQL_RES *res);
*/
public static native boolean mysql_eof(long resultHandle);

/*
	MYSQL_FIELD *STDCALL mysql_fetch_field_direct(MYSQL_RES *res, unsigned int fieldnr);
	MYSQL_FIELD * STDCALL mysql_fetch_fields(MYSQL_RES *res);
*/

public static native ByteBuffer mysql_fetch_field_direct(long resultHandle, int fieldnr);
public static native ByteBuffer mysql_fetch_fields(long resultHandle);


/*
	MYSQL_ROW_OFFSET STDCALL mysql_row_tell(MYSQL_RES *res);
	MYSQL_FIELD_OFFSET STDCALL mysql_field_tell(MYSQL_RES *res);
*/
public static native long mysql_row_tell(long resultHandle);
public static native int mysql_field_tell(long resultHandle);

/*
	unsigned int STDCALL mysql_field_count(MYSQL *mysql);
	my_ulonglong STDCALL mysql_affected_rows(MYSQL *mysql);
	my_ulonglong STDCALL mysql_insert_id(MYSQL *mysql);
*/
public static native int mysql_field_count(long mysqlHandle);
public static native long mysql_affected_rows(long mysqlHandle);
public static native long mysql_insert_id(long mysqlHandle);

/*
	unsigned int STDCALL mysql_errno(MYSQL *mysql);
	const char * STDCALL mysql_error(MYSQL *mysql);
	const char *STDCALL mysql_sqlstate(MYSQL *mysql);
	unsigned int STDCALL mysql_warning_count(MYSQL *mysql);
	const char * STDCALL mysql_info(MYSQL *mysql);
*/
public static native int mysql_errno(long mysqlHandle);
public static native String mysql_error(long mysqlHandle);
public static native String mysql_sqlstate(long mysqlHandle);
public static native int mysql_warning_count(long mysqlHandle);
public static native String mysql_info(long mysqlHandle);

/*
	unsigned long STDCALL mysql_thread_id(MYSQL *mysql);
	const char * STDCALL mysql_character_set_name(MYSQL *mysql);
*/
public static native int mysql_thread_id(long mysqlHandle);
public static native String mysql_character_set_name(long mysqlHandle);

/*
	MYSQL *		STDCALL mysql_init(MYSQL *mysql);
*/
public static native long mysql_init(long mysqlHandle);

/*
	int		STDCALL mysql_ssl_set(MYSQL *mysql, const char *key,
					      const char *cert, const char *ca,
					      const char *capath, const char *cipher);
*/
public static native int mysql_ssl_set(long mysqlHandle, String key,
					  String cert, String ca,
					  String capath, String cipher);

/*
	my_bool		STDCALL mysql_change_user(MYSQL *mysql, const char *user,
						  const char *passwd, const char *db);
*/
public static native void mysql_change_user(long mysqlHandle, String user,
					  String passwd, String db) throws SQLException;


/*
	MYSQL *		STDCALL mysql_real_connect(MYSQL *mysql, const char *host,
						   const char *user,
						   const char *passwd,
						   const char *db,
						   unsigned int port,
						   const char *unix_socket,
						   unsigned int clientflag);
	void		STDCALL mysql_close(MYSQL *sock);
*/
public static native long mysql_real_connect(long mysqlHandle, String host,
					   String user,
					   String passwd,
					   String db,
					   int port,
					   String unix_socket,
					   int clientflag) throws SQLException;
public static native void mysql_close(long mysqlHandle);

public static native void kill_connection(long mysqlHandle);
public static native void kill_query(long mysqlHandle);


/*
	int		STDCALL mysql_select_db(MYSQL *mysql, const char *db);
	int		STDCALL mysql_query(MYSQL *mysql, const char *q);
*/
public static native void mysql_select_db(long mysqlHandle, String db) throws SQLException;
public static native void mysql_query(long mysqlHandle, String q) throws SQLException;

/*
	int		STDCALL mysql_send_query(MYSQL *mysql, const char *q,
						 unsigned long length);
*/
public static native void mysql_send_query(long mysqlHandle, String q) throws SQLException;

/*
	int		STDCALL mysql_read_query_result(MYSQL *mysql);
	int		STDCALL mysql_real_query(MYSQL *mysql, const char *q,
						unsigned long length);
*/
public static native int mysql_read_query_result(long mysqlHandle);
public static native void mysql_real_query(long mysqlHandle, String q) throws SQLException;

/* perform query on master
	int		STDCALL mysql_master_query(MYSQL *mysql, const char *q,
						unsigned long length);
	int		STDCALL mysql_master_send_query(MYSQL *mysql, const char *q,
						unsigned long length);

	replication is not supported
*/
/* perform query on slave
	int		STDCALL mysql_slave_query(MYSQL *mysql, const char *q,
						unsigned long length);
	int		STDCALL mysql_slave_send_query(MYSQL *mysql, const char *q,
						unsigned long length);

	replication is not supported
*/

/*
  enable/disable parsing of all queries to decide if they go on master or
  slave

	void            STDCALL mysql_enable_rpl_parse(MYSQL* mysql);
	void            STDCALL mysql_disable_rpl_parse(MYSQL* mysql);

 get the value of the parse flag
	int             STDCALL mysql_rpl_parse_enabled(MYSQL* mysql);

  enable/disable reads from master
	void            STDCALL mysql_enable_reads_from_master(MYSQL* mysql);
	void            STDCALL mysql_disable_reads_from_master(MYSQL* mysql);
 get the value of the master read flag
	int             STDCALL mysql_reads_from_master_enabled(MYSQL* mysql);

	enum mysql_rpl_type     STDCALL mysql_rpl_query_type(const char* q, int len);
*/

/* discover the master and its slaves
	int             STDCALL mysql_rpl_probe(MYSQL* mysql);

 set the master, close/free the old one, if it is not a pivot
	int             STDCALL mysql_set_master(MYSQL* mysql, const char* host,
						 unsigned int port,
						 const char* user,
						 const char* passwd);
	int             STDCALL mysql_add_slave(MYSQL* mysql, const char* host,
						unsigned int port,
						const char* user,
						const char* passwd);
*/

/*
void mysql_set_local_infile_handler(MYSQL *mysql,
                               int (*local_infile_init)(void **, const char *,
                            void *),
                               int (*local_infile_read)(void *, char *,
							unsigned int),
                               void (*local_infile_end)(void *),
                               int (*local_infile_error)(void *, char*,
							 unsigned int),
                               void *);
*/
//public static native void mysql_set_local_infile_handler(int mysqlHandler,
//                                                         ILocalInfileHandler handler);

/*
void mysql_set_local_infile_default(MYSQL *mysql);
*/
public static native void mysql_set_local_infile_default(long mysqlHandle);


/*
	int		STDCALL mysql_shutdown(MYSQL *mysql);
	int		STDCALL mysql_dump_debug_info(MYSQL *mysql);
*/
public static native void mysql_shutdown(long mysqlHandle, int shutdownlevel) throws SQLException;
public static native int mysql_dump_debug_info(long mysqlHandle);

/*	int		STDCALL mysql_refresh(MYSQL *mysql,
					     unsigned int refresh_options);
	int		STDCALL mysql_kill(MYSQL *mysql,unsigned long pid);
	int		STDCALL mysql_ping(MYSQL *mysql);
*/
public static native int mysql_refresh(long mysqlHandle, int refresh_options);
public static native int mysql_kill(long mysqlHandle, int pid);
public static native int mysql_ping(long mysqlHandle);

/*
	const char *		STDCALL mysql_stat(MYSQL *mysql);
	const char *		STDCALL mysql_get_server_info(MYSQL *mysql);
	const char *		STDCALL mysql_get_client_info(void);
	unsigned long	STDCALL mysql_get_client_version(void);
	unsigned long	STDCALL mysql_get_server_version(MYSQL *mysql);
	const char *		STDCALL mysql_get_host_info(MYSQL *mysql);
*/
public static native String	mysql_stat(long mysqlHandle);
public static native String	mysql_get_server_info(long mysqlHandle);
public static native String	mysql_get_client_info();
public static native int mysql_get_client_version();
public static native int mysql_get_server_version(long mysqlHandle);
public static native String	mysql_get_host_info(long mysqlHandle);

/*
	int		STDCALL mysql_set_server_option(MYSQL *mysql,
						enum enum_mysql_set_option
						option);
	unsigned int	STDCALL mysql_get_proto_info(MYSQL *mysql);
*/
public static native int mysql_set_server_option(long mysqlHandle, int option);
public static native int mysql_get_proto_info(long mysqlHandle);

/*
	MYSQL_RES *	STDCALL mysql_list_dbs(MYSQL *mysql,const char *wild);
	MYSQL_RES *	STDCALL mysql_list_tables(MYSQL *mysql,const char *wild);
	MYSQL_RES *	STDCALL mysql_list_fields(MYSQL *mysql, const char *table,
						 const char *wild);
	MYSQL_RES *	STDCALL mysql_list_processes(MYSQL *mysql);
*/
public static native long mysql_list_dbs(long mysqlHandle, String wild) throws SQLException;
public static native long mysql_list_tables(long mysqlHandle, String wild) throws SQLException;
public static native long mysql_list_fields(long mysqlHandle, String table, String wild) throws SQLException;
public static native long mysql_list_processes(long mysqlHandle) throws SQLException;

/*
	MYSQL_RES *	STDCALL mysql_store_result(MYSQL *mysql);
	MYSQL_RES *	STDCALL mysql_use_result(MYSQL *mysql);
*/
public static native long mysql_store_result(long mysqlHandle) throws SQLException;
public static native long mysql_use_result(long mysqlHandle) throws SQLException;

/*
	int		STDCALL mysql_options(MYSQL *mysql,enum mysql_option option,
					      const char *arg);
*/
public static native void mysql_options(long mysqlHandle, int option, String arg) throws SQLException;

/*
	void		STDCALL mysql_free_result(MYSQL_RES *result);
*/
public static native void mysql_free_result(long resultHandle);

/*
	void		STDCALL mysql_data_seek(MYSQL_RES *result,
						my_ulonglong offset);
	MYSQL_ROW_OFFSET STDCALL mysql_row_seek(MYSQL_RES *result,
							MYSQL_ROW_OFFSET offset);
	MYSQL_FIELD_OFFSET STDCALL mysql_field_seek(MYSQL_RES *result,
						   MYSQL_FIELD_OFFSET offset);
*/
public static native void mysql_data_seek(long resultHandle, long offset);
public static native long mysql_row_seek(long resultHandle, int offset);
public static native long mysql_field_seek(long resultHandle, int offset);

/*
	MYSQL_ROW	STDCALL mysql_fetch_row(MYSQL_RES *result);
	@returns NULL indicates exhausted reuslt, or error condition
		note that no Exception is thrown
*/
public static native long mysql_fetch_row(long resultHandle);

/*
	unsigned long * STDCALL mysql_fetch_lengths(MYSQL_RES *result);
	MYSQL_FIELD *	STDCALL mysql_fetch_field(MYSQL_RES *result);
*/
public static native int[] mysql_fetch_lengths(long resultHandle) throws SQLException;
public static native int[] mysql_fetch_lengths(long resultHandle, int[] lengths, int count) throws SQLException;
public static native ByteBuffer	mysql_fetch_field(long resultHandle);

/*
	unsigned long	STDCALL mysql_escape_string(char *to,const char *from,
						    unsigned long from_length);
	unsigned long STDCALL mysql_real_escape_string(MYSQL *mysql,
						       char *to,const char *from,
						       unsigned long length);
*/
public static native String mysql_escape_string(String from);
public static native String mysql_real_escape_string(long mysqlHandle, String from);

/*
	void		STDCALL mysql_debug(const char *debug);
*/
public static native void mysql_debug(String debug);

/*
	char *		STDCALL mysql_odbc_escape_string(MYSQL *mysql,
							 char *to,
							 unsigned long to_length,
							 const char *from,
							 unsigned long from_length,
							 void *param,
							 char *
							 (*extend_buffer)
							 (void *, char *to,
							  unsigned long *length));
	void 		STDCALL myodbc_remove_escape(MYSQL *mysql,char *name);
*/
public static native String	mysql_odbc_escape_string(long mysqlHandle,
						 String from,
						 long param);
public static native String	myodbc_remove_escape(long mysqlHandle, String name);

/*
	unsigned int	STDCALL mysql_thread_safe(void);
	my_bool		STDCALL mysql_embedded(void);
*/
public static native int mysql_thread_safe();
public static native boolean mysql_embedded();

/*
	MYSQL_MANAGER*  STDCALL mysql_manager_init(MYSQL_MANAGER* con);
	MYSQL_MANAGER*  STDCALL mysql_manager_connect(MYSQL_MANAGER* con,
						      const char* host,
						      const char* user,
						      const char* passwd,
						      unsigned int port);
	void            STDCALL mysql_manager_close(MYSQL_MANAGER* con);
	int             STDCALL mysql_manager_command(MYSQL_MANAGER* con,
							const char* cmd, int cmd_len);
	int             STDCALL mysql_manager_fetch_line(MYSQL_MANAGER* con,
							  char* res_buf,
							 int res_buf_size);
*/
public static native long  mysql_manager_init(long connectionHandle);
public static native long  mysql_manager_connect(long connectionHandle,
	                          String host,
	                          String user,
	                          String passwd,
	                          int port);
public static native void mysql_manager_close(long connectionHandle);
public static native int mysql_manager_command(long connectionHandle, String cmd);
public static native int mysql_manager_fetch_line(long connectionHandle, StringBuffer res_buf);

/*
	#define mysql_reload(mysql) mysql_refresh((mysql),REFRESH_GRANT)
*/
public static void mysql_reload(long mysqlHandle) { mysql_refresh(mysqlHandle,common.REFRESH_GRANT); }

/*
	#ifdef USE_OLD_FUNCTIONS
	MYSQL *		STDCALL mysql_connect(MYSQL *mysql, const char *host,
					      const char *user, const char *passwd);
	int		STDCALL mysql_create_db(MYSQL *mysql, const char *DB);
	int		STDCALL mysql_drop_db(MYSQL *mysql, const char *DB);
	#define	 mysql_reload(mysql) mysql_refresh((mysql),REFRESH_GRANT)
	#endif
	#define HAVE_MYSQL_REAL_CONNECT

*/
/*
	my_bool STDCALL mysql_commit(MYSQL * mysql);
	my_bool STDCALL mysql_rollback(MYSQL * mysql);
	my_bool STDCALL mysql_autocommit(MYSQL * mysql, my_bool auto_mode);
	my_bool STDCALL mysql_more_results(MYSQL *mysql);
	int STDCALL mysql_next_result(MYSQL *mysql);
*/
public static native void mysql_commit(long mysqlHandle) throws SQLException;
public static native void mysql_rollback(long mysqlHandle) throws SQLException;
public static native void mysql_autocommit(long mysqlHandle, boolean auto_mode) throws SQLException;
public static native boolean mysql_more_results(long mysqlHandle);
public static native boolean mysql_next_result(long mysqlHandle) throws SQLException;

}
