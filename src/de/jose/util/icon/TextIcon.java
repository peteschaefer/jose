package de.jose.util.icon;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class TextIcon implements Icon, Cloneable
{
    protected String text;
    protected Font font;

    protected Color color;
    protected float width;
    protected  float height;
    protected float ascent;
    protected Insets insets = new Insets(0,0,0,0);

    protected Paint savePaint;
    protected Font saveFont;
    protected AffineTransform saveTransform;

    public TextIcon(String text, Font font, Color color) {
        this.text = text;
        this.font = font;
        this.color = color;
        init();
    }

    public TextIcon(String text, Font font, float size, Color color) {
        this(text,font.deriveFont(size),color);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static TextIcon clone(TextIcon icon) {
        try {
            return (TextIcon)icon.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new TextIcon(this.text, this.font, this.color);
    }

    protected static Color white(float f)
    {
        int v = (int)(255*f);
        return new Color(v,v,v);
    }

    public static Color brighter(Color c, float fact)
    {
/*        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hsb[2] = Math.min(1.0f,hsb[2]*fact);
        return Color.getHSBColor(hsb[0],hsb[1],hsb[2]);
 */     int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        red = Math.min((int)(red*fact),255);
        green = Math.min((int)(green*fact),255);
        blue = Math.min((int)(blue*fact),255);
        return new Color(red,green,blue);
    }

    public TextIcon setInsets(Insets insets) {
        this.insets = insets;
        return this;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        save(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(font);
        g2.setColor(color);
        g2.drawString(text,x+ insets.left,y+insets.top+ascent);
        restore(g2);
    }

    @Override
    public int getIconWidth() {
        return (int)(width+insets.left+insets.right+0.5);
    }

    @Override
    public int getIconHeight() {
        return (int)(height+insets.top+insets.bottom+0.5);
    }

    public TextIcon pushed() {
        color = brighter(color,0.5f);
        return this;
    }

    public TextIcon hilited() {
        color = brighter(color,1.5f);
        return this;
    }

    public TextIcon disabled() {
        color = Color.lightGray;
        return this;
    }

    protected void save(Graphics2D g2) {
        savePaint = g2.getPaint();
        saveFont = g2.getFont();
        saveTransform = g2.getTransform();
    }

    protected void restore(Graphics2D g2) {
        g2.setPaint(savePaint);
        g2.setFont(saveFont);
        g2.setTransform(saveTransform);
    }

    private static FontRenderContext frc = null;

    protected void init()
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
