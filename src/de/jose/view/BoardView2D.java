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

package de.jose.view;

import de.jose.*;
import de.jose.chess.*;
import de.jose.comm.msg.MessageListener;
import de.jose.eboard.EBoardConnector;
import de.jose.image.*;
import de.jose.plugin.Score;
import de.jose.profile.FontEncoding;
import de.jose.profile.UserProfile;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.awt.Graphics2D;

import static de.jose.Application.AppMode.ANALYSIS;
import static de.jose.Application.AppMode.USER_INPUT;

public class BoardView2D
		extends BoardView
		implements MouseListener, MouseMotionListener
{

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

	/**	background double buffer (we DO NOT use the Java double buffering)	 */
	protected BufferedImage buffer = null;
	/**	left and top inset	 */
	protected Point2D.Double userInset = new Point2D.Double(0,0);	// todo float
	protected Point devInset=new Point(0,0);
	/**	size of one square	 */
	protected double userSquareSize;	//	todo float
	protected int devSquareSize;
	protected boolean isResizing;
	/**	todo thoughts on HiDpi displays.
	 * 	to take advantage of high-res screens, we would need to
	 * 	- determine scaling factor Graphics.getTransform().getScaleX()
	 * 	- enlarge the off-screen buffer accordingly
	 * 	- copy to the screen using Graphics.drawImage( )
	 * 		reset Grahphics.setTransform(identity)
	 */
	/**	texture offsets	 */
	protected static int[] textureOffsets;


	/**	randomize texture offsets ?
	 * 	true	no two squares will look the same
	 * 	false	textures are aligned
	 */
	protected static boolean randomTxtrOffset = true;

	static {
		textureOffsets = new int[2*OUTER_BOARD_SIZE];
		Random rnd = new Random(0xfafafb);
		for (int i=0; i<textureOffsets.length; i++)
			textureOffsets[i] = Math.abs(rnd.nextInt());
	}

	protected static final Color SHADOW_64 = new Color(0,0,0,64);

	/**	animation: number of frames per second (roughly)
	 * 	note that processing power is not the limiting factor here;
	 * 	the most determining factor is the scheduling of the Timer thread
	 * */
	protected static final int	FPS	= 100;

	/**	sprite image for sliding and mouse dragging	 */
	protected Board2DSprite sprite1 = null;
	/**	one more for castlings	*/
	protected Board2DSprite sprite2 = null;

	/**	current font for rendering pieces	*/
	protected String currentFont;
	/**	square surfaces	*/
	protected Surface currentLight,currentDark;
	/**	piece surfaces	*/
	protected Surface currentWhite,currentBlack;
	/**	background surface	*/
	protected Surface currentBackground;

	/*	todo use more fine-grained redraw flags
		let's say a bitset for each square, + background + eval bar
		when updating hints, set only flag for affected squares ...
	* */
	protected boolean forceRedraw = false;
	protected Rectangle2D drawEval = null;
	/**	lock cached images (i.e. prevent them from Garbage collection)	*/
	protected boolean lockImgCache;

	public BoardView2D(IBoardAdapter board, boolean lockImgCache)
	{
		super(board);

		setDoubleBuffered(Version.useDoubleBuffer());	//	we'll use our own buffer

		addMouseListener(this);
		addMouseMotionListener(this);

		this.lockImgCache = lockImgCache;
		recalcSize(getGraphics2D());
	}

	public void init()
	{
		UserProfile prf = AbstractApplication.theUserProfile;
		Map map = (Map)prf.get("board.images");
		if (map != null)
		{
			Iterator i = map.entrySet().iterator();
            int count = 0;
			while (i.hasNext())
				try {
					Map.Entry mety = (Map.Entry)i.next();
					String key = (String)mety.getKey();

					FontCapture.MapEntry fety = (FontCapture.MapEntry)mety.getValue();
					FontCapture.add(key, fety, lockImgCache);
					count++;
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
//System.out.println(count+" images read");
		}

		updateProfile(prf);
		showCoords(prf.getBoolean("board.coords"));
		flip(prf.getBoolean("board.flip"));
		showEvalbar(prf.getBoolean("board.evalbar"));
		showSuggestions(prf.getBoolean("board.suggestions"));
	}

	public void refresh(boolean stopAnimation)	{
		if (stopAnimation) {
			hideAllHints(true);
			if (sprite1!=null) sprite1.drop();
			if (sprite2!=null) sprite2.drop();
		}
		super.refresh(stopAnimation);
	}


	public void activate(boolean active)
	{
		/*	nothing to do - position will be synched in paint()	*/
		//eboard.connect(); // right place?
	}

	public void storeProfile(UserProfile prf)
	{
		//	store image data
		//	TO DO
		Map map = FontCapture.getAllImages(currentFont, devSquareSize, null, currentWhite, currentBlack);
		Iterator i = map.entrySet().iterator();
		FontCapture.MapEntry fety = null;

		while (i.hasNext()) {
			Map.Entry mety = (Map.Entry)i.next();
			String key = (String)mety.getKey();
			fety = (FontCapture.MapEntry)mety.getValue();
			map.put(key,fety);
		}
		if (fety != null)
			prf.set("board.images",map);
	}

	@Override
	public void doSetEval(Score score)
	{
		boolean modified = ! Score.equals(score,this.eval);
		this.eval = score;
		if (modified) {
			//	when eval bar switches state, a redraw is necessary
//			forceRedraw = true;
//			repaint();

			//	use paintImmediately() and paint()
			//	some care has to be taken to run it in the main event thread
			//	but it avoids the cost of repaint() !
			if (SwingUtilities.isEventDispatchThread())
				paintEvalImmediately();
			else SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					paintEvalImmediately();
				}
			});
		}
	}

	public void paintEvalImmediately()
	{
		drawEval = evalRect(true);
		paintImmediately(drawEval.getBounds());
		//	should call back to paint(), below
		drawEval = null;
	}

	@Override
	public void paint(Graphics g) {
		if (drawEval!=null) {
//			g.setClip(drawEval);
			Graphics2D g2 = (Graphics2D)g;
			Border b = new SoftBevelBorder(BevelBorder.RAISED);
			Util.grow(drawEval,b.getBorderInsets(this));
			g2.setClip(drawEval);

			AffineTransform save_tf = null;
			try {
				save_tf = ImgUtil.setIdentityTransform(g2,true);
				//g2.setClip(evalRect(false));
				drawEvalbar((Graphics2D)g,b,false);
			}
			finally {
				if (save_tf != null) g2.setTransform(save_tf);
			}
		}
		else
			super.paint(g);
	}

	@Override
	public void showEvalbar(boolean on) {
		boolean hadeval = (showEvalbar && eval!=null);
		super.showEvalbar(on);
		boolean haseval = (showEvalbar && eval!=null);
		if (hadeval!=haseval)
			forceRedraw = true;
		//	when eval bar switches state, a redraw is necessary
	}

	/**
	 *	paint the board
	 *
	 * this method <i>might</i> paint into a double buffer
	 * (although we have turned double buffering off)
	 *
	 * 	we have to take special care that the moving sprites are painted
	 * 	into the same buffer (otherwise, we would see a flicker)
	 *
	 *  TODO think about using this double buffer directly (instead of creating our own buffer)
	 *  OR create the buffer in graphics memory so that copying is faster (VolatileImage)
	 * */
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		if (isResizing)
			paintUgly(g2);
