package de.jose.util;

import ch.randelshofer.quaqua.colorchooser.JColorWheel;
import de.jose.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class ButtonIcon
    extends TextIcon
{
    private int size;

    public Color[] bg;
    public Color[] fg;
    public float[] steps;

    public ButtonIcon(String text, Font font, int size, Color color) {
        super(text, font, size*0.65f, color);
        this.size = size;
        steps = new float[] { 0.0f, 0.3f, 0.6f, 0.8f, 1.0f };
        bg = new Color[] {
                white(1.0f),white(0.95f), white(0.95f), white(0.9f), white(0.85f)
        };
        float h = Color.RGBtoHSB(super.color.getRed(), super.color.getGreen(), super.color.getBlue(), null)[0];
        fg = new Color[] {
               hue(h,1.0f), hue(h,0.95f), hue(h,0.9f), hue(h,0.85f), hue(h,0.8f)
        };
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g;
        //  draw background
        //  filled circle with radial gradient; off-center focus
        Point2D center = new Point2D.Float(x+size/2, y+size/2);
        Point2D focus = new Point2D.Float(x+size/3, y+size/3);
        RadialGradientPaint p1 = new RadialGradientPaint(
                center, size/2, focus, steps, bg, MultipleGradientPaint.CycleMethod.NO_CYCLE);

        save(g2);
        g2.setPaint(p1);
        g2.fillOval(x, y, size, size);

        //  draw text; centered
        RadialGradientPaint p2 = new RadialGradientPaint(
                center, size/2, center, steps, fg, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g2.setPaint(p2);

        g2.setFont(super.font);
        g2.drawString(super.text,
                x+(size-super.width)/2,
                y+(size-super.height)/2+super.ascent);
        restore(g2);
    }

    public ButtonIcon pushed() {
        Util.swap(bg,0,4);
        Util.swap(bg,1,3);
        return this;
    }

    public ButtonIcon hilited() {
        fg[0] = brighter(fg[0],1.5f);
        fg[1] = brighter(fg[1],1.3f);
        fg[2] = brighter(fg[2],1.1f);
        return this;
    }

    protected static Color white(float f)
    {
        int v = (int)(255*f);
        return new Color(v,v,v);
    }

    protected static Color hue(float h, float b)
    {
        float s = 1.0f;
        return Color.getHSBColor(h,s,b);
    }

    protected static Color brighter(Color c, float fact)
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
}
