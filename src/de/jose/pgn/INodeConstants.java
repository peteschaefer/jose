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

public enum INodeConstants
{
    /** available node types
     *  as returned by Node.typeOf();
     */
    invalid,

    ANNOTATION_NODE,     //= 1
    COMMENT_NODE,        //= 2
    DIAGRAM_NODE,        //= 3
    LINE_NODE,           //= 4
    MOVE_NODE,           //= 5
    RESULT_NODE,         //= 6
    STATIC_TEXT_NODE,    //= 7
    TAG_NODE             //= 8
}
