package de.jose.view;

import javax.swing.*;
import java.awt.*;

public class RowLayout
    implements LayoutManager2
{
    private int rowWidth,rowHeight;

    public void setRowSize(int rowWidth,int rowHeight) {
        this.rowWidth = rowWidth;
        this.rowHeight = rowHeight;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {}

    @Override
    public float getLayoutAlignmentX(Container target) { return 0; }

    @Override
    public float getLayoutAlignmentY(Container target) { return 0; }

    @Override
    public void invalidateLayout(Container target) {}

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    interface ComponentVisitor {
        Dimension visit(Component c);
    }

    ComponentVisitor GetPreferredSize = new ComponentVisitor() {
        @Override
        public Dimension visit(Component c) {
            return c.getPreferredSize();
        }
    };
    ComponentVisitor GetMinimumSize = new ComponentVisitor() {
        @Override
        public Dimension visit(Component c) {
            return c.getMinimumSize();
        }
    };
    ComponentVisitor GetMaximumSize = new ComponentVisitor() {
        @Override
        public Dimension visit(Component c) {
            return c.getMaximumSize();
        }
    };
    ComponentVisitor GetBestSize = new ComponentVisitor() {
        @Override
        public Dimension visit(Component c) {
            Dimension d = c.getPreferredSize();
            Dimension d1 = c.getMinimumSize();
            Dimension d2 = c.getMaximumSize();
            return new Dimension(
                    Math.min(Math.max(d.width,d1.width),d2.width),
                    Math.min(Math.max(d.height,d1.height),d2.height));
        }
    };

    void placeNext(Rectangle r, Dimension d)
    {
        if (r.x+r.width+d.width > rowWidth)
            //  line break
            r.setBounds( 0, r.y+rowHeight, d.width, d.height);
        else
            //  continue line
            r.setBounds( r.x+r.width, r.y, d.width, d.height);
    }

    private Dimension forEach(Container target,
                              ComponentVisitor visitor, boolean update)
    {
        Rectangle r = new Rectangle(0,0,0,0);
        for(int i=0; i < target.getComponentCount(); i++) {
            Component c = target.getComponent(i);
            Dimension d2 = visitor.visit(c);
            placeNext(r,d2);
            if (update) c.setBounds(r);
        }
        return new Dimension(r.x+r.width,r.y+r.height);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return forEach(parent, GetPreferredSize, false);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return forEach(parent, GetMinimumSize, false);
    }

    @Override
    public Dimension maximumLayoutSize(Container parent) {
        return forEach(parent, GetMaximumSize, false);
    }

    @Override
    public void layoutContainer(Container parent) {
        Dimension d = forEach(parent, GetBestSize, true);
        parent.setSize(d.width, d.height);
    }
}
