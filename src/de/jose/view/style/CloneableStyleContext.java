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

import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.util.Enumeration;


/**
 * a StyleContext that can be cloned
 *
 *
 * @author Peter Schäfer
 */
public class CloneableStyleContext
		extends StyleContext
		implements Cloneable
{
	static final long serialVersionUID = 9020773135093130756L;

	public Object clone()
	{
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			//	must not be thrown, or can it ?
			throw new RuntimeException("unexpected CloneNotSupportedException");
		}
	}


	protected void copyFrom(StyleContext that)
	{
		Enumeration en = that.getStyleNames();
		while (en.hasMoreElements()) {
			String styleName = (String)en.nextElement();
			Style thisStyle = this.getStyle(styleName);
			Style thatStyle = that.getStyle(styleName);

			this.addAttributes(thisStyle,thatStyle);
		}
	}
}
