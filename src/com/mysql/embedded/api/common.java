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

/**
 * common constants for the MySQL API, version 4.1.7
 *
 * @author Peter Schäfer
 */

public interface common
{

/*
** Common definition between mysql server & client
*/


	public static final int NAME_LEN	            = 64;		/* Field/table name length */
	public static final int HOSTNAME_LENGTH         = 60;
	public static final int USERNAME_LENGTH         = 16;
	public static final int SERVER_VERSION_LENGTH   = 60;

	public static final String LOCAL_HOST	        = "localhost";
	public static final String LOCAL_HOST_NAMEDPIPE = ".";

//	#if defined(__WIN__) && !defined( _CUSTOMCONFIG_)
	public static final String MYSQL_NAMEDPIPE      = "MySQL";
	public static final String MYSQL_SERVICENAME    = "MySql";
//	#endif /* __WIN__ */
/*
  Length of random string sent by server on handshake; this is also length of
  obfuscated password, recieved from client
*/
	public static final int SCRAMBLE_LENGTH     = 20;
	public static final int SCRAMBLE_LENGTH_323 = 8;
/* length of password stored in the db: new passwords are preceeded with '*' */
	public static final int SCRAMBLED_PASSWORD_CHAR_LENGTH = (SCRAMBLE_LENGTH*2+1);
	public static final int SCRAMBLED_PASSWORD_CHAR_LENGTH_323 = (SCRAMBLE_LENGTH_323*2);


	public static final int NOT_NULL_FLAG	    = 1;		/* Field can't be NULL */
	public static final int PRI_KEY_FLAG	    = 2;		/* Field is part of a primary key */
	public static final int UNIQUE_KEY_FLAG     = 4;		/* Field is part of a unique key */
	public static final int MULTIPLE_KEY_FLAG   = 8;		/* Field is part of a key */
	public static final int BLOB_FLAG	        = 16;		/* Field is a blob */
	public static final int UNSIGNED_FLAG	    = 32;		/* Field is unsigned */
	public static final int ZEROFILL_FLAG	    = 64;		/* Field is zerofill */
	public static final int BINARY_FLAG	        = 128;

/* The following are only sent to new clients */
	public static final int ENUM_FLAG	        = 256;		/* field is an enum */
	public static final int AUTO_INCREMENT_FLAG = 512;		/* field is a autoincrement field */
	public static final int TIMESTAMP_FLAG	    = 1024;		/* Field is a timestamp */
	public static final int SET_FLAG	        = 2048;		/* field is a set */
	public static final int NUM_FLAG	        = 32768;		/* Field is num (for clients) */
	public static final int PART_KEY_FLAG	    = 16384;		/* Intern; Part of some key */
	public static final int GROUP_FLAG	        = 32768;		/* Intern: Group field */
	public static final int UNIQUE_FLAG	        = 65536;		/* Intern: Used by sql_yacc */

	public static final int REFRESH_GRANT		= 1;	/* Refresh grant tables */
	public static final int REFRESH_LOG		    = 2;	/* Start on new log file */
	public static final int REFRESH_TABLES		= 4;	/* close all tables */
	public static final int REFRESH_HOSTS		= 8;	/* Flush host cache */
	public static final int REFRESH_STATUS		= 16;	/* Flush status variables */
	public static final int REFRESH_THREADS		= 32;	/* Flush thread cache */
	public static final int REFRESH_SLAVE       = 64;      /* Reset master info and restart slave thread */
	public static final int REFRESH_MASTER      = 128;     /* Remove all bin logs in the index and truncate the index */

/* The following can't be set with mysql_refresh() */
	public static final int REFRESH_READ_LOCK	= 16384;	/* Lock tables for read */
	public static final int REFRESH_FAST		= 32768;	/* Intern flag */

/* RESET (remove all queries) from query cache */
	public static final int REFRESH_QUERY_CACHE	        = 65536;
	public static final int REFRESH_QUERY_CACHE_FREE    = 0x20000; /* pack query cache */
	public static final int REFRESH_DES_KEY_FILE	    = 0x40000;
	public static final int REFRESH_USER_RESOURCES	    = 0x80000;

