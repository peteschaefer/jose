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

package de.jose.pgn;

public interface INodeConstants
{
    /** available node types
     *  as returned by Node.typeOf();
     */

    public static final int ANNOTATION_NODE     = 1;
    public static final int COMMENT_NODE        = 2;
    public static final int DIAGRAM_NODE        = 3;
    public static final int LINE_NODE           = 4;
    public static final int MOVE_NODE           = 5;
    public static final int RESULT_NODE         = 6;
    public static final int STATIC_TEXT_NODE    = 7;
    public static final int TAG_NODE            = 8;
}
