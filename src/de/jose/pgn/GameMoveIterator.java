package de.jose.pgn;

import java.util.Stack;

import static de.jose.pgn.INodeConstants.LINE_NODE;
import static de.jose.pgn.INodeConstants.MOVE_NODE;

/**
 * Iterates the MoveNodes of a Game
 */
public class GameMoveIterator
{
    private LineNode root;
    private MoveNode current = null;
    private Stack<LineNode> stack = new Stack<LineNode>();

    public GameMoveIterator(LineNode root) {
        this.root = root;
    }

    public MoveNode next()
    {
        Node n;
        if (current == null)
            n = root.first(MOVE_NODE,LINE_NODE);
        else
            n = current.next(MOVE_NODE,LINE_NODE);

        for(;;)
        {
            if (n==null) {
                if (stack.isEmpty())
                    return current=null;
                n = stack.pop().next(MOVE_NODE,LINE_NODE);
            }
            else if (n.is(MOVE_NODE))
                return current = (MoveNode)n;
            else {
                stack.push((LineNode) n);
                n = ((LineNode) n).first(MOVE_NODE, LINE_NODE);
            }
        }
    }
}
