/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch?fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util.print;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.Serializable;

/**
 * a PageFormat that can be serialized, e.g. stored in the user profile.
 *
 * @author Peter Schäfer
 */

public class SerializablePageFormat
		extends PageFormat
		implements Serializable, Cloneable
{
	static final long serialVersionUID = 1352166305288507145L;

	public SerializablePageFormat(PageFormat that)
	{
		this.setOrientation(that.getOrientation());
		this.setPaper(new SerializablePaper(that.getPaper()));
	}

	public Object clone()
	{
		return new SerializablePageFormat(this);
	}

	private void writeObject(java.io.ObjectOutputStream out)
		 throws IOException
	{
		out.writeInt(getOrientation());
		out.writeObject(getPaper());
	}

	private void readObject(java.io.ObjectInputStream in)
		 throws IOException, ClassNotFoundException
	{
		int orientation = in.readInt();
		Paper paper = (Paper)in.readObject();

		setOrientation(orientation);
		setPaper(paper);
	}

	public boolean equals(Object that)
	{
		if (this==that)
			return true;
		else if (that instanceof PageFormat)
			return equals(this, (PageFormat)that);
		else
			return false;
	}

	public int hashCode()
	{
		return  (int)Double.doubleToRawLongBits(getWidth()) ^
		        (int)Double.doubleToRawLongBits(getHeight()) ^
		        (int)Double.doubleToRawLongBits(getImageableX()) ^
		        (int)Double.doubleToRawLongBits(getImageableY()) ^
		        (int)Double.doubleToRawLongBits(getImageableWidth()) ^
		        (int)Double.doubleToRawLongBits(getImageableHeight()) ^
				getOrientation();
	}

	public static boolean equals(PageFormat f1, PageFormat f2)
	{
		return  (f1.getOrientation()==f2.getOrientation()) &&
		        (f1.getWidth()==f2.getWidth()) &&
		        (f1.getHeight()==f2.getHeight()) &&
		        (f1.getImageableX()==f2.getImageableX()) &&
		        (f1.getImageableY()==f2.getImageableY()) &&
		        (f1.getImageableHeight()==f2.getImageableHeight()) &&
		        (f1.getImageableWidth()==f2.getImageableWidth());
	}
}
