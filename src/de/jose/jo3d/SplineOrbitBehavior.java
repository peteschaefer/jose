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

import de.jose.jo3d.interpolators.KBKeyFrame;
import de.jose.jo3d.interpolators.KBRotPosScaleSplinePathInterpolator;

import javax.media.j3d.Alpha;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3f;
import java.util.Vector;

/**
 * extends KBRotPosScaleSplinePathInterpolator
 * and adds callback methods
 */

public class SplineOrbitBehavior
        extends KBRotPosScaleSplinePathInterpolator
        implements IOrbitBehavior

{
    protected Vector listeners = new Vector();
    protected Point3d theEye = new Point3d();

      /**
      * Constructs a new KBRotPosScaleSplinePathInterpolator object that
      * varies the rotation, translation, and scale of the target
      * TransformGroup's transform.  At least 2 key frames are required for
      * this interpolator. The first key
      * frame's knot must have a value of 0.0 and the last knot must have a
      * value of 1.0.  An intermediate key frame with index k must have a
      * knot value strictly greater than the knot value of a key frame with
      * index less than k.
      * @param alpha the alpha object for this interpolator
      * @param target the TransformGroup node affected by this interpolator
      * @param axisOfTransform the transform that specifies the local
      * coordinate system in which this interpolator operates.
      * @param keys an array of key frames that defien the motion path
      */
    public SplineOrbitBehavior(Alpha alpha,
				       TransformGroup target,
				       Transform3D axisOfTransform,
				       KBKeyFrame keys[])
    {
	    super(alpha,target, axisOfTransform, keys);
    }


    public Transform3D integrateTransform(Transform3D tf,
	                                  double heading, double pitch, double bank,
	                                  double scale, Vector3f pos)
	{
        Transform3D result = super.integrateTransform(tf,heading,pitch,bank,scale,pos);

        theEye.set(pos);
        notifyListeners(OrbitBehavior.ORBIT_EVENT,null);

        return result;
    }

    public Tuple3d getEyePoint(Tuple3d eye)
    {
        if (eye==null) eye = new Point3d();
        eye.set(theEye);
        return eye;
    }

    public void addListener(ICallbackListener list)
	{
		if (!listeners.contains(list))
			listeners.add(list);
	}

	public void removeListener(ICallbackListener list)
	{
		listeners.remove(list);
	}

	public void notifyListeners(int actionCode, Object params)
	{
		for (int i=0; i<listeners.size(); i++)
			((ICallbackListener)listeners.get(i)).behaviorCallback(this, actionCode, params);
	}
}
