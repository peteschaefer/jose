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

package de.jose.devtools;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Stripifier;
import com.sun.j3d.utils.geometry.Triangulator;
import com.sun.j3d.utils.scenegraph.io.NamedObjectException;
import com.sun.j3d.utils.scenegraph.io.SceneGraphFileWriter;
import com.sun.j3d.utils.scenegraph.io.SceneGraphStreamWriter;
import com.sun.j3d.utils.scenegraph.io.UnsupportedUniverseException;
import de.jose.util.file.FileUtil;
import de.jose.util.map.MapUtil;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author peter.schaefer
 * @author $Author: $
 * @version "$Revision:  $","$Date:  $"
 */
public class Jo3DFileWriter
{
	/**	file info tag	*/
	private HashMap ftag;
	/**	branch graph; collects shape objects	*/
	private BranchGroup branch;
	/**	stripifier used to optimize geometry	*/
	private Stripifier stripifier;
	private Triangulator triangulator;
	private NormalGenerator normalGenerator;
	/**	convert TriangleArrays into TriangleStripArrays (strongly recommended !!)	*/
	private boolean stripify;
	/**	generate normals (recommended)	*/
	private boolean generateNormals;
	/**	use geometry by reference	*/
	private boolean geometryByReference;
	/**	use interleaved geometry	*/
	private boolean interleavedGeometry;

	private static Random random = new Random();

	public Jo3DFileWriter()
	{
		ftag = new HashMap();
		branch = new BranchGroup();
		stripifier = new Stripifier(Stripifier.COLLECT_STATS);
		triangulator = new Triangulator();
		normalGenerator = new NormalGenerator();
		stripify = true;
		generateNormals = true;
		geometryByReference = true;
		interleavedGeometry = true;
	}


	public void writeZip(File file)
		throws IOException
	{
		FileOutputStream fout = new FileOutputStream(file);
		ZipOutputStream out = new ZipOutputStream(fout);
		ZipEntry zety = new ZipEntry(FileUtil.setExtension(file.getName(),"j3ds"));
		out.putNextEntry(zety);
		writeStream(out);
		out.closeEntry();
		out.close();
		fout.close();
	}

	public void writeFile(File file)
		throws IOException
	{
		SceneGraphFileWriter writer;
        try {
            writer = new SceneGraphFileWriter(file,null,false, "Java3D Scenegraph",ftag);
        } catch (UnsupportedUniverseException uuex) {
            //  UnsupportedUniverseException is my facvorite Exception ;-)
            throw new IOException(uuex.getMessage());
        }

		writer.writeBranchGraph(branch,null);
		writer.close();
  	}

    public void writeStream(File file)
        throws IOException
    {
        FileOutputStream stream = new FileOutputStream(file);
        writeStream(stream);
        stream.close();
    }

    public void writeStream(OutputStream out)
        throws IOException
    {
        SceneGraphStreamWriter writer = new SceneGraphStreamWriter(out);

        branch.setUserData(ftag);
        try {
            writer.writeBranchGraph(branch,new HashMap());
        } catch (NamedObjectException noex) {
            throw new IOException(noex.getMessage());
        }
        writer.close();
    }

	public void setFileVersion(int version)
	{
		MapUtil.put(ftag,"version", version);
	}

	public void setTitle(String title, String lang)
	{
		if (lang==null || lang.length()==0)
			ftag.put("title", title);
		else
			ftag.put("title."+lang,title);
	}

	public void setAuthor(String author)						{ ftag.put("author", author); }

	public void setGeometryByReference(boolean byRef)			{ geometryByReference = byRef; }

	public void setInterleavedGeometry(boolean interleaved)		{ interleavedGeometry = interleaved; }

	public void setStripify(boolean stripy)						{ stripify = stripy; }

	public void setGenerateNormals(boolean gener)				{ generateNormals = gener; }

    public HashMap getFileParams()                          { return ftag; }

	public void add(Shape3D shape, Transform3D offset, String name, int lod,
                    float lod_threshhold, double creaseAngle)
	{
        HashMap tag = new HashMap();
		tag.put("name", name);
		MapUtil.put(tag,"lod", lod);
		MapUtil.put(tag,"lod_thresh", lod_threshhold);

        add(shape,offset,tag,creaseAngle);
    }

