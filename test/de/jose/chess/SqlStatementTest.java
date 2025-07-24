package de.jose.chess;

import de.jose.db.ParamStatement;
import de.jose.pgn.SearchRecord;
import de.jose.util.GlobMatcher;
import org.junit.jupiter.api.Test;

import static de.jose.util.GlobMatcher.GLOB_WILDCARDS;
import static de.jose.util.GlobMatcher.SQL_WILDCARDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SqlStatementTest
{
    static void assertSearchPatterns(String likePattern, String regexPattern, String pattern, boolean caseSensitive)
    {
        ParamStatement sql = new ParamStatement();
        SearchRecord.appendNameSearchPattern(sql,"Player","Name",pattern, caseSensitive);
//        if (caseSensitive)
//            assertEquals(" BINARY Player.Name LIKE BIANRY ? ",sql.where.toString());
//        else
//            assertEquals(" Player.Name LIKE ? ",sql.where.toString());

        if (likePattern != null)
            assertEquals(likePattern, (String)sql.getParameter(1));
        if (regexPattern != null)
            assertEquals(regexPattern, (String)sql.getParameter(2));
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

        assertSearchPatterns("Sch\u00e4fer_%P_%",   "Sch\\wfer\\W+P\\W+.*",     "Sch\u00e4fer,P.",false); //  \u00e4 = a umlaut, ä
        assertSearchPatterns("Sch\u00e4fe_%P_%",    "Sch\\wfe\\w\\W+P\\W+.*",   "Sch\u00e4fe?,P.",false);
        assertSearchPatterns("Sch\u00e4f%_P_%",     "Sch\\wf\\w*\\W+P\\W+.*",   "Sch\u00e4f*,P.",false);
        assertSearchPatterns("Sch_fer_%P_%",        "Sch\\wfer\\W+P\\W+.*",     "Sch?fer,P.",false);
        assertSearchPatterns("Sch%fer_%P_%",        "Sch\\w*fer\\W+P\\W+.*",    "Sch*fer,P.",false);
        assertSearchPatterns("Sch%_fer_%P_%",       "Sch\\w*\\wfer\\W+P\\W+.*", "Sch*?fer,P.",false);
        assertSearchPatterns("Sch%_fer_%P_%",       "Sch\\w*\\w\\w*fer\\W+P\\W+.*","Sch*?*fer,P.",false);

        //  always have a % at the end
        assertSearchPatterns("Sch\u00e4fer_%P%","Sch\\wfer\\W+P.*", "Sch\u00e4fer,P",false);

        //  not always have a % at the end
        assertEquals("Schaefer_",SearchRecord.makeLikePattern("Schaefer,",false));
    }

    int globMatch(String pattern, String input) {
        GlobMatcher gl = new GlobMatcher(pattern,false,false, true, SQL_WILDCARDS);
        return gl.match(input);
    }

    @Test
    void testGlobMatcher () {
        GlobMatcher gl = new GlobMatcher("*a?c*d",false,false, true, GLOB_WILDCARDS);

        assertEquals(4, gl.match("abcde"));
        assertEquals(4, gl.match("Abcde"));
        assertEquals(4, gl.match("\u00e4bcde"));
        assertEquals(7, gl.match("xxabcyde"));

        gl = new GlobMatcher("sc",false,false, true, GLOB_WILDCARDS);
        assertEquals(2, gl.match("Sc"));
        assertEquals(2, gl.match("Sch"));
        assertEquals(2, gl.match("Scacco,Mauro"));

        assertEquals(12, globMatch("Sc%","Scacco,Mauro"));
        assertEquals( 9, globMatch("Schaefer_","Schaefer,"));
        assertEquals( 2, globMatch("_%"," L"));
        assertEquals(10, globMatch("Schaefer_%","Schaefer L"));

        assertEquals( 9, globMatch("Schaefer_","Schaefers"));
    }
}
