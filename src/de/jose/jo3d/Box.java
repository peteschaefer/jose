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

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;

import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * a box
 */

public class Box
		extends Shape3D
{
 
	/**	
	 * @param a a corner
	 * @param b the size of the box
	 */
	public Box(Point3f a, Vector3f b)
	{
		this (a.x,a.y,a.z, a.x+b.x,a.y+b.y,a.z+b.z);
	}
	
	/**
	 * @param a a corner
	 * @param b the size of the box
	 */
	public Box(Point3f a, Point3f b)
	{
		this (a.x,a.y,a.z, b.x,b.y,b.z);
	}

	/**
	 * @param a a corner
	 * @param b the size of the box
	 */
	public Box(Point3d a, Point3d b)
	{
		this ((float)a.x,(float)a.y,(float)a.z, (float)b.x,(float)b.y,(float)b.z);
	}

	public Box(float ax, float ay, float az, 
			   float bx, float by, float bz)
	{
		super();
		GeometryArray geo = createGeometry(Math.max(ax,bx), Math.max(ay,by), Math.max(az,bz),
								   Math.min(ax,bx), Math.min(ay,by), Math.min(az,bz));
		setGeometry(geo);
	}
	
	private static float[] f = new float[72];
	private static int[] strips = new int[] { 4,4,4,4,4,4 };
	
	protected static GeometryArray createGeometry(float ax, float ay, float az, 
												  float bx, float by, float bz)
	{
		
		int i=0;
		// front face
		f[i++] = ax; f[i++] = by; f[i++] = az;
		f[i++] = ax; f[i++] = ay; f[i++] = az;
		f[i++] = bx; f[i++] = ay; f[i++] = az;
		f[i++] = bx; f[i++] = by; f[i++] = az;
		// back face
		f[i++] = bx; f[i++] = by; f[i++] = bz;
		f[i++] = bx; f[i++] = ay; f[i++] = bz;
		f[i++] = ax; f[i++] = ay; f[i++] = bz;
		f[i++] = ax; f[i++] = by; f[i++] = bz;
		// right face
		f[i++] = ax; f[i++] = by; f[i++] = bz;
		f[i++] = ax; f[i++] = ay; f[i++] = bz;
		f[i++] = ax; f[i++] = ay; f[i++] = az;
		f[i++] = ax; f[i++] = by; f[i++] = az;
		// left face
		f[i++] = bx; f[i++] = by; f[i++] = az;
		f[i++] = bx; f[i++] = ay; f[i++] = az;
		f[i++] = bx; f[i++] = ay; f[i++] = bz;
		f[i++] = bx; f[i++] = by; f[i++] = bz;
		// top face
		f[i++] = ax; f[i++] = ay; f[i++] = az;
		f[i++] = ax; f[i++] = ay; f[i++] = bz;
		f[i++] = bx; f[i++] = ay; f[i++] = bz;
		f[i++] = bx; f[i++] = ay; f[i++] = az;
		// bottom face
		f[i++] = bx; f[i++] = by; f[i++] = az;
		f[i++] = bx; f[i++] = by; f[i++] = bz;
		f[i++] = ax; f[i++] = by; f[i++] = bz;
		f[i++] = ax; f[i++] = by; f[i++] = az;

		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(f);
		gi.setStripCounts(strips);
		
//		Triangulator tr = new Triangulator();
//		tr.triangulate(gi);
		
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		
		Stripifier st = new Stripifier(/*Stripifier.COLLECT_STATS*/);
		st.stripify(gi);
		
/*		StripifierStats stats = st.getStripifierStats();
		System.out.print("total tris = "+stats.getTotalTris());
		System.out.print(", ");
		System.out.print("num strips = "+stats.getNumStrips());
		System.out.print(", ");
		System.out.println("num verts = "+stats.getNumVerts());
*/		
		return gi.getGeometryArray();
	}
	
	
}
