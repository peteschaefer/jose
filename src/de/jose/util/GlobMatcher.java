package de.jose.util;

public class GlobMatcher
{
    public static char[] GLOB_WILDCARDS = {'?','*'};    //  out-of-the-box Glob matcher
    public static char[] GLOBX_WILDCARDS = {'?','*','.'};   //  Glob + punctuation wildcard
    public static char[] SQL_WILDCARDS = {'_','%'}; //  SQL LIKE matcher (identical to Glob, only different wildcard characters)

    public GlobMatcher(String pattern, boolean caseSensitive, boolean accentSensitive, boolean greedy, char[] wildcards) {
        this.pattern = pattern;
        this.caseSensitive = caseSensitive;
        this.accentSensitive = accentSensitive;
        this.greedy = greedy;
        this.wildcards = wildcards;
    }

    public int match(String input) {
        return match(pattern,input);
    }

    private boolean caseSensitive=false;
    private boolean accentSensitive=false;
    private boolean greedy=false;
    private char[] wildcards = GLOB_WILDCARDS;
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
            return 0;

        char cp = p0.charAt(0);
        CharSequence p1 = p0.subSequence(1, p0.length());
        if (cp==wildcards[1]) {
            //  match *
            int m1 = -1;
            if (i0.length() > 0) {
                //  * matches 1 or more chars
                CharSequence i1 = i0.subSequence(1, i0.length());
                m1 = mplus(match(p0, i1), 1);
            }

            //  * matches 0 chars
            int m2 = match(p1, i0);

            return greedy ? mmax(m1, m2) : mmin(m1, m2);
        }

        if (wildcards.length >= 3 && cp==wildcards[2]) {
            //  match . = at least one punctuation+
            if (i0.length() > 0 && !Character.isLetterOrDigit(i0.charAt(0))) {
                int q1=1;
                if (greedy) {
                    while(q1 < i0.length() && !Character.isLetterOrDigit(i0.charAt(q1)))
                        q1++;
                }
                CharSequence i1 = i0.subSequence(q1,i0.length());
                return mplus(match(p1,i1),q1);
            }
        }

        //  else: at least one char match
        if (i0.length() == 0)
            return -p0.length();

        assert(i0.length() > 0);
        CharSequence i1 = i0.subSequence(1, i0.length());

        if (cp==wildcards[0]) {
            //  match ? arbitrary char
            return mplus(match(p1, i1),1);
        }

        //  else match 1 character
        char ci = i0.charAt(0);
        if (!accentSensitive) {
            cp = TextUtil.stripDiacritics(cp);
            ci = TextUtil.stripDiacritics(ci);
        }
        if (!caseSensitive) {
            cp = Character.toUpperCase(cp);
            ci = Character.toUpperCase(ci);
        }
        if (cp != ci)
            return -i0.length();
        else
            return mplus(match(p1,i1),1);
    //  todo unroll tail-recursion?
    }
}
