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

package de.jose.image;

import de.jose.view.ViewUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static de.jose.image.ImgUtil.IDENTITY;

public class Sprite
		implements ActionListener
{
	protected BufferedImage theBackground;
	protected Graphics2D	backg;
	protected int bufWidth, bufHeight;

	protected BufferedImage	theSprite;
	protected Graphics2D	spriteg;

	protected BufferedImage	theBuffer;
	protected Graphics2D	bufferg;

	protected Graphics2D	screeng;
	//protected Rectangle		screenBounds;

	protected Point			current;
	protected Point			offset;
	protected Animation		anim;

	protected Sprite()
	{ }
/*
	public Sprite(Graphics2D screen, Rectangle bounds,
				  BufferedImage sprite, Point start,
				  int offx, int offy)
	{
		this (null, screen, bounds, sprite, start, offx, offy);
	}

	public Sprite(BufferedImage background,
				  Graphics2D screen, Rectangle bounds,
				  BufferedImage sprite, Point start,
				  int offx, int offy)
	{
		init(background, screen,bounds,sprite,start, offx,offy);
	}
*/
	public void init(BufferedImage background,
				  Graphics2D screen, Rectangle bounds,
				  BufferedImage sprite, Point start,
				  int offx, int offy)
	{
		/*	screen	*/
		screeng = screen;
		/* note: bounds in user-space coordinates.
			but all painting happens in device-space coodinates
		 */
		// screenBounds = bounds;
/*		AffineTransform tf = screen.getTransform();
		tf.transform(bounds.getLocation(), screenBounds.getLocation());
		Point2D screenSize = new Point2D.Double(bounds.getWidth(), bounds.getHeight());
		tf.transform(screenSize,screenSize);
		screenBounds.setSize((int)screenSize.getX(),(int)screenSize.getY());
		//	s.t. screenBounds == background-bounds in device-space coordinates
		//	todo user theBackground.getWidth() instead
*/
		/*	sprite	*/
		theSprite = sprite;
		spriteg = (Graphics2D)sprite.getGraphics();

		bufWidth = sprite.getWidth();
		bufHeight = sprite.getHeight();

		if ((theBuffer==null) ||
			(theBuffer.getWidth()<bufWidth) ||
			(theBuffer.getHeight()<bufHeight))
		{
			theBuffer = spriteg.getDeviceConfiguration().createCompatibleImage(bufWidth,bufHeight);
			bufferg = (Graphics2D)theBuffer.getGraphics();
		}

		/*	background	*/
		theBackground = background;
		backg = (Graphics2D)theBackground.getGraphics();

		current = start;
		offset = new Point(offx,offy);	//	user-space, dev-space ?

		updateBuffer();
	}

	public void resetBackground(BufferedImage background)
	{
		/*	background	*/
		theBackground = background;
		backg = (Graphics2D)theBackground.getGraphics();

		updateBuffer();
	}

	public final void paint()
	{
		paint(current.x, current.y, screeng);
	}

	public final void paint(Graphics2D g)
	{
		paint(current.x, current.y, g);
	}

	public final Rectangle getCurrentBounds()
	{
		return getBounds(current.x, current.y);
	}

	public final Rectangle getBounds(int x, int y)
	{
		int bx = x+offset.x;
		int by = y+offset.y;
		int bwidth = Math.min(bufWidth,theBackground.getWidth()-bx);
		int bheight = Math.min(bufHeight,theBackground.getHeight()-by);
		return new Rectangle(bx,by, bwidth,bheight);
	}

	public final Point getCenter()
	{
		return ViewUtil.center(getCurrentBounds());
	}

	public final void paint(int x, int y)
	{
		paint(x,y,screeng);
	}

	public void paint(int x, int y, Graphics2D g)
	{
		//	restore background to buffer
		Rectangle r = getBounds(x,y);
		ImgUtil.copy(theBackground, r.x, r.y, bufferg, 0,0, r.width, r.height);

		//	paint sprite into buffer
		ImgUtil.copy(theSprite, bufferg);
		//	show buffer on screen
		AffineTransform save_tf = g.getTransform();
		try {
			g.setTransform(IDENTITY);
			ImgUtil.copy(theBuffer, 0,0, g, r.x,r.y, r.width,r.height);
		} finally {
			g.setTransform(save_tf);
		}
	}

	public void updateBuffer()
	{
		//	restore background to buffer
		Rectangle r = getBounds(current.x,current.y);
		ImgUtil.copy(theBackground, r.x, r.y, bufferg, 0,0, r.width, r.height);
	}


	public void hide()
	{
		Rectangle r = getBounds(current.x,current.y);
		//	todo reset g.transform
		AffineTransform save_tf = screeng.getTransform();
		try {
			screeng.setTransform(IDENTITY);
			ImgUtil.copy(theBackground, r.x, r.y, screeng, r.x, r.y, r.width, r.height);
		} finally {
			screeng.setTransform(save_tf);
		}
	}

	public boolean moveTo(int x, int y)
	{
		if (x == current.x && y == current.y)
			return false;

		/*	restore exposed area	*/
		Rectangle r1 = new Rectangle();
		Rectangle r2 = new Rectangle();

		ImgUtil.computeExposedArea(
			new Rectangle(current.x, current.y, theSprite.getWidth(), theSprite.getHeight()),
			new Rectangle(x,y, theSprite.getWidth(), theSprite.getHeight()),
			r1, r2);

		paint(current.x = x, current.y = y);

		AffineTransform save_tf = screeng.getTransform();
		try {
			screeng.setTransform(IDENTITY);

			int screenWidth = theBackground.getWidth();
			int screenHeight = theBackground.getHeight();
			//	screen size in device-space coardinates

			r1.x += offset.x;
			r1.y += offset.y;
			r1.width = Math.min(r1.width, screenWidth - r1.x);
			r1.height = Math.min(r1.height, screenHeight - r1.y);
			if (r1.width > 0 && r1.height > 0)
				ImgUtil.copy(theBackground, r1.x, r1.y,
						screeng, r1.x, r1.y,
						r1.width, r1.height);
			//screeng.drawRect(r1.x,r1.y,r1.width,r1.height);
			//System.out.println(r1.x+","+r1.y+","+r1.width+","+r1.height);

			r2.x += offset.x;
			r2.y += offset.y;
			r2.width = Math.min(r2.width, screenWidth - r2.x);
			r2.height = Math.min(r2.height, screenHeight - r2.y);
			if (r2.width > 0 && r2.height > 0)
				ImgUtil.copy(theBackground, r2.x, r2.y,
						screeng, r2.x, r2.y,
						r2.width, r2.height);
		} finally {
			screeng.setTransform(save_tf);
		}
		//screeng.drawRect(r2.x,r2.y,r2.width,r2.height);
		//System.out.println(r2.x+","+r2.y+","+r2.width+","+r2.height);
		return true;
	}

	public final boolean moveTo(Point p) {
		return moveTo(p.x,p.y);
	}

	public final void moveTo(int x, int y, long time, int fps)	{
		moveTo(new Point(x,y), time, fps);
	}

	public void moveTo(Point dest, long duration, int fps)
	{
		finishAnimation();

		if (fps <= 0) {
			//	animate synchroneously
			anim = new Animation(dest,duration,0);
			anim.run();
		}
		else {
			int delay = (int)Math.round(1000.0/fps);
			anim = new Animation(dest,duration,delay);
			anim.start();
		}
	}


	public boolean isAnimating()
	{
		return anim!=null;
	}

	public void finishAnimation()
	{
		if (anim!=null) anim.shortcut();
		anim = null;
	}

	/**	called when the animation finishes	*/
	public void onAnimationFinish()
	{
		/**	overwrite, if you like	*/
	}

	/**	Timer call back	*/
	public void actionPerformed(ActionEvent e)
	{
		Animation a = (Animation)e.getSource();
		a.step();
	}

	protected static int	steps = 0;
	protected static int	paints = 0;

	private class Animation extends Timer
	{
		protected Point2D.Double	startPoint;
		protected Point				endPoint;
		protected Point2D.Double	diff;

		protected double		startTime;
		protected double		duration;
		protected long			endTime;

		Animation (Point p, long dur, int delay)
		{
			super(delay, Sprite.this);
			setRepeats(true);
			setCoalesce(true);

			startPoint = new Point2D.Double(current.x,current.y);
			endPoint = p;
			diff = new Point2D.Double(endPoint.x-startPoint.x, endPoint.y-startPoint.y);

			long now = System.currentTimeMillis();
			startTime = now;
			duration = dur;
			endTime = now+dur;
		}

		public void run()
		{
			while (step())	;
		}

		public void shortcut()
		{
			endTime = 0;
			step();
		}

		protected boolean step()
		{
			steps++;
			long now = System.currentTimeMillis();
			if (now >= endTime) {
				if (moveTo(endPoint)) paints++;
				stop();
				anim = null;
				onAnimationFinish();
//				System.out.println("steps="+steps+", paints="+paints);				
				return false;
			}
			else {
				double fract = ((double)now-startTime) / duration;
				int x = (int)Math.round(startPoint.x + fract*diff.x);
				int y = (int)Math.round(startPoint.y + fract*diff.y);

				if (moveTo(x,y)) paints++;
				return true;
			}
		}
	}
}
