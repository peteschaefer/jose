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


package de.jose.view.style;

import de.jose.Util;
import de.jose.util.AWTUtil;
import de.jose.util.FontUtil;
import de.jose.util.print.Triplet;

import javax.swing.text.*;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.CSS;
import java.util.*;
import java.util.List;
import java.awt.*;


/**
 *	extends StyleContext
 *  - it can create a clone copy
 *  - styles maintain a parent-child hierarchy
 *
 *
 * @author Peter Schäfer
 */
public class JoStyleContext
		extends StyleContext
        implements Cloneable
{
	static final long serialVersionUID = 9020773135093130756L;

	/** font scaling factor to adjust for screen resolution */
	private float fontScale = 1.0f;

	public JoStyleContext()
	{
		super();
		setNormFontScale();
	}

	public JoStyleContext(StyleContext copy)
	{
		this();
		copyFrom(copy);
	}

	public JoStyleContext copy()
	{
		return new JoStyleContext(this);
	}

	public Object clone()
	{
		return copy();
	}


	public float getFontScale()     	        { return fontScale; }

	public void setFontScale(float fontScale)   { this.fontScale = fontScale; }

	public void setNormFontScale()              { setFontScale((float)AWTUtil.getNormalizingTransform().getScaleX()); }

	public void setScreenResolution(float dpi)  { setFontScale(dpi/72.0f); }

	public Style addStyle(String nm, Style parent)
	{
		Style result = super.addStyle(nm,parent);
		if (parent != null) {
			ArrayList children;
			if (parent.isDefined("children"))
				children = (ArrayList)parent.getAttribute("children");
			else {
				children = new ArrayList();
				parent.addAttribute("children",children);
			}
			children.add(result);
		}
		return result;
	}

	public static Style getParent(Style style)
	{
		if (style.isDefined(StyleConstants.ResolveAttribute))
			return (Style)style.getAttribute(StyleConstants.ResolveAttribute);
		else
			return null;
	}

	public static boolean getBooleanAttribute(Style style, Object key)
	{
		Object value = style.getAttribute(key);
		if (value==null) return false;
		if (value instanceof Boolean) return ((Boolean)value).booleanValue();
		if (value instanceof String) return Boolean.valueOf((String)value).booleanValue();
		throw new IllegalArgumentException();
	}

	public static Object get1Attribute(AttributeSet style, Object key)
	{
		if (style.isDefined(key))
			return style.getAttribute(key);
		else
			return null;
	}

	public static List getChildren(Style style)
	{
		if (style.isDefined("children"))
			return (List)style.getAttribute("children");
		else
			return null;
	}

	public static int getNestLevel(Style style)
	{
		int result = 0;
		for ( ; style != null; style = getParent(style))
			result++;
		return result;
	}


	/**	get Styles, sorted by pre-order	*/
	public Style[] getStylesPreOrder()
	{
		ArrayList collect = new ArrayList();
		Style root = getStyle("base");
		collectStyles(root,collect);
		Style[] result = new Style[collect.size()];
		collect.toArray(result);
		return result;
	}

	protected void collectStyles(Style parent, ArrayList collect)
	{
		collect.add(parent);
		List children = getChildren(parent);
		if (children != null)
			for (int i=0; i<children.size(); i++) {
				Style child = (Style)children.get(i);
				collectStyles(child,collect);
			}
	}

	protected void clear()
	{
		Enumeration allStyles = getStyleNames();
		while (allStyles.hasMoreElements()) {
			String styleName = (String)allStyles.nextElement();
			removeStyle(styleName);
		}
	}

	public boolean useFigurineFont()
	{
		return useFigurineFont(getStyle("base"));
	}

	public void setFigurineFont(boolean useFont)
	{
		setFigurineFont(getStyle("base"),useFont);
	}


	public String getFigurineLanguage()
	{
		return getFigurineLanguage(getStyle("base"));
	}

	public void setFigurineLanguage(String lang)
	{
		setFigurineLanguage(getStyle("base"),lang);
	}


	public static boolean useFigurineFont(Style style)
	{
		return Util.toboolean(style.getAttribute("figurine.usefont"));
	}

	public static void setFigurineFont(Style style, boolean useFont)
	{
		((MutableAttributeSet)style).addAttribute("figurine.usefont", useFont?"true":"false");
	}


	public static String getFigurineLanguage(Style style)
	{
		return (String)style.getAttribute("figurine.language");
	}

	public static void setFigurineLanguage(Style style, String lang)
	{
		((MutableAttributeSet)style).addAttribute("figurine.language",lang);
	}


    /**
     * make sure that all custom fonts (symbols, figurines) are loaded
     */
    public void assertCustomFonts()
    {
        Style root = getStyle("base");
        assertFonts(root);
    }

    protected void assertFonts(Style root)
    {
        if (root.isDefined(StyleConstants.FontFamily)
            || root.isDefined(CSS.Attribute.FONT_FAMILY))
        {
            String family = JoFontConstants.getFontFamily(root);
	        boolean bold = JoFontConstants.isBold(root);
	        boolean italic = JoFontConstants.isBold(root);
            FontUtil.getCustomFont(family,bold,italic,true);
        }

        List children = getChildren(root);
		if (children != null)
			for (int i=0; i<children.size(); i++) {
				Style child = (Style)children.get(i);
                assertFonts(child);
            }
    }

	/**
	 * make sure that all custom fonts (symbols, figurines) are loaded
	 * Set<String>
	 */
	public Set collectFontInfo()
	{
	    Style root = getStyle("base");
		HashSet set = new HashSet();
	    collectFontInfo(root,set);
		return set;
	}

	protected void collectFontInfo(Style root, Set collect)
	{
		String family = JoFontConstants.getFontFamily(root);
		boolean bold = JoFontConstants.isBold(root);
		boolean italic = JoFontConstants.isItalic(root);

		collect.add(new Triplet(family,bold,italic));

	    List children = getChildren(root);
		if (children != null)
			for (int i=0; i<children.size(); i++) {
				Style child = (Style)children.get(i);
	            collectFontInfo(child,collect);
	        }
	}

	protected void copyFrom(StyleContext that)
	{
		copyTree((StyleContext.NamedStyle)that.getStyle("base"),null);

		if (that instanceof JoStyleContext)
			this.fontScale = ((JoStyleContext)that).fontScale;
	}

	private void clearAttributes(MutableAttributeSet style)
	{
		Enumeration attrs = style.getAttributeNames();
		while (attrs.hasMoreElements()) {
			Object key = attrs.nextElement();
			if (!style.isDefined(key)) continue;    //  skip inherited
			if (key.equals(StyleConstants.ResolveAttribute)) continue;  //  don't modify parent
			if (key.equals("children")) continue;   //  don't modify hierarchy
			style.removeAttribute(key);
		}
	}

	private void copyAttributes(StyleContext.NamedStyle fromNode, StyleContext.NamedStyle toNode)
	{
		//  copy all attributes
		//  except: inherited attributes, "chilren", "parent"

		NamedStyle copy = new NamedStyle();
		copy.addAttributes(fromNode);
		copy.removeAttribute(StyleConstants.ResolveAttribute);
		copy.removeAttribute("children");

		toNode.addAttributes(copy);
	}

	protected void copyTree(StyleContext.NamedStyle thatNode, Style thisParent)
	{
		if (thatNode==null) return;

		String name = thatNode.getName();
		StyleContext.NamedStyle thisNode = (StyleContext.NamedStyle)getStyle(name);
		if (thisNode==null)
			thisNode = (StyleContext.NamedStyle)addStyle(name,thisParent);
		else {
			clearAttributes(thisNode);
			thisNode.setResolveParent(thisParent);
		}

		copyAttributes(thatNode,thisNode);

		List children = getChildren(thatNode);
		if (children != null)
			for (int i=0; i < children.size(); i++) {
				StyleContext.NamedStyle child = (StyleContext.NamedStyle)children.get(i);
				copyTree(child,thisNode);
			}
	}

    public static Style getDefiningParent(Style style, Object attribute)
    {
        for (;;) {
			if (style.isDefined(attribute))
				return style;
			else {
				Style parent = (Style)style.getResolveParent();
				if (parent==null || parent==style)
					return null; //  done
				else
					style = parent;
			}
        }
    }

    public static Set getDefiningParents(Set styles, Object attribute)
    {
        Set result = new HashSet();
        Iterator i = styles.iterator();
        while (i.hasNext())
        {
            Style style = (Style)i.next();
            style = getDefiningParent(style,attribute);
            if (style!=null)
                result.add(style);
        }
        return result;
    }

	public void modifyFontSize(Style style, float factor, int min)
	{
		if (style==null) style = getStyle("base");
        style = getDefiningParent(style,StyleConstants.FontSize);        
        if (style==null) return;

		int baseFontSize = JoFontConstants.getFontSize(style);
		if (factor < 1.0f)
			factor = Math.min(factor, ((float)baseFontSize+min)/baseFontSize);
		else
			factor = Math.max(factor, ((float)baseFontSize+min)/baseFontSize);
		doModifyFontSize(style,factor,baseFontSize);
	}

	private void doModifyFontSize(Style style, float baseFactor, int baseFontSize)
	{
		/** is font size declared in THIS style ? */
		Integer thisValue = (Integer)get1Attribute(style,StyleConstants.FontSize);
		//  TODO use CSS.Attributes

		if (thisValue==null) {
            //  empty, or inherited from parent
        }
		else
        {
			int thisSize = thisValue.intValue();
			/** is there already a multiplication factor defined ?
			 *  if so - use it !
			 */
			float thisFactor = JoFontConstants.getFontScaleFactor(style);
			if (thisFactor <= 0) {
				thisFactor = (float)thisSize/baseFontSize;
				JoFontConstants.setFontScaleFactor(style,thisFactor);
			}

			int newValue = Math.round(thisFactor*baseFactor*baseFontSize);
			if (newValue < 1) newValue = 1;
			StyleConstants.setFontSize(style,newValue);
		}

		/** recurse to children */
		List children = getChildren(style);
		if (children!=null) {
			Iterator i = children.iterator();
			while (i.hasNext())
				doModifyFontSize((Style)i.next(), baseFactor, baseFontSize);
		}
	}

	/**
	 * get sclaed font for a specific screen resolution
	 *
	 * @param attr
	 * @param dpi
	 * @return
	 */
	public Font getFont(AttributeSet attr, float dpi)
	{
		// PENDING(prinz) add cache behavior
		int style = Font.PLAIN;
		if (JoFontConstants.isBold(attr))
		    style |= Font.BOLD;
		if (JoFontConstants.isItalic(attr))
		    style |= Font.ITALIC;
		String family = JoFontConstants.getFontFamily(attr);
		int size = JoFontConstants.getFontSize(attr);

		/**
		 * if either superscript or subscript is
		 * is set, we need to reduce the font size
		 * by 2.
		 */
		if (JoFontConstants.isSuperscript(attr) ||
			JoFontConstants.isSubscript(attr)) {
			size -= 2;
		}

		return getFont(family, style, size, dpi);
	}

	/**
	 * get sclaed font for a specific screen resolution
	 *
	 * @param dpi
	 * @return
	 */
	public Font getFont(String family, int style, float size, float dpi)
	{
		float scale = dpi/72.0f;
		return FontUtil.newFont(family, style, scale*size);
	}



	public Font getFont(String family, int style, float size)
	{
		if (fontScale <= 0.0f) fontScale = 1.0f;
		return FontUtil.newFont(family, style, fontScale*size);
	}

	public Font getFont(String family, int style, int size)
	{
		return getFont(family,style,(float)size);
	}

	public int getPixelSize(float ptsize)
	{
		return Math.round(ptsize * fontScale);
	}

	public int getPixelSize(AttributeSet set)
	{
		return getFont(set).getSize();
	}
}
