package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.db.ParamStatement;
import de.jose.pgn.SearchRecord;
import de.jose.util.GlobMatcher;
import de.jose.view.input.AutoCompleteTextField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.jose.pgn.SearchRecord.MYSQL_RLIKE_WILDCARDS;
import static de.jose.pgn.SearchRecord.POSIX_WILDCARDS;
import static de.jose.util.GlobMatcher.GLOB_WILDCARDS;
import static de.jose.util.GlobMatcher.SQL_WILDCARDS;
import static java.util.regex.Pattern.*;

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
    public List<String> getCompletions(String prefix, int limit)
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
            SearchRecord.appendNameSearchPattern(sql,table,column, prefix, MYSQL_RLIKE_WILDCARDS, caseSensitive);

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
    public int prefixLength(String query, String result)
    {
        if (!query.equals(lastQuery)) {
            lastQuery = query;
   //         glob = new GlobMatcher(SearchRecord.makeLikePattern(query,false),false,false,true, SQL_WILDCARDS);
            String regexStr = SearchRecord.makeRegexPattern(query,false,POSIX_WILDCARDS,caseSensitive);
            int flags = UNICODE_CHARACTER_CLASS;
            if (!caseSensitive)
                flags |= CASE_INSENSITIVE|UNICODE_CASE;
            regex = Pattern.compile(regexStr,flags);
        }

        Matcher mat = regex.matcher(result);
        if (mat.find() && mat.start() == 0)
           return mat.end();
        //  else
        return 0;
    }

    //  todo constrain by Collection Ids (GameSource)
    //  todo color-insensitive Players
    private String table, column;
    private boolean caseSensitive=false;
    private ParamStatement sql;
    private String lastQuery;
    //private GlobMatcher glob;
    private Pattern regex;
    private AtomicInteger queryCounter = new AtomicInteger(0);
}
