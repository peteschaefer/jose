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

import de.jose.view.BoardView3D;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Group;
import javax.media.j3d.ModelClip;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import java.util.Vector;

/**
 *
 *	@author Peter Schäfer
 */

public class GlobalClip
		extends Group
		implements ICallbackListener
{
//	public static final double size = 4*PieceGroup.squareSize*BoardView3D.scaleFactor;
	public static final double size = 1140*BoardView3D.scaleFactor;

    public static final boolean[] ENABLE_NONE = { false,false,false,false,false,false, };

	private Vector4d[]	eqns;
	private boolean		cullAll;

	/**	Vector of ClipSwitches	*/
	private Vector		switches;
	/**	number of pooled ModelClips that are currently in use	*/
	private int 		poolCount;

	public GlobalClip(int poolSize)
	{
		super();
		eqns	= new Vector4d[4];
		cullAll	= false;
		switches = new Vector();

		setCapability(Group.ALLOW_CHILDREN_READ);

		//	create a pool of modelclips
		while (poolSize-- > 0) {
			ModelClip clip = new ModelClip();
			clip.setCapability(ModelClip.ALLOW_ENABLE_WRITE);
			clip.setCapability(ModelClip.ALLOW_PLANE_WRITE);
			clip.setCapability(ModelClip.ALLOW_SCOPE_WRITE);
			clip.setCapability(ModelClip.ALLOW_INFLUENCING_BOUNDS_WRITE);
			clip.setEnables(ENABLE_NONE);
			addChild(clip);
		}
		poolCount = 0;
	}

	public boolean cullAll()			{ return cullAll; }

	public int countPlanes()			{ return eqns.length; }

	public Vector4d getPlane(int i)		{ return eqns[i]; }

	public int numSwitches()			{ return switches.size(); }

	public void addSwitch(ClipSwitch node)		{
		switches.add(node);
	}

	public ModelClip newClip(Group scope)
	{
		ModelClip clip = (ModelClip)getChild(poolCount++);
		clip.addScope(scope);
		clip.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), 5000));
		return clip;
	}

	public void clearClips()
	{
		while (poolCount > 0) {
			ModelClip clip = (ModelClip)getChild(--poolCount);
			clip.removeAllScopes();
			clip.setEnables(ENABLE_NONE);
		}
	}

/*
	public void addClip(ModelClip clip)
	{
		//	can only add BranchGroups to live scene graphs !
		BranchGroup branch = new BranchGroup();
		branch.addChild(clip);
		addChild(branch);
	}
*/
	public ClipSwitch getSwitch(int i) {
		return (ClipSwitch)switches.get(i);
	}

	public int intersect(LocalBoundingBox bounds, Vector4d eqn)
	{
		Point3d p1 = new Point3d();
		Point3d p2 = new Point3d();
		Point3d p = new Point3d();
		bounds.getLower(p1);
		bounds.getUpper(p2);

		Transform3D local2vw = bounds.getLocalToVWorld();
		if (local2vw==null) return 0;	//	not live; can't determine transform

		boolean result = intersect(p1,local2vw,eqn);
		p.set(p2.x,p1.y,p1.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		p.set(p1.x,p2.y,p1.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		p.set(p1.x,p1.y,p2.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		p.set(p2.x,p2.y,p1.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		p.set(p2.x,p1.y,p2.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		p.set(p1.x,p2.y,p2.z);
		if (intersect(p,local2vw,eqn) != result) return 0;
		if (intersect(p2,local2vw,eqn) != result) return 0;

		return result ? +1:-1;
	}

	private boolean intersect(Point3d p, Transform3D local2vw, Vector4d eqn)
	{
		local2vw.transform(p);
		return dist(p,eqn) > 0;
	}

	private static double dist(Point3d p, Vector4d eqn)
	{
		return p.x*eqn.x + p.y*eqn.y + p.z*eqn.z + eqn.w;
	}

	private static double dist(double x, double y, double z, Vector4d eqn)
	{
		return x*eqn.x + y*eqn.y + z*eqn.z + eqn.w;
	}

	protected void calcEquations(Point3d eye)
	{
		//	latitude = 0.0				view from top
		//	latitude = pi/2				view from front
		//	pi/2 < latiftude < 3*pi/2	view from below
		//	latitude = 3*pi/2			view from behind

		//	the 4 clipping planes are determined by the eye position
		//	and the 4 corners of the board.


		//	4. the corner points
		Vector3d lower_left		= new Vector3d(-size,-size,0.0);
		Vector3d lower_right	= new Vector3d(+size,-size,0.0);
		Vector3d upper_left		= new Vector3d(-size,+size,0.0);
		Vector3d upper_right	= new Vector3d(+size,+size,0.0);

		lower_left.sub(eye);
		lower_right.sub(eye);
		upper_left.sub(eye);
		upper_right.sub(eye);

		if (eye.z < 0)
		{
			//	view from below: cull everything
			cullAll = true;
		}
		else {
			cullAll = false;
			//	front
			eqns[0] = getPlaneEquation(eye,lower_left,lower_right);
			//	back
			eqns[1] = getPlaneEquation(eye,upper_right,upper_left);
			//	right
			eqns[2] = getPlaneEquation(eye,lower_right,upper_right);
			//	left
			eqns[3] = getPlaneEquation(eye,upper_left,lower_left);
		}
	}

	/**
	 *	calculate the normal vector of a plane,
	 * 	given 3 points
	 */
	public static Vector4d getPlaneEquation(Point3d a, Vector3d u, Vector3d v)
	{
		//	n  = (b-a) x (c-a);
		Vector3d n = new Vector3d();
		n.cross(u,v);

		//	get the right distance
		//	w = <n,a>
		double w = n.dot(Util3D.ptov(a));

		//	the plane equation is
		//	n x v - w = 0
		return new Vector4d(n.x,n.y,n.z, -w);
	}

	public void behaviorCallback(Object source, int actionCode, Object params)
	{
		//	callback from Orbit Behavior
		if (actionCode == OrbitBehavior.ORBIT_EVENT) {
			//	1. find the eye position
			Point3d eye = new Point3d();
			((IOrbitBehavior)source).getEyePoint(eye);

/*			canvas.getCenterEyeInImagePlate(eye);
			//	2. transform to vworld coordinates
			Transform3D tf1 = new Transform3D();
			canvas.getImagePlateToVworld(tf1);
			tf1.transform(eye);
*/
			//	better: compute the eye point directly from the polar coordinates

			//	3. transform to local coordinates
//			eye.scale(1.0/BoardView3D.scaleFactor);

			updateAll(eye);
		}
	}

	public void updateAll(Point3d eye)
	{
		calcEquations(eye);
		updateAll();
	}

	public void updateAll()
	{
		for (int i=0; i < numSwitches(); i++)
			getSwitch(i).updateAll();
	}
}
