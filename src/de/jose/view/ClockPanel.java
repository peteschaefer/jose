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
import de.jose.chess.Clock;
import de.jose.chess.Constants;
import de.jose.chess.EngUtil;
import de.jose.comm.Command;
import de.jose.comm.CommandAction;
import de.jose.comm.msg.MessageListener;
import de.jose.image.ImgUtil;
import de.jose.image.Surface;
import de.jose.image.TextureCache;
import de.jose.profile.LayoutProfile;
import de.jose.window.JoMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClockPanel
		extends JoPanel
		implements Constants, MessageListener
{
	//-------------------------------------------------------------------------------
	//	Constants
	//-------------------------------------------------------------------------------

	/**	draw analog clock	 */
	public static final int ANALOG	= 0x01;
	/**	draw digital clock	 */
	public static final int DIGITAL	= 0x02;
	/**	draw both	 */
	public static final int BOTH	= 0x03;

	/**	show seconds if below 20 minutes	 */
	public static final long SHOW_SECONDS = 20*MINUTE;

	/**	output format for digital clock	 */
	protected static SimpleDateFormat[] DIGITAL_FORMATS = {
		new SimpleDateFormat("HH:mm"),	//	hours + minutes
		new SimpleDateFormat("HH mm"),
		new SimpleDateFormat("mm.ss"),	//	minutes + seconds
	};

	static {
		Calendar cal = new GregorianCalendar(new SimpleTimeZone(0,"don't care"));
		for (int i=0; i<DIGITAL_FORMATS.length; i++)
			DIGITAL_FORMATS[i].setCalendar(cal);
	}

	/**	angle constants	 */
	public static final double FULL_CIRCLE	= Math.PI*2;

	public static final Color SHADOW_64 = new Color(0,0,0,64);
	public static final Color SHADOW_96 = new Color(0,0,0,96);

    protected Icon WHITE_ICON  = null;
    protected Icon BLACK_ICON  = null;
    protected Icon OFF_ICON  = null;

	//-------------------------------------------------------------------------------
	//	Fields
	//-------------------------------------------------------------------------------

  	protected Clock theClock;

	/**	font for digial display	 */
	protected static Font digitalFont = new Font("SansSerif", Font.PLAIN, 100);

	protected int textHeight;
	protected float textAspectRatio = 0.0f;

    /** bounding box for white clock */
    protected Rectangle whiteBox;
    protected Rectangle blackBox;

	//-------------------------------------------------------------------------------
	//	Constructors
	//-------------------------------------------------------------------------------

	public ClockPanel(LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		this(Application.theApplication.theClock, profile, withContextMenu, withBorder);
	}

	public ClockPanel(Clock clock, LayoutProfile profile, boolean withContextMenu, boolean withBorder)
	{
		super(profile,withContextMenu,withBorder);
		theClock = clock;
		setOpaque(true);
		setFocusable(false);    //  don't request keyboard focus
	}

	/**	called when first shown	 */
	public void init()
	{
		theClock.addMessageListener(this);
		//setDoubleBuffered(false);	//	only for debugging
        whiteBox = calcBox(WHITE);
        blackBox = calcBox(BLACK);
	}

    public void reshape(int x, int y, int width, int height)
    {
        super.reshape(x,y,width,height);

        whiteBox = calcBox(WHITE);
        blackBox = calcBox(BLACK);
    }

	//-------------------------------------------------------------------------------
	//	Methods
	//-------------------------------------------------------------------------------

	protected Icon getWhiteIcon() {
		if (WHITE_ICON==null) WHITE_ICON = JoToolBar.create1AwesomeIcon("\uf111:#ffffff:#000000",32);
		return WHITE_ICON;
	}

	protected Icon getBlackIcon() {
		if (BLACK_ICON==null) BLACK_ICON = JoToolBar.create1AwesomeIcon("\uf111:#000000:#ffffff",32);
		return BLACK_ICON;
	}

	protected Icon getOffIcon() {
		if (OFF_ICON==null) OFF_ICON = JoToolBar.create1AwesomeIcon("\uf111:#808080",32);
		return OFF_ICON;
	}
/*
	protected void finalize()
		throws Throwable
	{
		theClock.removeMessageListener(this);
		super.finalize();
	}
*/
	public final int getDisplayMode()			{
		return AbstractApplication.theUserProfile.getInt("clock.display");
	}

	public final Surface getBackgroundSurface() {
		return (Surface)AbstractApplication.theUserProfile.get("board.surface.background");
	}

	public final void setDisplayMode(int mod)	{
		AbstractApplication.theUserProfile.set("clock.display",mod);

        whiteBox = calcBox(WHITE);
        blackBox = calcBox(BLACK);
	}

	public void handleMessage(Object who, int what, Object data)
    {
        if (who==theClock)
            switch (what) {
            case Clock.EVENT_STATE:
                  repaint(); break;
            case Clock.EVENT_UPDATE_CURRENT:
                  repaint(theClock.getCurrent()==WHITE ? whiteBox:blackBox); break;
			case Clock.EVENT_UPDATE_BOTH:
				  repaint(whiteBox.union(blackBox)); break;
            }
	}


	protected Rectangle calcBox(int color)
	{
		int width, height;
		int sizex, sizey;
		Rectangle box;
		float aspectRatio = (float)getHeight() / getWidth();
		boolean top_down;
		boolean flipped = AbstractApplication.theUserProfile.getBoolean("board.flip");

		switch (getDisplayMode()) {
		default:
		case BOTH:
			//	fall-through intended
		case ANALOG:
			top_down = aspectRatio > 1.0;

			if (top_down) {
				width = getWidth();
				height = getHeight()/2;
			} else {
				width = getWidth()/2;
				height = getHeight();
			}

			sizex = sizey = Math.min(width,height);

			if (top_down)
				box = new Rectangle((width-sizex)/2, height-sizey, sizex,sizey);
			else
				box = new Rectangle(width-sizex, (height-sizey)/2, sizex,sizey);
			ViewUtil.inset(box,0.05f,0.05f);

			if (flipped == EngUtil.isBlack(color)) {
				if (top_down)
					box.y += sizey;
				else
					box.x += sizex;
			}
			break;

		case DIGITAL:
			if (textAspectRatio==0.0) {
				Graphics g = getGraphics();	//	any graphics context will do
				if (g!=null) {
					FontMetrics fmx = g.getFontMetrics(digitalFont);
					textHeight = fmx.getAscent()-fmx.getLeading()-fmx.getDescent();
					textAspectRatio = ((float)textHeight) / fmx.stringWidth("00:00");
				}
			}

			top_down = aspectRatio > textAspectRatio;

			if (top_down) {
				width = getWidth();
				height = Math.min(getHeight()/2, (int)(width*textAspectRatio));
				box = new Rectangle(0, (getHeight()/2-height)/2, width, height);
			}
			else {
				width = getWidth()/2;
				height = Math.min(getHeight(), (int)(width*textAspectRatio));
				box = new Rectangle(0, (getHeight()-height)/2, width, height);
			}
			ViewUtil.inset(box,0.05f,0.05f);

			if (flipped == EngUtil.isBlack(color)) {
				if (top_down)
					box.y += getHeight()/2;
				else
					box.x += width;
			}
			break;
		}
		return box;
	}

	/**
	 *	paint the clocks
	 * */
	protected void paintComponent(Graphics g0)
	{
		Graphics2D g = (Graphics2D)g0;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Surface srf = getBackgroundSurface();
		Rectangle r = g.getClipBounds();
		if (srf.useTexture()) {
			Image txtr = TextureCache.getTexture(srf.texture, TextureCache.LEVEL_MAX);
			TextureCache.paintTexture(g, r.x,r.y,r.width,r.height, txtr);
		}
		else {
			g.setPaint(srf.getPaint(r.width,r.height));
			g.fillRect(r.x,r.y,r.width,r.height);
		}

		switch (getDisplayMode()) {
		default:
		case BOTH:
			//	fall-through intended
		case ANALOG:
			drawAnalogClock(g,whiteBox, theClock.getWhiteTime(), WHITE, theClock.getCurrent()==WHITE);
			drawAnalogClock(g,blackBox, theClock.getBlackTime(), BLACK, theClock.getCurrent()==BLACK);
			break;

		case DIGITAL:
			drawDigitalClock(g,whiteBox, theClock.getWhiteTime(), WHITE, theClock.getCurrent()==WHITE);
			drawDigitalClock(g,blackBox, theClock.getBlackTime(), BLACK, theClock.getCurrent()==BLACK);
			break;
		}
	}

	public static void drawAnalogBackground(Graphics2D g, Rectangle box, boolean active)
	{
		Point center = ViewUtil.center(box);
		AffineTransform oldTransform = g.getTransform();

		AffineTransform baseTransform = (AffineTransform)oldTransform.clone();
		baseTransform.translate(center.x,center.y);
		baseTransform.scale(box.width/100.0, box.height/100.0);

		AffineTransform atf;

		/*	background	*/
		atf = (AffineTransform)baseTransform.clone();
		atf.scale(2.0, 2.0);
		g.setTransform(atf);

		g.setColor(Color.white);
		g.fillOval(-25,-25,50,50);

		g.setColor(active ? Color.black:Color.lightGray);
		//g.setColor(SHADOW_64);
		g.drawOval(-25,-25,50,50);
		//g.setColor(active ? Color.black:Color.lightGray);

		for (int i=0; i<60; i++) {
			double angle = FULL_CIRCLE/60*i;
			atf = (AffineTransform)baseTransform.clone();
			atf.rotate(angle);
			g.setTransform(atf);
			g.drawLine(0,49, 0, ((i%5)==0) ? 44:47);
		}
		/*	digits	*/
		Font f = digitalFont.deriveFont(Font.PLAIN, 8);
		g.setFont(f);

		atf = (AffineTransform)baseTransform.clone();
		g.setTransform(atf);

		for (int i=1; i<=12; i++) {
			double angle = FULL_CIRCLE/12*i;
			Point p = new Point(0,-40);
			p = ViewUtil.rotate(p, angle);
			ViewUtil.drawCentered(g, Integer.toString(i), p);
		}

		g.setTransform(oldTransform);
	}

	protected static final int[] hourX = { 2,   3,   0,  -3, -2, };
	protected static final int[] hourY = { 2, -26, -36, -26,  2, };

	protected static final int[] minuteX = { 2,   2,   0,  -2, -2, };
	protected static final int[] minuteY = { 2, -30, -43, -30,  2, };


    public static float[] getHourHandCoords(float scale, float z)
    {
       float[] result = new float[hourX.length*3];
       for (int i=0; i<hourX.length; i++) {
           result[3*i] = (float)hourX[i]*scale/100f;
           result[3*i+1] = (float)hourY[i]*scale/100f;
           result[3*i+2] = z;
       }
       return result;
    }


    public static float[] getMinuteHandCoords(float scale, float z)
    {
        float[] result = new float[minuteX.length*3];
        for (int i=0; i<minuteX.length; i++) {
            result[3*i] = (float)minuteX[i]*scale/100f;
            result[3*i+1] = (float)minuteY[i]*scale/100f;
            result[3*i+2] = z;
        }
        return result;
     }

	public void drawAnalogClock(Graphics2D g, Rectangle box, long time, int state, boolean active)
	{
		if (!ViewUtil.hitClip(g,box)) return;

        /*  icon    */
          Icon icon;
          if (!active)
              icon = getOffIcon();
          else if (EngUtil.isWhite(state))
              icon = getWhiteIcon();
          else
              icon = getBlackIcon();
          icon.paintIcon(this,g, box.x, box.y+box.height-icon.getIconHeight());

//		time -= time%SECOND;	//	no use to show fractions of a second
		drawAnalogBackground(g, box, active);

  		Point center = ViewUtil.center(box);
		AffineTransform oldTransform = g.getTransform();

		AffineTransform baseTransform = (AffineTransform)oldTransform.clone();
		baseTransform.translate(center.x,center.y);
		baseTransform.scale(box.width/100.0, box.height/100.0);

		AffineTransform atf;

		/*	hands	*/
		double hourAngle = (time%HOUR12)*FULL_CIRCLE/HOUR12;
		double minuteAngle = (time%HOUR)*FULL_CIRCLE/HOUR;
		double secondAngle = (time%MINUTE)*FULL_CIRCLE/MINUTE;

		/*	hour	*/
		atf = (AffineTransform)baseTransform.clone();
		atf.rotate(-hourAngle+FULL_CIRCLE/2);
		g.setTransform(atf);
		g.setColor(active ? Color.black : Color.lightGray);
		g.fillPolygon(hourX, hourY, hourX.length);

		/*	minute	*/
		atf = (AffineTransform)baseTransform.clone();
		atf.rotate(-minuteAngle);
		g.setTransform(atf);
		g.fillPolygon(minuteX, minuteY, minuteX.length);

		/*	flag	*/
/*		Point flagCenter = ViewUtil.rotate(new Point(0,96), -FULL_CIRCLE/12);
		double startAngle = FULLCIRCLE/2;
		double endAngle = 5*FULL_CIRCLE/24;
		double flagAngle;
		long t = time%HOUR;
		if (t<0 || t > 5*MINUTE)
			flagAngle = startAngle;
		else
			flagAngle = startAngle- (endAngle - startAngle) * t/(5*MINUTE);
*/
		/*	second	*/
		if (time <= SHOW_SECONDS) {
			atf = (AffineTransform)baseTransform.clone();
			atf.scale(0.5, 0.5);
			atf.rotate(-secondAngle);
			g.setTransform(atf);
			g.setColor(active ? Color.red : Color.lightGray);
			g.drawLine(0,0,0,-90);
		}

		g.setTransform(oldTransform);
	}

	public void drawDigitalClock(Graphics2D g, Rectangle box, long time, int state, boolean active)
	{
		if (!ViewUtil.hitClip(g,box)) return;

        /*  icon    */
        Icon icon;
        if (!active)
            icon = getOffIcon();
        else if (EngUtil.isWhite(state))
            icon = getWhiteIcon();
        else
            icon = getBlackIcon();
        icon.paintIcon(this,g, box.x,box.y+box.height-icon.getIconHeight());

        Point center = ViewUtil.center(box);
        AffineTransform oldTransform = g.getTransform();

        AffineTransform baseTransform = (AffineTransform)oldTransform.clone();
        baseTransform.translate(center.x,center.y);
        baseTransform.scale(box.height/100.0, box.height/100.0);
        g.setTransform(baseTransform);

		Font f = digitalFont.deriveFont(Font.PLAIN, 100/*box.height*digitalFont.getSize()/textHeight*/);
		g.setFont(f);

		String text = toString(time);

		Point p = new Point(0,0);   // ViewUtil.center(box);
		if (active) {
/*			g.setColor(SHADOW_64);
			ViewUtil.drawCentered(g, text, p.x+box.height/24, p.y+box.height/24);
*/
			g.setColor(Color.black);
			ViewUtil.drawCentered(g, text, p);
		}
		else {
			g.setColor(SHADOW_96);
			ViewUtil.drawCentered(g, text, p);
		}

        g.setTransform(oldTransform);
	}

	public static final String toString(long time)
	{
		if (time < 0)
			return "--.--";

		long mod = (time%1000);
		if (mod > 500)
			time += 1000-mod;
		else
			time -= mod;

		int index;
		if (time > SHOW_SECONDS)
			index = (int)((time/1000)%2);
		else
			index = 2;
		return DIGITAL_FORMATS[index].format(new Date(time));
	}

	//-------------------------------------------------------------------------------
	//	Interface JoComponent
	//-------------------------------------------------------------------------------

	public void adjustContextMenu(Collection list, MouseEvent event)
	{
		super.adjustContextMenu(list,event);
		list.add(ContextMenu.SEPARATOR);
		list.add(Boolean.valueOf(getDisplayMode()==ANALOG));
		list.add("clock.mode.analog");
		list.add(Boolean.valueOf(getDisplayMode()==DIGITAL));
		list.add("clock.mode.digital");
//		list.add(new Boolean(getDisplayMode()==BOTH));
//		list.add("clock.mode.both");

		list.add(null);
		JoMenuBar.addTimeControlItems(list);

		list.add(null);
		list.add("menu.edit.option");
		list.add(3);
	}

	public void setupActionMap(Map<String, CommandAction> map)
	{
		super.setupActionMap(map);

		CommandAction action;
		action = new CommandAction() {
			public void Do(Command cmd) {
				setDisplayMode(ANALOG);
				repaint();
			}
		};
		map.put("clock.mode.analog", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				setDisplayMode(DIGITAL);
				repaint();
			}
		};
		map.put("clock.mode.digital", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				setDisplayMode(BOTH);
				repaint();
			}
		};
		map.put("clock.mode.both", action);

		action = new CommandAction() {
			public void Do(Command cmd) {
				repaint();
			}
		};
		map.put("broadcast.board.flip", action);

	}


}
