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

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * models polar coordinates
 */

public class Polar3d
        extends Tuple3d
{
	public Polar3d()
	{

	}

	public Polar3d(double longi, double lati, double dist)
	{
		setPolarCoordinates(longi,lati,dist);
	}

	public Polar3d(Tuple3d vector)
	{
		setCartesianCoordinates(vector);
	}

	public double getLongitude() {
		return x;
	}

	public void setLongitude(double longitude) {
		x = longitude;
	}

	public void incrementLongitude(double longitude) {
		x += longitude;
	}

	public double getLatitude() {
		return y;
	}

	public void setLatitude(double latitude) {
		y = latitude;
	}

	public void incrementLatitude(double latitude) {
		y += latitude;
	}

	public double getDistance() {
		return z;
	}

	public void setDistance(double distance) {
		z = distance;
	}

	public void incrementDistance(double distance) {
		z += distance;
	}

	public void setPolarCoordinates(double longi, double lati, double dist)
	{
		x = longi;
		y = lati;
		z = dist;
	}

	public void setCartesianCoordinates(double cx, double cy, double cz)
	{
		z = Math.sqrt(cx*cx+cy*cy+cz*cz);
		if (z == 0.0) {
			y = x = 0.0;
		}
		else {
			y = Math.acos(cz/z);
			double a = z * Math.sin(y);
			x = Math.asin(cx/a);
		}
	}

	public void setCartesianCoordinates(Tuple3d vector)
	{
		setCartesianCoordinates(vector.x,vector.y,vector.z);
	}

	public Vector3d toCartesian()
	{
		Vector3d v = new Vector3d();
		toCartesian(v);
		return v;
	}

    public static Vector3d toCartesian(double longitude, double latitude, double radius)
    {
        Vector3d v = new Vector3d(longitude,latitude,radius);
        toCartesian(v,v);
        return v;
    }

	public Tuple3d toCartesian(Tuple3d v)
	{
        return toCartesian(this,v);
    }

    public static Tuple3d toCartesian(Tuple3d p, Tuple3d v)
    {
        return toCartesian(p.x,p.y,p.z, v);
    }

    public static Tuple3d toCartesian(double longitude, double latitude, double radius, Tuple3d v)
    {
		double sinLat = Math.sin(latitude);
		double cosLat = Math.cos(latitude);
		double sinLng = Math.sin(longitude);
		double cosLng = Math.cos(longitude);

		v.x =   radius * sinLat * sinLng;
		v.y = - radius * sinLat * cosLng;
		v.z =   radius * cosLat;
		return v;
	}
}