    public void add(Shape3D shape, Transform3D offset, HashMap tag, double creaseAngle)
    {
		Point3f lower = new Point3f();
		Point3f upper = new Point3f();
		//	clean up the shape's geometry

		for (int i=0; i<shape.numGeometries(); i++)
		{
			Geometry geo = shape.getGeometry(i);
			if (geo instanceof GeometryArray) {
				geo = analyzeGeometry((GeometryArray)geo, offset, lower, upper, creaseAngle);
				shape.setGeometry(geo,i);
			}
		}

		Bounds bounds = new BoundingBox(new Point3d(lower), new Point3d(upper));
		shape.setUserData(tag);
		shape.setBounds(bounds);
		shape.setCollisionBounds(bounds);
		//	don't need any of these:
		shape.setAppearance(null);

	    String name = (String)tag.get("name");
	    int lod = MapUtil.get(tag,"lod",0);
		String key = name+"/"+lod;
		branch.addChild(shape);
	}

	public void printStats()
	{
		System.out.println(stripifier.getStripifierStats());
	}

	public static boolean isSet(int x, int flags)	{ return (x&flags)==flags; }

	/**
	 * optimize shape geometry
	 */
	private GeometryArray analyzeGeometry(GeometryArray geo, Transform3D offset,
										  Point3f lower, Point3f upper, double creaseAngle)
	{
		GeometryInfo gi = new GeometryInfo(geo);

		//	first, generate normals (if not already done)
        if (generateNormals /*&& ! isSet(geo.getVertexFormat(),GeometryArray.NORMALS)*/)
        {
            normalGenerator.setCreaseAngle(creaseAngle);
			normalGenerator.generateNormals(gi);
        }

		switch (gi.getPrimitive())
		{
		case GeometryInfo.POLYGON_ARRAY:
		case GeometryInfo.QUAD_ARRAY:
			triangulator.triangulate(gi); break;
		}

		if (stripify)		//	prefer triangle strips
		{
			if (gi.getPrimitive()==GeometryInfo.TRIANGLE_ARRAY)
				stripifier.stripify(gi);
		}

		//	apply offset
		Point3f[] ps = gi.getCoordinates();
		if (offset != null)
		{
			for (int i=0; i<ps.length; i++)
				offset.transform(ps[i]);

			Vector3f[] nrms = gi.getNormals();
			for (int i=0; i<nrms.length; i++)
				offset.transform(nrms[i]);
		}

		//	calc bounds
		for (int i=0; i<ps.length; i++)
		{
			Point3f p = ps[i];
			if (p.x < lower.x) lower.x = p.x;
			if (p.y < lower.y) lower.y = p.y;
			if (p.z < lower.z) lower.z = p.z;
			if (p.x > upper.x) upper.x = p.x;
			if (p.y > upper.y) upper.y = p.y;
			if (p.z > upper.z) upper.z = p.z;
		}

	return gi.getGeometryArray(geometryByReference, geometryByReference && interleavedGeometry, false);
	//	return gi.getIndexedGeometryArray(false, geometryByReference, geometryByReference && interleavedGeometry, false, false);

	}

	public void randomize(GeometryInfo gi)
	{
		Point3f[] x = gi.getCoordinates();
		Vector3f[] n = gi.getNormals();

		//	we assume an indexed triangle array; every three indices define a triangle
		for (int i=x.length-3; i>=0; i -= 3)
		{
			int j = (Math.abs(random.nextInt()) % (x.length/3)) * 3;
			//	swap i <--> j
			swap(x,i,j);
			swap(x,i+1,j+1);
			swap(x,i+2,j+2);
			swap(n,i,j);
			swap(n,i+1,j+1);
			swap(n,i+2,j+2);
		}
	}

	private static final void swap(Object[] a, int i, int j)
	{
		Object aux = a[i];
		a[i] = a[j];
		a[j] = aux;
	}

} // class Jo3DFileWriter

/*
 * $Log: $
 *
 */

