package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.LineNode;

public class SwapLines extends NodeEdit {

    private LineNode line2;

    public SwapLines(Game game, LineNode line1, LineNode line2) {
        super(game,line1);
        this.line2 = line2;
    }
}
