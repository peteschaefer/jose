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

package de.jose.pgn;

import de.jose.Language;
import de.jose.Util;
import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.JoStatement;
import de.jose.util.StringUtil;
import de.jose.util.IntArray;
import de.jose.util.map.IntHashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Iterator;



public class Collection
{

	protected static final String SQL_INSERT =
		"INSERT INTO Collection (PId,OPId,Name,Path,Attributes,SourceURL,LastModified,GameCount,Id) "+
		" VALUES (?,?,?,?,?,?,?,?,?)";
	protected static final String SQL_UPDATE =
		"UPDATE Collection SET PId=?,OPId=?,Name=?,Path=?,Attributes=?,SourceURL=?,LastModified=?,GameCount=? "+
		" WHERE Id=? ";

	protected static final String SQL_SELECT =
	    "SELECT Collection.Id, Collection.PId, Collection.OPId, Collection.Name, Collection.Path," +
	    "   Collection.Attributes, Collection.SourceURL, Collection.LastModified," +
	    "   Collection.GameCount ";

	protected static final String SQL_READ =
		SQL_SELECT+" FROM Collection WHERE Id = ? ";
    protected static final String SQL_READ_ROOT =
        SQL_SELECT+", substring(Path,1,1) AS SubPath" +
        " FROM Collection WHERE PId <= 0 OR PId IS NULL " +
        " ORDER BY SubPath, Name";
	protected static final String SQL_READ_CHILDREN =
		SQL_SELECT+", substring(Path,1,1) AS SubPath" +
        " FROM Collection WHERE PId = ? " +
	    " ORDER BY SubPath, Name";
	protected static final String SQL_READ_BY_GID =
		SQL_SELECT+" FROM Game,Collection WHERE Collection.Id = Game.CId AND Game.Id = ? ";
    protected static final String SQL_HAS_CONTENTS =
       "SELECT Collection.Id " +
       " FROM Collection " +
       " WHERE PId = ? " +
       "   OR (Id=? AND GameCount > 0) ";

	/**	Id of the Clipboard	 */
	public static final int		CLIPBOARD_ID	= 1;
	/**	Id of the Autosave collection	 */
	public static final int		AUTOSAVE_ID		= 2;
	/** Id of the Trash collection  */
    public static final int     TRASH_ID        = 3;
	/** Id of the Download collection */
	public static final int		DOWNLOADS_ID	= 4;

	public static final String TRASH_PATH		= ":/"+TRASH_ID+"/";
	public static final String CLIPBOARD_PATH	= ":/"+CLIPBOARD_ID+"/";
	public static final String AUTOSAVE_PATH	= ":/"+AUTOSAVE_ID+"/";
	public static final String DOWNLOADS_PATH	= ":/"+DOWNLOADS_ID+"/";

    /** Attribute bits  */
    /** marked for deletion */
    public static final short   DELETED         = 0x0040;
    /** this is a system file (clipboard,autosave,trash)    */
    public static final short   SYSTEM          = 0x0010;
    /** read complete (set after successful import) */
    public static final short   READ_COMPLETE   = 0x0008;
    /** this attribute indicates that there is a position index for this collection
     *  (see MoreGame.PosMain and MoreGame.PosVar)
     * */
    public static final short POS_INDEX         = 0x0100;


	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	primary key	*/
	public int			Id;
	/**	parent Id	*/
	public int			PId;
	/** original parent Id (for moved collections) **/
	public int          OPId;
	/** display name    */
	public String		Name;
	/**	path (with Ids)	*/
	public String		Path;
	/**	bit flags	*/
	public short		Attributes;
	/**	original download URL	*/
	public String		SourceURL;
	/**	last modification date	*/
	public Timestamp	LastModified;
	/**	number of games	*/
	public int			GameCount;


	public static Collection newCollection(String name, int id)
	{
		Collection result = new Collection();
		result.Id				= id;
		result.Name				= name;
		result.Attributes   	= 0;
		result.LastModified		= new Timestamp(System.currentTimeMillis());
		result.GameCount		= 0;
		return result;
	}

	public static int getSequence(JoConnection conn) throws SQLException
	{
		return conn.getSequence("Collection","Id");
	}

	public static int getSequence(JoConnection conn, int count) throws SQLException
	{
		return conn.getSequence("Collection","Id",count);
	}

	public static void resetSequence(JoConnection conn) throws SQLException
	{
		conn.resetSequence("Collection","Id");
	}

