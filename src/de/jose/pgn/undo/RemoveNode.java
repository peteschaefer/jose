package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

public class RemoveNode extends NodeEdit {

    public RemoveNode(Game game, Node node) {
        super(game, node);
    }
}