	public static final int CLIENT_LONG_PASSWORD	    = 1;	/* new more secure passwords */
	public static final int CLIENT_FOUND_ROWS	        = 2;	/* Found instead of affected rows */
	public static final int CLIENT_LONG_FLAG	        = 4;	/* Get all column flags */
	public static final int CLIENT_CONNECT_WITH_DB	    = 8;	/* One can specify db on connect */
	public static final int CLIENT_NO_SCHEMA	        = 16;	/* Don't allow database.table.column */
	public static final int CLIENT_COMPRESS		        = 32;	/* Can use compression protocol */
	public static final int CLIENT_ODBC		            = 64;	/* Odbc client */
	public static final int CLIENT_LOCAL_FILES	        = 128;	/* Can use LOAD DATA LOCAL */
	public static final int CLIENT_IGNORE_SPACE	        = 256;	/* Ignore spaces before '(' */
	public static final int CLIENT_PROTOCOL_41	        = 512;	/* New 4.1 protocol */
	public static final int CLIENT_INTERACTIVE	        = 1024;	/* This is an interactive client */
	public static final int CLIENT_SSL                  = 2048;     /* Switch to SSL after handshake */
	public static final int CLIENT_IGNORE_SIGPIPE       = 4096;     /* IGNORE sigpipes */
	public static final int CLIENT_TRANSACTIONS	        = 8192;	/* Client knows about transactions */
	public static final int CLIENT_RESERVED             = 16384;   /* Old flag for 4.1 protocol  */
	public static final int CLIENT_SECURE_CONNECTION    = 32768;  /* New 4.1 authentication */
	public static final int CLIENT_MULTI_STATEMENTS     = 65536;   /* Enable/disable multi-stmt support */
	public static final int CLIENT_MULTI_RESULTS        = 131072;  /* Enable/disable multi-results */
	public static final int CLIENT_REMEMBER_OPTIONS	    = 1 << 31;

	public static final int SERVER_STATUS_IN_TRANS     = 1;	/* Transaction has started */
	public static final int SERVER_STATUS_AUTOCOMMIT   = 2;	/* Server in auto_commit mode */
	public static final int SERVER_STATUS_MORE_RESULTS = 4;	/* More results on server */
	public static final int SERVER_MORE_RESULTS_EXISTS = 8;    /* Multi query - next query exists */
	public static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
	public static final int SERVER_QUERY_NO_INDEX_USED      = 32;

	public static final int MYSQL_ERRMSG_SIZE	    = 200;
	public static final int NET_READ_TIMEOUT	    = 30;		/* Timeout on read */
	public static final int NET_WRITE_TIMEOUT	    = 60;		/* Timeout on write */
	public static final int NET_WAIT_TIMEOUT	    = 8*60*60;		/* Wait for new query */

//	struct st_vio;					/* Only C */
//	typedef struct st_vio Vio;

	public static final int MAX_TINYINT_WIDTH      = 3;       /* Max width for a TINY w.o. sign */
	public static final int MAX_SMALLINT_WIDTH     = 5;       /* Max width for a SHORT w.o. sign */
	public static final int MAX_MEDIUMINT_WIDTH    = 8;       /* Max width for a INT24 w.o. sign */
	public static final int MAX_INT_WIDTH          = 10;      /* Max width for a LONG w.o. sign */
	public static final int MAX_BIGINT_WIDTH       = 20;      /* Max width for a LONGLONG */
	public static final int MAX_CHAR_WIDTH		   = 255;	/* Max length for a CHAR colum */
	public static final int MAX_BLOB_WIDTH		   = 8192;	/* Default width for blob */
/*
	typedef struct st_net {
	#if !defined(CHECK_EMBEDDED_DIFFERENCES) || !defined(EMBEDDED_LIBRARY)
	  Vio* vio;
	  unsigned char *buff,*buff_end,*write_pos,*read_pos;
	  my_socket fd;					/* For Perl DBI/dbd * /
	  unsigned long max_packet,max_packet_size;
	  unsigned int pkt_nr,compress_pkt_nr;
	  unsigned int write_timeout, read_timeout, retry_count;
	  int fcntl;
	  my_bool compress;
	  /*
	    The following variable is set if we are doing several queries in one
	    command ( as in LOAD TABLE ... FROM MASTER ),
	    and do not want to confuse the client with OK at the wrong time
	  * /
	  unsigned long remain_in_buf,length, buf_length, where_b;
	  unsigned int *return_status;
	  unsigned char reading_or_writing;
	  char save_char;
	  my_bool no_send_ok;
	  /*
	    Pointer to query object in query cache, do not equal NULL (0) for
	    queries in cache that have not stored its results yet
	  * /
	#endif
	  char last_error[MYSQL_ERRMSG_SIZE], sqlstate[SQLSTATE_LENGTH+1];
	  unsigned int last_errno;
	  unsigned char error;
	  gptr query_cache_query;
	  my_bool report_error; /* We should report error (we have unreported error) * /
	  my_bool return_errno;
	} NET;
*/
	public static final int packet_error = ~0;

/* Shutdown/kill enums and constants */

/* Bits for THD::killable. */
	public static final byte MYSQL_SHUTDOWN_KILLABLE_CONNECT    = (byte)(1 << 0);
	public static final byte MYSQL_SHUTDOWN_KILLABLE_TRANS      = (byte)(1 << 1);
	public static final byte MYSQL_SHUTDOWN_KILLABLE_LOCK_TABLE = (byte)(1 << 2);
	public static final byte MYSQL_SHUTDOWN_KILLABLE_UPDATE     = (byte)(1 << 3);


/* options for mysql_set_option * /
	enum enum_mysql_set_option
	{
	  MYSQL_OPTION_MULTI_STATEMENTS_ON,
	  MYSQL_OPTION_MULTI_STATEMENTS_OFF
	};
*/
//	#define net_new_transaction(net) ((net)->pkt_nr=0)

/*
	my_bool	my_net_init(NET *net, Vio* vio);
	void	my_net_local_init(NET *net);
	void	net_end(NET *net);
	void	net_clear(NET *net);
	my_bool net_realloc(NET *net, unsigned long length);

	#ifndef EMBEDDED_LIBRARY /* To be removed by HF * /
	my_bool	net_flush(NET *net);
	#else
	#define net_flush(A)
	#endif

	my_bool	my_net_write(NET *net,const char *packet,unsigned long len);
	my_bool	net_write_command(NET *net,unsigned char command,
				  const char *header, unsigned long head_len,
				  const char *packet, unsigned long len);
	int	net_real_write(NET *net,const char *packet,unsigned long len);
	unsigned long my_net_read(NET *net);
*/

