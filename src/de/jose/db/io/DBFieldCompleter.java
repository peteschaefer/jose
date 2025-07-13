package de.jose.db.io;

import de.jose.view.input.AutoCompleteTextField;

import java.util.List;

public class DBFieldCompleter implements AutoCompleteTextField.Completer
{
    @Override
    public List<String> findTexts(String prefix, int limit) {
    //  todo select distinct field from table where field like 'prefix%'
        return List.of();
    }
    //  todo constrain by Collection Ids (GameSource)
    //  todo with, w/out color sensitive Players
    // todo ignore case
}
