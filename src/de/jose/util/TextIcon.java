package de.jose.util;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

public class TextIcon implements Icon
{
    private String text;
    private Font font;
    private Color color;
    private float width,height,ascent;

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
        Font oldFont = g2.getFont();
        Color oldColor = g2.getColor();

        g2.setFont(font);
        g2.setColor(color);
        g2.drawString(text,x,y+ascent);

        g2.setFont(oldFont);
        g2.setColor(oldColor);
    }

    @Override
    public int getIconWidth() {
        return (int)(width+0.5);
    }

    @Override
    public int getIconHeight() {
        return (int)(height+0.5);
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