	public static Collection newCollection(int parentId, String name, JoConnection conn)
		throws SQLException
	{
		Collection result = newCollection(name, Collection.getSequence(conn));
		if (parentId <= 0)
			result.setParent(null);
		else
			result.setParent(conn,parentId);
		return result;
	}

	public static Collection newFileCollection(int parentId, String fileName, String url, JoConnection conn)
		throws SQLException
	{
		Collection result 	= newCollection(parentId,fileName,conn);
		result.SourceURL	= url;
		return result;
	}

	public static String makeUniqueName(int parentId, String name, JoConnection conn) throws SQLException
	{
		if (!exists(parentId,name,conn))
			return name;

		String prefix = name+" [";
		String suffix = "]";
		int next = 2;

		int k2 = name.lastIndexOf(']');
		if (k2 > 0) {
			int k1 = name.lastIndexOf('[',k2);
			if (k1 >= 0) {
				String inbetween = name.substring(k1+1,k2);
				if (StringUtil.isInteger(inbetween)) {
					next = Integer.parseInt(inbetween)+1;
					prefix = name.substring(0,k1+1);
					suffix = name.substring(k2);
				}
			}
		}

		for ( ; ; next++) {
			String newName = prefix+next+suffix;
			if (!exists(parentId,newName,conn))
				return newName;
		}
		//	never reached
	}

	public static int makeDownloads(JoConnection conn) throws SQLException {
		String sql =
			"INSERT INTO Collection "+
			" VALUES (?,null,null, ?, ?, 16, '-', {fn now()}, 0) "+
			" ON DUPLICATE IGNORE";

		JoPreparedStatement pstm = conn.getPreparedStatement(sql);
		pstm.setInt(1,DOWNLOADS_ID);
		pstm.setString(2,"collection.downloads");
		pstm.setString(3,DOWNLOADS_PATH);
		boolean ok = pstm.execute();
		return DOWNLOADS_ID;
	}
	
	public static boolean exists(int parentId, String name, JoConnection conn) throws SQLException
	{
		String sql =
			"SELECT Id FROM Collection "+
			" WHERE Name = ? "+
			" AND PId ";
		if (parentId==0)
			sql += "IS NULL";
		else
			sql += "=?";

		JoPreparedStatement pstm = conn.getPreparedStatement(sql);
		pstm.setString(1,name);
		if (parentId != 0) pstm.setInt(2,parentId);
		return pstm.exists();
	}

	public static Collection readCollection(JoConnection conn, int id)
		throws SQLException
	{
		Collection result = new Collection();
		result.read(conn,id);
		return result;
	}

