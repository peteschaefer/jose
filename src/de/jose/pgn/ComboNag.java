package de.jose.pgn;

import de.jose.Language;

import java.io.File;
import java.io.IOException;

public class ComboNag
{
    protected static final String[] wb = {"White","Black"};
    protected static final String[] grd1 = {"slight","moderate","decisive","crushing"};
    protected static final String[] grd2 = {"slight","moderate","decisive" };
    protected static final String[] lasting = {"the","a lasting" };
    protected static final String[] suff = {"insufficient","sufficient","more than adequate" };
    protected static final String[] side = {"center","kingside","queenside" };
    protected static final String[] poor = {"poor","good" };
    protected static final String[] poorly = {"poorly","well" };
    protected static final String[] verypoor = {"very poor","poor","good","very good" };
    protected static final String[] placed = {"protected","placed" };
    protected static final String[] vulnerable = {"vulnerable","well protected" };
    protected static final String[] officer = {"knight","bishop","rook","queen" };
    protected static final String[] moderate = {"moderate","severe" };
    protected static final String[] weak = {"very weak","moderately weak","moderately strong","very strong" };
    protected static final String[] phase = {"opening","middle","end" };
    protected static final String[] spacetime = {"space","time (development)" };
    protected static final String[] empty = new String[0];

    //  "composable nags"
    // [14..21] white/black has a slight/moderate/decisive/crushing advantage
    protected static ComboNag advantage1    = new ComboNag(14, "has a", grd1, "advantage" );
    // [22..23] white/black is in Zugzwang
    protected static ComboNag zugzwang      = new ComboNag(22, "is in", "zugzwang" );
    // [24..35] w/b has slight/moderate/decisive space/time advantage
    protected static ComboNag advantage2    = new ComboNag(24, "has a", grd2, spacetime, "advantage" );
    //  [36..41] w/b has the/a lasting initiative/attack
    protected static ComboNag initiative    = new ComboNag(36, "has", lasting, "initiative" );
    protected static ComboNag attack        = new ComboNag(40, "has the", "attack" );
    //  [42..47] w/b has insufficient/sufficient/more than adequate compentsion for material deficit
    protected static ComboNag compensation  = new ComboNag(42,  "has", suff, "compensation for material deficit" );
    //  [48..65] w/b has slight/moderate/decisive center/kingside/queenside control advantage
    protected static ComboNag control       = new ComboNag(48, "has a", grd2, side, "control advantage" );
    //   [66..69] w/b has a vulnerable/well protected first rank
    protected static ComboNag rank          = new ComboNag(66,"has a", vulnerable, "first rank" );
    //  [70..77] w/b has a poorly/well protected/placed king
    protected static ComboNag king          = new ComboNag(70, "has a", poorly, placed, "king" );
    //  [78..85] w/b has very/moderately weak/strong pawn structure
    protected static ComboNag pawns         = new ComboNag(78, "has a", weak, "pawn structure" );
    //  [86..101] w/b has poor/good knight/bishop/rook/queen placement
    protected static ComboNag placement     = new ComboNag(86, "has", poor, officer, "placement" );
    //  [102..104] w/b has poor/good piece coordination
    protected static ComboNag coordination  = new ComboNag(102, "has", poor, "coordination" );
    //  [106..129] w/b has played the opening/middlegame/ending (very) poorly/well
    protected static ComboNag play          = new ComboNag(106, "has played a", verypoor, phase, "game" );
    //  [130..135] w/b has slight/moderate/decisive counterplay
    protected static ComboNag counterplay   = new ComboNag(130,"has", grd2, "counterplay" );
    //  [136..139] w/b has moderate/severe time control pressure
    protected static ComboNag timecontrol   = new ComboNag(136,"has", moderate, "time control pressure" );

    protected static AdvantageNag advantage = new AdvantageNag();

    //  [14..139]
    public static ComboNag[] ALL = {
            advantage, /*advantage1,*/
            zugzwang, /*advantage2,*/ initiative,
            attack, compensation, /*control,*/ rank, king, pawns, placement,
            play, counterplay, timecontrol
    };

    //  --------------------- fields ----------------

