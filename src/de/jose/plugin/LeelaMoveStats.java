package de.jose.plugin;

import de.jose.util.StringUtil;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

/**
 * Move statistics for Leela
 * note that score and WDL is alreasy in Score
 * here go internal Leele variables
 */
public class LeelaMoveStats
{
    //  internal Node ID
    public int nodeId;
    public long N, Nplus;
    public double P, WL, D, M, Q, U, S, V;

    public static DecimalFormat PERCENT = new DecimalFormat("###.#");
    public static DecimalFormat NUM1 = new DecimalFormat("###.#;-###.#");
    public static DecimalFormat NUM2 = new DecimalFormat("###.##;-###.##");

    public LeelaMoveStats(String input) {
        parse(input);
    }

    String print() {
        StringBuffer buf = new StringBuffer();

        if (N>=0) {
            buf.append("N:");
            buf.append(StringUtil.formatLargeInt(N,NUM1));
        }
        if (Nplus>0) {
            buf.append("(+");
            buf.append(StringUtil.formatLargeInt(Nplus,NUM1));
            buf.append(")");
        }
        print(buf,"P:",P,PERCENT);
        print(buf,"WL:",WL,NUM2);
        print(buf,"D:",D,NUM2);
        print(buf,"M:",M,NUM1);
        print(buf,"Q:",Q,NUM2);
        print(buf,"U:",U,NUM2);
        print(buf,"S:",S,NUM2);
        print(buf,"V:",V,NUM2);
        return buf.toString();
    }


    public void parse(String input) {
        nodeId = (int)StringUtil.parseLong(input, "(",")");
        N = StringUtil.parseLong(input, "N:","(+");
        Nplus = StringUtil.parseLong(input, "(+",")");
        P = StringUtil.parseDouble(input, "P:","%");
        WL = StringUtil.parseDouble(input, "WL:",")");
        D = StringUtil.parseDouble(input, "D:",")");
        M = StringUtil.parseDouble(input, "M:",")");
        Q = StringUtil.parseDouble(input, "Q:",")");
        U = StringUtil.parseDouble(input, "U:",")");
        S = StringUtil.parseDouble(input, "S:",")");
        V = StringUtil.parseDouble(input, "V:",")");
    }

    public static String reformat(String input) {
        LeelaMoveStats stats = new LeelaMoveStats(input);
        return stats.print();
    }

    private static FieldPosition pos = new FieldPosition(0);

    private void print(StringBuffer buf, String label, double num, DecimalFormat format)
    {
        if (Double.isNaN(num)) return;
        buf.append(" ");
        buf.append(label);
        format.format(num,buf,pos);
    }
}
