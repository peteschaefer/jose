package de.jose.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RowLayout
    implements LayoutManager2
{
    private int rowWidth;//rowHeight;
    private Rectangle current = new Rectangle();
    private Dimension max = new Dimension();

    public void setRowSize(int rowWidth) {
        this.rowWidth = rowWidth;
        //this.rowHeight = rowHeight;
    }

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

    void placeNext(Dimension d)
    {
        if (current.x+current.width+d.width > rowWidth)
            //  line break
            current.setBounds( 0, max.height, d.width, d.height);
        else
            //  continue line
            current.setBounds( current.x+current.width, current.y, d.width, d.height);
    }

    private Dimension forEach(Container target,
                              ComponentVisitor visitor, boolean update)
    {
        current.setBounds(0, 0, 0,0);
        max.width = max.height = 0;
        for(int i=0; i < target.getComponentCount(); i++) {
            Component c = target.getComponent(i);
            if (!c.isVisible()) continue;

            Dimension d2 = visitor.visit(c);
            placeNext(d2);
            max.width = Math.max(max.width, current.x+current.width);
            max.height = Math.max(max.height, current.y+current.height);
            if (update) c.setBounds(current);
        }
        return max;
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


    @Override
    public void addLayoutComponent(Component comp, Object constraints) {}

    @Override
    public float getLayoutAlignmentX(Container target) { return 0; }

    @Override
    public float getLayoutAlignmentY(Container target) { return 0; }

    @Override
    public void invalidateLayout(Container target) {
        layoutContainer(target);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

}
