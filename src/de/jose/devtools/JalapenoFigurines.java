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

import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.profile.FontEncoding;
import de.jose.image.FontCapture;
import de.jose.image.ImgUtil;
import de.jose.image.Surface;
import de.jose.Config;

import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * @author Peter Schäfer
 */

public class JalapenoFigurines
{
	public static final String FONT_FAMILY = "Chess Berlin";

	protected static final Surface WHITE_SURFACE = new Surface(Surface.COLOR,Color.white,null);
	protected static final Surface BLACK_SURFACE = new Surface(Surface.COLOR,Color.black,null);

	public static void main(String[] args) throws Exception
	{
		File dir = new File(args[0]);
		int min_size = Integer.parseInt(args[1]);
		int max_size = Integer.parseInt(args[2]);

		Config config = new Config(new File("config"));
		FontEncoding.config(config.enumerateElements("FONT_ENCODING"));

		for (int size=min_size; size<=max_size; size++)
		{
			File file = new File(dir, "fig"+size+".png");
			System.out.print("["+file.getName());

			Font font = new Font(FONT_FAMILY, Font.PLAIN, size);
			BufferedImage img = new BufferedImage(7*size, 2*size, BufferedImage.TYPE_4BYTE_ABGR);

			//  empty dark square
			paint(img,font, 0,2*size, Constants.WHITE+Constants.EMPTY, Constants.DARK_SQUARE);

			for (int p= Constants.PAWN; p<=Constants.KING; p++)
			{
				paint(img,font, p*size,size, Constants.WHITE+p, Constants.LIGHT_SQUARE);
				paint(img,font, p*size,2*size, Constants.BLACK+p, Constants.LIGHT_SQUARE);
			}

			ImgUtil.writePng(img,file);
			System.out.println("]");
		}
	}

	public static void paint(BufferedImage img, Font font,
	                         int x, int y, int piece, int background)
	{
		String c = FontEncoding.get (FONT_FAMILY,piece,background);
		FontCapture.draw(img, x,y, font, c, WHITE_SURFACE,BLACK_SURFACE, false);
	}
}