/*
		else if (sizeChanged()) {
			paintUgly(g);
			recalcSize();
			buffer = null;
			repaint(2000);
		}
*/
		else {
			/*	paint off screen	*/
            prepareImage(g2);
            /*	then copy	*/
			AffineTransform save_tf=null;
			try {
				save_tf = ImgUtil.setIdentityTransform(g2,true);
				//	undo scale, but keep dislocation
				g2.drawImage(buffer, 0, 0, null);
				if (sprite1.isMoving()) sprite1.paint(g2);
				if (sprite2.isMoving()) sprite2.paint(g2);
			} finally {
				if (save_tf!=null) g2.setTransform(save_tf);
			}
		}
	}

	/**
	 * called during resizing; live resizing would be too expensive (FontCapture!)
	 * that's why we simply paint a scaled image. after resizing, the correct image is painted again
	 */
	protected void paintUgly(Graphics2D g)
	{
		Rectangle bounds = getBounds();
		AffineTransform tf = g.getTransform();
		//	TODO tf might have a displacment. why? do we need to take account of it?
		/*	Note: all computations done in device-space	 */
		//Point2D screenSize = getScreenSize(false);
		Rectangle src = new Rectangle(0,0,buffer.getWidth(),buffer.getHeight());
	//	Rectangle screen = new Rectangle(0,0, (int)screenSize.getX(), (int)screenSize.getY());
		Rectangle2D screen = getScreenBounds(g,false);
		Rectangle dst = new Rectangle();

		double uglyScale = calcSquareSize(screen.getBounds().getSize()) / (double) devSquareSize;

		AffineTransform save_tf = null;
		try {
			save_tf = ImgUtil.setIdentityTransform(g,true);

			dst.width = (int) Math.round(src.width * uglyScale);
			dst.height = (int) Math.round(src.height * uglyScale);

			//  center on screen
			dst.x = (int)((screen.getWidth() - dst.width) / 2);
			dst.y = (int)((screen.getHeight() - dst.height) / 2);

			//  fill empty area with background pattern
			Area clip = new Area(new Rectangle(0,0,(int)screen.getWidth(),(int)screen.getHeight()));
			clip.subtract(new Area(dst));

			if (!clip.isEmpty()) {
				Shape oldClip = g.getClip();
				g.setClip(clip);
				if (currentBackground.useTexture()) {
					Image txtr = TextureCache.getTexture(currentBackground.texture, TextureCache.LEVEL_MAX);
					TextureCache.paintTexture(g, 0,0, (int)screen.getWidth(), (int)screen.getHeight(), txtr);
				} else {
					g.setPaint(currentBackground.getPaint(getWidth(), getHeight()));
					g.fillRect(0,0, (int)screen.getWidth(), (int)screen.getHeight());
				}
				g.setClip(oldClip);
			}

			ImgUtil.copy(
					buffer, src.x, src.y, src.width, src.height,
					g, dst.x, dst.y, dst.width, dst.height);
			/** ImgUtil.copy adjusts negative coords and avoids painting outside the graphics port
			 *  (because Java2D don't like it)
			 */
		} finally {
			if (save_tf!=null) g.setTransform(save_tf);
		}
	}

	/**	calculate square size and inset after resize	 */
	public void recalcSize(Graphics2D g)
	{
		// in user-space coordinates, used by Graphics2D
		Point2D.Double scale = getBufferScaleFactor(g);
		Point bufferSize = getBufferSize(scale);

		devSquareSize = (int)calcSquareSize(bufferSize);

		Point2D dins = calcInsetPoint(bufferSize,devSquareSize);
		devInset.x = (int)dins.getX();
		devInset.y = (int)dins.getY();

		userSquareSize = devSquareSize / Math.max(scale.getX(),scale.getY());
		userInset.x = devInset.x / scale.getX();
		userInset.y = devInset.y / scale.getY();
	}

	protected Point2D calcInsetPoint(Point2D viewportSize,double squareSize)
	{
		float divx = 8.0f, divy = 8.0f;
		if (showCoords) {
			divx += 0.4f;
			divy += 0.4f;
		}
		if (showEvalbar)
			divx += 0.4f;

		Point2D.Double inset = new Point2D.Double(0,0);

		inset.x = (viewportSize.getX()-divx*squareSize) / 2;
		if (showCoords)
			inset.x += 0.4*squareSize;

		inset.y = (viewportSize.getY()-divy*squareSize) / 2;
		return inset;
	}

	protected Point2D.Double getBufferScaleFactor(Graphics2D g)
	{
		AffineTransform tf;
		if (g!=null)
			tf = g.getTransform();
		else
			tf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
		return new Point2D.Double(tf.getScaleX(), tf.getScaleY());
	}

	protected Point getBufferSize(Point2D scaleFactor)
	{
		Point size = new Point(getWidth(),getHeight());
		size.x = (int)((size.x*scaleFactor.getX())+0.5);
		size.y = (int)((size.y*scaleFactor.getY())+0.5);
		return size;
	}

	protected Rectangle2D getScreenBounds(Graphics2D g, boolean userSpace)
	{
		Rectangle r = getBounds();
		Point2D p1 = new Point2D.Double(r.getX(),r.getY());
		Point2D p2 = new Point2D.Double(r.getX()+r.getWidth(),r.getY()+r.getHeight());

		if (!userSpace) {
			//	else: transform to device space
			AffineTransform tf = g.getTransform();
			tf.transform(p1,p1);
			tf.transform(p2,p2);
		}

		return new Rectangle2D.Double(
				p1.getX(), p1.getY(),
				p2.getX()-p1.getX(), p2.getY()-p1.getY());
	}

	protected double calcSquareSize(Point2D viewportSize)
	{
		float divx = 8.0f, divy = 8.0f;
		if (showCoords) {
			divx += 0.4f;
			divy += 0.4f;
		}
		if (showEvalbar)
			divx += 0.4f;

		return (float)Math.min(
				(viewportSize.getX()-4)	/ divx,
				(viewportSize.getY()-4) / divy);
	}

	protected final double calcSquareSize(Dimension viewportSize)
	{
		Point2D sizep2d = new Point(viewportSize.width,viewportSize.height);
		return calcSquareSize(sizep2d);
	}

	public Graphics2D getBufferGraphics()
	{
		return (Graphics2D)buffer.getGraphics();
	}

	/**	repaint one square	 */
	/*public void updateOne(Graphics2D g, int file, int row)
	{
		prepareImage(g);

		paintImmediate(g,EngUtil.square(file,row));
	}*/

	protected void paintImmediate(Graphics2D g, int square)
	{
		Point2D p = origin(square,false);
		AffineTransform save_tf = null;

		int x0 = (int)p.getX();
		int y0 = (int)p.getY();
		int x1 = x0+devSquareSize;
		int y1 = y0+devSquareSize;

		try {
			save_tf = ImgUtil.setIdentityTransform(g,true);
			g.drawImage(buffer,
					x0,y0, x1,y1,
					x0,y0, x1,y1, null);
		} finally {
			if (save_tf!=null) g.setTransform(save_tf);
		}
	}

	public Point getScreenLocation (int square)
	{
		Point2D pt2d = origin(square,true);
		Point pt = new Point(
				(int)(pt2d.getX()+0.5),
				(int)(pt2d.getY()+0.5) );
		SwingUtilities.convertPointToScreen(pt,this);
		return pt;
	}

	/**
	 *	@param p a point on the screen
	 *	@return the square index (or 0, if off the board)
	 * */
	public final int findSquare(Point2D p, boolean userSpace)
	{
		return findSquare(p.getX(),p.getY(), userSpace);
	}

	/**
	 *	@param x
     *  @param y a point on the screen (in user-space coordinates)
	 *	@return the square index (or 0, if off the board)
	 * */

	public int findSquare(double x, double y, boolean userSpace)
	{
		double squareSize = userSpace ? userSquareSize : devSquareSize;
		Point2D inset = userSpace ? userInset : devInset;

		if (squareSize==0 || x<inset.getX() || y<inset.getY())
			return 0;

		int file = (int)((x-inset.getX())/squareSize);
		int row = (int)((y-inset.getY())/squareSize);

		if (flipped) {
			file = FILE_H-file;
			row = ROW_1+row;
		}
		else {
			file = FILE_A+file;
			row = ROW_8-row;
		}

		if (EngUtil.innerSquare(file,row))
			return EngUtil.square(file,row);
		else
			return 0;
	}

	public int[] findPreferredSquares(Point2D mousePoint, Rectangle spriteBounds)
	{
		int[] result = new int[6];
		/**	1. choice: mouse location	*/
		result[0] = findSquare(mousePoint,true);
		/**	2. choice: intersection with sprite (sorted by area)	*/
		if (spriteBounds!=null) {
			result[1] = findSquare(ViewUtil.center(spriteBounds),false);
			/**	next choice: any intersection with sprite	*/
			result[2] = findSquare(ViewUtil.topLeft(spriteBounds),false);
			result[3] = findSquare(ViewUtil.topRight(spriteBounds),false);
			result[4] = findSquare(ViewUtil.bottomLeft(spriteBounds),false);
			result[5] = findSquare(ViewUtil.bottomRight(spriteBounds),false);
			/* remove duplicates */
			for (int i=0; i < 5; i++)
				for (int j=i+1; j < 6; j++)
					if (result[j]==result[i]) result[j]=0;
		}
		return result;
	}


	/**
	 *	@return the screen location of the upper left corner of a given square
	 * */
	protected Point2D origin(int file, int row, boolean userSpace)
	{
		double squareSize = userSpace ? userSquareSize : devSquareSize;
		Point2D inset = userSpace ? userInset : devInset;
		if (flipped)
			return new Point2D.Double(
							(FILE_H-file) * squareSize + inset.getX(),
						 	(row-ROW_1) * squareSize + inset.getY());
		else
			return new Point2D.Double(
							(file-FILE_A) * squareSize + inset.getX(),
						 	(ROW_8-row) * squareSize + inset.getY());
	}

	/**
	 *	@return the screen location of the upper left corner of a given square
	 * */
	protected final Point2D origin(int square, boolean userSpace)
	{
		return origin(EngUtil.fileOf(square),
					EngUtil.rowOf(square),
					userSpace);
	}

    protected final Point2D lowerRight(int square, boolean userSpace)
    {
        Point2D p = origin(square,userSpace);
		double squareSize = userSpace ? userSquareSize : devSquareSize;
		return new Point2D.Double(
				p.getX()+squareSize, p.getY()+squareSize);
    }

    protected final Point2D center(int square, boolean userSpace)
    {
        Point2D p = origin(square,userSpace);
		double squareSize = userSpace ? userSquareSize : devSquareSize;

		return new Point2D.Double(
				p.getX()+squareSize/2, p.getY()+squareSize/2);
    }


	/**
	 *	move (slide) a piece from one square to another
	 *
	 * @param duration duration of move in millisecs
	 * @param frameRate if > 0: desired frames per second;
	 *					if <= 0: draw synchronously
	 * */
	public void move(int startSquare,
					 int piece, int endSquare,
					 float duration, int frameRate)
	{
		sprite1.animate(startSquare,endSquare,
						piece, EMPTY,
						calcMillis(duration),frameRate);
	}


	public void move(Move mv, float time)
	{
		long millis = calcMillis(time);
		int piece = mv.moving.piece();

		if (showAnimationHints) showHint(mv, millis, ANIM_HINT_COLOR);

		//	just in case that an old animation is still running:
		sprite1.drop();

		/*	set up sprite */
		finishMove(mv, millis);
	}

	protected void doShowHint(Hint hnt,boolean repaint)
	{
		if (repaint) doRepaintHints();
	}

	protected void doHideHint(Hint hnt, boolean repaint)
	{
		if (repaint) doRepaintHints();
	}
