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
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class Arrow3D
        extends BranchGroup
{
	public Arrow3D(Point2d p1, Point2d p2, float[] coord, float z, Color color)
	{
		this.setCapability(BranchGroup.ALLOW_DETACH);

		//  create poly
		FlatPolygon poly = new FlatPolygon(coord,false);

		//  move into place
		Transform3D tf = new Transform3D();
//		tf.rotZ(Math.atan2(p2.y-p1.y, p2.x-p1.x));
		Util3D.translate(tf,p1.x,p1.y,z);
		Util3D.rotZ(tf,Math.atan2(p2.y-p1.y, p2.x-p1.x));

		TransformGroup tg = new TransformGroup(tf);

		//  create appearance
		Appearance app = Util3D.createAppearance(PolygonAttributes.CULL_NONE);
//		if (color.getTransparency()!=0)
//			Util3D.setTransparency(app, (float)color.getTransparency()/255.0f);
		//  partial transparency doesn't work with DirectX ??
		float[] rgb = new float[3]; //  { 1.0f,0.0f,0.0f, };
		color.getColorComponents(rgb);
		Color3f c3f = new Color3f(rgb);

		Material mat = app.getMaterial();
		mat.setDiffuseColor(c3f);
		mat.setAmbientColor(c3f);
		mat.setSpecularColor(c3f);
		mat.setLightingEnable(true);


		poly.setAppearance(app);

		tg.addChild(poly);
		this.addChild(tg);

		poly.setCollidable(false);
		poly.setPickable(false);
	}
}
