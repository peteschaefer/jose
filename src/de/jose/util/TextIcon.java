package de.jose.util;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

public class TextIcon implements Icon
{
    protected String text;
    protected Font font;
    protected Color color;
    protected float width;
    protected  float height;
    protected float ascent;

    protected Paint savePaint;
    protected Font saveFont;

    public TextIcon(String text, Font font, Color color) {
        this.text = text;
        this.font = font;
        this.color = color;
        init();
    }

    public TextIcon(String text, Font font, float size, Color color) {
        this(text,font.deriveFont(Font.PLAIN,size),color);
    }

    public TextIcon(String text, Font font, int style, float size, Color color) {
        this(text,font.deriveFont(style,size),color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        save(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(color);
        g2.drawString(text,x,y+ascent);
        restore(g2);
    }

    @Override
    public int getIconWidth() {
        return (int)(width+0.5);
    }

    @Override
    public int getIconHeight() {
        return (int)(height+0.5);
    }

    protected void save(Graphics2D g2) {
        savePaint = g2.getPaint();
        saveFont = g2.getFont();
    }

    protected void restore(Graphics2D g2) {
        g2.setPaint(savePaint);
        g2.setFont(saveFont);
    }

    private static FontRenderContext frc = null;

    private void init()
    {
        if (frc==null)
            frc = new FontRenderContext(null,false,true);
        LineMetrics lm = font.getLineMetrics(text,frc);
        Rectangle2D stringBounds = font.getStringBounds(text, frc);
        ascent = lm.getAscent();
        width = (float)stringBounds.getWidth();
        height = (float)stringBounds.getHeight();
    }
}
