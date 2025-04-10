/*
 *	@(#)RotPosScaleTCBSplinePathInterpolator.java 1.22 01/07/31 16:42:23
 *
 * Copyright (c) 1996-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
 * OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 * FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed,licensed or intended
 * for use in the design, construction, operation or maintenance of
 * any nuclear facility.
 */

//package com.sun.j3d.utils.behaviors.interpolators;
package de.jose.jo3d.interpolators;

import javax.media.j3d.*;
import javax.vecmath.*;


/**
 * RotPosScaleTCBSplinePathInterpolator behavior.  This class defines a 
 * behavior that varies the rotational, translational, and scale components 
 * of its target TransformGroup by using the Kochanek-Bartels cubic spline 
 * interpolation to interpolate among a series of key frames
 * (using the value generated by the specified Alpha object).  The
 * interpolated position, orientation, and scale are used to generate
 * a transform in the local coordinate system of this interpolator.  
 *
 * @since Java3D 1.1
 */

public class RotPosScaleTCBSplinePathInterpolator 
extends TCBSplinePathInterpolator {

    private Transform3D    rotation    = new Transform3D();
    private Matrix4d       tMat        = new Matrix4d();
    private Matrix4d       sMat        = new Matrix4d();
    private Quat4f         iQuat       = new Quat4f();     // interpolated quaternion
    private Vector3f       iPos        = new Vector3f();   // interpolated position 
    private Point3f        iScale      = new Point3f();    // interpolated scale 

    CubicSplineCurve   cubicSplineCurve = new CubicSplineCurve();
    CubicSplineSegment cubicSplineSegments[]; 
    int                numSegments;
    int                currentSegmentIndex;
    CubicSplineSegment currentSegment;

    // non-public, default constructor used by cloneNode
    RotPosScaleTCBSplinePathInterpolator() {
    }

    /**
     * Constructs a new RotPosScaleTCBSplinePathInterpolator object that 
     * varies the rotation, translation, and scale of the target 
     * TransformGroup's transform.  At least 2 key frames are required for 
     * this interpolator. The first key
     * frame's knot must have a value of 0.0 and the last knot must have a
     * value of 1.0.  An intermediate key frame with index k must have a
     * knot value strictly greater than the knot value of a key frame with
     * index less than k.
     * @param alpha the alpha object for this interpolator
     * @param target the TransformGroup node affected by this interpolator
     * @param axisOfRotPosScale the transform that specifies the local
     * coordinate system in which this interpolator operates.
     * @param keys an array of key frames that defien the motion path 
     */
    public RotPosScaleTCBSplinePathInterpolator(Alpha alpha,
						TransformGroup target,
						Transform3D axisOfTransform,
						TCBKeyFrame keys[]) {
	super(alpha, target, axisOfTransform, keys);
        // Create a spline curve using the derived key frames
        cubicSplineCurve = new CubicSplineCurve(this.keyFrames); 
        numSegments = cubicSplineCurve.numSegments;

    }


    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.setTransformAxis(Transform3D)</code>
     */
    public void setAxisOfRotPosScale(Transform3D axisOfRotPosScale) {
        setTransformAxis(axisOfRotPosScale);
    }
    
    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>TransformInterpolator.getTransformAxis()</code>
     */       
    public Transform3D getAxisOfRotPosScale() {
        return getTransformAxis();
    }
    
    /**
     * Computes the new transform for this interpolator for a given
     * alpha value.
     *
     * @param alphaValue alpha value between 0.0 and 1.0
     * @param transform object that receives the computed transform for
     * the specified alpha value
     *
     * @since Java 3D 1.3
     */
    public void computeTransform(float alphaValue, Transform3D transform) {

	// compute the current value of u from alpha and the 
	// determine lower and upper knot points
	computePathInterpolation(alphaValue);

	// Determine the segment within which we will be interpolating
	currentSegmentIndex = this.lowerKnot - 1;
    
	// if we are at the start of the curve 
	if (currentSegmentIndex == 0 && currentU == 0f) {

	    iQuat.set(keyFrames[1].quat);
	    iPos.set(keyFrames[1].position);
	    iScale.set(keyFrames[1].scale);

	    // if we are at the end of the curve 
	} else if (currentSegmentIndex == (numSegments-1) && 
		   currentU == 1.0) {

	    iQuat.set(keyFrames[upperKnot].quat);
	    iPos.set(keyFrames[upperKnot].position);
	    iScale.set(keyFrames[upperKnot].scale);

	    // if we are somewhere in between the curve
	} else {

	    // Get a reference to the current spline segment i.e. the
	    // one bounded by lowerKnot and upperKnot 
	    currentSegment 
		= cubicSplineCurve.getSegment(currentSegmentIndex);

	    // interpolate quaternions 
	    currentSegment.getInterpolatedQuaternion(currentU,iQuat);

	    // interpolate position
	    currentSegment.getInterpolatedPositionVector(currentU,iPos); 

	    // interpolate position
	    currentSegment.getInterpolatedScale(currentU,iScale);

	}

	// Alway normalize the quaternion
	iQuat.normalize();	    
	tMat.set(iQuat);

	// Set the translation components.
	tMat.m03 = iPos.x;
	tMat.m13 = iPos.y;
	tMat.m23 = iPos.z;
	rotation.set(tMat);
    
	// construct a Transform3D from:  axis * rotation * axisInverse 
	transform.mul(axis, rotation);
	transform.setScale(new Vector3d(iScale));   
	transform.mul(transform, axisInverse);
	   
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        RotPosScaleTCBSplinePathInterpolator spline =
	    new RotPosScaleTCBSplinePathInterpolator();
        spline.duplicateNode(this, forceDuplicate);
        return spline;
    }
    
   /**
     * Copies RotPosScaleTCBSplinePathInterpolator information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P> 
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
        super.duplicateNode(originalNode, forceDuplicate);
	RotPosScaleTCBSplinePathInterpolator interpolator = 
	    (RotPosScaleTCBSplinePathInterpolator)originalNode;
	
        cubicSplineCurve = new CubicSplineCurve(interpolator.keyFrames);
        numSegments = cubicSplineCurve.numSegments;
    }
}
