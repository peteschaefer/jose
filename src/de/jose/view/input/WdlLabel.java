package de.jose.view.input;

import de.jose.plugin.Score;

import javax.swing.*;
import java.awt.*;

public class WdlLabel
    extends JoBigLabel
{
    private Score wdlScore;

    public WdlLabel(String text) {
        super(text);
    }

    public WdlLabel(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    public void setWdlScore(Score wdlScore) {
        this.wdlScore = wdlScore;
        //  provisional:
        if (wdlScore != null) {
            String text = getText();
            text += "\n" + wdlScore.wdlString(null);
            setText(text);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (wdlScore != null)
            paintScore(g);
    }

    protected void paintScore(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int txtHeight = getFontMetrics(getFont()).getHeight();

        int w1 = (int)(wdlScore.rel(wdlScore.win)*width+0.5);
        int w2 = (int)(wdlScore.rel(wdlScore.draw)*width+0.5);

        g.setColor(Color.white);
        g.fillRect(0, height-txtHeight, w1, txtHeight);
        g.setColor(Color.gray);
        g.fillRect(w1, height-txtHeight, w1+w2, txtHeight);
        g.setColor(Color.black);
        g.fillRect(w1+w2, height-txtHeight, width-w1-w2, txtHeight);
    }
}
