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

import java.awt.print.Paper;
import java.io.IOException;
import java.io.Serializable;

/**
 * a paper format that can be serialized (e.g. stored in the user profile)
 *
 * @author Peter Schäfer
 */

public class SerializablePaper
		extends Paper
		implements Serializable, Cloneable
{
	static final long serialVersionUID = 1276485211878415687L;

	public SerializablePaper(Paper that)
	{
		this.setSize(that.getWidth(),that.getHeight());
		this.setImageableArea(
				that.getImageableX(),that.getImageableY(),
				that.getImageableWidth(),that.getImageableHeight());
	}

	public Object clone()
	{
		return new SerializablePaper(this);
	}

	private void writeObject(java.io.ObjectOutputStream out)
		 throws IOException
	{
		out.writeDouble(getWidth());
		out.writeDouble(getHeight());
		out.writeDouble(getImageableX());
		out.writeDouble(getImageableY());
		out.writeDouble(getImageableWidth());
		out.writeDouble(getImageableHeight());
	}

	private void readObject(java.io.ObjectInputStream in)
		 throws IOException
	{
		double width = in.readDouble();
		double height = in.readDouble();
		double imgX = in.readDouble();
		double imgY = in.readDouble();
		double imgWidth = in.readDouble();
		double imgHeight = in.readDouble();

		setSize(width,height);
		setImageableArea(imgX,imgY, imgWidth,imgHeight);
	}

}
