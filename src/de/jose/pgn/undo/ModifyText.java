package de.jose.pgn.undo;

import de.jose.pgn.Node;

public abstract class ModifyText extends NodeEdit
{
    StringBuffer snippet;
    int pos1;

    public ModifyText(String name, Node node) {
        super(name, node);
    }
}
