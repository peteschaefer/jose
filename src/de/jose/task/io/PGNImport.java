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

package de.jose.task.io;

import de.jose.Application;
import de.jose.Language;
import de.jose.Version;
import de.jose.db.DBAdapter;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.Setup;
import de.jose.pgn.*;
import de.jose.task.DBTask;
import de.jose.util.StringUtil;
import de.jose.util.file.FileUtil;
import de.jose.util.file.XBufferedReader;
import de.jose.util.file.XStringBuffer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * Task that reads PGN data from a stream
 * and insert writes it into the database
 *
 * Reading and Writing is done in two "parallel" threads, coupled by a buffer.
 *
 * The benefit of parellel threads will be greatest, if one of the threads is blocked.
 * E.g. if the writer thread waits for the database server, or if the reader thread waits for
 * a network download.
 *
 * If the database server is embbeded (in the very same JVM) there won't be much perfomance improvement.
 *
 * @author Peter Schäfer
 */
public class PGNImport
		extends DBTask
{
	/**	run in asynch mode (separate thread for writer)
	 * 	makes sense if the DB server is running on a different machine
	 * 	makes little sense with en embedded DB
	 */
//	public static final boolean ASYNCH = Version.getSystemProperty("jose.asynch.import",false);

	/**	associated Collection	 */
	protected int CId;
	/**	input reader	 */
	protected XBufferedReader in;
	/**	input line buffer	 */
	protected XStringBuffer lineBuffer;
	/**	GameBuffer object	 */
	protected GameBuffer gm;
	/**	set if end of input stream is reached	 */
	protected boolean eof;
	/**	total number of chars (approximate)	 */
	protected long total;
	/**	number of rows	 */
	protected int rowCount;
	/**	writer thread	 */
	protected Writer writer;
	/**	key and value indexes; returned by GameBuffer.parseTag()	*/
	protected int[] key = new int[2];
	protected int[] value = new int[2];
	/**	tag left over from preivous game ? */
	protected boolean leftover;
	/** ader table keys disabled during import ? */
	protected boolean disableKeys;
	protected boolean canDisableKeys = true;

	/**	time for DB updates (for debugging)	 */
	long dbTime = 0;
	boolean broadcast = false;

	/** preserve line breaks inside comment text ?  */
	public boolean preserveLineBreaks = false;

	/**	instance of this task	 */
	protected int instance;

	/**	counts running instance of this class	 */
	public static int gGameImporterInstance = 0;

	protected static final int DISABLE_KEY_FILE_SIZE        = 10*1024*1024;
	protected static final int DISABLE_KEY_LIMIT            = 50000;
	protected static final double DISABLE_KEY_PERCENTAGE    = 0.75;

	/** if more than ... games were inserted, we might need to call ANALYZE TABLE */
	protected static final int ANALYZE_LIMIT                = 5000;

	protected static PGNFileFilter pgnFilter = PGNFileFilter.newPGNFilter();
	protected static PGNFileFilter epdFilter = PGNFileFilter.newEPDFilter();

	/**
	 * Please note that we use de.jose.util.file.XBufferedReader and de.jose.util.file.XStringBuffer
	 * for two reasons
	 *
	 *	(1)		XBufferedReader has a readLine() methods that works on StringBuffers
	 *			rather than Strings - avoiding unnecessary object allocations
	 *	(2)		XStringBuffer has a public getValue() method that returns the actual
	 *			underlying char[] - avoiding unnecessary array copies
	 *
	 * apart from that, these classes are identical to the original java.lang classes
	 */

	/**	size of input BufferedReader	 */
	protected static final int READ_BUFFER_SIZE = 32768;
	/**	size of Game (Row) Buffer	 */
	protected static final int GAME_BUFFER_SIZE = 120;

	public int getCollectionId()	{ return CId; }

	/**
	 * responsible for ASYNCH updates
	 */
	class Writer extends Thread
	{
		SQLException error;
		boolean sleeping = false;
		boolean finished = false;

		public void run()
		{
            try {
//              lockTables();	//	supposed to improve performance, but doesn't
                for(;;)
                {
                    if (eof) {
                        if (!gm.isEmpty())
                            gm.update(PGNImport.this);	//	after clearing the buffer, reader is signaled
                        finished = true;
                        PGNImport.this.interrupt(); //  signal reader that we are finished
                        return;
                    }

                    if (gm.isFull()) {
                        gm.update(PGNImport.this);	//	after clearing the buffer, reader is signaled
                    } else try {
                        //	wait for reader to fill the buffer
                        PGNImport.this.interrupt();
                        //System.out.println("writer wait "+(System.currentTimeMillis()-startTime));

                        sleeping = true;
                        sleep(5000);
                        sleeping = false;
                    }  catch (InterruptedException iex) {
                        //	signal from reader that buffer is full
                        sleeping = false;
                    }
                }
            } catch (SQLException sqlex) {
                Application.error(sqlex);
                error = sqlex;
                finished = true;
                return;
            } finally {
//              try { unlockTables(); } catch (SQLException ex) { /* can't help it */ }
            }
		}

        /**
         * locking tables before buld inserts promises better performance
         */
        protected void lockTables() throws SQLException
        {
            connection.executeUpdate(
                    "LOCK TABLES" +
                    " Game WRITE, MoreGame WRITE," +
                    " Player WRITE, Event WRITE, Site WRITE, Opening WRITE");
        }

        protected void unlockTables() throws SQLException
        {
            connection.executeUpdate("UNLOCK TABLES");
        }

		/**
		 * be careful NOT to call interrupt() while the thread is actually running.
		 * Cloudscape does not like this at all.
		 * Therfore we only call interrupt when it is really necessary.
		 */
		public boolean wakeUp() {
			if (sleeping)
				interrupt();	//	will raise an InterruptedException and resume execution
			return sleeping;
		}
	}

	protected PGNImport(String taskName, String fileName, String url,
	                    Reader input, long length)
		throws Exception
	{
		super(taskName+"."+gGameImporterInstance,true);

		broadcastOnUpdate(getName());
		broadcast = true;
		/*	database will be updated; close open results to avoid deadlocks !	*/

		instance = gGameImporterInstance++;
		fileName = de.jose.pgn.Collection.makeUniqueName(0,fileName,getConnection());
		de.jose.pgn.Collection collection = de.jose.pgn.Collection.newFileCollection(0, fileName,url, connection);
		collection.setParent(null);	//	create on root level

		CId = collection.Id;
		collection.insert(connection);

		initReader(input,length,null);

		connection.setAutoCommit(false);

		HashMap pmap = new HashMap();
		pmap.put("fileName",fileName);

		setSilentTime(0);
		setProgressText(StringUtil.replace(Language.get("dialog.read-progress.text"),pmap));
		setProgressTitle(Language.get("dialog.read-progress.title"));
	}

	/** constructor for single game parser */
	public PGNImport(Reader input, long length) throws Exception
	{
		super(PGNImport.class.getName(),null);
		initReader(input,length,null);
	}

	public PGNImport(Reader input, long length, GameBuffer buffer) throws Exception
	{
		super(PGNImport.class.getName(),null);
		initReader(input,length,buffer);
	}

	private void initReader(Reader input, long length, GameBuffer buffer) throws Exception
	{
		in = new XBufferedReader(input, READ_BUFFER_SIZE);
		//  when closing the buffered reader, do not close the underlying stream !!
		total = length;
		lineBuffer = new XStringBuffer(80);
		rowCount = 0;
		if (buffer==null)
			gm = new GameBuffer(connection, CId, 1, GAME_BUFFER_SIZE);
		else
			gm = buffer;
		eof = false;
	}

	public GameBuffer getGameBuffer()   { return gm; }

	public static PGNImport newImporter(String fileName, String url, Reader input, long length) throws Exception
	{
		if (pgnFilter.accept(null,fileName))
			return new PGNImport("PGNImport", FileUtil.trimExtension(fileName) ,url, input,length);
		else if (epdFilter.accept(null,fileName))
			return new EPDImport("EPDImport", FileUtil.trimExtension(fileName), url, input,length);
		else {
            //  throw new IllegalArgumentException("unknown file type "+fileName);
            return null;
        }
	}


	public static PGNImport openFile(File file)
		throws Exception
	{
		return openFile(file,0L);
	}

	public static PGNImport openFile(File file, long silentTime)
		throws Exception
	{
		String name = file.getName();
		String trimmedName = FileUtil.trimExtension(name);
		PGNImport reader = null;

		//  TODO streamline this !
		if (pgnFilter.accept(null,name) || epdFilter.accept(null,name))
        {
			//  plain PGN
		    FileInputStream fin = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(fin,4096);

			Reader fr = new InputStreamReader(bin, "ISO-8859-1");
			/**	PGN uses ISO-8859-1 by definition
			 *	can we rely on that ?
			 */
			reader = newImporter(file.getName(),
			                "file://"+file.getCanonicalPath(),
			                fr, file.length());
            if (reader==null) throw new IOException("Unknown File Type "+file.getName());

			reader.setSilentTime(silentTime);
			reader.start();
		}
		else if (FileUtil.hasExtension(name,"zip"))
        {
			//  ZIP
			ZipEnumeration en = new ZipEnumeration(file,null);
			while (en.hasMoreElements()) {
				ZipEntry entry = en.nextZipEntry();
				long size = entry.getSize();

                InputStream zin = en.getInputStream(file,entry.getName());

				Reader fr = new InputStreamReader(zin);
				reader = newImporter(entry.getName(),
				                    "file://"+file.getCanonicalPath()+"!"+entry.getName() ,
				                    fr, size);
                if (reader==null) continue; //  unknown file type

				reader.setSilentTime(silentTime);
				reader.start();
			}
			en.close();
		}
		else if (FileUtil.hasExtension(name,"tar") ||
		        FileUtil.hasExtension(name,"tgz") || FileUtil.hasExtension(name,"tgzip") ||
		        FileUtil.hasExtension(name,"tbz") || FileUtil.hasExtension(name,"tbz2") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"gz") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"gzip") ||
			FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"bz") ||
			FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"bz2")) {
		    //  tar (also tar.gz, tgz, tar.bz, tar.bz2, tbz, tbz2
			TarEnumeration en = new TarEnumeration(file,null);
			while (en.hasMoreElements()) {
				TarEntry entry = en.nextTarEntry();
				long size = entry.getSize();

                InputStream zin = en.getInputStream(file,entry.getName());

				Reader fr = new InputStreamReader(zin);
				reader = newImporter(entry.getName(),
				                    "file://"+file.getCanonicalPath()+"!"+entry.getName() ,
				                    fr, size);
                if (reader==null) continue; //  unknown file type

				reader.setSilentTime(silentTime);
				reader.start();
			}
			en.close();
	    }
        else if (FileUtil.hasExtension(name,"gzip") || FileUtil.hasExtension(name,"gz"))
        {
			//  GZIP
			long size = file.length();
			InputStream in = new FileInputStream(file);
			GZIPInputStream gin = new GZIPInputStream(in,4096);

			Reader fr = new InputStreamReader(gin);
			reader = newImporter(trimmedName,
								"file://"+file.getCanonicalPath(), fr, -1/* deflated size ? */);
			if (reader==null) throw new IOException("Unknown File Type "+file.getName());

			reader.setSilentTime(silentTime);
			reader.start();
        }
        else if (FileUtil.hasExtension(name,"bz") || FileUtil.hasExtension(name,"bz2"))
        {
			//  BZIP
	        long size = file.length();
	        InputStream in = new FileInputStream(file);

	        CBZip2InputStream bzin = FileUtil.createBZipInputStream(in);

	        Reader fr = new InputStreamReader(bzin);
	        reader = newImporter(trimmedName,
						        "file://"+file.getCanonicalPath(), fr, -1/* deflated size ? */);
	        if (reader==null) throw new IOException("Unknown File Type "+file.getName());

	        reader.setSilentTime(silentTime);
	        reader.start();
        }

		if (reader==null)
            throw new IOException("Unknown File Type "+file.getName());

		return reader;
	}

	public static void openURL(URL url)
		throws Exception
	{
		String name = FileUtil.getFilePath(url);
		String trimmedName = FileUtil.trimExtension(name);

        URLConnection conn = url.openConnection();
        int size = conn.getContentLength();
        PGNImport reader = null;

		if (pgnFilter.accept(null,name) || epdFilter.accept(null,name))
        {
		    InputStream fin = conn.getInputStream();
            BufferedInputStream bin = new BufferedInputStream(fin,4096);

			Reader fr = new InputStreamReader(bin, "ISO-8859-1");
			/**	PGN uses ISO-8859-1 by definition
			 *	can we rely on that ?
			 */
			reader = newImporter(name, url.toExternalForm(), fr, size);
            if (reader==null) throw new IllegalArgumentException("unknown file type "+name);
			reader.start();
		}
		else if (FileUtil.hasExtension(name,"zip"))
        {
            InputStream in = conn.getInputStream();
            ZipInputStream zin = new ZipInputStream(in);

            for (ZipEntry ety = zin.getNextEntry(); ety != null; ety = zin.getNextEntry())
            {
				Reader fr = new InputStreamReader(zin);
				reader = newImporter(ety.getName(), url+"!"+ety.getName(), fr, ety.getSize());
                if (reader==null) continue; //   "unknown file type "+file.getName()

				reader.start();

                zin.closeEntry();
            }
		}
		else if (FileUtil.hasExtension(name,"tgz") || FileUtil.hasExtension(name,"tgzip") ||
		        FileUtil.hasExtension(name,"tbz") || FileUtil.hasExtension(name,"tbz2") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"gz") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"gzip") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"bz") ||
		        FileUtil.hasExtension(trimmedName,"tar") && FileUtil.hasExtension(name,"bz2"))
		{
			//  tar.gz
			InputStream in = conn.getInputStream();
			TarInputStream tin = TarEnumeration.createTarInputStream(in,name);

			for (TarEntry ety = tin.getNextEntry(); ety != null; ety = tin.getNextEntry())
			{
				Reader fr = new InputStreamReader(tin);
				reader = newImporter(ety.getName(), url+"!"+ety.getName(), fr, ety.getSize());
			    if (reader==null) continue; //   "unknown file type "+file.getName()

				reader.start();
			}
		}
		else if (FileUtil.hasExtension(name,"gzip"))
        {
	        InputStream in = conn.getInputStream();
            GZIPInputStream gin = new GZIPInputStream(in,4096);

            Reader fr = new InputStreamReader(gin);
	        reader = newImporter(trimmedName, url.toExternalForm(), fr, -1/*deflated size ?? */);
            if (reader==null) throw new IllegalArgumentException("unknown file type "+trimmedName);

            reader.start();
        }
		else if (FileUtil.hasExtension(name,"bz") || FileUtil.hasExtension(name,"bz2"))
        {
			//  BZip
			InputStream in = conn.getInputStream();

			CBZip2InputStream bzin = FileUtil.createBZipInputStream(in);

		    Reader fr = new InputStreamReader(bzin);
			reader = newImporter(trimmedName, url.toExternalForm(), fr, -1/*deflated size ?? */);
		    if (reader==null) throw new IllegalArgumentException("unknown file type "+trimmedName);

		    reader.start();
        }
        else
            throw new IllegalArgumentException("unknown file type "+name);
	}


	public int init() throws Exception
	{
		super.init();

		if (Version.getSystemProperty("jose.asynch.import",false)) {
			writer = new Writer();

			writer.setPriority(Thread.MAX_PRIORITY);
			this.setPriority(Thread.MIN_PRIORITY);
			//	usually the writer thread needs more time
			JoConnection.getAdapter().setProcessPriority(DBAdapter.LONG_RUNNING_INSERT);

			writer.start();
		}

		/**
		 * if delayed key writing is enabled, we don't care about disabling keys
		 *
		 * if delayed key writing is off, we have to avoid huge key updates.
		 * in that case, it might be useful to completely disable keys and
		 * enable them afterwards.
		 */
		canDisableKeys = ! connection.getAdapter().can("delayed_key_write");

		if (canDisableKeys && total > DISABLE_KEY_FILE_SIZE) {
			disableKeys(getConnection());
			disableKeys = true;
		}
		else
			disableKeys = false;
		return RUNNING;
	}

	public int work()
		throws Exception
	{
		if (Version.getSystemProperty("jose.asynch.import",false)) {
			if (writer.error!=null)
				throw writer.error;
				/*	throw this exception on behalf of the writer thread	*/

			if (gm.isFull()) {
				try {
					//	wait until writer has cleared the buffer
					writer.wakeUp();		//	kick it
					//System.out.println("reader wait "+(System.currentTimeMillis()-startTime));
					sleep(5000);
				} catch (InterruptedException iex) {
					//	signal from writer that buffer is available
				}
				return RUNNING;
			}
		}

		in.skipWhiteSpace();

		if (leftover) {
			//	parse tag (left over from previous scan)
			gm.insertTag(lineBuffer.getValue(),key,value);
			leftover = false;
		}

		lineBuffer.setLength(0);
		boolean readOK = read1Game();

		/*	insert into database */
		if (readOK) {
			gm.addBatch();	//	if buffer becomes full, writer will be kicked off

			if (gm.isFull()) {
				if (Version.getSystemProperty("jose.asynch.import",false)) {
					if (writer.sleeping) {
						writer.wakeUp();
						yield();
					}
				} else
					gm.update(null);
			}

			rowCount++;
			if (canDisableKeys && !disableKeys &&
			     (rowCount > DISABLE_KEY_LIMIT) &&
			     (getProgress() < DISABLE_KEY_PERCENTAGE))
			{
				disableKeys(getConnection());
				disableKeys = true;
			}
		}

		if (eof) {
			if (Version.getSystemProperty("jose.asynch.import",false)) {
				while (writer.isAlive())
					try {
						System.err.println("j1");
						writer.join();
						System.err.println("j2");
					} catch (InterruptedException iex) {
						//	allright ...
						System.err.println("j2");
					}

				if (writer.error!=null)
					throw writer.error;
					/*	throw this exception on behalf of the writer thread	*/
			}
			else if (!gm.isEmpty())
				gm.update(null);

			if (disableKeys) {
				total = 0;  //  reset progress bar
				enableKeys(getConnection());
				disableKeys = false;
			}

			return SUCCESS;
		}
		else
			return RUNNING;
	}

	public boolean read1Game()
	        throws IOException
	{
		boolean result = false;
		boolean inText = false;
		boolean wasEmpty = true;
		int i=0;
		int startText = 0;

		for (;;)
		{
			if (!inText)
				startText = i;

			if (!in.readLine(lineBuffer) && lineBuffer.length()==i) {
				eof = true;
				break;	//	EOF;
			}
			if (lineBuffer.length()==i) {
				wasEmpty = true;
				if (inText) {
					//  double line breaks are always preserved
					lineBuffer.append("\n"); i++;
				}
				continue;	//	empty line (outside of text)
			}

			//	parse tag
			int wasTag = gm.parseTag(lineBuffer.getValue(), i,lineBuffer.length(), key,value);

			if (inText) {
				if ((wasTag == GameBuffer.TAG_LINE) && wasEmpty) {
					leftover = true;
//					wasEmpty = (wasTag==GameBuffer.EMPTY_LINE);
					break;		//	start of next tag section
				}
			}
			else {
				if (wasTag == GameBuffer.TAG_LINE)
					gm.insertTag(lineBuffer.getValue(),key,value);
				if (wasTag != GameBuffer.EMPTY_LINE)
					result = true;
				if (wasTag == GameBuffer.TEXT_LINE)
					inText = true;	//	now entering text section
			}

			if (inText) {
                boolean brk = preserveLineBreaks;
                if (lineBuffer.charAt(i)=='%' || lineBuffer.charAt(i)==';')
                    brk = true;    //  single line comment: line break MUST be preserved
                lineBuffer.append(brk ? '\n':' ');
            }

			wasEmpty = (wasTag==GameBuffer.EMPTY_LINE);
			i = lineBuffer.length();
		}

		gm.setGameText(gm.row, lineBuffer.getValue(), startText,i);

		return result;
	}

	protected void finishCollection(int CId, int rowCount, int attributes)
		throws SQLException
	{
		String sql = "UPDATE Collection " +
					" SET GameCount = ?, Attributes = ? "+
		        	" WHERE Id = ?";

		JoPreparedStatement pstm = connection.getPreparedStatement(sql);
		pstm.setInt(1, rowCount);
		pstm.setInt(2, attributes);
		pstm.setInt(3, CId);
		pstm.execute();
	}

	/**	commit work after successful execution
	 */
	public void commit()
		throws Exception
	{
		finishCollection(CId,rowCount, Collection.READ_COMPLETE);
		connection.commit();
	}

	/**	rollback work after abortion
	 * 	(may not be supported by the database, actually ... )
	 */
	public void rollback()
		throws Exception
	{
		finishCollection(CId,rowCount, Collection.DELETED);
		connection.rollback();
	}


	public static void disableKeys(JoConnection connection) throws SQLException
	{
		/** this is a BIG one; let's disable indexes
		 *  and restore them later
		 */
		JoConnection.getAdapter().disableConstraints("Game",connection);
		JoConnection.getAdapter().disableConstraints("MoreGame",connection);
	}

	public static void enableKeys(JoConnection connection)
	{
		try {
			JoConnection.getAdapter().enableConstraints("Game",connection);
		} catch (SQLException sqlex) {
			Application.error(sqlex);
		}
		try {
			JoConnection.getAdapter().enableConstraints("MoreGame",connection);
		} catch (SQLException sqlex) {
			Application.error(sqlex);
		}
	}

	/**
	 * perform any necessary cleanup
	 * @param state the state of the task

     */
	public int done(int state)
	{
//		System.out.println("PGN import: "+(double)getElapsedTime()/1000.0+
//						" unique strings="+Version.getSystemProperty("jose.unique.strings",true));

        boolean wasShared = shared;
        try {
            //  it's important to call commit() first
            //  but don't release the connection right now
            shared = true;  //  inidicates not to release the connection
            int result = super.done(state);

            /*	update finished; refresh display, if necessary	*/
            if (broadcast)
                DBTask.broadcastAfterUpdate(CId);

            //  enable keys; we can do it safely since here we ar in a separate thread
            if (disableKeys) {
                enableKeys(getConnection());
                disableKeys = false;
            }

            //  flag tables for needing analysis
            Setup setup = new Setup(Application.theApplication.theConfig,"MAIN",getConnection());
            if (rowCount > ANALYZE_LIMIT)
                try { setup.markAllDirty(); } catch (SQLException ex) { /* ignore */ }

            //  reset process priority
            try {
                if (connection != null) {
                    JoConnection.getAdapter().setProcessPriority(DBAdapter.NORMAL_QUERY);
                    connection.setAutoCommit(true);
                }
                in.close();
            } catch (Exception ex) {
                //	what the heck...
                Application.error(ex);
            }

            if (--gGameImporterInstance == 0)
                setup.analyzeTables(false);  //  actuall do analyze the tables



            return result;

        } finally {
             //	release connection
            shared = wasShared;
            if (!shared) {
                connection.release();
                connection = null;
            }
        }
    }

	/*	@return the approximate progress state (0.0 = just started, 1.0 = finished),
			< 0 if unknown
			should be thread-safe
	*/
	public double getProgress()
	{
		if (total <= 0)
			return PROGRESS_UNKNOWN;
		else
			return ((double)in.getPosition())/total;
	}

}
