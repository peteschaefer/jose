package de.jose.view.style;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

public class JoSimpleAttributeSet extends SimpleAttributeSet
{
    //@Serial
    private static final long serialVersionUID = -6631553454711782652L;

    public JoSimpleAttributeSet(AttributeSet source) {
        super(source);
    }

    @Override
    public Object getAttribute(Object name) {
        if (name.equals(StyleConstants.Foreground))
        {
            Color color = (Color)super.getAttribute(name);
            Boolean dark = (Boolean)getAttribute("dark.mode");
            boolean isDark = Boolean.TRUE.equals(dark);
            if (isDark)
                return invert(color);
            else
                return color;
        }
        return super.getAttribute(name);
    }

    static float[] hsb = new float[4];

    public static Color invert(Color color)
    {
        if (isGrey(color))
            return new Color(255-color.getRed(), 255-color.getGreen(), 255-color.getBlue());
        else {
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
            hsb[1] = 1.0f-hsb[1];
            hsb[2] = 1.0f-hsb[2];
            return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        }
    }

    public static boolean isGrey(Color color)
    {
        return (color.getRed() == color.getGreen()) && (color.getGreen() == color.getBlue());
    }
}
