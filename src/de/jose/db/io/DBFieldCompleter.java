package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.ParamStatement;
import de.jose.pgn.SearchRecord;
import de.jose.util.CharUtil;
import de.jose.util.GlobMatcher;
import de.jose.view.input.AutoCompleteTextField;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static de.jose.util.GlobMatcher.GLOBX_WILDCARDS;
import static de.jose.util.GlobMatcher.SQL_WILDCARDS;

public class DBFieldCompleter implements AutoCompleteTextField.Completer
{
    public DBFieldCompleter(String table, String column) {
        this.table = table;
        this.column = column;

        this.sql = new ParamStatement();
        sql.select.append(column);
        sql.from.append(table);
        sql.order.append(column);
    }

    @Override
    public ArrayList<String> getCompletions(String prefix, int limit)
    {
        if (queryCounter.get()%2 == 1) {
            //  another query is still active
            int cleanCounter = queryCounter.incrementAndGet();
            assert(cleanCounter%2 == 0);
        }

        int cleanCounter = queryCounter.get();
        assert(cleanCounter%2 == 0);

        int thisQuery = queryCounter.incrementAndGet();
        assert(thisQuery%2 == 1);

        JoConnection conn=null;
        ArrayList<String> result = new ArrayList<>();
        try {
            sql.where.setLength(0);
            sql.clearParameters();
            SearchRecord.appendNameSearchPattern(sql,table,column, prefix, caseSensitive);

            if (limit > 0) {
                sql.limit.setLength(0);
                sql.limit.append(String.valueOf(limit + 1));
            }

            //prefix = prefix.replace('?','_');
            //prefix = prefix.replace('*','%');
            //prefix += "%";

            //  note: table has collection ut8_ci. It is already unicode aware & case-insensitive
            //  note: 'distinct' is not needed b/c Player,Event,Site are already normalized

            if (thisQuery < queryCounter.get()) return null;    //  new query is underway
            conn = JoConnection.get();
            if (thisQuery < queryCounter.get()) return null;    //  new query is underway

            JoPreparedStatement pstm = sql.toPreparedStatement(conn);
            boolean ok = pstm.execute();
            assert(ok);
            if (!ok)
                return null;

            if (thisQuery < queryCounter.get())
                return null;    //  new query is underway

            ResultSet res = pstm.getResultSet();
            while(res.next()) {
                if (thisQuery < queryCounter.get()) return null;    //  new query is underway
                result.add(res.getString(1));
            }

        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            JoConnection.release(conn);
        }

        cleanCounter = queryCounter.incrementAndGet();
        assert(cleanCounter%2 == 0);
        return result;
    }

    @Override
    public boolean canComplete(String query) {
        /**
         *  skip completion if
         *      - query is empty
         *      - too few letters (->combinatorial explosion)
         *      - starts with *, or ?? (combinatorial explosion)
         *      - ends with * (can not delineate prefix from completion)
         */
        if (query.isEmpty() || CharUtil.countLettersAndDigits(query) <= minLetters) return false;
        if (query.startsWith("*") || query.startsWith("??")) return false;
        if (query.endsWith("*")) return false;
        return true;
    }

    @Override
    public int prefixLength(String query, String result)
    {
        if (!query.equals(lastQuery)) {
            lastQuery = query;
            glob = new GlobMatcher(SearchRecord.makeGlobxPattern(query,false),
                    false,false,true, GLOBX_WILDCARDS);
            /*  Note that GlobX matcher is strict about punctuation.
                Simple Glob and SQL LIKE is not.

                That means that an SQL query may return more results than
                the Auto-Completer would accept. That's a bit odd but not very much.
                @see AutoCompleteTextField.truncatePrefixes()
             */
        }

        int mat = glob.match(result);
        //  else
        return Math.max(0,mat);
    }

    //  todo constrain by Collection Ids (GameSource)
    //  todo color-insensitive Players
    private int minLetters = 2;   //  don't autocomplete on an empty string (it works, but is not intuitive)

    private String table, column;
    private boolean caseSensitive=false;
    private ParamStatement sql;
    private String lastQuery;
    private GlobMatcher glob;
    //private Pattern regex;
    private AtomicInteger queryCounter = new AtomicInteger(0);
}
