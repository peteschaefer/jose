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

import javax.media.j3d.Geometry;
import javax.media.j3d.Shape3D;

public class FlatPolygon
        extends Shape3D
{
    /**
     * create a flat polygon from two-dimensional coordinates
     * the polygon will be located in the x-y plane
     *
     * @param f an array of x, y and z coordinates (interleaved)
     */
    public FlatPolygon(float[] f, boolean geobyref)
    {
        super();

        int[] strips = new int[] { f.length/3 };

		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(f);
        gi.setStripCounts(strips);

        NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);

//       	Triangulator tr = new Triangulator();
//		tr.triangulate(gi);

		Stripifier st = new Stripifier();
		st.stripify(gi);

        Geometry geo = gi.getGeometryArray();
        if (geobyref)
            geo.setDuplicateOnCloneTree(false);
		setGeometry(geo);
    }
}
