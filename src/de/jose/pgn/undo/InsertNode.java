package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

public class InsertNode extends NodeEdit {

    public InsertNode(Game game, Node node) {
        super(game,node);
    }
}
