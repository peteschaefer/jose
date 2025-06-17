package de.jose.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetAdapter
{
    protected ResultSet delegate;

    public ResultSetAdapter(ResultSet delegate) { this.delegate = delegate; }

    public boolean next() throws SQLException { return delegate.next(); }
    public void close() throws SQLException { delegate.close();}

    public int getInt(int columnIndex) throws SQLException { return delegate.getInt(columnIndex); }
    public long getLong(int columnIndex) throws SQLException { return delegate.getLong(columnIndex); }
    public String getString(int columnIndex) throws SQLException { return delegate.getString(columnIndex); }
    public byte[] getBytes(int columnIndex) throws SQLException { return delegate.getBytes(columnIndex); }
}
