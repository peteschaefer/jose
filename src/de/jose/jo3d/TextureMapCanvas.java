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

import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *	this Canvas operates offscreen
 *	and manufactures TextureCubeMaps from scene snapshots
 *
 *  VERY EXPERIMENTAL
 *
 *	@author Peter Schäfer
 */

public class TextureMapCanvas
		extends Canvas3D
{
	/**	image size (must be power of 2)	*/
	private int size;
	/**	transform to apply to the viewing direction	 */
	private Transform3D viewTransform;
	/**	the associated view	*/
	private View view;
	/**	the transform group to operate on */
	private TransformGroup targetTG;

	/**	by default, the camera is located at 0,0,0 and looking down the negative z-axis
	 * 	the following transforms are used to turn it to the six face of the cube
	 */
	private static Transform3D	ROT_POS_X;
	private static Transform3D	ROT_NEG_X;
	private static Transform3D	ROT_POS_Y;
	private static Transform3D	ROT_NEG_Y;
	private static Transform3D	ROT_POS_Z;
	private static Transform3D	ROT_NEG_Z;

	private static void setupTransforms()
	{
		Transform3D tf = new Transform3D();

		ROT_POS_X	= new Transform3D();
		ROT_POS_X.rotY(-Math.PI/2);
		tf.rotZ(-Math.PI/2);
		ROT_POS_X.mul(tf);

		ROT_NEG_X	= new Transform3D();
		ROT_NEG_X.rotY(Math.PI/2);
		tf.rotZ(+Math.PI/2);
		ROT_NEG_X.mul(tf);

		ROT_POS_Y	= new Transform3D();
		ROT_POS_Y.rotX(Math.PI/2);

		ROT_NEG_Y	= new Transform3D();
		ROT_NEG_Y.rotX(-Math.PI/2);
		tf.rotZ(-Math.PI);
		ROT_NEG_Y.mul(tf);

		ROT_POS_Z	= new Transform3D();
		ROT_POS_Z.rotX(Math.PI);	//	POS_Z OK

		ROT_NEG_Z	= new Transform3D();
		//	ROT_NEG_Z points already in the right direction
	}

	static {
		setupTransforms();
	}

	/**	aux. variables	*/
	private Transform3D savetf = new Transform3D();
	private Transform3D auxtf = new Transform3D();
	private int savePolicy;

	/**
	 *
	 * @param gconfig
	 * @param u
	 * @param sz size of resulting texture (must be power of 2)
	 */
	public TextureMapCanvas(GraphicsConfiguration gconfig, SimpleUniverse u, int sz)
	{
		super(gconfig, true);
		size = sz;
		viewTransform = new Transform3D();

		//	get the camera's transform group
		targetTG = u.getViewingPlatform().getViewPlatformTransform();

		// attach the same view to the offscreen canvas
		view = u.getViewer().getView();

		//	set the "screen" size
		Screen3D onscreen = u.getCanvas().getScreen3D();
		Screen3D screen = getScreen3D();
		screen.setSize(size,size);
		screen.setPhysicalScreenWidth(onscreen.getPhysicalScreenWidth());
		screen.setPhysicalScreenHeight(onscreen.getPhysicalScreenHeight());
	}

	/**
	 * move the eye position
	 * by default, the eye is postioned at 0,0,0 and looks down the negative z-axis
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setEyePosition(double x, double y, double z)
	{
		viewTransform.setTranslation(new Vector3d(x,y,z));
	}

	/**
	 * create a new TextureCubeMap texture
	 *
	 * @param mutable if the texture can be modified in a live scene
	 */
	public TextureCubeMap createTextureCubeMap(boolean mutable)
	{
		TextureCubeMap map = new TextureCubeMap(Texture.BASE_LEVEL, Texture.RGB, size);

		for (int face=0; face<6; face++) {
			ImageComponent2D buffer = createImageBuffer();
			if (mutable)
				buffer.setCapability(ImageComponent2D.ALLOW_IMAGE_WRITE);
				//	set this capability if the image can be modified in a live scene
			map.setImage(0, face, buffer);
		}

		return map;
	}

	/**
	 * fill a TextureCubeMap with a snapshot of the current scene
	 */
	public void snapshot(TextureCubeMap map)
	{
		setupView();

		for (int face=0; face<6; face++)
			snapshot(face, (ImageComponent2D)map.getImage(0,face));

		restoreView();
	}

	private ImageComponent2D createImageBuffer()
	{
		BufferedImage bimage = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);
		ImageComponent2D buffer = new ImageComponent2D(ImageComponent.FORMAT_RGB, bimage, true,true);
		return buffer;
	}

	/**
	 * take a snapshot from the current scene and create an Image
	 *
	 * @return an ImageComponent that contains a snapshot of the current scene
	 */
	public ImageComponent2D snapshot(int direction)
	{
		setupView();

		ImageComponent2D buffer = createImageBuffer();
		snapshot(direction,buffer);

		restoreView();
		return buffer;
	}

	private void setupView()
	{
		savePolicy = view.getProjectionPolicy();
		targetTG.getTransform(savetf);

		view.addCanvas3D(this);
		view.setProjectionPolicy(View.PARALLEL_PROJECTION);
	}

	private void restoreView()
	{
		targetTG.setTransform(savetf);
		view.setProjectionPolicy(savePolicy);
		view.removeCanvas3D(this);
	}

	/**
	 * take a snapshot from the current scene and create an Image
	 *
	 * @param buffer an ImageComponent that contains a snapshot of the current scene
	 */
	private void snapshot(int face, ImageComponent2D buffer)
	{
		auxtf.set(viewTransform);
		switch (face)
		{
		case TextureCubeMap.POSITIVE_X:	auxtf.mul(ROT_POS_X); break;
		case TextureCubeMap.NEGATIVE_X:	auxtf.mul(ROT_NEG_X); break;
		case TextureCubeMap.POSITIVE_Y:	auxtf.mul(ROT_POS_Y); break;
		case TextureCubeMap.NEGATIVE_Y:	auxtf.mul(ROT_NEG_Y); break;
		case TextureCubeMap.POSITIVE_Z:	auxtf.mul(ROT_POS_Z); break;
		case TextureCubeMap.NEGATIVE_Z:	auxtf.mul(ROT_NEG_Z); break;
		default:	throw new IllegalArgumentException();
		}
		targetTG.setTransform(auxtf);

		buffer.setCapability(ImageComponent2D.ALLOW_IMAGE_READ);

		setOffScreenBuffer(buffer);
		renderOffScreenBuffer();
		waitForOffScreenRendering();
	}


}
