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
import java.awt.*;

/**
 *	a Decal is used for painting shadows and reflections on top of planar surfaces
 *
 *	we simply use a plane with a transparent texture
 * 	clipping is optional
 *
 * 	to avoid z-buffer fighting, the plane is lifted a bit
 * 	I know this is crude and it would be better to use a DecalGroup but it simply DOES NOT WORK !!
 * 	(DecalGroup with OpenGL is a complete disaster)
 *
 *	@author Peter Schäfer
 */

public class Decal
		extends TransformGroup
{
	/**	the plane where we put the texture on */
	protected Plane plane;

	protected Decal(boolean mirrored, boolean init)
	{
		super();

		if (mirrored)
			plane = new Plane(-0.5f,0f,0f, +0.5f,1f,0f, 1f,0f, 0f,1f, Plane.XY_PLANE);	//	mirrors the texture coordinates
		else
			plane = new Plane(-0.5f,0f,0f, +0.5f,1f,0f, 0f,0f, 1f,1f, Plane.XY_PLANE);

		plane.setPickable(false);
		plane.setCollidable(false);
		plane.setCapability(TransformGroup.ALLOW_BOUNDS_READ);
		plane.setCapability(Shape3D.ALLOW_LOCAL_TO_VWORLD_READ);

		setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		setCapability(TransformGroup.ALLOW_BOUNDS_READ);
		setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

		setBoundsAutoCompute(false);
		setBounds(plane.getBounds());
	}

	public Decal(float z, boolean mirrored)
	{
		this(mirrored,true);

		plane.setAppearance(createAppearance(z));
		addChild(plane);
	}

	public Appearance getAppearance()
	{
		return plane.getAppearance();
	}

	public void setAppearance(Appearance app)
	{
		plane.setAppearance(app);
	}

	public void setTexture(String textureName, Component observer)
	{
		getAppearance().setTexture(TextureCache3D.getDecalTexture(textureName,observer));
	}


	public void rotate(double angle)
	{
		Transform3D tf = new Transform3D();
		getTransform(tf);
		Util3D.rotZ(tf,angle);
		setTransform(tf);
	}


	public void scale(double scalex, double scaley)
	{
		Transform3D tf = new Transform3D();
		getTransform(tf);
		Util3D.scale(tf, scalex,scaley,1);
		setTransform(tf);
	}

	/**
	 *
	 * @param zoffset polygon offset in device coordinates (whatever these are ?!)
	 * @return
	 */
	public static Appearance createAppearance(float zoffset)
	{
		TransparencyAttributes transp = new TransparencyAttributes();
		transp.setTransparencyMode(TransparencyAttributes.FASTEST);
		transp.setTransparency(0.0f);		//	opaque
		//	we need flexible transparency based on the alpha component of the texture

		Appearance appearance = new Appearance();
		appearance.setTransparencyAttributes(transp);

		PolygonAttributes polyattr = new PolygonAttributes();
		polyattr.setPolygonOffset(zoffset);
		appearance.setPolygonAttributes(polyattr);

//		TextureAttributes attr = new TextureAttributes();
//		attr.setTextureMode(TextureAttributes.MODULATE);
//		appearance.setTextureAttributes(attr);

//		ColoringAttributes ca = new ColoringAttributes(1f,0f,0f, ColoringAttributes.FASTEST);
//		appearance.setColoringAttributes(ca);

		Material m = new Material();
		m.setLightingEnable(false);
//		m.setDiffuseColor(1f,0f,0f,0.0f);
		appearance.setMaterial(m);

		return appearance;
 	}
}
