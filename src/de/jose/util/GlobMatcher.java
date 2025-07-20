package de.jose.util;

public class GlobMatcher
{
    public GlobMatcher(String pattern, boolean caseSensitive, boolean accentSensitive) {
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.accentSensitive = accentSensitive;
    }

    public int match(String input) {
        return match(pattern,input);
    }

    private boolean caseSensitive=false;
    private boolean accentSensitive=false;
    private String pattern;

    private static int match_max(int m1, int m2) {
        if (m1 < 0) return m2;
        return Math.max(m1, m2);
    }
    private int plus_one(int m1) {
        if (m1 < 0) return m1;
        return m1 + 1;
    }

    private int match(CharSequence p0, CharSequence i0) {
        if (p0.length() == 0)
            return -i0.length();
        if (i0.length() == 0)
            return 0;

        char cp = p0.charAt(0);
        CharSequence p1 = p0.subSequence(1, p0.length());
        CharSequence i1 = i0.subSequence(1, i0.length());
        switch (cp) {
            case '*':   return match_max( plus_one(match(p0,i1)), match(p1,i0) );
            case '?':   return plus_one(match(p1, i1));
        }
        //  else match 1 character
        char ci = i0.charAt(0);
        if (!caseSensitive) {
            cp = CharUtil.toUpperCase(cp);
            ci = CharUtil.toUpperCase(ci);
        }
        if (!accentSensitive) {
            cp = CharUtil.stripDiacritic(cp);
            ci = CharUtil.stripDiacritic(ci);
        }
        if (cp != ci)
            return 0;
        else
            return plus_one(match(p1,i1));
    //  todo unroll tail-recursion
    }
}
