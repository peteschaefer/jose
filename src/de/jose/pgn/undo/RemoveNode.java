package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

public class RemoveNode extends GameEdit {

    public RemoveNode(Node node) {
        super(node);
    }
}
