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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.font.GlyphVector;
import java.awt.font.FontRenderContext;
import java.util.Map;
import java.text.AttributedCharacterIterator;

/**
 * @author Peter Schäfer
 */

public class DelegateGraphics2D
        extends Graphics2D
{
	protected Graphics2D g2;


	public DelegateGraphics2D()
	{
	}

	public DelegateGraphics2D(Graphics g)
	{
		setGraphics(g);
	}

	public void setGraphics(Graphics g)
	{
		g2 = (Graphics2D)g;
	}

	public void addRenderingHints(Map hints)
	{
		g2.addRenderingHints(hints);
	}


	public void clip(Shape s)
	{
		g2.clip(s);
	}

	public void draw(Shape s)
	{
		g2.draw(s);
	}

	public void drawGlyphVector(GlyphVector g, float x, float y)
	{
		g2.drawGlyphVector(g, x, y);
	}

	public void drawImage(BufferedImage img,
					BufferedImageOp op,
					int x,
					int y)
	{
		g2.drawImage(img, op, x, y);
	}

	public boolean drawImage(Image img,
	                                  AffineTransform xform,
	                                  ImageObserver obs)
	{
		return g2.drawImage(img, xform, obs);
	}

	public void drawRenderableImage(RenderableImage img,
	                                         AffineTransform xform)
	{
		g2.drawRenderableImage(img, xform);
	}

	public void drawRenderedImage(RenderedImage img,
	                                       AffineTransform xform)
	{
		g2.drawRenderedImage(img, xform);
	}

	public void drawString(AttributedCharacterIterator iterator,
	                                float x, float y)
	{
		g2.drawString(iterator, x, y);
	}

	public void drawString(String s, float x, float y)
	{
		g2.drawString(s, x, y);
	}

	public void fill(Shape s)
	{
		g2.fill(s);
	}

	public Color getBackground()
	{
		return g2.getBackground();
	}

	public Composite getComposite()
	{
		return g2.getComposite();
	}

	public GraphicsConfiguration getDeviceConfiguration()
	{
		return g2.getDeviceConfiguration();
	}

	public FontRenderContext getFontRenderContext()
	{
		return g2.getFontRenderContext();
	}

	public Paint getPaint()
	{
		return g2.getPaint();
	}

	public Object getRenderingHint(RenderingHints.Key hintKey)
	{
		return g2.getRenderingHint(hintKey);
	}

	public RenderingHints getRenderingHints()
	{
		return g2.getRenderingHints();
	}

	public Stroke getStroke()
	{
		return g2.getStroke();
	}

	public AffineTransform getTransform()
	{
		return g2.getTransform();
	}

	public boolean hit(Rectangle rect,
					Shape s,
					boolean onStroke)
	{
		return g2.hit(rect, s, onStroke);
	}

	public void rotate(double theta)
	{
		g2.rotate(theta);
	}

	public void rotate(double theta, double x, double y)
	{
		g2.rotate(theta, x, y);
	}

	public void scale(double sx, double sy)
	{
		g2.scale(sx, sy);
	}

	public void setBackground(Color color)
	{
		g2.setBackground(color);
	}

	public void setComposite(Composite comp)
	{
		g2.setComposite(comp);
	}

	public void setPaint( Paint paint )
	{
		g2.setPaint(paint);
	}

	public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue)
	{
		g2.setRenderingHint(hintKey, hintValue);
	}

	public void setRenderingHints(Map hints)
	{
		g2.setRenderingHints(hints);
	}

	public void setStroke(Stroke s)
	{
		g2.setStroke(s);
	}

	public void setTransform(AffineTransform Tx)
	{
		g2.setTransform(Tx);
	}

	public void shear(double shx, double shy)
	{
		g2.shear(shx, shy);
	}

	public void transform(AffineTransform Tx)
	{
		g2.transform(Tx);
	}

	public void translate(double tx, double ty)
	{
		g2.translate(tx, ty);
	}

	public void clearRect(int x, int y, int width, int height)
	{
		g2.clearRect(x, y, width, height);
	}

	public void clipRect(int x, int y, int width, int height)
	{
		g2.clipRect(x, y, width, height);
	}

	public void copyArea(int x, int y, int width, int height,
					int dx, int dy)
	{
		g2.copyArea(x, y, width, height, dx, dy);
	}

	public Graphics create()
	{
		return g2.create();
	}

	public Graphics create(int x, int y, int width, int height) {
		return g2.create(x, y, width, height);
	}

	public void dispose()
	{
		g2.dispose();
	}

	public void draw3DRect(int x, int y, int width, int height,
				boolean raised) {
		g2.draw3DRect(x, y, width, height, raised);
	}

	public void drawArc(int x, int y, int width, int height,
					int startAngle, int arcAngle)
	{
		g2.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	public void drawBytes(byte data[], int offset, int length, int x, int y) {
		g2.drawBytes(data, offset, length, x, y);
	}

	public void drawChars(char data[], int offset, int length, int x, int y) {
		g2.drawChars(data, offset, length, x, y);
	}

	public boolean drawImage(Image img,
					  int dx1, int dy1, int dx2, int dy2,
					  int sx1, int sy1, int sx2, int sy2,
					  ImageObserver observer)
	{
		return g2.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	public boolean drawImage(Image img,
					  int dx1, int dy1, int dx2, int dy2,
					  int sx1, int sy1, int sx2, int sy2,
					  Color bgcolor,
					  ImageObserver observer)
	{
		return g2.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}

	public boolean drawImage(Image img, int x, int y,
					  Color bgcolor,
					  ImageObserver observer)
	{
		return g2.drawImage(img, x, y, bgcolor, observer);
	}

	public boolean drawImage(Image img, int x, int y,
					  ImageObserver observer)
	{
		return g2.drawImage(img, x, y, observer);
	}

	public boolean drawImage(Image img, int x, int y,
					  int width, int height,
					  Color bgcolor,
					  ImageObserver observer)
	{
		return g2.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	public boolean drawImage(Image img, int x, int y,
					  int width, int height,
					  ImageObserver observer)
	{
		return g2.drawImage(img, x, y, width, height, observer);
	}

	public void drawLine(int x1, int y1, int x2, int y2)
	{
		g2.drawLine(x1, y1, x2, y2);
	}

	public void drawOval(int x, int y, int width, int height)
	{
		g2.drawOval(x, y, width, height);
	}

	public void drawPolygon(Polygon p) {
		g2.drawPolygon(p);
	}

	public void drawPolygon(int xPoints[], int yPoints[],
					 int nPoints)
	{
		g2.drawPolygon(xPoints, yPoints, nPoints);
	}

	public void drawPolyline(int xPoints[], int yPoints[],
					  int nPoints)
	{
		g2.drawPolyline(xPoints, yPoints, nPoints);
	}

	public void drawRect(int x, int y, int width, int height) {
		g2.drawRect(x, y, width, height);
	}

	public void drawRoundRect(int x, int y, int width, int height,
					   int arcWidth, int arcHeight)
	{
		g2.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void drawString(AttributedCharacterIterator iterator,
	                                int x, int y)
	{
		g2.drawString(iterator, x, y);
	}

	public void drawString(String str, int x, int y)
	{
		g2.drawString(str, x, y);
	}

	public void fill3DRect(int x, int y, int width, int height,
				boolean raised) {
		g2.fill3DRect(x, y, width, height, raised);
	}

	public void fillArc(int x, int y, int width, int height,
					int startAngle, int arcAngle)
	{
		g2.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	public void fillOval(int x, int y, int width, int height)
	{
		g2.fillOval(x, y, width, height);
	}

	public void fillPolygon(Polygon p) {
		g2.fillPolygon(p);
	}

	public void fillPolygon(int xPoints[], int yPoints[],
					 int nPoints)
	{
		g2.fillPolygon(xPoints, yPoints, nPoints);
	}

	public void fillRect(int x, int y, int width, int height)
	{
		g2.fillRect(x, y, width, height);
	}

	public void fillRoundRect(int x, int y, int width, int height,
					   int arcWidth, int arcHeight)
	{
		g2.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	public void finalize() {
		g2.finalize();
	}

	public Shape getClip()
	{
		return g2.getClip();
	}

	public Rectangle getClipBounds()
	{
		return g2.getClipBounds();
	}

	public Rectangle getClipBounds(Rectangle r) {
		return g2.getClipBounds(r);
	}

	public Rectangle getClipRect() {
		return g2.getClipRect();
	}

	public Color getColor()
	{
		return g2.getColor();
	}

	public Font getFont()
	{
		return g2.getFont();
	}

	public FontMetrics getFontMetrics() {
		return g2.getFontMetrics();
	}

	public FontMetrics getFontMetrics(Font f)
	{
		return g2.getFontMetrics(f);
	}

	public boolean hitClip(int x, int y, int width, int height) {
		return g2.hitClip(x, y, width, height);
	}

	public void setClip(Shape clip)
	{
		g2.setClip(clip);
	}

	public void setClip(int x, int y, int width, int height)
	{
		g2.setClip(x, y, width, height);
	}

	public void setColor(Color c)
	{
		g2.setColor(c);
	}

	public void setFont(Font font)
	{
		g2.setFont(font);
	}

	public void setPaintMode()
	{
		g2.setPaintMode();
	}

	public void setXORMode(Color c1)
	{
		g2.setXORMode(c1);
	}

	public String toString() {
		return g2.toString();
	}

	public void translate(int x, int y)
	{
		g2.translate(x, y);
	}
}
