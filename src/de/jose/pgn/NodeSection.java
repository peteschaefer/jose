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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (first()!=null) sb.append(first().toString());
        sb.append("..");
        if (last()!=null) sb.append(last().toString());
        sb.append("]");
        return sb.toString();
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
        while(first()!=last() && first().is(type)) setFirst(first().next());
        while(last()!=first() && last().is(type)) setLast(last().previous());
    }

    public NodeSection trim(int type1, int type2) {
        trimLeft(type1,type2);
        trimRight(type1,type2);
        return this;
    }

    public NodeSection trimLeft(int type1, int type2) {
        while(first()!=last() && (first().is(type1) || first().is(type2))) setFirst(first().next());
        while(last()!=first() && (last().is(type1) || last().is(type2))) setLast(last().previous());
        return this;
    }

    public NodeSection trimRight(int type1, int type2) {
        while(last()!=first() && (last().is(type1) || last().is(type2))) setLast(last().previous());
        return this;
    }

    public void swap(NodeSection that) {
        Node b = this.next();
        Node d = that.next();

        if (b==that.first()) {
            that.remove();
            that.insertBefore(this.first());
        }
        else if (d==this.first()) {
            that.remove();
            that.insertAfter(this.last());
        }
        else {
            Node a = this.previous();
            Node c = that.previous();

            this.remove();
            that.remove();

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
            p.subLength(this.getLength());
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
            p.addLength(this.getLength());
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
            p.addLength(this.getLength());
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
