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

import de.jose.jo3d.interpolators.KBKeyFrame;

import javax.media.j3d.Transform3D;
import javax.vecmath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class SplineKeyFrame
        implements Cloneable
{
	/**	alpha threshold	*/
	protected float alphaWeight;

	/**	cartesian location	*/
	protected Point3d		pos;
	/**	polar location	(for reference)	*/
	protected Polar3d		polar;

	/**	heading: rotation about the Y axis	*/
	protected double heading;
	/**	pitch: rotation about the X axis	*/
	protected double pitch;
	/**	bank: rotation about the Z axis	*/
	protected double bank;
    /** camera vector in cartesian coordinates (radius=1)   */
    protected Vector3d  camera;

    protected double tension,continuity,bias;

	/**	scale	*/
	protected double	scale;


	public SplineKeyFrame()
	{
		alphaWeight = 1.0f;
		pos = new Point3d();
		polar = new Polar3d();
		heading = 0.0;
		pitch = 0.0;
		bank = 0.0;
        camera = new Vector3d();
		scale = 1.0;
        tension = 0.0;
        continuity = 0.0;
        bias = 0.0;
	}

	public SplineKeyFrame(float alphaWght, Tuple3d p, Tuple3d cameraAngle, double scl)
	{
		alphaWeight = alphaWght;
		pos = new Point3d(p);
		polar = new Polar3d();
		polar.setCartesianCoordinates(pos);
		heading = cameraAngle.x;
		pitch = cameraAngle.y;
		bank = cameraAngle.z;
        camera = new Vector3d();
        updateCameraVector();
		scale = scl;
	}

	public Object clone()
	{
		try {
			SplineKeyFrame copy = (SplineKeyFrame)super.clone();
			copy.pos = (Point3d)copy.pos.clone();
			copy.polar = (Polar3d)copy.polar.clone();
            copy.camera = (Vector3d)copy.camera.clone();
			return copy;
		} catch (CloneNotSupportedException cnsex) {
			//	never happens
			return null;
		}
	}

	public float getAlphaWeight()			{ return alphaWeight; }

	public Point3d getLocation()			{ return pos; }

	public Polar3d getPolarLocation()		{ return polar; }

	public Vector3d getCameraAngle()		{ return new Vector3d(heading,pitch,bank); }

    /**
     * get the Camere vector in cartesian coordinates
     */
    public Vector3d getCameraVector()       { return camera; }

	public double getHeading()				{ return heading; }
	public double getPitch()				{ return pitch; }
	public double getBank()					{ return bank; }

    private void updateCameraVector()
    {
        Polar3d.toCartesian(bank,-pitch,-1.0, camera);
    }

	public void setLocation(Tuple3d tuple)
	{
		pos.set(tuple);
		polar.setCartesianCoordinates(tuple);
	}

	public void setLocation(Tuple3f tuple)
	{
		setLocation((double)tuple.x, (double)tuple.y, (double)tuple.z);
	}

	public void setLocation(double x, double y, double z)
	{
		pos.set(x,y,z);
		polar.setCartesianCoordinates(x,y,z);
	}


	public void setPolarLocation(Tuple3d tuple)
	{
		polar.set(tuple);
		polar.toCartesian(pos);
	}

	public void setPolarLocation(double longitude, double latitude, double distance)
	{
		polar.setPolarCoordinates(longitude,latitude,distance);
		polar.toCartesian(pos);
	}

	public void setCameraAngle(Tuple3d tuple)
	{
		setCameraAngle(tuple.x,tuple.y,tuple.z);
	}

	public void setCameraAngle(double head, double pit, double bnk)
	{
		heading = head;
		pitch = pit;
		bank = bnk;
        updateCameraVector();
	}

	public void incrementCameraAngle(double head, double pit, double bnk)
	{
		heading += head;
		pitch += pit;
		bank += bnk;
        updateCameraVector();
	}

	public double getScale()					{ return scale; }

	public void setScale(double value)			{ scale=value; }


	public void incrementLocation(double x, double y, double z)
	{
		setLocation(pos.x+x, pos.y+y, pos.z+z);
	}

	public void incrementLocation(Tuple3d t)
	{
		incrementLocation(t.x,t.y,t.z);
	}

	public void rotatePolarLocation(double longi, double lati, double dist)
	{
		polar.incrementLongitude(longi);
		polar.incrementLatitude(lati);
		polar.incrementDistance(dist);

		bank += longi;
		pitch -= lati;
		polar.toCartesian(pos);
        updateCameraVector();
	}


	public Transform3D applyTransform(Transform3D tf)
	{
		if (tf==null) tf = new Transform3D();

		tf.setIdentity();
		if (bank != 0.0)
			Util3D.rotZ(tf, bank);
		if (heading != 0.0)
			Util3D.rotY(tf, -heading);
		if (pitch != 0.0)
			Util3D.rotX(tf, -pitch);

		// Scale the transformation matrix
		if (scale != 1.0)
			Util3D.scale(tf, scale,scale,scale);

		tf.setTranslation(Util3D.ptov(getLocation()));

		return tf;
	}

	public static KBKeyFrame[] toKeyFrames(List frames, int from, int to)
	{
		KBKeyFrame[] kbf = new KBKeyFrame[to-from];

		float alpha = 0.0f;
		float totalAlpha = (float)getTotalAlpha(frames,from,to);
		for (int j=from; j < to; j++)
		{
			SplineKeyFrame frm = (SplineKeyFrame)frames.get(j);
			if (j > 0) alpha += frm.getAlphaWeight();

			Point3f p3f = Util3D.pdtopf(frm.getLocation());
			Point3f scale = new Point3f((float)frm.getScale(), (float)frm.getScale(), (float)frm.getScale());
			kbf[j-from] = new KBKeyFrame(alpha/totalAlpha, 0, p3f,
			        			(float)frm.heading, (float)frm.pitch,  (float)frm.bank,
								scale, (float)frm.tension, (float)frm.continuity, (float)frm.bias);
		}

		return kbf;
	}

	private static void print(PrintWriter out, double value, String separator)
	{
		out.print(value);
        out.print(separator);
	}

	public void print(PrintWriter out, SplineKeyFrame previous)
	{
    	print(out,  alphaWeight, ", ");

        print(out,  pos.x, ",");
		print(out,  pos.y, ",");
		print(out,  pos.z, ", ");

		print(out,  bank, ",");
        print(out,  pitch, ",");
        print(out,  heading, ", ");

	    print(out,  scale, "");
		out.println();
	}

	public static void print(PrintWriter out, List frames, int from, int to)
	{
		out.println("; start of sequence");

		((SplineKeyFrame)frames.get(from)).print(out, null);
		for (int i=from+1; i<to; i++)
			((SplineKeyFrame)frames.get(i)).print(out, (SplineKeyFrame)frames.get(i-1));

		out.println("; end of sequence");
	}

    public static List read(String inputFileName, List result)
        throws IOException
    {
        FileReader reader = new FileReader(inputFileName);
        result = read(reader,result);
        reader.close();
        return result;
    }

    public static List read(InputStream input, List result)
        throws IOException
    {
        Reader reader = new InputStreamReader(input);
        result = read(reader,result);
        input.close();
        return result;
    }

    public static List read(Reader reader, List result)
        throws IOException
    {
        if (result==null) result = new ArrayList();

        BufferedReader breader = new BufferedReader(reader);
        for (;;) {
            String line = breader.readLine();
            if (line==null) break;  //  EOF
            line = line.trim();
            if (line.length()==0 || line.startsWith(";")) continue; //  empty or comment line

            readLine(line, result);
        }


        return result;
    }

    public static void readLine(String line, List result)
    {
        SplineKeyFrame frm;
        if (result.isEmpty())
            frm = new SplineKeyFrame();
        else {
            frm = (SplineKeyFrame)result.get(result.size()-1);
            frm = (SplineKeyFrame)frm.clone();
        }

        StringTokenizer tok = new StringTokenizer(line,", ");

        frm.alphaWeight     = Float.parseFloat(tok.nextToken());
        frm.pos.x           = Float.parseFloat(tok.nextToken());
        frm.pos.y           = Float.parseFloat(tok.nextToken());
        frm.pos.z           = Float.parseFloat(tok.nextToken());
        frm.bank            = closeAngle(frm.bank, Float.parseFloat(tok.nextToken()));
        frm.pitch           = closeAngle(frm.pitch, Float.parseFloat(tok.nextToken()));
        frm.heading         = closeAngle(frm.heading, Float.parseFloat(tok.nextToken()));
        frm.scale           = Float.parseFloat(tok.nextToken());

        if (tok.hasMoreTokens())    frm.tension = Float.parseFloat(tok.nextToken());
        if (tok.hasMoreTokens())    frm.continuity = Float.parseFloat(tok.nextToken());
        if (tok.hasMoreTokens())    frm.bias = Float.parseFloat(tok.nextToken());

        frm.polar.setCartesianCoordinates(frm.pos);
        frm.updateCameraVector();

        result.add(frm);
    }

    /**
     * the spline interpolator is not very intelligent about angles:
     * if a want to interpolate between 350° and 0° it will
     * go all the way back to 0° instead of 360°, which was intended !
     *
     * that's why we adjust the angles to the next multiple of 360° (=2*pi)
     * (as a result, you can not use larger steps than 180° but that doesn't matter)
     */
    private static double closeAngle (double a1, double a2)
    {
        while ((a1-a2) > Math.PI)
            a2 += 2*Math.PI;
        while ((a2-a1) > Math.PI)
            a2 -= 2*Math.PI;
        return a2;
    }


	public static double getTotalAlpha(List frames, int from, int to)
	{
		Iterator i = frames.iterator();
		float totalAlpha = 0.0f;
		for (int j=from+1; j<to; j++)
		{
			SplineKeyFrame frm = (SplineKeyFrame)frames.get(j);
			totalAlpha += frm.getAlphaWeight();
		}
		return totalAlpha;
	}
}
