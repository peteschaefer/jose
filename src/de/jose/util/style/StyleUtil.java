/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util.style;

import de.jose.Application;
import de.jose.Version;
import de.jose.image.Surface;
import de.jose.util.WinRegistry;
import de.jose.view.style.JoFontConstants;
import de.jose.Util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * StyleUtil
 *
 * @author Peter Sch�fer
 */

public class StyleUtil
{
    // ------------------------------------------------------------
     //  Styles Constants
     // ------------------------------------------------------------
     protected static StyleContext editStyles = new StyleContext();

     protected static Style empty = editStyles.addStyle("empty",null);
     protected static Style plain = editStyles.addStyle("plain",null);

     public static Style bold = editStyles.addStyle("bold",null);
     public static Style italic = editStyles.addStyle("italic",null);
     public static Style underline = editStyles.addStyle("underline",null);

     public static Style unbold = editStyles.addStyle("unbold",null);
     public static Style unitalic = editStyles.addStyle("unitalic",null);
     public static Style ununderline = editStyles.addStyle("ununderline",null);

     public static Style left = editStyles.addStyle("left",null);
     public static Style center = editStyles.addStyle("center",null);
     public static Style right = editStyles.addStyle("right",null);

     protected static Style color = editStyles.addStyle("color",null);
     protected static Style size = editStyles.addStyle("size",null);
     protected static Style scale = editStyles.addStyle("scale",null);

     static {
         StyleConstants.setBold(bold,true);
         StyleConstants.setBold(unbold,false);

         StyleConstants.setItalic(italic,true);
         StyleConstants.setItalic(unitalic,false);

         StyleConstants.setUnderline(underline,true);
         StyleConstants.setUnderline(ununderline,false);

         StyleConstants.setBold(plain,false);
         StyleConstants.setItalic(plain,false);
         StyleConstants.setUnderline(plain,false);

         StyleConstants.setAlignment(left,StyleConstants.ALIGN_LEFT);
         StyleConstants.setAlignment(center,StyleConstants.ALIGN_CENTER);
         StyleConstants.setAlignment(right,StyleConstants.ALIGN_RIGHT);
     }

     public static Style coloredStyle(Color color)
     {
         StyleConstants.setForeground(StyleUtil.color,color);
         return StyleUtil.color;
     }

    public static Style plainStyle(Color color, String family, int size)
    {
        if (color==null)
            StyleUtil.plain.removeAttribute(StyleConstants.Foreground);
        else
            StyleConstants.setForeground(StyleUtil.plain,color);
        if (family==null)
            StyleUtil.plain.removeAttribute(StyleConstants.FontFamily);
        else
            StyleConstants.setFontFamily(StyleUtil.plain,family);
        if (size <= 0)
            StyleUtil.plain.removeAttribute(StyleConstants.FontSize);
        else
            StyleConstants.setFontSize(StyleUtil.plain,size);
        return StyleUtil.plain;
    }

     public static Style sizedStyle(int size)
     {
         StyleConstants.setFontSize(StyleUtil.size,size);
         return StyleUtil.size;
     }

    public static Style sizedStyle(int size, float scale)
    {
        StyleConstants.setFontSize(StyleUtil.scale,size);
        JoFontConstants.setFontScaleFactor(StyleUtil.scale,scale);
        return StyleUtil.scale;
    }

    public static boolean differsSize(AttributeSet style, AttributeSet base)
    {
        return (StyleConstants.getFontSize(style) != StyleConstants.getFontSize(base));
    }

    public static boolean differsColor(AttributeSet style, AttributeSet base)
    {
        return !Util.equals(StyleConstants.getForeground(style), StyleConstants.getForeground(base));
    }

    public static boolean differsFamily(AttributeSet style, AttributeSet base)
    {
        return !Util.equals(StyleConstants.getFontFamily(style), StyleConstants.getFontFamily(base));
    }

    private static Color getWindowsSystemColor(String key)
    {
        int value = WinRegistry.getIntValue("HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\DWM",key);
        if (value != Integer.MIN_VALUE)
            return new Color(value);
        else
            return null;
    }

    public static Color getProfileAccentColor() {
        Surface sf = (Surface)Application.theUserProfile.get("lnf.accent.color");
        if (sf!=null)
            return sf.color;
        else
            return null;
    }

    public static Color getProfileSelectionColor() {
        Surface sf = (Surface)Application.theUserProfile.get("lnf.select.color");
        if (sf!=null)
            return sf.color;
        else
            return null;
    }

    public static Color getDefaultAccentColor() {
        return Color.decode("#2675BF");
    }

    public static Color getSystemAccentColor()
    {
        if (Version.windows) {
            //  get system accent color from registry
            return getWindowsSystemColor("AccentColor");
        }
        if (Version.mac) {
            //  todo does this work ?
            return SystemColor.activeCaption;
        }
        return null;
    }

