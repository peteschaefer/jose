package de.jose.util.icon;

import de.jose.image.ImgUtil;

import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.awt.BasicStroke.CAP_ROUND;

public class TextShapeIcon extends TextIcon
{
    //private Shape glyph, strokedGlyph;
    private Shape[] mask;
    private Color bgColor;

    public TextShapeIcon(String text, Font font, float size,
                         Color color, Color bgColor)
    {
        super(text, font, size, color);
        this.bgColor = bgColor;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new TextShapeIcon(text,font,font.getSize(),color,bgColor);
    }

    @Override
    protected void init()
    {
        super.init();

        Shape glyph = ImgUtil.getOutline(font, text);

        int strokeWidth = (int) Math.round(font.getSize() / 6.0f);
        strokeWidth = Math.max(strokeWidth,1);
        Stroke str = new BasicStroke(strokeWidth,CAP_ROUND,BasicStroke.JOIN_ROUND);
        Shape strokedGlyph = str.createStrokedShape(glyph);
        mask = ImgUtil.getMask(strokedGlyph, true);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g;
        save(g2);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        AffineTransform tf1 = AffineTransform.getTranslateInstance(x+insets.left, y+insets.top+ascent);
        AffineTransform tf2 = g2.getTransform();
        tf1.preConcatenate(tf2);
        g2.setTransform(tf1);

        g2.setColor(bgColor);
        ImgUtil.fill(g2,mask);

        g2.setFont(font);
        g2.setColor(color);

        g2.drawString(text,0,0);
        restore(g2);
    }
}
