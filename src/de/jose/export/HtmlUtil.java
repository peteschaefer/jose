/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.export;

import de.jose.profile.FontEncoding;
import de.jose.util.FontUtil;
import de.jose.util.file.FileUtil;
import de.jose.view.style.JoFontConstants;
import de.jose.view.style.JoStyleContext;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.image.FontCapture;
import de.jose.image.Surface;
import de.jose.image.ImgUtil;
import de.jose.task.GameSource;
import de.jose.task.io.XMLExport;
import de.jose.sax.CSSXMLReader;
import de.jose.Application;

import javax.swing.text.StyleContext;
import javax.swing.text.Style;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;

import static de.jose.pgn.PgnConstants.NAG_MAX;

/**
 * @author Peter Sch�fer
 */

public class HtmlUtil
{
	protected static final Surface WHITE = new Surface(Surface.COLOR,Color.white,null);
	protected static final Surface BLACK = new Surface(Surface.COLOR,Color.black,null);

	public static String getImageFileName(String family, int piece, int size)
	{
		return family+"/"+size+"/"+EngUtil.lowerPieceCharacter(piece)+".png";
	}

	public static void createImages(ExportContext context, File targetDir) throws Exception
	{
		//  look for figurine styles
		StyleContext.NamedStyle figStyle = (StyleContext.NamedStyle)context.styles.getStyle("body.figurine");
		createFigurineImages(context.styles, figStyle, targetDir);

		Style inlineStyle = context.styles.getStyle("body.inline");
		Font inlineFont = context.styles.getFont(inlineStyle);
		createInlineImages(inlineFont,targetDir);

		//  create diagrams for JavaScript
		if (ExportConfig.getBooleanParam(context.config,"large-icons",false)) {
			//  use style "html.large"
//			int size = context.profile.getInt("xsl.dhtml.diasize",20);  //  OLD
			Style diaStyle = context.profile.getStyleContext().getStyle("html.large");
			Font diaFont = context.styles.getFont(diaStyle);
			createInlineImages(diaFont,targetDir);
		}

		//	create annotation symbols "nag-xx.png"
		Style symbolStyle = context.styles.getStyle("body.symbol");
		if (symbolStyle != null) {
			Font symbolFont = context.styles.getFont(symbolStyle);
			String fontFamily = JoFontConstants.getFontFamily(symbolStyle);
			FontEncoding fontEncoding = FontEncoding.getEncoding(fontFamily);
			File subDir = new File(targetDir,inlineFont.getFamily()+"/"+inlineFont.getSize());
			//	note we abuse the same directory as inline figurines
			createAnnotationImages(symbolFont,fontEncoding,subDir);
		}
	}

	private static void createAnnotationImages(Font symbolFont, FontEncoding fontEncoding, File subDir) throws Exception
	{
		Set<Character> syms = new HashSet<>();
		//symbolFont = FontUtil.newFont(fontFamily, Font.PLAIN, 16);
		for(int i=0; i < NAG_MAX; ++i) {
			String sym = fontEncoding.getSymbol(i);
			if (sym==null) continue;
			char c = sym.charAt(0);
			if (syms.contains(c)) continue;
			syms.add(c);
			String filename = "nag-"+Integer.toHexString(c)+".png";
			//	todo create annotation symbols "nag-xx.png"
			createAnnotationImage(symbolFont,sym,subDir,filename);
		}
	}