    private final int base;
    private final String[] color;
    public final String verb;
    public final String[] adjective;
    public final String[] subst;
    public final String selector;
    //  current selection
    protected int selcol = 0;
    protected int seladj = 0;
    protected int selsubst = 0;

    public ComboNag(int base, String verb, String selector) {
        this(base, verb, null, null, selector);
    }

    public ComboNag(int base, String verb, String[] adjective, String selector) {
        this(base, verb, adjective, null, selector);
    }

    public ComboNag(int abase, String averb, String[] anadjective, String[] asubst, String aselector) {
        this.base = abase;
        this.color = wb;
        this.verb = averb;
        this.adjective = (anadjective != null) ? anadjective : empty;
        this.subst = (asubst != null) ? asubst : empty;
        this.selector = aselector;
    }

    void select(int col, int adj, int subst) {
        assert (col < color.length);
        assert (adj < adjective.length);
        assert (subst < this.subst.length);
        selcol = col;
        seladj = adj;
        selsubst = subst;
    }

    int code() {
        return base
                + selsubst * adjective.length * color.length
                + seladj * color.length
                + selcol;
    }

    int maxCode() {
        return base
                + subst.length * adjective.length * color.length
                + adjective.length * color.length
                + color.length;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(color[selcol]);  //  "white/black"
        buf.append(" ");
        buf.append(verb);
        buf.append(" ");//  "has a"
        if (adjective.length > 0 && !adjective[seladj].isEmpty()) {
            buf.append(adjective[seladj]);  //  "slight/moderate/decisive"
            buf.append(" ");
        }
        if (subst.length > 0 && !subst[selsubst].isEmpty()) {
            buf.append(subst[selsubst]);    //  "king side/queen side"
            buf.append(" ");
        }
        buf.append(selector);              //   "advantage"
        return buf.toString();
    }

    public static void main(String[] args) throws IOException
    {
        Language.setLanguage(new File("config"),"en");

        for(ComboNag cn : ALL) {
            for(int s3=0; s3 < Math.max(1,cn.subst.length); s3++) {
                for(int s2=0; s2 < Math.max(1,cn.adjective.length); s2++) {
                    for(int s1=0; s1 < cn.color.length; s1++) {
                        cn.select(s1,s2,s3);
                        System.out.print(cn.code());
                        System.out.print(" : \t");
                        System.out.println(cn.toString());

                        System.out.print("\t\t");
                        System.out.println(Language.get("pgn.nag."+cn.code()));
                        System.out.print("\t\t");
                        System.out.println(Language.get("pgn.nag."+cn.code()+".tip"));
                    }
                }
            }
        }
    }
}
// [14..21] white/black has a slight/moderate/decisive/crushing advantage
// ComboNag advantage1    = new ComboNag(14, "has a", grd1, "advantage" );

// [24..35] w/b has slight/moderate/decisive space/time advantage
// ComboNag advantage2    = new ComboNag(24, "has a", grd2, spacetime, "advantage" );

//  [48..65] w/b has slight/moderate/decisive center/kingside/queenside control advantage
// ComboNag control       = new ComboNag(48, "has a", grd2, side, "control advantage" );

class AdvantageNag extends ComboNag
{
    private static final String[] type = {
            "",
            "space", "time (development)",
            "center control", "kingside control", "queenside control" };
    //  combines advantage1, advantage2, control
    AdvantageNag()
    {
        super(0, "has a", grd1, type, "advantage");
    }

    @Override
    void select(int col, int adj, int subst) {
        //  "crushing" is only available for advantage1
        if (subst >= 1)
            adj = Math.min(adj, grd2.length-1);

        super.select(col, adj, subst);
    }

    @Override
    int code() {
        switch(selsubst) {
            case 0:
                advantage1.select(selcol,seladj,0);
                return advantage1.code();
            case 1:
            case 2:
                advantage2.select(selcol,seladj,selsubst-1);
                return advantage2.code();
            default:
                control.select(selcol,seladj,selsubst-3);
                return control.code();
        }
    }

    @Override
    int maxCode() {
        return control.maxCode();
    }

}
