package de.jose.util;

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

    public ButtonIcon(String text, Font font, int size) {
        super(text, font, size*0.65f, Color.black);
        this.size = size;
        steps = new float[] { 0.0f, 0.3f, 0.6f, 0.8f, 1.0f };
        bg = new Color[] {
                white(1.0f),white(0.95f), white(0.95f), white(0.9f), white(0.85f)
        };
    }

    @Override
    public int getIconHeight() {
        return size+insets.top+insets.bottom;
    }

    @Override
    public int getIconWidth() {
        return size+insets.left+insets.right;
    }

    public ButtonIcon fixedColor(Color color)
    {
        super.color=color;
        fg = new Color[] {
                super.color,super.color,super.color,super.color,super.color
        };
        return this;
    }

    public ButtonIcon huedColor(Color color) {
        super.color = color;
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        fg = new Color[]{
                Color.getHSBColor(hsb[0],hsb[1],hsb[2]*1.1f),
                Color.getHSBColor(hsb[0],hsb[1],hsb[2]*1.0f),
                Color.getHSBColor(hsb[0],hsb[1],hsb[2]*0.95f),
                Color.getHSBColor(hsb[0],hsb[1],hsb[2]*0.9f),
                Color.getHSBColor(hsb[0],hsb[1],hsb[2]*0.8f),
        };
        return this;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g;
        //  draw background
        //  filled circle with radial gradient; off-center focus
        Point2D center = new Point2D.Float(x+insets.left+size/2, y+insets.top+size/2);
        Point2D focus = new Point2D.Float(x+insets.left+size/3, y+insets.top+size/3);
        RadialGradientPaint p1 = new RadialGradientPaint(
                center, size/2, focus, steps, bg, MultipleGradientPaint.CycleMethod.NO_CYCLE);

        save(g2);
        g2.setPaint(p1);
        g2.fillOval(x+insets.left, y+insets.top, size, size);

        //  draw text; centered
        Paint p2;
        if (fg!=null)
            p2 = new RadialGradientPaint(
                center, size/2, center, steps, fg, MultipleGradientPaint.CycleMethod.NO_CYCLE);
        else
            p2 = super.color;
        g2.setPaint(p2);

        g2.setFont(super.font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.drawString(super.text,
                x+insets.left+(size-super.width)/2,
                y+insets.top+(size-super.height)/2+super.ascent);
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
}
