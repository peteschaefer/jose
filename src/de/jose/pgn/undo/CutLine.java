package de.jose.pgn.undo;

import de.jose.pgn.Game;
import de.jose.pgn.Node;
import de.jose.pgn.NodeSection;

public class CutLine extends NodeEdit
{
    NodeSection snippet;
    public CutLine(Node node) {
        super("cut",node);
        snippet = new NodeSection(node,node.parent().last());
    }
}
