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

package de.jose.view.colorchooser;

import java.util.Map;

public class JoChessSurfaceButton
        extends JoSurfaceButton
{

    protected Map map;
    protected String current;

    public JoChessSurfaceButton(Map surfacemap, String currentsurface)
    {
        super();
        map = surfacemap;
        current = currentsurface;
    }

    protected JoSurfaceChooser createChooser()
    {
        JoSurfaceChooser result = super.createChooser();
//        ChessPreviewPanel preview = new ChessPreviewPanel(result,map,current);
//        result.setPreviewPanel(preview);
        return result;
    }

}
