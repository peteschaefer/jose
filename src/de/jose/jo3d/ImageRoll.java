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

import com.sun.j3d.utils.image.TextureLoader;
import de.jose.jo3d.interpolators.KBKeyFrame;
import de.jose.jo3d.interpolators.KBRotPosScaleSplinePathInterpolator;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4d;
import java.awt.*;
import java.net.URL;

public class ImageRoll
        extends TransformGroup
{
    /** switch node contaning the shape */
    private Switch switchGroup;
    /** interpolator    */
    private Interpolator interpolator;

    /**
     * @param image
     * @param alpha
     * @param start
     * @param end
     */
    public ImageRoll(URL imageUrl,
                     Alpha alpha,
                     Point3f start, Point3f end,
                     Dimension size,
                     Component observer)
    {
        super();
        setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        interpolator = new KBRotPosScaleSplinePathInterpolator(alpha, this, new Transform3D(), createKeyFrames(start,end));
        Bounds bounds = new BoundingSphere(new Point3d(0,0,0), Double.MAX_VALUE);
		interpolator.setSchedulingBounds(bounds);
		interpolator.setEnable(true);
        addChild(interpolator);

        switchGroup = new Switch(Switch.CHILD_NONE);
        switchGroup.setCapability(Switch.ALLOW_SWITCH_WRITE);
        Group g = new Group();
        switchGroup.addChild(createPlane(createTexture(imageUrl,observer),size));
//        switchGroup.addChild(new Box(0f,0f,-size.height, +size.width,10f,0f));
        addChild(switchGroup);
    }

    public ImageRoll(URL imageUrl, Point3f start, Point3f end, Dimension size, Component observer)
    {
        /*  a trapezoid alpha
            - increasing from 0..1
            - stays at 1 for some time
            - decreasing to 0
         */
        this (imageUrl,
              new Alpha(1, Alpha.INCREASING_ENABLE+Alpha.DECREASING_ENABLE,
                       0L,0L, 5000L,0L, 10000L, 2000L,0L, 0L),
            start,end, size, observer);
    }

    public void start()
    {
        switchGroup.setWhichChild(Switch.CHILD_ALL);
        interpolator.getAlpha().setStartTime(System.currentTimeMillis());
    }


    public void hide()
    {
        switchGroup.setWhichChild(Switch.CHILD_NONE);
    }


    public ModelClip setClip(double x, double y, double z, double d)
    {
        boolean[] enables = { true,false,false,false,false,false };
        Vector4d plane = new Vector4d(0,0,-1,0);
        ModelClip clip = new ModelClip();
        clip.setPlane(0,plane);
        clip.setEnables(enables);
        clip.addScope(switchGroup);
        clip.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0),5000));
        return clip;
    }


    public static Texture createTexture(URL url, Component observer)
    {
        TextureLoader loader = new TextureLoader(url, "RGBA", TextureLoader.Y_UP, observer);
        return loader.getTexture();
    }

    public static Shape3D createPlane(Texture texture, Dimension size)
    {
        Plane p = new Plane(0f,0f,-size.height, size.width,0f,0f, 1f,0f,0f,1f, Plane.XZ_PLANE);

        Appearance appearance = Decal.createAppearance(0f);
        appearance.setTexture(texture);

        Util3D.setCullFace(appearance,PolygonAttributes.CULL_NONE);
        p.setAppearance(appearance);
        return p;
    }

    private static KBKeyFrame[] createKeyFrames(Point3f start, Point3f end)
    {
        KBKeyFrame[] result = new KBKeyFrame[2];
        Point3f scale0 = new Point3f(0f,0f,0f);
        Point3f scale1 = new Point3f(1f,1f,1f);

        result[0] = new KBKeyFrame(0f, 1, start, 0f,0f,0f, scale0, 0f,0f,0f);
        result[1] = new KBKeyFrame(1f, 1, end,   0f,0f,0f, scale1, 0f,0f,0f);

        return result;
    }
}
