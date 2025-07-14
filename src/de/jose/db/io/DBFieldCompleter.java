package de.jose.db.io;

import de.jose.db.JoConnection;
import de.jose.db.JoPreparedStatement;
import de.jose.view.input.AutoCompleteTextField;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBFieldCompleter implements AutoCompleteTextField.Completer
{
    public DBFieldCompleter(String table, String column) {
        this.table = table;
        this.column = column;
    }

    @Override
    public List<String> getCompletions(String prefix, int limit) {
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

            conn = JoConnection.get();
            JoPreparedStatement pstm = new JoPreparedStatement(conn,sql);
            pstm.setString(1, prefix);
            pstm.execute();

            ResultSet res = pstm.getResultSet();
            while(res.next()) result.add(res.getString(1));

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            JoConnection.release(conn);
        }
        return result;
    }
    //  todo constrain by Collection Ids (GameSource)
    //  todo color-insensitive Players
    private String table, column;
}
