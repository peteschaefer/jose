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

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.Enumeration;

/**
 *  animates a piece
 *  provides listener callbacks
 *
 */

public class PieceAnimator
           extends CallbackBehavior
{
    /**	Wakeup post Id for starting the animation	 */
	public static final int ANIMATION_START	= 1003;
    /**	Wakeup post Id for stopping the animation	 */
	public static final int ANIMATION_STOP  = 1004;


    /** the piece groups this behavior is operating on */
    protected PieceGroup target;
    /**    the starting point of the animation */
    private Vector3d startPoint;
    /**    the end point of the animation */
    private Point3d endPoint;
     /**     the time-based alpaha function */
    private Alpha alpha;

    /** start condition */
    private WakeupCondition startCondition;
    /** animation condition */
    private WakeupCondition animationCondition;


	public PieceAnimator()
	{
	}

    /** set the target piece group */
    public void setTarget(PieceGroup pg)
	{
		target = pg;
		startPoint = target.location();
	}

    /** set the target location */
    public void setEndPoint(Point3d p)      { endPoint = p; }

    /** set the target location */
    public void setEndPoint(double x, double y, double z)      { endPoint = new Point3d(x,y,z); }

    /** set the alpha interpolators */
    public void setAlpha(Alpha anAlpha)
	{
		alpha = anAlpha;
		alpha.setStartTime(System.currentTimeMillis());
    }

    public void setDuration(long duration)
    {
        setAlpha(new Alpha(1,duration));
	}

    /** kick of the animation */
    public void start()
	{
		postId(ANIMATION_START);
	}

    /** explicitly stop the animation */
    public void stop()
	{
		postId(ANIMATION_STOP);
	}

	/**	is this animation currently running ? */
	public final boolean isActive()			{ return target != null; }

    /** called by the J3D system */
    public void initialize()
    {
        WakeupCriterion[] animationCriteria = {
            new WakeupOnElapsedFrames(0),
            new WakeupOnBehaviorPost(null, ANIMATION_STOP),
        };

        startCondition = new WakeupOnBehaviorPost(null, ANIMATION_START);
        animationCondition = new WakeupOr(animationCriteria);
        wakeupOn(startCondition);
    }

    private void animate(double t)
    {
		target.moveTo( endPoint.x*t + startPoint.x*(1-t),
					   endPoint.y*t + startPoint.y*(1-t),
					   endPoint.z*t + startPoint.z*(1-t));
//		System.out.println("move "+t+" "+System.currentTimeMillis()%1000);
    }

	private void finish()
	{
		animate(1.0);
		target = null;
		notifyListeners(ICallbackListener.DEACTIVATE, null);
	}

    /**	implements Behavior	 */
	public void processStimulus(Enumeration criteria)
	{
        WakeupCondition nextWakeup = startCondition;

		while (criteria.hasMoreElements())
		{
			WakeupCriterion wake = (WakeupCriterion)criteria.nextElement();
            if (wake instanceof WakeupOnBehaviorPost)
			{
				if (isActive())
				{
					WakeupOnBehaviorPost post = (WakeupOnBehaviorPost)wake;
					switch (post.getPostId())
					{
					case ANIMATION_START:
						//  let's go
						notifyListeners(ICallbackListener.ACTIVATE, null);
						animate(alpha.value());
						nextWakeup = animationCondition;
						break;

					case ANIMATION_STOP:
						//  explicitly stopped
						finish();
						break;
					}
				}
			}
			else if (wake instanceof WakeupOnElapsedFrames)
			{
				if (alpha.finished()) {
					//	finished
					finish();
				}
				else {
					//	keep on moving
					animate(alpha.value());
					nextWakeup = animationCondition;
				}
			}
		}

        wakeupOn(nextWakeup);
	}
}
