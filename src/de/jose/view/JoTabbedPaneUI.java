package de.jose.view;

import de.jose.util.ReflectionUtil;
import de.jose.Version;

import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.*;
import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * a TabbedPaneUI that does two things:
 * * handle right mouse clicks
 * * paint a close button in the upper right corner
 *
 * two methods are overridden, all others are passed on to the original UI
 *
 * @author Peter Schäfer
 */
public class JoTabbedPaneUI
        extends BasicTabbedPaneUI
{
    /** delegate to... */
    protected TabbedPaneUI delegate;

    public JoTabbedPaneUI(TabbedPaneUI delegate)
    {
        this.delegate = delegate;
    }

    // --------------------------------------------
    //  overridden methods
    // --------------------------------------------

    protected MouseListener createMouseListener()
    {
        try {
            MouseListener original = (MouseListener)ReflectionUtil.invoke(delegate,"createMouseListener");
            return new DelegateMouseListener(original);
        } catch (Exception e) {
            return super.createMouseListener();
        }
    }

    protected void installDefaults()
    {
        Insets contentBorderInsets;
        if (!Version.mac)
        {
            contentBorderInsets = UIManager.getInsets("TabbedPane.contentBorderInsets");
//            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,-15));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));

            super.installDefaults();

            UIManager.put("TabbedPane.contentBorderInsets", contentBorderInsets);
        }
        else
            super.installDefaults();
    }

    // --------------------------------------------
    //  delegated methods
    // --------------------------------------------


    public int tabForCoordinate(JTabbedPane pane, int x, int y) {
        return delegate.tabForCoordinate(pane, x, y);
    }

    public Rectangle getTabBounds(JTabbedPane pane, int index) {
        return delegate.getTabBounds(pane, index);
    }

    public int getTabRunCount(JTabbedPane pane) {
        return delegate.getTabRunCount(pane);
    }

    public void installUI(JComponent c) {
        delegate.installUI(c);
    }

    public void uninstallUI(JComponent c) {
        delegate.uninstallUI(c);
    }

    public void paint(Graphics g, JComponent c) {
        delegate.paint(g, c);
    }

    public void update(Graphics g, JComponent c) {
        delegate.update(g, c);
    }

    public Dimension getPreferredSize(JComponent c) {
        return delegate.getPreferredSize(c);
    }

    public Dimension getMinimumSize(JComponent c) {
        return delegate.getMinimumSize(c);
    }

    public Dimension getMaximumSize(JComponent c) {
        return delegate.getMaximumSize(c);
    }

    public boolean contains(JComponent c, int x, int y) {
        return delegate.contains(c, x, y);
    }

    public int getAccessibleChildrenCount(JComponent c) {
        return delegate.getAccessibleChildrenCount(c);
    }

    public Accessible getAccessibleChild(JComponent c, int i) {
        return delegate.getAccessibleChild(c, i);
    }


    // --------------------------------------------
    //  delegated MouseListener
    // --------------------------------------------

      static class DelegateMouseListener extends MouseAdapter
      {
          protected MouseListener delegate;

          DelegateMouseListener(MouseListener delegate) {
              this.delegate = delegate;
          }

          public void mousePressed(MouseEvent e) {
              if (!ContextMenu.isTrigger(e))
                  delegate.mousePressed(e);
          }
      }

}
