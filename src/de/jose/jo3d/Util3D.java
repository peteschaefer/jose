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

import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.geometry.GeometryInfo;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

/**
 * some utilities for 3D and vector math
 */

public class Util3D
{
	private static Transform3D aux = new Transform3D();
	private static Vector3d auxv = new Vector3d();
	private static Point3d auxp = new Point3d();

	/**	this transform matrix hegates of the z-values
	 * 	(creating a reflection about the x-y-plane)
	 */
	private static double[] mirrorzvalues = {
		1, 0, 0, 0,
		0, 1, 0, 0,
		0, 0,-1, 0,
		0, 0, 0, 1,
	};

	public static Transform3D rotZ180 = new Transform3D();
	public static Transform3D mirrorZ = new Transform3D(mirrorzvalues);
	public static final Color3f white3f = new Color3f(Color.white);
	public static final Color3f black3f = new Color3f(Color.black);

	static {
		rotZ180.rotZ(Math.PI);
	}

	public static Transform3D translate(Transform3D tf, double x, double y, double z)
	{
		auxv.set(x,y,z);
		aux.setIdentity();
		aux.setTranslation(auxv);
		tf.mul(aux);
		return tf;
	}

	public static Transform3D setTranslation(Transform3D tf, double x, double y, double z)
	{
		auxv.set(x,y,z);
		tf.setTranslation(auxv);
		return tf;
	}

	public static Transform3D rotX(Transform3D tf, double angle)
	{
		aux.setIdentity();
		aux.rotX(angle);
		tf.mul(aux);
		return tf;
	}

	public static Transform3D rotZ(Transform3D tf, double angle)
	{
		aux.setIdentity();
		aux.rotZ(angle);
		tf.mul(aux);
		return tf;
	}

    public static Transform3D rotY(Transform3D tf, double angle)
	{
		aux.setIdentity();
		aux.rotY(angle);
		tf.mul(aux);
		return tf;
	}

	public static Vector3f mirrorZ(Tuple3f v)
	{
		Vector3f result = new Vector3f(v);
		result.z = -result.z;
		return result;
	}

	public static Vector3d mirrorZ(Tuple3d v)
	{
		Vector3d result = new Vector3d(v);
		result.z = -result.z;
		return result;
	}

	public static Transform3D getScale(double x, double y, double z)
	{
		auxv.set(x,y,z);
		Transform3D tf = new Transform3D();
		tf.setScale(auxv);
		return tf;
	}

	public static Vector3d negate(Vector3d tup)
	{
		return (Vector3d)negate((Tuple3d)tup);
	}

	public static Tuple3d negate(Tuple3d tup)
	{
		Tuple3d result = (Tuple3d)tup.clone();
		result.negate();
		return result;
	}

	public static void translateTo(TransformGroup tg, Vector3d v)
	{
		aux.setIdentity();
		aux.set(v);
		tg.setTransform(aux);
	}

	public static void scale(Transform3D tf, double x, double y, double z)
	{
		auxv.set(x,y,z);
		aux.setIdentity();
		aux.setScale(auxv);
		tf.mul(aux);
	}

