package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.LineNode;

public class SwapLines extends GameEdit {

    public SwapLines(LineNode line1, LineNode line2) {
        super(line1);
    }
}
