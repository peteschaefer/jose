/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Schäfer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.image;

import de.jose.util.AWTUtil;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * a Surface is a kind of serializable representation of a Paint
 * i.e. it holds either
 * - a Color
 * - a Texture
 * - a Gradient
 *
 * @author Peter Schäfer
 */
public class Surface	
		implements Serializable, Cloneable
{
	static final long serialVersionUID = 372903138495649079L;

	/** paint with plain Color  */
    public static final byte COLOR       = 1;
    /** paint with texture  */
    public static final byte TEXTURE     = 2;
    /** paint with gradient colors  */
    public static final byte GRADIENT    = 3;

    /** one of the above */
	public byte 	mode;
    /** plain color, and first gradient color */
	public Color	color;
    /** texture file name */
	public String	texture;
    /** second gradient color   */
	public Color    gradientColor;
    /** reverse gradient colors ? */
    public boolean  reversed;
    /** gradient coordinates (relative to 0..1)   */
    public float    x1,y1, x2,y2;
    /** repeat gradient */
    public boolean  cyclic;

    public Surface()
    {
        this(COLOR,null,null);
    }

    public Surface(byte aMode, Color aColor, String aTexture)
    {
        mode = aMode;
        color = aColor;
        texture = aTexture;
        gradientColor = null;
        x1 = y1 = 0.0f;
        x2 = y2 = 1.0f;
        cyclic = false;
    }

    public static Surface newColor(Color color)                               { return new Surface(COLOR,color,null); }

    public static Surface newColor(Color color, String texture)               { return new Surface(COLOR,color,texture); }


    public static Surface newColor(int r, int g, int b)                       { return new Surface(COLOR,new Color(r,g,b),null); }

    public static Surface newColor(int r, int g, int b, String texture)       { return new Surface(COLOR,new Color(r,g,b),texture); }


    public static Surface newTexture(String texture)                          { return new Surface(TEXTURE,null,texture); }

    public static Surface newTexture(Color color, String texture)             { return new Surface(TEXTURE,color,texture); }

    public static Surface newTexture(int r, int g, int b, String texture)     { return new Surface(TEXTURE,new Color(r,g,b),texture); }

	public static Surface newGradient(Color c1, Color c2)
	{
		Surface result = new Surface(GRADIENT,c1,null);
		result.gradientColor = c2;
		return result;
	}


    public boolean useTexture()     { return mode == TEXTURE; }

	public final Surface copy()
	{
		try {
			return (Surface)clone();
		} catch (CloneNotSupportedException cnsex) {
			//	not thrown at all
			throw new IllegalStateException();
		}
	}

    public GradientPaint getGradientPaint(float offx, float offy, float scalex, float scaley)
    {
        if (reversed)
            return new GradientPaint(offx+x1*scalex,offy+y1*scaley, gradientColor,
                                offx+x2*scalex,offy+y2*scaley, color, cyclic);
        else
            return new GradientPaint(offx+x1*scalex,offy+y1*scaley, color,
                                offx+x2*scalex,offy+y2*scaley, gradientColor, cyclic);
    }

	public TexturePaint getTexturePaint(float offx, float offy, float scalex, float scaley)
	{
		BufferedImage img = TextureCache.getTexture(texture,TextureCache.LEVEL_MAX);
		Rectangle2D anchor = new Rectangle2D.Float(offx,offy, scalex, scaley);
		return new TexturePaint(img,anchor);
	}

	public Paint getPaint(float offx, float offy, float scalex, float scaley)
	{
		switch (mode) {
		case COLOR:		return color;
		case GRADIENT:	return getGradientPaint(offx,offy,scalex,scaley);
		case TEXTURE:	return getTexturePaint(offx,offy,scalex,scaley);
		}
		return null;
	}

	public Paint getPaint(float scalex, float scaley)
	{
		return getPaint(0f,0f, scalex,scaley);
	}

	public boolean isDark()
	{
		switch (mode)
        {
        case TEXTURE:
                return TextureCache.isDark(texture, TextureCache.LEVEL_MIN);
        default:
        case COLOR:
                return ImgUtil.isDark(color);
        case GRADIENT:
                return ImgUtil.isDark(color) || ImgUtil.isDark(gradientColor);
        }
	}
 
	public boolean equals(Object obj)
	{
		if (obj==null) return false;
		if (! (obj instanceof Surface)) return false;

		Surface s = (Surface)obj;
		if (mode != s.mode) return false;

		if (color==null)
		{
			if (s.color!=null) return false;
		}
		else
		{
			if (! color.equals(s.color)) return false;
		}

		if (texture==null)
		{
			if (s.texture!=null) return false;
		}
		else
		{
			if (! texture.equals(s.texture)) return false;
		}

        if (gradientColor==null)
        {
            if (s.gradientColor!=null) return false;
        }
        else
        {
            if (! gradientColor.equals(s.gradientColor)) return false;
        }

        return (x1==s.x1) && (y1==s.y1) && (x2==s.x2) && (y2==s.y2) && (reversed==s.reversed);
	}

    public String hashString()
    {
        StringBuffer buf = new StringBuffer();
        appendHashString(buf);
        return buf.toString();
    }

    public void appendHashString(StringBuffer buf)
    {
        buf.append(mode);
        switch (mode) {
        case COLOR:
                AWTUtil.appendHexColor(buf,color); break;
        case TEXTURE:
                buf.append(texture); break;
        case GRADIENT:
                buf.append(x1);
                buf.append(",");
                buf.append(y1);
                buf.append(",");
                AWTUtil.appendHexColor(buf, reversed ? gradientColor:color);
                buf.append(",");
                buf.append(x2);
                buf.append(",");
                buf.append(y2);
                buf.append(",");
                AWTUtil.appendHexColor(buf, reversed ? color:gradientColor);
                buf.append(",");
                buf.append(cyclic);
                break;
        }
    }


}
