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

import de.jose.Util;

import java.util.Set;

public interface PgnConstants
{
	
	/*	predefined tag keys	*/
	public static final String TAG_EVENT				= "Event";
	public static final String TAG_SITE					= "Site";
	public static final String TAG_DATE					= "Date";
	public static final String TAG_ROUND				= "Round";
	public static final String TAG_BOARD				= "Board";
	public static final String TAG_WHITE				= "White";
	public static final String TAG_BLACK				= "Black";
	public static final String TAG_RESULT				= "Result";

	public static final String TAG_WHITE_ELO			= "WhiteELO";
	public static final String TAG_BLACK_ELO			= "BlackELO";
	public static final String TAG_WHITE_TITLE			= "WhiteTitle";
	public static final String TAG_BLACK_TITLE			= "BlackTitle";
	public static final String TAG_EVENT_DATE			= "EventDate";
	public static final String TAG_ECO					= "ECO";
	public static final String TAG_OPENING				= "Opening";
	public static final String TAG_PLY_COUNT			= "PlyCount";
    public static final String TAG_ANNOTATOR			= "Annotator";

	public static final String TAG_FEN					= "FEN";
	public static final String TAG_VARIANT              = "Variant";

    public static Set DEFAULT_TAGS = Util.toHashSet(
            new Object[] { TAG_EVENT, TAG_SITE, TAG_DATE, TAG_ROUND, TAG_BOARD,
              TAG_WHITE, TAG_BLACK, TAG_RESULT, TAG_WHITE_ELO, TAG_BLACK_ELO,
              TAG_WHITE_TITLE, TAG_BLACK_TITLE, TAG_EVENT_DATE, TAG_ECO, TAG_OPENING,
              TAG_PLY_COUNT, TAG_ANNOTATOR, TAG_FEN,
            });

	/**	constant for result:	result is unknown	 */
	public static final int RESULT_UNKNOWN		= -1;
	/**	constant for result:	black wins	 */
	public static final int BLACK_WINS			=  0;
	/**	constant for result:	draw	 */
	public static final int DRAW				=  1;
	/**	constant for result:	white wins	 */
	public static final int WHITE_WINS			=  2;
	
	/**	string representation of results:	 */
	public static final String STRING_UNKNOWN		= "*";
	public static final String STRING_BLACK_WINS	= "0-1";
	public static final String STRING_DRAW			= "1/2-1/2";
	public static final String STRING_WHITE_WINS	= "1-0";
	public static final String STRING_DRAW_SHORT	= "1/2";

	/**	NAG number for !	 */
	public static final int NAG_GOOD				= 1;
	/**	NAG number for ?	 */
	public static final int NAG_BAD					= 2;
	/**	NAG number for !!	 */
	public static final int NAG_VERY_GOOD			= 3;
	/**	NAG number for ??	 */
	public static final int NAG_VERY_BAD			= 4;
	/**	NAG number for !?	 */
	public static final int NAG_INTERESTING			= 5;
	/**	NAG number for ?!	 */
	public static final int NAG_DUBIOUS				= 6;

	public static final int NAG_SLIGHT_ADVANTAGE_WHITE		= 14;
	public static final int NAG_SLIGHT_ADVANTAGE_BLACK		= 15;

	public static final int NAG_MODERATE_ADVANTAGE_WHITE	= 16;
	public static final int NAG_MODERATE_ADVANTAGE_BLACK	= 17;

	public static final int NAG_DECISIVE_ADVANTAGE_WHITE	= 18;
	public static final int NAG_DECISIVE_ADVANTAGE_BLACK	= 19;

	/**	non-standard NAGs defined by Fritz (equivalent to Informator symbols)	*/

	/**	non-standard NAG defined by jose	*/
	/**	NAG number for "print diagram"	*/
	public static final int NAG_DIAGRAM                      = 201;
    public static final int NAG_DIAGRAM_DEPRECATED           = 250;

	/**	max. nag number	*/
	public static final int NAG_MAX					= 255;


	/**	bit flag indicating an unknown second value	 */
	public static final int SECOND_UNKNOWN	= 0x0001;
	/**	bit flag indicating an unknown minute value	 */
	public static final int MINUTE_UNKNOWN	= 0x0002;
	/**	bit flag indicating an unknown hour value	 */
	public static final int HOUR_UNKNOWN	= 0x0004;
	/**	bit flag indicating an unknown day value	 */
	public static final int DAY_UNKNOWN		= 0x0010;
	/**	bit flag indicating an unknown month value	 */
	public static final int MONTH_UNKNOWN	= 0x0020;
	/**	bit flag indicating an unknown year value	 */
	public static final int YEAR_UNKNOWN	= 0x0040;
	/** bit flag indicating an unknown month and day    */
	public static final int DAY_MONTH_UNKNOWN   = DAY_UNKNOWN+MONTH_UNKNOWN;
	/** bit flag indicating an exact date   */
	public static final int DATE_EXACT      = 0;
}
