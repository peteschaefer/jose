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
    private boolean greedy=false;
    private String pattern;

    private static int mmax(int m1, int m2) {
        if (m1 < 0) return m2;
        if (m2 < 0) return m1;
        return Math.max(m1, m2);
    }
    private static int mmin(int m1, int m2) {
        if (m1 < 0) return m2;
        if (m2 < 0) return m1;
        return Math.min(m1, m2);
    }
    private static int mplus(int m1,int i) {
        if (m1 < 0) return m1;
        return m1 + i;
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
            case '*':
                int m1 = mplus(match(p0,i1),1);
                int m2 = match(p1,i0);
                return greedy? mmax(m1,m2) : mmin(m1,m2);
            case '?':
                return mplus(match(p1, i1),1);
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
            return -i0.length();
        else
            return mplus(match(p1,i1),1);
    //  todo unroll tail-recursion?
    }
}
