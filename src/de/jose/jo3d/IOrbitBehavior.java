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

/**
 * common interface for OrbitBehavior and SplineOrbitBehavior
 * get the location of the viewer (the camera) in world coordinates
 *
 */
public interface IOrbitBehavior
{

    /**
     * get the location of the viewer (the camera) in world coordinates
     */
    public Tuple3d getEyePoint(Tuple3d eye);
}
