package de.jose.db;

import de.jose.util.map.IntHashMap;
import de.jose.util.map.IntHashSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MoreGameCache
{
    protected static class Row {
        //int GId;
        String fen;
        byte[] bin;
        long whiteSignature, blackSignature;
        boolean hasVariations;

        Row(ResultSet rs) throws SQLException {
            fen = rs.getString(2);
            bin = rs.getBytes(3);
            whiteSignature = rs.getLong(4);
            blackSignature = rs.getLong(5);
            hasVariations = rs.getInt(6) > 0;
        }
    }

    protected IntHashMap rows = new IntHashMap();
    protected IntHashSet collections = new IntHashSet();

    public int size() { return rows.size(); }
    public void clear() {
        rows.clear();
        collections.clear();
    }

    protected void putRow(ResultSet res) throws SQLException {
        int GId = res.getInt(1);
        Row row = new Row(res);
        rows.put(GId, row);
    }

    public void warmUp() throws SQLException
    {
        ParamStatement sql = new ParamStatement();
        sql.select.append("MoreGame.GId");
        makeFullTableScanStatement(sql);

        JoConnection conn=null;
        try {
            conn = JoConnection.get();
            JoPreparedStatement pstm = sql.execute(conn);
            ResultSetAdapter scan = beginFullTableScan(pstm.getResultSet());
            while(scan.next())
                /* copies data into cache */ ;
            scan.close();
        } finally {
            JoConnection.release(conn);
        }
    }

    public void makeFullTableScanStatement(ParamStatement pstm)
    {
        if (pstm.from.length() > 0) pstm.from.append(", ");
        pstm.from.append("MoreGame");
        if (pstm.select.length() > 0) pstm.select.append(", ");
        pstm.select.append("MoreGame.FEN, MoreGame.Bin, MoreGame.WhiteSignature, MoreGame.BlackSignature");
    }

    public ResultSetAdapter beginFullTableScan(ResultSet delegate) {
        collections.clear();
        collections.add(0);  //  indicates a full-table scan
        return new ReadThroughResultSet(delegate);
    }

    public void makeCollectionScanStatement(ParamStatement pstm,

                                            int[] gameRange)
    {
        pstm.from.append(", MoreGame");
        pstm.select.append(", MoreGame.FEN, MoreGame.Bin, MoreGame.WhiteSignature, MoreGame.BlackSignature," +
                            " 0 AS HasVariations"); //  TODO LOCATE(0xf0,Bin), or (Attributes & 1)
        pstm.where.append(" AND MoreGame.GId BETWEEN ? AND ?");
        pstm.addIntParameter(gameRange[0]);
        pstm.addIntParameter(gameRange[1]);
    }

    public ResultSetAdapter beginCollectionScan(ResultSet delegate, IntHashSet collections) {
        collections.addAll(collections);  //  indicates a full-table scan
        return new ReadThroughResultSet(delegate);
    }

    public ResultSetAdapter beginCachedScan(ResultSet delegate) {
        return new CachedResultSet(delegate);
    }

    public boolean hasFullTable() {
        return collections.contains(0);
    }

    public boolean hasCollection(int CId) {
        return collections.contains(CId);
    }

    /**
     * reads a ResultSet and copies all its contents into the Cache
     */
    public class ReadThroughResultSet extends ResultSetAdapter {

        public ReadThroughResultSet(ResultSet delegate) {
            super(delegate);
        }

        @Override
        public boolean next() throws SQLException {
            if (!super.next()) return false;
            putRow(super.delegate);
            return true;
        }
    }

    /**
     * reads Ids from a ResultSet and fills in missing data
     */
    public class CachedResultSet extends ResultSetAdapter {

        protected Row nextRow = null;

        public CachedResultSet(ResultSet delegate) {
            super(delegate);
        }

        @Override
        public boolean next() throws SQLException {
            if (!super.next()) {
                nextRow = null;
                return false;
            }
            int GId = delegate.getInt(1);
            nextRow = (Row) rows.get(GId);
            return true;
        }

        @Override
        public byte[] getBytes(int columnIndex) throws SQLException {
            if (columnIndex==3 && nextRow!=null)
                return nextRow.bin;
            return super.getBytes(columnIndex);
        }

        @Override
        public long getLong(int columnIndex) throws SQLException {
            if (columnIndex==4 && nextRow!=null)
                return nextRow.whiteSignature;
            if (columnIndex==5 && nextRow!=null)
                return nextRow.blackSignature;
            return super.getLong(columnIndex);
        }

        @Override
        public int getInt(int columnIndex) throws SQLException {
            if (columnIndex==6 && nextRow!=null)
                return nextRow.hasVariations ? 1:0;
            return super.getInt(columnIndex);
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            if (columnIndex==2 && nextRow!=null)
                return nextRow.fen;
            return super.getString(columnIndex);
        }
    }
}
