package de.jose.pgn.undo;

import de.jose.pgn.Node;

public class DeleteText extends ModifyText
{
    public DeleteText(Node node, int pos1, int pos2) {
        super("delete", node);
        this.snippet = new StringBuffer();
        this.pos1 = pos1;
        snippet.append(getRelText(pos1,pos2));
    }

    public boolean conflateWith(Node nd, int pos3, int pos4) {
        if (nd!=node) return false;
        if (pos4!=pos1) return false;
        snippet.insert(0,getRelText(pos3,pos1));
        pos1 = pos3;
        return true;
    }
}