/*
	protected void doHideAllHints(int count, boolean repaint)
	{
		if (repaint) doRepaintHints();
	}
*/

	protected void doRepaintHints() {
		forceRedraw = true;
		repaint();
	}

	public synchronized void synch(boolean redraw)
	{
		/*	synch with actual position	*/
		Graphics2D g = getBufferGraphics();

		for (int file = FILE_A; file <= FILE_H; file++)
			for (int row = ROW_1; row <= ROW_8; row++)
			{
				int square = EngUtil.square(file,row);
				if (square==sprite1.src || square==sprite2.src ||
					square==sprite1.dst || square==sprite2.dst)
					continue;
				/**
				 * squares that take part in animation are not synched
				 * they will be synched by Board2DSprite AFTER the animation has finished
				 * TODO could this conflict with hint arrows ?
				 */

				int newPiece = board.pieceAt(square);
				if (redraw || newPiece != pieceAt(square))
					set(g,newPiece,square);
			}

		if (redraw) {
			sprite1.updateBuffer();
			sprite2.updateBuffer();
		}

		EBoardConnector eboard = Application.theApplication.eboard;
		if (eboard!=null) eboard.synchFromApp();
	}


	public void startContinuousResize()
	{
		/** while continous resizing:
		 *  don't compute piece images, it's just too expensive
		 *  use the old images scaled down
		 */
		isResizing = true;
	}

	public void finishContinuousResize()
	{
		/**
		 * return to normal painting
		 */
		isResizing = false;
		repaint();
	}

	private void paintHints()
	{
		synchronized (hints) {
			if (hints.isEmpty()) return;

			int painted = 0;
			Graphics2D g = getBufferGraphics();
			int fontSize = (int) (devSquareSize * 0.17f);
			Font f = new Font("SansSerif", Font.PLAIN, fontSize);
			g.setFont(f);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			for (int i = 0; i < hints.size(); i++) {
				Hint hnt = (Hint) hints.get(i);
				if (hnt != null) {
					Point2D pfrom = center(hnt.from,false);
					Point2D pto = center(hnt.to,false);
					int hmid = hnt.intermediateSquare();

					if (hmid!=0) {
						//	for knight moves
						Point2D pmid = center(hmid,false);
						paintLine(g, pfrom, pmid, devSquareSize / 16, hnt.color);
						paintArrow(g, pmid, pto, devSquareSize / 16, hnt.color);
						paintArrowLabel(g, pmid, pto, hnt.label);
						painted++;
					}
					else {
						paintArrow(g, pfrom, pto, devSquareSize / 16, hnt.color);
						paintArrowLabel(g, pfrom, pto, hnt.label);
						painted++;
					}
				}
			}
		}

		sprite1.updateBuffer();
		sprite2.updateBuffer();
	}

	public static final float[] createArrowFloatCoordinates(float length, float width, float tip)
	{
		float[] xyz = {
			0,                  -width,         0,
			length-tip,         -width,         0,
			length-tip,         -tip,           0,
			length,             0,              0,
			length-tip,         tip,            0,
			length-tip,         +width,         0,
			0,                  +width,         0,
			//	text anchor point:
			length-tip,			0,				0,
		};
		return xyz;
	}

	private void paintLine(Graphics2D g, Point2D p1, Point2D p2,
							int width, Color color)
	{
		/** set up a a polygon of normal width */
		int length = (int)Math.round(p1.distance(p2));

		/** rotate into place   */

//		tf.scale(box.width/100.0, box.height/100.0);
		g.setColor(color);
		//g.fillPolygon(x,y, x.length-1);
		g.setStroke(new BasicStroke(2*width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		g.drawLine ((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
	}

	private void paintArrow(Graphics2D g, Point2D p1, Point2D p2,
	                        int width, Color color)
	{
		/** set up a a polygon of normal width */
		int length = (int)Math.round(p1.distance(p2));
		int tip = (int)(2*width);

		//int[] x = createArrowXCoordinates(length,width,tip);
		//int[] y = craeteArrowYCoordinates(length,width,tip);

		/** rotate into place   */
		AffineTransform oldTransform = null;
		try {
			oldTransform = g.getTransform();
			AffineTransform rot1 = (AffineTransform) oldTransform.clone();
			rot1.translate(p1.getX(), p1.getY());

//		tf.scale(box.width/100.0, box.height/100.0);
			double angle = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
			rot1.rotate(angle);    //  TODO

			g.setTransform(rot1);
			g.setColor(color);
			//g.fillPolygon(x,y, x.length-1);
			g.setStroke(new BasicStroke(2 * width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.drawLine(0, 0, length, 0);
			g.drawLine(length, 0, length - tip, +tip);
			g.drawLine(length, 0, length - tip, -tip);

			//g.drawPolygon(x,y, x.length-1);
		} finally {
			g.setTransform(oldTransform);
		}
	}

	private void paintArrowLabel(Graphics2D g, Point2D p1, Point2D p2, String label)
	{
		/** set up a a polygon of normal width */
		int length = (int)Math.round(p1.distance(p2));

		/** rotate into place   */
		AffineTransform oldTransform = null;
		try {
			oldTransform = g.getTransform();
			AffineTransform rot1 = (AffineTransform) oldTransform.clone();
			rot1.translate(p1.getX(), p1.getY());

//		tf.scale(box.width/100.0, box.height/100.0);
			double angle = Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
			rot1.rotate(angle);

			g.setTransform(rot1);
			g.setColor(Color.black);
			//g.drawPolygon(x,y, x.length-1);

			if (label!=null) {
				FontMetrics mtx = g.getFontMetrics();
				Rectangle2D box = mtx.getStringBounds(label, g);
				Point textAnchor = new Point(length, 0);
				int texty = (int) (textAnchor.y + mtx.getAscent() / 2 - devSquareSize * 0.02);
				if (angle >= -Math.PI / 2 && angle <= Math.PI / 2) {
					g.drawString(label, (int) (textAnchor.x - box.getWidth()), texty);
				} else {
					//	rotate text box, too
					AffineTransform rot2 = (AffineTransform) oldTransform.clone();
					rot2.translate(p1.getX(), p1.getY());
					rot2.rotate(angle - Math.PI);
					g.setTransform(rot2);
					g.drawString(label, -textAnchor.x, texty);
				}
			}
		} finally {
			g.setTransform(oldTransform);
		}
	}

	protected void finishMove(Move mv, long millis)
	{
		if (!sprite1.isPickedUp())
			sprite1.pickUp(mv.from, mv.moving.piece());
		//	maybe picked up by mouseStart(), maybe here

		int piece = sprite1.pc;
		if (mv.isPromotion())
			piece = mv.getPromotionPiece() + EngUtil.colorOf(piece);

		switch (mv.castlingMask()) {    //  FRC
		case WHITE_KINGS_CASTLING:
			sprite2.pickUp(mv.to,WHITE_ROOK); break;
		case WHITE_QUEENS_CASTLING:
			sprite2.pickUp(mv.to, WHITE_ROOK); break;
		case BLACK_KINGS_CASTLING:
			sprite2.pickUp(mv.to, BLACK_ROOK); break;
		case BLACK_QUEENS_CASTLING:
			sprite2.pickUp(mv.to, BLACK_ROOK); break;
		}

		sprite1.dropTo(mv, piece, millis,FPS, !mv.isCastling());

		switch (mv.castlingMask()) {    //  FRC
		case WHITE_KINGS_CASTLING:
			sprite2.dropTo(F1,WHITE_ROOK, calcMillis(0.4f),FPS,false); break;
		case WHITE_QUEENS_CASTLING:
			sprite2.dropTo(D1, WHITE_ROOK, calcMillis(0.4f),FPS,false); break;
		case BLACK_KINGS_CASTLING:
			sprite2.dropTo(F8, BLACK_ROOK, calcMillis(0.4f),FPS,false); break;
		case BLACK_QUEENS_CASTLING:
			sprite2.dropTo(D8, BLACK_ROOK, calcMillis(0.4f),FPS,false); break;
		}

		if (mv.isEnPassant())
			set(EMPTY, mv.getEnPassantSquare());
	}

	public void doFlip(boolean on)
	{
		recalcSize(getGraphics2D());
		forceRedraw = true;
	}

	public void doShowCoords(boolean on)
	{
		recalcSize(getGraphics2D());
		forceRedraw = true;
	}

	public void doShowEvalbar(boolean on)
	{
		recalcSize(getGraphics2D());
		forceRedraw = true;
	}

	public void updateProfile(UserProfile prf)
	{
		String user_font = prf.getString("font.diagram");

		if (currentFont==null || !currentFont.equals(user_font))
		{
			/*	font has changed	*/
			currentFont = user_font;
			forceRedraw = true;
		}

		if (!prf.get("board.surface.light").equals(currentLight))
		{
			currentLight = (Surface)prf.get("board.surface.light");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.dark").equals(currentDark))
		{
			currentDark = (Surface)prf.get("board.surface.dark");
			forceRedraw = true;
		}


		if (!prf.get("board.surface.white").equals(currentWhite))
		{
			currentWhite = (Surface)prf.get("board.surface.white");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.black").equals(currentBlack))
		{
			currentBlack = (Surface)prf.get("board.surface.black");
			forceRedraw = true;
		}

		if (!prf.get("board.surface.background").equals(currentBackground))
		{
			currentBackground = (Surface)prf.get("board.surface.background");
			forceRedraw = true;
		}

		showAnimationHints = prf.getBoolean("board.animation.hints");

		flip(prf.getBoolean("board.flip"));
		showCoords(prf.getBoolean("board.coords"));
		showEvalbar(prf.getBoolean("board.evalbar"));
		showSuggestions(prf.getBoolean("board.suggestions"));
	}

	private boolean sizeChanged(Graphics2D screeng)
	{
		Point2D scale = getBufferScaleFactor(screeng);
		Point bufferSize = getBufferSize(scale);

		return (buffer==null)
				|| (buffer.getWidth()!=bufferSize.getX()
				|| (buffer.getHeight()!=bufferSize.getY()));
	}

	private BufferedImage createBuffer(Graphics2D screeng, int width, int height)
	{
		return screeng.getDeviceConfiguration().createCompatibleImage(width,height);
		/**
		 * TODO think about using VolatileImage (reside in graphic card memory)
		 * but have to handle the case when contents is lost
		 */
//		return screeng.getDeviceConfiguration().createCompatibleVolatileImage(width,height);
	}

	/**
	 *	prepare the off-screen image
	 * */
	protected void prepareImage(Graphics2D screeng)
	{
		boolean redraw = forceRedraw;
		forceRedraw = false;

		if (sizeChanged(screeng))
		{
			/*	size has changed	*/
			recalcSize(screeng);
			Point2D.Double scaleFactor = getBufferScaleFactor(screeng);
			Point bufferSize = getBufferSize(scaleFactor);

			buffer = createBuffer(screeng, bufferSize.x, bufferSize.y);

			if (sprite1==null)
				sprite1 = new Board2DSprite(buffer);
			else
				sprite1.resetBackground(buffer);

			if (sprite2==null)
				sprite2 = new Board2DSprite(buffer);
			else
				sprite2.resetBackground(buffer);

			redraw = true;
		}

		Graphics2D g = (Graphics2D)buffer.getGraphics();
		ImgUtil.setRenderingHints(g);

		int boardSize = 8*devSquareSize;
		int x2 = devInset.x+boardSize;
		int y2 = devInset.y+boardSize;
		Border b = new SoftBevelBorder(BevelBorder.RAISED);

		if (redraw) {
			if (lockImgCache) FontCapture.unlock();    //	make outdated images available for gc

			int devWidth = buffer.getWidth();
			int devHeight = buffer.getHeight();
			if (currentBackground.useTexture()) {
				//	paint background around board
				Image txtr = TextureCache.getTexture(currentBackground.texture, TextureCache.LEVEL_MAX);
				TextureCache.paintTexture(g, 0, 0, devWidth, devInset.y, txtr);
				TextureCache.paintTexture(g, 0, devInset.y, devInset.x, boardSize, txtr);
				TextureCache.paintTexture(g, x2, devInset.y, devWidth - x2, boardSize, txtr);
				TextureCache.paintTexture(g, 0, y2, devWidth, devHeight - y2, txtr);
			} else {
				//	paint background around board
				g.setPaint(currentBackground.getPaint(devWidth, devHeight));
				g.fillRect(0, 0, devWidth, devInset.y);
				g.fillRect(0, devInset.y, devInset.x, boardSize);
				g.fillRect(x2, devInset.y, devWidth - x2, boardSize);
				g.fillRect(0, y2, devWidth, devHeight - y2);
			}


			g.setColor(Color.black);
			//		g.drawRect(inset.x-2, inset.y-2, boardSize+4,boardSize+4);
			b.paintBorder(this, g, devInset.x - 2, devInset.y - 2,
					boardSize + 4, boardSize + 4);

			if (showCoords)/** draw coordinates    */
				drawCoordinates(g);
		}

		if (showEvalbar)
			drawEvalbar(g,b,false);	//	always draw it

		synch(redraw);

		if (redraw)
			paintHints();

		paintHook(redraw);
	}

	private void drawCoordinates(Graphics2D g) {
		char[] c = new char[2];
		int fontSize = (int) (devSquareSize * 0.3f);
		Font f = new Font("SansSerif", Font.PLAIN, fontSize);
		g.setFont(f);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FontMetrics fmx = g.getFontMetrics();

		int drop = Math.max(devSquareSize / 48, 1);

		for (int i = 0; i < 8; i++) {
			c[0] = (char) (flipped ? ('1' + i) : ('8' - i));
			int x0 = devInset.x - fmx.charWidth(c[0]) * 9 / 8 - 4;
			int y0 = devInset.y + devSquareSize * i + (devSquareSize + fmx.getAscent()) / 2;

			c[1] = (char) (flipped ? ('h' - i) : ('a' + i));
			int x1 = devInset.x + devSquareSize * i + (devSquareSize - fmx.charWidth(c[1])) / 2;
			int y1 = devInset.y + 8 * devSquareSize + fmx.getAscent();

			g.setColor(SHADOW_64);
			g.drawChars(c, 0, 1, x0 + drop, y0 + drop);
			g.drawChars(c, 1, 1, x1 + drop, y1 + drop);

			if (currentBackground.isDark())
				g.setColor(Color.white);
			else
				g.setColor(Color.black);
			g.drawChars(c, 0, 1, x0, y0);
			g.drawChars(c, 1, 1, x1, y1);
		}
	}

	public Rectangle2D evalRect(boolean userSpace)
	{
		double squareSize = userSpace ? userSquareSize : devSquareSize;
		Point2D inset = userSpace ? userInset : devInset;

		double boardSize = 8*squareSize;
		double x2 = inset.getX()+boardSize;
		double gap = squareSize*0.1f;
		double wid = squareSize*0.25f;

//		if (b!=null)
//			b.paintBorder(this, g, x2+gap - 2, devInset.y - 2, wid + 4, boardSize + 4);

		Rectangle2D rect = new Rectangle2D.Double();
		rect.setRect(x2+gap, inset.getY(),	wid, boardSize);
		return rect;
	}

	public void drawEvalbar(Graphics2D g, Border b, boolean userSpace)
	{
		if (!showEvalbar) return;	//	that was easy
		if (this.eval==null) return;	//	no useful score

		//double squareSize = userSpace ? userSquareSize : devSquareSize;
		//Point2D inset = userSpace ? userInset : devInset;

		int boardSize, x2,y0,gap,wid;
		if (userSpace)
		{
			boardSize = (int)(8*userSquareSize);
			x2 = (int)(userInset.x+boardSize);
			y0 = (int)userInset.y;
			gap = (int)(userSquareSize*0.1f);
			wid = (int)(userSquareSize*0.25f);
		}
		else
		{
			boardSize = 8*devSquareSize;
			x2 = devInset.x+boardSize;
			y0 = devInset.y;
			gap = (int)(devSquareSize*0.1f);
			wid = (int)(devSquareSize*0.25f);
		}

		if (eval.hasWDL())
			doDrawEvalbar(g, b, x2, gap, y0, wid, boardSize);
		else if (currentBackground.useTexture()) {
			//	paint background around board
			Image txtr = TextureCache.getTexture(currentBackground.texture, TextureCache.LEVEL_MAX);
			TextureCache.paintTexture(g, x2, y0, wid, boardSize, txtr);
		} else {
			//	paint background around board
			g.setPaint(currentBackground.getPaint(wid, boardSize));
			g.fillRect(x2, y0, wid, boardSize);
		}

	}

	private void doDrawEvalbar(Graphics2D g, Border b,
							   int x2, int gap,
							   int y0, int wid,
							   int boardSize)
	{
		int y2 = y0 +boardSize;
		int hwhite = (int)(boardSize * eval.rel(eval.win)+0.5);
		int hgrey =  (int)(boardSize * eval.rel(eval.draw)+0.5);
		int hblack = boardSize - hwhite-hgrey;

		if (b !=null)
			b.paintBorder(this, g, x2 + gap - 2, y0 - 2, wid + 4, boardSize + 4);

		if (hwhite > 0) {
			g.setColor(Color.white);
			if (flipped)
				g.fillRect(x2 + gap, y0, wid, hwhite);
			else
				g.fillRect(x2 + gap, y2 - hwhite, wid, hwhite);
		}
		if (hgrey > 0) {
			g.setColor(Color.gray);
			if (flipped)
				g.fillRect(x2 + gap, y2 - hblack - hgrey, wid, hgrey);
			else
				g.fillRect(x2 + gap, y2 - hwhite - hgrey, wid, hgrey);
		}
		if (hblack > 0) {
			g.setColor(Color.black);
			if (flipped)
				g.fillRect(x2 + gap, y2 - hblack, wid, hblack);
			else
				g.fillRect(x2 + gap, y0, wid, hblack);
		}
	}


	protected void paintHook(boolean redraw)
	{
		//	for derived classes
	}



	public final BufferedImage getPieceImage(int piece, Rectangle bounds, boolean userSpace)
	{
		return getPieceImage(currentFont,
							userSpace ? (int)userSquareSize : devSquareSize,
							piece,
							currentWhite, currentBlack, 
							bounds,lockImgCache);
	}

	public  static BufferedImage getPieceImage(String font, int size, int piece,
									   Surface white, Surface black,
									   Rectangle bounds, boolean lock)
	{
		String c = FontEncoding.get(font,piece);
		if (c==null) c = "?";

		try {
			return FontCapture.getImage(font, size, c, white, black, bounds, lock);
		} catch (FileNotFoundException e) {
			Application.error(e);
			return null;
		}
	}

	public ImageIcon getPopupIcon(int piece)
	{
		return new ImageIcon(getPieceImage(piece,null,true));
	}

	/**
	 * set a piece at a given square and draw it immediately
	 */
    public void set(Graphics2D g, int piece, int square)
    {
		paint(g,piece,square);
		pieces[square] = piece;
	}

	protected void paint(Graphics2D g, int piece, int square)
	{
		drawBackground(g, square);

		if (piece != EMPTY) {
			Point2D p = origin(square,false);
			Rectangle imgBounds = new Rectangle();
			Image img = getPieceImage(piece,imgBounds,false);
			g.drawImage(img, (int)(p.getX()+imgBounds.x), (int)(p.getY()+imgBounds.y), null);
		}
	}

	protected Surface getBackground(int square)
	{
		boolean light = EngUtil.isLightSquare(square);
		return light ? currentLight:currentDark;
	}

	protected void drawBackground(Graphics2D g, int square)
	{
		Point2D p = origin(square,false);
		int px = (int)p.getX();
		int py = (int)p.getY();

		Surface surf = getBackground(square);
		if (surf.useTexture())
			try {
				if (randomTxtrOffset)
					TextureCache.paintTexture(g, px,py, devSquareSize,devSquareSize,
								 surf.texture, TextureCache.LEVEL_MAX,
								 textureOffsets[2*square],
								 textureOffsets[2*square+1]);
				else
					TextureCache.paintTexture(g, px,py, devSquareSize,devSquareSize,
								surf.texture, TextureCache.LEVEL_MAX,
								px, py);
			} catch (IOException fnex) {
				//	what can we do ?
			}
		else {
			/*	color	*/
			g.setPaint(surf.getPaint(px,py, devSquareSize,devSquareSize));
			g.fillRect(px,py, devSquareSize,devSquareSize);
		}
	}


	public void captureImage(MessageListener callbackTarget, boolean transparent)
	{
		//  create a copy
		BufferedImage img;
		forceRedraw = true;

		Surface oldBackground = currentBackground;
		BufferedImage oldBuffer = buffer;
		boolean wasRedraw = forceRedraw;

		try {
			if (transparent) {
				buffer = img = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				currentBackground = Surface.newColor(new Color(0,0,0,0));
			}
			else
				buffer = img = getGraphicsConfiguration().createCompatibleImage(buffer.getWidth(), buffer.getHeight());

			Graphics2D g = (Graphics2D)img.getGraphics();
			prepareImage(g);
			//	 todo why? why not copy buffer directly ??

			if (sprite1.isMoving()) sprite1.paint(g);
			if (sprite2.isMoving()) sprite2.paint(g);

		} finally {
			currentBackground = oldBackground;
			buffer = oldBuffer;
			forceRedraw = wasRedraw;
		}

		callbackTarget.handleMessage(this,MESSAGE_CAPTURE_IMAGE,img);
	}

	//-------------------------------------------------------------------------------
	//	Interface MouseListener
	//-------------------------------------------------------------------------------


	public void mouseClicked(MouseEvent e)
	{
		if (! ContextMenu.isTrigger(e) && ! promoPopupShowing)
			mouseClickSquare = mouseClick(e.getPoint());
	}

	public void mousePressed(MouseEvent e)
	{
		if (! ContextMenu.isTrigger(e) && ! promoPopupShowing)
			mouseStart(e.getPoint());
	}

	public void mouseEntered(MouseEvent e)
	{
		mouseMovement(e.getPoint());
	}

	public void mouseExited(MouseEvent e)
	{
		setCursor(Cursor.getDefaultCursor());
	}

	public void mouseReleased(MouseEvent e)
	{
		if (! ContextMenu.isTrigger(e) && ! promoPopupShowing)
			mouseEnd(e.getPoint());
	}




	/**
	 * called when the mouse is pressed
	 */
	public void mouseStart(Point startPoint)
	{
		int square = findSquare(mouseStartPoint = startPoint,true);

		if (board.canMove(square)) {
			mouseStartSquare = square;
			int piece = board.pieceAt(mouseStartSquare);
			sprite1.pickUp(mouseStartSquare,piece);
		}
		else	/*	piece can't move: ignore	*/
			mouseStartSquare = 0;
	}

	public void mouseEnd(Point p)
	{
		mouseEnd(p,0);
	}

	public int mouseClick(Point point)
	{
		int square = findSquare(point,true);
		if (square==0 || mouseClickSquare==square)
			return 0;
		if (mouseClickSquare==0) {
			//	one-click moves may be confusing
			//if (guessMove1(square,board.getPosition()))
			//	return 0;
		}
		else {
			if (guessMove2(new int[]{mouseClickSquare,square}))
				return 0;
			if ((Application.theApplication.theMode==USER_INPUT || Application.theApplication.theMode==ANALYSIS))
			{
				Move[] mvs = guessDoubleTake(mouseClickSquare,square,board.getPosition());
				if (mvs!=null
						&& tryMouseMove(mvs[0])
						&& tryMouseMove(mvs[1]))
					return 0;
			}
		}
		//	otherwise: memorize square for second click
		return square;
	}

	protected boolean guessMove1(int square, Position pos)
	{
		MoveIterator moves = new MoveIterator(pos);
		Move candidate = null;
		while(moves.next())
		{
			Move mv = moves.getMove();
			if ((MoveGesture.origSquare(mv)==square || MoveGesture.destSquare(mv)==square) && pos.checkMove(mv))
				if (!mv.isPromotion() || mv.getPromotionPiece()==Constants.QUEEN) {
					if (candidate!=null)
						return false;	//	ambiguous
					candidate = new Move(mv);
					candidate.setPromotionPiece(0);	//	yet
				}
		}
		return (candidate!=null) && tryMouseMove(candidate);
	}

	protected boolean tryMouseMove(int s1, int s2, int promoPiece) {
		Move mv = new Move(s1,s2);
		mv.setPromotionPiece(promoPiece);
		return tryMouseMove(mv);
	}

	protected boolean tryMouseMove(Move mv)
	{
		mouseMove = mv;
		if (board.isLegal(mouseMove)) {
			/*	legal move	*/
			finishMove(mouseMove, calcMillis(0.1f));
			board.userMove(mouseMove);
			return true;
		}

		if (couldBePromotion(mouseMove)) {
			/*	show popup	*/
			mouseStartSquare = mouseMove.from;
			showPromotionPopup(board.movesNext(), origin(mouseMove.to,true));
			/*	when the user selects an item, mouseEnd will be called again	*/
			return true;	//	important: keep mouseMove
		}
		return false;
	}

	protected boolean guessMove2(int[] s)
	{
		if (EngUtil.colorOf(board.pieceAt(s[1])) == board.movesNext())
			Util.swap(s,0,1);

		int p0 = board.pieceAt(s[0]);
		int p1 = board.pieceAt(s[1]);
		return (EngUtil.colorOf(p0) == board.movesNext())
			&& (EngUtil.colorOf(p1) != EngUtil.colorOf(p0))
			&& tryMouseMove(s[0],s[1],0);
	}

	protected Move[] guessDoubleTake(int s1, int s2, Position pos)
	{
		//  guess take-take combination
		int s3;
		MoveIterator moves = new MoveIterator(pos);
		while(moves.next())
		{
			Move mv = moves.getMove();
			if (mv.from==s1)
				s3 = s2;
			else if (mv.from==s2)
				s3 = s1;
			else
				continue;

			if (!pos.checkMove(mv)) continue;
			//  if the intermediate move is a promotion, prefer a queen
			//  (it will be captured, anyway)
			if (mv.isPromotion() && mv.getPromotionPiece()!=Constants.QUEEN) continue;

			if (pos.tryMove(mv))
				try {
					MoveIterator moves2 = new MoveIterator(pos);
					while(moves2.next()) {
						Move mv2 = moves2.getMove();
						if (mv2.from==s3 && mv.to==mv2.to && pos.checkMove(mv2))
							return new Move[] { mv,mv2 };
					}
				} finally {
					pos.undoMove();
				}
		}
		return null;
	}


	public void promotionPopup(int promoPiece)
	{
		if (promoPiece <= 0)
			mouseEnd(origin(mouseMove.from,true), 0);				//	cancel move
		else
			mouseEnd(origin(mouseMove.to,true), promoPiece);			//	make move
	}

	/**
	 * called when the mouse is released
	 */
	private void mouseEnd(Point2D endPoint, int promoPiece)
	{
		if (mouseStartSquare==0) return;	//	irrelevant mouse click

		int[] destSquare = findPreferredSquares(endPoint,
							sprite1.isMoving() ? sprite1.getCurrentBounds():null);
		//	careful: sprite.getCurrentBounds() needs to be interpreted in device-space coordinates

		int i=0;
		for (i=0; i<destSquare.length; i++)
		{
			if (destSquare[i]==0)
				continue;

			if (destSquare[i] == mouseStartSquare)
			{	//	piece dropped (touch-move rule not enforced!)
				sprite1.dropTo(mouseStartSquare, board.pieceAt(mouseStartSquare), 100,FPS,true);
				break;
			}

			if (tryMouseMove(mouseStartSquare,destSquare[i],promoPiece))
				break;
		}

		/*	illegal move	*/
		if (i >= destSquare.length) {
			Sound.play("sound.error");
			sprite1.dropTo(mouseStartSquare, board.pieceAt(mouseStartSquare), 500,FPS,true);
		}

		if (!promoPopupShowing) {
			mouseStartSquare = 0;
			//mouseClickSquare = 0;
			mouseMove = null;
		}
	}

	protected class Board2DSprite extends Sprite
	{
		/**	source, start square	*/
		int src;
		/**	moving piece	*/
		int pc;
		/**	destination square (may be 0)	*/
		int dst;
		/**	destination piece (may be different from moving piece)	*/
		int dstpc;
		/** hint move   */
		Move hint;
		/** HACK clear original square after drop ? */
		boolean clearOnDrop = true;

		Board2DSprite(BufferedImage background)
		{
			Rectangle imgBounds = new Rectangle();
			BufferedImage img = getPieceImage(WHITE_QUEEN,imgBounds,false);

			init(background,getGraphics2D(), BoardView2D.this.getBounds(),
				img, new Point(0,0), imgBounds.x, imgBounds.y);
		}

		public boolean isMoving()		{ return src!=0; }

		public void drop()
		{
			if (isAnimating()) {
				finishAnimation();  //  calls onAnimationFinis(), drop(), again !!
				return;
			}
			if (src!=0) {
				if (src!=dst) {
					if (clearOnDrop) BoardView2D.this.set(EMPTY,src);
//					BoardView2D.this.set(board.pieceAt(src),src);
					//  TODO This line was removed for a reason, but why ?
					BoardView2D.this.paintImmediate(getGraphics2D(),src);
				}
				src = 0;
				pc = 0;
			}
			if (dst!=0) {
				BoardView2D.this.set(dstpc,dst);
				BoardView2D.this.paintImmediate(getGraphics2D(),dst);
				dst = 0;
				dstpc = 0;
			}
		}

		public void animate(int startSquare, int endSquare,
							int movingPiece, int destPiece,
							long duration, int frameRate)
		{
			pickUp(startSquare,movingPiece);
			dropTo(endSquare,destPiece, duration,frameRate,true);
		}

		public boolean isPickedUp()
		{
			return pc!=0;
		}

		public void pickUp(int startSquare, int movingPiece)
		{
			drop();

			src = startSquare;
			pc = movingPiece;

			dst = 0;
			dstpc = 0;

			set(EMPTY,startSquare);

			Rectangle imgBounds = new Rectangle();
			BufferedImage img = getPieceImage(pc,imgBounds,false);

			Point2D orig2d = origin(src,false);
			Point orig = new Point((int)orig2d.getX(),(int)orig2d.getY());
			init(buffer,getGraphics2D(), BoardView2D.this.getBounds(),
					img, orig, imgBounds.x, imgBounds.y);
		}

		public void dropTo(Move mv, int destPiece, long duration, int frameRate, boolean clear)
		{
			hint = mv;
			switch (mv.castlingMask())
			{
			case WHITE_KINGS_CASTLING:
				dropTo(G1, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case WHITE_QUEENS_CASTLING:
				dropTo(C1, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case BLACK_KINGS_CASTLING:
				dropTo(G8, destPiece,duration,frameRate,clear);  //  FRC
				break;
			case BLACK_QUEENS_CASTLING:
				dropTo(C8, destPiece,duration,frameRate,clear);  //  FRC
				break;
			default:
				dropTo(mv.to, destPiece,duration,frameRate,clear);
				break;
			}
		}

		public void dropTo(int endSquare, int destPiece, long duration, int frameRate, boolean clear)
		{
			dst = endSquare;
			dstpc = destPiece;
			clearOnDrop = clear;

			Point2D orig2d = origin(dst,false);
			Point orig = new Point((int)orig2d.getX(),(int)orig2d.getY());
			moveTo(orig, duration,frameRate);
		}


		/**
		 * call back when animation is finished
		 */
		public void onAnimationFinish()
		{
			drop();
		}
	}

	//-------------------------------------------------------------------------------
	//	Interface MouseMotionListener
	//-------------------------------------------------------------------------------

	public void mouseDragged(MouseEvent e)
	{
		/*	drag along	*/
		if ((mouseStartSquare!=0) && sprite1.isMoving()) {
			Point pt = e.getPoint();
			Point2D.Double diff = new Point2D.Double(
					pt.x - mouseStartPoint.x,
					pt.y - mouseStartPoint.y);
			//	(dx,dy) in user-space coordinates.

			//	Sprite lives in device-scpace coordinates
			Point2D orig = origin(mouseStartSquare,false);
			getGraphics2D().getTransform().transform(diff,diff);

			sprite1.moveTo(
					(int)(orig.getX()+diff.getX()),
					(int)(orig.getY()+diff.getY()));
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		mouseMovement(e.getPoint());
	}

	public void mouseMovement(Point p)
	{
		int square = findSquare(p,true);

		if (board.canMove(square))
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		else
			setCursor(Cursor.getDefaultCursor());
	}


}
