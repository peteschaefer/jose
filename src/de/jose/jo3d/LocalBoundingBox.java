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

package de.jose.jo3d;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;

/**
 *	a Bounds object with additional info about its local coordinate space
 *
 *	@author Peter Schäfer
 */

public class LocalBoundingBox
		extends BoundingBox
{
	/**	the local -> VWorld transformation	*/
	protected boolean		isTransient;
	protected Node 			localNode;
	protected Transform3D	localToVWorld;

	public LocalBoundingBox(Node node, Point3d lower, Point3d upper)
	{
		super(lower,upper);
		setLocalToVWorld(node);
	}

	public LocalBoundingBox(Node node, BoundingBox copy)
	{
		super();
		Point3d p = new Point3d();
		copy.getLower(p);
		setLower(p);
		copy.getUpper(p);
		setUpper(p);
		setLocalToVWorld(node);
	}

	/**	the local -> VWorld transformation	*/
	public Transform3D getLocalToVWorld()
	{
		if (!localNode.isLive())
			return null;	//	can't determine transform if not live
		if (isTransient)
			localNode.getLocalToVworld(localToVWorld);
		return localToVWorld;
	}

	/**	the local -> VWorld transformation	*/
	public void setLocalToVWorld(Transform3D alocalToVWorld)
	{
		localToVWorld = alocalToVWorld;
		localNode = null;
		isTransient = false;
	}


	/**	the local -> VWorld transformation	*/
	public void setLocalToVWorld(Node node)
	{
		localToVWorld = new Transform3D();
		localNode = node;
		isTransient = true;
	}
}
