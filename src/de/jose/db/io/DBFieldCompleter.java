package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.view.input.AutoCompleteTextField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DBFieldCompleter implements AutoCompleteTextField.Completer
{
    public DBFieldCompleter(String table, String column) {
        this.table = table;
        this.column = column;
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
            prefix = prefix.replace('?','_');
            prefix = prefix.replace('*','%');
            prefix += "%";

            String sql = "select "+column+" from "+table+" where "+column+" like ?";
            if (limit > 0) sql += " limit "+(limit+1);
            //  note: table has collection ut8_ci. It is already unicode aware & case-insensitive
            //  note: 'distinct' is not needed b/c Player,Event,Site are already normalized

            if (thisQuery < queryCounter.get()) return null;    //  new query is underway
            conn = JoConnection.get();
            if (thisQuery < queryCounter.get()) return null;    //  new query is underway

            JoPreparedStatement pstm = new JoPreparedStatement(conn,sql);
            pstm.setString(1, prefix);
            pstm.execute();

            if (thisQuery < queryCounter.get()) return null;    //  new query is underway

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
    //  todo constrain by Collection Ids (GameSource)
    //  todo color-insensitive Players
    private String table, column;
    private AtomicInteger queryCounter = new AtomicInteger(0);
}