	private static void createAnnotationImage(Font font, String sym, File subDir, String fileName) throws Exception {
		File targetFile = new File(subDir,fileName);
		if (targetFile.exists()) return;

		subDir.mkdirs();

		int size = font.getSize();

		Rectangle bounds = new Rectangle();
		BufferedImage img1 = new BufferedImage(size,size,BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage img0 = FontCapture.capture1(font.getFamily(),font.getSize(), sym,
				WHITE,BLACK,bounds, false, false);

		img1.getGraphics().drawImage(img0,
				bounds.x, bounds.y,
				bounds.x+bounds.width, bounds.y+bounds.height,
				0,0, bounds.width,bounds.height, null);

		ImgUtil.writePng(img1,targetFile);
	}

	public static void createCSS(ExportContext context, File xslFile, File targetFile) throws Exception
	{
		GameSource dummy = GameSource.gameArray(new int[0]);
		Source source = new SAXSource(new CSSXMLReader(context), dummy);
		StreamResult result = new StreamResult(targetFile);

		TransformerFactory tfFactory = TransformerFactory.newInstance();

		Transformer tf = tfFactory.newTransformer(new StreamSource(xslFile));

		tf.transform(source,result);
	}

	protected static void createFigurineImages(JoStyleContext styleContext,
	                                           StyleContext.NamedStyle baseStyle, File targetDir)
	        throws Exception
	{
		//  collect needed fonts
		Set fonts = new HashSet();
		collectFonts(styleContext,baseStyle,fonts);
		Iterator i = fonts.iterator();
		while (i.hasNext())
			createFigurineImages((Font)i.next(),targetDir);
	}

	protected static void createFigurineImages(Font font, File targetDir) throws Exception
	{
		targetDir = new File(targetDir,font.getFamily()+"/"+font.getSize());
		targetDir.mkdirs();

		for (int p=Constants.PAWN; p<=Constants.KING; p++)
		{
			String fileName = EngUtil.lowerPieceCharacter(p)+".png";
			File targetFile = new File(targetDir,fileName);
			if (targetFile.exists()) continue;

			String c = FontEncoding.getFigurine(font.getFamily(),p);
			BufferedImage img = FontCapture.capture1(font.getFamily(), font.getSize(), c,
			                        WHITE,BLACK,null, true, false);

			ImgUtil.writePng(img,targetFile);
		}
	}

	protected static void createInlineImages(Font font, File targetDir) throws Exception
	{
		//  create images with various color and background
		FontEncoding enc = FontEncoding.getEncoding(font.getFamily());

		for (int p = Constants.PAWN; p <= Constants.KING; p++)
		{
			createInlineImage(p+Constants.WHITE,FontEncoding.LIGHT_SQUARE, enc,font, targetDir, "wl");
			createInlineImage(p+Constants.WHITE,FontEncoding.DARK_SQUARE, enc,font, targetDir, "wd");
			createInlineImage(p+Constants.BLACK,FontEncoding.LIGHT_SQUARE, enc,font, targetDir, "bl");
			createInlineImage(p+Constants.BLACK,FontEncoding.DARK_SQUARE, enc,font, targetDir, "bd");
		}

		//  empty square
		createInlineImage(Constants.EMPTY, FontEncoding.LIGHT_SQUARE, enc,font, targetDir, "el");
		createInlineImage(Constants.EMPTY, FontEncoding.DARK_SQUARE, enc,font, targetDir, "ed");

		//  borders
		createInlineImage(FontEncoding.BORDER_TOP_LEFT, enc,font,targetDir, "brdtl");
		createInlineImage(FontEncoding.BORDER_TOP, enc,font,targetDir, "brdt");
		createInlineImage(FontEncoding.BORDER_TOP_RIGHT, enc,font,targetDir, "brdtr");

		createInlineImage(FontEncoding.BORDER_LEFT, enc,font,targetDir, "brdl");
		createInlineImage(FontEncoding.BORDER_RIGHT, enc,font,targetDir, "brdr");

		createInlineImage(FontEncoding.BORDER_BOTTOM_LEFT, enc,font,targetDir, "brdbl");
		createInlineImage(FontEncoding.BORDER_BOTTOM, enc,font,targetDir, "brdb");
		createInlineImage(FontEncoding.BORDER_BOTTOM_RIGHT, enc,font,targetDir, "brdbr");

		for (int i=0; i<8; i++)
		{
			createInlineImage(FontEncoding.BORDER_LEFT_1+i, enc,font,targetDir, "brdl"+(char)('1'+i));
			createInlineImage(FontEncoding.BORDER_BOTTOM_A, enc,font,targetDir, "brdb"+(char)('a'+i));
		}
	}
	
	protected static void createInlineImage(int piece, int background, FontEncoding enc, Font font,
	                                        File targetDir, String suffix) throws Exception
	{
		targetDir = new File(targetDir,font.getFamily()+"/"+font.getSize());

		String fileName;
		if (piece==Constants.EMPTY)
			fileName = suffix+".png";
		else
			fileName = EngUtil.lowerPieceCharacter(piece)+suffix+".png";
		File targetFile = new File(targetDir,fileName);
		if (targetFile.exists()) return;

		targetDir.mkdirs();

		int size = font.getSize();
		String c = enc.get(piece,background);
		Rectangle bounds = new Rectangle();
		BufferedImage img1 = new BufferedImage(size,size,BufferedImage.TYPE_4BYTE_ABGR);
		BufferedImage img0 = FontCapture.capture1(font.getFamily(),font.getSize(), c,
		                            WHITE,BLACK,bounds, false, false);

		img1.getGraphics().drawImage(img0,
		        bounds.x, bounds.y,
		        bounds.x+bounds.width, bounds.y+bounds.height,
		        0,0, bounds.width,bounds.height, null);

		ImgUtil.writePng(img1,targetFile);
	}

	protected static void createInlineImage(int part, FontEncoding enc, Font font, File targetDir, String fileName) throws Exception
	{
		targetDir = new File(targetDir,font.getFamily()+"/"+font.getSize());

		fileName = fileName+".png";
		File targetFile = new File(targetDir,fileName);
		if (targetFile.exists()) return;

		targetDir.mkdirs();

		int size = font.getSize();
		String c = enc.getBorder(false,part);

		if (c != null) {
			Rectangle bounds = new Rectangle();
			BufferedImage img1 = new BufferedImage(size,size,BufferedImage.TYPE_4BYTE_ABGR);
			BufferedImage img0 = FontCapture.capture1(font.getFamily(),size, c,
										WHITE,BLACK,bounds, false, false);

			img1.getGraphics().drawImage(img0,
					bounds.x, bounds.y,
					bounds.x+bounds.width, bounds.y+bounds.height,
					0,0, bounds.width,bounds.height, null);


			ImgUtil.writePng(img1,targetFile);
		}
	}


	protected static void collectFonts(JoStyleContext styleContext, StyleContext.NamedStyle root, Set collect)
	{
		collect.add(styleContext.getFont(root));

		List children = JoStyleContext.getChildren(root);
		if (children!=null)
			for (int i=0; i<children.size(); i++) {
				StyleContext.NamedStyle child = (StyleContext.NamedStyle)children.get(i);
				collectFonts(styleContext,child,collect);
			}
	}

	public static File exportTemporary(ExportContext context, boolean asynch) throws Exception
	{
		switch (context.getOutput()) {
		case ExportConfig.OUTPUT_HTML:
			context.target = File.createTempFile("jose",".html");
            createCollateral(context);
			break;
		case ExportConfig.OUTPUT_XML:
			context.target = File.createTempFile("jose",".xml");
			break;
		default:
			throw new IllegalArgumentException();
		}


		exportFile(context,asynch);
		return (File)context.target;
	}

	public static void exportFile(ExportContext context, boolean asynch) throws Exception
	{
		//  setup XML exporter with appropriate style sheet
		XMLExport task = new XMLExport(context);

		if (asynch)
			task.start();   //  don't wait for task to complete
		else
			task.run();     //  wait for task to complete
	}

	public static File createCollateral(ExportContext context)
	        throws Exception
	{
		boolean needsImages =
		        (context.styles.useFigurineFont() && "img".equals(context.profile.getString("xsl.html.figs")))
		        || ExportConfig.getBooleanParam(context.config,"large-icons",false);
		boolean needsCSS = context.profile.getBoolean("xsl.css.standalone");

		//  create CSS and images, if necessary
		if (context.collateral==null)
			context.collateral = (File)context.profile.get("xsl.html.img.dir");

		File homeFile = ExportConfig.getFile(context.config);
		File homeDir = homeFile.getParentFile();

		if (needsImages || needsCSS) {
			if (context.collateral==null && context.target instanceof File)
				context.collateral = ((File)context.target).getParentFile();
			context.collateral.mkdirs();
			if (needsImages)    //  create images
				createImages(context,context.collateral);
			if (needsCSS) {       //  create CSS
				String cssCreator = ExportConfig.getParam(context.config, "css-xsl");
				String cssTarget = ExportConfig.getParam(context.config, "css-file");
				File targetFile = new File(context.collateral,cssTarget);
				if (!targetFile.exists())
					createCSS(context, new File(homeDir,cssCreator), targetFile);
			}

			//  copy JavaScript
			String[] jsFiles = ExportConfig.getParams(context.config, "js-file");
			if (jsFiles != null)
				for (int i=0; i < jsFiles.length; i++)
				{
					File targetFile = new File(context.collateral,jsFiles[i]);
					if (!targetFile.exists())
						FileUtil.copyFile(new File(homeDir, jsFiles[i]), context.collateral);
				}
		}

		//  copy navigation images
		if (ExportConfig.getBooleanParam(context.config,"nav-icons",false)) {
			//  images/nav --> collateral/nav
			File srcdir = new File(Application.theWorkingDirectory,"images/nav");
			File dstdir = new File(context.collateral, "nav");
			dstdir.mkdirs();

			String[] images = srcdir.list();
			for (int i=0; i < images.length; i++)
			{
				File src = new File(srcdir, images[i]);
				File dst = new File(dstdir, images[i]);
				if (!dst.exists()) FileUtil.copyFile(src,dst);
			}
		}

		return context.collateral;
	}

}
