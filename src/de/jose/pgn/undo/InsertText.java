package de.jose.pgn.undo;

import de.jose.pgn.Node;

public class InsertText extends ModifyText
{
    public InsertText(Node node, int pos1, String text)
    {
        super("edit",node);
        this.pos1 = pos1;
        this.snippet = new StringBuffer(text);
    }

    public boolean conflateWith(Node nd, int pos3, String text) {
        if (nd!=node) return false;
        if (pos3!=(pos1+snippet.length())) return false;
        snippet.append(text);
        return true;
    }
}