	public static Appearance createAppearance()
	{
		Material mat = new Material();
		mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
		mat.setDiffuseColor(Util3D.white3f);
		mat.setAmbientColor(Util3D.white3f);
		mat.setSpecularColor(Util3D.white3f);
		mat.setLightingEnable(true);

		Appearance app = new Appearance();
		app.setMaterial(mat);

		app.setCapability(Appearance.ALLOW_MATERIAL_READ);
		app.setCapability(Appearance.ALLOW_TEXTURE_READ);
		app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		app.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);

		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.MODULATE);
		ta.setCapability(TextureAttributes.ALLOW_TRANSFORM_READ);
		ta.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);

		//	MODULATE, DECAL, BLEND, REPLACE, or COMBINE
		ta.setPerspectiveCorrectionMode(TextureAttributes.FASTEST);
		//	FASTEST or NICEST
		app.setTextureAttributes(ta);

		return app;
	}

	public static Appearance createAppearance(int culling)
	{
		Appearance app = createAppearance();
		setCullFace(app, culling);
		return app;
	}

	public static Appearance createAppearance(int culling, float shininess, float transparency)
	{
		Appearance app = createAppearance(culling);
		app.getMaterial().setShininess(shininess);

		setTransparency(app,transparency);
		app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
		app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		app.getTransparencyAttributes().setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);

		return app;
	}

	public static Appearance createAppearance(Vector4f splane, Vector4f tplane, Transform3D transform)
	{
		Appearance app = createAppearance();

		TexCoordGeneration tcg = new TexCoordGeneration(
											TexCoordGeneration.OBJECT_LINEAR,
											TexCoordGeneration.TEXTURE_COORDINATE_2);
		if (splane != null)	tcg.setPlaneS(splane);
		if (tplane != null) tcg.setPlaneT(tplane);
		app.setTexCoordGeneration(tcg);

		if (transform != null)
			app.getTextureAttributes().setTextureTransform(transform);

		return app;
	}

	public static void setCullFace(Appearance app, int culling)
	{
		PolygonAttributes pattr = app.getPolygonAttributes();
		if (pattr==null) {
			pattr = new PolygonAttributes();
			app.setPolygonAttributes(pattr);
		}
		pattr.setCullFace(culling);
	}

	public static void setPolygonOffset(Appearance app, float offset)
	{
		PolygonAttributes pattr = app.getPolygonAttributes();
		if (pattr==null) {
			pattr = new PolygonAttributes();
			app.setPolygonAttributes(pattr);
		}
		pattr.setPolygonOffset(offset);
	}

	public static void setColor(Appearance app, Color col)
	{
		Material mat = app.getMaterial();
		if (mat==null) {
			mat = new Material();
			app.setMaterial(mat);
		}
		mat.setAmbientColor(new Color3f(col));
		app.setMaterial(mat);
	}

	public static void setTransparency(Appearance app, float transparency)
	{
		TransparencyAttributes ta = app.getTransparencyAttributes();
		if (ta==null) {
			ta = new TransparencyAttributes();
			ta.setTransparencyMode(TransparencyAttributes.FASTEST);
			ta.setTransparency(transparency);
			app.setTransparencyAttributes(ta);
		}
		ta.setTransparency(transparency);
	}

	public static void setIgnoreVertexColors(Appearance app, boolean ignore)
	{
		RenderingAttributes attr = app.getRenderingAttributes();
		if (attr==null) {
			attr = new RenderingAttributes();
			attr.setCapability(RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE);
			app.setRenderingAttributes(attr);
		}
		attr.setIgnoreVertexColors(ignore);
	}

	public static void setShadeModel(Appearance app, int model)
	{
		ColoringAttributes attr = app.getColoringAttributes();
		if (attr==null) {
			attr = new ColoringAttributes();
			attr.setCapability(ColoringAttributes.ALLOW_SHADE_MODEL_WRITE);
			attr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
			app.setColoringAttributes(attr);
		}
		attr.setShadeModel(model);
	}
