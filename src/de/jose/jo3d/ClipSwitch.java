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

import javax.media.j3d.Group;
import javax.media.j3d.ModelClip;
import javax.media.j3d.Node;
import javax.media.j3d.Switch;
import javax.vecmath.Vector4d;
import java.util.BitSet;
import java.util.Vector;

/**
 *	a Switch node that deals with dynamic clipping and culling
 *
 *
 *	@author Peter Schäfer
 */

public class ClipSwitch
			extends Switch
{
	/**	currently enabled nodes	*/
	protected	BitSet	enabled;
	/**	currently visible nodes	*/
	protected	BitSet	visible;
	/**	associated clips (Vector of ModelClip)	*/
	protected 	Vector	clips;
	/**	associated board clip	*/
	protected	GlobalClip	globalClip;

	private static final boolean[] ENABLE_NONE	= { false,false,false,false,false,false };

	public ClipSwitch(GlobalClip global)
	{
		super(Switch.CHILD_MASK);
		setCapability(Switch.ALLOW_SWITCH_WRITE);

		enabled = new BitSet(8);
		visible = new BitSet(8);
		clips = new Vector(8);
		globalClip = global;

		setCapability(Switch.ALLOW_CHILDREN_READ);
		globalClip.addSwitch(this);
	}


	public void addChild(Node node)
	{
		Group grp = (Group)node;
		int idx = numChildren();
		super.addChild(grp);
		enabled.clear(idx);
		visible.clear(idx);

		ModelClip clip = globalClip.newClip(grp);
		clips.add(clip);
	}

	public ModelClip getModelClip(int idx)
	{
		return (ModelClip)clips.get(idx);
	}

	public void setEnable(int idx, boolean on)
	{
		if (enabled.get(idx) == on) return;	//	nothing changed
		if (on)
			enabled.set(idx);
		else
			enabled.clear(idx);

		update(idx);
	}

	public void setVisible(int idx, boolean on)
	{
		if (visible.get(idx) == on) return;	//	nothing changed
		if (on)
			visible.set(idx);
		else
			visible.clear(idx);
		setChildMask(visible);
	}

	public void update(int idx)
	{
		if (!enabled.get(idx) || globalClip.cullAll() || !isLive()) {
			setVisible(idx,false);
			return;	//	that was easy
		}
		//	else: check intersection
		Group child = (Group)getChild(idx);
		if (!child.isLive())
			return;	//	can't compute VWorld transform while not alive

		LocalBoundingBox bounds = (LocalBoundingBox)child.getBounds();

		ModelClip clip = (ModelClip)clips.get(idx);
		//	intersect with all clipping planes
		int planeCount = 0;
		for (int i=0; i<globalClip.countPlanes(); i++)
		{
			Vector4d plane = new Vector4d(globalClip.getPlane(i));

			switch (globalClip.intersect(bounds,plane))
			{
			case +1:	//	completely hidden by this plane -> cull
						setVisible(idx,false);
						clip.setEnables(ENABLE_NONE);
						return;
			case 0:		//	intersect; add clip plane
						clip.setPlane(i, plane);
						clip.setEnable(i,true);
						planeCount++;
						break;
			case -1:	//	completely visible -> no clipping
						clip.setEnable(i,false);
						break;
			}
		}
		setVisible(idx,true);
		if (planeCount > 2)
			System.out.println("Warning: more than 2 clip planes");
			//	will not work with all graphic cards; or will be excessively expensive
	}

	public void updateAll()
	{
		for (int i=0; i<numChildren(); i++)
			update(i);
	}
}