	public static Collection readCollection(int id)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			return readCollection(conn,id);
		} finally {
			if (conn!=null) conn.release();
		}
	}

	public static Collection readCollectionByGame(JoConnection conn, int GId)
		throws SQLException
	{
		Collection result = new Collection();
		result.readByGame(conn,GId);
		return result;
	}

	public static Collection readCollectionByGame(int GId)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			return readCollectionByGame(conn,GId);
		} finally {
			if (conn!=null) conn.release();
		}
	}

    public static final int getMaxIndex(JoConnection conn, int collId) throws SQLException
    {
        String sql;
        if (JoConnection.getAdapter().preferMaxAggregate())
            sql = "SELECT MAX(Idx) FROM Game WHERE CId = ?";
        else
            sql = "SELECT Idx FROM Game WHERE CId = ? ORDER BY Idx DESC";

        JoPreparedStatement stm = conn.getPreparedStatement(sql);
        stm.setInt(1,collId);
        return stm.selectInt();
    }

	public static Date getLatestModified(String pattern1, String pattern2) throws SQLException
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();

			String sql =
			        "SELECT MAX(LastModified) FROM Collection" +
			        " WHERE SourceURL LIKE ?"+
			        "    OR SourceURL LIKE ?";

			JoPreparedStatement stm = conn.getPreparedStatement(sql);
			stm.setString(1,pattern1);
			stm.setString(2,pattern2);
			return stm.selectTimestamp();

		} finally {
			if (conn!=null) conn.release();
		}
	}

	public static void touch(Date lastMod, Date newValue) throws SQLException
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();

			String sql =
			        "UPDATE Collection" +
			        " SET LastModified = ? " +
			        " WHERE LastModified = ? ";

			JoPreparedStatement stm = conn.getPreparedStatement(sql);
			stm.setTimestamp(1,newValue);
			stm.setTimestamp(2,lastMod);
			stm.execute();

		} finally {
			if (conn!=null) conn.release();
		}
	}

	public final boolean isSystem()
	{
		return Util.allOf(Attributes,SYSTEM);
	}

	public static boolean isSystem(int CId)
	{
		return CId <= TRASH_ID;
	}

	public final boolean isTrash()
	{
		return Id == TRASH_ID;
	}

	public final boolean isClipboard()
	{
		return Id == CLIPBOARD_ID;
	}

	public final boolean isInTrash()
	{
		return Path.startsWith(TRASH_PATH) && (Id!=TRASH_ID);
	}

	public static final boolean isInTrash(int CId) throws Exception
	{
		Collection col = readCollection(CId);
		return col.isInTrash();
	}

	public final boolean isInClipboard()
	{
		return Path.startsWith(CLIPBOARD_PATH);
	}
	
	public final boolean isTopLevel()
	{
		return PId == 0;
	}

	public final boolean hasGames()
	{
		return GameCount > 0;
	}

	public void setParent(Collection parent)
	{
		if (parent == null) {
			//	root folder
			PId = 0;
            if (isSystem(Id))
			    Path = ":/"+Id+"/";
            else
                Path = "/"+Id+"/";
		}
		else {
			//	subfolder
			PId = parent.Id;
			Path = parent.Path+Id+"/";
		}
	}

	public void setParent(JoConnection conn, int pid)
		throws SQLException
	{
		Collection parent = null;
		if (pid > 0)
			parent = readCollection(conn,pid);
		setParent(parent);
	}

	public void renameTo(JoConnection conn, String newName)
		throws SQLException
	{
		Name = newName;
		update(conn);
	}

	public void renameTo(String newName)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			renameTo(conn,newName);
		} finally {
			if (conn!=null) conn.release();
		}
	}

	public String toString()
	{
		if (isSystem())
			return Language.get(Name);
		else
			return Name;
	}

	public void insert(JoConnection conn)
		throws SQLException
	{
		JoPreparedStatement pstm = conn.getPreparedStatement(SQL_INSERT);
		setParams(pstm,1);
		pstm.execute();
	}

	public void update(JoConnection conn)
		throws SQLException
	{
		JoPreparedStatement pstm = conn.getPreparedStatement(SQL_UPDATE);
		setParams(pstm,1);
		pstm.execute();
	}

    public static void setAttribute(JoConnection conn, int CId, int attribute, boolean on)
        throws SQLException
    {
        String sql;
        if (on)
            sql = "UPDATE Collection SET Attributes = Attributes | "+attribute+" WHERE Id = ?";
        else
            sql = "UPDATE Collection SET Attributes = Attributes & ~"+attribute+" WHERE Id = ?";

        JoPreparedStatement pstm = conn.getPreparedStatement(sql);
        pstm.setInt(1,CId);
        pstm.execute();
    }


	protected int setParams(JoPreparedStatement pstm, int i)
		throws SQLException
	{
		pstm.setIntNull		(i++,	PId);
		pstm.setIntNull     (i++,   OPId);
		pstm.setString		(i++, 	Name);
		pstm.setString		(i++,	Path);
		pstm.setInt 		(i++, 	Attributes);
		pstm.setString		(i++, 	SourceURL);
		pstm.setTimestamp	(i++, 	LastModified);
		pstm.setInt			(i++, 	GameCount);
		//	Id always come last (see SQL_UPDATE)
		pstm.setInt			(i++, 	Id);

		return i;
	}

	public void read(JoConnection conn, int id)
		throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = conn.getPreparedStatement(SQL_READ);
			pstm.setInt(1,id);
			pstm.execute();

			if (pstm.next())
				read(pstm);
			else
				throw new SQLException("Collection."+id+" not found");

		} finally {
			if (pstm != null) pstm.closeResult();
		}
	}

	public void readByGame(JoConnection conn, int GId)
		throws SQLException
	{
		JoPreparedStatement pstm = null;
		try {
			pstm = conn.getPreparedStatement(SQL_READ_BY_GID);
			pstm.setInt(1,GId);
			pstm.execute();

			if (pstm.next())
				read(pstm);
			else
				throw new SQLException("Collection not found");

		} finally {
			if (pstm != null) pstm.closeResult();
		}
	}

	public static List readChildren(JoConnection conn, int pid)
		throws SQLException
	{
		JoPreparedStatement pstm = null;
		List result = new ArrayList();
		try {
            if (pid <= 0)
                pstm = conn.getPreparedStatement(SQL_READ_ROOT);
            else {
                pstm = conn.getPreparedStatement(SQL_READ_CHILDREN);
			    pstm.setInt(1,pid);
            }
			pstm.execute();

			while (pstm.next()) {
				Collection coll = new Collection();
				coll.read(pstm);
				result.add(coll);
			}

		} finally {
			if (pstm != null) pstm.closeResult();
		}
		return result;
	}


	public static IntHashSet getTree(JoConnection conn, int pid, boolean includeParent)
		throws SQLException
	{
        return getTree(conn,pid,includeParent,null);
    }


    public static IntHashSet getTree(JoConnection conn, int pid, boolean includeParent, IntHashSet result)
        throws SQLException
    {
        return getTree(conn, "%/"+pid+"/%",includeParent, result);
    }

    public static IntHashSet getTree(JoConnection conn, String path, boolean includeParents, IntHashSet result)
        throws SQLException
    {
		String sql = "SELECT Id FROM Collection WHERE Path LIKE ?";
        JoPreparedStatement pstm = null;
        if (!includeParents)
            path += "/%";

        try {
            pstm = conn.getPreparedStatement(sql);
            pstm.setString(1,path);

            return pstm.selectIntHashSet(result);
        } finally {
            if (pstm!=null) pstm.closeResult();
        }
    }

    public static IntHashSet getTrashedCollections(boolean includeParents)
        throws Exception
    {
        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            IntHashSet result = new IntHashSet();
            getTree(conn, TRASH_PATH+"%",includeParents, result);
            getTree(conn, AUTOSAVE_PATH+"%",includeParents, result);
            getTree(conn, CLIPBOARD_PATH+"%",includeParents, result);
            return result;
        } finally {
            JoConnection.release(conn);
        }
    }


	public static Collection[] childrenArray(JoConnection conn, int pid)
		throws SQLException
	{
		List list = readChildren(conn,pid);
		Collection[] result = new Collection[list.size()];
		list.toArray(result);
		return result;
	}

	public int read(JoPreparedStatement pstm)
		throws SQLException
	{
		int i=1;
		Id 				= pstm.getInt(i++);
		PId 			= pstm.getInt(i++);
		OPId            = pstm.getInt(i++);
		Name			= pstm.getString(i++);
		Path			= pstm.getString(i++);
		Attributes		= pstm.getShort(i++);
		SourceURL		= pstm.getString(i++);
		LastModified	= pstm.getTimestamp(i++);
		GameCount		= pstm.getInt(i++);
		return i;
	}

	public static int countGames(int CId)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			return countGames(conn,CId);
		} finally {
			if (conn!=null) conn.release();
		}
	}

	public static int countGames(int[] CIds)
		throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			return countGames(conn,CIds);
		} finally {
			if (conn!=null) conn.release();
		}
	}

	public static int countGames(JoConnection conn, int CId)
		throws SQLException
	{
		JoPreparedStatement pstm = conn.getPreparedStatement("SELECT GameCount FROM Collection WHERE Id = ?  ");
		pstm.setInt(1,CId);
		return pstm.selectInt();
	}

	public static int countGames(JoConnection conn, int[] CIds)
		throws SQLException
	{
		if (CIds==null || CIds.length==0) return 0;

		JoPreparedStatement pstm = conn.getPreparedStatement(
		        "SELECT SUM(GameCount) FROM Collection " +
		        " WHERE Id IN ("+IntArray.toString(CIds,0,CIds.length,",")+")");

		return pstm.selectInt();
	}

    public static boolean hasContents(int CId) throws SQLException
    {
        JoConnection conn = null;
        try {
            conn = JoConnection.get();
            return hasContents(conn,CId);
        } finally {
            if (conn!=null) conn.release();
        }
    }

    public static boolean hasContents(JoConnection conn, int CId) throws SQLException
    {
        JoPreparedStatement pstm = conn.getPreparedStatement(SQL_HAS_CONTENTS);
        pstm.setInt(1,CId);
        pstm.setInt(2,CId);
        return pstm.exists();
    }

	public static int dropAllGames(JoConnection conn, int CId)
		throws SQLException
	{
		/**
		 * 	DELETE FROM Game WHERE CId = ?
		 */

		JoPreparedStatement deleteStm;

		if (conn.getAdapter().canCascadingDelete()) {
			//	CASCADING DELETE is fine: MoreGame will be deleted automatically
			deleteStm = conn.getPreparedStatement("DELETE FROM Game WHERE CId = ? ");
		}
		else if (conn.getAdapter().canSubselect()) {
			//	got to delete MoreGame ourselves
			JoPreparedStatement subDeleteStm = conn.getPreparedStatement(
			        "DELETE FROM MoreGame WHERE GId IN (SELECT GId FROM Game WHERE CId = ?)");
			subDeleteStm.setInt(1,CId);
			subDeleteStm.execute();

			deleteStm = conn.getPreparedStatement("DELETE FROM Game WHERE CId = ? ");
		}
		else if (conn.getAdapter().canMultiTableDelete()) {
			//	special case MySQL:
			deleteStm = conn.getPreparedStatement(
			  	"DELETE FROM Game,MoreGame USING Game,MoreGame WHERE Game.CId = ? AND MoreGame.GId = Game.Id");
		}
		else
			throw new UnsupportedOperationException("no sufficient database capabilities");

		deleteStm.setInt(1,CId);
		deleteStm.execute();
		int result = deleteStm.getUpdateCount();

		JoPreparedStatement updateStm = conn.getPreparedStatement(
		        "UPDATE Collection SET GameCount = 0 WHERE Id = ? ");
		updateStm.setInt(1,CId);
		updateStm.execute();
		return result;
	}

    public void restore(JoConnection conn) throws SQLException
    {
        PId = OPId;
        OPId = 0;
        updatePath(conn,true);
    }

    public static final void updatePath(JoConnection conn, int CId, boolean recurse)
        throws SQLException
    {
        if (CId > 0) {
            //  update one
            Collection coll = readCollection(conn,CId);
            coll.updatePath(conn, recurse);
        }
        else {
            //  update all roots
            List roots = Collection.readChildren(conn,0);
            for (Iterator i = roots.iterator(); i.hasNext(); )
            {
                Collection coll = (Collection)i.next();
                coll.updatePath(conn,recurse);
            }
        }
    }

    public void updatePath(JoConnection conn, boolean recurse)
        throws SQLException
    {
        setParent(conn,PId);  //  looks like a tautology, but updates path !
        update(conn);

        if (recurse) {
            List children = Collection.readChildren(conn,Id);
            for (Iterator i = children.iterator(); i.hasNext(); )
            {
                Collection coll = (Collection)i.next();
                coll.updatePath(conn,recurse);
            }
        }
    }


	/**
	 * update game count when the database has become inconsistent
	 */
	public static final void repairGameCounts()
			throws Exception
	{
		JoConnection conn = null;
		try {
			conn = JoConnection.get();
			repairGameCounts(conn);
		} finally {
			JoConnection.release(conn);
		}
	}

	/**
	 * update game count when the database has become inconsistent
	 */
	public static final void repairGameCounts(JoConnection conn)
			throws SQLException
	{
		//	TODO
		if (JoConnection.getAdapter().canSubselect()) {
			String sql =
					"UPDATE Collection " +
					" SET GameCount = (SELECT COUNT(*) FROM Game WHERE Game.Id = Collection.Id)";
			conn.executeUpdate(sql);
		} else {
			String sql1 =
				"SELECT Collection.Id, Collection.GameCount, COUNT(Game.Id)" +
				" FROM Collection LEFT OUTER JOIN Game ON Collection.Id = Game.CId" +
				" GROUP BY Collection.Id, Collection.GameCount ";
			String sql2 =
				"UPDATE Collection SET GameCount = ? WHERE Id = ?";

			JoStatement stm = null;
			JoPreparedStatement pstm = null;
			try {
				stm = new JoStatement(conn);
				pstm = conn.getPreparedStatement(sql2);

				ResultSet res = stm.executeQuery(sql1);
				while (res.next()) {
					int CId = res.getInt(1);
					int oldCount = res.getInt(2);
					int newCount = res.getInt(3);
					if (oldCount != newCount) {
						pstm.setInt(1,newCount);
						pstm.setInt(2,CId);
						pstm.execute();
					}
				}
			} finally {
				if (stm!=null) stm.close();
			}
		}
	}

	public static int getId(String cname, boolean includeSystem)
			throws SQLException
	{
		JoConnection conn = null;
		try {
			String sql = "SELECT Id FROM Collection WHERE Name LIKE ?";
			if (!includeSystem) sql +=" AND NOT Path LIKE ':%'";

			conn = JoConnection.get();
			JoPreparedStatement pstm = conn.getPreparedStatement(sql);
			pstm.setString(1,cname);
			return pstm.selectInt();

		} finally {
			JoConnection.release(conn);
		}
	}
}
