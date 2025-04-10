package de.jose.book;

import de.jose.pgn.PgnConstants;
import de.jose.pgn.PgnUtil;

import java.text.DecimalFormat;

public class GameRef
{
    public String white;
    public String black;
    public int year;
    public int result;

    private static DecimalFormat year2 = new DecimalFormat("''00");

    public String toString(boolean shortFmt)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(white, 0, shortFmt ? shortLen(white):white.length());
        sb.append("-");
        sb.append(black, 0, shortFmt ? shortLen(black):black.length());
        sb.append(" ");
        if (shortFmt)
            sb.append(year2.format(year%100));
        else
            sb.append(year);

        if (!shortFmt || result != PgnConstants.RESULT_UNKNOWN) {
            sb.append(" ");
            sb.append(PgnUtil.resultString(result));
        }
        return sb.toString();
    }

    private int shortLen(String str) {
        int i = str.indexOf(",");
        if (i >= 0)
            return i;
        else
            return str.length();
    }

    public String toString() {
        return toString(true);
    }
}