    public static Color getSystemSelectionColor()
    {
        if (Version.windows) {
            //  get system accent color from registry
            return getWindowsSystemColor("ColorizationColor");
            //  not "AccentColor"
        }
        if (Version.linux) {
            //  getting a usable accent color from Linux is hopeless
            //  there *are* accent colors in Gnome and KDE, but there is no useful
            //  API to retrieve them. Linux Desktop is fucked, believe me.
            //  Use our own profile store instead.
            return null;
        }
        if (Version.mac) {
            //  todo does this work ?
            return SystemColor.textHighlight;
        }
        return null;
    }

    public static boolean getSystemDarkMode() {
         if (Version.windows) {
             int value = WinRegistry.getIntValue(
                "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize","AppsUseLightTheme");
             if (value != Integer.MIN_VALUE)
                 return (value==0);
         }
        return false;
    }

    private static int rgbDiff(Color a, Color b)
    {
        return Math.abs(a.getRed()-b.getRed())
                + Math.abs(a.getGreen()-b.getGreen())
                + Math.abs(a.getBlue()-b.getBlue());
    }

    public static Color contrast(Color from, Color a, Color b)
    {
        int da = rgbDiff(from,a);
        int db = rgbDiff(from,b);
        if (da < db)
            return b;
        else
            return a;
    }

    /**
     * @return true if a contiguous area has the same attribute (in this case: bold)
     */
    public static boolean isContiguous(StyledDocument doc, int from, int to, Object attrKey)
    {
        for (int i=from; i < to; )
        {
            Element celm = doc.getCharacterElement(i);
            AttributeSet cstyle = celm.getAttributes();

            Boolean value = (Boolean)cstyle.getAttribute(attrKey);
            if (value==null || !value.booleanValue())
                return false;

            i = celm.getEndOffset();
        }
        return true;
    }

      /**
       * get character style at one location
       * @param doc
       * @param pos
       * @return
       */
      public static AttributeSet getCharacterStyleAt(StyledDocument doc, int pos)
      {
          Element celm = doc.getCharacterElement(pos);
          if (celm!=null)
              return celm.getAttributes();
          else
              return null;
      }

      public static boolean hasDifferentCharacterAttributes(AttributeSet style, AttributeSet base)
      {
          return  (StyleConstants.isBold(style) != StyleConstants.isBold(base)) ||
                  (StyleConstants.isItalic(style) != StyleConstants.isItalic(base)) ||
                  (StyleConstants.isUnderline(style) != StyleConstants.isUnderline(base)) ||
                  (StyleConstants.getFontSize(style) != StyleConstants.getFontSize(base)) ||
                  (!StyleConstants.getForeground(style).equals(StyleConstants.getForeground(base)));
      }

    /**
     * @param acol
     * @return a color suitable for dark mode; with high contrast
     */
    public static Color mapDarkTextColor(Color acol)
    {
        if (acol.getRed()==acol.getGreen() && acol.getGreen()==acol.getBlue()) {
            //  grey colors get inverted
            int red = 255 - acol.getRed();
            return new Color(red,red,red);
        }
        else {
            //  we want high contrast:
            //  - bright, but not too bright
            //  - close to white = low saturation
            return pastelize(acol,0.3f);
        }
    }

    public static Color pastelize(Color col, float f) {
        //  this creates pastel-like colors; which suit dark-mode nicely
        float[] hsb = new float[4];
        Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), hsb);
        hsb[1] = f*hsb[1];   //  low saturation
        hsb[2] = 1.0f - (1.0f-hsb[2])*f; //  bright ?
        return Color.getHSBColor(hsb[0],hsb[1],hsb[2]);
    }

    public static Color invertColor(Color col) {
        if (col.getRed()==col.getGreen() && col.getGreen()==col.getBlue()) {
            //  grey colors get inverted
            int red = 255 - col.getRed();
            return new Color(red,red,red);
        }
        else {
            float[] hsb = new float[4];
            Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), hsb);
            //hsb[1] = f*hsb[1];   //  low saturation
            hsb[2] = 1.0f - hsb[2]; //  bright ?
            return Color.getHSBColor(hsb[0],hsb[1],hsb[2]);
        }
    }

    public static Color mapDarkIconColor(Color acol, float pastel)
    {
        if (acol.getRed()==acol.getGreen() && acol.getGreen()==acol.getBlue()) {
            //  grey colors get inverted
            int red = 255 - 64 - acol.getRed()/2;
            return new Color(red,red,red);
        }
        else {
           return pastelize(acol,pastel);
        }
    }

    public static Color getLinkColor() {
        Color col = Color.blue;
        if (Application.theApplication.isDarkLookAndFeel())
            return col = mapDarkTextColor(col);
        return col;
    }

}