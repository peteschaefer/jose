package de.jose.view.input;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class JoButton
        extends JButton implements MouseListener
{
    public JoButton() {
        addMouseListener(this);
    }

    public JoButton(Action a) {
        super(a);
        addMouseListener(this);
    }

    public JoButton(Icon icon) {
        super(icon);
        addMouseListener(this);
    }

    public JoButton(String text) {
        super(text);
        addMouseListener(this);
    }

    public JoButton(String text, Icon icon) {
        super(text, icon);
        addMouseListener(this);
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
