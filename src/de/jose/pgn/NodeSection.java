package de.jose.pgn;

/**
 * A section of consecutive nodes.
 * Contrary to LineNode, does not take parentship of those nodes.
 */
public class NodeSection
{
    protected Node firstNode;
    protected Node lastNode;

    public Node first() { return firstNode; }
    public Node last() { return lastNode; }

    public void setFirst(Node firstNode) { this.firstNode = firstNode; }
    public void setLast(Node lastNode) { this.lastNode = lastNode; }

    public LineNode parent() {
        assert(first().parent()==last().parent());
        return first().parent();
    }

    private void setParent(LineNode p) {
        for(Node n = first(); ; ) {
            n.setParent(p);
            if (n==last()) break;
            n = n.next();
        }
    }

    public Node previous() { return first().previous(); }
    public Node next() { return last().next(); }

    public NodeSection(Node first, Node last) {
        assert(first.parent()==last.parent());
        setFirst(first);
        setLast(last);
    }

    public int getLength() {
        int len=0;
        for(Node n=first(); ; ) {
            len += n.getLength();
            if (n==last()) break;
            n = n.next();
        }
        return len;
    }

    public void trim(int type) {
        while(first()!=last() && first().type==type) setFirst(first().next());
        while(last()!=first() && last().type==type) setLast(last().previous());
    }

    public void swap(NodeSection that) {
        if (next()==that.first()) {
            that.remove();
            that.insertBefore(this.first());
        }
        else if (that.next()==this.first()) {
            that.remove();
            that.insertAfter(this.last());
        }
        else {
            Node a = this.previous();
            Node b = this.next();
            Node c = that.previous();
            Node d = that.next();

            that.insertBetween(a,b);
            this.insertBetween(c,d);
        }
    }

    public void append(NodeSection that) {
        that.insertAfter(this.last());
    }

    public void remove() {
        Node a = previous();
        Node b = next();

        LineNode p = parent();
        if (p!=null) {
            p.setLength(p.getLength()-this.getLength());
            if (p.first()==this.first())
                p.setFirst(b);
            if (p.last()==this.last())
                p.setLast(a);
        }

        if (a!=null) a.setNext(b);
        if (b!=null) b.setPrevious(a);

        first().setPrevious(null);
        last().setNext(null);
        setParent(null);
    }

    public void insertBefore(Node that)
    {
        Node a = that.previous();

        if (a!=null) a.setNext(first());
        first().setPrevious(a);

        last().setNext(that);
        that.setPrevious(last());

        LineNode p = that.parent();
        setParent(p);
        if (p!=null)
            p.setLength(p.getLength() + this.getLength());
        if (a==null)
            p.setFirst(first());
    }

    public void insertAfter(Node that)
    {
        Node b = that.next();

        that.setNext(first());
        first().setPrevious(that);

        if (b!=null) b.setPrevious(last());
        last().setNext(b);

        LineNode p = that.parent();
        setParent(p);
        if (p!=null)
            p.setLength(p.getLength() + this.getLength());
        if (b==null)
            p.setLast(last());
    }

    private void insertBetween(Node a, Node b) {
        assert(a==null || b==null || a.parent()==b.parent());
        assert(a==null || b==null || a.next()==b && a==b.previous());

        if (a!=null)
            this.insertAfter(a);
        else if (b!=null)
            this.insertBefore(b);
        else {
            first().setPrevious(null);
            last().setNext(null);
        }
    }
}
