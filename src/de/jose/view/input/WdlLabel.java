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

    private static final int ALPHA = 255;
    private static final Color WHITE = new Color(255, 255, 255, ALPHA);
    private static final Color GREY = new Color(128, 128, 128, ALPHA);
    private static final Color BLACK = new Color(0, 0, 0, ALPHA);

    protected void paintScore(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int txtHeight = getFontMetrics(getFont()).getHeight();
        int barHeight = txtHeight;
        int arc = 4;

        int w1 = (int)(wdlScore.rel(wdlScore.win)*width+0.5);
        int w2 = (int)(wdlScore.rel(wdlScore.draw)*width+0.5);
        int w3 = width-w1-w2;
        int y = Math.min(height-barHeight, 2*txtHeight+4);

        g.setColor(WHITE);
        g.fillRect(0, y, w1, barHeight);
        g.setColor(GREY);
        g.fillRect(w1, y, w2, barHeight);
        g.setColor(BLACK);
        g.fillRect(w1+w2, y, w3, barHeight);
    }
}
