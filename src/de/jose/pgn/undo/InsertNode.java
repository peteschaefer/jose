package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;

public class InsertNode extends NodeEdit
{
    public InsertNode(Node node) {
        super("insert",node);
    }
}
