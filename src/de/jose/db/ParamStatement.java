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

package de.jose.db;

import de.jose.util.IntArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Vector;

/**
 *
 * note:
 * StringBuffer.append(StringBuffer) is new since 1.4
 * for backward compatibility (compile with 1.4, run with 1.3)
 * we use StringBuffer.append(Object) instead.
 *
 */
public class ParamStatement
{
	public StringBuffer select;
	public StringBuffer from;
	public StringBuffer where;
	public StringBuffer group;
	public StringBuffer order;
	public StringBuffer having;
	public StringBuffer limit;

	public StringBuffer update;
	public StringBuffer set;
	public StringBuffer delete;
    public StringBuffer insert;
	public StringBuffer replace;

	protected Vector parameters;
	protected IntArray parameterTypes;

    /** UNIONS (Vector od ParamStatement) */
    protected Vector unions;

	protected static final int[] EMPTY = new int[10];

	public ParamStatement()
	{
		select	= new StringBuffer();
		from	= new StringBuffer();
		where	= new StringBuffer();
		group	= new StringBuffer();
		order	= new StringBuffer();
		having 	= new StringBuffer();
		limit	= new StringBuffer();
		update	= new StringBuffer();
		set		= new StringBuffer();
		delete	= new StringBuffer();
        insert  = new StringBuffer();
		replace = new StringBuffer();

		parameters = new Vector();
		parameterTypes = new IntArray();
        unions = null;
	}
	
	public ParamStatement(ParamStatement copy)
	{
		this();
		select.append((Object)copy.select);
		from.append((Object)copy.from);
		where.append((Object)copy.where);
		group.append((Object)copy.group);
		order.append((Object)copy.order);
		having.append((Object)copy.having);
		limit.append((Object)copy.limit);
		update.append((Object)copy.update);
		set.append((Object)copy.set);
		delete.append((Object)copy.delete);
		insert.append((Object)copy.insert);
        replace.append((Object)copy.replace);

		parameters = (Vector)copy.parameters.clone();
		parameterTypes = (IntArray)copy.parameterTypes.clone();

        if (copy.unions != null) {
            unions = new Vector();
            for (int i=0; i < copy.countUnions(); i++)
                unions.add(new ParamStatement(copy.getUnion(i)));
    	}
    }

	public String assembleSQL(boolean withLimit)
	{
		//	assemble SQL
		return assembleSQL(null, true, withLimit).toString();
	}

	public StringBuffer assembleSQL(StringBuffer sql, boolean withOrder, boolean withLimit)
	{
		if (sql==null) sql = new StringBuffer();

		if (insert.length() > 0) {
		    sql.append("INSERT INTO ");
		    sql.append((Object)insert);

		    if (select.length() > 0) {      //  INSERT INTO ... SELECT
		        sql.append(" SELECT ");
		        sql.append((Object)select);
		    }
		}
		else if (select.length() > 0) {
			sql.append("SELECT ");
			sql.append((Object)select);
		}
		else if (update.length() > 0) {
			sql.append("UPDATE ");
			sql.append((Object)update);
		}
		else if (delete.length() > 0) {
			sql.append("DELETE ");
			sql.append((Object)delete);
		}
        else if (replace.length() > 0) {
            sql.append("REPLACE INTO ");
            sql.append((Object)insert);
        }
		else
			throw new IllegalStateException("SELECT, UPDATE, INSERT or DELETE expected");
		
		if (from.length() > 0) {
			sql.append(" FROM ");
			sql.append((Object)from);
		}
		if (set.length() > 0) {
			sql.append(" SET ");
			sql.append((Object)set);
		}
		if (where.length() > 0) {
			sql.append(" WHERE ");
			sql.append((Object)where);
		}
		if (group.length() > 0) {
			sql.append(" GROUP BY ");
			sql.append((Object)group);
		}
		if (having.length() > 0) {
			sql.append(" HAVING ");
			sql.append((Object)having);
		}

        for (int i=0; i < countUnions(); i++)
        {
            sql.append(" UNION ALL ");  //  UNION ALL, right ?
            getUnion(i).assembleSQL(sql, false, false);
        }

		if (withOrder && order.length() > 0) {
			sql.append(" ORDER BY ");
			sql.append((Object)order);
		}

        if (withLimit && limit.length() > 0) {
            sql.append(" LIMIT ");
            sql.append((Object)limit);
        }

		return sql;
	}

	public String toString()
	{
		StringBuffer buf = assembleSQL(null, true, true);
		if (countParameters() > 0) {
			buf.append(" [");
            int i=1;
			for ( ; i <= countParameters(); i++) {
				if (i > 1) buf.append(",");
				buf.append(getParameter(i));
			}
            for (int j=0; j < countUnions(); j++)
            {
                ParamStatement ustm = getUnion(j);
                for (int k=1; k<=ustm.countParameters(); k++)
                {
                    if (i > 1) buf.append(",");
                    buf.append(ustm.getParameter(k));
                }
            }
			buf.append("]");
		}
		return buf.toString();
	}

	public String toString(DBAdapter adapter)
	{
		String sql = toString();
		return adapter.escapeSql(sql);
	}

