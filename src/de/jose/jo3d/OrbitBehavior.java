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

import com.sun.j3d.utils.behaviors.vp.ViewPlatformBehavior;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3d;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Enumeration;
import java.util.Vector;

/**
 * this behavior lets the user move the camera position 
 * while dragging the mouse
 * (which is placed in an "orbit" above the scene) 
 */

public class OrbitBehavior
		extends ViewPlatformBehavior
        implements IOrbitBehavior
{
	/**	Wakeup Post Id for zooming	 */
	public static final int ZOOM_EVENT	= 1001;
	/**	Wakeup post Id for orbiting	 */
	public static final int ORBIT_EVENT	= 1002;
	/** factor for mouse wheel units    */
    protected static final float MOUSE_WHEEL_UNIT = 4f;

	/**	the view transform	*/
	protected Transform3D tf;
	protected Polar3d current = new Polar3d();
	protected Polar3d initial = new Polar3d();
	protected Polar3d nominal = new Polar3d();
	protected Polar3d min = new Polar3d();
	protected Polar3d max = new Polar3d();

	
	/**	what are we doing	 */
	protected int what;
	/**	point in (screen coordinates) where dragging started	 */
	protected Point mouseStart;
	protected WakeupCondition startCondition;
	protected WakeupCondition dragCondition;
	protected MouseQueue mouseQueue;
	protected boolean triggered;
	protected long targetTime;
	protected double targetLock;
	protected Vector listeners = new Vector();
	
	/**	default minimal distance	 */
	private static final double MIN_DISTANCE = 0.0;
	
	public OrbitBehavior(boolean isTriggered, MouseQueue q)
	{
		super();
		mouseQueue = q;
		current.setPolarCoordinates(0.0, 0.0, 0.0);

		///	moving the mouse by 1 pixel will increase the parameters by ..
		nominal.setPolarCoordinates(0.003,0.003,0.01);

		min.setPolarCoordinates(-Double.MAX_VALUE,-Double.MAX_VALUE,MIN_DISTANCE);
		max.setPolarCoordinates(Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE);

		triggered = isTriggered;
		tf = new Transform3D();
	}

	public void cropLatitude(double minLatitude, double maxLatitude)
	{
		min.setLatitude(minLatitude);
		max.setLatitude(maxLatitude);
	}

	public void cropLongitude(double minLongitude, double maxLongitude)
	{
		min.setLongitude(minLongitude);
		max.setLongitude(maxLongitude);
	}

	public void cropDistance(double minDistance, double maxDistance)
	{
		min.setDistance(minDistance);
		max.setDistance(maxDistance);
	}

	public void initialize()
	{
		WakeupCriterion[] postEvents = {
			new WakeupOnBehaviorPost(null, ZOOM_EVENT),
			new WakeupOnBehaviorPost(null, ORBIT_EVENT),
		};
		WakeupCriterion[] dragEvents = {
			new WakeupOnElapsedFrames(0),
		//	new WakeupOnElapsedTime(40),
			new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED),
		};
		
		//	the behavior is triggered by a post event
		if (triggered)
			startCondition = new WakeupOr(postEvents);
		else
			startCondition = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
		dragCondition = new WakeupOr(dragEvents);
		
		wakeupOn(startCondition);
	}

	public void processStimulus(Enumeration criteria)
	{
		WakeupCondition nextWakeup = startCondition;
		
		while (criteria.hasMoreElements())
		{
			WakeupCriterion wake = (WakeupCriterion)criteria.nextElement();
			if (wake instanceof WakeupOnBehaviorPost)
			{
				WakeupOnBehaviorPost post = (WakeupOnBehaviorPost)wake;
				switch (post.getPostId())
				{
				case ZOOM_EVENT:
					startZoom(mouseQueue.lastEvent(MouseEvent.MOUSE_PRESSED));
					nextWakeup = dragCondition;
					break;
				case ORBIT_EVENT:
					startOrbit(mouseQueue.lastEvent(MouseEvent.MOUSE_PRESSED));
					nextWakeup = dragCondition;
					break;
				}
			}
			else if (wake instanceof WakeupOnElapsedFrames)
			{
				//	keep on dragging
				MouseEvent mevt = mouseQueue.lastEvent(MouseEvent.MOUSE_DRAGGED);
				if (mevt != null) {
					if (isZooming())
						zoom(mevt);
					else if (isOrbiting())
						orbit(mevt);
				}
				nextWakeup = dragCondition;			
			}
			else if (wake instanceof WakeupOnAWTEvent)
			{
				AWTEvent[] evts = ((WakeupOnAWTEvent)wake).getAWTEvent();
				
				for (int i=0; i<evts.length; i++)
					switch (evts[i].getID()) {
					case MouseEvent.MOUSE_PRESSED:
						startOrbit((MouseEvent)evts[i]);
						mouseQueue.clear(MouseEvent.MOUSE_DRAGGED);
						break;
					case MouseEvent.MOUSE_RELEASED:
						//	end dragging
						drop((MouseEvent)evts[i]);
						mouseQueue.clear(MouseEvent.MOUSE_DRAGGED);
						nextWakeup = startCondition;
						break;
					}
			}
		}
			   
			   
		if (nextWakeup==startCondition)
			notifyListeners(ICallbackListener.DEACTIVATE, null);
		wakeupOn(nextWakeup);
	}
	
	public final double getDistance()				{ return current.getDistance(); }
	
	public final void setDistance(double d)			{ current.setDistance(d); }
	
	public final double getLatitude()				{ return current.getLatitude(); }
	
	public final void setLatitude(double d)			{ current.setLatitude(d); }
	
	public final double getLongitude()				{ return current.getLongitude(); }
	
	public final void setLongitude(double d)		{ current.setLongitude(d); }

	/**
	 * @param result holds the result on return (optional)
	 * @return the location of the eye in VWorld coordinates
	 */
	public Tuple3d getEyePoint(Tuple3d result)
	{
		if (result==null) result = new Point3d();
		//	convert polar coordinates to cartesian
		return current.toCartesian(result);
/*		result.x =   getDistance() * Math.sin(getLatitude()) * Math.sin(getLongitude());
		result.y = - getDistance() * Math.sin(getLatitude()) * Math.cos(getLongitude());
		result.z =   getDistance() * Math.cos(getLatitude());
		return result;
		//	or: retrun current.toCartesian()
*/	}

	/**
	 * @return true if the eye point is above the horizon
	 */
	public boolean aboveHorizon()
	{
		return (getLatitude() < Math.PI/2) ||
			   (getLatitude() > Math.PI*3/2);
	}

	public void apply()
	{
		apply(ORBIT_EVENT,null);
	}

	private void apply(int actionCode, MouseEvent evt)
	{
		tf.setIdentity();
		
		Util3D.rotZ(tf, current.getLongitude());

		Util3D.rotX(tf, current.getLatitude());
		
		Util3D.translate(tf, 0.0,0.0, current.getDistance());
		
		targetTG.setTransform(tf);

		notifyListeners(actionCode,evt);
	}

	/**
	 * apply the current transform to a point
	 * @return
	 */
	public void apply(Point3f p)
	{
		tf.transform(p);
	}

	protected boolean isZooming()
	{
		return (what==ZOOM_EVENT);
	}
	
	protected boolean isOrbiting()
	{
		return (what==ORBIT_EVENT);
	}
	
	protected void initDrag(MouseEvent evt)
	{
		mouseStart = evt.getPoint();
		initial.set(current);
	}
	
	protected void startZoom(MouseEvent evt)
	{
//		System.out.println("start zoom");
		notifyListeners(ICallbackListener.ACTIVATE, evt);
		what = ZOOM_EVENT;
		initDrag(evt);
	}
	
	protected void startOrbit(MouseEvent evt)
	{
//		System.out.println("start orbit");
		notifyListeners(ICallbackListener.ACTIVATE, evt);
		what = ORBIT_EVENT;
		initDrag(evt);
	}
	
	protected void zoom(MouseEvent evt)
	{
		int diff = evt.getY()-mouseStart.y;
		current.setDistance(initial.getDistance() - diff*nominal.getDistance());
		apply(ZOOM_EVENT, evt);
	}

    public void zoomWheel(MouseWheelEvent evt)
    {
        /*  what amount should we scroll ?  */
        startZoom(evt);

        float diff = evt.getWheelRotation() * MOUSE_WHEEL_UNIT;
        current.setDistance(initial.getDistance() - diff*nominal.getDistance());
        apply(ZOOM_EVENT, evt);
    }

	protected void orbit(MouseEvent evt)
	{
		int diffx = evt.getX()-mouseStart.x;
		int diffy = evt.getY()-mouseStart.y;
		
		if (! evt.isShiftDown()) {
			//	move towards the next multiple of 45 degrees
			double nextlock = closestMultipleOf(initial.getLongitude() - diffx*nominal.getLongitude(), Math.PI/2);
			if (nextlock==current.getLongitude())
				;
			else {
				if (nextlock != targetLock) {
					targetTime = System.currentTimeMillis()+2000;
					targetLock = nextlock;
				}
				current.setLongitude (animationStep(current.getLongitude(), targetLock,
													(targetTime-System.currentTimeMillis())/2000.0));
			}
		}
		else {
			//	free rotate
			current.setLongitude (initial.getLongitude() - diffx*nominal.getLongitude());
			targetLock = 0.0;
		}
		
		current.setLatitude (initial.getLatitude() - diffy*nominal.getLatitude());

		if (current.getLatitude() < min.getLatitude()) current.setLatitude (min.getLatitude());
		if (current.getLongitude() < min.getLongitude()) current.setLongitude (min.getLongitude());
		if (current.getDistance() < min.getDistance()) current.setDistance (min.getDistance());

		if (current.getLatitude() > max.getLatitude()) current.setLatitude (max.getLatitude());
		if (current.getLongitude() > max.getLongitude()) current.setLongitude (max.getLongitude());
		if (current.getDistance() > max.getDistance()) current.setDistance (max.getDistance());

		apply(ORBIT_EVENT, evt);
	}
	
	protected double animationStep(double from, double to, double remaining)
	{
		if (remaining <= 0.0)
			return to;
		else if (remaining >= 1.0)
			return from;
		else
			return (to*(1.0-remaining) + from*(remaining));
	}
	
	protected static double closestMultipleOf(double x, double mod)
	{
		return Math.rint(x/mod)*mod;
	}
	
	protected void drop(MouseEvent evt)
	{
		if (isZooming())
			zoom(evt);
		else if (isOrbiting())
			orbit(evt);
		what = 0;
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
