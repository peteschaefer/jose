package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

public class CutLine extends NodeEdit {

    public CutLine(Game game, Node node) {
        super(game,node);
    }
}