	  /* The following is for user defined functions */
/*
	enum Item_result {STRING_RESULT, REAL_RESULT, INT_RESULT, ROW_RESULT};

	typedef struct st_udf_args
	{
	  unsigned int arg_count;		/* Number of arguments * /
	  enum Item_result *arg_type;		/* Pointer to item_results * /
	  char **args;				/* Pointer to argument * /
	  unsigned long *lengths;		/* Length of string arguments * /
	  char *maybe_null;			/* Set to 1 for all maybe_null args * /
	} UDF_ARGS;
*/
	  /* This holds information about the result */
/*
	typedef struct st_udf_init
	{
	  my_bool maybe_null;			/* 1 if function can return NULL * /
	  unsigned int decimals;		/* for real functions * /
	  unsigned long max_length;		/* For string functions * /
	  char	  *ptr;				/* free pointer for function data * /
	  my_bool const_item;			/* 0 if result is independent of arguments * /
	} UDF_INIT;
*/
	  /* Constants when using compression */
	public static final int NET_HEADER_SIZE = 4;		/* standard header size */
	public static final int COMP_HEADER_SIZE = 3;		/* compression header extra size */

	  /* Prototypes to password functions */

/*
  These functions are used for authentication by client and server and
  implemented in sql/password.c

	void randominit(struct rand_struct *, unsigned long seed1,
	                unsigned long seed2);
	double my_rnd(struct rand_struct *);
	void create_random_string(char *to, unsigned int length, struct rand_struct *rand_st);

	void hash_password(unsigned long *to, const char *password, unsigned int password_len);
	void make_scrambled_password_323(char *to, const char *password);
	void scramble_323(char *to, const char *message, const char *password);
	my_bool check_scramble_323(const char *, const char *message,
	                           unsigned long *salt);
	void get_salt_from_password_323(unsigned long *res, const char *password);
	void make_password_from_salt_323(char *to, const unsigned long *salt);

	void make_scrambled_password(char *to, const char *password);
	void scramble(char *to, const char *message, const char *password);
	my_bool check_scramble(const char *reply, const char *message,
	                       const unsigned char *hash_stage2);
	void get_salt_from_password(unsigned char *res, const char *password);
	void make_password_from_salt(char *to, const unsigned char *hash_stage2);

/* end of password.c * /

	char *get_tty_password(char *opt_message);
	const char *mysql_errno_to_sqlstate(unsigned int mysql_errno);

/* Some other useful functions * /

	my_bool my_init(void);
	int load_defaults(const char *conf_file, const char **groups,
			  int *argc, char ***argv);
	my_bool my_thread_init(void);
	void my_thread_end(void);

	#ifdef _global_h
	ulong STDCALL net_field_length(uchar **packet);
	my_ulonglong net_field_length_ll(uchar **packet);
	char *net_store_length(char *pkg, ulonglong length);
	#endif
*/


	public static final int  NULL_LENGTH = ~0; /* For net_store_length */
	public static final int MYSQL_STMT_HEADER       = 4;
	public static final int MYSQL_LONG_DATA_HEADER  = 6;

}