	public JoPreparedStatement toPreparedStatement(JoConnection conn, int type, int concurrency)
		throws SQLException
	{
		String sql = assembleSQL(JoConnection.getAdapter().useResultLimit());
		sql = JoConnection.getAdapter().escapeSql(sql);

		JoPreparedStatement pstm = conn.getPreparedStatement(sql, type, concurrency);
//		pstm.setFetchSize(Integer.MIN_VALUE);	//	--> use dynamic ResultSet for mysql driver

        int i = setParameters(pstm,1);

        for (int j=0; j < countUnions(); j++)
            i = getUnion(j).setParameters(pstm,i);

        return pstm;
    }

    protected int setParameters(JoPreparedStatement pstm, int i)
        throws SQLException
    {
		for (int j=1; j <= countParameters(); j++) {
			int colType = getType(j);
			switch (colType) {
			case Types.CHAR:
			case Types.VARCHAR:
				//	workaround required for mySQL
				pstm.setString(i++, (String)getParameter(j)); break;
			case Types.DATE:
				//	workaround required for mySQL
				pstm.setDate(i++, (Date)getParameter(j)); break;
			case Types.TIME:
				//	workaround required for mySQL
				pstm.setTime(i++, (Date)getParameter(j)); break;
			case Types.TIMESTAMP:
				//	workaround required for mySQL
				pstm.setTimestamp(i++, (Date)getParameter(j)); break;
			default:
				pstm.setObject(i++, getParameter(j), colType); break;
			}

		}
        return i;
	}

	public JoPreparedStatement toPreparedStatement(JoConnection conn)
		throws SQLException
	{
		return toPreparedStatement(conn, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	public JoPreparedStatement execute(JoConnection conn) throws SQLException
	{
		JoPreparedStatement pstm = toPreparedStatement(conn);
		pstm.execute();
		return pstm;
	}

	public JoPreparedStatement execute(JoConnection conn, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		JoPreparedStatement pstm = toPreparedStatement(conn,resultSetType,resultSetConcurrency);
		pstm.execute();
		return pstm;
	}

	public int executeUpdate(JoConnection conn) throws SQLException
	{
		JoPreparedStatement pstm = toPreparedStatement(conn);
		try {
			if (pstm.execute())
				throw new SQLException("UPDATE statement expected");
			return pstm.getUpdateCount();
		} finally {
			if (pstm!=null) pstm.close();
		}
	}

	public int selectInt(JoConnection conn)
		throws SQLException
	{
		JoPreparedStatement pstm = toPreparedStatement(conn);
		return pstm.selectInt();
	}


	public final void addParameter(int type, Object value)
	{
		parameterTypes.add(type);
		parameters.add(value);
	}

    public final void insertParameter(int index, int type, Object value)
    {
        parameterTypes.add(index-1,type);
        parameters.insertElementAt(value,index-1);
    }

	public final void addStringParameter(int type, String stringValue)
	{
		switch (type)
		{
		default:
		case Types.CHAR:
		case Types.VARCHAR:		
			addParameter(type, stringValue); break;
		case Types.INTEGER:
			addParameter(type, new Integer(stringValue)); break;
		case Types.DOUBLE:
			addParameter(type, new Double(stringValue)); break;
		}
	}

	public final void addIntParameter(int value)
	{
		addParameter(Types.INTEGER, new Integer(value));
	}

    public final void addIntParameters(int[] values)
    {
        for (int i=0; i<values.length; i++)
            addParameter(Types.INTEGER, new Integer(values[i]));
    }

	public final void insertIntParameter(int index, int value)
	{
		insertParameter(index, Types.INTEGER, new Integer(value));
	}

    public final ParamStatement getUnion(int i)
    {
        return (ParamStatement)unions.get(i);
    }

    public final int countUnions()
    {
        return (unions==null) ? 0:unions.size();
    }

    public final void addUnion(ParamStatement pstm)
    {
        if (unions==null) unions = new Vector();
        unions.add(pstm);
    }

	public final int countParameters()
	{
		return parameters.size();
	}
	
	public final Object getParameter(int n)
	{
		return parameters.get(n-1);
	}
	
	public final void clearParameters()
	{
		parameters.clear();
		parameterTypes.clear();
	}
	
	public final int getType(int n)
	{
		return parameterTypes.get(n-1);
	}

	public void setLimit(int start, int length)
	{
		limit.setLength(0);
		if (start > 0) {
			limit.append(start);
			limit.append(",");
			if (length > 0)
				limit.append(length);
			else
				limit.append(String.valueOf(Integer.MAX_VALUE));	//	retrieve rest
		}
		else if (length > 0)
			limit.append(length);
	}


    public Object mark()
    {
        int[] state = new int[10];

        state[0] = select.length();
        state[1] = from.length();
        state[2] = where.length();
        state[3] = group.length();
        state[4] = order.length();
        state[5] = update.length();
        state[6] = set.length();
        state[7] = delete.length();
        state[8] = delete.length();
        state[9] = parameters.size();

        return state;
    }

    public void reset(Object mk)
    {
        int[] state = (int[])mk;

        select.setLength(state[0]);
        from.setLength(state[1]);
        where.setLength(state[2]);
        group.setLength(state[3]);
        order.setLength(state[4]);
        update.setLength(state[5]);
        set.setLength(state[6]);
        delete.setLength(state[7]);
        insert.setLength(state[8]);
        parameters.setSize(state[9]);
        parameterTypes.setSize(state[9]);

	    if (unions!=null) unions.clear();
    }

	public void clear()
	{
		reset(EMPTY);
		clearParameters();
        unions = null;
	}
}
