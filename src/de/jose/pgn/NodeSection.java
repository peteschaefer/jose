package de.jose.pgn;

public class NodeSection
{
    public Node first,last;

    public NodeSection(Node first, Node last) {
        this.first = first;
        this.last = last;
    }

    public void trim(int type) {
        while(first!=last && first.type==type) first = first.nextNode;
        while(last!=first && last.type==type) last = last.previousNode;
    }

    public void swap(NodeSection that) {
        //  todo
    }
    public void append(NodeSection that) {
        //  todo
    }
}
