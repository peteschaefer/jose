package de.jose.plugin;

import java.util.Objects;

/**
 * Contains the score of an engine PV
 */
public class Score
{
    /**	engine (current color) mates in ... moves (+plies to mate)	*/
    public static final int WHITE_MATES	    = +30000;
    /**	engine gets mated in ... moves (-plies to mate)	*/
    public static final int BLACK_MATES	    = -30000;
    /**	evaluation is not known	*/
    public static final int UNKNOWN		    = -36000;

    /** evaluation flags:   this evaluation is exact (default)    */
    public static final int EVAL_EXACT              = 0x00;
    /** evaluation flags:   this evaluation is a lower bound    */
    public static final int EVAL_LOWER_BOUND        = 0x01;
    /** evaluation flags:   this evaluation is an upper bound    */
    public static final int EVAL_UPPER_BOUND        = 0x02;
    /** evaluation flags:   this evaluation is book game count   */
    public static final int EVAL_GAME_COUNT         = 0x03;


    //  evaluation in centipawns. Encodes mate in ..., too. POV = White
    public int cp;
    //  POV = current
    public int cp_current;
    //  kind of score (exact, lower, upper bound)
    public int flags;
    //  win-draw-lose evaluation (optional)
    public int win,draw,lose;
    //  Leela moves-left
    public int moves_left;

    public Score() { clear(); }

    public Score(Score that) {
        cp = that.cp;
        cp_current = that.cp_current;
        flags = that.flags;
        win = that.win;
        draw = that.draw;
        lose = that.lose;
        moves_left = that.moves_left;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return cp == score.cp && flags == score.flags && win == score.win && draw == score.draw && lose == score.lose;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cp, flags, win, draw, lose);
    }

    public static boolean equals(Score s1, Score s2)
    {
        if (s1 == null) return (s2==null);
        if (s2 == null) return false;
        return s1.equals(s2);
    }

    public void copy(Score that) {
        this.cp = that.cp;
        this.cp_current = that.cp_current;
        this.flags = that.flags;
        this.win = that.win;
        this.draw = that.draw;
        this.lose = that.lose;
        this.moves_left = that.moves_left;
    }

    public void clear() {
        cp=cp_current=UNKNOWN;
        flags=EVAL_EXACT;
        win=draw=lose=0;
        moves_left=0;
    }

    public int sumWDL() {
        return win+draw+lose;
    }
    public boolean hasWDL() {
        return sumWDL() > 0;
    }

    public float rel(int val) {
        int sum = sumWDL();
        return (sum==0) ? 0.0f : (float)val/sum;
    }

    public void swapWDL() {
        int temp = win;
        win = lose;
        lose = temp;
    }
}