/*
	public static Appearance createAppearance(String textureFile, float scale, Component observer)
		throws IOException
	{
		TextureLoader loader = new TextureLoader(textureFile,observer);
		ImageComponent2D i2d = loader.getImage();

		Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, 256,256);
		tex.setImage(0,i2d);

		Appearance app = new Appearance();

		Material mat = new Material();

		Transform3D textureTransform = new Transform3D();
		textureTransform.setScale(scale);	//	depending on the size of the texture & the screen

		TexCoordGeneration tcg = new TexCoordGeneration(
							TexCoordGeneration.EYE_LINEAR,
							TexCoordGeneration.TEXTURE_COORDINATE_3);

		TextureAttributes ta = new TextureAttributes(
							TextureAttributes.MODULATE,
							textureTransform,
							new Color4f(Color.white),
							TextureAttributes.NICEST);

		app.setTexCoordGeneration(tcg);
		app.setMaterial(mat);
		app.setTextureAttributes(ta);
		app.setTexture(tex);

		return app;
	}
*/
	/**
	 *		result = p + d*v
	 */
	public static void add(Point3d p, double d, Vector3d v, Point3d result)
	{
		result.x = p.x+d*v.x;
		result.y = p.y+d*v.y;
		result.z = p.z+d*v.z;
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @param z z-coordinate of plan
	 * @return the intersection point, null if parallel
	 */
	public static Point3d intersectRayZ(Point3d p, Vector3d v, double z)
	{
		double dist = distanceFromZ(p,v,z);
		if (dist==Double.POSITIVE_INFINITY)
			return p;
		else if (dist==Double.NaN)
			return null;
		else {
			Point3d result = new Point3d();
			add(p,dist,v, result);
			return result;
		}
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @param z z-coordinate of plan
	 * @return the distance from the starting point to the intersection point,
	 * 	Double.POSITIVE_INFINITY if the ray lies within the plane
	 *  Double.NaN if the ray is parallel to the plane
	 */
	public static double distanceFromZ(Point3d p, Vector3d v, double z)
	{
		if (v.z==0.0) {
			if (p.z==z)
				return Double.POSITIVE_INFINITY;		//	ray lies in the plane, infinitely many intersections
			else
				return Double.NaN;						//	ray is parallel, no intersection
		}
		else
			return (z-p.z)/v.z;
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @param x x-coordinate of plane
	 * @return the intersection point, null if parallel
	 */
	public static Point3d intersectRayX(Point3d p, Vector3d v, double x)
	{
		double dist = distanceFromX(p,v,x);
		if (dist==Double.POSITIVE_INFINITY)
			return p;
		else if (dist==Double.NaN)
			return null;
		else {
			Point3d result = new Point3d();
			add(p,dist,v, result);
			return result;
		}
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @param x x-coordinate of plan
	 * @return the distance from the starting point to the intersection point,
	 * 	Double.POSITIVE_INFINITY if the ray lies within the plane
	 *  Double.NaN if the ray is parallel to the plane
	 */
	public static double distanceFromX(Point3d p, Vector3d v, double x)
	{
		if (v.x==0.0) {
			if (p.x==x)
				return Double.POSITIVE_INFINITY;		//	ray lies in the plane, infinitely many intersections
			else
				return Double.NaN;						//	ray is parallel, no intersection
		}
		else
			return (x-p.x)/v.x;
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @return the intersection point, null if parallel
	 */
	public static Point3d intersectRayY(Point3d p, Vector3d v, double y)
	{
		double dist = distanceFromY(p,v,y);
		if (dist==Double.POSITIVE_INFINITY)
			return p;
		else if (dist==Double.NaN)
			return null;
		else {
			Point3d result = new Point3d();
			add(p,dist,v, result);
			return result;
		}
	}

	/**
	 * interset a ray with a plane that is parallel to the x-y-plane
	 *
	 * @param p	start of ray
	 * @param v ray direction
	 * @param y y-coordinate of plan
	 * @return the distance from the starting point to the intersection point,
	 * 	Double.POSITIVE_INFINITY if the ray lies within the plane
	 *  Double.NaN if the ray is parallel to the plane
	 */
	public static double distanceFromY(Point3d p, Vector3d v, double y)
	{
		if (v.y==0.0) {
			if (p.y==y)
				return Double.POSITIVE_INFINITY;		//	ray lies in the plane, infinitely many intersections
			else
				return Double.NaN;						//	ray is parallel, no intersection
		}
		else
			return (y-p.y)/v.y;
	}


	/**
	 * intersect a ray with a orthogonal box
	 * @return the number of intersections (0,1, or 2)
	 */
	public static int intersectRay(Point3d p, Vector3d v,
								   Bounds bounds,
								  Point3d r1, Point3d r2)
	{
		if (bounds instanceof BoundingBox)
			return intersectRay(p,v, (BoundingBox)bounds, r1,r2);
		else if (bounds instanceof BoundingSphere)
			return intersectRay(p,v, (BoundingSphere)bounds, r1,r2);
		else
			throw new IllegalArgumentException("only BoundingBox and BoundingSphere are supported");
	}

	/**
	 * intersect a ray with a orthogonal box
	 * @return the number of intersections (0,1, or 2)
	 */
	public static int intersectRay(Point3d p, Vector3d v,
								   BoundingBox box,
								  Point3d r1, Point3d r2)
	{
		Point3d b1 = new Point3d();
		Point3d b2 = new Point3d();
		box.getLower(b1);
		box.getUpper(b2);

		return intersectRay(p,v, b1,b2, r1,r2);
	}

	/**
	 * intersect a ray with a orthogonal box
	 * @param p start of ray
	 * @param v direcetion of ray
	 * @param b1 lower corner of box
	 * @param b2 upper corner of box
	 * @param r1 result
	 * @param r2 result
	 * @return the number of intersections (0,1, or 2)
	 */
	public static int intersectRay(Point3d p, Vector3d v,
								  Point3d b1, Point3d b2,
								  Point3d r1, Point3d r2)
	{

		int result = 0;
		double d1 = 0.0;

		double[] d = {
			distanceFromX(p,v, b1.x),
			distanceFromX(p,v, b2.x),
			distanceFromY(p,v, b1.y),
			distanceFromY(p,v, b2.y),
			distanceFromZ(p,v, b1.z),
			distanceFromZ(p,v, b2.z),
		};

		for (int i=0; i<6; i++)
		{
			if (d[i]==Double.NaN || d[i]==Double.POSITIVE_INFINITY)	continue;

			add(p,d[i],v, auxp);
			if (isInside(auxp, b1,b2))
			{
				//	a hit !
				if (++result==1)
				{
					d1 = d[i];
					r1.set(auxp);
				}
				else
				{
					if (d[i] < d1)
					{	//	swap
						r2.set(r1);
						r1.set(auxp);
					}
					else
						r2.set(auxp);
					break;
				}
			}
		}
		return result;
	}


	/**
	 * intersect a ray with a sphere
	 *
	 * @return the number of intersections (0,1, or 2)
	 */
	public static int intersectRay(Point3d p, Vector3d v,
								   BoundingSphere  sphere,
								  Point3d r1, Point3d r2)
	{
		Point3d c = new Point3d();
		sphere.getCenter(c);

		return intersectRay(p,v, c,sphere.getRadius(), r1,r2);
	}

	/**
	 * intersect ray with a sphere
	 * @param p	start point of ray
	 * @param v direction of ray
	 * @param c center of sphere
	 * @param r radius of sphere
	 * @param r1 result
	 * @param r2 result
	 * @return number of intersections (0,1, or 2)
	 */
	public static int intersectRay(Point3d p, Vector3d v,
								   Point3d c, double r,
								   Point3d r1, Point3d r2)
	{
		/**
		 * go to solve the quadratic equation:
		 *
			d²f  + d g + h = 0

			where
		 	q := p-c

			f := vx² + vy² + vz²
			g := 2 (vx qx + vy qy + vz qz)
			h := (qx² + qy² + qz²) - r²

		 */

		if (v.x==0 && v.y==0 && v.z==0) throw new IllegalArgumentException("direction vector must not be 0");

		Point3d q = new Point3d(p);
		q.sub(c);

		double f = v.x*v.x + v.y*v.y + v.z*v.z;
		double g = 2 * (v.x*q.x + v.y*q.y + v.z*q.z) / f;
		double h = (q.x*q.x + q.y*q.y + q.z*q.z - r*r) / f;

		/**	now solve
				d² + d g + h = 0

			the solutions are
				d = -g/2 +/- sqrt (g²/4 - h)
		 */

		double discr = g*g/4-h;

		if (discr < 0)
			return 0;	//	no solutions -> no intersection
		else if (discr == 0.0)
		{
			//	1 intersection (tangent)
			add(p, -g/2, v, r1);
			return 0;
		}
		else {
			//	2 intersections
			double x = Math.sqrt(discr);
			add(p, -g/2-x, v, r1);
			add(p, -g/2+x, v, r2);
			return 2;
		}
	}

	/**
	 * is te given point in a box ?
	 * @param p
	 * @param lower lower corner of box
	 * @param upper upper corner of box
	 * @return true if p is inside the box
	 */
	public static boolean isInside(Point3d p, Point3d lower, Point3d upper)
	{
		return (p.x >= lower.x) && (p.x <= upper.x) &&
				(p.y >= lower.y) && (p.y <= upper.y) &&
				(p.z >= lower.z) && (p.z <= upper.z);
	}

	/** converts vector to a point	 */
	public static final Point3d vtop(Vector3d v)	{ return new Point3d(v.x,v.y,v.z); }
	public static final Point3f vtop(Vector3f v)	{ return new Point3f(v.x,v.y,v.z); }

	public static final Point3d vtopd(Vector3f v)	{ return new Point3d(v.x,v.y,v.z); }
	public static final Point3f vtopf(Vector3d v)	{ return new Point3f((float)v.x,(float)v.y,(float)v.z); }

	/** converts a point to a vector	 */
	public static final Vector3d ptov(Point3d p)	{ return new Vector3d(p.x,p.y,p.z); }
	public static final Vector3f ptov(Point3f p)	{ return new Vector3f(p.x,p.y,p.z); }

	public static final Point3d pftopd(Point3f p)	{ return new Point3d(p.x,p.y,p.z); }
	public static final Point3f pdtopf(Point3d p)	{ return new Point3f((float)p.x, (float)p.y, (float)p.z); }

	/**
	 * convert local coordinates to on-screen coordinates,
	 * relative to the upper-left corner of the AWT window
	 */
	public static Point localToAWT(Canvas3D canvas, Node nd, Point3d p)
	{
		auxp.set(p);
		nd.getLocalToVworld(aux);
		aux.transform(auxp);
		//	now we got VWorld coordinates; these are projected to the Image Plate
		canvas.getVworldToImagePlate(aux);
		aux.transform(auxp);
		//	now project the Imape Plate coordinates to the AWT coordinate system
		Point2d result = new Point2d();
		canvas.getPixelLocationFromImagePlate(auxp,result);
		return new Point((int)result.x, (int)result.y);
	}

	/**	calculate a bounding box for a geometry
	 */
	public static BoundingBox calcBounds(Shape3D shape)
	{
		Point3f lower = new Point3f(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
		Point3f upper = new Point3f(-Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE);

		Enumeration geoms = shape.getAllGeometries();
		while (geoms.hasMoreElements())
			calcBounds((GeometryArray)geoms.nextElement(), lower,upper);

		return new BoundingBox(pftopd(lower), pftopd(upper));
	}

	public static BoundingBox calcBounds(GeometryArray geom)
	{
		Point3f lower = new Point3f(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
		Point3f upper = new Point3f(-Float.MAX_VALUE,-Float.MAX_VALUE,-Float.MAX_VALUE);

		calcBounds(geom, lower,upper);

		return new BoundingBox(pftopd(lower), pftopd(upper));
	}

	private static void calcBounds(GeometryArray geom, Point3f lower, Point3f upper)
	{
		int max = 3*geom.getVertexCount();
		float[] v = new float[3*256];
		float x,y,z;

		for (int offset = 0; offset < max; offset += v.length)
		{
			if (max-offset < v.length)
				v = new float[max-offset];

			geom.getCoordinates(offset/3, v);

			for (int j = v.length-1; j >= 0; )
			{
				z = v[j--];
				y = v[j--];
				x = v[j--];

				if (x<lower.x) lower.x = x;
				if (x>upper.x) upper.x = x;
				if (y<lower.y) lower.y = y;
				if (y>upper.y) upper.y = y;
				if (z<lower.z) lower.z = z;
				if (z>upper.z) upper.z = z;
			}
		}
	}

	/**	set all necessary capabilities for picking a shape
	 */
	public static void setPickCapabilities(Shape3D shape)
	{
		Enumeration enums = shape.getAllGeometries();
		while (enums.hasMoreElements())
		{
			Geometry geo = (Geometry)enums.nextElement();
			geo.setCapability(Geometry.ALLOW_INTERSECT);
			geo.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
			geo.setCapability(GeometryArray.ALLOW_FORMAT_READ);
			geo.setCapability(GeometryArray.ALLOW_COUNT_READ);
			//	needed for picking !
		}

		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
//		shape.setBoundsAutoCompute(true);
		shape.setPickable(true);
	}

	public static int countPolygons(Shape3D shape)
	{
		int result = 0;
		Enumeration geos = shape.getAllGeometries();
		while (geos.hasMoreElements())
			result += countPolygons((Geometry)geos.nextElement());
		return result;
	}

	public static int countPolygons(Geometry geo)
	{
		if (geo instanceof GeometryArray) {
			GeometryArray ga = (GeometryArray)geo;
			return ga.getValidVertexCount();
		}
		//	else: dunno how to count vertices on CompressedGeometry ;-(
		return 0;
	}

	public static int countPolygons(Group g)
	{
		int result = 0;
		Enumeration en = g.getAllChildren();
		while (en.hasMoreElements())
		{
			Node n = (Node)en.nextElement();
			if (n instanceof Shape3D)
				result += countPolygons((Shape3D)n);
			else if (n instanceof Group)
				result += countPolygons((Group)n);
		}
		return result;
	}

	public static void repack(Shape3D shape, boolean ref)
	{

		for (int i=shape.numGeometries()-1; i >= 0; i--)
		{
			Geometry geo = shape.getGeometry(i);
			if (geo instanceof GeometryArray)
			{
				GeometryArray geoa = (GeometryArray)geo;
				boolean isRef = (geoa.getVertexCount() & GeometryArray.BY_REFERENCE) != 0;

				if (ref != isRef)
				{
					GeometryInfo geoi = new GeometryInfo(geoa);
					geoa = geoi.getGeometryArray(ref,false,false);
					shape.setGeometry(geoa, i);
				}
			}
			else if (geo instanceof CompressedGeometry)
			{
				CompressedGeometry cgeo = (CompressedGeometry)geo;
				if (cgeo.isByReference() && !ref)
					System.err.println("compressed geometry is by reference");
				if (!cgeo.isByReference() && ref)
					System.err.println("compressed geometry is not by reference");
			}
			else
				System.err.println(geo.getClass().getName()+" geometry not supported");
		}
	}

	public static Shape3D[] collectShapes(Scene scene)
	{
		BranchGroup group = scene.getSceneGroup();
		Vector collect = new Vector();
		collectShapes(group, collect);

		Shape3D[] shapes = new Shape3D[collect.size()];
		collect.toArray(shapes);
		return shapes;
	}

	public static void collectShapes(Group grp, Collection collect)
	{
		Enumeration children = grp.getAllChildren();
		while (children.hasMoreElements())
		{
			Object obj = children.nextElement();
			if (obj instanceof Shape3D)
				collect.add(obj);
			else if (obj instanceof Group)
				collectShapes((Group)obj, collect);
			//	else: ignore
		}
	}


	public static Shape3D[] toShapes(Geometry[] geos)
	{
		Shape3D[] shapes = new Shape3D[geos.length];
		for (int i=0; i<shapes.length; i++)
		{
			shapes[i] = new Shape3D();
			shapes[i].setGeometry(geos[i]);
		}

		return shapes;
	}


    private static String nativeVersion = null;
	private static String graphicsCard = null;
    private static boolean anisotropic = false;
    private static boolean fsaa = false;

	/**
     * retrieve version info for the current Java3D package
	 * @param canvas a Canvas3D (declared as Object so that calling class need not import it!)
     */
    private static void getVersionInfo(Object canvas)
    {
        if (canvas==null) {
 /*           GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();

            /* We need to set this to force choosing a pixel format
               that support the canvas
            *
            template.setSceneAntialiasing(template.PREFERRED);

            GraphicsConfiguration config =
            GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getBestConfiguration(template);
            canvas = new Canvas3D(config);
            /*  this is likely to crash he JVM so we better return nothing
*/
            nativeVersion = null;
            anisotropic = false;
            fsaa = false;
            return;
        }

    	Map c3dMap = ((Canvas3D)canvas).queryProperties();

	    if (c3dMap.containsKey("native.renderer"))
	        graphicsCard = c3dMap.get("native.vendor")+" "+c3dMap.get("native.renderer");
	    else
	        graphicsCard = "";

	    nativeVersion = (String)c3dMap.get("native.version");

        anisotropic =  ((Number)c3dMap.get("textureAnisotropicFilterDegreeMax")).doubleValue() > 1.0;
        fsaa = ((Boolean)c3dMap.get("sceneAntialiasingAvailable")).booleanValue();

        int passes = ((Number)c3dMap.get("sceneAntialiasingNumPasses")).intValue();
        //  passes = 0  FSAA disabled
        //  passes = 1  driver supports multisampling (fast)
        //  passes = 8  FSAA is emulated (veeery slow)

    }

	/**
	 * @param canvas a Canvas3D (declared as Object so that calling class need not import it!)
	 */
    public static String getNativeVersion(Object canvas)
    {
        if (nativeVersion==null) getVersionInfo(canvas);
        return nativeVersion;
    }

	/**
	 * @param canvas a Canvas3D (declared as Object so that calling class need not import it!)
	 */
    public static String getGraphicsCard(Object canvas)
    {
        if (graphicsCard==null) getVersionInfo(canvas);
        return graphicsCard;
    }

	/**
	 * @param canvas a Canvas3D (declared as Object so that calling class need not import it!)
	 */
    public static boolean hasAnisotropicFiltering(Object canvas)
    {
        if (nativeVersion==null) getVersionInfo(canvas);
        return anisotropic;
    }

	/**
	 * @param canvas a Canvas3D (declared as Object so that calling class need not import it!)
	 */
    public static boolean hasFullScreenAntiAliasing(Object canvas)
    {
        if (nativeVersion==null) getVersionInfo(canvas);
        return fsaa;
    }

}
