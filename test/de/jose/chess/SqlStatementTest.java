package de.jose.chess;

import de.jose.db.ParamStatement;
import de.jose.pgn.SearchRecord;
import de.jose.util.GlobMatcher;
import org.junit.jupiter.api.Test;

import static de.jose.util.GlobMatcher.GLOB_WILDCARDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlStatementTest
{
    static String makeCondition(String pattern, boolean caseSensitive)
    {
        ParamStatement sql = new ParamStatement();
        SearchRecord.appendNameSearchPattern(sql,"Player","Name",pattern,caseSensitive);
        if (caseSensitive)
            assertEquals(" BINARY Player.Name LIKE BIANRY ? ",sql.where.toString());
        else
            assertEquals(" Player.Name LIKE ? ",sql.where.toString());
        return (String)sql.getParameter(1);
    }

    @Test
    void testNameLikePatterns()
    {
        //  assemble LIKE pattern from input.
        //  Rules are simple:
        //      ? becomes _
        //      * becomes %
        //      punctuation and whitespace become %_
        //
        //  Note that punction and whitespace patterns also matches regular character,
        //  i.e. we many find too many results.
        //  But we are tolerant about the number of whitespaces (which is more important)
        //
        //  We could use RLIKE for more exact matching, but RLIKE loses accent-insensitivity
        //  (which we do want to preserve, b/c it's useful).
        //
        //  Later mysql version have _ai (accent-insensitive) collations.
        //  Once these become available, RLIKE would become interesting again.

        assertEquals("Sch�fer_%P_%",makeCondition("Sch�fer,P.",false));
        assertEquals("Sch�fe_%P_%",makeCondition("Sch�fe?,P.",false));
        assertEquals("Sch�f%_P_%",makeCondition("Sch�f*,P.",false));
        assertEquals("Sch_fer_%P_%",makeCondition("Sch?fer,P.",false));
        assertEquals("Sch%fer_%P_%",makeCondition("Sch*fer,P.",false));
        assertEquals("Sch%_fer_%P_%",makeCondition("Sch*?fer,P.",false));
        assertEquals("Sch%_fer_%P_%",makeCondition("Sch*?*fer,P.",false));

        //  always have a % at the end
        assertEquals("Sch�fer_%P%",makeCondition("Sch�fer,P",false));
    }

    @Test
    void testGlobMatcher () {
        GlobMatcher gl = new GlobMatcher("*a?c*d",false,false, true, GLOB_WILDCARDS);

        assertEquals(4, gl.match("abcde"));
        assertEquals(4, gl.match("Abcde"));
        assertEquals(4, gl.match("�bcde"));
        assertEquals(7, gl.match("xxabcyde"));
    }
}
