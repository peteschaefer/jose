package de.jose.plugin;

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


    //  evaluation in centipawns. Encodes mate in ..., too.
    public int cp;
    //  kind of score (exact, lower, upper bound)
    public int flags;
    //  win-draw-lose evaluation (optional)
    public int win,draw,lose;

    public Score() { clear(); }

    public void copy(Score that) {
        this.cp = that.cp;
        this.flags = that.flags;
        this.win = that.win;
        this.draw = that.draw;
        this.lose = that.lose;
    }

    public void clear() {
        cp=UNKNOWN;
        flags=EVAL_EXACT;
        win=draw=lose=0;
    }

    public int sumWDL() {
        return win+draw+lose;
    }

    public boolean hasWDL() {
        return sumWDL() > 0;
    }
}
