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

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

public class Plane
		extends Shape3D
{

	/**	Plane orientation	*/

	/**	coplanar to x-y (constant z)	*/
	public static final int XY_PLANE		= 1;
	/**	coplanar to y-z (constant x)	*/
	public static final int YZ_PLANE		= 2;
	/**	coplanar to x-z (constant y)	*/
	public static final int XZ_PLANE		= 3;

	/**
	 *	creates a rectangular shape
	 *
	 * @param ax	x coordinate of first corner
	 * @param ay	y coordinate of first corner
	 * @param az	z coordinate of first corner
	 * @param bx	x coordinate of second corner
	 * @param by	y coordinate of second corner
	 * @param bz	z coordinate of second corner
	 * @param as	s texture coordinate of first corner
	 * @param at	t texture coordinate of first corner
	 * @param bs	s texture coordinate of second corner
	 * @param bt	t texture coordinate of second corner
	 */
	public Plane(float ax, float ay, float az, 
				 float bx, float by, float bz,
				 float as, float at,
				 float bs, float bt,
				 int orientation)
	{
		super();
		
		Point3d lower = new Point3d(Math.min(ax,bx), Math.min(ay,by), Math.min(az,bz));
		Point3d upper = new Point3d(Math.max(ax,bx), Math.max(ay,by), Math.max(az,bz));
		GeometryArray geo = createGeometry((float)upper.x, (float)upper.y, (float)upper.z,
										    (float)lower.x, (float)lower.y, (float)lower.z,
                                            as, at, bs, bt, orientation);
		
		geo.setCapability(GeometryArray.ALLOW_INTERSECT);
		geo.setCapability(GeometryArray.ALLOW_COUNT_READ);
		geo.setCapability(GeometryArray.ALLOW_FORMAT_READ);
		geo.setCapability(GeometryArray.ALLOW_COORDINATE_READ);

		BoundingBox box = new LocalBoundingBox(this, lower,upper);
		setBoundsAutoCompute(false);
		setBounds(box);

		setGeometry(geo);
	}

	public Plane(float ax, float ay, float az,
				 float bx, float by, float bz,
				 int orientation)
	{
		this(ax,ay,az, bx,by,bz, 0.0f,0.0f, 1.0f,1.0f, orientation);
	}

	public Plane(float ax, float ay, float az,
				 float bx, float by, float bz,
				 Appearance app)
	{
		this(ax,ay,az, bx,by,bz, 0.0f,0.0f, 1.0f,1.0f, XY_PLANE);
		setAppearance(app);
	}

	public Plane(Point3d a, Point3d b, int orientation)
	{
		this ((float)a.x,(float)a.y,(float)a.z, (float)b.x,(float)b.y,(float)b.z, orientation);
	}

	private static float[] f = new float[12];
	private static int[] strips = new int[] { 4 };
	private static float[] g = new float[8];

	protected static GeometryArray createGeometry(float ax, float ay, float az,
												  float bx, float by, float bz,
												  float as, float at,
												  float bs, float bt,
												  int orientation)
	{
		int i=0;

		switch (orientation) {
		case XY_PLANE:
			f[i++] = ax; f[i++] = ay; f[i++] = az;
			f[i++] = bx; f[i++] = ay; f[i++] = (az+bz)/2;
			f[i++] = bx; f[i++] = by; f[i++] = bz;
			f[i++] = ax; f[i++] = by; f[i++] = (az+bz)/2;
			break;

		case XZ_PLANE:
			f[i++] = ax; f[i++] = ay; f[i++] = az;
			f[i++] = bx; f[i++] = (ay+by)/2; f[i++] = az;
			f[i++] = bx; f[i++] = by; f[i++] = bz;
			f[i++] = ax; f[i++] = (ay+by)/2; f[i++] = bz;
			break;

		case YZ_PLANE:
			f[i++] = ax; f[i++] = ay; f[i++] = az;
			f[i++] = (ax+bx)/2; f[i++] = by; f[i++] = az;
			f[i++] = bx; f[i++] = by; f[i++] = bz;
			f[i++] = (ax+bx)/2; f[i++] = ay; f[i++] = bz;
			break;
		}

		//	texture coordinates
		i=0;
		g[i++] = as; g[i++] = at;
		g[i++] = bs; g[i++] = at;
		g[i++] = bs; g[i++] = bt;
		g[i++] = as; g[i++] = bt;

		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(f);
		gi.setStripCounts(strips);
		gi.setTextureCoordinates2(g);

//		Triangulator tr = new Triangulator();
//		tr.triangulate(gi);
		
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		
		Stripifier st = new Stripifier();
		st.stripify(gi);
		
		return gi.getGeometryArray();
	}
	
/*
1. QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
2. | GeometryArray.TEXTURE_COORDINATE_2);
3. Point3f p = new Point3f();
4. p.set(-1.0f, 1.0f, 0.0f);
5. plane.setCoordinate(0, p);
6. p.set(-1.0f, -1.0f, 0.0f);
7. plane.setCoordinate(1, p);
8. p.set( 1.0f, -1.0f, 0.0f);
9. plane.setCoordinate(2, p);
10. p.set( 1.0f, 1.0f, 0.0f);
11. plane.setCoordinate(3, p);
12.
13. Point2f q = new Point2f();
14. q.set(0.0f, 1.0f);
15. plane.setTextureCoordinate(0, q);
16. q.set(0.0f, 0.0f);
17. plane.setTextureCoordinate(1, q);
18. q.set(1.0f, 0.0f);
19. plane.setTextureCoordinate(2, q);
20. q.set(1.0f, 1.0f);
21. plane.setTextureCoordinate(3, q);
*/
}
