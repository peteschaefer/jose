package de.jose.db;

import de.jose.Application;
import de.jose.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Random;
import java.util.Vector;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MariaDBAdapter extends MySQLAdapter
{
    @Override
    public Process startStandaloneServer(boolean printCommandLine) throws IOException {
        /*		setup the MariaDB server
        */

        /**	.../mariadbd
         *	--port=...
         *  --socket=...
         *  --datadir=...
         *  --basedir=...
         *  -u root
         */
        File mariadir = getDataDir();
        File tmpdir = new File(Application.theDatabaseDirectory, "tmp");

        Vector command = new Vector();
        Vector env = new Vector();
        String binPath = Application.theWorkingDirectory.getAbsolutePath()+File.separator+"bin";
        String execPath = binPath+File.separator+ Version.osDir+File.separator+"mariadbd";
//		String defaultsPath = Application.theWorkingDirectory.getAbsolutePath()+
//		                    File.separator+"config"+File.separator+"mysql.ini";

        command.add(execPath);
        //  more config parameters are read from my.ini
        //  groups: mysqld server mysqld-4.1
//		command.add("--defaults-file="+defaultsPath);

        //  most of the following are already defined in my.ini
        //  doesn't hurt to define them twice:
//        command.add("--skip-bdb");
//        command.add("--skip-innodb");
//        command.add("--skip-grant-tables");
//        command.add("--skip-name-resolve");
        command.add("--character-set-server=utf8mb3");
        command.add("--collation-server=utf8_general_ci");
        command.add("--console");	// do write to std-out

        //	MyISAM parameters; bump up default values to accomodate GIGA databases
        command.add("--key-buffer-size=64M");
        command.add("--max-allowed-packet=1M");
        command.add("--table-open-cache=64");
        command.add("--net-buffer-length=8K");
        command.add("--read-buffer-size=16M");
        command.add("--read-rnd-buffer_size=128M");
        command.add("--sort-buffer-size=512M");
        command.add("--bulk-insert-buffer-size=256M");	//	for import?
        command.add("--myisam-sort-buffer-size=256M");
        command.add("--myisam-recover-options=FORCE"); //  always check for corrupted index files, etc.
        //command.add("--myisam-use-mmap=ON");	//	since 5.1 !
        //	default table size for tmp and memory tables is 16MB. Not enough.
        //	huge database have around 3GB, or more.
        command.add("--tmp-table-size=16G");
        command.add("--max-heap-table-size=16G");
        //command.add("--default-time-zone='+00:00'"); does not work

        //  controls hash joins?


        //  big query optimisation
        // Turn on disk-ordered reads
        command.add("--optimizer-switch=mrr=on");
        command.add("--optimizer_switch=mrr_cost_based=off");
        // Turn on Batched Key Access (BKA)
        command.add("--join-cache-level=8");
        // Size limit for the whole join
        command.add("--join-buffer-space-limit=300M");
        // Limit for each individual table
        command.add("--join_buffer_size=100M");
        command.add("--optimizer_switch=index_merge_sort_intersection=on");


        //	for server-side operation: set connection timeout as high as possible:
        String infTimeout = Version.windows ? "2147483" : "31536000";
//		command.add("--wait-timeout=" + infTimeout);
//		command.add("--interactive-timeout=" + infTimeout);
        // don't rely on automatic timeout. use connection pool watchdog instead

        //	MySQL 8.0.x
//		command.add("--upgrade=NONE");	//	don't upgrad old MyISAM tables
//		command.add("--mysqld="+execPath);	//	used for "mysqld_safe"
//		command.add("--log-error=error.log");
//		command.add("--secure-file-priv="+Application.theDatabaseDirectory);

        props.put("--default-character-set","utf8mb3");
        props.put("--default-collation","utf8_general_ci");


        /** delayed key write is optional; makes sense when importing large files   */
        if (can("delayed_key_write"))
        {
            command.add("--delay-key-write=ALL");
            props.put("--delay_key_write","ALL");
            /** when delayed key writing is enabled,
             *  "myisam-recover" is especially important
             */
        }

        if (!Version.MYSQL_UDF) command.add("--skip-external-locking");
        //command.add("--skip-locking");

        // only connect to local host; skip DNS name resolve
//		if (Version.mysql40) {
//			command.add("--skip-thread-priority");
//			command.add("--console");   //  don't write error log
//			//	does this option improve response times ?
//		}

		/*  use exact lettercase for table names
			this is already the default for Linux but we have to eplicitly
			force it on OS X (which may or may not be case sensitive)
			on Windows, it doesn't matter anyway
			(it would matter if we used mixed casing, but we don't)
		*/
        command.add("--lower_case_table_names=1");
        command.add("--default-storage-engine=MYISAM");
        //  means: always use lower case, compare insensitive
        //  lower_case_table_names=0 would make even more sense, but not accepted on Windows.
        //  todo check with imported files

        if (Version.unix && Version.MYSQL_UDF) {
            //	set library path fo UDF
            String libPath = Application.theWorkingDirectory.getAbsolutePath()+
                            "/lib/"+Version.osDir;
            env.add("LD_LIBRARY_PATH="+libPath);
        }

        boolean tcpConnect = false;
        if (! Version.getSystemProperty("jose.pipe",true))
            tcpConnect = true;
        else if (Version.linuxIntel && (props.getProperty("socket-file")!=null)) {
            //	UNIX: use sockets
            //  note that Mac OS X is UNIX, too
            String socket = props.getProperty("socket-file");
            command.add("--socket="+socket.trim());
            //	if current user is root, we have to supply -u
            String userName = Version.getSystemProperty("user.name");
            if ("root".equals(userName)) {
                command.add("-u");
                command.add("root");
            }

            //	(UnixSocketFactory is currently only implemented for Linux/Intel platform
            //	 however, porting to other Unixes should be easy)
            command.add("--skip-networking");		//	disable TCP/IP for external connections

            props.put("socketFactory","de.jose.db.UnixSocketFactory");
            props.put("socketPath",socket);

            props.put("url", "jdbc:mariadb://./jose");
        }
        else if (Version.winNTfamily && (props.getProperty("pipe-name")!=null)) {
            //	Win NT: use named pipes
            String pipe = props.getProperty("pipe-name");

            //	params to mysqld
            command.add("--enable-named-pipe");
            command.add("--skip-networking");		//	disable TCP/IP
            command.add("--socket="+pipe.trim());
            //	params to JDBC driver
            props.put("socketFactory","com.mysql.jdbc.NamedPipeSocketFactory");
            props.put("namedPipePath","\\\\.\\pipe\\"+pipe);
            props.put("url", "jdbc:mariadb://./jose");
//			File pipefile = new File("\"\\\\\\\\.\\\\pipe\\\\\"+pipe");
//			System.out.println("pipe exists: "+pipefile.exists());
        }
        else
            tcpConnect = true;

        if (tcpConnect) {
            //	else: use TCP/IP. choose a random port
            //	avoid conflicting ports with other mySql servers
            //	note that an open TCP/IP port constitutes a security risk, unless there is a firewall
            String portno = (String)props.get("port-no");
            if (portno==null) {
                //  choose a random port number from the private range (i.e. 49152 through 65535)
                Random rnd = new Random();
                int pno = 49152 + Math.abs(rnd.nextInt()) % (65535-49152);
                portno = String.valueOf(pno);
                props.put("port-no",portno);
            }

            command.add("--port="+portno.trim());

            props.put("url", "jdbc:mariadb://localhost:"+portno+"/jose");
            System.err.println(props.get("url"));
        }

        //	set data directory
        command.add("--datadir");
        command.add(mariadir.getAbsolutePath());

        command.add("--tmpdir");
        command.add(tmpdir.getAbsolutePath());
        tmpdir.mkdirs();

        //	set base directory
        command.add("--basedir");
        command.add(binPath);

        return createServerProcess(command, env, printCommandLine);
    }

    public File getDataDir() {
        return new File(Application.theDatabaseDirectory, "mariadb" + File.separator + "data");
    }

    @Override
    protected boolean serverIsReady(String line) {
        return line.contains("ready for connections");
    }

    @Override
    public boolean askBootstrap(File datadir) {
        //  Crossover Mysql data directory to MariaDB directory.
        //  Simply move it to its new location :)
        Path josedir = new File(datadir, "jose").toPath();
        if (!Files.exists(josedir)) {
            //  look for an old MySQL directory - steal it :)
            Path mysqldir = new File (datadir.getParentFile().getParentFile(), "mysql"+File.separator+"jose").toPath();
            if (Files.exists(mysqldir))
                try {
                    Files.move(mysqldir,josedir,REPLACE_EXISTING);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
        }

        return super.askBootstrap(datadir);
    }

}
